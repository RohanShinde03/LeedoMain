package com.tribeappsoft.leedo.admin.callSchedule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callLog.TelephonyCallService;
import com.tribeappsoft.leedo.util.Helper;

import java.util.Locale;
import java.util.Objects;

public class ShowScheduleCallPopupActivity extends AppCompatActivity {

    private String TAG = "ShowScheduleCallPopupActivity";
    private String mobile_number="",country_code = "+91", full_name = "",lead_uid="",api_token="";
    private AppCompatActivity context;
    private MediaPlayer mp;
    private Vibrator vibrator;
    private TelephonyManager mgr;
    private boolean  onCall = false;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int lead_id=0,lead_status_id=0,lead_call_schedule_id=0;
    private TextToSpeech tts;
    private AlertDialog alertDialog = null;
    private int user_id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ///setContentView(R.layout.activity_show_reminder_popup);
        context = ShowScheduleCallPopupActivity.this;

        //set finish in touch outside false
        this.setFinishOnTouchOutside(false);

        //
        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        if (getIntent()!=null) {

            String page = getIntent().getStringExtra("page");
            //String title = getIntent().getStringExtra("title");
            //String body = getIntent().getStringExtra("body");
            lead_id = getIntent().getIntExtra("lead_id",0);
            lead_uid = getIntent().getStringExtra("cu_id");
            lead_status_id = getIntent().getIntExtra("lead_status_id",0);
            lead_call_schedule_id = getIntent().getIntExtra("lead_call_schedule_id",0);
            country_code = getIntent().getStringExtra("country_code");
            mobile_number = getIntent().getStringExtra("mobile_number");
            String call_remarks = getIntent().getStringExtra("call_remarks");
            full_name = getIntent().getStringExtra("full_name");
            String project_name = getIntent().getStringExtra("project_name");
            String unit_category = getIntent().getStringExtra("unit_category");
            // notifyCallSchedule = getIntent().getBooleanExtra("notify",false);

            Log.e(TAG, "onCreate: lead_id : "+lead_id+"lead_uid :"+lead_uid+"lead_status_id: "+lead_status_id+"lead_call_schedule_id: "+lead_call_schedule_id );

            //new Handler().postDelayed(() -> Toast.makeText(context, "on getIntent ShowScheduleCallPopupActivity !", Toast.LENGTH_LONG).show(), 1000);

            if (page != null && !page.isEmpty()) {
                showCallScheduleDialog(lead_id,lead_uid,lead_status_id,lead_call_schedule_id, country_code,mobile_number, full_name, project_name, unit_category, call_remarks);
            }
        }


        tts=new TextToSpeech(ShowScheduleCallPopupActivity.this, status -> {
            // TODO Auto-generated method stub
            if(status == TextToSpeech.SUCCESS){
                int result=tts.setLanguage(Locale.UK);
                tts.setSpeechRate(0.7f);
                if(result==TextToSpeech.LANG_MISSING_DATA ||
                        result==TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.e("error", "This Language is not supported");
                }
                else{
                    Log.e("else", "ConvertTextToSpeech");
                    //ConvertTextToSpeech();
                }
            }
            else Log.e(TAG, "TTS Error Initialization Failed!");
        });


        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
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
    }

    @SuppressLint("SetTextI18n")
    private void showCallScheduleDialog(int lead_id, String lead_uid, int lead_status_id, int lead_call_schedule_id, String country_code, String mobile_number, String full_name, String project_name, String unit_category, String call_remarks)
    {
        Log.e(TAG, "showCallScheduleDialog: "+lead_uid+""+full_name+""+project_name+""+unit_category);
        Log.e(TAG, "showCallScheduleDialogData "+lead_uid+""+full_name+""+project_name+""+unit_category+""+call_remarks);
        if (!onCall)
        {
            //vibrate when scan completed
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            //long[] pattern = { 0, 200, 0 }; // 0 to start now, 200 to vibrate 200 ms, 0 to sleep for 0 ms.
            //vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.

            long[] pattern = {0, 100, 1000, 300, 1000, 300, 1000, 300};
            // 0 is for delay, 100 says vibrate for 100ms for the first time,
            // next comes delay of 1000ms, and post that vibrate again for 300ms
            //v.vibrate(pattern, -1); //-1 is important to stop

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //vibrator.vibrate(VibrationEffect.createOneShot(10000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
                Objects.requireNonNull(vibrator).vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                //v.vibrate(2000);  // Vibrate method for below API Level 26
                Objects.requireNonNull(vibrator).vibrate(pattern, -1); // 0 to repeat endlessly.
            }

            //play sound when Scan Completed
            mp = MediaPlayer.create(getApplicationContext(), R.raw.reminder_alarm__2018);
            try {
                //Before playing audio, call prepare
                //mp.prepare();
                //For Looping (true = looping; false = no looping)
                mp.setLooping(false);
                //To play audio ,call
                mp.start();
                //To pause, call
                //mp.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else Log.e(TAG, "showDialog: onCall "+ true);

        //new Handler().postDelayed(() -> Toast.makeText(context, "on showCallScheduleDialog ShowScheduleCallPopupActivity !", Toast.LENGTH_LONG).show(), 1000);

        //  Log.e(TAG, "showDialog");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_call_now, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setInverseBackgroundForced(true);
        alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
       /* AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);*/
        //LinearLayoutCompat ll_mainLayout =  alertLayout.findViewById(R.id.ll_mainLayout);
        MaterialTextView mTv_callNow_cuIdNumber =  alertLayout.findViewById(R.id.mTv_layout_callNow_cuIdNumber);
        MaterialTextView mTv_callNow_leadName =  alertLayout.findViewById(R.id.mTv_layout_callNow_leadName);
        MaterialTextView mTv_callNow_projectName=  alertLayout.findViewById(R.id.mTv_layout_callNow_projectName);
        MaterialTextView mTv_layout_callNow_remarks=  alertLayout.findViewById(R.id.mTv_layout_callNow_remarks);
        LinearLayoutCompat ll_callNowRemark=  alertLayout.findViewById(R.id.ll_callNowRemark);
        MaterialButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        MaterialButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);

        /*tv_msg.setText(title!=null && !title.isEmpty() ? title  :"It's time to call your claimed lead!");
        tv_desc.setText(body!=null && !body.isEmpty() ?  body : "You set Call Schedule in Leedo App, Click to Call Now in details!");*/
        /*btn_negativeButton.setText(getString(R.string.dismiss));
        btn_positiveButton.setText(getString(R.string.view));*/

        mTv_callNow_cuIdNumber.setText(mobile_number!=null && !mobile_number.isEmpty() ? mobile_number  :"--");
        mTv_callNow_leadName.setText(full_name!=null && !full_name.isEmpty() ? full_name  :"--");
        mTv_callNow_projectName.setText(project_name!=null && !project_name.isEmpty() ? project_name + " | " +unit_category  :"--");
        mTv_layout_callNow_remarks.setText(call_remarks!=null && !call_remarks.isEmpty() ? call_remarks:"--");
        ll_callNowRemark.setVisibility(call_remarks!=null && !call_remarks.isEmpty() ? View.VISIBLE :View.GONE);
        Log.e(TAG, "showDialog Display");

        //view button click
        btn_positiveButton.setOnClickListener(view -> {

           /*//To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();*/

            //mobile number/call
            if (mobile_number!=null) {
                //new Helper().openPhoneDialer(Objects.requireNonNull(context), model.getMobile_number());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkCallPermissions()) prepareToMakePhoneCall(lead_id, lead_uid, lead_status_id, lead_call_schedule_id, country_code + mobile_number);
                    else showPermissionDialogue();
                }
                else prepareToMakePhoneCall(lead_id,lead_uid,lead_status_id,lead_call_schedule_id, country_code + mobile_number);

            }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");

            //dismiss dialog
            alertDialog.dismiss();

            //finish this activity
            //finish();
        });

        //main layout click
       /* alertLayout.setOnClickListener(view -> {

            //dismiss dialog
            alertDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            //go to call scheduling page
            startActivity(new Intent(context, CallScheduleMainActivity.class).putExtra("notify", true));

            //finish this activity
            finish();

        });*/

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

        new Handler().postDelayed(() -> {
            //stop media playing after 5 seconds
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();
        }, 4000);

        // playing after 5 seconds
        //new Handler().postDelayed(this::ConvertTextToSpeech, 9000);

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
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
            //finish();
        });
    }


    //check call permission
    private boolean checkCallPermissions() {
        return  (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED);
    }

    private void showPermissionDialogue()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_allow_permission, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog;
        alertDialog = alertDialogBuilder.create();
        AppCompatTextView tv_msg,tv_desc;
        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_renew_dialog_desc);
        MaterialButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_renew_negativeButton);
        MaterialButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_renew_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);
        LinearLayoutCompat ll_storage =  alertLayout.findViewById(R.id.ll_app_permissions_storage);
        LinearLayoutCompat ll_call_logs =  alertLayout.findViewById(R.id.ll_app_permissions_call_logs);
        LinearLayoutCompat ll_telephone =  alertLayout.findViewById(R.id.ll_app_permissions_telephone);
        LinearLayoutCompat ll_calender =  alertLayout.findViewById(R.id.ll_app_permissions_calender);
        LinearLayoutCompat ll_camera =  alertLayout.findViewById(R.id.ll_app_permissions_camera);
        LinearLayoutCompat ll_microphone =  alertLayout.findViewById(R.id.ll_app_permissions_microphone);
        View view_call_logs =  alertLayout.findViewById(R.id.view_call_logs);

        ll_storage.setVisibility(View.GONE);
        ll_telephone.setVisibility(View.VISIBLE);
        view_call_logs.setVisibility(View.VISIBLE);
        ll_call_logs.setVisibility(View.VISIBLE);
        ll_microphone.setVisibility(View.GONE);
        tv_msg.setText(getString(R.string.allow_access_to_contacts_and_phone_log));
        tv_desc.setText(getString(R.string.leedo_needs_requesting_permission));
        btn_negativeButton.setText(getString(R.string.deny));
        btn_positiveButton.setText(getString(R.string.allow));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //request for permissions
            // if (checkCallPermissions()) prepareToMakePhoneCall();
            requestPermissionCall();
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }
    //request camera permission
    private void requestPermissionCall()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CALL_PHONE)
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_PHONE_STATE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.PROCESS_OUTGOING_CALLS))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, context.getString(R.string.call_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                }, CALL_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == CALL_PERMISSION_REQUEST_CODE)  //handling camera permission
        {
            Log.e(TAG, "onRequestPermissionsResult:  "+ CALL_PERMISSION_REQUEST_CODE);
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Log.e(TAG, "onRequestPermissionsResult: permission grant success!");
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //make a phone call once permission is granted
                if (mobile_number!=null) prepareToMakePhoneCall();
                else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
            }
            else Log.e(TAG, "onRequestPermissionsResult: permission grant failure!");
        }
        else Log.e(TAG, "onRequestPermissionsResult: Wrong Error Code" );
    }

    private void prepareToMakePhoneCall() {

        Log.e(TAG, "prepareToMakePhoneCall: lead_id : "+lead_id+"lead_uid :"+lead_uid+"lead_status_id: "+lead_status_id+"lead_call_schedule_id: "+lead_call_schedule_id );
        //start the service first
      /*  context.startService(new Intent(context, TelephonyCallService.class)
                .putExtra("call_lead_id",lead_id)
                .putExtra("lead_status_id",lead_status_id)
                .putExtra("call_schedule_id",lead_call_schedule_id)
                .putExtra("user_id",user_id)
                .putExtra("cu_id",lead_uid)
                .putExtra("api_token",api_token)
                .putExtra("from_make_phone_Call",true)
        );*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //start the startForegroundService first
            context.startForegroundService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",lead_id)
                    .putExtra("lead_status_id",lead_status_id)
                    .putExtra("call_schedule_id",lead_call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",lead_uid)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );

        } else {
            //start the service first
            context.startService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",lead_id)
                    .putExtra("lead_status_id",lead_status_id)
                    .putExtra("call_schedule_id",lead_call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",lead_uid)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );
        }



         //put the call_lead_id and call_cuID in sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.putInt("call_lead_id", lead_id);
            editor.putInt("lead_status_id", lead_status_id);
            editor.putInt("call_schedule_id", lead_call_schedule_id);
            editor.putString("cu_id", lead_uid);
            editor.putBoolean("from_make_phone_Call", true);
            editor.apply();

            Log.e(TAG, "prepareToMakePhoneCall: sharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t lead_status_id "+ sharedPreferences.getInt("lead_status_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t cu_id "+sharedPreferences.getString("cu_id", null)
            );
        }


        new Helper().showSuccessCustomToast(context, "Calling from Leedo App...!");
        new Handler().postDelayed(() -> new Helper().makePhoneCall(context, country_code + mobile_number), 2000);


        //finish activity after 2 seconds
        new Handler().postDelayed(this::finish, 2000);
    }



    private void prepareToMakePhoneCall(int call_lead_id, String call_cuID, int call_lead_status_id, int call_schedule_id, String customer_mobile) {

        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //start the startForegroundService first
            context.startForegroundService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",call_cuID)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );

        } else {
            //start the service first
            context.startService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",call_cuID)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );
        }




        //update into sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.putInt("call_lead_id", lead_id);
            editor.putInt("lead_status_id", lead_status_id);
            editor.putInt("call_schedule_id", lead_call_schedule_id);
            editor.putString("cu_id", lead_uid);
            editor.putBoolean("from_make_phone_Call", true);
            editor.apply();

            Log.e(TAG, "prepareToMakePhoneCall: sharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t lead_status_id "+ sharedPreferences.getInt("lead_status_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t cu_id "+sharedPreferences.getString("cu_id", null)
            );
        }


        new Helper().showSuccessCustomToast(context, "Calling from Leedo App...!");
        new Handler().postDelayed(() -> new Helper().makePhoneCall(context, customer_mobile), 2000);

        //finish activity after 2 seconds
        new Handler().postDelayed(this::finish, 2000);
    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        String text = "Hello it's a time to call your scheduled lead" + full_name;
        if("".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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


        new Handler().postDelayed(() -> {

            if(tts != null){
                tts.stop();
                tts.shutdown();
            }
        }, 9000);


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

        Log.e(TAG, "onBackPress");
        super.onBackPressed();

        //To stop audio, call
        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

        if(tts != null){
            tts.stop();
            tts.shutdown();
        }

        if (sharedPreferences!=null) {
            //update sharedPref with flag
            editor = sharedPreferences.edit();
            editor.putBoolean("applicationCreated", false);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {

        //Close the Text to Speech Library
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        super.onDestroy();
    }

}