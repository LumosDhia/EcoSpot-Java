package tn.esprit.user;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.util.Config;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceLoginController {

    @FXML private ImageView webcamView;
    @FXML private Button scanBtn;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private Webcam webcam;
    private AtomicBoolean stopCamera = new AtomicBoolean(false);
    private UserService userService = new UserService();
    private final String FACE_SERVICE_URL = Config.get("FACE_SERVICE_URL", "http://localhost:8001");
    
    private boolean enrollMode = false;
    private String enrollEmail = null;

    @FXML
    public void initialize() {
        startWebcam();
        
        // Ensure camera stops if window is closed via X button
        Platform.runLater(() -> {
            if (webcamView.getScene() != null && webcamView.getScene().getWindow() != null) {
                webcamView.getScene().getWindow().setOnCloseRequest(e -> stopCamera.set(true));
            }
        });
    }

    public void setEnrollmentMode(String email) {
        this.enrollMode = true;
        this.enrollEmail = email;
        Platform.runLater(() -> {
            scanBtn.setText("Enroll Face");
            statusLabel.setText("Registering Face for: " + email);
        });
    }

    private void startWebcam() {
        Thread thread = new Thread(() -> {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(new Dimension(640, 480));
                webcam.open();
                
                Platform.runLater(() -> {
                    statusLabel.setText("Camera Ready");
                    scanBtn.setDisable(false);
                });

                while (!stopCamera.get()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                        Platform.runLater(() -> webcamView.setImage(fxImage));
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                webcam.close();
            } else {
                Platform.runLater(() -> {
                    statusLabel.setText("No camera found!");
                    statusLabel.setStyle("-fx-text-fill: #e53e3e;");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void handleScan(ActionEvent event) {
        if (webcam == null || !webcam.isOpen()) return;

        loadingIndicator.setVisible(true);
        scanBtn.setDisable(true);
        statusLabel.setText("Verifying face...");

        BufferedImage image = webcam.getImage();
        String base64Image = encodeImageToBase64(image);

        if (base64Image == null) {
            showError("Failed to capture image.");
            return;
        }

        if (enrollMode) {
            enrollFace(base64Image, event);
        } else {
            verifyFace(base64Image, event);
        }
    }

    private void enrollFace(String base64Image, ActionEvent event) {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"image\": \"" + base64Image + "\", \"user_id\": \"" + enrollEmail + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACE_SERVICE_URL + "/enroll"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.contains("\"status\":\"success\"") || response.contains("\"success\"")) {
                            statusLabel.setText("Face Enrolled Successfully!");
                            statusLabel.setStyle("-fx-text-fill: #2d6a4f;");
                            new Thread(() -> {
                                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                                Platform.runLater(() -> closeWindow(event));
                            }).start();
                        } else {
                            showError("Enrollment failed: " + response);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Face service unavailable."));
                    return null;
                });
    }

    private String encodeImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void verifyFace(String base64Image, ActionEvent event) {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"image\": \"" + base64Image + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACE_SERVICE_URL + "/recognize"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        processResponse(response, event);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Face service unavailable. Ensure port 8001 is running.");
                    });
                    return null;
                });
    }

    private void processResponse(String response, ActionEvent event) {
        // Simple JSON parsing (since we don't have a library like Jackson here yet, 
        // or we can use a regex/substring for simplicity in this specific case)
        if (response.contains("\"user_id\"") && !response.contains("\"user_id\":null")) {
            String userId = response.split("\"user_id\":\"")[1].split("\"")[0];
            statusLabel.setText("Welcome " + userId + "!");
            statusLabel.setStyle("-fx-text-fill: #2d6a4f;");
            
            // Perform login in our system
            User user = userService.getUserByEmail(userId);
            if (user != null) {
                tn.esprit.util.SessionManager.login(user);
                closeWindow(event);
                // Trigger navigation in LoginController or handle it here
                // For simplicity, we'll assume the main window will check session
            } else {
                showError("User found in Face ID but not in Database.");
            }
        } else {
            showError("Face not recognized.");
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e53e3e;");
        loadingIndicator.setVisible(false);
        scanBtn.setDisable(false);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        stopCamera.set(true);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
