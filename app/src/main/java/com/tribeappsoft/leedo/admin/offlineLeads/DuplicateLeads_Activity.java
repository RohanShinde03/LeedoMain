package com.tribeappsoft.leedo.admin.offlineLeads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.adapter.DuplicateLeadListAdapter;
import com.tribeappsoft.leedo.admin.offlineLeads.model.OfflineLeadModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
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
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;


public class DuplicateLeads_Activity extends AppCompatActivity {

    @BindView(R.id.sr_duplicateLeadList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_duplicateLeadList) RecyclerView recyclerView;
    @BindView(R.id.ll_duplicateLeadList_searchBar) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.ll_duplicateLeadList_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.pb_duplicateLeadList) ContentLoadingProgressBar pb;
    @BindView(R.id.ll_duplicateLeadList_backToTop) LinearLayoutCompat ll_backToTop;

    @BindView(R.id.edt_duplicateLeadList_search) AppCompatEditText edt_search;
    @BindView(R.id.iv_duplicateLeadList_clearSearch) AppCompatImageView iv_clearSearch;

    private String TAG = "DuplicateLeads_Activity",api_token ="",filter_text="",lead_sync_time="";
    private ArrayList<OfflineLeadModel> offlineLeadModelArrayList;
    private DuplicateLeadListAdapter recyclerAdapter;
    private int current_page = 1, last_page = 1,user_id=0;
    Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int total=0;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplicate_leads_);
        //  overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= DuplicateLeads_Activity.this;

        if (getSupportActionBar()!=null)
        {
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(total==0 ? getString(R.string.all_duplicate_Leads) : "All Duplicate Leads (" + total + ")");

            getSupportActionBar().setTitle(total==0 ? getString(R.string.all_duplicate_Leads) : "All Duplicate Leads (" + total + ")");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        //get from notification click
        if (getIntent()!=null) notify = getIntent().getBooleanExtra("notify", false);


        offlineLeadModelArrayList =new ArrayList<>();
        //temp_arrayList =new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

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


        if (isNetworkAvailable(context)) {

            //call api
            swipeRefresh.setRefreshing(true);

            //call reset api
            resetApiCall();
        }
        else {
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            ll_noData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }


        //setting up scroll listener for reach to end listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE)
                {
                    Log.d("-----","end");
                    //gone back to top
                    if (ll_backToTop.getVisibility() == View.VISIBLE) {
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
                            if (isNetworkAvailable(context))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                new Handler().postDelayed(() -> call_getDuplicateLeads(), 1000);
                            } else NetworkError(Objects.requireNonNull(context));

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
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
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

            //LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            //Objects.requireNonNull(layoutManager).scrollToPositionWithOffset(0, 0);

            recyclerView.smoothScrollToPosition(0);

            //show view
            showViews();

            new Animations().slideOutBottom(ll_backToTop);
            ll_backToTop.setVisibility(View.GONE);
        });

    }
    /* private void setupRecycleView()
     {
         LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
         recyclerView.setLayoutManager(linearLayoutManager);
         recyclerView.setHasFixedSize(true);
         recyclerAdapter = new SalesExecutiveAdapter(context, teamLeadArrayList);
         recyclerView.setAdapter(recyclerAdapter);
     }
 */
    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
        if (verticalDivider != null) {
            dividerItemDecoration.setDrawable(verticalDivider);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerAdapter = new DuplicateLeadListAdapter(context, offlineLeadModelArrayList);
        recyclerView.setAdapter(recyclerAdapter);

    }
    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (isNetworkAvailable(context)) {

                //call api
                swipeRefresh.setRefreshing(true);

                //call reset api
                resetApiCall();
            }
            else {
                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                ll_noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void hideViews()
    {
        ll_searchBar.animate().translationY(-ll_searchBar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews()
    {

        ll_searchBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }



    //On Resume
    @Override
    public void onResume() {
        super.onResume();

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            int isDuplicateLeadUpdated = sharedPreferences.getInt("isDuplicateLeadUpdated", 0);
            //Get duplicate lead data
            if(isDuplicateLeadUpdated==1)
            {
                editor.remove("isDuplicateLeadUpdated");
                editor.apply();

                ll_searchBar.setVisibility(View.VISIBLE);
                swipeRefresh.setRefreshing(true);

                //set refresh api
                resetApiCall();

              //  get last sync time
                getLastOfflineSyncedTime();
            }
        }

        perform_search();
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
                                new Handler().postDelayed(() -> delayRefreshLastSync(), 100);
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

    private void delayRefreshLastSync()
    {
        if(context!=null)
        {
            runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                hideProgressBar();
            });
        }
        if (sharedPreferences != null) {
            editor = sharedPreferences.edit();
            editor.putString("lead_sync_time", lead_sync_time);
            editor.apply();
        }
    }

    private void call_getDuplicateLeads() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllDuplicateLeads(api_token,filter_text,current_page, user_id);
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
                        Log.d(TAG, "onCompleted:");

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
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
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
                                                if (jsonObject.has("total")) total = !jsonObject.get("total").isJsonNull() ? jsonObject.get("total").getAsInt() : 0;

                                                if (sharedPreferences != null) {
                                                    editor = sharedPreferences.edit();
                                                    editor.putInt("total_duplicate_leads", total);
                                                    editor.apply();
                                                }

                                                setDuplicateDataJson(jsonObject.getAsJsonObject());
                                            }
                                        }
                                    }else Log.e(TAG, "Outside Data: ");
                                } else
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setDuplicateDataJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();

                //1. clear arrayList
                //offlineLeadModelArrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }

    private void setJson(JsonObject jsonObject){

        OfflineLeadModel model = new OfflineLeadModel();

        if (jsonObject.has("offline_id")) model.setOffline_id(!jsonObject.get("offline_id").isJsonNull() ? jsonObject.get("offline_id").getAsInt() : 0 );
        if (jsonObject.has("prefix_id")) model.setPrefix_id(!jsonObject.get("prefix_id").isJsonNull() ? jsonObject.get("prefix_id").getAsInt() : 0 );
        if (jsonObject.has("prefix")) model.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setCustomer_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("email")) model.setCustomer_email(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
        if (jsonObject.has("country_code_1")) model.setCountry_code_1(!jsonObject.get("country_code_1").isJsonNull() ? jsonObject.get("country_code_1").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("alternate_mobile_number")) model.setAlternate_mobile_number(!jsonObject.get("alternate_mobile_number").isJsonNull() ? jsonObject.get("alternate_mobile_number").getAsString() : "" );
        if (jsonObject.has("address_line_1")) model.setAddress_line_1(!jsonObject.get("address_line_1").isJsonNull() ? jsonObject.get("address_line_1").getAsString() : "" );
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setCustomer_project_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("unit_category_id")) model.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) model.setCustomer_unit_type(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("project_name")) model.setCustomer_project_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() :"");
        if (jsonObject.has("lead_profession_id")) model.setLead_profession_id(!jsonObject.get("lead_profession_id").isJsonNull() ? jsonObject.get("lead_profession_id").getAsInt() : 0 );
        if (jsonObject.has("lead_profession")) model.setLead_profession(!jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() :"");
        if (jsonObject.has("lead_ni_reason")) model.setLead_ni_reason(!jsonObject.get("lead_ni_reason").isJsonNull() ? jsonObject.get("lead_ni_reason").getAsString() : "" );
        if (jsonObject.has("lead_ni_other_reason")) model.setLead_ni_other_reason(!jsonObject.get("lead_ni_other_reason").isJsonNull() ? jsonObject.get("lead_ni_other_reason").getAsString() : "" );
        if (jsonObject.has("budget_limit_id")) model.setBudget_limit_id(!jsonObject.get("budget_limit_id").isJsonNull() ? jsonObject.get("budget_limit_id").getAsInt() : 0 );
        if (jsonObject.has("budget_limit")) model.setBudget_limit(!jsonObject.get("budget_limit").isJsonNull() ? jsonObject.get("budget_limit").getAsString() : "" );
        if (jsonObject.has("income_range_id")) model.setIncome_range_id(!jsonObject.get("income_range_id").isJsonNull() ? jsonObject.get("income_range_id").getAsInt() : 0 );
        if (jsonObject.has("income_range")) model.setIncome_range(!jsonObject.get("income_range").isJsonNull() ? jsonObject.get("income_range").getAsString() : "" );
        if (jsonObject.has("lead_profession_id")) model.setLead_profession_id(!jsonObject.get("lead_profession_id").isJsonNull() ? jsonObject.get("lead_profession_id").getAsInt() : 0 );
        if (jsonObject.has("lead_profession")) model.setLead_profession(!jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() : "" );
        if (jsonObject.has("is_first_home")) model.setIs_first_home(!jsonObject.get("is_first_home").isJsonNull() ? jsonObject.get("is_first_home").getAsInt() : 0 );
        if (jsonObject.has("lead_stage_id")) model.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage")) model.setLead_stage(!jsonObject.get("lead_stage").isJsonNull() ? jsonObject.get("lead_stage").getAsString() : "" );
        if (jsonObject.has("lead_status_id")) model.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0 );
        if (jsonObject.has("dob")) model.setDob(!jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "" );
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("lead_types_id")) model.setLead_types_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0 );
        if (jsonObject.has("is_site_visited")) model.setIs_site_visited(!jsonObject.get("is_site_visited").isJsonNull() ? jsonObject.get("is_site_visited").getAsInt() : 0 );
        if (jsonObject.has("visit_date")) model.setVisit_date(!jsonObject.get("visit_date").isJsonNull() ? jsonObject.get("visit_date").getAsString() : "" );
        if (jsonObject.has("visit_time")) model.setVisit_time(!jsonObject.get("visit_time").isJsonNull() ? jsonObject.get("visit_time").getAsString() : "" );
        if (jsonObject.has("visit_remark")) model.setVisit_remark(!jsonObject.get("visit_remark").isJsonNull() ? jsonObject.get("visit_remark").getAsString() : "" );
        if (jsonObject.has("lead_types")) model.setLead_types(!jsonObject.get("lead_types").isJsonNull() ? jsonObject.get("lead_types").getAsString() : "" );
        if (jsonObject.has("remarks")) model.setRemarks(!jsonObject.get("remarks").isJsonNull() ? jsonObject.get("remarks").getAsString() : "" );

        offlineLeadModelArrayList.add(model);
    }

    private void delayRefresh()
    {
        if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                Log.e(TAG, "onResume: "+total);

                if (getSupportActionBar()!=null) {
                    getSupportActionBar().setTitle(total==0 ? getString(R.string.all_duplicate_Leads) : "All Duplicate Leads (" + total + ")");
                }

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new DuplicateLeadListAdapter(context, offlineLeadModelArrayList);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no data
                    swipeRefresh.setRefreshing(false);
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.GONE);
                } else {
                    //data available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });
        }

    }//delayRefresh

    private void notifyRecyclerDataChange()
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                Log.e(TAG, "onResume: "+total);
                if (getSupportActionBar()!=null) {
                    getSupportActionBar().setTitle(total==0 ? getString(R.string.all_duplicate_Leads) : "All Duplicate Leads (" + total + ")");
                }

                if (recyclerView.getAdapter()!=null) {

                    Log.e(TAG, "notifyRecyclerDataChange: sz "+ offlineLeadModelArrayList.size() );
                    //recyclerView adapter
                    recyclerView.getAdapter().notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noData.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noData.setVisibility(View.GONE);
                        //exFab.setVisibility(View.VISIBLE);
                    }

                }

            });
        }

    }

    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                // hideProgressBar();
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void perform_search() {

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

                        offlineLeadModelArrayList = new ArrayList<>();
                        //1. clear arrayList
                        offlineLeadModelArrayList.clear();
                        //2. reset page flag to 1
                        current_page = 1;
                        last_page = 1;
                        //3. Get search text
                        filter_text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());

                        swipeRefresh.setRefreshing(true);
                        //doFilter(text);
                        call_getDuplicateLeads();
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
    }


    private void resetApiCall()
    {
        if (isNetworkAvailable(context))
        {
            //Clear Search --> reset all params
            //1. clear arrayList
            offlineLeadModelArrayList = new ArrayList<>();
            offlineLeadModelArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Set search text clear
            filter_text = "";

            Log.e(TAG, "resetApiCall: ");
            swipeRefresh.setRefreshing(true);
            call_getDuplicateLeads();
        }
        else
        {
            NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);

        }
    }

    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        pb.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        pb.setVisibility(View.GONE);
        //Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    /*Overflow Menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
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
        // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down);
        if(notify) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down);
        }
    }

}
