package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class ArticlesManagementController {

    @FXML private Button homeBtn;

    @FXML
    public void initialize() {
        System.out.println("Articles Management Screen Initialized");
    }

    @FXML
    private void goToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHomeMouse(javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToBlog(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/BlogManagement.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
