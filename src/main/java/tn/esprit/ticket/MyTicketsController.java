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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
        tn.esprit.util.NavigationHistory.track(myTicketsFlowPane, "/ticket/MyTickets.fxml");
        
        // Setup Filter
        statusFilter.setItems(FXCollections.observableArrayList(
                "All statuses",
                "PENDING",
                "APPROVED",
                "REJECTED",
                "NEEDS REVISION",
                "COMPLETED"
        ));
        statusFilter.setValue("All statuses");
        
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterAndDisplay();
        });

        // Current User
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            tn.esprit.user.User u = tn.esprit.util.SessionManager.getCurrentUser();
            userNameLabel.setText(u.getUsername());
            userTickets = ticketService.getByUserId(u.getId());
        } else {
            // Guest fallback
            userNameLabel.setText("Guest");
            userTickets = java.util.Collections.emptyList();
        }

        filterAndDisplay();
    }

    private void filterAndDisplay() {
        myTicketsFlowPane.getChildren().clear();
        
        String filter = statusFilter.getValue();
        List<Ticket> filtered = userTickets;
        
        if (!"All statuses".equals(filter)) {
            filtered = userTickets.stream()
                    .filter(t -> statusMatchesFilter(t.getStatus(), filter))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30;");
            Label title = new Label("No tickets found");
            title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 18px; -fx-font-weight: bold;");
            Label subtitle = new Label("Try another status filter or create a new ticket.");
            subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
            emptyBox.getChildren().addAll(title, subtitle);
            myTicketsFlowPane.getChildren().add(emptyBox);
            return;
        }

        for (Ticket t : filtered) {
            myTicketsFlowPane.getChildren().add(createMyTicketCard(t));
        }
    }

    private Node createMyTicketCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("ticket-card");

        VBox content = new VBox(10);
        content.getStyleClass().add("ticket-card-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");
        loc.setWrapText(true);

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("ticket-description");
        desc.setWrapText(true);

        Label statusLabel = new Label(toUserStatusText(t.getStatus()));
        statusLabel.getStyleClass().add("my-ticket-status-badge");
        applyStatusClass(statusLabel, t.getStatus());

        HBox metaRow = new HBox(8, statusLabel);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        if (t.getStatus() == TicketStatus.SENT_BACK && t.getAdminNotes() != null && !t.getAdminNotes().isBlank()) {
            Label adminNote = new Label("Admin revision note: " + t.getAdminNotes());
            adminNote.getStyleClass().add("my-ticket-revision-note");
            adminNote.setWrapText(true);
            content.getChildren().add(adminNote);
        }

        content.getChildren().addAll(title, loc, desc, metaRow);

        // Footer Row
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("ticket-footer");
        
        Label dateLab = new Label("🕒 " + (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown"));
        dateLab.getStyleClass().add("ticket-footer-text");
        
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

        if (canModifyTicket(t)) {
            Button btnEdit = new Button(t.getStatus() == TicketStatus.SENT_BACK ? "Edit & Resubmit" : "Edit");
            btnEdit.getStyleClass().add("ticket-btn-complete");
            btnEdit.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/CreateTicket.fxml"));
                    Parent root = loader.load();
                    CreateTicketController controller = loader.getController();
                    controller.setTicketForEdit(t);
                    Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    stage.getScene().setRoot(root);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            Button btnDelete = new Button("Delete");
            btnDelete.getStyleClass().add("ticket-btn-view");
            btnDelete.setOnAction(e -> handleDeleteTicket(t));

            footer.getChildren().addAll(dateLab, footSpacer, btnDelete, btnEdit, btnView);
            card.getChildren().addAll(content, footer);
            return card;
        }
        
        footer.getChildren().addAll(dateLab, footSpacer, btnView);

        card.getChildren().addAll(content, footer);

        return card;
    }

    private boolean statusMatchesFilter(TicketStatus status, String filter) {
        if ("APPROVED".equalsIgnoreCase(filter)) {
            return status == TicketStatus.PUBLISHED;
        }
        if ("REJECTED".equalsIgnoreCase(filter)) {
            return status == TicketStatus.REFUSED;
        }
        if ("NEEDS REVISION".equalsIgnoreCase(filter)) {
            return status == TicketStatus.SENT_BACK;
        }
        return status.name().equalsIgnoreCase(filter);
    }

    private String toUserStatusText(TicketStatus status) {
        if (status == TicketStatus.PUBLISHED) return "APPROVED";
        if (status == TicketStatus.REFUSED) return "REJECTED";
        if (status == TicketStatus.SENT_BACK) return "NEEDS REVISION";
        return status.name();
    }

    private boolean canModifyTicket(Ticket t) {
        return t != null && t.getStatus() != TicketStatus.PUBLISHED;
    }

    private void handleDeleteTicket(Ticket t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this ticket?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                ticketService.delete(t);
                userTickets = ticketService.getByUserId(tn.esprit.util.SessionManager.getCurrentUser().getId());
                filterAndDisplay();
            }
        });
    }

    private void applyStatusClass(Label statusLabel, TicketStatus status) {
        statusLabel.getStyleClass().removeAll(
                "my-ticket-status-pending",
                "my-ticket-status-approved",
                "my-ticket-status-rejected",
                "my-ticket-status-revision",
                "my-ticket-status-completed"
        );
        if (status == TicketStatus.PUBLISHED) {
            statusLabel.getStyleClass().add("my-ticket-status-approved");
        } else if (status == TicketStatus.REFUSED) {
            statusLabel.getStyleClass().add("my-ticket-status-rejected");
        } else if (status == TicketStatus.SENT_BACK) {
            statusLabel.getStyleClass().add("my-ticket-status-revision");
        } else if (status == TicketStatus.COMPLETED) {
            statusLabel.getStyleClass().add("my-ticket-status-completed");
        } else {
            statusLabel.getStyleClass().add("my-ticket-status-pending");
        }
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

    @FXML
    private void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
    }
    @FXML
    private void goToCreateTicket(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/CreateTicket.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
