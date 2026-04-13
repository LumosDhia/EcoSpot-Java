package tn.esprit.home;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HomeController {

    @FXML private Button blogBtn;
    @FXML private Button heroBlogBtn;

    @FXML
    public void initialize() {
        System.out.println("Home Screen Initialized");
    }

    @FXML
    private void goToBlog() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/BlogManagement.fxml"));
            Stage stage = (Stage) blogBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) blogBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToEvents() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/event/EventManagement.fxml"));
            Stage stage = (Stage) blogBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToSponsors(ActionEvent event) {
        switchScene("/event/SponsorManagement.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) blogBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(blogBtn);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(blogBtn);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(blogBtn);
    }
}
