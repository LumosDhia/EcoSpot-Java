package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class SeedPublishedTodayTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // Find a valid user and category (assuming 1 exists as fallback)
            int userId = 1;
            int catId = 1;
            
            String sql = "INSERT INTO article (title, content, created_by_id, category_id, published_at, writer_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            String[] titles = {"Today's Eco Breakthrough", "Renewable Energy Update 2026", "Local Green Community News"};
            for (String t : titles) {
                ps.setString(1, t);
                ps.setString(2, "Content for " + t);
                ps.setInt(3, userId);
                ps.setInt(4, catId);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(6, userId);
                ps.executeUpdate();
            }
            
            System.out.println("Published 3 new articles with TODAY's timestamp.");
        } catch (SQLException e) {
            // Check if column names are different
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
