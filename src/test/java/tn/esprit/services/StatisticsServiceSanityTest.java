package tn.esprit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class StatisticsServiceSanityTest {

    private static StatisticsService service;
    private static Connection cnx;

    @BeforeAll
    public static void setUp() {
        service = new StatisticsService();
        cnx = MyConnection.getInstance().getCnx();
    }

    @Test
    public void testDatabaseConnection() {
        assertNotNull(cnx, "Database connection should not be null");
    }

    @Test
    public void testTablesExist() throws SQLException {
        DatabaseMetaData md = cnx.getMetaData();
        String[] tables = {"article", "app_user", "article_view_event", "article_reaction_java", "comment", "category"};
        for (String table : tables) {
            ResultSet rs = md.getTables(null, null, table, null);
            assertTrue(rs.next(), "Table " + table + " should exist");
        }
    }

    @Test
    public void testAdminStatsUnfiltered() {
        // Test basic stats for admin (null email)
        int articles = service.getTotalPublishedArticles(null);
        int views = service.getTotalViews(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1), null);
        int comments = service.getTotalComments(null);
        Map<String, Integer> reactions = service.getTotalReactions(null);

        System.out.println("Admin Stats: Articles=" + articles + ", Views=" + views + ", Comments=" + comments + ", Reactions=" + reactions);
        
        assertTrue(articles >= 0);
        assertTrue(views >= 0);
        assertTrue(comments >= 0);
        assertNotNull(reactions);
    }

    @Test
    public void testNGOStatsFiltered() {
        // Test with a non-existent email to see if it returns 0 (filtered correctly)
        String dummyEmail = "nonexistent@eco.tn";
        int articles = service.getTotalPublishedArticles(dummyEmail);
        int views = service.getTotalViews(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1), dummyEmail);
        
        System.out.println("Dummy NGO Stats: Articles=" + articles + ", Views=" + views);
        
        assertEquals(0, articles, "Should return 0 for non-existent user");
        assertEquals(0, views, "Should return 0 for non-existent user");
    }

    @Test
    public void testCategoryStats() {
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> stats = service.getCategoryStats(now.minusYears(1), now.plusDays(1), null);
        assertNotNull(stats);
        for (Map<String, Object> stat : stats) {
            assertTrue(stat.containsKey("name"));
            assertTrue(stat.containsKey("articles"));
            assertTrue(stat.containsKey("views"));
        }
    }
}
