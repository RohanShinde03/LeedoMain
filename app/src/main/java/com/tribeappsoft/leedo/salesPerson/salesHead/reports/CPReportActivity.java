package com.tribeappsoft.leedo.salesPerson.salesHead.reports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;

import com.tribeappsoft.leedo.salesPerson.salesHead.reports.adapter.CPReportRecyclerAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.cp_filter.AllProjectsActivity;
import com.tribeappsoft.leedo.admin.reports.teamStats.model.TeamStatsModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;
import com.tribeappsoft.leedo.util.ZoomLinearLayout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class CPReportActivity extends AppCompatActivity {

    @BindView(R.id.spl_cpReport_refresher) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_cpReport_recycler) RecyclerView recyclerView;
    @BindView(R.id.ll_cpReport_legends) LinearLayoutCompat ll_legends;
    @BindView(R.id.ll_cpReport_filter) LinearLayoutCompat ll_filter;
    @BindView(R.id.mTv_cpReport_FilterText) MaterialTextView mTv_FilterText;
    @BindView(R.id.iv_cpReport_clearFilter) AppCompatImageView iv_clearFilter;

    @BindView(R.id.zll_cpReport_teamStats)
    ZoomLinearLayout zll;
    @BindView(R.id.ll_cpReport_not_found) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_cpReport_go_to_top) LinearLayoutCompat ll_back_to_top;
    @BindView(R.id.pb_cpReport) ContentLoadingProgressBar pb;

    private Activity context;
    private ArrayList<TeamStatsModel> itemArrayList;
    private CPReportRecyclerAdapter recyclerAdapter;
    private int current_page =0, last_page =0;
    private long selected_project_id =0;
    private String TAG = "CPReportActivity", api_token ="", search_text = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AppCompatTextView tvFilterItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_p_report);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = CPReportActivity.this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>CP Report Stats</font>"));
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

        hideProgressBar();

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getCpWiseReport();
        }
        else {
            Helper.NetworkError(context);
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible legends
            ll_legends.setVisibility(View.GONE);
            //visible no data
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
                    if (ll_back_to_top.getVisibility() == View.VISIBLE)
                    {
                        new Animations().slideOutBottom(ll_back_to_top);
                        ll_back_to_top.setVisibility(View.GONE);
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
                            if (Helper.isNetworkAvailable(Objects.requireNonNull(CPReportActivity.this)))
                            {
                                //swipeRefresh.setRefreshing(true);
                                showProgressBar();
                                call_getCpWiseReport();
                            } else Helper.NetworkError(Objects.requireNonNull(CPReportActivity.this));

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
                    new Animations().slideOutBottom(ll_back_to_top);
                    ll_back_to_top.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
                    ll_back_to_top.setVisibility(View.VISIBLE);
                    new Animations().slideInBottom(ll_back_to_top);
                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );
                    //hide pb
                    hideProgressBar();

                    new Animations().slideOutBottom(ll_back_to_top);
                    ll_back_to_top.setVisibility(View.GONE);
                }
            }

        });



        //scroll to top
        ll_back_to_top.setOnClickListener(v -> {
            Log.e(TAG, "Back TO TOP");
            recyclerView.smoothScrollToPosition(0);

            new Animations().slideOutBottom(ll_back_to_top);
            ll_back_to_top.setVisibility(View.GONE);

        });

        // zll inventoryHome
        zll.setOnTouchListener((v, event) -> {
            zll.init(CPReportActivity.this);
            return false;
        });


        //clear filter
        iv_clearFilter.setOnClickListener(v -> {
            //set refresh api
            resetApiCall();
        });


    }

    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new CPReportRecyclerAdapter(context, itemArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }

    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(context)) {

                //set swipe refreshing to true
                swipeRefresh.setRefreshing(true);
                //reset api call
                resetApiCall();

            }
            else {

                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            }
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    @Override
    protected void onResume() {
        super.onResume();

        //set filter
        setFilter();
    }


    private void hideViews()
    {
        ll_legends.animate().translationY(-ll_legends.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews()
    {
        ll_legends.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }


    //CallReminderAPI
    private void call_getCpWiseReport()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getCPWiseReport(api_token, current_page, search_text, selected_project_id);
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

        TeamStatsModel myModel = new TeamStatsModel();
        if (jsonObject.has("cp_id")) myModel.setSales_person_id(!jsonObject.get("cp_id").isJsonNull() ? jsonObject.get("cp_id").getAsInt() : 0 );
        if (jsonObject.has("name")) myModel.setFull_name(!jsonObject.get("name").isJsonNull() ? jsonObject.get("name").getAsString() : "" );
        if (jsonObject.has("leads")) myModel.setLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0" );
        if (jsonObject.has("leads_site_visits")) myModel.setLeads_site_visits(!jsonObject.get("leads_site_visits").isJsonNull() ? jsonObject.get("leads_site_visits").getAsString() : "0");
        if (jsonObject.has("lead_tokens")) myModel.setLead_tokens(!jsonObject.get("lead_tokens").isJsonNull() ? jsonObject.get("lead_tokens").getAsString() : "0");
        if (jsonObject.has("booking_master")) myModel.setBooking_master(!jsonObject.get("booking_master").isJsonNull() ? jsonObject.get("booking_master").getAsString() : "0");
        if (jsonObject.has("lead_token_ghp_plus")) myModel.setLead_tokens_ghp_plus(!jsonObject.get("lead_token_ghp_plus").isJsonNull() ? jsonObject.get("lead_token_ghp_plus").getAsString() : "0");
        if (jsonObject.has("cp_executives")) myModel.setFos_count(!jsonObject.get("cp_executives").isJsonNull() ? jsonObject.get("cp_executives").getAsString() : "0");

        itemArrayList.add(myModel);
    }

    //DelayRefresh
    public  void delayRefresh()
    {
        //if (context != null)
        {

            runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);
                //hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new CPReportRecyclerAdapter(context, itemArrayList);
                recyclerView.setAdapter(recyclerAdapter);

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );

                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //visible legends
                    ll_legends.setVisibility(View.GONE);
                    //exFab.setVisibility(View.GONE);
                } else {

                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //visible legends
                    ll_legends.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        //if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

                if (recyclerAdapter!=null)
                {
                    //recyclerView adapter
                    recyclerAdapter.notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noData.setVisibility(View.VISIBLE);
                        //visible legends
                        ll_legends.setVisibility(View.GONE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noData.setVisibility(View.GONE);
                        //visible legends
                        ll_legends.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.VISIBLE);
                    }
                }

            });
        }

    }


    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(CPReportActivity.this,message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void resetApiCall()
    {
        if (Helper.isNetworkAvailable(context))
        {
            swipeRefresh.setRefreshing(true);


            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putLong("filterProjectId", 0);
                editor.putString("filterProjectName", "");
                editor.apply();
            }

            //hide filter layout
            ll_filter.setVisibility(View.GONE);


            //Clear Search --> reset all params
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            selected_project_id =0;
            //3. Set search text clear
            search_text = "";
            //4. notify data set changed
            recyclerAdapter.notifyDataSetChanged();
            Log.e(TAG, "resetApiCall: ");

            swipeRefresh.setRefreshing(true);
            //call the api
            call_getCpWiseReport();
        }
        else
        {
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }
    }

    private void setFilter()
    {

        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            selected_project_id = sharedPreferences.getLong("filterProjectId", 0);
            String filterProjectName = sharedPreferences.getString("filterProjectName", "");
            editor.apply();

            if (selected_project_id > 0) {

                mTv_FilterText.setText(filterProjectName);
                ll_filter.setVisibility(View.VISIBLE);
                new Animations().wiggleEffect(ll_filter);

                if (recyclerAdapter != null)
                {
                    if (Helper.isNetworkAvailable(context))
                    {
                        itemArrayList = new ArrayList<>();
                        //1. clear arrayList
                        itemArrayList.clear();
                        //2. reset page flag to 1
                        current_page = 1;
                        last_page = 1;
                        //3. Get search text
                        search_text = "";
                        //4. notify data set changed
                        recyclerAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(true);
                        //call the api
                        call_getCpWiseReport();
                    }
                    else Helper.NetworkError(context);
                }
            }
        }


    }



    void showProgressBar() {
        pb.setVisibility(View.VISIBLE);
    }

    void hideProgressBar() {
        pb.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_self, menu);

        //MenuItem filterItem = menu.findItem(R.id.action_filter);
        //filterItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search_self);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(context).getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null)
        {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search));

            //Code for changing the search icon
            ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            icon.setColorFilter(Color.WHITE);
            //icon.setImageResource(R.drawable.ic_home_search);

            //AutoCompleteTextView searchTextView =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            AutoCompleteTextView searchTextView =  searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            /// Code for changing the textColor and hint color for the search view
            searchTextView.setHintTextColor(getResources().getColor(R.color.main_white));
            searchTextView.setTextColor(getResources().getColor(R.color.main_white));

            //Code for changing the voice search icon
            //ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            //voiceIcon.setImageResource(R.drawable.my_voice_search_icon);

            //Code for changing the close search icon
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
                    if(newText.trim().isEmpty())
                        doFilter(newText);
                    return false;
                }
            });


            searchView.setOnCloseListener(() -> {
                Log.e(TAG, "onCreateOptionsMenu: onClose ");
                //doFilter("");
                //resetApiCall();
                return false;
            });
        }
        if (searchView != null)
        {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
            }
        }

        final MenuItem menuItem = menu.findItem(R.id.action_filter);
        View actionView = menuItem.getActionView();
        tvFilterItemCount = actionView.findViewById(R.id.cart_badge);
        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));


        return true;
    }


    private void doFilter(String query) {

        if (Helper.isNetworkAvailable(context))
        {
            itemArrayList = new ArrayList<>();
            //1. clear arrayList
            itemArrayList.clear();
            //2. reset page flag to 1
            current_page = 1;
            last_page = 1;
            //3. Get search text
            search_text = query;
            //4. notify data set changed
            recyclerAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(true);
            //call the api
            call_getCpWiseReport();
        }
        else Helper.NetworkError(context);
    }

    private void setupBadge() {

        if (tvFilterItemCount != null) {
            int filterCount = 0;
            Log.e(TAG, "setupBadge: "+ filterCount);
            if (tvFilterItemCount.getVisibility() != View.GONE) {
                tvFilterItemCount.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case (android.R.id.home):
                onBackPressed();
                break;

            case (R.id.action_filter):
                startActivity(new Intent(context, AllProjectsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }
}
