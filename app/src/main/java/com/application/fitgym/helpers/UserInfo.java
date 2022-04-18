package com.application.fitgym.helpers;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String name,imageURL;


    public UserInfo(){
        this.name="";
        this.imageURL="";

    }

    private UserInfo(String name,String imageURL){
        this.name=name;
        this.imageURL=imageURL;
    }


    public static UserInfo storeAndGetUserInfoBundle(String name,String imageURL){
        return new UserInfo(name,imageURL);
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }
}
