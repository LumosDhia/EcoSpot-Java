package tn.esprit.blog;

import java.time.LocalDateTime;

public class Blog {
    private int id;
    private String title;
    private String content;
    private String author;
    private String image;
    private LocalDateTime publishedAt;
    private Category category;
    private int views;
    private int likesCount;
    private int dislikesCount;
    private int readingTime;
    private int commentsCount;

    public Blog() {}

    public Blog(int id, String title, String content, String author, String image, LocalDateTime publishedAt, Category category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.image = image;
        this.publishedAt = publishedAt;
        this.category = category;
        this.views = 0;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.readingTime = 5; // Default 5 min
        this.commentsCount = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public void setDislikesCount(int dislikesCount) { this.dislikesCount = dislikesCount; }
    public int getReadingTime() {
        if (content == null || content.isEmpty()) return 0;
        String[] words = content.split("\\s+");
        return (int) Math.ceil(words.length / 200.0);
    }

    public void setReadingTime(int readingTime) { this.readingTime = readingTime; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
}
