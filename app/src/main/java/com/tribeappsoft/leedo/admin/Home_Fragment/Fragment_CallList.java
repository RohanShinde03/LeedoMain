package com.tribeappsoft.leedo.admin.Home_Fragment;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callSchedule.adapter.ScheduledCallsAdapter;
import com.tribeappsoft.leedo.admin.callSchedule.filter.FilterCallScheduleActivity;
import com.tribeappsoft.leedo.admin.callSchedule.model.ScheduledCallsModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.compactCalender.Event;
import com.tribeappsoft.leedo.util.compactCalender.view.CompactCalendarView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;


public class Fragment_CallList extends Fragment //implements CallScheduleMainActivity.onTabChangeInterface
{
    @BindView(R.id.cl_ScheduledCalls) CoordinatorLayout parent;
    @BindView(R.id.sr_ScheduledCalls) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_ScheduledCalls) RecyclerView recyclerView;
    @BindView(R.id.ll_ScheduledCalls_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.pb_ScheduledCalls) ContentLoadingProgressBar pb;
    @BindView(R.id.ll_ScheduledCalls_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_ScheduledCalls_backToTop) LinearLayoutCompat ll_backToTop;

    private String TAG = "Fragment_CallList";
    private String api_token="", filter_text = "",todoDate=null,startDate=null,endDate=null;
    private ArrayList<ScheduledCallsModel> itemArrayList;
    private ScheduledCallsAdapter recyclerAdapter;
    private Context context;
    private int current_page =1, user_id = 0,last_page =1, project_id =0,sales_person_id=0, lead_count = 0, site_visit_count = 0,call_schedule_count = 0,
            reminder_count = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    //private AppCompatTextView tvFilterItemCount;
    private Calendar currentCalender;
    private SimpleDateFormat dateFormatForDisplaying, dateFormatForMonth;
    private static Fragment_CallList instance = null;

    public Fragment_CallList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: Fragment_CallList");
        instance=this;
        setHasOptionsMenu(true);
    }

    public static Fragment_CallList getInstance(){
        return instance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_calls, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);
        setupUI(parent);
        //init
        init();

        return rootView;
    }

    private void init()
    {
        //SharedPreference
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        //user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        todoDate = sharedPreferences.getString("todoDate", "");
        startDate = sharedPreferences.getString("startDate", "");
        endDate = sharedPreferences.getString("endDate", "");
        project_id = sharedPreferences.getInt("project_id", 0);
        sales_person_id = sharedPreferences.getInt("selected_sales_person_id",  sharedPreferences.getInt("user_id", 0));
        editor.apply();

        Log.e(TAG, "onCreateView: todoDate :"+todoDate+"  startDate:"+startDate+"  endDate:"+ endDate);
        //isSalesTeamLead = sharedPreferences.getBoolean("isSalesTeamLead", false);
        itemArrayList = new ArrayList<>();
        //onStop = false;

        // Compact Calendar
        currentCalender = Calendar.getInstance(Locale.getDefault());
        dateFormatForDisplaying = new SimpleDateFormat("dddd-MMMM-yyyy hh:mm:ss a", Locale.getDefault());
        dateFormatForMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());


        //SetRecyclerView
        setupRecycleView();

        //set swipe Refresh
        setSwipeRefresh();

        //set up recyclerScroll
        setUpRecyclerScroll();
    }

    public void onPageChange(){

        itemArrayList.clear();
        //SharedPreference
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        //user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        todoDate = sharedPreferences.getString("todoDate", "");
        startDate = sharedPreferences.getString("startDate", "");
        endDate = sharedPreferences.getString("endDate", "");
        project_id = sharedPreferences.getInt("project_id", 0);
        sales_person_id = sharedPreferences.getInt("selected_sales_person_id",  sharedPreferences.getInt("user_id", 0));
        editor.apply();

        Log.e(TAG, "onCreateView: todoDate :"+todoDate+"  startDate:"+startDate+"  endDate:"+ endDate);
        //isSalesTeamLead = sharedPreferences.getBoolean("isSalesTeamLead", false);

        //hide pb
        hideProgressBar();

        refreshApiCall();
    }

    private void setUpRecyclerScroll()
    {
        //Use This For Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE)
                {

                    Log.d("-----","end");
                    //TODO temp gone for design issues
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
                            if (isNetworkAvailable(Objects.requireNonNull(requireActivity())))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();

                                new Handler().postDelayed(() -> call_getAllCalls(),500);

                            } else NetworkError(Objects.requireNonNull(requireActivity()));

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
                    Log.d(TAG, "onScrolled: up" );
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down" );
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
            Log.e(TAG, "Back TO TOP");
            recyclerView.smoothScrollToPosition(0);

            new Animations().slideOutBottom(ll_backToTop);
            ll_backToTop.setVisibility(View.GONE);

        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    /*@Override
    public void callOnTabChangedMethod() {
        Log.e(TAG, "callOnTabChangedMethod: ");
    }*/


    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    //public interface OnCountSelectedListener {
     //   public void getScheduledCount(int count);
    //}



    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: frag " );


        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            //boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            //boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            //boolean callScheduleAdded = sharedPreferences.getBoolean("callScheduleAdded", false);
            //boolean callCompletedAdded = sharedPreferences.getBoolean("callCompletedAdded", false);
            editor.apply();

            if (sharedPreferences.getBoolean("isFilter", false)) {
                Log.e(TAG, "onResume: isFilter "+ sharedPreferences.getBoolean("isFilter", false));

                project_id = sharedPreferences.getInt("project_id", 0);
                //user_id = sharedPreferences.getInt("sales_person_id",  sharedPreferences.getInt("user_id", 0));
                //filterCount = sharedPreferences.getInt("filterCount", 0);

                //reset api call
                resetApiCall();

                //update flag to false
                //editor.putBoolean("isFilter", false);
                //editor.apply();

                /*FragmentHome frag = (FragmentHome)getTargetFragment();
                if(frag != null){
                    frag.onSetTabsViewPager(0, 0, 0, 0,0,false);
                }*/

                //set badge count 0 and visible false
              //  ((CallScheduleMainActivity) Objects.requireNonNull(getActivity())).onSetTabsViewPager(getCurDate(),0, 0, 0, false);

            }
            else if (sharedPreferences.getBoolean("clearFilter", false)) {
                Log.e(TAG, "onResume: clearFilter "+ sharedPreferences.getBoolean("clearFilter", false));

                //clear fields
                project_id = 0;
                user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);

                //reset api call
                resetApiCall();

                editor.remove("clearFilter");
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("isFilter");
                editor.remove("filterCount");
                editor.remove("tabAt");
                editor.putBoolean("clearFilter", false);
                editor.apply();

                //call fragments method
                call_getCallLogCount();
            }
            else if (sharedPreferences.getBoolean("callScheduleAdded", false)) {
                Log.e(TAG, "onResume: callScheduleAdded "+ sharedPreferences.getBoolean("callScheduleAdded", false));

                //call api
                swipeRefresh.setRefreshing(true);
                refreshApiCall();
                call_getCallLogCount();


                editor.putBoolean("callScheduleAdded", false);
                editor.apply();
            }
            else if (sharedPreferences.getBoolean("callCompletedAdded", false)) {
                Log.e(TAG, "onResume: callCompletedAdded "+ sharedPreferences.getBoolean("callCompletedAdded", false));

                swipeRefresh.setRefreshing(true);
                //refresh api call
                refreshApiCall();
                call_getCallLogCount();

                editor.putBoolean("callCompletedAdded", false);
                editor.apply();
            }
        }

        //refresh api call
        // refreshApiCall();

        //set up badge
        //setupBadge();
    }


    //SetUpRecyclerView
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter= new ScheduledCallsAdapter(getActivity(), itemArrayList, true);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                /*put tab value*/
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putInt("tabAt", 0);
                    editor.apply();
                }
                swipeRefresh.setRefreshing(true);

                //1.set home tab count
                call_getCallLogCount();

                refreshApiCall();
            }
            else {

                NetworkError(requireActivity());
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            }
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    //CallReminderAPI
    private void call_getAllCalls() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getHomeCallScheduleList(api_token,sales_person_id, todoDate,startDate,endDate,current_page, project_id, filter_text);
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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
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

                                                setScheduledCallJson(jsonObject.getAsJsonObject());
                                            }
                                        }
                                    }else Log.e(TAG, "Outside Data: ");
                                } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setScheduledCallJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }

    private void setJson(JsonObject jsonObject) {

        ScheduledCallsModel model = new ScheduledCallsModel();
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("call_schedule_id")) model.setCall_schedule_id(!jsonObject.get("call_schedule_id").isJsonNull() ? jsonObject.get("call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("prev_call_schedule_id")) model.setPrev_call_schedule_id(!jsonObject.get("prev_call_schedule_id").isJsonNull() ? jsonObject.get("prev_call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) model.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "--" );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "--" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("prefix")) model.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("lead_uid")) model.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        if (jsonObject.has("sales_person_name")) model.setSales_person_name(!jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "" );
        if (jsonObject.has("lead_types_name")) model.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "" );
        if (jsonObject.has("schedules_created_at")) model.setCreated_at(!jsonObject.get("schedules_created_at").isJsonNull() ? jsonObject.get("schedules_created_at").getAsString() : Helper.getDateTime() );
        //if (jsonObject.has("schedule_updated_at")) model.setCreated_at(!jsonObject.get("schedule_updated_at").isJsonNull() ? jsonObject.get("schedule_updated_at").getAsString() : getDateTime() );
        if (jsonObject.has("cp_name")) model.setCp_name(!jsonObject.get("cp_name").isJsonNull() ? jsonObject.get("cp_name").getAsString() : "" );
        if (jsonObject.has("cp_executive_name")) model.setCp_executive_name(!jsonObject.get("cp_executive_name").isJsonNull() ? jsonObject.get("cp_executive_name").getAsString() : "" );
        if (jsonObject.has("scheduledBy")) model.setSchedule_by(!jsonObject.get("scheduledBy").isJsonNull() ? jsonObject.get("scheduledBy").getAsString() : "" );
        if (jsonObject.has("scheduled_on")) model.setScheduled_on(!jsonObject.get("scheduled_on").isJsonNull() ? jsonObject.get("scheduled_on").getAsString() : "" );
        if (jsonObject.has("call_schedule_id")) model.setCall_schedule_id(!jsonObject.get("call_schedule_id").isJsonNull() ? jsonObject.get("call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("lead_status_id")) model.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0 );
        if (jsonObject.has("schedule_status_id")) model.setSchedule_status_id(!jsonObject.get("schedule_status_id").isJsonNull() ? jsonObject.get("schedule_status_id").getAsInt() : 0 );
        if (jsonObject.has("call_schedule_date")) model.setCall_schedule_date(!jsonObject.get("call_schedule_date").isJsonNull() ? jsonObject.get("call_schedule_date").getAsString() : "" );
        if (jsonObject.has("call_schedule_time")) model.setCall_schedule_time(!jsonObject.get("call_schedule_time").isJsonNull() ? jsonObject.get("call_schedule_time").getAsString() : "" );

        //if (jsonObject.has("status")) model.setScheduledOn(!jsonObject.get("status").isJsonNull() ? jsonObject.get("status").getAsString() : "" );
        //if (jsonObject.has("remark")) model.setRemark(!jsonObject.get("remark").isJsonNull() ? jsonObject.get("remark").getAsString() : "" );

        if (jsonObject.has("schedule_details")) {
            if (!jsonObject.get("schedule_details").isJsonNull() && jsonObject.get("schedule_details").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("schedule_details").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                model.setDetailsTitleModelArrayList(arrayList);
            }
        }
        itemArrayList.add(model);
    }

    private void setOtherInfoJson(JsonObject jsonObject, ArrayList<LeadDetailsTitleModel> arrayList) {

        LeadDetailsTitleModel model = new LeadDetailsTitleModel();
        if (jsonObject.has("section_title")) model.setLead_details_title(!jsonObject.get("section_title").isJsonNull() ? jsonObject.get("section_title").getAsString() : "");
        //if (jsonObject.has("section_item_desc")) model.setLead_details_title(!jsonObject.get("section_item_desc").isJsonNull() ? jsonObject.get("section_item_desc").getAsString() : "");

        if (jsonObject.has("section_items")) {
            if (!jsonObject.get("section_items").isJsonNull() && jsonObject.get("section_items").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("section_items").getAsJsonArray();
                ArrayList<LeadDetailsModel> arrayList1 = new ArrayList<>();
                arrayList1.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setSectionDetailsJson(jsonArray.get(i).getAsJsonObject(), arrayList1);
                }
                model.setLeadDetailsModels(arrayList1);
            }
        }
        arrayList.add(model);
    }

    private void setSectionDetailsJson(JsonObject jsonObject, ArrayList<LeadDetailsModel> arrayList1) {
        LeadDetailsModel model = new LeadDetailsModel();
        if (jsonObject.has("section_item_title")) model.setLead_details_text(!jsonObject.get("section_item_title").isJsonNull() ? jsonObject.get("section_item_title").getAsString() : "");
        if (jsonObject.has("section_item_desc")) model.setLead_details_value(!jsonObject.get("section_item_desc").isJsonNull() ? jsonObject.get("section_item_desc").getAsString() : "");
        arrayList1.add(model);
    }

    //DelayRefresh

    public void delayRefresh()
    {
        if (context != null) {
            Objects.requireNonNull(requireActivity()).runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new ScheduledCallsAdapter(getActivity(), itemArrayList, true);
                recyclerView.setAdapter(recyclerAdapter);

                int count = recyclerAdapter.getItemCount();
                if (count == 0) {
                    new Handler().postDelayed(() -> {
                        //no calls
                        recyclerView.setVisibility(View.GONE);
                        ll_noDataFound.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    },500);

                } else {
                    //Registrations are available
                    new Handler().postDelayed(() -> {
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noDataFound.setVisibility(View.GONE);
                        //exFab.setVisibility(View.VISIBLE);
                    },500);

                }
            });

        }
    }

    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                if (recyclerView.getAdapter()!=null)
                {
                    //recyclerView adapter
                    recyclerView.getAdapter().notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noDataFound.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noDataFound.setVisibility(View.GONE);
                        //exFab.setVisibility(View.VISIBLE);
                    }

                }

            });
        }
    }

    private void refreshApiCall()
    {
        if (isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. clear search text
            filter_text = "";


            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();

            /*put tab value*/
            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("tabAt", 0);
                editor.apply();
            }

            //4. clear filters if applied from sharedPref
           /* if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("isFilter");
                editor.remove("filterCount");
                editor.remove("tabAt");
                editor.putBoolean("clearFilter", false);
                editor.apply();
            }*/

            //clear fields
           // project_id = filterCount = 0;
            user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);

            //call api
            swipeRefresh.setRefreshing(true);
            call_getAllCalls();

            //call fragments method
           // call_getCallLogCount();
        }
        else {
            hideProgressBar();
            NetworkError(requireActivity());
        }

        //set up badge
      //  setupBadge();
    }

    private void resetApiCall()
    {
        if (isNetworkAvailable(Objects.requireNonNull(requireActivity())))
        {
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();

            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;

            //3. Set search other_ids clear
            filter_text = "";
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();

            //call get sales feed api
            swipeRefresh.setRefreshing(true);

            // showProgressBar();
            call_getAllCalls();
        }
        else {
            hideProgressBar();
            NetworkError(requireActivity());
        }

    }

    private void call_getCallLogCount()
    {
        ApiClient client = ApiClient.getInstance();
            client.getApiService().getHomeAllCounts(api_token, sales_person_id,project_id, todoDate,startDate,endDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject()) {
                                    JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                    if (jsonObject.has("lead_count")) lead_count = !jsonObject.get("lead_count").isJsonNull() ? jsonObject.get("lead_count").getAsInt() : 0 ;
                                    if (jsonObject.has("site_visit_count")) site_visit_count = !jsonObject.get("site_visit_count").isJsonNull() ? jsonObject.get("site_visit_count").getAsInt() : 0 ;
                                    if (jsonObject.has("call_schedule_count")) call_schedule_count = !jsonObject.get("call_schedule_count").isJsonNull() ? jsonObject.get("call_schedule_count").getAsInt() : 0 ;
                                    if (jsonObject.has("reminder_count")) reminder_count = !jsonObject.get("reminder_count").isJsonNull() ? jsonObject.get("reminder_count").getAsInt() : 0 ;
                                }
                            }onSuccessSetCount();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                swipeRefresh.setRefreshing(false);
            }
        });

    }

    private void onSuccessSetCount() {
        if(getActivity() != null){
            getActivity().runOnUiThread(() ->{

                swipeRefresh.setRefreshing(false);
                FragmentHome.getInstance().onSetTabsViewPager(call_schedule_count, site_visit_count,lead_count, reminder_count,0, true,false);
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_search_self, menu);

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search_self);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(requireActivity()).getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search_calls));

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
                    //}
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
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
            }
        }

    }


    private void doFilter(String query) {

        if (isNetworkAvailable(requireActivity()))
        {
            //1. clear arrayList
            itemArrayList.clear();

            current_page = last_page = 1;

            swipeRefresh.setRefreshing(true);
            showProgressBar();
            filter_text = query;
            //call get sales feed api
            call_getAllCalls();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

        }
        else {
            hideProgressBar();
            NetworkError(requireActivity());
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case (R.id.action_call_schedule_calendar):
                showCustomCalendarAlertDialog();
                break;

            case (R.id.action_call_schedule_filter):
                startActivity(new Intent(context, FilterCallScheduleActivity.class)
                        .putExtra("tabAt", 0)
                        .putExtra("user_id", user_id));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showCustomCalendarAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_custom_calendar, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        LinearLayoutCompat ll_prevMonth =  alertLayout.findViewById(R.id.ll_layoutCustomCalendar_prevMonth);
        LinearLayoutCompat ll_nextMonth =  alertLayout.findViewById(R.id.ll_layoutCustomCalendar_nextMonth);
        MaterialTextView mTv_thisMonth =  alertLayout.findViewById(R.id.mTv_layoutCustomCalendar_thisMonth);
        CompactCalendarView cv_CompactCalendar =  alertLayout.findViewById(R.id.cv_layoutCustomCalendar_CompactCalendar);

        //set def this month
        mTv_thisMonth.setText(dateFormatForMonth.format(cv_CompactCalendar.getFirstDayOfCurrentMonth()));
        //tv_thisYear.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        //load events
        loadEvents(cv_CompactCalendar);

        cv_CompactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked)
            {

                mTv_thisMonth.setText(dateFormatForMonth.format(dateClicked));

                //List<Event> events = compactCalendarView.getEvents(dateClicked);
                Log.e(TAG, "inside onclick " + dateFormatForDisplaying.format(dateClicked));
                Log.e(TAG, "onDayClick: "+dateClicked );

                //new Helper().showCustomToast(getActivity(), "You Click on"+ getStringDateFromDate(dateClicked));
                //Toast.makeText(context, "You Click on"+dateFormat.format(dateClicked),Toast.LENGTH_SHORT).show();

                //insert current date
                //insertCurDate(getStringDateFromDate(dateClicked));

                //set date to UI
                //setDateToUI(getFormatDateForToDo(getStringDateFromDate(dateClicked)));

                //dismiss the dialog
                alertDialog.dismiss();

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mTv_thisMonth.setText(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });


        ll_prevMonth.setOnClickListener(v -> cv_CompactCalendar.scrollLeft());

        ll_nextMonth.setOnClickListener(v -> cv_CompactCalendar.scrollRight());

        //
        //alertDialog.dismiss();

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(requireActivity()).getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmLp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmLp.gravity =  Gravity.CENTER;
        wmLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmLp.width = pixel-100;
        //wmLp.x = 100;   //x position
        //wmLp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(requireActivity().getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmLp.height );
        alertDialog.getWindow().setAttributes(wmLp);
    }


    private void loadEvents(CompactCalendarView compactCalendarView)
    {
        // Get Current Date
        //final Calendar c = Calendar.getInstance();
        //int mYear = c.get(Calendar.YEAR);
        //int mMonth = c.get(Calendar.FEBRUARY);
        //int mDay = c.get(Calendar.DAY_OF_MONTH);

        addEvents(compactCalendarView, 03, 1, 2020, 3);
        addEvents(compactCalendarView, 29, 5, 2020, 2);
    }


    private void addEvents(CompactCalendarView compactCalendarView, int day, int month, int year, int eventCount) {
        currentCalender.setTime(new Date());
        if (day > -1) {
            currentCalender.set(Calendar.DAY_OF_MONTH,day);
        }
        if (month > -1) {
            currentCalender.set(Calendar.MONTH, month);
        }
        if (year > -1) {
            currentCalender.set(Calendar.ERA, GregorianCalendar.AD);
            currentCalender.set(Calendar.YEAR, year);
        }

        setToMidnight(currentCalender);
        long timeInMillis = currentCalender.getTimeInMillis();

        List<Event> events = getEvents(timeInMillis, eventCount);
        compactCalendarView.addEvents(events);
    }

    private List<Event> getEvents(long timeInMillis, int eventCount)
    {

        ArrayList<Event> arrayList = new ArrayList<>();
        for (int i =0; i< eventCount; i++) {

            //return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis, itemArrayList));
            //return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis));
            Event event = new Event(Color.argb(255, 51, 153, 255), timeInMillis);
            arrayList.add(event);
        }

        return arrayList;


      /*  if (day < 2)
        {
            return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis, itemArrayList));

        } else if (day > 2 && day <= 4) {
            return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis, itemArrayList));
            //new Event(Color.argb(255, 51, 153, 255), timeInMillis, "Event at " + new Date(timeInMillis)),
            //new Event(Color.argb(255, 0, 255, 85), timeInMillis, "Event 2 at " + new Date(timeInMillis)));
        } else {
            return Arrays.asList(
                    new Event(Color.argb(255, 51, 153, 255), timeInMillis, itemArrayList),
                    new Event(Color.argb(255, 0, 255, 85), timeInMillis, itemArrayList));
            //new Event(Color.argb(255, 51, 153, 255), timeInMillis, "Event at " + new Date(timeInMillis)));
            //new Event(Color.argb(255, 0, 255, 85), timeInMillis, "Event 2 at " + new Date(timeInMillis)));
            // new Event(Color.argb(255, 0, 255, 85), timeInMillis, "Event 2 at " + new Date(timeInMillis)));

        }*/
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }




    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        ll_loadingContent.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        ll_loadingContent.setVisibility(View.GONE);
        //Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {
                //hide pb
                hideProgressBar();
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(getActivity(),message);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            });
        }
    }



    //SetupUI
    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(Objects.requireNonNull(requireActivity()), view);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //onStop = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
    }

}