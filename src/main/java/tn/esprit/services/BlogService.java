package tn.esprit.services;

import tn.esprit.blog.Blog;
import tn.esprit.blog.Category;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements GlobalInterface<Blog> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void add(Blog blog) {
        // Implementation for adding blog
    }

    @Override
    public void add2(Blog blog) {
        // Implementation for adding blog (PreparedStatement)
        String req = "INSERT INTO article (title, content, image, created_at, published_at, slug, created_by_id, writer_id, category_id, views) " +
                     "VALUES (?, ?, ?, NOW(), NOW(), ?, UNHEX(?), UNHEX(?), ?, 0)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage() != null ? blog.getImage() : "");
            
            // Slug from title
            String slug = blog.getTitle().toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
            ps.setString(4, slug + "-" + System.currentTimeMillis() % 1000); // Simple uniqueness
            
            // Use Admin ID as default for now
            String adminId = "019CA478D1D377B4AE80630614A4A0FD";
            ps.setString(5, adminId);
            ps.setString(6, adminId);
            
            if (blog.getCategory() != null) {
                ps.setInt(7, blog.getCategory().getId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            
            ps.executeUpdate();
            System.out.println("Article added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Blog blog) {
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
        String req = "UPDATE article SET title = ?, content = ?, image = ?, category_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getImage());
            if (blog.getCategory() != null) {
                ps.setInt(4, blog.getCategory().getId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setInt(5, blog.getId());
            ps.executeUpdate();
            System.out.println("Article updated successfully!");
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
        // Updated query to join with app_user for author info
        String req = "SELECT a.*, c.name as category_name, u.firstname, u.roles " +
                     "FROM article a " +
                     "LEFT JOIN category c ON a.category_id = c.id " +
                     "LEFT JOIN app_user u ON a.created_by_id = u.id " +
                     "WHERE (a.title LIKE ? OR a.content LIKE ?)";
        
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setContent(rs.getString("content"));
                b.setImage(rs.getString("image"));
                
                // Roles for filtering logic in controllers
                String roles = rs.getString("roles");
                String authorName = rs.getString("firstname");
                if (authorName != null) {
                    // Prepend [ADMIN] if role contains ROLE_ADMIN for filtering
                    if (roles != null && roles.contains("ROLE_ADMIN")) {
                        b.setAuthor("Admin [" + authorName + "]");
                    } else {
                        b.setAuthor(authorName);
                    }
                } else {
                    b.setAuthor("Unknown Author");
                }
                
                Timestamp ts = rs.getTimestamp("published_at");
                if (ts != null) {
                    b.setPublishedAt(ts.toLocalDateTime());
                } else if (rs.getTimestamp("created_at") != null) {
                    b.setPublishedAt(rs.getTimestamp("created_at").toLocalDateTime());
                } else {
                    b.setPublishedAt(LocalDateTime.now());
                }
                
                int catId = rs.getInt("category_id");
                if (!rs.wasNull()) {
                    b.setCategory(new Category(catId, rs.getString("category_name")));
                }
                
                b.setViews(rs.getInt("views"));
                b.setLikesCount((int) (Math.random() * 50)); 
                b.setReadingTime((int) (Math.random() * 10) + 1);
                
                blogs.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blogs;
    }
}
