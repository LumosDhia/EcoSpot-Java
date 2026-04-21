package tn.esprit.tests;

import tn.esprit.util.MyConnection;
import java.sql.*;

public class Diagnostic {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- ARTICLES ---");
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, title, status, category_id FROM article LIMIT 10");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Title: " + rs.getString("title") + " | Status: " + rs.getString("status") + " | CatID: " + rs.getInt("category_id"));
            }

            System.out.println("\n--- CATEGORIES ---");
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT id, name FROM category");
            while (rs2.next()) {
                System.out.println("ID: " + rs2.getInt("id") + " | Name: " + rs2.getString("name"));
            }

            System.out.println("\n--- VIEW EVENTS SAMPLE ---");
            ResultSet rs3 = cnx.createStatement().executeQuery("SELECT article_id, viewed_at FROM article_view_event LIMIT 5");
            while (rs3.next()) {
                System.out.println("ArticleID: " + rs3.getInt("article_id") + " | Date: " + rs3.getTimestamp("viewed_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
