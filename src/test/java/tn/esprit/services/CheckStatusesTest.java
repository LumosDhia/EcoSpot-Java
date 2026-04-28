package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckStatusesTest {
    @Test
    public void checkStatuses() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- DISTINCT STATUSES ---");
            ResultSet rs = cnx.createStatement().executeQuery("SELECT DISTINCT status FROM article");
            while (rs.next()) {
                System.out.println("Status: '" + rs.getString("status") + "'");
            }

            System.out.println("\n--- DISTINCT REACTIONS ---");
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT DISTINCT reaction FROM article_reaction_event");
            while (rs2.next()) {
                System.out.println("Reaction: '" + rs2.getString("reaction") + "'");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
