package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

import java.io.File;
import java.io.IOException;

public class CompleteTicketController {

    @FXML private Label ticketTitleLabel;
    @FXML private TextArea completionDescriptionInput;
    @FXML private Label fileNameLabel;
    @FXML private Label errorLabel;
    @FXML private ImageView proofPreview;

    private final TicketService ticketService = new TicketService();
    private Ticket currentTicket;
    private File selectedFile;

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(completionDescriptionInput, "/ticket/CompleteTicket.fxml");
    }

    public void setTicket(Ticket ticket) {
        this.currentTicket = ticket;
        if (ticket != null) {
            ticketTitleLabel.setText(ticket.getTitle());
        }
    }

    @FXML
    private void chooseProofImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose proof image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
            proofPreview.setImage(new javafx.scene.image.Image(file.toURI().toString(), true));
        }
    }

    @FXML
    private void submitCompletion(ActionEvent event) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        if (currentTicket == null) {
            showError("No ticket selected.");
            return;
        }

        String message = completionDescriptionInput.getText() == null ? "" : completionDescriptionInput.getText().trim();
        if (message.length() < 10) {
            showError("Please provide a completion description (min 10 chars).");
            return;
        }
        if (selectedFile == null) {
            showError("Please attach a proof image.");
            return;
        }

        currentTicket.setCompletionMessage(message);
        currentTicket.setCompletionImage(selectedFile.toURI().toString());
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            currentTicket.setCompletedById(SessionManager.getCurrentUser().getId());
        }
        currentTicket.setStatus(TicketStatus.IN_PROGRESS); // completion submitted, pending admin validation
        ticketService.update(currentTicket);

        goToTickets(event);
    }

    @FXML
    private void cancel(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToTickets(event);
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void goToTickets(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ticket/TicketManagement.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

