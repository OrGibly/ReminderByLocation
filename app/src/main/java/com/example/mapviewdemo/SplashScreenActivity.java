package com.example.mapviewdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int TIME_OUT = 1000;
    private static boolean startCommandWasGiven = false;
    private static String START_COMMAND_WAS_GIVEN_BUNDLE_KEY = "scwg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if(savedInstanceState!=null){
            startCommandWasGiven = savedInstanceState.getBoolean(START_COMMAND_WAS_GIVEN_BUNDLE_KEY);
        }
//      prevents multiple starting of the activity.
        if(!startCommandWasGiven){
            startCommandWasGiven = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run(){
                    Intent intent = new Intent(SplashScreenActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },TIME_OUT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(START_COMMAND_WAS_GIVEN_BUNDLE_KEY,startCommandWasGiven);
    }
}
