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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

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
    @FXML private VBox nearbyEventsContainer;
    @FXML private WebView mapView;
    
    // AI Elements
    @FXML private Label aiSuccessLabel;
    @FXML private Label aiAnalysisLabel;

    private Event event;
    private tn.esprit.services.SponsorService sponsorService = new tn.esprit.services.SponsorService();
    private tn.esprit.services.EventService eventService = new tn.esprit.services.EventService();
    private tn.esprit.services.GeocodingService geocodingService = new tn.esprit.services.GeocodingService();
    private tn.esprit.services.OpenRouterEventService aiService = new tn.esprit.services.OpenRouterEventService();

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

        eventImg.setVisible(false);
        eventImg.setManaged(false);
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            String localUrl = tn.esprit.util.ImageUploadUtils.getImageUrl("events", event.getImage());
            Image img = new Image(localUrl, true);
            img.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                    javafx.application.Platform.runLater(() -> {
                        eventImg.setImage(img);
                        eventImg.setVisible(true);
                        eventImg.setManaged(true);
                    });
                }
            });
            img.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    System.err.println("[Event Image] Failed to load: " + localUrl);
                }
            });
        }

        loadSponsors();
        loadParticipants();
        loadMap();
        loadNearbyEvents();
        loadAiInsights();
    }

    private void loadAiInsights() {
        if (aiSuccessLabel == null || event == null) return;
        
        aiSuccessLabel.setText("ANALYZING...");
        aiAnalysisLabel.setText("Our AI is currently analyzing this event's impact and success potential...");

        new Thread(() -> {
            try {
                tn.esprit.services.OpenRouterEventService.PredictionResult result = aiService.predictAttendance(event);
                javafx.application.Platform.runLater(() -> {
                    aiSuccessLabel.setText(result.successLevel);
                    aiAnalysisLabel.setText(result.analysis);
                    
                    // Style based on level
                    if ("HIGH".equals(result.successLevel)) {
                        aiSuccessLabel.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else if ("LOW".equals(result.successLevel)) {
                        aiSuccessLabel.setStyle("-fx-background-color: #e63946; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else {
                        aiSuccessLabel.setStyle("-fx-background-color: #fca311; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    aiSuccessLabel.setText("ERROR");
                    aiAnalysisLabel.setText("AI Service is temporarily unavailable. Please check your internet or API key.");
                });
            }
        }).start();
    }

    private void loadNearbyEvents() {
        if (nearbyEventsContainer == null || event == null) return;
        nearbyEventsContainer.getChildren().clear();
        
        java.util.List<Event> allEvents = eventService.getAll();
        java.util.List<Event> nearby = new java.util.ArrayList<>();
        
        for (Event e : allEvents) {
            if (e.getId() != event.getId() && e.getLatitude() != 0) {
                double dist = geocodingService.calculateDistance(event.getLatitude(), event.getLongitude(), e.getLatitude(), e.getLongitude());
                if (dist < 100) { // 100km radius
                    nearby.add(e);
                }
            }
        }
        
        nearby.sort((a, b) -> {
            double distA = geocodingService.calculateDistance(event.getLatitude(), event.getLongitude(), a.getLatitude(), a.getLongitude());
            double distB = geocodingService.calculateDistance(event.getLatitude(), event.getLongitude(), b.getLatitude(), b.getLongitude());
            return Double.compare(distA, distB);
        });

        if (nearby.isEmpty()) {
            Label none = new Label("No other events nearby.");
            none.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            nearbyEventsContainer.getChildren().add(none);
            return;
        }

        for (int i = 0; i < Math.min(3, nearby.size()); i++) {
            Event e = nearby.get(i);
            double dist = geocodingService.calculateDistance(event.getLatitude(), event.getLongitude(), e.getLatitude(), e.getLongitude());
            
            HBox box = new HBox(10);
            box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            box.setStyle("-fx-padding: 8; -fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-cursor: hand;");
            box.setOnMouseClicked(event -> {
                setEvent(e); // Reload with new event
            });
            
            Label title = new Label(e.getName());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d6a4f;");
            
            Label distLabel = new Label(String.format("%.1f km", dist));
            distLabel.setStyle("-fx-text-fill: #fca311; -fx-font-size: 11; -fx-font-weight: bold;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            box.getChildren().addAll(title, spacer, distLabel);
            nearbyEventsContainer.getChildren().add(box);
        }
    }

    private void loadMap() {
        if (mapView == null || event == null) return;

        double lat = event.getLatitude();
        double lon = event.getLongitude();

        if (lat == 0 && lon == 0) {
            lat = 36.8065;
            lon = 10.1815;
        }

        // Explicit pixel height is CRITICAL for Leaflet in JavaFX WebView.
        // Percentage heights don't resolve when using loadContent(), causing blank tiles.
        final double mapHeight = 290;
        final String safeName = event.getName().replace("'", "\\'").replace("\"", "\\\"");
        final String safeLoc  = event.getLocation().replace("'", "\\'").replace("\"", "\\\"");
        final double fLat = lat;
        final double fLon = lon;

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  * { margin:0; padding:0; box-sizing:border-box; }" +
                "  body { background:#f3f4f6; overflow:hidden; }" +
                "  #map { width:100%; height:" + (int) mapHeight + "px; }" +
                "</style></head><body>" +
                "<div id='map'></div><script>" +
                "  var map = L.map('map', {" +
                "    zoomControl: true," +
                "    zoomAnimation: false," +
                "    fadeAnimation: false," +
                "    markerZoomAnimation: false" +
                "  }).setView([" + fLat + "," + fLon + "], 13);" +
                "  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {" +
                "    maxZoom: 19," +
                "    attribution: '&copy; OpenStreetMap contributors &copy; CARTO'" +
                "  }).addTo(map);" +
                "  L.marker([" + fLat + "," + fLon + "]).addTo(map)" +
                "    .bindPopup('<b>" + safeName + "</b><br>" + safeLoc + "').openPopup();" +
                "  new ResizeObserver(() => map.invalidateSize()).observe(document.getElementById('map'));" +
                "</script></body></html>";

        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        engine.loadContent(html);
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
