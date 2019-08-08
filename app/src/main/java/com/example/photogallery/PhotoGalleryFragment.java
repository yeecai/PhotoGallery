package com.example.photogallery;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.Model.GallleryItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PhotoGalleryFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final String TAG = "PhotoGalleryFragment";

    private List<GallleryItem.GalleryItem> mItems = new ArrayList<>();

    RecyclerView recyclerView;
    View view;
    private PhotoGalleryFragmentRecyclerViewAdapter mAdapter;

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
        //ButterKnife.bind(this);
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
            mAdapter = new PhotoGalleryFragmentRecyclerViewAdapter(GallleryItem.ITEMS, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }



    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(GallleryItem item);
    }


    private class FetchItemsTask extends AsyncTask<Void,Void,List<GallleryItem.GalleryItem>> {
        @Override
        protected List<GallleryItem.GalleryItem> doInBackground(Void... voids) {
            /*try {
                String result = new FlickrFetchr()
                        //.getUrlString("https://www.bignerdranch.com");
                     .getUrlString("http://120.78.214.127/");
                   //.getUrlString("https://www.google.com");
                Log.i(TAG, "Fetched contents of url"+result);
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Failed to fetch url:", e);
            }*/

            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GallleryItem.GalleryItem> items) {
            mItems = items;
          //  mAdapter.updateData(items);
            mAdapter = new PhotoGalleryFragmentRecyclerViewAdapter(mItems, mListener);
            recyclerView.setAdapter(mAdapter);
        }
    }

}
