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
import org.json.JSONObject;
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
    private final String FACE_SERVICE_URL = sanitizeUrl(Config.get("FACE_SERVICE_URL", "http://localhost:8001"));

    private String sanitizeUrl(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    private boolean enrollMode = false;
    private String enrollEmail = null;

    @FXML
    public void initialize() {
        startWebcam();
        checkServiceStatus();
        
        // Ensure camera stops if window is closed via X button
        Platform.runLater(() -> {
            if (webcamView.getScene() != null && webcamView.getScene().getWindow() != null) {
                webcamView.getScene().getWindow().setOnCloseRequest(e -> stopCamera.set(true));
            }
        });
    }

    private void checkServiceStatus() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACE_SERVICE_URL + "/health"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Face Service: ONLINE");
                            statusLabel.setStyle("-fx-text-fill: #2d6a4f;");
                            scanBtn.setDisable(false);
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("Face Service: OFFLINE (Run run_face_service.bat)");
                        statusLabel.setStyle("-fx-text-fill: #e53e3e;");
                        scanBtn.setDisable(true);
                    });
                    return null;
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
        statusLabel.setText("Processing...");

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
        JSONObject json = new JSONObject();
        json.put("image", base64Image);
        json.put("user_id", enrollEmail);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACE_SERVICE_URL + "/enroll"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            statusLabel.setText("Face Enrolled Successfully!");
                            statusLabel.setStyle("-fx-text-fill: #2d6a4f;");
                            new Thread(() -> {
                                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                                Platform.runLater(() -> closeWindow(event));
                            }).start();
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(response.body());
                                showError("Error: " + errorJson.optString("detail", "Enrollment failed"));
                            } catch (Exception e) {
                                showError("Enrollment failed: " + response.body());
                            }
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
        JSONObject json = new JSONObject();
        json.put("image", base64Image);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FACE_SERVICE_URL + "/recognize"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            processResponse(response.body(), event);
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(response.body());
                                showError(errorJson.optString("detail", "Recognition failed"));
                            } catch (Exception e) {
                                showError("Recognition failed.");
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Face service unavailable. Run face_service/main.py");
                    });
                    return null;
                });
    }

    private void processResponse(String responseBody, ActionEvent event) {
        try {
            JSONObject response = new JSONObject(responseBody);
            String userId = response.getString("user_id");
            statusLabel.setText("Welcome " + userId + "!");
            statusLabel.setStyle("-fx-text-fill: #2d6a4f;");
            
            User user = userService.getUserByEmail(userId);
            if (user != null) {
                tn.esprit.util.SessionManager.login(user);
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> closeWindow(event));
                }).start();
            } else {
                showError("User found in Face ID but not in Database.");
            }
        } catch (Exception e) {
            showError("Invalid response from face service.");
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
