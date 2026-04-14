package tn.esprit.event;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import tn.esprit.services.EventService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EventCardController {

    @FXML private ImageView eventImage;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label nameLabel;
    @FXML private Label capacityLabel;
    @FXML private Text descriptionText;
    @FXML private Button detailsBtn;

    private Event event;
    private EventService eventService = new EventService();
    private Runnable refreshCallback;

    public void setData(Event event, Runnable refreshCallback) {
        this.event = event;
        this.refreshCallback = refreshCallback;

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                String imgUrl = tn.esprit.util.ImageUploadUtils.getImageUrl("events", event.getImage());
                eventImage.setImage(new Image(imgUrl, true));
            } catch (Exception e) {
                // Keep default
            }
        }
        
        locationLabel.setText(event.getLocation() != null ? event.getLocation() : "Unknown");
        
        if (event.getStartedAt() != null) {
            dateLabel.setText(event.getStartedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }
        
        nameLabel.setText(event.getName());
        if (event.getCapacity() <= 0) {
            capacityLabel.setText("✅ Completed");
        } else {
            capacityLabel.setText("👥 " + event.getCapacity() + " spots left");
        }
        
        String desc = event.getDescription();
        if (desc != null && desc.length() > 100) {
            descriptionText.setText(desc.substring(0, 97) + "...");
        } else {
            descriptionText.setText(desc);
        }


        detailsBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventDetail.fxml"));
                Parent root = loader.load();
                
                EventDetailController controller = loader.getController();
                controller.setEvent(event);
                
                Stage stage = (Stage) detailsBtn.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
