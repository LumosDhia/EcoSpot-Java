package tn.esprit.tests;

import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckReactionData {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- article_reaction_event ---");
            ResultSet rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_event");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT reaction, acted_at FROM article_reaction_event LIMIT 5");
            while (rs.next()) {
                System.out.println("Reaction: " + rs.getString("reaction") + " | Date: " + rs.getTimestamp("acted_at"));
            }

            System.out.println("\n--- article_reaction_java ---");
            rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_java");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT type FROM article_reaction_java LIMIT 5");
            while (rs.next()) {
                System.out.println("Type: " + rs.getString("type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
