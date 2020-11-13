package com.tribeappsoft.leedo.admin.Home_Fragment;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.Home_Fragment.adapter.HomeReminderAdapter;
import com.tribeappsoft.leedo.admin.reminder.RecyclerViewSwipeDecorator;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.ReminderModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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


public class Fragment_Reminders extends Fragment //implements CallScheduleMainActivity.onTabChangeInterface
{

    @BindView(R.id.cl_fragSalesPerson_reminder) CoordinatorLayout parent;
    @BindView(R.id.sr_reminder) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_reminder) RecyclerView recyclerView;
    @BindView(R.id.ll_reminder_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.ll_allSiteVisit_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_reminder_backToTop) LinearLayoutCompat ll_backToTop;

    private String TAG = "Fragment_Reminders",search_text="";
    private HomeReminderAdapter recyclerAdapter;
    private String api_token="",todoDate="",startDate=null,endDate=null;
    private ArrayList<ReminderModel> itemArrayList = new ArrayList<>();
    private Context context;
    private int current_page =1, last_page =0,recyclerPosition=0,project_id = 0,sales_person_id =0 ,lead_count = 0, site_visit_count = 0,call_schedule_count = 0,reminder_count = 0;
    //user_id=0,tabAt=0,

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    //private boolean isSalesHead=false;
    private static Fragment_Reminders instance = null;

    private Fragment_Reminders fragment_reminders;

    public Fragment_Reminders() {
        // Required empty public constructor


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: Fragment_Reminders");
        instance=this;
        setHasOptionsMenu(true);
    }

    public static Fragment_Reminders getInstance(){
        return instance;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_reminders, container, false);
        fragment_reminders=Fragment_Reminders.this;
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);
        setupUI(parent);

        //get date here and pass it to the api
      /*  if (this.getArguments()!=null) {
            Bundle bundle = getArguments();
            todo_date= bundle.getString("todo_date");
            Log.e(TAG, "onCreateView: "+todo_date);
        }*/

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

        //user_id = sharedPreferences.getInt("user_id", 0);

        project_id = sharedPreferences.getInt("project_id", 0);
        sales_person_id = sharedPreferences.getInt("selected_sales_person_id",  sharedPreferences.getInt("user_id", 0));
        api_token = sharedPreferences.getString("api_token", "");
        todoDate = sharedPreferences.getString("todoDate", "");
        startDate = sharedPreferences.getString("startDate", "");
        endDate = sharedPreferences.getString("endDate", "");

        Log.e(TAG, "onCreateView: todoDate : "+todoDate+"startDate:"+startDate+"endDate:"+ endDate);

        itemArrayList = new ArrayList<>();

        //hide pb
        hideProgressBar();

        //SetRecyclerView
        setupRecycleView();

        //set swipe Refresh
        setSwipeRefresh();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity())))
        {
            //swipeRefresh.setRefreshing(true);
            showProgressBar();
            new Handler().postDelayed(this::call_getAllReminder,500);

        } else Helper.NetworkError(requireActivity());


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                //hideViews();
            }

            @Override
            public void onShow() {
                //showViews();
            }

        });

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
                    //if (ll_backToTop.getVisibility() == View.VISIBLE)
                    //{
                    // new Animations().slideOutBottom(ll_backToTop);
                    // ll_backToTop.setVisibility(View.GONE);
                    // }


                    //TODO Rohan 16-09-2019
                    if (!swipeRefresh.isRefreshing())
                    {
                        //if swipe refreshing is on means user has done swipe-refreshed
                        //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                        //to avoid this, Have added below api call within this block

                        Log.e(TAG, "onScrollStateChanged: current_page "+current_page );
                        Log.e(TAG, "onScrollStateChanged: last_page "+last_page );
                        if (current_page <= last_page)  //
                        {
                            if (Helper.isNetworkAvailable(requireActivity()))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                new Handler().postDelayed(() -> call_getAllReminder(),500);

                            } else Helper.NetworkError(requireActivity());

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
                    //new Animations().slideOutBottom(ll_backToTop);
                    //ll_backToTop.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
                    //ll_backToTop.setVisibility(View.VISIBLE);
                    //new Animations().slideInBottom(ll_backToTop);
                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );
                    // new Animations().slideOutBottom(ll_backToTop);
                    // ll_backToTop.setVisibility(View.GONE);
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

        // set swipe
        swipe();
    }

    public void onPageChange(){

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();

        //user_id = sharedPreferences.getInt("user_id", 0);
        project_id = sharedPreferences.getInt("project_id", 0);
        sales_person_id = sharedPreferences.getInt("selected_sales_person_id",  sharedPreferences.getInt("user_id", 0));
        api_token = sharedPreferences.getString("api_token", "");
        todoDate = sharedPreferences.getString("todoDate", "");
        startDate = sharedPreferences.getString("startDate", "");
        endDate = sharedPreferences.getString("endDate", "");

        Log.e(TAG, "onPageChange: todoDate : "+todoDate+"startDate:"+startDate+"endDate:"+ endDate);
        if(Helper.isNetworkAvailableContext(requireActivity())){
            swipeRefresh.setRefreshing(true);
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = last_page = 1;
            call_getAllReminder();
        }else {
            Helper.NetworkError(requireActivity());
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: fragment " );

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            if (sharedPreferences.getBoolean("isReminderAdded", false)) {

                Log.e(TAG, "onResume: isReminderAdded "+ sharedPreferences.getBoolean("isReminderAdded", false));

                //update flag to false
                editor.putBoolean("isReminderAdded", false);

                if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                    //set home tab count
                    call_getCallLogCount();

                    /*swipeRefresh.setRefreshing(true);
                    //1. clear arrayList
                    itemArrayList.clear();
                    //2. reset page flag to 1
                    current_page = last_page = 1;
                    //3.call reminder api
                    call_getAllReminder();*/
                }
                else
                {
                    //show network error
                    Helper.NetworkError(requireActivity());
                    //hide main layouts
                    swipeRefresh.setRefreshing(false);
                    recyclerView.setVisibility(View.GONE);
                    //visible no data
                    ll_noDataFound.setVisibility(View.VISIBLE);
                }
            }
            editor.apply();
        }

    }


    //SetUpRecyclerView
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new HomeReminderAdapter(getActivity(), itemArrayList,fragment_reminders);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(requireActivity())) {

                //3.set home tab count
                call_getCallLogCount();
                //1. clear arrayList
                itemArrayList = new ArrayList<>();
                itemArrayList.clear();
                //2. reset page flag to 1
                current_page = 1;
                last_page = 1;

                /*put tab value*/
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putInt("tabAt", 3);
                    editor.apply();
                }
/*
                if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    editor.remove("isFilter");
                    editor.remove("filterCount");
                    editor.remove("tabAt");
                    editor.putBoolean("clearFilter", false);
                    editor.apply();
                }*/

                //call api
                swipeRefresh.setRefreshing(true);
                //4. call reminder api
                call_getAllReminder();


                //resetCallGetReminder();
            }
            else {

                Helper.NetworkError(requireActivity());
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


    public void resetCallGetReminder()
    {
        call_getCallLogCount();

        if (recyclerAdapter!=null)
        {
            int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
            Log.e(TAG, "count: "+count );

            if (count == 0) {
                Log.e(TAG, "resetCallGetReminder: call paginate api");
                swipeRefresh.setRefreshing(true);

                new Helper().showCustomToast(requireActivity(), "Please wait. Loading next reminders...");
                //call to paginate api
                new Handler().postDelayed(this::call_PaginateApi, 1200);
            }
        }

    }


    //CallReminderAPI
    private void call_getAllReminder()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getHomeReminders(api_token,sales_person_id,project_id,current_page,search_text,todoDate,startDate,endDate);
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
                    public void onCompleted()
                    {
                        Log.e(TAG, "onCompleted: "+current_page );
                        if (current_page ==2 ) delayRefresh();
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

                                                setReminderJson(jsonObject.getAsJsonObject());
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

    private void setReminderJson(JsonObject jsonObject)
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

        ReminderModel myModel = new ReminderModel();
        if (jsonObject.has("remind_at")) myModel.setReminder_date(!jsonObject.get("remind_at").isJsonNull() ? jsonObject.get("remind_at").getAsString() : "" );
        if (jsonObject.has("reminder_comments")) myModel.setReminder_name(!jsonObject.get("reminder_comments").isJsonNull() ? jsonObject.get("reminder_comments").getAsString() : "" );
        if (jsonObject.has("is_done")) myModel.setMark_as_done(!jsonObject.get("is_done").isJsonNull() ? jsonObject.get("is_done").getAsInt() : 0);
        if (jsonObject.has("reminder_id")) myModel.setReminder_id(!jsonObject.get("reminder_id").isJsonNull() ? jsonObject.get("reminder_id").getAsInt() : 0);
        if (jsonObject.has("remind_at_date_format")) myModel.setRemind_at_date_format(!jsonObject.get("remind_at_date_format").isJsonNull() ? jsonObject.get("remind_at_date_format").getAsString() : Helper.getTodaysDateString());
        if (jsonObject.has("remind_at_date")) myModel.setRemind_at_date(!jsonObject.get("remind_at_date").isJsonNull() ? jsonObject.get("remind_at_date").getAsString() : Helper.getTodaysDateString());
        if (jsonObject.has("remind_at_time")) myModel.setRemind_at_time_format(!jsonObject.get("remind_at_time").isJsonNull() ? jsonObject.get("remind_at_time").getAsString() : "");
        if (jsonObject.has("remind_at_time_format")) myModel.setRemind_at_time_format1(!jsonObject.get("remind_at_time_format").isJsonNull() ? jsonObject.get("remind_at_time_format").getAsString() : "");
        if (jsonObject.has("done_at_date")) myModel.setDone_at_date(!jsonObject.get("done_at_date").isJsonNull() ? jsonObject.get("done_at_date").getAsString() : "");
        if (jsonObject.has("sales_person_name")) myModel.setSales_person_name(!jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "--");
        itemArrayList.add(myModel);
        Log.e(TAG, "setJson: Size"+itemArrayList.size() );
        Log.e(TAG, "setJson: Size"+itemArrayList.toString() );
    }

    //DelayRefresh
    public  void delayRefresh()
    {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                Log.e(TAG, "delayRefresh: Size"+itemArrayList.size() );

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new HomeReminderAdapter(getActivity(), itemArrayList, fragment_reminders);
                recyclerView.setAdapter(recyclerAdapter);

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );

                if (count == 0) {

                    new Handler().postDelayed(() -> {
                        //no data
                        recyclerView.setVisibility(View.GONE);
                        ll_noDataFound.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    },500);

                } else {
                    //Registrations are available
                    new Handler().postDelayed(() -> {
                        //Registrations are available
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
        Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {

            Log.e(TAG, "notifyRecyclerDataChange: current_page" +current_page);
            Log.e(TAG, "notifyRecyclerDataChange: size" +itemArrayList.size());
            //hide pb
            swipeRefresh.setRefreshing(false);
            hideProgressBar();

            if (recyclerView.getAdapter()!=null)
            {
                Log.e(TAG, "notifyRecyclerDataChange: in adapter " );
                //recyclerView adapter
                recyclerView.getAdapter().notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    new Handler().postDelayed(() -> {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noDataFound.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    },500);

                } else {

                    //Registrations are available
                    new Handler().postDelayed(() -> {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noDataFound.setVisibility(View.GONE);
                        //exFab.setVisibility(View.VISIBLE);
                    },500);


                }

            }

        });

    }


    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {
            if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
            Helper.onErrorSnack(requireActivity(),message);
            recyclerView.setVisibility(View.GONE);
            ll_noDataFound.setVisibility(View.VISIBLE);
        });
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
                FragmentHome.getInstance().onSetTabsViewPager(call_schedule_count, site_visit_count,lead_count, reminder_count,3, true,false);
            });
        }
    }




    //Swipe Method
    private void swipe()
    {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /*This is useful for icon set on left and right swipe(12/09/2019)--Sukrut*/
            @Override
            public void onChildDraw (@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.secondaryColor))
                        .addSwipeRightActionIcon(R.drawable.ic_check_black_24dp)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }



            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                recyclerPosition= viewHolder.getAdapterPosition();

                //swiped position

                if (direction == ItemTouchHelper.LEFT) {

                    //swipe left

                    //Check Network Available
                    if (Helper.isNetworkAvailable(requireActivity())) {
                        //swipe left
                        ReminderModel item = recyclerAdapter.getData().get(recyclerPosition);
                        call_deleteReminder(item);
                    } else Helper.NetworkError(requireActivity());
                }
                else if(direction == ItemTouchHelper.RIGHT){

                    //swipe right

                    //Check Network Available
                    if (Helper.isNetworkAvailable(requireActivity()))
                    {
                        ReminderModel item = recyclerAdapter.getData().get(recyclerPosition);
                        call_markAsDone(item);
                    }
                    else Helper.NetworkError(requireActivity());
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    //Delete Reminder api
    private void call_deleteReminder(ReminderModel item) {

        if (Helper.isNetworkAvailable(requireActivity())) {

            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("api_token", api_token);
            jsonObject.addProperty("reminder_id",item.getReminder_id() );
            ApiClient client = ApiClient.getInstance();
            Call<JsonObject> call = client.getApiService().deleteReminder(jsonObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        //ll_pb.setVisibility(View.GONE);
                        switch (success) {
                            case "1":
                                DeleteReminder();
                                break;
                            case "2":
                                showErrorLog("Error occurred during deleting reminder!");
                                break;
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
            });

        } else Helper.NetworkError(requireActivity());

    }

    private void DeleteReminder()
    {

        Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {

            recyclerAdapter.removeItemSwipe(recyclerPosition);
            notifyRecyclerDataChange();

            resetCallGetReminder();
            call_getCallLogCount();

//                Snackbar snack = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Reminder Deleted Successfully!", Snackbar.LENGTH_LONG);
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snack.getView().getLayoutParams();
//                params.setMargins(15, 10, 15, 20);
//                snack.setAction("UNDO", view -> {
//                    recyclerAdapter.restoreItem(item, recyclerPosition);
//                    recyclerView.scrollToPosition(recyclerPosition);
//                });
//                snack.setActionTextColor(Color.YELLOW);
//                snack.show();

        });
    }

    private void call_PaginateApi(){

        //call paginate api to load next items
        Log.e(TAG, "call_PaginateApi: current_page "+current_page );
        Log.e(TAG, "call_PaginateApi: last_page "+last_page );
        if (current_page <= last_page)  //
        {
            if (Helper.isNetworkAvailable(requireActivity()))
            {
                //swipeRefresh.setRefreshing(true);
                showProgressBar();
                call_getAllReminder();
            } else Helper.NetworkError(requireActivity());
        }
        else {
            swipeRefresh.setRefreshing(false);
            Log.e(TAG, "Last page");
        }
    }

    //MarkAsDone API
    private void call_markAsDone(ReminderModel item) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("is_done", 1);
        jsonObject.addProperty("reminder_id",item.getReminder_id() );
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().markAsDoneReminder(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.body() != null) {
                    String success = response.body().get("success").toString();
                    switch (success) {
                        case "1":
                            markAsDoneReminder();
                            new Helper().showCustomToast(requireActivity(),"Mark as done Successfully");
                            break;
                        case "2":
                            showErrorLog("Error occurred during reminder!");
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void markAsDoneReminder()
    {
        Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {

            recyclerAdapter.removeItemSwipe(recyclerPosition);
            notifyRecyclerDataChange();
            resetCallGetReminder();
            call_getCallLogCount();

            //TODO
            //recyclerAdapter.removeItemSwipe(recyclerPosition);
            //delayRefresh();
        });
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
            searchView.setQueryHint(getString(R.string.search_reminder));

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
                    if (!newText.trim().isEmpty()) {
                        doFilter(newText);
                    }
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

        if (Helper.isNetworkAvailable(requireActivity()))
        {
            //1. clear arrayList
            itemArrayList.clear();

            current_page = last_page = 1;

            swipeRefresh.setRefreshing(true);
            showProgressBar();
            search_text = query;
            //call get sales feed api
            call_getAllReminder();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

        } else Helper.NetworkError(requireActivity());

    }





    @SuppressLint("SetTextI18n")
    private void showProgressBar() {
        //hideSoftKeyboard(getActivity(), getWindow().getDecorView().getRootView());
        ll_loadingContent.setVisibility(View.VISIBLE);
        // Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        ll_loadingContent.setVisibility(View.GONE);
        //Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
    public void onStop() {
        super.onStop();
      //  onStop = true;
        Log.e(TAG, "onStop: ");
    }

   /* @Override
    public void callOnTabChangedMethod() {
        Log.e(TAG, "callOnTabChangedMethod: ");
    }*/
}


