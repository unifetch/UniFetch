package com.jay.unidrive;

import com.parse.Parse;
import com.parse.ParseACL;

import android.app.Application;
import android.content.Context;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(getResources().getString(R.string.back4app_app_id))
                .clientKey(getResources().getString(R.string.back4app_client_key))
                .server(getResources().getString(R.string.back4app_server_url))
                .build()
        );
//    ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}