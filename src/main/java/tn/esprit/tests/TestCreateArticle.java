package tn.esprit.tests;

import tn.esprit.blog.Blog;
import tn.esprit.blog.Category;
import tn.esprit.services.BlogService;

public class TestCreateArticle {
    public static void main(String[] args) {
        BlogService blogService = new BlogService();
        
        Blog testArticle = new Blog();
        testArticle.setTitle("Automated Test Article: " + System.currentTimeMillis());
        testArticle.setContent("<h1>Programmatic Content</h1><p>This article was created via Java code directly using the <b>BlogService</b>.</p><p>It demonstrates that the database integration is working perfectly.</p>");
        testArticle.setImage("https://images.unsplash.com/photo-1518173946687-a4c8a98039f5?w=800&q=80");
        
        // Set category to null for the test to avoid foreign key constraints if ID 1 doesn't exist
        testArticle.setCategory(null);
        
        System.out.println("Attempting to create article...");
        blogService.add2(testArticle);
        System.out.println("Done!");
    }
}
