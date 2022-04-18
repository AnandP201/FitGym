package com.application.fitgym.models;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class plans extends RealmObject {
    @PrimaryKey
    private ObjectId _id=new ObjectId();

    private String PlanID,Title,Description,Price,Duration;

    @Required
    String _partition;

    public String getPlanID() {
        return PlanID;
    }

    public void setPlanID(String planID) {
        this.PlanID = planID;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public void set_partition() {
        this._partition = "subscription";
    }
}
