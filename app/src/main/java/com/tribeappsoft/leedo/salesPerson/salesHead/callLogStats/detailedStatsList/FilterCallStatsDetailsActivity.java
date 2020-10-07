package com.tribeappsoft.leedo.salesPerson.salesHead.callLogStats.detailedStatsList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callSchedule.model.ScheduledCallsModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.callLogStats.adapter.FilterScheduledCallsAdapter;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class FilterCallStatsDetailsActivity extends AppCompatActivity {

    @BindView(R.id.cl_callStatDetails) CoordinatorLayout parent;
    @BindView(R.id.sr_callStatDetails) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_callStatDetails) RecyclerView recyclerView;
    @BindView(R.id.ll_callStatDetails_search) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.edt_callStatDetails_search) TextInputEditText edt_search;
    @BindView(R.id.iv_callStatDetails_clearSearch) AppCompatImageView iv_clearSearch;

    @BindView(R.id.ll_callStatDetails_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.pb_callStatDetails) ContentLoadingProgressBar pb;
    @BindView(R.id.ll_callStatDetails_backToTop) LinearLayoutCompat ll_backToTop;

    @BindView(R.id.iv_itemLead_SiteVisitDetails_ec) AppCompatImageView ivExpandIcon;
    @BindView(R.id.ll_site_visit_details_filter) LinearLayoutCompat ll_showFilter;
    @BindView(R.id.mtv_full_name) MaterialTextView mtvFullName;
    @BindView(R.id.mtv_project_name) MaterialTextView mtvProjectName;
    @BindView(R.id.mtv_from_date) MaterialTextView mtvFromDate;
    @BindView(R.id.mtv_to_date) MaterialTextView mtvToDate;

    @BindView(R.id.ll_callStatsDetailsFilterMain) LinearLayoutCompat ll_callStatsDetailsFilterMain;
    @BindView(R.id.ll_callDetailsSalesNameLayout) LinearLayoutCompat ll_callDetailsSalesNameLayout;
    @BindView(R.id.ll_LeadDetailsProjectLayout) LinearLayoutCompat llLeadDetailsProjectLayout;
    @BindView(R.id.ll_LeadDetailsFromDateLayout) LinearLayoutCompat llLeadDetailsFromDateLayout;
    @BindView(R.id.ll_LeadDetailsToDateLayout) LinearLayoutCompat llLeadDetailsToDateLayout;
    @BindView(R.id.tv_itemLead_filter_count_status) MaterialTextView filterCountText;

    private String TAG = "FilterCallCompletedDetailsActivity", api_token="", filter_text = "", from_date="", to_date="";
    private ArrayList<ScheduledCallsModel> itemArrayList;
    private FilterScheduledCallsAdapter recyclerAdapter;
    private Activity context;
    private int current_page =1, last_page =1, filterCount = 0, project_id =0, schedule_status_id = 0,sales_person_id=0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isExpand=false, sales_team_lead_stats = false;
    private final Animations anim = new Animations();

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_call_stats);
        ButterKnife.bind(this);
        context = FilterCallStatsDetailsActivity.this;

        if(getIntent()!=null)
        {
            sales_team_lead_stats = getIntent().getBooleanExtra("sales_team_lead_stats", false);
            sales_person_id = getIntent().getIntExtra("sales_person_id", 0);
            schedule_status_id = getIntent().getIntExtra("schedule_status_id",0);
            project_id = getIntent().getIntExtra("project_id", 0);
            from_date = getIntent().getStringExtra("from_date");
            to_date = getIntent().getStringExtra("to_date");
            String sales_person_name = getIntent().getStringExtra("full_name");
            String project_name = getIntent().getStringExtra("Project_name");

           // filterCount = sales_person_id == 0 && (from_date ==null && to_date ==null) ? 1 : sales_person_id > 0 && (from_date != null && to_date !=null) ? 3 : 2;

            Log.e(TAG, "onCreate: filterCount"+filterCount);

            ll_callDetailsSalesNameLayout.setVisibility(sales_person_name == null || sales_person_name.trim().isEmpty() ? View.GONE :View.VISIBLE );
            llLeadDetailsProjectLayout.setVisibility(project_name == null || project_name.trim().isEmpty() ? View.GONE :View.VISIBLE );
            llLeadDetailsFromDateLayout.setVisibility(from_date == null || from_date.trim().isEmpty() ? View.GONE :View.VISIBLE );
            llLeadDetailsToDateLayout.setVisibility(to_date == null || to_date.trim().isEmpty() ? View.GONE :View.VISIBLE );

            if(sales_person_name !=null && !sales_person_name.trim().isEmpty())filterCount++;
            if(project_name !=null && !project_name.trim().isEmpty())filterCount++;
            if((from_date!=null && to_date !=null) && (!from_date.trim().isEmpty() && !to_date.trim().isEmpty() ))filterCount++;
            filterCountText.setText(String.format("%d%s", filterCount, getString(R.string.filter_applied)));

            mtvFullName.setText(sales_person_name !=null && !sales_person_name.trim().isEmpty() ? sales_person_name : "--");
            mtvProjectName.setText(project_name !=null && !project_name.trim().isEmpty() ? project_name : "--");
            mtvFromDate.setText(from_date!=null && !from_date.trim().isEmpty() ? Helper.formatDateFromString(from_date) : "--");
            mtvToDate.setText(to_date!=null && !to_date.trim().isEmpty() ? Helper.formatDateFromString(to_date) : "--");

        }

        if (getSupportActionBar() != null) {

            getSupportActionBar().setTitle(schedule_status_id == 1 ? getString(R.string.scheduled_calls) :  schedule_status_id == 2 ? getString(R.string.rescheduled_calls) : getString(R.string.cancelled_calls));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        init();
    }

    private void init()
    {
        //SharedPreference
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        //user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        //boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        //boolean isSalesTeamLead = sharedPreferences.getBoolean("isSalesTeamLead", false);
        itemArrayList = new ArrayList<>();

       /* // Compact Calendar
        Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dddd-MMMM-yyyy hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());*/

        //SetRecyclerView
        setupRecycleView();

        //set swipe Refresh
            setSwipeRefresh();

        //set up recyclerScroll
        setUpRecyclerScroll();

        //def call api
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = last_page = 1;

            swipeRefresh.setRefreshing(true);
            call_getAllCalls();
        }
        else {
            Helper.NetworkError(context);
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible no data
            ll_noDataFound.setVisibility(View.VISIBLE);
        }

        ll_callStatsDetailsFilterMain.setOnClickListener(v -> {
            //temp
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


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }

        });

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

    private void setUpRecyclerScroll()
    {
        //Use This For Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE)
                {

                    Log.d("-----","end");
                    //TODO temp gone for design issues
                    //gone back to top
                    if (ll_backToTop.getVisibility() == View.VISIBLE)
                    {
                        new Animations().slideOutBottom(ll_backToTop);
                        ll_backToTop.setVisibility(View.GONE);
                    }


                    //TODO Rohan 16-09-2019
                    if (!swipeRefresh.isRefreshing())
                    {
                        //if swipe refreshing is on means user has done swipe-refreshed
                        //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                        //to avoid this, Have added below api call within this block

                        Log.e(TAG, "onScrollStateChanged: current_page "+current_page );
                        if (current_page <= last_page)  //
                        {
                            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                call_getAllCalls();
                            } else Helper.NetworkError(Objects.requireNonNull(context));

                        } else Log.e(TAG, "Last page");
                    }
                }
                /*else
                {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }*/
            }

            int currentScrollPosition = 0;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentScrollPosition += dy;

                if (dy > 0) {

                    // Scrolling up
                    Log.d(TAG, "onScrolled: up" );
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down" );
                    ll_backToTop.setVisibility(View.VISIBLE);
                    new Animations().slideInBottom(ll_backToTop);
                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }
            }

        });
        //scroll to top
        ll_backToTop.setOnClickListener(v -> {
            Log.e(TAG, "Back TO TOP");
            recyclerView.smoothScrollToPosition(0);

            new Animations().slideOutBottom(ll_backToTop);
            ll_backToTop.setVisibility(View.GONE);

        });

    }


    private void hideViews() {
        ll_searchBar.animate().translationY(-ll_searchBar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        ll_searchBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }


    /*@Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: frag " );

        //perform search
        perform_search();

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            from_date = sharedPreferences.getString("sendFromDate","");
            to_date = sharedPreferences.getString("sendToDate", "");
            boolean callScheduleAdded = sharedPreferences.getBoolean("callSccallCompletedAddedheduleAdded", false);
            boolean callCompletedAdded = sharedPreferences.getBoolean("", false);
            editor.apply();

            if(isFilter) {

                *//*if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    user_id = sharedPreferences.getInt("sales_person_id",  sharedPreferences.getInt("user_id", 0));
                    filterCount = sharedPreferences.getInt("filterCount", 0);
                    editor.apply();
                }
                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n sales_person_id:- "+user_id+"\n from_date:- " +from_date+"\n to_date:- "+to_date);
*//*
                //reset api call
                resetApiCall();
            }
            else if (clearFilter) {

                //all filters are cleared

                Log.e(TAG, "onResume:clearFilter  ");
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("clearFilter");
                    editor.remove("project_id");
                    editor.remove("sales_person_id");
                    editor.remove("isFilter");
                    editor.remove("filterCount");
                    editor.remove("from_date");
                    editor.remove("to_date");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }

                //clear fields
                project_id = filterCount = 0;
                user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);
                from_date = to_date = "";
                //reset api call
                resetApiCall();
            }

            if(callScheduleAdded) {
                //refresh api call
                //call api
                swipeRefresh.setRefreshing(true);

                refreshApiCall();

                Log.e(TAG, "onResume:callScheduleAdded  ");

                //update flag to false
                if(sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("callScheduleAdded", false);
                    editor.apply();
                }
            }
            if(callCompletedAdded) {
                //refresh api call
                refreshApiCall();

                //update flag to false
                if(sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("callCompletedAdded", false);
                    editor.apply();
                }
            }
        }

    }*/


    //SetUpRecyclerView
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter= new FilterScheduledCallsAdapter(context, itemArrayList, true);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                  refreshApiCall();
            }
            else {

                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            }
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }


    //CallReminderAPI
    private void call_getAllCalls() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getFilterScheduledCallLeads(api_token, sales_person_id, "",from_date,to_date, current_page, project_id,schedule_status_id,filter_text, sales_team_lead_stats);
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
                    public void onCompleted() {
                        if (current_page ==2 ) delayRefresh();
                        else notifyRecyclerDataChange();
                    }
                    @Override
                    public void onError(final Throwable e) {

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
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject())
                                            {
                                                JsonObject jsonObject = JsonObjectResponse.body().get("data").getAsJsonObject();

                                                //set page number
                                                if (jsonObject.has("current_page")) current_page = !jsonObject.get("current_page").isJsonNull() ? jsonObject.get("current_page").getAsInt() : 0;
                                                if (jsonObject.has("last_page")) last_page = !jsonObject.get("last_page").isJsonNull() ? jsonObject.get("last_page").getAsInt() : 0;
                                                //if (JsonObjectResponse.body().has("per_page")) per_page = !JsonObjectResponse.body().get("per_page").isJsonNull() ? JsonObjectResponse.body().get("per_page").getAsInt() : 0;

                                                setScheduledCallJson(jsonObject.getAsJsonObject());
                                            }
                                        }
                                    }else Log.e(TAG, "Outside Data: ");
                                } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setScheduledCallJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }

    private void setJson(JsonObject jsonObject) {

        ScheduledCallsModel model = new ScheduledCallsModel();
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("call_schedule_id")) model.setCall_schedule_id(!jsonObject.get("call_schedule_id").isJsonNull() ? jsonObject.get("call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("prev_call_schedule_id")) model.setPrev_call_schedule_id(!jsonObject.get("prev_call_schedule_id").isJsonNull() ? jsonObject.get("prev_call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) model.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "--" );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "--" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("prefix")) model.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("lead_uid")) model.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        if (jsonObject.has("sales_person_name")) model.setSales_person_name(!jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "" );
        if (jsonObject.has("lead_types_name")) model.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "" );
        if (jsonObject.has("schedules_created_at")) model.setCreated_at(!jsonObject.get("schedules_created_at").isJsonNull() ? jsonObject.get("schedules_created_at").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("cp_name")) model.setCp_name(!jsonObject.get("cp_name").isJsonNull() ? jsonObject.get("cp_name").getAsString() : "" );
        if (jsonObject.has("cp_executive_name")) model.setCp_executive_name(!jsonObject.get("cp_executive_name").isJsonNull() ? jsonObject.get("cp_executive_name").getAsString() : "" );
        if (jsonObject.has("scheduledBy")) model.setSchedule_by(!jsonObject.get("scheduledBy").isJsonNull() ? jsonObject.get("scheduledBy").getAsString() : "" );
        if (jsonObject.has("scheduled_on")) model.setScheduled_on(!jsonObject.get("scheduled_on").isJsonNull() ? jsonObject.get("scheduled_on").getAsString() : "" );
        if (jsonObject.has("call_schedule_id")) model.setCall_schedule_id(!jsonObject.get("call_schedule_id").isJsonNull() ? jsonObject.get("call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("lead_status_id")) model.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0 );
        if (jsonObject.has("schedule_status_id")) model.setSchedule_status_id(!jsonObject.get("schedule_status_id").isJsonNull() ? jsonObject.get("schedule_status_id").getAsInt() : 0 );

        //if (jsonObject.has("status")) model.setScheduledOn(!jsonObject.get("status").isJsonNull() ? jsonObject.get("status").getAsString() : "" );
        //if (jsonObject.has("remark")) model.setRemark(!jsonObject.get("remark").isJsonNull() ? jsonObject.get("remark").getAsString() : "" );

        if (jsonObject.has("schedule_details")) {
            if (!jsonObject.get("schedule_details").isJsonNull() && jsonObject.get("schedule_details").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("schedule_details").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                model.setDetailsTitleModelArrayList(arrayList);
            }
        }
        itemArrayList.add(model);
    }

    private void setOtherInfoJson(JsonObject jsonObject, ArrayList<LeadDetailsTitleModel> arrayList) {

        LeadDetailsTitleModel model = new LeadDetailsTitleModel();
        if (jsonObject.has("section_title")) model.setLead_details_title(!jsonObject.get("section_title").isJsonNull() ? jsonObject.get("section_title").getAsString() : "");
        //if (jsonObject.has("section_item_desc")) model.setLead_details_title(!jsonObject.get("section_item_desc").isJsonNull() ? jsonObject.get("section_item_desc").getAsString() : "");

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

    //DelayRefresh

    public  void delayRefresh()
    {
        if (context != null) {
            Objects.requireNonNull(context).runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new FilterScheduledCallsAdapter(context, itemArrayList, true);
                recyclerView.setAdapter(recyclerAdapter);

                int count = recyclerAdapter.getItemCount();
                if (count == 0) {
                    //no calls
                    recyclerView.setVisibility(View.GONE);
                    ll_noDataFound.setVisibility(View.VISIBLE);
                    if(filterCount== 0){
                        ll_callStatsDetailsFilterMain.setVisibility(View.GONE);
                    }else {
                        ll_callStatsDetailsFilterMain.setVisibility(View.VISIBLE);
                    }
                    //exFab.setVisibility(View.GONE);
                } else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noDataFound.setVisibility(View.GONE);
                    if(filterCount== 0){
                        ll_callStatsDetailsFilterMain.setVisibility(View.GONE);
                    }else {
                        ll_callStatsDetailsFilterMain.setVisibility(View.VISIBLE);
                    }
                    //exFab.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                if (recyclerView.getAdapter()!=null)
                {
                    //recyclerView adapter
                    recyclerView.getAdapter().notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noDataFound.setVisibility(View.VISIBLE);
                        if(filterCount== 0){
                            ll_callStatsDetailsFilterMain.setVisibility(View.GONE);
                        }else {
                            ll_callStatsDetailsFilterMain.setVisibility(View.VISIBLE);
                        }
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noDataFound.setVisibility(View.GONE);
                        if(filterCount== 0){
                            ll_callStatsDetailsFilterMain.setVisibility(View.GONE);
                        }else {
                            ll_callStatsDetailsFilterMain.setVisibility(View.VISIBLE);
                        }
                        //exFab.setVisibility(View.VISIBLE);
                    }

                }

            });
        }
    }

   /* private void perform_search() {

        //or you can search by the editTextFiler

        //search ime action click
        edt_search.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (recyclerView.getAdapter() != null)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    // Get search text
                    String filterText  =  Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                    // regex to match any number of spaces
                    filterText = filterText.trim().replaceAll("\\s+", " ");
                    Log.e(TAG, "perform_search: filterText "+filterText);
                    doFilter(filterText);
                }

                return true;
            }
            return false;
        });


        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                //if (recyclerAdapter != null) {
                //  String text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                //  doFilter(text);

                if (Objects.requireNonNull(edt_search.getText()).length() < 1)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    iv_clearSearch.setVisibility(View.GONE);

                    //call reset api
                    //resetApiCall();

                } else {
                    //visible empty search ll
                    iv_clearSearch.setVisibility(View.VISIBLE);
                }
                //}
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                iv_clearSearch.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }
        });


        //clear searchText
        iv_clearSearch.setOnClickListener(v -> {
            edt_search.setText("");
            //call reset api
            resetApiCall();
        });
    }*/

    private void refreshApiCall()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. clear search text
            filter_text = "";
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
            //4. clear filters if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("isFilter");
                editor.remove("filterCount");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.putBoolean("clearFilter", false);
                editor.apply();
            }
            //clear fields
           // project_id = filterCount = 0;
            //user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);
           // from_date = to_date = "";

            //call api
            swipeRefresh.setRefreshing(true);
            call_getAllCalls();
        }
        else {
            Helper.NetworkError(context);
        }

    }

   /* private void resetApiCall()
    {
        if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Set search other_ids clear
            filter_text = "";
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
            //call get sales feed api
            swipeRefresh.setRefreshing(true);
            // showProgressBar();
            call_getAllCalls();

        } else NetworkError(context);

    }*/

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
                    doFilter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(newText.trim().isEmpty())
                        doFilter(newText);
                    return false;
                }
            });


            searchView.setOnCloseListener(() -> {
                doFilter("");
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



    private void doFilter(String query) {

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Get search text
            filter_text = query;

            swipeRefresh.setRefreshing(true);
            //doFilter(text);
            call_getAllCalls();
        }
        else Helper.NetworkError(Objects.requireNonNull(context));
    }


    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        pb.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        pb.setVisibility(View.GONE);
        //Objects.requireNonNull(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context,message);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            });
        }
    }


/*
    //SetupUI
    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(Objects.requireNonNull(context), view);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }*/


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
