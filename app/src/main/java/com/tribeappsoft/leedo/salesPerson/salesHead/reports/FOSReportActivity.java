package com.tribeappsoft.leedo.salesPerson.salesHead.reports;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.adapter.FOSReportRecyclerAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.model.CPFosModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;
import com.tribeappsoft.leedo.util.ZoomLinearLayout;

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

public class FOSReportActivity extends AppCompatActivity {


    @BindView(R.id.sfl_fosReport) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_fosReport_recycler) RecyclerView recyclerView;
    @BindView(R.id.ll_fosReport_notData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_fosReport_legends) LinearLayoutCompat ll_legends;
    @BindView(R.id.zll_fosReport_teamStats)
    ZoomLinearLayout zll;

    private Activity context;
    private ArrayList<CPFosModel> itemArrayList;
    private FOSReportRecyclerAdapter recyclerAdapter;
    private String TAG= "FOSReportActivity";
    private int cp_id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_o_s_report);
        ButterKnife.bind(this);
        context = FOSReportActivity.this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>FOS Report Stats</font>"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null) {
            cp_id = getIntent().getIntExtra("cp_id", 0);
        }


        //init ArrayList
        itemArrayList = new ArrayList<>();

        //setup recyclerView
        setUpRecyclerView();

        //set up swipe refresh
        setSwipeRefresh();


        if (Helper.isNetworkAvailable(context)) {
            swipeRefresh.setRefreshing(true);
            call_getFOSWiseReport();
        }
        else {
            swipeRefresh.setRefreshing(false);
            Helper.NetworkError(context);
            //hide recycler
            recyclerView.setVisibility(View.GONE);
            //hide legends
            ll_legends.setVisibility(View.GONE);
            //visible no data
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


        // zll inventoryHome
        zll.setOnTouchListener((v, event) -> {
            zll.init(FOSReportActivity.this);
            return false;
        });

    }


    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new FOSReportRecyclerAdapter(context, itemArrayList,cp_id);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(context)) {

                //set swipe refreshing to true
                swipeRefresh.setRefreshing(true);

                //call api
                call_getFOSWiseReport();
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

    private void hideViews()
    {
        ll_legends.animate().translationY(-ll_legends.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews()
    {
        ll_legends.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }


    private void call_getFOSWiseReport(){
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getCPFOSWiseReport(cp_id);
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
                        delayRefresh();
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {

                                                itemArrayList.clear();
                                                JsonArray jsonArray=JsonObjectResponse.body().get("data").getAsJsonArray();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
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

        CPFosModel teamStatsModel = new CPFosModel();
        if (jsonObject.has("cp_executive_id")) teamStatsModel.setCp_executive_id(!jsonObject.get("cp_executive_id").isJsonNull() ? jsonObject.get("cp_executive_id").getAsInt() : 0);
        if (jsonObject.has("user_id")) teamStatsModel.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0);
        if (jsonObject.has("person_id")) teamStatsModel.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0);
        if (jsonObject.has("full_name")) teamStatsModel.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("leads")) teamStatsModel.setLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0");
        if (jsonObject.has("leads_site_visits")) teamStatsModel.setLeads_site_visits(!jsonObject.get("leads_site_visits").isJsonNull() ? jsonObject.get("leads_site_visits").getAsString() : "0");
        if (jsonObject.has("lead_tokens")) teamStatsModel.setLead_tokens(!jsonObject.get("lead_tokens").isJsonNull() ? jsonObject.get("lead_tokens").getAsString() : "0");
        if (jsonObject.has("booking_master")) teamStatsModel.setBooking_master(!jsonObject.get("booking_master").isJsonNull() ? jsonObject.get("booking_master").getAsString() : "0");
        if (jsonObject.has("lead_tokens_ghp_plus")) teamStatsModel.setLead_tokens_ghp_plus(!jsonObject.get("lead_tokens_ghp_plus").isJsonNull() ? jsonObject.get("lead_tokens_ghp_plus").getAsString() : "0");
        itemArrayList.add(teamStatsModel);

    }



    //DelayRefresh
    public  void delayRefresh()
    {
        if (context != null) {
            Objects.requireNonNull(context).runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new FOSReportRecyclerAdapter(context, itemArrayList,cp_id);
                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );

                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    //hide legends
                    ll_legends.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.GONE);
                } else {

                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //visible legends
                    ll_legends.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });

        }
    }




    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                //hide recycler
                recyclerView.setVisibility(View.GONE);
                //visible no data
                ll_noData.setVisibility(View.VISIBLE);

                Helper.onErrorSnack(FOSReportActivity.this, message);
            });
        }
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
