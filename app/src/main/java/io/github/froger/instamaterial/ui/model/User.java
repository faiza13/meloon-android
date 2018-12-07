package io.github.froger.instamaterial.ui.model;

import java.io.Serializable;

/**
 * Created by faiza on 25/11/2017.
 */

public class User implements Serializable {

    String nom ,prenom,username,email,password,birthday,image_path,address,sexe,description,photoPath,link_uri;
    int profil_id,tablesNbr,followersNbr,followingNbr,epinglenbr,following;

    public User() {
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public int getProfil_id() {
        return profil_id;
    }

    public void setProfil_id(int profil_id) {
        this.profil_id = profil_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getTablesNbr() {
        return tablesNbr;
    }

    public void setTablesNbr(int tablesNbr) {
        this.tablesNbr = tablesNbr;
    }

    public int getFollowersNbr() {
        return followersNbr;
    }

    public void setFollowersNbr(int followersNbr) {
        this.followersNbr = followersNbr;
    }

    public int getFollowingNbr() {
        return followingNbr;
    }

    public void setFollowingNbr(int followingNbr) {
        this.followingNbr = followingNbr;
    }

    public int getEpinglenbr() {
        return epinglenbr;
    }

    public void setEpinglenbr(int epinglenbr) {
        this.epinglenbr = epinglenbr;
    }

    public String getLink_uri() {
        return link_uri;
    }

    public void setLink_uri(String link_uri) {
        this.link_uri = link_uri;
    }
}
