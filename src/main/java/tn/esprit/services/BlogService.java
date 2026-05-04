package tn.esprit.services;

import tn.esprit.blog.Blog;
import tn.esprit.blog.Category;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import tn.esprit.blog.Tag;

public class BlogService implements GlobalInterface<Blog> {

    private Connection getCnx() {
        Connection c = MyConnection.getInstance().getCnx();
        if (c == null) {
            throw new RuntimeException("CRITICAL ERROR: Database connection is null. Is MySQL/MariaDB running?");
        }
        return c;
    }

    private static final Map<String, LocalDateTime> viewHistory = new HashMap<>();
    private static final int VIEW_COOLDOWN_MINUTES = 15;

    @Override
    public void add(Blog blog) {
        add2(blog);
    }

    @Override
    public void add2(Blog blog) {
        String req = "INSERT INTO article (title, content, image, created_at, published_at, slug, created_by_id, writer_id, category_id, views, admin_revision_note, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (PreparedStatement ps = getCnx().prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage() != null ? blog.getImage() : "");
            
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(4, Timestamp.valueOf(now));

            if (blog.getIsPublished()) {
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            String slug = blog.getTitle().toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
            slug = slug + "-" + System.currentTimeMillis() % 1000;
            blog.setSlug(slug);
            ps.setString(6, slug);

            int userId = resolveCurrentUserId();
            if (userId <= 0) {
                throw new IllegalStateException("No valid user session found.");
            }
            ps.setInt(7, userId);
            ps.setInt(8, userId);

            if (blog.getCategory() != null) {
                ps.setInt(9, blog.getCategory().getId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setString(10, blog.getAdminRevisionNote());
            ps.setString(11, blog.getIsPublished() ? "published" : "draft");

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int articleId = generatedKeys.getInt(1);
                blog.setId(articleId);
                saveTags(articleId, blog.getTags());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String resolveCurrentOwnerEmail() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }

    private int resolveCurrentUserId() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getId() : -1;
    }

    private void saveTags(int articleId, List<Tag> tags) {
        String delReq = "DELETE FROM article_tag WHERE article_id = ?";
        try (PreparedStatement ps = getCnx().prepareStatement(delReq)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        if (tags != null && !tags.isEmpty()) {
            String insReq = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
            try (PreparedStatement ps = getCnx().prepareStatement(insReq)) {
                for (Tag tag : tags) {
                    ps.setInt(1, articleId);
                    ps.setInt(2, tag.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void delete(Blog blog) {
        saveTags(blog.getId(), null);

        String req = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, blog.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Blog blog) {
        String req = "UPDATE article SET title = ?, content = ?, image = ?, category_id = ?, published_at = ?, admin_revision_note = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage());
            if (blog.getCategory() != null) {
                ps.setInt(4, blog.getCategory().getId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (blog.getIsPublished()) {
                if (blog.getPublishedAt() != null) {
                    ps.setTimestamp(5, Timestamp.valueOf(blog.getPublishedAt()));
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                }
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setString(6, blog.getAdminRevisionNote());
            ps.setString(7, blog.getIsPublished() ? "published" : "draft");
            ps.setInt(8, blog.getId());
            ps.executeUpdate();

            saveTags(blog.getId(), blog.getTags());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean incrementViews(int articleId, String viewerId) {
        String cacheKey = viewerId + ":" + articleId;
        LocalDateTime lastView = viewHistory.get(cacheKey);
        LocalDateTime now = LocalDateTime.now();

        if (lastView != null && Duration.between(lastView, now).toMinutes() < VIEW_COOLDOWN_MINUTES) {
            return false;
        }

        String req = "UPDATE article SET views = views + 1 WHERE id = ?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
            viewHistory.put(cacheKey, now);

            tn.esprit.util.StatisticsCollector.getInstance().recordView(articleId, viewerId, null);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Blog> getAll() {
        return search("");
    }

    public List<Blog> search(String query) {
        List<Blog> blogs = new ArrayList<>();
        String req = "SELECT a.*, c.name as category_name, u.username, u.role, u.email as author_email " +
                     "FROM article a " +
                     "LEFT JOIN category c ON a.category_id = c.id " +
                     "LEFT JOIN user u ON a.created_by_id = u.id " +
                     "WHERE a.title LIKE ?";

        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setContent(rs.getString("content"));
                b.setImage(rs.getString("image"));
                b.setSlug(rs.getString("slug"));

                String authorName = rs.getString("username");
                String role = rs.getString("role");
                b.setCreatedByEmail(rs.getString("author_email"));
                if (authorName != null) {
                    b.setAuthor(authorName);
                } else {
                    b.setAuthor("Admin");
                }

                Timestamp ts = rs.getTimestamp("published_at");
                if (ts != null) {
                    b.setPublishedAt(ts.toLocalDateTime());
                    b.setIsPublished(true);
                } else {
                    b.setIsPublished(false);
                    Timestamp created = rs.getTimestamp("created_at");
                    b.setPublishedAt(created != null ? created.toLocalDateTime() : LocalDateTime.now());
                }

                int catId = rs.getInt("category_id");
                if (!rs.wasNull()) {
                    b.setCategory(new Category(catId, rs.getString("category_name")));
                }

                b.setViews(rs.getInt("views"));
                b.setReadingTime(b.getReadingTime());
                b.setAdminRevisionNote(rs.getString("admin_revision_note"));

                ReactionService rsrv = new ReactionService();
                b.setLikesCount(rsrv.getLikes(b.getId()));
                b.setDislikesCount(rsrv.getDislikes(b.getId()));

                b.setTags(getTagsForArticle(b.getId()));
                b.setComments(new CommentService().getByArticleId(b.getId()));
                b.setCommentsCount(b.getComments().size());

                blogs.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blogs;
    }

    private List<Tag> getTagsForArticle(int articleId) {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT t.* FROM tag t JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = ?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }
}
