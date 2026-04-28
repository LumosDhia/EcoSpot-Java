package tn.esprit.user;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String role;
    private LocalDateTime timeoutUntil;
    private String avatarStyle;
    private String address;
    private String city;
    private String zipcode;

    public User() {}

    public User(int id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(int id, String username, String email, String password, String role, LocalDateTime timeoutUntil) {
        this(id, username, email, password, role);
        this.timeoutUntil = timeoutUntil;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getTimeoutUntil() { return timeoutUntil; }
    public void setTimeoutUntil(LocalDateTime timeoutUntil) { this.timeoutUntil = timeoutUntil; }

    public boolean isTimedOut() {
        return timeoutUntil != null && timeoutUntil.isAfter(LocalDateTime.now());
    }

    public String getAvatarStyle() { return avatarStyle; }
    public void setAvatarStyle(String avatarStyle) { this.avatarStyle = avatarStyle; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
}
