package com.fyp.findmyway.ui;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

//import com.zerokol.views.JoystickView;
//import com.zerokol.views.JoystickView.OnJoystickMoveListener;

import com.fyp.findmyway.R;
import com.fyp.findmyway.views.JoystickView;
import com.fyp.findmyway.views.JoystickView.OnJoystickMoveListener;


public class ManualControlActivity extends FragmentActivity {


    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    // Importing as others views
    private JoystickView joystick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);

        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        // referring as others views
        joystick = (JoystickView) findViewById(R.id.joystickView);

        // Listener of events, it'll return the angle in degrees and power in percent
        // return to the direction of the movement
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                angleTextView.setText("Angle: " + String.valueOf(angle) + "°");
                powerTextView.setText("Power: " + String.valueOf(power) + "%");

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

}


