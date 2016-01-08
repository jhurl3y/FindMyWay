package com.fyp.findmyway.ui;

import android.app.ProgressDialog;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.fyp.findmyway.R;
import com.fyp.findmyway.services.Constants;
import com.fyp.findmyway.services.DataTransmissionService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
                ConnectionCallbacks, OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, RoutingListener {

    protected static final String TAG = "MainActivity";
    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent
    // than this value.
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

//    Keys for storing activity state in the Bundle.
//    protected final static String LOCATION_KEY = "location-key";
//    protected final static String START_MARKER_KEY = "start-marker-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Represents a geographical location.
     */
    protected Location currentLocation;
    /**
     * Map to use.
     */
    protected GoogleMap googleMap;
    /**
     * Current location marker.
     */
    protected Marker startMarker;

    protected Marker endMarker;

    protected Boolean showButtons;

    private ArrayList<Polyline> polylines;
    private ArrayList<Circle> circles;
    private ProgressDialog progressDialog;
    private LatLng destPos;

    // Intent request codes
    private static final int MANUAL_CODE = 1;
    private static final int DESTINATON_CODE = 2;
    private static final int BlUETOOTH_CODE = 3;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private DataTransmissionService dtService = null;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    TextView connectionStatus = null;

    private boolean mBound = false;

    private boolean btStart = false;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        showButtons = false;
        polylines = new ArrayList<>();
        circles = new ArrayList<>();
        connectionStatus = (TextView) findViewById(R.id.connection_status);
    }

    public void onBtnClicked(View view) {

        switch (view.getId()) {
            case R.id.dst:
                Intent intent = new Intent(this, DirectionsActivity.class);
                Bundle extras = new Bundle();
                extras.putDouble("long", currentLocation.getLongitude());
                extras.putDouble("lat", currentLocation.getLatitude());

                intent.putExtras(extras);
                startActivityForResult(intent, DESTINATON_CODE);
                break;
            case R.id.return_location:
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new
                        LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                break;
            case R.id.bluetooth:
                Intent serverIntent = new Intent(this, BluetoothActivity.class);
                startActivityForResult(serverIntent, BlUETOOTH_CODE);
                break;
            case R.id.manual:
                Intent manualIntent = new Intent(this, ManualControlActivity.class);
                startActivityForResult(manualIntent, MANUAL_CODE);
                break;
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (dtService == null || mBound == false){
            return;
        }

        if (dtService.getmHandler() == null){
            return;
        }

        if (dtService.getState() != DataTransmissionService.STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            dtService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null){
            switch (requestCode){
                case DESTINATON_CODE:
                    Bundle extras = data.getExtras();
                    destPos =  new LatLng(extras.getDouble("lat"), extras.getDouble("long"));

                    if (endMarker != null) {
                        endMarker.remove();
                    }

                    progressDialog = ProgressDialog.show(this, "Please wait.",
                            "Fetching route information.", true);
                    Routing routing = new Routing.Builder()
                            .travelMode(Routing.TravelMode.WALKING)
                            .withListener(this)
                            .waypoints(startMarker.getPosition(), destPos)
                            .build();
                    routing.execute();
                    break;
                case BlUETOOTH_CODE:
                     if (mBound){
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        mOutStringBuffer = new StringBuffer("");
                        dtService.setmHandler(mHandler);
                        btStart = true;
                        connectDevice(data, false);
                     }
                    break;
                case MANUAL_CODE:
                    dtService.setmHandler(mHandler);
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBound = true;
            DataTransmissionService.LocalBinder binder = (DataTransmissionService.LocalBinder) service;
            dtService = binder.getService();
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
                            connectionStatus.setText(getString(R.string.title_connected_to) + " " + mConnectedDeviceName);
                            break;
                        case DataTransmissionService.STATE_CONNECTING:
                            connectionStatus.setText(R.string.title_connecting);
                            break;
                        case DataTransmissionService.STATE_LISTEN:
                        case DataTransmissionService.STATE_NONE:
                            connectionStatus.setText(R.string.title_not_connected);
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

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link BluetoothActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(BluetoothActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        dtService.connect(device, secure);
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
        if (polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        if (circles.size()>0) {
            for (Circle circle : circles) {
                circle.remove();
            }
        }
        polylines = new ArrayList<>();
        circles = new ArrayList<>();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        progressDialog.dismiss();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())) // Sets the center of the map to current location
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //add route(s) to the map.

        if (route.isEmpty()) {
            Toast.makeText(this,"Could not find a route, try again", Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < route.size(); i++) {

            List <LatLng> points = route.get(i).getPoints();

            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("Waypoints for journey:");
            sb.append("\n");

            for (LatLng l : points)
            {
                sb.append(" " + l.latitude + " " + l.longitude);
                // sb.append("\n");
            }
            sendMessage(sb.toString());

            Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(10)
                    .color(Color.BLUE)
                    .zIndex(1));
            polylines.add(polyline);

            for (int j = 0; j < points.size(); j++ ) {
                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(points.get(j))
                        .radius(0.3)
                        .fillColor(Color.DKGRAY)
                        .strokeColor(Color.DKGRAY)
                        .zIndex(2));
                circles.add(circle);
            }

            Toast.makeText(getApplicationContext(), "Distance: " + route.get(i).getDistanceText() + " Duration: " + route.get(i).getDurationText(), Toast.LENGTH_LONG).show();
        }

        endMarker = googleMap.addMarker(new MarkerOptions().position(destPos)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker()));
    }

    @Override
    public void onRoutingFailure() {
        // The Routing request failed
        progressDialog.dismiss();
        Toast.makeText(this,"Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingCancelled() {

    }

    protected void createLocationRequest() {
        // fused location provider will return location updates that are accurate to within a few feet
        mLocationRequest = new LocationRequest()
                            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                            .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                View main_bar = findViewById(R.id.main_bar);
                View dst = findViewById(R.id.dst);
                View return_location = findViewById(R.id.return_location);

                if (showButtons){
                    main_bar.setVisibility(main_bar.VISIBLE);
                    main_bar.animate().translationY(0);
                    dst.animate().alpha(1.0f);
                    return_location.animate().alpha(1.0f);
                    showButtons = !showButtons;
                } else {
                    main_bar.setVisibility(main_bar.VISIBLE);
                    main_bar.animate().translationY(-main_bar.getHeight()*2);
                    dst.animate().alpha(0.0f);
                    return_location.animate().alpha(0.0f);
                    showButtons = !showButtons;
                }
            }
        });
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        updateMap();
    }

    private void updateMap() {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        if (startMarker != null) {
            startMarker.setPosition(latLng);
        } else {
            startMarker = googleMap.addMarker(new MarkerOptions().position(latLng)
                                                                 .title("You are here!")
                                                                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location)));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Sets the center of the map to current location
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            updateMap();
            startLocationUpdates();
        } else {
            Toast.makeText(this, R.string.common_google_play_services_unknown_issue, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(this, DataTransmissionService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        // mBound == true &
        if (btStart == true){
            if (dtService.getState() == DataTransmissionService.STATE_NONE){
                dtService.start();
            }
            btStart = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dtService != null) {
            dtService.stop();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        return true;
    }

}
