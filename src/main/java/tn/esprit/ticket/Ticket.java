package tn.esprit.ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private int id;
    private String title;
    private String description;
    private String location;
    private String image;
    private TicketStatus status;
    private TicketPriority priority;
    private ActionDomain domain;
    private double latitude;
    private double longitude;
    private int userId;
    private Integer assignedNgoId;
    private boolean isSpam;
    private LocalDateTime createdAt;

    private String adminNotes;
    private Integer completedById;
    private String completionMessage;
    private String completionImage;
    private LocalDateTime achievedAt;
    private List<Consigne> consignes = new ArrayList<>();

    public Ticket() {
        this.status = TicketStatus.PENDING;
        this.priority = TicketPriority.MEDIUM;
        this.createdAt = LocalDateTime.now();
    }

    // New getters/setters
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public Integer getCompletedById() { return completedById; }
    public void setCompletedById(Integer completedById) { this.completedById = completedById; }
    public String getCompletionMessage() { return completionMessage; }
    public void setCompletionMessage(String completionMessage) { this.completionMessage = completionMessage; }
    public String getCompletionImage() { return completionImage; }
    public void setCompletionImage(String completionImage) { this.completionImage = completionImage; }
    public LocalDateTime getAchievedAt() { return achievedAt; }
    public void setAchievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; }

    public Ticket(int id, String title, String description, String location, double latitude, double longitude, int userId) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public ActionDomain getDomain() { return domain; }
    public void setDomain(ActionDomain domain) { this.domain = domain; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getAssignedNgoId() { return assignedNgoId; }
    public void setAssignedNgoId(Integer assignedNgoId) { this.assignedNgoId = assignedNgoId; }
    public boolean isSpam() { return isSpam; }
    public void setSpam(boolean spam) { isSpam = spam; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Consigne> getConsignes() { return consignes; }
    public void setConsignes(List<Consigne> consignes) { this.consignes = consignes; }
}
