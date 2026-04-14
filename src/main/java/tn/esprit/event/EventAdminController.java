package tn.esprit.event;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.services.EventService;

import java.io.IOException;
import java.util.List;

public class EventAdminController {

    @FXML private FlowPane eventContainer;

    private EventService eventService = new EventService();

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(eventContainer, "/event/EventAdmin.fxml");
        refreshData();
    }

    private void refreshData() {
        eventContainer.getChildren().clear();
        List<Event> events = eventService.getAll();
        
        for (Event e : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventAdminCard.fxml"));
                Parent card = loader.load();
                
                EventAdminCardController controller = loader.getController();
                controller.setData(e, this::refreshData);
                
                eventContainer.getChildren().add(card);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddEvent() {
        switchScene("/event/EventForm.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            switchScene("/event/EventManagement.fxml");
        }
    }

    @FXML
    private void goToHome() { switchScene("/home/Home.fxml"); }

    @FXML
    private void goToEvents() { switchScene("/event/EventManagement.fxml"); }

    @FXML
    private void goToBlog() { switchScene("/blog/BlogManagement.fxml"); }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) eventContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(eventContainer); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(eventContainer); }
    @FXML private void handleClose() { tn.esprit.util.WindowUtils.close(eventContainer); }
}
