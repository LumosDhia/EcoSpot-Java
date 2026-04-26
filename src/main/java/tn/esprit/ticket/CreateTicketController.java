package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.services.GeocodingService;
import tn.esprit.services.OpenRouterService;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;

public class CreateTicketController {

    @FXML private Label userNameLabel;
    @FXML private TextField titleInput;
    @FXML private TextArea descriptionInput;
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox consignesContainer;
    @FXML private Button aiSuggestBtn;
    @FXML private TextField locationInput;
    @FXML private Button searchLocationBtn;
    @FXML private ListView<GeocodingService.Place> locationResultsList;
    @FXML private Label fileNameLabel;
    @FXML private Label errorLabel;
    @FXML private Label timeoutLabel;
    @FXML private Button submitBtn;

    private File selectedFile;
    private final TicketService ticketService = new TicketService();
    private final GeocodingService geocodingService = new GeocodingService();
    private final OpenRouterService openRouterService = new OpenRouterService();
    private Ticket editingTicket;
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            User u = SessionManager.getCurrentUser();
            userNameLabel.setText(u.getUsername());
            
            if (u.isTimedOut()) {
                showTimeoutWarning(u);
            }
        } else {
            userNameLabel.setText("Guest");
        }
        
        setupInputControls();
        tn.esprit.util.NavigationHistory.track(titleInput, "/ticket/CreateTicket.fxml");

        if (editingTicket == null) {
            handleAddConsigne(null); // Add one empty row by default
        }

        setupLocationSearch();
    }

    private void showTimeoutWarning(User u) {
        if (timeoutLabel != null) {
            timeoutLabel.setText("⚠️ Your account is temporarily in timeout until " + 
                u.getTimeoutUntil().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                ". You cannot submit new tickets.");
            timeoutLabel.setVisible(true);
            timeoutLabel.setManaged(true);
        }
        if (submitBtn != null) {
            submitBtn.setDisable(true);
            submitBtn.setOpacity(0.5);
        }
    }

    private void setupLocationSearch() {
        locationResultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                locationInput.setText(newVal.getDisplayName());
                selectedLat = newVal.getLat();
                selectedLon = newVal.getLon();
                locationResultsList.setVisible(false);
                locationResultsList.setManaged(false);
            }
        });
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
    void handleAddConsigne(ActionEvent event) {
        addConsigneRow("");
    }

    private void addConsigneRow(String initialText) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField field = new TextField(initialText);
        field.setPromptText("Enter instruction step...");
        field.setPrefWidth(600);
        field.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 4;");
        HBox.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        deleteBtn.setOnAction(e -> consignesContainer.getChildren().remove(row));

        row.getChildren().addAll(field, deleteBtn);
        consignesContainer.getChildren().add(row);
    }

    @FXML
    void handleLocationSearch(ActionEvent event) {
        String query = locationInput.getText().trim();
        if (query.length() < 3) {
            errorLabel.setText("Please enter at least 3 characters to search.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        searchLocationBtn.setDisable(true);
        searchLocationBtn.setText("...");

        // Run search in a separate thread to keep UI responsive
        new Thread(() -> {
            java.util.List<GeocodingService.Place> results = geocodingService.search(query);
            javafx.application.Platform.runLater(() -> {
                searchLocationBtn.setDisable(false);
                searchLocationBtn.setText("Search");
                if (results.isEmpty()) {
                    errorLabel.setText("No locations found for: " + query);
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    locationResultsList.setVisible(false);
                    locationResultsList.setManaged(false);
                } else {
                    locationResultsList.getItems().setAll(results);
                    locationResultsList.setVisible(true);
                    locationResultsList.setManaged(true);
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    void handleAiSuggest(ActionEvent event) {
        String title = titleInput.getText().trim();
        String desc = descriptionInput.getText().trim();

        if (desc.length() < 10) {
            errorLabel.setText("Please provide a longer description (min 10 chars) for AI analysis.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        aiSuggestBtn.setDisable(true);
        aiSuggestBtn.setText("✨ Thinking...");

        // Save current scroll position
        double currentScroll = mainScrollPane.getVvalue();

        new Thread(() -> {
            OpenRouterService.AiResponse response = openRouterService.generateTasks(title, desc);
            javafx.application.Platform.runLater(() -> {
                aiSuggestBtn.setDisable(false);
                aiSuggestBtn.setText("✨ AI Suggest Tasks");
                
                if (response.tasks.isEmpty()) {
                    errorLabel.setText("AI could not generate tasks. Check your .env key or description.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                } else {
                    consignesContainer.getChildren().clear();
                    for (String task : response.tasks) {
                        addConsigneRow(task);
                    }
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                    
                    // Restore scroll position after layout updates
                    javafx.application.Platform.runLater(() -> mainScrollPane.setVvalue(currentScroll));
                }
            });
        }).start();
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

        Button submitBtn = (Button) event.getSource();
        String originalText = submitBtn.getText();
        submitBtn.setDisable(true);
        submitBtn.setText("Validating...");

        // Perform Spam Detection first using the unified OpenRouterService
        new Thread(() -> {
            OpenRouterService.DetectionResult result = openRouterService.checkSpam(title, desc);
            javafx.application.Platform.runLater(() -> {
                Ticket t = editingTicket != null ? editingTicket : new Ticket();
                if (editingTicket != null && editingTicket.getStatus() == TicketStatus.PUBLISHED) {
                    showError("Accepted tickets can no longer be edited.");
                    submitBtn.setDisable(false);
                    submitBtn.setText(originalText);
                    return;
                }
                
                t.setTitle(title);
                t.setDescription(desc);
                t.setSpam(result.isSpam);
                if (result.isSpam) {
                    System.out.println("AI Flagged as SPAM: " + result.reason);
                }

                // Collect Consignes
                t.getConsignes().clear();
                for (Node node : consignesContainer.getChildren()) {
                    if (node instanceof HBox) {
                        HBox row = (HBox) node;
                        for (Node child : row.getChildren()) {
                            if (child instanceof TextField) {
                                String txt = ((TextField) child).getText().trim();
                                if (!txt.isEmpty()) {
                                    t.getConsignes().add(new Consigne(txt));
                                }
                            }
                        }
                    }
                }

                t.setLocation(loc);
                t.setLatitude(selectedLat);
                t.setLongitude(selectedLon);

                if (editingTicket != null) {
                    t.setStatus(TicketStatus.PENDING);
                } else {
                    t.setStatus(TicketStatus.PENDING);
                }
                if (t.getPriority() == null) t.setPriority(TicketPriority.MEDIUM);
                if (t.getDomain() == null) t.setDomain(ActionDomain.OTHER);
                t.setAdminNotes(null);

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
                    } else {
                        ticketService.add(t);
                    }
                    
                    if (t.isSpam()) {
                        ticketService.checkAndApplyTimeout(t.getUserId());
                    }
                    
                    goToMyTickets(event);
                } catch (RuntimeException ex) {
                    showError(ex.getMessage());
                    submitBtn.setDisable(false);
                    submitBtn.setText(originalText);
                }
            });
        }).start();
    }

    public void setTicketForEdit(Ticket ticket) {
        if (ticket == null || ticket.getStatus() == TicketStatus.PUBLISHED) {
            return;
        }
        this.editingTicket = ticketService.getById(ticket.getId());
        if (this.editingTicket == null) {
            this.editingTicket = ticket;
        }
        titleInput.setText(this.editingTicket.getTitle());
        descriptionInput.setText(this.editingTicket.getDescription());
        consignesContainer.getChildren().clear();
        if (this.editingTicket.getConsignes().isEmpty()) {
            addConsigneRow("");
        } else {
            for (Consigne c : this.editingTicket.getConsignes()) {
                addConsigneRow(c.getText());
            }
        }
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
