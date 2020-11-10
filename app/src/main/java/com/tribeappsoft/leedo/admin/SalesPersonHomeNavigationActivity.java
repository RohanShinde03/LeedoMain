package com.tribeappsoft.leedo.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.Home_Fragment.FragmentHome;
import com.tribeappsoft.leedo.admin.booked_customers.BookedCustomersActivity;
import com.tribeappsoft.leedo.admin.callLog.DeviceAdminDemoReceiver;
import com.tribeappsoft.leedo.admin.callSchedule.CallScheduleMainActivity;
import com.tribeappsoft.leedo.admin.leadreassign.LeadReAssign_Activity;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.leads.AllLeadsActivity;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.admin.notifications.NotificationsActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.AddNewOfflineLeadActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.AllOfflineLeads_Activity;
import com.tribeappsoft.leedo.admin.project_brochures.ProjectBrochuresActivity;
import com.tribeappsoft.leedo.admin.project_creation.AllProjectActivity;
import com.tribeappsoft.leedo.admin.project_floor_plans.ProjectFloorPlanActivity;
import com.tribeappsoft.leedo.admin.project_quotations.ProjectQuotationActivity;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.admin.reminder.AllReminderActivity;
import com.tribeappsoft.leedo.admin.reports.LeadReportsActivity;
import com.tribeappsoft.leedo.admin.site_visits.AllSiteVisitsActivity;
import com.tribeappsoft.leedo.admin.user_profile.UserProfileActivity;
import com.tribeappsoft.leedo.admin.users.AllUsersActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.appUpdate.GooglePlayStoreAppVersionNameLoader;
import com.tribeappsoft.leedo.appUpdate.WSCallerVersionListener;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.CircularAnim;
import com.tribeappsoft.leedo.util.CustomTypefaceSpan;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.isValidContextForGlide;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class SalesPersonHomeNavigationActivity extends AppCompatActivity implements WSCallerVersionListener{

    @BindView(R.id.app_bar_salesPerson) AppBarLayout app_bar;
    LinearLayoutCompat navHeader_user_profile_ll;
    @BindView(R.id.nav_view_salesPerson) NavigationView navigationView;
    @BindView(R.id.view_salesPerson_disableLayout) View viewDisableLayout;
    @BindView(R.id.drawer_layout_salesPerson) DrawerLayout mDrawerLayout;
    @BindView(R.id.fab_salesPerson_add) FloatingActionButton fab_add;
    @BindView(R.id.tv_salesPerson_titleAddLead) AppCompatTextView tv_titleAddLead;
    @BindView(R.id.fab_salesPerson_addSiteVisit) FloatingActionButton fab_addSiteVisit;
    @BindView(R.id.tv_salesPerson_titleAddSiteVisit) AppCompatTextView tv_titleAddSiteVisit;
    @BindView(R.id.fab_salesPerson_addCallLog) FloatingActionButton fab_addCallLog;
    @BindView(R.id.tv_salesPerson_titleCallLog) AppCompatTextView tv_titleCallLog;
    @BindView(R.id.fab_salesPerson_addReminder) FloatingActionButton fab_addReminder;
    @BindView(R.id.tv_salesPerson_titleAddReminder) AppCompatTextView tv_titleAddReminder;
    @BindView(R.id.fab_salesPerson_directBooking) FloatingActionButton fab_directBooking;
    @BindView(R.id.tv_salesPerson_titleDirectBooking) AppCompatTextView tv_titleDirectBooking;
    AppCompatTextView tv_navHeader_userName, tv_navHeader_userEmail;
    ImageView iv_navHeader_userPic;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "SalesPersonHomeNavigationActivity";
    private static final String TODO = null;
    private Activity context;
    //private static final int Permission_CODE_RPS = 321;
    private boolean isLogout= false, isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward, rotate_backward;
    private int leadClicked = 0,user_id =0; //tabSelected = 0,

    // @BindView(R.id.tv_salesPersonHome_title) AppCompatTextView tv_home_title;
    private boolean isForceUpdate = true,isSalesHead=false, isAdmin= false;
    private String api_token="",android_id ="", lead_sync_time="";
    private static final int Permission_CODE_DeviceAdmin = 5912;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    //private int requestCode;
    //private String[] permissions;
    //private int[] grantResults;
    //private AppUpdateManager appUpdateManager;
    //private InstallStateUpdatedListener installStateUpdatedListener;
    //private NetworkStateReceiver networkStateReceiver;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_person_home_navigation);
        context = SalesPersonHomeNavigationActivity.this;
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar_salesPerson);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setElevation(16);

        //set status bar color
        //change_status_bar_color();

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.app_name));
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }


        //set elevation to app bar layout
        app_bar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // If we have some pinned children, and we're offset to only show those views,
                // we want to be elevate
                ViewCompat.setElevation(app_bar, app_bar.getTargetElevation());
            } else {
                // Otherwise, we're inline with the content
                ViewCompat.setElevation(app_bar, 16f);
            }
        });


        //set Animation to Fab
        fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_backward);

        FloatingActionButton fab = findViewById(R.id.fab_salesPerson_add);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        DrawerLayout drawer = findViewById(R.id.drawer_layout_salesPerson);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = findViewById(R.id.nav_view_salesPerson);
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);

        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        View header = navigationView.getHeaderView(0);
        navHeader_user_profile_ll = header.findViewById(R.id.ll_navHeader_salesPerson);
        iv_navHeader_userPic = header.findViewById(R.id.iv_salesPersonNavHeader_userPic);
        tv_navHeader_userName =  header.findViewById(R.id.tv_salesPersonNavHeader_userName);
        tv_navHeader_userEmail = header.findViewById(R.id.tv_salesPersonNavHeader_userEmail);

        navHeader_user_profile_ll.setOnClickListener(v -> {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(SalesPersonHomeNavigationActivity.this, UserProfileActivity.class));
        });

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //get android id
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(TAG, "onCreate: android id "+android_id );
        Log.e(TAG, "onCreate: isSalesHead "+isSalesHead );

        /*if (applicationCreated) {
            //check for the device admin permission allowed or not
            checkForDeviceAdminPermission();
        }*/

        //set school name as a title
        // tv_home_title.setText(getString(R.string.app_name));

        //if(getIntent()!=null){
        //isEvent_Registration=getIntent().getBooleanExtra("isEvent_Registration",false);
        //notifyEvents = getIntent().getBooleanExtra("notifyEvents",false);
        //tabSelected=getIntent().getIntExtra("tabSelected",0);
        //}


      /*  navHeader_user_profile_ll.setOnClickListener(v -> {
            startActivity(new Intent(context, ViewProfileActivity.class));
            DrawerLayout drawer1 = findViewById(R.id.drawer_layout_salesPerson);
            drawer1.closeDrawer(GravityCompat.START);
        });*/

        //call the method to set Def_Frag
        setDefaultFrag(savedInstanceState);

        //set def home menu item checked
        navigationView.getMenu().getItem(0).setChecked(true);


        // add Lead main
        fab_add.setOnClickListener(view -> {
            Log.e(TAG, "onCreate:  fab_add.setOnClickListener");
            if (isFabOpen) {

                new Handler(getMainLooper()).postDelayed(() -> {

                    if (leadClicked!=1)
                    {
                        if (isNetworkAvailable(Objects.requireNonNull(context)))
                        {
                            CircularAnim.fullActivity(context, view)
                                    .colorOrImageRes(R.color.colorPrimary)
                                    .go(() -> startActivityForResult(new Intent(context, AddNewLeadActivity.class).putExtra("fromHomeScreen_AddLead",true), 111));
                        }
                        else {

                            CircularAnim.fullActivity(context, view)
                                    .colorOrImageRes(R.color.primary_gray)
                                    .go(() -> startActivityForResult(new Intent(context, AddNewOfflineLeadActivity.class), 111));

                        }
                        new Handler().postDelayed(() -> {
                            animateFAB();
                            leadClicked = 1;
                        }, 300);
                        // new Handler().postDelayed(this::animateFAB, 10);
                        //startActivityForResult(new Intent(context, AddNewLeadActivity.class), 111);
                    }
                }, 200);


            }
            else {
                animateFAB();
            }
        });

        tv_titleAddLead.setOnClickListener(view -> {
            if (isFabOpen) {

                new Handler(getMainLooper()).postDelayed(() -> {
                    if (leadClicked!=1)
                    {
                        if (isNetworkAvailable(Objects.requireNonNull(context))){
                            CircularAnim.fullActivity(context, view)
                                    .colorOrImageRes(R.color.colorPrimary)
                                    .go(() -> startActivityForResult(new Intent(context, AddNewLeadActivity.class).putExtra("fromHomeScreen_AddLead",true), 111));
                        }
                        else {
                            CircularAnim.fullActivity(context, view)
                                    .colorOrImageRes(R.color.primary_gray)
                                    .go(() -> startActivityForResult(new Intent(context, AddNewOfflineLeadActivity.class), 111));
                        }

                        new Handler().postDelayed(() -> {
                            animateFAB();
                            leadClicked = 1;
                        }, 300);
                    }
                }, 200);




            } else {
                animateFAB();
            }

        });

        // add Lead main
       /* fab_add.setOnClickListener(view -> {
            Log.e(TAG, "onCreate:  fab_add.setOnClickListener");
            if (isFabOpen) {

                if (isNetworkAvailable(Objects.requireNonNull(context)))
                {
                    new Handler(getMainLooper()).postDelayed(() -> {

                        if (leadClicked!=1)
                        {
                            CircularAnim.fullActivity(context, view)
                                    .colorOrImageRes(R.color.secondaryColor)
                                    .go(() -> startActivityForResult(new Intent(context, AddNewLeadActivity.class),111));
                            new Handler().postDelayed(() -> {
                                animateFAB();
                                leadClicked = 1;
                            }, 300);
                            // new Handler().postDelayed(this::animateFAB, 10);
                            //startActivityForResult(new Intent(context, AddNewLeadActivity.class), 111);
                        }
                    }, 200);
                }else startActivity(new Intent(context, AddNewOfflineLeadActivity.class));

            }
            else {
                animateFAB();
            }
        });

        tv_titleAddLead.setOnClickListener(view -> {
            if (isFabOpen) {

                new Handler(getMainLooper()).postDelayed(() -> {
                    if (leadClicked!=1)
                    {
                        CircularAnim.fullActivity(context, view)
                                .colorOrImageRes(R.color.secondaryColor)
                                .go(() -> startActivityForResult(new Intent(context, AddNewLeadActivity.class), 111));

                        new Handler().postDelayed(() -> {
                            animateFAB();
                            leadClicked = 1;
                        }, 300);
                    }
                }, 200);

            } else {
                animateFAB();
            }

        });*/


        //site visit
        fab_addSiteVisit.setOnClickListener(view ->{

            animateFAB();
            new Handler(getMainLooper()).postDelayed(() -> CircularAnim.fullActivity(context, view)
                    .colorOrImageRes(R.color.secondaryLightColor)
                    .go(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                            .putExtra("fromSiteVisit_or_token", 1)
                            .putExtra("fromHomeScreen_AddSiteVisit",true)
                            .putExtra("forId", 1))), 200);
        });

        tv_titleAddSiteVisit.setOnClickListener(view -> {

            animateFAB();
            new Handler(getMainLooper()).postDelayed(() -> CircularAnim.fullActivity(context, view)
                    .colorOrImageRes(R.color.secondaryLightColor)
                    .go(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                            .putExtra("fromSiteVisit_or_token", 1)
                            .putExtra("fromHomeScreen_AddSiteVisit",true)
                            .putExtra("forId", 1))), 200);
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 112);
        });


        //add call log

        fab_addCallLog.setOnClickListener(view ->{
            animateFAB();
            new Handler().postDelayed(() -> {

                CircularAnim.fullActivity(context, view)
                        .colorOrImageRes(R.color.secondaryLightColor)
                        .go(() -> startActivityForResult(new Intent(context, CustomerIdActivity.class)
                                .putExtra("fromSiteVisit_or_token", 5), 114));
               /*
                startActivityForResult(new Intent(context, CustomerIdActivity.class).
                        putExtra("fromSiteVisit_or_token", 5), 114);*/
            }, 200);
        });
        tv_titleCallLog.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> CircularAnim.fullActivity(context, view)
                    .colorOrImageRes(R.color.secondaryLightColor)
                    .go(() -> startActivityForResult(new Intent(context, CustomerIdActivity.class)
                            .putExtra("fromSiteVisit_or_token", 5), 114)), 200);

        });

/*
        tv_titleCallLog.setOnClickListener(view -> {
            animateFAB();
           *//* new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 2)
                    //.putExtra("forId", 2) TODO for update S.E can generate GHP WO site visit so forID = 3
                    .putExtra("forId", 3)
            ), 200);*//*
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 113);

        });*/

        //direct booking
        fab_directBooking.setOnClickListener(view ->{
            animateFAB();
            new Handler().postDelayed(() -> CircularAnim.fullActivity(context, view)
                    .colorOrImageRes(R.color.secondaryLightColor)
                    .go(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                            .putExtra("fromHomeScreen_AddBooking",true)
                            .putExtra("fromSiteVisit_or_token", 3)
                            .putExtra("forId", 3))), 200);

        });
        tv_titleDirectBooking.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> CircularAnim.fullActivity(context, view)
                    .colorOrImageRes(R.color.secondaryLightColor)
                    .go(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                            .putExtra("fromHomeScreen_AddBooking",true)
                            .putExtra("fromSiteVisit_or_token", 3)
                            .putExtra("forId", 3))), 200);
            //startActivityForResult(new Intent(context, CustomerIdActivity.class), 113);

        });



        //add reminder
        fab_addReminder.setOnClickListener(view ->{
            animateFAB();
            new Handler().postDelayed(() -> {
                CircularAnim.fullActivity(context, view)
                        .colorOrImageRes(R.color.secondaryLightColor)
                        .go(() -> startActivityForResult(new Intent(context, AddReminderActivity.class)
                                .putExtra("fromHomeScreen_AddReminder",true)
                                .putExtra("fromOther", 1), 114));

                //  startActivityForResult(new Intent(context, AddReminderActivity.class).putExtra("fromOther", 1), 114);
            }, 200);
            //new Helper().openReminderIntent(context, "Reminder test title", "Reminder test description", "2020-03-07 10:09:50", "2020-03-08 16:10:50");
        });
        tv_titleAddReminder.setOnClickListener(view -> {
            animateFAB();
            new Handler().postDelayed(() -> {
                CircularAnim.fullActivity(context, view)
                        .colorOrImageRes(R.color.secondaryLightColor)
                        .go(() -> startActivityForResult(new Intent(context, AddReminderActivity.class)
                                .putExtra("fromHomeScreen_AddReminder",true)
                                .putExtra("fromOther", 1), 114));

                // startActivityForResult(new Intent(context, AddReminderActivity.class).putExtra("fromOther", 1), 114);
            }, 200);
        });

        //fab_add.setOnClickListener(view -> animateFAB());
        viewDisableLayout.setOnClickListener(v -> animateFAB());


        if (isNetworkAvailable(context))//check for app update
            new GooglePlayStoreAppVersionNameLoader(getApplicationContext(), SalesPersonHomeNavigationActivity.this).execute();

        //register receiver here
        //networkStateReceiver = new NetworkStateReceiver();
        //networkStateReceiver.addListener(this);
        //this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }


    protected void applyFontToMenuItem(MenuItem mi) {

        Typeface font = Typeface.createFromAsset(getAssets(), "metropolis_medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        //mNewTitle.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, mNewTitle.length(), 0); //Use this if you want to center the items
        mi.setTitle(mNewTitle);
    }


    private void setDefaultFrag(Bundle savedInstanceState) {
        //set the default fragment
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            //   bundle.putInt("tabSelected", tabSelected);
            Fragment defaultFragment = new FragmentHome();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            defaultFragment.setArguments(bundle);
            ft.replace(R.id.content_salesPerson_home, defaultFragment);
            ft.commit();
            //set school name as a title
            //tv_home_title.setText(getString(R.string.app_name));
        }
    }


    @Override
    protected void onStart()
    {
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();


        if (isNetworkAvailable(this)) {
            //call get Token
            getToken();
        } else NetworkError(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: ");

        //if (isNetworkAvailable(this)) //call verify user new Thread(this::call_getVerifyUser).start();
        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            new Handler().postDelayed(this::getLastOfflineSyncedTime,100);
            new Handler().postDelayed(this::getLeadData,100);
        }

        //update UserProfile
        updateUser();

        //set offline leads badge count
        setSideNavBadge();

        //check offline leads available for sync
        //setOfflineLeads();

        //if (isNetworkAvailable(Objects.requireNonNull(context))) {
        // getCheckTokenValidity();

        //check new version state
        //checkNewAppVersionState();
        //}else NetworkError(context);

    }

    public void setSideNavBadge()
    {
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_salesPerson_offlineLeads);
        MenuItemCompat.setActionView(item, R.layout.badge_layout_offline_leads);
        RelativeLayout relativeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);
        MaterialTextView tv =  relativeLayout.findViewById(R.id.mTv_badgeOfflineLeadsCount);

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.apply();
        int total_duplicate_leads = sharedPreferences.getInt("total_duplicate_leads", 0);

        if (total_duplicate_leads > 0) {
            tv.setText(String.valueOf(total_duplicate_leads));
        }else{
            tv.setText("");
            //item.setEnabled(false);
        }
    }

    private void getLastOfflineSyncedTime()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getLastOfflineLeadSyncTime(api_token, user_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;

                            if (isSuccess == 1) {
                                if (response.body().has("data"))
                                {
                                    if (response.body().has("data")) lead_sync_time = !response.body().get("data").isJsonNull() ? response.body().get("data").getAsString() :"not synced yet";
                                }
                                //set delayRefresh
                                new Handler().postDelayed(() -> delayRefresh(), 100);
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                Log.e(TAG, "onError: App API USER " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void delayRefresh()
    {
        if (sharedPreferences != null) {
            editor = sharedPreferences.edit();
            editor.putString("lead_sync_time", lead_sync_time);
            editor.apply();
        }
    }




    private void getLeadData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadForm_Data(api_token,user_id);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonObjectResponse -> {
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
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        //  setLeadDetails();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject()) {
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                setJson(jsonObject);
                                            }
                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
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

    private void setJson(JsonObject jsonObject)
    {

        if (jsonObject.has("namePrefix"))
        {
            if (!jsonObject.get("namePrefix").isJsonNull() && jsonObject.get("namePrefix").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("namePrefix", jsonObject.get("namePrefix").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("income_range_types"))
        {
            if (!jsonObject.get("income_range_types").isJsonNull() && jsonObject.get("income_range_types").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("income_range_types", jsonObject.get("income_range_types").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("budget_limit_types"))
        {
            if (!jsonObject.get("budget_limit_types").isJsonNull() && jsonObject.get("budget_limit_types").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("budget_limit_types", jsonObject.get("budget_limit_types").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("unit_categories"))
        {
            if (!jsonObject.get("unit_categories").isJsonNull() && jsonObject.get("unit_categories").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("unit_categories", jsonObject.get("unit_categories").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("lead_stages"))
        {
            if (!jsonObject.get("lead_stages").isJsonNull() && jsonObject.get("lead_stages").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("lead_stages", jsonObject.get("lead_stages").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("lead_types"))
        {
            if (!jsonObject.get("lead_types").isJsonNull() && jsonObject.get("lead_types").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("lead_types", jsonObject.get("lead_types").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }

        if (jsonObject.has("professions"))
        {
            if (!jsonObject.get("professions").isJsonNull() && jsonObject.get("professions").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("professions", jsonObject.get("professions").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }


        if (jsonObject.has("ref_projects"))
        {
            if (!jsonObject.get("ref_projects").isJsonNull() && jsonObject.get("ref_projects").isJsonArray()) {
                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("ref_projects", jsonObject.get("ref_projects").getAsJsonArray().toString());
                    editor.apply();
                }
            }
        }
    }

    private void setOfflineLeads()
    {
        Log.e(TAG, "setOfflineLeads: ");
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            String offlineData = null;
            if (sharedPreferences.getString("DownloadModel", null)!=null) offlineData = sharedPreferences.getString("DownloadModel", null);


            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                if (offlineData !=null)
                {
                    final JsonObject jsonObject = new JsonObject();
                    Gson gson  = new Gson();
                    JsonArray jsonArray = gson.fromJson(offlineData, JsonArray.class);
                    jsonObject.addProperty("api_token",api_token);
                    jsonObject.add("offline_leads",jsonArray);

                    //showProgressBar(getString(R.string.syncing_oldEnquiry));
                    new Handler().postDelayed(() -> {
                        new Helper().onSnackForHomeLeadSync(context,"New offline leads detected! Syncing now...");
                        call_SyncOfflineLeads(jsonObject);
                    },4000); }

            }
            //else NetworkError(getActivity());
        }
    }


    private void call_SyncOfflineLeads(JsonObject jsonObject)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().add_OfflineLeads(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0, total_duplicate_leads =0;
                        String status_msg = null;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (response.body().has("duplicate_leads")) total_duplicate_leads = response.body().get("duplicate_leads").getAsInt();
                        if (response.body().has("status_msg")) status_msg = response.body().get("status_msg").getAsString();

                        if (isSuccess==1)
                        {
                            // clear shared pref of offline leads
                            if (sharedPreferences!=null)
                            {
                                editor = sharedPreferences.edit();
                                editor.putInt("total_duplicate_leads", total_duplicate_leads);
                                editor.remove("DownloadModel");
                                editor.apply();
                            }

                            onSuccessSync(status_msg);

                        }
                        else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                            showErrorLog(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void onSuccessSync(String status_msg)
    {
        if (context!=null) {

            runOnUiThread(() -> {

                //setOfflineLeads();
                //hideProgressBar();

                //show success toast
                new Handler().postDelayed(() -> {

                    Toast.makeText(context, status_msg != null ? status_msg : getString(R.string.offline_lead_synced_successfully), Toast.LENGTH_LONG).show();
                    //new Helper().showSuccessCustomToast(getActivity(), status_msg != null ? status_msg : getString(R.string.offline_lead_synced_successfully));
                },2000);
            });
        }
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
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

   /* private void getToken()
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

                    sharedPreferences = new Helper().getSharedPref(context);
                    editor = sharedPreferences.edit();
                    editor.putString("fcm_token", token );
                    editor.apply();
                    Log.e("fcm_tokenStored_1", sharedPreferences.getString("fcm_token", null)!=null ? sharedPreferences.getString("fcm_token", "") : "__ empty" );

                });
    }*/


    private void updateUser()
    {

        String firstName = null,lastName = null, profile_photo = null, user_email= null;
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            //firstName
            if (sharedPreferences.getString("first_name", null) != null) firstName = sharedPreferences.getString("first_name", null);
            //lastName
            if (sharedPreferences.getString("last_name", null) != null) lastName = sharedPreferences.getString("last_name", null);
            //mobile1
            if (sharedPreferences.getString("email", null) != null) user_email = sharedPreferences.getString("email", null);
            //photoPath
            if (sharedPreferences.getString("profile_photo", null) != null) profile_photo = sharedPreferences.getString("profile_photo", null);
        }

        String user_name = lastName!=null ? firstName + " "+ lastName : firstName;

        Log.e(TAG, "updateUser:user_name "+ firstName );
        Log.e(TAG, "updateUser:user_name "+ lastName );
        Log.e(TAG, "updateUser:user_name "+ user_email );
        tv_navHeader_userName.setText( user_name!=null ? user_name : getString(R.string.user_name));
        //tv_userRole.setText( designation!=null ? designation : getString(R.string.user_role));
        tv_navHeader_userEmail.setText( user_email!=null ? user_email : getString(R.string.user_email));


        if (profile_photo!=null)
        {
            final Context context = getApplicationContext();
            if (isValidContextForGlide(context))
            {
                Glide.with(getApplicationContext())
                        .load(profile_photo)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().centerCrop())
                        .apply(new RequestOptions().placeholder(context.getResources().getDrawable(R.drawable.ic_profile_default_user_icon)))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .apply(new RequestOptions().error(R.drawable.ic_profile_default_user_icon))
                        //.skipMemoryCache(true)
                        .into(iv_navHeader_userPic);
            }
        }


        navigationView = findViewById(R.id.nav_view_salesPerson);
        Menu m = navigationView.getMenu();

        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
        MenuItem item_nav_projectList = m.findItem(R.id.nav_salesPerson_project_list);
        MenuItem item_nav_userList = m.findItem(R.id.nav_salesPerson_users);
        MenuItem item_nav_lead_reassign = m.findItem(R.id.nav_salesPerson_leadReassign);
        MenuItem item_nav_lead_Reports = m.findItem(R.id.nav_salesPerson_Reports);


        item_nav_projectList.setVisible(isSalesHead || isAdmin);
        item_nav_userList.setVisible(isSalesHead || isAdmin);
        item_nav_lead_reassign.setVisible(isSalesHead || isAdmin);
        item_nav_lead_Reports.setVisible(isSalesHead || isAdmin);

    }
    /**
     * Needed only for FLEXIBLE update
     */
//    private void unregisterInstallStateUpdListener() {
//        if (appUpdateManager != null && installStateUpdatedListener != null) appUpdateManager.unregisterListener(installStateUpdatedListener);
//    }


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
                            if (isLogout)
                            {
                                new Helper().onSnackForHome(context,"Please wait...Logging out!");
                                setLogout();
                            }
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void showErrorLog(final String message) {
        Objects.requireNonNull(context).runOnUiThread(() -> onErrorSnack(Objects.requireNonNull(context), message));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111  && resultCode  == RESULT_OK) {
            /// From Add Lead form
            Objects.requireNonNull(data).getStringExtra("result");
           /* viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);*/
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result"));
            leadClicked = 0;
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName());
        }
        else if ( requestCode == 112  && resultCode  == RESULT_OK) {
            // From Site Visit
            Objects.requireNonNull(data).getStringExtra("result");
            /*viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);
*/
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if ( requestCode == 113  && resultCode  == RESULT_OK) {
            // From Tokens
            Objects.requireNonNull(data).getStringExtra("result");
         /*   viewFragment(new FragmentSalesPersonHomeFeeds(), "HomeFragment", R.id.bNav_salesPerson_home);
            setActionBarTitle(getString(R.string.feeds));
            navView.setSelectedItemId(R.id.bNav_salesPerson_home);
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );*/
            //ccp.setCountryForNameCode(country.getName());
            //new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if (requestCode == 114  && resultCode  == RESULT_OK) {
            //From Reminders
            Objects.requireNonNull(data).getStringExtra("result");
         /*   viewFragment(new FragmentSalesPersonReminders(), "FRAGMENT_OTHER", R.id.bNav_salesPerson_reminders);
            setActionBarTitle(getString(R.string.reminder));
            navView.setSelectedItemId(R.id.bNav_salesPerson_reminders);*/
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
            //unregisterInstallStateUpdListener();
        }

    }

    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
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
    private void animateFAB()
    {

        if(isFabOpen)
        {

            //fab_share.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_white));
            //fab_share.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close_white));
//            fab_share.startAnimation(rotate_backward);

            fab_add.startAnimation(rotate_backward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add_lead));

            //close view
            closeView();

            fab_addSiteVisit.startAnimation(fab_close);
            fab_addSiteVisit.setClickable(false);
            fab_addCallLog.startAnimation(fab_close);
            fab_addCallLog.setClickable(false);
            fab_addReminder.startAnimation(fab_close);
            fab_addReminder.setClickable(false);
            fab_directBooking.startAnimation(fab_close);
            fab_directBooking.setClickable(false);

            /*fab_ni.startAnimation(fab_close);
            fab_cold.startAnimation(fab_close);
            fab_hot.setClickable(false);
            fab_ni.setClickable(false);
            fab_cold.setClickable(false);*/

            new Animations().slideOutBottom(tv_titleCallLog);
            new Animations().slideOutBottom(tv_titleAddSiteVisit);
            new Animations().slideOutBottom(tv_titleAddLead);
            new Animations().slideOutBottom(tv_titleAddReminder);
            new Animations().slideOutBottom(tv_titleDirectBooking);

            //hide textView
            tv_titleAddLead.setVisibility(View.GONE);
            tv_titleAddSiteVisit.setVisibility(View.GONE);
            tv_titleCallLog.setVisibility(View.GONE);
            tv_titleAddReminder.setVisibility(View.GONE);
            tv_titleDirectBooking.setVisibility(View.GONE);

            // viewDisableLayout.setVisibility(View.GONE);
            isFabOpen = false;


        }
        else
        {

            fab_add.startAnimation(rotate_forward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_person_add_rotated_24px));

            //open view
            openView();

            fab_addSiteVisit.startAnimation(fab_open);
            fab_addSiteVisit.setClickable(true);
            fab_addCallLog.startAnimation(fab_open);
            fab_addCallLog.setClickable(true);
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
            tv_titleCallLog.setVisibility(View.VISIBLE);
            tv_titleAddReminder.setVisibility(View.VISIBLE);
            tv_titleDirectBooking.setVisibility(View.VISIBLE);

            new Animations().slideInBottomFab(tv_titleCallLog);
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
        //set status bar color main
        new Helper().setStatusBarColor(context, R.color.main_light_grey);
    }

    private void closeView()
    {

        // Hide the Panel
        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.GONE);

        //set status bar color as it is primary
        new Helper().setStatusBarColor(context, R.color.primaryDarkColor);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.sales_person_home_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = item -> {
        // Handle navigation view item clicks here.

        Fragment selectedFragment = null;

        int id = item.getItemId();
        switch (id) {

            case R.id.nav_salesPerson_home:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                //tv_home_title.setText(getString(R.string.app_name));
                selectedFragment = new FragmentHome();
                break;

            case R.id.nav_salesPerson_project_list:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllProjectActivity.class);
                return true;

            case R.id.nav_salesPerson_users:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllUsersActivity.class);
                return true;
            case R.id.nav_salesPerson_Reports:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(LeadReportsActivity.class);
                return true;

            case R.id.nav_salesPerson_leads:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllLeadsActivity.class);
                return true;

            case R.id.nav_salesPerson_offlineLeads:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllOfflineLeads_Activity.class);
                return true;

            case R.id.nav_salesPerson_reminders:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllReminderActivity.class);
                return true;

            case R.id.nav_salesPerson_callSchedule:
                //this.setTitle(getString(R.string.menu_home));

                closeDrawerAndOpenActivity(CallScheduleMainActivity.class);
                return true;


            case R.id.nav_salesPerson_siteVisitList:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(AllSiteVisitsActivity.class);
                return true;

            case R.id.nav_salesPerson_bookedCustomers:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(BookedCustomersActivity.class);
                return true;

            case R.id.nav_salesPerson_leadReassign:
                closeDrawerAndOpenActivity(LeadReAssign_Activity.class);
                return true;

            case R.id.nav_salesPerson_projectBrochures:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(ProjectBrochuresActivity.class);
                return true;

            case R.id.nav_salesPerson_projectQuotations:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(ProjectQuotationActivity.class);
                return true;

            case R.id.nav_salesPerson_projectFloorPlans:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(ProjectFloorPlanActivity.class);
                return true;

            case R.id.nav_salesPerson_notifications:
                //this.setTitle(getString(R.string.menu_home));
                closeDrawerAndOpenActivity(NotificationsActivity.class);
                return true;

            case R.id.nav_salesPerson_logout:
                //set Logout Alert
                showLogoutAlert();
                break;
        }

        if (selectedFragment != null) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout_salesPerson);
            drawer.closeDrawer(GravityCompat.START);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.trans_right_in,R.anim.trans_right_out,R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.replace(R.id.content_salesPerson_home, selectedFragment);
            transaction.commitAllowingStateLoss();
            //transaction.commit();
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_salesPerson);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    };

    private void showLogoutAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog;
        alertDialog = alertDialogBuilder.create();
        AppCompatTextView tv_msg,tv_desc;
        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getString(R.string.logout_));
        tv_desc.setText(getString(R.string.do_you_want_to_logout));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.logout));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //set is logout true
            isLogout = true;

            if (isNetworkAvailable(context)) {
                call_updateFCM();
                new Helper().onSnackForHome(context,"Please wait...Logging out!");
                new Handler().postDelayed(this::logout, 2000);

                //finally logout from app
                // logoutUser();

            }else NetworkError(context);

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
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

    private void logoutUser() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id", user_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().logoutUser(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull() && response.body().isJsonObject()) {

                            int isSuccess = 0;
                            if (response.body().has("success")) {
                                isSuccess = response.body().get("success").getAsInt();
                            }
                            if (isSuccess == 0) {
                                if (response.body().has("msg")) showErrorLog1(!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : getString(R.string.something_went_wrong_try_again));
                                else showErrorLog1(getString(R.string.something_went_wrong_try_again));
                            } else if (isSuccess == 1) {
                                logout();
                            } else showErrorLog1(getString(R.string.something_went_wrong_try_again));
                        }
                    }
                } else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog1(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog1(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog1(getString(R.string.unknown_error_try_again) + " " + response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog1(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog1(getString(R.string.weak_connection));
                else showErrorLog1(e.toString());
            }
        });
    }

    private void showErrorLog1(final String message) {
        runOnUiThread(() -> onErrorSnack(context, message));
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

    private void logout()
    {

        if(sharedPreferences!=null)
        {
            //normal logout
            editor = sharedPreferences.edit();

            //remove donation shortcut
            new Helper().createShortCut(this,false);

            int socialType = 0;

            if (sharedPreferences.getInt("socialType", 0) != 0) socialType = sharedPreferences.getInt("socialType", 0);

            if (socialType==2)  //Gmail
            {
                //gmail logout

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                /*Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>()
                        {
                            @Override
                            public void onResult(@NonNull Status status)
                            {
                                // ...
                                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                            }
                        });*/

            }
            else if (socialType==3)     //fb
            {
                //fb logout
                LoginManager.getInstance().logOut();
            }


            //clear the user data
            //clear the sharedPref data and save/commit
            editor.clear();
            editor.apply();
        }

        //clear/delete user payment data from app
        Checkout.clearUserData(context);

        // [START Unsubscribe_topics]
//        FireBaseMessaging.getInstance().unsubscribeFromTopic("members");
        // [END Unsubscribe_topics]

        new Helper().showCustomToast(context, "Logout Successful!");

        Intent intent=new Intent(context, LoginActivity.class);
        //SessionManager.getInstance().logoutUser();
        startActivity(intent);
        //FINISH THE SCREEN AND GO TO MAIN ACTIVITY
        finish();
    }
    private void change_status_bar_color()
    {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(getResources().getColor(R.color.primaryDarkColor));
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_salesPerson);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onGetResponse(boolean isUpdateAvailable) {

        Log.e("Result_APP_Update", String.valueOf(isUpdateAvailable));
        if (isUpdateAvailable) { runOnUiThread(this::showUpdateDialog); }
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

    private void call_updateFCM()
    {
        JsonObject jsonObject =new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id", user_id);
        jsonObject.addProperty("device_id", "");
        jsonObject.addProperty("device_type", "android");
        //for logout send blank fcm token
        jsonObject.addProperty("fcm_token",  "");
        jsonObject.addProperty("device_id", isLogout ? android_id : android_id!=null ? android_id : "");
        jsonObject.addProperty("device_type", "Android");

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

    public void closeDrawerAndOpenActivity(Class aClass) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        new Handler().postDelayed(() -> startActivity(new Intent(context, aClass)), 700);

    }

    /*@Override
    public void networkAvailable() {
        Log.e(TAG, "I'm in, baby!");
        //new Helper().showCustomToast(context, "Network Available!");

        new Handler().postDelayed(() -> {
            //new Helper().onSnackForHomeNetworkAvailable(context,"Device Network Available!");

            //check offline leads available for sync
            setOfflineLeads();
        }, 1000);

    }

    @Override
    public void networkUnavailable() {
        Log.d(TAG, "I'm dancing with myself");
        //new Helper().showCustomToast(context, "Network Lost again!");

        new Handler().postDelayed(() -> new Helper().onSnackForHomeLeadSync(context,"Oops, Device Network Lost..."), 1000);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //networkStateReceiver.removeListener(this);
        //this.unregisterReceiver(networkStateReceiver);
    }

    //@Override
    public void onSuccessNetworkListener() {
        Log.e(TAG, "onSuccessNetworkListener: ");

        new Handler().postDelayed(() -> {
            new Helper().onSnackForHomeNetworkAvailable(context,"Device Network Available!");

            //check offline leads available for sync
            setOfflineLeads();
        }, 1000);
    }
}
