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
            System.out.println("MainFX: Loading /home/Home.fxml...");
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Scene scene = new Scene(root, 1000, 800);
            primaryStage.setTitle("EcoSpot Desktop App");
            
            // Set App Icon
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/leaf.png")));
            } catch (Exception e) {
                System.out.println("MainFX: Warning - Could not load app icon: " + e.getMessage());
            }
            
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
            primaryStage.toFront();
            System.out.println("MainFX: Window displayed successfully.");

        } catch (IOException e) {
            System.err.println("MainFX: CRITICAL ERROR - Failed to load Home.fxml");
            e.printStackTrace();
        }
    }
}
