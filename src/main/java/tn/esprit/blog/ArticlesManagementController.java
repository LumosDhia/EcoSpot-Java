package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.BlogService;
import tn.esprit.user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ArticlesManagementController {

    @FXML private TableView<Blog> adminArticlesTable;
    @FXML private TableColumn<Blog, Integer> colId;
    @FXML private TableColumn<Blog, String> colTitle;
    @FXML private TableColumn<Blog, String> colStatus;
    @FXML private TableColumn<Blog, LocalDateTime> colCreated;
    @FXML private TableColumn<Blog, LocalDateTime> colPublished;
    @FXML private TableColumn<Blog, Void> colActions;

    @FXML private TableView<Blog> ngoArticlesTable;
    @FXML private TableColumn<Blog, Integer> colNgoId;
    @FXML private TableColumn<Blog, String> colNgoTitle;
    @FXML private TableColumn<Blog, String> colNgoWriter;
    @FXML private TableColumn<Blog, String> colNgoStatus;
    @FXML private TableColumn<Blog, LocalDateTime> colNgoCreated;
    @FXML private TableColumn<Blog, Void> colNgoActions;

    @FXML private Button homeBtn;
    @FXML private VBox adminSectionBox;
    @FXML private VBox ngoSectionBox;
    @FXML private Label breadcrumbPrefixLabel;
    @FXML private Label breadcrumbTitleLabel;
    @FXML private Label ngoSectionTitleLabel;
    @FXML private Label ngoSectionSubtitleLabel;

    private BlogService blogService = new BlogService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean ngoMode = false;

    @FXML
    public void initialize() {
        User currentUser = tn.esprit.util.SessionManager.getCurrentUser();
        ngoMode = currentUser != null && "NGO".equalsIgnoreCase(currentUser.getRole());
        setupTables();
        loadData();
    }

    private void setupTables() {
        // Admin Table setup
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Blog b = getTableView().getItems().get(getIndex());
                    setGraphic(createStatusBadge(b));
                }
            }
        });
        
        colCreated.setCellValueFactory(new PropertyValueFactory<>("publishedAt"));
        colCreated.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(formatter));
            }
        });

        colPublished.setCellValueFactory(new PropertyValueFactory<>("publishedAt"));
        colPublished.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(formatter));
            }
        });

        setupActionButtons(colActions, true, false);

        // NGO Table setup
        colNgoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNgoTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colNgoWriter.setCellValueFactory(new PropertyValueFactory<>("author"));
        colNgoStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Blog b = getTableView().getItems().get(getIndex());
                    setGraphic(createStatusBadge(b));
                }
            }
        });
        colNgoCreated.setCellValueFactory(new PropertyValueFactory<>("publishedAt"));
        colNgoCreated.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(formatter));
            }
        });

        setupActionButtons(colNgoActions, false, ngoMode);
    }

    private void setupActionButtons(TableColumn<Blog, Void> column, boolean isAdminTable, boolean allowNgoEdit) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button revisionBtn = new Button("Revision");
            private final HBox pane = new HBox(5, viewBtn, editBtn, deleteBtn, revisionBtn);

            {
                viewBtn.setStyle("-fx-text-fill: #fdae6b; -fx-background-color: transparent; -fx-border-color: #fdae6b; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                editBtn.setStyle("-fx-text-fill: #f1933e; -fx-background-color: transparent; -fx-border-color: #f1933e; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-text-fill: #bc4749; -fx-background-color: transparent; -fx-border-color: #bc4749; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                revisionBtn.setStyle("-fx-text-fill: #557999; -fx-background-color: transparent; -fx-border-color: #557999; -fx-border-radius: 3; -fx-font-size: 10; -fx-cursor: hand;");
                
                // In admin screen, NGO table uses "Revision" instead of edit.
                // In NGO mode, NGO table allows editing own articles.
                if (!isAdminTable && !allowNgoEdit) {
                    editBtn.setVisible(false);
                    editBtn.setManaged(false);
                    revisionBtn.setVisible(true);
                    revisionBtn.setManaged(true);
                } else {
                    revisionBtn.setVisible(false);
                    revisionBtn.setManaged(false);
                }

                viewBtn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                revisionBtn.setOnAction(e -> handleReturnToRevision(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleView(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/ArticleShow.fxml"));
            Parent root = loader.load();
            ArticleShowController controller = loader.getController();
            controller.setArticle(blog);
            Stage stage = (Stage) adminArticlesTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/NewArticle.fxml"));
            Parent root = loader.load();
            NewArticleController controller = loader.getController();
            controller.setEditArticle(blog);
            Stage stage = (Stage) adminArticlesTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Blog blog) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + blog.getTitle() + "\"?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                blogService.delete(blog);
                loadData();
            }
        });
    }

    private void handleReturnToRevision(Blog blog) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Return for revision");
        inputDialog.setHeaderText("Send article back to NGO writer");
        inputDialog.setContentText("Revision note:");
        inputDialog.showAndWait().ifPresent(note -> {
            String trimmedNote = note == null ? "" : note.trim();
            if (trimmedNote.isEmpty()) {
                Alert warn = new Alert(Alert.AlertType.WARNING, "Revision note cannot be empty.");
                warn.showAndWait();
                return;
            }
            blog.setAdminRevisionNote(trimmedNote);
            blog.setIsPublished(false);
            blogService.update(blog);
            loadData();
        });
    }

    private Button createStatusBadge(Blog blog) {
        boolean revisionRequested = blog.getAdminRevisionNote() != null && !blog.getAdminRevisionNote().trim().isEmpty();
        Button btn;
        if (revisionRequested && !blog.getIsPublished()) {
            btn = new Button("Revision requested");
            btn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 4;");
        } else if (blog.getIsPublished()) {
            btn = new Button("Published");
            btn.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-font-size: 10; -fx-background-radius: 4;");
        } else {
            btn = new Button("Draft");
            btn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 10; -fx-background-radius: 4;");
        }
        return btn;
    }

    private void loadData() {
        List<Blog> allBlogs = blogService.getAll();
        User currentUser = tn.esprit.util.SessionManager.getCurrentUser();
        
        ObservableList<Blog> adminList;
        ObservableList<Blog> ngoList;

        if (currentUser != null && "NGO".equalsIgnoreCase(currentUser.getRole())) {
            configureForNgoDashboard();
            String ownerEmail = blogService.resolveCurrentOwnerEmail();
            adminList = FXCollections.observableArrayList();
            ngoList = FXCollections.observableArrayList(
                allBlogs.stream()
                    .filter(b -> ownerEmail != null && b.getCreatedByEmail() != null && b.getCreatedByEmail().equalsIgnoreCase(ownerEmail))
                    .collect(Collectors.toList())
            );
        } else {
            configureForAdminDashboard();
            adminList = FXCollections.observableArrayList(
                allBlogs.stream()
                    .filter(b -> b.getAuthor() != null && b.getAuthor().startsWith("Admin"))
                    .collect(Collectors.toList())
            );

            ngoList = FXCollections.observableArrayList(
                allBlogs.stream()
                    .filter(b -> b.getAuthor() == null || !b.getAuthor().startsWith("Admin"))
                    .collect(Collectors.toList())
            );
        }

        adminArticlesTable.setItems(adminList);
        ngoArticlesTable.setItems(ngoList);
    }

    private void configureForNgoDashboard() {
        adminSectionBox.setVisible(false);
        adminSectionBox.setManaged(false);
        ngoSectionBox.setVisible(true);
        ngoSectionBox.setManaged(true);
        breadcrumbPrefixLabel.setText("NGO › ");
        breadcrumbTitleLabel.setText("My articles");
        ngoSectionTitleLabel.setText("My articles");
        ngoSectionSubtitleLabel.setText("Only articles created by your NGO account are shown here.");
    }

    private void configureForAdminDashboard() {
        adminSectionBox.setVisible(true);
        adminSectionBox.setManaged(true);
        ngoSectionBox.setVisible(true);
        ngoSectionBox.setManaged(true);
        breadcrumbPrefixLabel.setText("Admin › ");
        breadcrumbTitleLabel.setText("Blog articles");
        ngoSectionTitleLabel.setText("NGO articles");
        ngoSectionSubtitleLabel.setText("You can only view, delete, or return for revision. No editing or publishing.");
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
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
    private void goToHome(ActionEvent event) {
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
    private void goToBlog(ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
    }

    @FXML
    private void goToNewArticle(ActionEvent event) {
        navigate(event, "/blog/NewArticle.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {
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
