package com.tribeappsoft.leedo.util.stickyScrollView.interfaces;

import androidx.annotation.StyleableRes;

/**
 * Created by Rohan on 22/10/19.
 */

public interface IResourceProvider {
    int getResourceId(@StyleableRes final int styleResId);
    void recycle();
}
