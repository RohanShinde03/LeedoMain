package com.tribeappsoft.leedo.util.stickyScrollView;

import android.os.Build;
import android.view.View;
import androidx.core.view.ViewCompat;

/**
 * Created by Rohan on 22/10/19..`
 */

public class PropertySetter {
    public static void setTranslationZ(View view, float translationZ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setTranslationZ(view, translationZ);
        } else if (translationZ != 0) {
            view.bringToFront();
            if (view.getParent() != null) {
                ((View) view.getParent()).invalidate();
            }
        }
    }
}
