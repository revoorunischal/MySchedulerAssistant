package com.myschedulerassistant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Pattern;


/**
 * Created by nischal on 9/30/2017.
 */

public class AddLocation extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1;
    SharedPreferences sharedpreferences;
    Place selectedPlace;
    ProgressBar progressBar;
    private EditText dateTime;
    private EditText placeSelected;
    private EditText purpose;
    private Button selectDate;
    private Button addSchedule;
    private LinearLayout addScheduleLayout;
    private LinearLayout addScheduleLoaderLayout;
            private View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                        switch (v.getId()) {
                            case R.id.selectDate:
                                final String date_Time = dateTime.getText().toString();
                                final int mYear;
                                final int mMonth;
                                final int mDay;
                                final int mHour;
                                final int mMinute;
                                if(date_Time == null || date_Time.equals("")){
                                    Calendar c = Calendar.getInstance();
                                    mYear = c.get(Calendar.YEAR);
                                    mMonth = c.get(Calendar.MONTH);
                                    mDay = c.get(Calendar.DAY_OF_MONTH);
                                    mHour = c.get(Calendar.HOUR_OF_DAY);
                                    mMinute = c.get(Calendar.MINUTE);
                                }else{
                                    Log.i("Date time","date fetched "+date_Time);
                                    String[] date_time_arr  = date_Time.split(Pattern.quote(" "));
                                    String date             = date_time_arr[0];
                                    String time             = date_time_arr[1];
                                    Log.i("Date time","date parsed "+date);
                                    Log.i("Date time","time parsed "+time);
                                    String[] date_array     = date.split("-");
                                    mDay                    = Integer.valueOf(date_array[0]);
                                    mMonth                  = Integer.valueOf(date_array[1])-1;
                                    mYear                   = Integer.valueOf(date_array[2]);
                                    String[] time_array     = time.split(Pattern.quote(":"));
                                    mHour                   = Integer.valueOf(time_array[0]);
                                    mMinute                 = Integer.valueOf(time_array[1]);
                                    Log.i("Date time","values day "+mDay+" month "+mMonth+" mYear "+mYear+" hour "+mHour+" minutes "+mMinute);
                                }
                                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, final int year,
                                                                  final int monthOfYear, final int dayOfMonth) {
                                                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                                                        new TimePickerDialog.OnTimeSetListener() {

                                                            @Override
                                                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                                                  int minute) {

                                                                dateTime.setText(dayOfMonth+"-"+(monthOfYear+1)+"-"+year+" "
                                                                +hourOfDay+":"+minute);
                                                            }
                                                        }, mHour, mMinute, false);
                                                timePickerDialog.show();
                                            }
                                        }, mYear, mMonth, mDay);
                                datePickerDialog.show();
                                break;
                            case R.id.add_schedule:
                                //TODO to be modified by messi adding in shared preferences  as of now modify it to db
                                try {
                                    JSONObject json = new JSONObject();
                                    json.put(MainActivity.PLACE, selectedPlace);
                                    json.put(MainActivity.PLACE_NAME, selectedPlace.getName());
                                    json.put(MainActivity.PLACE_ADDRESS, selectedPlace.getAddress());
                                    json.put(MainActivity.PLACE_LATITUDE, selectedPlace.getLatLng().latitude);
                                    json.put(MainActivity.PLACE_LONGITUDE, selectedPlace.getLatLng().longitude);
                                    json.put(MainActivity.PURPOSE, purpose.getText().toString());
                                    json.put(MainActivity.DATE_AND_TIME, dateTime.getText().toString());
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
                                            Context.MODE_PRIVATE);
                                    String jsonArrStr = sharedpreferences.getString(MainActivity.SCHEDULER_ARRAY, null);
                                    if (jsonArrStr != null) {
                                        JSONArray jsonArray = new JSONArray(jsonArrStr);
                                        jsonArray.put(json);
                                        editor.putString(MainActivity.SCHEDULER_ARRAY, jsonArray.toString());
                                    } else {
                                        JSONArray jsonArray = new JSONArray();
                                        jsonArray.put(json);
                                        editor.putString(MainActivity.SCHEDULER_ARRAY, jsonArray.toString());
                                    }
                                    editor.commit();
                                    Toast.makeText(getApplicationContext(), "Schedule Added", Toast.LENGTH_LONG).show();
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }

            };

    //to setup the map object
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.add_location);
        dateTime = (EditText) findViewById(R.id.dateTime);
        selectDate = (Button) findViewById(R.id.selectDate);
        selectDate.setOnClickListener(onClickListener);
        addSchedule = (Button) findViewById(R.id.add_schedule);
        addSchedule.setOnClickListener(onClickListener);
        placeSelected = (EditText) findViewById(R.id.selected_place);
        purpose = (EditText) findViewById(R.id.purpose);
        addScheduleLayout = (LinearLayout) findViewById(R.id.addscheduleScreen);
        addScheduleLoaderLayout = (LinearLayout) findViewById(R.id.addscheduleLoadScreen);
        progressBar = (ProgressBar) findViewById(R.id.addProgress);
        addScheduleLayout.setVisibility(View.INVISIBLE);
        addScheduleLoaderLayout.setVisibility(View.VISIBLE);
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
                Context.MODE_PRIVATE);
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this.getApplicationContext(), data);
                selectedPlace = place;
                String toastMsg = String.format("Place selected : %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                placeSelected.setText(place.getName());
                addScheduleLoaderLayout.setVisibility(View.INVISIBLE);
                addScheduleLayout.setVisibility(View.VISIBLE);

            }
        }
    }
}
