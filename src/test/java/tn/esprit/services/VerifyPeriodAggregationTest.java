package tn.esprit.services;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Map;

public class VerifyPeriodAggregationTest {
    private final StatisticsService service = new StatisticsService();

    @Test
    public void verifyPeriods() {
        LocalDate end = LocalDate.now();
        
        System.out.println("--- ENGAGEMENT PERIOD VERIFICATION ---");
        
        // Today
        check("Today", end, end);
        
        // Last 7 Days
        check("Last 7 Days", end.minusDays(7), end);
        
        // Last 30 Days
        check("Last 30 Days", end.minusDays(30), end);
        
        // Last 12 Months
        check("Last 12 Months", end.minusMonths(11).withDayOfMonth(1), end);
    }

    private void check(String label, LocalDate start, LocalDate end) {
        Map<String, Integer> reactions = service.getReactionsByPeriod(start, end, null);
        int total = reactions.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println(label + " (" + start + " to " + end + "): " + total + " reactions");
    }
}
