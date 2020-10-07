package com.tribeappsoft.leedo.salesPerson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.appUpdate.GooglePlayStoreAppVersionNameLoader;
import com.tribeappsoft.leedo.appUpdate.WSCallerVersionListener;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.admin.callLog.DeviceAdminDemoReceiver;
import com.tribeappsoft.leedo.salesPerson.homeFragments.BottomSheetFragment;
import com.tribeappsoft.leedo.salesPerson.homeFragments.FragmentSalesPersonHomeFeeds;
import com.tribeappsoft.leedo.salesPerson.homeFragments.FragmentSalesPersonLeads;
import com.tribeappsoft.leedo.salesPerson.homeFragments.FragmentSalesPersonPerformance;
import com.tribeappsoft.leedo.salesPerson.homeFragments.FragmentSalesPersonReminders;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.StausBarTransp;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class SalesPersonBottomNavigationActivity extends AppCompatActivity implements WSCallerVersionListener
{
    @BindView(R.id.bottomNav_view_salesPerson) BottomNavigationView navView;
    @BindView(R.id.view_salesPerson_disableLayout) View viewDisableLayout;
    @BindView(R.id.fab_salesPerson_add) FloatingActionButton fab_add;
    @BindView(R.id.tv_salesPerson_titleAddLead) AppCompatTextView tv_titleAddLead;
    @BindView(R.id.fab_salesPerson_addSiteVisit) FloatingActionButton fab_addSiteVisit;
    @BindView(R.id.tv_salesPerson_titleAddSiteVisit) AppCompatTextView tv_titleAddSiteVisit;
    @BindView(R.id.fab_salesPerson_addToken) FloatingActionButton fab_addToken;
    @BindView(R.id.tv_salesPerson_titleAddToken) AppCompatTextView tv_titleAddToken;
    @BindView(R.id.fab_salesPerson_addReminder) FloatingActionButton fab_addReminder;
    @BindView(R.id.tv_salesPerson_titleAddReminder) AppCompatTextView tv_titleAddReminder;
    @BindView(R.id.fab_salesPerson_directBooking) FloatingActionButton fab_directBooking;
    @BindView(R.id.tv_salesPerson_titleDirectBooking) AppCompatTextView tv_titleDirectBooking;

    private Boolean isFabOpen = false,notifyReminders=false, notifyPerformance = false;
    private Animation fab_open,fab_close,rotate_forward, rotate_backward;
    private Activity context;
    private String TAG = "SalesPersonBottomNavigationActivity", TODO =null, api_token="",
            other_ids ="", display_text ="", android_id ="";
    private static final int Permission_CODE_RPS = 321;
    private static final int Permission_CODE_DeviceAdmin = 5912;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int currentTab = 1, prev_selected_item_id =0, user_id =0, openFlag =0, leadClicked = 0;
    private boolean isForceUpdate = true;

    //support in-app update
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private static final int REQ_CODE_VERSION_UPDATE = 530;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_peson_bottom_navigation);
        //overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = SalesPersonBottomNavigationActivity.this;

        if (getIntent()!=null)
        {
            //showLeads = getIntent().getBooleanExtra("showLeads", false);
            notifyReminders = getIntent().getBooleanExtra("notifyReminders", false);
            notifyPerformance = getIntent().getBooleanExtra("notifyPerformance", false);
            openFlag = getIntent().getIntExtra("openFlag", 0);
            other_ids = getIntent().getStringExtra("other_ids");
            display_text = getIntent().getStringExtra("display_text");
        }

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //set status bar color
        StausBarTransp(context);

        //set Animation to Fab
        fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_backward);


        //get android id
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(TAG, "onCreate: android id "+android_id );

        if (applicationCreated) {
            //check for the device admin permission allowed or not
            checkForDeviceAdminPermission();
        }
        //start the service
        //startService(new Intent(context, TelephonyCallService.class));

        //set navigation graph
        //NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fl_salesPerson_homeContainer);
        //NavigationUI.setupWithNavController(navView, navHostFragment.getNavController());


        if(notifyReminders)
        {
            //set def frag
            viewFragment(new FragmentSalesPersonReminders(), "OtherFragment", R.id.bNav_salesPerson_reminders);
            navView.setSelectedItemId(R.id.bNav_salesPerson_reminders);
            //set actionBarTitle
            setActionBarTitle(getString(R.string.reminder));  //as default fragment is LeadsFragment
        }
        else if (notifyPerformance)
        {
            Log.e(TAG, "onCreate:  notifyPerformance" );
            Bundle bundle = new Bundle();
            bundle.putBoolean("notifyPerformance", true);
            bundle.putInt("openFlag", openFlag);
            bundle.putString("other_ids", other_ids);
            bundle.putString("display_text", display_text);
            Fragment fragment = new FragmentSalesPersonHomeFeeds();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //custom transition animation
            //fragmentTransaction.setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_in);
            fragmentTransaction.replace(R.id.fl_salesPerson_homeContainer, fragment);
            fragmentTransaction.commit();
            //set selected
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);
            //set actionBarTitle
            setActionBarTitle(getString(R.string.feeds));
        }
        else setDefaultFrag();//set def frag

        //set navItem select listener
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // add Lead main
        fab_add.setOnClickListener(view -> {
            if (isFabOpen) {

                if (leadClicked!=1) startActivityForResult(new Intent(context, AddNewLeadActivity.class), 111);
                new Handler().postDelayed(() -> {
                    animateFAB();
                    leadClicked = 1;
                }, 100);

                // new Handler().postDelayed(this::animateFAB, 10);
            }
            else {
                animateFAB();
            }
        });

        tv_titleAddLead.setOnClickListener(view -> {
            if (isFabOpen) {

                if (leadClicked!=1) startActivityForResult(new Intent(context, AddNewLeadActivity.class), 111);
                new Handler().postDelayed(() -> {
                    animateFAB();
                    leadClicked = 1;
                }, 100);

            } else {
                animateFAB();
            }

        });


        //site visit
        fab_addSiteVisit.setOnClickListener(view ->{

            animateFAB();
            new Handler(getMainLooper()).postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 1)
                    .putExtra("forId", 1)), 200);
        });

        tv_titleAddSiteVisit.setOnClickListener(view -> {

            animateFAB();
            new Handler(getMainLooper()).postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 1)
                    .putExtra("forId", 1)
            ), 200);
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 112);
        });


        //token
        fab_addToken.setOnClickListener(view ->{

            animateFAB();
            new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 2)
                    //.putExtra("forId", 2) TODO for update S.E can generate GHP WO site visit so forID = 3
                    .putExtra("forId", 3)
            ), 200);

        });

        tv_titleAddToken.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 2)
                    //.putExtra("forId", 2) TODO for update S.E can generate GHP WO site visit so forID = 3
                    .putExtra("forId", 3)
            ), 200);
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 113);

        });

        //direct booking
        fab_directBooking.setOnClickListener(view ->{
            animateFAB();
            new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 3)
                    .putExtra("forId", 3)
            ), 200);

        });
        tv_titleDirectBooking.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 3)
                    .putExtra("forId", 3)
            ), 200);
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 113);

        });



        //add reminder
        fab_addReminder.setOnClickListener(view ->{
            animateFAB();
            new Handler().postDelayed(() -> startActivityForResult(new Intent(context, AddReminderActivity.class).putExtra("fromOther", 1), 114), 200);
            //new Helper().openReminderIntent(context, "Reminder test title", "Reminder test description", "2020-03-07 10:09:50", "2020-03-08 16:10:50");
        });
        tv_titleAddReminder.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> startActivityForResult(new Intent(context, AddReminderActivity.class).putExtra("fromOther", 1), 114), 200);
        });

        //fab_add.setOnClickListener(view -> animateFAB());
        viewDisableLayout.setOnClickListener(v -> animateFAB());


        if (isNetworkAvailable(context))//check for app update
            new GooglePlayStoreAppVersionNameLoader(getApplicationContext(), SalesPersonBottomNavigationActivity.this).execute();

        //check in-app update
        //checkForAppUpdate();

    }


    public void setDefaultFrag() {

        //set def frag
        viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
        navView.setSelectedItemId(R.id.bNav_salesPerson_home);
        //set actionBarTitle
        setActionBarTitle(getString(R.string.feeds));  //as default fragment is LeadsFragment
    }


    //On Start
    @Override
    protected void onStart() {

        super.onStart();
        //check and call update fcm token
        if (isNetworkAvailable(this)) {
            //call get Token
            getToken();
        } else NetworkError(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //set prev selected item id
        navView.setSelectedItemId(prev_selected_item_id);

        //close fab open when returning back from activities opened by fab click
        //if (isClickEvent)animateFAB();

        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            getCheckTokenValidity();

            //check new version state
            //checkNewAppVersionState();
        }
        else NetworkError(context);

    }


    private void checkForDeviceAdminPermission()
    {
        try {
            // Initiate DevicePolicyManager.
            DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mAdminName = new ComponentName(this, DeviceAdminDemoReceiver.class);

            if (!Objects.requireNonNull(mDPM).isAdminActive(mAdminName)) {

                new Handler().postDelayed(() -> {
                    //set secure application dialog
                    showSecureApplicationAlert(mAdminName);
                }, 3000);

            } else {
                //mDPM.lockNow();
                //finish();
                Log.e(TAG, "checkForDeviceAdminPermission: Device Admin Added!");
                //startService(new Intent(context, TelephonyCallService.class));

//                 Intent intent = new Intent(MainActivity.this,
//                 TrackDeviceService.class);
//                 startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void showSecureApplicationAlert(ComponentName mAdminName)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getResources().getString(R.string.que_secure_your_application));
        tv_desc.setText(getString(R.string.msg_secure_sales_app));
        btn_negativeButton.setText(getString(R.string.not_now));
        btn_positiveButton.setText(getString(R.string.secure));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
            startActivityForResult(intent, Permission_CODE_DeviceAdmin);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        alertDialog.setOnDismissListener(dialogInterface -> {
            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                //update flag -- first time
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }
        });

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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }








    private void checkForAppUpdate() {

        Log.e(TAG, "checkForAppUpdate: ");

        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = installState -> {
            // Show module progress, log state, or install the update.
            if (installState.installStatus() == InstallStatus.DOWNLOADED)
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popupSnackbarForCompleteUpdateAndUnregister();
        };

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                Log.e(TAG, "checkForAppUpdate: update Available");

                // Request the update.
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                    // Before starting an update, register a listener for updates.
                    appUpdateManager.registerListener(installStateUpdatedListener);
                    // Start an update.
                    startAppUpdateFlexible(appUpdateInfo);
                }
                else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ) {
                    // Start an update.
                    startAppUpdateImmediate(appUpdateInfo);
                }
            }
        });
    }


    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }


    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {

        Snackbar snackbar = Snackbar.make(context.findViewById(android.R.id.content), "Update Downloaded!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.restart, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.main_white));
        snackbar.show();

        unregisterInstallStateUpdListener();
    }


    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            //FLEXIBLE:
                            // If the update is downloaded but not installed,
                            // notify the user to complete the update.
                            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                popupSnackbarForCompleteUpdateAndUnregister();
                            }

                            //IMMEDIATE:
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                startAppUpdateImmediate(appUpdateInfo);
                            }
                        });

    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }



    //Check Permission
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    //Get Token
    private void getToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = Objects.requireNonNull(task.getResult()).getToken();
                    // Log and toast
                    //Log.e(TAG, " "+ token);

                    /*if (!token.isEmpty())
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            //check Read Phone state permission
                            if (checkPermission()) {
                                getDeviceIMEI();

                                //call UpdateFCM api
                                call_updateFCM(false);
                            }
                            else requestPermissionReadPhoneState();
                        }
                        else {
                            getDeviceIMEI();

                            //call UpdateFCM api
                            call_updateFCM(false);
                        }
                    }*/
                    sharedPreferences = new Helper().getSharedPref(context);
                    editor = sharedPreferences.edit();
                    editor.putString("fcm_token", token );
                    editor.apply();
                    Log.e("fcm_tokenStored_1", sharedPreferences.getString("fcm_token", null)!=null ? sharedPreferences.getString("fcm_token", "") : "__ empty" );

                    //call UpdateFCM api
                    call_updateFCM(false);

                });
    }


    private void requestPermissionReadPhoneState()
    {

        //check permission were granted or not
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        {

            //new Helper().showCustomToast(this, getString(R.string.file_permissionRationale));
            return;
        }*/


        //shows permission dialog
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE))
        {

            //new Helper().showCustomToast(this, getString(R.string.file_permissionRationale));
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.READ_PHONE_STATE
                }, Permission_CODE_RPS);

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == Permission_CODE_RPS)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open documents once permission is granted

                //call get Token
                //getToken();

                if (isNetworkAvailable(this)) {
                    //call UpdateFCM api
                    call_updateFCM(false);

                } else NetworkError(this);

            }

        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111  && resultCode  == RESULT_OK) {
            /// From Add Lead form
            data.getStringExtra("result");
            viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result"));
            leadClicked = 0;
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName());
        }
        else if ( requestCode == 112  && resultCode  == RESULT_OK) {
            // From Site Visit
            data.getStringExtra("result");
            viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);

            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if ( requestCode == 113  && resultCode  == RESULT_OK) {
            // From Tokens
            data.getStringExtra("result");
            viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if (requestCode == 114  && resultCode  == RESULT_OK) {
            //From Reminders
            data.getStringExtra("result");
            viewFragment(new FragmentSalesPersonReminders(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_reminders);
            setActionBarTitle(getString(R.string.reminder));
            navView.setSelectedItemId(R.id.bNav_salesPerson_reminders);
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if (requestCode == 121 && resultCode == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }
        else if (requestCode == 111  && resultCode  == RESULT_CANCELED) {
            Log.e(TAG, "onActivityResult: Lead Cancelled" );
            leadClicked = 0;
        }

        //device admin permission
        else if (requestCode == Permission_CODE_DeviceAdmin && resultCode == RESULT_OK) {
            //start the service here
            Log.e(TAG, "onActivityResult: Device Admin Permission Grant");
            //startService(new Intent(context, TelephonyCallService.class));
            //finish();
        }

        //handle failure result
        if (requestCode == REQ_CODE_VERSION_UPDATE && resultCode != RESULT_OK) {

            Log.d(TAG, "Update flow failed! Result code: " + resultCode);
            // If the update is cancelled or fails,
            // you can request to start the update again.
            unregisterInstallStateUpdListener();
        }

    }

    private void animateFAB()
    {

        if(isFabOpen)
        {

            //fab_share.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_white));
            //fab_share.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close_white));
//            fab_share.startAnimation(rotate_backward);

            fab_add.startAnimation(rotate_backward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.main_white)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add_icon));

            //close view
            closeView();

            fab_addSiteVisit.startAnimation(fab_close);
            fab_addSiteVisit.setClickable(false);
            fab_addToken.startAnimation(fab_close);
            fab_addToken.setClickable(false);
            fab_addReminder.startAnimation(fab_close);
            fab_addReminder.setClickable(false);
            fab_directBooking.startAnimation(fab_close);
            fab_directBooking.setClickable(false);

            /*fab_ni.startAnimation(fab_close);
            fab_cold.startAnimation(fab_close);
            fab_hot.setClickable(false);
            fab_ni.setClickable(false);
            fab_cold.setClickable(false);*/

            new Animations().slideOutBottom(tv_titleAddToken);
            new Animations().slideOutBottom(tv_titleAddSiteVisit);
            new Animations().slideOutBottom(tv_titleAddLead);
            new Animations().slideOutBottom(tv_titleAddReminder);
            new Animations().slideOutBottom(tv_titleDirectBooking);

            //hide textView
            tv_titleAddLead.setVisibility(View.GONE);
            tv_titleAddSiteVisit.setVisibility(View.GONE);
            tv_titleAddToken.setVisibility(View.GONE);
            tv_titleAddReminder.setVisibility(View.GONE);
            tv_titleDirectBooking.setVisibility(View.GONE);

            viewDisableLayout.setVisibility(View.GONE);
            isFabOpen = false;


        }
        else
        {

            fab_add.startAnimation(rotate_forward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.main_black)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_person_add_rotated_24px));

            //open view
            openView();

            fab_addSiteVisit.startAnimation(fab_open);
            fab_addSiteVisit.setClickable(true);
            fab_addToken.startAnimation(fab_open);
            fab_addToken.setClickable(true);
            fab_addReminder.startAnimation(fab_open);
            fab_addReminder.setClickable(true);
            fab_directBooking.startAnimation(fab_open);
            fab_directBooking.setClickable(true);

            /*fab_ni.startAnimation(fab_open);
            fab_cold.startAnimation(fab_open);
            fab_ni.setClickable(true);
            fab_cold.setClickable(true);*/

            //visible textView
            tv_titleAddLead.setVisibility(View.VISIBLE);
            tv_titleAddSiteVisit.setVisibility(View.VISIBLE);
            tv_titleAddToken.setVisibility(View.VISIBLE);
            tv_titleAddReminder.setVisibility(View.VISIBLE);
            tv_titleDirectBooking.setVisibility(View.VISIBLE);

            new Animations().slideInBottomFab(tv_titleAddToken);
            new Animations().slideInBottomFab(tv_titleAddSiteVisit);
            new Animations().slideInBottomFab(tv_titleAddLead);
            new Animations().slideInBottomFab(tv_titleAddReminder);
            new Animations().slideInBottomFab(tv_titleDirectBooking);

            viewDisableLayout.setVisibility(View.VISIBLE);
            isFabOpen = true;

        }
    }



    private void openView()
    {
        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.VISIBLE);
    }

    private void closeView()
    {

        // Hide the Panel
        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.GONE);
    }


    /*private void set_default(BottomNavigationView navView)
    {
        Fragment selectedFragment = new FragmentSalesPersonLeads();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_salesPerson_homeContainer, selectedFragment);
        transaction.commitAllowingStateLoss();
        navView.setSelectedItemId(R.id.bNav_salesPerson_leads);
    }*/




    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item ->
    {
        //Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.bNav_salesPerson_menu:
                if (isFabOpen) animateFAB();
                //item.setCheckable(false);
                BottomSheetFragment bf = new BottomSheetFragment();
                bf.show(getSupportFragmentManager(), bf.getTag());
                navView.setSelectedItemId(prev_selected_item_id);
                //bf.setArguments(bundle);
                return true;
            case R.id.bNav_salesPerson_home:
                if (isFabOpen) animateFAB();
                if(currentTab != 1) viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home); currentTab = 1;
                setActionBarTitle(getString(R.string.feeds));
                return true;
            case R.id.bNav_salesPerson_leads:
                if (isFabOpen) animateFAB();
                if(currentTab != 2) viewFragment(new FragmentSalesPersonLeads(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_leads);currentTab = 2;
                setActionBarTitle(getString(R.string.leads));
                return true;
            case R.id.bNav_salesPerson_myPerformance:
                if (isFabOpen) animateFAB();
                if(currentTab != 3) viewFragment(new FragmentSalesPersonPerformance(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_myPerformance);currentTab = 3;
                setActionBarTitle(getString(R.string.my_performance));
                return true;
        /*    case R.id.bNav_salesPerson_bookings:
                if (isFabOpen) animateFAB();
                if(currentTab != 4) viewFragment(new FragmentSalesPersonBookings(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_bookings); currentTab = 4;
                setActionBarTitle(getString(R.string.bookings));
                return true;*/
            case R.id.bNav_salesPerson_reminders:
                if (isFabOpen) animateFAB();
                //if(currentTab != 2) selectedFragment = new FragmentSalesPersonReminders(); currentTab = 2;
                if(currentTab != 5) viewFragment(new FragmentSalesPersonReminders(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_reminders); currentTab = 5;
                setActionBarTitle(getString(R.string.reminder));
                return true;

        }
//        if (selectedFragment!=null)
//        {
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fl_salesPerson_homeContainer, selectedFragment);
//            transaction.commitAllowingStateLoss();
//            //return true;
//        }
        //else return false;
        return true;

    };


    private void viewFragment(Fragment fragment, String name, int itemId)
    {
        //set prev itemId
        prev_selected_item_id = itemId;
        //final FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //custom transition animation
        //fragmentTransaction.setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_in);
        fragmentTransaction.replace(R.id.fl_salesPerson_homeContainer, fragment);
        // 1. Know how many fragments there are in the stack
        //final int count = getSupportFragmentManager().getBackStackEntryCount();
        // 2. If the fragment is **not** "home type", save it to the stack
        //if( name.equals( "FRAGMENT_OTHER" ) ) {
            //fragmentTransaction.addToBackStack(name);
        //}
        // Commit !
        fragmentTransaction.commit();
        // 3. After the commit, if the fragment is not an "home type" the back stack is changed, triggering the
        // OnBackStackChanged callback
//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
//        {
//            @Override
//            public void onBackStackChanged()
//            {
//                // If the stack decreases it means I clicked the back button
//                if( getSupportFragmentManager().getBackStackEntryCount() <= count)
//                {
//                    // pop all the fragment and remove the listener
//                    getSupportFragmentManager().popBackStack("FRAGMENT_OTHER", POP_BACK_STACK_INCLUSIVE);
//                    getSupportFragmentManager().removeOnBackStackChangedListener(this);
//                    // set the home button selected
//                    navView.getMenu().getItem(1).setChecked(true);
//                    setActionBarTitle(getString(R.string.feeds));  //as default fragment is LeadsFragment
//                }
//            }
//        });
    }



    private void setActionBarTitle(String actionBarTitle)
    {
        if (getSupportActionBar()!=null)
        {
            ///getSupportActionBar().setTitle(s);

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(actionBarTitle);

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            //getSupportActionBar().setElevation(0); // to disable action bar elevation
        }
    }

    public void setSelected()
    {
        navView.getMenu().getItem(1).setChecked(true);
        setActionBarTitle(getString(R.string.feeds));
    }



    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */

    /*IMEI Device*/
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return TODO;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceUniqueIdentifier = tm.getImei();

                }else deviceUniqueIdentifier = tm.getDeviceId();
            }
            else deviceUniqueIdentifier = tm.getDeviceId();
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }


    //Call Update FCM
    private void call_updateFCM(boolean isLogout)
    {

        JsonObject jsonObject =new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id", user_id);
        //TODO commented
        //jsonObject.addProperty("device_id", isLogout ? "" : getDeviceIMEI()!=null ? getDeviceIMEI() : "");
        jsonObject.addProperty("device_id", isLogout ? "" : android_id!=null ? android_id : "");
        jsonObject.addProperty("device_type", "android");
        //for logout send blank fcm token
        jsonObject.addProperty("fcm_token", isLogout ? " " : sharedPreferences.getString("fcm_token", ""));


        ApiClient client = ApiClient.getInstance();
        client.getApiService().updateFCM(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1) {
                            Log.e(TAG, "onResponse: FCM Updated");

                            //set logout from app
                            if (isLogout) setLogout();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t)
            {
                Log.e(TAG, "onError: updateFCM" + t.toString());
                //showErrorLog(t.toString());
            }
        });
    }


    private void getCheckTokenValidity()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().checkTokenValidity(api_token, user_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 0) {
                                call_updateFCM(true);
                            }
                        }
                    }
                }
                else {

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
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
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


    private void showErrorLog(final String message) {
        Objects.requireNonNull(context).runOnUiThread(() -> onErrorSnack(Objects.requireNonNull(context), message));
    }


    private  void setLogout(){

        if(sharedPreferences!=null)
        {
            //normal logout
            editor = sharedPreferences.edit();

            //remove donation shortcut
            new Helper().createShortCut(this,false);

            //clear the user data
            //clear the sharedPref data and save/commit
            editor.clear();
            editor.apply();
        }

        //clear/delete user payment data from app
        //Checkout.clearUserData(context);

        // [START Unsubscribe_topics]
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("sales");

        new Helper().showCustomToast(context, "Logout Successful!");

        Intent intent=new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //SessionManager.getInstance().logoutUser();
        startActivity(intent);
        //FINISH THE SCREEN AND GO TO MAIN ACTIVITY
        finish();
    }


    @Override
    public void onGetResponse(boolean isUpdateAvailable) {

        Log.e("Result_APP_Update", String.valueOf(isUpdateAvailable));
        if (isUpdateAvailable) {
            runOnUiThread(this::showUpdateDialog);
        }
    }

    /**
     * Method to show update dialog
     */
    private void showUpdateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(context), R.style.material_AlertDialogTheme);

        alertDialogBuilder.setTitle(context.getString(R.string.app_name));
        alertDialogBuilder.setMessage("App Update Available");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Update Now", (dialog, id) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            dialog.cancel();
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (isForceUpdate) {
                finish();
            }
            dialog.dismiss();
        });
        alertDialogBuilder.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

    @Override
    protected void onDestroy() {
        unregisterInstallStateUpdListener();
        super.onDestroy();
    }

}
