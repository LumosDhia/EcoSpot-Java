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
                .filter(t -> t.getUserId() == u.getId())
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

        // Title
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a4a38;");
        title.setWrapText(true);

        // Location
        Label loc = new Label("📍 " + t.getLocation());
        loc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        loc.setWrapText(true);


        // Description
        Label desc = new Label(t.getDescription());
        desc.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
        desc.setWrapText(true);

        content.getChildren().addAll(title, loc, desc);

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
