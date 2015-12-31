package com.fyp.findmyway.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;cd 

/**
 * Created by james on 31/12/15.
 */
public class BluetoothService extends Service {

    @Override
    public void onCreate() {
        Log.d("BluetoothService", "Service started");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
