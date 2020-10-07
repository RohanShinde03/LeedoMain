package com.tribeappsoft.leedo.firebase;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.booked_customers.BookedCustomersActivity;
import com.tribeappsoft.leedo.admin.leads.AllLeadsActivity;
import com.tribeappsoft.leedo.admin.project_brochures.ProjectBrochuresActivity;
import com.tribeappsoft.leedo.admin.project_floor_plans.ProjectFloorPlanActivity;
import com.tribeappsoft.leedo.admin.project_quotations.ProjectQuotationActivity;
import com.tribeappsoft.leedo.admin.reminder.AllReminderActivity;
import com.tribeappsoft.leedo.admin.site_visits.AllSiteVisitsActivity;
import com.tribeappsoft.leedo.admin.user_profile.UserProfileActivity;
import com.tribeappsoft.leedo.admin.users.AllUsersActivity;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.models.NotificationModel;
import com.tribeappsoft.leedo.admin.notifications.NotificationsActivity;
import com.tribeappsoft.leedo.admin.callSchedule.CallScheduleMainActivity;
import com.tribeappsoft.leedo.profile.ViewProfileActivity;
import com.tribeappsoft.leedo.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.tribeappsoft.leedo.util.Helper.getDateTime;


/*
 * Created by ${ROHAN} on 5/2/19.
 */

public class FireBaseMessageService extends FirebaseMessagingService
{
    private String TAG = "FireBaseMessageService";
    NotificationCompat.Builder builder;

    private String title = "", body ="", page = null,  picture = null,lead_uid="",country_code="",mobile_number="",full_name="",project_name="",unit_category="",call_remarks="";
    private int notId = 0,lead_id=0,lead_status_id=0,lead_call_schedule_id=0;
    private Intent intent = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String BROADCAST_ACTION = "com.tribeappsoft.leado.ClaimNow";
    public static final String BROADCAST_ACTION_HOLD_FLAT = "com.tribeappsoft.leedo.holdFlat";
    //final static int RQS_1 = 2;


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.e(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        // sendRegistrationToServer(token);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.putString("fcm_token", token);
        editor.apply();
        Log.e("fcm_tokenStored", sharedPreferences.getString("fcm_token", null)!=null ? sharedPreferences.getString("fcm_token", "") : "__ empty" );

    }



    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        try {
            super.onMessageReceived(remoteMessage);
            Log.e(TAG, "msgReceive");
            // [START_EXCLUDE]
            // There are two types of messages data messages and notification messages. Data messages are handled
            // here in onMessageReceived whether the app is in the foreground or splash_background. Data messages are the type
            // traditionally used with GCM. NotificationModel messages are only received here in onMessageReceived when the app
            // is in the foreground. When the app is in the splash_background an automatically generated notification is displayed.
            // When the user taps on the notification they are returned to the app. Messages containing both notification
            // and data payloads are treated as notification messages. The Firebase console always sends notification
            // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
            // [END_EXCLUDE]

            // TODO(developer): Handle FCM messages here. DONE
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

            //Check if data needs to be processed by long running job
            /*if (true)
            {
                 //For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }*/

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0)
            {
                Log.e(TAG, "Message data payload: " + remoteMessage.getData());

                //title
                if (remoteMessage.getData().containsKey("title")) title = remoteMessage.getData().get("title") != null ? remoteMessage.getData().get("title") : "";
                else if (remoteMessage.getNotification() != null) title = remoteMessage.getNotification().getTitle() != null ? remoteMessage.getNotification().getTitle() : getString(R.string.app_name);

                //body
                if (remoteMessage.getData().containsKey("body")) body = remoteMessage.getData().get("body") != null ? remoteMessage.getData().get("body") : "";
                else if (remoteMessage.getNotification() != null) body = remoteMessage.getNotification().getBody() != null ? remoteMessage.getNotification().getBody() : "";

                //picture
                if (remoteMessage.getData().containsKey("picture")) picture = remoteMessage.getData().get("picture") != null ? remoteMessage.getData().get("picture") : "";

                //notId
                if (remoteMessage.getData().containsKey("notId")) notId = remoteMessage.getData().get("notId") != null ? Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("notId"))) : 0;

                //page
                if (remoteMessage.getData().containsKey("page")) page = remoteMessage.getData().get("page");


                if (remoteMessage.getData().containsKey("data")) {

                    Gson gson  = new Gson();
                    String extraData = remoteMessage.getData().get("data");
                    String firstChar = String.valueOf(Objects.requireNonNull(extraData).charAt(0));
                    if (firstChar.equalsIgnoreCase("[")) {
                        //json array
                        Log.e(TAG, "onMessageReceived: extraData "+extraData);
                    }else{

                        //json object
                        JsonObject jsonObject = gson.fromJson(extraData, JsonObject.class);
                        if (!jsonObject.isJsonNull() && jsonObject.has("lead_id")) lead_id = !jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("lead_status_id")) lead_status_id = !jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("lead_call_schedule_id")) lead_call_schedule_id = !jsonObject.get("lead_call_schedule_id").isJsonNull() ? jsonObject.get("lead_call_schedule_id").getAsInt() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("lead_uid")) lead_uid = !jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("country_code")) country_code = !jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("mobile_number")) mobile_number = !jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("full_name")) full_name = !jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("project_name")) project_name = !jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("unit_category")) unit_category = !jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : null;
                        if (!jsonObject.isJsonNull() && jsonObject.has("remarks")) call_remarks = !jsonObject.get("remarks").isJsonNull() ? jsonObject.get("remarks").getAsString() : null;
                        Log.e(TAG, "onMessageReceived: "+lead_uid );
                    }
                }

            }



            //showConfirmDialog();


            // Check if message contains a notification payload.
           /* if (remoteMessage.getNotification() != null)
            {

                Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

                title = remoteMessage.getNotification().getTitle() != null ? remoteMessage.getNotification().getTitle() : getString(R.string.app_name);
                body = remoteMessage.getNotification().getBody() != null ? remoteMessage.getNotification().getBody() : "";
                picture = remoteMessage.getNotification().getImageUrl() != null ? new Helper().getRealPathFromURI_2(getApplicationContext(), remoteMessage.getNotification().getImageUrl()) : "";

                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), );
            }*/

            //call method to setNotification
            setNotificationIntentData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //sendNotification(title,body, picture,intent, notId);
        }

    }



    private void setNotificationIntentData()
    {
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        if (sharedPreferences.getInt("user_id", 0) != 0)
        {

            //check user has logged in or not
            if (page != null)
            {

                switch (page)
                {
                    case "notificationPage":
                        intent = new Intent(this, NotificationsActivity.class);
                        intent.putExtra("notifyNotifications", true);  //Notifications
                        break;

                    case "new_lead":  // Add New Lead Notify
                        intent = new Intent(this, AllLeadsActivity.class);
                        intent.putExtra("notifyFeeds", true);
                        break;

                    case "new_visit":  // Add New Site Visit Notify
                        intent = new Intent(this, AllSiteVisitsActivity.class);
                        intent.putExtra("notify", true);
                        break;

                    case "new_allotment":  //Add New Allotment Notify
                        intent = new Intent(this, BookedCustomersActivity.class);
                        intent.putExtra("notify", true);
                        break;

                    case "new_doc_brochure":  // Add New Brochure Doc Notify
                        intent = new Intent(this, ProjectBrochuresActivity.class);
                        intent.putExtra("notify", true);
                        break;

                    case "new_doc_quotation":  // Add New Quotation Doc Notify
                        intent = new Intent(this, ProjectQuotationActivity.class);
                        intent.putExtra("notify", true);  //GHP success
                        break;

                    case "new_doc_floor_plan":  // Add New Floor Plan Doc Notify
                        intent = new Intent(this, ProjectFloorPlanActivity.class);
                        intent.putExtra("notify", true);  //GHP success
                        break;

                    case "new_lead_reassign"://Lead Reassign from on eto other sales person
                        intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                        intent.putExtra("notify", true);  //Notifications
                        break;

                    case "profile_update"://new user updated
                        intent = new Intent(this, UserProfileActivity.class);
                        intent.putExtra("notifyProfile", true);  //Notifications
                        break;

                    case "user_add"://new User Added
                        intent = new Intent(this, AllUsersActivity.class);
                        intent.putExtra("notify", true);  //Notifications
                        break;

                    case "ReminderPage":
                        intent = new Intent(this, AllReminderActivity.class);
                        intent.putExtra("notify", true); //unClaimed Leads
                        editor.putBoolean("applicationCreated", true);
                        break;

                    case "ScheduledCallReminder":  //Unit released by system
                        intent = new Intent(this, CallScheduleMainActivity.class);
                        intent.putExtra("notify", true);  //call schedule list
                        break;

                    case "ScheduledCallNow":
                        intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                        intent.putExtra("notify", true); //unClaimed Leads
                        editor.putBoolean("applicationCreated", true);
                        break;

                    default:
                        intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                        break;
                }
            } else {
                intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
            }
        }
        else {
            intent = new Intent(this, LoginActivity.class);
        }

        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        //call method to send notification
        if (!page.equalsIgnoreCase("ReminderPage")) sendNotification(title, body, picture, intent, notId);

        //start the service first
        //startService(new Intent(getApplicationContext(), TelephonyCallService.class));

        //save Notification
        String upload_date = null;
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy, hh:mm aaa", Locale.US);
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
            upload_date = sdf.format(c.getTime());
            Log.e(TAG, "onMessageReceived: Date  " +upload_date);
            //notifyDate = sdf.parse(upload_date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //call get unClaimed Leads
        if (page!=null)
        {
           /* if (page.equalsIgnoreCase("unClaimedLead"))
            {
                Log.e(TAG, "entered DisplayLoggingInfo");
//                if (sharedPreferences!=null) {
//                    editor = sharedPreferences.edit();
//                    editor.putBoolean("applicationCreated", true);
//                    editor.apply();
//                }

                Intent myIntent = new Intent();
                myIntent.setAction(BROADCAST_ACTION);
                myIntent.putExtra("time", upload_date);
                myIntent.putExtra("msg", body);
                myIntent.putExtra("page", page);
                sendBroadcast(myIntent);
            }
            else*/ if (page.equalsIgnoreCase("ReminderPage"))
        {

            Log.e(TAG, "entered ReminderPage");
            Intent myIntent = new Intent();
            myIntent.setAction(BROADCAST_ACTION);
            myIntent.putExtra("time", upload_date);
            myIntent.putExtra("title", title);
            myIntent.putExtra("body", body);
            myIntent.putExtra("page", page);
            sendBroadcast(myIntent);

              /*  //Calendar calNow = Calendar.getInstance();
                Intent intent = new Intent(getBaseContext(), ClaimNowReceiver.class);
                intent.putExtra("page","unClaimedLead");
                intent.putExtra("notifyFeeds",true);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getBaseContext(), RQS_1, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        pendingIntent);*/
        }
          /*  else if (page.equalsIgnoreCase("holdFlatPage"))
            {
                Intent myIntent = new Intent();
                myIntent.setAction(BROADCAST_ACTION_HOLD_FLAT);
                myIntent.putExtra("time", upload_date);
                myIntent.putExtra("title", title);
                myIntent.putExtra("body", body);
                myIntent.putExtra("page", page);
                sendBroadcast(myIntent);
            }*/
        else if (page.equalsIgnoreCase("ScheduledCallNow"))
        {

            //  Log.e(TAG, "setNotificationIntentData: "+title+""+body+page+""+lead_uid+mobile_number+""+full_name+project_name+""+unit_category);
            Intent myIntent = new Intent();
            myIntent.setAction(BROADCAST_ACTION);
            myIntent.putExtra("time", upload_date);
            myIntent.putExtra("title", title);
            myIntent.putExtra("body", body);
            myIntent.putExtra("page", page);
            myIntent.putExtra("lead_id", lead_id);
            myIntent.putExtra("lead_uid", lead_uid);
            myIntent.putExtra("lead_status_id", lead_status_id);
            myIntent.putExtra("country_code", country_code);
            myIntent.putExtra("mobile_number", mobile_number);
            myIntent.putExtra("full_name", full_name);
            myIntent.putExtra("call_remarks", call_remarks);
            myIntent.putExtra("project_name", project_name);
            myIntent.putExtra("unit_category", unit_category);
            myIntent.putExtra("lead_call_schedule_id", lead_call_schedule_id);
            sendBroadcast(myIntent);

            //Toast.makeText(this, "Call Schedule Notification", Toast.LENGTH_LONG).show();
        }

        }


        NotificationModel model = new NotificationModel();
        model.setNotification_id(notId);
        model.setTitle(title != null ? title : getString(R.string.app_name));
        model.setContent(body != null ? body : "");
        model.setPicture(picture != null ? picture : "");
        model.setPage(page != null ? page : "");
        model.setDate(upload_date != null ? upload_date : getDateTime());

        //call the method that saveNotification
        SaveNotification(model);
        //page = null;
        picture = null;
        title = null;
        body = null;

    }



    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     *
     */

    private void sendNotification(final String title, final String messageBody,
                                  String photoPath, final Intent intent, int notId)
    {
        Log.e(TAG, "sendNotification");

        //Intent intent = null;
        int count =1;
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.putInt("NotificationCount", count);
        editor.apply();

        if (photoPath!=null)
        {

            //avoids android.os.NetworkOnMainThreadException
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Bitmap bitmap; //= getBitmapfromUrl(photopath);
            try
            {
                bitmap =  new getBitmapAsync().execute(photoPath).get();
                if (intent!=null) send_PictureNotification(title, messageBody, intent, bitmap, count, notId);

            } catch (Exception e)
            {
                e.printStackTrace();
                bitmap = getBitmapfromUrl(photoPath);
                if (intent!=null) send_PictureNotification(title, messageBody, intent, bitmap, count, notId);
            }


            //new sendNotificationPicture(getApplicationContext()).execute(titile, messageBody, photopath);
        }
        else
        {
            //textNotification
            //setPendingIntent(messageBody, intent,titile);
            if (intent!=null) send_TextNotification(title,messageBody,intent, count);
        }


    }



    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title body FCM message body received.
     *
     */

    private void send_TextNotification(String title, String body, Intent intent, int count)
    {

        try
        {
            String channelId = getString(R.string.default_notification_channel_id);
            //  PendingIntent.getActivity(context, MY_UNIQUE_VALUE , notificationIntent, PendingIntent.FLAG_ONE_SHOT);
            //int request_code = new Random().nextInt();
            final int requestCode = (int) System.currentTimeMillis() / 1000;
            //int notifyId = notId + requestCode;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(getNotificationIcon()) // R.mipmap.ic_launcher
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.app_icon_leedo_foreground))
                    .setContentTitle(title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentText(body)
                    .setNumber(count)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setAutoCancel(true).setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setSound(notification_sound)
                    //Vibration
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    //LED
                    //.setLights(Color.RED, 3000, 3000)
                    //Tone
                    //.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
                    //.setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId, "Leedo App", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(requestCode, builder.build());
            }

            // Since android Oreo notification channel is needed.
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationChannel channel = new NotificationChannel
                        (channelId, title,
                        NotificationManager.IMPORTANCE_DEFAULT);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }*/


      /*  NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,extractNotifications(title,body,pendingIntent,arrayList_notification).build());*/

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void send_PictureNotification(String title, String body, Intent intent, Bitmap bitmap, int count, int notId)
    {

        try
        {
            String channelId = getString(R.string.default_notification_channel_id);
            //  PendingIntent.getActivity(context, MY_UNIQUE_VALUE , notificationIntent, PendingIntent.FLAG_ONE_SHOT);
            //int request_code = new Random().nextInt();
            final int requestCode = (int) System.currentTimeMillis() / 1000;
            //int notifyId = notId + requestCode;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(getNotificationIcon()) // R.mipmap.ic_launcher
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.app_icon_leedo_foreground))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setNumber(count)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                    .setSound(notification_sound)
                    //Vibration
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    //.setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentIntent(pendingIntent);


            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    NotificationChannel channel = new NotificationChannel(channelId, "Leedo App",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(requestCode, builder.build());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) { notificationManager.notify(notId, builder.build());
            }
        }

    }



    /*
     *To get a Bitmap image from the URL received
     * */
    public Bitmap getBitmapfromUrl(final String imageUrl)
    {
        Bitmap bitmap;
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);

            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


    @SuppressLint("StaticFieldLeak")
    private class getBitmapAsync extends AsyncTask<String,Void,Bitmap>
    {

        private getBitmapAsync() {
            super();
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {

            try
            {
                String imageUrl = params[0];
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                //Bitmap bitmap = BitmapFactory.decodeStream(input);

                return BitmapFactory.decodeStream(input);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            //do stuff
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            //do stuff
            super.onPostExecute(result);
        }
    }


    private int getNotificationIcon()
    {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
                //R.drawable.img_app_icon_new : R.drawable.img_app_icon_new;
                R.mipmap.app_icon_leedo_foreground : R.mipmap.app_icon_leedo_foreground;
    }


    private NotificationCompat.Builder extractNotifications(String title, String msg, PendingIntent contentIntent, ArrayList<String> arrayList_notification)
    {
        NotificationCompat.Builder mBuilder;
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.app_icon_leedo_foreground)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(" NotificationModel"))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setLights(Color.WHITE, 1000, 5000)
                        .setDefaults(Notification.DEFAULT_VIBRATE |
                                Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                        .setContentIntent(contentIntent);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("NotificationModel");
        for (int i=0;i<arrayList_notification.size();i++)
        {
            inboxStyle.addLine(arrayList_notification.get(i));
        }

        /*while (notifications.moveToNext()) {
            inboxStyle.addLine(notifications.getString(notifications.getColumnIndex(DatabaseHelper.NOTIFICATION_MESSAGE)));
        }*/
        inboxStyle.addLine(title + ": " + msg);
        inboxStyle.addLine(title + ": " + msg);
        inboxStyle.addLine(title + ": " + msg);
        inboxStyle.addLine(title + ": " + msg);
        inboxStyle.addLine(title + ": " + msg);
        mBuilder.setStyle(inboxStyle);
        return mBuilder;
    }


    private void SaveNotification(NotificationModel model)
    {

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        if (sharedPreferences!=null)
        {
            JSONObject jsonObject = new JSONObject();
            try
            {
                jsonObject.put("title", model.getTitle());
                jsonObject.put("content", model.getContent());
                jsonObject.put("picture", model.getPicture());
                jsonObject.put("page", model.getPage());
                jsonObject.put("date", model.getDate());
                jsonObject.put("data", "");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            String notification = null;
            if (sharedPreferences.getString("NotificationModel", null)!=null) notification = sharedPreferences.getString("NotificationModel", null);

            if (notification!=null)
            {
                try
                {
                    int count =0;
                    //JSONArray newJsonArray = new JSONArray(notification);
                    JSONArray newJsonArray = new JSONArray();
                    // 1st object
                    newJsonArray.put(jsonObject);
                    //Log.e("new_ary", newJsonArray.toString());
                    JSONArray oldJsonArray = new JSONArray(notification);
                    try
                    {
                        for(int i = 0; i < oldJsonArray.length(); i++) {
                            // prev json objects
                            newJsonArray.put(oldJsonArray.get(i));
                        }
                        count = newJsonArray.length();
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    if (sharedPreferences!=null)
                    {
                        editor= sharedPreferences.edit();
                        editor.putString("NotificationModel", newJsonArray.toString());
                        editor.putInt("NotificationCount", count);
                        editor.apply();
                        Log.e(TAG, "NewAry "+newJsonArray.toString());
                        //Log.e("jsnObj", jsonObject.toString());
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else
            {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);

                if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    editor.putString("NotificationModel", jsonArray.toString());
                    editor.apply();
                }
            }
        }

    }



    private void showConfirmDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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