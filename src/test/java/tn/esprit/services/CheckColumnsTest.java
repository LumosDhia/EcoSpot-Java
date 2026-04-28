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

            ResultSet rs3 = md.getColumns(null, null, "article_reaction_java", null);
            System.out.println("\n--- COLUMNS OF article_reaction_java ---");
            while (rs3.next()) {
                System.out.println("Column: " + rs3.getString("COLUMN_NAME"));
            }

            ResultSet rs4 = md.getColumns(null, null, "article_reaction", null);
            System.out.println("\n--- COLUMNS OF article_reaction ---");
            while (rs4.next()) {
                System.out.println("Column: " + rs4.getString("COLUMN_NAME"));
            }
            ResultSet rs5 = md.getColumns(null, null, "article_stats_daily", null);
            System.out.println("\n--- COLUMNS OF article_stats_daily ---");
            while (rs5.next()) {
                System.out.println("Column: " + rs5.getString("COLUMN_NAME"));
            }
            ResultSet rs6 = md.getColumns(null, null, "comment", null);
            System.out.println("\n--- COLUMNS OF comment ---");
            while (rs6.next()) {
                System.out.println("Column: " + rs6.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
