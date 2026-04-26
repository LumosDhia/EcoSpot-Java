package tn.esprit.user;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class DashboardController {

    @FXML private Label sidebarRoleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label dashboardInstructionLabel;
    @FXML private javafx.scene.layout.HBox timeoutBanner;
    @FXML private Label timeoutLabel;
    @FXML private VBox createTicketCard;
    @FXML private VBox createTicketLockOverlay;

    @FXML private VBox adminSidebarLinks;
    @FXML private VBox ngoSidebarLinks;
    @FXML private VBox userSidebarLinks;
    @FXML private javafx.scene.control.Button sidebarCreateTicketBtn;
    
    @FXML private FlowPane adminCardsGrid;
    @FXML private FlowPane ngoCardsGrid;
    @FXML private FlowPane userCardsGrid;

    @FXML
    public void initialize() {
        // When screens are restored via history, setUser(...) might not be called manually.
        // In that case, bind the dashboard from the active session automatically.
        if (tn.esprit.util.SessionManager.isLoggedIn() && tn.esprit.util.SessionManager.getCurrentUser() != null) {
            setUser(tn.esprit.util.SessionManager.getCurrentUser());
        } else {
            hideAllGrids();
        }
    }

    public void setUser(User user) {
        tn.esprit.util.NavigationHistory.track(adminCardsGrid, "/user/Dashboard.fxml");
        String role = user.getRole();
        String username = user.getUsername();

        userNameLabel.setText(username);
        
        // Handle Timeout Banner
        if (user.isTimedOut()) {
            timeoutBanner.setVisible(true);
            timeoutBanner.setManaged(true);
            timeoutLabel.setText("Your account is temporarily restricted from submitting or editing tickets due to multiple spam flags. Activities will be restored after " + 
                user.getTimeoutUntil().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Subtle pulse animation
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timeoutBanner.opacityProperty(), 0.7)),
                new KeyFrame(Duration.seconds(1), new KeyValue(timeoutBanner.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(timeoutBanner.opacityProperty(), 0.7))
            );
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.play();

            if (createTicketCard != null) {
                createTicketLockOverlay.setVisible(true);
                createTicketLockOverlay.setManaged(true);
                createTicketCard.setDisable(true);
                createTicketCard.setEffect(new ColorAdjust(0, -0.8, -0.1, 0));
                createTicketCard.setOpacity(1.0); // Keep opacity high for clarity
            }
            if (sidebarCreateTicketBtn != null) {
                sidebarCreateTicketBtn.setDisable(true);
                sidebarCreateTicketBtn.setOpacity(0.4);
            }
        } else {
            timeoutBanner.setVisible(false);
            timeoutBanner.setManaged(false);

            if (createTicketCard != null) {
                createTicketLockOverlay.setVisible(false);
                createTicketLockOverlay.setManaged(false);
                createTicketCard.setDisable(false);
                createTicketCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.05)));
                createTicketCard.setOpacity(1.0);
            }
            if (sidebarCreateTicketBtn != null) {
                sidebarCreateTicketBtn.setDisable(false);
                sidebarCreateTicketBtn.setOpacity(1.0);
            }
        }
        
        // Reset all
        hideAllGrids();

        if (role.equalsIgnoreCase("ADMIN")) {
            sidebarRoleLabel.setText("ADMIN PANEL");
            breadcrumbLabel.setText("Admin  ›  Dashboard");
            dashboardInstructionLabel.setText("Manage content, users, and community tickets.");

            adminSidebarLinks.setVisible(true);
            adminSidebarLinks.setManaged(true);
            adminCardsGrid.setVisible(true);
            adminCardsGrid.setManaged(true);
            enableCardHoverAnimation(adminCardsGrid);
        } else if (role.equalsIgnoreCase("NGO")) {
            sidebarRoleLabel.setText("NGO");
            breadcrumbLabel.setText("NGO  ›  Dashboard");
            dashboardInstructionLabel.setText("Manage your publications and community events.");

            ngoSidebarLinks.setVisible(true);
            ngoSidebarLinks.setManaged(true);
            ngoCardsGrid.setVisible(true);
            ngoCardsGrid.setManaged(true);
            enableCardHoverAnimation(ngoCardsGrid);
        } else {
            sidebarRoleLabel.setText("DASHBOARD");
            breadcrumbLabel.setText("User  ›  Dashboard");
            dashboardInstructionLabel.setText("Submit reports, browse community tickets and track impact.");

            userSidebarLinks.setVisible(true);
            userSidebarLinks.setManaged(true);
            userCardsGrid.setVisible(true);
            userCardsGrid.setManaged(true);
            enableCardHoverAnimation(userCardsGrid);
        }
    }

    private void enableCardHoverAnimation(FlowPane grid) {
        for (Node node : grid.getChildren()) {
            if (!(node instanceof VBox)) continue;
            VBox card = (VBox) node;
            card.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> animateCard(card, true));
            card.addEventHandler(MouseEvent.MOUSE_EXITED, event -> animateCard(card, false));
        }
    }

    private void animateCard(VBox card, boolean hover) {
        double rotate = hover ? -2.0 : 0.0;
        double translateY = hover ? -6.0 : 0.0;
        double scale = hover ? 1.02 : 1.0;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(150),
                new KeyValue(card.rotateProperty(), rotate),
                new KeyValue(card.translateYProperty(), translateY),
                new KeyValue(card.scaleXProperty(), scale),
                new KeyValue(card.scaleYProperty(), scale)
            )
        );
        timeline.play();

        if (hover) {
            card.setEffect(new DropShadow(18, Color.rgb(45, 106, 79, 0.22)));
        } else {
            card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.05)));
        }
    }

    private void hideAllGrids() {
        adminSidebarLinks.setVisible(false);
        adminSidebarLinks.setManaged(false);
        ngoSidebarLinks.setVisible(false);
        ngoSidebarLinks.setManaged(false);
        userSidebarLinks.setVisible(false);
        userSidebarLinks.setManaged(false);

        adminCardsGrid.setVisible(false);
        adminCardsGrid.setManaged(false);
        ngoCardsGrid.setVisible(false);
        ngoCardsGrid.setManaged(false);
        userCardsGrid.setVisible(false);
        userCardsGrid.setManaged(false);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    void goToPublicSite(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    void goToUserManagement(ActionEvent event) {
        navigate(event, "/user/UserManagement.fxml");
    }

    @FXML
    void goToPendingTickets(ActionEvent event) {
        if (isAdminUser()) {
            navigate(event, "/ticket/PendingTickets.fxml");
        } else {
            navigate(event, "/ticket/TicketManagement.fxml");
        }
    }

    @FXML
    void goToAllTickets(ActionEvent event) {
        if (isAdminUser()) {
            navigate(event, "/ticket/AdminAllTickets.fxml");
        } else {
            navigate(event, "/ticket/TicketManagement.fxml");
        }
    }

    @FXML
    void goToCompletions(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    void goToPendingTicketsGrid(MouseEvent event) {
        try {
            String target = isAdminUser() ? "/ticket/PendingTickets.fxml" : "/ticket/TicketManagement.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(target));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCompletionsGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/Achievements.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCreateTicket(ActionEvent event) {
        User u = tn.esprit.util.SessionManager.getCurrentUser();
        if (u != null && u.isTimedOut()) {
            return; // Safety check
        }
        navigate(event, "/ticket/CreateTicket.fxml");
    }

    @FXML
    void goToCreateTicketGrid(MouseEvent event) {
        User u = tn.esprit.util.SessionManager.getCurrentUser();
        if (u != null && u.isTimedOut()) {
            return; // Safety check
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/CreateTicket.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToMyTickets(ActionEvent event) {
        navigate(event, "/ticket/MyTickets.fxml");
    }

    @FXML
    void goToAchievements(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    void goToAchievementsGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/Achievements.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToMyTicketsGrid(MouseEvent event) {
        try {
            System.out.println("Card clicked: My Tickets");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/MyTickets.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            System.out.println("Card navigation successful.");
        } catch (IOException e) {
            System.err.println("Card navigation failed.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleCardClick(MouseEvent event) {
        try {
            System.out.println("Card clicked: User Management");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/UserManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            System.out.println("Card navigation successful.");
        } catch (IOException e) {
            System.err.println("Card navigation failed.");
            e.printStackTrace();
        }
    }

    @FXML
    void goToBlogManagement(ActionEvent event) {
        navigate(event, "/blog/ArticlesManagement.fxml");
    }

    @FXML
    void goToBlogManagementGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/ArticlesManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCreateArticleGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/NewArticle.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToEventManagement(ActionEvent event) {
        navigate(event, "/event/EventManagement.fxml");
    }

    @FXML
    void goToEventManagementGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToSponsorManagement(ActionEvent event) {
        navigate(event, "/event/SponsorManagement.fxml");
    }

    @FXML
    void goToSponsorManagementGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/SponsorManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCommentsManagement(ActionEvent event) {
        navigate(event, "/blog/CommentsManagement.fxml");
    }

    @FXML
    void goToCommentsManagementGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/CommentsManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToTaxonomyManagementGrid(MouseEvent event) {
        navigateToTaxonomy(event, null);
    }

    @FXML
    void goToCategoriesManagementGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/CategoriesManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToTagsManagementGrid(MouseEvent event) {
        navigateToTaxonomy(event, "TAGS");
    }

    private void navigateToTaxonomy(MouseEvent event, String section) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/TaxonomyManagement.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof tn.esprit.blog.TaxonomyManagementController) {
                tn.esprit.blog.TaxonomyManagementController taxonomyController =
                        (tn.esprit.blog.TaxonomyManagementController) controller;
                if ("CATEGORIES".equals(section)) {
                    taxonomyController.showCategoriesOnly();
                } else if ("TAGS".equals(section)) {
                    taxonomyController.showTagsOnly();
                }
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            System.out.println("Navigating to: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            System.out.println("Navigation successful.");
        } catch (IOException e) {
            System.err.println("Navigation failed for " + fxmlPath);
            e.printStackTrace();
        }
    }

    private boolean isAdminUser() {
        User current = tn.esprit.util.SessionManager.getCurrentUser();
        return current != null && "ADMIN".equalsIgnoreCase(current.getRole());
    }
}
