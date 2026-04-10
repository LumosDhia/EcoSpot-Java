package tn.esprit.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import tn.esprit.event.Sponsor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EventDetailController {

    @FXML private Label nameLabel;
    @FXML private ImageView eventImg;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label capacityLabel;
    @FXML private Text descriptionText;
    @FXML private javafx.scene.layout.FlowPane sponsorContainer;

    private Event event;
    private tn.esprit.services.SponsorService sponsorService = new tn.esprit.services.SponsorService();

    public void setEvent(Event event) {
        this.event = event;
        nameLabel.setText(event.getName());
        locationLabel.setText(event.getLocation());
        capacityLabel.setText(event.getCapacity() + " Participants");
        descriptionText.setText(event.getDescription());
        
        if (event.getStartedAt() != null) {
            dateLabel.setText(event.getStartedAt().format(DateTimeFormatter.ofPattern("MMM dd")) + " - " + 
                             (event.getEndedAt() != null ? event.getEndedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : ""));
        }

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                String imgUrl = tn.esprit.util.ImageUploadUtils.getImageUrl("events", event.getImage());
                eventImg.setImage(new Image(imgUrl, true));
            } catch (Exception e) {
                // Ignore
            }
        }

        loadSponsors();
    }

    private void loadSponsors() {
        sponsorContainer.getChildren().clear();
        java.util.List<Sponsor> sponsors = sponsorService.getSponsorsForEvent(event.getId());
        
        if (sponsors.isEmpty()) {
            Label noSponsors = new Label("No official sponsors yet.");
            noSponsors.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            sponsorContainer.getChildren().add(noSponsors);
            return;
        }

        for (Sponsor s : sponsors) {
            VBox box = new VBox(5);
            box.setAlignment(javafx.geometry.Pos.CENTER);
            
            ImageView iv = new ImageView();
            iv.setFitWidth(60);
            iv.setFitHeight(60);
            iv.setPreserveRatio(true);
            
            if (s.getImage() != null && !s.getImage().isEmpty()) {
                try {
                    String imgUrl = tn.esprit.util.ImageUploadUtils.getImageUrl("sponsors", s.getImage());
                    iv.setImage(new Image(imgUrl, true));
                } catch (Exception e) {}
            } else {
                // Placeholder for logo
                iv.setStyle("-fx-background-color: #eee;");
            }

            Label sName = new Label(s.getName());
            sName.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
            
            box.getChildren().addAll(iv, sName);
            sponsorContainer.getChildren().add(box);
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/event/EventManagement.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editEvent() {
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
    private void goToHome() {
        switchScene("/home/Home.fxml");
    }

    @FXML
    private void goToBlog() {
        switchScene("/blog/BlogManagement.fxml");
    }

    @FXML
    private void goToEvents() {
        switchScene("/event/EventManagement.fxml");
    }

    @FXML
    private void goToTickets() {
        switchScene("/ticket/TicketManagement.fxml");
    }

    @FXML
    private void goToAchievements() {
        switchScene("/ticket/Achievements.fxml");
    }

    @FXML
    private void handleJoinEvent() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Join event");
        alert.setHeaderText(event != null ? event.getName() : "Event");
        alert.setContentText("Thanks for your interest! Event participation flow will be available soon.");
        alert.showAndWait();
    }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(nameLabel);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(nameLabel);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(nameLabel);
    }
}
