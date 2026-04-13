package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.services.TicketService;

import java.util.stream.Collectors;

public class TicketPublicBoardController {

    @FXML private TableView<Ticket> publicTable;
    @FXML private TableColumn<Ticket, String> colTitle;
    @FXML private TableColumn<Ticket, ActionDomain> colDomain;
    @FXML private TableColumn<Ticket, TicketPriority> colPriority;
    @FXML private TableColumn<Ticket, String> colLocation;

    @FXML private VBox actionPanel;
    @FXML private TextArea proofMessageArea;
    @FXML private TextField proofImageField;

    private TicketService ticketService;
    private ObservableList<Ticket> publishedTickets = FXCollections.observableArrayList();
    private Ticket selectedTicket = null;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        setupTable();
        loadPublishedTickets();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDomain.setCellValueFactory(new PropertyValueFactory<>("domain"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        publicTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedTicket = newV;
            actionPanel.setDisable(newV == null);
        });
    }

    private void loadPublishedTickets() {
        publishedTickets.setAll(
            ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.PUBLISHED && t.getCompletedById() == null)
                .collect(Collectors.toList())
        );
        publicTable.setItems(publishedTickets);
    }

    @FXML
    private void handleComplete() {
        if (selectedTicket == null) return;

        String message = proofMessageArea.getText();
        String img = proofImageField.getText();

        if (message.isEmpty()) {
            showAlert("Required", "Please provide a brief message about the resolution.");
            return;
        }

        selectedTicket.setCompletedById(1); // Mocked user ID
        selectedTicket.setCompletionMessage(message);
        selectedTicket.setCompletionImage(img);
        // Status remains PUBLISHED but it won't show in the public list anymore because completed_by_id is set
        
        ticketService.update(selectedTicket);
        showAlert("Submitted", "Thank you! Your completion has been submitted to admins.");
        
        proofMessageArea.clear();
        proofImageField.clear();
        loadPublishedTickets();
    }

    @FXML
    private void goToMyReports(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/TicketReporting.fxml");
    }

    @FXML
    private void handleBackHome(javafx.event.ActionEvent event) {
        navigate(event, "/home/Home.fxml");
    }

    private void navigate(javafx.event.ActionEvent event, String path) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(path));
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
