package com.fyp.findmyway.activities;

import com.fyp.findmyway.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
                ConnectionCallbacks, OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

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

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        showButtons = false;
    }

    public void onBtnClicked(View view){
        if (view.getId() == R.id.dst) {
            Intent intent = new Intent(this, DirectionsActivity.class);
            Bundle extras = new Bundle();
            extras.putDouble("long", currentLocation.getLongitude());
            extras.putDouble("lat", currentLocation.getLatitude());

            intent.putExtras(extras);
            startActivityForResult(intent, 2);
        }

        if (view.getId() == R.id.return_location) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new
                    LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            LatLng pos =  new LatLng(extras.getDouble("lat"), extras.getDouble("long"));

            if (endMarker != null) {
                endMarker.setPosition(pos);
            } else {
                endMarker = googleMap.addMarker(new MarkerOptions().position(pos)
                                     .title("Destination")
                                     .icon(BitmapDescriptorFactory.defaultMarker()));
            }

        }
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
                if (showButtons){
                    findViewById(R.id.dst).animate().alpha(1.0f);
                    findViewById(R.id.return_location).animate().alpha(1.0f);
                    showButtons = !showButtons;
                } else {
                    findViewById(R.id.dst).animate().alpha(0.0f);
                    findViewById(R.id.return_location).animate().alpha(0.0f);
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
                                                                 .icon(BitmapDescriptorFactory.fromResource( R.drawable.ic_location )));
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        return true;
    }

}
