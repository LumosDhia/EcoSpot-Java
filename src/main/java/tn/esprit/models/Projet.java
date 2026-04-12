package tn.esprit.models;

import java.util.List;

public class Projet {
    //1-1 Personne : Bidirect
    private int id;
    private String nom;
    public List<Personne> personnes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


}
