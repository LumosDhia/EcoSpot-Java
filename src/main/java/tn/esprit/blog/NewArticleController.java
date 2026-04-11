package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.services.BlogService;
import tn.esprit.services.CategoryService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NewArticleController {

    @FXML private TextField titleField;
    @FXML private javafx.scene.web.HTMLEditor contentEditor;
    @FXML private ChoiceBox<String> publicationChoice;
    @FXML private Label fileNameLabel;
    @FXML private ImageView imagePreview;
    @FXML private VBox categoriesContainer;
    @FXML private VBox tagsContainer;

    private BlogService blogService = new BlogService();
    private CategoryService categoryService = new CategoryService();
    private String selectedImagePath = "";
    private ToggleGroup categoryGroup = new ToggleGroup();
    private Blog editArticle = null;

    @FXML
    public void initialize() {
        publicationChoice.getItems().addAll("Save as draft", "Publish immediately");
        publicationChoice.setValue("Save as draft");
        
        loadCategories();
        loadTags();
    }

    public void setEditArticle(Blog blog) {
        this.editArticle = blog;
        titleField.setText(blog.getTitle());
        contentEditor.setHtmlText(blog.getContent());
        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            selectedImagePath = blog.getImage();
            try {
                imagePreview.setImage(new Image(selectedImagePath, true));
                fileNameLabel.setText("Existing image");
            } catch (Exception e) {}
        }
        
        if (blog.getCategory() != null) {
            categoryGroup.getToggles().stream()
                .filter(t -> ((Category)t.getUserData()).getId() == blog.getCategory().getId())
                .findFirst()
                .ifPresent(t -> t.setSelected(true));
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAll();
        for (Category cat : categories) {
            RadioButton rb = new RadioButton(cat.getName());
            rb.setToggleGroup(categoryGroup);
            rb.setUserData(cat);
            rb.setStyle("-fx-text-fill: #555;");
            categoriesContainer.getChildren().add(rb);
        }
    }

    private void loadTags() {
        String[] tags = {"Eco-Friendly", "Conservation", "Climate Change", "Nature", "Green Living"};
        for (String tag : tags) {
            CheckBox cb = new CheckBox(tag);
            cb.setStyle("-fx-text-fill: #555;");
            tagsContainer.getChildren().add(cb);
        }
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Hero Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            selectedImagePath = selectedFile.toURI().toString();
            imagePreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    private void saveArticle() {
        String title = titleField.getText();
        String content = contentEditor.getHtmlText();
        
        if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter an article title.");
            alert.show();
            return;
        }

        Blog blog = (editArticle != null) ? editArticle : new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setImage(selectedImagePath);
        
        RadioButton selectedCat = (RadioButton) categoryGroup.getSelectedToggle();
        if (selectedCat != null) {
            blog.setCategory((Category) selectedCat.getUserData());
        }

        if (editArticle != null) {
            blogService.update(blog);
        } else {
            blogService.add2(blog);
        }
        
        // Go back to Articles
        goToArticles();
    }

    @FXML
    private void goToArticles() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/ArticlesManagement.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
