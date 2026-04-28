package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class TimezoneDebugTest {
    @Test
    public void test() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT CURDATE(), NOW(), @@global.time_zone, @@session.time_zone");
            if (rs.next()) {
                System.out.println("DB CURDATE: " + rs.getString(1));
                System.out.println("DB NOW: " + rs.getString(2));
                System.out.println("Global TZ: " + rs.getString(3));
                System.out.println("Session TZ: " + rs.getString(4));
            }
            System.out.println("Java LocalDate.now(): " + java.time.LocalDate.now());
            System.out.println("Java LocalDateTime.now(): " + java.time.LocalDateTime.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
