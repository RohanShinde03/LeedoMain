package com.tribeappsoft.leedo.admin.callSchedule.fragments;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
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
import com.tribeappsoft.leedo.admin.callSchedule.CallScheduleMainActivity;
import com.tribeappsoft.leedo.admin.callSchedule.adapter.CompletedCallsAdapter;
import com.tribeappsoft.leedo.admin.callSchedule.filter.FilterCallScheduleActivity;
import com.tribeappsoft.leedo.admin.callSchedule.model.ScheduledCallsModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;
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
import static com.tribeappsoft.leedo.util.Helper.getDateTime;
import static com.tribeappsoft.leedo.util.Helper.getSendFormatDateForToDo;
import static com.tribeappsoft.leedo.util.Helper.getTodaysDateStringToDo;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;


public class FragmentCompletedCalls extends Fragment //implements CallScheduleMainActivity.onTabChangeInterface
{


    @BindView(R.id.cl_CompletedCalls) CoordinatorLayout parent;
    @BindView(R.id.sr_CompletedCalls) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_CompletedCalls) RecyclerView recyclerView;

    @BindView(R.id.ll_CompletedCalls_search) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.edt_CompletedCalls_search) TextInputEditText edt_search;
    @BindView(R.id.iv_CompletedCalls_clearSearch) AppCompatImageView iv_clearSearch;

    @BindView(R.id.ll_CompletedCalls_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.pb_CompletedCalls)
    ContentLoadingProgressBar pb;
    @BindView(R.id.ll_CompletedCalls_backToTop) LinearLayoutCompat ll_backToTop;

    private String TAG = "FragmentCompletedCalls";
    private String api_token="", filter_text = "";
    private ArrayList<ScheduledCallsModel> itemArrayList;
    private CompletedCallsAdapter recyclerAdapter;
    private Context context;
    private int current_page =1, user_id = 0,sales_person_id =0, last_page =1, filterCount = 0, project_id =0, scheduledCount = 0, completedCount = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AppCompatTextView tvFilterItemCount;
    private Calendar currentCalender;
    private SimpleDateFormat dateFormatForDisplaying, dateFormatForMonth;
    private boolean isSalesHead = false, isAdmin = false, onStop = false;
    private static FragmentCompletedCalls instance = null;


    public FragmentCompletedCalls() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: FragmentCompletedCalls");
        instance=this;
        setHasOptionsMenu(true);
    }

    public static FragmentCompletedCalls getInstance(){
        return instance;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_completed_calls, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);
        setupUI(parent);

        //get date here and pass it to the api
        if (this.getArguments()!=null) {
            Bundle bundle = getArguments();
            String scheduledDate = bundle.getString("scheduledDate");
            Log.e(TAG, "onCreateView: "+scheduledDate);
        }

        //init
        init();

        return rootView;
    }

    private void init()
    {
        //SharedPreference
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        user_id = sharedPreferences.getInt("user_id", 0);
        sales_person_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        itemArrayList = new ArrayList<>();
        onStop = false;

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

       /* //def call api
        if (isNetworkAvailable(Objects.requireNonNull(getActivity())))
        {
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = last_page = 1;

            swipeRefresh.setRefreshing(true);
            call_getAllCalls();
        }
        else {
            NetworkError(getActivity());
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible no data
            ll_noDataFound.setVisibility(View.VISIBLE);
        }*/

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


    }


    private void setUpRecyclerScroll() {

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
                                call_getAllCalls();
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
                    Log.d(TAG, "onScrolled: upp " );
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
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

    private void hideViews() {
        ll_searchBar.animate().translationY(-ll_searchBar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        ll_searchBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }



    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: frag " );

        //perform search
        perform_search();


        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
         //   boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
          //  boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
          ///  boolean callScheduleAdded = sharedPreferences.getBoolean("callScheduleAdded", false);
            editor.apply();
            Log.e(TAG,"isFilterCC  : "+sharedPreferences.getBoolean("isFilterCC",false));
            Log.e(TAG,"project_id  : "+sharedPreferences.getInt("project_id",0));
            Log.e(TAG,"sales_person_id  : "+sharedPreferences.getInt("sales_person_id",0));

            if (sharedPreferences.getBoolean("isFilterCC", false)) {
                Log.e(TAG, "onResume: isFilterCC " + sharedPreferences.getBoolean("isFilterCC", false));

                project_id = sharedPreferences.getInt("project_id", 0);
                sales_person_id = sharedPreferences.getInt("sales_person_id",  sharedPreferences.getInt("user_id", 0));
                if(sales_person_id == 0)  sales_person_id = sharedPreferences.getInt("user_id",0);

                filterCount = sharedPreferences.getInt("filterCount", 0);

                //reset api call
                resetApiCall();

                //update flag to false
                //editor.putBoolean("isFilterCC", false);
                //editor.apply();

                //set badge count 0 and visible false
                ((CallScheduleMainActivity) Objects.requireNonNull(requireActivity())).onSetTabsViewPager(getCurDate(),0, 0, 1, false);

            }
            else if (sharedPreferences.getBoolean("clearFilter", false)) {
                Log.e(TAG, "onResume: clearFilter " + sharedPreferences.getBoolean("clearFilter", false));

                //clear fields
                project_id = filterCount = 0;
                user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);
                sales_person_id = sharedPreferences.getInt("user_id", 0);

                //reset api call
                resetApiCall();

                editor.remove("clearFilter");
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("isFilter");
                editor.remove("isFilterCC");
                editor.remove("filterCount");
                editor.remove("tabAt");
                editor.putBoolean("clearFilter", false);
                editor.apply();

                //call fragments method
                call_getCallLogCount(getCurDate());
            }
            else if (sharedPreferences.getBoolean("callScheduleAdded", false)) {
                Log.e(TAG, "onResume: callScheduleAdded "+ sharedPreferences.getBoolean("callScheduleAdded", false));

                //call api
                swipeRefresh.setRefreshing(true);
                refreshApiCall();

                editor.putBoolean("callScheduleAdded", false);
                editor.apply();
            }
            else
            {
                Log.e(TAG, "onResume: else All");
                if (!onStop)
                {

                    Log.e(TAG, "onResume: regular API Call");
                    //def call api
                    if (isNetworkAvailable(requireActivity()))
                    {
                        //1. clear arrayList
                        itemArrayList.clear();
                        //2. reset page flag to 1
                        current_page = last_page = 1;
                        //3. clear search text
                        filter_text = "";
                        //clear fields
                        filterCount = 0;
                        project_id = sharedPreferences.getInt("project_id", 0);

                        swipeRefresh.setRefreshing(true);
                        call_getAllCalls();
                    }
                    else {
                        NetworkError(requireActivity());
                        //hide main layouts
                        swipeRefresh.setRefreshing(false);
                        recyclerView.setVisibility(View.GONE);
                        //visible no data
                        ll_noDataFound.setVisibility(View.VISIBLE);
                    }
                }
            }

           /* if(isFilter) {
                if (sharedPreferences!=null) {

                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    user_id = sharedPreferences.getInt("sales_person_id",  sharedPreferences.getInt("user_id", 0));
                    filterCount = sharedPreferences.getInt("filterCount", 0);
                    editor.apply();
                }
                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n sales_person_id:- "+user_id );

                //reset api call
                resetApiCall();
            }
            else if (clearFilter) {

                //all filters are cleared

                Log.e(TAG, "onResume:clearFilter  ");
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.remove("clearFilter");
                    editor.remove("project_id");
                    editor.remove("sales_person_id");
                    editor.remove("isFilter");
                    editor.remove("filterCount");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }

                //clear fields
                project_id = filterCount = 0;
                user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);

                //reset api call
                resetApiCall();
            }
            if(callScheduleAdded) {
                //refresh api call
                refreshApiCall();

                //update flag to false
                if(sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("callScheduleAdded", false);
                    editor.apply();
                }
            }*/

        }

        //set up badge
        setupBadge();

    }


    //SetUpRecyclerView
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new CompletedCallsAdapter(getActivity(), itemArrayList, false);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                swipeRefresh.setRefreshing(true);
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
    private void call_getAllCalls()
    {
        String todoDate = getSendFormatDateForToDo(getCurDate());
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getCompletedCallLeads(api_token, sales_person_id, todoDate, current_page,  project_id, filter_text, user_id == sales_person_id);
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

                        if (current_page == 2) delayRefresh();
                        else notifyRecyclerDataChange();
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
        //if (jsonObject.has("schedules_created_at")) model.setCreated_at(!jsonObject.get("schedules_created_at").isJsonNull() ? jsonObject.get("schedules_created_at").getAsString() : getDateTime() );
        if (jsonObject.has("schedule_updated_at")) model.setCreated_at(!jsonObject.get("schedule_updated_at").isJsonNull() ? jsonObject.get("schedule_updated_at").getAsString() : getDateTime() );
        if (jsonObject.has("cp_name")) model.setCp_name(!jsonObject.get("cp_name").isJsonNull() ? jsonObject.get("cp_name").getAsString() : "" );
        if (jsonObject.has("cp_executive_name")) model.setCp_executive_name(!jsonObject.get("cp_executive_name").isJsonNull() ? jsonObject.get("cp_executive_name").getAsString() : "" );
        if (jsonObject.has("scheduledBy")) model.setSchedule_by(!jsonObject.get("scheduledBy").isJsonNull() ? jsonObject.get("scheduledBy").getAsString() : "" );
        if (jsonObject.has("scheduled_on")) model.setScheduled_on(!jsonObject.get("scheduled_on").isJsonNull() ? jsonObject.get("scheduled_on").getAsString() : "" );
        if (jsonObject.has("call_schedule_id")) model.setCall_schedule_id(!jsonObject.get("call_schedule_id").isJsonNull() ? jsonObject.get("call_schedule_id").getAsInt() : 0 );
        if (jsonObject.has("lead_status_id")) model.setLead_status_id(!jsonObject.get("lead_status_id").isJsonNull() ? jsonObject.get("lead_status_id").getAsInt() : 0 );
        if (jsonObject.has("schedule_status_id")) model.setSchedule_status_id(!jsonObject.get("schedule_status_id").isJsonNull() ? jsonObject.get("schedule_status_id").getAsInt() : 0 );

        //if (jsonObject.has("status")) model.setScheduledOn(!jsonObject.get("status").isJsonNull() ? jsonObject.get("status").getAsString() : "" );
        //if (jsonObject.has("remark")) model.setRemark(!jsonObject.get("remark").isJsonNull() ? jsonObject.get("remark").getAsString() : "" );

        if (jsonObject.has("schedule_details")) {
            if (!jsonObject.get("schedule_details").isJsonNull() && jsonObject.get("schedule_details").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("schedule_details").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList1 = new ArrayList<>();
                arrayList1.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setScheduleOtherJson(jsonArray.get(i).getAsJsonObject(), arrayList1);
                }
                model.setCallDetailsTitleModelArrayList(arrayList1);
            }
        }

        if (jsonObject.has("schedule_call_details")) {
            if (!jsonObject.get("schedule_call_details").isJsonNull() && jsonObject.get("schedule_call_details").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("schedule_call_details").getAsJsonArray();
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
    private void setScheduleOtherJson(JsonObject jsonObject, ArrayList<LeadDetailsTitleModel> arrayList)
    {

        LeadDetailsTitleModel model = new LeadDetailsTitleModel();
        if (jsonObject.has("section_title"))
            model.setLead_details_title(!jsonObject.get("section_title").isJsonNull() ? jsonObject.get("section_title").getAsString() : "");

        if (jsonObject.has("section_items")) {
            if (!jsonObject.get("section_items").isJsonNull() && jsonObject.get("section_items").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("section_items").getAsJsonArray();
                ArrayList<LeadDetailsModel> arrayList1 = new ArrayList<>();
                arrayList1.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setScheduleSectionDetailsJson(jsonArray.get(i).getAsJsonObject(), arrayList1);
                }
                model.setLeadDetailsModels(arrayList1);
            }
        }
        arrayList.add(model);
    }

    private void setScheduleSectionDetailsJson(JsonObject jsonObject, ArrayList<LeadDetailsModel> arrayList1) {
        LeadDetailsModel model = new LeadDetailsModel();
        if (jsonObject.has("section_item_title")) model.setLead_details_text(!jsonObject.get("section_item_title").isJsonNull() ? jsonObject.get("section_item_title").getAsString() : "");
        if (jsonObject.has("section_item_desc")) model.setLead_details_value(!jsonObject.get("section_item_desc").isJsonNull() ? jsonObject.get("section_item_desc").getAsString() : "");
        arrayList1.add(model);
    }

    private void setOtherInfoJson(JsonObject jsonObject, ArrayList<LeadDetailsTitleModel> arrayList) {

        LeadDetailsTitleModel model = new LeadDetailsTitleModel();
        if (jsonObject.has("section_title"))
            model.setLead_details_title(!jsonObject.get("section_title").isJsonNull() ? jsonObject.get("section_title").getAsString() : "");

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
    public  void delayRefresh()
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
                recyclerAdapter = new CompletedCallsAdapter(requireActivity(), itemArrayList, false);
                recyclerView.setAdapter(recyclerAdapter);
                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );
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
            });
        }
    }

    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        if (requireActivity()!=null)
        {
            requireActivity().runOnUiThread(() -> {

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


    private void perform_search() {

        //or you can search by the editTextFiler

        //search ime action click
        edt_search.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (recyclerView.getAdapter() != null)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(getActivity(), Objects.requireNonNull(requireActivity()).getWindow().getDecorView().getRootView());
                    // Get search text
                    String filterText  =  Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                    // regex to match any number of spaces
                    filterText = filterText.trim().replaceAll("\\s+", " ");
                    Log.e(TAG, "perform_search: filterText "+filterText);
                    doFilter(filterText);
                }

                return true;
            }
            return false;
        });


        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                //if (recyclerAdapter != null) {
                //  String text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                //  doFilter(text);

                if (Objects.requireNonNull(edt_search.getText()).length() < 1)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(getActivity(), Objects.requireNonNull(requireActivity()).getWindow().getDecorView().getRootView());
                    iv_clearSearch.setVisibility(View.GONE);

                    //call reset api
                    //resetApiCall();

                } else {
                    //visible empty search ll
                    iv_clearSearch.setVisibility(View.VISIBLE);
                }
                //}
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                iv_clearSearch.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }
        });


        //clear searchText
        iv_clearSearch.setOnClickListener(v -> {
            edt_search.setText("");
            //call reset api
            resetApiCall();
        });
    }


    private void refreshApiCall()
    {
        if (isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. clear search text
            filter_text = "";
            if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
            //4. clear filters if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("isFilter");
                editor.remove("isFilterCC");
                editor.remove("filterCount");
                editor.remove("tabAt");
                editor.putBoolean("clearFilter", false);
                editor.apply();
            }

            //clear fields
            project_id = filterCount = 0;
            user_id = Objects.requireNonNull(sharedPreferences).getInt("user_id", 0);
            sales_person_id = sharedPreferences.getInt("user_id", 0);

            //call api
            swipeRefresh.setRefreshing(true);
            call_getAllCalls();

            //call fragments method
            call_getCallLogCount(getCurDate());
        }
        else {
            NetworkError(requireActivity());
        }

        //set up badge
        setupBadge();
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

        } else NetworkError(requireActivity());

    }


    private void call_getCallLogCount(String serviceDate)
    {
        String todoDate = getSendFormatDateForToDo(serviceDate);
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getCallLogCounts(api_token, user_id, todoDate).enqueue(new Callback<JsonObject>() {
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
                                    if (jsonObject.has("schedules_count")) scheduledCount = !jsonObject.get("schedules_count").isJsonNull() ? jsonObject.get("schedules_count").getAsInt() : 0 ;
                                    if (jsonObject.has("complete_count")) completedCount = !jsonObject.get("complete_count").isJsonNull() ? jsonObject.get("complete_count").getAsInt() : 0 ;
                                }
                            }

                            onSuccessSetCount(serviceDate);
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

    private void onSuccessSetCount(String serviceDate) {

        if(getActivity() != null){
            getActivity().runOnUiThread(() ->{

                swipeRefresh.setRefreshing(false);
                ((CallScheduleMainActivity) Objects.requireNonNull(requireActivity())).onSetTabsViewPager(serviceDate,scheduledCount, completedCount, 1, true);

                //((MyOffersActivity) Objects.requireNonNull(getActivity())).setFragmentLayout(7,3,2);
            });
        }
    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_call_schedule, menu);

        MenuItem searchItem = menu.findItem(R.id.action_call_schedule_search);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(requireActivity()).getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null)
        {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search));

            /*Code for changing the search icon */
            ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            icon.setColorFilter(Color.WHITE);
            //icon.setImageResource(R.drawable.ic_home_search);

            //AutoCompleteTextView searchTextView =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            AutoCompleteTextView searchTextView =  searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            /* Code for changing the textcolor and hint color for the search view */
            searchTextView.setHintTextColor(getResources().getColor(R.color.main_white));
            searchTextView.setTextColor(getResources().getColor(R.color.main_white));

            /*Code for changing the voice search icon */
            //ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            //voiceIcon.setImageResource(R.drawable.my_voice_search_icon);

            /*Code for changing the close search icon */
            ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            closeIcon.setColorFilter(Color.WHITE);
//            closeIcon.setImageResource(R.drawable.ic_search_close_icon);

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
                    if(newText.trim().isEmpty()) doFilter(newText);
                    return false;
                }
            });


            searchView.setOnCloseListener(() -> {
                doFilter("");
                return false;
            });
        }
        if (searchView != null)
        {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(Objects.requireNonNull(requireActivity()).getComponentName()));
            }
        }

        final MenuItem menuItem = menu.findItem(R.id.action_call_schedule_filter);
        //set visible only for SH or TL
        menuItem.setVisible(isSalesHead || isAdmin);

        View actionView = menuItem.getActionView();
        tvFilterItemCount = actionView.findViewById(R.id.cart_badge);
        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

    }

    private void setupBadge() {

        if (tvFilterItemCount != null) {

            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                filterCount = sharedPreferences.getInt("filterCount", 0);
                editor.apply();
            }

            Log.e(TAG, "setupBadge: "+filterCount );
            if (filterCount == 0) {
                if (tvFilterItemCount.getVisibility() != View.GONE) {
                    tvFilterItemCount.setVisibility(View.GONE);
                }
            } else {
                tvFilterItemCount.setText(String.valueOf(Math.min(filterCount, 99)));
                if (tvFilterItemCount.getVisibility() != View.VISIBLE) {
                    tvFilterItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void doFilter(String query) {

        if (isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Get search text
            filter_text = query;

            swipeRefresh.setRefreshing(true);
            //doFilter(text);
            call_getAllCalls();
        }
        else NetworkError(Objects.requireNonNull(requireActivity()));
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case (R.id.action_call_schedule_calendar):
                showCustomCalendarAlertDialog();
                break;

            case (R.id.action_call_schedule_filter):
                startActivity(new Intent(context, FilterCallScheduleActivity.class)
                        .putExtra("tabAt", 1)
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


                /*if (events != null) {
                    //   Log.d(TAG, bookingsFromMap.toString());
                    mutableBookings.clear();
                    for (Event booking : events) {
                        mutableBookings.add((String) booking.getData());
                    }
                    //  adapter.notifyDataSetChanged();
                    //Toast.makeText(CalenderEventActivityPending.this, "You Click in Calender : ", Toast.LENGTH_SHORT).show();
                }*/

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
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
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


    private void insertCurDate(String curDate) {
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.putString("curDate",curDate);
            editor.apply();
            //2019-01-08
        }
    }

    private void setDateToUI(String date) {

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.putString("setDateToUI", date);
            editor.putBoolean("isSetDateToUI", true);
            editor.apply();
        }
    }


    private String getCurDate()
    {
        String curDate = getTodaysDateStringToDo();
        if (sharedPreferences!=null) {

            editor = sharedPreferences.edit();
            editor.apply();
            curDate = sharedPreferences.getString("curDate",getTodaysDateStringToDo());
        }

        return curDate;
    }




    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        pb.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        pb.setVisibility(View.GONE);
        //Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                onErrorSnack(getActivity(),message);
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
                hideSoftKeyboard(Objects.requireNonNull(requireActivity()), view);
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onStop = true;
        Log.e(TAG, "onDestroy: ");
    }

   /* @Override
    public void callOnTabChangedMethod() {
        Log.e(TAG, "callOnTabChangedMethod: ");
    }*/
}


