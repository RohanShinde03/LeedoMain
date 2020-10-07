package com.tribeappsoft.leedo.salesPerson.claimNow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class ClaimNowActivity extends AppCompatActivity {

    private AppCompatActivity context;
    private ArrayList<LeadListModel> leadListModelArrayList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "ClaimNowActivity", api_token = "";
    private Dialog claimDialog;
    //private SwipeDismissDialog claimDialog;
    private int user_id = 0,claimPosition =0, claimCount=0, claimAPiCount =0,lead_id =0;
    private boolean isClaimNow = false, onCall = false;
    private MediaPlayer mp;
    private Vibrator vibrator;
    private TelephonyManager mgr;


    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_claim_now);
        //ButterKnife.bind(this);
        context = ClaimNowActivity.this;

        //set finish in touch outside false
        this.setFinishOnTouchOutside(false);

        Log.e(TAG, "onCreate: ");
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //
        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        //set vibrator
        if (vibrator ==null )vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //play sound when Scan Completed
        if (mp==null) mp = MediaPlayer.create(getApplicationContext(), R.raw.claim_now_alarm);
        //hidden pb
        //hideProgressBar();
        //leads arrayList
        leadListModelArrayList = new ArrayList<>();

        if (getIntent()!=null)
        {
            String page  = getIntent().getStringExtra("page");
            Log.e(TAG, "onCreate: "+page );
            if (page!=null && !page.isEmpty()) {
                if (Helper.isNetworkAvailable(context)) {
                    //showProgressBar("Getting unclaimed lead...");
                    call_getUnClaimedLeads();
                }
                else Helper.NetworkError(context);
            }
        }

    }



    private void call_getUnClaimedLeads()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getUnClaimedLeads(api_token);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonArrayResponse -> {
            throw new IllegalStateException("doOnNextException");
        });
        responseObservable.doOnError(throwable -> {
            throw new UnsupportedOperationException("onError exception");
        });
        responseObservable.subscribeOn(Schedulers.newThread())
                .asObservable()
                .subscribe(new Subscriber<Response<JsonObject>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d(TAG, "onCompleted:");
                        delayRefreshUnClaimedLeads();
                    }
                    @Override
                    public void onError(final Throwable e)
                    {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.connection_time_out));
                        else showErrorLog(e.toString());
                    }
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                leadListModelArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++) {
                                                    setJsonClaimedLeads(jsonArray,i);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
                                case 404:
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLog(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLog(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });

    }


    private void setJsonClaimedLeads(JsonArray jsonArray, int i)
    {
        JsonObject jsonObject=jsonArray.get(i).getAsJsonObject();
        LeadListModel model=new LeadListModel();
        if(jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ?jsonObject.get("lead_id").getAsInt() : 0);
        if(jsonObject.has("lead_uid")) model.setLead_cuid_number(!jsonObject.get("lead_uid").isJsonNull() ?jsonObject.get("lead_uid").getAsString() : "");
        if(jsonObject.has("unit_category")) model.setLead_unit_type(!jsonObject.get("unit_category").isJsonNull() ?jsonObject.get("unit_category").getAsString() : "");
        if(jsonObject.has("project_name")) model.setLead_project_name(!jsonObject.get("project_name").isJsonNull() ?jsonObject.get("project_name").getAsString() : "");
        if(jsonObject.has("mobile_number")) model.setLead_mobile(!jsonObject.get("mobile_number").isJsonNull() ?jsonObject.get("mobile_number").getAsString() :"");
        if(jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ?jsonObject.get("full_name").getAsString() :"");
        if(jsonObject.has("lead_types_name")) model.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ?jsonObject.get("lead_types_name").getAsString() :"");
        if(jsonObject.has("added_by")) model.setAdded_by(!jsonObject.get("added_by").isJsonNull() ?jsonObject.get("added_by").getAsString() :"");
        if(jsonObject.has("tag_date")) model.setTag_date(!jsonObject.get("tag_date").isJsonNull() ?jsonObject.get("tag_date").getAsString() :"");
        if(jsonObject.has("tag_elapsed_time")) model.setElapsed_time(!jsonObject.get("tag_elapsed_time").isJsonNull() ?jsonObject.get("tag_elapsed_time").getAsString() :"");
        leadListModelArrayList.add(model);
    }

    private void delayRefreshUnClaimedLeads()
    {
        runOnUiThread(() -> {

            if (sharedPreferences!=null) {
                //update sharedPref with flag for claim now dialog
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //hide pb
           // hideProgressBar();

            claimCount  = leadListModelArrayList.size();
            isClaimNow = leadListModelArrayList.size() > 0; // set true iff having unclaimed leads
            if (leadListModelArrayList!=null && leadListModelArrayList.size()>0) {

                //show claim now dialog
                new Handler(getMainLooper()).postDelayed(this::showDialog, 2500);
            }
            else if (claimCount == leadListModelArrayList.size() && isClaimNow)
            {
                Log.e(TAG, "delayRefreshUnClaimedLeads: ");
                //check claimCount == total size

                //To stop audio, call
                if (mp!=null) mp.stop();
                if (vibrator!=null) vibrator.cancel();

                //and new start main activity
//                startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class)
//                        .putExtra("openFlag", 0)
//                        .addFlags(FLAG_ACTIVITY_CLEAR_TOP)
//                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                );

                //checking all leads are claimed, then

            }
            else if (leadListModelArrayList!=null && leadListModelArrayList.size()==0)
            {
                //size zero and start main activity
                Log.e(TAG, "delayRefreshUnClaimedLeads: size 0 start main");
                //start main activity
                startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class)
                        .putExtra("openFlag", 0)
                        .addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
            }

        });
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

    private void showDialog()
    {

        claimDialog = new Dialog(Objects.requireNonNull(context)); //  R.style.claimDialogAnim
        claimDialog.setCancelable(false);
        //mAlertDlgBuilder.setInverseBackgroundForced(true);
        Drawable button = getResources().getDrawable(R.drawable.claim_button_drawable, context.getTheme());
        View view  = context.getLayoutInflater().inflate(R.layout.layout_claim_now_pop_up, null);
        claimDialog.setContentView(view);
        Objects.requireNonNull(claimDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
        ImageView close =  view.findViewById(R.id.iv_close_claimNow);
        AppCompatTextView tv_date =  view.findViewById(R.id.tv_claimNow_date);
        AppCompatTextView tv_claimTag =  view.findViewById(R.id.tv_claimNow_tag);
        //AppCompatImageView iv_tagIcon =  view.findViewById(R.id.iv_claimNow_tagIcon);
        //AppCompatTextView tv_unitType =  view.findViewById(R.id.tv_claimNow_unitType);
        AppCompatTextView tv_elapsedTime =  view.findViewById(R.id.tv_claimNow_elapsedTime);
        AppCompatTextView tv_name =  view.findViewById(R.id.tv_claimNow_projectName);
        AppCompatTextView tv_cuId =  view.findViewById(R.id.tv_claimNow_cu_id);
        AppCompatTextView tv_lead_name =  view.findViewById(R.id.tv_claimNow_lead_name);
        AppCompatTextView tv_addedBy =  view.findViewById(R.id.tv_claimNow_addedBy);
        AppCompatButton claim_btn =  view.findViewById(R.id.btn_claimNow_popup);
        //RipplePulseLayout mRipplePulseLayout = view.findViewById(R.id.rp_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        width_px = width_px -50;
        height_px=height_px-200;
        claimDialog.getWindow().setLayout(width_px,height_px);
        claimDialog.getWindow().getAttributes().windowAnimations = R.style.claimDialogAnim;

        //on dismiss listener
        claimDialog.setOnDismissListener(dialog -> {

            Log.e(TAG, "showDialog: onDismiss ");
            //dismiss dialog and update feed
            //if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            if (sharedPreferences!=null) {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //exit from app
            //exitFromApp();

        });


        //mRipplePulseLayout.startRippleAnimation();
        close.setOnClickListener(view12 -> {

            //Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
            //view.startAnimation(animZoomIn);

            //dismiss dialog and update feed
            if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            //stop vibrator
            if (vibrator!=null) vibrator.cancel();

            if (sharedPreferences!=null) {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //exit from app
            exitFromApp();
        });

        if(leadListModelArrayList!=null)
        {
            tv_date.setText(leadListModelArrayList.get(claimPosition).getTag_date());
            //tv_unitType.setText(leadListModelArrayList.get(claimPosition).getLead_unit_type());
            tv_cuId.setText(leadListModelArrayList.get(claimPosition).getLead_cuid_number());
            tv_lead_name.setText(leadListModelArrayList.get(claimPosition).getFull_name());
            tv_name.setText(String.format("%s | %s", leadListModelArrayList.get(claimPosition).getLead_project_name(), leadListModelArrayList.get(claimPosition).getLead_unit_type()));
            tv_elapsedTime.setText(leadListModelArrayList.get(claimPosition).getElapsed_time());
            tv_claimTag.setText(leadListModelArrayList.get(claimPosition).getLead_types_name());
            tv_addedBy.setText(leadListModelArrayList.get(claimPosition).getAdded_by());
            lead_id=leadListModelArrayList.get(claimPosition).getLead_id();
        }

        claim_btn.setBackgroundDrawable(button);
        claim_btn.setOnClickListener(view1 -> showConfirmDialog());


        if(claimDialog!=null) claimDialog.show();

        if (!onCall)
        {
            //set vibrate pattern
            //long[] pattern = { 0, 200, 0 };
            // Start without a delay
            // Vibrate for 3000 milliseconds
            // Sleep for 2000 milliseconds
            long[] pattern = {0, 2000, 1000, 2000, 1000, 2000, 1000, 2000, 1000, 2000, 1000};

            //make device vibrate when lead dialog is shown
            //vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //vibrator.vibrate(VibrationEffect.createOneShot(10000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                //v.vibrate(2000);  // Vibrate method for below API Level 26
                vibrator.vibrate(pattern, -1);
                // 0 to repeat endlessly.
                // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
            }
            try {

                if ( !mp.isPlaying()) {
                    Log.e(TAG, "showDialog: media not playing" );
                    //Before playing audio, call prepare
                    //mp.prepare();
                    //For Looping (true = looping; false = no looping)
                    mp.setLooping(true);
                    //To play audio ,call
                    mp.start();
                    //To pause, call
                    //mp.pause();

                    new Handler().postDelayed(() -> {
                        //stop media playing after 5 seconds
                        if (mp!=null) mp.stop();
                    }, 7000);
                }
                else {
                    Log.e(TAG, "showDialog: media Already playing" );
                    if (mp!=null)mp.stop();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            //To stop audio, call
            //if (mp!=null) mp.stop();
            //if (vibrator!=null) vibrator.cancel();
        }
        else  Log.e(TAG, "showDialog: onCall "+onCall );

    }


    private void exitFromApp()
    {
        //finish this activity
        finish();
        //finishAffinity(); // Close all Activities
        //exit from app
        //System.exit(0);
        Log.e(TAG, "exitFromApp: ");

        /*startActivity(new Intent(context, SplashScreenActivity.class)
                        .putExtra("exitApp", true)
                        .addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/
    }




    /*  private void showDialog()
    {

        View view  = context.getLayoutInflater().inflate(R.layout.layout_claim_now_pop_up, null);
        claimDialog = new SwipeDismissDialog.Builder(context)
                .setView(view)
                .build()
                .show(); //  R.style.claimDialogAnim
        //claimDialog.setCancelable(true);
        Drawable button = getResources().getDrawable(R.drawable.claim_button_drawable, context.getTheme());

        //Objects.requireNonNull(claimDialog.getWind).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
        ImageView close =  view.findViewById(R.id.iv_close_claimNow);
        AppCompatTextView tv_date =  view.findViewById(R.id.tv_claimNow_date);
        AppCompatTextView tv_claimTag =  view.findViewById(R.id.tv_claimNow_tag);
        //AppCompatImageView iv_tagIcon =  view.findViewById(R.id.iv_claimNow_tagIcon);
        //AppCompatTextView tv_unitType =  view.findViewById(R.id.tv_claimNow_unitType);
        AppCompatTextView tv_elapsedTime =  view.findViewById(R.id.tv_claimNow_elapsedTime);
        AppCompatTextView tv_name =  view.findViewById(R.id.tv_claimNow_projectName);
        AppCompatTextView tv_cuId =  view.findViewById(R.id.tv_claimNow_cu_id);
        AppCompatTextView tv_lead_name =  view.findViewById(R.id.tv_claimNow_lead_name);
        AppCompatTextView tv_addedBy =  view.findViewById(R.id.tv_claimNow_addedBy);
        AppCompatButton claim_btn =  view.findViewById(R.id.btn_claimNow_popup);
        //RipplePulseLayout mRipplePulseLayout = view.findViewById(R.id.rp_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        width_px = width_px -50;
        height_px=height_px-200;
        //claimDialog.getWindow().setLayout(width_px,height_px);
        //claimDialog.getWindow().getAttributes().windowAnimations = R.style.claimDialogAnim;

        //mRipplePulseLayout.startRippleAnimation();
        close.setOnClickListener(view12 -> {

            //Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
            //view.startAnimation(animZoomIn);

            //dismiss dialog and update feed
            if(claimDialog!=null) claimDialog.dismiss();

            if (sharedPreferences!=null) {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //finish this activity
            finish();
        });

        if(leadListModelArrayList!=null)
        {
            tv_date.setText(leadListModelArrayList.get(claimPosition).getTag_date());
            //tv_unitType.setText(leadListModelArrayList.get(claimPosition).getLead_unit_type());
            tv_cuId.setText(leadListModelArrayList.get(claimPosition).getLead_cuid_number());
            tv_lead_name.setText(leadListModelArrayList.get(claimPosition).getFull_name());
            tv_name.setText(String.format("%s | %s", leadListModelArrayList.get(claimPosition).getLead_project_name(), leadListModelArrayList.get(claimPosition).getLead_unit_type()));
            tv_elapsedTime.setText(leadListModelArrayList.get(claimPosition).getElapsed_time());
            tv_claimTag.setText(leadListModelArrayList.get(claimPosition).getLead_types_name());
            tv_addedBy.setText(leadListModelArrayList.get(claimPosition).getAdded_by());
            lead_id=leadListModelArrayList.get(claimPosition).getLead_id();
        }

        claim_btn.setBackgroundDrawable(button);
        claim_btn.setOnClickListener(view1 -> showConfirmDialog());
        //if(claimDialog!=null) claimDialog.show();
    }*/



    @SuppressLint("SetTextI18n")
    private void showConfirmDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                //showProgressBar("Adding site visit...");
                call_claimNow();
            } else Helper.NetworkError(context);

        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            if(claimDialog!=null) claimDialog.dismiss();

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

            if (sharedPreferences!=null) {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //finish this activity
            //finish();

        });

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

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
    }


    private void call_claimNow()
    {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("lead_id",lead_id);

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call_ = client.getApiService().addLeadClaimNow(jsonObject);
        call_.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call_, @NonNull Response<JsonObject> response)
            {
                if (response.isSuccessful())
                {
                    if (response.body() != null)
                    {
                        String success = response.body().get("success").toString();
                        if ("1".equals(success)) {
                            claimAPiCount = claimAPiCount + 1;
                            //remove index from array list
                            leadListModelArrayList.remove(claimPosition);
                            showSuccessPopup();
                        }
                        else if ("2".equals(success)) onAlreadyClaimedLead(response.body());
                        else showErrorLog("Failed to claim lead!");
                    }
                }
                else
                {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }


    //Success Pop Up
    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessPopup()
    {

        Dialog claimSuccessDialog = new Dialog(Objects.requireNonNull(context));
        claimSuccessDialog.setCancelable(true);
        View view  = context.getLayoutInflater().inflate(R.layout.layout_claim_now_success, null);
        claimSuccessDialog.setContentView(view);
        Objects.requireNonNull(claimSuccessDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
        AppCompatTextView tv_congrats = view.findViewById(R.id.tv_congrats);
        AppCompatTextView tv_msg = view.findViewById(R.id.tv_msg);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        width_px = width_px -50;
        height_px=height_px-250;
        claimSuccessDialog.getWindow().setLayout(width_px,height_px);

        tv_congrats.setText("Congratulations !");
        tv_msg.setText("This is your lead");

        new Handler(getMainLooper()).postDelayed(() -> {
            claimSuccessDialog.dismiss();

            delayRefreshUnClaimedLeads();

        }, 2000);

        /*claim_btn.setBackgroundDrawable(button);
        claim_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*Dialog Box*//*
                showConfirmDialog();

            }
        });*/

        claimSuccessDialog.show();
    }

    private void onAlreadyClaimedLead(JsonObject jsonObject)
    {
        runOnUiThread(() -> {

            //handle if some one claimed lead already
            //show error log
            if (jsonObject.has("msg")) showErrorLog(!jsonObject.get("msg").isJsonNull() ? jsonObject.get("msg").getAsString() : "Failed to claim lead!" );
            else showErrorLog("Failed to claim lead!");

            // remove position from arrayList
            leadListModelArrayList.remove(claimPosition);

            //To stop audio, call
            if (mp!=null) mp.stop();
            if (vibrator!=null) vibrator.cancel();

        });
    }

    private void showErrorLog(final String message) {
        runOnUiThread(() -> {
            //ll_pb.setVisibility(View.GONE);
            Helper.onErrorSnack(context, message);
           // hideProgressBar();
        });
    }

   /* void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }*/

    @Override
    protected void onPause() {
        super.onPause();

        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        Log.e(TAG, "onPause: ");

        //To stop audio, call
        new Handler().postDelayed(() -> {
            //stop media playing after 5 seconds
            if (mp!=null) mp.stop();
        }, 7000);

        if (vibrator!=null) vibrator.cancel();

//        if (sharedPreferences!=null) {
//            //update sharedPref with flag
//            editor = sharedPreferences.edit();
//            editor.putBoolean("applicationCreated", false);
//            editor.apply();
//        }

    }

    /*@Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop: ");

        //To stop audio, call
        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

        if (sharedPreferences!=null) {
            //update sharedPref with flag
            editor = sharedPreferences.edit();
            editor.putBoolean("applicationCreated", false);
            editor.apply();
        }

    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        if(claimDialog!=null) claimDialog.dismiss();

        Log.e(TAG, "onBackPress" );

        //To stop audio, call
        if (mp!=null) mp.stop();
        if (vibrator!=null) vibrator.cancel();

//        if (sharedPreferences!=null) {
//            //update sharedPref with flag
//            editor = sharedPreferences.edit();
//            editor.putBoolean("applicationCreated", false);
//            editor.apply();
//        }

        super.onBackPressed();
    }
}
