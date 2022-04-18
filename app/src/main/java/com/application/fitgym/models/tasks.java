package com.application.fitgym.models;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class tasks extends RealmObject {

    @PrimaryKey
    private ObjectId _id=new ObjectId();

    @Required
    private String _partition;

    private String title,description,createdOn,userID;


    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void set_partition() {
        this._partition = "work";
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}
