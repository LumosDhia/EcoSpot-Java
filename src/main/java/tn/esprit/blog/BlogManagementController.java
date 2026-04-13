package tn.esprit.blog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import tn.esprit.services.BlogService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class BlogManagementController {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane articlesGrid;
    @FXML private Button homeBtn;

    private BlogService blogService = new BlogService();
    private List<Blog> allBlogs;

    @FXML
    public void initialize() {
        // Init sort choices
        sortChoice.setItems(FXCollections.observableArrayList("Newest", "Oldest", "Most Viewed"));
        sortChoice.setValue("Newest");

        // Load data
        refreshData();

        // Listen for search changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplay();
        });

        // Listen for sort changes
        sortChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplay();
        });
    }

    private void refreshData() {
        allBlogs = blogService.getAll();
        // If DB is empty, add some mock data for demonstration as requested "like this"
        if (allBlogs.isEmpty()) {
            addMockData();
        }
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String query = searchField.getText().toLowerCase();
        List<Blog> filtered = allBlogs.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) || b.getContent().toLowerCase().contains(query))
                .collect(Collectors.toList());

        // Sorting
        String sort = sortChoice.getValue();
        if ("Newest".equals(sort)) {
            filtered.sort((b1, b2) -> b2.getPublishedAt().compareTo(b1.getPublishedAt()));
        } else if ("Oldest".equals(sort)) {
            filtered.sort((b1, b2) -> b1.getPublishedAt().compareTo(b2.getPublishedAt()));
        } else if ("Most Viewed".equals(sort)) {
            filtered.sort((b1, b2) -> Integer.compare(b2.getViews(), b1.getViews()));
        }

        displayBlogs(filtered);
    }

    private void displayBlogs(List<Blog> blogs) {
        articlesGrid.getChildren().clear();
        for (Blog b : blogs) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog/BlogCard.fxml"));
                Node card = loader.load();
                BlogCardController controller = loader.getController();
                controller.setData(b);
                articlesGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMockData() {
        Category cat1 = new Category(1, "Waste Management");
        Category cat2 = new Category(2, "Climate");
        Category cat3 = new Category(3, "Sustainability");
        Category cat4 = new Category(4, "Conservation");

        allBlogs.add(new Blog(1, "Innovating the Recycling Process for a Circular Economy", 
            "Transitioning to a circular economy requires a fundamental change in how we perceive and handle waste...", 
            "Admin User", "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=800", java.time.LocalDateTime.now().minusDays(7), cat1));
        
        allBlogs.add(new Blog(2, "The Role of Reforestation in Carbon Sequestration", 
            "Reforestation is one of the most effective natural solutions we have to combat climate change. By...", 
            "Admin User", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800", java.time.LocalDateTime.now().minusDays(11), cat2));

        allBlogs.add(new Blog(3, "Urban Gardening: From Balconies to Community Hubs", 
            "The rise of urban gardening is transforming barren city rooftops and concrete balconies into...", 
            "Alice User", "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=800", java.time.LocalDateTime.now().minusDays(17), cat3));

        allBlogs.add(new Blog(4, "Marine Ecosystems: Protecting the Ocean's Biodiversity", 
            "Our oceans cover more than 70% of the Earth's surface and harbor an incredible diversity of life, yet they...", 
            "Bob User", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800", java.time.LocalDateTime.now().minusDays(20), cat4));
            
        allBlogs.get(2).setViews(585);
        allBlogs.get(3).setViews(174);
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/Home.fxml"));
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
