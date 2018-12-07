package com.artmeloon.ui.model;

import java.io.Serializable;

public class Epingle implements Serializable {

    private int epingle_id,id_creator,idTableau;
    private String description,epingle_path,epingle_tags;
    private boolean isLiked;
    public int likesCount;
   public User user;

    public Epingle(int epingle_id, int id_creator, String description, String epingle_path, boolean isLiked,int likesCount) {
        this.epingle_id = epingle_id;
        this.id_creator = id_creator;
        this.description = description;
        this.epingle_path = epingle_path;
        this.isLiked = isLiked;
        this.likesCount = likesCount;

    }

    public Epingle() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public int getEpingle_id() {
        return epingle_id;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getDescription() {
        return description;
    }

    public String getEpingle_path() {
        return epingle_path;
    }

    public void setEpingle_id(int epingle_id) {
        this.epingle_id = epingle_id;
    }

    public String getEpingle_tags() {
        return epingle_tags;
    }

    public void setEpingle_tags(String epingle_tags) {
        this.epingle_tags = epingle_tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEpingle_path(String epingle_path) {
        this.epingle_path = epingle_path;
    }

    public int getId_creator() {
        return id_creator;
    }

    public void setId_creator(int id_creator) {
        this.id_creator = id_creator;
    }

    public int getIdTableau() {
        return idTableau;
    }

    public void setIdTableau(int idTableau) {
        this.idTableau = idTableau;
    }
}
