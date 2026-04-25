package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.util.EmailService;

import java.io.IOException;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    
    @FXML private Label infoLabel;
    @FXML private Label errorLabel;

    private UserService userService = new UserService();
    private String userEmail;

    @FXML
    public void initialize() {
        infoLabel.setText("Please enter your email to receive a password reset code.");
    }

    @FXML
    void handleSendCode(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email.");
            return;
        }

        // Check if user exists
        boolean userExists = false;
        for (User u : userService.getAllUsers()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                userExists = true;
                break;
            }
        }

        if (!userExists) {
            showError("No account found with this email.");
            return;
        }

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        
        if (userService.setResetCode(email, code)) {
            try {
                String htmlBody = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>" +
                    "<h1 style='color: #48bb78;'>Reset Your Password</h1>" +
                    "<p>Hello,</p>" +
                    "<p>We received a request to reset your password for your <strong>EcoSpot</strong> account.</p>" +
                    "<p>Please use the following verification code to proceed:</p>" +
                    "<div style='background: #f7fafc; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #2d3748; border-radius: 5px; margin: 20px 0;'>" +
                    code + "</div>" +
                    "<p>This code will expire in <strong>15 minutes</strong>.</p>" +
                    "<p>If you did not request a password reset, please ignore this email.</p>" +
                    "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='font-size: 12px; color: #a0aec0;'>Cheers,<br>EcoSpot Support Team</p>" +
                    "</div></body></html>";

                EmailService.sendEmail(email, "EcoSpot - Password Reset Code", htmlBody);
                
                this.userEmail = email;
                showStep(2);
                infoLabel.setText("A reset code has been sent to " + email);
                errorLabel.setVisible(false);
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("An error occurred while generating the code.");
        }
    }

    @FXML
    void handleVerifyCode(ActionEvent event) {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            showError("Please enter the verification code.");
            return;
        }

        if (userService.verifyResetCode(userEmail, code)) {
            showStep(3);
            infoLabel.setText("Code verified! Please enter your new password.");
            errorLabel.setVisible(false);
        } else {
            showError("Invalid or expired verification code.");
        }
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty()) {
            showError("Please enter a new password.");
            return;
        }
        if (newPass.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showError("Passwords do not match.");
            return;
        }

        if (userService.updatePassword(userEmail, newPass)) {
            infoLabel.setText("Password reset successful! You can now log in.");
            step3Box.setVisible(false);
            step3Box.setManaged(false);
            errorLabel.setVisible(false);
        } else {
            showError("Failed to update password.");
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showStep(int step) {
        step1Box.setVisible(step == 1);
        step1Box.setManaged(step == 1);
        step2Box.setVisible(step == 2);
        step2Box.setManaged(step == 2);
        step3Box.setVisible(step == 3);
        step3Box.setManaged(step == 3);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
