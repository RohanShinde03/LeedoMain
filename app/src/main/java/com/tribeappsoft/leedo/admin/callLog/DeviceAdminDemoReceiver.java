package com.tribeappsoft.leedo.admin.callLog;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class DeviceAdminDemoReceiver extends DeviceAdminReceiver
{
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
    };

    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
    };

}
