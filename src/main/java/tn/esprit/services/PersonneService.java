package tn.esprit.services;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.models.Personne;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements GlobalInterface<Personne> {

    //var
    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void add(Personne personne) {

        String req = "INSERT INTO `Personne`(`age`, `nom`, `prenom`, `cin`) VALUES ("+personne.getAge()+",'"+ personne.getNom()+"','"+personne.getPrenom()+"','"+personne.getCin()+"')";
        try {
            Statement st = cnx.createStatement();
            st.executeUpdate(req);
            System.out.println("Personne ajoutée avec succes");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void add2(Personne p){

        String req = "INSERT INTO `Personne`(`age`, `nom`, `prenom`, `cin`) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getAge());
            ps.setString(2, p.getNom());
            ps.setString(3, p.getPrenom());
            ps.setString(4, p.getCin());
            ps.executeUpdate();
            System.out.println("Personne ajoutée avec succes");

        }
        catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void delete(Personne personne) {

    }

    @Override
    public void update(Personne personne) {

    }

    @Override
    public List<Personne> getAll() {
        List<Personne> personnes = new ArrayList<>();
        String req = "SELECT * FROM `Personne`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Personne p = new Personne();
                p.setId(rs.getInt(1));
                p.setAge(rs.getInt("age"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setCin(rs.getString("cin"));
                //insert to list
                personnes.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personnes;
    }
}
