package com.application.fitgym.models.RealmModels;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class status extends RealmObject{

    @PrimaryKey
    private ObjectId _id =new ObjectId();

    @Required
    private String _partition;

    private String userAuthID,gymUserID,activePlans,stats,planActiveDuration,memberSince;

    public void setUserAuthID(String userAuthID) {
        this.userAuthID = userAuthID;
    }

    public void setGymUserID(String gymUserID) {
        this.gymUserID = gymUserID;
    }

    public void setActivePlans(String activePlans) {
        this.activePlans = activePlans;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public void setPlanActiveDuration(String planActiveDuration) {
        this.planActiveDuration = planActiveDuration;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public void set_partition() {
        this._partition="members";
    }

    public String getUserAuthID() {
        return userAuthID;
    }

    public String getGymUserID() {
        return gymUserID;
    }

    public String getActivePlans() {
        return activePlans;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public String getPlanActiveDuration() {
        return planActiveDuration;
    }

    public String getStats() {
        return stats;
    }
}
