package com.example.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationReceiver extends BroadcastReceiver{
    private static final String TAG = "NotificationReceiver" ;
    // public NotificationManager notificationManager;
    
    @Override
    public void onReceive(Context context, Intent intent) {
      //  Log.i(TAG, "received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

    int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
    Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);
    // Another option of notificationmanager
    // notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.notify(requestCode, notification);
   // Log.i(TAG,"notify now!");
    }
}
