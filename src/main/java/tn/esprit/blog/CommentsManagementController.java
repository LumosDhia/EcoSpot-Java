package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.CommentService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentsManagementController {

    // Admin
    @FXML private HBox adminHeaderBox;
    @FXML private Label breadcrumbPrefixLabel;
    @FXML private Label breadcrumbTitleLabel;
    @FXML private Label sectionTitleLabel;
    @FXML private Label sectionSubtitleLabel;
    @FXML private VBox adminSectionBox;
    @FXML private TableView<Comment> commentsTable;
    @FXML private TableColumn<Comment, Integer> colId;
    @FXML private TableColumn<Comment, String> colAuthor;
    @FXML private TableColumn<Comment, String> colArticle;
    @FXML private TableColumn<Comment, LocalDateTime> colDate;
    @FXML private TableColumn<Comment, String> colFlagged;
    @FXML private TableColumn<Comment, Void> colActions;

    // NGO
    @FXML private VBox ngoSectionBox;
    @FXML private TableView<Comment> ngoOwnCommentsTable;
    @FXML private TableColumn<Comment, Integer> colOwnId;
    @FXML private TableColumn<Comment, String> colOwnArticle;
    @FXML private TableColumn<Comment, LocalDateTime> colOwnDate;
    @FXML private TableColumn<Comment, String> colOwnFlagged;
    @FXML private TableColumn<Comment, Void> colOwnActions;
    @FXML private TableView<Comment> ngoArticleCommentsTable;
    @FXML private TableColumn<Comment, Integer> colArticleCommentId;
    @FXML private TableColumn<Comment, String> colArticleCommentAuthor;
    @FXML private TableColumn<Comment, String> colArticleCommentArticle;
    @FXML private TableColumn<Comment, LocalDateTime> colArticleCommentDate;
    @FXML private TableColumn<Comment, String> colArticleCommentFlagged;
    @FXML private TableColumn<Comment, Void> colArticleCommentActions;
    @FXML private Button homeBtn;

    private final CommentService commentService = new CommentService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean isAdmin;

    @FXML
    public void initialize() {
        tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
        isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        tn.esprit.util.NavigationHistory.track(homeBtn, "/blog/CommentsManagement.fxml");
        configureLayoutByRole();
        setupAdminTable();
        setupNgoTables();
        loadData();
    }

    private void configureLayoutByRole() {
        if (isAdmin) {
            adminSectionBox.setVisible(true);
            adminSectionBox.setManaged(true);
            ngoSectionBox.setVisible(false);
            ngoSectionBox.setManaged(false);
            adminHeaderBox.setVisible(true);
            adminHeaderBox.setManaged(true);
            breadcrumbPrefixLabel.setText("Admin › ");
            breadcrumbTitleLabel.setText("Comments moderation");
            sectionTitleLabel.setText("Flagged comments queue");
            sectionSubtitleLabel.setText("Flagged comments are hidden from article pages until accepted.");
        } else {
            adminSectionBox.setVisible(false);
            adminSectionBox.setManaged(false);
            ngoSectionBox.setVisible(true);
            ngoSectionBox.setManaged(true);
            adminHeaderBox.setVisible(false);
            adminHeaderBox.setManaged(false);
            breadcrumbPrefixLabel.setText("NGO › ");
            breadcrumbTitleLabel.setText("Comments");
            sectionTitleLabel.setText("My comments");
            sectionSubtitleLabel.setText("Edit your own comments. Flag comments on your articles for admin review.");
        }
    }

    private void setupAdminTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        colArticle.setCellValueFactory(new PropertyValueFactory<>("articleTitle"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setCellFactory(column -> dateCell());
        colFlagged.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().isFlagged() ? "Yes" : "No")
        );

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button acceptBtn = new Button("Accept");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(6, viewBtn, acceptBtn, deleteBtn);

            {
                viewBtn.setStyle("-fx-text-fill: #fdae6b; -fx-background-color: transparent; -fx-border-color: #fdae6b; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                acceptBtn.setStyle("-fx-text-fill: #2d6a4f; -fx-background-color: transparent; -fx-border-color: #2d6a4f; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-text-fill: #bc4749; -fx-background-color: transparent; -fx-border-color: #bc4749; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                acceptBtn.setOnAction(e -> handleAccept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Comment comment = getTableView().getItems().get(getIndex());
                acceptBtn.setVisible(comment.isFlagged());
                acceptBtn.setManaged(comment.isFlagged());
                setGraphic(pane);
            }
        });
    }

    private void setupNgoTables() {
        colOwnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOwnArticle.setCellValueFactory(new PropertyValueFactory<>("articleTitle"));
        colOwnDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colOwnDate.setCellFactory(column -> dateCell());
        colOwnFlagged.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().isFlagged() ? "Flagged (hidden)" : "Visible")
        );
        colOwnActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(6, viewBtn, editBtn, deleteBtn);
            {
                viewBtn.setStyle("-fx-text-fill: #fdae6b; -fx-background-color: transparent; -fx-border-color: #fdae6b; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                editBtn.setStyle("-fx-text-fill: #3182ce; -fx-background-color: transparent; -fx-border-color: #3182ce; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-text-fill: #bc4749; -fx-background-color: transparent; -fx-border-color: #bc4749; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Comment comment = getTableView().getItems().get(getIndex());
                editBtn.setDisable(comment.isFlagged());
                setGraphic(pane);
            }
        });

        colArticleCommentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colArticleCommentAuthor.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        colArticleCommentArticle.setCellValueFactory(new PropertyValueFactory<>("articleTitle"));
        colArticleCommentDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colArticleCommentDate.setCellFactory(column -> dateCell());
        colArticleCommentFlagged.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().isFlagged() ? "Flagged (hidden)" : "Visible")
        );
        colArticleCommentActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button flagBtn = new Button("Flag");
            private final HBox pane = new HBox(6, viewBtn, flagBtn);
            {
                viewBtn.setStyle("-fx-text-fill: #fdae6b; -fx-background-color: transparent; -fx-border-color: #fdae6b; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                flagBtn.setStyle("-fx-text-fill: #bc4749; -fx-background-color: transparent; -fx-border-color: #bc4749; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                flagBtn.setOnAction(e -> handleFlag(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Comment comment = getTableView().getItems().get(getIndex());
                flagBtn.setVisible(!comment.isFlagged());
                flagBtn.setManaged(!comment.isFlagged());
                setGraphic(pane);
            }
        });
    }

    private TableCell<Comment, LocalDateTime> dateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        };
    }

    private void loadData() {
        if (isAdmin) {
            List<Comment> comments = commentService.getAllCommentsForAdmin();
            commentsTable.setItems(FXCollections.observableArrayList(comments));
        } else {
            List<Comment> ownComments = commentService.getCommentsAuthoredByCurrentUser();
            List<Comment> commentsOnMyArticles = commentService.getCommentsOnCurrentUserArticles();
            ngoOwnCommentsTable.setItems(FXCollections.observableArrayList(ownComments));
            ngoArticleCommentsTable.setItems(FXCollections.observableArrayList(commentsOnMyArticles));
        }
    }

    private void handleView(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Comment details");
        alert.setHeaderText(comment.getAuthorName() + " on " + (comment.getArticleTitle() == null ? "Unknown article" : comment.getArticleTitle()));
        alert.setContentText(comment.getContent());
        alert.showAndWait();
    }

    private void handleEdit(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Edit comment");
        dialog.setHeaderText("Edit your comment on: " + (comment.getArticleTitle() != null ? comment.getArticleTitle() : "article"));
        dialog.setContentText("Content:");
        dialog.showAndWait().ifPresent(newContent -> {
            String trimmed = newContent == null ? "" : newContent.trim();
            if (trimmed.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Comment cannot be empty.").showAndWait();
                return;
            }
            if (trimmed.length() < 5) {
                new Alert(Alert.AlertType.WARNING, "Comment too short (min 5 chars).").showAndWait();
                return;
            }
            if (trimmed.length() > 500) {
                new Alert(Alert.AlertType.WARNING, "Comment too long (max 500 chars).").showAndWait();
                return;
            }
            if (commentService.updateContent(comment.getId(), trimmed)) {
                loadData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update comment.").showAndWait();
            }
        });
    }

    private void handleDelete(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this comment?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentService.delete(comment.getId());
                loadData();
            }
        });
    }

    private void handleAccept(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Accept this flagged comment and make it visible again?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (commentService.acceptFlaggedComment(comment.getId())) {
                    loadData();
                }
            }
        });
    }

    private void handleFlag(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Flag this comment? It will be hidden until an admin accepts it.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (commentService.flagComment(comment.getId())) {
                    loadData();
                }
            }
        });
    }

    @FXML
    private void goToHome(javafx.event.ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void goToHomeMouse(javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBlog(javafx.event.ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
    }

    @FXML
    private void goToEvents(javafx.event.ActionEvent event) {
        navigate(event, "/event/EventManagement.fxml");
    }

    @FXML
    private void goToTickets(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/TicketManagement.fxml");
    }

    @FXML
    private void goToAchievements(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    private void goToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
    }

    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(homeBtn);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(homeBtn);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(homeBtn);
    }
}
