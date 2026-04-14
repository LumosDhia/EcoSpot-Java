package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminAllTicketsController {

    @FXML private VBox ticketsListContainer;
    @FXML private Label userNameLabel;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        if (!isAdminUser()) {
            redirectToDashboard();
            return;
        }
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        }
        tn.esprit.util.NavigationHistory.track(ticketsListContainer, "/ticket/AdminAllTickets.fxml");
        loadAllTickets();
    }

    private void loadAllTickets() {
        ticketsListContainer.getChildren().clear();

        List<Ticket> all = ticketService.getAll();
        if (all.isEmpty()) {
            Label emptyLbl = new Label("No tickets found.");
            emptyLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-padding: 30;");
            ticketsListContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Ticket t : all) {
            ticketsListContainer.getChildren().add(createTicketRow(t));
        }
    }

    private Node createTicketRow(Ticket t) {
        VBox row = new VBox(10);
        row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 16;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(t.getTitle() == null ? "(Untitled ticket)" : t.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label status = new Label(t.getStatus() != null ? t.getStatus().name() : "UNKNOWN");
        status.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(title, spacer, status);

        Label meta = new Label(
                "📍 " + (t.getLocation() == null ? "No location" : t.getLocation()) +
                        "    •    🕒 " + (t.getCreatedAt() == null ? "Unknown date" : t.getCreatedAt().format(formatter))
        );
        meta.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Label desc = new Label(t.getDescription() == null ? "" : t.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openTicketDetail(t));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteTicket(t));

        actions.getChildren().addAll(deleteBtn, viewBtn);
        row.getChildren().addAll(top, meta, desc, actions);
        return row;
    }

    private void openTicketDetail(Ticket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/TicketDetail.fxml"));
            Parent root = loader.load();
            TicketDetailController controller = loader.getController();
            controller.setTicket(t);
            Stage stage = (Stage) ticketsListContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTicket(Ticket t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete ticket \"" + t.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                ticketService.delete(t);
                loadAllTickets();
            }
        });
    }

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
    }

    @FXML
    void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && SessionManager.isLoggedIn()) {
                controller.setUser(SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAdminUser() {
        return SessionManager.isLoggedIn()
                && SessionManager.getCurrentUser() != null
                && "ADMIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
    }

    private void redirectToDashboard() {
        if (ticketsListContainer == null || ticketsListContainer.getScene() == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && SessionManager.isLoggedIn()) {
                controller.setUser(SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ticketsListContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
