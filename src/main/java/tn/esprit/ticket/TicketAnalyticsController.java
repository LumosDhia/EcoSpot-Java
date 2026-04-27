package tn.esprit.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TicketAnalyticsController {

    @FXML private Label userNameLabel;
    @FXML private LineChart<String, Number> spamLineChart;
    @FXML private BarChart<String, Number> locationBarChart;
    @FXML private PieChart ngoPieChart;

    private final TicketService ticketService = new TicketService();

    @FXML
    public void initialize() {
        if (!isAdminUser()) {
            // fallback
            return;
        }

        if (SessionManager.getCurrentUser() != null) {
            userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        }

        tn.esprit.util.NavigationHistory.track(spamLineChart, "/ticket/TicketAnalytics.fxml");

        loadSpamTrends();
        loadLocationData();
        loadNgoEfficiency();
    }

    private void loadSpamTrends() {
        Map<LocalDate, Integer> trends = ticketService.getSpamTrends(7);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spam Tickets");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd");
        for (Map.Entry<LocalDate, Integer> entry : trends.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().format(dtf), entry.getValue()));
        }

        spamLineChart.getData().add(series);
        
        // Add some styling to the line
        javafx.application.Platform.runLater(() -> {
            if (series.getNode() != null) {
                series.getNode().setStyle("-fx-stroke: #ef4444; -fx-stroke-width: 3px;");
            }
        });
    }

    private void loadLocationData() {
        Map<String, Integer> locData = ticketService.getTicketsPerLocation();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (Map.Entry<String, Integer> entry : locData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        locationBarChart.getData().add(series);
    }

    private void loadNgoEfficiency() {
        Map<String, Integer> ngoData = ticketService.getResolvedTicketsPerNgo();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : ngoData.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        ngoPieChart.setData(pieChartData);
    }

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
                Parent root = loader.load();
                tn.esprit.user.DashboardController controller = loader.getController();
                if (controller != null && SessionManager.isLoggedIn()) {
                    controller.setUser(SessionManager.getCurrentUser());
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isAdminUser() {
        return SessionManager.isLoggedIn()
                && SessionManager.getCurrentUser() != null
                && "ADMIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
    }
}
