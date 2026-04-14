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
import tn.esprit.user.User;

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
    @FXML private TextField newCategoryField;
    @FXML private TextField newTagField;
    @FXML private VBox revisionAlertBox;
    @FXML private Label revisionNoteLabel;

    private BlogService blogService = new BlogService();
    private CategoryService categoryService = new CategoryService();
    private String selectedImagePath = "";
    private ToggleGroup categoryGroup = new ToggleGroup();
    private Blog editArticle = null;
    private TagService tagService = new TagService();
    private boolean ngoRevisionResubmission = false;

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(titleField, "/blog/NewArticle.fxml");
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
        
        ngoRevisionResubmission = isNgoRevisionResubmission(blog);
        if (ngoRevisionResubmission) {
            publicationChoice.setValue("Save as draft");
            publicationChoice.setDisable(true);
        } else {
            publicationChoice.setDisable(false);
            publicationChoice.setValue(blog.getIsPublished() ? "Publish immediately" : "Save as draft");
        }
        
        if (blog.getAdminRevisionNote() != null && !blog.getAdminRevisionNote().trim().isEmpty()) {
            revisionAlertBox.setVisible(true);
            revisionAlertBox.setManaged(true);
            revisionNoteLabel.setText(blog.getAdminRevisionNote());
        } else {
            revisionAlertBox.setVisible(false);
            revisionAlertBox.setManaged(false);
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
        categoriesContainer.getChildren().clear();
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

        // Validation Logic (Controle de Saisir)
        RadioButton selectedCat = (RadioButton) categoryGroup.getSelectedToggle();
        String validationError = ArticleInputValidator.validate(title, content, selectedCat != null);
        if (validationError != null) {
            showAlert("Validation Error", validationError);
            return;
        }

        Blog blog = (editArticle != null) ? editArticle : new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setImage(selectedImagePath);
        blog.setCategory((Category) selectedCat.getUserData());
        
        // Set publication status
        boolean publishRequested = "Publish immediately".equals(publicationChoice.getValue());
        if (publishRequested && (selectedImagePath == null || selectedImagePath.trim().isEmpty())) {
            showAlert("Validation Error", "An image is required before publishing an article.");
            return;
        }
        if (ngoRevisionResubmission) {
            // NGO resubmission after revision request must return to admin review queue.
            blog.setIsPublished(false);
            blog.setAdminRevisionNote(null);
        } else {
            blog.setIsPublished(publishRequested);
        }

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

    @FXML
    private void addCategoryFromInput() {
        createCategoryFromInputIfNeeded();
    }

    @FXML
    private void addTagsFromInput() {
        createTagsFromInputIfNeeded();
    }

    private boolean createCategoryFromInputIfNeeded() {
        if (newCategoryField == null) return true;
        String value = newCategoryField.getText();
        if (value == null || value.trim().isEmpty()) return true;

        Category created = categoryService.createIfMissing(value);
        if (created == null) {
            showAlert("Validation Error", "Invalid category. It must be 2-40 characters and start with a letter.");
            return false;
        }

        loadCategories();
        categoryGroup.getToggles().stream()
                .filter(t -> {
                    Object data = t.getUserData();
                    return data instanceof Category && ((Category) data).getId() == created.getId();
                })
                .findFirst()
                .ifPresent(t -> t.setSelected(true));
        newCategoryField.clear();
        return true;
    }

    private boolean createTagsFromInputIfNeeded() {
        if (newTagField == null) return true;
        String raw = newTagField.getText();
        if (raw == null || raw.trim().isEmpty()) return true;

        String[] parts = raw.split(",");
        List<Integer> createdOrMatchedIds = new ArrayList<>();
        List<String> invalidTags = new ArrayList<>();
        for (String p : parts) {
            String token = p == null ? "" : p.trim();
            if (token.isEmpty()) continue;
            Tag t = tagService.createIfMissing(token);
            if (t != null) {
                createdOrMatchedIds.add(t.getId());
            } else {
                invalidTags.add(token);
            }
        }
        if (!invalidTags.isEmpty()) {
            showAlert(
                    "Validation Error",
                    "Invalid tag(s): " + String.join(", ", invalidTags) +
                            ". Each tag must be 2-30 characters and start with a letter."
            );
            return false;
        }

        loadTags();
        for (javafx.scene.Node node : tagsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                Object data = cb.getUserData();
                if (data instanceof Tag) {
                    Tag tag = (Tag) data;
                    if (createdOrMatchedIds.contains(tag.getId())) {
                        cb.setSelected(true);
                    }
                }
            }
        }
        newTagField.clear();
        return true;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isNgoRevisionResubmission(Blog blog) {
        if (blog == null) return false;
        User currentUser = tn.esprit.util.SessionManager.getCurrentUser();
        boolean isNgo = currentUser != null && "NGO".equalsIgnoreCase(currentUser.getRole());
        boolean hasRevisionNote = blog.getAdminRevisionNote() != null && !blog.getAdminRevisionNote().trim().isEmpty();
        return isNgo && hasRevisionNote;
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
    private void goBackToArticles(javafx.event.ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToArticles();
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

    @FXML
    private void goToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null) {
                controller.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToEvents(javafx.event.ActionEvent event) {
        navigate(event, "/event/EventManagement.fxml");
    }

    @FXML
    private void goToTickets(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/TicketManagement.fxml");
    }

    @FXML
    private void goToAchievements(javafx.event.ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    private void navigate(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMinimize() {
        tn.esprit.util.WindowUtils.minimize(titleField);
    }

    @FXML
    private void handleMaximize() {
        tn.esprit.util.WindowUtils.toggleFullScreen(titleField);
    }

    @FXML
    private void handleClose() {
        tn.esprit.util.WindowUtils.close(titleField);
    }
}
