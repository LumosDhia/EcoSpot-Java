package tn.esprit.util;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowUtils {

    public static void minimize(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setIconified(true);
    }

    public static void toggleFullScreen(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    public static void close(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void makeDraggable(Stage stage, Node root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }
}
