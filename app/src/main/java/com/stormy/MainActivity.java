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

import org.json.JSONArray;
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

    private URL mWeatherUrl;

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
        mWeatherUrl = NetworkUtils.buildUrlToGetWeatherData(mLatitude, mLongitude);
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
        mWeatherUrl = NetworkUtils.buildUrlToGetWeatherData(mLatitude, mLongitude);
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
    // and the constants("currently", etc) should be placed in there; this should be refactored into several methods
    /*
    * This method is parsing the results we get from the api and getting SOME values;
    * and showing the results in the text views
     */
    private void jsonUtils(String forecastJsonString) throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonString);

        //Getting the city name with the value of 'timezone'
        String timezoneString = forecastJson.getString("timezone");//TODO: turn these names into constants
        String[] splittingTimezoneToGetCity = timezoneString.split("/");
        String city = splittingTimezoneToGetCity[1];
        mTvCity.setText(city);

        //Getting the values of 'today'
        JSONObject currently = forecastJson.getJSONObject("currently");

        //TODO:All this will probably go in a ContentValues and then we return that to be used
        Double precipProbability = currently.getDouble("precipProbability");
        String currentTemperature = currently.getString("temperature");
        String apparentTemperature = currently.getString("apparentTemperature");

        //Formatting values to display
        double precipitationChanceFormatted;
        try {
            precipitationChanceFormatted = Double.parseDouble(
                    formatValuesToDisplay(String.valueOf(precipProbability * 100)));
        } catch (NumberFormatException e) {
            precipitationChanceFormatted = 0;
            Log.e(TAG_MAIN, "Couldn't parse String to Double");
        }
        String temperatureFormatted = formatValuesToDisplay(currentTemperature);
        String apparentTemperatureFormatted = formatValuesToDisplay(apparentTemperature);

        //Displaying formatted values
        //TODO: These strings needs to be in the strings.xml
        mBinding.textPrecipitationChance.setText((precipitationChanceFormatted) + "% chance for Rain");
        mBinding.textTemperatureCurrent.setText(temperatureFormatted + "º");
        mBinding.textApparentTemperature.setText("Real Feel " + apparentTemperatureFormatted + "º");

        mBinding.textTemperatureNow.setText(temperatureFormatted + "º");

        //Setting the next days of the week
        mBinding.textDayOfWeekNowPlus1.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(1)));
        mBinding.textDayOfWeekNowPlus2.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(2)));
        mBinding.textDayOfWeekNowPlus3.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(3)));
        mBinding.textDayOfWeekNowPlus4.setText(DateUtils.getDayOfWeek(this, DateUtils.getNextDaysDate(4)));

        //Getting the values of daily temperature
        JSONObject daily = forecastJson.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        for (int i = 1; i < data.length(); i++) {
            if (i == 5) break;

            JSONObject eachDay = data.getJSONObject(i);
            String temperatureForEachDay = eachDay.getString("temperatureHigh");

            //Displaying the temperatures
            switch (i) {
                case 1:
                    mBinding.textTemperatureNowPlus1.setText(formatValuesToDisplay(temperatureForEachDay) + "º");
                    break;
                case 2:
                    mBinding.textTemperatureNowPlus2.setText(formatValuesToDisplay(temperatureForEachDay) + "º");
                    break;
                case 3:
                    mBinding.textTemperatureNowPlus3.setText(formatValuesToDisplay(temperatureForEachDay) + "º");
                    break;
                case 4:
                    mBinding.textTemperatureNowPlus4.setText(formatValuesToDisplay(temperatureForEachDay) + "º");
                    break;
                default:
                    Log.e(TAG_MAIN, "Invalid object from the daily json response.");
            }
        }
    }

    private String formatValuesToDisplay(String temperature) {
        return String.format(Locale.getDefault(), "%.0f", Double.parseDouble(temperature));
    }
}

