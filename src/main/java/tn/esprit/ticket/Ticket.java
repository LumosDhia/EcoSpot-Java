package tn.esprit.ticket;

import java.time.LocalDateTime;

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

    public Ticket() {
        this.status = TicketStatus.PENDING;
        this.priority = TicketPriority.MEDIUM;
        this.createdAt = LocalDateTime.now();
    }

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
}
