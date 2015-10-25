package com.sajarora.omgee;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.sajarora.omgee.api.OmgeeUser;

/**
 * Created by sajarora on 10/24/15.
 */
public class MainApplication extends Application {

    private static final String PREFS_USER = "prefs.user";
    public OmgeeUser mUser;
    private static MainApplication mInstance;
    private SharedPreferences mSharedPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //gets user from saved preferences
        checkUserLoggedIn();
    }

    private void checkUserLoggedIn() {
        String userString = mSharedPrefs.getString(PREFS_USER, null);
        if (userString != null)
            mUser = new Gson().fromJson(userString, OmgeeUser.class);
    }

    public static MainApplication getInstance(){
        return mInstance;
    }

    public OmgeeUser getUser(){
        return mUser;
    }

    public void setUser(OmgeeUser user){
        mSharedPrefs.edit().putString(PREFS_USER, new Gson().toJson(user)).commit();
        mUser = user;
    }
}
