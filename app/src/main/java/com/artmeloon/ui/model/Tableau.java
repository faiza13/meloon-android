package com.artmeloon.ui.model;

import java.io.Serializable;

/**
 * Created by faiza on 28/11/2017.
 */


public class Tableau implements Serializable {
    private int tableau_id,id_creator;
    private String nom, description,categorie;

    public Tableau(int tableau_id,int id_creator, String nom, String description,  String categorie) {
        this.tableau_id = tableau_id;
        this.id_creator = id_creator;
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
    }

    public Tableau() {
    }

    public Tableau(int tableau_id, String nom) {
        this.tableau_id = tableau_id;
        this.nom = nom;
    }

    public int getTableau_id() {
        return tableau_id;
    }

    public int getId_creator() {
        return id_creator;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setTableau_id(int tableau_id) {
        this.tableau_id = tableau_id;
    }

    public void setId_creator(int id_creator) {
        this.id_creator = id_creator;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
}