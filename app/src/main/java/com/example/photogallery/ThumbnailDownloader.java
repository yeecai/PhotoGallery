package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private final Handler mResponseHandler;
    private boolean mHasQuit;

    private Handler mRequestHandler;



    public void setmThumbnailDownloadListener(ThumbnailDownloadListener<T> mThumbnailDownloadListener) {
        this.mThumbnailDownloadListener = mThumbnailDownloadListener;
    }

    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }


    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a reqeuest for URL: " + mRequestMap.get(target));

                    try {
                        handleRequest(target);
                    } catch (IOException e) {
                        Log.e(TAG, "Error downloading photo", e);
                    }
                }
            }
        };
    }

    /* Download pic and save to bitmap */
    private void handleRequest(T target) throws IOException {
        final String url = mRequestMap.get(target);
        byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
        // cache it as long as it's downloaded

        final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0 ,bitmapBytes.length);

       // synchronized (bitmapCache) {

       // }
        Log.i(TAG, "bitmap created ");

        /* A callback trigger bindDrawable() in PhotoGalleryFragment.onCreate()*/
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mRequestMap.get(target) != url || mHasQuit) {
                    return;
                }

                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);

            }
        });

    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

}
