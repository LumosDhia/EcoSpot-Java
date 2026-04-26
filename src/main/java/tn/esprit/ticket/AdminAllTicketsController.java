package tn.esprit.ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.services.UserService;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminAllTicketsController {

    @FXML private VBox ticketsListContainer;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Label userNameLabel;
    @FXML private ComboBox<String> statusFilter;

    private final TicketService ticketService = new TicketService();
    private final UserService userService = new UserService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<User> ngoUsers;

    @FXML
    public void initialize() {
        if (!isAdminUser()) {
            redirectToDashboard();
            return;
        }
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            userNameLabel.setText(SessionManager.getCurrentUser().getUsername());
        }
        tn.esprit.util.NavigationHistory.track(ticketsListContainer, "/ticket/AdminAllTickets.fxml");
        
        setupFilters();
        loadNgoUsers();
        loadAllTickets();
    }

    private void setupFilters() {
        statusFilter.getItems().add("ALL");
        for (TicketStatus s : TicketStatus.values()) {
            statusFilter.getItems().add(s.name());
        }
        statusFilter.setValue("ALL");
        statusFilter.setOnAction(e -> loadAllTickets());
    }

    private void loadNgoUsers() {
        ngoUsers = userService.getAllUsers().stream()
                .filter(u -> "NGO".equalsIgnoreCase(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
    }

    private void loadAllTickets() {
        ticketsListContainer.getChildren().clear();

        List<Ticket> all = ticketService.getAll();
        String filter = statusFilter.getValue();
        
        if (filter != null && !"ALL".equals(filter)) {
            all = all.stream()
                    .filter(t -> t.getStatus().name().equals(filter))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (all.isEmpty()) {
            Label emptyLbl = new Label("No tickets found.");
            emptyLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-padding: 30;");
            ticketsListContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Ticket t : all) {
            ticketsListContainer.getChildren().add(createTicketRow(t));
        }
    }

    private Node createTicketRow(Ticket t) {
        VBox row = new VBox(10);
        row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 16;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(t.getTitle() == null ? "(Untitled ticket)" : t.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label status = new Label(t.getStatus() != null ? t.getStatus().name() : "UNKNOWN");
        status.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(title, spacer);
        
        if (t.isSpam()) {
            Label spamBadge = new Label("🚨 SPAM");
            spamBadge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #fecaca; -fx-border-radius: 12;");
            top.getChildren().add(spamBadge);
        }
        
        top.getChildren().add(status);

        Label meta = new Label(
                "📍 " + (t.getLocation() == null ? "No location" : t.getLocation()) +
                        "    •    🕒 " + (t.getCreatedAt() == null ? "Unknown date" : t.getCreatedAt().format(formatter))
        );
        meta.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        if (t.getAssignedNgoId() != null && t.getAssignedNgoId() > 0) {
            User ngo = ngoUsers.stream().filter(u -> u.getId() == t.getAssignedNgoId()).findFirst().orElse(null);
            String ngoName = (ngo != null) ? ngo.getUsername() : "Unknown NGO";
            Label assignedLbl = new Label("🤝 Assigned to: " + ngoName);
            assignedLbl.setStyle("-fx-text-fill: #0369a1; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 0;");
            row.getChildren().add(assignedLbl);
        }

        Label desc = new Label(t.getDescription() == null ? "" : t.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openTicketDetail(t));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteTicket(t));

        if (t.isSpam() && t.getUserId() > 0) {
            Button timeoutBtn = new Button("Timeout User");
            timeoutBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            timeoutBtn.setOnAction(e -> {
                userService.updateTimeout(t.getUserId(), java.time.LocalDateTime.now().plusHours(24));
                new Alert(Alert.AlertType.INFORMATION, "User has been put in a 24-hour timeout.").show();
            });
            actions.getChildren().add(timeoutBtn);
        }

        actions.getChildren().addAll(deleteBtn, viewBtn);

        // Assignment Controls for Published tickets
        if (t.getStatus() == TicketStatus.PUBLISHED) {
            HBox assignBox = new HBox(10);
            assignBox.setAlignment(Pos.CENTER_LEFT);
            assignBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #e5e7eb transparent transparent transparent;");
            
            Label assignLbl = new Label("Assign to NGO:");
            assignLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5563;");
            
            ComboBox<User> ngoCombo = new ComboBox<>();
            ngoCombo.setPromptText("Choose NGO...");
            ngoCombo.getItems().addAll(ngoUsers);
            ngoCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<User>() {
                @Override protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getUsername());
                }
            });
            ngoCombo.setButtonCell(new javafx.scene.control.ListCell<User>() {
                @Override protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getUsername());
                }
            });

            Button assignBtn = new Button("Assign");
            assignBtn.setStyle("-fx-background-color: #0369a1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            assignBtn.setOnAction(e -> {
                User selectedNgo = ngoCombo.getValue();
                if (selectedNgo != null) {
                    t.setAssignedNgoId(selectedNgo.getId());
                    t.setStatus(TicketStatus.ASSIGNED);
                    ticketService.update(t);
                    loadAllTickets();
                }
            });

            assignBox.getChildren().addAll(assignLbl, ngoCombo, assignBtn);
            row.getChildren().addAll(top, meta, desc, actions, assignBox);
        } else {
            row.getChildren().addAll(top, meta, desc, actions);
        }
        
        return row;
    }

    private void openTicketDetail(Ticket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ticket/TicketDetail.fxml"));
            Parent root = loader.load();
            TicketDetailController controller = loader.getController();
            controller.setTicket(t);
            Stage stage = (Stage) ticketsListContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTicket(Ticket t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete ticket \"" + t.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                double currentScroll = mainScrollPane.getVvalue();
                ticketService.delete(t);
                loadAllTickets();
                javafx.application.Platform.runLater(() -> mainScrollPane.setVvalue(currentScroll));
            }
        });
    }

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            goToDashboard(event);
        }
    }

    @FXML
    void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && SessionManager.isLoggedIn()) {
                controller.setUser(SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAdminUser() {
        return SessionManager.isLoggedIn()
                && SessionManager.getCurrentUser() != null
                && "ADMIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
    }

    private void redirectToDashboard() {
        if (ticketsListContainer == null || ticketsListContainer.getScene() == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
            Parent root = loader.load();
            tn.esprit.user.DashboardController controller = loader.getController();
            if (controller != null && SessionManager.isLoggedIn()) {
                controller.setUser(SessionManager.getCurrentUser());
            }
            Stage stage = (Stage) ticketsListContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
