package com.stormy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener{

    TextView mShowLocationTV;
    LocationManager locationManager;
    String locationProvider;
    Location mLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mShowLocationTV = (TextView) findViewById(R.id.text_location);

        //request in app permissions

        if (ContextCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},101);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocation = getLocation();
        if (mLocation != null){
            mShowLocationTV.setText("Initial Current Location. Latitude: " + mLocation.getLatitude() + "\n Longitude: " + mLocation.getLongitude());
        }
    }

    Location getLocation(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
        locationCritera.setPowerRequirement(Criteria.POWER_LOW);
        Location location = null;

        locationProvider = locationManager.getBestProvider(locationCritera,true);

        if (locationProvider != null && locationManager.isProviderEnabled(locationProvider)) {
            try {

                locationManager.requestLocationUpdates(locationProvider, 60 * 1000, 0, (LocationListener) this);
                location = locationManager.getLastKnownLocation(locationProvider);
                Toast.makeText(this, "Best Provider is " + locationProvider, Toast.LENGTH_LONG).show();
            /*TODO the requestLocationUpdates methods is set to check location updates every minute. We should find a reasonable interval to balance performance and battery consumption
            */

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        mShowLocationTV.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());


    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }
}

