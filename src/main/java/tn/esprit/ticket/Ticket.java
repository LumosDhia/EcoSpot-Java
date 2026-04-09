package tn.esprit.ticket;

public class Ticket {
    private int id;
    private int eventId;
    private double price;
    private String type;

    public Ticket() {}

    public Ticket(int id, int eventId, double price, String type) {
        this.id = id;
        this.eventId = eventId;
        this.price = price;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
