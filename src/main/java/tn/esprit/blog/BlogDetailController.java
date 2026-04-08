package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BlogDetailController {

    @FXML private Label heroTitle;
    @FXML private Label heroDate;
    @FXML private Label breadcrumbTitle;
    @FXML private ImageView articleImg;
    @FXML private Label categoryLabel;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label readTimeLabel;
    @FXML private Label authorLabel;
    @FXML private Text contentText;
    
    // Comments
    @FXML private VBox commentsContainer;
    @FXML private Label commentsCountLabel;
    @FXML private TextField commentAuthorField;
    @FXML private javafx.scene.control.TextArea commentArea;

    private Blog currentArticle;
    private tn.esprit.services.CommentService commentService = new tn.esprit.services.CommentService();

    public void setArticle(Blog blog) {
        this.currentArticle = blog;
        heroTitle.setText(blog.getTitle());
        breadcrumbTitle.setText(blog.getTitle());
        titleLabel.setText(blog.getTitle());
        
        if (blog.getPublishedAt() != null) {
            String dateFormatted = blog.getPublishedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            heroDate.setText("Published on " + dateFormatted);
            dateLabel.setText("🕒 " + blog.getPublishedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            try {
                articleImg.setImage(new Image(blog.getImage(), true));
            } catch (Exception e) {}
        }
        
        categoryLabel.setText(blog.getCategory() != null ? blog.getCategory().getName() : "General");
        readTimeLabel.setText("📖 " + blog.getReadingTime() + " min read");
        authorLabel.setText("👤 Writer: " + (blog.getAuthor() != null ? blog.getAuthor() : "Admin User"));
        
        contentText.setText(blog.getContent());
        
        loadComments();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        List<Comment> comments = commentService.getByArticleId(currentArticle.getId());
        commentsCountLabel.setText("Comments (" + comments.size() + ")");

        for (Comment comment : comments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/CommentItem.fxml"));
                Parent card = loader.load();
                CommentItemController controller = loader.getController();
                controller.setData(comment);
                commentsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handlePostComment() {
        String author = commentAuthorField.getText();
        String content = commentArea.getText();

        if (author == null || author.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            // In a real app, show alert. For now, silence or simple check.
            return;
        }

        Comment comment = new Comment(author, content, currentArticle.getId());
        commentService.add(comment);
        
        // Clear fields and reload
        commentAuthorField.clear();
        commentArea.clear();
        loadComments();
    }

    @FXML
    private void goToBlog() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/BlogManagement.fxml"));
            Stage stage = (Stage) heroTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) heroTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
