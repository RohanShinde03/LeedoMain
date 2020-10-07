package com.tribeappsoft.leedo.salesPerson.homeFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reminder.RecyclerViewSwipeDecorator;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.salesPerson.adapter.ReminderAdapter;
import com.tribeappsoft.leedo.salesPerson.models.ReminderModel;
import com.tribeappsoft.leedo.util.Animations;
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
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragmentSalesPersonReminders extends Fragment {


    @BindView(R.id.toolbar_reminders) MaterialToolbar toolbar;
    @BindView(R.id.cl_fragSalesPerson_reminder) CoordinatorLayout parent;
    @BindView(R.id.sr_reminder) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_reminder) RecyclerView recyclerView;
    @BindView(R.id.ll_reminder_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.exFab_hold_reminder) ExtendedFloatingActionButton exFab;
    @BindView(R.id.pb_reminder) ContentLoadingProgressBar pb;
    @BindView(R.id.ll_reminder_backToTop) LinearLayoutCompat ll_backToTop;
    //@BindView(R.id.mBtn_ReminderBackToTop) MaterialButton mBtn_backToTop;

    private String TAG = "FragSalesPersonReminders";
    private ReminderAdapter recyclerAdapter;
    private int userId=0;
    private String api_token="";
    private ArrayList<ReminderModel> itemArrayList;
    private Context context;
    private FragmentSalesPersonReminders fragmentSalesPersonReminders;
    private int current_page =1, last_page =1,recyclerPosition=0;

    public FragmentSalesPersonReminders() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sales_person_reminders, container, false);
        fragmentSalesPersonReminders=FragmentSalesPersonReminders.this;
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);
        toolbar.setTitle("Reminder");
        setupUI(parent);

        try {
            rootView.setFocusableInTouchMode(true);
            rootView.requestFocus();
            rootView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //goto Home Fragment
                        openFragment();
                        //doOnBackPressed();
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        //SharedPreference
        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        userId = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        itemArrayList = new ArrayList<>();


        //SetRecyclerView
        setupRecycleView();

        //set swipe Refresh
        setSwipeRefresh();


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
                        /*if (current_page <= last_page)  //
                        {
                            if (isNetworkAvailable(Objects.requireNonNull(getActivity())))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                call_getAllReminder();
                            } else NetworkError(Objects.requireNonNull(getActivity()));

                        } else Log.e(TAG, "Last page");*/
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

        //exFab.setOnClickListener(v -> startActivity(new Intent(context, AddReminderActivity.class)));

        // set swipe
        swipe();

        return rootView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    private void openFragment()
    {
        startActivity(new Intent(getActivity(), SalesPersonBottomNavigationActivity.class)
                        .putExtra("notifyPerformance", true)
                        .putExtra("openFlag", 0)
                //.addFlags(FLAG_ACTIVITY_CLEAR_TOP |  FLAG_ACTIVITY_SINGLE_TOP)
        );
        getActivity().finish();
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: frag " );

        //Call get All Reminders API
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

            swipeRefresh.setRefreshing(true);
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = last_page = 1;
            call_getAllReminder();
        }
        else
        {
            //show network error
            Helper.NetworkError(getActivity());
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible no data
            ll_noDataFound.setVisibility(View.VISIBLE);
        }
    }


    //SetUpRecyclerView
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new ReminderAdapter(getActivity(), itemArrayList,fragmentSalesPersonReminders);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

               /* //1. clear arrayList
                itemArrayList.clear();
                //2. reset page flag to 1
                current_page = 1;
                last_page = 1;

                //call api
                swipeRefresh.setRefreshing(true);
                call_getAllReminder();*/

                resetCallGetReminder();
            }
            else {

                Helper.NetworkError(getActivity());
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

        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;

            //call api
            swipeRefresh.setRefreshing(true);
            call_getAllReminder();
        }
        else
        {
            Helper.NetworkError(getActivity());
        }

    }


    //CallReminderAPI
    private void call_getAllReminder()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().Get_AllReminder(api_token,userId,current_page,"");
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
                        if (current_page ==1 ) delayRefresh();
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
        if (jsonObject.has("remind_at_time_format")) myModel.setRemind_at_time_format(!jsonObject.get("remind_at_time_format").isJsonNull() ? jsonObject.get("remind_at_time_format").getAsString() : "");
        if (jsonObject.has("remind_at_time_format1")) myModel.setRemind_at_time_format1(!jsonObject.get("remind_at_time_format1").isJsonNull() ? jsonObject.get("remind_at_time_format1").getAsString() : "");
        itemArrayList.add(myModel);
    }

    //DelayRefresh
    public  void delayRefresh()
    {
        if (context != null) {
            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
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

                if (current_page <= last_page)  //
                {
                    if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
                    {
                        //swipeRefresh.setRefreshing(true);
                        showProgressBar();
                        call_getAllReminder();
                    } else Helper.NetworkError(Objects.requireNonNull(getActivity()));

                } else Log.e(TAG, "Last page");
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
                    if (current_page <= last_page)  //
                    {
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
                        {
                            //swipeRefresh.setRefreshing(true);
                            showProgressBar();
                            call_getAllReminder();
                        } else Helper.NetworkError(Objects.requireNonNull(getActivity()));

                    } else Log.e(TAG, "Last page");

                }

            });
        }

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
                Helper.onErrorSnack(getActivity(),message);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
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
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(context, R.color.secondaryColor))
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
                    if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                        //swipe left
                        ReminderModel item = recyclerAdapter.getData().get(recyclerPosition);
                        call_deleteReminder(item);
                    } else Helper.NetworkError(getActivity());
                }
                else if(direction == ItemTouchHelper.RIGHT){

                    //swipe right

                    //Check Network Available
                    if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
                    {
                        ReminderModel item = recyclerAdapter.getData().get(recyclerPosition);
                        call_markAsDone(item);
                    }
                    else Helper.NetworkError(getActivity());
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    //Delete Reminder api
    private void call_deleteReminder(ReminderModel item) {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

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
                    try {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } else Helper.NetworkError(getActivity());

    }

    private void DeleteReminder()
    {

        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                recyclerAdapter.removeItemSwipe(recyclerPosition);
                notifyRecyclerDataChange();


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
    }

    public void call_api(){

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
                            new Helper().showCustomToast(Objects.requireNonNull(getActivity()),"Mark as done Successfully");
                            break;
                        case "2":
                            showErrorLog("Error occurred during reminder!");
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void markAsDoneReminder()
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                recyclerAdapter.removeItemSwipe(recyclerPosition);
                notifyRecyclerDataChange();


                //TODO
                //recyclerAdapter.removeItemSwipe(recyclerPosition);
                //delayRefresh();
            });
        }
    }


    //SetupUI
    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(Objects.requireNonNull(getActivity()), view);
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

}

