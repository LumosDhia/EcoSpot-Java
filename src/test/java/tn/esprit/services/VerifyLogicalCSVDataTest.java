package tn.esprit.services;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class VerifyLogicalCSVDataTest {

    private final StatisticsService statsService = new StatisticsService();

    @Test
    public void testDetailedArticleStats() {
        java.time.LocalDate now = java.time.LocalDate.now();
        List<Map<String, Object>> stats = statsService.getDetailedArticleStats(now.minusDays(30), now, null); // Admin view
        
        assertNotNull(stats, "Stats list should not be null");
        assertFalse(stats.isEmpty(), "Stats list should not be empty (assuming seeded data)");
        
        Map<String, Object> first = stats.get(0);
        assertTrue(first.containsKey("id"), "Should contain article ID");
        assertTrue(first.containsKey("title"), "Should contain title");
        assertTrue(first.containsKey("views"), "Should contain views");
        assertTrue(first.containsKey("comments"), "Should contain comments");
        assertTrue(first.containsKey("likes"), "Should contain likes");
        assertTrue(first.containsKey("dislikes"), "Should contain dislikes");
        
        System.out.println("Verified Article Stats for: " + first.get("title"));
        System.out.println("Views: " + first.get("views"));
        System.out.println("Comments: " + first.get("comments"));
        System.out.println("Likes: " + first.get("likes"));
        System.out.println("Dislikes: " + first.get("dislikes"));
    }
}
