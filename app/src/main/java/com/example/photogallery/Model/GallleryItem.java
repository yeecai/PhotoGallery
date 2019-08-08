package com.example.photogallery.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class GallleryItem {

    /**
     * An array of sample (Model) items.
     */
    public static final List<GalleryItem> ITEMS = new ArrayList<GalleryItem>();

    /**
     * A map of sample (Model) items, by ID.
     */
    public static final Map<String, GalleryItem> ITEM_MAP = new HashMap<String, GalleryItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createGalleryItem(i));
        }
    }

    private static void addItem(GalleryItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.mId, item);
    }

    private static GalleryItem createGalleryItem(int position) {
        return new GalleryItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


    public static class GalleryItem {
        private String mCaption;
        private String mId;
        private String mUrl;

        public GalleryItem(String mCaption, String mId, String mUrl) {
            this.mCaption = mCaption;
            this.mId = mId;
            this.mUrl = mUrl;
        }

        @Override
        public String toString() {
            return mCaption;
        }

        public String getmCaption() {
            return mCaption;
        }

        public void setmCaption(String mCaption) {
            this.mCaption = mCaption;
        }

        public String getmId() {
            return mId;
        }

        public void setmId(String mId) {
            this.mId = mId;
        }

        public String getmUrl() {
            return mUrl;
        }

        public void setmUrl(String mUrl) {
            this.mUrl = mUrl;
        }
    }
}
