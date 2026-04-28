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
