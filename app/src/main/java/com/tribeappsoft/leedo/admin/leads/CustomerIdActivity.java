package com.tribeappsoft.leedo.admin.leads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.booked_customers.MarkAsBook_Activity;
import com.tribeappsoft.leedo.admin.callLog.CallLogActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.admin.leads.adapter.CUIDAdapter;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
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
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class CustomerIdActivity extends AppCompatActivity {


    @BindView(R.id.edt_cuIdList_search) AppCompatEditText edt_search;
    @BindView(R.id.iv_cuIdList_clearSearch) AppCompatImageView iv_clearSearch;

    @BindView(R.id.sr_cuIdList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_cuIdList) RecyclerView recyclerView;
    @BindView(R.id.ll_cuIdList_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_cuIdList_searchBar) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.pb_cuIdList) ContentLoadingProgressBar pb;
    @BindView(R.id.ll_cuIDList_backToTop) LinearLayoutCompat ll_backToTop;
    //@BindView(R.id.mBtn_cuIDListBackToTop) MaterialButton mBtn_backToTop;


    public ArrayList<CUIDModel> itemArrayList;
    private Activity context;
    private String TAG = "CustomerIdActivity", api_token ="", filter_text="";
    private int fromSiteVisit_or_token = 0; // TODO fromSiteVisit_or_token -> 1 came from (SIte visit), 2 came from (Token), 3, came from (Direct Booking), 4 -> Add call schedule ,  0(nothing)
    private int user_id=0, forId = 0; //TODO forId => 1 - site visit , 2 - Token , 3 - Direct Booking
    private int current_page = 1, last_page = 1;
    private boolean isSalesHead = false,isAdmin = false, fromHomeScreen_AddSiteVisit=false,fromHomeScreen_AddBooking=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerid);

        //  overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        //init context
        context= CustomerIdActivity.this;

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.select_customer));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null){
            fromSiteVisit_or_token =  getIntent().getIntExtra("fromSiteVisit_or_token",0);
            fromHomeScreen_AddSiteVisit =  getIntent().getBooleanExtra("fromHomeScreen_AddSiteVisit",false);
            fromHomeScreen_AddBooking =  getIntent().getBooleanExtra("fromHomeScreen_AddBooking",false);
            forId =  getIntent().getIntExtra("forId",0);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        Log.e(TAG, "onCreate: isSalesHead : "+isSalesHead);


        itemArrayList = new ArrayList<>();

        //Set RecyclerView
        setupRecycleView();

        //set SwipeRefresh
        setSwipeRefresh();

        //call api get customerId
        if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            itemArrayList.clear();
            swipeRefresh.setRefreshing(true);
            new Thread(this::call_getLeadList).start();
        }
        else
        {
            NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }


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
                            if (isNetworkAvailable(context))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                call_getLeadList();
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

    public void setSwipeRefresh() {
        //getOffline();

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed
            edt_search.setText("");
            swipeRefresh.setRefreshing(true);

            //call reset api
            resetApiCall();

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

        perform_search();

    }

    //SetUpRecycler
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new CUIDAdapter(context, itemArrayList, cuidModel -> {

            if (fromSiteVisit_or_token == 1)
            {
                //sit visits
                startActivity(new Intent(context, AddSiteVisitActivity.class)
                        .putExtra("fromHomeScreen_AddSiteVisit", fromHomeScreen_AddSiteVisit)
                        .putExtra("cuidModel",cuidModel));
                finish();
            }
            else if (fromSiteVisit_or_token == 3)
            {
                //sit visits
                startActivity(new Intent(context, MarkAsBook_Activity.class)
                        .putExtra("fromHomeScreen_AddBooking", fromHomeScreen_AddBooking)
                        .putExtra("cuidModel",cuidModel));
                finish();
            }
            else if (fromSiteVisit_or_token ==2)
            {
                if (cuidModel.getLead_status_id()==2 || cuidModel.getLead_status_id()==3 || cuidModel.getLead_status_id()==4) {
                    //check for lead status id is 2, 3, 4 (claimed, assigned, added) (Site visit not added)
                    //show alert to ask for generate site visit first
                    showAddSiteVisitAlert(cuidModel.getCustomer_name(),cuidModel);
                }
                else {
                    //generate Token
                    startActivity(new Intent(context, GenerateTokenActivity.class)
                            .putExtra("cuidModel",cuidModel)
                            .putExtra("fromOther",2));
                    finish();
                }
            }
           /* else if (fromSiteVisit_or_token ==3)
            {
                //Direct booking
                startActivity(new Intent(context, AddFlatOnHoldActivity.class).putExtra("cuidModel",cuidModel));
                finish();
            }*/
            else if (fromSiteVisit_or_token ==4) {
                //add call schedule

                startActivity(new Intent(context, AddCallScheduleActivity.class)
                        .putExtra("customer_name", cuidModel.getCustomer_name())
                        .putExtra("lead_cu_id", cuidModel.getCustomer_mobile())
                        .putExtra("lead_id", cuidModel.getLead_id())
                        .putExtra("lead_status_id", cuidModel.getLead_status_id())
                        .putExtra("project_name", cuidModel.getCustomer_project_name()));
                finish();
            }

            else if(fromSiteVisit_or_token == 5){

                startActivity(new Intent(context, CallLogActivity.class)
                        .putExtra("cuidModel", cuidModel)
                        .putExtra("cu_id", cuidModel.getCustomer_mobile())
                        .putExtra("lead_name", cuidModel.getCustomer_name())
                        .putExtra("project_name", cuidModel.getCustomer_project_name())
                        .putExtra("lead_status_id", cuidModel.getLead_status_id())
                        .putExtra("call_lead_id", cuidModel.getLead_id()));
                finish();
            }

           /* else if(fromSiteVisit_or_token == 5){

                startActivity(new Intent(context, CallLogActivity.class)
                        .putExtra("cuidModel", cuidModel)
                        .putExtra("lead_cu_id", cuidModel.getCustomer_mobile())
                        .putExtra("lead_name", cuidModel.getCustomer_name())
                        .putExtra("project_name", cuidModel.getCustomer_project_name())
                        .putExtra("lead_status_id", cuidModel.getLead_status_id())
                        .putExtra("call_lead_id", cuidModel.getLead_id()));
                finish();
            }*/
            else{
                setResult(Activity.RESULT_OK, new Intent().putExtra("result", cuidModel));
                hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                onBackPressed();
            }

        }));

    }


    private void call_getLeadList() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadList(api_token, filter_text, forId, current_page, user_id, isSalesHead || isAdmin ? 1 : 0);
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

                                                setCUIDJson(jsonObject.getAsJsonObject());
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

    private void setCUIDJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setCustomerIDJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }

    private void setCustomerIDJson(JsonObject jsonObject)
    {
        CUIDModel myModel = new CUIDModel();
        if (jsonObject.has("lead_id")) myModel.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("is_my_lead")) myModel.setIsMyLead(!jsonObject.get("is_my_lead").isJsonNull() ? jsonObject.get("is_my_lead").getAsInt() : 0 );
        if (jsonObject.has("lead_uid")) myModel.setCu_id(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "");
        if (jsonObject.has("full_name")) myModel.setCustomer_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("country_code")) myModel.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
        if (jsonObject.has("project_name")) myModel.setCustomer_project_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("unit_category")) myModel.setCustomer_flat_type(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("sales_person_name")) myModel.Set_Sales_person_Name(!jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) myModel.setCustomer_mobile(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) myModel.setCustomer_email(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("is_kyc_uploaded")) myModel.setIs_kyc_uploaded(!jsonObject.get("is_kyc_uploaded").isJsonNull() ? jsonObject.get("is_kyc_uploaded").getAsInt() : 0 );
        if (jsonObject.has("first_name")) myModel.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString().trim() : "");
        if (jsonObject.has("middle_name")) myModel.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString().trim() : "");
        if (jsonObject.has("last_name")) myModel.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString().trim() : "");
        if (jsonObject.has("lead_status_id")) myModel.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0 );

        itemArrayList.add(myModel);
    }




    //Delay Refresh
    private void delayRefresh() {

        if (context != null) {
            runOnUiThread(() ->
            {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                Log.e(TAG, "delayRefresh: sz "+ itemArrayList.size() );
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(new CUIDAdapter(context, itemArrayList, cuidModel -> {

                    if (fromSiteVisit_or_token == 1)
                    {
                        //sit visits
                        startActivity(new Intent(context, AddSiteVisitActivity.class)
                                .putExtra("fromHomeScreen_AddSiteVisit", fromHomeScreen_AddSiteVisit)
                                .putExtra("cuidModel",cuidModel));
                        finish();
                    }
                    else if (fromSiteVisit_or_token == 3)
                    {
                        //sit visits
                        startActivity(new Intent(context, MarkAsBook_Activity.class)
                                .putExtra("fromHomeScreen_AddBooking", fromHomeScreen_AddBooking)
                                .putExtra("cuidModel",cuidModel));
                        finish();
                    }
                    else if (fromSiteVisit_or_token ==2)
                    {

                        if (cuidModel.getLead_status_id()==2 || cuidModel.getLead_status_id()==3 || cuidModel.getLead_status_id()==4)
                        {
                            //check for lead status id is 2, 3, 4 (claimed, assigned, added) (Site visit not added)
                            //show alert to ask for generate site visit first
                            showAddSiteVisitAlert(cuidModel.getCustomer_name(),cuidModel);
                        }
                        else {
                            //generate Token
                            startActivity(new Intent(context, GenerateTokenActivity.class)
                                    .putExtra("cuidModel",cuidModel)
                                    .putExtra("fromOther",2)
                            );
                            finish();
                        }
                    }
                  /*  else if (fromSiteVisit_or_token ==3)
                    {
                        //direct booking
                        startActivity(new Intent(context, AddFlatOnHoldActivity.class)
                                .putExtra("cuidModel", cuidModel)
                                .putExtra("fromOther",3)
                                .putExtra("lead_name", cuidModel.getCustomer_name())
                                .putExtra("lead_id", cuidModel.getLead_id())
                        );
                        finish();
                    }*/
                    else if (fromSiteVisit_or_token ==4) {
                        //add call schedule
                        startActivity(new Intent(context, AddCallScheduleActivity.class)
                                .putExtra("customer_name", cuidModel.getCustomer_name())
                                .putExtra("lead_cu_id", cuidModel.getCustomer_mobile())
                                .putExtra("lead_id", cuidModel.getLead_id())
                                .putExtra("lead_status_id", cuidModel.getLead_status_id())
                                .putExtra("project_name", cuidModel.getCustomer_project_name()));
                        finish();
                    }


                    else if(fromSiteVisit_or_token == 5){

                        startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", cuidModel)
                                .putExtra("cu_id", cuidModel.getCustomer_mobile())
                                .putExtra("lead_name", cuidModel.getCustomer_name())
                                .putExtra("project_name", cuidModel.getCustomer_project_name())
                                .putExtra("lead_status_id", cuidModel.getLead_status_id())
                                .putExtra("call_lead_id", cuidModel.getLead_id()));
                        finish();
                    }

                   /* else if(fromSiteVisit_or_token == 5){

                        startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", cuidModel)
                                .putExtra("lead_cu_id", cuidModel.getCustomer_mobile())
                                .putExtra("lead_name", cuidModel.getCustomer_name())
                                .putExtra("project_name", cuidModel.getCustomer_project_name())
                                .putExtra("lead_status_id", cuidModel.getLead_status_id())
                                .putExtra("call_lead_id", cuidModel.getLead_id()));
                        finish();
                    }*/
                    else{
                        setResult(Activity.RESULT_OK, new Intent().putExtra("result", cuidModel));
                        hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                        onBackPressed();
                    }

                }));

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );
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
            });
        }
    }


    private void notifyRecyclerDataChange()
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                if (recyclerView.getAdapter()!=null)
                {

                    Log.e(TAG, "notifyRecyclerDataChange: sz "+ itemArrayList.size() );
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



    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {
                //ll_pb.setVisibility(View.GONE);
                onErrorSnack(context,message);

                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void showAddSiteVisitAlert(String CustomerName, CUIDModel myModel)
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
                    .putExtra("cuidModel",myModel)
                    .putExtra("lead_id", myModel.getLead_id()));
            finish();
        });

        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //go to generate GHP
            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                    .putExtra("fromOther",2)
                    .putExtra("cuidModel",myModel)
                    .putExtra("lead_id", myModel.getLead_id()));

            finish();
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

                        itemArrayList = new ArrayList<>();
                        //1. clear arrayList
                        itemArrayList.clear();
                        //2. reset page flag to 1
                        current_page = 1;
                        last_page = 1;
                        //3. Get search text
                        filter_text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());

                        swipeRefresh.setRefreshing(true);
                        //doFilter(text);
                        call_getLeadList();
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
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Set search text clear
            filter_text = "";

            Log.e(TAG, "resetApiCall: ");
            swipeRefresh.setRefreshing(true);
            call_getLeadList();
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
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }


}


