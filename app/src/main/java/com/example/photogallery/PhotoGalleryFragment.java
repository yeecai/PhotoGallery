package com.example.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.Model.GallleryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */

public class PhotoGalleryFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private static final String TAG = "PhotoGalleryFragment";

    private List<GallleryItem.GalleryItem> mItems = new ArrayList<>();

    RecyclerView recyclerView;
    View view;
    private PhotoGalleryRVAdapter mAdapter;
    private ThumbnailDownloader<PhotoGalleryRVAdapter.PhotoHolder> mThumbnailDownloader;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoGalleryFragment() {
    }

    // TODO: Customize parameter initialization
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
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoGalleryRVAdapter.PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoGalleryRVAdapter.PhotoHolder photoHolder, Bitmap thumbnail) {
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
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
         //   Drawable placeholder = getResources().getDrawable(R.drawable.gizmo); //? why won't work in adapter
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


    private class FetchItemsTask extends AsyncTask<Void, Void, List<GallleryItem.GalleryItem>> {
        @Override
        protected List<GallleryItem.GalleryItem> doInBackground(Void... voids) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GallleryItem.GalleryItem> items) {
            mItems = items;
            mAdapter = new PhotoGalleryRVAdapter(mItems);
            recyclerView.setAdapter(mAdapter);
        }
    }

    private class PhotoGalleryRVAdapter extends RecyclerView.Adapter<PhotoGalleryRVAdapter.PhotoHolder> {

        private final List<GallleryItem.GalleryItem> mValues;
        private final String TAG = "rv";

        public PhotoGalleryRVAdapter(List<GallleryItem.GalleryItem> items) {
            mValues = items;
        }
        Drawable placeholder = getResources().getDrawable(R.drawable.gizmo);

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);*/

            //getActivity() or getContext()
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(final PhotoHolder holder, int position) {
            holder.mItem = mValues.get(position);

            // holder.mIdView.setText(mValues.get(position).getmId());
            holder.mContentView.setText(mValues.get(position).getmCaption());

            // TODO download a picture to replay the default

            holder.mItemImageView.setImageDrawable(placeholder);
            holder.bindDrawable(placeholder);

            mThumbnailDownloader.queueThumbnail(holder, holder.mItem.getmUrl());

        }


        @Override
        public int getItemCount() {
            return mValues.size();
        }


        public class PhotoHolder extends RecyclerView.ViewHolder {
            public final View mView;
            // public final TextView mIdView;
            private final ImageView mItemImageView;
            public final TextView mContentView;

            public GallleryItem.GalleryItem mItem;

            public PhotoHolder(View view) {
                super(view);
                mView = view;
                //  mIdView = (TextView) view.findViewById(R.id.item_number);
                mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
                mContentView = (TextView) view.findViewById(R.id.content);
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
        }

    }
}