package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.services.TicketService;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class TicketReportingController {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private ComboBox<ActionDomain> domainComboBox;
    @FXML private TextArea descriptionArea;

    @FXML private TableView<Ticket> myTicketsTable;
    @FXML private TableColumn<Ticket, String> colTitle;
    @FXML private TableColumn<Ticket, TicketStatus> colStatus;
    @FXML private TableColumn<Ticket, LocalDateTime> colDate;

    @FXML private VBox notesBox;
    @FXML private Label adminNotesLabel;

    private TicketService ticketService;
    private ObservableList<Ticket> myTickets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        domainComboBox.setItems(FXCollections.observableArrayList(ActionDomain.values()));
        setupTable();
        loadMyTickets();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        myTicketsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.getAdminNotes() != null) {
                notesBox.setVisible(true);
                adminNotesLabel.setText(newV.getAdminNotes());
            } else {
                notesBox.setVisible(false);
            }
        });
    }

    private void loadMyTickets() {
        // Mocking user_id = 1 for now
        int currentUserId = 1;
        myTickets.setAll(
            ticketService.getAll().stream()
                .filter(t -> t.getUserId() == currentUserId)
                .collect(Collectors.toList())
        );
        myTicketsTable.setItems(myTickets);
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText();
        String location = locationField.getText();
        ActionDomain domain = domainComboBox.getValue();
        String desc = descriptionArea.getText();

        if (title.isEmpty() || location.isEmpty()) {
            showAlert("Required Fields", "Please enter a title and location.");
            return;
        }

        Ticket t = new Ticket();
        t.setTitle(title);
        t.setLocation(location);
        t.setDomain(domain);
        t.setDescription(desc);
        t.setUserId(1); // Mocked user ID

        ticketService.add(t);
        showAlert("Submitted", "Your report is now pending administrative review.");
        
        titleField.clear();
        locationField.clear();
        domainComboBox.setValue(null);
        descriptionArea.clear();
        loadMyTickets();
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
        alert.show();
    }
}
