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
    private Ticket editingTicket;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            User u = SessionManager.getCurrentUser();
            userNameLabel.setText(u.getUsername());
        } else {
            userNameLabel.setText("Guest");
        }
        
        setupInputControls();
        tn.esprit.util.NavigationHistory.track(titleInput, "/ticket/CreateTicket.fxml");
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

        Ticket t = editingTicket != null ? editingTicket : new Ticket();
        t.setTitle(title);
        t.setDescription(desc);
        t.setLocation(loc);
        t.setStatus(TicketStatus.PENDING);
        if (t.getPriority() == null) t.setPriority(TicketPriority.MEDIUM);
        if (t.getDomain() == null) t.setDomain(ActionDomain.OTHER);
        t.setAdminNotes(null); // reset revision notes on resubmission
        
        if (SessionManager.isLoggedIn()) {
            t.setUserId(SessionManager.getCurrentUser().getId());
        } else {
            t.setUserId(0);
        }

        if (selectedFile != null) {
            t.setImage(selectedFile.toURI().toString());
        }
        try {
            if (editingTicket != null) {
                ticketService.update(t);
                System.out.println("Ticket resubmitted for review: " + title);
            } else {
                ticketService.add(t);
                System.out.println("Ticket created: " + title);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
            return;
        }

        // Redirect back to My Tickets after creation
        goToMyTickets(event);
    }

    public void setTicketForEdit(Ticket ticket) {
        if (ticket == null || ticket.getStatus() != TicketStatus.SENT_BACK) {
            return;
        }
        this.editingTicket = ticketService.getById(ticket.getId());
        if (this.editingTicket == null) {
            this.editingTicket = ticket;
        }
        titleInput.setText(this.editingTicket.getTitle());
        descriptionInput.setText(this.editingTicket.getDescription());
        locationInput.setText(this.editingTicket.getLocation());
        if (this.editingTicket.getImage() != null && !this.editingTicket.getImage().isBlank()) {
            fileNameLabel.setText("Current image kept");
        }
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

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
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
