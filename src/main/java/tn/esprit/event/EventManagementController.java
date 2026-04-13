package tn.esprit.event;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.services.EventService;

import javafx.concurrent.Task;
import javafx.application.Platform;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventManagementController {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane eventsGrid;

    private EventService eventService = new EventService();
    private List<Event> allEvents;

    @FXML
    public void initialize() {
        sortChoice.setItems(FXCollections.observableArrayList("Next Events", "Capacity High", "Name A-Z"));
        sortChoice.setValue("Next Events");

        refreshData();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
    }

    private void refreshData() {
        allEvents = eventService.getAll();
        filterAndDisplay();
    }

    private void filterAndDisplay() {
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

    private void addMockData() {
        allEvents = new ArrayList<>();
        allEvents.add(new Event(1, "Eco-Green Summit 2026", "eco-green-summit", 
            "A massive summit for ecology and green tech innovations.", 500, "Tunis, TN", 
            LocalDateTime.now().plusMonths(2), LocalDateTime.now().plusMonths(2).plusDays(2), 
            "https://images.unsplash.com/photo-1540575861501-7ad060e39fe5?w=800"));
        
        allEvents.add(new Event(2, "Forest Cleanup Day", "forest-cleanup", 
            "Join volunteers to clean the local forest areas.", 100, "Nabeul, TN", 
            LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(15).plusHours(6), 
            "https://images.unsplash.com/photo-1528190336454-13cd56b45b5a?w=800"));

        allEvents.add(new Event(3, "Organic Farming Workshop", "organic-farming", 
            "Learn how to grow your own organic food at home.", 30, "Bizerte, TN", 
            LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(4), 
            "https://images.unsplash.com/photo-1500651230702-0e2d8a49d4ad?w=800"));
    }

    @FXML
    private void goToHome() {
        switchScene("/home/Home.fxml");
    }

    @FXML
    private void goToBlog() {
        switchScene("/blog/BlogManagement.fxml");
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
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
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
