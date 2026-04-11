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
    @FXML private javafx.scene.web.WebView contentWebView;
    
    // Comments
    @FXML private VBox commentsContainer;
    @FXML private Label commentsCountLabel;
    @FXML private TextField commentAuthorField;
    @FXML private javafx.scene.control.TextArea commentArea;
    @FXML private Label commentErrorLabel;
    @FXML private Label commentSuccessLabel;
    @FXML private VBox commentInputSection;
    @FXML private VBox guestPromptSection;

    private Blog currentArticle;
    private tn.esprit.services.CommentService commentService = new tn.esprit.services.CommentService();

    @FXML
    public void initialize() {
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            commentInputSection.setVisible(true);
            commentInputSection.setManaged(true);
            guestPromptSection.setVisible(false);
            guestPromptSection.setManaged(false);
            
            // Pre-fill author name
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user != null) {
                commentAuthorField.setText(user.getUsername());
                commentAuthorField.setEditable(false); // They must use their account name
            }
        } else {
            commentInputSection.setVisible(false);
            commentInputSection.setManaged(false);
            guestPromptSection.setVisible(true);
            guestPromptSection.setManaged(true);
        }
    }

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
        
        contentWebView.getEngine().loadContent(blog.getContent());
        
        loadComments();
    }

    private void loadComments() {
        if (currentArticle == null) return;
        
        commentsContainer.getChildren().clear();
        List<Comment> comments = commentService.getByArticleId(currentArticle.getId());
        System.out.println("Loading comments for article ID: " + currentArticle.getId() + ". Found: " + comments.size());
        commentsCountLabel.setText("Comments (" + comments.size() + ")");

        for (Comment comment : comments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/CommentItem.fxml"));
                Parent card = loader.load();
                CommentItemController controller = loader.getController();
                controller.setData(comment);
                commentsContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Error loading comment card for " + comment.getAuthorName());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handlePostComment() {
        commentErrorLabel.setVisible(false);
        commentSuccessLabel.setVisible(false);

        if (currentArticle == null || currentArticle.getId() <= 0) {
            showError("This article is not saved in the database yet.");
            return;
        }

        String author = commentAuthorField.getText();
        String content = commentArea.getText();

        // 1. Basic empty check
        if (content == null || content.trim().isEmpty()) {
            showError("Your comment cannot be empty.");
            return;
        }

        // 2. Length control
        String trimmed = content.trim();
        if (trimmed.length() < 5) {
            showError("Your comment is too short (min 5 chars).");
            return;
        }
        if (trimmed.length() > 500) {
            showError("Your comment is too long (max 500 chars).");
            return;
        }

        // 3. Numeric checks
        if (trimmed.matches("\\d+")) {
            showError("Your comment cannot be only numbers.");
            return;
        }
        if (Character.isDigit(trimmed.charAt(0))) {
            showError("Your comment cannot start with a number.");
            return;
        }

        // 4. Simple Bad Words Filter
        String[] badWords = {"badword1", "badword2", "spam", "offensive"}; // Example list
        for (String word : badWords) {
            if (trimmed.toLowerCase().contains(word)) {
                showError("Your comment contains inappropriate language.");
                return;
            }
        }

        // 4. Post comment
        Comment comment = new Comment(author, trimmed, currentArticle.getId());
        boolean inserted = commentService.add(comment);
        if (!inserted) {
            String dbError = commentService.getLastErrorMessage();
            if (dbError != null && !dbError.isBlank()) {
                showError("Failed to post comment: " + dbError);
            } else {
                showError("Failed to post comment. Please try again.");
            }
            return;
        }
        
        // Clear fields and reload
        commentArea.clear();
        commentErrorLabel.setVisible(false);
        showSuccess("Comment posted successfully!");
        loadComments();
    }

    private void showSuccess(String message) {
        commentSuccessLabel.setText("✅ " + message);
        commentSuccessLabel.setVisible(true);
        // Optional: Hide after 3 seconds could be added here
    }

    private void showError(String message) {
        commentSuccessLabel.setVisible(false);
        commentErrorLabel.setText("❌ " + message);
        commentErrorLabel.setVisible(true);
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

    @FXML
    private void goToArticles() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/ArticlesManagement.fxml"));
            Stage stage = (Stage) heroTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(heroTitle);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(heroTitle);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(heroTitle);
    }

    @FXML
    private void goToLogin(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/Login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
