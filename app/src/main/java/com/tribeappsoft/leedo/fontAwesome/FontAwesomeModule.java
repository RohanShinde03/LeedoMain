package com.tribeappsoft.leedo.fontAwesome;

/**
 * Created by rohan on 1/3/18.
 */

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

public class FontAwesomeModule implements IconFontDescriptor
{

    @Override
    public String ttfFileName()
    {
        return "iconify/android-iconify-fontawesome.ttf";
    }

    @Override
    public Icon[] characters() {
        return FontAwesomeIcons.values();
    }
}