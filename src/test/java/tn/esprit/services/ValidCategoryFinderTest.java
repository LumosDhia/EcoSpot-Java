package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class ValidCategoryFinderTest {
    @Test
    public void find() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id FROM category LIMIT 1");
            if (rs.next()) {
                System.out.println("VALID CATEGORY ID: " + rs.getInt(1));
            } else {
                System.out.println("NO CATEGORIES FOUND!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
