package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.models.ArticleStatsRow;
import tn.esprit.services.StatisticsService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ArticleManagementStatsController {

    @FXML private TextField searchField;
    @FXML private TableView<ArticleStatsRow> articlesTable;
    @FXML private TableColumn<ArticleStatsRow, String> titleColumn;
    @FXML private TableColumn<ArticleStatsRow, String> authorColumn;
    @FXML private TableColumn<ArticleStatsRow, Integer> viewsColumn;
    @FXML private TableColumn<ArticleStatsRow, Integer> likesColumn;
    @FXML private TableColumn<ArticleStatsRow, Integer> dislikesColumn;
    @FXML private TableColumn<ArticleStatsRow, Integer> commentsColumn;

    private final StatisticsService statsService = new StatisticsService();
    private final ObservableList<ArticleStatsRow> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        viewsColumn.setCellValueFactory(new PropertyValueFactory<>("views"));
        likesColumn.setCellValueFactory(new PropertyValueFactory<>("likes"));
        dislikesColumn.setCellValueFactory(new PropertyValueFactory<>("dislikes"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

        loadData();

        // Setup Search
        FilteredList<ArticleStatsRow> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(article -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (article.getTitle().toLowerCase().contains(lowerCaseFilter)) return true;
                if (article.getAuthor().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });

        SortedList<ArticleStatsRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(articlesTable.comparatorProperty());
        articlesTable.setItems(sortedData);

        // Click to drill down
        articlesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && articlesTable.getSelectionModel().getSelectedItem() != null) {
                navigateToArticleStats(articlesTable.getSelectionModel().getSelectedItem().getId());
            }
        });
    }

    private void navigateToArticleStats(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/ArticleStats.fxml"));
            Parent root = loader.load();
            ArticleStatsController controller = loader.getController();
            controller.setOwnerEmail(null); // Admin
            controller.setArticleId(id);
            Stage stage = (Stage) articlesTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadData() {
        masterData.clear();
        List<Map<String, Object>> data = statsService.getAllArticlesWithStats();
        for (Map<String, Object> item : data) {
            masterData.add(new ArticleStatsRow(
                (int) item.get("id"),
                (String) item.get("title"),
                (String) item.get("author"),
                (int) item.get("views"),
                (int) item.get("likes"),
                (int) item.get("dislikes"),
                (int) item.get("comments")
            ));
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/Statistics.fxml"));
            Parent root = loader.load();
            StatisticsController controller = loader.getController();
            controller.setOwnerEmail(null); // Admin view
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
