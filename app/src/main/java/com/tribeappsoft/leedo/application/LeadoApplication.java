package com.tribeappsoft.leedo.application;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.firebase.FireBaseMessageService;
import com.tribeappsoft.leedo.util.Helper;

import java.util.Objects;

public class LeadoApplication extends Application implements Application.ActivityLifecycleCallbacks
{

    private String TAG ="LeadoSalesApplication";
    private static LeadoApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //MultiDex.install(this);

    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        LeadoApplication.instance = this;
        Log.e(TAG, "onCreate: Application Create" );

        SharedPreferences sharedPreferences = new Helper().getSharedPref(instance);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("applicationCreated", true);
        editor.apply();

        //register broadCast receiver
        if (broadcastReceiver!=null) registerReceiver(broadcastReceiver, new IntentFilter(FireBaseMessageService.BROADCAST_ACTION));

//        showConfirmDialog();
    }




    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        LeadoApplication.instance = this;
        Log.e(TAG, "onActivityCreated: ");
        //new Helper().showCustomToast(activity, "Activity Created!");
    }

    public static LeadoApplication getInstance()
    {
        if(LeadoApplication.instance == null) {
            LeadoApplication.instance = new LeadoApplication();
        }
        return LeadoApplication.instance;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //updateUI(intent);

            Log.e(TAG, "onReceive: broadcastReceiver ");

            if (intent!=null){
                String page  = intent.getStringExtra("page");
                Log.e(TAG, "intent page "+page);

                int lead_id  = intent.getIntExtra("lead_id",0);
                String lead_uid  = intent.getStringExtra("lead_uid");
                int lead_status_id  = intent.getIntExtra("lead_status_id",0);
                int lead_call_schedule_id  = intent.getIntExtra("lead_call_schedule_id",0);
                String country_code  = intent.getStringExtra("country_code");
                String mobile_number  = intent.getStringExtra("mobile_number");
                String call_remarks  = intent.getStringExtra("call_remarks");
                String full_name  = intent.getStringExtra("full_name");
                String project_name  = intent.getStringExtra("project_name");
                String unit_category  = intent.getStringExtra("unit_category");

                boolean notifyFeeds  = intent.getBooleanExtra("notifyFeeds", false);
                boolean notifyScheduleCall  = intent.getBooleanExtra("notify", false);

                String title  = intent.getStringExtra("title");
                String body  = intent.getStringExtra("body");


                //new Handler().postDelayed(() -> Toast.makeText(context, "on BroadCast Received!", Toast.LENGTH_LONG).show(), 1000);


                Log.e(TAG, "onReceive: BroadcastReceiver lead_id : "+lead_id+" lead_uid :"+lead_uid+"lead_status_id: "+lead_status_id+"lead_call_schedule_id: "+lead_call_schedule_id );

                if (Objects.requireNonNull(page).equalsIgnoreCase("unClaimedLead")) callToClaimNow(context, page, notifyFeeds);

                else if (page.equalsIgnoreCase("ReminderPage")) callToReminders(context, page, title, body);

                else if (page.equalsIgnoreCase("ScheduledCallNow")) callToScheduleCallNow(context, page,title, body,notifyScheduleCall, lead_id,lead_uid,lead_status_id,lead_call_schedule_id,country_code,mobile_number,full_name,project_name,unit_category,call_remarks);
            }

        }
    };


    private void callToClaimNow(Context context, String page, boolean notifyFeeds)
    {
        Log.e(TAG, "callToClaimNow "+page);
        context.getApplicationContext()
                .startActivity(new Intent()
                        .setClassName("com.tribeappsoft.leedo", "com.tribeappsoft.leedo.salesPerson.claimNow.ClaimNowActivity")
                        .putExtra("page", page)
                        .putExtra("notifyFeeds", notifyFeeds)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        //r.play();
    }

    private void callToReminders(Context context, String page, String title, String body)
    {

        context.getApplicationContext().startActivity(new Intent()
                .setClassName("com.tribeappsoft.leedo", "com.tribeappsoft.leedo.admin.reminder.ShowReminderPopupActivity")
                .putExtra("page", page)
                .putExtra("title", title)
                .putExtra("body", body)
                .putExtra("notifyReminders", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
    private void callToScheduleCallNow(Context context, String page, String title, String body, boolean notifyCallSchedule, int lead_id, String lead_uid, int lead_status_id, int lead_call_schedule_id, String country_code, String mobile_number, String full_name, String project_name, String unit_category, String call_remarks)
    {
        Log.e(TAG, "callToScheduleCallNow lead_uid: "+lead_uid+"lead_id :"+lead_id);
        context.getApplicationContext().startActivity(new Intent()
                        .setClassName("com.tribeappsoft.leedo", "com.tribeappsoft.leedo.admin.callSchedule.ShowScheduleCallPopupActivity")
                        .putExtra("page", page)
                        .putExtra("title", title)
                        .putExtra("body", body)
                        .putExtra("lead_id", lead_id)
                        .putExtra("cu_id", lead_uid)
                        .putExtra("lead_status_id", lead_status_id)
                        .putExtra("lead_call_schedule_id", lead_call_schedule_id)
                        .putExtra("country_code", country_code)
                        .putExtra("mobile_number", mobile_number)
                        .putExtra("call_remarks", call_remarks)
                        .putExtra("full_name", full_name)
                        .putExtra("project_name", project_name)
                        .putExtra("unit_category", unit_category)
                        .putExtra("notify", notifyCallSchedule)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        //new Handler().postDelayed(() -> Toast.makeText(context, "on Call To Schedule Call Now VJApl !", Toast.LENGTH_LONG).show(), 1000);

        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        //r.play();
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (broadcastReceiver!=null) unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.e(TAG, "onActivityStopped: ");
        // new Helper().showCustomToast(activity, "Activity Stopeed!");
        if (broadcastReceiver!=null) unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.e(TAG, "onActivityDestroyed: ");
        // new Helper().showCustomToast(activity, "Activity Destroyed!");
    }


    private void showConfirmDialog()
    {
        Log.e(TAG, "showConfirmDialog: ");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText("Claim Lead?");
        tv_desc.setText("Are you sure you want to claim this lead?");
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            // showSuccessPopup();
            alertDialog.dismiss();
            //if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            //if (mp!=null) mp.stop();
            //if (vibrator!=null) vibrator.cancel();

           /* if (isNetworkAvailable(Objects.requireNonNull(context))) {
                //showProgressBar("Adding site visit...");
                call_claimNow();
            } else NetworkError(context);*/

        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            //if (mp!=null) mp.stop();
            //if (vibrator!=null) vibrator.cancel();

            //finish this activity
            //finish();

        });

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        //int pixel= Objects.requireNonNull(getApplicationContext()).getWindowManager().getDefaultDisplay().getWidth();

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        //int width = display.getWidth();
        //int height = display.getHeight();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = display.getWidth() -100;

        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position


        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.drawable.bg_claim_popup));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
    }

}