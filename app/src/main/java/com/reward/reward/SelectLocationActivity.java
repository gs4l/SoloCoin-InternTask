package com.reward.reward;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.reward.reward.utils.GeofenceBroadcastReciever;
import com.reward.reward.utils.GeofenceRecieverService;
import com.reward.reward.utils.SharedPrefs;


public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = SelectLocationActivity.class.getSimpleName();

    private FusedLocationProviderClient mClient;
    private GeofencingClient geofencingClient;
    private Geofence geofence;
    private PendingIntent geofencePendingIntent;
    private GoogleMap mMap;
    private float GEOFENCE_RADIUS = 100;

    private static final int DEFAULT_ZOOM = 15;

    private Location lastLocation;
    private LatLng latLng;

    private Button submitButton;

    private boolean locationPermission;
    private Integer PERMISSION_ACCESS_FINE_LOCATION = 1;

    private SharedPrefs mPrefs;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_location);
        submitButton = findViewById(R.id.submit_button);

        mPrefs = new SharedPrefs(this);

        intent = new Intent(this, TimerActivity.class);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        mapFragment.getMapAsync(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);

        geofencingClient = LocationServices.getGeofencingClient(this);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                startActivity(intent);

            }
        });



    }

    private void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationPermission = true;
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI(){
        if (mMap == null){
            return;
        }
        try {
            if (locationPermission){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            }
            else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (Exception e){
            Log.e("Exception", e.getMessage());
        }
    }
    private void getDeviceLocation(){
        try {
            if (locationPermission) {
                Task<Location> locationResult = mClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastLocation = task.getResult();
                            if (lastLocation != null) {
                                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLng));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        latLng, DEFAULT_ZOOM));
                                mPrefs.setLat((float)latLng.latitude);
                                mPrefs.setLong((float)latLng.longitude);

                                createAndAddGeofences();
                            }
                        } else {
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void createAndAddGeofences(){
        geofence = new Geofence.Builder().setRequestId("MyGeofence")
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        submitButton.setVisibility(View.VISIBLE);
                        Log.d("Fail Error", "SUCESS");
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Fail Error", e.getMessage());
            }
        });
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(){
        if (geofencePendingIntent != null){
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceRecieverService.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermission = false;

        if(requestCode == PERMISSION_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationPermission = true;
            }
        }
        updateLocationUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();

    }

}
