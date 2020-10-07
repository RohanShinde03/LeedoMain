package com.tribeappsoft.leedo.salesPerson.teamStats;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatBookingDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatCancelBookingDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatLeadDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatSiteVisitDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.teamStats.model.TeamStatsModel;
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

public class TeamStatsActivity extends AppCompatActivity {

    @BindView(R.id.nsv_teamStats) NestedScrollView nsv;
    @BindView(R.id.ll_teamStats_main) LinearLayoutCompat ll_main;
    @BindView(R.id.zll_teamStats)
    ZoomLinearLayout zll;
    @BindView(R.id.ll_teamStats_Stat) LinearLayoutCompat ll_Stat;

    @BindView(R.id.mTv_teamStats_totalLeads) MaterialTextView mTv_totalLeads;
    @BindView(R.id.mTv_teamStats_totalSiteVisits) MaterialTextView mTv_totalSiteVisits;

    @BindView(R.id.mTv_teamStats_totalAllotments) MaterialTextView mTv_totalAllotments;
    @BindView(R.id.mTv_teamStats_totalCancelledAllotments) MaterialTextView mTv_totalCancelledAllotments;
    @BindView(R.id.ll_teamStats_addStats) LinearLayoutCompat ll_addStats;
    @BindView(R.id.ll_teamStats_totalStats) LinearLayoutCompat ll_totalStats;
    @BindView(R.id.mTv_teamStats_noStats) MaterialTextView mTv_noStats;
    @BindView(R.id.edt_teamStats_FromDate) TextInputEditText edt_performanceFromDate;
    @BindView(R.id.edt_teamStats_ToDate) TextInputEditText edt_performanceToDate;

    @BindView(R.id.ll_teamStats_selectProject) LinearLayoutCompat ll_selectProject;
    @BindView(R.id.mAcTv_teamStats_select_project) MaterialAutoCompleteTextView mAcTv_select_project;
    @BindView(R.id.mAcTv_teamStats_event_name) MaterialAutoCompleteTextView mAcTv_event_name;

    @BindView(R.id.ll_teamStats_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_teamStats_pb) LinearLayoutCompat ll_pb;
    @BindView(R.id.sfl_teamStats) ShimmerFrameLayout sfl;

    private Activity context;
    private String TAG = "TeamStatsActivity",api_token ="", selectedProjectName="",selectedEventName="", sendPerformanceFromDate = null, sendPerformanceToDate = null;
    private int user_id=0,selectedProjectId =0,selected_event_id=0,mYear, mMonth, mDay;
    private int eventStatusId =1; //TODO 1-> OnGoing 2-> Upcoming
    private DatePickerDialog datePickerDialog;
    //String ProjectArrayList
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<String> projectStringArrayList;
    private ArrayList<String> eventArrayList;
    private ArrayList<EventsModel> eventModelArrayList ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);
        //overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context = TeamStatsActivity.this;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(getString(R.string.menu_sales_team_report));
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

            //new Handler().postDelayed(this::call_getEvent, 100);

            new Handler().postDelayed(this::call_getTeamLeadPerformanceData, 1000);
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

    private void init()
    {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        projectModelArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();
        eventArrayList = new ArrayList<>();
        eventModelArrayList = new ArrayList<>();

        nsv.setVisibility(View.GONE);
        ll_main.setVisibility(View.GONE);
        ll_selectProject.setVisibility(View.GONE);
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
                        new Handler().postDelayed(this::call_getTeamLeadPerformanceData, 100);
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
                        new Handler().postDelayed(this::call_getTeamLeadPerformanceData, 100);
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

    private void call_getTeamLeadPerformanceData()
    {
        //showProgressBar("loading inventory...");
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().get_SalesManagerStats(api_token, user_id,selected_event_id,sendPerformanceFromDate,sendPerformanceToDate);
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

        if (jsonObject.has("stats") && !jsonObject.get("stats").isJsonNull()) {
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

        if (jsonObject.has("totalStats") && !jsonObject.get("totalStats").isJsonNull()) {
            if (!jsonObject.get("totalStats").isJsonNull() && jsonObject.get("totalStats").isJsonObject()) {
                JsonObject object = jsonObject.get("totalStats").getAsJsonObject();
                if (object.has("leads_site_visits")) model.setTotalSite_visits(!object.get("leads_site_visits").isJsonNull() ? object.get("leads_site_visits").getAsString() : "0" );
                if (object.has("leads")) model.setTotalLeads(!object.get("leads").isJsonNull() ? object.get("leads").getAsString() : "0" );
                if (object.has("lead_tokens")) model.setTotalGhp(!object.get("lead_tokens").isJsonNull() ? object.get("lead_tokens").getAsString() : "0" );
                if (object.has("lead_tokens_ghp_plus")) model.setTotalGhp_plus(!object.get("lead_tokens_ghp_plus").isJsonNull() ? object.get("lead_tokens_ghp_plus").getAsString() : "0" );
                if (object.has("booking")) model.setTotalAllotments(!object.get("booking").isJsonNull() ? object.get("booking").getAsString() : "0" );
                if (object.has("cancel_booking")) model.setCancel_booking(!object.get("cancel_booking").isJsonNull() ? object.get("cancel_booking").getAsString() : "0" );
            }
        }

        projectModelArrayList.add(model);
    }

    private void setPerformanceStatsJson(JsonObject jsonObject, ArrayList<TeamStatsModel> statsModelArrayList)
    {
        TeamStatsModel model = new TeamStatsModel();
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("leads_site_visits")) model.setLeads_site_visits(!jsonObject.get("leads_site_visits").isJsonNull() ? jsonObject.get("leads_site_visits").getAsString() : "0" );
        if (jsonObject.has("leads")) model.setLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0" );
        if (jsonObject.has("lead_tokens")) model.setLead_tokens(!jsonObject.get("lead_tokens").isJsonNull() ? jsonObject.get("lead_tokens").getAsString() : "0" );
        if (jsonObject.has("lead_tokens_ghp_plus")) model.setLead_tokens_ghp_plus(!jsonObject.get("lead_tokens_ghp_plus").isJsonNull() ? jsonObject.get("lead_tokens_ghp_plus").getAsString() : "0" );
        if (jsonObject.has("booking")) model.setBooking_master(!jsonObject.get("booking").isJsonNull() ? jsonObject.get("booking").getAsString() : "0" );
        if (jsonObject.has("cancel_booking")) model.setCancel_booking(!jsonObject.get("cancel_booking").isJsonNull() ? jsonObject.get("cancel_booking").getAsString() : "0" );

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
                zll.init(TeamStatsActivity.this);
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
                setPerformanceView(0);

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
                                setPerformanceView(position);

                            }, 1000);

                            break; // No need to keep looping once you found it.
                        }
                    }
                });

            }
        });
    }

    /**For Event**/
    private void call_getEvent() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getBookingEvents(api_token,user_id,eventStatusId);
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
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");

                        //setBanner();
                        //delayRefresh();
                        setEvents();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {

                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if (JsonObjectResponse.body().isJsonObject())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();

                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            JsonArray data  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                            if (data!=null && !data.isJsonNull())
                                            {
                                                eventModelArrayList.clear();
                                                for(int i=0;i<data.size();i++)
                                                {
                                                    setEventJson(data.get(i).getAsJsonObject());
                                                }

                                            }
                                        }
                                    }
                                    else
                                    {
                                        showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    }
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }

                    }
                });
    }

    private void setEventJson(JsonObject asJsonObject) {

        EventsModel eventModel =new EventsModel();
        if (asJsonObject.has("event_id")) eventModel.setEvent_id(!asJsonObject.get("event_id").isJsonNull() ? asJsonObject.get("event_id").getAsInt() : 0 );
        if (asJsonObject.has("event_location")) eventModel.setEvent_venue(!asJsonObject.get("event_location").isJsonNull() ? asJsonObject.get("event_location").getAsString().trim() : "event_venue" );
        if (asJsonObject.has("banner_path"))eventModel.setEvent_banner_path(!asJsonObject.get("banner_path").isJsonNull() ? asJsonObject.get("banner_path").getAsString().trim() : "event_banner_path");
        if (asJsonObject.has("event_start_date"))eventModel.setStart_date(!asJsonObject.get("event_start_date").isJsonNull() ? asJsonObject.get("event_start_date").getAsString().trim() : Helper.getTodaysDateString());
        if (asJsonObject.has("event_end_date"))eventModel.setEnd_date(!asJsonObject.get("event_end_date").isJsonNull() ? asJsonObject.get("event_end_date").getAsString().trim() : Helper.getTodaysDateString());
        if (asJsonObject.has("event_title"))eventModel.setEvent_title(!asJsonObject.get("event_title").isJsonNull() ? asJsonObject.get("event_title").getAsString().trim() : "event_title");
        if (asJsonObject.has("event_description"))eventModel.setEvent_description(!asJsonObject.get("event_description").isJsonNull() ? asJsonObject.get("event_description").getAsString().trim() : "event_description");

        if(eventModel.getEvent_title()!=null && !eventModel.getEvent_title().trim().isEmpty()){
            eventArrayList.add(eventModel.getEvent_title());
        }

        eventModelArrayList.add(eventModel);
    }

    private void setEvents()
    {
        // hideProgressBar();
        //set adapter for project names
        runOnUiThread(this::setEventAdapter);
    }

    private void setEventAdapter()
    {
        Log.e(TAG,"calledsetAdapter"+ eventArrayList.size());

        if (eventArrayList.size()>0 && eventModelArrayList.size()>0 )
        {
            Log.e(TAG,"calledsetAdapter"+ eventArrayList.size());
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, eventArrayList);
            //acTv_selectProject.setText(projectStringArrayList.get(myPosition));
            mAcTv_event_name.setAdapter(adapter);
            mAcTv_event_name.setThreshold(0);

            //def set
            selectedProjectId = eventModelArrayList.get(0).getEvent_id();
            selectedProjectName = eventModelArrayList.get(0).getEvent_title();

            mAcTv_event_name.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (EventsModel pojo : eventModelArrayList)
                {
                    if (pojo.getEvent_title().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selected_event_id = pojo.getEvent_id(); // This is the correct ID
                        selectedEventName = pojo.getEvent_title();
                        Log.e(TAG, "event name & id: "+selectedEventName + " \t" +selected_event_id);


                        if (Helper.isNetworkAvailable(Objects.requireNonNull(this)))
                        {
                            // showShimmer();
                            ll_pb.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(this::call_getTeamLeadPerformanceData, 100);
                        }else{ Helper.NetworkError(context); }

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }else {
            mAcTv_event_name.setText("Nothing to select!");
        }

    }

    private void setPerformanceView(int position)
    {
        if (projectModelArrayList.get(position).getTeamStatsModelArrayList()!=null && projectModelArrayList.get(position).getTeamStatsModelArrayList().size()>0)
        {
            ll_addStats.removeAllViews();

            int last =projectModelArrayList.get(position).getTeamStatsModelArrayList().size()-1;
            for (int i = projectModelArrayList.get(position).getTeamStatsModelArrayList().size()-1; i>=0; i--) {
                View rowView_sub = getPerformanceView(position, i,last);
                ll_addStats.addView(rowView_sub);
            }

            //set total stats
            //total leads
            mTv_totalLeads.setText(projectModelArrayList.get(position).getTotalLeads()!=null ? projectModelArrayList.get(position).getTotalLeads() : "0");
            //total site visits
            mTv_totalSiteVisits.setText(projectModelArrayList.get(position).getTotalSite_visits()!=null ? projectModelArrayList.get(position).getTotalSite_visits() : "0");
            //total ghp\s's
            //mTv_totalGHP.setText(projectModelArrayList.get(position).getTotalGhp()!=null ? projectModelArrayList.get(position).getTotalGhp() : "0");
            //total ghp +
           // mTv_totalGHP_plus.setText(projectModelArrayList.get(position).getTotalGhp_plus()!=null ? projectModelArrayList.get(position).getTotalGhp_plus() : "0");
            //total allotments
            mTv_totalAllotments.setText(projectModelArrayList.get(position).getTotalAllotments()!=null ? projectModelArrayList.get(position).getTotalAllotments() : "0");
            //total cancelled allotments
            mTv_totalCancelledAllotments.setText(projectModelArrayList.get(position).getCancel_booking()!=null ? projectModelArrayList.get(position).getCancel_booking() : "0");


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
    }


    private View getPerformanceView(int projectPosition, int position, int last)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_sales_manager_lead_performance_stats, null );

        final MaterialTextView mTv_spName = rowView_sub.findViewById(R.id.mTv_tl_stats_spName);
        final MaterialTextView mTv_leads = rowView_sub.findViewById(R.id.mTv_tl_stats_leads);
        final MaterialTextView mTv_siteVisits = rowView_sub.findViewById(R.id.mTv_tl_stats_siteVisits);
        final MaterialTextView mTv_allotments = rowView_sub.findViewById(R.id.mTv_tl_stats_allotments);
        final MaterialTextView mTv_cancelledAllotments = rowView_sub.findViewById(R.id.mTv_tl_stats_cancelledAllotments);
        final View view_line = rowView_sub.findViewById(R.id.view_line_tl_stats);
        final LinearLayoutCompat ll_teamStats = rowView_sub.findViewById(R.id.ll_teamStats);

        //set sales person name
        mTv_spName.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name() : "");
        //set leads
        mTv_leads.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLeads() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLeads() : "0");
        //set site visits
        mTv_siteVisits.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLeads_site_visits() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLeads_site_visits() : "0");
        //set ghp
        //mTv_ghp.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLead_tokens() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLead_tokens() : "0");
        //set ghp plus
       //mTv_ghpPlus.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLead_tokens_ghp_plus() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getLead_tokens_ghp_plus() : "0");
        //set Allotments
        mTv_allotments.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getBooking_master() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getBooking_master() : "0");
        //set cancelled allotments
        mTv_cancelledAllotments.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getBooking_master() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getCancel_booking() : "0");

        if(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id()==user_id)
        {
            //sales team lead highlighted by yellow color
            //ll_teamStats.setBackgroundColor(getResources().getColor(R.color.bgColor));

            //mTv_spName.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name() + " (SH)" : "");
            mTv_spName.setText(projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name() !=null ? projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name()   : "");
            mTv_spName.setTextAppearance(context,R.style.styleFontBoldText);
        }

        Log.e(TAG, "getPerformanceView: event_id"+selected_event_id);

        mTv_siteVisits.setOnClickListener(v -> {
            Log.e(TAG,"salespersonId"+projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id());
            if(Integer.parseInt(mTv_siteVisits.getText().toString()) > 0){
                Intent intent = new Intent(context, StatSiteVisitDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("event_id",selected_event_id);
                intent.putExtra("event_name",selectedEventName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamStatsActivity.this,"Details not available!");
            }

        });

        mTv_leads.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_leads.getText().toString()) > 0){
                Intent intent = new Intent(context, StatLeadDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("event_id",selected_event_id);
                intent.putExtra("event_name",selectedEventName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamStatsActivity.this,"Details not Available!");
            }
        });


        mTv_allotments.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_allotments.getText().toString()) > 0){
                Intent intent = new Intent(context, StatBookingDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("event_id",selected_event_id);
                intent.putExtra("event_name",selectedEventName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamStatsActivity.this,"Details not Available!");
            }
        });

        mTv_cancelledAllotments.setOnClickListener(v -> {
            if(Integer.parseInt(mTv_cancelledAllotments.getText().toString()) > 0){
                Intent intent = new Intent(context, StatCancelBookingDetailsActivity.class);
                intent.putExtra("sales_person_id", projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getSales_person_id());
                intent.putExtra("project_id",selectedProjectId);
                intent.putExtra("from_date",sendPerformanceFromDate);
                intent.putExtra("to_date",sendPerformanceToDate);
                intent.putExtra("Project_name",selectedProjectName);
                intent.putExtra("event_id",selected_event_id);
                intent.putExtra("event_name",selectedEventName);
                intent.putExtra("full_name",projectModelArrayList.get(projectPosition).getTeamStatsModelArrayList().get(position).getFull_name());
                startActivity(intent);
            }else {
                new Helper().showCustomToast(TeamStatsActivity.this,"Details not Available!");
            }
        });

        if(position==0) {
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
       // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }
}
