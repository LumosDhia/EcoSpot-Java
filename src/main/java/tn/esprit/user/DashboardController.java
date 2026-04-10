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
    @FXML private Label userSubRoleLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label dashboardInstructionLabel;
    @FXML private Label notificationBadge;

    @FXML private VBox adminSidebarLinks;
    @FXML private VBox ngoSidebarLinks;
    @FXML private VBox userSidebarLinks;
    
    @FXML private FlowPane adminCardsGrid;
    @FXML private FlowPane ngoCardsGrid;
    @FXML private FlowPane userCardsGrid;

    public void setUser(User user) {
        tn.esprit.util.NavigationHistory.track(adminCardsGrid, "/user/Dashboard.fxml");
        String role = user.getRole();
        String username = user.getUsername();

        userNameLabel.setText(username);
        
        // Reset all
        hideAllGrids();

        if (role.equalsIgnoreCase("ADMIN")) {
            sidebarRoleLabel.setText("ADMIN PANEL");
            userSubRoleLabel.setText("Verified Administrator");
            breadcrumbLabel.setText("Admin  ›  Dashboard");
            dashboardInstructionLabel.setText("Manage content, users, and community tickets.");
            notificationBadge.setText("2");

            adminSidebarLinks.setVisible(true);
            adminSidebarLinks.setManaged(true);
            adminCardsGrid.setVisible(true);
            adminCardsGrid.setManaged(true);
            enableCardHoverAnimation(adminCardsGrid);
        } else if (role.equalsIgnoreCase("NGO")) {
            sidebarRoleLabel.setText("NGO");
            userSubRoleLabel.setText("Verified Citizen");
            breadcrumbLabel.setText("Admin  ›  NGO Dashboard");
            dashboardInstructionLabel.setText("Manage your publications and community events.");
            notificationBadge.setText("4");

            ngoSidebarLinks.setVisible(true);
            ngoSidebarLinks.setManaged(true);
            ngoCardsGrid.setVisible(true);
            ngoCardsGrid.setManaged(true);
            enableCardHoverAnimation(ngoCardsGrid);
        } else {
            sidebarRoleLabel.setText("DASHBOARD");
            userSubRoleLabel.setText("Verified Citizen");
            breadcrumbLabel.setText("User  ›  Dashboard");
            dashboardInstructionLabel.setText("Submit reports, browse community tickets and track impact.");
            notificationBadge.setText("24");

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
    void goToUserManagement(ActionEvent event) {
        navigate(event, "/user/UserManagement.fxml");
    }

    @FXML
    void goToPendingTickets(ActionEvent event) {
        navigate(event, "/ticket/PendingTickets.fxml");
    }

    @FXML
    void goToPendingTicketsGrid(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/PendingTickets.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCreateTicket(ActionEvent event) {
        navigate(event, "/ticket/CreateTicket.fxml");
    }

    @FXML
    void goToCreateTicketGrid(MouseEvent event) {
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
}
