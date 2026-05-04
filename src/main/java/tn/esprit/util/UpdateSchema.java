package tn.esprit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateSchema {
    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "jdbc:mysql://localhost:3308/projetdev";
        String user = args.length > 1 ? args[1] : "root";
        String password = args.length > 2 ? args[2] : "root";
        if ("EMPTY".equals(password)) password = "";
        
        System.out.println("Connecting to: " + url + " as " + user + " (pass: " + (password.isEmpty() ? "NO" : "YES") + ")");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "ALTER TABLE user ADD COLUMN timeout_until DATETIME DEFAULT NULL";
            stmt.executeUpdate(sql);
            System.out.println("Column 'timeout_until' added successfully!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            // If column already exists, it's fine
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("Column already exists.");
            } else {
                e.printStackTrace();
            }
        }
    }
}
