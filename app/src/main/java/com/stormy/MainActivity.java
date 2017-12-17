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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private TextView tv_City,
            tv_Date,
            tv_PrecipitationProbability,
            tv_Temperature,
            tv_ApparentTemperature;
    TextView mShowLocationTV;
    LocationManager locationManager;
    String locationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the action bar with our custom view:
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setElevation(0);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_for_actionbar, null);

        actionBar.setCustomView(v);

        //Getting the views in the actionBar
        tv_City = actionBar.getCustomView().findViewById(R.id.text_city);
        tv_Date = actionBar.getCustomView().findViewById(R.id.text_date);


        tv_PrecipitationProbability = findViewById(R.id.text_precipitation_chance);
        tv_Temperature = findViewById(R.id.text_temperature_current);
        tv_ApparentTemperature = findViewById(R.id.text_apparent_temperature);


       // mShowLocationTV = (TextView) findViewById(R.id.location_Text);

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
        getLocation();
    }

    void getLocation(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
        locationCritera.setPowerRequirement(Criteria.POWER_LOW);

        locationProvider = locationManager.getBestProvider(locationCritera,true);
        if (locationProvider != null) {
            try {

                locationManager.requestLocationUpdates(locationProvider, 60 * 1000, 0, (LocationListener) this);
                Toast.makeText(this, "Best Provider is " + locationProvider, Toast.LENGTH_LONG).show();
            /*TODO the requestLocationUpdates methods is set to check location updates every minute. We should find a reasonable interval to balance performance and battery consumption
            */

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //mShowLocationTV.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());


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

