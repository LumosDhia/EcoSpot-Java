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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsController {

    @FXML private Button backBtn;
    @FXML private Label pageTitle;
    @FXML private ChoiceBox<String> periodSelector;
    
    // KPIs
    @FXML private Label totalViewsLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label engagementLabel;
    
    // Charts
    @FXML private LineChart<String, Number> viewsLineChart;
    @FXML private BarChart<String, Number> viewsBarChart;
    @FXML private BarChart<Number, String> categoryChart;
    @FXML private BarChart<String, Number> searchTermsChart;
    @FXML private BarChart<String, Number> hourlyChart;
    @FXML private ListView<String> topArticlesList;
    @FXML private Button manageArticlesBtn;

    private final StatisticsService statsService = new StatisticsService();
    private List<Integer> topArticleIds = new java.util.ArrayList<>();
    private String ownerEmail = null; // null = admin (all articles); non-null = NGO filter

    public void setOwnerEmail(String email) {
        this.ownerEmail = email;
        if (pageTitle != null) {
            pageTitle.setText(email != null ? "My Statistics" : "Blog Statistics");
        }
        if (manageArticlesBtn != null) {
            manageArticlesBtn.setVisible(email == null);
        }
        loadData();
    }

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
            controller.setOwnerEmail(ownerEmail);
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
        else if ("Last 7 Days".equals(period)) start = end.minusDays(6);
        else if ("Last 12 Months".equals(period)) start = end.minusMonths(11).withDayOfMonth(1);
        else start = end.minusDays(29);

        // Load KPIs
        totalViewsLabel.setText(String.valueOf(statsService.getViewsByPeriod(start, end, ownerEmail)));
        totalArticlesLabel.setText(String.valueOf(statsService.getPublishedArticlesByPeriod(start, end, ownerEmail)));
        totalCommentsLabel.setText(String.valueOf(statsService.getCommentsByPeriod(start, end, ownerEmail)));

        Map<String, Integer> reactions = statsService.getReactionsByPeriod(start, end, ownerEmail);
        int likes = reactions.getOrDefault("LIKE", 0);
        int dislikes = reactions.getOrDefault("DISLIKE", 0);
        engagementLabel.setText(String.valueOf(likes + dislikes));

        // Prepare time series data
        List<Map<String, Object>> timeSeries = new java.util.ArrayList<>();
        if ("Today".equals(period)) {
            List<Map<String, Object>> rawData = statsService.getViewsHourly(end, ownerEmail);
            for (int i = 0; i < 24; i++) {
                String label = String.format("%02d:00", i);
                int views = 0;
                for (Map<String, Object> row : rawData) {
                    if (label.equals(row.get("date"))) {
                        views = (int) row.get("views");
                        break;
                    }
                }
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("date", label);
                map.put("views", views);
                timeSeries.add(map);
            }
        } else if ("Last 12 Months".equals(period)) {
            List<Map<String, Object>> rawData = statsService.getViewsMonthly(start, end, ownerEmail);
            for (int i = 0; i <= 11; i++) {
                LocalDate d = start.plusMonths(i);
                if (d.isAfter(end)) break;
                String monthStr = d.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                int views = 0;
                for (Map<String, Object> row : rawData) {
                    if (monthStr.equals(row.get("date"))) {
                        views = (int) row.get("views");
                        break;
                    }
                }
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("date", monthStr);
                map.put("views", views);
                timeSeries.add(map);
            }
        } else {
            List<Map<String, Object>> rawData = statsService.getViewsTimeSeries(start, end, ownerEmail);
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            for (int i = 0; i <= days; i++) {
                LocalDate d = start.plusDays(i);
                String dayStr = d.toString();
                int views = 0;
                for (Map<String, Object> row : rawData) {
                    if (dayStr.equals(row.get("date"))) {
                        views = (int) row.get("views");
                        break;
                    }
                }
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("date", dayStr);
                map.put("views", views);
                timeSeries.add(map);
            }
        }
        
        boolean useLine = "Today".equals(period) || "Last 7 Days".equals(period);
        viewsLineChart.setVisible(useLine);
        viewsBarChart.setVisible(!useLine);
        
        XYChart.Series<String, Number> viewsSeries = new XYChart.Series<>();
        viewsSeries.setName("Views");
        for (Map<String, Object> dataPoint : timeSeries) {
            viewsSeries.getData().add(new XYChart.Data<>(dataPoint.get("date").toString(), (Number) dataPoint.get("views")));
        }
        
        if (useLine) {
            viewsLineChart.getData().clear();
            viewsLineChart.getData().add(viewsSeries);
        } else {
            viewsBarChart.getData().clear();
            viewsBarChart.getData().add(viewsSeries);
        }

        // Load Top Articles
        ObservableList<String> topArticles = FXCollections.observableArrayList();
        topArticleIds.clear();
        List<Map<String, Object>> articles = statsService.getTopArticlesByViews(start, end, 5, ownerEmail);
        for (int i = 0; i < articles.size(); i++) {
            Map<String, Object> art = articles.get(i);
            topArticles.add((i + 1) + ". " + art.get("title") + " - " + art.get("views") + " views");
            topArticleIds.add((Integer) art.get("id"));
        }
        topArticlesList.setItems(topArticles);

        // Load Category Chart (Horizontal Bar Chart)
        XYChart.Series<Number, String> catSeries = new XYChart.Series<>();
        catSeries.setName("Categories");
        List<Map<String, Object>> categoryStats = statsService.getCategoryStats(start, end, ownerEmail);
        for (Map<String, Object> cp : categoryStats) {
            Number v = (Number) cp.get("views");
            if (v.intValue() > 0) {
                catSeries.getData().add(new XYChart.Data<>(v, cp.get("name").toString()));
            }
        }
        categoryChart.getData().clear();
        categoryChart.getData().add(catSeries);

        // Load Hourly Chart
        XYChart.Series<String, Number> hourlySeries = new XYChart.Series<>();
        List<Map<String, Object>> hours = statsService.getViewsByHourOfDay(ownerEmail);
        for (Map<String, Object> h : hours) {
            hourlySeries.getData().add(new XYChart.Data<>(h.get("hour").toString() + ":00", (Number) h.get("views")));
        }
        hourlyChart.getData().clear();
        hourlyChart.getData().add(hourlySeries);

        // Load Search Terms (global — not article-scoped)
        XYChart.Series<String, Number> searchSeries = new XYChart.Series<>();
        List<Map<String, Object>> searches = statsService.getTopSearchTerms(start, end, 5);
        for (Map<String, Object> s : searches) {
            searchSeries.getData().add(new XYChart.Data<>(s.get("term").toString(), (Number) s.get("count")));
        }
        searchTermsChart.getData().clear();
        searchTermsChart.getData().add(searchSeries);
    }    @FXML
    private void handleExportCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Statistics CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        
        if (file != null) {
            String period = periodSelector.getValue();
            LocalDate end = LocalDate.now();
            LocalDate start;
            if ("Today".equals(period)) start = end;
            else if ("Last 7 Days".equals(period)) start = end.minusDays(6);
            else if ("Last 12 Months".equals(period)) start = end.minusMonths(11).withDayOfMonth(1);
            else start = end.minusDays(29);

            StringBuilder csv = new StringBuilder();
            
            // TABLE 1: PERFORMANCE TIMELINE
            if ("Today".equals(period)) {
                csv.append("--- HOURLY PERFORMANCE (").append(end).append(") ---\n");
                csv.append("Hour,Views,Articles Published,Comments,Likes,Dislikes\n");
                List<Map<String, Object>> hourly = statsService.getHourlyStats(end, ownerEmail);
                for (Map<String, Object> row : hourly) {
                    csv.append(row.get("hour")).append(",")
                       .append(row.get("views")).append(",")
                       .append(row.get("articles")).append(",")
                       .append(row.get("comments")).append(",")
                       .append(row.get("likes")).append(",")
                       .append(row.get("dislikes")).append("\n");
                }
            } else if ("Last 12 Months".equals(period)) {
                csv.append("--- MONTHLY PERFORMANCE SUMMARY ---\n");
                csv.append("Month,Views,Articles Published,Comments,Likes,Dislikes\n");
                for (int i = 0; i <= 11; i++) {
                    LocalDate d = start.plusMonths(i);
                    if (d.isAfter(end)) break;
                    String monthStr = d.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                    
                    // Note: This is slightly expensive (multiple queries) but ensures clean CSV data
                    LocalDate mStart = d.withDayOfMonth(1);
                    LocalDate mEnd = d.withDayOfMonth(d.lengthOfMonth());
                    if (mEnd.isAfter(end)) mEnd = end;

                    int views = statsService.getViewsByPeriod(mStart, mEnd, ownerEmail);
                    int articles = statsService.getPublishedArticlesByPeriod(mStart, mEnd, ownerEmail);
                    int comments = statsService.getCommentsByPeriod(mStart, mEnd, ownerEmail);
                    Map<String, Integer> reacts = statsService.getReactionsByPeriod(mStart, mEnd, ownerEmail);
                    
                    csv.append(monthStr).append(",")
                       .append(views).append(",")
                       .append(articles).append(",")
                       .append(comments).append(",")
                       .append(reacts.getOrDefault("LIKE", 0)).append(",")
                       .append(reacts.getOrDefault("DISLIKE", 0)).append("\n");
                }
            } else {
                csv.append("--- DAILY PERFORMANCE SUMMARY ---\n");
                csv.append("Date,Views,Articles Published,Comments,Likes,Dislikes\n");
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                for (int i = 0; i <= days; i++) {
                    LocalDate d = start.plusDays(i);
                    int views = statsService.getViewsByPeriod(d, d, ownerEmail);
                    int articles = statsService.getPublishedArticlesByPeriod(d, d, ownerEmail);
                    int comments = statsService.getCommentsByPeriod(d, d, ownerEmail);
                    Map<String, Integer> reacts = statsService.getReactionsByPeriod(d, d, ownerEmail);
                    csv.append(d).append(",")
                       .append(views).append(",")
                       .append(articles).append(",")
                       .append(comments).append(",")
                       .append(reacts.getOrDefault("LIKE", 0)).append(",")
                       .append(reacts.getOrDefault("DISLIKE", 0)).append("\n");
                }
            }
            csv.append("\n");

            // TABLE 2: ARTICLE PERFORMANCE SUMMARY (Sorted by Views)
            csv.append("--- ARTICLE PERFORMANCE SUMMARY (Ranked by Views) ---\n");
            csv.append("Rank,Article ID,Title,Published At,Views,Comments,Likes,Dislikes,Engagement\n");
            
            List<Map<String, Object>> articleStats = statsService.getDetailedArticleStats(start, end, ownerEmail);
            // Keep only articles with activity in the selected period
            articleStats.removeIf(art ->
                (int) art.get("views") + (int) art.get("comments") +
                (int) art.get("likes") + (int) art.get("dislikes") == 0
            );
            articleStats.sort((a, b) -> Integer.compare((int) b.get("views"), (int) a.get("views")));
            
            if (articleStats.isEmpty()) {
                csv.append("No article activity in this period.\n");
            }
            int rank = 1;
            for (Map<String, Object> art : articleStats) {
                int v = (int) art.get("views");
                int c = (int) art.get("comments");
                int l = (int) art.get("likes");
                int dl = (int) art.get("dislikes");
                
                csv.append(rank++).append(",")
                   .append(art.get("id")).append(",")
                   .append("\"").append(art.get("title").toString().replace("\"", "\"\"")).append("\",");
                Object pub = art.get("published_at");
                if (pub instanceof java.sql.Timestamp) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    csv.append(((java.sql.Timestamp) pub).toLocalDateTime().format(dtf));
                } else {
                    csv.append(pub == null ? "N/A" : pub);
                }
                csv.append(",")
                   .append(v).append(",")
                   .append(c).append(",")
                   .append(l).append(",")
                   .append(dl).append(",")
                   .append(v + c + l + dl).append("\n");
            }

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(csv.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleManageArticles(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/ArticleManagementStats.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
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
