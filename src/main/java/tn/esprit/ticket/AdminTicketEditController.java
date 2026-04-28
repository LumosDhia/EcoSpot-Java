package tn.esprit.ticket;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.GeocodingService;
import tn.esprit.services.OpenRouterService;
import tn.esprit.services.TicketService;
import tn.esprit.services.UserService;
import tn.esprit.user.User;
import tn.esprit.util.ImageUploadUtils;
import tn.esprit.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTicketEditController {

    @FXML private Label userNameLabel;
    @FXML private TextField titleInput;
    @FXML private TextArea descriptionInput;
    @FXML private TextField locationInput;
    @FXML private Button searchLocationBtn;
    @FXML private ListView<GeocodingService.Place> locationResultsList;
    @FXML private VBox consignesContainer;
    @FXML private Button aiSuggestBtn;
    @FXML private Label aiSpamLabel;
    @FXML private ComboBox<TicketStatus> statusCombo;
    @FXML private ComboBox<TicketPriority> priorityCombo;
    @FXML private ComboBox<ActionDomain> domainCombo;
    @FXML private ComboBox<User> ngoCombo;
    @FXML private Label aiNgoLabel;
    @FXML private CheckBox isSpamCheck;
    @FXML private ImageView ticketImageView;
    @FXML private Label fileNameLabel;
    @FXML private TextArea adminNotesInput;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private ScrollPane mainScrollPane;

    private final TicketService ticketService = new TicketService();
    private final UserService userService = new UserService();
    private final GeocodingService geocodingService = new GeocodingService();
    private final OpenRouterService openRouterService = new OpenRouterService();
    
    private Ticket currentTicket;
    private File selectedFile;
    private double selectedLat;
    private double selectedLon;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        }

        setupCombos();
        setupLocationSearch();
    }

    private void setupCombos() {
        statusCombo.getItems().setAll(TicketStatus.values());
        priorityCombo.getItems().setAll(TicketPriority.values());
        domainCombo.getItems().setAll(ActionDomain.values());

        List<User> ngoUsers = userService.getAllUsers().stream()
                .filter(u -> "NGO".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        ngoCombo.getItems().setAll(ngoUsers);
        
        ngoCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getUsername());
            }
        });
        ngoCombo.setButtonCell(new ListCell<User>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getUsername());
            }
        });
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

    public void setTicket(Ticket ticket) {
        this.currentTicket = ticket;
        if (ticket == null) return;

        titleInput.setText(ticket.getTitle());
        descriptionInput.setText(ticket.getDescription());
        locationInput.setText(ticket.getLocation());
        selectedLat = ticket.getLatitude();
        selectedLon = ticket.getLongitude();

        statusCombo.setValue(ticket.getStatus());
        priorityCombo.setValue(ticket.getPriority());
        domainCombo.setValue(ticket.getDomain());
        isSpamCheck.setSelected(ticket.isSpam());
        aiSpamLabel.setVisible(ticket.isSpam());
        aiSpamLabel.setManaged(ticket.isSpam());
        adminNotesInput.setText(ticket.getAdminNotes());

        if (ticket.getAssignedNgoId() != null && ticket.getAssignedNgoId() > 0) {
            userService.getAllUsers().stream()
                    .filter(u -> u.getId() == ticket.getAssignedNgoId())
                    .findFirst()
                    .ifPresent(u -> ngoCombo.setValue(u));
        }

        if (ticket.getAiSuggestedNgo() != null && !ticket.getAiSuggestedNgo().isEmpty()) {
            aiNgoLabel.setText("Recommended: " + ticket.getAiSuggestedNgo());
            aiNgoLabel.setVisible(true);
            aiNgoLabel.setManaged(true);
        }

        consignesContainer.getChildren().clear();
        for (Consigne c : ticket.getConsignes()) {
            addConsigneRow(c.getText());
        }

        if (ticket.getImage() != null && !ticket.getImage().isEmpty()) {
            String url = ImageUploadUtils.getImageUrl("tickets", ticket.getImage());
            ticketImageView.setImage(new Image(url, true));
            ticketImageView.setVisible(true);
            ticketImageView.setManaged(true);
        }
    }

    @FXML
    void handleAddConsigne(ActionEvent event) {
        addConsigneRow("");
    }

    private void addConsigneRow(String text) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField field = new TextField(text);
        field.setPromptText("Instruction step...");
        HBox.setHgrow(field, Priority.ALWAYS);
        field.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 4;");

        Button del = new Button("✕");
        del.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
        del.setOnAction(e -> consignesContainer.getChildren().remove(row));

        row.getChildren().addAll(field, del);
        consignesContainer.getChildren().add(row);
    }

    @FXML
    void handleLocationSearch(ActionEvent event) {
        String query = locationInput.getText().trim();
        if (query.length() < 3) return;

        searchLocationBtn.setDisable(true);
        new Thread(() -> {
            List<GeocodingService.Place> results = geocodingService.search(query);
            Platform.runLater(() -> {
                searchLocationBtn.setDisable(false);
                if (!results.isEmpty()) {
                    locationResultsList.getItems().setAll(results);
                    locationResultsList.setVisible(true);
                    locationResultsList.setManaged(true);
                }
            });
        }).start();
    }

    @FXML
    void handleAiSuggest(ActionEvent event) {
        String t = titleInput.getText();
        String d = descriptionInput.getText();
        if (d.length() < 10) return;

        aiSuggestBtn.setDisable(true);
        new Thread(() -> {
            OpenRouterService.AiResponse response = openRouterService.generateTasks(t, d);
            Platform.runLater(() -> {
                aiSuggestBtn.setDisable(false);
                if (!response.tasks.isEmpty()) {
                    consignesContainer.getChildren().clear();
                    for (String task : response.tasks) addConsigneRow(task);
                }
            });
        }).start();
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (currentTicket == null) return;

        currentTicket.setTitle(titleInput.getText().trim());
        currentTicket.setDescription(descriptionInput.getText().trim());
        currentTicket.setLocation(locationInput.getText().trim());
        currentTicket.setLatitude(selectedLat);
        currentTicket.setLongitude(selectedLon);
        currentTicket.setStatus(statusCombo.getValue());
        currentTicket.setPriority(priorityCombo.getValue());
        currentTicket.setDomain(domainCombo.getValue());
        currentTicket.setSpam(isSpamCheck.isSelected());
        currentTicket.setAdminNotes(adminNotesInput.getText());

        User assigned = ngoCombo.getValue();
        if (assigned != null) {
            currentTicket.setAssignedNgoId(assigned.getId());
            if (currentTicket.getStatus() == TicketStatus.PUBLISHED || currentTicket.getStatus() == TicketStatus.PENDING) {
                currentTicket.setStatus(TicketStatus.ASSIGNED);
            }
        }

        currentTicket.getConsignes().clear();
        for (Node n : consignesContainer.getChildren()) {
            if (n instanceof HBox) {
                HBox row = (HBox) n;
                TextField f = (TextField) row.getChildren().get(0);
                String val = f.getText().trim();
                if (!val.isEmpty()) currentTicket.getConsignes().add(new Consigne(val));
            }
        }

        if (selectedFile != null) {
            try {
                String name = ImageUploadUtils.saveImage(selectedFile, "tickets");
                currentTicket.setImage(name);
            } catch (IOException e) {
                errorLabel.setText("Image upload failed.");
                errorLabel.setVisible(true);
                return;
            }
        }

        ticketService.update(currentTicket);
        goBack(event);
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ticket/AdminAllTickets.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
