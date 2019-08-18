package com.example.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.photogallery.Model.GallleryItem;

import java.util.List;

public class PollService extends IntentService {
    public static final String PREM_PRIVATE = "com.example.photogallery.PRIVATE" ;
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public NotificationManager notificationManager;


    private static final String TAG = "PollService";

    private static int POLL_INTERVAL = 1000 * 60;

   // private static long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String ACTION_SHOW_NOTIFICATION = "com.example.photogallery.S​H​O​W​_​N​O​T​I​F​I​C​A​T​I​O​N";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
       if(!i​s​N​e​t​w​o​r​k​A​v​a​i​l​a​b​l​e​A​n​d​C​o​n​n​e​c​t​e​d​()){
           return;
       }

       String query = QueryPreferences.getStoredQuery(this);
       String lastResultId = QueryPreferences.getLastResultId(this);
        List<GallleryItem.GalleryItem> items;

        if(query == null) {
            items = new FlickrFetchr().fetchRecentPhotos();
        } else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getmId();

        if (resultId.equals(lastResultId)) {
            Log.i(TAG,"Nothing new"+resultId);
        } else {
            Log.i(TAG,"New photos!"+ resultId);
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channelId;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channelId = new NotificationChannel("1234567", "Notifaction with channelId", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channelId);
                mBuilder.setChannelId("1234567");
            }

            Notification notifcation = mBuilder.build();

           // notificationManager.notify(1, mBuilder.build());

            //sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PREM_PRIVATE);
            showBackgroundNotification(0, notifcation);

            Log.i(TAG,"notify now!");
        }


        QueryPreferences.setLastResultId(this, resultId);

    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PREM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }


    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);

        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreferences.setAlarmOn(context,isOn);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi =  PendingIntent
                .getService(context,0,i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private boolean i​s​N​e​t​w​o​r​k​A​v​a​i​l​a​b​l​e​A​n​d​C​o​n​n​e​c​t​e​d​() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        // available and connected
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean i​s​N​e​t​w​o​r​k​C​o​n​n​e​c​t​e​d = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return i​s​N​e​t​w​o​r​k​C​o​n​n​e​c​t​e​d;
    }
}
