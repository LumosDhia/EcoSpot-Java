package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.StatisticsService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StatisticsController {

    @FXML private Button backBtn;
    @FXML private ChoiceBox<String> periodSelector;
    
    // KPIs
    @FXML private Label totalViewsLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label likeRateLabel;
    
    // Charts
    @FXML private AreaChart<String, Number> viewsChart;
    @FXML private PieChart categoryChart;
    @FXML private BarChart<String, Number> searchTermsChart;
    @FXML private BarChart<String, Number> hourlyChart;
    @FXML private ListView<String> topArticlesList;

    private final StatisticsService statsService = new StatisticsService();
    private List<Integer> topArticleIds = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        periodSelector.setItems(FXCollections.observableArrayList("Today", "Last 7 Days", "Last 30 Days", "Last 12 Months"));
        periodSelector.setValue("Last 30 Days");
        periodSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadData());

        topArticlesList.setOnMouseClicked(event -> {
            int index = topArticlesList.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < topArticleIds.size()) {
                navigateToArticleStats(topArticleIds.get(index));
            }
        });
        
        loadData();
    }

    private void navigateToArticleStats(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/ArticleStats.fxml"));
            Parent root = loader.load();
            ArticleStatsController controller = loader.getController();
            controller.setArticleId(id);
            Stage stage = (Stage) topArticlesList.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void loadData() {
        LocalDate end = LocalDate.now();
        LocalDate start;
        
        String period = periodSelector.getValue();
        if ("Today".equals(period)) start = end;
        else if ("Last 7 Days".equals(period)) start = end.minusDays(7);
        else if ("Last 12 Months".equals(period)) start = end.minusMonths(12);
        else start = end.minusDays(30);

        // Load KPIs
        int views = statsService.getTotalViews(start, end);
        totalViewsLabel.setText(String.valueOf(views));
        
        totalArticlesLabel.setText(String.valueOf(statsService.getTotalPublishedArticles()));
        totalCommentsLabel.setText(String.valueOf(statsService.getTotalComments()));
        
        Map<String, Integer> reactions = statsService.getTotalReactions();
        int likes = reactions.getOrDefault("LIKE", 0);
        int dislikes = reactions.getOrDefault("DISLIKE", 0);
        int totalReactions = likes + dislikes;
        int likeRate = totalReactions > 0 ? (likes * 100 / totalReactions) : 0;
        likeRateLabel.setText(likeRate + "%");

        // Load Views Chart
        XYChart.Series<String, Number> viewsSeries = new XYChart.Series<>();
        viewsSeries.setName("Views");
        List<Map<String, Object>> timeSeries = statsService.getViewsTimeSeries(start, end);
        for (Map<String, Object> dataPoint : timeSeries) {
            viewsSeries.getData().add(new XYChart.Data<>(dataPoint.get("date").toString(), (Number) dataPoint.get("views")));
        }
        viewsChart.getData().clear();
        viewsChart.getData().add(viewsSeries);

        // Load Top Articles
        ObservableList<String> topArticles = FXCollections.observableArrayList();
        topArticleIds.clear();
        List<Map<String, Object>> articles = statsService.getTopArticlesByViews(start, end, 5);
        for (int i = 0; i < articles.size(); i++) {
            Map<String, Object> art = articles.get(i);
            topArticles.add((i + 1) + ". " + art.get("title") + " - " + art.get("views") + " views");
            topArticleIds.add((Integer) art.get("id"));
        }
        topArticlesList.setItems(topArticles);

        // Load Category Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        List<Map<String, Object>> cats = statsService.getCategoryStats();
        for (Map<String, Object> cat : cats) {
            Number v = (Number) cat.get("views");
            if (v.intValue() > 0) {
                pieChartData.add(new PieChart.Data(cat.get("name").toString(), v.doubleValue()));
            }
        }
        categoryChart.setData(pieChartData);

        // Load Hourly Chart
        XYChart.Series<String, Number> hourlySeries = new XYChart.Series<>();
        List<Map<String, Object>> hours = statsService.getViewsByHourOfDay();
        for (Map<String, Object> h : hours) {
            hourlySeries.getData().add(new XYChart.Data<>(h.get("hour").toString() + ":00", (Number) h.get("views")));
        }
        hourlyChart.getData().clear();
        hourlyChart.getData().add(hourlySeries);
        
        // Load Search Terms
        XYChart.Series<String, Number> searchSeries = new XYChart.Series<>();
        List<Map<String, Object>> searches = statsService.getTopSearchTerms(start, end, 5);
        for (Map<String, Object> s : searches) {
            searchSeries.getData().add(new XYChart.Data<>(s.get("term").toString(), (Number) s.get("count")));
        }
        searchTermsChart.getData().clear();
        searchTermsChart.getData().add(searchSeries);
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Statistics CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        
        if (file != null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30); // Default to last 30 days for export
            List<Map<String, Object>> data = statsService.getViewsTimeSeries(start, end);
            tn.esprit.util.ExportService.exportToCSV(data, file);
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
