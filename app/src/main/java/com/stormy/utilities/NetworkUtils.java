/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormy.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {

    private static final String TAG_NETWORK_UTILS = NetworkUtils.class.getName();

    //TODO: I've put a lot of comments, to help people that didn't see the API.
    // Remove unnecessary comments

    private static final String WEATHER_BASE_URL = "https://api.darksky.net/forecast";

    /*
    *Required parameters for the URL request
    */
    //Your Dark Sky secret key. (Your secret key must be kept secret)
    private static final String KEY_DARK_SKY_API = "PutApiKeyHereToTest";

    //Either be a UNIX time (that is, seconds since midnight GMT on 1 Jan 1970) or a string formatted
    //Required for Time Machine Request
    private long TIME;

    /*
    *Optional parameters (HTTP query parameters)
    */
    //Exclude some number of data blocks from the API response.
    // This is useful for reducing latency and saving cache space.
    private static final String EXCLUDE = "exclude=";
    //When present, return hour-by-hour data for the next 168 hours, instead of the next 48.
    private static final String EXTEND = "extend";
    //Return summary properties in the desired language. English is default
    private static final String LANGUAGE = "lang";
    //Return weather conditions in the requested units. Default is 'us' (imperial units)
    private static final String UNITS = "units=";

    /*
    * Returned objects
     */
    //Forecast Request returns the current weather conditions.
    //Time Machine Req: currently data point will refer to the time provided, rather than the current time.
    private static final String CURRENTLY = "currently";

    //Forecast Request minute-by-minute forecast for the next hour (where available).
    //Time Machine Request: minutely data block will be omitted,
    // unless you are requesting a time within an hour of the present.
    private static final String MINUTELY = "minutely";

    //Forecast Request hour-by-hour forecast for the next 48 hours.
    //Note: Hyperlocal next-hour precipitation forecasts are only currently available in the USA, UK, and Ireland.
    //Time Machine Request returns the forecasted (in the future) hour-by-hour weather and
    //hourly data block will contain data points starting at midnight (local time) of the day requested,
    // and continuing until midnight (local time) of the following day.
    private static final String HOURLY = "hourly";

    //Forecast Request day-by-day forecast for the next week.
    //Time Machine Request: daily data block will contain a single data point referring to the requested date.
    private static final String DAILY = "daily";

    //Note:Severe weather alerts are only currently available in the USA, UK, Canada, Germany, and Norway.
    //An alerts array, which, if present, contains any severe weather alerts pertinent to the requested location.
    //Forecast Request day-by-day forecast for the next week.
    //Time Machine Request: alerts data block will be omitted.
    private static final String ALERTS = "alerts";

    //A flags object containing miscellaneous metadata about the request.
    private static final String FLAGS = "flags";

    //String used several times to perform the request
    private static final String COMMA = ",";

    //Excluding everything for now except the 'currently' data
    private static final String parametersToExclude = MINUTELY
            + COMMA + HOURLY
            + COMMA + DAILY
            + COMMA + ALERTS
            + COMMA + FLAGS;


    /**
     * Builds the URL used to query DarkSky.
     * We this method we are ONLY getting the 'currently' data (and using auto for the units)
     * TODO: Make url for daily data (next 4 days)->  Time Machine Request
     *
     * @param latitude  The default should be where the user is currently located
     * @param longitude The default should be where the user is currently located
     * @return The URL to use to query the DarkSky for the current weather.
     */
    public static URL buildUrlForCurrentWeather(@NonNull double latitude, @NonNull double longitude) {

        //latitude and longitude is in decimal degrees
        String latitudeAndLongitude = latitude + COMMA + longitude;

        //I chose to use appendEncodedPath so he doesn't encode the commas,
        // since we need to exclude several parameters, not just one.
        //units = auto: automatically select units based on geographic location
        //units: we will probably let the user select the options he prefers in settings, we can use the default? ('us')
        Uri builtUri = Uri.parse(WEATHER_BASE_URL).buildUpon()
                .appendPath(KEY_DARK_SKY_API)
                .appendEncodedPath(latitudeAndLongitude + "?" + EXCLUDE + parametersToExclude + "&" + UNITS + "si")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
            Log.i(TAG_NETWORK_UTILS, "HttpURLConnection disconnected");
        }
    }
}