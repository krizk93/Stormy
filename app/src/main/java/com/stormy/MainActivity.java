package com.stormy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stormy.databinding.ActivityMainBinding;
import com.stormy.utilities.DateUtils;
import com.stormy.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private static final String TAG_MAIN = MainActivity.class.getSimpleName();

    ActivityMainBinding mBinding;

    private TextView mTvCity,
            mTvDate;

    private LocationManager mLocationManager;
    private String mLocationProvider;

    //temporary lat and long (Lisbon):
    private double mLatitude = 38.734099;
    private double mLongitude = -9.155056;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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
        mTvCity = actionBar.getCustomView().findViewById(R.id.text_city);
        mTvDate = actionBar.getCustomView().findViewById(R.id.text_date);
        mTvDate.setText(DateUtils.formatDate(this));

        //request in app permissions

        if (ContextCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},101);
        }

        //TODO:Don't forget to change this:
        URL mWeatherUrl = NetworkUtils.buildUrlForCurrentWeather(mLatitude, mLongitude);
        new DarkSkyQueryTask().execute(mWeatherUrl);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }

    void getLocation(){

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationCriteria.setPowerRequirement(Criteria.POWER_LOW);

        mLocationProvider = mLocationManager.getBestProvider(locationCriteria,true);
        if (mLocationProvider != null) {
            try {

                mLocationManager.requestLocationUpdates(mLocationProvider, 6 * 1000, 0, (LocationListener) this);
                Toast.makeText(this, "Best Provider is " + mLocationProvider, Toast.LENGTH_LONG).show();
            /*TODO the requestLocationUpdates methods is set to check location updates every minute. We should find a reasonable interval to balance performance and battery consumption
            */

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        URL mWeatherUrl = NetworkUtils.buildUrlForCurrentWeather(mLatitude, mLongitude);
        new DarkSkyQueryTask().execute(mWeatherUrl);
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

    //Temporary AsyncTask to get the results of the api and show them in the UI
    //TODO: Should be replaced by a loader
    public class DarkSkyQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {
            URL weatherUrl = params[0];
            String darkSkyApiResults = null;
            try {
                darkSkyApiResults = NetworkUtils.getResponseFromHttpUrl(weatherUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return darkSkyApiResults;
        }

        @Override
        protected void onPostExecute(String weatherResults) {
            if (weatherResults != null && !weatherResults.equals("")) {
                try {
                    jsonUtils(weatherResults);
                } catch (JSONException e) {
                    Log.e(TAG_MAIN, "Error parsing JSON object");
                }

            } else {
                Log.e(TAG_MAIN, "Problem with JSON results");
            }
        }
    }

    //TODO: In the future this method should be transferred to a class like the one in Sunshine app (OpenWeatherJsonUtils)
    // and the constants("currently", etc) should be placed in there
    /*
    * This method is parsing the results we get from the api and getting SOME values;
    * and showing the results in the text views
     */
    private void jsonUtils(String forecastJsonStr) throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        //Getting the city name with the value of 'timezone'
        String timezoneString = forecastJson.getString("timezone");
        String[] splittingTimezoneToGetCity = timezoneString.split("/");
        String city = splittingTimezoneToGetCity[1];
        mTvCity.setText(city);

        //Getting the 'currently' object and then its values
        JSONObject currently = forecastJson.getJSONObject("currently");//TODO: turn these names into constants

        //All this will probably go in a ContentValues and then we return that to be used
        Double precipProbability = currently.getDouble("precipProbability");
        String currentTemperature = currently.getString("temperature");
        String apparentTemperature = currently.getString("apparentTemperature");


        double precipitationChanceFormatted;
        try {
            //get the number to be able to multiply and format
            precipitationChanceFormatted = Double.parseDouble(
                    formatValuesToDisplay(String.valueOf(precipProbability * 100)));
        } catch (NumberFormatException e) {
            precipitationChanceFormatted = 0;
            Log.e(TAG_MAIN, "Couldn't parse String to Integer");
        }

        String temperatureFormatted = formatValuesToDisplay(currentTemperature);
        String apparentTemperatureFormatted = formatValuesToDisplay(apparentTemperature);

        //TODO: This needs to be in the strings.xml
        mBinding.textPrecipitationChance.setText((precipitationChanceFormatted) + "% chance for Rain");
        mBinding.textTemperatureCurrent.setText(temperatureFormatted + "º");
        mBinding.textApparentTemperature.setText("Real Feel " + apparentTemperatureFormatted + "º");

        mBinding.textTemperatureNow.setText(temperatureFormatted + "º");

        mBinding.textDayOfWeekNowPlus1.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(1)));
        mBinding.textDayOfWeekNowPlus2.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(2)));
        mBinding.textDayOfWeekNowPlus3.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(3)));
        mBinding.textDayOfWeekNowPlus4.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(4)));
    }

    private String formatValuesToDisplay(String temperature) {
        return String.format(Locale.getDefault(), "%.0f", Double.parseDouble(temperature));
    }
}

