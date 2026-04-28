package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.util.Map;

public class DebugStatsTest {
    @Test
    public void debugAll() {
        Connection cnx = MyConnection.getInstance().getCnx();
        StatisticsService service = new StatisticsService();
        try {
            System.out.println("--- SERVICE OVERVIEW ---");
            System.out.println("Total Articles (Unfiltered): " + getCount(cnx, "SELECT COUNT(*) FROM article"));
            System.out.println("Total Articles (Service): " + service.getTotalPublishedArticles(null));
            
            System.out.println("\n--- REACTION DATA ---");
            System.out.println("Total Reactions (Table): " + getCount(cnx, "SELECT COUNT(*) FROM article_reaction_event"));
            Map<String, Integer> reactions = service.getTotalReactions(null);
            System.out.println("Reactions Map: " + reactions);
            
            System.out.println("\n--- ARTICLE SAMPLE ---");
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, status FROM article LIMIT 5");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Status: [" + rs.getString("status") + "]");
            }

            System.out.println("\n--- REACTION SAMPLE ---");
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT reaction FROM article_reaction_event LIMIT 5");
            while (rs2.next()) {
                System.out.println("Reaction: [" + rs2.getString("reaction") + "]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCount(Connection cnx, String sql) throws SQLException {
        ResultSet rs = cnx.createStatement().executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return -1;
    }
}
