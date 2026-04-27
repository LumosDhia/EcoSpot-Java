package tn.esprit.event;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.services.SponsorService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SponsorManagementController {

    @FXML private FlowPane sponsorContainer;
    @FXML private TextField searchField;

    private SponsorService sponsorService = new SponsorService();
    private List<Sponsor> allSponsors;

    @FXML
    public void initialize() {
        refreshData();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        tn.esprit.util.NavigationHistory.track(searchField, "/event/SponsorManagement.fxml");
    }

    private void refreshData() {
        allSponsors = sponsorService.getAll();
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String query = searchField.getText().toLowerCase();
        List<Sponsor> filtered = allSponsors.stream()
                .filter(s -> s.getName().toLowerCase().contains(query) || 
                            s.getSector().toLowerCase().contains(query))
                .collect(Collectors.toList());

        displaySponsors(filtered);
    }

    private void displaySponsors(List<Sponsor> sponsors) {
        sponsorContainer.getChildren().clear();
        for (Sponsor s : sponsors) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/SponsorCard.fxml"));
                Parent card = loader.load();
                SponsorCardController controller = loader.getController();
                controller.setSponsor(s, this::refreshData);
                sponsorContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddSponsor() {
        switchScene("/event/SponsorForm.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            switchScene("/event/EventManagement.fxml");
        }
    }

    @FXML private void goToHome() { switchScene("/home/Home.fxml"); }
    @FXML private void goToEvents() { switchScene("/event/EventManagement.fxml"); }
    @FXML private void goToBlog() { try {
        Parent root = FXMLLoader.load(getClass().getResource("/blog/BlogManagement.fxml"));
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.getScene().setRoot(root);
        tn.esprit.util.WindowUtils.makeDraggable(stage, root);
    } catch (IOException e) { e.printStackTrace(); } }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(searchField); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(searchField); }
    @FXML private void handleClose() { tn.esprit.util.WindowUtils.close(searchField); }
}
