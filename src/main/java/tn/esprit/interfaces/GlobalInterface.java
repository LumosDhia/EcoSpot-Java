package tn.esprit.interfaces;

import tn.esprit.models.Personne;

import java.util.List;

public interface GlobalInterface <T> {
    void add(T t);
    void add2(T t);
    void delete(T t);
    void update(T t);
    List<T> getAll();
}
