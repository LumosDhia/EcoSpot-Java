package tn.esprit.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import tn.esprit.event.Sponsor;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EventDetailController {

    @FXML private Label nameLabel;
    @FXML private ImageView eventImg;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label capacityLabel;
    @FXML private javafx.scene.control.Button joinBtn;
    @FXML private Text descriptionText;
    @FXML private javafx.scene.layout.FlowPane sponsorContainer;
    @FXML private VBox participantContainer;

    private Event event;
    private tn.esprit.services.SponsorService sponsorService = new tn.esprit.services.SponsorService();
    private tn.esprit.services.EventService eventService = new tn.esprit.services.EventService();

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(nameLabel, "/event/EventDetail.fxml");
    }

    public void setEvent(Event event) {
        this.event = event;
        nameLabel.setText(event.getName());
        locationLabel.setText(event.getLocation());
        updateCapacityState();
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
        loadParticipants();
    }

    private void updateCapacityState() {
        if (event == null) {
            return;
        }
        int remaining = Math.max(0, event.getCapacity());
        boolean loggedIn = SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null;
        boolean alreadyJoined = false;
        if (loggedIn) {
            alreadyJoined = eventService.isUserParticipant(event.getId(), SessionManager.getCurrentUser().getId());
        }

        if (remaining <= 0) {
            capacityLabel.setText("Completed");
        } else {
            capacityLabel.setText(remaining + " spots left");
        }

        if (joinBtn != null) {
            if (alreadyJoined) {
                joinBtn.setText("Unjoin Event");
                joinBtn.setDisable(false);
            } else if (remaining <= 0) {
                joinBtn.setText("Completed");
                joinBtn.setDisable(true);
            } else {
                joinBtn.setText("Join Event");
                joinBtn.setDisable(false);
            }
        }
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

    private void loadParticipants() {
        if (participantContainer == null || event == null) {
            return;
        }
        participantContainer.getChildren().clear();
        java.util.List<User> participants = eventService.getParticipantsForEvent(event.getId());
        if (participants.isEmpty()) {
            Label none = new Label("No participants yet.");
            none.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            participantContainer.getChildren().add(none);
            return;
        }

        for (User user : participants) {
            Label participant = new Label("• " + user.getUsername());
            participant.setStyle("-fx-text-fill: #374151; -fx-font-size: 13;");
            participantContainer.getChildren().add(participant);
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            switchScene("/event/EventManagement.fxml");
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
        if (event == null) {
            return;
        }
        if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Join event");
            warn.setHeaderText(null);
            warn.setContentText("Please login first to join this event.");
            warn.showAndWait();
            return;
        }

        User current = SessionManager.getCurrentUser();
        if (eventService.isUserParticipant(event.getId(), current.getId())) {
            boolean unjoined = eventService.unjoinEvent(event.getId(), current.getId());
            if (unjoined) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Unjoin event");
                ok.setHeaderText(event.getName());
                ok.setContentText("You left this event successfully.");
                ok.showAndWait();
                event.setCapacity(event.getCapacity() + 1);
                updateCapacityState();
                loadParticipants();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Unjoin event");
                err.setHeaderText(event.getName());
                err.setContentText("Unable to leave this event right now.");
                err.showAndWait();
            }
            return;
        }

        if (event.getCapacity() <= 0) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Join event");
            info.setHeaderText(event.getName());
            info.setContentText("This event is completed.");
            info.showAndWait();
            updateCapacityState();
            return;
        }

        boolean joined = eventService.joinEvent(event.getId(), current.getId());
        if (joined) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Join event");
            ok.setHeaderText(event.getName());
            ok.setContentText("You joined successfully.");
            ok.showAndWait();
            event.setCapacity(Math.max(0, event.getCapacity() - 1));
            updateCapacityState();
            loadParticipants();
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Join event");
            err.setHeaderText(event.getName());
            err.setContentText("Unable to join this event right now (already joined or no spots left).");
            err.showAndWait();
            updateCapacityState();
        }
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
