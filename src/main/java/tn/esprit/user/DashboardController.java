package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
