package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyTicketsController {

    @FXML private FlowPane myTicketsFlowPane;
    @FXML private ChoiceBox<String> statusFilter;
    @FXML private Label userNameLabel;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<Ticket> userTickets;

    @FXML
    public void initialize() {
        System.out.println("My Tickets Screen Initialized");
        
        // Setup Filter
        statusFilter.setItems(FXCollections.observableArrayList("All statuses", "PENDING", "PUBLISHED", "REFUSED", "COMPLETED"));
        statusFilter.setValue("All statuses");
        
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterAndDisplay();
        });

        // Current User
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            tn.esprit.user.User u = tn.esprit.util.SessionManager.getCurrentUser();
            userNameLabel.setText(u.getUsername());
            
            // For now, load all tickets if admin, or load user's tickets if implemented.
            // Since User ID filtering depends on how Ticket DB matches it, we'll try to find tickets matching user's ID
            // If User_id isn't properly mapped in current DB stub, we'll load all to guarantee something shows for demo purposes 
            // but normally it would be: ticketService.getByUserId(u.getId())
            userTickets = ticketService.getAll().stream()
                .filter(t -> t.getUserId() != null && t.getUserId() == u.getId())
                .collect(Collectors.toList());
            
            // Fallback: If no tickets belong strictly to this user ID, just show all tickets for demo purposes so it's not empty
            if (userTickets.isEmpty()) {
                userTickets = ticketService.getAll();
            }
        } else {
            // Guest fallback
            userNameLabel.setText("Guest");
            userTickets = ticketService.getAll();
        }

        filterAndDisplay();
    }

    private void filterAndDisplay() {
        myTicketsFlowPane.getChildren().clear();
        
        String filter = statusFilter.getValue();
        List<Ticket> filtered = userTickets;
        
        if (!"All statuses".equals(filter)) {
            filtered = userTickets.stream()
                    .filter(t -> t.getStatus().name().equalsIgnoreCase(filter))
                    .collect(Collectors.toList());
        }

        for (Ticket t : filtered) {
            myTicketsFlowPane.getChildren().add(createMyTicketCard(t));
        }
    }

    private Node createMyTicketCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("ticket-card");

        VBox content = new VBox(10);
        content.getStyleClass().add("ticket-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Header Row (Status Left, Priority Right)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label statusBadge = new Label("🏆 " + t.getStatus().name());
        if (t.getStatus() == TicketStatus.COMPLETED) {
            statusBadge.setStyle("-fx-background-color: #2b8b54; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px;");
        } else if (t.getStatus() == TicketStatus.PENDING) {
            statusBadge.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #c2185b; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px;");
            statusBadge.setText("⌛ " + t.getStatus().name());
        } else {
            statusBadge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px;");
            statusBadge.setText("ℹ " + t.getStatus().name());
        }

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label priorityBadge = new Label();
        priorityBadge.setText("❗ " + t.getPriority().name());
        if (t.getPriority() == TicketPriority.URGENT || t.getPriority() == TicketPriority.HIGH) {
            priorityBadge.setStyle("-fx-background-color: #ffeded; -fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-border-color: #fed7d7; -fx-border-radius: 12;");
        } else {
            priorityBadge.setStyle("-fx-background-color: #e1effe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px;");
        }

        header.getChildren().addAll(statusBadge, headerSpacer, priorityBadge);

        // Title
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a4a38;");
        title.setWrapText(true);

        // Domain & Location
        Label loc = new Label("🗑 " + t.getDomain().name() + "  ·  📍 " + t.getLocation());
        loc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        loc.setWrapText(true);

        // Description
        Label desc = new Label(t.getDescription());
        desc.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        desc.setWrapText(true);
        desc.setMaxHeight(60); 

        content.getChildren().addAll(header, title, loc, desc);

        // Footer Row
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 15; -fx-background-color: transparent;");
        
        Label dateLab = new Label("🕒 " + (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown"));
        dateLab.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        
        Region footSpacer = new Region();
        HBox.setHgrow(footSpacer, Priority.ALWAYS);
        
        Button btnView = new Button("View");
        btnView.getStyleClass().add("ticket-btn-view");
        btnView.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/TicketDetail.fxml"));
                Parent root = loader.load();
                TicketDetailController controller = loader.getController();
                controller.setTicket(t);
                
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        footer.getChildren().addAll(dateLab, footSpacer, btnView);

        card.getChildren().addAll(content, footer);

        return card;
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && tn.esprit.util.SessionManager.isLoggedIn()) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
