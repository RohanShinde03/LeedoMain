package com.tribeappsoft.leedo.salesPerson.salesHead.claimedList;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.claimedList.adapter.SelectClaimedLeadsRecyclerAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.claimedList.filter.FilterMore_Activity;
import com.tribeappsoft.leedo.salesPerson.salesHead.claimedList.model.ClaimedLeadsModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.RecyclerItemClickListener;

import java.io.IOException;
import java.lang.reflect.Field;
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

public class ClaimedLeads_Activity extends AppCompatActivity {

    @BindView(R.id.sr_selectClaimedLeads) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_selectClaimedLeads) RecyclerView recyclerView;
    @BindView(R.id.ll_selectClaimedLeads_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.pb_selectClaimedLeads) ContentLoadingProgressBar pb_selectLeads;
    @BindView(R.id.mBtn_selectClaimedLeads_assign) MaterialButton mBtn_assignLeads;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    //@BindView(R.id.ll_selectLeads_search) LinearLayoutCompat ll_search;
    //@BindView(R.id.edt_selectLeads_search) TextInputEditText edt_search;
    //@BindView(R.id.iv_selectLeads_clearSearch) AppCompatImageView iv_clearSearch;

    private SelectClaimedLeadsRecyclerAdapter recyclerAdapter;
    private ArrayList<ClaimedLeadsModel> itemArrayList, multiSelect_list;
    private ClaimedLeads_Activity context;
    private ArrayList<Integer> leadIdArrayList;
    private String TAG = "SelectLeadsActivity",api_token="", search_text = "",from_date="",to_date="";
    private int project_id =0,sales_person_id =0,current_page =1,last_page =1,call = 0,filterCount=0;
    private ActionMode mActionMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isMultiSelect = false;
    private AppCompatTextView tvFilterItemCount;
    private int total=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claimed_leads_);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= ClaimedLeads_Activity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_claimed_leads));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        int user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+ api_token + "\n User id"+ user_id);
        editor.apply();

        itemArrayList = new ArrayList<>();
        multiSelect_list = new ArrayList<>();
        leadIdArrayList = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //hide pb
        hideProgressBar();
        hidePB();
        mBtn_assignLeads.setVisibility(View.GONE);

        //set swipe refresh
        setSwipeRefresh();

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getAllLeadsBySalesPerson();
        }
        else {
            Helper.NetworkError(context);
        }

        //do multi select
        doMultiSelect();

        mBtn_assignLeads.setOnClickListener(v -> checkValidation());

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
                    if (!swipeRefresh.isRefreshing())
                    {
                        //if swipe refreshing is on means user has done swipe-refreshed
                        //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                        //to avoid this, Have added below api call within this block

                        Log.e(TAG, "onScrollStateChanged: current_page "+current_page);
                        if (current_page <= last_page)  //
                        {
                            if (Helper.isNetworkAvailable(context))
                            {
                                //swipeRefresh.setRefreshing(true);
                                new Animations().slideOutBottom(mBtn_assignLeads);
                                mBtn_assignLeads.setVisibility(View.GONE);

                                showProgressBar();
                                call_getAllLeadsBySalesPerson();
                            } else Helper.NetworkError(context);

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
                    Log.d(TAG, "onScrolled: upp " );
                    //new Animations().slideOutBottom(mBtn_assignLeads);
                    //mBtn_assignLeads.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
                    mBtn_assignLeads.setVisibility(View.VISIBLE);
                    new Animations().slideInBottom(mBtn_assignLeads);
                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );
                    //hide pb
                    hideProgressBar();

                    //visible button
                    new Animations().slideOutBottom(mBtn_assignLeads);
                    mBtn_assignLeads.setVisibility(View.VISIBLE);
                }
            }

        });

    }

    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new SelectClaimedLeadsRecyclerAdapter(context,itemArrayList,multiSelect_list);
        recyclerView.setAdapter(recyclerAdapter);
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



    //On Resume
    @Override
    public void onResume() {
        super.onResume();

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            editor.apply();

            if(isFilter) {

                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    sales_person_id = sharedPreferences.getInt("sales_person_id", 0);
                    filterCount = sharedPreferences.getInt("filterCount_claim", 0);
                    from_date = sharedPreferences.getString("sendFromDate","");
                    to_date = sharedPreferences.getString("sendToDate", "");
                    editor.apply();
                }

                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n from_date:- " +from_date+"\n to_date:- "+to_date );

                //reset api call
                resetApiCall();



               // context.getActionBar().setTitle(Html.fromHtml("<font color=\"white\">" + getString(R.string.menu_claimed_leads) + "(" + total +")"+ "</font>"));

                /*if (mActionMode == null) {
                    mActionMode = startActionMode(mActionModeCallback);
                }

                if (mActionMode != null) {
                    mActionMode.setTitle(getString(R.string.menu_claimed_leads) + "(" + total + ")");
                }*/
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
                project_id =sales_person_id= filterCount = 0;
                from_date = to_date = "";

                //reset api call
                resetApiCall();

            /*    if (getSupportActionBar()!=null) {

                    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                    ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_claimed_leads));
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                }*/
            }
        }

        //setupBadge
        setupBadge();
    }


    private void call_getAllLeadsBySalesPerson()
    {
        ApiClient client = ApiClient.getInstance();
        int limit = 8,cp_executive_id=0;
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getClaimedReassignLeads(api_token, project_id,sales_person_id,cp_executive_id, from_date,to_date, limit,call* limit, search_text);
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
                        //set
                        if (current_page ==2 ) delayRefresh();
                        else notifyRecyclerDataChange();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if (JsonObjectResponse.isSuccessful())
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
                                                if (jsonObject.has("total")) total = !jsonObject.get("total").isJsonNull() ? jsonObject.get("total").getAsInt() : 0;
                                                //if (JsonObjectResponse.body().has("per_page")) per_page = !JsonObjectResponse.body().get("per_page").isJsonNull() ? JsonObjectResponse.body().get("per_page").getAsInt() : 0;

                                                setLeadsJson(jsonObject.getAsJsonObject());
                                                //dataSet();

                                            }
                                        }
                                    }else Log.e(TAG, "Outside Data: ");
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }

                    }
                });

    }


    private void setLeadsJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment call
                call = call + 1;
                Log.e(TAG, "setLeadsJson: "+call);

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }


    private void setJson(JsonObject jsonObject)
    {
        ClaimedLeadsModel model=new ClaimedLeadsModel();
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
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
        if (jsonObject.has("lead_types_name")) model.setLead_Type(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "" );
        if (jsonObject.has("created_at")) model.setTagDate(!jsonObject.get("created_at").isJsonNull() ? jsonObject.get("created_at").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("call_done")) model.setCall_done(!jsonObject.get("call_done").isJsonNull() ? jsonObject.get("call_done").getAsInt() : 0 );
        if (jsonObject.has("call_count")) model.setCall_count(!jsonObject.get("call_count").isJsonNull() ? jsonObject.get("call_count").getAsInt() : 0 );
        if (jsonObject.has("avg_time")) model.setAvg_time(!jsonObject.get("avg_time").isJsonNull() ? jsonObject.get("avg_time").getAsString() : "" );
        if (jsonObject.has("cp_name")) model.setCp_Name(!jsonObject.get("cp_name").isJsonNull() ? jsonObject.get("cp_name").getAsString() : "" );
        if (jsonObject.has("cp_executive_name")) model.setCp_executive_name(!jsonObject.get("cp_executive_name").isJsonNull() ? jsonObject.get("cp_executive_name").getAsString() : "" );

        if (jsonObject.has("lead_info")) {
            if (!jsonObject.get("lead_info").isJsonNull() && jsonObject.get("lead_info").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("lead_info").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                model.setDetailsTitleModelArrayList(arrayList);
            }
        }

        itemArrayList.add(model);
        Log.e(TAG, "setJson: "+itemArrayList.size());
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




    //delayRefresh
    private void delayRefresh()
    {
        //if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                hidePB();

                Log.e(TAG, "onResume: "+total);

                if (getSupportActionBar()!=null) {

                    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                    //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText("Total Records : " + "" + total + "");
                    ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(total==0 ? getString(R.string.menu_claimed_leads) :getString(R.string.menu_claimed_leads) + "(" + total + ")");
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                }

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new SelectClaimedLeadsRecyclerAdapter(context,itemArrayList,multiSelect_list);
                recyclerView.setAdapter(recyclerAdapter);


                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no data
                    swipeRefresh.setRefreshing(false);
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    mBtn_assignLeads.setVisibility(View.GONE);
                    //exFab.setVisibility(View.GONE);
                } else {
                    //data available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    mBtn_assignLeads.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });
        }

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
                hidePB();

                Log.e(TAG, "onResume: "+total);

                if (getSupportActionBar()!=null) {

                    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                    ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(total==0 ? getString(R.string.menu_claimed_leads) :getString(R.string.menu_claimed_leads) + "(" + total + ")");
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
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            multiSelect_list = new ArrayList<>();
            multiSelect_list.clear();
            //2. reset call flag to 0 && Filter flag to 0
            call = 0;
            //3. Set search other_ids clear
            search_text = "";
            //5. clear action mode
            if (mActionMode != null) {
                mActionMode.setTitle("");
                mActionMode.finish();
            }
            mActionMode = null;
            isMultiSelect = false;
            //6. clear id's arrayList
            leadIdArrayList = new ArrayList<>();
            leadIdArrayList.clear();



            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("filterCount_claim");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("isFilter");
                editor.putBoolean("clearFilter", false);
                editor.apply();
            }
            //clear filter fields
            project_id =sales_person_id= filterCount = 0;
            from_date = to_date = "";

            //call get sales feed api
            swipeRefresh.setRefreshing(true);
            // showProgressBar();
            call_getAllLeadsBySalesPerson();

            //set up badge count
            setupBadge();

            if (getSupportActionBar()!=null) {

                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_claimed_leads));
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }


            mBtn_assignLeads.setBackgroundColor(getResources().getColor(R.color.main_medium_grey));

        } else Helper.NetworkError(context);
    }


    private void checkValidation() {
        if(multiSelect_list.size()>0) {
            //call api method
            showSubmitMemberAlertDialog();

        }
        else
        {
            new Helper().showCustomToast(context, "Please select at least one lead!");
        }
    }


    private void showSubmitMemberAlertDialog()
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

        tv_msg.setText(getString(R.string.que_mark_as_unclaimed));
        //  String str = TextUtils.join(", ", recyclerAdapter.getSalesUserNames());
        tv_desc.setText(getString(R.string.lead_unclaimed_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showPB(getString(R.string.leads_mark_as_unclaimed));
                //api_call
                call_postMarksAsUnclaimedLeads();
            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
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

    private void call_postMarksAsUnclaimedLeads()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.add("leads", new Gson().toJsonTree(leadIdArrayList).getAsJsonArray());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().markLeadsAsUnclaimed(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1) {
                            onSuccessLeadUnclaimed();
                        }
                        else {
                            showErrorLogAssignTeamMembers("Failed to unclaimed the leads!");
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogAssignTeamMembers(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogAssignTeamMembers(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogAssignTeamMembers(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogAssignTeamMembers(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogAssignTeamMembers(context.getString(R.string.weak_connection));
                else showErrorLogAssignTeamMembers(e.toString());
            }
        });
    }


    private void onSuccessLeadUnclaimed()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            hidePB();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.lead_unclaimed_successfully));

            //set result OK
            //setResult(Activity.RESULT_OK, new Intent());
            //do back pressed
          //  new Handler().postDelayed(this::onBackPressed, 500);

            //do refresh api
            if (Helper.isNetworkAvailable(context)) {
                swipeRefresh.setRefreshing(true);
                //reset api call
                refreshFeedApi();
            }
            else Helper.NetworkError(context);


        });
    }




    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                hideProgressBar();
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void showErrorLogAssignTeamMembers(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                //hide pb
                hideProgressBar();
                //hide pb
                hidePB();

                //hide swipe refresh if refreshing
                swipeRefresh.setRefreshing(false);
                //show error log
                Helper.onErrorSnack(context, message);

            });
        }
    }


/*    private void perform_search() {

        //or you can search by the editTextFiler

        //search ime action click
        edt_search.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (recyclerView.getAdapter() != null)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    if (isNetworkAvailable(context))
                    {

                        //set swipe refreshing to true
                        swipeRefresh.setRefreshing(true);

                        String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                        // regex to match any number of spaces
                        filterText = filterText.trim().replaceAll("\\s+", " ");
                        Log.e(TAG, "perform_search: filterText "+filterText);
                        doFilter(filterText);

                    }
                    else NetworkError(Objects.requireNonNull(context));
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

                   *//* //set swipe refreshing to true
                    swipeRefresh.setRefreshing(true);

                    String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                    // regex to match any number of spaces
                    filterText = filterText.trim().replaceAll("\\s+", " ");

                    //call reset api
                    doFilter(filterText);*//*

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
            // doFilter("");
            resetApiCall();
        });
    }*/

    private void doFilter(String query) {

        if (Helper.isNetworkAvailable(context))
        {
            itemArrayList = new ArrayList<>();
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;

            // reset page flag to 1
            call = 0;

       /*     call = 0;
            last_lead_updated_at = null;*/
            //3. Get search text
            search_text = query;
            //4. notify data set changed
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(true);

            showProgressBar();
            //call the api
            call_getAllLeadsBySalesPerson();
        }
        else Helper.NetworkError(context);
    }

    private void resetApiCall()
    {
        if (Helper.isNetworkAvailable(context))
        {
            swipeRefresh.setRefreshing(true);

            //Clear Search --> reset all params
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 0
            current_page = 1;
            last_page = 1;
            //3. reset page flag to 1
            call = 0;
            //4. Set search text clear
            search_text = "";
            //edt_search.setText("");
            //5. notify adapter
            recyclerAdapter.notifyDataSetChanged();
            Log.e(TAG, "resetApiCall: ");
            //call the api
            call_getAllLeadsBySalesPerson();


        }
        else {
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case (android.R.id.home):
                //clear filter if applied from sharedPref
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("project_id");
                    editor.remove("sales_person_id");
                    editor.remove("filterCount_claim");
                    editor.remove("from_date");
                    editor.remove("to_date");
                    editor.remove("isFilter");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }
                //clear fields
                project_id =sales_person_id= filterCount = 0;
                from_date = to_date = "";

                onBackPressed();
                break;

            case (R.id.action_filter):
                startActivity(new Intent(context, FilterMore_Activity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
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


    //MultiSelect Items
    public void multi_select(int position) {
        if (mActionMode != null) {

            position = position < itemArrayList.size() ?  position : 0;

            if (multiSelect_list.contains(itemArrayList.get(position))) {
                //already added -- do remove from the list
                multiSelect_list.remove(itemArrayList.get(position));

                //remove id from an arrayList
                checkInsertRemoveUserIds(itemArrayList.get(position).getLead_id(), false);
            }
            else {
                //already not added -- do add into an list
                multiSelect_list.add(itemArrayList.get(position));

                //add selected id into an arrayList
                checkInsertRemoveUserIds(itemArrayList.get(position).getLead_id(), true);
            }


            if (multiSelect_list.size() > 0) {

                mActionMode.setTitle("" + multiSelect_list.size());
                showButton();
                //remove selected id from arrayList
            }
            else
            {
                mActionMode.setTitle("");
                hideButton();

                //remove selected id from arrayList

                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }

            refreshAdapter();

            //check arrayList
            checkArrayList();
        }
    }

    /*Menus for Item Deleting*/
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_delete_notifications, menu);
            //context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_clear_notifications) {//alertDialogHelper.showAlertDialog("","Delete Contact","DELETE","CANCEL",1,false);
                //deleteItems();
                if (mActionMode != null) {
                    mActionMode.setTitle("");
                    hideButton();
                    //remove selected id from arrayList
                    //    checkInsertRemoveUserIds(model.getLead_id(), true);
                    mActionMode.finish();
                }
                mActionMode = null;
                isMultiSelect = false;
                multiSelect_list = new ArrayList<>();
                multiSelect_list.clear();

                return true;
            }
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

    @SuppressLint("UseValueOf")
    private void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value) leadIdArrayList.add(userID);
            //else catStringArrayList.remove(new String(subcatName));
        else leadIdArrayList.remove(new Integer(userID));
    }

    //public ArrayList<Integer> getLeadIdArrayList() {
      //  return leadIdArrayList;
    //}

    private void checkArrayList()
    {
        //check  arrayList
        if (leadIdArrayList!=null && leadIdArrayList.size()>0) {
            Log.e(TAG, "lead id's Array: "+ Arrays.toString(leadIdArrayList.toArray()));
        }
        else Log.e(TAG, "lead id's Array: null" );
    }


    public void refreshAdapter()
    {
        recyclerAdapter.multiSelect_list = multiSelect_list;
        recyclerAdapter.itemArrayList = itemArrayList;
        recyclerAdapter.notifyDataSetChanged();
    }


    public void showButton()
    {
        //mBtn_submit.setVisibility(View.VISIBLE);
        mBtn_assignLeads.setEnabled(true);
        mBtn_assignLeads.setBackgroundColor(getResources().getColor(R.color.color_lead_claimed));
    }

    public void hideButton()
    {
        // mBtn_submit.setVisibility(View.GONE);
        mBtn_assignLeads.setEnabled(false);
        mBtn_assignLeads.setBackgroundColor(getResources().getColor(R.color.main_medium_grey));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

    void showProgressBar() {
        pb_selectLeads.setVisibility(View.VISIBLE);
    }

    void hideProgressBar() {
        pb_selectLeads.setVisibility(View.GONE);
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


}
