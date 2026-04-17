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
import java.util.List;
import tn.esprit.blog.Tag;

public class BlogService implements GlobalInterface<Blog> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void add(Blog blog) {
        // Implementation for adding blog
    }

    @Override
    public void add2(Blog blog) {
        String req = "INSERT INTO article (title, content, image, created_at, published_at, slug, created_by_id, writer_id, category_id, views, admin_revision_note) " +
                     "VALUES (?, ?, ?, NOW(), ?, ?, UNHEX(?), UNHEX(?), ?, 0, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage() != null ? blog.getImage() : "");
            
            // Handle published_at based on isPublished flag
            if (blog.getIsPublished()) {
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            
            // Slug from title
            String slug = blog.getTitle().toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
            ps.setString(5, slug + "-" + System.currentTimeMillis() % 1000); // Simple uniqueness
            
            String appUserIdHex = resolveCurrentAppUserIdHex();
            if (appUserIdHex == null || appUserIdHex.isBlank()) {
                throw new IllegalStateException("No valid app_user ID found for the current session user.");
            }
            ps.setString(6, appUserIdHex);
            ps.setString(7, appUserIdHex);
            
            if (blog.getCategory() != null) {
                ps.setInt(8, blog.getCategory().getId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.setString(9, blog.getAdminRevisionNote());
            
            ps.executeUpdate();
            
            // Get the generated ID
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int articleId = generatedKeys.getInt(1);
                saveTags(articleId, blog.getTags());
            }
            
            System.out.println("Article added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String resolveCurrentOwnerEmail() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        String matchedEmail = findAppUserEmailByEmail(currentUser.getEmail());
        if (matchedEmail != null) {
            return matchedEmail;
        }

        String mappedDemoEmail = mapDemoEmailToAppUserEmail(currentUser.getEmail());
        if (mappedDemoEmail != null) {
            return mappedDemoEmail;
        }

        return null;
    }

    private void saveTags(int articleId, List<Tag> tags) {
        // Delete existing tags
        String delReq = "DELETE FROM article_tag WHERE article_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(delReq)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        // Insert new tags
        if (tags != null && !tags.isEmpty()) {
            String insReq = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(insReq)) {
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
        // First delete tags
        saveTags(blog.getId(), null);
        
        String req = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, blog.getId());
            ps.executeUpdate();
            System.out.println("Article deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Blog blog) {
        String req = "UPDATE article SET title = ?, content = ?, image = ?, category_id = ?, published_at = ?, admin_revision_note = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage());
            if (blog.getCategory() != null) {
                ps.setInt(4, blog.getCategory().getId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            
            if (blog.getIsPublished()) {
                // If it was already published, keep old date or set new if null
                if (blog.getPublishedAt() != null) {
                    ps.setTimestamp(5, Timestamp.valueOf(blog.getPublishedAt()));
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                }
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            
            ps.setString(6, blog.getAdminRevisionNote());
            ps.setInt(7, blog.getId());
            ps.executeUpdate();
            
            saveTags(blog.getId(), blog.getTags());
            
            System.out.println("Article updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementViews(int articleId) {
        String req = "UPDATE article SET views = views + 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Blog> getAll() {
        return search(""); // Default search all
    }

    public List<Blog> search(String query) {
        List<Blog> blogs = new ArrayList<>();
        String req = "SELECT a.*, c.name as category_name, u.firstname, u.roles, u.email as author_email " +
                     "FROM article a " +
                     "LEFT JOIN category c ON a.category_id = c.id " +
                     "LEFT JOIN app_user u ON a.created_by_id = u.id " +
                     "WHERE a.title LIKE ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setContent(rs.getString("content"));
                b.setImage(rs.getString("image"));
                
                String authorName = rs.getString("firstname");
                String roles = rs.getString("roles");
                b.setCreatedByEmail(rs.getString("author_email"));
                if (authorName != null) {
                    if (roles != null && roles.contains("ROLE_ADMIN")) {
                        b.setAuthor("Admin [" + authorName + "]");
                    } else {
                        b.setAuthor(authorName);
                    }
                } else {
                    b.setAuthor("Admin");
                }
                
                Timestamp ts = rs.getTimestamp("published_at");
                if (ts != null) {
                    b.setPublishedAt(ts.toLocalDateTime());
                    b.setIsPublished(true);
                } else {
                    b.setIsPublished(false);
                    if (rs.getTimestamp("created_at") != null) {
                        b.setPublishedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    } else {
                        b.setPublishedAt(LocalDateTime.now());
                    }
                }
                
                int catId = rs.getInt("category_id");
                if (!rs.wasNull()) {
                    b.setCategory(new Category(catId, rs.getString("category_name")));
                }
                
                b.setViews(rs.getInt("views"));
                b.setReadingTime(b.getReadingTime());
                b.setAdminRevisionNote(rs.getString("admin_revision_note"));
                
                // Fetch tags and comments for this blog
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
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
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

    private String resolveCurrentAppUserIdHex() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String byEmail = findAppUserIdHexByEmail(currentUser.getEmail());
            if (byEmail != null) {
                return byEmail;
            }

            String mappedEmail = mapDemoEmailToAppUserEmail(currentUser.getEmail());
            if (mappedEmail != null) {
                return findAppUserIdHexByEmail(mappedEmail);
            }
        }
        return null;
    }

    private String findAppUserIdHexByEmail(String email) {
        String req = "SELECT HEX(id) AS id_hex FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id_hex");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String findAppUserEmailByEmail(String email) {
        String req = "SELECT email FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String mapDemoEmailToAppUserEmail(String email) {
        if (email == null || !email.endsWith("@mail.com")) {
            return null;
        }

        String localPart = email.substring(0, email.indexOf('@'));
        String candidate = localPart + "@ecospot.local";
        return findAppUserEmailByEmail(candidate);
    }
}
