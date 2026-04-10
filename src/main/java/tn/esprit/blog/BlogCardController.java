package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class BlogCardController {

    @FXML private ImageView articleImage;
    @FXML private Label categoryLabel;
    @FXML private Label dateLabel;
    @FXML private Label titleLabel;
    @FXML private Label viewsLabel;
    @FXML private Label likesLabel;
    @FXML private Label readTimeLabel;
    @FXML private Label authorLabel;
    @FXML private Text excerptText;
    @FXML private Button readMoreBtn;

    public void setData(Blog blog) {
        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            try {
                articleImage.setImage(new Image(blog.getImage(), true));
            } catch (Exception e) {
                // Keep default if loading fails
            }
        }
        
        categoryLabel.setText(blog.getCategory() != null ? blog.getCategory().getName() : "General");
        
        if (blog.getPublishedAt() != null) {
            dateLabel.setText(blog.getPublishedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        titleLabel.setText(blog.getTitle());
        viewsLabel.setText("👁 " + blog.getViews());
        likesLabel.setText("👍 " + blog.getLikesCount());
        readTimeLabel.setText("📖 " + blog.getReadingTime() + "m");
        authorLabel.setText("👤 " + (blog.getAuthor() != null ? blog.getAuthor() : "Anonymous"));
        
        String content = blog.getContent();
        if (content != null && content.length() > 100) {
            excerptText.setText(content.substring(0, 97) + "...");
        } else {
            excerptText.setText(content);
        }

        readMoreBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/BlogDetail.fxml"));
                Parent root = loader.load();
                
                BlogDetailController detailController = loader.getController();
                detailController.setArticle(blog);
                
                Stage stage = (Stage) readMoreBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
