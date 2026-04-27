package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementsController {

    @FXML private FlowPane achievementsFlowPane;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    // Pagination
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private HBox pageButtonsBox;
    @FXML private Label pageInfoLabel;

    private static final int PAGE_SIZE = 6;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<Ticket> allAchievements = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(achievementsFlowPane, "/ticket/Achievements.fxml");

        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            authLinks.setVisible(false); authLinks.setManaged(false);
            userLinks.setVisible(true); userLinks.setManaged(true);
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user.getRole().equalsIgnoreCase("ADMIN")) dashboardTopBtn.setText("📊 Admin Dashboard");
            else if (user.getRole().equalsIgnoreCase("NGO")) dashboardTopBtn.setText("📊 NGO Dashboard");
            else dashboardTopBtn.setText("📊 My Dashboard");
        } else {
            authLinks.setVisible(true); authLinks.setManaged(true);
            userLinks.setVisible(false); userLinks.setManaged(false);
        }

        loadAchievementData();
    }

    private void loadAchievementData() {
        allAchievements = ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.COMPLETED)
                .collect(Collectors.toList());
        currentPage = 0;
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int totalPages = getTotalPages();
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allAchievements.size());

        achievementsFlowPane.getChildren().clear();
        for (Ticket t : allAchievements.subList(from, to)) {
            achievementsFlowPane.getChildren().add(createAchievementCard(t));
        }

        updatePaginationControls(totalPages);
    }

    private void updatePaginationControls(int totalPages) {
        if (prevBtn == null) return;
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        int shown = Math.min((currentPage + 1) * PAGE_SIZE, allAchievements.size());
        int from = allAchievements.isEmpty() ? 0 : currentPage * PAGE_SIZE + 1;
        pageInfoLabel.setText(allAchievements.isEmpty()
                ? "No achievements yet"
                : String.format("Showing %d–%d of %d", from, shown, allAchievements.size()));

        pageButtonsBox.getChildren().clear();
        for (int i = 0; i < totalPages; i++) {
            final int page = i;
            Button btn = new Button(String.valueOf(i + 1));
            btn.getStyleClass().add(i == currentPage ? "page-btn-active" : "page-btn");
            btn.setOnAction(e -> { currentPage = page; displayCurrentPage(); });
            pageButtonsBox.getChildren().add(btn);
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) allAchievements.size() / PAGE_SIZE);
    }

    @FXML private void goToPrevPage() { if (currentPage > 0) { currentPage--; displayCurrentPage(); } }
    @FXML private void goToNextPage() { if (currentPage < getTotalPages() - 1) { currentPage++; displayCurrentPage(); } }

    private Node createAchievementCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("achievement-card");

        HBox header = new HBox();
        header.getStyleClass().add("achievement-header");
        header.setAlignment(Pos.CENTER_LEFT);
        String dateStr = t.getAchievedAt() != null ? t.getAchievedAt().format(formatter)
                : (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown Date");
        Label headerTitle = new Label("✔ Achieved " + dateStr);
        headerTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().add(headerTitle);

        VBox content = new VBox(10);
        content.getStyleClass().add("achievement-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");
        loc.setWrapText(true);

        javafx.scene.image.ImageView cardImg = new javafx.scene.image.ImageView();
        cardImg.setFitWidth(340); cardImg.setFitHeight(180); cardImg.setPreserveRatio(true);
        if (t.getImage() != null && !t.getImage().isEmpty()) loadImageRobustly(t.getImage(), cardImg);
        else cardImg.setManaged(false);

        Label completedByLabel = new Label("Completed by: " + (t.getCompletedById() != null ? "User #" + t.getCompletedById() : "—"));
        completedByLabel.getStyleClass().add("achievement-completed-by");

        content.getChildren().addAll(title, cardImg, loc, completedByLabel);

        VBox footer = new VBox();
        footer.setStyle("-fx-border-color: #e5e7eb transparent transparent transparent; -fx-padding: 15;");
        Button btnView = new Button("View ticket");
        btnView.getStyleClass().add("ticket-btn-view");
        btnView.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/TicketDetail.fxml"));
                Parent root = loader.load();
                TicketDetailController ctrl = loader.getController();
                ctrl.setTicket(t);
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ex) { ex.printStackTrace(); }
        });
        footer.getChildren().add(btnView);

        card.getChildren().addAll(header, content, footer);
        return card;
    }

    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/user/Dashboard.fxml"); }
    @FXML private void goBack(ActionEvent event) { if (!tn.esprit.util.NavigationHistory.goBack(event)) goToDashboard(event); }
    @FXML void handleLogout(ActionEvent event) { tn.esprit.util.SessionManager.logout(); navigate(event, "/home/Home.fxml"); }
    @FXML private void goToLogin(ActionEvent e) { navigate(e, "/user/Login.fxml"); }
    @FXML private void goToRegister(ActionEvent e) { navigate(e, "/user/Register.fxml"); }
    @FXML private void goToTickets(ActionEvent e) { navigate(e, "/ticket/TicketManagement.fxml"); }
    @FXML private void goToEvents(ActionEvent e) { navigate(e, "/event/EventManagement.fxml"); }
    @FXML private void goToBlog(ActionEvent e) { navigate(e, "/blog/BlogManagement.fxml"); }
    @FXML private void goToHome(javafx.scene.input.MouseEvent e) { navigate(e, "/home/Home.fxml"); }
    @FXML private void goToHome(ActionEvent e) { navigate(e, "/home/Home.fxml"); }

    private void loadImageRobustly(String rawPath, javafx.scene.image.ImageView view) {
        try {
            String imgPath = rawPath.startsWith("/uploads/") ? "http://127.0.0.1:8000" + rawPath : rawPath;
            javafx.scene.image.Image img = new javafx.scene.image.Image(imgPath, true);
            view.setImage(img);
            img.errorProperty().addListener((obs, o, n) -> {
                if (n) {
                    try {
                        javafx.scene.image.Image fb = new javafx.scene.image.Image("http://localhost/ecospot-web/public" + rawPath, true);
                        view.setImage(fb);
                        fb.errorProperty().addListener((o2, old, nw) -> { if (nw) { view.setManaged(false); view.setVisible(false); } });
                    } catch (Exception ex) { view.setManaged(false); view.setVisible(false); }
                }
            });
        } catch (Exception e) { view.setManaged(false); view.setVisible(false); }
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = event != null
                    ? (Stage) ((Node) event.getSource()).getScene().getWindow()
                    : (Stage) achievementsFlowPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigate(javafx.scene.input.MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
