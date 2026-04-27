package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class StatisticsEngagementTest {

    private final StatisticsService service = new StatisticsService();
    private final Connection cnx = MyConnection.getInstance().getCnx();

    @Test
    public void testEngagementAggregation() throws SQLException {
        // 1. Clean up existing test data in the window to be sure
        LocalDate today = LocalDate.now();
        cnx.createStatement().execute("DELETE FROM article_reaction_event WHERE DATE(acted_at) = '" + java.sql.Date.valueOf(today) + "'");

        // 2. Seed fresh test data
        String sql = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            // 3 Likes today
            for (int i = 0; i < 3; i++) {
                ps.setInt(1, 1);
                ps.setInt(2, i + 100);
                ps.setString(3, "LIKE");
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
            // 2 Dislikes today
            for (int i = 0; i < 2; i++) {
                ps.setInt(1, 1);
                ps.setInt(2, i + 200);
                ps.setString(3, "DISLIKE");
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        }

        // 3. Test the service
        Map<String, Integer> reactions = service.getReactionsByPeriod(today, today, null);
        
        // 4. Assertions
        assertEquals(3, reactions.getOrDefault("LIKE", 0), "Should have 3 likes today");
        assertEquals(2, reactions.getOrDefault("DISLIKE", 0), "Should have 2 dislikes today");
        
        System.out.println("Test Passed: Engagement correctly aggregated (3 Likes, 2 Dislikes)");
    }
}
