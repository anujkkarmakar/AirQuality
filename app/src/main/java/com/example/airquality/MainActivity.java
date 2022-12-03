package com.example.airquality;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.text.DecimalFormat;
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
    //8. particulates (PM2.5 and PM10)

    private TextView latitude;
    private TextView longitude;
    private TextView locationTV;
    TextView aqi;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final String url = "http://api.openweathermap.org/data/2.5/air_pollution";
    private static final String id = "1fd24c63c30371795275016b8df3a854";
    private static String lat = "23" , lon = "87";
    private static final int REQUEST_CODE = 100;

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
               (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))  {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        Button location = findViewById(R.id.getLocation);
        locationTV = findViewById(R.id.location);
        aqi = findViewById(R.id.aqi);
        Button airQI = findViewById(R.id.airQI);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayLastLocation();
                displayCurrentLocation();
                //getLocation();
            }
        });

        airQI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAQI();
            }
        });
    }

    //TODO: displayCurrentLocation()
    private void displayCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CancellationToken cancellationToken = new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            };

            fusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = String.valueOf(location.getLatitude());
                                lon = String.valueOf(location.getLongitude());
                                latitude.setText(lat);
                                longitude.setText(lon);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Cannot get Location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            askPermission();
        }
    }


    private void displayLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = String.valueOf(location.getLatitude());
                                lon = String.valueOf(location.getLongitude());
                                latitude.setText(lat);
                                longitude.setText(lon);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Cannot get Location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Cannot get Location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayCurrentLocation();
            }
            else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void getAQI() {
        String tempUrl = url + "?lat=" + lat + "&lon=" + lon + "&appid=" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        //Log.d("AQI:", response);
                        String output = "";
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject jsonArray = jsonObject.getJSONArray("list").getJSONObject(0);
                            JSONObject main = jsonArray.getJSONObject("main");

                            Toast.makeText(aqi.getContext(), "AQI SUCCESS", Toast.LENGTH_SHORT).show();
                            String airQuality = main.getString("aqi");
                            aqi.setText("AQI: " + airQuality);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
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
        longitude.setText("Longitude: " + lon);

        // JSON Parsing

        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            locationTV.setText(address);
        }catch (Exception e) {
            e.printStackTrace();
        }
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