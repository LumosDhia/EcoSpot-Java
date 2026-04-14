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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ArticleValidationAndPersistenceTest {

    @BeforeEach
    void loginAsDbBackedUserForWriteOperations() {
        String adminEmail = findEmailForRole("ROLE_ADMIN");
        if (adminEmail != null) {
            SessionManager.login(new User(0, "admin-test", adminEmail, "", "ADMIN"));
        }
    }

    @AfterEach
    void clearSessionAfterEachTest() {
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
        blog.setImage("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Example.jpg/640px-Example.jpg");
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
        String newTagA = "TagFromWriteUI_A_" + System.currentTimeMillis();
        String newTagB = "TagFromWriteUI_B_" + System.currentTimeMillis();

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
        blog.setImage("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Example.jpg/640px-Example.jpg");
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
    void articleOwnerAndRevisionWorkflowPersistsInDatabase() {
        BlogService blogService = new BlogService();
        CategoryService categoryService = new CategoryService();

        String ngoEmail = findEmailForRole("ROLE_NGO");
        String adminEmail = findEmailForRole("ROLE_ADMIN");
        assertNotNull(ngoEmail, "DB must contain at least one NGO app_user");
        assertNotNull(adminEmail, "DB must contain at least one ADMIN app_user");

        SessionManager.login(new User(0, "ngo-test", ngoEmail, "", "NGO"));
        assertEquals(ngoEmail, blogService.resolveCurrentOwnerEmail(), "Owner email resolution should match logged-in NGO");

        List<Category> categories = categoryService.getAll();
        Category category = categories.isEmpty() ? null : categories.get(0);

        String title = "Workflow Owner Revision " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("<p>Workflow test content long enough to validate persistence and role ownership behavior.</p>");
        blog.setImage("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Example.jpg/640px-Example.jpg");
        blog.setCategory(category);
        blog.setIsPublished(false);

        blogService.add2(blog);

        Blog created = blogService.search(title).stream()
                .filter(b -> title.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created workflow article should be found"));

        assertEquals(ngoEmail, created.getCreatedByEmail(), "Article should be owned by the currently logged-in NGO user");

        SessionManager.login(new User(0, "admin-test", adminEmail, "", "ADMIN"));
        created.setAdminRevisionNote("Please expand impact metrics and evidence.");
        created.setIsPublished(false);
        blogService.update(created);

        Blog revised = blogService.search(title).stream()
                .filter(b -> b.getId() == created.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Revised article should still be found"));

        assertNotNull(revised.getAdminRevisionNote(), "Revision note should be persisted");
        assertTrue(revised.getAdminRevisionNote().contains("impact metrics"), "Revision note content should match update");
        assertFalse(revised.getIsPublished(), "Revision-requested article must remain unpublished");

        blogService.delete(revised);
        SessionManager.logout();
    }

    @Test
    void taxonomyRenameAndUnlinkOperationsAffectArticlesInDatabase() {
        CategoryService categoryService = new CategoryService();
        TagService tagService = new TagService();
        BlogService blogService = new BlogService();

        String adminEmail = findEmailForRole("ROLE_ADMIN");
        assertNotNull(adminEmail, "DB must contain at least one ADMIN app_user");
        SessionManager.login(new User(0, "admin-test", adminEmail, "", "ADMIN"));

        String categoryName = "TaxFlowCat_" + System.currentTimeMillis();
        String tagName = "TaxTag_" + (System.currentTimeMillis() % 100000);
        Category category = categoryService.createIfMissing(categoryName);
        Tag tag = tagService.createIfMissing(tagName);

        assertNotNull(category, "Category creation should succeed");
        assertNotNull(tag, "Tag creation should succeed");

        String title = "Taxonomy Flow " + System.currentTimeMillis();
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent("<p>Taxonomy flow test verifies rename and unlink behavior on persistent article rows.</p>");
        blog.setImage("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Example.jpg/640px-Example.jpg");
        blog.setCategory(category);
        blog.setTags(List.of(tag));
        blog.setIsPublished(false);
        blogService.add2(blog);

        Blog created = blogService.search(title).stream()
                .filter(b -> title.equals(b.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Taxonomy test article should be found"));

        String renamedCategory = "TaxFlowCatRenamed_" + System.currentTimeMillis();
        String renamedTag = "TaxTagRen_" + (System.currentTimeMillis() % 100000);

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
        SessionManager.logout();
    }

    private String findEmailForRole(String roleKeyword) {
        String sql = "SELECT email FROM app_user WHERE roles LIKE ? LIMIT 1";
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, "%" + roleKeyword + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("email");
                    }
                }
            }
        } catch (SQLException e) {
            fail("Unable to query app_user role data: " + e.getMessage());
        }
        return null;
    }
}
