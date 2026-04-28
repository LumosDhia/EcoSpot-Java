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
        killOtherInstances();
        launch(args);
    }

    private static void killOtherInstances() {
        try {
            // For Windows: Kill other windows with the same title
            // Use taskkill to find and terminate processes with our app title
            new ProcessBuilder("taskkill", "/F", "/FI", "WINDOWTITLE eq EcoSpot Desktop App", "/T").start();
            // Small delay to let the OS release the process
            Thread.sleep(500);
        } catch (Exception e) {
            // Silently fail if taskkill is not available or fails
        }
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
