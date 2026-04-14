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
import javafx.scene.control.TextField;
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

public class PendingTicketsController {

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
        if (SessionManager.isLoggedIn()) {
            userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        }
        tn.esprit.util.NavigationHistory.track(ticketsListContainer, "/ticket/PendingTickets.fxml");
        loadPendingTickets();
    }

    private void loadPendingTickets() {
        ticketsListContainer.getChildren().clear();

        List<Ticket> pending = ticketService.getPendingForAdminReview();

        if (pending.isEmpty()) {
            Label emptyLbl = new Label("No pending tickets to review.");
            emptyLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-padding: 30;");
            ticketsListContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Ticket t : pending) {
            ticketsListContainer.getChildren().add(createTicketReviewRow(t));
        }
    }

    private Node createTicketReviewRow(Ticket t) {
        VBox row = new VBox(15);
        row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 20;");

        // Top Info Row
        HBox topInfo = new HBox(10);
        topInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label date = new Label(t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown Date");
        date.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111827;");
        
        Label loc = new Label("📍 " + t.getLocation());
        loc.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13px;");
        
        topInfo.getChildren().addAll(title, new Label("•"), loc, new Label("•"), date);

        // Body Row (Desc + Image)
        HBox body = new HBox(20);
        
        Label desc = new Label(t.getDescription());
        desc.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");
        desc.setWrapText(true);
        HBox.setHgrow(desc, Priority.ALWAYS);
        desc.setMaxWidth(Double.MAX_VALUE);
        
        body.getChildren().add(desc);

        // Try to load thumbnail if exists
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
            imgView.setFitWidth(150);
            imgView.setFitHeight(100);
            imgView.setPreserveRatio(true);
            loadImageRobustly(t.getImage(), imgView);
            body.getChildren().add(imgView);
        }

        // Action Row
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 15 0 0 0; -fx-border-color: #e5e7eb transparent transparent transparent;");

        Button btnPublish = new Button("✔ Publish");
        btnPublish.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 4;");
        btnPublish.setOnAction(e -> handleAction(t, TicketStatus.PUBLISHED, null));

        Button btnRefuse = new Button("✖ Refuse");
        btnRefuse.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 4;");
        btnRefuse.setOnAction(e -> handleAction(t, TicketStatus.REFUSED, null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Revision Inputs
        TextField revisionNoteInput = new TextField();
        revisionNoteInput.setPromptText("Admin comment for revision...");
        revisionNoteInput.setPrefWidth(250);
        revisionNoteInput.setStyle("-fx-padding: 8; -fx-border-color: #d1d5db; -fx-border-radius: 4;");

        Button btnRevise = new Button("↳ Send Back");
        btnRevise.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 4;");
        btnRevise.setOnAction(e -> {
            String note = revisionNoteInput.getText().trim();
            if (note.isEmpty()) {
                revisionNoteInput.setStyle("-fx-border-color: #ef4444; -fx-padding: 8; -fx-border-radius: 4;");
            } else {
                handleAction(t, TicketStatus.SENT_BACK, note);
            }
        });

        if (t.getStatus() == TicketStatus.IN_PROGRESS) {
            Label completionInfo = new Label("Completion proof submitted by user.");
            completionInfo.setStyle("-fx-text-fill: #92400e; -fx-font-size: 13px; -fx-font-weight: bold;");

            if (t.getCompletionMessage() != null && !t.getCompletionMessage().isBlank()) {
                Label completionMessage = new Label("Message: " + t.getCompletionMessage());
                completionMessage.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");
                completionMessage.setWrapText(true);
                body.getChildren().add(completionMessage);
            }

            Button btnAcceptCompletion = new Button("✔ Accept completion");
            btnAcceptCompletion.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 4;");
            btnAcceptCompletion.setOnAction(e -> {
                t.setAchievedAt(java.time.LocalDateTime.now());
                handleAction(t, TicketStatus.COMPLETED, null);
            });

            Button btnRejectCompletion = new Button("↺ Keep in tickets");
            btnRejectCompletion.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 4;");
            btnRejectCompletion.setOnAction(e -> {
                t.setAchievedAt(null);
                t.setCompletionMessage(null);
                t.setCompletionImage(null);
                handleAction(t, TicketStatus.PUBLISHED, "Completion proof rejected by admin.");
            });

            actions.getChildren().addAll(completionInfo, spacer, btnRejectCompletion, btnAcceptCompletion);
        } else {
            actions.getChildren().addAll(btnPublish, btnRefuse, spacer, revisionNoteInput, btnRevise);
        }

        row.getChildren().addAll(topInfo, body, actions);
        return row;
    }

    private void handleAction(Ticket t, TicketStatus newStatus, String adminNote) {
        if (!isAdminUser()) {
            redirectToDashboard();
            return;
        }
        t.setStatus(newStatus);
        if (adminNote != null) {
            t.setAdminNotes(adminNote);
        }
        ticketService.update(t);
        System.out.println("Ticket " + t.getId() + " updated to " + newStatus.name());
        loadPendingTickets();
    }

    private void loadImageRobustly(String rawPath, javafx.scene.image.ImageView view) {
        try {
            String imgPath = rawPath;
            if (imgPath.startsWith("/uploads/")) {
                imgPath = "http://127.0.0.1:8000" + imgPath;
            }
            javafx.scene.image.Image img = new javafx.scene.image.Image(imgPath, true);
            view.setImage(img);
            
            img.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    try {
                        String fb = "http://localhost/ecospot-web/public" + rawPath;
                        javafx.scene.image.Image fbImg = new javafx.scene.image.Image(fb, true);
                        view.setImage(fbImg);
                        fbImg.errorProperty().addListener((o, old, nw) -> {
                            if (nw) { view.setManaged(false); view.setVisible(false); }
                        });
                    } catch (Exception ex) { view.setManaged(false); view.setVisible(false); }
                }
            });
        } catch (Exception e) {
            view.setManaged(false);
            view.setVisible(false);
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

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
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
