package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.services.TicketService;

import java.util.List;

public class TicketManagementController {

    @FXML private TextField eventIdField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Integer> idCol;
    @FXML private TableColumn<Ticket, Integer> eventIdCol;
    @FXML private TableColumn<Ticket, Double> priceCol;
    @FXML private TableColumn<Ticket, String> typeCol;
    @FXML private TableColumn<Ticket, Void> actionsCol;
    @FXML private TextField searchField;

    private TicketService ticketService;
    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private Ticket selectedTicket = null;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        typeComboBox.setItems(FXCollections.observableArrayList("Standard", "VIP", "Backstage", "Early Bird"));
        
        setupTable();
        loadTickets();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        eventIdCol.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Selection listener
        ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTicket = newSelection;
                eventIdField.setText(String.valueOf(selectedTicket.getEventId()));
                priceField.setText(String.valueOf(selectedTicket.getPrice()));
                typeComboBox.setValue(selectedTicket.getType());
            }
        });
    }

    private void loadTickets() {
        List<Ticket> tickets = ticketService.getAll();
        ticketList.setAll(tickets);
        ticketTable.setItems(ticketList);
    }

    @FXML
    private void handleSave() {
        try {
            int eventId = Integer.parseInt(eventIdField.getText());
            double price = Double.parseDouble(priceField.getText());
            String type = typeComboBox.getValue();

            if (type == null) {
                showAlert("Validation Error", "Please select a ticket type.");
                return;
            }

            if (selectedTicket == null) {
                Ticket t = new Ticket(0, eventId, price, type);
                ticketService.add(t);
                showAlert("Success", "Ticket added successfully!");
            } else {
                selectedTicket.setEventId(eventId);
                selectedTicket.setPrice(price);
                selectedTicket.setType(type);
                ticketService.update(selectedTicket);
                showAlert("Success", "Ticket updated successfully!");
            }

            handleClear();
            loadTickets();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for Event ID and Price.");
        }
    }

    @FXML
    private void handleClear() {
        eventIdField.clear();
        priceField.clear();
        typeComboBox.setValue(null);
        selectedTicket = null;
        ticketTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
