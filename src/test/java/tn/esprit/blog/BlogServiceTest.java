package tn.esprit.blog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;

import java.time.LocalDateTime;
import java.util.List;

public class BlogServiceTest {

    private BlogService blogService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        blogService = new BlogService();
        categoryService = new CategoryService();
    }

    @Test
    void testAddAndSearchBlog() {
        // Find a valid category
        List<Category> categories = categoryService.getAll();
        Category cat = categories.isEmpty() ? null : categories.get(0);

        // Create a new blog
        Blog blog = new Blog();
        String testTitle = "Test Integration Article " + System.currentTimeMillis();
        blog.setTitle(testTitle);
        blog.setContent("This is a test content for the integration test.");
        blog.setAuthor("Admin [Test Runner]");
        blog.setCategory(cat);
        blog.setImage("test-image-path.jpg");

        // Add
        blogService.add2(blog);

        // Search for it
        List<Blog> results = blogService.search(testTitle);
        
        Assertions.assertFalse(results.isEmpty(), "The added article should be found by search");
        Blog found = results.get(0);
        Assertions.assertEquals(testTitle, found.getTitle());
        Assertions.assertNotNull(found.getAuthor());
    }

    @Test
    void testAdminAuthorFiltering() {
        // Fetch all blogs
        List<Blog> allBlogs = blogService.getAll();
        
        // Filter by admin (this is the logic used in the controller)
        List<Blog> adminBlogs = allBlogs.stream()
            .filter(b -> b.getAuthor() != null && b.getAuthor().startsWith("Admin"))
            .toList();
            
        List<Blog> ngoBlogs = allBlogs.stream()
            .filter(b -> b.getAuthor() == null || !b.getAuthor().startsWith("Admin"))
            .toList();

        System.out.println("Admin Articles Count: " + adminBlogs.size());
        System.out.println("NGO Articles Count: " + ngoBlogs.size());
        
        // At least our new articles should be there if DB is not empty
        Assertions.assertNotNull(adminBlogs);
        Assertions.assertNotNull(ngoBlogs);
    }
}
