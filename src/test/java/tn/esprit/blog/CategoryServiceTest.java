package tn.esprit.blog;

import org.junit.jupiter.api.*;
import tn.esprit.services.CategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryServiceTest {

    private static CategoryService categoryService;
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    static void init() {
        TestDatabaseMigration.applyMigrations();
        categoryService = new CategoryService();
    }

    // ── createIfMissing ─────────────────────────────────────────────────────

    @Test
    @Order(1)
    void createIfMissing_newCategory_returnsWithId() {
        Category cat = categoryService.createIfMissing("Renewables" + UNIQUE);
        assertNotNull(cat, "New category should be created");
        assertTrue(cat.getId() > 0, "New category should have a positive id");
        assertEquals("Renewables" + UNIQUE, cat.getName());
    }

    @Test
    @Order(2)
    void createIfMissing_existingCategory_returnsSameId() {
        String name = "Waste" + UNIQUE;
        Category first = categoryService.createIfMissing(name);
        Category second = categoryService.createIfMissing(name);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getId(), second.getId(), "Duplicate category should return same record");
    }

    @Test
    @Order(3)
    void createIfMissing_caseInsensitiveDedup() {
        String name = "Ocean" + UNIQUE;
        Category lower = categoryService.createIfMissing(name.toLowerCase());
        Category upper = categoryService.createIfMissing(name.toUpperCase());
        assertNotNull(lower);
        assertNotNull(upper);
        assertEquals(lower.getId(), upper.getId(), "Case-insensitive duplicate should not create new row");
    }

    @Test
    @Order(4)
    void createIfMissing_tooShortName_returnsNull() {
        assertNull(categoryService.createIfMissing("A"), "Single-char category should be rejected");
    }

    @Test
    @Order(5)
    void createIfMissing_startsWithDigit_returnsNull() {
        assertNull(categoryService.createIfMissing("1Category"), "Category starting with digit should be rejected");
    }

    @Test
    @Order(6)
    void createIfMissing_emptyName_returnsNull() {
        assertNull(categoryService.createIfMissing(""), "Empty category name should be rejected");
    }

    @Test
    @Order(7)
    void createIfMissing_nullName_returnsNull() {
        assertNull(categoryService.createIfMissing(null), "Null category name should be rejected");
    }

    @Test
    @Order(8)
    void createIfMissing_trims_whitespace() {
        String name = "  Climate" + UNIQUE + "  ";
        Category cat = categoryService.createIfMissing(name);
        assertNotNull(cat, "Category with surrounding whitespace should be accepted after trim");
        assertEquals("Climate" + UNIQUE, cat.getName());
    }

    // ── getAll ──────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void getAll_returnsNonEmptyList() {
        List<Category> cats = categoryService.getAll();
        assertNotNull(cats);
        assertFalse(cats.isEmpty(), "getAll should return seeded categories");
    }

    @Test
    @Order(10)
    void getAll_allEntriesHaveNonBlankName() {
        List<Category> cats = categoryService.getAll();
        assertTrue(cats.stream().allMatch(c -> c.getName() != null && !c.getName().isBlank()),
                "Every category returned should have a non-blank name");
    }

    // ── countArticlesForCategory ─────────────────────────────────────────────

    @Test
    @Order(11)
    void countArticlesForCategory_freshCategory_returnsZero() {
        Category cat = categoryService.createIfMissing("FreshCatCount" + UNIQUE);
        assertNotNull(cat);
        assertEquals(0, categoryService.countArticlesForCategory(cat.getId()),
                "Freshly created category with no articles should have count 0");
    }

    // ── renameCategory ───────────────────────────────────────────────────────

    @Test
    @Order(12)
    void renameCategory_validName_succeeds() {
        Category cat = categoryService.createIfMissing("RenameMe" + UNIQUE);
        assertNotNull(cat);
        String newName = "Renamed" + UNIQUE;
        assertTrue(categoryService.renameCategory(cat.getId(), newName), "Rename should succeed");

        List<Category> all = categoryService.getAll();
        assertTrue(all.stream().anyMatch(c -> newName.equals(c.getName())),
                "Renamed category should appear in getAll");
    }

    @Test
    @Order(13)
    void renameCategory_invalidName_returnsFalse() {
        Category cat = categoryService.createIfMissing("ValidForRename" + UNIQUE);
        assertNotNull(cat);
        assertFalse(categoryService.renameCategory(cat.getId(), "9bad"), "Rename to invalid name should return false");
    }

    // ── getArticleTitlesForCategory ──────────────────────────────────────────

    @Test
    @Order(14)
    void getArticleTitlesForCategory_freshCategory_returnsEmpty() {
        Category cat = categoryService.createIfMissing("EmptyCat" + UNIQUE);
        assertNotNull(cat);
        List<String> titles = categoryService.getArticleTitlesForCategory(cat.getId());
        assertNotNull(titles);
        assertTrue(titles.isEmpty(), "Freshly created category should have no articles");
    }

    // ── deleteCategoryAndUnlinkArticles ──────────────────────────────────────

    @Test
    @Order(15)
    void deleteCategory_removesFromGetAll() {
        Category cat = categoryService.createIfMissing("ToDelete" + UNIQUE);
        assertNotNull(cat);
        assertTrue(categoryService.deleteCategoryAndUnlinkArticles(cat.getId()), "Delete should return true");

        List<Category> all = categoryService.getAll();
        assertFalse(all.stream().anyMatch(c -> c.getId() == cat.getId()),
                "Deleted category should not appear in getAll");
    }
}
