package com.example.mapviewdemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {
    private static Context appContext;
    private static final String USER_ENTRIES_KEY = "userEntries";
    private static int entriesCount;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        countUserEntries();
    }

    public static Context getAppContext(){
        return appContext;
    }

    public static int getEntriesCount(){
        return entriesCount;
    }

    private void countUserEntries(){
        SharedPreferences sharedPreferences = getSharedPreferences(USER_ENTRIES_KEY, MODE_PRIVATE);
        int prevEntrancesCount = sharedPreferences.getInt(USER_ENTRIES_KEY,0);
        entriesCount = prevEntrancesCount+1;
        sharedPreferences.edit().putInt(USER_ENTRIES_KEY, entriesCount).apply();
    }
}
