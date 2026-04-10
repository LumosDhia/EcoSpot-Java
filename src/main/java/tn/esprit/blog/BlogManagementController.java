package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import tn.esprit.services.BlogService;

import javafx.concurrent.Task;
import javafx.application.Platform;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class BlogManagementController {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane articlesGrid;
    @FXML private Button homeBtn;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    private BlogService blogService = new BlogService();
    private List<Blog> allBlogs;

    @FXML
    public void initialize() {
        // Session Management
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            authLinks.setVisible(false);
            authLinks.setManaged(false);
            userLinks.setVisible(true);
            userLinks.setManaged(true);
            
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                dashboardTopBtn.setText("📊 Admin Dashboard");
            } else if (user.getRole().equalsIgnoreCase("NGO")) {
                dashboardTopBtn.setText("📊 NGO Dashboard");
            } else {
                dashboardTopBtn.setText("📊 My Dashboard");
            }
        } else {
            authLinks.setVisible(true);
            authLinks.setManaged(true);
            userLinks.setVisible(false);
            userLinks.setManaged(false);
        }

        // Init sort choices
        sortChoice.setItems(FXCollections.observableArrayList("Newest", "Oldest", "Most Viewed"));
        sortChoice.setValue("Newest");

        // Load data
        refreshData();

        // Listen for search changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplay();
        });

        // Listen for sort changes
        sortChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplay();
        });
    }

    private void refreshData() {
        allBlogs = blogService.getAll();
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String query = searchField.getText().toLowerCase();
        List<Blog> filtered = allBlogs.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) || b.getContent().toLowerCase().contains(query))
                .collect(Collectors.toList());

        // Sorting
        String sort = sortChoice.getValue();
        if ("Newest".equals(sort)) {
            filtered.sort((b1, b2) -> b2.getPublishedAt().compareTo(b1.getPublishedAt()));
        } else if ("Oldest".equals(sort)) {
            filtered.sort((b1, b2) -> b1.getPublishedAt().compareTo(b2.getPublishedAt()));
        } else if ("Most Viewed".equals(sort)) {
            filtered.sort((b1, b2) -> Integer.compare(b2.getViews(), b1.getViews()));
        }

        displayBlogs(filtered);
    }

    private void displayBlogs(List<Blog> blogs) {
        articlesGrid.getChildren().clear();
        
        // Use a Task to avoid freezing the UI thread while loading multiple FXML files
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Blog b : blogs) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/BlogCard.fxml"));
                    Node card = loader.load();
                    BlogCardController controller = loader.getController();
                    // Update UI and initialize listeners on the FX Thread
                    Platform.runLater(() -> {
                        controller.setData(b);
                        articlesGrid.getChildren().add(card);
                    });
                    
                    Thread.sleep(5); 
                }
                return null;
            }
        };

        new Thread(loadTask).start();
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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(javafx.event.ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    private void goToRegister(javafx.event.ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAchievements(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
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
    private void goToArticles(javafx.event.ActionEvent event) {
        navigate(event, "/blog/ArticlesManagement.fxml");
    }

    @FXML
    private void goToBlog(javafx.event.ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
    }

    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
