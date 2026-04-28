package tn.esprit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateSchema {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/projetdev";
        String user = "root";
        String password = "root";
        
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
