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
import tn.esprit.services.TagService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private TagService tagService = new TagService();

    @FXML
    public void initialize() {
        publicationChoice.getItems().addAll("Save as draft", "Publish immediately");
        publicationChoice.setValue("Save as draft");
        
        loadCategories();
        loadTags();

        // Fix ScrollPane jumping when space is pressed in HTMLEditor
        contentEditor.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                event.consume();
            }
        });
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
        
        // Tags pre-selection
        if (blog.getTags() != null) {
            for (javafx.scene.Node node : tagsContainer.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    Tag tag = (Tag) cb.getUserData();
                    if (blog.getTags().stream().anyMatch(t -> t.getId() == tag.getId())) {
                        cb.setSelected(true);
                    }
                }
            }
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
        tagsContainer.getChildren().clear();
        List<Tag> tags = tagService.getAll();
        for (Tag tag : tags) {
            CheckBox cb = new CheckBox(tag.getName());
            cb.setUserData(tag);
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
        
        // --- Validation Logic (Controle de Saisir) ---
        
        // 1. Title Validation
        if (title == null || title.trim().isEmpty()) {
            showAlert("Validation Error", "Title is required.");
            return;
        }
        if (title.length() < 5 || title.length() > 100) {
            showAlert("Validation Error", "Title must be between 5 and 100 characters.");
            return;
        }
        if (!title.matches(".*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*")) {
            showAlert("Validation Error", "Title must contain at least 5 letters.");
            return;
        }
        if (!title.substring(0, 1).matches("[a-zA-Z]")) {
            showAlert("Validation Error", "Title must start with a letter.");
            return;
        }

        // 2. Content Validation
        // Clean HTML tags to count actual text length for meaningful validation
        String plainText = content.replaceAll("<[^>]*>", "").trim();
        if (plainText.isEmpty()) {
            showAlert("Validation Error", "Content is required.");
            return;
        }
        if (plainText.length() < 20) {
            showAlert("Validation Error", "The article content must be more detailed (at least 20 characters).");
            return;
        }
        if (!plainText.matches(".*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*")) {
            showAlert("Validation Error", "The article content must contain at least 5 letters.");
            return;
        }

        // 3. Category Validation
        RadioButton selectedCat = (RadioButton) categoryGroup.getSelectedToggle();
        if (selectedCat == null) {
            showAlert("Validation Error", "Please select a category.");
            return;
        }

        // --- End Validation ---

        Blog blog = (editArticle != null) ? editArticle : new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setImage(selectedImagePath);
        blog.setCategory((Category) selectedCat.getUserData());

        // Extract Tags
        List<Tag> selectedTags = new ArrayList<>();
        for (javafx.scene.Node node : tagsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    selectedTags.add((Tag) cb.getUserData());
                }
            }
        }
        blog.setTags(selectedTags);

        if (editArticle != null) {
            blogService.update(blog);
        } else {
            blogService.add2(blog);
        }
        
        goToArticles();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    private void goToBlog() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/blog/BlogManagement.fxml"));
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
