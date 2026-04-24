package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class SeedRobustArticlesTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // Force status to 'PUBLISHED' or similar if needed
            String sql = "INSERT INTO article (title, content, created_by_id, category_id, published_at, writer_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            ps.setString(1, "URGENT: Eco News Today");
            ps.setString(2, "Content...");
            ps.setInt(3, 1);
            ps.setInt(4, 1);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(6, 1);
            ps.setString(7, "PUBLISHED"); // Just in case
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("No rows inserted into article table!");
            
            System.out.println("Published 1 ROBUST article with TODAY's timestamp.");
        } catch (SQLException e) {
            System.err.println("Robust seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
