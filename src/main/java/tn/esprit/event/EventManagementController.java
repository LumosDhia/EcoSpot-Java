package tn.esprit.event;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.services.EventService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventManagementController {

    // Auth & Navigation
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;

    // Event Management
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane eventsGrid;
    
    @FXML private Button manageEventsBtn;
    @FXML private Button manageSponsorsBtn;

    private EventService eventService = new EventService();
    private List<Event> allEvents;

    @FXML
    public void initialize() {
        // Session Management
        boolean isAdmin = false;
        boolean isNgo = false;
        
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            if (authLinks != null) {
                authLinks.setVisible(false);
                authLinks.setManaged(false);
            }
            if (userLinks != null) {
                userLinks.setVisible(true);
                userLinks.setManaged(true);
            }
            
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (dashboardTopBtn != null) {
                if (user.getRole().equalsIgnoreCase("ADMIN")) {
                    dashboardTopBtn.setText("📊 Admin Dashboard");
                    isAdmin = true;
                } else if (user.getRole().equalsIgnoreCase("NGO")) {
                    dashboardTopBtn.setText("📊 NGO Dashboard");
                    isNgo = true;
                } else {
                    dashboardTopBtn.setText("📊 My Dashboard");
                }
            }
        } else {
            if (authLinks != null) {
                authLinks.setVisible(true);
                authLinks.setManaged(true);
            }
            if (userLinks != null) {
                userLinks.setVisible(false);
                userLinks.setManaged(false);
            }
        }
        
        if (manageEventsBtn != null) {
            boolean canManageEvents = isAdmin || isNgo;
            manageEventsBtn.setVisible(canManageEvents);
            manageEventsBtn.setManaged(canManageEvents);
        }
        
        if (manageSponsorsBtn != null) {
            manageSponsorsBtn.setVisible(isAdmin);
            manageSponsorsBtn.setManaged(isAdmin);
        }

        // Event Management
        if (sortChoice != null) {
            sortChoice.setItems(FXCollections.observableArrayList("Next Events", "Capacity High", "Name A-Z"));
            sortChoice.setValue("Next Events");
            sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        }

        refreshData();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        }
    }

    private void refreshData() {
        allEvents = eventService.getAll();
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        if (searchField == null || sortChoice == null || eventsGrid == null) return;
        String query = searchField.getText().toLowerCase();
        List<Event> filtered = allEvents.stream()
                .filter(e -> e.getName().toLowerCase().contains(query) || e.getLocation().toLowerCase().contains(query))
                .collect(Collectors.toList());

        String sort = sortChoice.getValue();
        if ("Next Events".equals(sort)) {
            filtered.sort((e1, e2) -> e1.getStartedAt().compareTo(e2.getStartedAt()));
        } else if ("Capacity High".equals(sort)) {
            filtered.sort((e1, e2) -> Integer.compare(e2.getCapacity(), e1.getCapacity()));
        } else if ("Name A-Z".equals(sort)) {
            filtered.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        }

        displayEvents(filtered);
    }

    private void displayEvents(List<Event> events) {
        if (eventsGrid == null) return;
        eventsGrid.getChildren().clear();
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Event e : events) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventCard.fxml"));
                    Node card = loader.load();
                    EventCardController controller = loader.getController();
                    controller.setData(e, () -> Platform.runLater(() -> refreshData()));
                    
                    Platform.runLater(() -> eventsGrid.getChildren().add(card));
                    Thread.sleep(5); 
                }
                return null;
            }
        };
        new Thread(loadTask).start();
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    private void goToHome() {
        switchScene("/home/Home.fxml");
    }
    
    @FXML
    private void goToTickets(ActionEvent event) {
        navigate(event, "/ticket/TicketManagement.fxml");
    }
    
    @FXML
    private void goToBlog() {
        switchScene("/blog/BlogManagement.fxml");
    }

    @FXML
    private void goToAchievements(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    private void goToAdmin() {
        switchScene("/event/EventAdmin.fxml");
    }

    @FXML
    private void goToSponsors() {
        switchScene("/event/SponsorManagement.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (searchField != null && searchField.getScene() != null) {
                stage = (Stage) searchField.getScene().getWindow();
            } else {
                return;
            }
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigate(javafx.scene.input.MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(searchField);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(searchField);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(searchField);
    }
}
