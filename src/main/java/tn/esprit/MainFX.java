package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Scene scene = new Scene(root, 1000, 800);
            primaryStage.setTitle("EcoSpot Desktop App");
            
            // Set App Icon
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/leaf.png")));
            
            primaryStage.setScene(scene);
            primaryStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
