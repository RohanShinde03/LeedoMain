package com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import android.view.animation.Transformation;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.api.WebServer;
import com.tribeappsoft.leedo.admin.callLog.CallLogActivity;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.AddFlatOnHoldActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.DirectHoldFlatsActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.FlatAllotmentActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.model.DetailedStatFeedDetails;

import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.stickyScrollView.StickyScrollView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class StatGHPDetailsActivity extends AppCompatActivity {

    @BindView(R.id.swipeRefresh_statGhpDetails) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.cl_statGhpDetails) CoordinatorLayout cl_statGhpDetails;
    @BindView(R.id.ll_statGhpDetails_main) LinearLayoutCompat ll_main;

    @BindView(R.id.iv_itemLead_SiteVisitDetails_ec) AppCompatImageView ivExpandIcon;
    @BindView(R.id.ll_site_visit_details_filter) LinearLayoutCompat ll_showFilter;
    @BindView(R.id.mtv_full_name) MaterialTextView mtvFullName;
    @BindView(R.id.ll_noData) LinearLayoutCompat llNoData;
    @BindView(R.id.mtv_project_name) MaterialTextView mtvProjectName;
    @BindView(R.id.mtv_from_date) MaterialTextView mtvFromDate;
    @BindView(R.id.mtv_to_date) MaterialTextView mtvToDate;
    @BindView(R.id.mtv_event_name) MaterialTextView mtvEventName;
    @BindView(R.id.mtv_cp_full_name) MaterialTextView mtvCPName;
    @BindView(R.id.mtv_lead_status_name) MaterialTextView mtvLeadStatus;

    @BindView(R.id.ll_GHPDetailsFilterMain) LinearLayoutCompat llGHPDetailsFilterMain;
    @BindView(R.id.ll_GHPDetailsNameLayout) LinearLayoutCompat llGHPDetailsNameLayout;
    @BindView(R.id.ll_GHPDetailsProjectLayout) LinearLayoutCompat llGHPDetailsProjectLayout;
    @BindView(R.id.ll_GHPDetailsFromDateLayout) LinearLayoutCompat llGHPDetailsFromDateLayout;
    @BindView(R.id.ll_GHPDetailsToDateLayout) LinearLayoutCompat llGHPDetailsToDateLayout;
    @BindView(R.id.ll_GHPDetailsCpNameLayout) LinearLayoutCompat llLeadDetailsCpNameLayout;
    @BindView(R.id.ll_GHPDetailsEventNameLayout) LinearLayoutCompat llLeadDetailsEventNameLayout;
    @BindView(R.id.ll_GHPDetailsLeadStatusLayout) LinearLayoutCompat llLeadDetailsLeadStatusLayout;
    @BindView(R.id.tv_Ghp_filter_count_status) MaterialTextView filterCountText;

    //StickyScrollView
    @BindView(R.id.stv_statGhpDetails)
    StickyScrollView stv_GhpDetails;
    //Add Lead Data Layouts
    @BindView(R.id.ll_statGhpDetails_add_lead_layout) LinearLayoutCompat ll_statGhpDetailsMainLayout;
    @BindView(R.id.ll_statGhpDetails_addLeadData_content) LinearLayoutCompat ll_statGhpDetailsContentLayout;
    //loading content progressbar
    @BindView(R.id.ll_statGhpDetails_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg)
    AppCompatTextView tv_loadingMsg;

    private Activity context;
    private ArrayList<DetailedStatFeedDetails> itemArrayList,tempArrayList;
    private String TAG = "StatGHPDetailsActivity",api_token="",fromDate="",toDate="",full_name="",project_name = "",cp_name="",event_name="",lead_status="",filter_text="";
    private int sales_person_id=0,project_id,cp_executive_id=0,cp_id=0,flag = 0,filterCount=0, lead_token_status_id = 0,token_type_id = 0;// flag 1 => GHP ,flag 3-> GHP+ // lead_token_status_id ==2  GHP pen ding , 0 for regular
    private int leadStatusId= 0, event_id=0,current_page = 1, total_pages = 1,skip_count = 0;
    private boolean isExpand=false;
    private final Animations anim = new Animations();
    private boolean isTeamLeadStat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_g_h_p_details);
        ButterKnife.bind(this);
        context = StatGHPDetailsActivity.this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>GHP Details</font>"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        init();

        //init ArrayList
        itemArrayList = new ArrayList<>();
        tempArrayList = new ArrayList<>();

        //set up scrollView
        setUpScrollView();

        ivExpandIcon.setOnClickListener(v -> {
            if (isExpand)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(ivExpandIcon, false);
                collapse(ll_showFilter);
                isExpand = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(ivExpandIcon, true);
                expandSubView(ll_showFilter);
                isExpand = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        boolean isSiteVisitAdded;
        isSiteVisitAdded = sharedPreferences.getBoolean("siteVisitAdded", false);
        if(isSiteVisitAdded){
            editor.putBoolean("siteVisitAdded",false);
            editor.apply();
            resumeFeedApi();
        }

    }

    /*Collapsing View*/
    private void collapse(final View v)
    {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    /*Expandable View*/
    private void expandSubView(final View v)
    {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime==1) v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    private void init() {
        ll_pb.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        if (getIntent() != null) {
            sales_person_id = getIntent().getIntExtra("sales_person_id", 0);
            lead_token_status_id = getIntent().getIntExtra("lead_token_status_id", 0);
            token_type_id = getIntent().getIntExtra("token_type_id", 0);
            project_id = getIntent().getIntExtra("project_id", 0);
            cp_executive_id = getIntent().getIntExtra("cp_executive_id",0);
            cp_id = getIntent().getIntExtra("cp_id",0);
            fromDate = getIntent().getStringExtra("from_date");
            toDate = getIntent().getStringExtra("to_date");
            full_name = getIntent().getStringExtra("full_name");
            project_name = getIntent().getStringExtra("Project_name");
            event_id = getIntent().getIntExtra("event_id",0);
            leadStatusId = getIntent().getIntExtra("lead_status_id",0);
            event_name = getIntent().getStringExtra("event_name");
            lead_status = getIntent().getStringExtra("lead_status");
            cp_name = getIntent().getStringExtra("cp_name");
            flag = getIntent().getIntExtra("flag",0);
            isTeamLeadStat = getIntent().getBooleanExtra("isTeamLeaderStat",false);

            if(flag == 1){
                Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#FFFFFF'>GHP Details</font>"));
                lead_token_status_id = 1;
                token_type_id = 1;
            }else if (flag ==3){
                Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#FFFFFF'>GHP Plus Details</font>"));
                lead_token_status_id = 1;
                token_type_id = 3;
            }
            else {
                Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#FFFFFF'>GHP Payment Pending</font>"));
                lead_token_status_id = 3;
            }

            if(full_name == null || full_name.trim().isEmpty()){
                llGHPDetailsNameLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }
            if(project_name == null || project_name.trim().isEmpty()){
                llGHPDetailsProjectLayout.setVisibility(View.GONE);
            }else {
                filterCount++;
            }
            if(fromDate == null || fromDate.trim().isEmpty()){
                llGHPDetailsFromDateLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }
            if(toDate == null || toDate.trim().isEmpty()){
                llGHPDetailsToDateLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }
            if(cp_name == null || cp_name.trim().isEmpty()){
                llLeadDetailsCpNameLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }

            if(event_name == null || event_name.trim().isEmpty()){
                llLeadDetailsEventNameLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }

            if(lead_status == null || lead_status.trim().isEmpty()){
                llLeadDetailsLeadStatusLayout.setVisibility(View.GONE);
            }else{
                filterCount++;
            }

            filterCountText.setText(filterCount+ " filters applied");

            mtvFullName.setText(full_name);
            mtvProjectName.setText(project_name);
            mtvCPName.setText(cp_name);
            mtvEventName.setText(event_name);
            mtvLeadStatus.setText(lead_status);
            if(fromDate !=null){
                if(!fromDate.trim().isEmpty()){
                    // mtvFromDate.setText(fromDate);
                    mtvFromDate.setText(Helper.formatDateFromString(fromDate));
                }else{
                    mtvFromDate.setText("-");
                }
            }else {
                mtvFromDate.setText("-");
            }

            if(toDate != null){
                if(!toDate.trim().isEmpty()){
                    //mtvToDate.setText(toDate);
                    mtvToDate.setText(Helper.formatDateFromString(toDate));
                }else {
                    mtvToDate.setText("-");
                }
            }else {
                mtvToDate.setText("-");
            }
        }
        //set up swipe refresh
        setSwipeRefresh();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //set refresh api
            call_getGhpDetails();
        }
        else Helper.NetworkError(context);
    }

    private void setUpScrollView() {

        stv_GhpDetails.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
            View view = v.getChildAt(v.getChildCount() - 1);
            // Calculate the scroll_diff
            int diff = (view.getBottom() - (v.getHeight() + v.getScrollY()));
            // if diff is zero, then the bottom has been reached
            if (diff == 0)
            {
                // notify that we have reached the bottom
                Log.e(TAG, "MyScrollView: Bottom has been reached");

                if (!swipeRefresh.isRefreshing())
                {
                    showProgressBar();

                    Log.e(TAG, "onScrollStateChanged: current page " + current_page);
                    if (current_page <= total_pages)  //call paginate api till ary is not empty
                    {
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                            //call get sales feed
                            call_getGhpDetails();
                            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

                        } else Helper.NetworkError(Objects.requireNonNull(context));
                    } else {
                        Log.e(TAG, "stopApiCall");
                        hideProgressBar();
                    }
                }
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        ll_loadingContent.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        ll_loadingContent.setVisibility(View.GONE);
        //Objects.requireNonNull(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() ->
        {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                //set refreshing true
                swipeRefresh.setRefreshing(true);

                //gone visibility
                llNoData.setVisibility(View.GONE);

                //1. clear arrayList
                itemArrayList.clear();
                tempArrayList.clear();

                //remove all view from feed
                ll_statGhpDetailsContentLayout.removeAllViews();

                //2. reset call flag to 0 && Filter flag to 0
                skip_count = 0;  //openFlag = 0;
                current_page= 1;
                filter_text = "";
                showProgressBar();
                //set refresh api
                call_getGhpDetails();
            }
            else Helper.NetworkError(context);
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void resumeFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //set refreshing true
            swipeRefresh.setRefreshing(true);

            //gone visibility
            llNoData.setVisibility(View.GONE);

            //1. clear arrayList
            itemArrayList.clear();
            tempArrayList.clear();

            //remove all view from feed
            ll_statGhpDetailsContentLayout.removeAllViews();

            //2. reset call flag to 0 && Filter flag to 0
            skip_count = 0;  //openFlag = 0;
            current_page= 1;
            filter_text = "";
            showProgressBar();
            //set refresh api
            call_getGhpDetails();

        }else Helper.NetworkError(context);
    }

    private void call_getGhpDetails(){
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadTokenDetails_new(api_token,project_id,fromDate,toDate,sales_person_id, lead_token_status_id,cp_executive_id,cp_id,event_id,leadStatusId,isTeamLeadStat,token_type_id,current_page,filter_text);
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
                .subscribe(new Subscriber<Response<JsonObject>>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");
                        tempArrayList.addAll(itemArrayList);
                        if(skip_count > 0){
                            setUpdateFeed();
                        }else {
                            setFeeds();
                        }
                        current_page++;
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            JsonObject jsonObject = JsonObjectResponse.body().get("data").getAsJsonObject();
                                            current_page = jsonObject.get("current_page").getAsInt();
                                            total_pages = jsonObject.get("last_page").getAsInt();
                                            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                                            for(int i=0;i< jsonArray.size();i++){
                                                setJson(jsonArray.get(i).getAsJsonObject());
                                            }
                                        }
                                    } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        } else {
                            // error case
                            switch (JsonObjectResponse.code()) {
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


    private void setJson(JsonObject jsonObject) {

        DetailedStatFeedDetails detailedStatFeedDetails = new DetailedStatFeedDetails();
        if (jsonObject.has("lead_uid")) detailedStatFeedDetails.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "");
        if (jsonObject.has("person_id")) detailedStatFeedDetails.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsString() : "");
        if (jsonObject.has("lead_status_id")) detailedStatFeedDetails.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0);
        if (jsonObject.has("email")) detailedStatFeedDetails.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("mobile_number")) detailedStatFeedDetails.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("project_name")) detailedStatFeedDetails.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        if (jsonObject.has("unit_category")) detailedStatFeedDetails.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "");
        if (jsonObject.has("lead_types_name")) detailedStatFeedDetails.setLead_type(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "");
        if (jsonObject.has("full_name")) detailedStatFeedDetails.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("lead_status")) detailedStatFeedDetails.setLead_status_name(!jsonObject.get("lead_status").isJsonNull() ? jsonObject.get("lead_status").getAsString() : "");
        if (jsonObject.has("country_code")) detailedStatFeedDetails.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "+91");
        if (jsonObject.has("tag_elapsed_time")) detailedStatFeedDetails.setTag_elapsed_time(!jsonObject.get("tag_elapsed_time").isJsonNull() ? jsonObject.get("tag_elapsed_time").getAsString() : "");
        if (jsonObject.has("tag_date")) detailedStatFeedDetails.setTag_date(!jsonObject.get("tag_date").isJsonNull() ? jsonObject.get("tag_date").getAsString() : "");
        if (jsonObject.has("description")) detailedStatFeedDetails.setDescription(!jsonObject.get("description").isJsonNull() ? jsonObject.get("description").getAsString() : "");


        CUIDModel cuidModel = new CUIDModel();
        if (jsonObject.has("lead_id"))cuidModel.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0);
        if (jsonObject.has("lead_uid"))cuidModel.setCu_id(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "");
        if (jsonObject.has("country_code"))cuidModel.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number"))cuidModel.setCustomer_mobile(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("email"))cuidModel.setCustomer_email(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("prefix"))cuidModel.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "");
        if (jsonObject.has("first_name"))cuidModel.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "");
        if (jsonObject.has("middle_name"))cuidModel.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "");
        if (jsonObject.has("last_name"))cuidModel.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "");
        if (jsonObject.has("full_name"))cuidModel.setCustomer_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("is_kyc_uploaded"))cuidModel.setIs_kyc_uploaded(!jsonObject.get("is_kyc_uploaded").isJsonNull() ? jsonObject.get("is_kyc_uploaded").getAsInt() : 0);
        if (jsonObject.has("is_reminder"))cuidModel.setIs_reminder_set(!jsonObject.get("is_reminder").isJsonNull() ? jsonObject.get("is_reminder").getAsInt() : 0);
        if (jsonObject.has("lead_types_id"))cuidModel.setLead_types_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0);
        // if (jsonObject.has("lead_types_name"))cuidModel.setLeadt(!object.get("lead_types_name").isJsonNull() ? object.get("lead_types_name").getAsString() :"");
        if (jsonObject.has("lead_status_id"))cuidModel.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0);
        if (jsonObject.has("lead_status_name"))cuidModel.setLead_status_name(!jsonObject.get("lead_status_name").isJsonNull() ? jsonObject.get("lead_status_name").getAsString() :"");
        if (jsonObject.has("token_media_path"))cuidModel.setToken_media_path(!jsonObject.get("token_media_path").isJsonNull() ? jsonObject.get("token_media_path").getAsString() :"");
        if (jsonObject.has("token_id"))cuidModel.setToken_id(!jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0 );
        if (jsonObject.has("token_no"))cuidModel.setToken_no(!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() :"");
        if (jsonObject.has("project_id"))cuidModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0);
        if (jsonObject.has("project_name"))cuidModel.setCustomer_project_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() :"");
        if (jsonObject.has("event_title"))cuidModel.setEventName(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() :"");
        if (jsonObject.has("event_id"))cuidModel.setEvent_id(!jsonObject.get("event_id").isJsonNull() ? jsonObject.get("event_id").getAsInt() :0);
        if (jsonObject.has("token_type_id"))cuidModel.setToken_type_id(!jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() :0);
        if (jsonObject.has("token_type"))cuidModel.setToken_type(!jsonObject.get("token_type").isJsonNull() ? jsonObject.get("token_type").getAsString() :"");
        if (jsonObject.has("ghp_date"))cuidModel.setGhp_date(!jsonObject.get("ghp_date").isJsonNull() ? jsonObject.get("ghp_date").getAsString() :"");
        if (jsonObject.has("ghp_amount"))cuidModel.setGhp_amount(!jsonObject.get("ghp_amount").isJsonNull() ? jsonObject.get("ghp_amount").getAsString() :"");
        if (jsonObject.has("ghp_plus_date"))cuidModel.setGhp_plus_date(!jsonObject.get("ghp_plus_date").isJsonNull() ? jsonObject.get("ghp_plus_date").getAsString() :"");
        if (jsonObject.has("ghp_plus_amount"))cuidModel.setGhp_plus_amount(!jsonObject.get("ghp_plus_amount").isJsonNull() ? jsonObject.get("ghp_plus_amount").getAsString() :"");
        if (jsonObject.has("payment_link"))cuidModel.setPayment_link(!jsonObject.get("payment_link").isJsonNull() ? jsonObject.get("payment_link").getAsString() :"");
        if (jsonObject.has("payment_invoice_id"))cuidModel.setPayment_invoice_id(!jsonObject.get("payment_invoice_id").isJsonNull() ? jsonObject.get("payment_invoice_id").getAsString() :"");

        if (jsonObject.has("unit_hold_release_id")) detailedStatFeedDetails.setUnit_hold_release_id(!jsonObject.get("unit_hold_release_id").isJsonNull() ? jsonObject.get("unit_hold_release_id").getAsInt() : 0 );
        if (jsonObject.has("unit_id")) detailedStatFeedDetails.setUnit_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) detailedStatFeedDetails.setUnit_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "" );
        if (jsonObject.has("floor_id")) detailedStatFeedDetails.setFloor_id(!jsonObject.get("floor_id").isJsonNull() ? jsonObject.get("floor_id").getAsInt() : 0 );
        if (jsonObject.has("block_id")) detailedStatFeedDetails.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );

        if (jsonObject.has("lead_stage_id")) cuidModel.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage"))cuidModel.setLead_stage_name(!jsonObject.get("lead_stage").isJsonNull() ? jsonObject.get("lead_stage").getAsString() :"");
        if (jsonObject.has("remark"))cuidModel.setGhp_remark(!jsonObject.get("remark").isJsonNull() ? jsonObject.get("remark").getAsString() :"");
        //if (object.has("remark"))remarks=!object.get("remark").isJsonNull() ? object.get("remark").getAsString() :"";
        if (jsonObject.has("booking_id"))cuidModel.setBooking_id(!jsonObject.get("booking_id").isJsonNull() ? jsonObject.get("booking_id").getAsInt() :0);

        //lead_status_id==5 && site visit count > 1
        if(cuidModel.getLead_status_id()==5 && cuidModel.getSite_visit_count()>1) {
            detailedStatFeedDetails.setStatus_text("Site Revisited");
        }

        // Ghp generated and token type id 3 == ghp+
        if(cuidModel.getLead_status_id()==6 && cuidModel.getToken_type_id()==3) {
            detailedStatFeedDetails.setStatus_text("GHP+ Generated");
        }
        if(token_type_id == 3){
            if(jsonObject.has("ghp_details")){
                JsonObject jsonObj = jsonObject.get("ghp_details").getAsJsonObject();
                if (jsonObj.has("ghp_plus_date"))cuidModel.setGhp_plus_date(!jsonObj.get("ghp_plus_date").isJsonNull() ? jsonObj.get("ghp_plus_date").getAsString() :"");
                if (jsonObj.has("ghp_plus_amount"))cuidModel.setGhp_plus_amount(!jsonObj.get("ghp_plus_amount").isJsonNull() ? jsonObj.get("ghp_plus_amount").getAsString() :"");
            }
        }else if(token_type_id == 1){
            if(jsonObject.has("ghp_details")){
                JsonObject jsonObj = jsonObject.get("ghp_details").getAsJsonObject();
                if (jsonObj.has("ghp_date"))cuidModel.setGhp_date(!jsonObj.get("ghp_date").isJsonNull() ? jsonObj.get("ghp_date").getAsString() :"");
                if (jsonObj.has("ghp_amount"))cuidModel.setGhp_amount(!jsonObj.get("ghp_amount").isJsonNull() ? jsonObj.get("ghp_amount").getAsString() :"");
            }
        }


        detailedStatFeedDetails.setCuidModel(cuidModel);

        if (jsonObject.has("other_info")) {
            if (!jsonObject.get("other_info").isJsonNull() && jsonObject.get("other_info").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("other_info").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                detailedStatFeedDetails.setDetailsTitleModelArrayList(arrayList);
            }
        }


        itemArrayList.add(detailedStatFeedDetails);
    }

    private void setOtherInfoJson(JsonObject jsonObject, ArrayList<LeadDetailsTitleModel> arrayList) {

        LeadDetailsTitleModel model = new LeadDetailsTitleModel();
        if (jsonObject.has("section_title"))
            model.setLead_details_title(!jsonObject.get("section_title").isJsonNull() ? jsonObject.get("section_title").getAsString() : "");

        if (jsonObject.has("section_items")) {
            if (!jsonObject.get("section_items").isJsonNull() && jsonObject.get("section_items").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("section_items").getAsJsonArray();
                ArrayList<LeadDetailsModel> arrayList1 = new ArrayList<>();
                arrayList1.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setSectionDetailsJson(jsonArray.get(i).getAsJsonObject(), arrayList1);
                }
                model.setLeadDetailsModels(arrayList1);
            }
        }
        arrayList.add(model);
    }

    private void setSectionDetailsJson(JsonObject jsonObject, ArrayList<LeadDetailsModel> arrayList1) {
        LeadDetailsModel model = new LeadDetailsModel();
        if (jsonObject.has("section_item_title")) model.setLead_details_text(!jsonObject.get("section_item_title").isJsonNull() ? jsonObject.get("section_item_title").getAsString() : "");
        if (jsonObject.has("section_item_desc")) model.setLead_details_value(!jsonObject.get("section_item_desc").isJsonNull() ? jsonObject.get("section_item_desc").getAsString() : "");
        arrayList1.add(model);
    }


    private void setFeeds() {
        if (context != null) {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                if (itemArrayList != null && itemArrayList.size() > 0) {
                    //having data
                    Log.e(TAG,"skipCount1"+ skip_count);
                    llNoData.setVisibility(View.GONE);
                    ll_statGhpDetailsContentLayout.removeAllViews();
                    for (int i = skip_count; i < itemArrayList.size(); i++) {
                        View rowView_sub = getLeadDetailsView(i);
                        ll_statGhpDetailsContentLayout.addView(rowView_sub);
                    }
                    skip_count = ll_statGhpDetailsContentLayout.getChildCount();
                    Log.e(TAG,"skipCount2"+ skip_count);

                    ll_statGhpDetailsContentLayout.setVisibility(View.VISIBLE);
                    //visible main
                    ll_main.setVisibility(View.VISIBLE);

                    //set scrollView scroll to top
                    stv_GhpDetails.smoothScrollTo(0, 0);

                } else {
                    //empty feed
                    llNoData.setVisibility(View.VISIBLE);
                    ll_main.setVisibility(View.GONE);
                    ll_statGhpDetailsContentLayout.setVisibility(View.GONE);
                }

                Log.e(TAG,"current PAge : "+ current_page+" total Pages : "+total_pages);
                //hide pb when stop api call
                if (current_page >= total_pages) {
                    hideProgressBar();
                    //new Helper().showSuccessCustomToast(context, "Last page!");
                }
            });
        }
    }

    private void setUpdateFeed() {
        if (context != null) {
            context.runOnUiThread(() -> {

                if (itemArrayList != null && itemArrayList.size() > 0) {
                    //having data

                    llNoData.setVisibility(View.GONE);
                    Log.e(TAG, "setUpdateFeed: updateCount "+skip_count );
                    Log.e(TAG, "setUpdateFeed: size "+ itemArrayList.size() );

                    //ll_addFeedData.removeAllViews();
                    for (int i = skip_count; i < itemArrayList.size(); i++) {
                        View rowView_sub = getLeadDetailsView(i);
                        ll_statGhpDetailsContentLayout.addView(rowView_sub);
                    }

                    skip_count = ll_statGhpDetailsContentLayout.getChildCount();

                    ll_statGhpDetailsContentLayout.setVisibility(View.VISIBLE);
                    //visible main
                    ll_main.setVisibility(View.VISIBLE);

                } else {
                    //empty feed
                    llNoData.setVisibility(View.VISIBLE);
                    ll_main.setVisibility(View.GONE);
                    ll_statGhpDetailsContentLayout.setVisibility(View.GONE);
                }

                swipeRefresh.setRefreshing(false);
                Log.e(TAG,"current PAge : "+ current_page+" total Pages : "+total_pages);
                //hide pb when stop api call
                if (current_page >= total_pages) {
                    hideProgressBar();
                    //new Helper().showSuccessCustomToast(context, "Last page!");
                }
            });
        }
    }

    private View getLeadDetailsView(int position) {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_drill_down_leads, null);

        MaterialCardView mtCardView = rowView.findViewById(R.id.cv_itemDrillDownLeads_main);
        MaterialTextView mtvLeadType = rowView.findViewById(R.id.mTv_itemDrillDownLeads_tag);
        MaterialTextView mtvLeadDetailsCuIdNumber = rowView.findViewById(R.id.mTv_itemDrillDownLeads_cuIdNumber);
        MaterialTextView mtvLeadName = rowView.findViewById(R.id.mTv_itemDrillDownLeads_leadName);
        MaterialTextView mtvLeadStatus = rowView.findViewById(R.id.mTv_itemDrillDownLeads_status);
        MaterialTextView mtvProjectName = rowView.findViewById(R.id.mTv_itemDrillDownLeads_projectName);
        MaterialTextView mtvElapsedTime = rowView.findViewById(R.id.mTv_itemDrillDownLeads_elapsedTime);
        MaterialTextView mtvLeadDate = rowView.findViewById(R.id.mTv_itemDrillDownLeads_date);

        AppCompatImageView iv_whatsAppIcon = rowView.findViewById(R.id.iv_itemDrillDownLeads_leadWhatsApp);
        AppCompatImageView iv_callIcon = rowView.findViewById(R.id.iv_itemDrillDownLeads_leadCall);
        AppCompatImageView iv_others_leadOptions = rowView.findViewById(R.id.iv_itemDrillDownLeads_leadOptions);
        AppCompatImageView iv_leadDetails_ec = rowView.findViewById(R.id.iv_itemDrillDownLeads_leadDetails_ec);

        LinearLayoutCompat ll_others_viewLeadDetails = rowView.findViewById(R.id.ll_itemDrillDownLeads_viewLeadDetails);
        LinearLayoutCompat ll_others_addLeadDetails = rowView.findViewById(R.id.ll_itemDrillDownLeads_addLeadDetails);



        final DetailedStatFeedDetails myModel = itemArrayList.get(position);
        mtvLeadType.setText(myModel.getLead_type() != null && !myModel.getLead_type().trim().isEmpty() ? myModel.getLead_type() : "");
        mtvLeadDetailsCuIdNumber.setText(myModel.getLead_uid() != null && !myModel.getLead_uid().trim().isEmpty() ? myModel.getLead_uid() : "");
        mtvLeadName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        mtvProjectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty() ? myModel.getDescription() : "");
        mtvLeadStatus.setText(myModel.getLead_status_name() != null && !myModel.getLead_status_name().trim().isEmpty() ? myModel.getLead_status_name() : "");
        mtvElapsedTime.setText(myModel.getTag_elapsed_time() !=null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
        mtvLeadDate.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");


        //whatsApp
        iv_whatsAppIcon.setOnClickListener(v -> {
            if (myModel.getMobile_number()!=null)
            {
                Log.e(TAG,"country code - "+ myModel.getCountry_code());
                String country_code = myModel.getCountry_code();
                if(country_code == null){
                    country_code ="+91";
                }
                //send Message to WhatsApp Number
                sendMessageToWhatsApp(country_code+myModel.getMobile_number(), myModel.getFull_name());
            }
            else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
        });

        //mobile number call
        iv_callIcon.setOnClickListener(v -> {
            if (myModel.getMobile_number()!=null)
            {
                new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getMobile_number());
            }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
        });

        //set others popup menu's
        iv_others_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_others_leadOptions, myModel));

        //Set Lead Details
        if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
        {
            iv_leadDetails_ec.setVisibility(View.VISIBLE);
            ll_others_addLeadDetails.removeAllViews();
            for (int i = 0; i < myModel.getDetailsTitleModelArrayList().size(); i++) {
                //Log.e("ll_HomeFeed_own_", "onBindViewHolder: "+myModel.getDetailsTitleModelArrayList().size());

                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_item_leads_title, null);
                final AppCompatTextView tv_leads_tag_details_title_text = rowView_sub.findViewById(R.id.tv_itemLeadDetails_title);
                final LinearLayoutCompat ll_addDetails = rowView_sub.findViewById(R.id.ll_itemLeadDetails_addDetails);
                tv_leads_tag_details_title_text.setText(myModel.getDetailsTitleModelArrayList().get(i).getLead_details_title());
                ll_addDetails.removeAllViews();
                ArrayList<LeadDetailsModel> detailsModelArrayList = myModel.getDetailsTitleModelArrayList().get(i).getLeadDetailsModels();
                if (detailsModelArrayList != null && detailsModelArrayList.size() > 0) {
                    for (int j = 0; j < detailsModelArrayList.size(); j++) {
                        //Log.e("ll_HomeFeed_own_", "detailsModelArrayList.get(j).getLead_details_text() "+detailsModelArrayList.get(j).getLead_details_text());
                        @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null);
                        final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                        final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);

                        tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                        tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                        ll_addDetails.addView(rowView_subView);
                    }
                }
                ll_others_addLeadDetails.addView(rowView_sub);
            }

        } else iv_leadDetails_ec.setVisibility(View.GONE);


        //set expand Collapse Others
        mtCardView.setOnClickListener(view -> {

            if (myModel.isExpandedOthersView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_leadDetails_ec, false);
                collapse(ll_others_viewLeadDetails);
                myModel.setExpandedOthersView(false);
            } else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate(iv_leadDetails_ec, true);
                expandSubView(ll_others_viewLeadDetails);
                myModel.setExpandedOthersView(true);
            }
        });

        //unclaimed
        if (myModel.getCuidModel().getLead_status_id() == 1) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_lead_unclaimed));
        // lead claimed
        if (myModel.getCuidModel().getLead_status_id() == 2)  mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
        // lead assigned
        if (myModel.getCuidModel().getLead_status_id() == 3)  mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
        //self/ lead added
        if (myModel.getCuidModel().getLead_status_id() == 4) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
        //site visited
        if (myModel.getCuidModel().getLead_status_id() == 5) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
        //token /GHP  generated
        if (myModel.getCuidModel().getLead_status_id() == 6) {

            //token /upgraded with GHP Plus
            if(myModel.getCuidModel().getToken_type_id()==3) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_token_plus_generated));
            else mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));
        }

        //token /GHP  cancelled
        if (myModel.getCuidModel().getLead_status_id() == 7) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
        //on hold
        if (myModel.getCuidModel().getLead_status_id() == 8) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
        //booked
        if (myModel.getCuidModel().getLead_status_id() == 9) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));
        //booking cancelled
        if (myModel.getCuidModel().getLead_status_id() == 10) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
        //ghp pending
        if (myModel.getCuidModel().getLead_status_id() == 13) mtvLeadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_ghp_plus_pending));

        return rowView;
    }

    private void sendMessageToWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );
        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, WebServer.VJ_Website);

        String url = null;
        try {
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "Hello "+ main_title + ", Welcome to VJ family... Thank you for your registration." : "Hello", "UTF-8");
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            //new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

    }

    private void showPopUpMenu(View view, DetailedStatFeedDetails myModel) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_leadOption_directBooking, Menu.NONE, context.getString(R.string.direct_allotment));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_cancelBooking, Menu.NONE, context.getString(R.string.cancel_allotment_));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_continueAllotment, Menu.NONE, context.getString(R.string.continue_allotment));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_viewHoldFlat, Menu.NONE, context.getString(R.string.view_hold_flat));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_releaseFlat, Menu.NONE, context.getString(R.string.release_flat));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addSiteVisit, Menu.NONE, context.getString(R.string.add_site_visit));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addToken, Menu.NONE, context.getString(R.string.menu_generate_ghp));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_viewToken, Menu.NONE, context.getString(R.string.view_ghp));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addCallSchedule, Menu.NONE, context.getString(R.string.add_call_schedule));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addCallLog, Menu.NONE, context.getString(R.string.add_call_log));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addReminder, Menu.NONE, context.getString(R.string.add_reminder));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_callNow, Menu.NONE, context.getString(R.string.call_now));


        switch (myModel.getLead_status_id())
        {

            case  1:    //unclaimed
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);
                break;
            /*------------------------------------------------------------------------------------------------------*/

            case  2:    // lead claimed

            case  3:    // lead assigned

            case  4:     //self/ lead added

                //hide add token
                //TODO visible add token -- change in S.E. can generate GHP WO site visit
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  5:     //site visited
                //hide view GHP
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  6:        //GHP/Token generated


            case  13:   //GHP pending

                //hide add site visit & token
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  7:     //GHP/Token cancelled

                //hide add site visit & view GHP
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  8:     //on hold

                //call & reminder  option only
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);

                // visible continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(true);
                //hide direct allotment
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  9:    //booked

                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  10:    //booked cancelled

                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                break;

            default:    //def

                //hide all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible only add reminder and add call log
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);

                break;
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId())
            {
                case R.id.menu_leadOption_callNow:
                    if (myModel.getMobile_number() != null && !myModel.getMobile_number().trim().isEmpty())
                        new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getMobile_number());
                    return true;


                case R.id.menu_leadOption_directBooking:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddFlatOnHoldActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getLead_uid())
                                .putExtra("lead_name", myModel.getFull_name())
                                .putExtra("lead_id", myModel.getCuidModel().getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_cancelBooking:
                    //show cancel alert
                    showCancelAllotmentAlert(myModel.getFull_name(),myModel.getCuidModel().getBooking_id());
                    return true;

                case R.id.menu_leadOption_continueAllotment:
                    //show cancel alert

                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, FlatAllotmentActivity.class)
                                .putExtra("unit_hold_release_id",myModel.getUnit_hold_release_id())
                                .putExtra("unit_id", myModel.getUnit_id())
                                .putExtra("unit_name", myModel.getUnit_name())
                                .putExtra("project_name", myModel.getCuidModel().getCustomer_project_name())
                                .putExtra("project_id", myModel.getCuidModel().getProject_id())
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("block_id", myModel.getBlock_id())
                                .putExtra("floor_id", myModel.getFloor_id())
                                .putExtra("fromAddHoldFlat", true)
                                .putExtra("fromHoldList", true));
                    }
                    else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_viewHoldFlat:
                    //view hold flat list
                    context.startActivity(new Intent(context, DirectHoldFlatsActivity.class));
                    return true;

                case R.id.menu_leadOption_releaseFlat:
                    //show cancel alert
                    showReleaseHoldAlert(myModel.getFull_name(),myModel.getUnit_hold_release_id());
                    return true;


                case R.id.menu_leadOption_addCallLog:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getLead_uid())
                                .putExtra("lead_name", myModel.getFull_name())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("call_lead_id", myModel.getCuidModel().getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;


                case R.id.menu_leadOption_addCallSchedule:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddCallScheduleActivity.class)
                                .putExtra("customer_name", myModel.getFull_name())
                                .putExtra("lead_cu_id", myModel.getLead_uid())
                                .putExtra("lead_id", myModel.getCuidModel().getLead_id())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("fromFeed", true));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addReminder:
                    context.startActivity(new Intent(context, AddReminderActivity.class)
                            .putExtra("fromOther", 3)
                            .putExtra("lead_name", myModel.getFull_name())
                            .putExtra("lead_id", myModel.getCuidModel().getLead_id())
                    );
                    return true;

                case R.id.menu_leadOption_addSiteVisit:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddSiteVisitActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getLead_uid())
                                .putExtra("lead_name", myModel.getFull_name())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_id", myModel.getCuidModel().getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addToken:
                    if (myModel.getCuidModel()!=null)
                    {

                        if (myModel.getCuidModel().getLead_status_id()==2 || myModel.getCuidModel().getLead_status_id()==3 || myModel.getCuidModel().getLead_status_id()==4)
                        {
                            //check for lead status id is 2, 3, 4 (claimed, assigned, added) (Site visit not added)
                            //show alert to ask for generate site visit first
                            showAddSiteVisitAlert(myModel.getFull_name(),myModel);
                        }
                        else {
                            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                    .putExtra("fromOther",2)
                                    .putExtra("cuidModel", myModel.getCuidModel())
                                    .putExtra("cu_id", myModel.getLead_uid())
                                    .putExtra("lead_name", myModel.getFull_name())
                                    .putExtra("project_name", myModel.getDescription())
                                    .putExtra("lead_id", myModel.getCuidModel().getLead_id()));
                        }
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");

                    return true;

                case R.id.menu_leadOption_viewToken:

                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                .putExtra("fromOther",3)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getLead_uid())
                                .putExtra("lead_name", myModel.getFull_name())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("lead_id", myModel.getCuidModel().getLead_id()));
                    }else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");

                    return true;

                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();
    }

    private void showCancelAllotmentAlert(String CustomerName, int bookings_id)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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

        tv_msg.setText(getResources().getString(R.string.cancel_flat_allotment_que));
        tv_desc.setText(getString(R.string.cancel_allotment_text, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_cancelAllotment(bookings_id);

            } else Helper.NetworkError(context);

            alertDialog.dismiss();
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

    private void call_cancelAllotment(int bookings_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("booking_id", bookings_id);
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().cancelAllotment(jsonObject).enqueue(new Callback<JsonObject>()
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
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {

                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessAlert();
                                }
                                else showErrorLogClaimLead("Server response is empty!");

                            }else showErrorLogClaimLead("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLogClaimLead(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogClaimLead(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(context.getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }


    private void showReleaseHoldAlert(String CustomerName, int unit_hold_release_id)
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

        tv_msg.setText(getResources().getString(R.string.release_flat_question));
        tv_desc.setText(getString(R.string.que_release_flat, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                showCancellationProgressBar(getString(R.string.releasing_flat));
                call_markAsReleased(unit_hold_release_id);

            } else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

    private void call_markAsReleased(int unit_hold_release_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("unit_hold_release_id", unit_hold_release_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().directReleaseFlat(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() ) {
                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessReleaseFlatAlert();
                                }
                                else showErrorLogClaimLead("Server response is empty!");

                            }else showErrorLogClaimLead("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLogClaimLead(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogClaimLead(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(context.getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }

    @SuppressLint("InflateParams")
    private void showSuccessReleaseFlatAlert()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide
            hideCancellationProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");

            //show toast
            new Helper().showSuccessCustomToast(context, context.getString(R.string.flat_released_successfully));

            //set Feed Action Added to true
            /*if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

            //remove all view
            ll_addFeedData.removeAllViews();*/

            //resume feed api
             resumeFeedApi();

            //set scrollView scroll to top
            stv_GhpDetails.smoothScrollTo(0, 0);
        });

    }


    private void showAddSiteVisitAlert(String CustomerName, DetailedStatFeedDetails myModel)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;


        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getResources().getString(R.string.msg_add_ghp_without_site_visit));
        tv_desc.setText(getString(R.string.que_add_ghp_without_site_visit, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //goto add site visit

            context.startActivity(new Intent(context, AddSiteVisitActivity.class)
                    .putExtra("cuidModel", myModel.getCuidModel())
                    .putExtra("cu_id", myModel.getLead_uid())
                    .putExtra("lead_name", myModel.getFull_name())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getCuidModel().getLead_id()));
        });

        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //go to generate GHP
            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                    .putExtra("fromOther",2)
                    .putExtra("cuidModel", myModel.getCuidModel())
                    .putExtra("cu_id", myModel.getLead_uid())
                    .putExtra("lead_name", myModel.getFull_name())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getCuidModel().getLead_id()));

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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

    private void showCancellationProgressBar(String msg) {
        Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(msg);
        ll_pb.setVisibility(View.VISIBLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showErrorLogClaimLead(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();

                Helper.onErrorSnack(context, message);

            });
        }
    }

    private void hideCancellationProgressBar() {
        ll_pb.setVisibility(View.GONE);
        Objects.requireNonNull(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        Objects.requireNonNull(context).runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide
            hideCancellationProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");
            new Helper().showCustomToast(context, "Allotment cancelled successfully!!");

            //remove all view
            //ll_statGhpDetailsContentLayout.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            stv_GhpDetails.smoothScrollTo(0, 0);
        });

    }

    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(StatGHPDetailsActivity.this, message);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_self, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search_self);
        MenuItem filter = menu.findItem(R.id.action_filter);
        filter.setVisible(false);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(context).getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null)
        {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search));

            //Code for changing the search icon
            ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            icon.setColorFilter(Color.WHITE);
            //icon.setImageResource(R.drawable.ic_home_search);

            //AutoCompleteTextView searchTextView =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            AutoCompleteTextView searchTextView =  searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            /// Code for changing the textColor and hint color for the search view
            searchTextView.setHintTextColor(getResources().getColor(R.color.main_white));
            searchTextView.setTextColor(getResources().getColor(R.color.main_white));

            //Code for changing the voice search icon
            //ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            //voiceIcon.setImageResource(R.drawable.my_voice_search_icon);

            //Code for changing the close search icon
            ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            closeIcon.setColorFilter(Color.WHITE);
            /*closeIcon.setOnClickListener(view -> {

                searchTextView.setText("");
                //clear search text reset all
                doFilter("");
            });*/

            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.set(searchTextView, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
            } catch (Exception e) {
                e.printStackTrace();
            }


            //searchView.setOnQueryTextListener(FragmentVisitors.this);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if(!query.trim().isEmpty()){
                        //gone visibility
                        llNoData.setVisibility(View.GONE);
                        //1. clear arrayList
                        itemArrayList.clear();
                        tempArrayList.clear();
                        //remove all view from feed
                        ll_statGhpDetailsContentLayout.removeAllViews();

                        //2. reset call flag to 0 && Filter flag to 0
                        skip_count = 0;  //openFlag = 0;
                        current_page= 1;
                        filter_text = query;
                        showProgressBar();
                        //set refresh api
                        call_getGhpDetails();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    return false;
                }
            });

            searchView.setOnCloseListener(() -> {
                resumeFeedApi();
                return false;
            });
        }
        if (searchView != null)
        {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
