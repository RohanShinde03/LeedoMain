package com.tribeappsoft.leedo.util;

import android.content.DialogInterface;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by ${ROhan} on ${18/Jan/2018.
 */

public interface AlertAction
{
    void onPositiveButtonAction(AppCompatButton positiveButton, DialogInterface dialogInterface);
    void onNegativeButtonAction(AppCompatButton negativeButton, DialogInterface dialogInterface);
}
