package tn.esprit.blog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;
import tn.esprit.services.TagService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ArticleValidationAndPersistenceTest {

    @BeforeEach
    void loginAsAdmin() {
        TestDatabaseMigration.applyMigrations();
        String email = findEmailForRole("ADMIN");
        int id = findIdForRole("ADMIN");
        String username = findUsernameForRole("ADMIN");
        if (email != null) {
            SessionManager.login(new User(id, username != null ? username : "admin", email, "", "ADMIN"));
        }
    }

    @AfterEach
    void clearSession() {
        SessionManager.logout();
    }

    @Test
    void validationRejectsInvalidTitleAndContent() {
        assertEquals("Title is required.", ArticleInputValidator.validate("", "<p>valid content with enough letters</p>", true));
        assertEquals("Title must be between 5 and 100 characters.", ArticleInputValidator.validate("Abc", "<p>valid content with enough letters</p>", true));
        assertEquals("Title must start with a letter (not a number or symbol).", ArticleInputValidator.validate("1Valid title", "<p>valid content with enough letters</p>", true));
        assertEquals("Content is required.", ArticleInputValidator.validate("Valid Title", "<p>   </p>", true));
        assertEquals("The article content must be more detailed (at least 20 characters).", ArticleInputValidator.validate("Valid Title", "<p>short text</p>", true));
        assertEquals("Please select a category.", ArticleInputValidator.validate("Valid Title", "<p>This is long enough content with letters.</p>", false));
    }

    @Test
    void validationAcceptsValidInput() {
        String error = ArticleInputValidator.validate(
                "Healthy Oceans Future",
                "<p>This article explains concrete actions for a cleaner sea and community support.</p>",
                true
        );
        assertNull(error, "Valid article input should pass validation");
    }

    @Test
    void databaseWriteUpdateDeleteFlowWorksForArticles() {
        BlogService blogService = new BlogService();
        CategoryService categoryService = new CategoryService();
        List<Category> categories = categoryService.getAll();
        Category category = categories.isEmpty() ? null : categories.get(0);

        String baseTitle = "Article DB Flow " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(baseTitle);
        blog.setContent("<p>Initial DB content with enough details for persistence test.</p>");
        blog.setImage("test.jpg");
        blog.setCategory(category);
        blog.setIsPublished(false);

        blogService.add2(blog);

        List<Blog> inserted = blogService.search(baseTitle);
        assertFalse(inserted.isEmpty(), "Inserted article should be found in database");
        Blog created = inserted.stream()
                .filter(b -> baseTitle.equals(b.getTitle()))
                .findFirst()
                .orElse(inserted.get(0));
        assertTrue(created.getId() > 0, "Inserted article should have a valid id");

        String updatedTitle = baseTitle + " Updated";
        created.setTitle(updatedTitle);
        created.setContent("<p>Updated DB content with enough letters and words for test verification.</p>");
        blogService.update(created);

        List<Blog> updatedList = blogService.search(updatedTitle);
        Optional<Blog> updated = updatedList.stream().filter(b -> updatedTitle.equals(b.getTitle())).findFirst();
        assertTrue(updated.isPresent(), "Updated article title should be persisted in database");

        blogService.delete(created);
        List<Blog> afterDelete = blogService.search(updatedTitle);
        boolean stillExists = afterDelete.stream().anyMatch(b -> b.getId() == created.getId());
        assertFalse(stillExists, "Deleted article should not remain in database");
    }

    @Test
    void writingInterfaceCategoryAndTagCreationPersistsInDatabase() {
        CategoryService categoryService = new CategoryService();
        TagService tagService = new TagService();
        BlogService blogService = new BlogService();

        String newCategoryName = "CatFromWriteUI_" + System.currentTimeMillis();
        String newTagA = "TagWriteA" + System.currentTimeMillis() % 100000;
        String newTagB = "TagWriteB" + System.currentTimeMillis() % 100000;

        Category createdCategory = categoryService.createIfMissing(newCategoryName);
        assertNotNull(createdCategory, "New category should be inserted");
        assertTrue(createdCategory.getId() > 0, "Inserted category should have id");

        Tag createdTagA = tagService.createIfMissing(newTagA);
        Tag createdTagB = tagService.createIfMissing(newTagB);
        assertNotNull(createdTagA, "First tag should be inserted");
        assertNotNull(createdTagB, "Second tag should be inserted");

        Blog blog = new Blog();
        String title = "WriteUI CategoryTag Flow " + System.currentTimeMillis();
        blog.setTitle(title);
        blog.setContent("<p>Article created to verify new category/tag persistence from writing interface flow.</p>");
        blog.setImage("test.jpg");
        blog.setCategory(createdCategory);
        blog.setTags(List.of(createdTagA, createdTagB));
        blog.setIsPublished(false);

        blogService.add2(blog);

        List<Blog> found = blogService.search(title);
        Optional<Blog> loadedOpt = found.stream().filter(b -> title.equals(b.getTitle())).findFirst();
        assertTrue(loadedOpt.isPresent(), "Created article should be readable from DB");
        Blog loaded = loadedOpt.get();

        assertNotNull(loaded.getCategory(), "Created article should keep category");
        assertEquals(newCategoryName, loaded.getCategory().getName(), "Article should reference inserted category");
        assertTrue(loaded.getTags().stream().anyMatch(t -> newTagA.equals(t.getName())), "Article should include first new tag");
        assertTrue(loaded.getTags().stream().anyMatch(t -> newTagB.equals(t.getName())), "Article should include second new tag");

        blogService.delete(loaded);
    }

    @Test
    void articleOwnerEmailMatchesLoggedInUser() {
        BlogService blogService = new BlogService();
        CategoryService categoryService = new CategoryService();

        String adminEmail = findEmailForRole("ADMIN");
        assertNotNull(adminEmail, "DB must contain at least one ADMIN user");

        assertEquals(adminEmail, blogService.resolveCurrentOwnerEmail(),
                "Owner email should match the currently logged-in user");

        List<Category> categories = categoryService.getAll();
        Category category = categories.isEmpty() ? null : categories.get(0);

        String title = "OwnerEmail Test " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("<p>Test verifying owner email attribution on article creation.</p>");
        blog.setCategory(category);
        blog.setIsPublished(false);

        blogService.add2(blog);

        Blog created = blogService.search(title).stream()
                .filter(b -> title.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created article should be found"));

        assertEquals(adminEmail, created.getCreatedByEmail(),
                "Article createdByEmail should match the logged-in admin's email");

        blogService.delete(created);
    }

    @Test
    void articleRevisionNotePersistedByUpdate() {
        BlogService blogService = new BlogService();
        CategoryService categoryService = new CategoryService();

        List<Category> categories = categoryService.getAll();
        Category category = categories.isEmpty() ? null : categories.get(0);

        String title = "Revision Note Test " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("<p>Content for revision note persistence verification.</p>");
        blog.setCategory(category);
        blog.setIsPublished(false);
        blogService.add2(blog);

        Blog created = blogService.search(title).stream()
                .filter(b -> title.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article for revision test should be found"));

        created.setAdminRevisionNote("Please expand impact metrics and evidence.");
        created.setIsPublished(false);
        blogService.update(created);

        Blog revised = blogService.search(title).stream()
                .filter(b -> b.getId() == created.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Revised article should still be found"));

        assertNotNull(revised.getAdminRevisionNote(), "Revision note should be persisted");
        assertTrue(revised.getAdminRevisionNote().contains("impact metrics"), "Revision note content should match");
        assertFalse(revised.getIsPublished(), "Article with revision note must remain unpublished");

        blogService.delete(revised);
    }

    @Test
    void taxonomyRenameAndUnlinkOperationsAffectArticlesInDatabase() {
        CategoryService categoryService = new CategoryService();
        TagService tagService = new TagService();
        BlogService blogService = new BlogService();

        String categoryName = "TaxFlowCat_" + System.currentTimeMillis();
        String tagName = "TaxTag" + (System.currentTimeMillis() % 100000);
        Category category = categoryService.createIfMissing(categoryName);
        Tag tag = tagService.createIfMissing(tagName);

        assertNotNull(category, "Category creation should succeed");
        assertNotNull(tag, "Tag creation should succeed");

        String title = "Taxonomy Flow " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("<p>Taxonomy flow test verifies rename and unlink behavior on persistent article rows.</p>");
        blog.setCategory(category);
        blog.setTags(List.of(tag));
        blog.setIsPublished(false);
        blogService.add2(blog);

        Blog created = blogService.search(title).stream()
                .filter(b -> title.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Taxonomy test article should be found"));

        String renamedCategory = "TaxFlowCatRenamed_" + System.currentTimeMillis();
        String renamedTag = "TaxTagRen" + (System.currentTimeMillis() % 100000);

        assertTrue(categoryService.renameCategory(category.getId(), renamedCategory), "Category rename should succeed");
        assertTrue(tagService.renameTag(tag.getId(), renamedTag), "Tag rename should succeed");

        Blog afterRename = blogService.search(title).stream()
                .filter(b -> b.getId() == created.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article should still exist after taxonomy rename"));

        assertNotNull(afterRename.getCategory(), "Category should still be linked after rename");
        assertEquals(renamedCategory, afterRename.getCategory().getName(), "Article category label should reflect renamed category");
        assertTrue(afterRename.getTags().stream().anyMatch(t -> renamedTag.equals(t.getName())), "Article tag label should reflect renamed tag");

        assertTrue(tagService.deleteTagAndUnlinkArticles(tag.getId()), "Tag delete/unlink should succeed");
        Blog afterTagDelete = blogService.search(title).stream()
                .filter(b -> b.getId() == created.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article should still exist after tag delete"));
        assertFalse(afterTagDelete.getTags().stream().anyMatch(t -> renamedTag.equals(t.getName())), "Deleted tag should be unlinked from article");

        assertTrue(categoryService.deleteCategoryAndUnlinkArticles(category.getId()), "Category delete/unlink should succeed");
        Blog afterCategoryDelete = blogService.search(title).stream()
                .filter(b -> b.getId() == created.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article should still exist after category delete"));
        assertNull(afterCategoryDelete.getCategory(), "Deleted category should be unlinked from article");

        blogService.delete(afterCategoryDelete);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String findEmailForRole(String role) {
        String sql = "SELECT email FROM user WHERE role = ? LIMIT 1";
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            fail("Unable to query user role data: " + e.getMessage());
        }
        return null;
    }

    private int findIdForRole(String role) {
        String sql = "SELECT id FROM user WHERE role = ? LIMIT 1";
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            fail("Unable to query user id: " + e.getMessage());
        }
        return 1;
    }

    private String findUsernameForRole(String role) {
        String sql = "SELECT username FROM user WHERE role = ? LIMIT 1";
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            fail("Unable to query username: " + e.getMessage());
        }
        return null;
    }
}
