package tn.esprit.ticket;

public class Consigne {
    private int id;
    private String text;
    private int ticketId;

    public Consigne() {}

    public Consigne(String text) {
        this.text = text;
    }

    public Consigne(int id, String text, int ticketId) {
        this.id = id;
        this.text = text;
        this.ticketId = ticketId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
}
