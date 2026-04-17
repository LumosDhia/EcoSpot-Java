package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import tn.esprit.services.BlogService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlogManagementController {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane articlesGrid;
    @FXML private Button homeBtn;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    // Pagination controls
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private HBox pageButtonsBox;
    @FXML private Label pageInfoLabel;

    private static final int PAGE_SIZE = 8;

    private final BlogService blogService = new BlogService();
    private List<Blog> allBlogs = new ArrayList<>();
    private List<Blog> filteredBlogs = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
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

        sortChoice.setItems(FXCollections.observableArrayList("Newest", "Oldest", "Most Viewed"));
        sortChoice.setValue("Newest");

        refreshData();

        searchField.textProperty().addListener((obs, old, val) -> {
            currentPage = 0;
            filterAndDisplay();
        });

        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            currentPage = 0;
            filterAndDisplay();
        });
    }

    private void refreshData() {
        allBlogs = blogService.getAll();
        currentPage = 0;
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String query = searchField.getText().toLowerCase();
        filteredBlogs = allBlogs.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query)
                          || b.getContent().toLowerCase().contains(query))
                .collect(Collectors.toList());

        String sort = sortChoice.getValue();
        if ("Newest".equals(sort)) {
            filteredBlogs.sort((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()));
        } else if ("Oldest".equals(sort)) {
            filteredBlogs.sort((a, b) -> a.getPublishedAt().compareTo(b.getPublishedAt()));
        } else if ("Most Viewed".equals(sort)) {
            filteredBlogs.sort((a, b) -> Integer.compare(b.getViews(), a.getViews()));
        }

        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int totalPages = getTotalPages();
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredBlogs.size());
        List<Blog> pageBlogs = filteredBlogs.subList(from, to);

        articlesGrid.getChildren().clear();

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Blog b : pageBlogs) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/BlogCard.fxml"));
                    Node card = loader.load();
                    BlogCardController controller = loader.getController();
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

        updatePaginationControls(totalPages);
    }

    private void updatePaginationControls(int totalPages) {
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        int shown = Math.min((currentPage + 1) * PAGE_SIZE, filteredBlogs.size());
        int from = filteredBlogs.isEmpty() ? 0 : currentPage * PAGE_SIZE + 1;
        pageInfoLabel.setText(filteredBlogs.isEmpty()
                ? "No articles found"
                : String.format("Showing %d–%d of %d", from, shown, filteredBlogs.size()));

        pageButtonsBox.getChildren().clear();
        for (int i = 0; i < totalPages; i++) {
            final int page = i;
            Button btn = new Button(String.valueOf(i + 1));
            btn.getStyleClass().add(i == currentPage ? "page-btn-active" : "page-btn");
            btn.setOnAction(e -> {
                currentPage = page;
                displayCurrentPage();
            });
            pageButtonsBox.getChildren().add(btn);
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredBlogs.size() / PAGE_SIZE);
    }

    @FXML
    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            displayCurrentPage();
        }
    }

    @FXML
    private void goToNextPage() {
        if (currentPage < getTotalPages() - 1) {
            currentPage++;
            displayCurrentPage();
        }
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

    @FXML private void goToLogin(javafx.event.ActionEvent e) { navigate(e, "/user/Login.fxml"); }
    @FXML private void goToRegister(javafx.event.ActionEvent e) { navigate(e, "/user/Register.fxml"); }
    @FXML private void goToAchievements(javafx.event.ActionEvent e) { navigate(e, "/ticket/Achievements.fxml"); }
    @FXML private void goToEvents(javafx.event.ActionEvent e) { navigate(e, "/event/EventManagement.fxml"); }
    @FXML private void goToTickets(javafx.event.ActionEvent e) { navigate(e, "/ticket/TicketManagement.fxml"); }
    @FXML private void goToArticles(javafx.event.ActionEvent e) { navigate(e, "/blog/ArticlesManagement.fxml"); }
    @FXML private void goToBlog(javafx.event.ActionEvent e) { navigate(e, "/blog/BlogManagement.fxml"); }

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

    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(homeBtn); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(homeBtn); }
    @FXML private void handleClose() { tn.esprit.util.WindowUtils.close(homeBtn); }
}
