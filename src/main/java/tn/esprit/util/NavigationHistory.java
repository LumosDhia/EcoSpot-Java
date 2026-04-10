package tn.esprit.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

public final class NavigationHistory {
    private static final Map<Window, Deque<String>> HISTORY = new WeakHashMap<>();
    private static final int MAX_SIZE = 40;

    private NavigationHistory() {
    }

    public static void track(Node node, String fxmlPath) {
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((wObs, oldWindow, newWindow) -> {
                if (newWindow != null) {
                    push(newWindow, fxmlPath);
                }
            });
            if (newScene.getWindow() != null) {
                push(newScene.getWindow(), fxmlPath);
            }
        });
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            push(node.getScene().getWindow(), fxmlPath);
        }
    }

    public static boolean goBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Deque<String> stack = HISTORY.get(stage);
            if (stack == null || stack.size() < 2) {
                return false;
            }

            stack.removeLast(); // current
            String previous = stack.peekLast();
            if (previous == null) {
                return false;
            }

            Parent root = FXMLLoader.load(NavigationHistory.class.getResource(previous));
            stage.getScene().setRoot(root);
            return true;
        } catch (IOException | ClassCastException ex) {
            return false;
        }
    }

    private static void push(Window window, String fxmlPath) {
        Deque<String> stack = HISTORY.computeIfAbsent(window, k -> new ArrayDeque<>());
        if (stack.isEmpty() || !fxmlPath.equals(stack.peekLast())) {
            stack.addLast(fxmlPath);
            while (stack.size() > MAX_SIZE) {
                stack.removeFirst();
            }
        }
    }
}

