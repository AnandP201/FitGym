package com.application.fitgym.models;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class admin extends RealmObject {


    @PrimaryKey
    private ObjectId _id=new ObjectId();

    private String authID,adminName,customerIDs;

    @Required
    private String _partition="admin";

    public String getAdminName() {
        return adminName;
    }

    public String getCustomerIDs() {
        return customerIDs;
    }

    public void setCustomerIDs(String customerIDs) {
        this.customerIDs = customerIDs;
    }
}
