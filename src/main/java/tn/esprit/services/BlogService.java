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
    }

    @Override
    public void delete(Blog blog) {
        // Implementation for deleting
    }

    @Override
    public void update(Blog blog) {
        // Implementation for updating
    }

    @Override
    public List<Blog> getAll() {
        return search(""); // Default search all
    }

    public List<Blog> search(String query) {
        List<Blog> blogs = new ArrayList<>();
        // Note: Using 'article' table name based on Symfony standard mapping
        String req = "SELECT a.*, c.name as category_name FROM article a " +
                     "LEFT JOIN category c ON a.category_id = c.id " +
                     "WHERE a.title LIKE ? OR a.content LIKE ?";
        
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
                
                Timestamp ts = rs.getTimestamp("published_at");
                if (ts != null) {
                    b.setPublishedAt(ts.toLocalDateTime());
                } else {
                    b.setPublishedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                
                int catId = rs.getInt("category_id");
                if (!rs.wasNull()) {
                    b.setCategory(new Category(catId, rs.getString("category_name")));
                }
                
                b.setViews(rs.getInt("views"));
                // In a real app, we'd join reactions/comments, but using dummy for UI demo if needed
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
