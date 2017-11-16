package com.myschedulerassistant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Shwetha on 11/12/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    Double latitude, longitude;
    private DBHelper mDBHelper;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "latitude and longitude not found", Toast.LENGTH_SHORT).show();
            return;

        }


        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        Date date = new Date();
        long currentTime = date.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        date = calendar.getTime();
        long endTime = date.getTime();

        mDBHelper = DBHelper.getInstance(context);
        mDBHelper.open();
        Log.i("AlarmRec", "fetch for time range " + currentTime + " and " + endTime);
        List<Task> tasks = mDBHelper.getTasksOnTimeRange(currentTime, endTime);
        if (tasks.size() > 1) {
            String content = "";
            for (int i = 0; i < tasks.size(); i++) {
                content += tasks.get(i).getTaskName() + " at " + tasks.get(i).getTaskPlace();
            }
            Log.i("AlarmRec", "data fetched and following tasks for this range" + content);

            Double distanceAndDuration[] = null;
            while (distanceAndDuration == null) {
                distanceAndDuration = CommonFunctions.getDistanceAndDuration(latitude, longitude,
                        tasks.get(0).getlatitude(),
                        tasks.get(0).getlongitude(), null);
            }
            Toast.makeText(context, "distance " + distanceAndDuration[0] + " Time " + distanceAndDuration[1], Toast.LENGTH_SHORT).show();

            if (distanceAndDuration[1] > 0) {
                Log.i("AlarmRec", "comparing times " + distanceAndDuration[1] + " and current time " + currentTime + " scheduled time " + tasks.get(0).getDateTime());
                if (distanceAndDuration[1] + 10 >= ((tasks.get(0).getDateTime().getTime() - currentTime) / 60000)) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_task_notification)
                            .setContentTitle("You need to start now to reach your next location for " + tasks.get(0).getTaskName())
                            .setVibrate(new long[]{1000, 1000})
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setLights(0xff00ff00, 300, 100)
                            .setSound(uri)
                            .setContentText("");
                    Log.i("Notification", "Build Notification");
                    Intent resultIntent = new Intent(context, TaskListActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(TaskListActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(1, mBuilder.build());
                    Log.i("Notification", "Notify Notification");
                }
            }
        }
    }
}
