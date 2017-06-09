package com.apres.cmps116.url_logger;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ToggleButton;

import static com.apres.cmps116.url_logger.LoginActivity.editor;

/**
 * Created by cedriclinares on 2/18/17.
 */

public class displaydata extends AppCompatActivity {

    static ToggleButton statsBtn;
    Intent serviceIntent;
    TextView tracking;
    public SharedPreferences loginSettings;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_data);

        loginSettings = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor = loginSettings.edit();

        serviceIntent = new Intent (displaydata.this, MyService.class);
        statsBtn = (ToggleButton) findViewById(R.id.stats_btn);
        tracking = (TextView) findViewById(R.id.tracking);
        /*
        Toggle the button if it was previously toggled before the app closed
         */
       if(loginSettings.getBoolean("ischecked", false)){
            statsBtn.setTextOn("Off");
            statsBtn.setChecked(true);
            tracking.setText("Data Tracking is:ON");
           editor.putBoolean("ischecked", true);
           editor.commit();
            startService(serviceIntent);
        }


        statsBtn.setOnClickListener(new View.OnClickListener() { //Logic for toggle button
            @Override
            public void onClick(View v) {
                if (statsBtn.isChecked()){ //if On
                    statsBtn.setTextOn("Off");
                    statsBtn.setChecked(true);
                    tracking.setText("Data Tracking is:ON");
                    editor.putBoolean("ischecked", true);
                    editor.commit();
                    startService(serviceIntent);
                }
                else{ //if Off
                    statsBtn.setTextOff("On");
                    statsBtn.setChecked(false);
                    tracking.setText("Data Tracking is:OFF");
                    editor.putBoolean("ischecked", false);
                    editor.commit();
                    stopService(serviceIntent);
                }
            }
        });
    }

    public void logoutUser(View view){
        editor.clear();
        editor.commit();
        stopService(serviceIntent);     //Turn off data collection when we log out

        Intent intent = new Intent(displaydata.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //Prevent from log out when back button is clicked
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

}


