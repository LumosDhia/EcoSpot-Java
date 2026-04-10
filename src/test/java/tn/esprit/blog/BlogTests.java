package tn.esprit.blog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlogTests {

    @Test
    void testReadingTimeCalculation() {
        Blog blog = new Blog();
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 450; i++) {
            content.append("word ");
        }
        blog.setContent(content.toString());
        
        // 450 words / 200 = 2.25 -> ceilings to 3
        Assertions.assertEquals(3, blog.getReadingTime(), "Reading time calculation should be 3 minutes for 450 words");
    }

    @Test
    void testReadingTimeEmptyContent() {
        Blog blog = new Blog();
        blog.setContent("");
        Assertions.assertEquals(0, blog.getReadingTime(), "Reading time should be 0 for empty content");
    }

    @Test
    void testSettersAndGetters() {
        Blog blog = new Blog();
        blog.setTitle("Test Title");
        blog.setAuthor("Test Author");
        blog.setViews(100);
        
        Assertions.assertEquals("Test Title", blog.getTitle());
        Assertions.assertEquals("Test Author", blog.getAuthor());
        Assertions.assertEquals(100, blog.getViews());
    }
}
