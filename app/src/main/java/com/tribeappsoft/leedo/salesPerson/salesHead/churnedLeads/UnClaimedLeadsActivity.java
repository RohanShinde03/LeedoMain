package com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads.adapter.UnclaimedLeadsRecyclerAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads.filter.FilterActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.RecyclerItemClickListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UnClaimedLeadsActivity extends AppCompatActivity {


    @BindView(R.id.sr_unclaimedLeads) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_unclaimedLeads) RecyclerView recyclerView;
    @BindView(R.id.ll_unclaimedLeads_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.pb_unclaimedLeads) ContentLoadingProgressBar pb_unclaimedLeads;
    @BindView(R.id.mBtn_unclaimedLeads_autoAssignment) MaterialButton mBtn_autoAssignment;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    public UnClaimedLeadsActivity context;
    private UnclaimedLeadsRecyclerAdapter recyclerAdapter;
    private ArrayList<LeadListModel> modelArrayList, multiSelect_list;
    private ArrayList<Integer> leadIdsArrayList;
    private int user_id = 0, call = 0, project_id =0, filterCount = 0,total=0;
    private String TAG = "UnClaimedLeadsActivity",api_token="", filter_text="", from_date = "", to_date ="";
    private boolean stopApiCall = false, isMultiSelect = false;
    private ActionMode mActionMode;
    private  SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AppCompatTextView tvFilterItemCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_claimed_leads);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= UnClaimedLeadsActivity.this;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_unclaimed_leads));

            //getSupportActionBar().setTitle(getString(R.string.menu_unclaimed_leads));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //init
        init();

    }


    private void init()
    {

        //hide pb
        hidePB();
        //hide auto assignment button
        mBtn_autoAssignment.setVisibility(View.GONE);

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //init arrayLists
        modelArrayList = new ArrayList<>();
        multiSelect_list = new ArrayList<>();
        leadIdsArrayList = new ArrayList<>();

        //set up recycler
        setupRecycleView();

        //set up recycler scroll
        setUpRecyclerScroll();

        //set up swipeRefresh
        setSwipeRefresh();

        //def call api
        if (Helper.isNetworkAvailable(context))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset call flag to 0
            call = 0;

            swipeRefresh.setRefreshing(true);
            //show progress bar

            //showProgressBar();
            call_getUnclaimedLeads();
        }
        else Helper.NetworkError(context);

        //do multi select
        doMultiSelect();

        //auto churn leads
        mBtn_autoAssignment.setOnClickListener(view -> showAutoChurnAlertDialog());

    }


    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new UnclaimedLeadsRecyclerAdapter(context,modelArrayList, multiSelect_list);
        recyclerView.setAdapter(recyclerAdapter);
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

                    //TODO Rohan 16-09-2019
                    if (!swipeRefresh.isRefreshing()) {

                        //if swipe refreshing is on means user has done swipe-refreshed
                        //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                        //to avoid this, Have added below api call within this block

                        //swipeRefresh.setRefreshing(true);


                        Log.e(TAG, "onScrollStateChanged: call " + call);
                        if (!stopApiCall)  //call paginate api till ary is not empty
                        {
                            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {


                                if (leadIdsArrayList!=null && leadIdsArrayList.size()>0) {
                                    Log.e(TAG, "lead id's Array: "+ Arrays.toString(leadIdsArrayList.toArray()));
                                    mBtn_autoAssignment.setVisibility(View.GONE);
                                }
                                else {
                                    //swipeRefresh.setRefreshing(true);
                                    new Animations().slideOutBottom(mBtn_autoAssignment);
                                    mBtn_autoAssignment.setVisibility(View.GONE);
                                }

                                //show pb
                                showProgressBar();

                                //call get sales feed
                                call_getUnclaimedLeads();
                                //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

                            } else Helper.NetworkError(Objects.requireNonNull(context));

                        } else {
                            Log.e(TAG, "stopApiCall");
                            hideProgressBar();
                        }
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
                    Log.d(TAG, "onScrolled: upp " );
                    //new Animations().slideOutBottom(mBtn_assignLeads);
                    //mBtn_assignLeads.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );

                    if (leadIdsArrayList!=null && leadIdsArrayList.size()>0) {
                        Log.e(TAG, "lead id's Array: "+ Arrays.toString(leadIdsArrayList.toArray()));
                        mBtn_autoAssignment.setVisibility(View.GONE);
                    }
                    else
                    {
                        mBtn_autoAssignment.setVisibility(View.VISIBLE);
                        new Animations().slideInBottom(mBtn_autoAssignment);
                    }

                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );

                    //hide pb
                    hideProgressBar();

                    if (leadIdsArrayList!=null && leadIdsArrayList.size()>0) {
                        Log.e(TAG, "lead id's Array: "+ Arrays.toString(leadIdsArrayList.toArray()));
                        mBtn_autoAssignment.setVisibility(View.GONE);
                    }
                    else {
                        //visible button
                        new Animations().slideOutBottom(mBtn_autoAssignment);
                        mBtn_autoAssignment.setVisibility(View.VISIBLE);
                    }
                }
            }

        });

    }


    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(context)) {

                //set swipe refreshing to true
                swipeRefresh.setRefreshing(true);
                //reset api call
                refreshFeedApi();
            }
            else {

                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            }
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            boolean isManualUpdate = sharedPreferences.getBoolean("isManualUpdate", false);
            boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            editor.apply();

            if (isManualUpdate) {
                //refresh the api again

                //refresh api call
                refreshFeedApi();

                //remove update flag from shared pref
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("isManualUpdate");
                    editor.apply();
                }
            }

            if(isFilter) {

                if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    filterCount = sharedPreferences.getInt("filterCount", 0);
                    from_date = sharedPreferences.getString("sendFromDate","");
                    to_date = sharedPreferences.getString("sendToDate", "");
                    editor.apply();
                }
                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n from_date:- " +from_date+"\n to_date:- "+to_date );

                //reset api call
                resetApiCall();
            }
            else if (clearFilter) {

                //all filters are cleared

                Log.e(TAG, "onResume:clearFilter  ");
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("clearFilter");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }

                //clear fields
                project_id = filterCount = 0;
                from_date = to_date = "";

                //reset api call
                resetApiCall();
            }
        }

        //set up badge count
        setupBadge();

    }

    private void call_getUnclaimedLeads() {
        ApiClient client = ApiClient.getInstance();
        int limit = 8;
        int skip_count = call * limit;
        //limit, call * limit,  other_ids, last_lead_updated_at
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getUnclaimedLeads12Hours(api_token, limit, skip_count, filter_text, project_id, 0,  from_date, to_date, 0);
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

                        if (call ==1 ) delayRefresh();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (JsonObjectResponse.body().has("total")) total = !JsonObjectResponse.body().get("total").isJsonNull() ? JsonObjectResponse.body().get("total").getAsInt() : 0;
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
                stopApiCall = jsonArray.size() == 0;
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

            } else stopApiCall = true;
            //stop api call when data ary is null
        }
    }

    private void setJson(JsonObject jsonObject) {

        LeadListModel model = new LeadListModel();

        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0);
        if (jsonObject.has("leads_created_at")) model.setLeads_created_at(!jsonObject.get("leads_created_at").isJsonNull() ? jsonObject.get("leads_created_at").getAsString() : Helper.getDateTime());
        if (jsonObject.has("leads_updated_at")) model.setLeads_updated_at(!jsonObject.get("leads_updated_at").isJsonNull() ? jsonObject.get("leads_updated_at").getAsString() : Helper.getDateTime());
        if (jsonObject.has("lead_uid")) model.setLead_cuid_number(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "");
        if (jsonObject.has("leads_created_at")) model.setTag_date(!jsonObject.get("leads_created_at").isJsonNull() ? jsonObject.get("leads_created_at").getAsString() : "");
        if (jsonObject.has("unit_category")) model.setLead_unit_type(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "");
        if (jsonObject.has("project_name")) model.setLead_project_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number")) model.setLead_mobile(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("cp_name")) model.setCp_name(!jsonObject.get("cp_name").isJsonNull() ? jsonObject.get("cp_name").getAsString() : "");
        if (jsonObject.has("cp_executive_name")) model.setCp_executive_name(!jsonObject.get("cp_executive_name").isJsonNull() ? jsonObject.get("cp_executive_name").getAsString() : "");
        if (jsonObject.has("ref_name")) model.setRef_name(!jsonObject.get("ref_name").isJsonNull() ? jsonObject.get("ref_name").getAsString() : "");
        if (jsonObject.has("ref_mobile")) model.setRef_mobile(!jsonObject.get("ref_mobile").isJsonNull() ? jsonObject.get("ref_mobile").getAsString() : "");
        if (jsonObject.has("churn_sales_person_name")) model.setChurn_sales_person_name(!jsonObject.get("churn_sales_person_name").isJsonNull() ? jsonObject.get("churn_sales_person_name").getAsString() : "");
        if (jsonObject.has("churn_assign_date")) model.setChurn_assign_date(!jsonObject.get("churn_assign_date").isJsonNull() ? jsonObject.get("churn_assign_date").getAsString() : "");
        if (jsonObject.has("churn_count")) model.setChurn_count(!jsonObject.get("churn_count").isJsonNull() ? jsonObject.get("churn_count").getAsInt() : 0);
        if (jsonObject.has("lead_churn_id")) model.setLead_churn_id(!jsonObject.get("lead_churn_id").isJsonNull() ? jsonObject.get("lead_churn_id").getAsInt() : 0);
        if (jsonObject.has("churned_sales_person_id")) model.setChurned_sales_person_id(!jsonObject.get("churned_sales_person_id").isJsonNull() ? jsonObject.get("churned_sales_person_id").getAsInt() : 0);
        if (jsonObject.has("lead_types_id")) model.setLead_type_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0);
        if (jsonObject.has("lead_types_name")) model.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "");
        if (jsonObject.has("lead_status_id")) model.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0);

        //add model
        modelArrayList.add(model);
    }


    private void delayRefresh() {

        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //stop refreshing
            swipeRefresh.setRefreshing(false);

            Log.e(TAG, "onResume: "+total);
            if (getSupportActionBar()!=null) {

                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText("Total Records : " + "" + total + "");
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(total==0 ? getString(R.string.menu_unclaimed_leads) : getString(R.string.menu_unclaimed_leads) + "(" + total + ")");
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerAdapter= new UnclaimedLeadsRecyclerAdapter(context,modelArrayList, multiSelect_list);
            recyclerView.setAdapter(recyclerAdapter);

            int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
            if (count == 0) {
                //no data
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
                mBtn_autoAssignment.setVisibility(View.GONE);
                //exFab.setVisibility(View.GONE);
            } else {
                //data available
                recyclerView.setVisibility(View.VISIBLE);
                ll_noData.setVisibility(View.GONE);
                mBtn_autoAssignment.setVisibility(View.VISIBLE);
                //exFab.setVisibility(View.VISIBLE);
            }
        });

    }

    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        //if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                Log.e(TAG, "onResume: "+total);
                if (getSupportActionBar()!=null) {

                    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                    //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText("Total Records : " + "" + total + "");
                    ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(total==0 ? getString(R.string.menu_unclaimed_leads) : getString(R.string.menu_unclaimed_leads) + "(" + total + ")");
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                }

                if (recyclerAdapter!=null)
                {
                    //recyclerView adapter
                    recyclerAdapter.notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no data
                        recyclerView.setVisibility(View.GONE);
                        ll_noData.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //data available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noData.setVisibility(View.GONE);

                    }
                }
            });
        }

    }


    private void refreshFeedApi()
    {
        if (Helper.isNetworkAvailable(context))
        {

            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //1. clear arrayList
            modelArrayList = new ArrayList<>();
            modelArrayList.clear();
            multiSelect_list = new ArrayList<>();
            //2. reset call flag to 0 && Filter flag to 0
            call = 0;
            //3. Set search other_ids clear
            filter_text = "";
            //5. clear action mode
            if (mActionMode != null) {
                mActionMode.setTitle("");
                mActionMode.finish();
            }
            mActionMode = null;
            isMultiSelect = false;
            //6. clear id's arrayList
            leadIdsArrayList = new ArrayList<>();
            leadIdsArrayList.clear();

            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("isFilter");
                editor.putBoolean("clearFilter", false);
                editor.apply();
            }
            //clear fields
            project_id = filterCount = 0;
            from_date = to_date = "";

            //call get sales feed api
            swipeRefresh.setRefreshing(true);
            // showProgressBar();
            call_getUnclaimedLeads();

            //set up badge count
            setupBadge();

        } else Helper.NetworkError(context);

    }

    private void resetApiCall()
    {
        //5. clear action mode first
        if (mActionMode != null) {
            mActionMode.setTitle("");
            mActionMode.finish();
        }
        mActionMode = null;
        isMultiSelect = false;
        //6. clear id's arrayList
        leadIdsArrayList = new ArrayList<>();
        leadIdsArrayList.clear();

        if (Helper.isNetworkAvailable(context))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //1. clear arrayList
            modelArrayList = new ArrayList<>();
            modelArrayList.clear();
            multiSelect_list = new ArrayList<>();
            //2. reset call flag to 0 && Filter flag to 0
            call =  0;
            //3. Set search other_ids clear
            filter_text = "";
            //call get sales feed api
            swipeRefresh.setRefreshing(true);
            // showProgressBar();
            call_getUnclaimedLeads();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);
        } else Helper.NetworkError(context);

    }

    private void doMultiSelect()
    {
        //RecyclerView ItemTouch Method
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect) multi_select(position);
                //else Toast.makeText(getApplicationContext(), "Details Page", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiSelect_list = new ArrayList<>();
                    isMultiSelect = true;
                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);
            }
        }));
    }


    //MultiSelect Items
    public void multi_select(int position) {
        if (mActionMode != null) {

            position = position < modelArrayList.size() ?  position : 0;

            if (multiSelect_list.contains(modelArrayList.get(position))) {
                //already added -- do remove from the list
                multiSelect_list.remove(modelArrayList.get(position));

                //remove id from an arrayList
                checkInsertRemoveLeadIds(modelArrayList.get(position).getLead_id(), false);
            }
            else {
                //already not added -- do add into an list
                multiSelect_list.add(modelArrayList.get(position));

                //add selected id into an arrayList
                checkInsertRemoveLeadIds(modelArrayList.get(position).getLead_id(), true);
            }


            if (multiSelect_list.size() > 0) mActionMode.setTitle("" + multiSelect_list.size());
            else
            {
                mActionMode.setTitle("");
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
            refreshAdapter();

            //check arrayList
            checkArrayList();

        }
    }


    //Menus for Item Deleting
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_select_leads, menu);
            //context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId())
            {
                case (R.id.action_clear_selection):
                    //clear selection

                    //5. clear action mode
                    if (mActionMode != null) {
                        mActionMode.setTitle("");
                        mActionMode.finish();
                    }
                    mActionMode = null;
                    isMultiSelect = false;
                    multiSelect_list = new ArrayList<>();
                    multiSelect_list.clear();
                    //clear id's arrayList
                    leadIdsArrayList = new ArrayList<>();
                    leadIdsArrayList.clear();

                    //check arrayList
                    checkArrayList();

                    break;

                case (R.id.action_proceed):
                    startActivity(new Intent(context, SelectAssignmentLogicActivity.class)
                            .putParcelableArrayListExtra("multiSelect_list", multiSelect_list)
                            .putExtra("leadIdsArrayList", leadIdsArrayList)
                    );
                    break;
            }

           /* if (item.getItemId() == R.id.action_clear_selection) {//alertDialogHelper.showAlertDialog("","Delete Contact","DELETE","CANCEL",1,false);
                //clear selection
                showSelectedNotificationsDeleteAlert();
                return true;
            }*/
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiSelect_list = new ArrayList<>();
            refreshAdapter();
        }
    };

    public void refreshAdapter()
    {
        recyclerAdapter.multiSelect_list = multiSelect_list;
        recyclerAdapter.modelArrayList = modelArrayList;
        recyclerAdapter.notifyDataSetChanged();
    }

    private void checkInsertRemoveLeadIds(int lead_id, boolean value) {
        if (value) leadIdsArrayList.add(lead_id);
            //else catStringArrayList.remove(new String(subcatName));
        else leadIdsArrayList.remove(new Integer(lead_id));
    }

    private void checkArrayList()
    {
        //check  arrayList
        if (leadIdsArrayList!=null && leadIdsArrayList.size()>0) {
            Log.e(TAG, "lead id's Array: "+ Arrays.toString(leadIdsArrayList.toArray()));
            mBtn_autoAssignment.setVisibility(View.GONE);
        }
        else
        {
            Log.e(TAG, "lead id's Array: null" );
            mBtn_autoAssignment.setVisibility(View.VISIBLE);
        }
    }

    public void showAutoChurnAlertDialog()
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

        tv_msg.setText(getString(R.string.que_auto_churn));
        tv_desc.setText(getString(R.string.msg_auto_churn_leads_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //show auto assignment logic popup
            showAutoAssignmentPopup();
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmLp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmLp.gravity =  Gravity.CENTER;
        wmLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmLp.width = pixel-100;
        //wmLp.x = 100;   //x position
        //wmLp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmLp.height );
        alertDialog.getWindow().setAttributes(wmLp);
    }


    private void showAutoAssignmentPopup()
    {
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        final Dialog builder_accept=new BottomSheetDialog(context);
        builder_accept.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder_accept.setContentView(R.layout.layout_select_auto_churn_logic);
        builder_accept.setCancelable(false);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        AppCompatImageView iv_close = builder_accept.findViewById(R.id.iv_selectAutoChurnLogic_close);
        RadioGroup rdoGrp_selectAutoAssignment = builder_accept.findViewById(R.id.rdoGrp_selectAutoAssignment);
        MaterialButton mBtn_ok= builder_accept.findViewById(R.id.mBtn_selectAutoAssignmentLogic_submit);


        //property Buying for purpose
        Objects.requireNonNull(rdoGrp_selectAutoAssignment).setOnCheckedChangeListener((group, checkedId) ->
        {

            //int selectedId = rdoGrp_selectAutoAssignment.getCheckedRadioButtonId();
            //final RadioButton rBtn = rdoGrp_selectAutoAssignment.findViewById(selectedId);
            //final String btnText =  rBtn.getText().toString();
            //Toast.makeText(RegisterEventNewActivity.this, "Accommodation "+Accommodation, Toast.LENGTH_SHORT).show();

            //todo static set  1 ==> Meri Go Round, 2 ==> Random
            //int autoAssignmentLogicTypeId = btnText.contains(getString(R.string.meri_go_round)) ? 1 : 2;

            //set enabled
            Objects.requireNonNull(mBtn_ok).setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            mBtn_ok.setTextColor(context.getResources().getColor(R.color.main_white));


        });

        //share on WhatsApp
        Objects.requireNonNull(mBtn_ok).setOnClickListener(view -> {

            //check for logic selected or not

            // no radio buttons are checked
            if (rdoGrp_selectAutoAssignment.getCheckedRadioButtonId() == -1) new Helper().showCustomToast(context, "Please select Auto Assignment Logic!");

            else {
                // one of the radio buttons is checked

                // get selected radio button from radioGroup
                int selectedId = rdoGrp_selectAutoAssignment.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                final RadioButton rBtn = rdoGrp_selectAutoAssignment.findViewById(selectedId);
                final String btnText =  rBtn.getText().toString();
                //Toast.makeText(RegisterEventNewActivity.this, "Accommodation "+Accommodation, Toast.LENGTH_SHORT).show();

                //todo static set  1 ==> Meri Go Round, 2 ==> Random
                int autoAssignmentLogicTypeId = btnText.contains(getString(R.string.meri_go_round)) ? 1 : 2;

                //dismiss dialog
                builder_accept.dismiss();

                //call api
                if (Helper.isNetworkAvailable(context)) {
                    showPB(getString(R.string.auto_assigning_leads));
                    call_AutoAssignment(autoAssignmentLogicTypeId);
                }
                else Helper.NetworkError(context);
            }
        });

        //close popup
        Objects.requireNonNull(iv_close).setOnClickListener(view -> builder_accept.dismiss());

        builder_accept.show();
    }


    private void call_AutoAssignment(int auto_assignment_logic_type_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("project_id",project_id);
    /*    jsonObject.addProperty("limit",limit);
        jsonObject.addProperty("skip",skip_count);
        jsonObject.addProperty("filter_text",filter_text);
        jsonObject.addProperty("cp_executive_id",0);*/
        jsonObject.addProperty("from_date",from_date);
        jsonObject.addProperty("to_date",to_date);
        jsonObject.addProperty("user_id",user_id);
        /*   jsonObject.addProperty("churn_count",0);*/
        jsonObject.addProperty("method_type",auto_assignment_logic_type_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().leadChurnedByAuto(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            // String msg = response.body().get("data").getAsString();
                            onSuccessAutoAssignment();
                        }
                        else {
                            showAutoAssignmentErrorLog("Failed to auto churn the leads!");
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showAutoAssignmentErrorLog(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showAutoAssignmentErrorLog(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showAutoAssignmentErrorLog(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showAutoAssignmentErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showAutoAssignmentErrorLog(context.getString(R.string.weak_connection));
                else showAutoAssignmentErrorLog(e.toString());
            }
        });
    }

    private void onSuccessAutoAssignment()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide pb
            hidePB();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.leads_auto_assigned_successfully));

            //do refresh api
            if (Helper.isNetworkAvailable(context)) {
                swipeRefresh.setRefreshing(true);
                //refresh api call
                refreshFeedApi();
            }
            else Helper.NetworkError(context);

        });
    }


    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {


                swipeRefresh.setRefreshing(false);
                //hide pb
                hideProgressBar();

                hidePB();

                mBtn_autoAssignment.setVisibility(View.GONE);

                Helper.onErrorSnack(context, message);
                //hide recycler view
                recyclerView.setVisibility(View.GONE);

                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }


    private void showAutoAssignmentErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                //hide pb
                hideProgressBar();

                hidePB();
                Helper.onErrorSnack(context, message);

                //show auto assignment logic popup to retry again
                //new Handler().postDelayed(this::showAutoAssignmentPopup, 1500);
            });
        }
    }



    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        pb_unclaimedLeads.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        pb_unclaimedLeads.setVisibility(View.GONE);
        //Objects.requireNonNull(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showPB(String message)
    {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hidePB()
    {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_self, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_self);
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
//            closeIcon.setImageResource(R.drawable.ic_search_close_icon);

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
                Log.e(TAG, "onCreateOptionsMenu: onClose ");
                //doFilter("");
                //resetApiCall();
                return false;
            });
        }
        if (searchView != null)
        {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
            }
        }

        final MenuItem menuItem = menu.findItem(R.id.action_filter);
        View actionView = menuItem.getActionView();
        tvFilterItemCount = actionView.findViewById(R.id.cart_badge);
        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return true;
    }

    private void setupBadge() {

        if (tvFilterItemCount != null) {
            Log.e(TAG, "setupBadge: "+filterCount );
            if (filterCount == 0) {
                if (tvFilterItemCount.getVisibility() != View.GONE) {
                    tvFilterItemCount.setVisibility(View.GONE);
                }
            } else {
                tvFilterItemCount.setText(String.valueOf(Math.min(filterCount, 99)));
                if (tvFilterItemCount.getVisibility() != View.VISIBLE) {
                    tvFilterItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }



    private void doFilter(String query) {

        if (Helper.isNetworkAvailable(context))
        {
            modelArrayList = new ArrayList<>();
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset page flag to 1
            call = 0;
            //3. Get search text
            filter_text = query;
            //4. notify data set changed
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(true);

            //showProgressBar();
            //call the api
            call_getUnclaimedLeads();
        }
        else Helper.NetworkError(context);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case (android.R.id.home):

                //clear filter if applied from sharedPref
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("project_id");
                    editor.remove("from_date");
                    editor.remove("to_date");
                    editor.remove("isFilter");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }
                //clear fields
                project_id = filterCount = 0;
                from_date = to_date = "";

                onBackPressed();
                break;

            case (R.id.action_filter):
                startActivity(new Intent(context, FilterActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }


}
