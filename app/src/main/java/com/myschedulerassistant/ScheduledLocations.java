package com.myschedulerassistant;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;

import java.util.Iterator;
import java.util.List;

/**
 * Created by nischal on 10/7/2017.
 */

public class ScheduledLocations extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    SharedPreferences sharedpreferences;
    Button fetchfastestRoute;
    Boolean pageLoaded = false;
    JSONArray displayedLocs;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private Marker[] scheduled_locations;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.fetchFastestRoute:
                    if (!pageLoaded || mCurrLocationMarker == null) {
                        Log.i(ScheduledLocations.class.getName(), "Page no loaded yet");
                    } else {
                        pageLoaded = false;
                        ProgressDialog progressDialog;
                        progressDialog = new ProgressDialog(ScheduledLocations.this);
                        progressDialog.setMessage("Fetching");
                        progressDialog.show();
                        Double[] distances = new Double[scheduled_locations.length];
                        Marker prev_location = mCurrLocationMarker;
                        for (int i = 0; i < scheduled_locations.length; i++) {
                            Marker cur_loc = scheduled_locations[i];
                            Double distance = null;
                            while (distance == null) {
                                distance = CommonFunctions.getDistance(cur_loc.getPosition().latitude,
                                        cur_loc.getPosition().longitude, prev_location.getPosition().latitude,
                                        prev_location.getPosition().longitude, mMap);

                            }
                            prev_location = cur_loc;
                            Toast.makeText(getApplicationContext(), "distance " + distance, Toast.LENGTH_SHORT).show();
                            distances[i] = distance;
                        }
                        Toast.makeText(getApplicationContext(), "displayed all locations", Toast.LENGTH_SHORT).show();

                        progressDialog.hide();
                        pageLoaded = true;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.scheduled_locations);
        fetchfastestRoute = (Button) findViewById(R.id.fetchFastestRoute);
        fetchfastestRoute.setOnClickListener(onClickListener);

        displayedLocs = new JSONArray();
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
                Context.MODE_PRIVATE);
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Toast.makeText(getApplicationContext(), polyline.getTag().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        DBHelper dbHelper = DBHelper.getInstance(ScheduledLocations.this);
        dbHelper.open();
        List<Task> taskList = dbHelper.getTodaysTasks();
        if (taskList.size() > 0) {
            int i = 0;
            Iterator<Task> itr = taskList.iterator();
            scheduled_locations = new Marker[taskList.size()];
            while (itr.hasNext()) {
                Task currentTask = itr.next();
                Log.i("maps", currentTask.toString());
                LatLng latLng = new LatLng(currentTask.getlatitude(), currentTask.getlongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(currentTask.getTaskName() + " on " + currentTask.formatDate());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                scheduled_locations[i++] = mMap.addMarker(markerOptions);
            }
            pageLoaded = true;
        } else {
            Toast.makeText(getApplicationContext(), "No schedules for today. Please Add a schedule " +
                    "and come here to check the aggregated view", Toast.LENGTH_LONG).show();
        }
        /*sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
                Context.MODE_PRIVATE);
        String jsonArrStr = sharedpreferences.getString(MainActivity.SCHEDULER_ARRAY, null);
        try {
            if (jsonArrStr != null) {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(jsonArrStr);
                scheduled_locations = new Marker[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_fetched = jsonArray.getJSONObject(i);
                    LatLng latLng = new LatLng((Double) json_fetched.get(MainActivity.PLACE_LATITUDE),
                            (Double) json_fetched.get(MainActivity.PLACE_LONGITUDE));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(json_fetched.getString(MainActivity.PURPOSE) + " on " + json_fetched.getString(MainActivity.DATE_AND_TIME));
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    scheduled_locations[i] = mMap.addMarker(markerOptions);
                    displayedLocs.put(json_fetched);  //TODO all the later caluclations are donw only on these
                }
                pageLoaded = true;
            } else {
                Toast.makeText(getApplicationContext(), "No schedules added. Please Add a schedule " +
                        "and come here to check the aggregated view", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


}
