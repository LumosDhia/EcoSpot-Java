package tn.esprit.event;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.services.EventService;
import tn.esprit.services.GeocodingService;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventManagementController {

    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private Button dashboardTopBtn;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoice;
    @FXML private FlowPane eventsGrid;
    @FXML private Button manageEventsBtn;
    @FXML private Button manageSponsorsBtn;

    // Pagination
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private HBox pageButtonsBox;
    @FXML private Label pageInfoLabel;

    private static final int PAGE_SIZE = 8;

    private final EventService eventService = new EventService();
    private final GeocodingService geocodingService = new GeocodingService();
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
        boolean isAdmin = false;
        boolean isNgo = false;

        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            if (authLinks != null) { authLinks.setVisible(false); authLinks.setManaged(false); }
            if (userLinks != null) { userLinks.setVisible(true); userLinks.setManaged(true); }

            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (dashboardTopBtn != null) {
                if (user.getRole().equalsIgnoreCase("ADMIN")) { dashboardTopBtn.setText("📊 Admin Dashboard"); isAdmin = true; }
                else if (user.getRole().equalsIgnoreCase("NGO")) { dashboardTopBtn.setText("📊 NGO Dashboard"); isNgo = true; }
                else { dashboardTopBtn.setText("📊 My Dashboard"); }
            }
        } else {
            if (authLinks != null) { authLinks.setVisible(true); authLinks.setManaged(true); }
            if (userLinks != null) { userLinks.setVisible(false); userLinks.setManaged(false); }
        }

        boolean canManage = isAdmin || isNgo;
        if (manageEventsBtn != null) { manageEventsBtn.setVisible(canManage); manageEventsBtn.setManaged(canManage); }
        if (manageSponsorsBtn != null) { manageSponsorsBtn.setVisible(isAdmin); manageSponsorsBtn.setManaged(isAdmin); }

        if (sortChoice != null) {
            sortChoice.setItems(FXCollections.observableArrayList("Next Events", "Nearby", "Capacity High", "Name A-Z"));
            sortChoice.setValue("Next Events");
            sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> { currentPage = 0; filterAndDisplay(); });
        }

        refreshData();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> { currentPage = 0; filterAndDisplay(); });
            tn.esprit.util.NavigationHistory.track(searchField, "/event/EventManagement.fxml");
        }
    }

    private void refreshData() {
        allEvents = eventService.getAll();
        currentPage = 0;
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        if (searchField == null || sortChoice == null || eventsGrid == null) return;
        String query = searchField.getText().toLowerCase();
        filteredEvents = allEvents.stream()
                .filter(e -> e.getName().toLowerCase().contains(query) || e.getLocation().toLowerCase().contains(query))
                .collect(Collectors.toList());

        String sort = sortChoice.getValue();
        if ("Next Events".equals(sort)) filteredEvents.sort((a, b) -> a.getStartedAt().compareTo(b.getStartedAt()));
        else if ("Capacity High".equals(sort)) filteredEvents.sort((a, b) -> Integer.compare(b.getCapacity(), a.getCapacity()));
        else if ("Name A-Z".equals(sort)) filteredEvents.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        else if ("Nearby".equals(sort)) {
            if (SessionManager.isLoggedIn()) {
                tn.esprit.user.User user = SessionManager.getCurrentUser();
                if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                    String addr = user.getAddress() + ", " + user.getCity();
                    List<GeocodingService.Place> places = geocodingService.search(addr);
                    if (!places.isEmpty()) {
                        double userLat = places.get(0).getLat();
                        double userLon = places.get(0).getLon();
                        filteredEvents.sort((a, b) -> {
                            double distA = geocodingService.calculateDistance(userLat, userLon, a.getLatitude(), a.getLongitude());
                            double distB = geocodingService.calculateDistance(userLat, userLon, b.getLatitude(), b.getLongitude());
                            return Double.compare(distA, distB);
                        });
                    }
                }
            }
        }

        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int totalPages = getTotalPages();
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredEvents.size());
        List<Event> pageEvents = filteredEvents.subList(from, to);

        eventsGrid.getChildren().clear();

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Event e : pageEvents) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/EventCard.fxml"));
                    Node card = loader.load();
                    EventCardController ctrl = loader.getController();
                    ctrl.setData(e, () -> Platform.runLater(() -> refreshData()));
                    Platform.runLater(() -> eventsGrid.getChildren().add(card));
                    Thread.sleep(5);
                }
                return null;
            }
        };
        new Thread(loadTask).start();

        updatePaginationControls(totalPages);
    }

    private void updatePaginationControls(int totalPages) {
        if (prevBtn == null) return;
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        int shown = Math.min((currentPage + 1) * PAGE_SIZE, filteredEvents.size());
        int from = filteredEvents.isEmpty() ? 0 : currentPage * PAGE_SIZE + 1;
        pageInfoLabel.setText(filteredEvents.isEmpty()
                ? "No events found"
                : String.format("Showing %d–%d of %d", from, shown, filteredEvents.size()));

        pageButtonsBox.getChildren().clear();
        for (int i = 0; i < totalPages; i++) {
            final int page = i;
            Button btn = new Button(String.valueOf(i + 1));
            btn.getStyleClass().add(i == currentPage ? "page-btn-active" : "page-btn");
            btn.setOnAction(e -> { currentPage = page; displayCurrentPage(); });
            pageButtonsBox.getChildren().add(btn);
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredEvents.size() / PAGE_SIZE);
    }

    @FXML private void goToPrevPage() { if (currentPage > 0) { currentPage--; displayCurrentPage(); } }
    @FXML private void goToNextPage() { if (currentPage < getTotalPages() - 1) { currentPage++; displayCurrentPage(); } }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController ctrl = loader.getController();
            if (ctrl != null) ctrl.setUser(tn.esprit.util.SessionManager.getCurrentUser());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goBack(ActionEvent event) { if (!tn.esprit.util.NavigationHistory.goBack(event)) goToDashboard(event); }

    @FXML void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        navigate(event, "/home/Home.fxml");
    }

    @FXML private void goToLogin(ActionEvent e) { navigate(e, "/user/Login.fxml"); }
    @FXML private void goToRegister(ActionEvent e) { navigate(e, "/user/Register.fxml"); }
    @FXML private void goToHome() { switchScene("/home/Home.fxml"); }
    @FXML private void goToTickets(ActionEvent e) { navigate(e, "/ticket/TicketManagement.fxml"); }
    @FXML private void goToBlog() { switchScene("/blog/BlogManagement.fxml"); }
    @FXML private void goToAchievements(ActionEvent e) { navigate(e, "/ticket/Achievements.fxml"); }
    @FXML private void goToAdmin() { switchScene("/event/EventAdmin.fxml"); }
    @FXML private void goToSponsors() { switchScene("/event/SponsorManagement.fxml"); }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (searchField != null && searchField.getScene() != null) stage = (Stage) searchField.getScene().getWindow();
            else return;
            stage.getScene().setRoot(root);
            tn.esprit.util.WindowUtils.makeDraggable(stage, root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleMinimize() { tn.esprit.util.WindowUtils.minimize(searchField); }
    @FXML private void handleMaximize() { tn.esprit.util.WindowUtils.toggleFullScreen(searchField); }
    @FXML private void handleClose() { tn.esprit.util.WindowUtils.close(searchField); }
}
