package tn.esprit.user;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;

public class EditUserController {

    @FXML private Label userInfoLabel;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private User userToEdit;
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
    }

    public void setUserToEdit(User user) {
        if (!isAdminUser()) {
            return;
        }
        this.userToEdit = user;
        
        // Setup header text: "email -- Username"
        userInfoLabel.setText(user.getEmail() + " — " + user.getUsername());

        // Select correct role
        String role = user.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            roleComboBox.getSelectionModel().select("Administrator");
        } else if ("NGO".equalsIgnoreCase(role)) {
            roleComboBox.getSelectionModel().select("NGO Partner");
        } else {
            roleComboBox.getSelectionModel().select("Normal user");
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!isAdminUser()) {
            navigateToUserManagement(event);
            return;
        }
        if (userToEdit == null) {
            showError("No user loaded to edit.");
            return;
        }

        if (userToEdit.getId() < 0) {
            showError("Cannot modify fixed system accounts.");
            return;
        }

        String selectedValue = roleComboBox.getValue();
        if (selectedValue == null || selectedValue.trim().isEmpty()) {
            showError("Please select a role.");
            return;
        }
        String systemRole = "USER";
        if ("Administrator".equals(selectedValue)) systemRole = "ADMIN";
        else if ("NGO Partner".equals(selectedValue)) systemRole = "NGO";
        else if (!"Normal user".equals(selectedValue)) {
            showError("Invalid role selected.");
            return;
        }

        // Update User Role directly via DB method
        userService.updateUserRoleDirectly(userToEdit.getId(), systemRole);

        System.out.println("Role updated successfully to " + systemRole + " for user " + userToEdit.getEmail());
        navigateToUserManagement(event);
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
