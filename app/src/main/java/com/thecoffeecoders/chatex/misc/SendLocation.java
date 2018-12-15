package com.thecoffeecoders.chatex.misc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.auth.LoginActivity;

import java.security.Permission;

public class SendLocation extends FragmentActivity implements OnMapReadyCallback {

    LocationManager mLocationManager;
    LocationListener mLocationListener;

    Location mLocation;

    private GoogleMap mMap;
    
    FloatingActionButton mCurrentLocationBtn;
    FloatingActionButton mSendLocationBtn;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && getIntent().getStringExtra("location")==null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestCurrentLocation();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.send_location_map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        initializeViews();
        if(getIntent().getExtras() == null){
            setLocationListener();
        }else{
            mSendLocationBtn.hide();
            mCurrentLocationBtn.hide();
        }
    }

    private void initializeViews() {
        mCurrentLocationBtn = findViewById(R.id.fabCurrentLocation);
        mCurrentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCurrentLocation();
            }
        });
        mSendLocationBtn = findViewById(R.id.fabSendLocation);
        mSendLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocation();
            }
        });
    }

    private void sendLocation() {
        if(mLocation != null){
            //Set the result and send the user back to ChatActivity
            Intent returnIntent = new Intent();
            String returndata = String.valueOf(mLocation.getLatitude())+","+String.valueOf(mLocation.getLongitude());
            returnIntent.putExtra("location", returndata);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private void setLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setLocationMarker(location);
                mLocationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                createAlertDialog("No location access", "Please turn on your location provider.");
            }
        };
    }


    public void setLocationMarker(Location location) {
        mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        mLocation = location;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(getIntent().getStringExtra("location") == null){
            mMap.setOnMapClickListener(new OnMapClickListener());
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    setLocationMarker(lastLocation);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                }
                requestCurrentLocation();
            }
        }else{
            String intentData = getIntent().getStringExtra("location");
            String[] arr = intentData.split(",");
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(Double.valueOf(arr[0]));
            location.setLongitude(Double.valueOf(arr[1]));
            setLocationMarker(location);
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            createAlertDialog("No location access", "Please turn on your location provider.");
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, mLocationListener);
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(SendLocation.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(alertMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public class OnMapClickListener implements GoogleMap.OnMapClickListener {

        @Override
        public void onMapClick(LatLng latLng) {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            setLocationMarker(location);
        }
    }
}
