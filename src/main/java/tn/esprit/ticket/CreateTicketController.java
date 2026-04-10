package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;

import java.io.File;
import java.io.IOException;

public class CreateTicketController {

    @FXML private Label userNameLabel;
    @FXML private TextField titleInput;
    @FXML private TextArea descriptionInput;
    @FXML private TextField locationInput;
    @FXML private Label fileNameLabel;
    @FXML private Label errorLabel;

    private File selectedFile;
    private final TicketService ticketService = new TicketService();

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            User u = SessionManager.getCurrentUser();
            userNameLabel.setText(u.getUsername());
        } else {
            userNameLabel.setText("Guest");
        }
        
        setupInputControls();
    }

    private void setupInputControls() {
        // Validation visually triggers when users are typing
        titleInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().length() < 5) {
                titleInput.setStyle("-fx-border-color: #ef4444; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            } else {
                titleInput.setStyle("-fx-border-color: #10b981; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            }
        });

        descriptionInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().length() < 20) {
                descriptionInput.setStyle("-fx-border-color: #ef4444; -fx-padding: 5; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            } else {
                descriptionInput.setStyle("-fx-border-color: #10b981; -fx-padding: 5; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            }
        });

        locationInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                locationInput.setStyle("-fx-border-color: #ef4444; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            } else {
                locationInput.setStyle("-fx-border-color: #10b981; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 4; -fx-font-size: 14px;");
            }
        });
    }


    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Ticket Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String title = titleInput.getText().trim();
        String desc = descriptionInput.getText().trim();
        String loc = locationInput.getText().trim();

        if (title.length() < 5) {
            showError("Title must be at least 5 characters.");
            return;
        }

        if (desc.length() < 20) {
            showError("Description must be at least 20 characters.");
            return;
        }

        if (loc.isEmpty()) {
            showError("Please provide a valid location.");
            return;
        }

        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription(desc);
        t.setLocation(loc);
        t.setStatus(TicketStatus.PENDING);
        t.setPriority(TicketPriority.MEDIUM);
        t.setDomain(ActionDomain.OTHER); // Defaulting to OTHER
        
        if (SessionManager.isLoggedIn()) {
            t.setUserId(SessionManager.getCurrentUser().getId());
        } else {
            t.setUserId(0);
        }

        if (selectedFile != null) {
            // Ideally copy to uploads folder, but for now just save absolute path or mock relative path
            // For typical dev, we just store standard path:
            t.setImage(selectedFile.toURI().toString());
        }

        ticketService.add(t);
        System.out.println("Ticket created: " + title);

        // Redirect back to My Tickets after creation
        goToMyTickets(event);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    void goToDashboard(ActionEvent event) {
        navigate(event, "/user/Dashboard.fxml");
    }

    private void goToMyTickets(ActionEvent event) {
        navigate(event, "/ticket/MyTickets.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
