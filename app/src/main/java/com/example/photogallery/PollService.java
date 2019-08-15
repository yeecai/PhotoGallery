package com.example.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.photogallery.Model.GallleryItem;

import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;

public class PollService extends IntentService {
    private NotificationManager notificationManager;


    private static final String TAG = "PollService";

    private static int POLL_INTERVAL = 1000 * 60;

   // private static long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

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
//        Log.i(TAG, "Received an intent"+ intent);

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
        }

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

        notificationManager.notify(1, mBuilder.build());
        Log.i(TAG,"notify now!");




       /* notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NewMessageNotification newMessageNotification = new NewMessageNotification(this, notificationManager);

        synchronized (newMessageNotification) {
            Log.i(TAG, "notify");
            newMessageNotification.notify();
        }
        QueryPreferences.setLastResultId(this, resultId);
*/
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
