package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckColumnsTest {
    @Test
    public void checkColumns() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "article_reaction_event", null);
            System.out.println("--- COLUMNS OF article_reaction_event ---");
            while (rs.next()) {
                System.out.println("Column: " + rs.getString("COLUMN_NAME"));
            }

            ResultSet rs2 = md.getColumns(null, null, "article", null);
            System.out.println("\n--- COLUMNS OF article ---");
            while (rs2.next()) {
                System.out.println("Column: " + rs2.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
