package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.services.WeatherService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class TicketDetailController {

    @FXML private Label statusBadge;
    @FXML private Label priorityBadge;
    @FXML private Label titleLabel;
    @FXML private Label domainLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView ticketImageView;
    @FXML private HBox authLinks;
    @FXML private HBox userLinks;
    @FXML private HBox completionBox;
    @FXML private VBox consigneBox;
    @FXML private VBox aiInsightsBox;
    @FXML private Label aiCategoryLabel;
    @FXML private Label aiNgoLabel;
    @FXML private Label aiSpamLabel;
    @FXML private VBox consignesDisplayContainer;
    @FXML private VBox weatherBox;
    @FXML private HBox forecastContainer;
    @FXML private Button dashboardTopBtn;

    private Ticket currentTicket;
    private final TicketService ticketService = new TicketService();
    private final WeatherService weatherService = new WeatherService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        tn.esprit.util.NavigationHistory.track(titleLabel, "/ticket/TicketDetail.fxml");
        // Session Management
        if (tn.esprit.util.SessionManager.isLoggedIn()) {
            authLinks.setVisible(false);
            authLinks.setManaged(false);
            userLinks.setVisible(true);
            userLinks.setManaged(true);
            
            tn.esprit.user.User user = tn.esprit.util.SessionManager.getCurrentUser();
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                dashboardTopBtn.setText("📊 Admin Dashboard");
            } else if (user.getRole().equalsIgnoreCase("NGO")) {
                dashboardTopBtn.setText("📊 NGO Dashboard");
            } else {
                dashboardTopBtn.setText("📊 My Dashboard");
            }
        } else {
            authLinks.setVisible(true);
            authLinks.setManaged(true);
            userLinks.setVisible(false);
            userLinks.setManaged(false);
        }
        // Completion submission must happen only from TicketManagement panel.
        completionBox.setVisible(false);
        completionBox.setManaged(false);
    }

    public void setTicket(Ticket t) {
        this.currentTicket = t;
        
        titleLabel.setText(t.getTitle());
        descriptionLabel.setText(t.getDescription());
        locationLabel.setText("📍 " + t.getLocation());
        domainLabel.setText("🏢 Domain: " + (t.getDomain() != null ? t.getDomain().name() : "Other"));
        dateLabel.setText("Created " + (t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "Unknown"));
        statusBadge.setText(t.getStatus().name());
        priorityBadge.setText(t.getPriority().name());

        // Handle AI Insights display
        boolean hasAiData = false;
        
        if (t.isSpam() && t.getSpamReason() != null && !t.getSpamReason().isEmpty()) {
            aiSpamLabel.setText("Reason: " + t.getSpamReason());
            aiSpamLabel.setVisible(true);
            aiSpamLabel.setManaged(true);
            
            aiCategoryLabel.setVisible(false);
            aiCategoryLabel.setManaged(false);
            aiNgoLabel.setVisible(false);
            aiNgoLabel.setManaged(false);
            hasAiData = true;
        } else {
            aiSpamLabel.setVisible(false);
            aiSpamLabel.setManaged(false);
            
            if (t.getAiCategory() != null && !t.getAiCategory().isEmpty()) {
                aiCategoryLabel.setText("Suggested Category: " + t.getAiCategory());
                aiCategoryLabel.setVisible(true);
                aiCategoryLabel.setManaged(true);
                hasAiData = true;
            }
            if (t.getAiSuggestedNgo() != null && !t.getAiSuggestedNgo().isEmpty()) {
                aiNgoLabel.setText("Recommended Routing: " + t.getAiSuggestedNgo());
                aiNgoLabel.setVisible(true);
                aiNgoLabel.setManaged(true);
                hasAiData = true;
            }
        }
        
        if (hasAiData) {
            aiInsightsBox.setVisible(true);
            aiInsightsBox.setManaged(true);
        } else {
            aiInsightsBox.setVisible(false);
            aiInsightsBox.setManaged(false);
        }

        consignesDisplayContainer.getChildren().clear();
        if (t.getConsignes() != null && !t.getConsignes().isEmpty()) {
            consigneBox.setVisible(true);
            consigneBox.setManaged(true);
            for (Consigne c : t.getConsignes()) {
                Label l = new Label("• " + c.getText());
                l.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 15px;");
                l.setWrapText(true);
                consignesDisplayContainer.getChildren().add(l);
            }
        } else {
            consigneBox.setVisible(false);
            consigneBox.setManaged(false);
        }

        loadWeather(t);

        // Priority Badge styling
        priorityBadge.getStyleClass().removeAll("ticket-badge-low", "ticket-badge-medium", "ticket-badge-high");
        if (t.getPriority() == TicketPriority.LOW) priorityBadge.getStyleClass().add("ticket-badge-low");
        else if (t.getPriority() == TicketPriority.MEDIUM) priorityBadge.getStyleClass().add("ticket-badge-medium");
        else priorityBadge.getStyleClass().add("ticket-badge-high");

        // Load image if path exists
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                String imgPath = t.getImage();
                if (imgPath.startsWith("/uploads/")) {
                    // Prepend standard local Symfony dev server port
                    imgPath = "http://127.0.0.1:8000" + imgPath;
                }
                
                System.out.println("Loading ticket image: " + imgPath);
                
                // Load in background (true)
                Image img = new Image(imgPath, true); 
                ticketImageView.setImage(img);
                ticketImageView.setManaged(true);
                ticketImageView.setVisible(true);
                
                // Fallback mechanism if 8000 isn't available
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        System.out.println("Warning: HTTP 8000 failed. Trying Apache localhost...");
                        try {
                            String fallback = "http://localhost/ecospot-web/public" + t.getImage();
                            if (t.getImage().startsWith("http")) fallback = t.getImage(); // Just in case it's absolute
                            Image fallbackImg = new Image(fallback, true);
                            ticketImageView.setImage(fallbackImg);
                            
                            fallbackImg.errorProperty().addListener((o, oldV, newV) -> {
                                if (newV) {
                                    System.out.println("All image loading attempts failed. Hiding image view.");
                                    ticketImageView.setManaged(false);
                                    ticketImageView.setVisible(false);
                                }
                            });
                        } catch (Exception ex) {
                            ticketImageView.setManaged(false);
                            ticketImageView.setVisible(false);
                        }
                    }
                });
                
            } catch (Exception e) {
                ticketImageView.setManaged(false);
                ticketImageView.setVisible(false);
            }
        } else {
            ticketImageView.setManaged(false);
            ticketImageView.setVisible(false);
        }
    }

    private void loadWeather(Ticket t) {
        if (t.getLatitude() == 0 && t.getLongitude() == 0) {
            weatherBox.setVisible(false);
            weatherBox.setManaged(false);
            return;
        }

        weatherBox.setVisible(true);
        weatherBox.setManaged(true);
        forecastContainer.getChildren().clear();
        
        Label loading = new Label("Loading weather forecast...");
        loading.setStyle("-fx-text-fill: #0ea5e9;");
        forecastContainer.getChildren().add(loading);

        new Thread(() -> {
            java.util.List<WeatherService.ForecastDay> forecast = weatherService.getWeeklyForecast(t.getLatitude(), t.getLongitude());
            Platform.runLater(() -> {
                forecastContainer.getChildren().clear();
                if (forecast.isEmpty()) {
                    forecastContainer.getChildren().add(new Label("Failed to load weather."));
                } else {
                    for (WeatherService.ForecastDay day : forecast) {
                        forecastContainer.getChildren().add(createForecastCard(day));
                    }
                }
            });
        }).start();
    }

    private VBox createForecastCard(WeatherService.ForecastDay day) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #e0f2fe; -fx-min-width: 100;");

        Label dateLbl = new Label(day.getDate().substring(5)); // Show MM-DD
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label iconLbl = new Label(getWeatherEmoji(day.getWeatherCode()));
        iconLbl.setStyle("-fx-font-size: 24px;");

        Label tempLbl = new Label(Math.round(day.getTempMax()) + "° / " + Math.round(day.getTempMin()) + "°");
        tempLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label descLbl = new Label(day.getDescription());
        descLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        descLbl.setWrapText(true);
        descLbl.setAlignment(javafx.geometry.Pos.CENTER);

        card.getChildren().addAll(dateLbl, iconLbl, tempLbl, descLbl);
        return card;
    }

    private String getWeatherEmoji(int code) {
        if (code == 0) return "☀️";
        if (code >= 1 && code <= 3) return "☁️";
        if (code >= 45 && code <= 48) return "🌫️";
        if (code >= 51 && code <= 67) return "🌧️";
        if (code >= 71 && code <= 77) return "❄️";
        if (code >= 80 && code <= 82) return "🌦️";
        if (code >= 85 && code <= 86) return "🌨️";
        if (code >= 95) return "⛈️";
        return "❓";
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
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

    @FXML
    void handleLogout(ActionEvent event) {
        tn.esprit.util.SessionManager.logout();
        goToHome();
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        navigate(event, "/user/Login.fxml");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        navigate(event, "/user/Register.fxml");
    }

    @FXML
    private void goToHome() {
        navigate(null, "/home/Home.fxml");
    }
    
    @FXML
    private void goToTickets(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            navigate(event, "/ticket/TicketManagement.fxml");
        }
    }
    
    @FXML
    private void goToBlog(ActionEvent event) {
        navigate(event, "/blog/BlogManagement.fxml");
    }

    @FXML
    private void goToAchievements(ActionEvent event) {
        navigate(event, "/ticket/Achievements.fxml");
    }

    @FXML
    private void goToEvents(ActionEvent event) {
        navigate(event, "/event/EventManagement.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) titleLabel.getScene().getWindow();
            }
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
