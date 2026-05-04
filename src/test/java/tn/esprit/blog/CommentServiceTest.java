package tn.esprit.blog;

import org.junit.jupiter.api.*;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;
import tn.esprit.services.CommentService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceTest {

    private static CommentService commentService;
    private static BlogService blogService;
    private static int testArticleId;
    private static String adminUsername;
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    static void createTestArticle() {
        TestDatabaseMigration.applyMigrations();
        commentService = new CommentService();
        blogService = new BlogService();

        loginAsAdmin();

        CategoryService catService = new CategoryService();
        List<Category> cats = catService.getAll();
        Category cat = cats.isEmpty() ? null : cats.get(0);

        Blog article = new Blog();
        article.setTitle("CommentTest Article " + UNIQUE);
        article.setContent("Content for comment service integration tests.");
        article.setIsPublished(true);
        article.setCategory(cat);
        blogService.add2(article);

        List<Blog> found = blogService.search("CommentTest Article " + UNIQUE);
        assertFalse(found.isEmpty(), "Test article must be created before comment tests");
        testArticleId = found.get(0).getId();
    }

    @AfterAll
    static void deleteTestArticle() {
        loginAsAdmin();
        Blog article = new Blog();
        article.setId(testArticleId);
        blogService.delete(article);
        SessionManager.logout();
    }

    @BeforeEach
    void loginBeforeEach() {
        loginAsAdmin();
    }

    @AfterEach
    void logoutAfterEach() {
        SessionManager.logout();
    }

    // ── add ─────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void add_validComment_returnsTrue() {
        Comment c = new Comment("admin", "This is a test comment.", testArticleId);
        assertTrue(commentService.add(c), "Adding a valid comment should succeed");
    }

    @Test
    @Order(2)
    void add_usesSessionUsernameAsAuthor() {
        Comment c = new Comment(null, "Author from session test " + UNIQUE, testArticleId);
        assertTrue(commentService.add(c));

        List<Comment> comments = commentService.getByArticleId(testArticleId);
        assertTrue(comments.stream().anyMatch(
                cm -> adminUsername.equals(cm.getAuthorName()) && cm.getContent().contains("Author from session test")),
                "Comment author should be taken from session username");
    }

    // ── getByArticleId ───────────────────────────────────────────────────────

    @Test
    @Order(3)
    void getByArticleId_returnsCommentsForArticle() {
        List<Comment> comments = commentService.getByArticleId(testArticleId);
        assertNotNull(comments);
        assertFalse(comments.isEmpty(), "Should return at least the comments added in earlier tests");
    }

    @Test
    @Order(4)
    void getByArticleId_nonExistentArticle_returnsEmpty() {
        List<Comment> comments = commentService.getByArticleId(Integer.MAX_VALUE);
        assertNotNull(comments);
        assertTrue(comments.isEmpty(), "Non-existent article should have no comments");
    }

    @Test
    @Order(5)
    void getByArticleId_commentHasCorrectArticleId() {
        Comment c = new Comment("admin", "Article ID check " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> comments = commentService.getByArticleId(testArticleId);
        assertTrue(comments.stream().allMatch(cm -> cm.getArticleId() == testArticleId),
                "All returned comments should belong to the queried article");
    }

    // ── flagComment / acceptFlaggedComment ───────────────────────────────────

    @Test
    @Order(6)
    void flagComment_hidesFromPublic() {
        Comment c = new Comment("admin", "FlagMe " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> before = commentService.getByArticleId(testArticleId);
        Comment toFlag = before.stream()
                .filter(cm -> cm.getContent().startsWith("FlagMe"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Comment to flag not found"));

        assertTrue(commentService.flagComment(toFlag.getId()), "Flag should succeed");

        List<Comment> after = commentService.getByArticleId(testArticleId);
        assertFalse(after.stream().anyMatch(cm -> cm.getId() == toFlag.getId()),
                "Flagged comment should be hidden from public view");
    }

    @Test
    @Order(7)
    void acceptFlaggedComment_restoresVisibility() {
        Comment c = new Comment("admin", "FlagAndAccept " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> all = commentService.getAllCommentsForAdmin();
        Comment toFlag = all.stream()
                .filter(cm -> cm.getContent().startsWith("FlagAndAccept"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Comment not found for flag/accept test"));

        commentService.flagComment(toFlag.getId());
        assertTrue(commentService.acceptFlaggedComment(toFlag.getId()), "Accept should succeed");

        List<Comment> afterAccept = commentService.getByArticleId(testArticleId);
        assertTrue(afterAccept.stream().anyMatch(cm -> cm.getId() == toFlag.getId()),
                "Accepted comment should be visible again");
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    void delete_removesComment() {
        Comment c = new Comment("admin", "DeleteMe " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> before = commentService.getByArticleId(testArticleId);
        Comment toDelete = before.stream()
                .filter(cm -> cm.getContent().startsWith("DeleteMe"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Comment to delete not found"));

        commentService.delete(toDelete.getId());

        List<Comment> after = commentService.getByArticleId(testArticleId);
        assertFalse(after.stream().anyMatch(cm -> cm.getId() == toDelete.getId()),
                "Deleted comment should not appear in subsequent queries");
    }

    // ── updateContent ─────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void updateContent_persistsChange() {
        Comment c = new Comment("admin", "UpdateContent original " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> all = commentService.getByArticleId(testArticleId);
        Comment toUpdate = all.stream()
                .filter(cm -> cm.getContent().startsWith("UpdateContent original"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Comment for content update not found"));

        String newContent = "UpdateContent modified " + UNIQUE;
        assertTrue(commentService.updateContent(toUpdate.getId(), newContent), "updateContent should return true");

        List<Comment> after = commentService.getByArticleId(testArticleId);
        assertTrue(after.stream().anyMatch(cm -> newContent.equals(cm.getContent())),
                "Updated content should be persisted");
    }

    // ── getAllCommentsForAdmin ────────────────────────────────────────────────

    @Test
    @Order(10)
    void getAllCommentsForAdmin_includesFlaggedComments() {
        Comment c = new Comment("admin", "AdminFlagCheck " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> all = commentService.getAllCommentsForAdmin();
        Comment target = all.stream()
                .filter(cm -> cm.getContent().startsWith("AdminFlagCheck"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Admin comment list incomplete"));

        commentService.flagComment(target.getId());

        List<Comment> afterFlag = commentService.getAllCommentsForAdmin();
        assertTrue(afterFlag.stream().anyMatch(cm -> cm.getId() == target.getId() && cm.isFlagged()),
                "Admin view should include flagged comments");
    }

    // ── getCommentsAuthoredByCurrentUser ─────────────────────────────────────

    @Test
    @Order(11)
    void getCommentsAuthoredByCurrentUser_returnsOnlyOwnComments() {
        Comment c = new Comment("admin", "MyOwnComment " + UNIQUE, testArticleId);
        commentService.add(c);

        List<Comment> mine = commentService.getCommentsAuthoredByCurrentUser();
        assertNotNull(mine);
        assertTrue(mine.stream().anyMatch(cm -> cm.getContent().startsWith("MyOwnComment")),
                "Should include comment made by current user");
        assertTrue(mine.stream().allMatch(cm -> adminUsername.equalsIgnoreCase(cm.getAuthorName())),
                "All returned comments should belong to the current user");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void loginAsAdmin() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT id, username, email FROM user WHERE role = 'ADMIN' LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                adminUsername = username;
                SessionManager.login(new User(rs.getInt("id"), username, rs.getString("email"), "", "ADMIN"));
            } else {
                adminUsername = "admin";
                SessionManager.login(new User(1, "admin", "admin@ecospot.tn", "", "ADMIN"));
            }
        } catch (SQLException e) {
            adminUsername = "admin";
            SessionManager.login(new User(1, "admin", "admin@ecospot.tn", "", "ADMIN"));
        }
    }
}
