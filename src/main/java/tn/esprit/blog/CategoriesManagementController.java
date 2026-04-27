package tn.esprit.blog;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.CategoryService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CategoriesManagementController {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, String> colCategoryName;
    @FXML private TableColumn<Category, Number> colCategoryArticles;
    @FXML private TableColumn<Category, Void> colCategoryActions;
    @FXML private ListView<String> categoryArticlesList;

    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(categoriesTable, "/blog/CategoriesManagement.fxml");

        colCategoryName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCategoryArticles.setCellValueFactory(data ->
                new SimpleIntegerProperty(categoryService.countArticlesForCategory(data.getValue().getId())));
        setupCategoryActions();

        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                categoryArticlesList.setItems(FXCollections.emptyObservableList());
                return;
            }
            List<String> titles = categoryService.getArticleTitlesForCategory(selected.getId());
            categoryArticlesList.setItems(FXCollections.observableArrayList(titles));
        });

        refreshData();
    }

    private void setupCategoryActions() {
        colCategoryActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #fdae6b; -fx-text-fill: #fdae6b; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #bc4749; -fx-text-fill: #bc4749; -fx-cursor: hand;");
                editBtn.setOnAction(e -> onEditCategory(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDeleteCategory(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void onEditCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Update category name for all related articles");
        dialog.setContentText("New name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String newName = result.get().trim();
        if (newName.isEmpty()) return;
        if (categoryService.renameCategory(category.getId(), newName)) {
            refreshData();
        } else {
            showValidationError("Invalid category name. Use 2-40 chars and start with a letter.");
        }
    }

    private void onDeleteCategory(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category");
        confirm.setHeaderText("Delete category \"" + category.getName() + "\"?");
        confirm.setContentText("This removes the category from all linked articles.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (categoryService.deleteCategoryAndUnlinkArticles(category.getId())) {
            refreshData();
        }
    }

    private void refreshData() {
        categoriesTable.setItems(FXCollections.observableArrayList(categoryService.getAll()));
        categoryArticlesList.setItems(FXCollections.emptyObservableList());
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
    }

    @FXML
    private void goToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && tn.esprit.util.SessionManager.getCurrentUser() != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
