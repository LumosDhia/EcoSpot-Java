package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckViewColumnsTest {
    @Test
    public void check() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "article_view_event", null);
            System.out.println("--- COLUMNS OF article_view_event ---");
            while (rs.next()) {
                System.out.println("Column: " + rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
