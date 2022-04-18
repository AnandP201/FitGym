package com.application.fitgym.models;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class customers extends RealmObject {

    @PrimaryKey @Required
    private ObjectId _id=new ObjectId();

    @Required
    private String _partition;

    private String Name,Gender,authID,Age,Phone,RegistrationStatus;



    public String getName() {
        return Name;
    }

    public String getGender() {
        return Gender;
    }

    public void set_partition(String _partition) {
        this._partition = _partition;
    }


    public String getAuthID() {
        return authID;
    }


    public String getAge() {
        return Age;
    }

    public String getPhone() {
        return Phone;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAuthID(String authID) {
        this.authID = authID;
    }

    public void setAge(String age) {
        Age = age;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setRegistrationStatus(String registrationStatus) {
        RegistrationStatus = registrationStatus;
    }

    public String getRegistrationStatus() {
        return RegistrationStatus;
    }
}
