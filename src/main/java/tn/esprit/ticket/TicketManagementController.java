package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;

import java.io.IOException;

public class TicketManagementController {

    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Integer> colId;
    @FXML private TableColumn<Ticket, String> colTitle;
    @FXML private TableColumn<Ticket, String> colLocation;
    @FXML private TableColumn<Ticket, TicketStatus> colStatus;
    @FXML private TableColumn<Ticket, TicketPriority> colPriority;

    private final TicketService ticketService = new TicketService();

    @FXML
    public void initialize() {
        System.out.println("Ticket Management Initialized - Loading Records...");

        // Set up Cell Value Factories
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        // Load data from database
        loadTicketData();
    }

    private void loadTicketData() {
        try {
            // Fetch all tickets and filter out those that are "COMPLETED"
            ObservableList<Ticket> filteredTickets = FXCollections.observableArrayList();
            for (Ticket t : ticketService.getAll()) {
                if (t.getStatus() != TicketStatus.COMPLETED) {
                    filteredTickets.add(t);
                }
            }
            
            ticketTable.setItems(filteredTickets);
            System.out.println("Loaded " + filteredTickets.size() + " active tickets.");
        } catch (Exception e) {
            System.err.println("Error fetching tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome(javafx.scene.input.MouseEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    private void navigate(javafx.scene.input.MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
