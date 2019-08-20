package com.example.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.Model.GallleryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */

public class PhotoGalleryFragment extends VisibleFragment {

    //  Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    //  Customize parameters
    private int mColumnCount = 1;

    private static final String TAG = "PhotoGalleryFragment";

    private  int cacheSize = 4 * 1024 * 1024; // 4MiB

    LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private List<GallleryItem.GalleryItem> mItems = new ArrayList<>();

    RecyclerView recyclerView;
    View view;


    private PhotoGalleryRVAdapter mAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private Bitmap cachedPic;
    private WebView mWebView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoGalleryFragment() {
    }

    // Customize parameter initialization
    @SuppressWarnings("unused")
    public static PhotoGalleryFragment newInstance(int columnCount) {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }


        setRetainInstance(true);
        setHasOptionsMenu(true);
        //new FetchItemsTask().execute();
        updateItems();

        /*Intent i = PollService.newIntent(getActivity());
        getActivity().startService(i);*/
        PollService.setServiceAlarm(getActivity(), true);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<PhotoHolder>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                        String url = photoHolder.mItem.getmUrl();
                        if (bitmapCache.get(url) == null) {
                            bitmapCache.put(url, thumbnail);
                        }
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);


        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            recyclerView = (RecyclerView) view;

          //  recyclerView.setHasFixedSize(true);
           // recyclerView.setItemViewCacheSize(40);
          //  recyclerView.setDrawingCacheEnabled(true);

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            mAdapter = new PhotoGalleryRVAdapter(GallleryItem.ITEMS);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.clearQueue();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_photo_gallery, menu);
        
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);

        if(PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();

            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {

        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GallleryItem.GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }


        @Override
        protected List<GallleryItem.GalleryItem> doInBackground(Void... voids) {

           // String query = "robot";
        if(mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }

        }

        @Override
        protected void onPostExecute(List<GallleryItem.GalleryItem> items) {
            mItems = items;
            mAdapter = new PhotoGalleryRVAdapter(mItems);
            recyclerView.setAdapter(mAdapter);
        }
    }

    private class PhotoGalleryRVAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private final List<GallleryItem.GalleryItem> mValues;
        private final String TAG = "rv";
        private SearchView searchView;

        public PhotoGalleryRVAdapter(List<GallleryItem.GalleryItem> items) {
            mValues = items;
        }
        Drawable placeholder;

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            //getActivity() or getContext()
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }


        @Override
        public void onBindViewHolder(final PhotoHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.bindGalleryItem(holder.mItem);

            cachedPic = bitmapCache.get(holder.mItem.getmUrl());
            if(cachedPic==null) {
                placeholder = getResources().getDrawable(R.drawable.ic_launcher_background);
                mThumbnailDownloader.queueThumbnail(holder, holder.mItem.getmUrl());
            }else{
                holder.mContentView.setText("cached");
                Drawable drawable = new BitmapDrawable(getResources(), cachedPic);
                Log.d(TAG, "onBindViewHolder: cached");
                placeholder = drawable;
            }
            holder.mItemImageView.setImageDrawable(placeholder);
            holder.bindDrawable(placeholder);
        }


        @Override
        public int getItemCount() {
            return mValues.size();
        }

    }

    public class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final View mView;
        private final ImageView mItemImageView;
        public final TextView mContentView;
        public GallleryItem.GalleryItem mItem;

        public PhotoHolder(View view) {
            super(view);
            mView = view;

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            mContentView = (TextView) view.findViewById(R.id.content);
            mView.setOnClickListener(this);
        }

        // Do we need to bind them manually?
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GallleryItem.GalleryItem galleryItem) {
            mItem = galleryItem;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
           // Intent i = new Intent(Intent.ACTION_VIEW, mItem.getPhotoPageUri());
            Intent i = PhotoPageActivity
                    .newIntent(getActivity(), mItem.getPhotoPageUri());
            startActivity(i);
        }
    }


}