package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatBookingDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatGHPDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatLeadDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatSiteVisitDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamLeadStatsModel;
import com.tribeappsoft.leedo.admin.reports.teamStats.model.TeamStatsModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.ZoomLinearLayout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class TeamLeadStatsActivity extends AppCompatActivity {

    @BindView(R.id.nsv_teamLeadStats) NestedScrollView nsv;
    @BindView(R.id.ll_teamLeadStats_main) LinearLayoutCompat ll_main;
    @BindView(R.id.zll_teamLeadStats)
    ZoomLinearLayout zll;
    @BindView(R.id.ll_teamLeadStats_Stat) LinearLayoutCompat ll_Stat;

    @BindView(R.id.ll_teamLeadStats_addStats) LinearLayoutCompat ll_addStats;
    @BindView(R.id.mTv_itemTeamLeadStats_noTeamLeads) MaterialTextView mTv_noTeamLeads;
    @BindView(R.id.ll_teamLeadStats_selectProject) LinearLayoutCompat ll_selectProject;
    @BindView(R.id.mAcTv_teamLeadStats_select_project) MaterialAutoCompleteTextView mAcTv_select_project;
    @BindView(R.id.edt_teamLeadStats_FromDate) TextInputEditText edt_performanceFromDate;
    @BindView(R.id.edt_teamLeadStats_ToDate) TextInputEditText edt_performanceToDate;

    @BindView(R.id.ll_teamLeadStats_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_teamLeadStats_pb) LinearLayoutCompat ll_pb;
    @BindView(R.id.sfl_teamLeadStats) ShimmerFrameLayout sfl;


    private Activity context;
    private String TAG = "TeamStatsActivity",api_token ="", selectedProjectName="",sendPerformanceFromDate = null, sendPerformanceToDate = null;
    private int user_id=0,selectedProjectId =0,mYear, mMonth, mDay;
    private DatePickerDialog datePickerDialog;
    //String ProjectArrayList
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<String> projectStringArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_lead_stats);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = TeamLeadStatsActivity.this;
        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_teamLeadStats));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //init
        init();

        //Get team stats
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            //show shimmer
            showShimmer();
            new Handler().postDelayed(this::call_getTeamLeadWiseStatsData, 1000);
        }
        else {

            //show network error
            Helper.NetworkError(context);

            //hide shimmer
            hideShimmer();

            //hide main layout
            nsv.setVisibility(View.GONE);
            ll_main.setVisibility(View.GONE);
            ll_selectProject.setVisibility(View.GONE);
            //show no data
            ll_noData.setVisibility(View.VISIBLE);
        }

        //set visit date time
        edt_performanceFromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        edt_performanceToDate.setOnClickListener(view -> {
            if (sendPerformanceFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(this, "Please select from date first!");
        });


    }

    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(this),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_performanceFromDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPerformanceFromDate = year + "-" + mth + "-" + dayOfMonth;

                    if (Helper.isNetworkAvailable(Objects.requireNonNull(this)))
                    {
                        //  showShimmer();
                        ll_pb.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(this::call_getTeamLeadWiseStatsData, 100);
                    }

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "From Date: "+ sendPerformanceFromDate);


                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(this),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_performanceToDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPerformanceToDate = year + "-" + mth + "-" + dayOfMonth;

                    if (Helper.isNetworkAvailable(Objects.requireNonNull(this)))
                    {
                        // showShimmer();
                        ll_pb.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(this::call_getTeamLeadWiseStatsData, 100);
                    }

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendPerformanceToDate);

                    //check button EnabledView
                    // checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(Helper.getLongNextDateFromString(sendPerformanceFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void init()
    {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        projectModelArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        nsv.setVisibility(View.GONE);
        ll_main.setVisibility(View.GONE);
        ll_selectProject.setVisibility(View.GONE);


        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            //scrolling up and down
            if(scrollY<oldScrollY){
                //vertical scrolling down
                Log.d(TAG, "onCreateView: scrolling down" );

                //show views
                //showViews();

            }else{

                //vertical scrolling upp
                Log.d(TAG, "onCreateView: scrolling up" );

                //hide views
                hideViews();
            }

            //reached at top of scrollView
            if (!nsv.canScrollVertically(-1)) {

                Log.d(TAG, "onCreateView: TOP of scrollView" );
                // top of scroll view

                showViews();
            }
        });


    }

    private void hideViews()
    {
        ll_selectProject.animate().translationY(-ll_selectProject.getHeight()).setInterpolator(new AccelerateInterpolator(3));
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_selectProject.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void call_getTeamLeadWiseStatsData()
    {
        //showProgressBar("loading inventory...");
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().GetTeamLeadWiseReport(api_token, user_id,sendPerformanceFromDate,sendPerformanceToDate);
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
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        setTeamStatsData();
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
                        if(JsonObjectResponse.isSuccessful()) {
                            if(JsonObjectResponse.body()!=null) {
                                if(!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess ==0) {
                                        //no team created
                                        if (JsonObjectResponse.body().has("msg")) showErrorLog(!JsonObjectResponse.body().get("msg").isJsonNull()? JsonObjectResponse.body().get("msg").getAsString() : "You have not assigned any team members!");
                                        else showErrorLog("You have not assigned any team members!");
                                    }
                                    else if (isSuccess==1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {

                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                projectModelArrayList.clear();
                                                projectStringArrayList.clear();

                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setPerformanceDetailsJson(jsonArray.get(i).getAsJsonObject());
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



    private void setPerformanceDetailsJson(JsonObject jsonObject) {

        ProjectModel model =new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }

        if (jsonObject.has("teamStats") && !jsonObject.get("teamStats").isJsonNull()) {
            if (!jsonObject.get("teamStats").isJsonNull() && jsonObject.get("teamStats").isJsonArray()) {
                ArrayList<TeamLeadStatsModel> teamLeadStatsModelArrayList = new ArrayList<>();
                teamLeadStatsModelArrayList.clear();
                JsonArray jsonArray = jsonObject.get("teamStats").getAsJsonArray();
                if (jsonArray.size()>0) {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setTeamLeadStatsJson(jsonArray.get(j).getAsJsonObject(), teamLeadStatsModelArrayList);
                    }

                    model.setTeamLeadStatsModelArrayList(teamLeadStatsModelArrayList);
                }
            }
        }

        projectModelArrayList.add(model);
    }

    private void setTeamLeadStatsJson(JsonObject jsonObject, ArrayList<TeamLeadStatsModel> teamLeadStatsModelArrayList)
    {
        TeamLeadStatsModel model = new TeamLeadStatsModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("sales_team_lead_id")) model.setSales_team_lead_id(!jsonObject.get("sales_team_lead_id").isJsonNull() ? jsonObject.get("sales_team_lead_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );


        if (jsonObject.has("stats")) {
            if (!jsonObject.get("stats").isJsonNull() && jsonObject.get("stats").isJsonArray()) {
                ArrayList<TeamStatsModel> statsModelArrayList = new ArrayList<>();
                statsModelArrayList.clear();
                JsonArray jsonArray = jsonObject.get("stats").getAsJsonArray();
                if (jsonArray.size()>0) {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setPerformanceStatsJson(jsonArray.get(j).getAsJsonObject(), statsModelArrayList);
                    }
                }
                model.setTeamStatsModelArrayList(statsModelArrayList);
            }
        }

        if (jsonObject.has("totalstats")) {
            if (!jsonObject.get("totalstats").isJsonNull() && jsonObject.get("totalstats").isJsonObject()) {
                JsonObject object = jsonObject.get("totalstats").getAsJsonObject();
                if (object.has("leads_site_visits")) model.setLeads_site_visits(!object.get("leads_site_visits").isJsonNull() ? object.get("leads_site_visits").getAsString() : "0" );
                if (object.has("leads")) model.setLeads(!object.get("leads").isJsonNull() ? object.get("leads").getAsString() : "0" );
                if (object.has("lead_tokens_ghp")) model.setLead_tokens_ghp(!object.get("lead_tokens_ghp").isJsonNull() ? object.get("lead_tokens_ghp").getAsString() : "0" );
                if (object.has("lead_tokens_ghp_plus")) model.setLead_tokens_ghp_plus(!object.get("lead_tokens_ghp_plus").isJsonNull() ? object.get("lead_tokens_ghp_plus").getAsString() : "0" );
                if (object.has("booking_master")) model.setBooking_master(!object.get("booking_master").isJsonNull() ? object.get("booking_master").getAsString() : "0" );
            }
        }


        teamLeadStatsModelArrayList.add(model);
    }
    private void setPerformanceStatsJson(JsonObject jsonObject, ArrayList<TeamStatsModel> statsModelArrayList)
    {
        TeamStatsModel model = new TeamStatsModel();
        if (jsonObject.has("team_member_id")) model.setSales_person_id(!jsonObject.get("team_member_id").isJsonNull() ? jsonObject.get("team_member_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        //if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        //if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("leads")) model.setLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0" );
        if (jsonObject.has("leads_site_visits")) model.setLeads_site_visits(!jsonObject.get("leads_site_visits").isJsonNull() ? jsonObject.get("leads_site_visits").getAsString() : "0" );
        if (jsonObject.has("lead_tokens_ghp")) model.setLead_tokens(!jsonObject.get("lead_tokens_ghp").isJsonNull() ? jsonObject.get("lead_tokens_ghp").getAsString() : "0" );
        if (jsonObject.has("lead_tokens_ghp_plus")) model.setLead_tokens_ghp_plus(!jsonObject.get("lead_tokens_ghp_plus").isJsonNull() ? jsonObject.get("lead_tokens_ghp_plus").getAsString() : "0" );
        if (jsonObject.has("booking_master")) model.setBooking_master(!jsonObject.get("booking_master").isJsonNull() ? jsonObject.get("booking_master").getAsString() : "0" );

        statsModelArrayList.add(model);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTeamStatsData() {

        runOnUiThread(() -> {

            //hide shimmer
            hideShimmer();
            ll_pb.setVisibility(View.GONE);
            //set adapter for project names
            setAdapterProjectNames();

            // zll inventoryHome
            zll.setOnTouchListener((v, event) -> {
                zll.init(TeamLeadStatsActivity.this);
                return false;
            });


        });
    }

    private void setAdapterProjectNames()
    {
        runOnUiThread(() -> {

            if (projectStringArrayList.size() >0 &&  projectModelArrayList.size()>0)
            {
                //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, projectStringArrayList);
                mAcTv_select_project.setText(projectStringArrayList.get(0));
                mAcTv_select_project.setAdapter(adapter);
                mAcTv_select_project.setThreshold(0);

                //def set
                selectedProjectId = projectModelArrayList.get(0).getProject_id();
                selectedProjectName = projectModelArrayList.get(0).getProject_name();

                //set def 0th index data
                //setPerformanceView(0);
                setTeamLeadStatsView(0);

                nsv.setVisibility(View.VISIBLE);
                ll_main.setVisibility(View.VISIBLE);
                ll_selectProject.setVisibility(View.VISIBLE);


                mAcTv_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                {

                    String itemName = adapter.getItem(position);
                    for (ProjectModel pojo : projectModelArrayList) {

                        if (pojo.getProject_name().equals(itemName))
                        {
                            //int id = pojo.getCompany_id(); // This is the correct ID
                            selectedProjectId = pojo.getProject_id(); // This is the correct ID
                            selectedProjectName = pojo.getProject_name();

                            Log.e(TAG, "Project name & id " + selectedProjectName +"\t"+ selectedProjectId);
                            ll_pb.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(() -> {

                                //set def specific index data
                                //setPerformanceView(position);
                                setTeamLeadStatsView(position);

                            }, 1000);

                            break; // No need to keep looping once you found it.
                        }
                    }
                });

            }
        });

    }


    private void setTeamLeadStatsView(int position)
    {
        if (projectModelArrayList.get(position).getTeamLeadStatsModelArrayList()!=null && projectModelArrayList.get(position).getTeamLeadStatsModelArrayList().size()>0)
        {
            ll_addStats.removeAllViews();
            //int last =projectModelArrayList.get(position).getTeamLeadStatsModelArrayList().size()-1;
            //for (int i = projectModelArrayList.get(position).getTeamLeadStatsModelArrayList().size()-1; i>=0; i--) {
            for (int i = 0;  i < projectModelArrayList.get(position).getTeamLeadStatsModelArrayList().size();  i++) {
                View rowView_sub = getTeamLeadView(position, i);
                ll_addStats.addView(rowView_sub);
            }

            //visible add stats
            ll_addStats.setVisibility(View.VISIBLE);
            //hide no team lead
            mTv_noTeamLeads.setVisibility(View.GONE);
            //hide pb
            ll_pb.setVisibility(View.GONE);
        }
        else
        {
            //no stats handle case

            //hide add stats
            ll_addStats.setVisibility(View.GONE);
            //visible no team lead
            mTv_noTeamLeads.setVisibility(View.VISIBLE);
            //hide pb
            ll_pb.setVisibility(View.GONE);
        }
    }


    private View getTeamLeadView(int projectPosition, int position)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_team_lead_stats, null );

        final LinearLayoutCompat ll_addStats = rowView_sub.findViewById(R.id.ll_itemTeamLeadStats_addStats);
        final LinearLayoutCompat ll_totalStats = rowView_sub.findViewById(R.id.ll_itemTeamLeadStats_totalStats);
        final MaterialTextView mTv_totalLeads = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_totalLeads);
        final MaterialTextView mTv_totalSiteVisits = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_totalSiteVisits);
        final MaterialTextView mTv_totalGHP = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_totalGHP);
        final MaterialTextView mTv_totalGHP_plus = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_totalGHP_plus);
        final MaterialTextView mTv_totalAllotments = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_totalAllotments);
        final MaterialTextView mTv_noStats = rowView_sub.findViewById(R.id.mTv_itemTeamLeadStats_noStats);
        //final View view_line = rowView_sub.findViewById(R.id.view_line_itemTeamLeadStats);


        if (projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList()!=null && projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().size()>0)
        {
            ll_addStats.removeAllViews();

            //int last =projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().size()-1;
            for (int i = projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().size()-1; i>=0; i--) {
                //for (int i = 0; i< projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().size(); i++) {
                View rowView_sub_one = getPerformanceView(projectPosition, position, i);
                ll_addStats.addView(rowView_sub_one);
            }

            //set total stats
            //total leads
            mTv_totalLeads.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLeads()!=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLeads() : "0");
            //total site visits
            mTv_totalSiteVisits.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLeads_site_visits()!=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLeads_site_visits() : "0");
            //total ghp\s's
            mTv_totalGHP.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLead_tokens_ghp()!=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLead_tokens_ghp() : "0");
            //total ghp +
            mTv_totalGHP_plus.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLead_tokens_ghp_plus()!=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getLead_tokens_ghp_plus() : "0");
            //total allotments
            mTv_totalAllotments.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getBooking_master()!=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getBooking_master() : "0");


//            if(position==0) {
//                //  Log.e("i:", "position: "+position);
//                view_line.setVisibility(View.GONE);
//            }


            //visible add stats
            ll_addStats.setVisibility(View.VISIBLE);
            //visible total stats
            ll_totalStats.setVisibility(View.VISIBLE);
            //hide no stats
            mTv_noStats.setVisibility(View.GONE);
            //hide pb
            ll_pb.setVisibility(View.GONE);
        }
        else
        {
            //no stats handle case

            //hide add stats
            ll_addStats.setVisibility(View.GONE);
            //hide total stats
            ll_totalStats.setVisibility(View.GONE);
            //visible no stats
            mTv_noStats.setVisibility(View.VISIBLE);
            //hide pb
            ll_pb.setVisibility(View.GONE);
        }


        return rowView_sub;
    }




    private View getPerformanceView(int projectPosition, int position, int i)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_team_lead_performance_stats, null );

        final MaterialTextView mTv_spName = rowView_sub.findViewById(R.id.mTv_tl_stats_spName);
        final MaterialTextView mTv_leads = rowView_sub.findViewById(R.id.mTv_tl_stats_leads);
        final MaterialTextView mTv_siteVisits = rowView_sub.findViewById(R.id.mTv_tl_stats_siteVisits);
        final MaterialTextView mTv_ghp = rowView_sub.findViewById(R.id.mTv_tl_stats_ghp);
        final MaterialTextView mTv_ghpPlus = rowView_sub.findViewById(R.id.mTv_tl_stats_ghpPlus);
        final MaterialTextView mTv_allotments = rowView_sub.findViewById(R.id.mTv_tl_stats_allotments);
        final View view_line = rowView_sub.findViewById(R.id.view_line_tl_stats);
        final LinearLayoutCompat ll_teamStats = rowView_sub.findViewById(R.id.ll_teamStats);


        //if(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id()==user_id)


        //set sales person name
        mTv_spName.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name() : "");
        //set leads
        mTv_leads.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLeads() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLeads() : "0");
        //set site visits
        mTv_siteVisits.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLeads_site_visits() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLeads_site_visits() : "0");
        //set ghp
        mTv_ghp.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLead_tokens() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLead_tokens() : "0");
        //set ghp plus
        mTv_ghpPlus.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLead_tokens_ghp_plus() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getLead_tokens_ghp_plus() : "0");
        //set Allotments
        mTv_allotments.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getBooking_master() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getBooking_master() : "0");

        if(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getSales_team_lead_id() == projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id())
        {
            //sales team lead highlighted by yellow color
            //ll_teamStats.setBackgroundColor(getResources().getColor(R.color.bgColor));
            mTv_spName.setText(projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name() !=null ? projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name() + " (TL)" : "");
            mTv_spName.setTextAppearance(context, R.style.styleFontBoldText);
        }


        mTv_siteVisits.setOnClickListener(v -> {
            Log.e(TAG,"salespersonId"+projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
            if(Integer.parseInt(mTv_siteVisits.getText().toString()) > 0){
                Intent intent = new Intent(context, StatSiteVisitDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name());
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                startActivity(intent);
            }
            else {
                new Helper().showCustomToast(TeamLeadStatsActivity.this,"Details not Available!");
            }
        });

        mTv_leads.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_leads.getText().toString()) > 0){
                Intent intent = new Intent(context, StatLeadDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamLeadStatsActivity.this,"Details not Available!");
            }
        });

        mTv_ghp.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_ghp.getText().toString()) > 0){
                Intent intent = new Intent(context, StatGHPDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("flag",1);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamLeadStatsActivity.this,"Details not Available!");
            }
        });

        mTv_ghpPlus.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_ghpPlus.getText().toString()) > 0){
                Intent intent = new Intent(context, StatGHPDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("flag",3);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamLeadStatsActivity.this,"Details not Available!");
            }
        });

        mTv_allotments.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_leads.getText().toString()) > 0){
                Intent intent = new Intent(context, StatBookingDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamLeadStatsModelArrayList().get(position).getTeamStatsModelArrayList().get(i).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamLeadStatsActivity.this,"Details not Available!");
            }
        });


        if(i==0) {
            //  Log.e("i:", "position: "+position);
            view_line.setVisibility(View.GONE);
        }

        return rowView_sub;
    }


    private void showShimmer() {
        sfl.setVisibility(View.VISIBLE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
    }

    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            Objects.requireNonNull(context).runOnUiThread(() -> {


                //hide shimmer effect
                hideShimmer();

                //hide pb
                ll_pb.setVisibility(View.GONE);

                Helper.onErrorSnack(context,message);

                //hide main layout
                nsv.setVisibility(View.GONE);
                ll_main.setVisibility(View.GONE);
                ll_selectProject.setVisibility(View.GONE);
                //show no data
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
