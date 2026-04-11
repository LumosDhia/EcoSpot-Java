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
    public void initialize() {
        // Prevent showing login form when user is already authenticated.
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            javafx.application.Platform.runLater(this::redirectIfAlreadyLoggedIn);
        }
    }

    @FXML
    void goToHome(javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    void handleLogin(ActionEvent event) {
        performLogin(emailField.getText(), passwordField.getText(), event);
    }

    @FXML
    void handleQuickAdmin(ActionEvent event) {
        performLogin("admin@mail.com", "admin123", event);
    }

    @FXML
    void handleQuickNGO(ActionEvent event) {
        performLogin("ngo@mail.com", "ngo123", event);
    }

    @FXML
    void handleQuickUser(ActionEvent event) {
        performLogin("user@mail.com", "user123", event);
    }

    private void performLogin(String email, String password, ActionEvent event) {
        if (email.isEmpty()) {
            showError("Please enter your email.");
            return;
        }
        User user = userService.authenticate(email, password);
        if (user != null) {
            tn.esprit.util.SessionManager.login(user);
            navigate(event, "/home/Home.fxml");
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

    private void redirectIfAlreadyLoggedIn() {
        try {
            User current = tn.esprit.util.SessionManager.getCurrentUser();
            if (current == null || emailField.getScene() == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(current);
            }
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            // Fallback route if dashboard loading fails.
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ignored) {
            }
        }
    }
}
