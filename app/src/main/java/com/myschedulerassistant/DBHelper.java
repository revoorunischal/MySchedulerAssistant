package com.myschedulerassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Shwetha on 10/31/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "Tasks.db";
    private static final int DATABASE_VERSION = 1;
    private static DBHelper dbHelper = null;
    private SQLiteDatabase database = null;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    public void open() throws SQLException {
        database = getWritableDatabase();
    }


    public Task createTask(String taskName, Date taskDate, String placeName, Double latitude, Double longitude, Double duration) {
        ContentValues values = new ContentValues();
        values.put("taskName", taskName);
        values.put("taskDate", taskDate.getTime());
        values.put("taskPlace", placeName);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("duration", duration);
        long insertId = database.insert("TASKS", null, values);

        if (insertId != -1) {
            return new Task(insertId, taskName, placeName, taskDate, latitude, longitude, duration);
        }

        Log.e(TAG, "Error inserting data!");
        return null;
    }


    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<Task>();

        Cursor cursor = database.rawQuery("select * from tasks order by taskDate ASC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = new Task();
            task.setId(cursor.getLong(0));
            task.setTaskName(cursor.getString(1));
            task.setDateTime(new Date((cursor.getLong(2))));
            task.setTaskPlace(cursor.getString(3));
            task.setlatitude(cursor.getDouble(4));
            task.setlongitude(cursor.getDouble(5));
            task.setDuration(cursor.getDouble(6));
            tasks.add(task);
            cursor.moveToNext();
        }
        cursor.close();
        return tasks;
    }

    public List<Task> getTasksOnDate(Date date) {
        List<Task> tasks = new ArrayList<Task>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        date = calendar.getTime();
        long startDate = calendar.getTime().getTime();
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        date = calendar.getTime();
        long endDate = date.getTime();
        Cursor cursor = database.rawQuery("select * from tasks where taskDate >='" + startDate + "' and taskDate<='" + endDate + "' order by taskDate ASC", null);
        Log.i("DB", "select * from tasks where taskDate >='" + startDate + "' and taskDate<=" + endDate + "' order by taskDate ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = new Task();
            task.setId(cursor.getLong(0));
            task.setTaskName(cursor.getString(1));
            task.setDateTime(new Date((cursor.getLong(2))));
            task.setTaskPlace(cursor.getString(3));
            task.setlatitude(cursor.getDouble(4));
            task.setlongitude(cursor.getDouble(5));
            task.setDuration(cursor.getDouble(6));
            tasks.add(task);
            cursor.moveToNext();
        }
        cursor.close();
        return tasks;
    }


    public void deleteTask(long id) {
        database.execSQL("delete from TASKS where id = \"" + id + "\"");
    }

    public Task getSelectedTask(int position) {
        List<Task> tasks = getAllTasks();
        return tasks.get(position);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS TASKS(" +
                "id INTEGER PRIMARY KEY, " +
                "taskName TEXT NOT NULL, " +
                "taskDate LONG NOT NULL, " +
                "taskPlace TEXT NOT NULL, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "duration REAL NOT NULL " +
                ");");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        db.execSQL("DROP TABLE IF EXISTS TASKS");
        onCreate(db);
    }

}

