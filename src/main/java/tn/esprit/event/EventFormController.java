package tn.esprit.event;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.services.EventService;
import tn.esprit.event.Sponsor;
import tn.esprit.services.OpenRouterEventService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.stage.FileChooser;
import tn.esprit.util.ImageUploadUtils;
import tn.esprit.services.GeocodingService;

public class EventFormController {

    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField capacityField;
    @FXML private Label imageNameLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveBtn;
    @FXML private javafx.scene.layout.FlowPane selectedSponsorsFlow;
    
    // AI Elements
    @FXML private VBox aiResultBox;
    @FXML private Label aiSuccessLabel;
    @FXML private Label aiAnalysisLabel;

    // Location Map Search
    @FXML private Button searchLocationBtn;
    @FXML private Label locationErrorLabel;
    @FXML private ListView<GeocodingService.Place> locationResultsList;

    private tn.esprit.services.EventService eventService = new tn.esprit.services.EventService();
    private tn.esprit.services.SponsorService sponsorService = new tn.esprit.services.SponsorService();
    private OpenRouterEventService aiService = new OpenRouterEventService();
    private GeocodingService geocodingService = new GeocodingService();
    private Event currentEvent;
    private boolean isEdit = false;
    private java.util.List<Sponsor> selectedSponsors = new java.util.ArrayList<>();
    private File selectedImageFile;
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(saveBtn, "/event/EventForm.fxml");
        
        if (locationResultsList != null) {
            locationResultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    locationField.setText(newVal.getDisplayName());
                    selectedLat = newVal.getLat();
                    selectedLon = newVal.getLon();
                    locationResultsList.setVisible(false);
                    locationResultsList.setManaged(false);
                }
            });
        }
    }

    private void updateSponsorsFlow() {
        selectedSponsorsFlow.getChildren().clear();
        for (Sponsor s : selectedSponsors) {
            Label label = new Label(s.getName());
            label.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");
            selectedSponsorsFlow.getChildren().add(label);
        }
    }

    @FXML
    private void openSponsorPicker() {
        java.util.List<Sponsor> all = sponsorService.getAll();
        Dialog<java.util.List<Sponsor>> dialog = new Dialog<>();
        dialog.setTitle("Select Sponsors");
        dialog.setHeaderText("Choose sponsors for this event");

        ButtonType saveButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();
        for (Sponsor s : all) {
            CheckBox cb = new CheckBox(s.getName());
            if (selectedSponsors.contains(s)) cb.setSelected(true);
            cb.setUserData(s);
            checkBoxes.add(cb);
            vbox.getChildren().add(cb);
        }
        
        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setPrefHeight(300);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return checkBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (Sponsor) cb.getUserData())
                        .collect(java.util.stream.Collectors.toList());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selected -> {
            this.selectedSponsors = selected;
            updateSponsorsFlow();
        });
    }

    @FXML
    private void quickAddSponsor() {
        switchScene("/event/SponsorForm.fxml");
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        this.isEdit = true;
        formTitle.setText("Edit Event");
        saveBtn.setText("Update Event");

        nameField.setText(event.getName());
        locationField.setText(event.getLocation());
        this.selectedLat = event.getLatitude();
        this.selectedLon = event.getLongitude();
        capacityField.setText(String.valueOf(event.getCapacity()));
        if (event.getImage() != null) {
            imageNameLabel.setText(event.getImage());
        }
        if (event.getStartedAt() != null) startDatePicker.setValue(event.getStartedAt().toLocalDate());
        if (event.getEndedAt() != null) endDatePicker.setValue(event.getEndedAt().toLocalDate());
        descriptionArea.setText(event.getDescription());
        
        // Load sponsors
        this.selectedSponsors = sponsorService.getSponsorsForEvent(event.getId());
        updateSponsorsFlow();
    }

    @FXML
    void handleLocationSearch(ActionEvent event) {
        String query = locationField.getText().trim();
        if (query.length() < 3) {
            locationErrorLabel.setText("Please enter at least 3 characters to search.");
            locationErrorLabel.setVisible(true);
            locationErrorLabel.setManaged(true);
            return;
        }

        searchLocationBtn.setDisable(true);
        searchLocationBtn.setText("...");

        new Thread(() -> {
            java.util.List<GeocodingService.Place> results = geocodingService.search(query);
            javafx.application.Platform.runLater(() -> {
                searchLocationBtn.setDisable(false);
                searchLocationBtn.setText("Search");
                if (results.isEmpty()) {
                    locationErrorLabel.setText("No locations found for: " + query);
                    locationErrorLabel.setVisible(true);
                    locationErrorLabel.setManaged(true);
                    locationResultsList.setVisible(false);
                    locationResultsList.setManaged(false);
                } else {
                    locationResultsList.getItems().setAll(results);
                    locationResultsList.setVisible(true);
                    locationResultsList.setManaged(true);
                    locationErrorLabel.setVisible(false);
                    locationErrorLabel.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleAiPredict() {
        if (nameField.getText().isEmpty() || descriptionArea.getText().isEmpty()) {
            showAlert("Missing Data", "Please enter at least a Name and Description for AI analysis.");
            return;
        }

        // Create a temp event for AI to analyze
        Event temp = new Event();
        temp.setName(nameField.getText());
        temp.setDescription(descriptionArea.getText());
        temp.setLocation(locationField.getText());
        try {
            temp.setCapacity(Integer.parseInt(capacityField.getText()));
        } catch (Exception e) {
            temp.setCapacity(100);
        }
        temp.setStartedAt(startDatePicker.getValue() != null ? 
            LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(9, 0)) : LocalDateTime.now());

        saveBtn.setDisable(true); // Disable save during AI call
        aiAnalysisLabel.setText("Analyzing event potential... please wait...");
        aiResultBox.setVisible(true);
        aiResultBox.setManaged(true);

        new Thread(() -> {
            OpenRouterEventService.PredictionResult result = aiService.predictAttendance(temp);
            javafx.application.Platform.runLater(() -> {
                aiSuccessLabel.setText(result.successLevel);
                aiAnalysisLabel.setText(result.analysis);
                
                // Color code based on level
                if ("HIGH".equals(result.successLevel)) aiSuccessLabel.setStyle("-fx-text-fill: #2d6a4f; -fx-font-weight: bold;");
                else if ("LOW".equals(result.successLevel)) aiSuccessLabel.setStyle("-fx-text-fill: #e63946; -fx-font-weight: bold;");
                else aiSuccessLabel.setStyle("-fx-text-fill: #fca311; -fx-font-weight: bold;");
                
                saveBtn.setDisable(false);
            });
        }).start();
    }

    @FXML
    private void save() {
        if (validate()) {
            if (!isEdit) {
                currentEvent = new Event();
                currentEvent.setSlug(nameField.getText().toLowerCase().replace(" ", "-"));
            }

            currentEvent.setName(nameField.getText());
            currentEvent.setLocation(locationField.getText());
            currentEvent.setLatitude(selectedLat);
            currentEvent.setLongitude(selectedLon);
            currentEvent.setCapacity(Integer.parseInt(capacityField.getText()));
            
            if (selectedImageFile != null) {
                try {
                    String fileName = ImageUploadUtils.saveImage(selectedImageFile, "events");
                    currentEvent.setImage(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Upload Error", "Failed to save the image.");
                    return;
                }
            }
            
            currentEvent.setStartedAt(LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(9, 0)));
            currentEvent.setEndedAt(LocalDateTime.of(endDatePicker.getValue(), LocalTime.of(17, 0)));
            currentEvent.setDescription(descriptionArea.getText());

            if (isEdit) {
                eventService.update(currentEvent);
            } else {
                eventService.add(currentEvent);
            }

            // Save Many-to-Many Sponsor links
            // 1. Get current assigned sponsors to compare
            java.util.List<Sponsor> currentlyAssigned = sponsorService.getSponsorsForEvent(currentEvent.getId());
            
            // 2. Remove those that are no longer selected
            for (Sponsor s : currentlyAssigned) {
                if (selectedSponsors.stream().noneMatch(sel -> sel.getId() == s.getId())) {
                    sponsorService.unassignSponsorFromEvent(currentEvent.getId(), s.getId());
                }
            }
            
            // 3. Add new ones
            for (Sponsor sel : selectedSponsors) {
                if (currentlyAssigned.stream().noneMatch(cur -> cur.getId() == sel.getId())) {
                    sponsorService.assignSponsorToEvent(currentEvent.getId(), sel.getId());
                }
            }

            goBack(null);
        }
    }

    @FXML
    private void importImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
        }
    }

    private boolean validate() {
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String capacityStr = capacityField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Check if mandatory fields are empty
        if (name.isEmpty() || location.isEmpty() || capacityStr.isEmpty() 
            || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Missing Information", "All fields marked with (*) are required.");
            return false;
        }

        if (!isEdit && selectedImageFile == null) {
            showAlert("Missing Photo", "Please select an image for the event.");
            return false;
        }

        if (selectedLat == 0 && selectedLon == 0) {
            showAlert("Invalid Location", "Please search and select a valid location from the list.");
            return false;
        }

        // Name validation
        if (name.length() < 5) {
            showAlert("Invalid Name", "Event name must be at least 5 characters long.");
            return false;
        }

        // Capacity validation
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                showAlert("Invalid Capacity", "Capacity must be a positive number greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Capacity", "Capacity must be a valid number.");
            return false;
        }


        // Date validation
        if (startDatePicker.getValue().isBefore(java.time.LocalDate.now()) && !isEdit) {
            showAlert("Invalid Date", "Start date cannot be in the past.");
            return false;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showAlert("Invalid Date", "End date cannot be before start date.");
            return false;
        }

        // Description validation
        if (description.length() < 20) {
            showAlert("Description too short", "Description must be at least 20 characters long.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    @FXML
    private void cancel() {
        goBack(null);
    }

    @FXML
    private void goBack(ActionEvent event) {
        if (event != null && tn.esprit.util.NavigationHistory.goBack(event)) {
            return;
        }
        switchScene("/event/EventManagement.fxml");
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

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(saveBtn);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(saveBtn);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(saveBtn);
    }
}
