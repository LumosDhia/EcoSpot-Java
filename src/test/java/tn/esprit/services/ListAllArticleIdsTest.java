package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class ListAllArticleIdsTest {
    @Test
    public void list() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, title FROM article ORDER BY id DESC");
            System.out.println("ALL ARTICLES:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | " + rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
