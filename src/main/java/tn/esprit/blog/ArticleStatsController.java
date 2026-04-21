package tn.esprit.blog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.StatisticsService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ArticleStatsController {

    @FXML private Button backBtn;
    @FXML private Label articleTitleLabel;
    @FXML private Label articleMetaLabel;
    @FXML private Label totalViewsLabel;
    @FXML private Label likesLabel;
    @FXML private Label dislikesLabel;
    @FXML private Label commentsLabel;
    
    @FXML private AreaChart<String, Number> viewsTimelineChart;
    @FXML private LineChart<String, Number> reactionsChart;
    @FXML private BarChart<String, Number> commentsChart;

    private final StatisticsService statsService = new StatisticsService();
    private int articleId;

    public void setArticleId(int id) {
        this.articleId = id;
        loadData();
    }

    private void loadData() {
        Map<String, Object> basic = statsService.getArticleBasicStats(articleId);

        articleTitleLabel.setText((String) basic.getOrDefault("title", "Article #" + articleId));
        Timestamp ts = (Timestamp) basic.get("published_at");
        String dateStr = ts != null
                ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "Draft";
        articleMetaLabel.setText("By " + basic.getOrDefault("author", "Unknown") + " | Published on " + dateStr);
        totalViewsLabel.setText(String.valueOf(basic.getOrDefault("views", 0)));
        likesLabel.setText(String.valueOf(basic.getOrDefault("likes", 0)));
        dislikesLabel.setText(String.valueOf(basic.getOrDefault("dislikes", 0)));
        commentsLabel.setText(String.valueOf(basic.getOrDefault("comments", 0)));

        // Views Timeline
        XYChart.Series<String, Number> viewsSeries = new XYChart.Series<>();
        viewsSeries.setName("Views");
        List<Map<String, Object>> timeline = statsService.getArticleViewsTimeline(articleId, 30);
        for (Map<String, Object> point : timeline) {
            viewsSeries.getData().add(new XYChart.Data<>((String) point.get("date"), (Number) point.get("views")));
        }
        viewsTimelineChart.getData().clear();
        viewsTimelineChart.getData().add(viewsSeries);
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/Statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
