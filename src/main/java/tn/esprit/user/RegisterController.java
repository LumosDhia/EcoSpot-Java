package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import tn.esprit.services.UserService;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private TextField zipCodeField;
    @FXML private TextField cityField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Label errorLabel;

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
    void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    private UserService userService = new UserService();

    @FXML
    void handleRegister(ActionEvent event) {
        String result = userService.validateAndRegister(
            firstNameField.getText(),
            lastNameField.getText(),
            emailField.getText(),
            addressField.getText(),
            zipCodeField.getText(),
            cityField.getText(),
            passwordField.getText(),
            repeatPasswordField.getText(),
            termsCheckBox.isSelected()
        );

        if ("SUCCESS".equals(result)) {
            System.out.println("Registration successful!");
            errorLabel.setVisible(false);
            // Maybe navigate to Login or show success
            goToLogin(event);
        } else {
            showError(result);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    void goToEvents(ActionEvent event) {
        navigate(event, "/event/EventManagement.fxml");
    }

    @FXML
    void goToTickets(ActionEvent event) {
        navigate(event, "/ticket/TicketManagement.fxml");
    }

    @FXML
    void goToAchievements(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    void goToBlog(ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
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
