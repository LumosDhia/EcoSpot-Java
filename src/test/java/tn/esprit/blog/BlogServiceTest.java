package tn.esprit.blog;

import org.junit.jupiter.api.*;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlogServiceTest {

    private BlogService blogService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        TestDatabaseMigration.applyMigrations();
        blogService = new BlogService();
        categoryService = new CategoryService();
        loginAsAdmin();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    @Test
    void testAddAndSearchBlog() {
        List<Category> categories = categoryService.getAll();
        Category cat = categories.isEmpty() ? null : categories.get(0);

        String testTitle = "Test Integration Article " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(testTitle);
        blog.setContent("This is a test content for the integration test.");
        blog.setCategory(cat);
        blog.setImage("test-image-path.jpg");
        blog.setIsPublished(true);

        blogService.add2(blog);

        List<Blog> results = blogService.search(testTitle);
        assertFalse(results.isEmpty(), "The added article should be found by search");
        Blog found = results.get(0);
        assertEquals(testTitle, found.getTitle());
        assertNotNull(found.getAuthor());

        blogService.delete(found);
    }

    @Test
    void testAdd_delegatesToAdd2() {
        String testTitle = "DelegationTest " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(testTitle);
        blog.setContent("Testing that add() properly delegates to add2().");
        blog.setIsPublished(false);

        blogService.add(blog);

        List<Blog> results = blogService.search(testTitle);
        assertFalse(results.isEmpty(), "add() should persist the article via add2()");

        blogService.delete(results.get(0));
    }

    @Test
    void testSearchReturnsSlug() {
        String testTitle = "SlugTest " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(testTitle);
        blog.setContent("Article with slug verification content.");
        blog.setIsPublished(false);

        blogService.add2(blog);

        List<Blog> results = blogService.search(testTitle);
        assertFalse(results.isEmpty());
        Blog found = results.get(0);
        assertNotNull(found.getSlug(), "Article should have a slug after being saved");
        assertFalse(found.getSlug().isBlank(), "Slug should not be blank");

        blogService.delete(found);
    }

    @Test
    void testAdminAuthorFiltering() {
        List<Blog> allBlogs = blogService.getAll();

        List<Blog> adminBlogs = allBlogs.stream()
                .filter(b -> b.getAuthor() != null && b.getAuthor().startsWith("Admin"))
                .toList();

        List<Blog> userBlogs = allBlogs.stream()
                .filter(b -> b.getAuthor() == null || !b.getAuthor().startsWith("Admin"))
                .toList();

        assertNotNull(adminBlogs);
        assertNotNull(userBlogs);
    }

    @Test
    void testUpdatePersistsChanges() {
        String original = "UpdateTest " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(original);
        blog.setContent("Original content for update test.");
        blog.setIsPublished(false);
        blogService.add2(blog);

        Blog created = blogService.search(original).stream()
                .filter(b -> original.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article to update not found"));

        String updated = original + " Updated";
        created.setTitle(updated);
        created.setContent("Updated content with sufficient length.");
        blogService.update(created);

        List<Blog> results = blogService.search(updated);
        assertTrue(results.stream().anyMatch(b -> updated.equals(b.getTitle())),
                "Updated title should be persisted");

        blogService.delete(created);
    }

    @Test
    void testDeleteRemovesArticle() {
        String title = "DeleteTest " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("Content for delete test.");
        blog.setIsPublished(false);
        blogService.add2(blog);

        List<Blog> found = blogService.search(title);
        assertFalse(found.isEmpty(), "Article should exist before delete");
        Blog toDelete = found.get(0);

        blogService.delete(toDelete);

        List<Blog> afterDelete = blogService.search(title);
        assertFalse(afterDelete.stream().anyMatch(b -> b.getId() == toDelete.getId()),
                "Deleted article should not be found");
    }

    @Test
    void testResolveCurrentOwnerEmail_returnsSessionEmail() {
        String email = blogService.resolveCurrentOwnerEmail();
        assertNotNull(email, "Owner email should not be null when user is logged in");
        assertTrue(email.contains("@"), "Owner email should be a valid email");
    }

    @Test
    void testResolveCurrentOwnerEmail_noSession_returnsNull() {
        SessionManager.logout();
        assertNull(blogService.resolveCurrentOwnerEmail(), "Owner email should be null with no session");
        loginAsAdmin(); // restore for @AfterEach
    }

    private void loginAsAdmin() {
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
}
