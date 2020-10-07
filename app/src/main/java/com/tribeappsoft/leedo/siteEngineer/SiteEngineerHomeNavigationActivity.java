package com.tribeappsoft.leedo.siteEngineer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.profile.ViewProfileActivity;
import com.tribeappsoft.leedo.siteEngineer.homeFragments.FragmentSiteEngineerAllVisits;
import com.tribeappsoft.leedo.siteEngineer.homeFragments.FragmentSiteEngineerHome;
import com.tribeappsoft.leedo.util.CustomTypefaceSpan;
import com.tribeappsoft.leedo.util.Helper;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.isValidContextForGlide;

public class SiteEngineerHomeNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private GoogleApiClient mGoogleApiClient;

    LinearLayoutCompat navHeader_user_profile_ll;
    AppCompatTextView tv_navHeader_userName, tv_navHeader_userEmail;
    ImageView iv_navHeader_userPic;
    private String TAG = "SiteEngineerHomeNavigationActivity";
    private static final String TODO = null;
    private Activity context;
    private static final int Permission_CODE_RPS = 321;
    private boolean isLogout= false;

    @BindView(R.id.tv_siteEngineerHome_title) AppCompatTextView tv_home_title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_engineer_home_navigation);
        context = SiteEngineerHomeNavigationActivity.this;
        ButterKnife.bind(this);


        Toolbar toolbar = findViewById(R.id.toolbar_siteEngineer);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_siteEngineer);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout_siteEngineer);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = findViewById(R.id.nav_view_siteEngineer);
        navigationView.setNavigationItemSelectedListener(this);
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        View header = navigationView.getHeaderView(0);
        navHeader_user_profile_ll = header.findViewById(R.id.ll_navHeader_siteEngineer);
        iv_navHeader_userPic = header.findViewById(R.id.iv_siteEngineerNavHeader_userPic);
        tv_navHeader_userName =  header.findViewById(R.id.tv_siteEngineerNavHeader_userName);
        tv_navHeader_userEmail = header.findViewById(R.id.tv_siteEngineerNavHeader_userEmail);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        //set school name as a title
        tv_home_title.setText(getString(R.string.menu_home));



        navHeader_user_profile_ll.setOnClickListener(v -> {
            startActivity(new Intent(context, ViewProfileActivity.class));

            DrawerLayout drawer1 = findViewById(R.id.drawer_layout_siteEngineer);
            drawer1.closeDrawer(GravityCompat.START);
        });

        /*if(isEvent_Registration || notifyEvents)
        {
//            Bundle bundle = new Bundle();
//            bundle.putInt("enquiry_id", enquiry_id);
            Fragment defaultFragment = new Frag_StudentEventRegistrationList();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.trans_right_in,R.anim.trans_right_out,R.anim.trans_left_in, R.anim.trans_left_out);
            //defaultFragment.setArguments(bundle);
            ft.replace(R.id.content_salesPerson_home, defaultFragment);
            ft.commit();
            tv_home_title.setText(getString(R.string.event_registrations));
        }
        else*/
        {
            //call the method to set Def_Frag
            setDefaultFrag(savedInstanceState);
        }


    }

    protected void applyFontToMenuItem(MenuItem mi) {

        Typeface font = Typeface.createFromAsset(getAssets(), "metropolis_medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        //mNewTitle.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, mNewTitle.length(), 0); //Use this if you want to center the items
        mi.setTitle(mNewTitle);
    }


    private void setDefaultFrag(Bundle savedInstanceState)
    {
        //set the default fragment
        if (savedInstanceState == null)
        {
            Bundle bundle = new Bundle();
            //bundle.putInt("tabSelected", tabSelected);
            Fragment defaultFragment = new FragmentSiteEngineerHome();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            defaultFragment.setArguments(bundle);
            ft.replace(R.id.content_siteEngineer_home, defaultFragment);
            ft.commit();
            //set school name as a title
            tv_home_title.setText(getString(R.string.menu_home));

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


        //check and call update fcm token
        if (isNetworkAvailable(context))
        {
            //TODO Enable call get Token
            //getToken();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                //check Read Phone state permission
                if (checkPermission())
                {
                    getDeviceIMEI();

                    //call UpdateFCM api
                    new Thread(this::call_updateFCM).start();

                }
                else requestPermissionReadPhoneState();
            }
            else
            {
                getDeviceIMEI();

                //call UpdateFCM api
                new Thread(this::call_updateFCM).start();
            }

        } else NetworkError(context);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //if (isNetworkAvailable(this)) //call verify user new Thread(this::call_getVerifyUser).start();

        //update UserProfile
        updateUser();
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
            if (sharedPreferences.getString("firstName", null) != null) firstName = sharedPreferences.getString("firstName", null);
            //lastName
            if (sharedPreferences.getString("lastName", null) != null) lastName = sharedPreferences.getString("lastName", null);
            //mobile1
            if (sharedPreferences.getString("email", null) != null) user_email = sharedPreferences.getString("email", null);
            //photoPath
            if (sharedPreferences.getString("profile_path", null) != null) profile_photo = sharedPreferences.getString("profile_path", null);
        }

        String user_name = lastName!=null ? firstName + " "+ lastName : firstName;

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
                        .apply(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.ic_profile_icon_nav_d)))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .apply(new RequestOptions().error(R.drawable.ic_profile_icon_nav_d))
                        //.skipMemoryCache(true)
                        .into(iv_navHeader_userPic);
            }
        }

    }

    private void call_updateFCM()
    {
        int student_id =0, student_type =0;
        String fcmToken = "", api_token = "";
        if ( sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            if (sharedPreferences.getInt("student_id", 0) != 0) student_id = sharedPreferences.getInt("student_id", 0);
            if (sharedPreferences.getInt("student_type", 0) != 0) student_type = sharedPreferences.getInt("student_type", 0);
            if (sharedPreferences.getString("fcm_token", null) != null) fcmToken = sharedPreferences.getString("fcm_token", null);
            if (sharedPreferences.getString("api_token", null) != null) api_token = sharedPreferences.getString("api_token", "");
        }

        JsonObject jsonObject =new JsonObject();

        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("fcm_token_user_id", student_id);
        jsonObject.addProperty("user_type_id", student_type);
        jsonObject.addProperty("device_id", getDeviceIMEI()!=null ? getDeviceIMEI() : "");
        jsonObject.addProperty("device_type", "android");
        //for logout send blank fcm token
        jsonObject.addProperty("fcm_token", isLogout ? "" :  fcmToken);


        ApiClient client = ApiClient.getInstance();
        client.getApiService().updateStudentFCM(jsonObject).enqueue(new Callback<JsonObject>()
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

                        if (isSuccess==1)
                        {
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


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED;
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
                    new Thread(this::call_updateFCM).start();

                } else NetworkError(this);

            }

        }

    }
    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
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




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_site_engineer_home_navigation, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment selectedFragment = null;
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_siteEngineer_home:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                tv_home_title.setText(getString(R.string.menu_home));
                selectedFragment = new FragmentSiteEngineerHome();
                break;

            case R.id.nav_siteEngineer_allSiteVisits:
                //this.setTitle(getString(R.string.organization_details));
                tv_home_title.setText(getString(R.string.all_site_visits));
                selectedFragment = new FragmentSiteEngineerAllVisits();
                break;

            case R.id.nav_siteEngineer_logout:
                //set Logout Alert
                showLogoutAlertDialog();
                break;

        }

        if (selectedFragment != null) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout_siteEngineer);
            drawer.closeDrawer(GravityCompat.START);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.trans_right_in,R.anim.trans_right_out,R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.replace(R.id.content_siteEngineer_home, selectedFragment);
            transaction.commitAllowingStateLoss();
            //transaction.commit();
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_siteEngineer);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    private void showLogoutAlertDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.material_AlertDialogTheme);
        builder.setTitle("Logout!");
        builder.setMessage(getString(R.string.do_you_want_to_logout));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.logout), (dialog, which) ->{

            //call UpdateFCM api
            isLogout = true;
            new Thread(this::call_updateFCM).start();
            new Handler().postDelayed(this::logout, 1000);

        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();

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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_siteEngineer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
