package com.funky.practice.realm;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by funky on 05/05/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        Realm.setDefaultConfiguration(
                new RealmConfiguration.Builder()
                        .name("db.realm")
                        .deleteRealmIfMigrationNeeded()
                        .schemaVersion(1)
                        .build());
    }
}
