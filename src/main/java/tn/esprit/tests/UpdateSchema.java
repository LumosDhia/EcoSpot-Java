package tn.esprit.tests;

import tn.esprit.util.MyConnection;
import java.sql.Connection;
import java.sql.Statement;

public class UpdateSchema {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement()) {
            // 1. Add admin_revision_note column if it doesn't exist
            System.out.println("Adding admin_revision_note column...");
            try {
                st.execute("ALTER TABLE article ADD COLUMN admin_revision_note TEXT NULL");
            } catch (Exception e) {
                System.out.println("Column probably already exists or error: " + e.getMessage());
            }

            // 2. Create comment table
            System.out.println("Creating comment table...");
            st.execute("CREATE TABLE IF NOT EXISTS comment (" +
                       "id INT AUTO_INCREMENT PRIMARY KEY, " +
                       "article_id INT NOT NULL, " +
                       "author_id VARCHAR(32) DEFAULT NULL, " + // UNHEX ID format in web
                       "content TEXT NOT NULL, " +
                       "created_at DATETIME NOT NULL, " +
                       "author_name VARCHAR(255) DEFAULT 'Anonymous', " +
                       "FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE" +
                       ") ENGINE=InnoDB;");

            System.out.println("Database schema updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
