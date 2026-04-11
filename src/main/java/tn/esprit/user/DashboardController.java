package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private Label dashboardRoleLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label roleDescriptionLabel;
    @FXML private Button userMgmtBtn;
    @FXML private VBox adminCard;

    public void setUser(User user) {
        String role = user.getRole();
        String username = user.getUsername();

        welcomeLabel.setText("Welcome, " + username + "!");
        
        switch (role.toUpperCase()) {
            case "ADMIN":
                dashboardRoleLabel.setText("Admin Dashboard");
                roleDescriptionLabel.setText("You have full control over the platform.");
                userMgmtBtn.setVisible(true);
                userMgmtBtn.setManaged(true);
                adminCard.setVisible(true);
                adminCard.setManaged(true);
                break;
            case "NGO":
                dashboardRoleLabel.setText("NGO Dashboard");
                roleDescriptionLabel.setText("Manage your organization events and impact.");
                break;
            default:
                dashboardRoleLabel.setText("User Dashboard");
                roleDescriptionLabel.setText("Discover events and read the latest news.");
                break;
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    void goToUserManagement(ActionEvent event) {
        navigate(event, "/user/UserManagement.fxml");
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
