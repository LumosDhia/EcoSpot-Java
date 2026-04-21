package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class FinalCheckTest {
    @Test
    public void checkAll() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- article_reaction_event SCHEMA ---");
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "article_reaction_event", null);
            while (rs.next()) {
                System.out.println("Col: " + rs.getString("COLUMN_NAME") + " | Type: " + rs.getString("TYPE_NAME"));
            }

            System.out.println("\n--- REACTION DATA COUNT ---");
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_event");
            if (rs2.next()) System.out.println("Total Rows: " + rs2.getInt(1));

            System.out.println("\n--- REACTION DATA SAMPLE ---");
            ResultSet rs3 = cnx.createStatement().executeQuery("SELECT * FROM article_reaction_event LIMIT 5");
            ResultSetMetaData rsmd = rs3.getMetaData();
            int colCount = rsmd.getColumnCount();
            while (rs3.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    sb.append(rsmd.getColumnName(i)).append(": ").append(rs3.getString(i)).append(" | ");
                }
                System.out.println(sb.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
