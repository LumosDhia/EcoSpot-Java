package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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

public class TicketManagementController {

    @FXML private FlowPane ticketsFlowPane;
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
    private List<Ticket> allTickets = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(ticketsFlowPane, "/ticket/TicketManagement.fxml");

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

        loadTicketData();
    }

    private void loadTicketData() {
        allTickets = ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.PUBLISHED
                          || t.getStatus() == TicketStatus.ASSIGNED
                          || t.getStatus() == TicketStatus.IN_PROGRESS
                          || t.getStatus() == TicketStatus.COMPLETED)
                .collect(Collectors.toList());
        currentPage = 0;
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int totalPages = getTotalPages();
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allTickets.size());

        ticketsFlowPane.getChildren().clear();
        for (Ticket t : allTickets.subList(from, to)) {
            ticketsFlowPane.getChildren().add(createTicketCard(t));
        }

        updatePaginationControls(totalPages);
    }

    private void updatePaginationControls(int totalPages) {
        if (prevBtn == null) return;
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        int shown = Math.min((currentPage + 1) * PAGE_SIZE, allTickets.size());
        int from = allTickets.isEmpty() ? 0 : currentPage * PAGE_SIZE + 1;
        pageInfoLabel.setText(allTickets.isEmpty()
                ? "No tickets found"
                : String.format("Showing %d–%d of %d", from, shown, allTickets.size()));

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
        return (int) Math.ceil((double) allTickets.size() / PAGE_SIZE);
    }

    @FXML private void goToPrevPage() { if (currentPage > 0) { currentPage--; displayCurrentPage(); } }
    @FXML private void goToNextPage() { if (currentPage < getTotalPages() - 1) { currentPage++; displayCurrentPage(); } }

    private Node createTicketCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("ticket-card");

        VBox content = new VBox();
        content.getStyleClass().add("ticket-card-content");
        content.setSpacing(10);
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");
        loc.setWrapText(true);

        ImageView cardImg = new ImageView();
        cardImg.setFitWidth(340); cardImg.setFitHeight(180); cardImg.setPreserveRatio(true);
        if (t.getImage() != null && !t.getImage().isEmpty()) loadImageRobustly(t.getImage(), cardImg);
        else cardImg.setManaged(false);

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("ticket-description");
        desc.setWrapText(true);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        Button btnView = new Button("View");
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

        Button btnComplete = new Button("✔ I completed this");
        btnComplete.getStyleClass().add("ticket-btn-complete");
        btnComplete.setOnAction(e -> openCompletionForm(e, t));

        buttons.getChildren().add(btnView);
        if (tn.esprit.util.SessionManager.isLoggedIn() &&
                (t.getStatus() == TicketStatus.PUBLISHED || t.getStatus() == TicketStatus.ASSIGNED)) {
            buttons.getChildren().add(btnComplete);
        } else if (tn.esprit.util.SessionManager.isLoggedIn() && t.getStatus() == TicketStatus.IN_PROGRESS) {
            Label submitted = new Label("Completion submitted for review");
            submitted.setStyle("-fx-text-fill: #b45309; -fx-font-size: 12px; -fx-font-weight: bold;");
            buttons.getChildren().add(submitted);
        }

        content.getChildren().addAll(title, loc, cardImg, desc, buttons);

        VBox footer = new VBox();
        footer.getStyleClass().add("ticket-footer");
        String dateStr = t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown Date";
        Label footerText = new Label("Published " + dateStr);
        footerText.getStyleClass().add("ticket-footer-text");
        footer.getChildren().add(footerText);

        card.getChildren().addAll(content, footer);
        return card;
    }

    private void openCompletionForm(ActionEvent event, Ticket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/CompleteTicket.fxml"));
            Parent root = loader.load();
            CompleteTicketController ctrl = loader.getController();
            ctrl.setTicket(t);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController ctrl = loader.getController();
            if (ctrl != null) ctrl.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        navigate(event, "/home/Home.fxml");
    }

    @FXML private void goToLogin(ActionEvent e) { navigate(e, "/user/Login.fxml"); }
    @FXML private void goToRegister(ActionEvent e) { navigate(e, "/user/Register.fxml"); }
    @FXML private void goToAchievements(ActionEvent e) { navigate(e, "/ticket/Achievements.fxml"); }
    @FXML private void goToEvents(ActionEvent e) { navigate(e, "/event/EventManagement.fxml"); }
    @FXML private void goToTickets(ActionEvent e) { navigate(e, "/ticket/TicketManagement.fxml"); }
    @FXML private void goToBlog(ActionEvent e) { navigate(e, "/blog/BlogManagement.fxml"); }
    @FXML private void goToHome(javafx.scene.input.MouseEvent e) { navigate(e, "/home/Home.fxml"); }
    @FXML private void goToHome(ActionEvent e) { navigate(e, "/home/Home.fxml"); }

    private void loadImageRobustly(String rawPath, ImageView view) {
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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
