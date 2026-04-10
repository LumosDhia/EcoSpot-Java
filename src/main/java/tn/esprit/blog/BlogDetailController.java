package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class BlogDetailController {

    @FXML private Label heroTitle;
    @FXML private Label heroDate;
    @FXML private Label breadcrumbTitle;
    @FXML private ImageView articleImg;
    @FXML private Label categoryLabel;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label readTimeLabel;
    @FXML private Label viewsLabel;
    @FXML private Label likesLabel;
    @FXML private Label authorLabel;
    @FXML private Text contentText;

    public void setArticle(Blog blog) {
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
        viewsLabel.setText("👁 " + blog.getViews() + " views");
        likesLabel.setText("👍 " + blog.getLikesCount());
        authorLabel.setText("👤 Writer: " + (blog.getAuthor() != null ? blog.getAuthor() : "Admin User"));
        
        contentText.setText(blog.getContent());
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
