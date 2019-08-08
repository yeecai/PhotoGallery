package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import com.example.photogallery.Model.GallleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "8cf0d1896f61decdfe551b77c4deda82";
    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
   // private static final String testURL = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=8cf0d1896f61decdfe551b77c4deda82&format=json&nojsoncallback=1";


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            //wont reach endpoint untill call getInputStream()
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int finishRead = 0;
            byte[] buffer = new byte[1024];

            while ((finishRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, finishRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }



    public List<GallleryItem.GalleryItem> fetchItems() {
        List<GallleryItem.GalleryItem> items = new ArrayList<>();
            try {
                //https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=8cf0d1896f61decdfe551b77c4deda82&format=json&nojsoncallback=1
                //String url = Uri.parse("http://api.flickr.com/services/rest/")
                /*String url =Uri.parse(ENDPOINT)
                        .buildUpon()
                        .appendQueryParameter("method", "flickr.photos.getRecent")
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("extras", "url_s")
                        .build().toString();*/
                //  <head><title>301 Moved Permanently</title></head>
               String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=8cf0d1896f61decdfe551b77c4deda82&format=json&nojsoncallback=1";

                String jsonString = getUrlString(url);
                Log.i(TAG,"Received JSON: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                parseItems(items, jsonBody);
            } catch (IOException e) {
                Log.e(TAG, "falied to fetch items", e);
            }catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON", e);
            }

            return items;
    }

    private void parseItems(List<GallleryItem.GalleryItem> items, JSONObject jsonBody) throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GallleryItem.GalleryItem item = new GallleryItem.GalleryItem(null,null,null);
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
