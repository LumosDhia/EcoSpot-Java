package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class AbsoluteSeeder {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // Get valid binary IDs
            byte[] binId = null;
            ResultSet rs = cnx.createStatement().executeQuery("SELECT created_by_id FROM article WHERE created_by_id IS NOT NULL LIMIT 1");
            if (rs.next()) binId = rs.getBytes(1);
            
            String sql = "INSERT INTO article (title, content, created_at, published_at, views, category_id, writer_id, created_by_id, updated_by_id, slug, seo_title, seo_description, seo_keywords, image) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            LocalDateTime now = LocalDateTime.now();
            for (int h : new int[]{8, 12, 18}) {
                ps.setString(1, "Final Article @ " + h + ":00");
                ps.setString(2, "Content...");
                ps.setTimestamp(3, Timestamp.valueOf(now.withHour(h)));
                ps.setTimestamp(4, Timestamp.valueOf(now.withHour(h)));
                ps.setInt(5, 0);
                ps.setInt(6, 50); // Valid category ID
                ps.setBytes(7, binId);
                ps.setBytes(8, binId);
                ps.setBytes(9, binId);
                ps.setString(10, "final-" + h + "-" + System.currentTimeMillis());
                ps.setString(11, "SEO");
                ps.setString(12, "SEO");
                ps.setString(13, "SEO");
                ps.setString(14, "");
                
                ps.executeUpdate();
            }
            System.out.println("ABSOLUTE SEEDER FINISHED.");
        } catch (SQLException e) {
            System.err.println("ABSOLUTE SEEDER FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
