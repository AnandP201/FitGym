package com.application.fitgym.models.CustomModels;

public class ProfileData {

    private String name,gymID,membersince,plans,age,gender;
    private byte []imageData;
    public ProfileData(String n,String gid,String ms,String p,String a,String g,byte []b){
        this.name=n;
        this.gymID=gid;
        this.membersince=ms;
        this.plans=p;
        this.age=a;
        this.gender=g;
        this.imageData=b;
    }

    public String getName() {
        return name;
    }

    public String getGymID() {
        return gymID;
    }

    public String getMembersince() {
        return membersince;
    }

    public String getPlans() {
        return plans;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public byte[] getImageData() {
        return imageData;
    }
}
