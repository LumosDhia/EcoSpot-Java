package tn.esprit.user;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;

public class CreateUserController {

    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (!isAdminUser()) {
            return;
        }
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Normal user",
                "Administrator",
                "NGO Partner"
        ));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    void handleCreateUser(ActionEvent event) {
        if (!isAdminUser()) {
            navigateToUserManagement(event);
            return;
        }
        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        String selectedRole = roleComboBox.getValue();
        String systemRole = "USER";
        if ("Administrator".equals(selectedRole)) systemRole = "ADMIN";
        else if ("NGO Partner".equals(selectedRole)) systemRole = "NGO";

        String result = userService.validateAndRegisterAdmin(
                firstName, lastName, email, systemRole, password, confirmPassword
        );

        if ("SUCCESS".equals(result)) {
            System.out.println("User created successfully by Admin.");
            navigateToUserManagement(event);
        } else {
            showError(result);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        navigateToUserManagement(event);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateToUserManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/UserManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAdminUser() {
        User current = tn.esprit.util.SessionManager.getCurrentUser();
        return current != null && "ADMIN".equalsIgnoreCase(current.getRole());
    }
}
