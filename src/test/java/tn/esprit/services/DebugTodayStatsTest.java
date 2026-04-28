package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDate;

public class DebugTodayStatsTest {
    @Test
    public void debug() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("System LocalDate: " + LocalDate.now());
            
            String sql = "SELECT COUNT(*) FROM article_view_event WHERE DATE(viewed_at) = CURDATE()";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Total views in DB for CURDATE(): " + rs.getInt(1));
            }
            
            String sql2 = "SELECT viewed_at FROM article_view_event ORDER BY viewed_at DESC LIMIT 5";
            ResultSet rs2 = cnx.createStatement().executeQuery(sql2);
            System.out.println("Recent viewed_at values:");
            while (rs2.next()) {
                System.out.println(" - " + rs2.getTimestamp("viewed_at"));
            }
            
            String sql3 = "SELECT COUNT(*) FROM article_reaction_event WHERE DATE(acted_at) = CURDATE()";
            ResultSet rs3 = cnx.createStatement().executeQuery(sql3);
            if (rs3.next()) {
                System.out.println("Total reactions in DB for CURDATE(): " + rs3.getInt(1));
            }
            String sql4 = "SELECT id, title, published_at, status FROM article ORDER BY id DESC LIMIT 5";
            ResultSet rs4 = cnx.createStatement().executeQuery(sql4);
            System.out.println("Recent articles:");
            while (rs4.next()) {
                System.out.println(" - ID: " + rs4.getInt("id") + " | Title: " + rs4.getString("title") + " | Pub: " + rs4.getTimestamp("published_at") + " | Status: " + rs4.getString("status"));
            }
            
            String sql5 = "SELECT COUNT(*) FROM article WHERE DATE(published_at) = CURDATE()";
            ResultSet rs5 = cnx.createStatement().executeQuery(sql5);
            if (rs5.next()) {
                System.out.println("Total articles in DB for CURDATE(): " + rs5.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
