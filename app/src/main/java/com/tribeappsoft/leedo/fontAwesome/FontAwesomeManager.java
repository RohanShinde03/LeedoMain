package com.tribeappsoft.leedo.fontAwesome;

/**
 * Created by rohan on 1/3/18.
 */
import android.content.Context;
import android.graphics.Typeface;

public class FontAwesomeManager
{

    public static final String ROOT = "fonts/";
    public static final String FONTAWESOME = "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

}