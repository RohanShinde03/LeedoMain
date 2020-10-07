package com.tribeappsoft.leedo.salesPerson.bookings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.salesPerson.adapter.BookingEventsAdapter;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

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

public class BookingEventsActivity extends AppCompatActivity {

    private static final String TAG = "BookingEventsActivity";
    private AppCompatActivity context;

    @BindView(R.id.btn_frag_salesPersonEvents_ongoing) MaterialButton btn_ongoing;
    @BindView(R.id.btn_frag_salesPersonEvents_upcoming) MaterialButton btn_upcoming;
    @BindView(R.id.sr_frag_salesPersonEvents) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_frag_salesPersonEvents) RecyclerView recyclerView;
    @BindView(R.id.ll_frag_salesPersonEventsList) LinearLayoutCompat ll_studentEventList;
    @BindView(R.id.ll_frag_salesPersonEvents_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.tv_frag_salesPerson_noEventsText) AppCompatTextView tv_noStudentEvent;

    private String api_token = null;
    private int sales_person_id = 0, eventStatusId =1; //TODO 1-> OnGoing 2-> Upcoming
    private ArrayList<EventsModel> eventModelArrayList = new ArrayList<>();
    private BookingEventsAdapter eventAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean notifyEvents=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_events);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        context = BookingEventsActivity.this;
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.events));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null)
        {
            //showLeads = getIntent().getBooleanExtra("showLeads", false);
            notifyEvents = getIntent().getBooleanExtra("notifyEvents", false);
        }


        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        sales_person_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        eventModelArrayList = new ArrayList<>();


        //set up def selected
        btn_ongoing.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btn_ongoing.setTextColor(getResources().getColor(R.color.main_white));
        eventStatusId = 1;

        btn_upcoming.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        btn_upcoming.setTextColor(getResources().getColor(R.color.colorAccent));

        //set fab animate
        //new Handler().postDelayed(this::animateFab, 500);

        //set up recyclerView
        setupRecycleView();

        //setup swipeRefresh
        setSwipeRefresh();


        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            //TODO Replace method call
            swipeRefresh.setRefreshing(true);
            call_getEvent();

        } else {

            //show network error
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }


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


    private void setupRecycleView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        eventAdapter = new BookingEventsAdapter(context,eventModelArrayList,eventStatusId);
        recyclerView.setAdapter(eventAdapter);
    }


    private void setSwipeRefresh() {

        //getOffline();
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            swipeRefresh.setOnRefreshListener(() ->
            {
                swipeRefresh.setRefreshing(true);
                call_getEvent();
                // setTempData();

            });

           /* swipeRefresh.setColorSchemeResources(R.color.secondaryColor,
                    R.color.secondaryColor,
                    R.color.secondaryColor,
                    R.color.secondaryColor);*/

            swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);


        }else Helper.NetworkError(context);
    }

    private void hideViews()
    {

        ll_studentEventList.animate().translationY(-ll_studentEventList.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_studentEventList.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE );
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }


    @Override
    public void onResume() {
        super.onResume();


        btn_upcoming.setOnClickListener(v -> {

            btn_upcoming.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btn_upcoming.setTextColor(getResources().getColor(R.color.main_white));

            btn_ongoing.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
            btn_ongoing.setTextColor(getResources().getColor(R.color.colorAccent));

            eventStatusId = 2;

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //TODO Replace method call
                call_getEvent();

               /* int count1 = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count1: "+count1 );*/
            } else {
                Helper.NetworkError(context);
            }

        });

        btn_ongoing.setOnClickListener(v -> {


            //btn_ongoing.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_rectangle_gmail_red_round, context.getTheme()));
            btn_ongoing.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btn_ongoing.setTextColor(getResources().getColor(R.color.main_white));

            btn_upcoming.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
            btn_upcoming.setTextColor(getResources().getColor(R.color.colorAccent));

            eventStatusId = 1;

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                //TODO Replace method call
                call_getEvent();
            } else {
                Helper.NetworkError(context);
            }
        });

    }


    /**For Event**/
    private void call_getEvent() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getBookingEvents(api_token,sales_person_id,eventStatusId);
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
                        delayRefresh();
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

        eventModelArrayList.add(eventModel);
    }

    private void delayRefresh() {

        if (context != null) {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                eventAdapter = new BookingEventsAdapter(context, eventModelArrayList,eventStatusId);
                recyclerView.setAdapter(eventAdapter);

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {

                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    switch (eventStatusId)
                    {
                        case 1: //ongoing
                            tv_noStudentEvent.setText(context.getString(R.string.you_have_no_ongoing_events));
                            break;
                        case 2://upcoming
                            tv_noStudentEvent.setText(context.getString(R.string.you_have_no_upcoming_events));
                            break;
                        case 3: //previous
                            tv_noStudentEvent.setText(context.getString(R.string.you_have_no_previous_events));
                            break;
                        default:
                            tv_noStudentEvent.setText(context.getString(R.string.you_have_no_events));
                            break;
                    }

                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }
            });
        }

    }


    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                //hideProgressBar();
                Helper.onErrorSnack(context,message);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }

    }
    /*Overflow Menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }

    @Override
    public void onBackPressed()
    {

        if(notifyEvents)
        {
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            finish();
        }else super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }


}


/*private void setTempData()
    {
        EventsModel eventModel_1 =new EventsModel();
        eventModel_1.setEvent_id(1);
        eventModel_1.setReg_start_date( "21 Aug 2019" );
        eventModel_1.setReg_end_date( "22 Aug 2019" );
        eventModel_1.setEvent_venue( "Hinjewadi Phase 2, Pune" );
        eventModel_1.setEvent_banner_path( "https://javdekars.com/images/banner/its-time-project-banner.jpg");
        eventModel_1.setStart_date( "24 Aug 2019" );
        eventModel_1.setEnd_date( "25 Aug 2019");
        eventModel_1.setEvent_title("YashOne Hinjewadi Booking");
        eventModel_1.setEvent_description("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        eventModel_1.setEvent_fee("event_fee");
        eventModel_1.setStatus("status");
        eventModel_1.setEvent_status("event_status");


        eventModelArrayList.add(eventModel_1);

        swipeRefresh.setRefreshing(false);
    }*/