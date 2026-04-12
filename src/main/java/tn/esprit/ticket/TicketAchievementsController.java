package tn.esprit.ticket;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.services.TicketService;

import java.util.List;
import java.util.stream.Collectors;

public class TicketAchievementsController {

    @FXML private FlowPane achievementsFlow;

    private TicketService ticketService;

    @FXML
    public void initialize() {
        ticketService = new TicketService();
        loadAchievements();
    }

    private void loadAchievements() {
        List<Ticket> completed = ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.COMPLETED)
                .collect(Collectors.toList());

        achievementsFlow.getChildren().clear();
        for (Ticket t : completed) {
            achievementsFlow.getChildren().add(createAchievementCard(t));
        }
    }

    private VBox createAchievementCard(Ticket t) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setAlignment(Pos.TOP_CENTER);

        Label trophy = new Label("🏆");
        trophy.setStyle("-fx-font-size: 32px;");

        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-alignment: center;");
        title.setWrapText(true);

        Label location = new Label("📍 " + t.getLocation());
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        Separator sep = new Separator();

        Label worker = new Label("Hero ID: " + t.getCompletedById());
        worker.setStyle("-fx-background-color: #e9f5ee; -fx-text-fill: #2d6a4f; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold;");

        Label message = new Label("\"" + t.getCompletionMessage() + "\"");
        message.setStyle("-fx-font-style: italic; -fx-text-fill: #555; -fx-text-alignment: center;");
        message.setWrapText(true);

        card.getChildren().addAll(trophy, title, location, sep, worker, message);
        return card;
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
}
