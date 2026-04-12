package tn.esprit.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class TicketManagementController {

    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Integer> colId;
    @FXML private TableColumn<Ticket, Integer> colEvent;
    @FXML private TableColumn<Ticket, String> colType;
    @FXML private TableColumn<Ticket, Double> colPrice;
    @FXML private TableColumn<Ticket, Void> colActions;

    @FXML
    public void initialize() {
        System.out.println("Ticket Management Initialized");
        // Here you would normally load data from a service
        // ticketTable.setItems(ticketService.getAllTickets());
    }

    @FXML
    private void goToHome(javafx.scene.input.MouseEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void goToHome(ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    @FXML
    private void handleCreateTicket(ActionEvent event) {
        System.out.println("Opening Create Ticket Dialog...");
        // Implement creation logic
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
