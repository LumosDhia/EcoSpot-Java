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
        int views = statisticsService.getTotalViews(start, end);
        assertTrue(views >= 0, "Total views should be non-negative");
    }

    @Test
    public void testGetTotalPublishedArticles() {
        int count = statisticsService.getTotalPublishedArticles();
        assertTrue(count >= 0, "Total published articles should be non-negative");
    }

    @Test
    public void testGetCategoryStats() {
        List<Map<String, Object>> stats = statisticsService.getCategoryStats();
        assertNotNull(stats, "Category stats should not be null");
    }

    @Test
    public void testGetViewsTimeSeries() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        List<Map<String, Object>> series = statisticsService.getViewsTimeSeries(start, end);
        assertNotNull(series, "Views time series should not be null");
    }

    @Test
    public void testGetTopArticlesByViews() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Map<String, Object>> top = statisticsService.getTopArticlesByViews(start, end, 5);
        assertNotNull(top, "Top articles list should not be null");
        assertTrue(top.size() <= 5, "List size should not exceed limit");
    }

    @Test
    public void testGetArticleStatusFunnel() {
        Map<String, Integer> funnel = statisticsService.getArticleStatusFunnel();
        assertNotNull(funnel, "Status funnel should not be null");
    }
}
