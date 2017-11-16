package com.myschedulerassistant;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    public static Boolean mLocationPermissionGranted;
    Button add_Schedule;
    Button view_Schedule;
    Button todays_Schedule;
    private AlarmManager manager;
    private DBHelper mDBHelper;
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
                        Intent intent1 = new Intent(MainActivity.this, TaskListActivity.class);
                        intent1.putExtra("TASKS", 1);
                        startActivity(intent1);
                        break;
                    case R.id.todaysSchedule:
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationPermission();
        add_Schedule = (Button) findViewById(R.id.addLocation);
        view_Schedule = (Button) findViewById(R.id.viewSchedule);
        todays_Schedule = (Button) findViewById(R.id.todaysSchedule);
        add_Schedule.setOnClickListener(onClickListener);
        view_Schedule.setOnClickListener(onClickListener);
        todays_Schedule.setOnClickListener(onClickListener);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //Set a pendingIntent
        /*PendingIntent pendingIntent;
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 300000;

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();*/

        Intent intent = new Intent(getApplicationContext(), GoogleService.class);
        MainActivity.this.startService(intent);
    }

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
