package com.example.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import com.example.photogallery.Model.GallleryItem;

import java.util.List;

public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000*10;

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

        QueryPreferences.setLastResultId(this, resultId);
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
