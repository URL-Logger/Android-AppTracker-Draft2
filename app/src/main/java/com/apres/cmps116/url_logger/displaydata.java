package com.apres.cmps116.url_logger;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ToggleButton;

/**
 * Created by cedriclinares on 2/18/17.
 */

public class displaydata extends AppCompatActivity {

    ToggleButton statsBtn;
    public static final String PREFRENCES_FILE_NAME = "MyAppPreferences";
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_data);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        serviceIntent = new Intent (displaydata.this, MyService.class);
        statsBtn = (ToggleButton) findViewById(R.id.stats_btn);
        /*
        Toggle the button if it was previously toggled before the app closed
         */
        if(LoginActivity.loginSettings.getBoolean("On", false)){
            statsBtn.setTextOn("On");
            statsBtn.setChecked(true);
            startService(serviceIntent);
        }
        statsBtn.setOnClickListener(new View.OnClickListener() { //Logic for toggle button
            @Override
            public void onClick(View v) {
                if (statsBtn.isChecked()){ //if On
                    statsBtn.setTextOn("On");
                    statsBtn.setChecked(true);
                    LoginActivity.editor.putBoolean("On", true);
                    LoginActivity.editor.commit();
                    startService(serviceIntent);
                }
                else{ //if Off
                    statsBtn.setTextOff("Off");
                    statsBtn.setChecked(false);
                    LoginActivity.editor.putBoolean("On", false);
                    LoginActivity.editor.commit();
                    stopService(serviceIntent);
                }
            }
        });
    }

    public void logoutUser(View view){
        LoginActivity.editor.clear();
        LoginActivity.editor.commit();
        stopService(serviceIntent);     //Turn off data collection when we log out

        Intent intent = new Intent(displaydata.this, LoginActivity.class);
        startActivity(intent);
    }

}


