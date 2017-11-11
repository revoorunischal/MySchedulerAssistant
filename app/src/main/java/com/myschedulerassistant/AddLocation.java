package com.myschedulerassistant;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddLocation extends AppCompatActivity {
    private static final int PLACE_PICKER_REQUEST = 1;
    SharedPreferences sharedpreferences;
    Place selectedPlace = null;
    private EditText dateTime;
    private EditText placeSelected;
    private EditText duration;
    private EditText purpose;
    private Button selectDate;
    private Button selectPlace;
    private Button addTask;
    private LinearLayout addScheduleLayout;
    //Add the task details as objects
    private Task currentTask;
    private Date selectedDate = null;
    private String taskName, taskPlace;
    private Double latitude, longitude, taskDuration;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            hideKeyboard(v);
            switch (v.getId()) {
                case R.id.selectDate:
                    final String date_Time = dateTime.getText().toString();
                    final int mYear;
                    final int mMonth;
                    final int mDay;
                    final int mHour;
                    final int mMinute;
                    if (selectedDate == null) {
                        Calendar c = Calendar.getInstance();
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                        mHour = c.get(Calendar.HOUR_OF_DAY);
                        mMinute = c.get(Calendar.MINUTE);
                        selectedDate = c.getTime();
                    } else {
                        Calendar c = Calendar.getInstance();
                        c.setTime(selectedDate);
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                        mHour = c.get(Calendar.HOUR_OF_DAY);
                        mMinute = c.get(Calendar.MINUTE);

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
                                                    String date = (monthOfYear + 1) + "-" + dayOfMonth + "-" + year + " " + hourOfDay + ":" + minute;
                                                    try {
                                                        SimpleDateFormat TimePickersdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                                                        selectedDate = TimePickersdf.parse(date);
                                                        dateTime.setText(MainActivity.sdf.format(selectedDate));
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        //Worst case scenario Handling
                                                        dateTime.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year + " "
                                                                +hourOfDay+":"+minute);
                                                        selectedDate = new Date(year - 1900, monthOfYear, dayOfMonth, hourOfDay, minute);
                                                    }

                                                }
                                            }, mHour, mMinute, false);
                                    timePickerDialog.show();
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();

                    break;
                case R.id.AddSchedule:
                    if (selectedPlace == null) {
                        CommonFunctions.showAlertMsg("Data Missing", "Please select a place", AddLocation.this);
                        break;
                    }
                    taskPlace = selectedPlace.getName().toString();
                    latitude = selectedPlace.getLatLng().latitude;
                    longitude = selectedPlace.getLatLng().longitude;
                    taskName = purpose.getText().toString();

                    if (taskName == null || taskName.trim().isEmpty()) {
                        CommonFunctions.showAlertMsg("Data Missing", "Task Name Mandatory ", AddLocation.this);
                        break;
                    }
                    String durationVal = duration.getText().toString();
                    if (taskName == null || taskName.trim().isEmpty()) {
                        CommonFunctions.showAlertMsg("Data Missing", "Duration is mandatory", AddLocation.this);
                        break;
                    }
                    taskDuration = Double.valueOf(durationVal);

                    DBHelper dbHelper = DBHelper.getInstance(AddLocation.this);
                    dbHelper.open();
                    currentTask = dbHelper.createTask(taskName, selectedDate, taskPlace, latitude, longitude, taskDuration);
                    if (currentTask != null) {
                        List<Task> tasksOnThisdate = dbHelper.getTasksOnDate(selectedDate);
                        if (tasksOnThisdate.size() > 1) {
                            int i;
                            for (i = 0; i < tasksOnThisdate.size(); i++) {
                                if (tasksOnThisdate.get(i).getId() == currentTask.getId()) {
                                    break;
                                }
                            }
                            if (i == 0 || tasksOnThisdate.size() == 2) {
                                Log.i("Add Location", "first if clause");
                                if (checkIfTaskCannotBeAdded(tasksOnThisdate.get(0), tasksOnThisdate.get(1), dbHelper)) {
                                    break;
                                }
                                Log.i("Add Location ", "Allowed to add");
                            } else if (i == tasksOnThisdate.size() - 1) {
                                Log.i("Add Location", "second if clause");
                                if (checkIfTaskCannotBeAdded(tasksOnThisdate.get(i - 1), tasksOnThisdate.get(i), dbHelper)) {
                                    break;
                                }
                                Log.i("Add Location ", "Allowed to add");
                            } else {
                                Log.i("Add Location", "else");
                                Log.i("Add Location", "first route");
                                if (checkIfTaskCannotBeAdded(tasksOnThisdate.get(i - 1), tasksOnThisdate.get(i), dbHelper)) {
                                    break;
                                }
                                Log.i("Add Location ", "Allowed to add");
                                Log.i("Add Location", "first route");
                                if (checkIfTaskCannotBeAdded(tasksOnThisdate.get(i), tasksOnThisdate.get(i + 1), dbHelper)) {
                                    break;
                                }
                                Log.i("Add Location", "second route");
                            }
                        } else if (tasksOnThisdate.size() == 1) {
                            Log.i("Add Location", "only one task for today so adding directly");
                        } else {
                            //Should not reach this
                            Log.e("Add Location", "failed to add the location retry");
                            CommonFunctions.showAlertMsg("Failed", "Failed to add the record please try again", AddLocation.this);
                            break;
                        }
                    } else {
                        CommonFunctions.showAlertMsg("Failed", "Failed to add the record please try again", AddLocation.this);
                        break;
                    }
                    Toast.makeText(AddLocation.this, "Schedule Added", Toast.LENGTH_LONG).show();

                    finish();
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
        placeSelected = (EditText) findViewById(R.id.selected_place);
        selectPlace = (Button) findViewById(R.id.selectPlace);
        purpose = (EditText) findViewById(R.id.purpose);
        duration = (EditText) findViewById(R.id.duration);
        //to hide keyboard
        purpose.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        addTask = (Button) findViewById(R.id.AddSchedule);
        addTask.setOnClickListener(onClickListener);
        addScheduleLayout = (LinearLayout) findViewById(R.id.addscheduleScreen);
        addScheduleLayout.setVisibility(View.VISIBLE);
        selectPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AddLocation.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this.getBaseContext(), data);
                selectedPlace = place;
                //String toastMsg = String.format("Place selected : %s", place.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                placeSelected.setText(place.getName());
                addScheduleLayout.setVisibility(View.VISIBLE);

            }
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
                Context.MODE_PRIVATE);
        super.onResume();
    }

    public boolean checkIfTaskCannotBeAdded(Task startTask, Task endTask, DBHelper dbHelper) {

        Double[] distAndDuration = null;
        while (distAndDuration == null) {
            distAndDuration = CommonFunctions.getDistanceAndDuration(startTask.getlatitude(),
                    startTask.getlongitude(), endTask.getlatitude(),
                    endTask.getlongitude(), null);
        }
        Log.i("Add Location", "duration of travel " + distAndDuration[1] + " duration of task " + startTask.getDuration());
        Double timeToTravel = distAndDuration[1] + (startTask.getDuration() * 60.0);
        Log.i("Add Location", "time to travel " + timeToTravel);
        long diffInTaskSchedule = endTask.getDateTime().getTime() - startTask.getDateTime().getTime();

        Log.i("Add Location", "diffInSchedule time milliseconds " + diffInTaskSchedule);
        diffInTaskSchedule = (diffInTaskSchedule / 1000) / 60;
        Log.i("Add Location", "diffInSchedule time minutes " + diffInTaskSchedule);
        timeToTravel = timeToTravel - (timeToTravel * 0.05);
        Log.i("Add Location", "time to travel with error threshold " + timeToTravel);
        if (timeToTravel > diffInTaskSchedule) {
            CommonFunctions.showAlertMsg("Conflict", "This Task is conflicting with another task and " +
                    " you might fall short of time covering both so try to prepone or postpone the task", AddLocation.this);
            dbHelper.deleteTask(currentTask.getId());
            return true;
        }
        return false;
    }
}
