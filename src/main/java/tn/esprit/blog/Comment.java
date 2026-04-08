package tn.esprit.blog;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private int articleId;
    private int authorId; // Reference to Personne/User ID
    private boolean flagged;

    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.flagged = false;
    }

    public Comment(String author, String content, int articleId) {
        this();
        this.author = author;
        this.content = content;
        this.articleId = articleId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}
