package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class InstantCheckTest {
    @Test
    public void test() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // Insert
            cnx.createStatement().executeUpdate("INSERT INTO article (title, content, created_at, category_id) VALUES ('INSTANT_CHECK', 'CONTENT', NOW(), 1)");
            System.out.println("Inserted INSTANT_CHECK");
            
            // List
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, title FROM article WHERE title = 'INSTANT_CHECK'");
            if (rs.next()) {
                System.out.println("FOUND INSTANT_CHECK ID: " + rs.getInt(1));
            } else {
                System.out.println("NOT FOUND INSTANT_CHECK!");
            }
            
            // Check global count
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article");
            if (rs2.next()) System.out.println("GLOBAL COUNT: " + rs2.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
