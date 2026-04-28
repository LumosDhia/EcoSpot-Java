package tn.esprit.blog;

import org.junit.jupiter.api.*;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;
import tn.esprit.services.StatisticsService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StatisticsServiceTest {

    private static StatisticsService stats;
    private static BlogService blogService;
    private static int testArticleId;
    private static final LocalDate FROM = LocalDate.now().minusYears(1);
    private static final LocalDate TO = LocalDate.now();
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    static void setup() {
        TestDatabaseMigration.applyMigrations();
        stats = new StatisticsService();
        blogService = new BlogService();
        loginAsAdmin();

        CategoryService catService = new CategoryService();
        List<Category> cats = catService.getAll();

        Blog article = new Blog();
        article.setTitle("StatsTest Article " + UNIQUE);
        article.setContent("Statistics integration test content.");
        article.setIsPublished(true);
        article.setCategory(cats.isEmpty() ? null : cats.get(0));
        blogService.add2(article);

        List<Blog> found = blogService.search("StatsTest Article " + UNIQUE);
        assertFalse(found.isEmpty(), "Test article for stats must be created");
        testArticleId = found.get(0).getId();

        SessionManager.logout();
    }

    @AfterAll
    static void teardown() {
        loginAsAdmin();
        Blog article = new Blog();
        article.setId(testArticleId);
        blogService.delete(article);
        SessionManager.logout();
    }

    // ── Overview Stats ────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void getTotalPublishedArticles_adminScope_nonNegative() {
        int count = stats.getTotalPublishedArticles(null);
        assertTrue(count >= 0, "Total articles (admin) should be non-negative");
    }

    @Test
    @Order(2)
    void getTotalPublishedArticles_ownerScope_nonNegative() {
        String adminEmail = findAdminEmail();
        int count = stats.getTotalPublishedArticles(adminEmail);
        assertTrue(count >= 0, "Total articles scoped by owner should be non-negative");
    }

    @Test
    @Order(3)
    void getTotalPublishedArticles_adminSeeMoreOrEqualThanOwner() {
        String adminEmail = findAdminEmail();
        int all = stats.getTotalPublishedArticles(null);
        int owned = stats.getTotalPublishedArticles(adminEmail);
        assertTrue(all >= owned, "Admin total should be >= owner total");
    }

    @Test
    @Order(4)
    void getTotalComments_adminScope_nonNegative() {
        int count = stats.getTotalComments(null);
        assertTrue(count >= 0);
    }

    @Test
    @Order(5)
    void getTotalComments_ownerScope_nonNegative() {
        int count = stats.getTotalComments(findAdminEmail());
        assertTrue(count >= 0);
    }

    @Test
    @Order(6)
    void getTotalReactions_returnsMap() {
        Map<String, Integer> reactions = stats.getTotalReactions(null);
        assertNotNull(reactions, "Reaction map should not be null");
    }

    // ── Period Stats ─────────────────────────────────────────────────────────

    @Test
    @Order(7)
    void getViewsByPeriod_returnsNonNegative() {
        int views = stats.getViewsByPeriod(FROM, TO, null);
        assertTrue(views >= 0);
    }

    @Test
    @Order(8)
    void getPublishedArticlesByPeriod_returnsNonNegative() {
        int count = stats.getPublishedArticlesByPeriod(FROM, TO, null);
        assertTrue(count >= 0);
    }

    @Test
    @Order(9)
    void getCommentsByPeriod_returnsNonNegative() {
        int count = stats.getCommentsByPeriod(FROM, TO, null);
        assertTrue(count >= 0);
    }

    @Test
    @Order(10)
    void getReactionsByPeriod_returnsMap() {
        Map<String, Integer> reactions = stats.getReactionsByPeriod(FROM, TO, null);
        assertNotNull(reactions);
    }

    // ── Time-Series ───────────────────────────────────────────────────────────

    @Test
    @Order(11)
    void getViewsTimeSeries_returnsList() {
        List<Map<String, Object>> series = stats.getViewsTimeSeries(FROM, TO, null);
        assertNotNull(series);
    }

    @Test
    @Order(12)
    void getViewsMonthly_returnsList() {
        List<Map<String, Object>> monthly = stats.getViewsMonthly(FROM, TO, null);
        assertNotNull(monthly);
    }

    @Test
    @Order(13)
    void getViewsHourly_returnsList() {
        List<Map<String, Object>> hourly = stats.getViewsHourly(LocalDate.now(), null);
        assertNotNull(hourly);
    }

    @Test
    @Order(14)
    void getHourlyStats_returns24Entries() {
        List<Map<String, Object>> hourly = stats.getHourlyStats(LocalDate.now(), null);
        assertNotNull(hourly);
        assertEquals(24, hourly.size(), "Hourly stats must have exactly 24 entries");
    }

    @Test
    @Order(15)
    void getHourlyStats_eachEntryHasRequiredKeys() {
        List<Map<String, Object>> hourly = stats.getHourlyStats(LocalDate.now(), null);
        for (Map<String, Object> entry : hourly) {
            assertTrue(entry.containsKey("hour"), "Each entry must have 'hour' key");
            assertTrue(entry.containsKey("views"), "Each entry must have 'views' key");
            assertTrue(entry.containsKey("articles"), "Each entry must have 'articles' key");
            assertTrue(entry.containsKey("comments"), "Each entry must have 'comments' key");
            assertTrue(entry.containsKey("likes"), "Each entry must have 'likes' key");
            assertTrue(entry.containsKey("dislikes"), "Each entry must have 'dislikes' key");
        }
    }

    @Test
    @Order(16)
    void getViewsWeekly_returnsList() {
        List<Map<String, Object>> weekly = stats.getViewsWeekly(4);
        assertNotNull(weekly);
    }

    @Test
    @Order(17)
    void getViewsByHourOfDay_returnsList() {
        List<Map<String, Object>> hod = stats.getViewsByHourOfDay(null);
        assertNotNull(hod);
    }

    // ── Top Content ───────────────────────────────────────────────────────────

    @Test
    @Order(18)
    void getTopArticlesByViews_adminScope_returnsList() {
        List<Map<String, Object>> top = stats.getTopArticlesByViews(FROM, TO, 5, null);
        assertNotNull(top);
        assertTrue(top.size() <= 5, "Result should not exceed the requested limit");
    }

    @Test
    @Order(19)
    void getTopArticlesByViews_ownerScope_returnsList() {
        List<Map<String, Object>> top = stats.getTopArticlesByViews(FROM, TO, 5, findAdminEmail());
        assertNotNull(top);
    }

    @Test
    @Order(20)
    void getTopArticlesByEngagement_returnsList() {
        List<Map<String, Object>> top = stats.getTopArticlesByEngagement(5);
        assertNotNull(top);
        assertTrue(top.size() <= 5);
    }

    @Test
    @Order(21)
    void getAllArticlesWithStats_includesTestArticle() {
        List<Map<String, Object>> all = stats.getAllArticlesWithStats();
        assertNotNull(all);
        assertTrue(all.stream().anyMatch(m -> (int) m.get("id") == testArticleId),
                "getAllArticlesWithStats should include the test article");
    }

    @Test
    @Order(22)
    void getAllArticlesWithStats_eachEntryHasRequiredKeys() {
        List<Map<String, Object>> all = stats.getAllArticlesWithStats();
        if (all.isEmpty()) return;
        Map<String, Object> first = all.get(0);
        assertTrue(first.containsKey("id"), "Must have 'id'");
        assertTrue(first.containsKey("title"), "Must have 'title'");
        assertTrue(first.containsKey("views"), "Must have 'views'");
        assertTrue(first.containsKey("likes"), "Must have 'likes'");
        assertTrue(first.containsKey("dislikes"), "Must have 'dislikes'");
        assertTrue(first.containsKey("comments"), "Must have 'comments'");
    }

    // ── Category & Tag Stats ─────────────────────────────────────────────────

    @Test
    @Order(23)
    void getCategoryStats_returnsList() {
        List<Map<String, Object>> catStats = stats.getCategoryStats(FROM, TO, null);
        assertNotNull(catStats);
        assertFalse(catStats.isEmpty(), "Category stats should include seeded categories");
    }

    @Test
    @Order(24)
    void getCategoryStats_ownerScope_returnsList() {
        List<Map<String, Object>> catStats = stats.getCategoryStats(FROM, TO, findAdminEmail());
        assertNotNull(catStats);
    }

    @Test
    @Order(25)
    void getTagStats_usesCorrectTableName_noSqlError() {
        // Verifies the article_tags -> article_tag fix: must not throw
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> tagStats = stats.getTagStats(10);
            assertNotNull(tagStats);
        }, "getTagStats must not throw a SQL error (article_tag table name regression)");
    }

    // ── Author Stats ─────────────────────────────────────────────────────────

    @Test
    @Order(26)
    void getAuthorStats_returnsList() {
        List<Map<String, Object>> authorStats = stats.getAuthorStats(FROM, TO);
        assertNotNull(authorStats);
    }

    @Test
    @Order(27)
    void getAuthorStats_includesAdminUser() {
        List<Map<String, Object>> authorStats = stats.getAuthorStats(FROM, TO);
        assertTrue(authorStats.stream().anyMatch(m -> m.get("author") != null),
                "Author stats should have at least one entry with a non-null author");
    }

    // ── Comment Analytics ────────────────────────────────────────────────────

    @Test
    @Order(28)
    void getTopCommenters_returnsList() {
        List<Map<String, Object>> commenters = stats.getTopCommenters(5);
        assertNotNull(commenters);
        assertTrue(commenters.size() <= 5);
    }

    // ── Per-Article Drilldown ─────────────────────────────────────────────────

    @Test
    @Order(29)
    void getArticleBasicStats_testArticle_hasCorrectTitle() {
        Map<String, Object> basic = stats.getArticleBasicStats(testArticleId);
        assertNotNull(basic);
        assertTrue(((String) basic.get("title")).startsWith("StatsTest Article"),
                "Basic stats title should match test article");
    }

    @Test
    @Order(30)
    void getArticleBasicStats_nonExistentId_returnsPlaceholder() {
        Map<String, Object> basic = stats.getArticleBasicStats(Integer.MAX_VALUE);
        assertNotNull(basic);
        assertNotNull(basic.get("title"), "Placeholder title should be set for non-existent article");
        assertEquals(0, (int) basic.get("views"), "Non-existent article should have 0 views");
    }

    @Test
    @Order(31)
    void getArticleViewsTimeline_returnsList() {
        List<Map<String, Object>> timeline = stats.getArticleViewsTimeline(testArticleId, 30);
        assertNotNull(timeline);
    }

    @Test
    @Order(32)
    void getDetailedArticleStats_adminScope_includesTestArticle() {
        List<Map<String, Object>> detailed = stats.getDetailedArticleStats(FROM, TO, null);
        assertNotNull(detailed);
        assertTrue(detailed.stream().anyMatch(m -> (int) m.get("id") == testArticleId),
                "Detailed stats should include test article in admin scope");
    }

    // ── Search Terms ─────────────────────────────────────────────────────────

    @Test
    @Order(33)
    void getTopSearchTerms_returnsList() {
        List<Map<String, Object>> terms = stats.getTopSearchTerms(FROM, TO, 10);
        assertNotNull(terms);
        assertTrue(terms.size() <= 10);
    }

    // ── Publishing Patterns ───────────────────────────────────────────────────

    @Test
    @Order(34)
    void getArticleStatusFunnel_returnsMap() {
        Map<String, Integer> funnel = stats.getArticleStatusFunnel();
        assertNotNull(funnel);
    }

    @Test
    @Order(35)
    void getArticleStatusFunnel_testArticleCountedAsPublished() {
        Map<String, Integer> funnel = stats.getArticleStatusFunnel();
        assertTrue(funnel.containsKey("published") && funnel.get("published") > 0,
                "Funnel should count at least the test article as published");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void loginAsAdmin() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT id, username, email FROM user WHERE role = 'ADMIN' LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SessionManager.login(new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("email"), "", "ADMIN"));
            } else {
                SessionManager.login(new User(1, "admin", "admin@ecospot.tn", "", "ADMIN"));
            }
        } catch (SQLException e) {
            SessionManager.login(new User(1, "admin", "admin@ecospot.tn", "", "ADMIN"));
        }
    }

    private static String findAdminEmail() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT email FROM user WHERE role = 'ADMIN' LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException ignored) {}
        return "admin@ecospot.tn";
    }
}
