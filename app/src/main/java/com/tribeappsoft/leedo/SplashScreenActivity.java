package com.tribeappsoft.leedo;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


import com.tribeappsoft.leedo.accountsHead.AccountsHeadHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.booked_customers.BookedCustomersActivity;
import com.tribeappsoft.leedo.admin.callSchedule.CallScheduleMainActivity;
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
import com.tribeappsoft.leedo.profile.ViewProfileActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.MainActivity;
import com.tribeappsoft.leedo.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class SplashScreenActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String host = null, webLabel = null, page = null, title= null, body=null, picture=null;
    private Uri webUri = null;
    private int notId =0;
    private boolean exitApp = false;
    private String TAG="SplashScreenActivity";
    //private String TAG="SplashScreenActivity";
    // http://admin.privateedukolhapur.com/#/studentevent/3


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Make sure this is before calling super.onCreate
        //this.setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash_screen);

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        //set packageManager for Device Admin
        //PackageManager p = getPackageManager();
        // ComponentName componentName = new ComponentName(this, SplashScreenActivity.class);// activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        //p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //startService(new Intent(context, TelephonyCallService.class));

        //handle the web intents
        String TAG = "SplashScreen";
        if (getIntent()!=null) {

            //check exit from App for claim now lead popup
            exitApp = getIntent().getBooleanExtra("exitApp", false);

            Uri data = getIntent().getData();

            if (data != null)
            {
                String scheme = data.getScheme();
                if (scheme != null) Log.e("scheme", "" + scheme);

                host = data.getHost();
                if (host != null) Log.e("host", "" + host);

                webUri = data;
                //webUri = Uri.parse(data.toString());

                List<String> path = getIntent().getData().getPathSegments();
                //String url = data.toString(); //.substring(19 , data.toString().length());
                //Log.e(TAG, "url " + url);

                //String p = path.get(0);
                //Log.e(TAG, "onCreate: p-- "+ p );
                webLabel =  path!=null && path.size()>0  ? path.get(0) : "homeScreen";
                if (webLabel != null) Log.e(TAG, "webLabel " + webLabel);

            }


            //handle notification tray here
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                // Create channel to show notifications.
                String channelId  = getString(R.string.default_notification_channel_id);
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null)
                {
                    notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                            channelName, NotificationManager.IMPORTANCE_HIGH));
                }
            }


            // If a notification message is tapped, any data accompanying the notification
            // message is available in the intent extras. In this sample the launcher
            // intent is fired when the notification is tapped, so any accompanying data would
            // be handled here. If you want a different intent fired, set the click_action
            // field of the notification message to the desired intent. The launcher intent
            // is used when no click_action is specified.
            //
            // Handle possible data accompanying notification message.
            // [START handle_data_extras]

            if (getIntent().getExtras() != null)
            {
                for (String key : getIntent().getExtras().keySet())
                {
                    Object value = getIntent().getExtras().get(key);
                    Log.d(TAG, "Key: " + key + " Value: " + value);


                    //page
                    if (getIntent().getExtras().containsKey("page")) page = String.valueOf(getIntent().getExtras().get("page"));

                    //title
                    if (getIntent().getExtras().containsKey("title")) title = String.valueOf(getIntent().getExtras().get("title"));

                    //body
                    if (getIntent().getExtras().containsKey("body")) body = String.valueOf(getIntent().getExtras().get("body"));

                    //picture
                    if (getIntent().getExtras().containsKey("picture")) picture = String.valueOf(getIntent().getExtras().get("picture"));

                    //not Id
                    if (getIntent().getExtras().containsKey("notId")) notId = Integer.parseInt(getIntent().getExtras().getString("notId"));

                }

            }

        }

        //code for creating app shortCut Menus
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();

            //check if logged in
            if (sharedPreferences.contains("mobile_number") || sharedPreferences.contains("user_id"))
            {
                if (sharedPreferences.getInt("user_id", 0) != 0)
                {
                    //create shortcut
                    new Helper().createShortCut(this, true);

                } else new Helper().createShortCut(this,false);
                //remove shortcut

            } else new Helper().createShortCut(this,false);
            //remove shortcut
        }

    }


    @Override
    protected void onResume()
    {
        super.onResume();
        //getToken();

        //handle push here
        if (page!=null)
        {
            Intent intent;

            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.apply();

                int user_type_id = sharedPreferences.getInt("user_type_id",0);

                Log.e(TAG, "onResume: user_type_id"+user_type_id );

                if (user_type_id == 1)
                {
                    //sales person
                    switch (page)
                    {

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

                        case "ReminderPage":
                            intent = new Intent(this, AllReminderActivity.class);
                            intent.putExtra("notifyReminders", true);  //Notifications
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

                        case "new_lead_reassign"://Lead Reassign from on eto other sales person
                            intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                            intent.putExtra("notify", true); //unClaimed Leads
                            break;

                        case "profile_update"://new user updated
                            intent = new Intent(this, UserProfileActivity.class);
                            intent.putExtra("notifyProfile", true);  //Notifications
                            break;

                        case "user_add"://new User Added
                            intent = new Intent(this, AllUsersActivity.class);
                            intent.putExtra("notify", true);  //Notifications
                            break;



                        default:
                            intent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                            break;
                    }

                }
                else if (user_type_id ==2)
                {

                    //site engineer
                    switch (page)
                    {

                        case "notificationPage":
                            intent = new Intent(this, MainActivity.class);
                            intent.putExtra("notifyNotifications", true);  //Notifications
                            break;

                        default:
                            intent = new Intent(this, MainActivity.class);
                            break;
                    }

                }
                else if (user_type_id ==3)
                {
                    //accounts head
                    switch (page)
                    {

                        case "notificationPage":
                            intent = new Intent(this, MainActivity.class);
                            intent.putExtra("notifyNotifications", true);  //Notifications
                            break;

                        default:
                            intent = new Intent(this, AccountsHeadHomeNavigationActivity.class);
                            break;
                    }

                }
                else
                {
                    //goto home screen
                    intent = new Intent(this, LoginActivity.class);
                }
            }
            else
            {
                //goto home screen
                intent = new Intent(this, LoginActivity.class);
            }


            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();


            String upload_date =null;
            try
            {
                Calendar c = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                upload_date = sdf.format(c.getTime());
                //notifyDate = sdf.parse(upload_date);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            //save Notification
            NotificationModel model = new NotificationModel();
            model.setNotification_id(notId);
            model.setTitle(title!=null ? title : getString(R.string.app_name) );
            model.setContent(body!=null ? body : "");
            model.setPicture(picture!=null ? picture : "");
            model.setPage(page!=null ? page : "");
            model.setPage(page!=null ? page : "");
            model.setDate(upload_date!=null ? upload_date : Helper.getDateTime());

            //call the method that saveNotification
            SaveNotification(model);
            page = null;
            picture = null;
            title = null;
            body = null;


        } // [END handle_data_extras]
        else if (exitApp)
        {

            new Handler().postDelayed(() -> {

                //do exit from if coming from claim now lead popup
                finishAffinity(); // Close all Activities
                //exit from app
                System.exit(0);
            }, 1500);

        }
        else
        {
            //checkUserStatus();
            //new Handler().postDelayed(this::checkUserStatus, 10);
            new Handler(getMainLooper()).postDelayed(this::checkUserStatus, 0);
        }

        //new Handler().postDelayed(this::checkUserStatus, 3000);
    }


//    private void getToken()
//    {
//        FirebaseIDService firebaseId=new FirebaseIDService();
//        String token_firebase=firebaseId.getFireBaseToken();
//
//        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
//        if (sharedPreferences!=null)
//        {
//            if (token_firebase!=null)
//            {
//                editor = sharedPreferences.edit();
//                editor.putString("fcmtokensplash", token_firebase);
//                editor.apply();
//                Log.d("fcmtokenStored", token_firebase);
//            }
//        }
//    }



    /*
     * Call the method that checks the user is already signed in or not
     *
     * */
    private void checkUserStatus()
    {

        if (sharedPreferences!=null)
        {
            //check if already signUp or not
            if (sharedPreferences.getString("mobile_number",null)!=null)
            {
                //check for whether user is sales person or site engineer or accounts head
                if (sharedPreferences.getInt("user_type_id",0)!=0)
                {
                    int user_type_id = sharedPreferences.getInt("user_type_id",0);
                    if (host==null)
                    {

                        //do normal home process
                        if (user_type_id ==1 ) {
                            //user is sales person
                            gotoSalesPersonHomeScreen();
                        }
                        else if (user_type_id ==2) {
                            //site engineer

                        }
                        else if (user_type_id ==3)
                        {
                            //accounts head
                            gotoAccountsHeadHomeScreen();
                        }
                        else gotoLoginScreen();


                    }
                    else
                    {

                        //check the host from web intents and navigate to the activity
                        Intent webIntent;
                        switch (webLabel)
                        {

                            case "staffevent":
                                webIntent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                                //webIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                break;

                            case "homeScreen":
                                webIntent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                                //webIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                break;

                            default:
                                webIntent = new Intent(this, SalesPersonHomeNavigationActivity.class);
                                break;
                        }

                        webIntent.putExtra("webUri", webUri);
                        webIntent.putExtra("isWebUri", true);
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(webIntent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();

                    }

                }
                else
                {
                    //go to the login screen
                    gotoLoginScreen();
                }
            }
            else
            {
                //go to the login screen
                gotoLoginScreen();
            }

        }
        else
        {
            //go to the login screen
            gotoLoginScreen();
        }

    }

    private void gotoLoginScreen()
    {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out);
        finish();
    }

    void gotoSalesPersonHomeScreen()
    {
        startActivity(
                new Intent(this, SalesPersonHomeNavigationActivity.class)
                //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                //.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

    }

    void gotoAccountsHeadHomeScreen()
    {
        startActivity(
                new Intent(this, SalesPersonHomeNavigationActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

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
                        for(int i = 0; i < oldJsonArray.length(); i++)
                        {
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
                        Log.e("NewAry", newJsonArray.toString());
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
}