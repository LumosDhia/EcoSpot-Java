package tn.esprit.models;

public class ArticleStatsRow {
    private int id;
    private String title;
    private String author;
    private int views;
    private int likes;
    private int dislikes;
    private int comments;

    public ArticleStatsRow(int id, String title, String author, int views, int likes, int dislikes, int comments) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
    public int getDislikes() { return dislikes; }
    public int getComments() { return comments; }
}
