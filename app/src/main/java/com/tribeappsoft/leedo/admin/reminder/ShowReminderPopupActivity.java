package com.tribeappsoft.leedo.admin.reminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.R;

import java.util.Objects;

public class ShowReminderPopupActivity extends AppCompatActivity {

    private String TAG = "ShowReminderPopupActivity";
    private AppCompatActivity context;
    private MediaPlayer mp;
    private Vibrator vibrator;
    private TelephonyManager mgr;
    private boolean  onCall = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_show_reminder_popup);
        context = ShowReminderPopupActivity.this;

        //set finish in touch outside false
        this.setFinishOnTouchOutside(false);

        //
        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        //api_token = sharedPreferences.getString("api_token", "");
        //user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        if (getIntent()!=null) {

            String page = getIntent().getStringExtra("page");
            String title = getIntent().getStringExtra("title");
            String body = getIntent().getStringExtra("body");
            Log.e(TAG, "onCreate: " + page);
            if (page != null && !page.isEmpty()) {
                    showReminderDialog(title, body);
            }
        }
    }


    //Get PHone state Listener
    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //Incoming call: Pause music
                Log.e(TAG, "onCallStateChanged: Call RINGING " );
                //set on call true
                onCall = true;
                //stop vibration
                if (vibrator!=null) vibrator.cancel();
                //To pause, call
                if (mp!=null )mp.stop();
            } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
                Log.e(TAG, "onCallStateChanged: Call IDLE " );
                //set on call false
                onCall = false;
                //long[] pattern = { 0, 200, 0 };
                //if (vibrator!=null) vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.
                // To pause, call
                //if (mp!=null )mp.stop();
            }
            else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
                Log.e(TAG, "onCallStateChanged: Call OFFHOOK " );
                //set on Call true
                onCall = true;
                //stop vibration
                if (vibrator!=null) vibrator.cancel();
                //To pause, call
                if (mp!=null )mp.stop();
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };




    @Override
    protected void onResume() {
        super.onResume();

        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }


    @SuppressLint("SetTextI18n")
    private void showReminderDialog(String title, String body)
    {

        if (!onCall)
        {
            //vibrate when scan completed
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = { 0, 200, 0 }; // 0 to start now, 200 to vibrate 200 ms, 0 to sleep for 0 ms.
            //vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //vibrator.vibrate(VibrationEffect.createOneShot(10000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                //v.vibrate(2000);  // Vibrate method for below API Level 26
                vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.
            }

            //play sound when Scan Completed
            mp = MediaPlayer.create(getApplicationContext(), R.raw.reminder_alarm__2018);
            try {
                //Before playing audio, call prepare
                //mp.prepare();
                //For Looping (true = looping; false = no looping)
                mp.setLooping(true);
                //To play audio ,call
                mp.start();
                //To pause, call
                //mp.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else Log.e(TAG, "showDialog: onCall "+onCall );


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setInverseBackgroundForced(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);

        tv_msg.setText(title!=null && !title.isEmpty() ? title  :"You have a Reminder!");
        tv_desc.setText(body!=null && !body.isEmpty() ?  body : "You set reminder in Leedo App, Click to view in details!");
        btn_negativeButton.setText(getString(R.string.dismiss));
        btn_positiveButton.setText(getString(R.string.view));

        //view button click
        btn_positiveButton.setOnClickListener(view -> {

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            //reminders page
            alertDialog.dismiss();

            //go to reminders page
            startActivity(new Intent(context,AllReminderActivity.class).putExtra("notifyReminders", true));

            //finish this activity
            finish();
        });

        //main layout click
        alertLayout.setOnClickListener(view -> {

            //dismiss dialog
            alertDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            //go to reminders page
            startActivity(new Intent(context, AllReminderActivity.class).putExtra("notifyReminders", true));

            //finish this activity
            finish();

        });

        btn_negativeButton.setOnClickListener(view -> {

            //dismiss dialog
            alertDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            //finish this activity
            finish();

        });
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.TOP;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_claim_popup));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.reminderDialogAnim;
        alertDialog.setOnDismissListener(dialog -> {
            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();
            //finish this activity
            finish();

        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        Log.e(TAG, "onPause: ");

        //To stop audio, call
        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

        if (sharedPreferences!=null) {
            //update sharedPref with flag
            editor = sharedPreferences.edit();
            editor.putBoolean("applicationCreated", false);
            editor.apply();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...
            event.startTracking(); // Needed to track long presses

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();
            Log.e(TAG, "onKeyDown: Lock Button click" );

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...

            Log.e(TAG, "onKeyDown: Lock Button click -- Long Press" );
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        Log.e(TAG, "onBackPress" );

        //To stop audio, call
        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

        if (sharedPreferences!=null) {
            //update sharedPref with flag
            editor = sharedPreferences.edit();
            editor.putBoolean("applicationCreated", false);
            editor.apply();
        }

        super.onBackPressed();
    }

}
