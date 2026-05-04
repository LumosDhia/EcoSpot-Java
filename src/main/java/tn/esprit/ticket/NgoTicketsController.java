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
import tn.esprit.util.ImageUploadUtils;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NgoTicketsController {

    @FXML private Label userNameLabel;
    @FXML private Label myTasksCount;
    @FXML private Label availableCount;
    @FXML private FlowPane myTasksFlowPane;
    @FXML private FlowPane availableFlowPane;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        if (!isNgoUser()) {
            redirectToDashboard();
            return;
        }
        userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        loadTickets();
    }

    private void loadTickets() {
        int currentUserId = SessionManager.getCurrentUser().getId();
        
        List<Ticket> myTasks = ticketService.getByNgoId(currentUserId);
        myTasksFlowPane.getChildren().clear();
        for (Ticket t : myTasks) {
            myTasksFlowPane.getChildren().add(createTicketCard(t, true));
        }
        myTasksCount.setText(String.valueOf(myTasks.size()));

        List<Ticket> available = ticketService.getAvailableForNgo();
        availableFlowPane.getChildren().clear();
        for (Ticket t : available) {
            availableFlowPane.getChildren().add(createTicketCard(t, false));
        }
        availableCount.setText(String.valueOf(available.size()));
    }

    private Node createTicketCard(Ticket t, boolean isAssigned) {
        VBox card = new VBox();
        card.getStyleClass().add("ticket-card");
        card.setPrefWidth(380);

        VBox content = new VBox();
        content.getStyleClass().add("ticket-card-content");
        content.setSpacing(10);
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");

        ImageView cardImg = new ImageView();
        cardImg.setFitWidth(340); cardImg.setFitHeight(180); cardImg.setPreserveRatio(true);
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            String url = ImageUploadUtils.getImageUrl("tickets", t.getImage());
            cardImg.setImage(new javafx.scene.image.Image(url, true));
        } else {
            cardImg.setManaged(false);
        }

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("ticket-description");
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        Button btnView = new Button("View Details");
        btnView.getStyleClass().add("ticket-btn-view");
        btnView.setOnAction(e -> openDetail(t));

        buttons.getChildren().add(btnView);

        if (isAssigned) {
            if (t.getStatus() != TicketStatus.COMPLETED && t.getStatus() != TicketStatus.IN_PROGRESS) {
                Button btnStart = new Button("▶ Start Working");
                btnStart.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
                btnStart.setOnAction(e -> {
                    t.setStatus(TicketStatus.IN_PROGRESS);
                    ticketService.update(t);
                    loadTickets();
                });
                buttons.getChildren().add(btnStart);
            } else if (t.getStatus() == TicketStatus.IN_PROGRESS) {
                Button btnComplete = new Button("✔ Mark Completed");
                btnComplete.getStyleClass().add("ticket-btn-complete");
                btnComplete.setOnAction(e -> openCompletionForm(e, t));
                buttons.getChildren().add(btnComplete);
            } else {
                Label statusLbl = new Label("Status: " + t.getStatus());
                statusLbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                buttons.getChildren().add(statusLbl);
            }
        } else {
            Button btnClaim = new Button("🤝 Claim Ticket");
            btnClaim.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
            btnClaim.setOnAction(e -> {
                t.setAssignedNgoId(SessionManager.getCurrentUser().getId());
                t.setStatus(TicketStatus.ASSIGNED);
                ticketService.update(t);
                loadTickets();
            });
            buttons.getChildren().add(btnClaim);
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

    private void openDetail(Ticket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/TicketDetail.fxml"));
            Parent root = loader.load();
            TicketDetailController ctrl = loader.getController();
            ctrl.setTicket(t);
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
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
    void goBack(ActionEvent event) {
        redirectToDashboard();
    }

    private boolean isNgoUser() {
        return SessionManager.isLoggedIn() && "NGO".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
    }

    private void redirectToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController ctrl = loader.getController();
            if (ctrl != null) ctrl.setUser(SessionManager.getCurrentUser());
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
