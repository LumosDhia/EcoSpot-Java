package tn.esprit.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.util.List;

public class UserManagementController {

    @FXML private VBox userListContainer;
    
    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (!isAdminUser()) {
            javafx.application.Platform.runLater(() -> {
                if (userListContainer != null && userListContainer.getScene() != null) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
                        Parent root = loader.load();
                        DashboardController controller = loader.getController();
                        if (controller != null && tn.esprit.util.SessionManager.getCurrentUser() != null) {
                            controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
                        }
                        Stage stage = (Stage) userListContainer.getScene().getWindow();
                        stage.getScene().setRoot(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return;
        }
        System.out.println("UserManagementController Initialized");
        refreshUserList();
    }

    private void refreshUserList() {
        if (!isAdminUser()) {
            return;
        }
        System.out.println("Refreshing user list...");
        userListContainer.getChildren().clear();
        List<User> users = userService.getAllUsers();
        System.out.println("Found " + users.size() + " users.");

        for (User user : users) {
            userListContainer.getChildren().add(createUserRow(user));
        }
    }

    private HBox createUserRow(User user) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: white; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0;");
        
        // Identity Column
        HBox identityBox = new HBox(15);
        identityBox.setAlignment(Pos.CENTER_LEFT);
        identityBox.setMinWidth(300);
        HBox.setHgrow(identityBox, Priority.ALWAYS);

        StackPane avatar = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.web(getAvatarColor(user.getUsername())));
        Label initials = new Label(getInitials(user.getUsername()));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initials);

        VBox identityText = new VBox(2);
        Label nameLabel = new Label(user.getUsername());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2d3748;");
        Label emailLabel = new Label("✉ " + user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px;");
        identityText.getChildren().addAll(nameLabel, emailLabel);
        if (user.isTimedOut()) {
            Label timeoutBadge = new Label("TIMEOUT");
            timeoutBadge.setStyle("-fx-background-color: #fff5f5; -fx-text-fill: #e53e3e; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 5; -fx-background-radius: 3;");
            identityText.getChildren().add(timeoutBadge);
        }
        identityBox.getChildren().addAll(avatar, identityText);

        // Role Column
        Label roleBadge = new Label(formatRole(user.getRole()));
        roleBadge.setMinWidth(200);
        roleBadge.setPadding(new Insets(5, 12, 5, 12));
        roleBadge.setStyle(getRoleStyle(user.getRole()));

        // Management Column
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(180); // Increased width for 3 buttons
        
        Button timeoutBtn = new Button(user.isTimedOut() ? "🔓" : "⏳");
        timeoutBtn.setTooltip(new javafx.scene.control.Tooltip(user.isTimedOut() ? "Lift Timeout" : "Apply 24h Timeout"));
        timeoutBtn.setStyle(user.isTimedOut() 
            ? "-fx-background-color: #f0fff4; -fx-text-fill: #38a169; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;"
            : "-fx-background-color: #fffaf0; -fx-text-fill: #dd6b20; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");
        
        // Only allow timing out Community Members (not Admins or NGOs)
        boolean isCitizen = !"ADMIN".equalsIgnoreCase(user.getRole()) && !"NGO".equalsIgnoreCase(user.getRole());
        if (!isCitizen && !user.isTimedOut()) {
            timeoutBtn.setDisable(true);
            timeoutBtn.setOpacity(0.0); // Hide it but keep space
            timeoutBtn.setManaged(false);
        }

        timeoutBtn.setOnAction(e -> {
            if (user.isTimedOut()) {
                userService.updateTimeout(user.getId(), null);
            } else {
                userService.updateTimeout(user.getId(), java.time.LocalDateTime.now().plusHours(24));
            }
            refreshUserList();
        });

        Button editBtn = new Button("✏"); // Edit
        editBtn.setTooltip(new javafx.scene.control.Tooltip("Edit User"));
        editBtn.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");
        if (user.getId() < 0) editBtn.setDisable(true);
        editBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/EditUser.fxml"));
                Parent root = loader.load();
                EditUserController controller = loader.getController();
                controller.setUserToEdit(user);
                
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Button deleteBtn = new Button("🗑");
        deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Delete User"));
        deleteBtn.setStyle("-fx-background-color: #fff5f5; -fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");
        if (user.getId() < 0) deleteBtn.setDisable(true);
        deleteBtn.setOnAction(e -> {
            if (!isAdminUser()) return;
            userService.removeUser(user.getId());
            refreshUserList();
        });

        actions.getChildren().addAll(timeoutBtn, editBtn, deleteBtn);

        row.getChildren().addAll(identityBox, roleBadge, actions);
        return row;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length > 1) return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String name) {
        return "#38a169"; // All avatars green to match design
    }

    private String formatRole(String role) {
        if ("ADMIN".equals(role)) return "🛡 Administrator";
        if ("NGO".equals(role)) return "🤝 NGO Partner";
        return "👤 Community Member";
    }

    private String getRoleStyle(String role) {
        if ("ADMIN".equals(role)) return "-fx-background-color: #fff5f5; -fx-text-fill: #c53030; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 12px;";
        return "-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 12px;";
    }

    @FXML
    void goToCreateUser(ActionEvent event) {
        if (!isAdminUser()) {
            goToDashboard(event);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/CreateUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAdminUser() {
        User current = tn.esprit.util.SessionManager.getCurrentUser();
        return current != null && "ADMIN".equalsIgnoreCase(current.getRole());
    }
}
