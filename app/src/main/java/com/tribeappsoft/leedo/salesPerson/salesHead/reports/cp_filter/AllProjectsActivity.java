package com.tribeappsoft.leedo.salesPerson.salesHead.reports.cp_filter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.cp_filter.adapter.SelectProjectRecyclerAdapter;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllProjectsActivity extends AppCompatActivity {

    @BindView(R.id.sr_allProjects) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_allProjects) RecyclerView recyclerView;
    @BindView(R.id.ll_allProjects_noData) LinearLayoutCompat ll_noData;

    private Activity context;
    private ArrayList<ProjectModel> itemArrayList;
    private String TAG = "AllProjectsActivity", api_token ="";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_projects);
        ButterKnife.bind(this);
        context = AllProjectsActivity.this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>Select Project</font>"));
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

        //init ArrayList
        itemArrayList = new ArrayList<>();

        //setup recyclerView
        setUpRecyclerView();

        //set up swipe refresh
        setSwipeRefresh();


        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            swipeRefresh.setRefreshing(true);
            call_getAllProjects();
        }
        else {
            Helper.NetworkError(context);
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible no data
            ll_noData.setVisibility(View.VISIBLE);

        }
    }

    //SetUpRecycler
    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SelectProjectRecyclerAdapter(context, itemArrayList, model -> {
            if (sharedPreferences!=null)
            {
                editor= sharedPreferences.edit();
                editor.putLong("filterProjectId", model.getProject_id());
                editor.putString("filterProjectName", model.getProject_name());
                editor.apply();
            }

            //on Back pressed
            onBackPressed();

        }));

    }


    public void setSwipeRefresh() {
        //getOffline();

        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                swipeRefresh.setRefreshing(true);
                new Thread(this::call_getAllProjects).start();
            }
            else {
                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
            }
        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    private void call_getAllProjects()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAllProjects(api_token).enqueue(new Callback<JsonObject>()
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
                                //set json
                                setJson(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() -> delayRefresh(), 1000);
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                //clear list
                itemArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setProjectJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }


    private void setProjectJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        itemArrayList.add(model);
    }


    //Delay Refresh
    private void delayRefresh() {

        if (context != null) {

            runOnUiThread(() ->
            {

                swipeRefresh.setRefreshing(false);

                Log.e(TAG, "delayRefresh: sz "+ itemArrayList.size() );
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(new SelectProjectRecyclerAdapter(context, itemArrayList, model -> {
                    if (sharedPreferences!=null)
                    {
                        editor= sharedPreferences.edit();
                        editor.putLong("filterProjectId", model.getProject_id());
                        editor.putString("filterProjectName", model.getProject_name());
                        editor.apply();
                    }

                    onBackPressed();

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


    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
                Helper.onErrorSnack(context,message);
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
