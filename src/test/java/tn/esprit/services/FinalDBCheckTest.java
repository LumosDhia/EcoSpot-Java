package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class FinalDBCheckTest {
    @Test
    public void check() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            int[] ids = {75, 97, 84, 109};
            for (int id : ids) {
                ResultSet rs = cnx.createStatement().executeQuery("SELECT id, title, published_at, created_at FROM article WHERE id = " + id);
                if (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") + " | Title: " + rs.getString("title") + " | Published: " + rs.getTimestamp("published_at") + " | Created: " + rs.getTimestamp("created_at"));
                } else {
                    System.out.println("ID: " + id + " NOT FOUND");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
