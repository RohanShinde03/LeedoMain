package com.tribeappsoft.leedo.admin.booked_customers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callLog.CallLogActivity;
import com.tribeappsoft.leedo.admin.callLog.TelephonyCallService;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.salesPerson.adapter.CustomerAdapter;
import com.tribeappsoft.leedo.salesPerson.models.FeedsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.stickyScrollView.StickyScrollView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class BookedCustomersActivity extends AppCompatActivity {

    private String TAG="BookedCustomersActivity";
    @BindView(R.id.cl_allBookedCustomers) CoordinatorLayout parent;
    @BindView(R.id.ll_allBookedCustomers_filter) LinearLayoutCompat ll_filter;
    @BindView(R.id.tv_allBookedCustomers_filterTitle) AppCompatTextView tv_filterTitle;
    @BindView(R.id.iv_allBookedCustomers_closeFilter) AppCompatImageView iv_closeFilter;
    @BindView(R.id.nsv_allBookedCustomers) StickyScrollView nsv;
    @BindView(R.id.ll_allBookedCustomers_addFeedData) LinearLayoutCompat ll_addFeedData;
    @BindView(R.id.ll_allBookedCustomers_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_allBookedCustomers_backToTop) LinearLayoutCompat ll_backToTop;

    @BindView(R.id.ll_allBookedCustomers_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_allBookedCustomers_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_allBookedCustomers_search) AppCompatEditText edt_search;

    @BindView(R.id.iv_allBookedCustomers_VoiceSearch) AppCompatImageView iv_VoiceSearch;
    @BindView(R.id.iv_allBookedCustomers_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.sr_allBookedCustomers) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.view_allBookedCustomers) View view;
    @BindView(R.id.exFab_allBookedCustomers_markBook) ExtendedFloatingActionButton exFab_markBook;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<FeedsModel> modelArrayList;
    //private ArrayList<LeadListModel> leadListModelArrayList;
    //private ArrayList<EventsModel> eventsModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<String> namePrefixArrayList, leadStageStringArrayList;

    private CustomerAdapter adapter = null;

    private int openFlag = 0,user_id = 0,call = 0, lastPosition = -1, skip_count =0, call_lead_id =0, call_lead_status_id =0 ;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private String  api_token = "", filter_text="", display_text ="", last_lead_updated_at = null, customer_mobile = null,
            call_cuID= null, call_lead_name= "", call_project_name= "";
    //private Dialog claimDialog;
    private boolean stopApiCall = false,isSalesHead=false, onStop = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_customers);
        ButterKnife.bind(this);
        context= BookedCustomersActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_booked_customers));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //hide pb
        hideCancellationProgressBar();

        //initialise contents
        init();

        //set up scrollView
        setUpScrollView();

    }

    private String getFilterJson()
    {
        JsonObject jsonObject = new JsonObject();
        int selectedProjectId = 0;
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("lead_status_id", 3);

        Log.e(TAG, "jsonObject"+jsonObject.toString());
        return jsonObject.toString();
    }


    private void init()
    {
        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //gone toolbar
        // appbar.setVisibility(View.GONE);
        //toolbar.setVisibility(View.GONE);
        ll_filter.setVisibility(View.GONE);
        //ll_searchBar.setVisibility(View.GONE);

        //get Bundle arguments
     /*   Bundle bundle = this.getArguments();
        if (bundle != null) {
            //fromAchievements = bundle.getBoolean("fromAchievements",false);
            openFlag = bundle.getInt("openFlag", 0);
            other_ids = bundle.getString("other_ids", "");
            display_text = bundle.getString("display_text", "");
            Log.e(TAG, "onCreateView: openFlag "+ openFlag );
            Log.e(TAG, "init: otherIds "+ other_ids);
        }*/

        //init arrayLists
        modelArrayList = new ArrayList<>();
        //leadListModelArrayList = new ArrayList<>();
        //eventsModelArrayList = new ArrayList<>();
        leadStagesModelArrayList = new ArrayList<>();
        leadStageStringArrayList = new ArrayList<>();
        namePrefixArrayList = new ArrayList<>();

        namePrefixArrayList.add("Mr.");
        namePrefixArrayList.add("Ms.");
        namePrefixArrayList.add("Mrs.");
        namePrefixArrayList.add(".");

        //setup recyclerView
        //setupRecycleView();

        //set up swipeRefresh
        setSwipeRefresh();

        //call get lead data to get lead stages
        if (isNetworkAvailable(context)) getLeadData();

        if (openFlag ==0)
        {
            //set cached feed only for WO filtered data
            String jA_feeds = null;
            if (sharedPreferences.getString("jA_allBookings", null) != null) jA_feeds = sharedPreferences.getString("jA_allBookings", null);
            if (jA_feeds!=null) {

                //hide pb
                hideProgressBar();

                JsonArray jsonArray = new Gson().fromJson(jA_feeds, JsonArray.class);
                modelArrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //set feeds data
                setCachedFeeds();
            }
        }

        exFab_markBook.setOnClickListener(v -> new Handler().postDelayed(() -> startActivity(new Intent(context, CustomerIdActivity.class)
                .putExtra("fromSiteVisit_or_token", 3)
                .putExtra("forId", 3)
        ), 200));


        //voice input
        iv_VoiceSearch.setOnClickListener(view -> {
            //hide keyboard
            hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());
            //new Animations().clickEffect(iv_VoiceSearch);

            //set onStop false to call method in onResume
            onStop = false;
            //prompt speech input
            promptSpeechInput();

        });


        //close filter
        iv_closeFilter.setOnClickListener(v -> {

            //same action as swipeRefresh
            if (isNetworkAvailable(context))
            {

                //appbar.setVisibility(View.INVISIBLE);
                //toolbar.setVisibility(View.INVISIBLE);
                ll_filter.setVisibility(View.GONE);
                //visible slideView
                //sliderView.setVisibility(View.VISIBLE);


                swipeRefresh.setRefreshing(true);

                //set refresh api
                refreshFeedApi();

                //get event banners list
                //call events api
                //call get EventsBannersList
                //  new Handler(getMainLooper()).postDelayed(this::call_getEventBannerList, 1000);

                //get claim now leads
                //call_getUnClaimedLeads();
            }
            else NetworkError(context);
        });
    }


    private void setUpScrollView() {

        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {


            // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
            View view = v.getChildAt(v.getChildCount() - 1);


            // Calculate the scroll_diff
            int diff = (view.getBottom() - (v.getHeight() + v.getScrollY()));
            // if diff is zero, then the bottom has been reached
            if (diff == 0)
            {
                // notify that we have reached the bottom
                Log.e(TAG, "MyScrollView: Bottom has been reached");

                if (!swipeRefresh.isRefreshing())
                {

                    //if swipe refreshing is on means user has done swipe-refreshed
                    //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                    //to avoid this, Have added below api call within this block

                    //gone back to top
                    if (ll_backToTop.getVisibility() == View.VISIBLE)
                    {
                        new Animations().slideOutBottom(ll_backToTop);
                        ll_backToTop.setVisibility(View.GONE);
                    }

                    //swipeRefresh.setRefreshing(true);
                    showProgressBar();

                    Log.e(TAG, "onScrollStateChanged: call " + call);
                    if (!stopApiCall)  //call paginate api till ary is not empty
                    {
                        if (isNetworkAvailable(context)) {

                            //call get sales feed
                            //showProgressBar();

                            call_getSalesFeed();
                            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

                        } else NetworkError(context);
                    }
                    else {
                        Log.e(TAG, "stopApiCall");
                        hideProgressBar();
                    }
                }
            }

//            if (!nsv.canScrollVertically(1))
//            {
//                // bottom of scroll view
//                new Helper().showCustomToast(getActivity(), "Bottom");
//            }


            //scrolling up and down
            if(scrollY<oldScrollY){

                showViews();
                exFab_markBook.extend();

                //vertical scrolling down
                Log.d(TAG, "onCreateView: scrolling down" );

                if (call > 1 && ll_backToTop.getVisibility() == View.GONE ) {

                    if (modelArrayList.size()>3)
                    {
                        Log.d(TAG, "setUpScrollView: more than 3 cards");
                        ll_backToTop.setVisibility(View.VISIBLE);
                        new Animations().slideInBottom(ll_backToTop);
                    }
                }

                //show views
                if (openFlag!=0) showViews();

            }else{

                hideViews();
                exFab_markBook.shrink();

                //vertical scrolling upp
                Log.d(TAG, "onCreateView: scrolling up" );

                if (call > 1 && ll_backToTop.getVisibility() == View.VISIBLE ) {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }

                //hide views
                if (openFlag!=0) hideViews();
            }

            //reached at top of scrollView
            if (!nsv.canScrollVertically(-1)) {

                Log.d(TAG, "onCreateView: TOP of scrollView" );
                // top of scroll view
                if (ll_backToTop.getVisibility() == View.VISIBLE)
                {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }
            }
        });


        //scroll to top
        ll_backToTop.setOnClickListener(v -> {

            //LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            //Objects.requireNonNull(layoutManager).scrollToPositionWithOffset(0, 0);

            //recyclerView.smoothScrollToPosition(0);
            nsv.smoothScrollTo(0, 0);

            new Animations().slideOutBottom(ll_backToTop);
            ll_backToTop.setVisibility(View.GONE);
        });
    }


    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() ->
        {

            if (isNetworkAvailable(context)) {

                //appbar.setVisibility(View.INVISIBLE);
                //toolbar.setVisibility(View.INVISIBLE);

                ll_filter.setVisibility(View.GONE);

                swipeRefresh.setRefreshing(true);

                //set refresh api
                refreshFeedApi();


                //call get EventsBannersList
                //new Handler(getMainLooper()).postDelayed(this::call_getEventBannerList, 1000);

                //get claim now leads
                //call_getUnClaimedLeads();
            }
            else NetworkError(context);
        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            updateUI(intent);
//        }
//    };


    @Override
    public void onResume() {
        super.onResume();


        int  isLeadAdd=0;
        Log.e(TAG, "onResume: from onResume");

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            isLeadAdd = sharedPreferences.getInt("isLeadAdd",0);

            if (sharedPreferences.getBoolean("feedActionAdded", false)) {

                Log.e(TAG, "onResume: feedActionAdded "+ sharedPreferences.getBoolean("feedActionAdded", false));
                //if any action taken on feed make the stop flag to false
                onStop = false;

                //update flag to false
                editor.putBoolean("feedActionAdded", false);
            }
            editor.apply();
        }

        if(isLeadAdd == 1)
        {
            editor.remove("isLeadAdd");
            editor.apply();

            if (isNetworkAvailable(context)) {

                ll_filter.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(true);

                //set refresh api
                refreshFeedApi();

            }
            else NetworkError(context);


        }

        if (openFlag == 1) {

            //leads

            //Set Data For Leads Added
            tv_filterTitle.setText(String.format("%s leads", display_text));
            ll_filter.setVisibility(View.VISIBLE);

            //set search other_ids
            edt_search.setText(String.format("%s leads", display_text));
            /*ll_searchBar.setVisibility(View.INVISIBLE);*/
            //hide voice search
            iv_VoiceSearch.setVisibility(View.GONE);
            //visible clear search
            new Animations().slideInLeft(iv_clearSearch);
            iv_clearSearch.setVisibility(View.VISIBLE);
            //search for leads
            //filterFeedApi(other_ids + " leads");
            filterFeedApi();
        }
        else if (openFlag == 2) {

            //site visits

            //Set Data For Site Visits
            tv_filterTitle.setText(String.format("%s site visit", display_text));
            ll_filter.setVisibility(View.VISIBLE);

            //set search other_ids
            edt_search.setText(String.format("%s site visit", display_text));
            /*ll_searchBar.setVisibility(View.INVISIBLE);*/
            //hide voice search
            iv_VoiceSearch.setVisibility(View.GONE);
            //visible clear search
            new Animations().slideInLeft(iv_clearSearch);
            iv_clearSearch.setVisibility(View.VISIBLE);
            //search for ghp generated
            //filterFeedApi(other_ids + " site visit");
            filterFeedApi();
        }
        else if (openFlag == 3) {

            //ghp

            //Set Data For GHP generated
            tv_filterTitle.setText(String.format("%s ghp", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s ghp", display_text));
            /*ll_searchBar.setVisibility(View.INVISIBLE);*/
            //hide voice search
            iv_VoiceSearch.setVisibility(View.GONE);
            //visible clear search
            new Animations().slideInLeft(iv_clearSearch);
            iv_clearSearch.setVisibility(View.VISIBLE);
            //search for ghp generated
            //filterFeedApi(other_ids+  " ghp");
            filterFeedApi();
        }
        else if (openFlag == 4) {

            //on hold

            //Set Data For Bookings
            tv_filterTitle.setText(String.format("%s hold flats", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s hold flats", display_text));
            /*ll_searchBar.setVisibility(View.INVISIBLE);*/
            //hide voice search
            iv_VoiceSearch.setVisibility(View.GONE);
            //visible clear search
            new Animations().slideInLeft(iv_clearSearch);
            iv_clearSearch.setVisibility(View.VISIBLE);
            //search for booked flats
            //filterFeedApi(other_ids + " hold flats");
            filterFeedApi();
        }
        else if (openFlag == 5) {

            //bookings

            //Set Data For Bookings
            tv_filterTitle.setText(String.format("%s allotted flats", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s allotted flats", display_text));
            /*ll_searchBar.setVisibility(View.INVISIBLE);*/
            //hide voice search
            iv_VoiceSearch.setVisibility(View.GONE);
            //visible clear search
            new Animations().slideInLeft(iv_clearSearch);
            iv_clearSearch.setVisibility(View.VISIBLE);
            //search for booked flats
            //filterFeedApi(other_ids + " booked flats");
            filterFeedApi();
        }
        else {

            if (!onStop)
            {
                Log.e(TAG, "onResume: onStopped called" );
                if (isNetworkAvailable(context)) {

                    //gone visibility
                    ll_noData.setVisibility(View.GONE);
                    ll_filter.setVisibility(View.GONE);
                    //swipeRefresh.setRefreshing(true);

                    //resume feed api
                    resumeFeedApi();

                    //set scrollView scroll to top
                    // nsv.smoothScrollTo(0, 0);


                    /*new Handler(getMainLooper()).postDelayed(() -> {
                        //call events api
                        //swipeRefresh.setRefreshing(true);
                        //call get EventsBannersList
                        call_getEventBannerList();

                    }, 1000);*/
                }
                else
                {

                    ll_noData.setVisibility(View.VISIBLE);
                    /*ll_searchBar.setVisibility(View.INVISIBLE);*/
                    ll_addFeedData.setVisibility(View.GONE);
                    ll_loadingContent.setVisibility(View.GONE);
                    NetworkError(context);
                }
            }
        }

        //register broadCast receiver
        //if (broadcastReceiver!=null) getActivity().registerReceiver(broadcastReceiver, new IntentFilter(FireBaseMessageService.BROADCAST_ACTION));

        //perform search
        perform_search();

    }


    private void hideViews()
    {
        ll_filter.animate().translationY(-ll_filter.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        ll_search.animate().translationY(-ll_search.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_search.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        ll_filter.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }


    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                edt_search.setText(result != null ? result.get(0) : "");
                //clear focus and hide keyboard
                edt_search.clearFocus();
                hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());
            }
        }
    }


//    private void updateUI(Intent intent) {
//        if (intent!=null) {
//            if (isNetworkAvailable(getActivity())) {
//                swipeRefresh.setRefreshing(true);
//                //get claim now leads when broadCast Receiver receives
//                call_getUnClaimedLeads();
//            }
//            else NetworkError(getActivity());
//        }
//    }


    private void perform_search() {

        //or you can search by the editTextFiler

        //search ime action click
        edt_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //if (recyclerAdapter != null)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());

                    //call set search api
                    searchFeedApi();
                }

                return true;
            }
            return false;
        });


        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                //if (recyclerAdapter != null) {
                //  String other_ids = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                //  doFilter(other_ids);

                if (Objects.requireNonNull(Objects.requireNonNull(edt_search.getText()).toString()).length() < 1)
                {
                    edt_search.clearFocus();
                    hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());

                    if (iv_clearSearch.getVisibility() == View.VISIBLE)
                    {
                        //check for already visible or not

                        //hide clear search
                        iv_clearSearch.setVisibility(View.GONE);
                        //visible voice search
                        iv_VoiceSearch.setVisibility(View.VISIBLE);
                    }


                    Log.e(TAG, "afterTextChanged: < 1");

                    //reset feed api
                    //resetFeedApi();

                } else {

                    //for avoiding repeating animations
                    if (iv_clearSearch.getVisibility() == View.GONE)
                    {
                        //check for already visible or not

                        //hide voice search
                        iv_VoiceSearch.setVisibility(View.GONE);
                        //visible clear search
                        new Animations().slideInLeft(iv_clearSearch);
                        iv_clearSearch.setVisibility(View.VISIBLE);
                    }
                }


                //}
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                //iv_clearSearch.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }

        });


        //clear searchText
        iv_clearSearch.setOnClickListener(v -> {

            //clear edit Text
            edt_search.setText("");

            //call resetApi
            resetFeedApi();
        });
    }


    private void refreshFeedApi()
    {
        if (isNetworkAvailable(context))
        {
            //clear editText
            edt_search.setText("");
            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //1. clear arrayList
            modelArrayList.clear();
            //remove all view from feed
            ll_addFeedData.removeAllViews();

            //2. reset call flag to 0 && Filter flag to 0
            call = openFlag = 0;
            //3. Set search other_ids clear
            filter_text = "";
            //4. clear other ids & display text
            display_text = "";
            last_lead_updated_at = null;
            //call get sales feed api
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);
        } else NetworkError(context);

    }


    private void resetFeedApi()
    {
        if (isNetworkAvailable(context))
        {
            ll_noData.setVisibility(View.GONE);
            //Clear Search --> reset all params
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset page flag to 1
            call = openFlag = 0;
            //3. Set search filter_text clear
            filter_text = "";
            //4. Set search other_ids clear
            display_text = "";
            last_lead_updated_at = null;
            showProgressBar();
            //5. call get sales feed api
            call_getSalesFeed();
        }
        else NetworkError(context);
    }



    private void searchFeedApi()
    {
        if (isNetworkAvailable(context))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //remove all views
            ll_addFeedData.removeAllViews();
            //2. reset call flag to 0
            call = 0;
            //3. Get search other_ids
            filter_text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
            // -- if flag is from performance then add project name with search other_ids else add
            //filter_text = openFlag == 0 ? Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault()) : other_ids + " "+ Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());

            last_lead_updated_at = null;

            //swipeRefresh.setRefreshing(true);
            showProgressBar();

            //call get sales feed api
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

        } else NetworkError(context);
    }

    private void resumeFeedApi()
    {
        if (isNetworkAvailable(context))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset call flag to 0
            call = 0;
            //3. Get search other_ids
            filter_text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
            last_lead_updated_at = null;

            swipeRefresh.setRefreshing(true);
            //show progress bar
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);

        }else NetworkError(context);
    }

    private void filterFeedApi()
    {
        if (isNetworkAvailable(context))
        {
            //1. clear arrayList
            modelArrayList.clear();
            //2. reset call flag to 0
            call = 0;
            //3. Get filter other_ids
            filter_text = ""; //text;
            last_lead_updated_at = null;

            swipeRefresh.setRefreshing(true);

            //show progress bar
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);

        }else NetworkError(context);
    }


    private void getLeadData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadFormData(api_token);
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
                        //setLeadDetails();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLogUpdateLead(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLogUpdateLead(getString(R.string.weak_connection));
                        else showErrorLogUpdateLead(e.toString());
                    }
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject()) {
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                setGetLeadDataJson(jsonObject);
                                            }
                                        }
                                    }
                                    else showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
                                case 404:
                                    showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLogUpdateLead(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLogUpdateLead(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });
    }

    private void setGetLeadDataJson(JsonObject jsonObject) {
        // get lead stages array
        if (jsonObject.has("lead_stages"))
        {
            if (!jsonObject.get("lead_stages").isJsonNull() && jsonObject.get("lead_stages").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_stages").getAsJsonArray();
                //clear list
                leadStagesModelArrayList.clear();
                leadStageStringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setLeadStagesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }
    private void setLeadStagesJson(JsonObject jsonObject) {
        LeadStagesModel myModel = new LeadStagesModel();
        if (jsonObject.has("lead_stage_id")) myModel.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage_name")) {
            myModel.setLead_stage_name(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
            leadStageStringArrayList.add(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
        }

        leadStagesModelArrayList.add(myModel);
    }

    private void call_getSalesFeed() {
        ApiClient client = ApiClient.getInstance();
        int limit = 6;
        skip_count = call * limit;
        String other_ids=getFilterJson();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getSalesFeed(api_token, user_id, limit, call * limit, filter_text, other_ids, last_lead_updated_at);
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonObjectResponse -> {
            throw new IllegalStateException("doOnNextException");
        });
        responseObservable.doOnError(throwable -> {
            throw new UnsupportedOperationException("onError exception");
        });
        responseObservable.subscribeOn(Schedulers.io())
                .asObservable()
                .subscribe(new Subscriber<Response<JsonObject>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");

                        if (call == 1) setFeeds();
                        else setUpdateFeed();

                        //if (call ==1 ) delayRefresh();
                        //else notifyRecyclerDataChange();

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
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) setFeedJson(JsonObjectResponse.body());
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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


    private void setFeedJson(JsonObject jsonObject) {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                //increment call
                call = call + 1;
                //get feed data ary
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                //set stop api call if data ary is empty
                //stopApiCall = jsonArray.size() == 0;
                stopApiCall = jsonArray.size() == 0 || jsonArray.size()<6;
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                // cached data of 1st skip count
                if (call==1)
                {
                    //put an array into SharedPref
                    if (sharedPreferences!=null) {
                        editor = sharedPreferences.edit();
                        editor.putString("jA_allBookings", jsonArray.toString());
                        editor.apply();
                    }
                }

            } else stopApiCall = true;
            //stop api call when data ary is null
        }
    }

    private void setJson(JsonObject jsonObject) {

        FeedsModel model = new FeedsModel();

        if (jsonObject.has("feed_type_id")) model.setFeed_type_id(!jsonObject.get("feed_type_id").isJsonNull() ? jsonObject.get("feed_type_id").getAsInt() : 0);
        if (jsonObject.has("tag")) model.setTag(!jsonObject.get("tag").isJsonNull() ? jsonObject.get("tag").getAsString() : "");
        if (jsonObject.has("tag_date")) model.setTag_date(!jsonObject.get("tag_date").isJsonNull() ? jsonObject.get("tag_date").getAsString() : "");
        if (jsonObject.has("tag_elapsed_time")) model.setTag_elapsed_time(!jsonObject.get("tag_elapsed_time").isJsonNull() ? jsonObject.get("tag_elapsed_time").getAsString() : "");
        if (jsonObject.has("small_header_title")) model.setSmall_header_title(!jsonObject.get("small_header_title").isJsonNull() ? jsonObject.get("small_header_title").getAsString() : "");
        if (jsonObject.has("main_title")) model.setMain_title(!jsonObject.get("main_title").isJsonNull() ? jsonObject.get("main_title").getAsString() : "");
        if (jsonObject.has("description")) model.setDescription(!jsonObject.get("description").isJsonNull() ? jsonObject.get("description").getAsString() : "");
        if (jsonObject.has("status_text")) model.setStatus_text(!jsonObject.get("status_text").isJsonNull() ? jsonObject.get("status_text").getAsString() : "");
        if (jsonObject.has("status_sub_text")) model.setStatus_sub_text(!jsonObject.get("status_sub_text").isJsonNull() ? jsonObject.get("status_sub_text").getAsString() : "");
        if (jsonObject.has("status_timestamp")) model.setStatus_timestamp(!jsonObject.get("status_timestamp").isJsonNull() ? jsonObject.get("status_timestamp").getAsString() : "");

        //call actions
        if (jsonObject.has("actions")) {
            if (!jsonObject.get("actions").isJsonNull() && jsonObject.get("actions").isJsonObject()) {
                JsonObject object = jsonObject.get("actions").getAsJsonObject();
                if (object.has("call"))
                    model.setCall(!object.get("call").isJsonNull() ? object.get("call").getAsString() : "");
            }
        }

        if (jsonObject.has("other_info")) {
            if (!jsonObject.get("other_info").isJsonNull() && jsonObject.get("other_info").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("other_info").getAsJsonArray();
                ArrayList<LeadDetailsTitleModel> arrayList = new ArrayList<>();
                arrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setOtherInfoJson(jsonArray.get(i).getAsJsonObject(), arrayList);
                }
                model.setDetailsTitleModelArrayList(arrayList);
            }
        }

        //lead id
        if (jsonObject.has("ids"))
        {
            if (!jsonObject.get("ids").isJsonNull() && jsonObject.get("ids").isJsonObject())
            {
                JsonObject object = jsonObject.get("ids").getAsJsonObject();
                if (object.has("lead_id")) model.setLead_id(!object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0);
                //if (object.has("lead_id")) last_lead_id = !object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0;
                if (object.has("lead_status_id"))model.setLead_status_id(!object.get("lead_status_id").isJsonNull() ? object.get("lead_status_id").getAsInt() : 0);
                if (object.has("booking_id"))model.setBooking_id(!object.get("booking_id").isJsonNull() ? object.get("booking_id").getAsInt() : 0);

                CUIDModel cuidModel = new CUIDModel();
                if (object.has("lead_id"))cuidModel.setLead_id(!object.get("lead_id").isJsonNull() ? object.get("lead_id").getAsInt() : 0);
                if (object.has("updated_at"))last_lead_updated_at = !object.get("updated_at").isJsonNull() ? object.get("updated_at").getAsString() : null;
                if (object.has("lead_uid"))cuidModel.setCu_id(!object.get("lead_uid").isJsonNull() ? object.get("lead_uid").getAsString() : "");
                if (object.has("country_code"))cuidModel.setCountry_code(!object.get("country_code").isJsonNull() ? object.get("country_code").getAsString() : "");
                if (object.has("mobile_number"))cuidModel.setCustomer_mobile(!object.get("mobile_number").isJsonNull() ? object.get("mobile_number").getAsString() : "");
                if (object.has("email"))cuidModel.setCustomer_email(!object.get("email").isJsonNull() ? object.get("email").getAsString() : "");
                if (object.has("prefix"))cuidModel.setPrefix(!object.get("prefix").isJsonNull() ? object.get("prefix").getAsString() : "");
                if (object.has("first_name"))cuidModel.setFirst_name(!object.get("first_name").isJsonNull() ? object.get("first_name").getAsString() : "");
                if (object.has("middle_name"))cuidModel.setMiddle_name(!object.get("middle_name").isJsonNull() ? object.get("middle_name").getAsString() : "");
                if (object.has("last_name"))cuidModel.setLast_name(!object.get("last_name").isJsonNull() ? object.get("last_name").getAsString() : "");
                if (object.has("full_name"))cuidModel.setCustomer_name(!object.get("full_name").isJsonNull() ? object.get("full_name").getAsString() : "");
                if (object.has("is_kyc_uploaded"))cuidModel.setIs_kyc_uploaded(!object.get("is_kyc_uploaded").isJsonNull() ? object.get("is_kyc_uploaded").getAsInt() : 0);
                if (object.has("is_reminder"))cuidModel.setIs_reminder_set(!object.get("is_reminder").isJsonNull() ? object.get("is_reminder").getAsInt() : 0);
                if (object.has("call_log_count"))cuidModel.setCall_log_count(!object.get("call_log_count").isJsonNull() ? object.get("call_log_count").getAsInt() : 0);
                if (object.has("site_visit_count"))cuidModel.setSite_visit_count1(!object.get("site_visit_count").isJsonNull() ? object.get("site_visit_count").getAsInt() : 0);
                if (object.has("call_schedule_count"))cuidModel.setCall_schedule_count(!object.get("call_schedule_count").isJsonNull() ? object.get("call_schedule_count").getAsInt() : 0);

                if (object.has("lead_types_id"))cuidModel.setLead_types_id(!object.get("lead_types_id").isJsonNull() ? object.get("lead_types_id").getAsInt() : 0);
                //if (object.has("lead_types_name"))cuidModel.setLe(!object.get("lead_types_name").isJsonNull() ? object.get("lead_types_name").getAsString() :"");
                if (object.has("lead_status_id"))cuidModel.setLead_status_id(!object.get("lead_status_id").isJsonNull() ? object.get("lead_status_id").getAsInt() : 0);
                if (object.has("lead_status_name"))cuidModel.setLead_status_name(!object.get("lead_status_name").isJsonNull() ? object.get("lead_status_name").getAsString() :"");
                if (object.has("token_media_path"))cuidModel.setToken_media_path(!object.get("token_media_path").isJsonNull() ? object.get("token_media_path").getAsString() :"");
                if (object.has("token_id"))cuidModel.setToken_id(!object.get("token_id").isJsonNull() ? object.get("token_id").getAsInt() : 0 );
                if (object.has("token_no"))cuidModel.setToken_no(!object.get("token_no").isJsonNull() ? object.get("token_no").getAsString() :"");
                if (object.has("project_id"))cuidModel.setProject_id(!object.get("project_id").isJsonNull() ? object.get("project_id").getAsInt() : 0);
                if (object.has("project_name"))cuidModel.setCustomer_project_name(!object.get("project_name").isJsonNull() ? object.get("project_name").getAsString() :"");
                if (object.has("event_title"))cuidModel.setEventName(!object.get("event_title").isJsonNull() ? object.get("event_title").getAsString() :"");
                if (object.has("event_id"))cuidModel.setEvent_id(!object.get("event_id").isJsonNull() ? object.get("event_id").getAsInt() :0);
                if (object.has("token_type_id"))cuidModel.setToken_type_id(!object.get("token_type_id").isJsonNull() ? object.get("token_type_id").getAsInt() :0);
                if (object.has("token_type"))cuidModel.setToken_type(!object.get("token_type").isJsonNull() ? object.get("token_type").getAsString() :"");
                if (object.has("ghp_date"))cuidModel.setGhp_date(!object.get("ghp_date").isJsonNull() ? object.get("ghp_date").getAsString() :"");
                if (object.has("ghp_amount"))cuidModel.setGhp_amount(!object.get("ghp_amount").isJsonNull() ? object.get("ghp_amount").getAsString() :"");
                if (object.has("ghp_plus_date"))cuidModel.setGhp_plus_date(!object.get("ghp_plus_date").isJsonNull() ? object.get("ghp_plus_date").getAsString() :"");
                if (object.has("ghp_plus_amount"))cuidModel.setGhp_plus_amount(!object.get("ghp_plus_amount").isJsonNull() ? object.get("ghp_plus_amount").getAsString() :"");
                if (object.has("payment_link"))cuidModel.setPayment_link(!object.get("payment_link").isJsonNull() ? object.get("payment_link").getAsString() :"");
                if (object.has("payment_invoice_id"))cuidModel.setPayment_invoice_id(!object.get("payment_invoice_id").isJsonNull() ? object.get("payment_invoice_id").getAsString() :"");

                if (object.has("unit_hold_release_id")) model.setUnit_hold_release_id(!object.get("unit_hold_release_id").isJsonNull() ? object.get("unit_hold_release_id").getAsInt() : 0 );
                if (object.has("unit_id")) model.setUnit_id(!object.get("unit_id").isJsonNull() ? object.get("unit_id").getAsInt() : 0 );
                if (object.has("unit_name")) model.setUnit_name(!object.get("unit_name").isJsonNull() ? object.get("unit_name").getAsString() : "" );
                if (object.has("floor_id")) model.setFloor_id(!object.get("floor_id").isJsonNull() ? object.get("floor_id").getAsInt() : 0 );
                if (object.has("block_id")) model.setBlock_id(!object.get("block_id").isJsonNull() ? object.get("block_id").getAsInt() : 0 );

                if (object.has("sales_person_name")) cuidModel.setAssigned_by(!object.get("sales_person_name").isJsonNull() ? object.get("sales_person_name").getAsString() : "--" );
                if (object.has("lead_stage_id")) cuidModel.setLead_stage_id(!object.get("lead_stage_id").isJsonNull() ? object.get("lead_stage_id").getAsInt() : 0 );
                if (object.has("lead_stage_name"))cuidModel.setLead_stage_name(!object.get("lead_stage_name").isJsonNull() ? object.get("lead_stage_name").getAsString() :"");
                if (object.has("remark"))cuidModel.setGhp_remark(!object.get("remark").isJsonNull() ? object.get("remark").getAsString() :"");
                //if (object.has("remark"))remarks=!object.get("remark").isJsonNull() ? object.get("remark").getAsString() :"";

                //lead_status_id==5 && site visit count > 1
                if(cuidModel.getLead_status_id()==5 && cuidModel.getSite_visit_count()>1) {
                    model.setStatus_text("Site Revisited");
                }

                // Ghp generated and token type id 3 == ghp+
                if(cuidModel.getLead_status_id()==6 && cuidModel.getToken_type_id()==3) {
                    model.setStatus_text("GHP+ Generated");
                }

                model.setCuidModel(cuidModel);
            }
        }

        //add model
        modelArrayList.add(model);
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


    private void setCachedFeeds() {
        if (context != null) {
            context.runOnUiThread(() -> {

                // swipeRefresh.setRefreshing(false);

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addFeedData.removeAllViews();
                    for (int i = 0; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    /*   ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);*/
                    ll_addFeedData.setVisibility(View.VISIBLE);

                    //set scrollView scroll to top
                    //nsv.smoothScrollTo(0, 0);

                } else {
                    //empty feed
                    ll_noData.setVisibility(View.VISIBLE);
                    //ll_searchBar.setVisibility(View.GONE);
                    ll_addFeedData.setVisibility(View.GONE);
                }

                //hide pb when stop api call
                if (stopApiCall || modelArrayList.size()<=2) hideProgressBar();

            });
        }
    }


    private void setFeeds() {
        if (context != null) {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addFeedData.removeAllViews();
                    for (int i = 0; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    /*   ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);*/
                    ll_addFeedData.setVisibility(View.VISIBLE);

                    //set scrollView scroll to top
                    nsv.smoothScrollTo(0, 0);

                } else {
                    //empty feed
                    ll_noData.setVisibility(View.VISIBLE);
                    //ll_searchBar.setVisibility(View.GONE);
                    ll_addFeedData.setVisibility(View.GONE);
                }


                /*if (sharedPreferences!=null)
                {
                    editor = sharedPreferences.edit();
                    boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
                    editor.apply();
                    Log.e(TAG, "onCreate: applicationCreated " + applicationCreated );

                    //call methods
                    //get claim now leads only when an application is created
                    if (applicationCreated)
                    {
                        //if (isNetworkAvailable(Objects.requireNonNull(getActivity()))) call_getUnClaimedLeads();
                        //else NetworkError(getActivity());

                        //start claimNow activity
                        new Handler(getMainLooper()).postDelayed(() -> startActivity(new Intent(context, ClaimNowActivity.class)
                                .putExtra("page", "unClaimedLead")
                                .putExtra("notifyFeeds", true)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), 0);
                    }
                }*/

                //hide pb when stop api call
                if (stopApiCall || modelArrayList.size()<=2) hideProgressBar();

                //also hide pb if arrayList is having less than 2 records

            });
        }
    }

    private void setUpdateFeed() {
        if (context != null) {
            context.runOnUiThread(() -> {

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    Log.e(TAG, "setUpdateFeed: updateCount "+skip_count );
                    Log.e(TAG, "setUpdateFeed: size "+ modelArrayList.size() );

                    //ll_addFeedData.removeAllViews();
                    for (int i = skip_count; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }
                    /*   ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);*/
                    ll_addFeedData.setVisibility(View.VISIBLE);

                } else {
                    //empty feed
                    ll_noData.setVisibility(View.VISIBLE);
                    //ll_searchBar.setVisibility(View.GONE);
                    ll_addFeedData.setVisibility(View.GONE);
                }

                swipeRefresh.setRefreshing(false);
                //hide pb when stop api call
                if (stopApiCall) {
                    hideProgressBar();
                    //new Helper().showSuccessCustomToast(context, "Last page!");
                }

//                //visible back to top
//                new Handler().postDelayed(() -> {
//                    if (call > 3) {
//                        ll_backToTop.setVisibility(View.VISIBLE);
//                        new Animations().slideInBottom(ll_backToTop);
//                    }
//                }, 1500);


            });
        }
    }


    private View getFeedsView(int position) {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_home_booking, null);

        LinearLayoutCompat ll_main = rowView.findViewById(R.id.ll_itemHomeFeed_main);
        setAnimation(ll_main, position);

        //own feed views
        LinearLayoutCompat ll_own_view = rowView.findViewById(R.id.ll_homeFeed_ownView);
        LinearLayoutCompat ll_own_main = rowView.findViewById(R.id.ll_homeFeed_ownMain);
        AppCompatTextView tv_own_date = rowView.findViewById(R.id.tv_homeFeed_ownDate);
        AppCompatImageView iv_own_tagIcon = rowView.findViewById(R.id.iv_homeFeed_ownTagIcon);
        AppCompatTextView tv_own_tag = rowView.findViewById(R.id.tv_homeFeed_ownTag);
        AppCompatTextView tv_own_elapsedTime = rowView.findViewById(R.id.tv_homeFeed_ownElapsedTime);
        AppCompatTextView tv_own_cuIdNumber = rowView.findViewById(R.id.tv_homeFeed_ownCuIdNumber);
        AppCompatImageView iv_ownReminderIcon = rowView.findViewById(R.id.iv_homeFeed_ownReminderIcon);
        AppCompatTextView tv_own_Lead_name = rowView.findViewById(R.id.tv_homeFeed_ownLeadName);
        AppCompatImageView iv_editOwnLeadName = rowView.findViewById(R.id.iv_homeFeed_update_ownLeadName);
        AppCompatTextView tv_own_projectName = rowView.findViewById(R.id.tv_homeFeed_ownProjectName);
        AppCompatTextView tv_ownLeadStage = rowView.findViewById(R.id.tv_homeFeed_ownLeadStage);
        AppCompatTextView tv_ownLeadStage_dot = rowView.findViewById(R.id.tv_homeFeed_ownLeadStage_dot);
        LinearLayoutCompat ll_leadStage_dot = rowView.findViewById(R.id.ll_leadStage_dot);
        AppCompatImageView iv_ownLeadBusinessWhatsApp = rowView.findViewById(R.id.iv_homeFeed_ownLeadBusinessWhatsApp);
        AppCompatImageView iv_ownLeadWhatsApp = rowView.findViewById(R.id.iv_homeFeed_ownLeadWhatsApp);
        AppCompatImageView iv_ownLeadSms = rowView.findViewById(R.id.iv_homeFeed_ownLeadSms);
        AppCompatImageView iv_ownLeadGmail = rowView.findViewById(R.id.iv_homeFeed_ownLeadGmail);
        AppCompatImageView iv_own_Lead_call = rowView.findViewById(R.id.iv_homeFeed_ownLeadCall);
        AppCompatImageView iv_own_leadOptions = rowView.findViewById(R.id.iv_homeFeed_ownLeadOptions);
        AppCompatTextView tv_own_status = rowView.findViewById(R.id.tv_homeFeed_ownStatus);
        LinearLayoutCompat ll_leadStatus = rowView.findViewById(R.id.ll_homeFeed_leadStatus);
        // AppCompatTextView tv_own_status = rowView.findViewById(R.id.tv_homeFeed_ownStatus);
        //  AppCompatTextView tv_own_token_number = rowView.findViewById(R.id.tv_homeFeed_ownTokenNumber);
        //LinearLayoutCompat ll_own_leadDetailsMain = rowView.findViewById(R.id.ll_HomeFeed_own_leadDetailsMain);
        AppCompatImageView iv_own_leadDetails_ec = rowView.findViewById(R.id.iv_homeFeed_own_leadDetails_ec);
        LinearLayoutCompat ll_own_viewLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownViewLeadDetails);
        LinearLayoutCompat ll_own_addLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownAddLeadDetails);

        AppCompatTextView tv_siteVisit_badge = rowView.findViewById(R.id.tv_homeFeed_siteVisit_badge);
        AppCompatTextView tv_callCount_badge = rowView.findViewById(R.id.tv_homeFeed_callCount_badge);

        // Lead added by
        LinearLayoutCompat ll_lead_addedBy = rowView.findViewById(R.id.ll_lead_addedBy);
        AppCompatTextView tv_lead_AddedBy = rowView.findViewById(R.id.tv_lead_AddedBy);

        ll_lead_addedBy.setVisibility(isSalesHead ? View.VISIBLE :View.GONE);

        final FeedsModel myModel = modelArrayList.get(position);
        //if (myModel.getFeed_type_id() == 1) {}
        //Own View
        //set siteVisit badge count
        tv_siteVisit_badge.setText(String.valueOf(myModel.getCuidModel().getSite_visit_count1()));
        //set call count badge count
        tv_callCount_badge.setText(String.valueOf(myModel.getCuidModel().getCall_log_count()));

        //tag date
        tv_own_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
        //tag icon
        iv_own_tagIcon.setImageResource(R.drawable.ic_tag_general);
        //tag other_ids
        tv_own_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? "| "+myModel.getTag() : "");
        tv_own_tag.setVisibility(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? View.VISIBLE :View.GONE);
        //elapsed time
        tv_own_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
        //cu_id number
        tv_own_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty() ? myModel.getSmall_header_title() : "");
        //lead name
        tv_own_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty() ? myModel.getMain_title() : "");
        //project name
        tv_own_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty() ? myModel.getDescription() : "");
        //lead stage name
        tv_ownLeadStage.setText(myModel.getCuidModel() != null && myModel.getCuidModel().getLead_stage_name()!=null ? myModel.getCuidModel().getLead_stage_name() : "");
        tv_ownLeadStage_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
        Log.e(TAG, "getFeedsView:myModel.getCuidModel().getLead_stage_id() "+myModel.getCuidModel().getLead_stage_id() );
        ll_leadStage_dot.setVisibility(myModel.getCuidModel().getLead_stage_id()==0? View.GONE :View.VISIBLE);

        //sales person name
        tv_lead_AddedBy.setText(myModel.getCuidModel().getAssigned_by()!= null && !myModel.getCuidModel().getAssigned_by().trim().isEmpty() ? myModel.getCuidModel().getAssigned_by() : "--");

        //status
        //tv_own_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
        //token number/sub status other_ids
        //tv_own_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
        //mobile number/call
        iv_own_Lead_call.setOnClickListener(v -> {
            if (myModel.getCall()!=null) {
                //get the customer mobile number
                customer_mobile = myModel.getCall();
                //get lead id
                call_lead_id = myModel.getLead_id();
                //get lead name
                call_lead_name = myModel.getMain_title();
                // get project name
                call_project_name = myModel.getCuidModel().getCustomer_project_name();
                //get lead status id
                call_lead_status_id = myModel.getLead_status_id();
                //get cuId
                call_cuID = myModel.getSmall_header_title();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkCallPermissions()) prepareToMakePhoneCall();
                    else requestPermissionCall();
                }
                else prepareToMakePhoneCall();
                //new Helper().openPhoneDialer(Objects.requireNonNull(getActivity()), myModel.getCall());
            }else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
        });

        //whatsApp
        iv_ownLeadWhatsApp.setOnClickListener(v -> {
            if (myModel.getCall()!=null) {
                //send Message to WhatsApp Number
                sendMessageFromWhatsApp(myModel.getCall(), myModel.getMain_title());
            }
            else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
        });

        //Business whatsApp
        iv_ownLeadBusinessWhatsApp.setOnClickListener(v -> {
            if (myModel.getCall()!=null) {
                //send Message to WhatsApp Number
                sendMessageFromBusinessWhatsApp(myModel.getCall(), myModel.getMain_title());
            }
            else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
        });

        //sms
        iv_ownLeadSms.setOnClickListener(v -> {
            if (myModel.getCall()!=null) {
                //send Message to WhatsApp Number
                sendMessageFromSMSApp(myModel.getCall(), myModel.getMain_title());
            }
            else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
        });


        //GMail
        iv_ownLeadGmail.setOnClickListener(v -> {
            if (myModel.getCuidModel().getCustomer_email()!=null) {
                //send Message to WhatsApp Number
                sendMessageFromGmailApp(myModel.getCuidModel().getCustomer_email(), myModel.getMain_title());
            }
            else new Helper().showCustomToast(context, "Customer Email not added!");
        });


        switch (myModel.getCuidModel().getLead_stage_id()) {
            case 1:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorhot));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                break;
            case 2:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorwarm));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                break;
            case 3:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorcold));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                break;
            case 4:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorni));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                break;
            case 5:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                break;
            default:
                tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));

        }



        //set own popup menu's
        iv_own_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_own_leadOptions, myModel,position));

        if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0) {
            int callCount=0;

            iv_own_leadDetails_ec.setVisibility(View.VISIBLE);
            //Set Lead Details
            ll_own_addLeadDetails.removeAllViews();
            for (int i = 0; i < myModel.getDetailsTitleModelArrayList().size(); i++) {
                //Log.e("ll_HomeFeed_own_", "onBindViewHolder: "+myModel.getDetailsTitleModelArrayList().size());

                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_item_leads_title, null);
                final AppCompatTextView tv_leads_tag_details_title_text = rowView_sub.findViewById(R.id.tv_itemLeadDetails_title);
                final LinearLayoutCompat ll_addDetails = rowView_sub.findViewById(R.id.ll_itemLeadDetails_addDetails);
                tv_leads_tag_details_title_text.setText(myModel.getDetailsTitleModelArrayList().get(i).getLead_details_title());
                ll_addDetails.removeAllViews();
                ArrayList<LeadDetailsModel> detailsModelArrayList = myModel.getDetailsTitleModelArrayList().get(i).getLeadDetailsModels();
                if (detailsModelArrayList != null && detailsModelArrayList.size() > 0) {

                    for (int j = 0; j < detailsModelArrayList.size(); j++) {
                        //Log.e("ll_HomeFeed_own_", "detailsModelArrayList.get(j).getLead_details_text() "+detailsModelArrayList.get(j).getLead_details_text());
                        @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null);
                        final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                        final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);
                        final View view_visibleFor_call = rowView_subView.findViewById(R.id.view_visibleFor_call);
                        final View view_visibleFor_siteVisit = rowView_subView.findViewById(R.id.view_visibleFor_siteVisit);

                        if(detailsModelArrayList.get(j).getLead_details_text().equals("Call Time:"))
                        {
                            callCount++;
                            Log.e(TAG, "getFeedsView: callCount"+callCount );
                        }
                        tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                        tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());
                        view_visibleFor_call.setVisibility(detailsModelArrayList.get(j).getLead_details_text().equals("Remarks:")? View.VISIBLE : View.GONE);
                        view_visibleFor_siteVisit.setVisibility(detailsModelArrayList.get(j).getLead_details_text().equals("Remark:")? View.VISIBLE : View.GONE);

                        ll_addDetails.addView(rowView_subView);
                    }
                }
                ll_own_addLeadDetails.addView(rowView_sub);
            }
        } else iv_own_leadDetails_ec.setVisibility(View.GONE);


        //set expand Collapse Own
        iv_own_leadDetails_ec.setOnClickListener(view -> {

            if (myModel.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_own_leadDetails_ec, false);
                collapse(ll_own_viewLeadDetails);
                myModel.setExpandedOwnView(false);
            } else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate(iv_own_leadDetails_ec, true);
                expandSubView(ll_own_viewLeadDetails);
                myModel.setExpandedOwnView(true);
            }
        });

        ll_own_main.setOnClickListener(view -> {

            if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
            {
                if (myModel.isExpandedOwnView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(iv_own_leadDetails_ec, false);
                    collapse(ll_own_viewLeadDetails);
                    myModel.setExpandedOwnView(false);
                } else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(iv_own_leadDetails_ec, true);
                    expandSubView(ll_own_viewLeadDetails);
                    myModel.setExpandedOwnView(true);
                }
            }
        });


        //set visibility
        iv_own_leadOptions.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
        iv_ownLeadWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
        iv_ownLeadBusinessWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
        iv_own_Lead_call.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
        if (myModel.getCuidModel()!=null) iv_ownReminderIcon.setVisibility(myModel.getCuidModel().getIs_reminder_set() == 0 ? View.GONE : View.VISIBLE);


        //booked
        if (myModel.getLead_status_id() == 3) tv_own_status.setTextColor(context.getResources().getColor(R.color.color_flat_booked));
        // if (myModel.getLead_status_id() == 3) ll_leadStatus.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked_background));
        ll_leadStatus.setVisibility(myModel.getLead_status_id() == 3 ? View.VISIBLE : View.GONE);
        if (myModel.getLead_status_id() == 3) ll_own_main.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked_background));

        iv_editOwnLeadName.setOnClickListener(v -> {
            //showEditNameDialog(myModel.getCuidModel(),position,"own");
            showUpdateLeadPopUpMenu(iv_editOwnLeadName, myModel, position);
        });


        //visible view
        ll_own_view.setVisibility(View.VISIBLE);

        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        ll_main.setLayoutParams(params);


        return rowView;
    }



    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            new Animations().slideInBottom(v);
            lastPosition = p;
        }
    }


    private void sendMessageFromWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
        String company_name =  sharedPreferences.getString("company_name", "");
        String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();

        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);

        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
            //url = "https://wa.me/"+ number +"?text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "Hello "+ main_title + ", Welcome to VJ family... Thank you for your registration." : "Hello", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            //new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendMessageFromBusinessWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
        String company_name =  sharedPreferences.getString("company_name", "");
        String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();


        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);

        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
            //url = "https://wa.me/"+ number +"?text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "Hello "+ main_title + ", Welcome to VJ family... Thank you for your registration." : "Hello", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_business_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "Business WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            //new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

    }


    private void sendMessageFromSMSApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
        String company_name =  sharedPreferences.getString("company_name", "");
        String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();

        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);

        try {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address",""+number);
            smsIntent.putExtra("sms_body",""+extra_text);
            smsIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(smsIntent);
        } catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            Toast.makeText(context, "Messaging App not installed!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendMessageFromGmailApp(String email, String main_title)
    {
        Log.e(TAG, "sendMessageToGmail: "+ main_title );

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
        String company_name =  sharedPreferences.getString("company_name", "");
        String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();

        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);

        try{

            Intent intent=new Intent(Intent.ACTION_SEND);
            String[] recipients={""+email};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT,"Greetings from Leedo App");
            intent.putExtra(Intent.EXTRA_TEXT,""+extra_text);
            intent.putExtra(Intent.EXTRA_CC,"");
            intent.putExtra(Intent.EXTRA_BCC, "");
            intent.setType("text/html");
            intent.setPackage("com.google.android.gm");
            startActivity(Intent.createChooser(intent, "Send mail"));

        }catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            Toast.makeText(context, "GMail App not installed!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateLeadPopUpMenu(View view, FeedsModel myModel, int position) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadName, Menu.NONE, context.getString(R.string.update_lead_name));
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadStage, Menu.NONE, context.getString(R.string.update_lead_stage));

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId())
            {
                case R.id.menu_updateLead_updateLeadName:
                    //show update lead name alert
                    showEditNameDialog(myModel.getCuidModel(), position,"own");
                    return true;

                case R.id.menu_updateLead_updateLeadStage:
                    //show update stage alert
                    showUpdateLeadStageAlert(myModel,position);
                    return true;

                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();
    }


    private void showPopUpMenu(View view, FeedsModel myModel, int position) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_leadOption_directBooking, Menu.NONE, context.getString(R.string.direct_allotment));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_cancelBooking, Menu.NONE, context.getString(R.string.cancel_allotment_));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_continueAllotment, Menu.NONE, context.getString(R.string.continue_allotment));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_viewHoldFlat, Menu.NONE, context.getString(R.string.view_hold_flat));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_releaseFlat, Menu.NONE, context.getString(R.string.release_flat));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addSiteVisit, Menu.NONE, context.getString(R.string.add_site_visit));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addToken, Menu.NONE, context.getString(R.string.menu_generate_ghp));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_viewToken, Menu.NONE, context.getString(R.string.view_ghp));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addCallSchedule, Menu.NONE, context.getString(R.string.add_call_schedule));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addCallLog, Menu.NONE, context.getString(R.string.add_call_log));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addReminder, Menu.NONE, context.getString(R.string.add_reminder));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_callNow, Menu.NONE, context.getString(R.string.call_now));
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadName, Menu.NONE, context.getString(R.string.update_lead_name));
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLead, Menu.NONE, context.getString(R.string.update_lead));
        popupMenu.getMenu().add(1, R.id.menu_updateLead_updateLeadStage, Menu.NONE, context.getString(R.string.update_lead_stage));


        switch (myModel.getLead_status_id())
        {

            case  1:    //new lead
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadName).setVisible(false);


                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLead).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadStage).setVisible(true);
                break;
            /*------------------------------------------------------------------------------------------------------*/

            case  2:    // site visited

                //hide view GHP
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);

                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadName).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLead).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadStage).setVisible(true);


            case  3:    // booked
                //hide view GHP
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(true);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadName).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLead).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadStage).setVisible(false);

                break;



            case  4:     //booking cancelled

                //hide add token
                //TODO visible add token -- change in S.E. can generate GHP WO site visit
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadName).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLead).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadStage).setVisible(true);
                break;

            /*------------------------------------------------------------------------------------------------------*/



            default:    //def

                //hide all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible only add reminder and add call log
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadName).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_updateLead_updateLeadStage).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                break;
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId())
            {
                case R.id.menu_leadOption_callNow:
                    if (myModel.getCall() != null && !myModel.getCall().trim().isEmpty())
                        new Helper().openPhoneDialer(context, myModel.getCall());
                    return true;


                case R.id.menu_leadOption_directBooking:
    /*                if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddFlatOnHoldActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(context, "Failed to get lead details!");*/
                    return true;

                case R.id.menu_leadOption_cancelBooking:
                    //show cancel alert
                    showCancelAllotmentAlert(myModel.getMain_title(),myModel.getLead_id());
                    return true;

                case R.id.menu_leadOption_continueAllotment:
                    //show cancel alert

                    /*if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, FlatAllotmentActivity.class)
                                .putExtra("unit_hold_release_id",myModel.getUnit_hold_release_id())
                                .putExtra("unit_id", myModel.getUnit_id())
                                .putExtra("unit_name", myModel.getUnit_name())
                                .putExtra("project_name", myModel.getCuidModel().getCustomer_project_name())
                                .putExtra("project_id", myModel.getCuidModel().getProject_id())
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("block_id", myModel.getBlock_id())
                                .putExtra("floor_id", myModel.getFloor_id())
                                .putExtra("fromAddHoldFlat", true)
                                .putExtra("fromHoldList", true));
                    }
                    else new Helper().showCustomToast(context, "Failed to get lead details!");*/

                    return true;

                case R.id.menu_leadOption_viewHoldFlat:
                    //view hold flat list
                    // context.startActivity(new Intent(context, DirectHoldFlatsActivity.class));
                    return true;

                case R.id.menu_leadOption_releaseFlat:
                    //show cancel alert
                    // showReleaseHoldAlert(myModel.getMain_title(),myModel.getUnit_hold_release_id());
                    return true;

                case R.id.menu_leadOption_addCallLog:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getCuidModel().getCustomer_mobile())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("call_lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(context, "Failed to get lead details!");
                    return true;


                case R.id.menu_leadOption_addCallSchedule:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddCallScheduleActivity.class)
                                .putExtra("customer_name", myModel.getMain_title())
                                .putExtra("lead_cu_id", myModel.getCuidModel().getCustomer_mobile())
                                .putExtra("lead_id",  myModel.getLead_id())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getCuidModel().getCustomer_project_name())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("fromFeed", true));

                    } else new Helper().showCustomToast(context, "Failed to get lead details!");

                    return true;


                case R.id.menu_leadOption_addReminder:
                    context.startActivity(new Intent(context, AddReminderActivity.class)
                            .putExtra("fromOther", 3)
                            .putExtra("lead_name", myModel.getMain_title())
                            .putExtra("lead_id", myModel.getLead_id())
                    );
                    return true;

                case R.id.menu_leadOption_addSiteVisit:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddSiteVisitActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("lead_cu_id", myModel.getCuidModel().getCustomer_mobile())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(context, "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addToken:
/*                    if (myModel.getCuidModel()!=null)
                    {

                        if (myModel.getLead_status_id()==2 || myModel.getLead_status_id()==3 || myModel.getLead_status_id()==4)
                        {
                            //check for lead status id is 2, 3, 4 (claimed, assigned, added) (Site visit not added)
                            //show alert to ask for generate site visit first
                            showAddSiteVisitAlert(myModel.getMain_title(),myModel);
                        }
                        else {
                            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                    .putExtra("fromOther",2)
                                    .putExtra("cuidModel", myModel.getCuidModel())
                                    .putExtra("cu_id", myModel.getSmall_header_title())
                                    .putExtra("lead_name", myModel.getMain_title())
                                    .putExtra("project_name", myModel.getDescription())
                                    .putExtra("lead_id", myModel.getLead_id()));
                        }
                    } else new Helper().showCustomToast(context, "Failed to get lead details!");*/

                    return true;

                case R.id.menu_leadOption_viewToken:

                   /* if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                .putExtra("fromOther",3)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("lead_id", myModel.getLead_id()));
                    }else new Helper().showCustomToast(context, "Failed to get lead details!");
*/
                    return true;

                case R.id.menu_updateLead_updateLeadName:
                    //show update lead name alert
                    showEditNameDialog(myModel.getCuidModel(), position,"own");
                    return true;

                case R.id.menu_updateLead_updateLead:
                    startActivity(new Intent(context, AddNewLeadActivity.class)
                            .putExtra("isUpdateLead",true)
                            .putExtra("lead_id",myModel.getCuidModel().getLead_id())
                            .putExtra("current_lead_status_id",myModel.getLead_status_id())
                            .putExtra("salesPersonName",myModel.getCuidModel().getAssigned_by()));
                    return true;

                case R.id.menu_updateLead_updateLeadStage:
                    //show update stage alert
                    showUpdateLeadStageAlert(myModel,position);
                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();
    }


    //check call permission
    private boolean checkCallPermissions() {
        return  (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        );
    }


    //request camera permission
    private void requestPermissionCall()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CALL_PHONE)
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_PHONE_STATE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.PROCESS_OUTGOING_CALLS))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, getString(R.string.call_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, CALL_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == CALL_PERMISSION_REQUEST_CODE)  //handling camera permission
        {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //make a phone call once permission is granted
                if (customer_mobile!=null) prepareToMakePhoneCall();
                else new Helper().showCustomToast(context, "Customer Mobile Number not found!");
            }
        }
    }

    private void prepareToMakePhoneCall() {

        //start the service first
        //Objects.requireNonNull(getActivity()).startService(new Intent(context, TelephonyCallService.class));

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //new MediaProjectionManager(context).createScreenCaptureIntent();
            MediaProjectionManager projectionManager = new MediaProjectionManager(context);
            projectionManager.createScreenCaptureIntent();

            //MediaProjectionManager.createScreenCaptureIntent();
        }*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //start the startForegroundService first
            context.startForegroundService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",0)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",call_cuID)
                    .putExtra("api_token",api_token)
                    .putExtra("lead_name", call_lead_name)
                    .putExtra("lead_project_name", call_project_name)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );

        } else {
            //start the service first
            context.startService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",0)
                    .putExtra("user_id",user_id)
                    .putExtra("lead_cu_id",call_cuID)
                    .putExtra("api_token",api_token)
                    .putExtra("lead_name", call_lead_name)
                    .putExtra("lead_project_name", call_project_name)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );
        }


       /* //update into sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.putInt("call_lead_id", call_lead_id);
            editor.putInt("lead_status_id", call_lead_status_id);
            editor.putInt("call_schedule_id", 0);
            editor.putString("cu_id", call_cuID);
            editor.putBoolean("from_make_phone_Call", true);
            editor.apply();

            Log.e(TAG, "prepareToMakePhoneCall: sharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                    + "\n\t lead_status_id "+ sharedPreferences.getInt("lead_status_id", 0)
                    + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                    + "\n\t cu_id "+sharedPreferences.getString("cu_id", null)
            );
        }*/

        new Helper().showSuccessCustomToast(context, "Calling from Lead Management App...!");

        new Handler().postDelayed(() -> {
            //make a call
            new Helper().makePhoneCall(context, customer_mobile);
        }, 1500);


    }

    private void showCancelAllotmentAlert(String CustomerName, int bookings_id)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getResources().getString(R.string.cancel_flat_allotment_que));
        tv_desc.setText(getString(R.string.cancel_allotment_text, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (isNetworkAvailable(context))
            {
                showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_cancelAllotment(bookings_id);

            } else NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }


    private void call_cancelAllotment(int bookings_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lead_id", bookings_id);
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().cancelAllotment(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() ) showSuccessAlert();
                                else showErrorLogClaimLead("Server response is empty!");
                            } else showErrorLogClaimLead("Invalid response from server!");
                        }
                        else showErrorLogClaimLead("Failed to cancel the Unit Booking! Try again.");
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogClaimLead(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(context.getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }




    @SuppressLint("SetTextI18n")
    private void showEditNameDialog(CUIDModel model, int position, String ownOrOther)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View editNameDialog = inflater != null ? inflater.inflate(R.layout.layout_edit_lead_name_dialog, null) : null;
        alertDialogBuilder.setView(editNameDialog);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert editNameDialog != null;

        AutoCompleteTextView acTv_prefix_mrs = editNameDialog.findViewById(R.id.acTv_prefix_mrs);
        TextInputLayout til_editLeadName = editNameDialog.findViewById(R.id.til_leadName);
        TextInputEditText edt_editLeadName = editNameDialog.findViewById(R.id.edt_leadName);

        TextInputLayout til_editLeadMiddleName = editNameDialog.findViewById(R.id.til_leadMiddleName);
        TextInputEditText edt_editLeadMiddleName = editNameDialog.findViewById(R.id.edt_leadMiddleName);

        TextInputLayout til_editLeadLastName = editNameDialog.findViewById(R.id.til_leadLastName);
        TextInputEditText edt_editLeadLastName = editNameDialog.findViewById(R.id.edt_leadLastName);

        AppCompatButton btn_negativeButton =  editNameDialog.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  editNameDialog.findViewById(R.id.btn_custom_alert_positiveButton);

        acTv_prefix_mrs.setText(model.getPrefix());
        edt_editLeadName.setText(model.getFirst_name());
        edt_editLeadMiddleName.setText(model.getMiddle_name());
        edt_editLeadLastName.setText(model.getLast_name());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, namePrefixArrayList);
        //set def selected
        acTv_prefix_mrs.setText(model.getPrefix());
        acTv_prefix_mrs.setAdapter(adapter);
        acTv_prefix_mrs.setThreshold(0);

        btn_positiveButton.setOnClickListener(view -> {

            if(edt_editLeadName.getText().toString().trim().isEmpty()) {

                new Helper().showCustomToast(context, "Please enter first name!");
                edt_editLeadName.requestFocus();
            }else{
                alertDialog.dismiss();

                if (isNetworkAvailable(context)) {

                    model.setPrefix(acTv_prefix_mrs.getText().toString()!=null ? acTv_prefix_mrs.getText().toString():"");
                    model.setFirst_name(edt_editLeadName.getText().toString() != null ? edt_editLeadName.getText().toString(): "");
                    model.setMiddle_name(edt_editLeadMiddleName.getText().toString()!= null ? edt_editLeadMiddleName.getText().toString() : "");
                    model.setLast_name(edt_editLeadLastName.getText().toString() != null ? edt_editLeadLastName.getText().toString() : "");

                    showProgressBar();

                    post_UpdateLead(model,position,ownOrOther);
                    //showProgressBar("Adding site visit...");
                    //  call_claimNow(fromFeed);

                } else NetworkError(context);
            }
            // showSuccessPopup();


        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
        });


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_claim_popup));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
    }

    private void post_UpdateLead(CUIDModel model, int position, String ownOrOther)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",model.getLead_id());
        jsonObject.addProperty("country_code",model.getCountry_code());
        jsonObject.addProperty("prefix",model.getPrefix());
        jsonObject.addProperty("first_name", model.getFirst_name());
        jsonObject.addProperty("middle_name", model.getMiddle_name());
        jsonObject.addProperty("last_name", model.getLast_name());
     /*   jsonObject.addProperty("mobile_number", model.getCustomer_mobile());
        jsonObject.addProperty("email", model.getCustomer_email());*/

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_updateLeadDetails(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            hideProgressBar();
                            new Helper().showSuccessCustomToast(context,"Lead Name updated successfully!");
                            modelArrayList.get(position).setMain_title(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            if(ownOrOther.equals("own")){
                                AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadName);
                                textView.setText(String.format(Locale.getDefault(), "%s %s %s %s", model.getPrefix(), model.getFirst_name(), model.getMiddle_name(), model.getLast_name()));
                            }/*else if(ownOrOther.equals("other")){
                                AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_othersLeadName);
                                textView.setText(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            }*/
                            //onSuccessUpdateInfo();
                        }
                        else showErrorLogUpdateLead("Failed to update customer details! Try again.");
                    }
                }
                else {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogUpdateLead(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogUpdateLead(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLogUpdateLead(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLogUpdateLead(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogUpdateLead(getString(R.string.weak_connection));
                else showErrorLogUpdateLead(e.toString());
            }
        });
    }


    private void showUpdateLeadStageAlert(FeedsModel myModel, int pos)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_edit_lead_stage_dialog, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        MaterialTextView mTv_cuIdNumber =  alertLayout.findViewById(R.id.mTv_updateLeadStage_cuIdNumber);
        MaterialTextView mTv_leadName =  alertLayout.findViewById(R.id.mTv_updateLeadStage_leadName);
        AutoCompleteTextView acTv_leadStage =  alertLayout.findViewById(R.id.acTv_updateLeadStage_leadStage);
        MaterialButton mBtn_negativeButton =  alertLayout.findViewById(R.id.mBtn_updateLeadStage_negativeButton);
        MaterialButton mBtn_positiveButton =  alertLayout.findViewById(R.id.mBtn_updateLeadStage_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        mTv_cuIdNumber.setText(myModel.getSmall_header_title());
        mTv_leadName.setText(myModel.getMain_title());

        final int[] selectedLeadStageId = {0};
        final String[] selectedLeadStageName = {""};

        //set selected lead stage id name
        if (myModel.getCuidModel().getLead_stage_id()!=0 && leadStagesModelArrayList!=null&& leadStageStringArrayList.size()>0) {
            acTv_leadStage.setText(leadStagesModelArrayList.get(getIndexOfListForLeadStage(leadStagesModelArrayList, myModel.getCuidModel().getLead_stage_id())).getLead_stage_name());
            selectedLeadStageId[0] =  myModel.getCuidModel().getLead_stage_id();
            selectedLeadStageName[0] = leadStagesModelArrayList.get(getIndexOfListForLeadStage(leadStagesModelArrayList, myModel.getCuidModel().getLead_stage_id())).getLead_stage_name();
        }


        if (Objects.requireNonNull(leadStagesModelArrayList).size() >0 &&  leadStageStringArrayList.size()>0)
        {
            // ArrayList<LeadStagesModel> stringList2 = new ArrayList<>(leadStageStringArrayList)));
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadStageStringArrayList);
            // acTv_leadStage.setAdapter(adapter);
            //acTv_leadStage.setThreshold(0);
            //acTv_leadStage.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            adapter = new CustomerAdapter(context, leadStagesModelArrayList);
            acTv_leadStage.setAdapter(adapter);

            acTv_leadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                String itemName = adapter.getItem(position).getLead_stage_name();
                for (LeadStagesModel pojo : leadStagesModelArrayList) {
                    if (pojo.getLead_stage_name().equals(itemName)) {

                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadStageId[0] = pojo.getLead_stage_id(); // This is the correct ID
                        selectedLeadStageName[0] = pojo.getLead_stage_name();
                        //acTv_leadStage.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Status name & id " + selectedLeadStageName[0] +"\t"+ selectedLeadStageId[0]);

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

        mBtn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (isNetworkAvailable(context))
            {
                //showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_updateLeadStage(myModel.getLead_id(), selectedLeadStageId[0], pos, selectedLeadStageName[0]);

            } else NetworkError(context);
        });

        mBtn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }


    private int getIndexOfListForLeadStage(List<LeadStagesModel> list, int stage_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  IntStream.range(0, list.size())
                    .filter(i -> list.get(i).getLead_stage_id() == stage_id)
                    .findFirst().orElse(-1);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getLead_stage_id() == stage_id)
                    return i;
            return -1;
        }
    }


    private void call_updateLeadStage(int lead_id, int lead_stage_id, int pos, String selectedLeadStageName)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("lead_stage_id", lead_stage_id);
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().Post_changeLeadStage(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {

                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            showSuccessUpdateLeadStage(pos, selectedLeadStageName, lead_stage_id);
                        }
                        else showErrorLogClaimLead("Failed to update lead stage. Invalid response from server!");
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogClaimLead(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(context.getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }

    private void showSuccessUpdateLeadStage(int position, String selectedLeadStageName, int lead_stage_id) {


        context.runOnUiThread(() -> {


            //update lead stage id and lead stage name
            modelArrayList.get(position).getCuidModel().setLead_stage_id(lead_stage_id);
            modelArrayList.get(position).getCuidModel().setLead_stage_name(selectedLeadStageName);

            Log.e(TAG, "showSuccessUpdateLeadStage: myModel.getCuidModel().getLead_stage_id()"+lead_stage_id );
            //set lead stage name
            AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadStage);
            AppCompatTextView textView_dot = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadStage_dot);
            LinearLayoutCompat ll_leadStage_dot = ll_addFeedData.getChildAt(position).findViewById(R.id.ll_leadStage_dot);
            textView.setText(selectedLeadStageName);
            textView_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            ll_leadStage_dot.setVisibility(lead_stage_id==0?View.GONE :View.VISIBLE);

            switch (lead_stage_id) {
                case 1:
                    textView.setTextColor(context.getResources().getColor(R.color.colorhot));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                    break;
                case 2:
                    textView.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    break;
                case 3:
                    textView.setTextColor(context.getResources().getColor(R.color.colorcold));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                    break;
                case 4:
                    textView.setTextColor(context.getResources().getColor(R.color.colorni));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                case 5:
                    textView.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                    break;
                default:
                    textView.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }

            //show success toast
            new Helper().showSuccessCustomToast(context,"Lead stage updated successfully!");
        });
    }


    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        context.runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide
            hideCancellationProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");
            new Helper().showCustomToast(context, "Allotment cancelled successfully!!");

            //remove all view
            ll_addFeedData.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            nsv.smoothScrollTo(0, 0);
        });

    }



    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {
                //ll_pb.setVisibility(View.GONE);

                /*if (sharedPreferences!=null) {
                    //remove cached data key from sharedPref
                    editor = sharedPreferences.edit();
                    editor.remove("jA_events");
                    editor.apply();
                    eventsModelArrayList.clear();
                }*/

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();
                onErrorSnack(context, message);

                ll_addFeedData.setVisibility(View.GONE);
                /*ll_searchBar.setVisibility(View.INVISIBLE);*/

                //recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }


    private void showErrorLogClaimLead(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();

                onErrorSnack(context, message);

            });
        }
    }

    private void showErrorLogUpdateLead(final String message)
    {
        if (context!=null){

            //hide pb
            hideProgressBar();

            //ll_pb.setVisibility(View.GONE);
            onErrorSnack(context, message);
        }
    }


    private void expandSubView(final View v) {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime == 1)
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }


    private void collapse(final View v) {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

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


    private void hideCancellationProgressBar() {
        ll_pb.setVisibility(View.GONE);
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showCancellationProgressBar(String msg) {
        hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(msg);
        ll_pb.setVisibility(View.VISIBLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(context, view);
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
    public void onPause()
    {
        super.onPause();
        Log.e(TAG, "onPause: ");
        //if (broadcastReceiver!=null) context.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        onStop = true;
        Log.e(TAG, "onStop: ");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
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

}