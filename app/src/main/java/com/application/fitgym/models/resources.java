package com.application.fitgym.models;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class resources extends RealmObject {


    @PrimaryKey
    private ObjectId _id=new ObjectId();

    @Required
    private String _partition;


    private String userID;
    private byte[] Data;


    public void set_partition(){
        this._partition="data";
    }

    public byte[] getData() {
        return Data;
    }

    public String getUserID() {
        return userID;
    }

    public void setData(byte[] data) {
        Data = data;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
