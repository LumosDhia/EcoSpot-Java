package tn.esprit.event;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private String name;
    private String slug;
    private String description;
    private int capacity;
    private String location;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String image;
    private double latitude;
    private double longitude;
    private java.util.List<Sponsor> sponsors = new java.util.ArrayList<>();

    public Event() {}

    public Event(int id, String name, String slug, String description, int capacity, String location, LocalDateTime startedAt, LocalDateTime endedAt, String image) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.capacity = capacity;
        this.location = location;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.image = image;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public java.util.List<Sponsor> getSponsors() { return sponsors; }
    public void setSponsors(java.util.List<Sponsor> sponsors) { this.sponsors = sponsors; }
}
