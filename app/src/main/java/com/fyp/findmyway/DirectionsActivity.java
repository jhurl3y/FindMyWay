package com.fyp.findmyway;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DirectionsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    protected GoogleMap googleMap;
    protected Marker curr;
    protected Marker dest;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_directions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            LatLng current_position = new LatLng(extras.getDouble("lat"), extras.getDouble("long"));
            curr = googleMap.addMarker(new MarkerOptions().position(current_position)
                    .icon(BitmapDescriptorFactory.fromResource( R.drawable.ic_location )));

            double lat_offset = 1.0/111111.0*40;
            double long_offset = 1.0/111111.0*Math.cos(extras.getDouble("lat"))*40;
            // new offset location (dst)
            LatLng offset_loc = new LatLng(extras.getDouble("lat") + lat_offset, extras.getDouble("long") + long_offset);
            dest = googleMap.addMarker(new MarkerOptions().position(offset_loc)
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(offset_loc) // Sets the center of the map to current location
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.setOnCameraChangeListener(this);
        }
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        // Get the center of the Map.
        LatLng centerOfMap = googleMap.getCameraPosition().target;
        // Update Marker's position to the center of the Map.
       // dest.setPosition(centerOfMap);
    }
}
