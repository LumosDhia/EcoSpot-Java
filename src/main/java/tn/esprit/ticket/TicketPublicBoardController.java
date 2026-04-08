package tn.esprit.ticket;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

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
        card.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header (Priority and Domain)
        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #f0f7f4; -fx-border-width: 0 0 1 0;");
        
        Label priorityLabel = new Label("⬤ " + t.getPriority().name());
        priorityLabel.setStyle("-fx-background-color: #e9f5ee; -fx-text-fill: #2d6a4f; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label domainLabel = new Label("🗑 Domain: " + (t.getDomain() != null ? t.getDomain().name() : "Other"));
        domainLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        header.getChildren().addAll(priorityLabel, spacer, domainLabel);

        // Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(20));
        
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label loc = new Label("📍 " + t.getLocation());
        loc.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        
        Label desc = new Label(t.getDescription());
        desc.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");
        desc.setWrapText(true);
        desc.setMinHeight(60);
        
        content.getChildren().addAll(title, loc, desc);

        // Buttons
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(0, 20, 20, 20));
        
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #fca311; -fx-border-radius: 5; -fx-text-fill: #fca311; -fx-cursor: hand;");
        
        Button completeBtn = new Button("✔️ I completed this");
        completeBtn.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        completeBtn.setPadding(new Insets(8, 15, 8, 15));
        
        // CRITICAL: Check authentication
        if (!SessionManager.isLoggedIn()) {
            completeBtn.setVisible(false);
            completeBtn.setManaged(false);
        }

        completeBtn.setOnAction(e -> handleCompletionRequest(t));

        actions.getChildren().addAll(viewBtn, completeBtn);

        card.getChildren().addAll(header, content, actions);
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
        // Mocking for now
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
