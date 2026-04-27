package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

public class SeedRandomEngagementTest {
    @Test
    public void seedRandomData() throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random rand = new Random();
        
        // Clear all event data for a fresh demonstration
        cnx.createStatement().execute("DELETE FROM article_reaction_event");
        
        String sql = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            // Period Mapping: [daysAgo, count]
            int[][] setup = {
                {0, 15},  // Today: 15 events
                {1, 10},  // Yesterday: 10 events
                {5, 20},  // 5 days ago: 20 events
                {15, 30}, // 15 days ago: 30 events
                {40, 50}  // 40 days ago: 50 events
            };
            
            for (int[] period : setup) {
                int daysAgo = period[0];
                int count = period[1];
                LocalDateTime date = LocalDateTime.now().minusDays(daysAgo);
                
                for (int i = 0; i < count; i++) {
                    ps.setInt(1, 1); // Article ID 1
                    ps.setInt(2, rand.nextInt(1000)); // Random user
                    ps.setString(3, rand.nextBoolean() ? "LIKE" : "DISLIKE");
                    ps.setTimestamp(4, Timestamp.valueOf(date));
                    ps.executeUpdate();
                }
                System.out.println("Seeded " + count + " events for " + daysAgo + " days ago.");
            }
        }
    }
}
