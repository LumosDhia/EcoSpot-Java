package tn.esprit.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.services.EventService;
import tn.esprit.util.ImageUploadUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EventAdminCardController {

    @FXML private ImageView eventImage;
    @FXML private Label nameLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;

    private Event event;
    private EventService eventService = new EventService();
    private Runnable refreshCallback;

    public void setData(Event event, Runnable refreshCallback) {
        this.event = event;
        this.refreshCallback = refreshCallback;

        nameLabel.setText(event.getName());
        locationLabel.setText(event.getLocation());
        if (event.getStartedAt() != null) {
            dateLabel.setText(event.getStartedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                String imgUrl = ImageUploadUtils.getImageUrl("events", event.getImage());
                eventImage.setImage(new Image(imgUrl, true));
            } catch (Exception e) {}
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventForm.fxml"));
            Parent root = loader.load();
            EventFormController controller = loader.getController();
            controller.setEvent(event);
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText("Are you sure you want to delete this event?");
        alert.setContentText(event.getName());

        if (alert.showAndWait().get() == ButtonType.OK) {
            eventService.delete(event);
            if (refreshCallback != null) refreshCallback.run();
        }
    }
}
