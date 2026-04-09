package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.services.TicketService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TicketManagementController {

    // Moderation TAB
    @FXML private TextArea adminNotesArea;
    @FXML private TableView<Ticket> pendingTable;
    @FXML private TableColumn<Ticket, Integer> pIdCol;
    @FXML private TableColumn<Ticket, String> pTitleCol;
    @FXML private TableColumn<Ticket, Integer> pUserCol;
    @FXML private TableColumn<Ticket, TicketPriority> pPriorityCol;
    @FXML private TableColumn<Ticket, String> pLocationCol;

    // Completion TAB
    @FXML private Label proofMessageLabel;
    @FXML private TableView<Ticket> completionTable;
    @FXML private TableColumn<Ticket, Integer> cIdCol;
    @FXML private TableColumn<Ticket, String> cTitleCol;
    @FXML private TableColumn<Ticket, Integer> cWorkerCol;
    @FXML private TableColumn<Ticket, LocalDateTime> cDateCol;

    private TicketService ticketService;
    private ObservableList<Ticket> allTickets = FXCollections.observableArrayList();
    private Ticket selectedPending = null;
    private Ticket selectedCompletion = null;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        setupTables();
        loadData();
    }

    private void setupTables() {
        // Pending Table
        pIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        pTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        pUserCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        pPriorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        pLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        pendingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedPending = newV;
            if (newV != null) adminNotesArea.setText(newV.getAdminNotes());
        });

        // Completion Table
        cIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        cTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        cWorkerCol.setCellValueFactory(new PropertyValueFactory<>("completedById"));
        cDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        completionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedCompletion = newV;
            if (newV != null) proofMessageLabel.setText(newV.getCompletionMessage());
        });
    }

    private void loadData() {
        List<Ticket> tickets = ticketService.getAll();
        allTickets.setAll(tickets);

        // Filter for Pending Table (PENDING or SENT_BACK)
        ObservableList<Ticket> pending = FXCollections.observableArrayList(
            tickets.stream().filter(t -> t.getStatus() == TicketStatus.PENDING || t.getStatus() == TicketStatus.SENT_BACK).collect(Collectors.toList())
        );
        pendingTable.setItems(pending);

        // Filter for Completion Table (Workers submitted proof but not achieved yet)
        ObservableList<Ticket> completions = FXCollections.observableArrayList(
            tickets.stream().filter(t -> t.getCompletedById() != null && t.getAchievedAt() == null).collect(Collectors.toList())
        );
        completionTable.setItems(completions);
    }

    @FXML
    private void handlePublish() {
        if (selectedPending == null) return;
        selectedPending.setStatus(TicketStatus.PUBLISHED);
        selectedPending.setAdminNotes(null);
        ticketService.update(selectedPending);
        showAlert("Success", "Ticket published successfully!");
        loadData();
    }

    @FXML
    private void handleSendBack() {
        if (selectedPending == null) return;
        String notes = adminNotesArea.getText();
        if (notes == null || notes.isEmpty()) {
            showAlert("Required", "Please provide notes for the user.");
            return;
        }
        selectedPending.setStatus(TicketStatus.SENT_BACK);
        selectedPending.setAdminNotes(notes);
        ticketService.update(selectedPending);
        showAlert("Success", "Ticket sent back for fixes.");
        loadData();
    }

    @FXML
    private void handleRefuse() {
        if (selectedPending == null) return;
        selectedPending.setStatus(TicketStatus.REFUSED);
        selectedPending.setAdminNotes(adminNotesArea.getText());
        ticketService.update(selectedPending);
        showAlert("Refused", "Ticket has been refused.");
        loadData();
    }

    @FXML
    private void handleMarkAchieved() {
        if (selectedCompletion == null) return;
        selectedCompletion.setStatus(TicketStatus.COMPLETED);
        selectedCompletion.setAchievedAt(LocalDateTime.now());
        ticketService.update(selectedCompletion);
        showAlert("🏆 Achievement Unlocked", "Validation complete! User/NGO rewarded.");
        loadData();
    }

    @FXML
    private void handleBackHome(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
