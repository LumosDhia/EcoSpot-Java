package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    void goToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        // Login logic here
        System.out.println("Login button clicked");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        // Register logic here
        System.out.println("Register button clicked");
    }
}
