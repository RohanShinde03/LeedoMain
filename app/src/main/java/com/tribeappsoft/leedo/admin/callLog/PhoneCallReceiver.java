package com.tribeappsoft.leedo.admin.callLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.FacebookSdk;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.getDateTime;
import static com.tribeappsoft.leedo.util.Helper.getSecondsDiff;
import static com.tribeappsoft.leedo.util.Helper.getTimeFromDateTimeString;

public class PhoneCallReceiver extends BroadcastReceiver {

    private String TAG = "PhoneCallReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date dateCallStartTime;
    private static String str_callStartTime;
    private boolean isIncoming, from_make_phone_Call = false;
    private static String savedNumber;  //because the passed incoming is only valid in ringing
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static int user_id =0, lead_id =0, lead_status_id =0, call_schedule_id = 0;
    private static String api_token ="", lead_cu_id = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent!=null){
            Log.e(TAG, "onReceive: ");

            //initialise shared pref
            sharedPreferences = new Helper().getSharedPref(FacebookSdk.getApplicationContext());
            editor = sharedPreferences.edit();
            editor.apply();
            api_token = sharedPreferences.getString("api_token", "");
            user_id = sharedPreferences.getInt("user_id", 0);
            lead_id = sharedPreferences.getInt("call_lead_id", 0);
            lead_status_id = sharedPreferences.getInt("lead_status_id", 0);
            call_schedule_id = sharedPreferences.getInt("call_schedule_id", 0);
            lead_cu_id = sharedPreferences.getString("cu_id", "");
            from_make_phone_Call = sharedPreferences.getBoolean("from_make_phone_Call", false);

            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (Objects.requireNonNull(intent.getAction()).equals("android.intent.action.NEW_OUTGOING_CALL")) {

                savedNumber = Objects.requireNonNull(intent.getExtras()).getString("android.intent.extra.PHONE_NUMBER");
            }
            else{

                String stateStr = Objects.requireNonNull(intent.getExtras()).getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if(Objects.equals(stateStr, TelephonyManager.EXTRA_STATE_IDLE)){
                    state = TelephonyManager.CALL_STATE_IDLE;
                }
                else if(Objects.requireNonNull(stateStr).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                }
                else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state, number);
            }
        }
    }


    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                dateCallStartTime = new Date();
                str_callStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                savedNumber = number;
                //onIncomingCallReceived(context, number, callStartTime);
                Toast.makeText(context, "onIncomingCallReceived "+ number + "\t date time :" + str_callStartTime, Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCallStateChanged: onIncomingCallReceived "+ number + "\t date time :" + str_callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offHook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    dateCallStartTime = new Date();
                    str_callStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    //onOutgoingCallStarted(context, savedNumber, callStartTime);
                    //Toast.makeText(context, "onOutgoingCallStarted "+ savedNumber + "\t date time :" + str_callStartTime, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onCallStateChanged: onOutgoingCallStarted "+ savedNumber + "\t date time :" + str_callStartTime);

//                    if (from_make_phone_Call) {
//                        //start call recording
//                        startRecording();
//                    }
                }
                else
                {
                    isIncoming = true;
                    dateCallStartTime = new Date();
                    //onIncomingCallAnswered(context, savedNumber, callStartTime);
                    Toast.makeText(context, "onIncomingCallAnswered "+ savedNumber + "\t date time :" + dateCallStartTime, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCallStateChanged: onIncomingCallAnswered "+ savedNumber + "\t date time :" + dateCallStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    //onMissedCall(context, savedNumber, callStartTime);
                    //Toast.makeText(context, "outgoingCall onMissedCall "+ savedNumber + "\t date time :" + str_callStartTime, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCallStateChanged: incoming onMissedCall "+ savedNumber + "\t date time :" + str_callStartTime);
                }
                else if(isIncoming){
                    //onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    //Toast.makeText(context, "onIncomingCallEnded "+ savedNumber + "\t start date time :" + dateCallStartTime + "\t End Date Time : "+new Date(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCallStateChanged: onIncomingCallEnded "+ savedNumber + "\t start date time :" + dateCallStartTime + "\t End Date Time : "+new Date());
                }
                else if(lastState == TelephonyManager.CALL_STATE_OFFHOOK){
                    //Answered Call which is ended

                    //onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    String endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    //Toast.makeText(context, "onOutgoingCallEnded "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCallStateChanged: onOutgoingCall Ended "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);

                    if (from_make_phone_Call) {
                        long secDiff = getSecondsDiff(dateCallStartTime, new Date());
                        Log.e(TAG, "onCallStateChanged: secDiff "+secDiff);

                        //stop Recoding
                        //stopRecording();

                        if (secDiff>40) {
                            //callee has accepted an outgoing call. -- Answered

                            Log.e(TAG, "onCallStateChanged: onOutgoingCallEnded -- Answered "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);

                            //goto add call log
                            context.startActivity(new Intent(context, CallLogActivity.class)
                                    .putExtra("fromPhoneCall", true)
                                    .putExtra("call_lead_id", lead_id) // call completed
                                    .putExtra("cu_id", lead_cu_id)
                                    .putExtra("call_status_id", 1) // call completed
                                    .putExtra("lead_status_id", lead_status_id) // call completed
                                    .putExtra("call_start_time", str_callStartTime) // call start date time
                                    .putExtra("call_end_time", endDateTime) // call end date time
                                    .putExtra("call_schedule_id", call_schedule_id) // call_schedule_id iff call is being scheduled
                                    //.putExtra("record_file_path", audioFile.getAbsolutePath())
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        else
                        {
                            //other hand didn't pick my call

                            Log.e(TAG, "onCallStateChanged: onOutgoingCallEnded -- Not Answered "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);
                            Toast.makeText(context, "Adding Call Log...", Toast.LENGTH_LONG).show();
                            callAddCallLog(context, str_callStartTime);
                            if(sharedPreferences!=null) {
                                Log.e(TAG, "in sharedPreferences ");
                                editor = sharedPreferences.edit();
                                editor.putBoolean("callCompletedAdded", true);
                                editor.apply();
                            }
                            Log.e(TAG, "onCallStateChanged: callCompletedAdded");
                        }
                    }
                }
                else{

                    //onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    String endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    //Toast.makeText(context, "onOutgoingCallEnded "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCallStateChanged: onOutgoingCallEnded "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);

                    if (from_make_phone_Call) {
                        Toast.makeText(context, "Adding Call Log...", Toast.LENGTH_LONG).show();
                        callAddCallLog(context, str_callStartTime);
                    }
                }
                break;
        }
        lastState = state;
    }


    //Derived classes should override these to respond to specific events of interest
    //protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);
    //protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);
    //protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

    //protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);
    ///protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);
    //protected abstract void onMissedCall(Context ctx, String number, Date start);


    private void callAddCallLog(Context context, String str_callStartTime)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("call_status_id", 2); // call not received
        jsonObject.addProperty("call_log_purpose_id", 1);
        jsonObject.addProperty("call_log_date",  getDateTime());
        jsonObject.addProperty("start_time", getTimeFromDateTimeString(str_callStartTime));
        jsonObject.addProperty("end_time", getTimeFromDateTimeString(str_callStartTime)); //send end date time same
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("lead_status_id", lead_status_id);
        jsonObject.addProperty("call_schedule_id", call_schedule_id);
        jsonObject.addProperty("cu_id", lead_cu_id);
        jsonObject.addProperty("call_remarks", "Call not received!");
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("media_type_id", 3);

        //send reminder fields if user is adding reminder
        jsonObject.addProperty("remind_at", "");
        jsonObject.addProperty("reminder_comments",  "");
        jsonObject.addProperty("is_reminder", 0);
        jsonObject.addProperty("is_done", 0);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addCallLog(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()  && response.body().isJsonObject()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //setJson(response.body());
                                onAddCallLogSuccess(context);
                            }
                            else showErrorLog(context, context.getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(context, context.getString(R.string.something_went_wrong_try_again));
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(context, context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(context, context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(context, context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context, context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context, context.getString(R.string.weak_connection));
                else showErrorLog(context, e.toString());
            }
        });
    }


    private void showErrorLog(Context context, final String message) {
        //show error toast
        Toast.makeText(context, "Error "+message, Toast.LENGTH_SHORT).show();

        //make phone call false
        from_make_phone_Call = false;

        //set Feed Action Added to true
        if(sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.putBoolean("feedActionAdded", true);
            editor.putBoolean("callCompletedAdded", true);
            //remove lead id and cuId after adding call log
            editor.remove("call_lead_id");
            editor.remove("lead_status_id");
            editor.remove("call_schedule_id");
            editor.remove("cu_id");
            editor.remove("from_make_phone_Call");
            editor.apply();

            Log.e(TAG, "onAddCallLogSuccess: removedFrom SharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t from_make_phone_Call "+ sharedPreferences.getBoolean("from_make_phone_Call", false)
            );
        }

    }


    private void onAddCallLogSuccess(Context context)
    {
        //hide pb
        //hideProgressBar();

        //show success toast
        new Handler().postDelayed(() -> Toast.makeText(context, "Call log added successfully!", Toast.LENGTH_SHORT).show(), 2000);

        //make phone call false
        from_make_phone_Call = false;

        //set Feed Action Added to true
        if(sharedPreferences!=null)
        {
            Log.e(TAG, "in sharedPreferences " );
            editor = sharedPreferences.edit();
            editor.putBoolean("feedActionAdded", true);
            editor.putBoolean("callCompletedAdded", true);
            editor.apply();
            //remove lead id and cuId after adding call log
            editor.remove("call_lead_id");
            editor.remove("lead_status_id");
            editor.remove("call_schedule_id");
            editor.remove("cu_id");
            editor.remove("from_make_phone_Call");
            editor.apply();

            Log.e(TAG, "onAddCallLogSuccess: removedFrom SharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t from_make_phone_Call "+ sharedPreferences.getBoolean("from_make_phone_Call", false)
            );
        }

        Log.e(TAG, "onAddCallLogSuccess: callCompletedAdded" );
    }

}
