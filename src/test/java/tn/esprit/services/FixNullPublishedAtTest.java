package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class FixNullPublishedAtTest {
    @Test
    public void fix() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            String sql = "UPDATE article SET published_at = created_at WHERE published_at IS NULL";
            int rows = cnx.createStatement().executeUpdate(sql);
            System.out.println("Successfully updated " + rows + " articles with missing publication dates.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
