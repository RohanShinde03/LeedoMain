package com.tribeappsoft.leedo.util.stickyScrollView.provider;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

import com.tribeappsoft.leedo.util.stickyScrollView.interfaces.IScreenInfoProvider;


/**
 * Created by Rohan on 22/10/19.
 */

//@ParametersAreNonnullByDefault
public class ScreenInfoProvider implements IScreenInfoProvider {

    private final Context mContext;

    public ScreenInfoProvider(Context context) {
        mContext = context;
    }

    @Override
    public int getScreenHeight() {
        return getDeviceDimension().y;
    }

    @Override
    public int getScreenWidth() {
        return getDeviceDimension().x;
    }

    Point getDeviceDimension() {
        Point lPoint = new Point();
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        lPoint.x = metrics.widthPixels;
        lPoint.y = metrics.heightPixels;
        return lPoint;
    }
}
