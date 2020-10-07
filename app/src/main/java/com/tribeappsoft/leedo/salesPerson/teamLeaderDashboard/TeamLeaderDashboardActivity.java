package com.tribeappsoft.leedo.salesPerson.teamLeaderDashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatBookingDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatGHPDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatLeadDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatSiteVisitDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.teamLeaderDashboard.filter.Filter_TeamLeaderStats;
import com.tribeappsoft.leedo.salesPerson.teamLeaderDashboard.model.MyTeamLeaderStatsModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class TeamLeaderDashboardActivity extends AppCompatActivity {


    @BindView(R.id.tv_teamLeaderDashboard_project_name)
    AutoCompleteTextView acTv_select_project;
    @BindView(R.id.tv_teamLeaderDashboard_select_SalesPerson) AutoCompleteTextView acTv__select_SalesPerson;

    //unclaimed leads
    @BindView(R.id.ll_teamLeaderDashboard_unClaimedLeadsBlock) LinearLayoutCompat ll_unClaimedLeadsBlock;
    @BindView(R.id.tv_teamLeaderDashboard_UnclaimedLeads)
    AppCompatTextView tv_UnclaimedLeads;
    //claimed leads
    @BindView(R.id.ll_teamLeaderDashboard_leadsBlock) LinearLayoutCompat ll_leadsBlock;
    @BindView(R.id.tv_teamLeaderDashboard_TotalLeads) AppCompatTextView tv_TotalLeads;
    //Site visits
    @BindView(R.id.ll_teamLeaderDashboard_siteVisitsBlock) LinearLayoutCompat ll_siteVisitsBlock;
    @BindView(R.id.tv_teamLeaderDashboard_siteVisits) AppCompatTextView tv_siteVisits;
    //GHP generated
    @BindView(R.id.ll_teamLeaderDashboard_ghp_generated_block) LinearLayoutCompat ll_ghp_generated_block;
    @BindView(R.id.tv_teamLeaderDashboard_token_generated) AppCompatTextView tv_token_generated;
    //GHP+ generated
    @BindView(R.id.ll_teamLeaderDashboard_ghpPlus_generated_block) LinearLayoutCompat ll_ghpPlus_generated_block;
    @BindView(R.id.tv_teamLeaderDashboard_token_plus_generated) AppCompatTextView tv_token_plus_generated;
    //GHP payment pending
    @BindView(R.id.ll_teamLeaderDashboard_ghp_pending_block) LinearLayoutCompat ll_ghp_pending_block;
    @BindView(R.id.tv_teamLeaderDashboard_ghpPaymentPending) AppCompatTextView tv_ghpPaymentPending;

    //Allotments
    @BindView(R.id.ll_teamLeaderDashboard_allotments_block) LinearLayoutCompat ll_allotments_block;
    @BindView(R.id.tv_teamLeaderDashboard_allotments) AppCompatTextView tv_allotments;

    // @BindView(R.id.tv_status_sold) AppCompatTextView tv_status_sold;
    // @BindView(R.id.tv_status_reserved) AppCompatTextView tv_status_reserved;

    //From & TO date
    @BindView(R.id.edt_teamLeaderDashboard_FromDate)
    TextInputEditText edt_teamLeaderDashboardFromDate;
    @BindView(R.id.edt_teamLeaderDashboard_ToDate) TextInputEditText edt_teamLeaderDashboardToDate;

    @BindView(R.id.ll_achievements_pb) LinearLayoutCompat ll_pb;
    @BindView(R.id.nsv_teamLeaderDashboard)
    NestedScrollView nsv_stats;
    @BindView(R.id.sfl_myPerformance_stats)
    ShimmerFrameLayout sfl;
    @BindView(R.id.ll_teamLeaderDashboard_noData) LinearLayoutCompat ll_noData;

    private Activity context;
    private String TAG = "TeamLeaderDashboardActivity",api_token ="",from_date="",to_date="",sales_person_name="",project_name="",event_name = "",cp_name = "",lead_status = "";
    private MyTeamLeaderStatsModel myTeamLeaderStatsModel = null;
    // private ArrayList<MySalesHeadStatsModel> myPerformanceModelArrayList;
    private int sales_person_id=0,filterCount_dash=0,project_id=0,event_id=0, cp_id = 0, lead_status_id = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActionMode mActionMode;
    private AppCompatTextView tvFilterItemCount;
    private int user_id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_leader_dashboard);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context= TeamLeaderDashboardActivity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_teamLeaderDashboard));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id",0);
        editor.apply();
        sales_person_id = user_id;

        //sr_achievements.setRefreshing(false);
        nsv_stats.setVisibility(View.GONE);
        hideProgressBar();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            // showProgressBar("Please wait...");
            showShimmer();
            new Handler().postDelayed(this::getMyPerformance, 100);
        }
        else
        {
            //hide pb
            hideProgressBar();
            Helper.NetworkError(context);

            nsv_stats.setVisibility(View.GONE);
            //show no data
            ll_noData.setVisibility(View.VISIBLE);
        }
    }

    //On Resume
    @Override
    public void onResume() {
        super.onResume();

        nsv_stats.getParent().requestChildFocus(nsv_stats, nsv_stats);

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            editor.apply();

            if(isFilter) {

                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    sales_person_id = sharedPreferences.getInt("sales_person_id", user_id);
                    if(sales_person_id == 0){
                        sales_person_id = user_id;
                    }
                    event_id = sharedPreferences.getInt("event_id",0);
                    cp_id = sharedPreferences.getInt("cp_id",0);
                    lead_status_id = sharedPreferences.getInt("lead_status_id",0);
                    filterCount_dash = sharedPreferences.getInt("filterCount_dash", 0);
                    from_date = sharedPreferences.getString("sendFromDate","");
                    to_date = sharedPreferences.getString("sendToDate", "");
                    sales_person_name = sharedPreferences.getString("sales_person_name","");
                    project_name = sharedPreferences.getString("project_name","");
                    event_name = sharedPreferences.getString("event_name","");
                    cp_name = sharedPreferences.getString("cp_name","");
                    lead_status = sharedPreferences.getString("lead_status_name","");
                    editor.apply();
                }

                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n from_date:- " +from_date+"\n to_date:- "+to_date+"\n sales_person_name:- "+sales_person_name+"\n project_name:- "+project_name );

                //reset api call
                // showProgressBar();
                if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                    showProgressBar("Please wait...");
                    //showShimmer();
                    new Handler().postDelayed(this::getMyPerformance, 100);
                }
                else
                {
                    //hide pb
                    hideProgressBar();
                    Helper.NetworkError(context);

                    nsv_stats.setVisibility(View.GONE);
                    //show no data
                    ll_noData.setVisibility(View.VISIBLE);
                }

            }
            else if (clearFilter) {

                //all filters are cleared

                Log.e(TAG, "onResume:clearFilter  ");

                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("clearFilter");
                    editor.apply();
                }

                //clear fields
                project_id = sales_person_id = filterCount_dash = event_id = lead_status_id = cp_id = 0;
                from_date = to_date = "";

                //reset api call

                resetData();
            }
        }

        //setupBadge
        setupBadge();
    }


    private void getMyPerformance() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getFilteredStatsForTeamLeader(api_token,project_id,sales_person_id,from_date,to_date,event_id,cp_id,lead_status_id);
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
                        runOnUIThread();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getResources().getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getResources().getString(R.string.weak_connection));
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
                                            Log.e(TAG, "onNext: in data" );
                                            if (/*!JsonObjectResponse.body().get("data").isJsonNull() &&*/ JsonObjectResponse.body().get("data").isJsonObject()) {

                                                Log.e(TAG, "onNext: " +JsonObjectResponse.body().get("data").getAsJsonObject().toString());
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                Log.e(TAG, "onNext: " +JsonObjectResponse.body().get("data").getAsJsonObject());
                                                setDataJson(jsonObject);
                                            }
                                            Log.e(TAG, "onNext: out data" );
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

    private void setDataJson(JsonObject jsonObject)
    {
        myTeamLeaderStatsModel = new MyTeamLeaderStatsModel();
        Log.e(TAG, "setProjectNamesJson: " );
        if (jsonObject.has("project_id")) myTeamLeaderStatsModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) myTeamLeaderStatsModel.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("unclaimedleads")) myTeamLeaderStatsModel.setUnclaimedleads(!jsonObject.get("unclaimedleads").isJsonNull() ? jsonObject.get("unclaimedleads").getAsString() : "0" );
        if (jsonObject.has("leads")) myTeamLeaderStatsModel.setTotalLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0" );
        if (jsonObject.has("sitevisit")) myTeamLeaderStatsModel.setSitevisit(!jsonObject.get("sitevisit").isJsonNull() ? jsonObject.get("sitevisit").getAsString() : "0" );
        if (jsonObject.has("ghp")) myTeamLeaderStatsModel.setGhp(!jsonObject.get("ghp").isJsonNull() ? jsonObject.get("ghp").getAsString() : "0");
        if (jsonObject.has("ghpPlus")) myTeamLeaderStatsModel.setGhpPlus(!jsonObject.get("ghpPlus").isJsonNull() ? jsonObject.get("ghpPlus").getAsString() : "0" );
        if (jsonObject.has("ghpPaymentPending")) myTeamLeaderStatsModel.setGhpPaymentPending(!jsonObject.get("ghpPaymentPending").isJsonNull() ? jsonObject.get("ghpPaymentPending").getAsString() : "0" );
        if (jsonObject.has("allotments")) myTeamLeaderStatsModel.setAllotments(!jsonObject.get("allotments").isJsonNull() ? jsonObject.get("allotments").getAsString() : "0" );
        if (jsonObject.has("projects")) myTeamLeaderStatsModel.setProjects(!jsonObject.get("projects").isJsonNull() ? jsonObject.get("projects").getAsString() : "0" );
        if (jsonObject.has("events")) myTeamLeaderStatsModel.setEvents(!jsonObject.get("events").isJsonNull() ? jsonObject.get("events").getAsString() : "0" );
        /*if (jsonObject.has("project_units") && !jsonObject.get("project_units").isJsonNull()) {
            if (jsonObject.get("project_units").isJsonObject()) {
                JsonObject jsonObjectUnits = jsonObject.get("project_units").getAsJsonObject();
                if (jsonObjectUnits.has("Sold")) myPerformanceModel.setProject_units_sold(!jsonObjectUnits.get("Sold").isJsonNull() ? jsonObjectUnits.get("Sold").getAsString() : "" );
                if (jsonObjectUnits.has("Reserved")) myPerformanceModel.setProject_units_Reserved(!jsonObjectUnits.get("Reserved").isJsonNull() ? jsonObjectUnits.get("Reserved").getAsString() : "" );
            }
        }*/
        // myPerformanceModelArrayList.add(myPerformanceModel);
    }

    private void runOnUIThread()
    {
        // sr_achievements.setRefreshing(false);

        if(context!= null)
        {
            runOnUiThread(() -> {
                //sr_achievements.setRefreshing(false);
                hideShimmer();
                hideProgressBar();

                nsv_stats.setVisibility(View.VISIBLE);
                ll_pb.setVisibility(View.GONE);

                //set adapter for project names
                //setAdapterForProjectName();

                if(myTeamLeaderStatsModel!=null)
                {

                    tv_UnclaimedLeads.setText(myTeamLeaderStatsModel.getUnclaimedleads());
                    tv_TotalLeads.setText(myTeamLeaderStatsModel.getTotalLeads());
                    tv_siteVisits.setText(myTeamLeaderStatsModel.getSitevisit());
                    tv_token_generated.setText(myTeamLeaderStatsModel.getGhp());
                    tv_token_plus_generated.setText(myTeamLeaderStatsModel.getGhpPlus());
                    tv_ghpPaymentPending.setText(myTeamLeaderStatsModel.getGhpPaymentPending());
                    tv_allotments.setText(myTeamLeaderStatsModel.getAllotments());
                }
                else {
                    nsv_stats.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                }


                //un-claimed leads click
                ll_unClaimedLeadsBlock.setOnClickListener(view -> {
                    // unclaimed list
                    openDetailsActivity(StatLeadDetailsActivity.class, ll_unClaimedLeadsBlock, 4);
                });

                //claimed leads click
                ll_leadsBlock.setOnClickListener(view -> {
                    // claimed list
                    openDetailsActivity(StatLeadDetailsActivity.class, ll_leadsBlock, 0);
                });

                //site visits  click
                ll_siteVisitsBlock.setOnClickListener(view -> {
                    // Site visits
                    openDetailsActivity(StatSiteVisitDetailsActivity.class, ll_siteVisitsBlock, 0);
                });

                //ghp click
                ll_ghp_generated_block.setOnClickListener(view -> {
                    // GHP
                    openDetailsActivity(StatGHPDetailsActivity.class, ll_ghp_generated_block, 1);
                });

                //ghp+ click
                ll_ghpPlus_generated_block.setOnClickListener(view -> {
                    //GHP plus
                    openDetailsActivity(StatGHPDetailsActivity.class, ll_ghpPlus_generated_block, 3);
                });

                //ghp payment pending click
                ll_ghp_pending_block.setOnClickListener(view -> {
                    //ghp payment pending
                    openDetailsActivity(StatGHPDetailsActivity.class, ll_ghp_pending_block, 2);
                });

                //allotments click
                ll_allotments_block.setOnClickListener(view -> {
                    //all allotments
                    openDetailsActivity(StatBookingDetailsActivity.class, ll_allotments_block, 0);
                });

            });

        }
    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }


    private void openDetailsActivity(Class aClass,  LinearLayoutCompat linearLayoutCompat, int flag)
    {

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(context, aClass);
            if (flag ==2) {
                //lead token status id
                intent.putExtra("lead_token_status_id",3);
                intent.putExtra("flag",0);
            }
            else {
                intent.putExtra("flag",flag);
            }
            if(flag == 4){
                intent.putExtra("lead_status_id",1);
                intent.putExtra("flag",1);
            }else {
                intent.putExtra("lead_status_id",lead_status_id);
            }

            intent.putExtra("sales_person_id",sales_person_id);
            intent.putExtra("project_id", project_id);
            intent.putExtra("from_date", from_date);
            intent.putExtra("to_date", to_date);
            intent.putExtra("Project_name", project_name);
            intent.putExtra("full_name", sales_person_name);
            intent.putExtra("cp_id",cp_id);
            intent.putExtra("event_id",event_id);
            intent.putExtra("cp_name",cp_name);
            intent.putExtra("event_name",event_name);
            intent.putExtra("lead_status",lead_status);
            intent.putExtra("isTeamLeaderStat",true);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = linearLayoutCompat;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());

        }, 600);

    }

    private void resetData()
    {
        if (Helper.isNetworkAvailable(context))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);

            //5. clear action mode
            if (mActionMode != null) {
                mActionMode.setTitle("");
                mActionMode.finish();
            }
            mActionMode = null;

            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("cp_id");
                editor.remove("event_id");
                editor.remove("lead_status_id");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("sales_person_name");
                editor.remove("project_name");
                editor.remove("isFilter");
                editor.putBoolean("clearFilter", false);
                editor.apply();

            }
            //clear filter fields
            project_id = sales_person_id = filterCount_dash = 0;
            from_date = to_date = sales_person_name = project_name = "";
            sales_person_id = user_id;


            // showProgressBar();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                showProgressBar("Please wait...");
                //showShimmer();
                new Handler().postDelayed(this::getMyPerformance, 100);
            }
            else
            {
                //hide pb
                hideProgressBar();
                Helper.NetworkError(context);

                nsv_stats.setVisibility(View.GONE);
                //show no data
                ll_noData.setVisibility(View.VISIBLE);
            }

            //set up badge count
            setupBadge();



        } else Helper.NetworkError(context);
    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        //tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showShimmer() {
        sfl.setVisibility(View.VISIBLE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_sales_head_filter, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_filter_);
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
                onBackPressed();
                break;

            case (R.id.action_filter_):
                startActivity(new Intent(context, Filter_TeamLeaderStats.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBadge() {

        if (tvFilterItemCount != null) {
            Log.e(TAG, "setupBadge: "+filterCount_dash );
            if (filterCount_dash == 0) {
                if (tvFilterItemCount.getVisibility() != View.GONE) {
                    tvFilterItemCount.setVisibility(View.GONE);
                }
            } else {
                tvFilterItemCount.setText(String.valueOf(Math.min(filterCount_dash, 99)));
                if (tvFilterItemCount.getVisibility() != View.VISIBLE) {
                    tvFilterItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //clear filter if applied from sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.remove("project_id");
            editor.remove("sales_person_id");
            editor.remove("cp_id");
            editor.remove("event_id");
            editor.remove("lead_status_id");
            editor.remove("from_date");
            editor.remove("to_date");
            editor.remove("isFilter");
            editor.putBoolean("clearFilter", false);
            editor.apply();
        }

        //clear fields
        project_id = sales_person_id = filterCount_dash = 0;
        sales_person_id = user_id;
        from_date = to_date = sales_person_name = project_name = "";
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
