package com.tribeappsoft.leedo.salesPerson.homeFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.profile.ViewProfileActivity;
import com.tribeappsoft.leedo.util.CustomTypefaceSpan;
import com.tribeappsoft.leedo.util.Helper;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

/*
 * Created by ${ROHAN} on 20/8/19.
 */
public class BottomSheetFragment extends BottomSheetDialogFragment implements NavigationView.OnNavigationItemSelectedListener
{


    @BindView(R.id.iv_bottomSheetFrag_closeDialog) AppCompatImageView iv_closeDialog;
    @BindView(R.id.nav_view_bottomSheetFrag_salesPerson) NavigationView nav_view;
    @BindView(R.id.ll_bottomSheetFrag_userProf) LinearLayoutCompat ll_userProf;
    @BindView(R.id.iv_bottomSheetFrag_userPic) CircleImageView iv_userPic;
    @BindView(R.id.tv_bottomSheetFrag_userName) AppCompatTextView tv_userName;
    @BindView(R.id.tv_bottomSheetFrag_userEmail) AppCompatTextView tv_userEmail;

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id =0;
    private String TAG = "BottomSheetFragment", api_token = "";
    private boolean isSalesTeamLead = false, isSalesHead = false;

    public BottomSheetFragment() {

    }

    @Override
    public int getTheme() {
        //return super.getTheme();
        return R.style.AppBottomSheetDialogTheme;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        return new BottomSheetDialog(requireContext(), getTheme());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        //set def fragment
        //new SalesPersonBottomNavigationActivity().setDefaultFrag();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style)
    {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.layout_bottom_sheet_dialog_fragment, null);
        context = contentView.getContext();
        ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        //tv_title.setText(getString(R.string.app_name)); R.style.AppBottomSheetDialogTheme

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        user_id = sharedPreferences.getInt("user_id", 0);
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        isSalesTeamLead = sharedPreferences.getBoolean("isSalesTeamLead", false);
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();


        DisplayMetrics displayMetrics = Objects.requireNonNull(getActivity()).getResources().getDisplayMetrics();
        //int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int maxHeight = (int) (height*0.74); //custom height of bottom sheet

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null) {
            ((BottomSheetBehavior) behavior).setPeekHeight(maxHeight);  //changed default peek height of bottom sheet
        }

        if (behavior != null)
        {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
            {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState)
                {
                    String state = "";
                    switch (newState) {
                        case BottomSheetBehavior.STATE_DRAGGING: {
                            //imgBtnClose.setVisibility(View.INVISIBLE);
                            iv_closeDialog.setVisibility(View.GONE);
                            state = "DRAGGING";
                            break;
                        }
                        case BottomSheetBehavior.STATE_SETTLING: {
                            // imgBtnClose.setVisibility(View.INVISIBLE);
                            iv_closeDialog.setVisibility(View.GONE);
                            state = "SETTLING";
                            break;
                        }
                        case BottomSheetBehavior.STATE_EXPANDED: {
                            // imgBtnClose.setVisibility(View.VISIBLE);
                            iv_closeDialog.setVisibility(View.VISIBLE);
                            state = "EXPANDED";
                            break;
                        }
                        case BottomSheetBehavior.STATE_COLLAPSED: {
                            //imgBtnClose.setVisibility(View.INVISIBLE);
                            iv_closeDialog.setVisibility(View.GONE);
                            state = "COLLAPSED";
                            break;
                        }
                        case BottomSheetBehavior.STATE_HIDDEN: {
                            // imgBtnClose.setVisibility(View.INVISIBLE);
                            iv_closeDialog.setVisibility(View.GONE);
                            dismiss();
                            state = "HIDDEN";
                            break;
                        }
                        case BottomSheetBehavior.STATE_HALF_EXPANDED:
                            iv_closeDialog.setVisibility(View.GONE);
                            dismiss();
                            break;
                    }
                    Log.i("BottomSheetFrag", "onStateChanged: "+ state);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }


        //update user data
        updateUser();

        //close dialog
        iv_closeDialog.setOnClickListener(view -> dismiss());

        //open profile
        ll_userProf.setOnClickListener(view -> {
            dismiss();
            startActivity(new Intent(context, ViewProfileActivity.class));
        });

        NavigationView navigationView = contentView.findViewById(R.id.nav_view_bottomSheetFrag_salesPerson);
        navigationView.setNavigationItemSelectedListener(this);

        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        MenuItem item_nav_unClaimedLeads = m.findItem(R.id.nav_salesPerson_unClaimedLeads);
        MenuItem item_nav_claimedLeads = m.findItem(R.id.nav_salesPerson_claimedLeads);
        MenuItem item_nav_siteVisitList = m.findItem(R.id.nav_salesPerson_siteVisitList);
        MenuItem item_nav_ghpList = m.findItem(R.id.nav_salesPerson_ghpList);
        MenuItem item_nav_salesHeadDashboard = m.findItem(R.id.nav_salesPerson_salesHeadDashboard);
        MenuItem item_nav_openSaleBlocks = m.findItem(R.id.nav_salesPerson_openSaleBlocks);
        MenuItem item_nav_leadReassign = m.findItem(R.id.nav_salesPerson_leadReassign);
        MenuItem item_nav_teamLeadList = m.findItem(R.id.nav_salesPerson_teamLeadList);
        MenuItem item_nav_cpFOSReports = m.findItem(R.id.nav_salesPerson_cpFOSReports);
        MenuItem item_nav_teamLeadStats = m.findItem(R.id.nav_salesPerson_teamLeadStats);
        MenuItem item_nav_salesExecutiveList = m.findItem(R.id.nav_salesPerson_salesExecutiveList);
        MenuItem item_nav_siteVisitStat = m.findItem(R.id.nav_salesPerson_siteVisitStat);

        //visible sales head options
        item_nav_unClaimedLeads.setVisible(isSalesHead);
        item_nav_claimedLeads.setVisible(isSalesHead);
        item_nav_siteVisitList.setVisible(isSalesHead);
        item_nav_ghpList.setVisible(isSalesHead);
        item_nav_salesHeadDashboard.setVisible(isSalesHead);
        item_nav_openSaleBlocks.setVisible(isSalesHead);
        item_nav_leadReassign.setVisible(isSalesHead);
        item_nav_teamLeadList.setVisible(isSalesHead);
        item_nav_cpFOSReports.setVisible(isSalesHead);
        item_nav_teamLeadStats.setVisible(isSalesHead);
        item_nav_salesExecutiveList.setVisible(isSalesHead);
        item_nav_siteVisitStat.setVisible(isSalesHead);

        MenuItem item_nav_teamStats = m.findItem(R.id.nav_salesPerson_teamStats);
        MenuItem item_nav_teamLeaderDashboard = m.findItem(R.id.nav_salesPerson_teamLeaderDashboard);
        Log.e(TAG, "isSalesTeamLead:  "+ isSalesTeamLead);
        //set view team stats visible option only for sales team lead
        item_nav_teamStats.setVisible(isSalesTeamLead);
        item_nav_teamLeaderDashboard.setVisible(isSalesTeamLead);


       /* navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch (id) {

                    case R.id.nav_salesPerson_home:
                        //this.setTitle(getString(R.string.menu_home));
                        //set school name as a title
                        dismiss();
                        return true;

                    case R.id.nav_salesPerson_inventory:
                        //this.setTitle(getString(R.string.menu_home));
                        //set school name as a title
                        //startActivity(new Intent(context, InventoryHomeActivity.class));

                        boolean b=!m.findItem(R.id.nav_salesPerson_SalesHeadCallLogsStats).isVisible();
                        //setting submenus visible state
                        m.findItem(R.id.nav_salesPerson_unClaimedLeads).setVisible(b);
                        m.findItem(R.id.nav_salesPerson_claimedLeads).setVisible(b);
                        m.findItem(R.id.nav_salesPerson_siteVisitList).setVisible(b);

//                        dismiss();
                        return true;

                }

                return false;

            }
        });*/
    }


    private void applyFontToMenuItem(MenuItem mi) {

        Typeface font = Typeface.createFromAsset(context.getAssets(), "metropolis_medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        //mNewTitle.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, mNewTitle.length(), 0); //Use this if you want to center the items
        mi.setTitle(mNewTitle);
    }


    private void updateUser()
    {

        String first_name = null,last_name = null, profile_photo = null, user_email= null;
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            //firstName
            if (sharedPreferences.getString("first_name", null) != null) first_name = sharedPreferences.getString("first_name", null);
            //lastName
            if (sharedPreferences.getString("last_name", null) != null) last_name = sharedPreferences.getString("last_name", null);
            //mobile1
            if (sharedPreferences.getString("email", null) != null) user_email = sharedPreferences.getString("email", null);
            //photoPath
            if (sharedPreferences.getString("profile_photo", null) != null) profile_photo = sharedPreferences.getString("profile_photo", null);
        }

        String user_name = last_name!=null ? first_name + " "+ last_name : first_name;

        tv_userName.setText( user_name!=null ? user_name : getString(R.string.user_name));
        //tv_userRole.setText( designation!=null ? designation : getString(R.string.user_role));
        tv_userEmail.setText( user_email!=null ? user_email : getString(R.string.user_email));


        if (profile_photo!=null)
        {
            final Context context = getApplicationContext();
            if (Helper.isValidContextForGlide(context))
            {
                Glide.with(getApplicationContext())
                        .load(profile_photo)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().centerCrop())
                        .apply(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.ic_profile_icon_nav_d)))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .apply(new RequestOptions().error(R.drawable.ic_profile_icon_nav_d))
                        //.skipMemoryCache(true)
                        .into(iv_userPic);
            }
        }

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {

           /* case R.id.nav_salesPerson_home:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                dismiss();
                return true;

            case R.id.nav_salesPerson_inventory:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, InventoryHomeActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_callSchedule:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, CallScheduleMainActivity.class));
                dismiss();
                return true;


            case R.id.nav_salesPerson_unClaimedLeads:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, UnClaimedLeadsActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_claimedLeads:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, ClaimedLeads_Activity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_siteVisitList:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, SiteVisitList_Activity.class));
                dismiss();
                return true;

                case R.id.nav_salesPerson_siteVisitStat:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, SiteVisitStatActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_ghpList:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, GhpList_Activity.class));
                dismiss();
                return true;


            case R.id.nav_salesPerson_salesHeadDashboard:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, SalesHeadDashboard_Activity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_teamLeaderDashboard:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, TeamLeaderDashboardActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_openSaleBlocks:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, BlockForOpenSaleActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_leadReassign:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, LeadReAssign_Activity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_teamLeadList:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, TeamLeaderList.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_salesExecutiveList:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, SalesExecutivesActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_cpFOSReports:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, CPReportActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_teamLeadStats:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, TeamLeadStatsActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_teamStats:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, TeamStatsActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_allottedFlats:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, AllottedFlats_Activity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_holdFlats:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, isSalesHead ? DirectHoldFlatsActivity.class : DirectHoldFlatsActivity.class));
                //startActivity(new Intent(context, isSalesHead ? SHHoldFlatListActivity.class : DirectHoldFlatsActivity.class));
                //startActivity(new Intent(context, DirectHoldFlatsActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_booking:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, BookingEventsActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_projectBrochures:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, ProjectBrochuresActivity.class));
                dismiss();
                return true;

            case R.id.nav_salesPerson_notifications:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, NotificationsActivity.class));
                dismiss();
                return true;
                 *//* case R.id.nav_salesPerson_allCustomers:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, MyPerformanceActivity.class));
                dismiss();
                return  true;*//*
           *//* case R.id.nav_salesPerson_unClaimedLeads:
                //this.setTitle(getString(R.string.menu_home));
                //set school name as a title
                startActivity(new Intent(context, NotificationsActivity.class));
                dismiss();
                return  true;*//*

            case R.id.nav_salesPerson_logout:
                //set Logout Alert
                dismiss();
                showLogoutAlert();
                break;*/

        }
        return true;
    }


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
            if (isNetworkAvailable(context)) {
                call_updateFCM();
                new Handler().postDelayed(this::logout, 1000);
            }else NetworkError(context);

        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= getActivity().getWindowManager().getDefaultDisplay().getWidth();
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


    public boolean isNetworkAvailable(Context activity)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void NetworkError(Context activity)
    {
        //Snackbar.make(activity.findViewById(android.R.id.content), "Network is not Available", Snackbar.LENGTH_LONG).show();
        Toast.makeText(activity, "Network not available!", Toast.LENGTH_SHORT).show();
    }

    private void logout()
    {

        if(sharedPreferences!=null)
        {
            //normal logout
            editor = sharedPreferences.edit();

            //remove donation shortcut
            new Helper().createShortCut(context,false);

            //int socialType = 0;
            //if (sharedPreferences.getInt("socialType", 0) != 0) socialType = sharedPreferences.getInt("socialType", 0);

            /*if (socialType==2)  //Gmail
            {
                //gmail logout

                // Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>()
                        {
                            @Override
                            public void onResult(@NonNull Status status)
                            {
                                // ...
                                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                            }
                        });

            }
            else if (socialType==3)     //fb
            {
                //fb logout
                LoginManager.getInstance().logOut();
            }*/


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
        // [END Unsubscribe_topics]


        Toast.makeText(context, "Logout Successful!", Toast.LENGTH_SHORT).show();
        //new Helper().showCustomToast(, "Logout Successful!");
        Intent intent=new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //SessionManager.getInstance().logoutUser();
        context.startActivity(intent);
        //FINISH THE SCREEN AND GO TO MAIN ACTIVITY
        //context.finish();
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


}