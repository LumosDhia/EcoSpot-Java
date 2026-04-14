package tn.esprit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    // Singleton: one shared database connection manager instance in the app.

    // DB properties
    final String URL = "jdbc:mysql://localhost:3306/projetdev";
    final String USR = "root";
    final String PWD = "root";

    // Attributes
    // 2. static instance
    static MyConnection instance = null;
    Connection cnx;

    public static MyConnection getInstance() {
        // 3 verif
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    // constructor
    // 1 : Privatisation du constructeur
    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(URL, USR, PWD);
            System.out.println("Connexion etablie avec succes!");
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
