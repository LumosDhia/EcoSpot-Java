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
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;
import java.io.IOException;
import tn.esprit.services.UserService;
import tn.esprit.util.GeocodeService;
import tn.esprit.util.CaptchaService;

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
    @FXML private ImageView captchaImageView;
    @FXML private TextField captchaField;

    private UserService userService = new UserService();
    private GeocodeService geocodeService = new GeocodeService();
    private CaptchaService captchaService = new CaptchaService();
    private ContextMenu citySuggestions = new ContextMenu();

    @FXML
    public void initialize() {
        refreshCaptcha(null);
        // ... (existing initialize logic)
        cityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() >= 3) {
                geocodeService.searchCities(newValue).thenAccept(cities -> {
                    Platform.runLater(() -> showCitySuggestions(cities));
                });
            } else {
                Platform.runLater(() -> citySuggestions.hide());
            }
        });
    }

    private void showCitySuggestions(java.util.List<String> cities) {
        if (cities.isEmpty()) {
            citySuggestions.hide();
            return;
        }

        citySuggestions.getItems().clear();
        for (String city : cities) {
            MenuItem item = new MenuItem(city);
            item.setStyle("-fx-font-size: 14px; -fx-padding: 5 10 5 10;");
            item.setOnAction(e -> {
                cityField.setText(city);
                citySuggestions.hide();
            });
            citySuggestions.getItems().add(item);
        }

        // Only show if the field is still focused to avoid annoying popups
        if (cityField.isFocused()) {
            if (citySuggestions.isShowing()) {
                citySuggestions.hide(); // Hide and reshow to refresh position/size
            }
            citySuggestions.show(cityField, javafx.geometry.Side.BOTTOM, 0, 0);
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
    void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        // Validate Captcha first
        if (!captchaService.verify(captchaField.getText())) {
            showError("Invalid robot verification code.");
            refreshCaptcha(null);
            captchaField.clear();
            return;
        }

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
            goToLogin(event);
        } else {
            showError(result);
        }
    }

    @FXML
    void refreshCaptcha(ActionEvent event) {
        captchaImageView.setImage(captchaService.generateCaptcha());
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    void handleFaceEnroll(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email first to enroll face.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/FaceLogin.fxml"));
            Parent root = loader.load();
            
            FaceLoginController controller = loader.getController();
            controller.setEnrollmentMode(email);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("EcoSpot - Face ID Enrollment");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load Face Enrollment view.");
        }
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
