package com.myschedulerassistant;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    Button  add_Schedule;
    Button  view_Schedule;
    public static Boolean mLocationPermissionGranted;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String SCHEDULER_ARRAY = "array";

    //JSON Object strings
    public static final String PLACE = "place";
    public static final String PLACE_NAME = "place_name";
    public static final String PLACE_ADDRESS = "place_address";
    public static final String PLACE_LATITUDE = "place_lat";
    public static final String PLACE_LONGITUDE = "place_lon";
    public static final String PURPOSE = "purpose";
    public static final String DATE_AND_TIME = "dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationPermission();
        add_Schedule = (Button)findViewById(R.id.addLocation);
        view_Schedule = (Button)findViewById(R.id.viewSchedule);
        add_Schedule.setOnClickListener(onClickListener);
        view_Schedule.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(mLocationPermissionGranted) {
                switch (v.getId()) {
                    case R.id.addLocation:
                        Intent addLocationIntent = new Intent(MainActivity.this, AddLocation.class);
                        startActivity(addLocationIntent);
                        break;
                    case R.id.viewSchedule:
                        Intent viewScheduleIntent = new Intent(MainActivity.this, ScheduledLocations.class);
                        startActivity(viewScheduleIntent);
                        break;
                }
            }else {
                Toast toast = Toast.makeText(getApplicationContext(), "No permission given to access location", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;
                } else {

                    Toast toast = Toast.makeText(getApplicationContext(), "No permission given to access location", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

}
