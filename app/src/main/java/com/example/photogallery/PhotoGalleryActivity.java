package com.example.photogallery;

import androidx.fragment.app.Fragment;

public class PhotoGalleryActivity  extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance(3);
    }
}
