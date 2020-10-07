package com.tribeappsoft.leedo.salesPerson.filterFeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callLog.CallLogActivity;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.AddFlatOnHoldActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.DirectHoldFlatsActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.FlatAllotmentActivity;
import com.tribeappsoft.leedo.salesPerson.models.FeedsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.stickyScrollView.StickyScrollView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class FeedFilterActivity extends AppCompatActivity {


    @BindView(R.id.sr_feedFilter) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.cl_feedFilter) CoordinatorLayout parent;
    @BindView(R.id.ll_feedFilter_main) LinearLayoutCompat ll_main;
    @BindView(R.id.nsv_feedFilter)
    StickyScrollView nsv;
    @BindView(R.id.ll_feedFilter_addFeedData) LinearLayoutCompat ll_addFeedData;
    @BindView(R.id.ll_feedFilter_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_feedFilter_backToTop) LinearLayoutCompat ll_backToTop;
    @BindView(R.id.ll_feedFilter_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.tv_filterFeed_noData) AppCompatTextView tv_noData;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<FeedsModel> modelArrayList;
    private ArrayList<LeadListModel> leadListModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<String> namePrefixArrayList, leadStageStringArrayList;
    private Dialog claimDialog;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private int openFlag = 0,user_id = 0,call = 0, lastPosition = -1, claimPosition =0,claimAPiCount =0,
            lead_id =0, skip_count =0, call_lead_id =0, call_lead_status_id = 0;
    private String TAG = "FeedFilterActivity", api_token = "", filter_text="", other_ids ="",
            display_text ="", last_lead_updated_at = null, customer_mobile = null,call_cuID= null;

    private boolean stopApiCall = false,  isClaimNow = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_filter);
        ButterKnife.bind(this);
        context = FeedFilterActivity.this;

        if(getIntent()!=null)
        {
            openFlag = getIntent().getIntExtra("openFlag", 0);
            other_ids = getIntent().getStringExtra("other_ids");
            display_text = getIntent().getStringExtra("display_text");
            Log.e(TAG, "onCreateView: openFlag "+ openFlag );
            Log.e(TAG, "init: otherIds "+ other_ids);
        }

        //call method to hide keyBoard
        setupUI(parent);

        //hide pb
        hideCancellationProgressBar();

        //initialise contents
        init();

        //set up scrollView
        setUpScrollView();

    }

    private void init()
    {
        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //init arrayLists
        modelArrayList = new ArrayList<>();
        leadListModelArrayList = new ArrayList<>();
        leadStagesModelArrayList = new ArrayList<>();
        leadStageStringArrayList = new ArrayList<>();
        namePrefixArrayList = new ArrayList<>();
        namePrefixArrayList.add("Mr.");
        namePrefixArrayList.add("Ms.");
        namePrefixArrayList.add("Mrs.");
        namePrefixArrayList.add(".");

        //set up swipeRefresh
        setSwipeRefresh();

        //set up filter
        setUpFilter();

        //call get lead data to get lead stages
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) getLeadData();

       /* //close filter
        iv_closeFilter.setOnClickListener(v -> {

            //same action as swipeRefresh
            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //set refresh api
                refreshFeedApi();

            }
            else NetworkError(context);
        });*/
    }


    private void setUpScrollView() {

        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

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

                    //if swipe refreshing is on means user has done swipe-refreshed
                    //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                    //to avoid this, Have added below api call within this block

                    //gone back to top
                    if (ll_backToTop.getVisibility() == View.VISIBLE)
                    {
                        new Animations().slideOutBottom(ll_backToTop);
                        ll_backToTop.setVisibility(View.GONE);
                    }

                    //swipeRefresh.setRefreshing(true);
                    showProgressBar();

                    Log.e(TAG, "onScrollStateChanged: call " + call);
                    if (!stopApiCall)  //call paginate api till ary is not empty
                    {
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                            //call get sales feed
                            call_getSalesFeed();
                            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

                        } else Helper.NetworkError(Objects.requireNonNull(context));
                    } else {
                        Log.e(TAG, "stopApiCall");
                        hideProgressBar();
                    }
                }
            }

//            if (!nsv.canScrollVertically(1))
//            {
//                // bottom of scroll view
//                new Helper().showCustomToast(context, "Bottom");
//            }


            //scrolling up and down
            if(scrollY<oldScrollY){
                //vertical scrolling down
                Log.d(TAG, "onCreateView: scrolling down" );

                if (call > 1 && ll_backToTop.getVisibility() == View.GONE ) {

                    if (modelArrayList.size()>3)
                    {
                        Log.d(TAG, "setUpScrollView: more than 3 cards");
                        ll_backToTop.setVisibility(View.VISIBLE);
                        new Animations().slideInBottom(ll_backToTop);
                    }
                }


            }else{

                //vertical scrolling upp
                Log.d(TAG, "onCreateView: scrolling up" );

                if (call > 1 && ll_backToTop.getVisibility() == View.VISIBLE ) {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }

            }

            //reached at top of scrollView
            if (!nsv.canScrollVertically(-1)) {

                Log.d(TAG, "onCreateView: TOP of scrollView" );
                // top of scroll view
                if (ll_backToTop.getVisibility() == View.VISIBLE)
                {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }
            }
        });


        //scroll to top
        ll_backToTop.setOnClickListener(v -> {

            //LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            //Objects.requireNonNull(layoutManager).scrollToPositionWithOffset(0, 0);

            //recyclerView.smoothScrollToPosition(0);
            nsv.smoothScrollTo(0, 0);

            new Animations().slideOutBottom(ll_backToTop);
            ll_backToTop.setVisibility(View.GONE);
        });
    }

    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() ->
        {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //set refreshing true
                swipeRefresh.setRefreshing(true);

                //set refresh api
                refreshFeedApi();
            }
            else Helper.NetworkError(context);
        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


    private void setUpFilter()
    {
        if (openFlag == 1) {

            //leads

            //Set Data For Leads Added
            setUpActionBar(String.format("%s leads", display_text));

            //search for leads
            filterFeedApi();
        }
        else if (openFlag == 2) {

            //site visits

            //Set Data For Site Visits
            setUpActionBar(String.format("%s site visit", display_text));

            //search for ghp generated
            filterFeedApi();
        }
        else if (openFlag == 3) {

            //ghp

            //Set Data For GHP generated
            setUpActionBar(String.format("%s ghp", display_text));

            //search for ghp generated
            filterFeedApi();
        }
        else if (openFlag == 4) {

            //on hold

            setUpActionBar(String.format("%s hold units", display_text));

            //filterFeedApi(other_ids + " hold flats");
            filterFeedApi();
        }
        else if (openFlag == 5) {

            //bookings


            //Set Data For Bookings
            setUpActionBar(String.format("%s allotted units", display_text));

            //filterFeedApi(other_ids + " booked flats");
            filterFeedApi();
        }
        else {

            Log.e(TAG, "onResume: onStopped called" );
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //gone visibility
                ll_noData.setVisibility(View.GONE);

                setUpActionBar("All Leads");

                //resume feed api
                resumeFeedApi();
            }
            else
            {
                tv_noData.setText(getString(R.string.no_feeds));
                ll_noData.setVisibility(View.VISIBLE);
                ll_addFeedData.setVisibility(View.GONE);
                ll_loadingContent.setVisibility(View.GONE);
                Helper.NetworkError(context);
            }

        }
    }

    private void setUpActionBar(String title)
    {
        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(title);

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }


    private void refreshFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //1. clear arrayList
            modelArrayList.clear();
            //remove all view from feed
            //ll_addFeedData.removeAllViews();

            //2. reset call flag to 0 && Filter flag to 0
            call = 0;  //openFlag =0;
            //3. Set search other_ids clear
            filter_text = "";
            //4. clear other ids & display text
            //other_ids = display_text = "";

            last_lead_updated_at = null;
            //call get sales feed api
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);
        } else Helper.NetworkError(context);

    }

    private void resumeFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset call flag to 0
            call = 0;
            //3. Get search other_ids
            filter_text = "";
            last_lead_updated_at = null;

            swipeRefresh.setRefreshing(true);
            //show progress bar
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);

        }else Helper.NetworkError(context);
    }

    private void filterFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset call flag to 0
            call = 0;
            //3. Get filter other_ids
            filter_text = ""; //text;
            last_lead_updated_at = null;

            swipeRefresh.setRefreshing(true);

            //show progress bar
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);

        }else Helper.NetworkError(context);
    }

    private void resetFeedApiWithDelay()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //Clear Search --> reset all params
            //1. clear arrayList
            modelArrayList.clear();
            //ll_addFeedData.removeAllViews();
            //2. reset page flag to 1
            call = 0;
            //3. Set search other_ids clear
            filter_text = "";
            //4. show refreshing and progress bar
            swipeRefresh.setRefreshing(true);
            last_lead_updated_at = null;

            showProgressBar();
            //5. call get sales feed api
            new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 500);
        }
        else Helper.NetworkError(Objects.requireNonNull(context));
    }

    private void getLeadData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadFormData(api_token);
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
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        //setLeadDetails();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLogUpdateLead(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLogUpdateLead(getString(R.string.weak_connection));
                        else showErrorLogUpdateLead(e.toString());
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
                                                setGetLeadDataJson(jsonObject);
                                            }
                                        }
                                    }
                                    else showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
                                case 404:
                                    showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLogUpdateLead(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLogUpdateLead(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });
    }

    private void setGetLeadDataJson(JsonObject jsonObject) {
        // get lead stages array
        if (jsonObject.has("lead_stages"))
        {
            if (!jsonObject.get("lead_stages").isJsonNull() && jsonObject.get("lead_stages").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_stages").getAsJsonArray();
                //clear list
                leadStagesModelArrayList.clear();
                leadStageStringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setLeadStagesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }
    private void setLeadStagesJson(JsonObject jsonObject) {
        LeadStagesModel myModel = new LeadStagesModel();
        if (jsonObject.has("lead_stage_id")) myModel.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage_name")) {
            myModel.setLead_stage_name(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
            leadStageStringArrayList.add(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
        }

        leadStagesModelArrayList.add(myModel);
    }



    private void call_getSalesFeed() {
        ApiClient client = ApiClient.getInstance();
        int limit = 6;
        skip_count = call * limit;
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getSalesFeed(api_token, user_id, limit, call * limit, filter_text, other_ids, last_lead_updated_at);
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonObjectResponse -> {
            throw new IllegalStateException("doOnNextException");
        });
        responseObservable.doOnError(throwable -> {
            throw new UnsupportedOperationException("onError exception");
        });
        responseObservable.subscribeOn(Schedulers.io())
                .asObservable()
                .subscribe(new Subscriber<Response<JsonObject>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");

                        if (call == 1) setFeeds();
                        else setUpdateFeed();

                        //if (call ==1 ) delayRefresh();
                        //else notifyRecyclerDataChange();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) setFeedJson(JsonObjectResponse.body());
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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


    private void setFeedJson(JsonObject jsonObject) {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                //increment call
                call = call + 1;
                //get feed data ary
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                //set stop api call if data ary is empty
                //stopApiCall = jsonArray.size() == 0;
                stopApiCall = jsonArray.size() == 0 || jsonArray.size()<6;
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                // cached data of 1st skip count
                /*if (call==1)
                {
                    //put an array into SharedPref
                    if (sharedPreferences!=null) {
                        editor = sharedPreferences.edit();
                        editor.putString("jA_feeds", jsonArray.toString());
                        editor.apply();
                    }
                }*/

            } else stopApiCall = true;
            //stop api call when data ary is null
        }
    }

    private void setJson(JsonObject jsonObject) {

        FeedsModel model = new FeedsModel();

        if (jsonObject.has("feed_type_id")) model.setFeed_type_id(!jsonObject.get("feed_type_id").isJsonNull() ? jsonObject.get("feed_type_id").getAsInt() : 0);
        if (jsonObject.has("tag")) model.setTag(!jsonObject.get("tag").isJsonNull() ? jsonObject.get("tag").getAsString() : "");
        if (jsonObject.has("tag_date")) model.setTag_date(!jsonObject.get("tag_date").isJsonNull() ? jsonObject.get("tag_date").getAsString() : "");
        if (jsonObject.has("tag_elapsed_time")) model.setTag_elapsed_time(!jsonObject.get("tag_elapsed_time").isJsonNull() ? jsonObject.get("tag_elapsed_time").getAsString() : "");
        if (jsonObject.has("small_header_title")) model.setSmall_header_title(!jsonObject.get("small_header_title").isJsonNull() ? jsonObject.get("small_header_title").getAsString() : "");
        if (jsonObject.has("main_title")) model.setMain_title(!jsonObject.get("main_title").isJsonNull() ? jsonObject.get("main_title").getAsString() : "");
        if (jsonObject.has("description")) model.setDescription(!jsonObject.get("description").isJsonNull() ? jsonObject.get("description").getAsString() : "");
        if (jsonObject.has("status_text")) model.setStatus_text(!jsonObject.get("status_text").isJsonNull() ? jsonObject.get("status_text").getAsString() : "");
        if (jsonObject.has("status_sub_text")) model.setStatus_sub_text(!jsonObject.get("status_sub_text").isJsonNull() ? jsonObject.get("status_sub_text").getAsString() : "");
        if (jsonObject.has("status_timestamp")) model.setStatus_timestamp(!jsonObject.get("status_timestamp").isJsonNull() ? jsonObject.get("status_timestamp").getAsString() : "");

        //call actions
        if (jsonObject.has("actions")) {
            if (!jsonObject.get("actions").isJsonNull() && jsonObject.get("actions").isJsonObject()) {
                JsonObject object = jsonObject.get("actions").getAsJsonObject();
                if (object.has("call"))
                    model.setCall(!object.get("call").isJsonNull() ? object.get("call").getAsString() : "");
            }
        }

        if (jsonObject.has("other_info")) {
            if (!jsonObject.get("other_info").isJsonNull() && jsonObject.get("other_info").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("other_info").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                model.setDetailsTitleModelArrayList(arrayList);
            }
        }

        //lead id
        if (jsonObject.has("ids"))
        {
            if (!jsonObject.get("ids").isJsonNull() && jsonObject.get("ids").isJsonObject())
            {
                JsonObject object = jsonObject.get("ids").getAsJsonObject();
                if (object.has("lead_id")) model.setLead_id(!object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0);
                //if (object.has("lead_id")) last_lead_id = !object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0;
                if (object.has("lead_status_id"))model.setLead_status_id(!object.get("lead_status_id").isJsonNull() ? object.get("lead_status_id").getAsInt() : 0);
                if (object.has("booking_id"))model.setBooking_id(!object.get("booking_id").isJsonNull() ? object.get("booking_id").getAsInt() : 0);

                CUIDModel cuidModel = new CUIDModel();
                if (object.has("lead_id"))cuidModel.setLead_id(!object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0);
                if (object.has("updated_at"))last_lead_updated_at = !object.get("updated_at").isJsonNull() ? object.get("updated_at").getAsString() : null;
                if (object.has("lead_uid"))cuidModel.setCu_id(!object.get("lead_uid").isJsonNull() ? object.get("lead_uid").getAsString() : "");
                if (object.has("country_code"))cuidModel.setCountry_code(!object.get("country_code").isJsonNull() ? object.get("country_code").getAsString() : "");
                if (object.has("mobile_number"))cuidModel.setCustomer_mobile(!object.get("mobile_number").isJsonNull() ? object.get("mobile_number").getAsString() : "");
                if (object.has("email"))cuidModel.setCustomer_email(!object.get("email").isJsonNull() ? object.get("email").getAsString() : "");
                if (object.has("prefix"))cuidModel.setPrefix(!object.get("prefix").isJsonNull() ? object.get("prefix").getAsString() : "");
                if (object.has("first_name"))cuidModel.setFirst_name(!object.get("first_name").isJsonNull() ? object.get("first_name").getAsString() : "");
                if (object.has("middle_name"))cuidModel.setMiddle_name(!object.get("middle_name").isJsonNull() ? object.get("middle_name").getAsString() : "");
                if (object.has("last_name"))cuidModel.setLast_name(!object.get("last_name").isJsonNull() ? object.get("last_name").getAsString() : "");
                if (object.has("full_name"))cuidModel.setCustomer_name(!object.get("full_name").isJsonNull() ? object.get("full_name").getAsString() : "");
                if (object.has("is_kyc_uploaded"))cuidModel.setIs_kyc_uploaded(!object.get("is_kyc_uploaded").isJsonNull() ? object.get("is_kyc_uploaded").getAsInt() : 0);
                if (object.has("is_reminder"))cuidModel.setIs_reminder_set(!object.get("is_reminder").isJsonNull() ? object.get("is_reminder").getAsInt() : 0);
                if (object.has("lead_types_id"))cuidModel.setLead_types_id(!object.get("lead_types_id").isJsonNull() ? object.get("lead_types_id").getAsInt() : 0);
                //if (object.has("lead_types_name"))cuidModel.setLe(!object.get("lead_types_name").isJsonNull() ? object.get("lead_types_name").getAsString() :"");
                if (object.has("lead_status_id"))cuidModel.setLead_status_id(!object.get("lead_status_id").isJsonNull() ? object.get("lead_status_id").getAsInt() : 0);
                if (object.has("lead_status_name"))cuidModel.setLead_status_name(!object.get("lead_status_name").isJsonNull() ? object.get("lead_status_name").getAsString() :"");
                if (object.has("token_media_path"))cuidModel.setToken_media_path(!object.get("token_media_path").isJsonNull() ? object.get("token_media_path").getAsString() :"");
                if (object.has("token_id"))cuidModel.setToken_id(!object.get("token_id").isJsonNull() ? object.get("token_id").getAsInt() : 0 );
                if (object.has("token_no"))cuidModel.setToken_no(!object.get("token_no").isJsonNull() ? object.get("token_no").getAsString() :"");
                if (object.has("project_id"))cuidModel.setProject_id(!object.get("project_id").isJsonNull() ? object.get("project_id").getAsInt() : 0);
                if (object.has("project_name"))cuidModel.setCustomer_project_name(!object.get("project_name").isJsonNull() ? object.get("project_name").getAsString() :"");
                if (object.has("event_title"))cuidModel.setEventName(!object.get("event_title").isJsonNull() ? object.get("event_title").getAsString() :"");
                if (object.has("event_id"))cuidModel.setEvent_id(!object.get("event_id").isJsonNull() ? object.get("event_id").getAsInt() :0);
                if (object.has("token_type_id"))cuidModel.setToken_type_id(!object.get("token_type_id").isJsonNull() ? object.get("token_type_id").getAsInt() :0);
                if (object.has("token_type"))cuidModel.setToken_type(!object.get("token_type").isJsonNull() ? object.get("token_type").getAsString() :"");
                if (object.has("ghp_date"))cuidModel.setGhp_date(!object.get("ghp_date").isJsonNull() ? object.get("ghp_date").getAsString() :"");
                if (object.has("ghp_amount"))cuidModel.setGhp_amount(!object.get("ghp_amount").isJsonNull() ? object.get("ghp_amount").getAsString() :"");
                if (object.has("ghp_plus_date"))cuidModel.setGhp_plus_date(!object.get("ghp_plus_date").isJsonNull() ? object.get("ghp_plus_date").getAsString() :"");
                if (object.has("ghp_plus_amount"))cuidModel.setGhp_plus_amount(!object.get("ghp_plus_amount").isJsonNull() ? object.get("ghp_plus_amount").getAsString() :"");
                if (object.has("payment_link"))cuidModel.setPayment_link(!object.get("payment_link").isJsonNull() ? object.get("payment_link").getAsString() :"");
                if (object.has("payment_invoice_id"))cuidModel.setPayment_invoice_id(!object.get("payment_invoice_id").isJsonNull() ? object.get("payment_invoice_id").getAsString() :"");

                if (object.has("unit_hold_release_id")) model.setUnit_hold_release_id(!object.get("unit_hold_release_id").isJsonNull() ? object.get("unit_hold_release_id").getAsInt() : 0 );
                if (object.has("unit_id")) model.setUnit_id(!object.get("unit_id").isJsonNull() ? object.get("unit_id").getAsInt() : 0 );
                if (object.has("unit_name")) model.setUnit_name(!object.get("unit_name").isJsonNull() ? object.get("unit_name").getAsString() : "" );
                if (object.has("floor_id")) model.setFloor_id(!object.get("floor_id").isJsonNull() ? object.get("floor_id").getAsInt() : 0 );
                if (object.has("block_id")) model.setBlock_id(!object.get("block_id").isJsonNull() ? object.get("block_id").getAsInt() : 0 );

                if (object.has("lead_stage_id")) cuidModel.setLead_stage_id(!object.get("lead_stage_id").isJsonNull() ? object.get("lead_stage_id").getAsInt() : 0 );
                if (object.has("lead_stage"))cuidModel.setLead_stage_name(!object.get("lead_stage").isJsonNull() ? object.get("lead_stage").getAsString() :"");
                if (object.has("remark"))cuidModel.setGhp_remark(!object.get("remark").isJsonNull() ? object.get("remark").getAsString() :"");
                //if (object.has("remark"))remarks=!object.get("remark").isJsonNull() ? object.get("remark").getAsString() :"";


                //lead_status_id==5 && site visit count > 1
                if(cuidModel.getLead_status_id()==5 && cuidModel.getSite_visit_count()>1) {
                    model.setStatus_text("Site Revisited");
                }

                // Ghp generated and token type id 3 == ghp+
                if(cuidModel.getLead_status_id()==6 && cuidModel.getToken_type_id()==3) {
                    model.setStatus_text("GHP+ Generated");
                }

                model.setCuidModel(cuidModel);
            }
        }

        //add model
        modelArrayList.add(model);
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

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addFeedData.removeAllViews();
                    for (int i = 0; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    ll_addFeedData.setVisibility(View.VISIBLE);
                    //visible main
                    ll_main.setVisibility(View.VISIBLE);

                    //set scrollView scroll to top
                    nsv.smoothScrollTo(0, 0);

                } else {
                    //empty feed

                    //set no data msg
                    tv_noData.setText(openFlag ==1 ? getString(R.string.no_leads)
                            : openFlag == 2 ?  getString(R.string.no_site_visits_available)
                            : openFlag == 3 ?  getString(R.string.no_ghp_available)
                            : openFlag == 4 ?  getString(R.string.no_on_holds_available)
                            : openFlag == 5 ?  getString(R.string.no_allotments_available)
                            : getString(R.string.no_feeds)
                    );

                    ll_noData.setVisibility(View.VISIBLE);
                    ll_main.setVisibility(View.GONE);
                    ll_addFeedData.setVisibility(View.GONE);
                }


                /*if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
                    editor.apply();
                    Log.e(TAG, "onCreate: applicationCreated " + applicationCreated );

                    //call methods
                    //get claim now leads only when an application is created
                    if (applicationCreated)
                    {
                        //if (isNetworkAvailable(Objects.requireNonNull(context))) call_getUnClaimedLeads();
                        //else NetworkError(context);

                        //start claimNow activity
                        new Handler(getMainLooper()).postDelayed(() -> startActivity(new Intent(context, ClaimNowActivity.class)
                                .putExtra("page", "unClaimedLead")
                                .putExtra("notifyFeeds", true)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), 0);
                    }
                }*/

                //hide pb when stop api call
                if (stopApiCall || modelArrayList.size()<=2) hideProgressBar();

                //also hide pb if arrayList is having less than 2 records

            });
        }
    }

    private void setUpdateFeed() {
        if (context != null) {
            context.runOnUiThread(() -> {

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    Log.e(TAG, "setUpdateFeed: updateCount "+skip_count );
                    Log.e(TAG, "setUpdateFeed: size "+ modelArrayList.size() );

                    //ll_addFeedData.removeAllViews();
                    for (int i = skip_count; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    ll_addFeedData.setVisibility(View.VISIBLE);
                    //visible main
                    ll_main.setVisibility(View.VISIBLE);

                } else {
                    //empty feed

                    //set no data msg
                    tv_noData.setText(openFlag ==1 ? getString(R.string.no_leads)
                            : openFlag == 2 ?  getString(R.string.no_site_visits_available)
                            : openFlag == 3 ?  getString(R.string.no_ghp_available)
                            : openFlag == 4 ?  getString(R.string.no_on_holds_available)
                            : openFlag == 5 ?  getString(R.string.no_allotments_available)
                            : getString(R.string.no_feeds)
                    );

                    ll_noData.setVisibility(View.VISIBLE);
                    ll_main.setVisibility(View.GONE);
                    ll_addFeedData.setVisibility(View.GONE);
                }

                swipeRefresh.setRefreshing(false);
                //hide pb when stop api call
                if (stopApiCall) {
                    hideProgressBar();
                    //new Helper().showSuccessCustomToast(context, "Last page!");
                }

//                //visible back to top
//                new Handler().postDelayed(() -> {
//                    if (call > 3) {
//                        ll_backToTop.setVisibility(View.VISIBLE);
//                        new Animations().slideInBottom(ll_backToTop);
//                    }
//                }, 1500);


            });
        }
    }


    private View getFeedsView(int position) {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_home_feeds, null);

        LinearLayoutCompat ll_main = rowView.findViewById(R.id.ll_itemHomeFeed_main);
        setAnimation(ll_main, position);

        //own feed views
        LinearLayoutCompat ll_own_view = rowView.findViewById(R.id.ll_homeFeed_ownView);
        LinearLayoutCompat ll_own_main = rowView.findViewById(R.id.ll_homeFeed_ownMain);
        AppCompatTextView tv_own_date = rowView.findViewById(R.id.tv_homeFeed_ownDate);
        AppCompatImageView iv_own_tagIcon = rowView.findViewById(R.id.iv_homeFeed_ownTagIcon);
        AppCompatTextView tv_own_tag = rowView.findViewById(R.id.tv_homeFeed_ownTag);
        AppCompatTextView tv_own_elapsedTime = rowView.findViewById(R.id.tv_homeFeed_ownElapsedTime);
        AppCompatTextView tv_own_cuIdNumber = rowView.findViewById(R.id.tv_homeFeed_ownCuIdNumber);
        AppCompatImageView iv_ownReminderIcon = rowView.findViewById(R.id.iv_homeFeed_ownReminderIcon);
        AppCompatTextView tv_own_Lead_name = rowView.findViewById(R.id.tv_homeFeed_ownLeadName);
        AppCompatImageView iv_editOwnLeadName = rowView.findViewById(R.id.iv_homeFeed_update_ownLeadName);
        AppCompatTextView tv_own_projectName = rowView.findViewById(R.id.tv_homeFeed_ownProjectName);
        AppCompatTextView tv_ownLeadStage = rowView.findViewById(R.id.tv_homeFeed_ownLeadStage);
        AppCompatTextView tv_ownLeadStage_dot = rowView.findViewById(R.id.tv_homeFeed_ownLeadStage_dot);
        LinearLayoutCompat ll_leadStage_dot = rowView.findViewById(R.id.ll_leadStage_dot);
        AppCompatImageView iv_ownLeadWhatsApp = rowView.findViewById(R.id.iv_homeFeed_ownLeadWhatsApp);
        AppCompatImageView iv_own_Lead_call = rowView.findViewById(R.id.iv_homeFeed_ownLeadCall);
        AppCompatImageView iv_own_leadOptions = rowView.findViewById(R.id.iv_homeFeed_ownLeadOptions);
        AppCompatTextView tv_own_status = rowView.findViewById(R.id.tv_homeFeed_ownStatus);
      //  AppCompatTextView tv_own_token_number = rowView.findViewById(R.id.tv_homeFeed_ownTokenNumber);
        //LinearLayoutCompat ll_own_leadDetailsMain = rowView.findViewById(R.id.ll_HomeFeed_own_leadDetailsMain);
        AppCompatImageView iv_own_leadDetails_ec = rowView.findViewById(R.id.iv_homeFeed_own_leadDetails_ec);
        LinearLayoutCompat ll_own_viewLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownViewLeadDetails);
        LinearLayoutCompat ll_own_addLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownAddLeadDetails);

/*
        //other feed views
        LinearLayoutCompat ll_others_main = rowView.findViewById(R.id.ll_homeFeed_othersMain);
        LinearLayoutCompat ll_others_view = rowView.findViewById(R.id.ll_homeFeed_othersView);
        AppCompatTextView tv_others_date = rowView.findViewById(R.id.tv_homeFeed_othersDate);
        AppCompatImageView iv_others_tagIcon = rowView.findViewById(R.id.iv_homeFeed_othersTagIcon);
        AppCompatTextView tv_others_tag = rowView.findViewById(R.id.tv_homeFeed_othersTag);
        AppCompatTextView tv_others_elapsedTime = rowView.findViewById(R.id.tv_homeFeed_othersElapsedTime);
        AppCompatImageView iv_othersReminderIcon = rowView.findViewById(R.id.iv_homeFeed_othersReminderIcon);
        AppCompatTextView tv_others_cuIdNumber = rowView.findViewById(R.id.tv_homeFeed_othersCuIdNumber);
        AppCompatTextView tv_others_Lead_name = rowView.findViewById(R.id.tv_homeFeed_othersLeadName);
        AppCompatTextView tv_others_projectName = rowView.findViewById(R.id.tv_homeFeed_othersProjectName);
        AppCompatTextView tv_othersLeadStage = rowView.findViewById(R.id.tv_homeFeed_othersLeadStage);
        AppCompatTextView tv_othersLeadStage_dot = rowView.findViewById(R.id.tv_homeFeed_othersLeadStage_dot);
        LinearLayoutCompat ll_othersLeadStage_dot = rowView.findViewById(R.id.ll_homeFeed_othersLeadStage_dot);
        AppCompatTextView tv_others_Lead_status = rowView.findViewById(R.id.tv_homeFeed_othersLeadStatus);
        AppCompatTextView tv_others_token_number = rowView.findViewById(R.id.tv_homeFeed_othersTokenNumber);
        AppCompatImageView iv_othersLeadWhatsApp = rowView.findViewById(R.id.iv_homeFeed_othersLeadWhatsApp);
        AppCompatImageView iv_others_Lead_call = rowView.findViewById(R.id.iv_homeFeedOthersLeadCall);
        AppCompatImageView iv_others_leadOptions = rowView.findViewById(R.id.iv_homeFeed_othersLeadOptions);
        MaterialButton mBtn_others_claimNow = rowView.findViewById(R.id.mBtn_homeFeed_othersClaimNow);
        AppCompatImageView iv_others_leadDetails_ec = rowView.findViewById(R.id.iv_homeFeed_others_leadDetails_ec);
        LinearLayoutCompat ll_others_viewLeadDetails = rowView.findViewById(R.id.ll_homeFeed_othersViewLeadDetails);
        LinearLayoutCompat ll_others_addLeadDetails = rowView.findViewById(R.id.ll_homeFeed_othersAddLeadDetails);
        // LinearLayoutCompat ll_others_leadDetailsMain = rowView.findViewById(R.id.ll_HomeFeed_Others_leadDetailsMain);
        // LinearLayoutCompat ll_others_elapsed_time = rowView.findViewById(R.id.ll_tag_others_elapsed_time);*/

        final FeedsModel myModel = modelArrayList.get(position);
        if (myModel.getFeed_type_id() == 1) {}
            //Own View

            //tag date
            tv_own_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
            //tag icon
            iv_own_tagIcon.setImageResource(R.drawable.ic_tag_general);
            //tag other_ids
            tv_own_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? myModel.getTag() : "");
            //elapsed time
            tv_own_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
            //cu_id number
            tv_own_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty() ? myModel.getSmall_header_title() : "");
            //lead name
            tv_own_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty() ? myModel.getMain_title() : "");
            //project name
            tv_own_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty() ? myModel.getDescription() : "");
            //lead stage name
            tv_ownLeadStage.setText(myModel.getCuidModel() != null && myModel.getCuidModel().getLead_stage_name()!=null ? myModel.getCuidModel().getLead_stage_name() : "");
            tv_ownLeadStage_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            Log.e(TAG, "getFeedsView Filter: ll_leadStage_dot"+ll_leadStage_dot );
            ll_leadStage_dot.setVisibility(myModel.getCuidModel().getLead_stage_id()==0 ? View.GONE :View.VISIBLE);

            //status
            tv_own_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
            //token number/sub status other_ids
           // tv_own_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
            //mobile number/call
            iv_own_Lead_call.setOnClickListener(v -> {
                if (myModel.getCall()!=null) {
                    //get the customer mobile number
                    customer_mobile = myModel.getCall();
                    //get lead id
                    call_lead_id = myModel.getLead_id();
                    //get lead status id
                    call_lead_status_id = myModel.getLead_status_id();
                    //get cuId
                    call_cuID = myModel.getSmall_header_title();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkCallPermissions()) prepareToMakePhoneCall();
                        else requestPermissionCall();
                    }
                    else prepareToMakePhoneCall();

                    //new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getCall());
                }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
            });

            //whatsApp
            iv_ownLeadWhatsApp.setOnClickListener(v -> {
                if (myModel.getCall()!=null) {
                    //send Message to WhatsApp Number
                    sendMessageToWhatsApp(myModel.getCall(), myModel.getMain_title());
                }
                else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
            });


            switch (myModel.getCuidModel().getLead_stage_id()) {
                case 1:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorhot));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                    break;
                case 2:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    break;
                case 3:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorcold));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                    break;
                case 4:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorni));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                    break;
                default:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }



            //set own popup menu's
            iv_own_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_own_leadOptions, myModel));

            if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0) {

                iv_own_leadDetails_ec.setVisibility(View.VISIBLE);
                //Set Lead Details
                ll_own_addLeadDetails.removeAllViews();
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
                    ll_own_addLeadDetails.addView(rowView_sub);
                }
            } else iv_own_leadDetails_ec.setVisibility(View.GONE);


            //set expand Collapse Own
            iv_own_leadDetails_ec.setOnClickListener(view -> {

                if (myModel.isExpandedOwnView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(iv_own_leadDetails_ec, false);
                    collapse(ll_own_viewLeadDetails);
                    myModel.setExpandedOwnView(false);
                } else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(iv_own_leadDetails_ec, true);
                    expandSubView(ll_own_viewLeadDetails);
                    myModel.setExpandedOwnView(true);
                }
            });

            ll_own_main.setOnClickListener(view -> {

                if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
                {
                    if (myModel.isExpandedOwnView())  //expanded
                    {
                        // //do collapse View
                        new Animations().toggleRotate(iv_own_leadDetails_ec, false);
                        collapse(ll_own_viewLeadDetails);
                        myModel.setExpandedOwnView(false);
                    } else    // collapsed
                    {
                        //do expand view
                        new Animations().toggleRotate(iv_own_leadDetails_ec, true);
                        expandSubView(ll_own_viewLeadDetails);
                        myModel.setExpandedOwnView(true);
                    }
                }
            });


            //set visibility
            iv_own_leadOptions.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_ownLeadWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_own_Lead_call.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);

            if (myModel.getCuidModel()!=null) iv_ownReminderIcon.setVisibility(myModel.getCuidModel().getIs_reminder_set() == 0 ? View.GONE : View.VISIBLE);

            //unclaimed
            if (myModel.getLead_status_id() == 1) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_unclaimed));
            // lead claimed
            if (myModel.getLead_status_id() == 2)  tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
            // lead assigned
            if (myModel.getLead_status_id() == 3)  tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
            //self/ lead added
            if (myModel.getLead_status_id() == 4) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
            //site visited
            if (myModel.getLead_status_id() == 5) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
            //token /GHP  generated
            if (myModel.getLead_status_id() == 6) {

                //token /upgraded with GHP Plus
                if(myModel.getCuidModel().getToken_type_id()==3) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_plus_generated));
                else tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));
            }

            //token /GHP  cancelled
            if (myModel.getLead_status_id() == 7) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //on hold
            if (myModel.getLead_status_id() == 8) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
            //booked
            if (myModel.getLead_status_id() == 9) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));
            //booking cancelled
            if (myModel.getLead_status_id() == 10) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //ghp pending
            if (myModel.getLead_status_id() == 13) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_ghp_plus_pending));

            iv_editOwnLeadName.setOnClickListener(v -> {
                //showEditNameDialog(myModel.getCuidModel(),position,"own");
                showUpdateLeadPopUpMenu(iv_editOwnLeadName, myModel, position);
            });


            //visible view
            ll_own_view.setVisibility(View.VISIBLE);


       /* else if (myModel.getFeed_type_id() == 2) {

            //Others View
            //tag date
            tv_others_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
            //tag icon
            iv_others_tagIcon.setImageResource(R.drawable.ic_tag_general);
            //tag other_ids
            tv_others_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? myModel.getTag() : "");
            //elapsed time
            tv_others_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
            //cu_id number
            tv_others_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty() ? myModel.getSmall_header_title() : "");
            //lead name
            tv_others_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty() ? myModel.getMain_title() : "");
            //project name
            tv_others_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty() ? myModel.getDescription() : "");
            //lead stage
            tv_othersLeadStage.setText(myModel.getCuidModel() != null && myModel.getCuidModel().getLead_stage_name()!=null ? myModel.getCuidModel().getLead_stage_name() : "");
            tv_othersLeadStage_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            Log.e(TAG, "getFeedsView:Filter ll_othersLeadStage_dot"+ll_othersLeadStage_dot );
            ll_othersLeadStage_dot.setVisibility(myModel.getCuidModel().getLead_stage_id()==0 ? View.GONE:View.VISIBLE);
            //status
            tv_others_Lead_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
            //token number/sub status other_ids
            tv_others_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
            //mobile number/call
            iv_others_Lead_call.setOnClickListener(v -> {
                if (myModel.getCall()!=null)
                {
                    new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getCall());
                }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
            });

            //whatsApp
            iv_othersLeadWhatsApp.setOnClickListener(v -> {
                if (myModel.getCall()!=null)
                {
                    //send Message to WhatsApp Number
                    sendMessageToWhatsApp(myModel.getCall(), myModel.getMain_title());
                }
                else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
            });


            switch (myModel.getCuidModel().getLead_stage_id()) {
                case 1:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorhot));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                    break;
                case 2:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    break;
                case 3:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorcold));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                    break;
                case 4:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorni));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                    break;
                default:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }

            //set others popup menu's
            iv_others_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_others_leadOptions, myModel));


            //Set Lead Details
            if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
            {
                iv_others_leadDetails_ec.setVisibility(View.VISIBLE);
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

            } else iv_others_leadDetails_ec.setVisibility(View.GONE);


            //set expand Collapse Others
            iv_others_leadDetails_ec.setOnClickListener(view -> {

                if (myModel.isExpandedOthersView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(iv_others_leadDetails_ec, false);
                    collapse(ll_others_viewLeadDetails);
                    myModel.setExpandedOthersView(false);
                } else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(iv_others_leadDetails_ec, true);
                    expandSubView(ll_others_viewLeadDetails);
                    myModel.setExpandedOthersView(true);
                }
            });

            ll_others_main.setOnClickListener(view -> {

                if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
                {
                    if (myModel.isExpandedOthersView())  //expanded
                    {
                        // //do collapse View
                        new Animations().toggleRotate(iv_others_leadDetails_ec, false);
                        collapse(ll_others_viewLeadDetails);
                        myModel.setExpandedOthersView(false);
                    } else    // collapsed
                    {
                        //do expand view
                        new Animations().toggleRotate(iv_others_leadDetails_ec, true);
                        expandSubView(ll_others_viewLeadDetails);
                        myModel.setExpandedOthersView(true);
                    }
                }
            });

            //set visibility
            iv_others_leadOptions.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_others_Lead_call.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_othersLeadWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            mBtn_others_claimNow.setVisibility(myModel.getLead_status_id() ==1  ? View.VISIBLE : View.GONE);
            if (myModel.getCuidModel()!=null) iv_othersReminderIcon.setVisibility(myModel.getCuidModel().getIs_reminder_set() == 0 ? View.GONE : View.VISIBLE);

            mBtn_others_claimNow.setOnClickListener(view -> {

                lead_id=myModel.getLead_id();
                showConfirmDialog(true, position);
            });


            //unclaimed
            if (myModel.getLead_status_id() == 1) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_unclaimed));
            // lead claimed
            if (myModel.getLead_status_id() == 2)  tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
            // lead assigned
            if (myModel.getLead_status_id() == 3)  tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
            //self/ lead added
            if (myModel.getLead_status_id() == 4) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
            //site visited
            if (myModel.getLead_status_id() == 5) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
            //token /GHP  generated
            if (myModel.getLead_status_id() == 6) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));
            //token /GHP  cancelled
            if (myModel.getLead_status_id() == 7) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //on hold
            if (myModel.getLead_status_id() == 8) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
            //booked
            if (myModel.getLead_status_id() == 9) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));
            //booking cancelled
            if (myModel.getLead_status_id() == 10) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //ghp pending
            if (myModel.getLead_status_id() == 13) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_ghp_plus_pending));



            //visible other view
            ll_others_view.setVisibility(View.VISIBLE);

        }*/

        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 30);
        ll_main.setLayoutParams(params);


        return rowView;
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            new Animations().slideInBottom(v);
            lastPosition = p;
        }
    }


    private void sendMessageToWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );

        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
     String company_name =  sharedPreferences.getString("company_name", "");
String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();

        //String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, sales_person_name, WebServer.VJ_Website, sales_person_name, "+91-"+sales_person_mobile, sales_person_email);
        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);

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

    private void showUpdateLeadPopUpMenu(View view, FeedsModel myModel, int position) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadName, Menu.NONE, context.getString(R.string.update_lead_name));
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadStage, Menu.NONE, context.getString(R.string.update_lead_stage));

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId())
            {
                case R.id.menu_updateLead_updateLeadName:
                    //show update lead name alert
                    showEditNameDialog(myModel.getCuidModel(), position,"own");
                    return true;

                case R.id.menu_updateLead_updateLeadStage:
                    //show update stage alert
                    showUpdateLeadStageAlert(myModel,position);
                    return true;

                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();
    }


    private void showPopUpMenu(View view, FeedsModel myModel) {

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
                    if (myModel.getCall() != null && !myModel.getCall().trim().isEmpty())
                        new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getCall());
                    return true;


                case R.id.menu_leadOption_directBooking:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddFlatOnHoldActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_cancelBooking:
                    //show cancel alert
                    showCancelAllotmentAlert(myModel.getMain_title(),myModel.getBooking_id());
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
                    showReleaseHoldAlert(myModel.getMain_title(),myModel.getUnit_hold_release_id());
                    return true;


                case R.id.menu_leadOption_addCallLog:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("call_lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;


                case R.id.menu_leadOption_addCallSchedule:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddCallScheduleActivity.class)
                                .putExtra("customer_name", myModel.getMain_title())
                                .putExtra("lead_cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_id", myModel.getLead_id())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("fromFeed", true));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addReminder:
                    context.startActivity(new Intent(context, AddReminderActivity.class)
                            .putExtra("fromOther", 3)
                            .putExtra("lead_name", myModel.getMain_title())
                            .putExtra("lead_id", myModel.getLead_id())
                    );
                    return true;

                case R.id.menu_leadOption_addSiteVisit:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddSiteVisitActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addToken:
                    if (myModel.getCuidModel()!=null)
                    {

                        if (myModel.getLead_status_id()==2 || myModel.getLead_status_id()==3 || myModel.getLead_status_id()==4)
                        {
                            //check for lead status id is 2, 3, 4 (claimed, assigned, added) (Site visit not added)
                            //show alert to ask for generate site visit first
                            showAddSiteVisitAlert(myModel.getMain_title(),myModel);
                        }
                        else {
                            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                    .putExtra("fromOther",2)
                                    .putExtra("cuidModel", myModel.getCuidModel())
                                    .putExtra("cu_id", myModel.getSmall_header_title())
                                    .putExtra("lead_name", myModel.getMain_title())
                                    .putExtra("project_name", myModel.getDescription())
                                    .putExtra("lead_id", myModel.getLead_id()));
                        }
                    } else new Helper().showCustomToast(Objects.requireNonNull(context), "Failed to get lead details!");

                    return true;

                case R.id.menu_leadOption_viewToken:

                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                .putExtra("fromOther",3)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("lead_id", myModel.getLead_id()));
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

    //check call permission
    private boolean checkCallPermissions() {
        return  (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    //request camera permission
    private void requestPermissionCall()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CALL_PHONE)
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_PHONE_STATE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.PROCESS_OUTGOING_CALLS))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, getString(R.string.call_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, CALL_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == CALL_PERMISSION_REQUEST_CODE)  //handling camera permission
        {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //make a phone call once permission is granted
                if (customer_mobile!=null) prepareToMakePhoneCall();
                else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
            }
        }
    }

    private void prepareToMakePhoneCall() {

        //start the service first
        //startService(new Intent(context, TelephonyCallService.class));

      /*  //start the service first
        context.startService(new Intent(context, TelephonyCallService.class)
                .putExtra("call_lead_id",call_lead_id)
                .putExtra("lead_status_id",call_lead_status_id)
                .putExtra("call_schedule_id",0)
                .putExtra("user_id",user_id)
                .putExtra("cu_id",call_cuID)
                .putExtra("api_token",api_token)
                .putExtra("from_make_phone_Call",true)

        );*/

        //update into sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.putInt("call_lead_id", call_lead_id);
            editor.putInt("lead_status_id", call_lead_status_id);
            editor.putInt("call_schedule_id", 0);
            editor.putString("cu_id", call_cuID);
            editor.putBoolean("from_make_phone_Call", true);
            editor.apply();

            Log.e(TAG, "prepareToMakePhoneCall: sharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t lead_status_id "+ sharedPreferences.getInt("lead_status_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t cu_id "+sharedPreferences.getString("cu_id", null)
            );
        }



        new Helper().makePhoneCall(context, customer_mobile);

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


    private void showAddSiteVisitAlert(String CustomerName, FeedsModel myModel)
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
                    .putExtra("cu_id", myModel.getSmall_header_title())
                    .putExtra("lead_name", myModel.getMain_title())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getLead_id()));
        });

        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //go to generate GHP
            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                    .putExtra("fromOther",2)
                    .putExtra("cuidModel", myModel.getCuidModel())
                    .putExtra("cu_id", myModel.getSmall_header_title())
                    .putExtra("lead_name", myModel.getMain_title())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getLead_id()));

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


    @SuppressLint("SetTextI18n")
    private void showEditNameDialog(CUIDModel model,int position,String ownOrOther)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View editNameDialog = inflater != null ? inflater.inflate(R.layout.layout_edit_lead_name_dialog, null) : null;
        alertDialogBuilder.setView(editNameDialog);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert editNameDialog != null;

        AutoCompleteTextView acTv_prefix_mrs = editNameDialog.findViewById(R.id.acTv_prefix_mrs);
        TextInputLayout til_editLeadName = editNameDialog.findViewById(R.id.til_leadName);
        TextInputEditText edt_editLeadName = editNameDialog.findViewById(R.id.edt_leadName);

        TextInputLayout til_editLeadMiddleName = editNameDialog.findViewById(R.id.til_leadMiddleName);
        TextInputEditText edt_editLeadMiddleName = editNameDialog.findViewById(R.id.edt_leadMiddleName);

        TextInputLayout til_editLeadLastName = editNameDialog.findViewById(R.id.til_leadLastName);
        TextInputEditText edt_editLeadLastName = editNameDialog.findViewById(R.id.edt_leadLastName);

        AppCompatButton btn_negativeButton =  editNameDialog.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  editNameDialog.findViewById(R.id.btn_custom_alert_positiveButton);

        acTv_prefix_mrs.setText(model.getPrefix());
        edt_editLeadName.setText(model.getFirst_name());
        edt_editLeadMiddleName.setText(model.getMiddle_name());
        edt_editLeadLastName.setText(model.getLast_name());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, namePrefixArrayList);
        //set def selected
        acTv_prefix_mrs.setText(model.getPrefix());
        acTv_prefix_mrs.setAdapter(adapter);
        acTv_prefix_mrs.setThreshold(0);

        btn_positiveButton.setOnClickListener(view -> {

            if(edt_editLeadName.getText().toString().trim().isEmpty()) {

                new Helper().showCustomToast(context, "Please enter first name!");
                edt_editLeadName.requestFocus();
            }else{
                alertDialog.dismiss();
                if(claimDialog!=null) claimDialog.dismiss();

                if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))

                {
                    model.setPrefix(acTv_prefix_mrs.getText().toString()!=null ? acTv_prefix_mrs.getText().toString():"");
                    model.setFirst_name(edt_editLeadName.getText().toString() != null ? edt_editLeadName.getText().toString(): "");
                    model.setMiddle_name(edt_editLeadMiddleName.getText().toString()!= null ? edt_editLeadMiddleName.getText().toString() : "");
                    model.setLast_name(edt_editLeadLastName.getText().toString() != null ? edt_editLeadLastName.getText().toString() : "");

                    showProgressBar();

                    post_UpdateLead(model,position,ownOrOther);
                    //showProgressBar("Adding site visit...");
                    //  call_claimNow(fromFeed);

                } else Helper.NetworkError(context);
            }
            // showSuccessPopup();


        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_claim_popup));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
    }

    private void post_UpdateLead(CUIDModel model,int position,String ownOrOther)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",model.getLead_id());
        jsonObject.addProperty("country_code",model.getCountry_code());
        jsonObject.addProperty("prefix",model.getPrefix());
        jsonObject.addProperty("first_name", model.getFirst_name());
        jsonObject.addProperty("middle_name", model.getMiddle_name());
        jsonObject.addProperty("last_name", model.getLast_name());
        jsonObject.addProperty("mobile_number", model.getCustomer_mobile());
        jsonObject.addProperty("email", model.getCustomer_email());

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_updateLead(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            hideProgressBar();
                            new Helper().showSuccessCustomToast(Objects.requireNonNull(context),"Lead Name updated successfully!");
                            modelArrayList.get(position).setMain_title(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            if(ownOrOther.equals("own")){
                                AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadName);
                                textView.setText(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            }/*else if(ownOrOther.equals("other")){
                                AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_othersLeadName);
                                textView.setText(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            }*/
                            //onSuccessUpdateInfo();
                        }
                        else showErrorLogUpdateLead("Failed to update customer details! Try again.");
                    }
                }
                else {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogUpdateLead(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogUpdateLead(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLogUpdateLead(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLogUpdateLead(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogUpdateLead(getString(R.string.weak_connection));
                else showErrorLogUpdateLead(e.toString());
            }
        });
    }


    private void showUpdateLeadStageAlert(FeedsModel myModel, int pos)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_edit_lead_stage_dialog, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        MaterialTextView mTv_cuIdNumber =  alertLayout.findViewById(R.id.mTv_updateLeadStage_cuIdNumber);
        MaterialTextView mTv_leadName =  alertLayout.findViewById(R.id.mTv_updateLeadStage_leadName);
        AutoCompleteTextView acTv_leadStage =  alertLayout.findViewById(R.id.acTv_updateLeadStage_leadStage);
        MaterialButton mBtn_negativeButton =  alertLayout.findViewById(R.id.mBtn_updateLeadStage_negativeButton);
        MaterialButton mBtn_positiveButton =  alertLayout.findViewById(R.id.mBtn_updateLeadStage_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        mTv_cuIdNumber.setText(myModel.getSmall_header_title());
        mTv_leadName.setText(myModel.getMain_title());

        final int[] selectedLeadStageId = {0};
        final String[] selectedLeadStageName = {""};

        //set selected lead stage id name
        if (myModel.getCuidModel().getLead_stage_id()!=0 && leadStagesModelArrayList!=null&& leadStageStringArrayList.size()>0) {
            acTv_leadStage.setText(leadStagesModelArrayList.get(getIndexOfListForLeadStage(leadStagesModelArrayList, myModel.getCuidModel().getLead_stage_id())).getLead_stage_name());
            selectedLeadStageId[0] =  myModel.getCuidModel().getLead_stage_id();
            selectedLeadStageName[0] = leadStagesModelArrayList.get(getIndexOfListForLeadStage(leadStagesModelArrayList, myModel.getCuidModel().getLead_stage_id())).getLead_stage_name();
        }


        if (leadStagesModelArrayList.size() >0 &&  leadStageStringArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadStageStringArrayList);
            acTv_leadStage.setAdapter(adapter);
            acTv_leadStage.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_leadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                String itemName = adapter.getItem(position);
                for (LeadStagesModel pojo : leadStagesModelArrayList) {
                    if (pojo.getLead_stage_name().equals(itemName)) {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadStageId[0] = pojo.getLead_stage_id(); // This is the correct ID
                        selectedLeadStageName[0] = pojo.getLead_stage_name();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Stage name & id " + selectedLeadStageName[0] +"\t"+ selectedLeadStageId[0]);

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

        mBtn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_updateLeadStage(myModel.getLead_id(), selectedLeadStageId[0], pos, selectedLeadStageName[0]);

            } else Helper.NetworkError(context);
        });

        mBtn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
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


    private int getIndexOfListForLeadStage(List<LeadStagesModel> list, int stage_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  IntStream.range(0, list.size())
                    .filter(i -> list.get(i).getLead_stage_id() == stage_id)
                    .findFirst().orElse(-1);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getLead_stage_id() == stage_id)
                    return i;
            return -1;
        }
    }


    private void call_updateLeadStage(int lead_id, int lead_stage_id, int pos, String selectedLeadStageName)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("lead_stage_id", lead_stage_id);
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().Post_updateLeadStage(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {

                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            showSuccessUpdateLeadStage(pos, selectedLeadStageName, lead_stage_id);
                        }
                        else showErrorLogClaimLead("Failed to update lead stage. Invalid response from server!");
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

    private void showSuccessUpdateLeadStage(int position, String selectedLeadStageName, int lead_stage_id) {


        Objects.requireNonNull(context).runOnUiThread(() -> {

            //update lead stage id and lead stage name
            modelArrayList.get(position).getCuidModel().setLead_stage_id(lead_stage_id);
            modelArrayList.get(position).getCuidModel().setLead_stage_name(selectedLeadStageName);
            Log.e(TAG, "showSuccessUpdateLeadStage: lead_stage_id"+lead_stage_id );

            //set lead stage name
            AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadStage);
            AppCompatTextView textView_dot = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadStage_dot);
            LinearLayoutCompat ll_leadStage_dot = ll_addFeedData.getChildAt(position).findViewById(R.id.ll_leadStage_dot);
            textView.setText(selectedLeadStageName);
            textView_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            ll_leadStage_dot.setVisibility(lead_stage_id==0?View.GONE:View.VISIBLE);
            switch (lead_stage_id) {
                case 1:
                    textView.setTextColor(context.getResources().getColor(R.color.colorhot));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                    break;
                case 2:
                    textView.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    break;
                case 3:
                    textView.setTextColor(context.getResources().getColor(R.color.colorcold));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                    break;
                case 4:
                    textView.setTextColor(context.getResources().getColor(R.color.colorni));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                    break;
                default:
                    textView.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }


            //show success toast
            new Helper().showSuccessCustomToast(Objects.requireNonNull(context),"Lead stage updated successfully!");
        });
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
            ll_addFeedData.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            nsv.smoothScrollTo(0, 0);
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
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

            //remove all view
            ll_addFeedData.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            nsv.smoothScrollTo(0, 0);
        });

    }



    private void delayRefreshUnClaimedLeads()
    {
        if(context!=null)
        {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                //set applicationCreated false to avoid repeating call of getUnclaimed api
                if (sharedPreferences!=null) {
                    //update sharedPref with flag for claim now dialog
                    editor = sharedPreferences.edit();
                    editor.putBoolean("applicationCreated", false);
                    editor.apply();
                    Log.e(TAG, "delayRefreshUnClaimedLeads: setApplicationCreated false ");
                }

                //claimCount  = leadListModelArrayList.size();
                isClaimNow = leadListModelArrayList.size() > 0; // set true iff having unclaimed leads
                if (leadListModelArrayList.size() > 0) {
                    //showDialog();
                    new Handler(getMainLooper()).postDelayed(this::showDialog, 1000);
                }
                else {
                    //leadListModelArrayList.size();
                    if (isClaimNow) {
                        //check claimCount == total size

                        //refresh feeds api
                        refreshFeedApi();
                    }
                }

            });
        }
    }


    private void showDialog()
    {
        //Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
        } else {
            assert v != null;
            v.vibrate(1000);  // Vibrate method for below API Level 26
        }*/
        if (context!=null)
        {
            claimDialog = new Dialog(Objects.requireNonNull(context));
            claimDialog.setCancelable(true);
            Drawable button = getResources().getDrawable(R.drawable.claim_button_drawable, context.getTheme());
            @SuppressLint("InflateParams") View view  = context.getLayoutInflater().inflate(R.layout.layout_claim_now_pop_up, null);
            claimDialog.setContentView(view);
            Objects.requireNonNull(claimDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
            ImageView close =  view.findViewById(R.id.iv_close_claimNow);
            AppCompatTextView tv_date =  view.findViewById(R.id.tv_claimNow_date);
            AppCompatTextView tv_claimTag =  view.findViewById(R.id.tv_claimNow_tag);
            //AppCompatImageView iv_tagIcon =  view.findViewById(R.id.iv_claimNow_tagIcon);
            //AppCompatTextView tv_unitType =  view.findViewById(R.id.tv_claimNow_unitType);
            AppCompatTextView tv_elapsedTime =  view.findViewById(R.id.tv_claimNow_elapsedTime);
            AppCompatTextView tv_name =  view.findViewById(R.id.tv_claimNow_projectName);
            AppCompatTextView tv_cuId =  view.findViewById(R.id.tv_claimNow_cu_id);
            AppCompatTextView tv_lead_name =  view.findViewById(R.id.tv_claimNow_lead_name);
            AppCompatTextView tv_addedBy =  view.findViewById(R.id.tv_claimNow_addedBy);
            AppCompatButton claim_btn =  view.findViewById(R.id.btn_claimNow_popup);
            // RipplePulseLayout mRipplePulseLayout = view.findViewById(R.id.rp_layout);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
            int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
            width_px = width_px -50;
            height_px=height_px-200;
            claimDialog.getWindow().setLayout(width_px,height_px);

            //mRipplePulseLayout.startRippleAnimation();
            close.setOnClickListener(view12 -> {

                //dismiss dialog and update feed
                if(claimDialog!=null) claimDialog.dismiss();

                if (sharedPreferences!=null)
                {
                    //update sharedPref with flag
                    editor = sharedPreferences.edit();
                    editor.putBoolean("applicationCreated", false);
                    editor.apply();
                }

                //reset api with delay
                resetFeedApiWithDelay();

            });

            if(leadListModelArrayList!=null)
            {
                tv_date.setText(leadListModelArrayList.get(claimPosition).getTag_date());
                //tv_unitType.setText(leadListModelArrayList.get(claimPosition).getLead_unit_type());
                tv_cuId.setText(leadListModelArrayList.get(claimPosition).getLead_cuid_number());
                tv_lead_name.setText(leadListModelArrayList.get(claimPosition).getFull_name());
                tv_name.setText(String.format("%s | %s", leadListModelArrayList.get(claimPosition).getLead_project_name(), leadListModelArrayList.get(claimPosition).getLead_unit_type()));
                tv_elapsedTime.setText(leadListModelArrayList.get(claimPosition).getElapsed_time());
                tv_claimTag.setText(leadListModelArrayList.get(claimPosition).getLead_types_name());
                tv_addedBy.setText(leadListModelArrayList.get(claimPosition).getAdded_by());
                lead_id=leadListModelArrayList.get(claimPosition).getLead_id();
            }

            claim_btn.setBackgroundDrawable(button);
            claim_btn.setOnClickListener(view1 -> showConfirmDialog(false, 0));
            claimDialog.show();
        }
    }



    @SuppressLint("SetTextI18n")
    private void showConfirmDialog(boolean fromFeed, int position)
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

        tv_msg.setText("Claim Lead?");
        tv_desc.setText("Are you sure you want to claim this lead?");
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            // showSuccessPopup();
            alertDialog.dismiss();
            if(claimDialog!=null) claimDialog.dismiss();

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //showProgressBar("Adding site visit...");
                call_claimNow(fromFeed, position);

            } else Helper.NetworkError(context);

        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            if(claimDialog!=null) claimDialog.dismiss();

            if (sharedPreferences!=null)
            {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //reset api with delay
            resetFeedApiWithDelay();

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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_claim_popup));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
    }


    private void call_claimNow(boolean fromFeed, int position)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("lead_id",lead_id);

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call_ = client.getApiService().addLeadClaimNow(jsonObject);
        call_.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call_, @NonNull Response<JsonObject> response)
            {
                if (response.isSuccessful())
                {
                    if (response.body() != null)
                    {
                        String success = response.body().get("success").toString();
                        if ("1".equals(success))
                        {
                            claimAPiCount = claimAPiCount + 1;
                            //remove index from array list
                            if (!fromFeed) leadListModelArrayList.remove(claimPosition);
                            showSuccessPopup(fromFeed, position);
                        }
                        else if ("2".equals(success)) onAlreadyClaimedLead(response.body(), fromFeed);
                        else showErrorLogClaimLead("Failed to claim lead!");
                    }
                }
                else
                {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogClaimLead(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }


    //Success Pop Up
    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessPopup(boolean fromFeed, int position)
    {

        Dialog claimSuccessDialog = new Dialog(Objects.requireNonNull(context));
        claimSuccessDialog.setCancelable(true);
        View view  = context.getLayoutInflater().inflate(R.layout.layout_claim_now_success, null);
        claimSuccessDialog.setContentView(view);
        Objects.requireNonNull(claimSuccessDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
        AppCompatTextView tv_congrats = view.findViewById(R.id.tv_congrats);
        AppCompatTextView tv_msg = view.findViewById(R.id.tv_msg);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        width_px = width_px -50;
        height_px=height_px-250;
        claimSuccessDialog.getWindow().setLayout(width_px,height_px);

        tv_congrats.setText("Congratulations !");
        tv_msg.setText("This is your lead");

        new Handler(getMainLooper()).postDelayed(() -> {
            claimSuccessDialog.dismiss();
            if (fromFeed)
            {
                //reset feed api with delay
                //resetFeedApiWithDelay();

                //set lead stage name
               /* LinearLayoutCompat ll_homeFeed_othersCard = ll_addFeedData.getChildAt(position).findViewById(R.id.ll_homeFeed_othersCard);
                ll_homeFeed_othersCard.setBackground(getResources().getDrawable(R.drawable.cardview_leftside_white));

                //hide claim now button
                MaterialButton mBtn_othersClaimNow = ll_addFeedData.getChildAt(position).findViewById(R.id.mBtn_homeFeed_othersClaimNow);
                mBtn_othersClaimNow.setVisibility(View.GONE);

                AppCompatTextView tv_others_Lead_status = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_othersLeadStatus);
                tv_others_Lead_status.setText("Claimed");
                tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));*/


            } else delayRefreshUnClaimedLeads();
        }, 1000);

        /*claim_btn.setBackgroundDrawable(button);
        claim_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*Dialog Box*//*
                showConfirmDialog();

            }
        });*/

        claimSuccessDialog.show();
    }

    private void onAlreadyClaimedLead(JsonObject jsonObject, boolean fromFeed)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {

                //handle if some one claimed lead already
                //show error log
                if (jsonObject.has("msg")) showErrorLogClaimLead(!jsonObject.get("msg").isJsonNull() ? jsonObject.get("msg").getAsString() : "Failed to claim lead!" );
                else showErrorLogClaimLead("Failed to claim lead!");

                //handle if from feed -- already claimed then call get feeds api again
                // else remove position from arrayList
                if (fromFeed)
                {
                    //reset feed api with delay
                    resetFeedApiWithDelay();
                }
                else leadListModelArrayList.remove(claimPosition);
                // remove position from arrayList

            });
        }
    }



    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {
                //ll_pb.setVisibility(View.GONE);

                /*if (sharedPreferences!=null) {
                    //remove cached data key from sharedPref
                    editor = sharedPreferences.edit();
                    editor.remove("jA_events");
                    editor.apply();
                    eventsModelArrayList.clear();
                }*/

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();

                Helper.onErrorSnack(context, message);

                ll_addFeedData.setVisibility(View.GONE);
                ll_main.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.GONE);

                //set no data msg
                tv_noData.setText(openFlag ==1 ? getString(R.string.no_leads)
                        : openFlag == 2 ?  getString(R.string.no_site_visits_available)
                        : openFlag == 3 ?  getString(R.string.no_ghp_available)
                        : openFlag == 4 ?  getString(R.string.no_on_holds_available)
                        : openFlag == 5 ?  getString(R.string.no_allotments_available)
                        : getString(R.string.no_feeds)
                );

                ll_noData.setVisibility(View.VISIBLE);
            });
        }
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


    private void showErrorLogUpdateLead(final String message)
    {
        if (context!=null){

            //hide pb
            hideProgressBar();

            //ll_pb.setVisibility(View.GONE);
            Helper.onErrorSnack(Objects.requireNonNull(context), message);
        }
    }



    private void expandSubView(final View v) {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime == 1)
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
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


    private void collapse(final View v) {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

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


    private void hideCancellationProgressBar() {
        ll_pb.setVisibility(View.GONE);
        Objects.requireNonNull(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showCancellationProgressBar(String msg) {
        Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(msg);
        ll_pb.setVisibility(View.VISIBLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }



    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(context, view);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
