package com.myschedulerassistant;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shwetha on 10/25/2017.
 */

public class ScheduleList {
    private static ScheduleList sScheduleList;
    private List<Task> mTasks;

    private ScheduleList(Context context) {
        mTasks = new ArrayList<>();
    }

    public static ScheduleList get(Context context) {
        if (sScheduleList == null) {
            sScheduleList = new ScheduleList(context);
        }
        return sScheduleList;
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public void addTask(Task task) {
        mTasks.add(task);
    }
}
