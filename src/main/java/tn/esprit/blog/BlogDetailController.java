package tn.esprit.blog;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import tn.esprit.util.TimeUtils;


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
    @FXML private Label viewsLabel;
    @FXML private WebView contentWebView;
    @FXML private FlowPane tagsFlowPane;
    @FXML private ImageView currentUserAvatar;

    // Comments
    @FXML private VBox commentsContainer;
    @FXML private Label commentsCountLabel;
    @FXML private TextField commentAuthorField;
    @FXML private javafx.scene.control.TextArea commentArea;
    @FXML private Label commentErrorLabel;
    @FXML private Label commentSuccessLabel;
    @FXML private VBox commentInputSection;
    @FXML private VBox guestPromptSection;

    // AI Voice Reader
    @FXML private HBox aiReaderPanel;
    @FXML private Label aiStatusLabel;
    @FXML private Button aiPlayBtn;
    @FXML private Button aiPauseBtn;
    @FXML private Button aiStopBtn;

    private static final int WORDS_PER_MINUTE = 150;

    @FXML private Button likeBtn;
    @FXML private Button dislikeBtn;

    private Blog currentArticle;
    private final tn.esprit.services.CommentService commentService = new tn.esprit.services.CommentService();
    private final tn.esprit.services.ReactionService reactionService = new tn.esprit.services.ReactionService();
    private final tn.esprit.services.BlogService blogService = new tn.esprit.services.BlogService();
    private final tn.esprit.services.UserService userService = new tn.esprit.services.UserService();

    // TTS state
    private Process ttsProcess;
    private volatile boolean isPlaying = false;
    private long playStartTime;
    private String remainingText;

    private static boolean schemaFixed = false;

    @FXML
    public void initialize() {
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            commentInputSection.setVisible(true);
            commentInputSection.setManaged(true);
            guestPromptSection.setVisible(false);
            guestPromptSection.setManaged(false);
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user != null) {
                commentAuthorField.setText(user.getUsername());
                commentAuthorField.setEditable(false);
                loadCurrentUserAvatar();
            }
        } else {
            commentInputSection.setVisible(false);
            commentInputSection.setManaged(false);
            guestPromptSection.setVisible(true);
            guestPromptSection.setManaged(true);
        }

        // Removed destructive schema fix that was wiping stats


        contentWebView.addEventFilter(ScrollEvent.SCROLL, event -> {
            ScrollPane parentScroll = findParentScrollPane(contentWebView);
            if (parentScroll != null) {
                double deltaY = event.getDeltaY();
                double vvalue = parentScroll.getVvalue();
                double contentHeight = parentScroll.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = parentScroll.getViewportBounds().getHeight();
                double scrollableHeight = contentHeight - viewportHeight;
                if (scrollableHeight > 0) {
                    parentScroll.setVvalue(vvalue - (deltaY / scrollableHeight));
                }
                event.consume();
            }
        });
    }

    private ScrollPane findParentScrollPane(javafx.scene.Node node) {
        javafx.scene.Node current = node.getParent();
        while (current != null) {
            if (current instanceof ScrollPane) return (ScrollPane) current;
            current = current.getParent();
        }
        return null;
    }

    private void loadCurrentUserAvatar() {
        tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
        if (user != null && currentUserAvatar != null) {
            String style = user.getAvatarStyle() != null ? user.getAvatarStyle() : "avataaars";
            String url = tn.esprit.services.AvatarService.getAvatarUrl(user.getUsername(), style);
            currentUserAvatar.setImage(new Image(url, true));
        }
    }

    @FXML
    private void handleLike() {
        if (!tn.esprit.util.SessionManager.isLoggedIn()) { showError("You must be logged in to react."); return; }
        int userId = tn.esprit.util.SessionManager.getCurrentUser().getId();
        reactionService.toggleReaction(currentArticle.getId(), userId, "like");
        refreshReactions(userId);
    }

    @FXML
    private void handleDislike() {
        if (!tn.esprit.util.SessionManager.isLoggedIn()) { showError("You must be logged in to react."); return; }
        int userId = tn.esprit.util.SessionManager.getCurrentUser().getId();
        reactionService.toggleReaction(currentArticle.getId(), userId, "dislike");
        refreshReactions(userId);
    }

    private void refreshReactions(int userId) {
        int likes = reactionService.getLikes(currentArticle.getId());
        int dislikes = reactionService.getDislikes(currentArticle.getId());
        String userReaction = reactionService.getUserReaction(currentArticle.getId(), userId);
        
        likeBtn.setText("👍 " + likes);
        dislikeBtn.setText("👎 " + dislikes);
        likeBtn.getStyleClass().removeAll("reaction-active");
        dislikeBtn.getStyleClass().removeAll("reaction-active");
        if ("like".equals(userReaction)) likeBtn.getStyleClass().add("reaction-active");
        else if ("dislike".equals(userReaction)) dislikeBtn.getStyleClass().add("reaction-active");
    }

    private static Image fallbackImage() {
        return new Image("https://images.unsplash.com/photo-1527330772182-997f9850cab9?q=80&w=1469&auto=format&fit=crop", true);
    }

    private void loadThumbnail(String url) {
        articleImg.setImage(fallbackImage());
        if (url == null || url.isEmpty()) return;
        try {
            Image img = new Image(url, true);
            img.errorProperty().addListener((obs, old, err) -> {
                if (err) articleImg.setImage(fallbackImage());
            });
            img.progressProperty().addListener((obs, old, prog) -> {
                if (prog.doubleValue() >= 1.0 && !img.isError()) articleImg.setImage(img);
            });
        } catch (Exception e) {
            // fallback already set
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
            dateLabel.setText("🕒 " + TimeUtils.formatRelativeTime(blog.getPublishedAt()));
        }

        loadThumbnail(blog.getImage());

        categoryLabel.setText(blog.getCategory() != null ? blog.getCategory().getName() : "General");
        categoryLabel.setStyle("-fx-cursor: hand;");
        categoryLabel.setOnMouseClicked(e -> {
            BlogManagementController.selectedTag = null;
            BlogManagementController.selectedCategory = blog.getCategory();
            goToBlog();
        });
        readTimeLabel.setText("📖 " + blog.getReadingTime() + " min read");
        authorLabel.setText("👤 Writer: " + (blog.getAuthor() != null ? blog.getAuthor() : "Admin User"));
        authorLabel.setStyle("-fx-cursor: hand;");
        authorLabel.setOnMouseClicked(e -> {
            BlogManagementController.selectedCategory = null;
            BlogManagementController.selectedTag = null;
            BlogManagementController.selectedAuthor = blog.getAuthor();
            goToBlog();
        });
        
        // Views functionality
        String viewerId = tn.esprit.util.SessionManager.isLoggedIn() 
                ? "user_" + tn.esprit.util.SessionManager.getCurrentUser().getId() 
                : "guest_" + System.getProperty("user.name", "unknown");

        boolean incremented = blogService.incrementViews(blog.getId(), viewerId);
        int displayViews = incremented ? blog.getViews() + 1 : blog.getViews();
        viewsLabel.setText("👁 " + displayViews + " views");

        // Populate Tags
        tagsFlowPane.getChildren().clear();
        if (blog.getTags() != null) {
            for (Tag tag : blog.getTags()) {
                Label tagLabel = new Label("#" + tag.getName());
                tagLabel.getStyleClass().add("tag-badge");
                tagLabel.setStyle("-fx-cursor: hand;");
                tagLabel.setOnMouseClicked(e -> {
                    BlogManagementController.selectedCategory = null;
                    BlogManagementController.selectedTag = tag;
                    goToBlog();
                });
                tagsFlowPane.getChildren().add(tagLabel);
            }
        }

        contentWebView.getEngine().loadContent(blog.getContent());

        // Auto-resize WebView to fit content (eliminates internal scrollbar)
        contentWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    try {
                        Object heightObj = contentWebView.getEngine().executeScript(
                            "Math.max(document.body.scrollHeight, document.documentElement.scrollHeight)"
                        );
                        if (heightObj instanceof Number) {
                            double h = ((Number) heightObj).doubleValue() + 30;
                            contentWebView.setPrefHeight(h);
                            contentWebView.setMinHeight(h);
                            contentWebView.setMaxHeight(h);
                        }
                    } catch (Exception ignored) {}
                });
            }
        });

        loadComments();

            int userId = tn.esprit.util.SessionManager.isLoggedIn()
                    ? tn.esprit.util.SessionManager.getCurrentUser().getId() : -1;
            refreshReactions(userId);
    }


    // ─── AI Voice Reader ────────────────────────────────────────────────────────

    @FXML
    private void handleTtsPlay() {
        if (isPlaying) return;

        if (remainingText == null) {
            remainingText = extractPlainText();
        }

        if (remainingText == null || remainingText.isBlank()) {
            aiStatusLabel.setText("No content to read");
            return;
        }

        startSpeaking(remainingText);
    }

    @FXML
    private void handleTtsPause() {
        if (!isPlaying) return;

        // Estimate how many words were spoken based on elapsed time
        long elapsedMs = System.currentTimeMillis() - playStartTime;
        int wordsSpoken = (int) (elapsedMs / 1000.0 / 60.0 * WORDS_PER_MINUTE);
        String[] words = remainingText.split("\\s+");
        remainingText = wordsSpoken < words.length
                ? String.join(" ", Arrays.copyOfRange(words, wordsSpoken, words.length))
                : null;

        killProcess();
        isPlaying = false;
        aiReaderPanel.getStyleClass().remove("ai-reading");
        aiPlayBtn.setDisable(false);
        aiPauseBtn.setDisable(true);
        aiStatusLabel.setText("⏸ Paused");
    }

    @FXML
    private void handleTtsStop() {
        killProcess();
        isPlaying = false;
        remainingText = null;
        aiReaderPanel.getStyleClass().remove("ai-reading");
        aiPlayBtn.setDisable(false);
        aiPauseBtn.setDisable(true);
        aiStatusLabel.setText("Ready to read aloud");
    }

    private void startSpeaking(String text) {
        killProcess();
        try {
            java.io.File tmp = java.io.File.createTempFile("ecospot_tts_", ".txt");
            tmp.deleteOnExit();
            Files.writeString(tmp.toPath(), text, StandardCharsets.UTF_8);

            String path = tmp.getAbsolutePath().replace("\\", "\\\\");
            String ps = String.format(
                "Add-Type -AssemblyName System.Speech; " +
                "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$s.Rate = 1; " +
                "$s.Speak([System.IO.File]::ReadAllText('%s', [System.Text.Encoding]::UTF8))",
                path
            );

            ttsProcess = new ProcessBuilder("powershell", "-NonInteractive", "-Command", ps)
                    .redirectErrorStream(true)
                    .start();

            isPlaying = true;
            playStartTime = System.currentTimeMillis();

            if (!aiReaderPanel.getStyleClass().contains("ai-reading"))
                aiReaderPanel.getStyleClass().add("ai-reading");
            aiPlayBtn.setDisable(true);
            aiPauseBtn.setDisable(false);
            aiStatusLabel.setText("🔊 Reading aloud...");

            Thread watcher = new Thread(() -> {
                try {
                    ttsProcess.waitFor();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                if (isPlaying) {
                    isPlaying = false;
                    remainingText = null;
                    Platform.runLater(() -> {
                        aiReaderPanel.getStyleClass().remove("ai-reading");
                        aiPlayBtn.setDisable(false);
                        aiPauseBtn.setDisable(true);
                        aiStatusLabel.setText("✅ Finished");
                    });
                }
            });
            watcher.setDaemon(true);
            watcher.start();

        } catch (Exception e) {
            aiStatusLabel.setText("TTS error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractPlainText() {
        // Try to get plain text from the WebView first (strips HTML naturally)
        try {
            Object result = contentWebView.getEngine().executeScript(
                "document.body ? document.body.innerText : ''"
            );
            if (result != null && !result.toString().isBlank()) return result.toString();
        } catch (Exception ignored) {}

        // Fallback: strip HTML tags manually
        return currentArticle != null
                ? currentArticle.getContent().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim()
                : "";
    }

    private void killProcess() {
        if (ttsProcess != null && ttsProcess.isAlive()) {
            ttsProcess.destroyForcibly();
        }
    }

    // ─── Comments ───────────────────────────────────────────────────────────────

    private void loadComments() {
        try {
            if (currentArticle == null) return;
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
        } catch (Exception e) {
            System.err.println("ERROR in loadComments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePostComment() {
        commentErrorLabel.setVisible(false);
        commentSuccessLabel.setVisible(false);

        if (currentArticle == null || currentArticle.getId() <= 0) { showError("Article not in database."); return; }

        String content = commentArea.getText();
        if (content == null || content.trim().isEmpty()) { showError("Comment cannot be empty."); return; }
        String trimmed = content.trim();
        if (trimmed.length() < 5) { showError("Comment too short (min 5 chars)."); return; }
        if (trimmed.length() > 500) { showError("Comment too long (max 500 chars)."); return; }
        if (trimmed.matches("\\d+")) { showError("Comment cannot be only numbers."); return; }
        if (Character.isDigit(trimmed.charAt(0))) { showError("Comment cannot start with a number."); return; }

        String[] badWords = {"badword1", "badword2", "spam", "offensive"};
        for (String word : badWords) {
            if (trimmed.toLowerCase().contains(word)) { showError("Comment contains inappropriate language."); return; }
        }

        Comment comment = new Comment(commentAuthorField.getText(), trimmed, currentArticle.getId());
        if (!commentService.add(comment)) {
            String reason = commentService.getLastErrorMessage();
            showError(reason != null ? reason : "Failed to post comment. Please try again.");
            return;
        }
        commentArea.clear();
        showSuccess("Comment posted successfully!");
        loadComments();
    }

    private void showSuccess(String msg) { commentSuccessLabel.setText("✅ " + msg); commentSuccessLabel.setVisible(true); }
    private void showError(String msg) { commentSuccessLabel.setVisible(false); commentErrorLabel.setText("❌ " + msg); commentErrorLabel.setVisible(true); }

    // ─── Navigation ─────────────────────────────────────────────────────────────

    @FXML
    private void goToBlog() {
        stopTtsAndNavigate("/blog/BlogManagement.fxml", null);
    }

    @FXML
    private void goToBlogReset() {
        tn.esprit.blog.BlogManagementController.selectedCategory = null;
        tn.esprit.blog.BlogManagementController.selectedTag = null;
        tn.esprit.blog.BlogManagementController.selectedAuthor = null;
        goToBlog();
    }
    @FXML private void goToHome() { stopTtsAndNavigate("/home/Home.fxml", null); }
    @FXML private void goToArticles() { stopTtsAndNavigate("/blog/ArticlesManagement.fxml", null); }
    @FXML private void goToEvents(javafx.event.ActionEvent e) { handleTtsStop(); navigate(e, "/event/EventManagement.fxml"); }
    @FXML private void goToTickets(javafx.event.ActionEvent e) { handleTtsStop(); navigate(e, "/ticket/TicketManagement.fxml"); }
    @FXML private void goToAchievements(javafx.event.ActionEvent e) { handleTtsStop(); navigate(e, "/ticket/Achievements.fxml"); }
    @FXML private void goToLogin(javafx.event.ActionEvent e) { navigate(e, "/user/Login.fxml"); }

    private void stopTtsAndNavigate(String fxmlPath, javafx.event.ActionEvent event) {
        handleTtsStop();
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) heroTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(heroTitle); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(heroTitle); }
    @FXML private void handleClose() { handleTtsStop(); tn.esprit.util.WindowUtils.close(heroTitle); }
}
