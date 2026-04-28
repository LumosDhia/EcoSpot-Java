package tn.esprit.services;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class FinalEngagementCheckTest {
    @Test
    public void checkServiceOutput() {
        StatisticsService service = new StatisticsService();
        LocalDate today = LocalDate.now();
        Map<String, Integer> reactions = service.getReactionsByPeriod(today, today, null);
        
        System.out.println("Service Output for Today: " + reactions);
        int total = reactions.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total Engagement for Today: " + total);
        
        assertTrue(total >= 7, "Total engagement should be at least 7 based on seeding.");
    }
}
