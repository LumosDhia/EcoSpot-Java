package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import tn.esprit.services.UserService;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserService userService = new UserService();

    @FXML
    void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showError("Please enter your email.");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter a password.");
            return;
        }

        User user = userService.authenticate(email, password);
        if (user != null) {
            System.out.println("Login successful for: " + user.getUsername());
            errorLabel.setVisible(false);
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
                Parent root = loader.load();
                
                DashboardController controller = loader.getController();
                controller.setUser(user);
                
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showError("Invalid email or password.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
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
