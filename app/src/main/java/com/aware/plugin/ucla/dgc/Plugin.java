package com.aware.plugin.ucla.dgc;

import android.content.Intent;

import com.aware.Aware;
import com.aware.utils.Aware_Plugin;

/**
 * Created by ferre on 15/12/2017.
 */

public class Plugin extends Aware_Plugin {

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "UCLA DGC";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {

            //initialise core library
            Aware.startAWARE(this);
        }

        return START_STICKY; //makes sure Android knows that we need this to be active if it turns it off at some point.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //turn off core library
        Aware.stopAWARE(this);
    }
}
