package tn.esprit.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.SponsorService;

import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;
import tn.esprit.util.ImageUploadUtils;

public class SponsorFormController {

    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField sectorField;
    @FXML private TextField locationField;
    @FXML private Label imageNameLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveBtn;

    private SponsorService sponsorService = new SponsorService();
    private Sponsor currentSponsor;
    private boolean isEdit = false;
    private File selectedImageFile;

    public void setSponsor(Sponsor sponsor) {
        this.currentSponsor = sponsor;
        this.isEdit = true;
        formTitle.setText("Edit Sponsor");
        saveBtn.setText("Update Sponsor");

        nameField.setText(sponsor.getName());
        sectorField.setText(sponsor.getSector());
        locationField.setText(sponsor.getLocation());
        if (sponsor.getImage() != null) {
            imageNameLabel.setText(sponsor.getImage());
        }
        descriptionArea.setText(sponsor.getDescription());
    }

    @FXML
    private void save() {
        if (validate()) {
            if (!isEdit) {
                currentSponsor = new Sponsor();
            }

            currentSponsor.setName(nameField.getText().trim());
            currentSponsor.setSector(sectorField.getText().trim());
            currentSponsor.setLocation(locationField.getText().trim());
            
            if (selectedImageFile != null) {
                try {
                    String fileName = ImageUploadUtils.saveImage(selectedImageFile, "sponsors");
                    currentSponsor.setImage(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Upload Error", "Failed to save the logo.");
                    return;
                }
            }
            
            currentSponsor.setDescription(descriptionArea.getText().trim());

            if (isEdit) {
                sponsorService.update(currentSponsor);
            } else {
                sponsorService.add(currentSponsor);
            }

            goBack();
        }
    }

    @FXML
    private void importImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sponsor Logo");
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
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String sector = sectorField.getText() == null ? "" : sectorField.getText().trim();
        String location = locationField.getText() == null ? "" : locationField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();

        if (name.isEmpty() || sector.isEmpty() || location.isEmpty() || description.isEmpty()) {
            showAlert("Missing Information", "All fields marked with (*) are required.");
            return false;
        }
        if (name.length() < 3 || name.length() > 80) {
            showAlert("Invalid Name", "Sponsor name must be between 3 and 80 characters.");
            return false;
        }
        if (!name.matches("^[A-Za-z].*")) {
            showAlert("Invalid Name", "Sponsor name must start with a letter (not a number or symbol).");
            return false;
        }
        if (sector.length() < 3 || sector.length() > 60) {
            showAlert("Invalid Sector", "Sector must be between 3 and 60 characters.");
            return false;
        }
        if (!sector.matches("^[A-Za-z].*")) {
            showAlert("Invalid Sector", "Sector must start with a letter (not a number or symbol).");
            return false;
        }
        if (location.length() < 3 || location.length() > 100) {
            showAlert("Invalid Location", "Location must be between 3 and 100 characters.");
            return false;
        }
        if (!location.matches("^[A-Za-z].*")) {
            showAlert("Invalid Location", "Location must start with a letter (not a number or symbol).");
            return false;
        }
        if (description.length() < 20 || description.length() > 1000) {
            showAlert("Invalid Description", "Description must be between 20 and 1000 characters.");
            return false;
        }
        if (!description.matches("^[A-Za-z].*")) {
            showAlert("Invalid Description", "Description must start with a letter (not a number or symbol).");
            return false;
        }
        
        if (!isEdit && selectedImageFile == null) {
            showAlert("Missing Logo", "Please select a logo for the sponsor.");
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

    @FXML private void cancel() { goBack(); }

    private void goBack() { switchScene("/event/SponsorManagement.fxml"); }

    @FXML private void goToHome() { switchScene("/home/Home.fxml"); }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(saveBtn); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(saveBtn); }
    @FXML private void handleClose() { tn.esprit.util.WindowUtils.close(saveBtn); }
}
