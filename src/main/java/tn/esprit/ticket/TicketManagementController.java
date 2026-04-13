package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.services.TicketService;

import java.util.List;

public class TicketManagementController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField locationField;
    @FXML private ComboBox<TicketStatus> statusComboBox;
    @FXML private ComboBox<TicketPriority> priorityComboBox;
    @FXML private ComboBox<ActionDomain> domainComboBox;
    
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Integer> idCol;
    @FXML private TableColumn<Ticket, String> titleCol;
    @FXML private TableColumn<Ticket, TicketStatus> statusCol;
    @FXML private TableColumn<Ticket, TicketPriority> priorityCol;
    @FXML private TableColumn<Ticket, ActionDomain> domainCol;
    @FXML private TableColumn<Ticket, String> locationCol;
    @FXML private TableColumn<Ticket, Void> actionsCol;
    @FXML private TextField searchField;

    private TicketService ticketService;
    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private Ticket selectedTicket = null;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        
        statusComboBox.setItems(FXCollections.observableArrayList(TicketStatus.values()));
        priorityComboBox.setItems(FXCollections.observableArrayList(TicketPriority.values()));
        domainComboBox.setItems(FXCollections.observableArrayList(ActionDomain.values()));
        
        setupTable();
        loadTickets();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        domainCol.setCellValueFactory(new PropertyValueFactory<>("domain"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        // Actions Column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteBtn.setCursor(javafx.scene.Cursor.HAND);
                deleteBtn.setOnAction(event -> {
                    Ticket t = getTableView().getItems().get(getIndex());
                    ticketService.delete(t);
                    loadTickets();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        // Selection listener
        ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTicket = newSelection;
                titleField.setText(selectedTicket.getTitle());
                descriptionArea.setText(selectedTicket.getDescription());
                locationField.setText(selectedTicket.getLocation());
                statusComboBox.setValue(selectedTicket.getStatus());
                priorityComboBox.setValue(selectedTicket.getPriority());
                domainComboBox.setValue(selectedTicket.getDomain());
            }
        });

        // Search Filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTickets(newValue);
        });
    }

    private void filterTickets(String query) {
        if (query == null || query.isEmpty()) {
            ticketTable.setItems(ticketList);
            return;
        }
        
        String lowerQuery = query.toLowerCase();
        ObservableList<Ticket> filtered = ticketList.filtered(t -> 
            t.getTitle().toLowerCase().contains(lowerQuery) || 
            t.getLocation().toLowerCase().contains(lowerQuery) ||
            t.getStatus().name().toLowerCase().contains(lowerQuery)
        );
        ticketTable.setItems(filtered);
    }

    private void loadTickets() {
        List<Ticket> tickets = ticketService.getAll();
        ticketList.setAll(tickets);
        ticketTable.setItems(ticketList);
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String location = locationField.getText();
        TicketStatus status = statusComboBox.getValue();
        TicketPriority priority = priorityComboBox.getValue();
        ActionDomain domain = domainComboBox.getValue();

        if (title.isEmpty() || status == null || priority == null) {
            showAlert("Validation Error", "Please fill in all mandatory fields (Title, Status, Priority).");
            return;
        }

        if (selectedTicket == null) {
            Ticket t = new Ticket();
            t.setTitle(title);
            t.setDescription(description);
            t.setLocation(location);
            t.setStatus(status);
            t.setPriority(priority);
            t.setDomain(domain);
            t.setUserId(1); // Default user for now
            ticketService.add(t);
            showAlert("Success", "Report submitted successfully!");
        } else {
            selectedTicket.setTitle(title);
            selectedTicket.setDescription(description);
            selectedTicket.setLocation(location);
            selectedTicket.setStatus(status);
            selectedTicket.setPriority(priority);
            selectedTicket.setDomain(domain);
            ticketService.update(selectedTicket);
            showAlert("Success", "Report updated successfully!");
        }

        handleClear();
        loadTickets();
    }

    @FXML
    private void handleClear() {
        titleField.clear();
        descriptionArea.clear();
        locationField.clear();
        statusComboBox.setValue(null);
        priorityComboBox.setValue(null);
        domainComboBox.setValue(null);
        selectedTicket = null;
        ticketTable.getSelectionModel().clearSelection();
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
