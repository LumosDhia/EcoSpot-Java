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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class TicketManagementController {

    @FXML private FlowPane ticketsFlowPane;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    private final TicketService ticketService = new TicketService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        System.out.println("Ticket Management Initialized - Loading Cards...");
        
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

        loadTicketData();
    }

    private void loadTicketData() {
        try {
            ticketsFlowPane.getChildren().clear();
            
            for (Ticket t : ticketService.getAll()) {
                if (t.getStatus() != TicketStatus.COMPLETED) {
                    Node card = createTicketCard(t);
                    ticketsFlowPane.getChildren().add(card);
                }
            }
            
            System.out.println("Loaded " + ticketsFlowPane.getChildren().size() + " active ticket cards.");
        } catch (Exception e) {
            System.err.println("Error fetching tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Node createTicketCard(Ticket t) {
        VBox card = new VBox();
        card.getStyleClass().add("ticket-card");

        VBox content = new VBox();
        content.getStyleClass().add("ticket-card-content");
        content.setSpacing(10);
        VBox.setVgrow(content, Priority.ALWAYS);

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        title.setWrapText(true);

        Label loc = new Label("📍 " + t.getLocation());
        loc.getStyleClass().add("ticket-location");
        loc.setWrapText(true);

        // Ticket Image Thumbnail
        ImageView cardImg = new ImageView();
        cardImg.setFitWidth(340);
        cardImg.setFitHeight(180);
        cardImg.setPreserveRatio(true);
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            loadImageRobustly(t.getImage(), cardImg);
        } else {
            cardImg.setManaged(false);
        }

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("ticket-description");
        desc.setWrapText(true);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        
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
        
        Button btnComplete = new Button("✔ I completed this");
        btnComplete.getStyleClass().add("ticket-btn-complete");
        btnComplete.setOnAction(e -> markCompleted(t));
        
        buttons.getChildren().add(btnView);
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            buttons.getChildren().add(btnComplete);
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

    private void markCompleted(Ticket t) {
        System.out.println("Marking ticket completed: " + t.getId());
        t.setStatus(TicketStatus.COMPLETED);
        t.setAchievedAt(java.time.LocalDateTime.now());
        ticketService.update(t);
        loadTicketData();
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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void goToAchievements(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
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

    private void navigate(javafx.scene.input.MouseEvent event, String fxmlPath) {


        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
