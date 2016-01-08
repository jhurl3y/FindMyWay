package com.fyp.findmyway.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;

//import com.zerokol.views.JoystickView;
//import com.zerokol.views.JoystickView.OnJoystickMoveListener;

import com.fyp.findmyway.R;
import com.fyp.findmyway.services.Constants;
import com.fyp.findmyway.services.DataTransmissionService;
import com.fyp.findmyway.views.JoystickView;
import com.fyp.findmyway.views.JoystickView.OnJoystickMoveListener;


public class ManualControlActivity extends FragmentActivity {


    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    // Importing as others views
    private JoystickView joystick;

    DataTransmissionService mService;
    boolean mBound = false;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    TextView connectionStat = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);

        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        // referring as others views
        joystick = (JoystickView) findViewById(R.id.joystickView);
        setupJoystick();
        connectionStat = (TextView) findViewById(R.id.connection_stat);
    }

    public void setupJoystick(){
        // Listener of events, it'll return the angle in degrees and power in percent
        // return to the direction of the movement
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                angleTextView.setText("Angle: " + String.valueOf(angle) + "°");
                powerTextView.setText("Power: " + String.valueOf(power) + "%");
                sendMessage(String.valueOf(angle));
                // Direction values not right.. front/right mixed up in library
                switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText(R.string.front_lab);
                        break;

                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText(R.string.front_right_lab);
                        break;

                    case JoystickView.RIGHT:
                        directionTextView.setText(R.string.right_lab);
                        break;

                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText(R.string.right_bottom_lab);
                        break;

                    case JoystickView.BOTTOM:
                        directionTextView.setText(R.string.bottom_lab);
                        break;

                    case JoystickView.BOTTOM_LEFT:
                        directionTextView.setText(R.string.bottom_left_lab);
                        break;

                    case JoystickView.LEFT:
                        directionTextView.setText(R.string.left_lab);
                        break;

                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText(R.string.left_front_lab);
                        break;

                    default:
                        directionTextView.setText(R.string.center_lab);
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, DataTransmissionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DataTransmissionService.LocalBinder binder = (DataTransmissionService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            if (mService.getState() == DataTransmissionService.STATE_CONNECTED){
                mConnectedDeviceName = mService.getConnectedDeviceName();
                mBluetoothAdapter = mService.getmAdapter();
                connectionStat.setText(getString(R.string.title_connected_to) + " " + mConnectedDeviceName);
                mService.setmHandler(mHandler);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case DataTransmissionService.STATE_CONNECTED:
                            connectionStat.setText(getString(R.string.title_connected_to) + " " + mConnectedDeviceName);
                            break;
                        case DataTransmissionService.STATE_CONNECTING:
                            connectionStat.setText(R.string.title_connecting);
                            break;
                        case DataTransmissionService.STATE_LISTEN:
                        case DataTransmissionService.STATE_NONE:
                            connectionStat.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    // String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    // String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService == null || mBound == false){
            return;
        }

        if (mService.getmHandler() == null){
            return;
        }

        if (mService.getState() != DataTransmissionService.STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mService.write(send);
        }
    }
}


