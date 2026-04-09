package tn.esprit.ticket;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketPublicBoardController {

    @FXML private FlowPane ticketsFlow;

    private TicketService ticketService;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        loadTickets();
    }

    private void loadTickets() {
        List<Ticket> published = ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.PUBLISHED && t.getCompletedById() == null)
                .collect(Collectors.toList());

        ticketsFlow.getChildren().clear();
        for (Ticket t : published) {
            ticketsFlow.getChildren().add(createTicketCard(t));
        }
    }

    private VBox createTicketCard(Ticket t) {
        VBox card = new VBox(0);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 0);");
        
        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #f3f4f6; -fx-border-width: 0 0 1 0;");
        
        Label priorityLabel = new Label("⬤ " + t.getPriority().name());
        if (t.getPriority() == TicketPriority.MEDIUM) {
            priorityLabel.getStyleClass().add("badge-medium");
        } else {
            priorityLabel.getStyleClass().add("badge-low");
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label domainLabel = new Label("🗑 Domain: " + (t.getDomain() != null ? t.getDomain().name() : "Waste"));
        domainLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(priorityLabel, spacer, domainLabel);

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Label loc = new Label("📍 " + t.getLocation());
        loc.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        
        Label desc = new Label(t.getDescription());
        desc.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 14px;");
        desc.setWrapText(true);
        desc.setMinHeight(50);
        
        content.getChildren().addAll(title, loc, desc);

        // Actions
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(0, 20, 15, 20));
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("btn-view-outline");
        
        Button completeBtn = new Button("✔ I completed this");
        completeBtn.getStyleClass().add("btn-complete-solid");
        
        if (!SessionManager.isLoggedIn()) {
            completeBtn.setVisible(false);
            completeBtn.setManaged(false);
        }
        completeBtn.setOnAction(e -> handleCompletionRequest(t));

        actions.getChildren().addAll(viewBtn, completeBtn);

        // Footer
        VBox footer = new VBox();
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setStyle("-fx-border-color: #f3f4f6; -fx-border-width: 1 0 0 0;");
        
        String dateStr = t.getCreatedAt() != null ? t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "02/03/2026";
        Label dateLabel = new Label("Published " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        
        footer.getChildren().add(dateLabel);

        card.getChildren().addAll(header, content, actions, footer);
        return card;
    }

    private void handleCompletionRequest(Ticket t) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Submit Proof");
        dialog.setHeaderText("Resolution Proof for: " + t.getTitle());
        dialog.setContentText("Please describe briefly how you resolved this issue:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            t.setCompletedById(SessionManager.getCurrentUser().getId());
            t.setCompletionMessage(message);
            ticketService.update(t);
            showAlert("Submitted", "Thank you for your contribution! An admin will review it.");
            loadTickets();
        });
    }

    @FXML
    private void goToAchievements(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/TicketAchievements.fxml");
    }

    @FXML
    private void goToBlog(javafx.event.ActionEvent event) {
        // Blog logic
    }

    @FXML
    private void handleBackHome(javafx.event.ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    private void navigate(javafx.event.ActionEvent event, String path) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(path));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
