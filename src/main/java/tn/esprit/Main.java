package tn.esprit;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.models.Personne;
import tn.esprit.models.Projet;
import tn.esprit.services.PersonneService;
import tn.esprit.util.MyConnection;

public class Main {
    public static void main(String[] args) {
        Personne personne = new Personne(27, "IBRAHIM", "DIAZ", "12345678");
        GlobalInterface ps = new PersonneService();
        //ps.add2(personne);
        //System.out.println(ps.getAll());

        Projet projet = new Projet();
        projet.setNom("PIDEV");


        System.out.println(projet.personnes);



     }
}