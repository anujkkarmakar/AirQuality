package com.example.airquality;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    //TODO: Request the user for location permission. If s/he denies, we cannot show the air quality
    //1. Air Quality Index
    //2. Carbon monoxide (CO)
    //3. Nitrogen monoxide (NO)
    //4. Nitrogen dioxide (NO2)
    //5. Ozone (O3)
    //6. Sulphur dioxide (SO2)
    //7. Ammonia (NH3)
    //8. particulates (PM2.5 and PM10).

    private TextView latitude;
    private TextView longitude;
    private TextView locationTV;
    private static TextView aqi;
    private Button location;
    LocationManager locationManager;

    private static final String url = "http://api.openweathermap.org/data/2.5/air_pollution?";
    private static final String id = "1fd24c63c30371795275016b8df3a854";
    private static String lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))  {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        location = findViewById(R.id.getLocation);
        locationTV = findViewById(R.id.location);
        aqi = findViewById(R.id.aqi);
        Button airQI = findViewById(R.id.airQI);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        airQI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AQITask().execute();
            }
        });
    }

    private static class AQITask extends AsyncTask<String, Void, String> {

        /**
         * @param strings
         * @deprecated
         */
        @Override
        protected String doInBackground(String... strings) {
            return HttpRequest.excuteGet(url + "lat=" + lat + "&lon=" + lon + "&appid=" + id);
        }

        @Override
        protected void onPostExecute(String s) {
            //we need to parse the JSON here
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject jsonArray = jsonObject.getJSONArray("list").getJSONObject(0);
                JSONObject main = jsonArray.getJSONObject("main");

                String airQuality = main.getString("aqi");
                aqi.setText(airQuality);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());
        latitude.setText("Latitude: " + lat);
        longitude.setText("longitude: " + lon);

        // JSON Parsing

//        try {
//            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
//            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            String address = addresses.get(0).getAddressLine(0);
//
//            locationTV.setText(address);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    /**
     * @param provider
     * @param status
     * @param extras
     * @deprecated
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}