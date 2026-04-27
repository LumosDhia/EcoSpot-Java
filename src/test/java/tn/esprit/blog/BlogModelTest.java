package tn.esprit.blog;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pure unit tests for Blog model — no database required. */
public class BlogModelTest {

    // ── getReadingTime ──────────────────────────────────────────────────────

    @Test
    void readingTime_nullContent_returnsZero() {
        Blog blog = new Blog();
        blog.setContent(null);
        assertEquals(0, blog.getReadingTime());
    }

    @Test
    void readingTime_blankContent_returnsZero() {
        Blog blog = new Blog();
        blog.setContent("   ");
        assertEquals(0, blog.getReadingTime());
    }

    @Test
    void readingTime_calculatesFromWordCount() {
        Blog blog = new Blog();
        // 200 words → ceil(200/200) = 1 min
        String content = "word ".repeat(200).trim();
        blog.setContent(content);
        assertEquals(1, blog.getReadingTime());
    }

    @Test
    void readingTime_roundsUp() {
        Blog blog = new Blog();
        // 201 words → ceil(201/200) = 2 min
        String content = "word ".repeat(201).trim();
        blog.setContent(content);
        assertEquals(2, blog.getReadingTime());
    }

    @Test
    void readingTime_storedValueTakesPrecedence() {
        Blog blog = new Blog();
        blog.setContent("word ".repeat(400).trim()); // would calculate to 2
        blog.setReadingTime(7);
        assertEquals(7, blog.getReadingTime());
    }

    @Test
    void readingTime_storedZeroFallsBackToCalculation() {
        Blog blog = new Blog();
        blog.setContent("word ".repeat(200).trim());
        blog.setReadingTime(0); // 0 means "not stored"
        assertEquals(1, blog.getReadingTime());
    }

    // ── slug ────────────────────────────────────────────────────────────────

    @Test
    void slug_defaultIsNull() {
        Blog blog = new Blog();
        assertNull(blog.getSlug());
    }

    @Test
    void slug_setAndGet() {
        Blog blog = new Blog();
        blog.setSlug("saving-the-ocean-123");
        assertEquals("saving-the-ocean-123", blog.getSlug());
    }

    // ── isPublished ─────────────────────────────────────────────────────────

    @Test
    void isPublished_defaultFalse() {
        Blog blog = new Blog();
        assertFalse(blog.getIsPublished());
    }

    @Test
    void isPublished_setTrue() {
        Blog blog = new Blog();
        blog.setIsPublished(true);
        assertTrue(blog.getIsPublished());
    }

    // ── tags ────────────────────────────────────────────────────────────────

    @Test
    void tags_defaultEmptyList() {
        Blog blog = new Blog();
        assertNotNull(blog.getTags());
        assertTrue(blog.getTags().isEmpty());
    }

    @Test
    void tags_setAndGet() {
        Blog blog = new Blog();
        Tag t = new Tag(1, "Recycling");
        blog.setTags(List.of(t));
        assertEquals(1, blog.getTags().size());
        assertEquals("Recycling", blog.getTags().get(0).getName());
    }

    // ── category ────────────────────────────────────────────────────────────

    @Test
    void category_setAndGet() {
        Blog blog = new Blog();
        Category cat = new Category(3, "Environment");
        blog.setCategory(cat);
        assertEquals(3, blog.getCategory().getId());
        assertEquals("Environment", blog.getCategory().getName());
    }

    // ── full constructor ─────────────────────────────────────────────────────

    @Test
    void fullConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Category cat = new Category(1, "Community");
        Blog blog = new Blog(42, "Title", "Content", "Author", "img.jpg", now, cat);

        assertEquals(42, blog.getId());
        assertEquals("Title", blog.getTitle());
        assertEquals("Content", blog.getContent());
        assertEquals("Author", blog.getAuthor());
        assertEquals("img.jpg", blog.getImage());
        assertEquals(now, blog.getPublishedAt());
        assertEquals(cat, blog.getCategory());
        assertEquals(0, blog.getViews());
        assertEquals(0, blog.getLikesCount());
        assertEquals(0, blog.getDislikesCount());
        assertEquals(0, blog.getCommentsCount());
    }
}
