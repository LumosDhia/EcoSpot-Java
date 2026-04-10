package tn.esprit.event;

import java.util.Date;

public class Event {
    private int id;
    private String name;
    private String location;
    private Date eventDate;

    public Event() {}

    public Event(int id, String name, String location, Date eventDate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.eventDate = eventDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
}
