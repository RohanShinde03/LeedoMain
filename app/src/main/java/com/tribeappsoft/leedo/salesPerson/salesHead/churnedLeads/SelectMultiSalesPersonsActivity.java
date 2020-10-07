package com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leadreassign.model.SalesPersonModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads.adapter.SelectMultiSalesPersonRecyclerAdapter;
import com.tribeappsoft.leedo.util.Helper;

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

public class SelectMultiSalesPersonsActivity extends AppCompatActivity {

    @BindView(R.id.sr_selectMultiSalesPerson) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_selectMultiSalesPerson) RecyclerView recyclerView;
    @BindView(R.id.ll_selectMultiSalesPerson_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.mBtn_selectMultiSalesPerson_confirm) MaterialButton mBtn_confirm;

    private SelectMultiSalesPersonsActivity context;
    private SelectMultiSalesPersonRecyclerAdapter recyclerAdapter;
    private ArrayList<SalesPersonModel> itemArrayList;
    private String TAG = "SelectMultiSalesPersonsActivity",api_token="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_multi_sales_persons);
        ButterKnife.bind(this);
        context= SelectMultiSalesPersonsActivity.this;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_select_sales_executives));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //init
        init();

    }

    private void init()
    {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        int user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+ api_token + "\n User id"+ user_id);
        editor.apply();

        itemArrayList = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            //call api
            swipeRefresh.setRefreshing(true);
            call_getSalesExecutives();
        }
        else {
            Helper.NetworkError(context);
        }

        mBtn_confirm.setOnClickListener(v -> checkValidation());
    }

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
        recyclerAdapter = new SelectMultiSalesPersonRecyclerAdapter(context,itemArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }


    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(context)) {

                //set swipe refreshing to true
                //call api
                swipeRefresh.setRefreshing(true);
                call_getSalesExecutives();

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
                                if (!JsonObjectResponse.body().isJsonNull() && JsonObjectResponse.body().isJsonObject()) {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();

                                                itemArrayList = new ArrayList<>();
                                                itemArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                            } else showErrorLog("Server response is empty!");

                                        }else showErrorLog("Invalid response from server!");
                                    } else {
                                        // error from server
                                        showErrorLog("Unknown error occurred from server! Try again.");
                                    }
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }

                    }
                });

    }//call team list

    private void setJson(JsonObject jsonObject)
    {
        SalesPersonModel model=new SalesPersonModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("person_id")) model.setUser_member_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        itemArrayList.add(model);
    }


    //delayRefresh
    private void delayRefresh()
    {
        //if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new SelectMultiSalesPersonRecyclerAdapter(context,itemArrayList);
                recyclerView.setAdapter(recyclerAdapter);

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no data
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);

                } else {
                    //data available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }
            });
        }

    }

    private void checkValidation() {
        if(recyclerAdapter!=null && recyclerAdapter.getUserIdArrayList().size()>0) {
            //setResult Ok
            setResult(Activity.RESULT_OK, new Intent()
                    .putExtra("result", recyclerAdapter.getUserIdArrayList())
                    .putExtra("result_names", recyclerAdapter.getUserNameArrayList()));

            Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
            onBackPressed();
        }
        else new Helper().showCustomToast(context, "Please select at least one Sales Executive!");
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




    public void showButton()
    {
        //mBtn_submit.setVisibility(View.VISIBLE);
        mBtn_confirm.setEnabled(true);
        mBtn_confirm.setBackgroundColor(getResources().getColor(R.color.main_black));
    }

    public void hideButton()
    {
        // mBtn_submit.setVisibility(View.GONE);
        mBtn_confirm.setEnabled(false);
        mBtn_confirm.setBackgroundColor(getResources().getColor(R.color.main_medium_grey));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, new Intent().putExtra("result", ""));
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
