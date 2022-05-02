package com.application.fitgym.helpers;

import android.app.Application;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.Sync;
import io.realm.mongodb.sync.SyncConfiguration;

public class GymApplication extends Application {


    public static final String APP_ID="gym-udzkw";
    public static final String GOOGLE_CLIENT_ID="368148182511-vph37jtog5hvt1jiusv33rfmmlse1k0u.apps.googleusercontent.com";

    public static App getGlobalAppInstance(){
        return new App(new AppConfiguration.Builder(APP_ID).appName("FitGym").build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
