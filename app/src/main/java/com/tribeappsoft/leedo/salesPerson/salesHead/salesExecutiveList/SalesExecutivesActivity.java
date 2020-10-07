package com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.adapter.SalesExecutiveAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.model.SalesExecutiveModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

public class SalesExecutivesActivity extends AppCompatActivity {

    @BindView(R.id.sr_salesExecutiveActivity) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_salesExecutiveActivity) RecyclerView recyclerView;
    @BindView(R.id.ll_salesExecutiveActivity_noData) LinearLayoutCompat ll_noData;
    Activity context;
    private String TAG = "SalesPersonsActivity",api_token ="";
    private ArrayList<SalesExecutiveModel> teamLeadArrayList;
    private SalesExecutiveAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_persons);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= SalesExecutivesActivity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_sales_executive));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        teamLeadArrayList=new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

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
        recyclerAdapter = new SalesExecutiveAdapter(context, teamLeadArrayList);
        recyclerView.setAdapter(recyclerAdapter);

    }
    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getSalesExecutives();
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


    //On Resume
    @Override
    public void onResume() {
        super.onResume();
        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getSalesExecutives();
        }
        else {
            Helper.NetworkError(context);
        }
    }

    private void call_getSalesExecutives()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllSalesPersons(api_token);
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
                        delayRefresh();
                        // swipeRefresh.setRefreshing(false);
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
                                if (!JsonObjectResponse.body().isJsonNull() && JsonObjectResponse.body().isJsonObject())
                                {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess == 1) {

                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                teamLeadArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }

                                            }

                                        }
                                    }else showErrorLog("Invalid response from server!");

                                }else showErrorLog("Server response is empty!");
                            } else {
                                // error from server
                                showErrorLog("Unknown error occurred from server! Try again.");
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

    }//call_get_salesExecutive list


    private void setJson(JsonObject jsonObject)
    {
        SalesExecutiveModel model=new SalesExecutiveModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("person_id")) model.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("is_team_lead")) model.setIs_team_lead(!jsonObject.get("is_team_lead").isJsonNull() ? jsonObject.get("is_team_lead").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setPhotopath(!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : "");

        teamLeadArrayList.add(model);

        // Log.e(TAG, "setJson: "+itemArrayList );
    }

    private void delayRefresh()
    {
        if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new SalesExecutiveAdapter(context, teamLeadArrayList);
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
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
