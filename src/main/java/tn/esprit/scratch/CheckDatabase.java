package tn.esprit.scratch;

import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            DatabaseMetaData meta = cnx.getMetaData();
            ResultSet cols = meta.getColumns(null, null, "ticket", null);
            System.out.println("--- Column Types ---");
            while (cols.next()) {
                System.out.println(cols.getString("COLUMN_NAME") + " : " + cols.getString("TYPE_NAME"));
            }
            
            String req = "SELECT * FROM `ticket` LIMIT 1";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rs.next()) {
                System.out.println("--- Column values for first row ---");
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.println(rsmd.getColumnName(i) + " = " + rs.getString(i));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
