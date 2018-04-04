package com.aware.android.ucla.dgc;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.google.activity_recognition.Settings;
import com.aware.plugin.ucla.dgc.R;
import com.aware.ui.PermissionsHandler;

import java.util.ArrayList;

/**
 * This is where the main interface of the app will be implemented
 * For now, we will just join a study and set the settings that are provided by the core library and existing plugins
 */
public class UCLA extends AppCompatActivity {

    private Button join;
    private Button sync;
    private ProgressBar joining;

    private LinearLayout cards_container;

    private ArrayList<String> REQUIRED_PERMISSIONS;
    private JoinObserver joinObserver = new JoinObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ucl);

        cards_container = findViewById(R.id.cards_container);
        cards_container.setVisibility(View.INVISIBLE);

        //Since Android 5+ we need to check in runtime if the permissions were given, so we will check every time the user launches the main UI.
        REQUIRED_PERMISSIONS = new ArrayList<>();
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_CALL_LOG);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_CONTACTS);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_SMS);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.RECORD_AUDIO);
        REQUIRED_PERMISSIONS.add(Manifest.permission.CHANGE_WIFI_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.INTERNET);

        joining = findViewById(R.id.progress_joining);
        joining.setVisibility(View.INVISIBLE);

        join = findViewById(R.id.join_study);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                joining.setVisibility(View.VISIBLE);

                Aware.joinStudy(getApplicationContext(), "https://api.awareframework.com/index.php/webservice/index/1553/ZDaTuBFymPPF"); //TODO: UPDATE to UCLA server dashboard study endpoint

                //Now let's set the settings we want for the study
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SIGNIFICANT_MOTION, true); //we only want to log accelerometer data when there is movement
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, true); //we want to use the ESM functionality of AWARE

                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 200 * 1000);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.THRESHOLD_ACCELEROMETER, 0.01); //changes need to be > 0.01 in each axis to log. Makes sensor less sensitive

                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_BAROMETER, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_BAROMETER, 200 * 1000);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.THRESHOLD_BAROMETER, 0.01); //changes need to be > 0.01 in each axis to log. Makes sensor less sensitive

                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LIGHT, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LIGHT, 200 * 1000);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.THRESHOLD_LIGHT, 5); //changes need to be > 5 lux to log. Makes sensor less sensitive

                //Activity Recognition settings
                Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true);
                //this is actually controlled by Google's algorithm. We want every 10 seconds, but this is not guaranteed. Recommended value is 60 s.
                Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 60);
                Aware.startPlugin(getApplicationContext(), "com.aware.plugin.google.activity_recognition"); //initialise plugin and set as active

                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true); //includes usage, and foreground
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_BATTERY, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_COMMUNICATION_EVENTS, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_CALLS, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MESSAGES, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_TOUCH, true);

                Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_WIFI, true);
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_WIFI, 5); //every 5 minutes

                //fused location
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.STATUS_GOOGLE_FUSED_LOCATION, true);
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.FREQUENCY_GOOGLE_FUSED_LOCATION, 300); //every 5 minutes.
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.MAX_FREQUENCY_GOOGLE_FUSED_LOCATION, 60); //every 60 s if mobile
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.ACCURACY_GOOGLE_FUSED_LOCATION, 102);
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.FALLBACK_LOCATION_TIMEOUT, 20); //if not moving for 20 minutes, new location captured
                Aware.setSetting(getApplicationContext(), com.aware.plugin.google.fused_location.Settings.LOCATION_SENSITIVITY, 5); //need to move 5 meter to assume new location
                Aware.startPlugin(getApplicationContext(), "com.aware.plugin.google.fused_location");

                //conversations
                Aware.startPlugin(getApplicationContext(), "com.aware.plugin.studentlife.audio_final");
                // there are no settings on this one... duty cycle is set to every 3 minutes, listen for 1 minute.

                //google calendar esm scheduler
                Aware.startPlugin(getApplicationContext(), "com.aware.plugin.esm.scheduler");

                //Settings for data synching strategies
                Aware.setSetting(getApplicationContext(), Aware_Preferences.WEBSERVICE_SILENT, true); //don't show notifications of synching events
                Aware.setSetting(getApplicationContext(), Aware_Preferences.WEBSERVICE_WIFI_ONLY, true); //only sync over wifi
                Aware.setSetting(getApplicationContext(), Aware_Preferences.WEBSERVICE_FALLBACK_NETWORK, 6); //after 6h without being able to use Wifi to sync, fallback to 3G for syncing.
                Aware.setSetting(getApplicationContext(), Aware_Preferences.REMIND_TO_CHARGE, true); //remind participants to charge their phone when reaching 15% of battery left
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_CLEAN_OLD_DATA, 1); //weekly basis cleanup of local storage, otherwise we run out of space locally on the device
                Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_WEBSERVICE, 60); //try to sync data to the server every 1h

                //Ask accessibility to be enabled
                Applications.isAccessibilityServiceActive(getApplicationContext());

                //Ask doze to be disabled
                Aware.isBatteryOptimizationIgnored(getApplicationContext(), getPackageName());
            }
        });

        sync = findViewById(R.id.sync_data);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sync = new Intent(Aware.ACTION_AWARE_SYNC_DATA);
                sendBroadcast(sync);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean permissions_ok = true;
        for (String p : REQUIRED_PERMISSIONS) { //loop to check all the required permissions.
            if (PermissionChecker.checkSelfPermission(this, p) != PermissionChecker.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {
            if (!Aware.IS_CORE_RUNNING) {
                Intent aware = new Intent(getApplicationContext(), Aware.class);
                startService(aware);

                Applications.isAccessibilityServiceActive(getApplicationContext());
                Aware.isBatteryOptimizationIgnored(getApplicationContext(), getPackageName());
            }

            if (Aware.isStudy(getApplicationContext())) {
                TextView welcome = findViewById(R.id.welcome);
                welcome.setText("AWARE Device ID: " + Aware.getSetting(this, Aware_Preferences.DEVICE_ID));

                if (cards_container.getVisibility() == View.INVISIBLE) {
                    cards_container.setVisibility(View.VISIBLE);
                    cards_container.addView(new com.aware.plugin.google.activity_recognition.ContextCard().getContextCard(getApplicationContext()));
                    //add more cards here
                }

                join.setVisibility(View.INVISIBLE);
                sync.setVisibility(View.VISIBLE);

            } else {
                IntentFilter joinFilter = new IntentFilter(Aware.ACTION_JOINED_STUDY);
                registerReceiver(joinObserver, joinFilter);

                join.setVisibility(View.VISIBLE);
                sync.setVisibility(View.INVISIBLE);
            }
        } else {

            finish();

            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.putExtra(PermissionsHandler.EXTRA_REDIRECT_ACTIVITY, getPackageName() + "/" + getClass().getName());
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(permissions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class JoinObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Aware.ACTION_JOINED_STUDY)) {

                unregisterReceiver(joinObserver);

                finish();

                Intent relaunch = new Intent(context, UCLA.class);
                startActivity(relaunch);
            }
        }
    }
}
