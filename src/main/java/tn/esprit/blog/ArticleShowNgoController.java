package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.services.BlogService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ArticleShowNgoController {

    @FXML private Label headerTitle;
    @FXML private Label idLabel;
    @FXML private Label titleLabel;
    @FXML private javafx.scene.web.WebView contentWebView;
    @FXML private Label imageLabel;
    @FXML private ImageView imagePreview;
    @FXML private Label dateLabel;

    private Blog currentBlog;
    private final BlogService blogService = new BlogService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setArticle(Blog blog) {
        this.currentBlog = blog;
        headerTitle.setText("Article: " + blog.getTitle());
        idLabel.setText(String.valueOf(blog.getId()));
        titleLabel.setText(blog.getTitle());
        contentWebView.getEngine().loadContent(blog.getContent() != null ? blog.getContent() : "");
        imageLabel.setText(blog.getImage() != null ? blog.getImage() : "No image");

        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            try {
                imagePreview.setImage(new Image(blog.getImage(), true));
            } catch (Exception ignored) {}
        }

        if (blog.getPublishedAt() != null) {
            dateLabel.setText(blog.getPublishedAt().format(formatter));
        } else {
            dateLabel.setText("Draft");
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/NewArticle.fxml"));
            Parent root = loader.load();
            NewArticleController controller = loader.getController();
            controller.setEditArticle(currentBlog);
            Stage stage = (Stage) headerTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/ArticlesManagement.fxml"));
            Stage stage = (Stage) headerTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + currentBlog.getTitle() + "\"?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                blogService.delete(currentBlog);
                handleBack();
            }
        });
    }
}
