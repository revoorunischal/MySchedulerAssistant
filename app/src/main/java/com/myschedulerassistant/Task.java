package com.myschedulerassistant;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shwetha on 10/25/2017.
 */

public class Task {
    private long mId;
    private String mTaskName;
    private String mTaskPlace;
    private Date mDateTime;
    private Double mlatitude;
    private Double mlongitude;
    private Double mduration;
    public Task() {
        //mId = UUID.randomUUID();
        //mDateTime = new Date();
    }

    public Task(long id, String taskName, String taskPlace, Date dateTime, Double latitude, Double longitude, Double duration) {
        mId = id;
        mTaskName = taskName;
        mTaskPlace = taskPlace;
        mDateTime = dateTime;
        mlatitude = latitude;
        mlongitude = longitude;
        mduration = duration;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getTaskName() {
        return mTaskName;
    }

    public void setTaskName(String mtaskName) {
        this.mTaskName = mtaskName;
    }


    public Date getDateTime() {
        return mDateTime;
    }

    public void setDateTime(Date dateTime) {
        mDateTime = dateTime;
    }

    public String getTaskPlace() {
        return mTaskPlace;
    }

    public void setTaskPlace(String taskPlace) {
        mTaskPlace = taskPlace;
    }

    public String formatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh-mm a");
        return sdf.format(mDateTime).toString();
    }

    public Double getlatitude() {
        return mlatitude;
    }

    public void setlatitude(Double latitude) {
        mlatitude = latitude;
    }

    public Double getlongitude() {
        return mlongitude;
    }

    public void setlongitude(Double longitude) {
        mlongitude = longitude;
    }

    public Double getDuration() {
        return mduration;
    }

    public void setDuration(Double duration) {
        mduration = duration;
    }

    @Override
    public String toString() {
        return "Task{" +
                "mId=" + mId +
                ", mTaskName='" + mTaskName + '\'' +
                ", mTaskPlace='" + mTaskPlace + '\'' +
                ", mDateTime=" + mDateTime +
                ", mlatitude=" + mlatitude +
                ", mlongitude=" + mlongitude +
                ", mduration=" + mduration +
                '}';
    }
}
