package com.tribeappsoft.leedo.admin.callLog;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.getDateTime;
import static com.tribeappsoft.leedo.util.Helper.getSecondsDiff;
import static com.tribeappsoft.leedo.util.Helper.getTimeFromDateTimeString;


public class TelephonyCallService extends IntentService
{
    private String TAG = "TelephonyCallService";
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private MediaRecorder recorder;
    //  private File audioFile;
    private boolean recordStarted = false,from_make_phone_Call = false, callCompletedAdded = false;
    private Handler handler;
    private Runnable runnable;
    private final int runTime = 5000;
    // Constants
    private static final int ID_SERVICE = 101;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id =0, lead_id =0, lead_status_id =0, call_schedule_id = 0;
    private String api_token ="", lead_cu_id = "", _callStartTime,lead_name="",project_name="";

    public TelephonyCallService() {
        super("TelephonyCallService");
    }


    public TelephonyCallService(String name) {
        super(name);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        handler = new Handler();
        runnable = () -> handler.postDelayed(runnable, runTime);
        handler.post(runnable);

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.e(TAG, "onHandleIntent: ");
        if (intent!=null) {

            lead_id = intent.getIntExtra("call_lead_id",0);
            lead_status_id = intent.getIntExtra("lead_status_id",0);
            call_schedule_id = intent.getIntExtra("call_schedule_id",0);
            lead_cu_id =  intent.getStringExtra("lead_cu_id");
            lead_name =  intent.getStringExtra("lead_name");
            project_name =  intent.getStringExtra("project_name");
            user_id = intent.getIntExtra("user_id",0);
            api_token = intent.getStringExtra("api_token");
            from_make_phone_Call = intent.getBooleanExtra("from_make_phone_Call",false);
            callCompletedAdded = intent.getBooleanExtra("callCompletedAdded",false);

            Log.e(TAG, "onHandleIntent: call_schedule_id : "+call_schedule_id+"lead_id : "+lead_id+"lead_status_id : "+lead_status_id +"lead_cu_id : "+lead_cu_id );
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "StartService");

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        //if (phoneCallBroadCastReceiver!=null)
        registerReceiver(phoneCallBroadCastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = getString(R.string.default_notification_channel_id);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            Notification builder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Caller ID is Active")
                    .setSmallIcon(R.mipmap.app_icon_leedo_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("New outgoing call started from Leedo App!"))
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, builder);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // Since android Oreo notification channel is needed.
                NotificationChannel channel = new NotificationChannel(channelId, "Leedo App", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                //notificationManager.notify(requestCode, build());
            }


        } else {

            String channelId = getString(R.string.default_notification_channel_id);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Caller ID is Active")
                    .setSmallIcon(R.mipmap.app_icon_leedo_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("New outgoing call started from Leedo App!"))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            Notification notification = builder.build();
            startForeground(1, notification);
        }

        lead_id = intent.getIntExtra("call_lead_id",0);
        lead_status_id = intent.getIntExtra("lead_status_id",0);
        call_schedule_id = intent.getIntExtra("call_schedule_id",0);
        lead_cu_id = intent.getStringExtra("lead_cu_id");
        lead_name = intent.getStringExtra("lead_name");
        project_name =  intent.getStringExtra("project_name");
        user_id = intent.getIntExtra("user_id",0);
        api_token = intent.getStringExtra("api_token");
        from_make_phone_Call = intent.getBooleanExtra("from_make_phone_Call",false);
        callCompletedAdded = intent.getBooleanExtra("callCompletedAdded",false);

//        Log.e(TAG, "onStartCommand: "+lead_id+lead_status_id+lead_cu_id+from_make_phone_Call+user_id+callCompletedAdded );
        Log.e(TAG, "onStartCommand: call_schedule_id : "+call_schedule_id+"lead_id : "+lead_id+"lead_status_id : "+lead_status_id +"lead_cu_id : "+lead_cu_id );

       /* sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        if(sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            api_token = sharedPreferences.getString("api_token", "");
            user_id = sharedPreferences.getInt("user_id", 0);
            lead_cu_id = sharedPreferences.getString("cu_id", "");
            lead_id = sharedPreferences.getInt("call_lead_id", 0);
            lead_status_id = sharedPreferences.getInt("lead_status_id", 0);
            call_schedule_id = sharedPreferences.getInt("call_schedule_id", 0);
            from_make_phone_Call = sharedPreferences.getBoolean("from_make_phone_Call", false);
            callCompletedAdded = sharedPreferences.getBoolean("callCompletedAdded", false);
            editor.apply();
            Log.e(TAG, "onStartCommand : call_schedule_id"+call_schedule_id+"lead_status_id"+lead_status_id);

        }
        else
        {
            Toast.makeText(getApplicationContext(), "sharedPreferences pref null", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onStartCommand: sharedPreferences pref null");
        }
*/

        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public BroadcastReceiver phoneCallBroadCastReceiver = new BroadcastReceiver() {

        private String TAG = "phoneCallBroadCastReceiver";
        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private Date dateCallStartTime;
        private String str_callStartTime;
        private boolean isIncoming;
        private String savedNumber;  //because the passed incoming is only valid in ringing

        @Override
        public void onReceive(Context context, Intent intent) {
            //updateUI(intent);

            Log.e(TAG, "onReceive: phoneCallBroadCastReceiver ");

            if (intent!=null){

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
                    if(!recordStarted)
                    {
                        isIncoming = true;
                        dateCallStartTime = new Date();
                        str_callStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        savedNumber = number;
                        //onIncomingCallReceived(context, number, callStartTime);
                        //Toast.makeText(context, "onIncomingCallReceived "+ number + "\t date time :" + str_callStartTime, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onCallStateChanged: onIncomingCallReceived "+ number + "\t date time :" + str_callStartTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offHook are pickups of incoming calls.  Nothing done on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        isIncoming = false;
                        dateCallStartTime = new Date();
                        str_callStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        _callStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        //onOutgoingCallStarted(context, savedNumber, callStartTime);
                        //Toast.makeText(context, "onOutgoingCallStarted "+ savedNumber + "\t date time :" + str_callStartTime, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onCallStateChanged: onOutgoingCallStarted "+ savedNumber + "\t date time :" + str_callStartTime);

                        if (from_make_phone_Call) {
                            //start call recording

                            // startRecording(lead_cu_id);
                            recordStarted = true;
                        }
                    }
                    else
                    {
                        if(!recordStarted)
                        {
                            isIncoming = true;
                            dateCallStartTime = new Date();
                            //onIncomingCallAnswered(context, savedNumber, callStartTime);
                            //Toast.makeText(context, "onIncomingCallAnswered "+ savedNumber + "\t date time :" + dateCallStartTime, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onCallStateChanged: onIncomingCallAnswered "+ savedNumber + "\t date time :" + dateCallStartTime);
                        }
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
                            clearNotification();

                            if (secDiff>10) {
                                //callee has accepted an outgoing call. -- Answered

                                Log.e(TAG, "onCallStateChanged: onOutgoingCallEnded -- Answered "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);

                                //goto add call log
                                context.startActivity(new Intent(context, CallLogActivity.class)
                                        .putExtra("fromPhoneCall", true)
                                        .putExtra("callCompletedAdded", callCompletedAdded)
                                        .putExtra("call_lead_id", lead_id) // call completed
                                        .putExtra("cu_id", lead_cu_id)
                                        .putExtra("lead_name", lead_name)
                                        .putExtra("project_name",project_name)
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

                                clearNotification();

                                Log.e(TAG, "onCallStateChanged: onOutgoingCallEnded -- Not Answered "+ savedNumber + "\t start date time :" + str_callStartTime + "\t End Date Time : "+endDateTime);
                                Toast.makeText(context, "Adding Call Log...", Toast.LENGTH_LONG).show();
                                callAddCallLog(context, str_callStartTime, endDateTime);//, audioFile.getAbsolutePath()
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
                            callAddCallLog(context, str_callStartTime, endDateTime);//, audioFile.getAbsolutePath()
                        }
                    }
                    break;
            }
            lastState = state;
        }

       /* private void startRecording(String cu_id) {

            //first create parent directory
            File parentDirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/VJ_Sales/");//Tokens/
            //create parent directory
            parentDirFile.mkdir();

            //child directory
            File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/VJ_Sales/CallRecordings/");
            //file = new File(dirFile.getPath());

            if(!dirFile.exists()) {
                //create file dir
                dirFile.mkdir();
            }

        *//*File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecordingDasa1");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }*//*

            //started call
            Toast.makeText(getApplicationContext(), "Call Recording Started...", Toast.LENGTH_LONG).show();
            Log.e(TAG, "startRecording: ");

            String file_name = "Recording_"+cu_id+"__";
            try {
                audioFile = File.createTempFile(file_name, ".mp3", dirFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = audioFile.getAbsolutePath();
            Log.e("TelephonyCallService", "startRecording: path "+path );

            recorder = new MediaRecorder();
            //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            //recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            //recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audioFile.getAbsolutePath());

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
            recordStarted = true;
        }
*/



        private void callAddCallLog(Context context, String str_callStartTime, String str_callEndTime )//String record_file_path
        {

            RequestBody call_status_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(2));
            RequestBody call_log_date = RequestBody.create(MediaType.parse("text/plain"), getDateTime());
            RequestBody start_time = RequestBody.create(MediaType.parse("text/plain"), getTimeFromDateTimeString(str_callStartTime));
            RequestBody end_time = RequestBody.create(MediaType.parse("text/plain"), getTimeFromDateTimeString(str_callEndTime));
            RequestBody lead_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_id));
            RequestBody lead_status_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_status_id));
            RequestBody call_remarks = RequestBody.create(MediaType.parse("text/plain"), "");
            RequestBody sales_person_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user_id));
            RequestBody call_schedule_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(call_schedule_id));
            RequestBody api_token_part = RequestBody.create(MediaType.parse("text/plain"), api_token);
            RequestBody remind_at = RequestBody.create(MediaType.parse("text/plain"),  "");
            RequestBody reminder_comments = RequestBody.create(MediaType.parse("text/plain"),  "");
            RequestBody is_reminder = RequestBody.create(MediaType.parse("text/plain"),"0");
            RequestBody is_done = RequestBody.create(MediaType.parse("text/plain"), "0");

            ApiClient client = ApiClient.getInstance();
            client.getApiService().addCallLog(call_status_id,  call_log_date, start_time, end_time,
                    lead_id_part, lead_status_id_part, call_remarks, sales_person_id, call_schedule_id_part,  api_token_part, remind_at, reminder_comments,  is_reminder, is_done ).enqueue(new Callback<JsonObject>()
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
        }


        private void onAddCallLogSuccess(Context context)
        {
            //hide pb
            //hideProgressBar();

            //show success toast
            Toast.makeText(context, "Call log added successfully!", Toast.LENGTH_SHORT).show();

            //make phone call false
            from_make_phone_Call = false;

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("callCompletedAdded", true);
               /* //remove lead id and cuId after adding call log
                editor.remove("cu_id");
                editor.remove("call_lead_id");
                editor.remove("lead_status_id");
                editor.remove("call_schedule_id");
                editor.remove("from_make_phone_Call");*/
                editor.apply();
            }
            Log.e(TAG, "onAddCallLogSuccess:callCompletedAdded " +callCompletedAdded);
        }
    };

    @Override
    public void onDestroy() {
        Log.e(TAG, "destroy");
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

        //open add Call page if destroyed activity
        if (recordStarted) openAddCallLog();

        //stop Recoding
        //stopRecording();
        clearNotification();

        if (phoneCallBroadCastReceiver!=null) unregisterReceiver(phoneCallBroadCastReceiver);

        //this.unregisterReceiver(new PhoneCallReceiver());
        super.onDestroy();
    }

    private void clearNotification() {

        recordStarted = false;
        from_make_phone_Call=false;

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            stopForeground( false );
            notificationManager.cancel(1);
            notificationManager.cancelAll();
        }

        //stop foreGround when onDestroyed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_DETACH);
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    /*private void stopRecording() {
        if (recordStarted) {
            Log.e(TAG, "stopRecording: ");
            Toast.makeText(getApplicationContext(), "Call Recording Saved!", Toast.LENGTH_LONG).show();
            recorder.stop();
            recorder.release();
            recorder = null;
            recordStarted = false;
            from_make_phone_Call=false;

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                stopForeground( false );
                notificationManager.cancel(1);
                //notificationManager.cancelAll();
            }

            //stop foreGround when onDestroyed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(Service.STOP_FOREGROUND_DETACH);
                stopForeground(Service.STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true);
            }
        }
    }
*/

    private void openAddCallLog()
    {
        Log.e(TAG, "openAddCallLog: when destroyed!");
        String endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        //goto add call log
        getBaseContext().startActivity(new Intent(getBaseContext(), CallLogActivity.class)
                .putExtra("fromPhoneCall", true)
                .putExtra("callCompletedAdded", callCompletedAdded)
                .putExtra("call_lead_id", lead_id) // call completed
                .putExtra("cu_id", lead_cu_id)
                .putExtra("lead_name", lead_name)
                .putExtra("project_name",project_name)
                .putExtra("call_status_id", 1) // call completed
                .putExtra("lead_status_id", lead_status_id) // call completed
                .putExtra("call_start_time", _callStartTime!=null ? _callStartTime : endDateTime) // call start date time
                .putExtra("call_end_time", endDateTime) // call end date time
                .putExtra("call_schedule_id", call_schedule_id) // call_schedule_id iff call is being scheduled
                // .putExtra("record_file_path", audioFile.getAbsolutePath())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}