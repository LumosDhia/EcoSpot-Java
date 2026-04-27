package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckAltReactionTableTest {
    @Test
    public void check() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "article_reaction_java", null);
            System.out.println("--- COLUMNS OF article_reaction_java TABLE ---");
            while (rs.next()) {
                System.out.println("Col: " + rs.getString("COLUMN_NAME") + " | Type: " + rs.getString("TYPE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
