package tn.esprit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class StatisticsServiceTest {

    private static StatisticsService statisticsService;

    @BeforeAll
    public static void setUp() {
        statisticsService = new StatisticsService();
    }

    @Test
    public void testGetTotalViews() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        // Updated to include ownerEmail parameter
        int views = statisticsService.getTotalViews(start, end, null);
        assertTrue(views >= 0, "Total views should be non-negative");
    }

    @Test
    public void testGetTotalPublishedArticles() {
        // Updated to include ownerEmail parameter
        int count = statisticsService.getTotalPublishedArticles(null);
        assertTrue(count >= 0, "Total published articles should be non-negative");
    }

    @Test
    public void testGetCategoryStats() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        // Updated to include period parameters
        List<Map<String, Object>> stats = statisticsService.getCategoryStats(start, end, null);
        assertNotNull(stats, "Category stats should not be null");
    }

    @Test
    public void testGetViewsTimeSeries() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        // Updated to include ownerEmail parameter
        List<Map<String, Object>> series = statisticsService.getViewsTimeSeries(start, end, null);
        assertNotNull(series, "Views time series should not be null");
    }

    @Test
    public void testGetTopArticlesByViews() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        // Updated to include ownerEmail parameter
        List<Map<String, Object>> top = statisticsService.getTopArticlesByViews(start, end, 5, null);
        assertNotNull(top, "Top articles list should not be null");
        assertTrue(top.size() <= 5, "List size should not exceed limit");
    }

    @Test
    public void testGetArticleStatusFunnel() {
        Map<String, Integer> funnel = statisticsService.getArticleStatusFunnel();
        assertNotNull(funnel, "Status funnel should not be null");
    }

    @Test
    public void testGetViewsByHourOfDay() {
        List<Map<String, Object>> hours = statisticsService.getViewsByHourOfDay(null);
        assertNotNull(hours, "Hourly views should not be null");
    }

    @Test
    public void testGetTopSearchTerms() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Map<String, Object>> searches = statisticsService.getTopSearchTerms(start, end, 5);
        assertNotNull(searches, "Search terms should not be null");
    }

    @Test
    public void testGetAuthorStats() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Map<String, Object>> authorStats = statisticsService.getAuthorStats(start, end);
        assertNotNull(authorStats, "Author stats should not be null");
    }

    @Test
    public void testGetArticleBasicStats() {
        // Test with ID 1 (assuming it exists or returns default)
        Map<String, Object> stats = statisticsService.getArticleBasicStats(1);
        assertNotNull(stats, "Basic article stats should not be null");
        assertTrue(stats.containsKey("title"));
    }
}
