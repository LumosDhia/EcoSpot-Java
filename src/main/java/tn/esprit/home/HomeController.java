package tn.esprit.home;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HomeController {

    @FXML private Button blogBtn;
    @FXML private Button heroBlogBtn;
    @FXML private Button dashboardTopBtn;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;

    @FXML
    public void initialize() {
        System.out.println("Home Screen Initialized");
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            // Hide Login/Register
            authLinks.setVisible(false);
            authLinks.setManaged(false);
            
            // Show Dashboard/Logout
            userLinks.setVisible(true);
            userLinks.setManaged(true);
            
            // Set Dashboard text based on role
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                dashboardTopBtn.setText("📊 Admin Dashboard");
            } else if (user.getRole().equalsIgnoreCase("NGO")) {
                dashboardTopBtn.setText("📊 NGO Dashboard");
            } else {
                dashboardTopBtn.setText("📊 My Dashboard");
            }
        } else {
            // Guest mode
            authLinks.setVisible(true);
            authLinks.setManaged(true);
            userLinks.setVisible(false);
            userLinks.setManaged(false);
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        System.out.println("Switching to Dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            System.out.println("Dashboard loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading Dashboard.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    private void goToHome() {
        // Already home, or reload
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) blogBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBlog(ActionEvent event) {
        // blog navigation logic if exists
    }

    @FXML
    private void goToTickets(javafx.event.Event event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ticket/TicketManagement.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
