package com.artmeloon.ui.model;

import java.io.Serializable;

/**
 * Created by Ghazy Boujmil on 05/12/2017.
 */

public class Comment implements Serializable {
    private int id,user_id,epingle_id;
    private String text_comment;

    public Comment() {
    }

    public Comment(int user_id, int epingle_id, String text_comment) {
        this.user_id = user_id;
        this.epingle_id = epingle_id;
        this.text_comment = text_comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getEpingle_id() {
        return epingle_id;
    }

    public void setEpingle_id(int epingle_id) {
        this.epingle_id = epingle_id;
    }

    public String getText_comment() {
        return text_comment;
    }

    public void setText_comment(String text) {
        this.text_comment = text;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", epingle_id=" + epingle_id +
                ", text_comment='" + text_comment + '\'' +
                '}';
    }
}
