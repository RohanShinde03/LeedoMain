package com.tribeappsoft.leedo.admin.offlineLeads;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.adapter.OfflineLeadListAdapter;
import com.tribeappsoft.leedo.admin.offlineLeads.model.OfflineLeadModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.NetworkStateReceiver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class AllOfflineLeads_Activity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    @BindView(R.id.exFab_allOfflineLeads_addLead) ExtendedFloatingActionButton exFab_addLead;
    @BindView(R.id.mBtn_allOfflineLeads_viewDuplicateLeads) MaterialButton mBtn_viewDuplicateLeads;
    @BindView(R.id.sw_allOfflineLeads) SwipeRefreshLayout sw_offlineLeads;
    @BindView(R.id.rv_allOfflineLeads) RecyclerView rv_offline_leads;
    @BindView(R.id.ll_allOfflineLeads_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.mTv_allOfflineLeads_LastSyncTime) AppCompatTextView mTv_LastSyncTime;

    Activity context;
    private ArrayList<OfflineLeadModel> offlineLeadModelArrayList,temp_arrayList;
    private OfflineLeadListAdapter offlineLeadListAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String api_token="",lead_sync_time="";
    private String TAG="AllOfflineLeads_Activity";
    private int user_id =0;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_offline_leads);

        ButterKnife.bind(this);
        context= AllOfflineLeads_Activity.this;

        offlineLeadModelArrayList =new ArrayList<>();
        temp_arrayList =new ArrayList<>();

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.all_offline_Leads));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

        //set offline leads
        setOfflineLeads();

        exFab_addLead.setOnClickListener(v -> {
            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                startActivity(new Intent(context, AddNewLeadActivity.class));
            }else startActivity(new Intent(context, AddNewOfflineLeadActivity.class));
        });

        mBtn_viewDuplicateLeads.setOnClickListener(v -> startActivity(new Intent(context, DuplicateLeads_Activity.class)));

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onResume() {
        super.onResume();

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        lead_sync_time = sharedPreferences.getString("lead_sync_time", "");
        //,total_offline_leads=0;
        int total_duplicate_leads = sharedPreferences.getInt("total_duplicate_leads", 0);
//        Log.e(TAG, "onResume: total_offline_leads"+total_offline_leads );
        editor.apply();

        mTv_LastSyncTime.setText(lead_sync_time!=null&& !lead_sync_time.trim().isEmpty() ? lead_sync_time :"not synced yet");

        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            mBtn_viewDuplicateLeads.setText(total_duplicate_leads !=0 ? "("+ total_duplicate_leads +")"+context.getString(R.string.view_duplicate_leads) :context.getString(R.string.view_duplicate_leads));
            new Handler().postDelayed(() -> getLastOfflineSyncedTime(false),100);
        } else NetworkError(context);

    }

    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        rv_offline_leads.setLayoutManager(linearLayoutManager);
        rv_offline_leads.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv_offline_leads.getContext(), linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
        if (verticalDivider != null) {
            dividerItemDecoration.setDrawable(verticalDivider);
        }
        rv_offline_leads.addItemDecoration(dividerItemDecoration);
        offlineLeadListAdapter = new OfflineLeadListAdapter(context, offlineLeadModelArrayList);
        rv_offline_leads.setAdapter(offlineLeadListAdapter);
    }

    public void setSwipeRefresh()
    {

        sw_offlineLeads.setOnRefreshListener(() -> {
            sw_offlineLeads.setRefreshing(true);
            //set offline leads
            setOfflineLeads();

            //check offline leads if available
            //checkOfflineLeads();
        });
        sw_offlineLeads.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }
    private void setOfflineLeads() {
        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        if (sharedPreferences!=null)
        {
            //set cached feed only for WO filtered data
            String offlineData = null;
            if (sharedPreferences.getString("DownloadModel", null) != null) offlineData = sharedPreferences.getString("DownloadModel", null);

            if (offlineData !=null) {

                offlineLeadModelArrayList.clear();
                temp_arrayList.clear();
                //hide pb
                hideCancellationProgressBar();

                JsonArray jsonArray = new Gson().fromJson(offlineData, JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }
                temp_arrayList.addAll(offlineLeadModelArrayList);

                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putInt("DownloadModelcount", jsonArray.size());
                    editor.apply();
                    Log.e(TAG, "offline lead count: "+jsonArray.size());
                }

                //set offline  data
                setOfflineLeadsAdapter();
            }
            else {
                //not available
                rv_offline_leads.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            }
        }

        runOnUiThread(() -> {
            if (sw_offlineLeads !=null) sw_offlineLeads.setRefreshing(false);
            hideCancellationProgressBar();
        });
    }

    private void checkOfflineLeads()
    {
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.apply();
            String offlineData = null;
            if (sharedPreferences.getString("DownloadModel", null)!=null) offlineData = sharedPreferences.getString("DownloadModel", null);

            if (isNetworkAvailable(Objects.requireNonNull(context))) {
                if (offlineData !=null)
                {
                    final JsonObject jsonObject = new JsonObject();
                    Gson gson  = new Gson();
                    JsonArray jsonArray = gson.fromJson(offlineData, JsonArray.class);
                    jsonObject.addProperty("api_token",api_token);
                    jsonObject.add("offline_leads",jsonArray);

                    new Helper().onSnackForHomeLeadSync(context,"New offline leads detected! Syncing now...");
                    new Handler().postDelayed(() -> call_SyncOfflineLeads(jsonObject),3000);
                }

            }
        }
    }

    private void setOfflineLeadsAdapter()
    {
        if (sw_offlineLeads !=null) sw_offlineLeads.setRefreshing(false);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        rv_offline_leads.setLayoutManager(manager);
        rv_offline_leads.setHasFixedSize(true);
        offlineLeadListAdapter = new OfflineLeadListAdapter(context,offlineLeadModelArrayList);
        rv_offline_leads.setAdapter(offlineLeadListAdapter);
        offlineLeadListAdapter.notifyDataSetChanged();

        int count = Objects.requireNonNull(rv_offline_leads.getAdapter()).getItemCount();
        if (count==0) {
            //no notifications available
            rv_offline_leads.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }
        else
        {
            //notifications available
            rv_offline_leads.setVisibility(View.VISIBLE);
            ll_noData.setVisibility(View.GONE);

        }
        if (sw_offlineLeads !=null) sw_offlineLeads.setRefreshing(false);

    }

    private void call_SyncOfflineLeads(JsonObject jsonObject)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().add_OfflineLeads(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0, total_duplicate_leads =0;
                        String status_msg = null;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (response.body().has("duplicate_leads")) total_duplicate_leads = response.body().get("duplicate_leads").getAsInt();
                        if (response.body().has("status_msg")) status_msg = response.body().get("status_msg").getAsString();


                        if (isSuccess==1)
                        {
                            // clear shared pref of offline leads
                            if (sharedPreferences!=null)
                            {
                                editor = sharedPreferences.edit();
                                editor.putInt("total_duplicate_leads", total_duplicate_leads);
                                editor.remove("DownloadModel");
                                editor.remove("DownloadModelcount");
                                editor.apply();
                            }

                            onSuccessSync(status_msg);

                        }
                        else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                            showErrorLog(getString(R.string.unknown_error_try_again));
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

    private void onSuccessSync(String status_msg)
    {
        if (context!=null)
        { runOnUiThread(() -> {
            setOfflineLeads();
            hideCancellationProgressBar();

            new Handler().postDelayed(() -> {

                Toast.makeText(context, status_msg != null ? status_msg : getString(R.string.offline_lead_synced_successfully), Toast.LENGTH_LONG).show();
                //new Helper().showSuccessCustomToast(context, status_msg != null ? status_msg : getString(R.string.offline_lead_synced_successfully));
            },2000);

            if (isNetworkAvailable(context)) {

                new Handler().postDelayed(() -> {
                    new Helper().onSnackForHomeLeadSync(context,"Updating last sync time...");

                    // call get update last sync time
                    getLastOfflineSyncedTime(true);
                }, 1500);

            }

        });
        }
    }


    private void getLastOfflineSyncedTime(boolean showMsg)
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
                                if (response.body().has("data")) {
                                    if (response.body().has("data")) lead_sync_time = !response.body().get("data").isJsonNull() ? response.body().get("data").getAsString() :"not synced yet";
                                }
                                //set delayRefresh
                                new Handler().postDelayed(() -> delayRefresh(showMsg), 1000);
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

    private void delayRefresh(boolean showMsg) {
        if(context!=null)
        {
            context.runOnUiThread(() -> {

                mTv_LastSyncTime.setText(lead_sync_time);
                if (showMsg) new Helper().showCustomToast(context, "Last Sync time updated successfully!");
            });

            if (sharedPreferences != null) {
                editor = sharedPreferences.edit();
                editor.putString("lead_sync_time", lead_sync_time);
                editor.apply();
            }
        }
    }



    private void showErrorLog(final String message)
    {
        if(context!=null)
        {
            runOnUiThread(() ->{
                hideCancellationProgressBar();
                if (sw_offlineLeads !=null) sw_offlineLeads.setRefreshing(false);
                ll_noData.setVisibility(View.VISIBLE);
                rv_offline_leads.setVisibility(View.GONE);
                onErrorSnack(AllOfflineLeads_Activity.this,message);
            });
        }
    }


    private void setJson(JsonObject jsonObject){

        OfflineLeadModel model = new OfflineLeadModel();

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
        if (jsonObject.has("unit_type")) model.setCustomer_unit_type(!jsonObject.get("unit_type").isJsonNull() ? jsonObject.get("unit_type").getAsString() :"");
        if (jsonObject.has("unit_category_id")) model.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_type")) model.setCustomer_unit_type(!jsonObject.get("unit_type").isJsonNull() ? jsonObject.get("unit_type").getAsString() : "" );
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

    private void hideCancellationProgressBar() {
        ll_pb.setVisibility(View.GONE);
        if (sw_offlineLeads !=null) sw_offlineLeads.setRefreshing(false);
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showCancellationProgressBar(String msg) {
        hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(msg);
        ll_pb.setVisibility(View.VISIBLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_self, menu);

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search_self);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(context).getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search_offline_lead));

            //Code for changing the search icon
            ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            // icon.setColorFilter(Color.WHITE);
            icon.setImageResource(R.drawable.ic_home_search2);

            //AutoCompleteTextView searchTextView =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            AutoCompleteTextView searchTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            /// Code for changing the textColor and hint color for the search view
            searchTextView.setHintTextColor(getResources().getColor(R.color.main_medium_grey));
            searchTextView.setTextColor(getResources().getColor(R.color.main_black));

            //Code for changing the voice search icon
            //ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            //voiceIcon.setImageResource(R.drawable.my_voice_search_icon);

            //Code for changing the close search icon
            ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            // closeIcon.setColorFilter(Color.WHITE);
            // closeIcon.setImageResource(R.drawable.ic_search_close_icon);
            closeIcon.setColorFilter(getResources().getColor(R.color.close_icon_gray));

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
                    //if (!newText.trim().isEmpty()) {
                    //doFilter(newText);
                    // }
                    return false;
                }
            });


            searchView.setOnCloseListener(() -> {
                Log.e(TAG, "onCreateOptionsMenu: onClose ");
                doFilter("");
                return false;
            });
        }
        if (searchView != null) {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
            }
        }
        return true;
    }

    // Filter Class
    public void doFilter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        offlineLeadModelArrayList.clear();

        if (charText.length() == 0)
        {
            offlineLeadModelArrayList.addAll(temp_arrayList);
        }
        else
        {
            for (OfflineLeadModel _obj : temp_arrayList)
            {
                if (_obj.getCustomer_name().toLowerCase(Locale.getDefault()).contains(charText)
                        ||_obj.getCustomer_email().toLowerCase(Locale.getDefault()).contains(charText)
                        ||_obj.getCustomer_unit_type().toLowerCase(Locale.getDefault()).contains(charText)
                        ||_obj.getAddress_line_1().toLowerCase(Locale.getDefault()).contains(charText)
                        ||_obj.getLead_stage().toLowerCase(Locale.getDefault()).contains(charText)
                )

                {
                    offlineLeadModelArrayList.add(_obj);
                }

            }
        }
        setOfflineLeadsAdapter();
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

    @Override
    public void networkAvailable() {
        Log.e(TAG, "I'm in, baby!");
        //new Helper().showCustomToast(context, "Network Available!");
        new Handler().postDelayed(() -> {

            new Helper().onSnackForHomeNetworkAvailable(context,"Device Network Available!");

            //check offline leads available for sync
            //checkOfflineLeads();
            //set offline leads
            new Handler().postDelayed(this::setOfflineLeads,5000);
        }, 1000);

    }

    @Override
    public void networkUnavailable() {
        Log.d(TAG, "I'm dancing with myself");
        //new Helper().showCustomToast(context, "Network Lost again!");

        new Handler().postDelayed(() -> new Helper().onSnackForHomeLeadSync(context,"Oops, Device Network Lost..."), 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

}