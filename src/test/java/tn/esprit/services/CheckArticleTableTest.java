package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckArticleTableTest {
    @Test
    public void check() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "article", null);
            System.out.println("--- COLUMNS OF article TABLE ---");
            while (rs.next()) {
                System.out.println("Col: " + rs.getString("COLUMN_NAME") + " | Type: " + rs.getString("TYPE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
