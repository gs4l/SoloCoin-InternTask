package com.reward.reward;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = SelectLocationActivity.class.getSimpleName();

    private FusedLocationProviderClient mClient;
    private GoogleMap mMap;

    private static final int DEFAULT_ZOOM = 15;

    private Location lastLocation;
    private LatLng latLng;

    private Button submitButton;

    private boolean locationPermission;
    private Integer PERMISSION_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_location);
        submitButton = findViewById(R.id.submit_button);


        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        mapFragment.getMapAsync(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);



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

                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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
