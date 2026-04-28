package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class ListTablesTest {
    @Test
    public void listTables() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[] {"TABLE"});
            while (rs.next()) {
                System.out.println("Table: " + rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
