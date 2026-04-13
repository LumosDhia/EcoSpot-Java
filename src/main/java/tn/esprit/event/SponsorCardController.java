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
import tn.esprit.services.SponsorService;

import java.io.IOException;

public class SponsorCardController {

    @FXML private ImageView sponsorImage;
    @FXML private Label nameLabel;
    @FXML private Label sectorLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;

    private Sponsor sponsor;
    private SponsorService sponsorService = new SponsorService();
    private Runnable refreshCallback;

    public void setSponsor(Sponsor sponsor, Runnable refreshCallback) {
        this.sponsor = sponsor;
        this.refreshCallback = refreshCallback;

        nameLabel.setText(sponsor.getName());
        sectorLabel.setText(sponsor.getSector());
        locationLabel.setText("📍 " + sponsor.getLocation());
        
        String desc = sponsor.getDescription();
        if (desc != null && desc.length() > 80) {
            descriptionLabel.setText(desc.substring(0, 77) + "...");
        } else {
            descriptionLabel.setText(desc);
        }

        if (sponsor.getImage() != null && !sponsor.getImage().isEmpty()) {
            try {
                String imgUrl = tn.esprit.util.ImageUploadUtils.getImageUrl("sponsors", sponsor.getImage());
                sponsorImage.setImage(new Image(imgUrl, true));
            } catch (Exception e) {
                // Fallback or log
            }
        }
    }

    @FXML
    private void editSponsor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/SponsorForm.fxml"));
            Parent root = loader.load();
            SponsorFormController controller = loader.getController();
            controller.setSponsor(sponsor);
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSponsor() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Sponsor");
        alert.setHeaderText("Delete " + sponsor.getName() + "?");
        alert.setContentText("This will also remove them from all linked events.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            sponsorService.delete(sponsor);
            if (refreshCallback != null) refreshCallback.run();
        }
    }
}
