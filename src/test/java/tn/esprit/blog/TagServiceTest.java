package tn.esprit.blog;

import org.junit.jupiter.api.*;
import tn.esprit.services.TagService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TagServiceTest {

    private static TagService tagService;
    private static final String UNIQUE = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    static void init() {
        TestDatabaseMigration.applyMigrations();
        tagService = new TagService();
    }

    // ── createIfMissing ─────────────────────────────────────────────────────

    @Test
    @Order(1)
    void createIfMissing_newTag_returnsWithId() {
        Tag tag = tagService.createIfMissing("Solar" + UNIQUE);
        assertNotNull(tag, "New tag should be created");
        assertTrue(tag.getId() > 0, "New tag should have a positive id");
        assertEquals("Solar" + UNIQUE, tag.getName());
    }

    @Test
    @Order(2)
    void createIfMissing_existingTag_returnsSameId() {
        String name = "Reuse" + UNIQUE;
        Tag first = tagService.createIfMissing(name);
        Tag second = tagService.createIfMissing(name);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getId(), second.getId(), "Duplicate tag should return same record");
    }

    @Test
    @Order(3)
    void createIfMissing_caseInsensitiveDedup() {
        String name = "Wind" + UNIQUE;
        Tag lower = tagService.createIfMissing(name.toLowerCase());
        Tag upper = tagService.createIfMissing(name.toUpperCase());
        assertNotNull(lower);
        assertNotNull(upper);
        assertEquals(lower.getId(), upper.getId(), "Tags differing only in case should be treated as duplicate");
    }

    @Test
    @Order(4)
    void createIfMissing_tooShortName_returnsNull() {
        assertNull(tagService.createIfMissing("A"), "Single-character tag should be rejected");
    }

    @Test
    @Order(5)
    void createIfMissing_startsWithDigit_returnsNull() {
        assertNull(tagService.createIfMissing("1Solar"), "Tag starting with digit should be rejected");
    }

    @Test
    @Order(6)
    void createIfMissing_emptyName_returnsNull() {
        assertNull(tagService.createIfMissing(""), "Empty tag name should be rejected");
    }

    @Test
    @Order(7)
    void createIfMissing_nullName_returnsNull() {
        assertNull(tagService.createIfMissing(null), "Null tag name should be rejected");
    }

    @Test
    @Order(8)
    void createIfMissing_trims_whitespace() {
        String name = "  Compost" + UNIQUE + "  ";
        Tag tag = tagService.createIfMissing(name);
        assertNotNull(tag, "Tag with surrounding whitespace should be accepted after trim");
        assertEquals("Compost" + UNIQUE, tag.getName());
    }

    // ── getAll ──────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void getAll_returnsList() {
        List<Tag> tags = tagService.getAll();
        assertNotNull(tags);
        assertFalse(tags.isEmpty(), "getAll should return at least one tag");
    }

    // ── countArticlesForTag ──────────────────────────────────────────────────

    @Test
    @Order(10)
    void countArticlesForTag_freshTag_returnsZero() {
        Tag tag = tagService.createIfMissing("FreshCount" + UNIQUE);
        assertNotNull(tag);
        assertEquals(0, tagService.countArticlesForTag(tag.getId()),
                "Newly created tag with no articles should have count 0");
    }

    // ── renameTag ────────────────────────────────────────────────────────────

    @Test
    @Order(11)
    void renameTag_validName_succeeds() {
        Tag tag = tagService.createIfMissing("RenameMe" + UNIQUE);
        assertNotNull(tag);
        String newName = "Renamed" + UNIQUE;
        assertTrue(tagService.renameTag(tag.getId(), newName), "Rename should succeed");

        List<Tag> all = tagService.getAll();
        assertTrue(all.stream().anyMatch(t -> newName.equals(t.getName())),
                "Renamed tag should appear in getAll");
    }

    @Test
    @Order(12)
    void renameTag_invalidName_returnsFalse() {
        Tag tag = tagService.createIfMissing("ValidRename" + UNIQUE);
        assertNotNull(tag);
        assertFalse(tagService.renameTag(tag.getId(), "1bad"), "Rename to invalid name should fail");
    }

    // ── deleteTagAndUnlinkArticles ───────────────────────────────────────────

    @Test
    @Order(13)
    void deleteTag_removesFromGetAll() {
        Tag tag = tagService.createIfMissing("ToDelete" + UNIQUE);
        assertNotNull(tag);
        assertTrue(tagService.deleteTagAndUnlinkArticles(tag.getId()), "Delete should succeed");

        List<Tag> all = tagService.getAll();
        assertFalse(all.stream().anyMatch(t -> t.getId() == tag.getId()),
                "Deleted tag should not appear in getAll");
    }

    @Test
    @Order(14)
    void getArticleTitlesForTag_freshTag_returnsEmpty() {
        Tag tag = tagService.createIfMissing("NoArticles" + UNIQUE);
        assertNotNull(tag);
        List<String> titles = tagService.getArticleTitlesForTag(tag.getId());
        assertNotNull(titles);
        assertTrue(titles.isEmpty(), "Fresh tag should have no article titles");
        tagService.deleteTagAndUnlinkArticles(tag.getId());
    }
}
