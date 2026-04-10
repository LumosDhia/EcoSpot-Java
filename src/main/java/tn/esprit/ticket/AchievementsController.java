package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

public class AchievementsController {

    @FXML private FlowPane achievementsFlowPane;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        System.out.println("Achievements Screen Initialized");
        
        // Session Management
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            authLinks.setVisible(false);
            authLinks.setManaged(false);
            userLinks.setVisible(true);
            userLinks.setManaged(true);
            
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                dashboardTopBtn.setText("📊 Admin Dashboard");
            } else if (user.getRole().equalsIgnoreCase("NGO")) {
                dashboardTopBtn.setText("📊 NGO Dashboard");
            } else {
                dashboardTopBtn.setText("📊 My Dashboard");
            }
        } else {
            authLinks.setVisible(true);
            authLinks.setManaged(true);
            userLinks.setVisible(false);
            userLinks.setManaged(false);
        }

        loadAchievementData();
    }

    private void loadAchievementData() {
        try {
            achievementsFlowPane.getChildren().clear();
            
            int count = 0;
            for (Ticket t : ticketService.getAll()) {
                if (t.getStatus() == TicketStatus.COMPLETED) {
                    Node card = createAchievementCard(t);
                    achievementsFlowPane.getChildren().add(card);
                    count++;
                }
            }
            
            System.out.println("Loaded " + count + " achievements.");
        } catch (Exception e) {
            System.err.println("Error fetching achievements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Node createAchievementCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("achievement-card");

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("achievement-header");
        header.setAlignment(Pos.CENTER_LEFT);
        
        String dateStr = t.getAchievedAt() != null ? t.getAchievedAt().format(formatter) : 
            (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown Date");
            
        Label headerTitle = new Label("✔ Achieved " + dateStr);
        headerTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().add(headerTitle);

        // Content
        VBox content = new VBox(10);
        content.getStyleClass().add("achievement-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");
        loc.setWrapText(true);

        Label completedByLabel = new Label("Completed by: " + (t.getCompletedById() != null ? "User #" + t.getCompletedById() : "—"));
        completedByLabel.getStyleClass().add("achievement-completed-by");

        content.getChildren().addAll(title, loc, completedByLabel);

        // Footer / Button
        VBox footer = new VBox();
        footer.setStyle("-fx-border-color: #e5e7eb transparent transparent transparent; -fx-padding: 15;");
        
        Button btnView = new Button("View ticket");
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
        
        footer.getChildren().add(btnView);

        card.getChildren().addAll(header, content, footer);

        return card;
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        navigate(event, "/user/Dashboard.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    private void goToHome(javafx.scene.input.MouseEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }
    
    @FXML
    private void goToTickets(ActionEvent event) {
        navigate(event, "/ticket/TicketManagement.fxml");
    }
    
    @FXML
    private void goToBlog(ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) achievementsFlowPane.getScene().getWindow();
            }
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void navigate(javafx.scene.input.MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
