package com.fyp.findmyway.services;


import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.OutputStream;

/**
 * Service to manage bluetooth connection with Pi.
 */
public class DataTransmissionService extends IntentService {

//    private static final String TAG = "DataTransmissionService";
//
//    private BluetoothAdapter btAdapter = null;
//    private BluetoothSocket btSocket = null;
//    private OutputStream outStream = null;
//    private BluetoothDevice device = null;

    public DataTransmissionService() {
        super("DataTransmissionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        if (intent == null){
//            return;
//        }
//
//        btAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (btAdapter == null) {
//            // Device does not support Bluetooth
//            return;
//        }
//
//        if (!btAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

    }

}
