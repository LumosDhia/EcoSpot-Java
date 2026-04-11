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
        if (nameField.getText().trim().isEmpty() || sectorField.getText().trim().isEmpty() || 
            locationField.getText().trim().isEmpty() || descriptionArea.getText().trim().isEmpty()) {
            showAlert("Missing Information", "All fields marked with (*) are required.");
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
