package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class ArticleCountDebugTest {
    @Test
    public void test() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article");
            if (rs.next()) System.out.println("BEFORE: " + rs.getInt(1));
            
            // Try a very simple insert
            try {
                cnx.createStatement().executeUpdate("INSERT INTO article (title, content, category_id) VALUES ('TEMP_TEST', 'CONTENT', 1)");
            } catch (SQLException e) {
                System.out.println("INSERT FAILED: " + e.getMessage());
            }
            
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article");
            if (rs2.next()) System.out.println("AFTER: " + rs2.getInt(1));
            
            // Check for the temp test article
            ResultSet rs3 = cnx.createStatement().executeQuery("SELECT id, title FROM article WHERE title = 'TEMP_TEST'");
            if (rs3.next()) {
                System.out.println("FOUND TEMP_TEST WITH ID: " + rs3.getInt(1));
            } else {
                System.out.println("TEMP_TEST NOT FOUND!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
