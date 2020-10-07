package com.tribeappsoft.leedo.salesPerson.salesHead.holdFlats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.HoldFlatModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.holdFlats.adapter.ShHoldFlatListAdapter;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class SHHoldFlatListActivity extends AppCompatActivity {

    @BindView(R.id.sr_sh_holdFlatList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_sh_holdFlatList) RecyclerView recyclerView;
    @BindView(R.id.ll_sh_holdFlatList_noData) LinearLayoutCompat ll_noData;

    private ShHoldFlatListAdapter recyclerAdapter;
    private String TAG = "SHHoldFlatListActivity", api_token ="";
    private Activity context;
    private ArrayList<HoldFlatModel> itemArrayList;
    private int user_id =0, event_id = 0;
    private boolean fromAddHoldFlat = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_h_hold_flat_list);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context= SHHoldFlatListActivity.this;

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.hold_flats));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //init
        itemArrayList = new ArrayList<>();

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 0);
            //event_title = getIntent().getStringExtra("event_title");
            fromAddHoldFlat = getIntent().getBooleanExtra("fromAddHoldFlat", false);
        }



        //Set RecyclerView
        setupRecycleView();

        //setup swipeRefresh
        setSwipeRefresh();


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                //hideViews();

            }

            @Override
            public void onShow() {
                //showViews();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // call get lead data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //showProgressBar("Please wait...");
            swipeRefresh.setRefreshing(true);
            new Thread(this::getHoldFlatList).start();
        }
        else Helper.NetworkError(context);
    }


    //SetUpRecycler
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new ShHoldFlatListAdapter(this, itemArrayList, api_token);
        recyclerView.setAdapter(recyclerAdapter);
    }


    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() ->
        {
            //getOffline();
            if (Helper.isNetworkAvailable(context))
            {
                swipeRefresh.setRefreshing(true);
                //showProgressBar("Getting batchMates list...");
                getHoldFlatList();

            }else Helper.NetworkError(context);

        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


    private void getHoldFlatList()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getDirectHoldFlats(api_token,event_id,user_id, 1);
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
                        delayRefresh();
                        //setHoldFlats();
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

                                            JsonArray data = JsonObjectResponse.body().get("data").getAsJsonArray();
                                            if (data != null && !data.isJsonNull())
                                            {
                                                itemArrayList.clear();
                                                for (int i = 0; i < data.size(); i++) {
                                                    setJson(data.get(i).getAsJsonObject());
                                                }
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
        HoldFlatModel model = new HoldFlatModel();
        if (jsonObject.has("unit_hold_release_id")) model.setFlat_hold_release_id(!jsonObject.get("unit_hold_release_id").isJsonNull() ? jsonObject.get("unit_hold_release_id").getAsInt() : 0 );
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("lead_uid")) model.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        if (jsonObject.has("token_id")) model.setToken_id(!jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0 );
        if (jsonObject.has("token_no")) model.setToken_number(!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() : "" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "91" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("block_name")) model.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "" );
        if (jsonObject.has("unit_id")) model.setUnit_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setUnit_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "" );
        if (jsonObject.has("unit_category")) model.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("hold_datetime")) model.setHold_datetime(!jsonObject.get("hold_datetime").isJsonNull() ? jsonObject.get("hold_datetime").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("countUp")) model.setCountUp(!jsonObject.get("countUp").isJsonNull() ? jsonObject.get("countUp").getAsLong() : 0 );
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("floor_id")) model.setFloor_id(!jsonObject.get("floor_id").isJsonNull() ? jsonObject.get("floor_id").getAsInt() : 0 );
        if (jsonObject.has("block_id")) model.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );
        if (jsonObject.has("unit_type_id")) model.setUnit_type_id(!jsonObject.get("unit_type_id").isJsonNull() ? jsonObject.get("unit_type_id").getAsInt() : 0 );


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
        model.setCuidModel(cuidModel);

        itemArrayList.add(model);
    }



    private void delayRefresh()    {

        //reply
        if (context!=null)
        {
            context.runOnUiThread(() -> {


                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);

                LinearLayoutManager manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new ShHoldFlatListAdapter(this, itemArrayList,api_token);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                Log.e(TAG,"count "+ itemArrayList.size());
                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                } else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //setClickable(recyclerView, false);
                }
            });
        }
    }


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            swipeRefresh.setRefreshing(false);
            Helper.onErrorSnack(context, message);
        });

    }


    // Overflow Menu
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
        if (fromAddHoldFlat) {
            //if directly came from add hold flat
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }

    }

}
