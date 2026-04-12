package tn.esprit.models;

public class Personne {

    //attributes
    private int id, age;
    private String nom, prenom, cin;

    //constructors
    public Personne() {}
    //Insert
    public Personne( int age, String nom, String prenom, String cin) {
        this.age = age;
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
    }
    //Fetch
    public Personne(int id, int age, String nom, String prenom, String cin) {
        this.id = id;
        this.age = age;
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
    }


    //Getters and setters



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }
//Display

    @Override
    public String toString() {
        return "Personne{" +
                "id=" + id +
                ", age=" + age +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", cin='" + cin + '\'' +
                '}';
    }
}
