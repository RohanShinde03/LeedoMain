package com.tribeappsoft.leedo.admin.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.models.NotificationModel;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

public class AlarmReceiver extends BroadcastReceiver
{
    private String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "Alarm received!", Toast.LENGTH_LONG).show();
        Log.e(TAG, "onReceive: ");
        if (intent!=null){
            String  reminderText  = intent.getStringExtra("reminderText");
            sendLocalNotification(context, reminderText);
        }
    }

    private void sendLocalNotification(Context context, String reminderText)
    {
        Log.e(TAG, "sendLocalNotification: text "+reminderText );
        String channelId = context.getString(R.string.default_notification_channel_id);
        //  PendingIntent.getActivity(context, MY_UNIQUE_VALUE , notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        //int request_code = new Random().nextInt();
        final int requestCode = (int) System.currentTimeMillis() / 1000;
        //int notifyId = notId + requestCode;
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                requestCode,
                new Intent(context, SalesPersonBottomNavigationActivity.class).putExtra("notifyReminders", true),
                PendingIntent.FLAG_ONE_SHOT);

        NotificationModel model = new NotificationModel();
        model.setTitle("You have a Reminder");
        model.setContent(reminderText);
        model.setPicture("");
        model.setPage("ReminderPage");
        model.setDate(Helper.getDateTime());
        //call the method that saveNotification
        SaveNotification(model, context);

        //Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri notification_sound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.reminder);
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(getNotificationIcon()) // R.mipmap.ic_launcher
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon_leedo_foreground))
                .setContentTitle("You have a Reminder")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(reminderText)
                .setNumber(1)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(reminderText))
                .setSound(notification_sound)
                //Vibration
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                //LED
                //.setLights(Color.RED, 3000, 3000)
                //Tone
                //.setSound( Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/raw/reminder"))
                //.setColor(getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel
                        (channelId, "Leedo App",
                                NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(requestCode, builder.build());
        }

        try {
            Uri path = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/raw/reminder.mp3");
            // The line below will set it as a default ring tone replace
            // RingtoneManager.TYPE_RINGTONE with RingtoneManager.TYPE_NOTIFICATION
            // to set it as a notification tone
            RingtoneManager.setActualDefaultRingtoneUri(
                    getApplicationContext(), RingtoneManager.TYPE_RINGTONE,
                    path);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), path);
            r.play();
        }
        catch (Exception e) {
            e.printStackTrace();
        }



        //vibrate when scan completed
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
        } else {
            v.vibrate(2000);  // Vibrate method for below API Level 26
        }

        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        //r.play();
        //AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //play sound when Scan Completed
        //final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.reminder);
        //mp.start();

    }


    private int getNotificationIcon()
    {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
                //R.drawable.img_app_icon_new : R.drawable.img_app_icon_new;
                R.mipmap.app_icon_leedo_foreground : R.mipmap.app_icon_leedo_foreground;
    }

    private void SaveNotification(NotificationModel model, Context context)
    {

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();

        if (sharedPreferences!=null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("title", model.getTitle());
                jsonObject.put("content", model.getContent());
                jsonObject.put("picture", model.getPicture());
                jsonObject.put("page", model.getPage());
                jsonObject.put("date", model.getDate());
                jsonObject.put("data", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String notification = null;if (sharedPreferences.getString("NotificationModel", null)!=null) notification = sharedPreferences.getString("NotificationModel", null);
            if (notification!=null) {
                try {
                    int count =0;
                    //JSONArray newJsonArray = new JSONArray(notification);
                    JSONArray newJsonArray = new JSONArray();
                    // 1st object
                    newJsonArray.put(jsonObject);
                    //Log.e("new_ary", newJsonArray.toString());
                    JSONArray oldJsonArray = new JSONArray(notification);
                    try {
                        for(int i = 0; i < oldJsonArray.length(); i++) {
                            // prev json objects
                            newJsonArray.put(oldJsonArray.get(i));
                        }
                        count = newJsonArray.length();
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    if (sharedPreferences!=null) {
                        editor= sharedPreferences.edit();
                        editor.putString("NotificationModel", newJsonArray.toString());
                        editor.putInt("NotificationCount", count);
                        editor.apply();
                        Log.e("NewAry", newJsonArray.toString());
                        //Log.e("jsnObj", jsonObject.toString());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putString("NotificationModel", jsonArray.toString());
                    editor.apply();
                }
            }
        }
    }

}
