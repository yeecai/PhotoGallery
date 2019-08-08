package com.example.photogallery;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.PhotoGalleryFragment.OnListFragmentInteractionListener;
import com.example.photogallery.Model.GallleryItem.GalleryItem;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * {@link RecyclerView.Adapter} that can display a {@link GalleryItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PhotoGalleryFragmentRecyclerViewAdapter extends RecyclerView.Adapter<PhotoGalleryFragmentRecyclerViewAdapter.ViewHolder> {

    private final List<GalleryItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final String TAG = "rv";

    public PhotoGalleryFragmentRecyclerViewAdapter(List<GalleryItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getmId());
        holder.mContentView.setText(mValues.get(position).getmCaption());

        /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public GalleryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public void updateData(List<GalleryItem> mItems) {
        mValues.clear();
        mValues.addAll(mItems);

        notifyDataSetChanged();
        Log.i(TAG, "updated");
       // Log.i(TAG, "updated", (Throwable) mItems);
//        mValues.get(1).getmId()
    }
}
