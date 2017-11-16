package com.myschedulerassistant;

/**
 * Created by nischal on 11/14/2017.
 */

import android.Manifest;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class GoogleService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    int check_interval = 300000;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private Intent alarmIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 30, check_interval);
        alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        fn_getlocation();
        AlarmManager manager;


        alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);

        /*
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = check_interval+5000;

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();

//        fn_getlocation();*/
    }

    @Override
    public void onLocationChanged(Location location) {
        //fn_getlocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Toast.makeText(getApplicationContext(), "getting location", Toast.LENGTH_SHORT).show();
        if (!isGPSEnable && !isNetworkEnable) {
            Toast.makeText(getApplicationContext(), "getting location failed no permission", Toast.LENGTH_SHORT).show();
        } else {

            if (isNetworkEnable) {
                location = null;

                if (isGPSEnable) {
                    location = null;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            Log.e("latitude", location.getLatitude() + "");
                            Log.e("longitude", location.getLongitude() + "");
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Toast.makeText(getApplicationContext(), "latitude long is through gps " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                            fn_update(location);
                        }
                    }
                }
            } else if (MainActivity.mLocationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {

                        Log.e("latitude", location.getLatitude() + "");
                        Log.e("longitude", location.getLongitude() + "");
                        Toast.makeText(getApplicationContext(), "latitude long is through network " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }

            }

        }

    }

    private void fn_update(Location location) {
        if (alarmIntent != null) {
            alarmIntent.putExtra("latutide", location.getLatitude() + "");
            alarmIntent.putExtra("longitude", location.getLongitude() + "");
            Toast.makeText(getApplicationContext(), "latitude long is updated " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
            sendBroadcast(alarmIntent);
        }

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }


}

