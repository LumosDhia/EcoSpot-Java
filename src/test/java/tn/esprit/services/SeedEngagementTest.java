package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class SeedEngagementTest {
    @Test
    public void seedData() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // Seed some reactions for today and yesterday
            String sql = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            // Article ID 1, User ID 1, LIKE, Now
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.setString(3, "LIKE");
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            
            // Article ID 1, User ID 2, DISLIKE, Yesterday
            ps.setInt(1, 1);
            ps.setInt(2, 2);
            ps.setString(3, "DISLIKE");
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
            ps.executeUpdate();
            
            System.out.println("Seeded 2 engagement events successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
