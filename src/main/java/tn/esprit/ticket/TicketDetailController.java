package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class TicketDetailController {

    @FXML private Label statusBadge;
    @FXML private Label priorityBadge;
    @FXML private Label titleLabel;
    @FXML private Label domainLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView ticketImageView;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private HBox completionBox;
    @FXML private Button dashboardTopBtn;

    private Ticket currentTicket;
    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
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
            
            // Hide completion box for guests
            completionBox.setVisible(false);
            completionBox.setManaged(false);
        }
    }

    public void setTicket(Ticket t) {
        this.currentTicket = t;
        
        titleLabel.setText(t.getTitle());
        descriptionLabel.setText(t.getDescription());
        locationLabel.setText("📍 " + t.getLocation());
        domainLabel.setText("🏢 Domain: " + (t.getDomain() != null ? t.getDomain().name() : "Other"));
        dateLabel.setText("Created " + (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown"));
        statusBadge.setText(t.getStatus().name());
        priorityBadge.setText(t.getPriority().name());

        // Priority Badge styling
        priorityBadge.getStyleClass().removeAll("ticket-badge-low", "ticket-badge-medium", "ticket-badge-high");
        if (t.getPriority() == TicketPriority.LOW) priorityBadge.getStyleClass().add("ticket-badge-low");
        else if (t.getPriority() == TicketPriority.MEDIUM) priorityBadge.getStyleClass().add("ticket-badge-medium");
        else priorityBadge.getStyleClass().add("ticket-badge-high");

        // Load image if path exists
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                // If the path starts with /uploads, it's relative to the project root or local storage
                // For now, I'll attempt to load it. If it fails, I'll hide the view.
                Image img = new Image(t.getImage(), true); 
                ticketImageView.setImage(img);
            } catch (Exception e) {
                ticketImageView.setManaged(false);
                ticketImageView.setVisible(false);
            }
        } else {
            ticketImageView.setManaged(false);
            ticketImageView.setVisible(false);
        }
    }

    @FXML
    void handleComplete(ActionEvent event) {
        if (currentTicket != null) {
            currentTicket.setStatus(TicketStatus.COMPLETED);
            currentTicket.setAchievedAt(java.time.LocalDateTime.now());
            ticketService.update(currentTicket);
            goToTickets(event);
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        goToHome();
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
    private void goToHome() {
        navigate(null, "/home/Home.fxml");
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
                stage = (Stage) titleLabel.getScene().getWindow();
            }
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
