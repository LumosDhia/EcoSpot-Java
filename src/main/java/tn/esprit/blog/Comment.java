package tn.esprit.blog;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int articleId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    public Comment() {}

    public Comment(int id, int articleId, String authorName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Comment(String authorName, String content, int articleId) {
        this.authorName = authorName;
        this.content = content;
        this.articleId = articleId;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
