package com.tribeappsoft.leedo.salesPerson.homeFragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.api.WebServer;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.salesPerson.adapter.CustomerAdapter;
import com.tribeappsoft.leedo.salesPerson.adapter.EventBannerSliderAdapter;
import com.tribeappsoft.leedo.admin.callLog.CallLogActivity;
import com.tribeappsoft.leedo.admin.callLog.TelephonyCallService;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.AddFlatOnHoldActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.DirectHoldFlatsActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.FlatAllotmentActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.salesPerson.models.FeedsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
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

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragmentSalesPersonHomeFeeds extends Fragment {


    @BindView(R.id.cl_fragSalesPersonHomeFeed) CoordinatorLayout parent;
    @BindView(R.id.ll_fragSalesPersonHomeFeeds_filter) LinearLayoutCompat ll_filter;
    @BindView(R.id.tv_fragSalesPersonHomeFeeds_filterTitle) AppCompatTextView tv_filterTitle;
    @BindView(R.id.iv_fragSalesPersonHomeFeeds_closeFilter) AppCompatImageView iv_closeFilter;
    @BindView(R.id.appbar_sales_person_homeFeed) AppBarLayout appbar;
    @BindView(R.id.toolbar_salesPerson_homeFeed) MaterialToolbar toolbar;
    @BindView(R.id.nsv_fragSalesPersonHomeFeed)
    StickyScrollView nsv;
    // @BindView(R.id.rv_fragSalesPersonHomeFeeds) RecyclerView recyclerView;

    @BindView(R.id.ll_fragSalesPersonHomeFeeds_addFeedData) LinearLayoutCompat ll_addFeedData;
    @BindView(R.id.ll_fragSalesPersonHomeFeeds_loadingContent) LinearLayoutCompat ll_loadingContent;
    @BindView(R.id.ll_fragSalesPersonHomeFeeds_backToTop) LinearLayoutCompat ll_backToTop;

    @BindView(R.id.ll_fragSalesPersonHomeFeeds_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_fragmentSalesPersonHomeFeeds_searchBar) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.edt_FragmentHomeFeed_search) TextInputEditText edt_search;
    @BindView(R.id.iv_FragmentHomeFeed_VoiceSearch) AppCompatImageView iv_VoiceSearch;
    @BindView(R.id.iv_FragmentHomeFeed_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.sr_fragSalesPersonHomeFeeds) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.view_fragSalesPersonHomeFeed) View view;
    @BindView(R.id.FragmentHomeFeed_imageSlider) SliderView sliderView;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<FeedsModel> modelArrayList;
    private ArrayList<LeadListModel> leadListModelArrayList;
    private ArrayList<EventsModel> eventsModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<String> namePrefixArrayList, leadStageStringArrayList;

    CustomerAdapter adapter = null;

    private int openFlag = 0,user_id = 0,call = 0, lastPosition = -1, claimPosition =0,claimAPiCount =0,
            lead_id =0, skip_count =0, call_lead_id =0, call_lead_status_id =0 ;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private String TAG = "FragmentSalesPersonHomeFeeds", api_token = "", filter_text="", other_ids ="",
            display_text ="", last_lead_updated_at = null, customer_mobile = null, call_cuID= null;
    private Dialog claimDialog;
    private boolean doubleBackToExitPressedOnce = false, stopApiCall = false,
            isClaimNow = false, onStop = false;

    public FragmentSalesPersonHomeFeeds() {
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
        View rootView = inflater.inflate(R.layout.fragment_sales_person_home_feeds, container, false);
        context = rootView.getContext();
        //setHasOptionsMenu(true);
        ButterKnife.bind(this, rootView);

        //call method to hide keyBoard
        setupUI(parent);

        try {
            rootView.setFocusableInTouchMode(true);
            rootView.requestFocus();
            rootView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        /*DrawerLayout drawer = Objects.requireNonNull(getActivity()).findViewById(R.id.student_drawer_layout);
                        if (drawer.isDrawerOpen(GravityCompat.START)) {
                            drawer.closeDrawer(GravityCompat.START);
                        }
                        else*/
                        {
                            doOnBackPressed();
                        }
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //hide pb
        hideCancellationProgressBar();

        //initialise contents
        init();

        //set up scrollView
        setUpScrollView();

        return rootView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


   /* private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new HomeFeedRecyclerAdapter(getActivity(), modelArrayList, fragmentSalesPersonHomeFeeds);
        recyclerView.setAdapter(recyclerAdapter);
        //recyclerView.setNestedScrollingEnabled(false);
    }*/

    private void init()
    {
        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        //gone toolbar
        appbar.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        ll_filter.setVisibility(View.GONE);
        //ll_searchBar.setVisibility(View.GONE);

        //get Bundle arguments
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            //fromAchievements = bundle.getBoolean("fromAchievements",false);
            openFlag = bundle.getInt("openFlag", 0);
            other_ids = bundle.getString("other_ids", "");
            display_text = bundle.getString("display_text", "");
            Log.e(TAG, "onCreateView: openFlag "+ openFlag );
            Log.e(TAG, "init: otherIds "+ other_ids);
        }

        //init arrayLists
        modelArrayList = new ArrayList<>();
        leadListModelArrayList = new ArrayList<>();
        eventsModelArrayList = new ArrayList<>();
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
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) getLeadData();

        if (openFlag ==0)
        {
            //set cached feed only for WO filtered data
            String jA_feeds = null;
            if (sharedPreferences.getString("jA_feeds", null) != null) jA_feeds = sharedPreferences.getString("jA_feeds", null);
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


        //voice input
        iv_VoiceSearch.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());
            //new Animations().clickEffect(iv_VoiceSearch);

            //set onStop false to call method in onResume
            onStop = false;
            //prompt speech input
            promptSpeechInput();

        });


        //close filter
        iv_closeFilter.setOnClickListener(v -> {

            //same action as swipeRefresh
            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                //get cachedData
                getCachedData();

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
                new Handler(getMainLooper()).postDelayed(this::call_getEventBannerList, 1000);

                //get claim now leads
                //call_getUnClaimedLeads();
            }
            else Helper.NetworkError(getActivity());
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
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

                            //call get sales feed
                            //showProgressBar();

                            call_getSalesFeed();
                            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 1000);

                        } else Helper.NetworkError(Objects.requireNonNull(getActivity()));
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


    private void getCachedData() {
        if (sharedPreferences != null) {
            editor = sharedPreferences.edit();
            editor.apply();
            String  jA_events = null;
            if (sharedPreferences.getString("jA_events", null) != null) jA_events = sharedPreferences.getString("jA_events", null);

            if (jA_events!=null){
                JsonArray jsonArray = new Gson().fromJson(jA_events, JsonArray.class);
                eventsModelArrayList.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setEventBannerJson(jsonArray.get(i).getAsJsonObject());
                }
                //set EventsSlider
                delayRefreshBanner();
            }
        }
    }

    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() ->
        {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

                //get cached data from shared pref
                getCachedData();

                //appbar.setVisibility(View.INVISIBLE);
                //toolbar.setVisibility(View.INVISIBLE);

                ll_filter.setVisibility(View.GONE);

                swipeRefresh.setRefreshing(true);

                //set refresh api
                refreshFeedApi();


                //call get EventsBannersList
                new Handler(getMainLooper()).postDelayed(this::call_getEventBannerList, 1000);

                //get claim now leads
                //call_getUnClaimedLeads();
            }
            else Helper.NetworkError(getActivity());
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

        Log.e(TAG, "onResume: from onResume");

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            if (sharedPreferences.getBoolean("feedActionAdded", false)) {

                Log.e(TAG, "onResume: feedActionAdded "+ sharedPreferences.getBoolean("feedActionAdded", false));
                //if any action taken on feed make the stop flag to false
                onStop = false;

                //update flag to false
                editor.putBoolean("feedActionAdded", false);
            }
            editor.apply();
        }


        if (openFlag == 1) {

            //leads

            //gone slider view
            sliderView.setVisibility(View.GONE);

            //Set Data For Leads Added
            tv_filterTitle.setText(String.format("%s leads", display_text));
            ll_filter.setVisibility(View.VISIBLE);

            //set search other_ids
            edt_search.setText(String.format("%s leads", display_text));
            ll_searchBar.setVisibility(View.INVISIBLE);
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

            //gone slider view
            sliderView.setVisibility(View.GONE);

            //Set Data For Site Visits
            tv_filterTitle.setText(String.format("%s site visit", display_text));
            ll_filter.setVisibility(View.VISIBLE);

            //set search other_ids
            edt_search.setText(String.format("%s site visit", display_text));
            ll_searchBar.setVisibility(View.INVISIBLE);
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

            //gone slider view
            sliderView.setVisibility(View.GONE);

            //Set Data For GHP generated
            tv_filterTitle.setText(String.format("%s ghp", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s ghp", display_text));
            ll_searchBar.setVisibility(View.INVISIBLE);
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

            //gone slider view
            sliderView.setVisibility(View.GONE);

            //Set Data For Bookings
            tv_filterTitle.setText(String.format("%s hold flats", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s hold flats", display_text));
            ll_searchBar.setVisibility(View.INVISIBLE);
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


            //gone slider view
            sliderView.setVisibility(View.GONE);

            //Set Data For Bookings
            tv_filterTitle.setText(String.format("%s allotted flats", display_text));
            ll_filter.setVisibility(View.VISIBLE);
            //set search other_ids
            edt_search.setText(String.format("%s allotted flats", display_text));
            ll_searchBar.setVisibility(View.INVISIBLE);
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
                if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {


                    //get cachedData
                    getCachedData();

                    //gone visibility
                    ll_noData.setVisibility(View.GONE);
                    ll_filter.setVisibility(View.GONE);
                    //swipeRefresh.setRefreshing(true);

                    //resume feed api
                    resumeFeedApi();

                    //set scrollView scroll to top
                    // nsv.smoothScrollTo(0, 0);


                    new Handler(getMainLooper()).postDelayed(() -> {
                        //call events api
                        //swipeRefresh.setRefreshing(true);
                        //call get EventsBannersList
                        call_getEventBannerList();

                    }, 1000);
                }
                else
                {

                    ll_noData.setVisibility(View.VISIBLE);
                    sliderView.setVisibility(View.GONE);
                    ll_searchBar.setVisibility(View.INVISIBLE);
                    ll_addFeedData.setVisibility(View.GONE);
                    ll_loadingContent.setVisibility(View.GONE);
                    Helper.NetworkError(getActivity());
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
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

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
                Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());
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
                    Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());

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
                    Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());

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
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
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
            other_ids = display_text = "";
            last_lead_updated_at = null;
            //call get sales feed api
            showProgressBar();
            call_getSalesFeed();
            //new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 0);
        } else Helper.NetworkError(getActivity());

    }


    private void resetFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
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
            other_ids = display_text = "";
            last_lead_updated_at = null;
            showProgressBar();
            //5. call get sales feed api
            call_getSalesFeed();
        }
        else Helper.NetworkError(Objects.requireNonNull(getActivity()));
    }

    private void resetFeedApiWithDelay()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);
            //Clear Search --> reset all params
            //1. clear arrayList
            modelArrayList.clear();
            //ll_addFeedData.removeAllViews();
            //2. reset page flag to 1
            call = 0;
            //3. Set search other_ids clear
            filter_text = "";
            //4. show refreshing and progress bar
            swipeRefresh.setRefreshing(true);
            last_lead_updated_at = null;

            showProgressBar();
            //5. call get sales feed api
            new Handler(getMainLooper()).postDelayed(this::call_getSalesFeed, 500);
        }
        else Helper.NetworkError(Objects.requireNonNull(getActivity()));
    }

    private void searchFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
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

        } else Helper.NetworkError(Objects.requireNonNull(getActivity()));
    }

    private void resumeFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
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

        }else Helper.NetworkError(getActivity());
    }

    private void filterFeedApi()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
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

        }else Helper.NetworkError(getActivity());
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


    private void call_getEventBannerList()
    {
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call_ = client.getApiService().getBookingEventBanners(api_token);
        call_.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call_, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonArray()) {
                                    JsonArray jsonArray = response.body().get("data").getAsJsonArray();
                                    eventsModelArrayList.clear();
                                    for (int i = 0; i < jsonArray.size(); i++) {
                                        setEventBannerJson(jsonArray.get(i).getAsJsonObject());
                                    }

                                    //put an array into SharedPref
                                    if (sharedPreferences!=null) {
                                        editor = sharedPreferences.edit();
                                        editor.putString("jA_events", jsonArray.toString());
                                        editor.apply();
                                    }

                                    //set events banner
                                    delayRefreshBanner();

                                } else showErrorLogEventBanner("Empty Data from server!");
                            } else showErrorLogEventBanner("Empty response from server!");
                        } else showErrorLogEventBanner(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else
                {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogEventBanner(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogEventBanner(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogEventBanner(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogEventBanner(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogEventBanner(getString(R.string.weak_connection));
                else showErrorLogEventBanner(e.toString());
            }
        });
    }

    private void setEventBannerJson(JsonObject asJsonObject) {
        EventsModel eventModel =new EventsModel();
        if (asJsonObject.has("event_id")) eventModel.setEvent_id(!asJsonObject.get("event_id").isJsonNull() ? asJsonObject.get("event_id").getAsInt() : 0 );
        if (asJsonObject.has("event_description"))eventModel.setEvent_description(!asJsonObject.get("event_description").isJsonNull() ? asJsonObject.get("event_description").getAsString().trim() : "");
        if (asJsonObject.has("event_title"))eventModel.setEvent_title(!asJsonObject.get("event_title").isJsonNull() ? asJsonObject.get("event_title").getAsString().trim() : "event_title");
        if (asJsonObject.has("banner_path")) eventModel.setEvent_banner_path(!asJsonObject.get("banner_path").isJsonNull() ? asJsonObject.get("banner_path").getAsString() : "" );
        eventsModelArrayList.add(eventModel);
    }


    private void delayRefreshBanner()
    {

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {


                if (eventsModelArrayList!=null && eventsModelArrayList.size()>0)
                {
                    EventBannerSliderAdapter adapter = new EventBannerSliderAdapter(context,eventsModelArrayList);
                    sliderView.setSliderAdapter(adapter);
                    sliderView.setIndicatorAnimation(IndicatorAnimations.THIN_WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    //sliderView.setAutoCycleDirection(SliderView.SCROLLBAR_POSITION_RIGHT);
                    sliderView.setIndicatorVisibility(true);
                    sliderView.setIndicatorSelectedColor(getResources().getColor(R.color.white));
                    sliderView.setIndicatorUnselectedColor(getResources().getColor(R.color.GrayLight));
                    sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
                    sliderView.startAutoCycle();

                    view.setVisibility(View.GONE);
                    sliderView.setVisibility(View.VISIBLE);
                }
                else
                {
                    view.setVisibility(View.VISIBLE);
                    sliderView.setVisibility(View.GONE);
                }

            });
        }
    }


    private void call_getSalesFeed() {
        ApiClient client = ApiClient.getInstance();
        int limit = 6;
        skip_count = call * limit;
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
                        editor.putString("jA_feeds", jsonArray.toString());
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

                if (object.has("lead_stage_id")) cuidModel.setLead_stage_id(!object.get("lead_stage_id").isJsonNull() ? object.get("lead_stage_id").getAsInt() : 0 );
                if (object.has("lead_stage"))cuidModel.setLead_stage_name(!object.get("lead_stage").isJsonNull() ? object.get("lead_stage").getAsString() :"");
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                // swipeRefresh.setRefreshing(false);

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addFeedData.removeAllViews();
                    for (int i = 0; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                if (modelArrayList != null && modelArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addFeedData.removeAllViews();
                    for (int i = 0; i < modelArrayList.size(); i++) {
                        View rowView_sub = getFeedsView(i);
                        ll_addFeedData.addView(rowView_sub);
                    }

                    ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);
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
                        new Handler(getMainLooper()).postDelayed(() -> startActivity(new Intent(getActivity(), ClaimNowActivity.class)
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

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
                    ll_searchBar.setVisibility(openFlag == 0 ? View.VISIBLE : View.INVISIBLE);
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
                    //new Helper().showSuccessCustomToast(getActivity(), "Last page!");
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
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_home_feeds, null);

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
        AppCompatImageView iv_ownLeadWhatsApp = rowView.findViewById(R.id.iv_homeFeed_ownLeadWhatsApp);
        AppCompatImageView iv_own_Lead_call = rowView.findViewById(R.id.iv_homeFeed_ownLeadCall);
        AppCompatImageView iv_own_leadOptions = rowView.findViewById(R.id.iv_homeFeed_ownLeadOptions);
        AppCompatTextView tv_own_status = rowView.findViewById(R.id.tv_homeFeed_ownStatus);
        //AppCompatTextView tv_own_token_number = rowView.findViewById(R.id.tv_homeFeed_ownTokenNumber);
        //LinearLayoutCompat ll_own_leadDetailsMain = rowView.findViewById(R.id.ll_HomeFeed_own_leadDetailsMain);
        AppCompatImageView iv_own_leadDetails_ec = rowView.findViewById(R.id.iv_homeFeed_own_leadDetails_ec);
        LinearLayoutCompat ll_own_viewLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownViewLeadDetails);
        LinearLayoutCompat ll_own_addLeadDetails = rowView.findViewById(R.id.ll_homeFeed_ownAddLeadDetails);


       /* //other feed views
        LinearLayoutCompat ll_others_main = rowView.findViewById(R.id.ll_homeFeed_othersMain);
        LinearLayoutCompat ll_others_view = rowView.findViewById(R.id.ll_homeFeed_othersView);
        AppCompatTextView tv_others_date = rowView.findViewById(R.id.tv_homeFeed_othersDate);
        AppCompatImageView iv_others_tagIcon = rowView.findViewById(R.id.iv_homeFeed_othersTagIcon);
        AppCompatTextView tv_others_tag = rowView.findViewById(R.id.tv_homeFeed_othersTag);
        AppCompatTextView tv_others_elapsedTime = rowView.findViewById(R.id.tv_homeFeed_othersElapsedTime);
        AppCompatImageView iv_othersReminderIcon = rowView.findViewById(R.id.iv_homeFeed_othersReminderIcon);
        AppCompatTextView tv_others_cuIdNumber = rowView.findViewById(R.id.tv_homeFeed_othersCuIdNumber);
        AppCompatTextView tv_others_Lead_name = rowView.findViewById(R.id.tv_homeFeed_othersLeadName);
        AppCompatTextView tv_others_projectName = rowView.findViewById(R.id.tv_homeFeed_othersProjectName);
        AppCompatTextView tv_othersLeadStage = rowView.findViewById(R.id.tv_homeFeed_othersLeadStage);
        AppCompatTextView tv_othersLeadStage_dot = rowView.findViewById(R.id.tv_homeFeed_othersLeadStage_dot);
        LinearLayoutCompat ll_othersLeadStage_dot = rowView.findViewById(R.id.ll_homeFeed_othersLeadStage_dot);
        AppCompatTextView tv_others_Lead_status = rowView.findViewById(R.id.tv_homeFeed_othersLeadStatus);
        AppCompatTextView tv_others_token_number = rowView.findViewById(R.id.tv_homeFeed_othersTokenNumber);
        AppCompatImageView iv_othersLeadWhatsApp = rowView.findViewById(R.id.iv_homeFeed_othersLeadWhatsApp);
        AppCompatImageView iv_others_Lead_call = rowView.findViewById(R.id.iv_homeFeedOthersLeadCall);
        AppCompatImageView iv_others_leadOptions = rowView.findViewById(R.id.iv_homeFeed_othersLeadOptions);
        MaterialButton mBtn_others_claimNow = rowView.findViewById(R.id.mBtn_homeFeed_othersClaimNow);
        AppCompatImageView iv_others_leadDetails_ec = rowView.findViewById(R.id.iv_homeFeed_others_leadDetails_ec);
        LinearLayoutCompat ll_others_viewLeadDetails = rowView.findViewById(R.id.ll_homeFeed_othersViewLeadDetails);
        LinearLayoutCompat ll_others_addLeadDetails = rowView.findViewById(R.id.ll_homeFeed_othersAddLeadDetails);
        // LinearLayoutCompat ll_others_leadDetailsMain = rowView.findViewById(R.id.ll_HomeFeed_Others_leadDetailsMain);
        // LinearLayoutCompat ll_others_elapsed_time = rowView.findViewById(R.id.ll_tag_others_elapsed_time);*/

        final FeedsModel myModel = modelArrayList.get(position);
        if (myModel.getFeed_type_id() == 1) {}
            //Own View

            //tag date
            tv_own_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
            //tag icon
            iv_own_tagIcon.setImageResource(R.drawable.ic_tag_general);
            //tag other_ids
            tv_own_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? myModel.getTag() : "");
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

            //status
            tv_own_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
            //token number/sub status other_ids
            //tv_own_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
            //mobile number/call
            iv_own_Lead_call.setOnClickListener(v -> {
                if (myModel.getCall()!=null) {
                    //get the customer mobile number
                    customer_mobile = myModel.getCall();
                    //get lead id
                    call_lead_id = myModel.getLead_id();
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
                }else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Customer Mobile Number not found!");
            });

            //whatsApp
            iv_ownLeadWhatsApp.setOnClickListener(v -> {
                if (myModel.getCall()!=null) {
                    //send Message to WhatsApp Number
                    sendMessageToWhatsApp(myModel.getCall(), myModel.getMain_title());
                }
                else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Customer Mobile Number not found!");
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
                default:
                    tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));


            }



            //set own popup menu's
            iv_own_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_own_leadOptions, myModel));

            if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0) {

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
                            tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                            tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());
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
            iv_own_Lead_call.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            if (myModel.getCuidModel()!=null) iv_ownReminderIcon.setVisibility(myModel.getCuidModel().getIs_reminder_set() == 0 ? View.GONE : View.VISIBLE);

            //unclaimed
            if (myModel.getLead_status_id() == 1) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_unclaimed));
            // lead claimed
            if (myModel.getLead_status_id() == 2)  tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
            // lead assigned
            if (myModel.getLead_status_id() == 3)  tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
            //self/ lead added
            if (myModel.getLead_status_id() == 4) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
            //site visited
            if (myModel.getLead_status_id() == 5) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
            //token /GHP  generated
            if (myModel.getLead_status_id() == 6)
            {
                //token /upgraded with GHP Plus
                if(myModel.getCuidModel().getToken_type_id()==3) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_plus_generated));
                else tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));

            }
            //token /GHP  cancelled
            if (myModel.getLead_status_id() == 7) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //on hold
            if (myModel.getLead_status_id() == 8) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
            //booked
            if (myModel.getLead_status_id() == 9) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));
            //booking cancelled
            if (myModel.getLead_status_id() == 10) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //ghp pending
            if (myModel.getLead_status_id() == 13) tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_ghp_plus_pending));

            iv_editOwnLeadName.setOnClickListener(v -> {
                //showEditNameDialog(myModel.getCuidModel(),position,"own");
                showUpdateLeadPopUpMenu(iv_editOwnLeadName, myModel, position);
            });


            //visible view
            ll_own_view.setVisibility(View.VISIBLE);



        /*else if (myModel.getFeed_type_id() == 2) {

            //Others View
            //tag date
            tv_others_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
            //tag icon
            iv_others_tagIcon.setImageResource(R.drawable.ic_tag_general);
            //tag other_ids
            tv_others_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty() ? myModel.getTag() : "");
            //elapsed time
            tv_others_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
            //cu_id number
            tv_others_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty() ? myModel.getSmall_header_title() : "");
            //lead name
            tv_others_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty() ? myModel.getMain_title() : "");
            //project name
            tv_others_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty() ? myModel.getDescription() : "");
            //lead stage
            tv_othersLeadStage.setText(myModel.getCuidModel() != null && myModel.getCuidModel().getLead_stage_name()!=null ? myModel.getCuidModel().getLead_stage_name() : "");
            tv_othersLeadStage_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            Log.e(TAG, "getFeedsView:myModel.getCuidModel().getLead_stage_id() "+myModel.getCuidModel().getLead_stage_id() );
            ll_othersLeadStage_dot.setVisibility(myModel.getCuidModel().getLead_stage_id()==0? View.GONE :View.VISIBLE);
            //status
            tv_others_Lead_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
            //token number/sub status other_ids
            tv_others_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
            //mobile number/call
            iv_others_Lead_call.setOnClickListener(v -> {
                if (myModel.getCall()!=null)
                {
                    new Helper().openPhoneDialer(Objects.requireNonNull(getActivity()), myModel.getCall());
                }else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Customer number not found!");
            });

            //whatsApp
            iv_othersLeadWhatsApp.setOnClickListener(v -> {
                if (myModel.getCall()!=null)
                {
                    //send Message to WhatsApp Number
                    sendMessageToWhatsApp(myModel.getCall(), myModel.getMain_title());
                }
                else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Customer number not found!");
            });


            switch (myModel.getCuidModel().getLead_stage_id()) {
                case 1:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorhot));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                    break;
                case 2:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                    break;
                case 3:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorcold));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                    break;
                case 4:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.colorni));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                    break;
                default:
                    tv_othersLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    tv_othersLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }

            //set others popup menu's
            iv_others_leadOptions.setOnClickListener(view -> showPopUpMenu(iv_others_leadOptions, myModel));


            //Set Lead Details
            if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
            {
                iv_others_leadDetails_ec.setVisibility(View.VISIBLE);
                ll_others_addLeadDetails.removeAllViews();
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

                            tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                            tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                            ll_addDetails.addView(rowView_subView);
                        }
                    }
                    ll_others_addLeadDetails.addView(rowView_sub);
                }

            } else iv_others_leadDetails_ec.setVisibility(View.GONE);


            //set expand Collapse Others
            iv_others_leadDetails_ec.setOnClickListener(view -> {

                if (myModel.isExpandedOthersView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(iv_others_leadDetails_ec, false);
                    collapse(ll_others_viewLeadDetails);
                    myModel.setExpandedOthersView(false);
                } else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(iv_others_leadDetails_ec, true);
                    expandSubView(ll_others_viewLeadDetails);
                    myModel.setExpandedOthersView(true);
                }
            });

            ll_others_main.setOnClickListener(view -> {

                if (myModel.getDetailsTitleModelArrayList() != null && myModel.getDetailsTitleModelArrayList().size() > 0)
                {
                    if (myModel.isExpandedOthersView())  //expanded
                    {
                        // //do collapse View
                        new Animations().toggleRotate(iv_others_leadDetails_ec, false);
                        collapse(ll_others_viewLeadDetails);
                        myModel.setExpandedOthersView(false);
                    } else    // collapsed
                    {
                        //do expand view
                        new Animations().toggleRotate(iv_others_leadDetails_ec, true);
                        expandSubView(ll_others_viewLeadDetails);
                        myModel.setExpandedOthersView(true);
                    }
                }
            });


            //set visibility
            iv_others_leadOptions.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_others_Lead_call.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            iv_othersLeadWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            mBtn_others_claimNow.setVisibility(myModel.getLead_status_id() ==1  ? View.VISIBLE : View.GONE);
            if (myModel.getCuidModel()!=null) iv_othersReminderIcon.setVisibility(myModel.getCuidModel().getIs_reminder_set() == 0 ? View.GONE : View.VISIBLE);

            mBtn_others_claimNow.setOnClickListener(view -> {

                lead_id=myModel.getLead_id();
                showConfirmDialog(true, position);
            });


            //unclaimed
            if (myModel.getLead_status_id() == 1) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_unclaimed));
            // lead claimed
            if (myModel.getLead_status_id() == 2)  tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
            // lead assigned
            if (myModel.getLead_status_id() == 3)  tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
            //self/ lead added
            if (myModel.getLead_status_id() == 4) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
            //site visited
            if (myModel.getLead_status_id() == 5) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
            //token /GHP  generated
            if (myModel.getLead_status_id() == 6) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));
            //token /GHP  cancelled
            if (myModel.getLead_status_id() == 7) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //on hold
            if (myModel.getLead_status_id() == 8) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
            //booked
            if (myModel.getLead_status_id() == 9) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));
            //booking cancelled
            if (myModel.getLead_status_id() == 10) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_cancelled));
            //ghp pending
            if (myModel.getLead_status_id() == 13) tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_ghp_plus_pending));



            //visible other view
            ll_others_view.setVisibility(View.VISIBLE);

        }*/

        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 30);
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


    private void sendMessageToWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );
       /* String extra_text = "Hello "+main_title+ ",\n\n\n"
                + "Greetings from Vilas Javadekar Developers. You have been successfully registered with us through one of sales manager. \n\n\n"
                + "We will always be at your service to help you book your Dream Home with VJ. Looking forward to welcome you to our VJ Parivaar soon!\n\n"
                + "Our official Website is : "+ WebServer.VJ_Website;*/

        @SuppressLint("StringFormatMatches") String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, WebServer.VJ_Website);

        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
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
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            //new Helper().showCustomToast(context, "WhatsApp not installed!");
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


    private void showPopUpMenu(View view, FeedsModel myModel) {

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


        switch (myModel.getLead_status_id())
        {

            case  1:    //unclaimed
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);
                break;
            /*------------------------------------------------------------------------------------------------------*/

            case  2:    // lead claimed

            case  3:    // lead assigned

            case  4:     //self/ lead added

                //hide add token
                //TODO visible add token -- change in S.E. can generate GHP WO site visit
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
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
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  5:     //site visited
                //hide view GHP
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
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  6:        //GHP/Token generated


            case  13:   //GHP pending

                //hide add site visit & token
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  7:     //GHP/Token cancelled

                //hide add site visit & view GHP
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
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
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  8:     //on hold

                //call & reminder  option only
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                //hidden call now because call option added in card
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);

                // visible continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(true);
                //hide direct allotment
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  9:    //booked

                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                break;

            /*------------------------------------------------------------------------------------------------------*/

            case  10:    //booked cancelled

                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);

                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                break;

            default:    //def

                //hide all
                popupMenu.getMenu().findItem(R.id.menu_leadOption_directBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_cancelBooking).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewToken).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
                // hide continue allotment & release hold
                popupMenu.getMenu().findItem(R.id.menu_leadOption_continueAllotment).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_viewHoldFlat).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_releaseFlat).setVisible(false);

                //visible only add reminder and add call log
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallSchedule).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_leadOption_addCallLog).setVisible(false);

                break;
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId())
            {
                case R.id.menu_leadOption_callNow:
                    if (myModel.getCall() != null && !myModel.getCall().trim().isEmpty())
                        new Helper().openPhoneDialer(Objects.requireNonNull(getActivity()), myModel.getCall());
                    return true;


                case R.id.menu_leadOption_directBooking:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddFlatOnHoldActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_cancelBooking:
                    //show cancel alert
                    showCancelAllotmentAlert(myModel.getMain_title(),myModel.getBooking_id());
                    return true;

                case R.id.menu_leadOption_continueAllotment:
                    //show cancel alert

                    if (myModel.getCuidModel()!=null)
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
                    else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");

                    return true;

                case R.id.menu_leadOption_viewHoldFlat:
                    //view hold flat list
                    context.startActivity(new Intent(context, DirectHoldFlatsActivity.class));
                    return true;

                case R.id.menu_leadOption_releaseFlat:
                    //show cancel alert
                    showReleaseHoldAlert(myModel.getMain_title(),myModel.getUnit_hold_release_id());
                    return true;

                case R.id.menu_leadOption_addCallLog:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, CallLogActivity.class)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("call_lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");
                    return true;


                case R.id.menu_leadOption_addCallSchedule:
                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, AddCallScheduleActivity.class)
                                .putExtra("customer_name", myModel.getMain_title())
                                .putExtra("lead_cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_id", myModel.getLead_id())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("fromFeed", true));
                    } else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");
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
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_id", myModel.getLead_id()));
                    } else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");
                    return true;

                case R.id.menu_leadOption_addToken:
                    if (myModel.getCuidModel()!=null)
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
                    } else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");

                    return true;

                case R.id.menu_leadOption_viewToken:

                    if (myModel.getCuidModel()!=null)
                    {
                        context.startActivity(new Intent(context, GenerateTokenActivity.class)
                                .putExtra("fromOther",3)
                                .putExtra("cuidModel", myModel.getCuidModel())
                                .putExtra("cu_id", myModel.getSmall_header_title())
                                .putExtra("lead_name", myModel.getMain_title())
                                .putExtra("project_name", myModel.getDescription())
                                .putExtra("lead_status_id", myModel.getLead_status_id())
                                .putExtra("lead_id", myModel.getLead_id()));
                    }else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Failed to get lead details!");

                    return true;

                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();
    }



    /*private void delayRefresh() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
            {

                hideProgressBar();
                swipeRefresh.setRefreshing(false);

                //ll_pb.setVisibility(View.GONE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new HomeFeedRecyclerAdapter(getActivity(), modelArrayList, fragmentSalesPersonHomeFeeds);
                recyclerView.setAdapter(recyclerAdapter);
                //recyclerView.setNestedScrollingEnabled(false);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    ll_searchBar.setVisibility(View.GONE);
                    //exFab.setVisibility(View.GONE);
                } else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    ll_searchBar.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });
        }

    }


    private void notifyRecyclerDataChange() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {


                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                hideProgressBar();

                if (recyclerView.getAdapter() != null) {
                    //recyclerView adapter
                    recyclerView.getAdapter().notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no VIDEOS
                        recyclerView.setVisibility(View.GONE);
                        ll_noData.setVisibility(View.VISIBLE);
                        ll_searchBar.setVisibility(View.GONE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //Registrations are available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noData.setVisibility(View.GONE);
                        ll_searchBar.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.VISIBLE);
                    }

                }

            });
        }

    }*/

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
        if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.CALL_PHONE)
                && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.PROCESS_OUTGOING_CALLS))
                && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO))
                && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(getActivity(), getString(R.string.call_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]
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
                new Helper().showCustomToast(Objects.requireNonNull(getActivity()), getString(R.string.permission_grant_success));
                //make a phone call once permission is granted
                if (customer_mobile!=null) prepareToMakePhoneCall();
                else new Helper().showCustomToast(Objects.requireNonNull(getActivity()), "Customer Mobile Number not found!");
            }
        }
    }

    private void prepareToMakePhoneCall() {

        //start the service first
        //Objects.requireNonNull(getActivity()).startService(new Intent(context, TelephonyCallService.class));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //start the startForegroundService first
            context.startForegroundService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",0)
                    .putExtra("user_id",user_id)
                    .putExtra("cu_id",call_cuID)
                    .putExtra("api_token",api_token)
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
                    .putExtra("cu_id",call_cuID)
                    .putExtra("api_token",api_token)
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


        //make a call
        new Helper().makePhoneCall(getActivity(), customer_mobile);

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
            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_cancelAllotment(bookings_id);

            } else Helper.NetworkError(getActivity());
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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

    private void showReleaseHoldAlert(String CustomerName, int unit_hold_release_id)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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

        tv_msg.setText(getResources().getString(R.string.release_flat_question));
        tv_desc.setText(getString(R.string.que_release_flat, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                showCancellationProgressBar(getString(R.string.releasing_flat));
                call_markAsReleased(unit_hold_release_id);

            } else Helper.NetworkError(getActivity());
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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

    private void showAddSiteVisitAlert(String CustomerName, FeedsModel myModel)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;


        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getResources().getString(R.string.msg_add_ghp_without_site_visit));
        tv_desc.setText(getString(R.string.que_add_ghp_without_site_visit, CustomerName));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //goto add site visit

            context.startActivity(new Intent(context, AddSiteVisitActivity.class)
                    .putExtra("cuidModel", myModel.getCuidModel())
                    .putExtra("cu_id", myModel.getSmall_header_title())
                    .putExtra("lead_name", myModel.getMain_title())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getLead_id()));
        });

        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //go to generate GHP
            context.startActivity(new Intent(context, GenerateTokenActivity.class)
                    .putExtra("fromOther",2)
                    .putExtra("cuidModel", myModel.getCuidModel())
                    .putExtra("cu_id", myModel.getSmall_header_title())
                    .putExtra("lead_name", myModel.getMain_title())
                    .putExtra("project_name", myModel.getDescription())
                    .putExtra("lead_id", myModel.getLead_id()));

        });
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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
        jsonObject.addProperty("booking_id", bookings_id);
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
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {

                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessAlert();
                                }
                                else showErrorLogClaimLead("Server response is empty!");

                            }else showErrorLogClaimLead("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLogClaimLead(msg);
                        }
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


    private void call_markAsReleased(int unit_hold_release_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("unit_hold_release_id", unit_hold_release_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().directReleaseFlat(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() ) {
                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessReleaseFlatAlert();
                                }
                                else showErrorLogClaimLead("Server response is empty!");

                            }else showErrorLogClaimLead("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLogClaimLead(msg);
                        }
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
    private void showEditNameDialog(CUIDModel model,int position,String ownOrOther)
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, namePrefixArrayList);
        //set def selected
        acTv_prefix_mrs.setText(model.getPrefix());
        acTv_prefix_mrs.setAdapter(adapter);
        acTv_prefix_mrs.setThreshold(0);

        btn_positiveButton.setOnClickListener(view -> {

            if(edt_editLeadName.getText().toString().trim().isEmpty()) {

                new Helper().showCustomToast(getActivity(), "Please enter first name!");
                edt_editLeadName.requestFocus();
            }else{
                alertDialog.dismiss();
                if(claimDialog!=null) claimDialog.dismiss();

                if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))

                {
                    model.setPrefix(acTv_prefix_mrs.getText().toString()!=null ? acTv_prefix_mrs.getText().toString():"");
                    model.setFirst_name(edt_editLeadName.getText().toString() != null ? edt_editLeadName.getText().toString(): "");
                    model.setMiddle_name(edt_editLeadMiddleName.getText().toString()!= null ? edt_editLeadMiddleName.getText().toString() : "");
                    model.setLast_name(edt_editLeadLastName.getText().toString() != null ? edt_editLeadLastName.getText().toString() : "");

                    showProgressBar();

                    post_UpdateLead(model,position,ownOrOther);
                    //showProgressBar("Adding site visit...");
                    //  call_claimNow(fromFeed);

                } else Helper.NetworkError(getActivity());
            }
            // showSuccessPopup();


        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
        });


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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

    private void post_UpdateLead(CUIDModel model,int position,String ownOrOther)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",model.getLead_id());
        jsonObject.addProperty("country_code",model.getCountry_code());
        jsonObject.addProperty("prefix",model.getPrefix());
        jsonObject.addProperty("first_name", model.getFirst_name());
        jsonObject.addProperty("middle_name", model.getMiddle_name());
        jsonObject.addProperty("last_name", model.getLast_name());
        jsonObject.addProperty("mobile_number", model.getCustomer_mobile());
        jsonObject.addProperty("email", model.getCustomer_email());

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_updateLead(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            hideProgressBar();
                            new Helper().showSuccessCustomToast(Objects.requireNonNull(getActivity()),"Lead Name updated successfully!");
                            modelArrayList.get(position).setMain_title(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
                            if(ownOrOther.equals("own")){
                                AppCompatTextView textView = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_ownLeadName);
                                textView.setText(model.getPrefix()+" "+model.getFirst_name()+" "+model.getMiddle_name()+" "+model.getLast_name());
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


        if (leadStagesModelArrayList.size() >0 &&  leadStageStringArrayList.size()>0)
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
                        Log.e(TAG, "Lead Stage name & id " + selectedLeadStageName[0] +"\t"+ selectedLeadStageId[0]);

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

        mBtn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                //showCancellationProgressBar(getString(R.string.cancelling_flat_allotment));
                call_updateLeadStage(myModel.getLead_id(), selectedLeadStageId[0], pos, selectedLeadStageName[0]);

            } else Helper.NetworkError(getActivity());
        });

        mBtn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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
        client.getApiService().Post_updateLeadStage(jsonObject).enqueue(new Callback<JsonObject>()
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


        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {


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
                    break;
                default:
                    textView.setTextColor(context.getResources().getColor(R.color.BlackLight));
                    textView_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));
            }

            //show success toast
            new Helper().showSuccessCustomToast(Objects.requireNonNull(getActivity()),"Lead stage updated successfully!");
        });
    }


    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide
            hideCancellationProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");
            new Helper().showCustomToast(getActivity(), "Allotment cancelled successfully!!");

            //remove all view
            ll_addFeedData.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            nsv.smoothScrollTo(0, 0);
        });

    }


    @SuppressLint("InflateParams")
    private void showSuccessReleaseFlatAlert()
    {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //hide
            hideCancellationProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");

            //show toast
            new Helper().showSuccessCustomToast(getActivity(), context.getString(R.string.flat_released_successfully));

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

            //remove all view
            ll_addFeedData.removeAllViews();

            //resume feed api
            resumeFeedApi();

            //set scrollView scroll to top
            nsv.smoothScrollTo(0, 0);
        });

    }

/*

    private void call_getUnClaimedLeads()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getUnClaimedLeads(api_token);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonArrayResponse -> {
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
                        Log.d(TAG, "onCompleted:");
                        delayRefreshUnClaimedLeads();
                    }
                    @Override
                    public void onError(final Throwable e)
                    {
                        try {
                            Log.e(TAG, "onError: " + e.toString());
                            if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                            else if (e instanceof IOException) showErrorLog(getString(R.string.connection_time_out));
                            else showErrorLog(e.toString());
                        }
                        catch (Throwable ex)
                        {
                            ex.printStackTrace();
                        }
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
                                            if (JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                leadListModelArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++) {
                                                    setJsonClaimedLeads(jsonArray,i);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
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
*/


    /* private void setJsonClaimedLeads(JsonArray jsonArray, int i)
     {
         JsonObject jsonObject=jsonArray.get(i).getAsJsonObject();
         LeadListModel model=new LeadListModel();
         if(jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ?jsonObject.get("lead_id").getAsInt() : 0);
         if(jsonObject.has("lead_uid")) model.setLead_cuid_number(!jsonObject.get("lead_uid").isJsonNull() ?jsonObject.get("lead_uid").getAsString() : "");
         if(jsonObject.has("unit_category")) model.setLead_unit_type(!jsonObject.get("unit_category").isJsonNull() ?jsonObject.get("unit_category").getAsString() : "");
         if(jsonObject.has("project_name")) model.setLead_project_name(!jsonObject.get("project_name").isJsonNull() ?jsonObject.get("project_name").getAsString() : "");
         if(jsonObject.has("mobile_number")) model.setLead_mobile(!jsonObject.get("mobile_number").isJsonNull() ?jsonObject.get("mobile_number").getAsString() :"");
         if(jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ?jsonObject.get("full_name").getAsString() :"");
         if(jsonObject.has("lead_types_name")) model.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ?jsonObject.get("lead_types_name").getAsString() :"");
         if(jsonObject.has("added_by")) model.setAdded_by(!jsonObject.get("added_by").isJsonNull() ?jsonObject.get("added_by").getAsString() :"");
         if(jsonObject.has("tag_date")) model.setTag_date(!jsonObject.get("tag_date").isJsonNull() ?jsonObject.get("tag_date").getAsString() :"");
         if(jsonObject.has("tag_elapsed_time")) model.setElapsed_time(!jsonObject.get("tag_elapsed_time").isJsonNull() ?jsonObject.get("tag_elapsed_time").getAsString() :"");
         leadListModelArrayList.add(model);
     }
 */
    private void delayRefreshUnClaimedLeads()
    {
        if(getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                //set applicationCreated false to avoid repeating call of getUnclaimed api
                if (sharedPreferences!=null) {
                    //update sharedPref with flag for claim now dialog
                    editor = sharedPreferences.edit();
                    editor.putBoolean("applicationCreated", false);
                    editor.apply();
                    Log.e(TAG, "delayRefreshUnClaimedLeads: setApplicationCreated false ");
                }

                //claimCount  = leadListModelArrayList.size();
                isClaimNow = leadListModelArrayList.size() > 0; // set true iff having unclaimed leads
                if (leadListModelArrayList.size() > 0) {
                    //showDialog();
                    new Handler(getMainLooper()).postDelayed(this::showDialog, 1000);
                }
                else {
                    //leadListModelArrayList.size();
                    if (isClaimNow) {
                        //check claimCount == total size

                        //refresh feeds api
                        refreshFeedApi();
                    }
                }

            });
        }
    }


    private void showDialog()
    {
        //Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
        } else {
            assert v != null;
            v.vibrate(1000);  // Vibrate method for below API Level 26
        }*/
        if (getActivity()!=null)
        {
            claimDialog = new Dialog(Objects.requireNonNull(getActivity()));
            claimDialog.setCancelable(true);
            Drawable button = getResources().getDrawable(R.drawable.claim_button_drawable, context.getTheme());
            @SuppressLint("InflateParams") View view  = getActivity().getLayoutInflater().inflate(R.layout.layout_claim_now_pop_up, null);
            claimDialog.setContentView(view);
            Objects.requireNonNull(claimDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
            ImageView close =  view.findViewById(R.id.iv_close_claimNow);
            AppCompatTextView tv_date =  view.findViewById(R.id.tv_claimNow_date);
            AppCompatTextView tv_claimTag =  view.findViewById(R.id.tv_claimNow_tag);
            //AppCompatImageView iv_tagIcon =  view.findViewById(R.id.iv_claimNow_tagIcon);
            //AppCompatTextView tv_unitType =  view.findViewById(R.id.tv_claimNow_unitType);
            AppCompatTextView tv_elapsedTime =  view.findViewById(R.id.tv_claimNow_elapsedTime);
            AppCompatTextView tv_name =  view.findViewById(R.id.tv_claimNow_projectName);
            AppCompatTextView tv_cuId =  view.findViewById(R.id.tv_claimNow_cu_id);
            AppCompatTextView tv_lead_name =  view.findViewById(R.id.tv_claimNow_lead_name);
            AppCompatTextView tv_addedBy =  view.findViewById(R.id.tv_claimNow_addedBy);
            AppCompatButton claim_btn =  view.findViewById(R.id.btn_claimNow_popup);
            // RipplePulseLayout mRipplePulseLayout = view.findViewById(R.id.rp_layout);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
            int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
            width_px = width_px -50;
            height_px=height_px-200;
            claimDialog.getWindow().setLayout(width_px,height_px);

            //mRipplePulseLayout.startRippleAnimation();
            close.setOnClickListener(view12 -> {

                //dismiss dialog and update feed
                if(claimDialog!=null) claimDialog.dismiss();

                if (sharedPreferences!=null)
                {
                    //update sharedPref with flag
                    editor = sharedPreferences.edit();
                    editor.putBoolean("applicationCreated", false);
                    editor.apply();
                }

                //reset api with delay
                resetFeedApiWithDelay();

            });

            if(leadListModelArrayList!=null)
            {
                tv_date.setText(leadListModelArrayList.get(claimPosition).getTag_date());
                //tv_unitType.setText(leadListModelArrayList.get(claimPosition).getLead_unit_type());
                tv_cuId.setText(leadListModelArrayList.get(claimPosition).getLead_cuid_number());
                tv_lead_name.setText(leadListModelArrayList.get(claimPosition).getFull_name());
                tv_name.setText(String.format("%s | %s", leadListModelArrayList.get(claimPosition).getLead_project_name(), leadListModelArrayList.get(claimPosition).getLead_unit_type()));
                tv_elapsedTime.setText(leadListModelArrayList.get(claimPosition).getElapsed_time());
                tv_claimTag.setText(leadListModelArrayList.get(claimPosition).getLead_types_name());
                tv_addedBy.setText(leadListModelArrayList.get(claimPosition).getAdded_by());
                lead_id=leadListModelArrayList.get(claimPosition).getLead_id();
            }

            claim_btn.setBackgroundDrawable(button);
            claim_btn.setOnClickListener(view1 -> showConfirmDialog(false, 0));
            claimDialog.show();
        }
    }



    @SuppressLint("SetTextI18n")
    private void showConfirmDialog(boolean fromFeed, int position)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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

        tv_msg.setText("Claim Lead?");
        tv_desc.setText("Are you sure you want to claim this lead?");
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            // showSuccessPopup();
            alertDialog.dismiss();
            if(claimDialog!=null) claimDialog.dismiss();

            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                //showProgressBar("Adding site visit...");
                call_claimNow(fromFeed, position);

            } else Helper.NetworkError(getActivity());

        });


        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            if(claimDialog!=null) claimDialog.dismiss();

            if (sharedPreferences!=null)
            {
                //update sharedPref with flag
                editor = sharedPreferences.edit();
                editor.putBoolean("applicationCreated", false);
                editor.apply();
            }

            //reset api with delay
            resetFeedApiWithDelay();

        });

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getWidth();
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


    private void call_claimNow(boolean fromFeed, int position)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("lead_id",lead_id);

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call_ = client.getApiService().addLeadClaimNow(jsonObject);
        call_.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call_, @NonNull Response<JsonObject> response)
            {
                if (response.isSuccessful())
                {
                    if (response.body() != null)
                    {
                        String success = response.body().get("success").toString();
                        if ("1".equals(success)) {

                            claimAPiCount = claimAPiCount + 1;
                            //remove index from array list
                            if (!fromFeed) leadListModelArrayList.remove(claimPosition);
                            showSuccessPopup(fromFeed, position);
                        }
                        else if ("2".equals(success)) onAlreadyClaimedLead(response.body(), fromFeed);
                        else showErrorLogClaimLead("Failed to claim lead!");
                    }
                }
                else
                {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogClaimLead(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogClaimLead(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogClaimLead(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogClaimLead(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogClaimLead(getString(R.string.weak_connection));
                else showErrorLogClaimLead(e.toString());
            }
        });
    }


    //Success Pop Up
    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessPopup(boolean fromFeed, int position)
    {

        Dialog claimSuccessDialog = new Dialog(Objects.requireNonNull(getActivity()));
        claimSuccessDialog.setCancelable(true);
        View view  = getActivity().getLayoutInflater().inflate(R.layout.layout_claim_now_success, null);
        claimSuccessDialog.setContentView(view);
        Objects.requireNonNull(claimSuccessDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_claim_popup));
        AppCompatTextView tv_congrats = view.findViewById(R.id.tv_congrats);
        AppCompatTextView tv_msg = view.findViewById(R.id.tv_msg);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        width_px = width_px -50;
        height_px=height_px-250;
        claimSuccessDialog.getWindow().setLayout(width_px,height_px);

        tv_congrats.setText("Congratulations !");
        tv_msg.setText("This is your lead");

        new Handler(getMainLooper()).postDelayed(() -> {
            claimSuccessDialog.dismiss();
            if (fromFeed)
            {
                //TODO comment edit text and refresh api with delay
                //clear editText
                //edt_search.setText("");
                //reset feed api with delay
                //resetFeedApiWithDelay();

               /* //set lead stage name
                LinearLayoutCompat ll_homeFeed_othersCard = ll_addFeedData.getChildAt(position).findViewById(R.id.ll_homeFeed_othersCard);
                ll_homeFeed_othersCard.setBackground(getResources().getDrawable(R.drawable.cardview_leftside_white));

                //hide claim now button
                MaterialButton mBtn_othersClaimNow = ll_addFeedData.getChildAt(position).findViewById(R.id.mBtn_homeFeed_othersClaimNow);
                mBtn_othersClaimNow.setVisibility(View.GONE);

                AppCompatTextView tv_others_Lead_status = ll_addFeedData.getChildAt(position).findViewById(R.id.tv_homeFeed_othersLeadStatus);
                tv_others_Lead_status.setText("Claimed");
                tv_others_Lead_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));*/

            } else delayRefreshUnClaimedLeads();
        }, 1000);

        /*claim_btn.setBackgroundDrawable(button);
        claim_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*Dialog Box*//*
                showConfirmDialog();
            }
        });*/

        claimSuccessDialog.show();
    }

    private void onAlreadyClaimedLead(JsonObject jsonObject, boolean fromFeed)
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                //handle if some one claimed lead already
                //show error log
                if (jsonObject.has("msg")) showErrorLogClaimLead(!jsonObject.get("msg").isJsonNull() ? jsonObject.get("msg").getAsString() : "Failed to claim lead!" );
                else showErrorLogClaimLead("Failed to claim lead!");

                //handle if from feed -- already claimed then call get feeds api again
                // else remove position from arrayList
                if (fromFeed)
                {
                    //reset feed api with delay
                    resetFeedApiWithDelay();
                }
                else leadListModelArrayList.remove(claimPosition);
                // remove position from arrayList

            });
        }
    }



    private void showErrorLog(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
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
                Helper.onErrorSnack(getActivity(), message);

                ll_addFeedData.setVisibility(View.GONE);
                ll_searchBar.setVisibility(View.INVISIBLE);

                sliderView.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }


    private void showErrorLogEventBanner(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();

                Helper.onErrorSnack(getActivity(), message);

                view.setVisibility(View.VISIBLE);
                sliderView.setVisibility(View.GONE);

            });
        }
    }

    private void showErrorLogClaimLead(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                //hide pb
                hideCancellationProgressBar();

                Helper.onErrorSnack(getActivity(), message);

            });
        }
    }

    private void showErrorLogUpdateLead(final String message)
    {
        if (context!=null){

            //hide pb
            hideProgressBar();

            //ll_pb.setVisibility(View.GONE);
            Helper.onErrorSnack(Objects.requireNonNull(getActivity()), message);
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
        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showCancellationProgressBar(String msg) {
        Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(msg);
        ll_pb.setVisibility(View.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(getActivity(), view);
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
        //if (broadcastReceiver!=null) getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        onStop = true;
        Log.e(TAG, "onStop: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void doOnBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if (getActivity() != null) getActivity().onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        new Helper().showCustomToast(Objects.requireNonNull(getActivity()), getResources().getString(R.string.app_exit_msg));
        new Handler(getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //viewDestroyed = true;
        Log.e(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }
}

 /*appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(verticalOffset == 0 || verticalOffset <= toolbar.getHeight() && !toolbar.getTitle().equals(mCollapsedTitle)){
                    mCollapsingToolbar.setTitle(mCollapsedTitle);
                }else if(!mToolbar.getTitle().equals(mExpandedTitle)){
                    mCollapsingToolbar.setTitle(mExpandedTitle);
                }

            }
        });*/


  /*if (openFlag == 1)
          {
          //Set Data For Leads Added

          //visible toolbar
          //BottomNavigationView mBottomNavigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.bottomNav_view_salesPerson);
          // mBottomNavigationView.setSelectedItemId(R.id.bNav_salesPerson_home);
          //mBottomNavigationView.getMenu().getItem(1).setChecked(true);

          //visible appBar & toolBar layout
          //toolbar.setTitle(getString(R.string.my_leads));
          //toolbar.setVisibility(View.VISIBLE);
          //appbar.setVisibility(View.VISIBLE);
          //sliderView.setVisibility(View.GONE);

          ll_filter.setVisibility(View.VISIBLE);

          //search for leads
          filterFeedApi("leads");
          }*/


/* private void setTempDataMySiteVisits()
    {

        tempArrayList = new ArrayList<>();
        tempArrayList.clear();

        //1st Data
        HomeFeedModel homeFeedModel = new HomeFeedModel(1,2,5,"26 Aug 2019","1 day ago","VJYOH023–0009","","","Rohan shinde","9545943354","YashOne,Wakad","2BHK","","","","","","","","Site Visit","","","","","R & L");
    // title 1  -- Site Visit
    LeadDetailsTitleModel titleModel21 = new LeadDetailsTitleModel("Site Visit Details :");
    LeadDetailsModel detailsModel21 = new LeadDetailsModel("Visit Date :","01 Sept 2019 at 01:00 pm");
    LeadDetailsModel detailsModel22 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
    ArrayList<LeadDetailsModel> detailsModelArrayList101 = new ArrayList<>();
        detailsModelArrayList101.add(detailsModel21);
                detailsModelArrayList101.add(detailsModel22);
                titleModel21.setLeadDetailsModels(detailsModelArrayList101);

                //set to lead list model
                ArrayList<LeadDetailsTitleModel> titleModelArrayList101 = new ArrayList<>();
        titleModelArrayList101.add(titleModel21);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList101);
        tempArrayList.add(homeFeedModel);


        //2nd Data
        homeFeedModel = new HomeFeedModel(1,2,5,"25 Aug 2019","2 day ago","VJYOH023–0010","","","Sachin Patil","9552945663","YashOne,Wakad","1BHK","","","","","","","","Site Visit","","","","","R & L");
        // title 1  -- Site Visit
        LeadDetailsTitleModel titleModel22 = new LeadDetailsTitleModel("Site Visit Details :");
        LeadDetailsModel detailsModel23 = new LeadDetailsModel("Visit Date :","02 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel24 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList102 = new ArrayList<>();
        detailsModelArrayList102.add(detailsModel23);
        detailsModelArrayList102.add(detailsModel24);
        titleModel22.setLeadDetailsModels(detailsModelArrayList102);

        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList102 = new ArrayList<>();
        titleModelArrayList102.add(titleModel22);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList102);
        tempArrayList.add(homeFeedModel);

        }


private void setTempDataMyTokens()
        {
        tempArrayList = new ArrayList<>();
        tempArrayList.clear();

        //1st Data
        HomeFeedModel homeFeedModel = new HomeFeedModel(1,2,6,"26 Aug 2019","1 day ago","VJYOH023–0009","VJY1HA01","","Rohan shinde","9545943354","YashOne,Wakad","2BHK","","","","","","","","Token generated","","","","","R & L");
        // title 1  -- Site Visit
        LeadDetailsTitleModel titleModel21 = new LeadDetailsTitleModel("Token Details :");
        LeadDetailsModel detailsModel21 = new LeadDetailsModel("Generation date :","05 Sept 2019 at 10:30 am");
        LeadDetailsModel detailsModel22 = new LeadDetailsModel("Token Number : ",getString(R.string.def_token_number));
        LeadDetailsModel detailsModel23 = new LeadDetailsModel("Token Type : ","Priority Token");
        ArrayList<LeadDetailsModel> detailsModelArrayList201 = new ArrayList<>();
        detailsModelArrayList201.add(detailsModel21);
        detailsModelArrayList201.add(detailsModel22);
        detailsModelArrayList201.add(detailsModel23);
        titleModel21.setLeadDetailsModels(detailsModelArrayList201);

        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList201 = new ArrayList<>();
        titleModelArrayList201.add(titleModel21);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList201);
        tempArrayList.add(homeFeedModel);


        //2nd Data
        homeFeedModel = new HomeFeedModel(1,2,6,"25 Aug 2019","2 day ago","VJYOH023–0010","VJY1HA01","","Sachin Patil","9545943354","YashOne,Wakad","2BHK","","","","","","","","Token generated","","","","","R & L");
        // title 1  -- Site Visit
        LeadDetailsTitleModel titleModel22 = new LeadDetailsTitleModel("Token Details :");
        LeadDetailsModel detailsModel24 = new LeadDetailsModel("Generation date :","04 Sept 2019 at 10:30 am");
        LeadDetailsModel detailsModel25 = new LeadDetailsModel("Token Number : ",getString(R.string.def_token_number));
        LeadDetailsModel detailsModel26 = new LeadDetailsModel("Token Type : ","Priority Token");
        ArrayList<LeadDetailsModel> detailsModelArrayList202 = new ArrayList<>();
        detailsModelArrayList202.add(detailsModel24);
        detailsModelArrayList202.add(detailsModel25);
        detailsModelArrayList202.add(detailsModel26);
        titleModel22.setLeadDetailsModels(detailsModelArrayList202);

        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList202 = new ArrayList<>();
        titleModelArrayList202.add(titleModel22);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList202);
        tempArrayList.add(homeFeedModel);

        }
private void setTempDataMyLeads()
        {
        tempArrayList = new ArrayList<>();
        tempArrayList.clear();

        //1st Data
        HomeFeedModel  homeFeedModel = new HomeFeedModel(1,2,4,"27 Aug 2019","2 hours ago","VJYOH023–0007","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Lead Added","","","","","R & L");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,4,"26 Aug 2019","1 day ago","VJYOH023–0010","","","Prashant Sawant","9595450022","YashOne,Wakad","2BHK","","","","","","","","Lead Added","","","","","R & L");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,4,"26 Aug 2019","1 day ago","VJYOH023–0011","","","Rohan Shinde","9595450022","YashOne,Wakad","2BHK","","","","","","","","Lead Added","","","","","R & L");
        tempArrayList.add(homeFeedModel);
        }

private void setTempDataMyBookings()
        {
        tempArrayList = new ArrayList<>();
        tempArrayList.clear();

        HomeFeedModel homeFeedModel = new HomeFeedModel(1,2,8,"27 Aug 2019","2 hours ago","VJYOH023–0007","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Booked","","","","","R & L");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,8,"26 Aug 2019","1 day ago","VJYOH023–0010","","","Prashant Sawant","9595450022","YashOne,Wakad","2BHK","","","","","","","","Booked","","","","","R & L");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,8,"26 Aug 2019","1 day ago","VJYOH023–0011","","","Rohan Shinde","9595450022","YashOne,Wakad","2BHK","","","","","","","","Booked","","","","","R & L");
        tempArrayList.add(homeFeedModel);

        }


private void setTempData()
        {

        // 1. Unclaimed,  2. Claimed, 3. Assigned, 4. Own(Self)  5.Site Visited, 6. Token Generated, 7. Booked, 8.Hold Flat
        //1. Direct  2.  R & L  3. cp
        tempArrayList = new ArrayList<>();
        tempArrayList.clear();
        //Static Data for HomeFeedModel
        HomeFeedModel homeFeedModel = new HomeFeedModel(1,2,4,"06 Sep 2019","Just now","VJYOH023–0007","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Lead Added","","","","","R & L");
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(2,3,1,"04 Sep 2019","2 day ago","VJ190831-0001","VJY1HA01","","Maruti Kesarkar","9545943354","YashOne,Wakad","1BHK","","","","","","","","UnClaimed","","","","","CP lead");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,6,"02 Sep 2019","4 day ago","VJYOH023–0009","VJY1HA01","","Rohan shinde","9545943354","YashOne,Wakad","2BHK","","","","","","","","Token generated","","","","","R & L");

        // title 1  -- R & L details
        LeadDetailsTitleModel titleModel = new LeadDetailsTitleModel("Reference Details :");
        LeadDetailsModel detailsModel = new LeadDetailsModel("Ref. By :","Mr. Sagar H. Patil");
        LeadDetailsModel detailsModel1 = new LeadDetailsModel("Ref. Mobile: ","9545943354");
        ArrayList<LeadDetailsModel> detailsModelArrayList = new ArrayList<>();
        detailsModelArrayList.add(detailsModel);
        detailsModelArrayList.add(detailsModel1);
        titleModel.setLeadDetailsModels(detailsModelArrayList);

        //title 2  Site Visit Details
        LeadDetailsTitleModel titleModel2 = new LeadDetailsTitleModel("Site Visit Details :");
        LeadDetailsModel detailsModel2 = new LeadDetailsModel("Visit Date :","03 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel3 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList1 = new ArrayList<>();
        detailsModelArrayList1.add(detailsModel2);
        detailsModelArrayList1.add(detailsModel3);
        titleModel2.setLeadDetailsModels(detailsModelArrayList1);



        //title 2  Site Visit Details
        LeadDetailsTitleModel titleModel3 = new LeadDetailsTitleModel("Token Details :");
        LeadDetailsModel detailsModel4 = new LeadDetailsModel("Generation date :","02 Sept 2019 at 10:30 am");
        LeadDetailsModel detailsModel5 = new LeadDetailsModel("Token Number : ",getString(R.string.def_token_number));
        LeadDetailsModel detailsModel6 = new LeadDetailsModel("Token Type : ","Priority Token");
        ArrayList<LeadDetailsModel> detailsModelArrayList2 = new ArrayList<>();
        detailsModelArrayList2.add(detailsModel4);
        detailsModelArrayList2.add(detailsModel5);
        detailsModelArrayList2.add(detailsModel6);
        titleModel3.setLeadDetailsModels(detailsModelArrayList2);



        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList = new ArrayList<>();
        titleModelArrayList.add(titleModel);
        titleModelArrayList.add(titleModel2);
        titleModelArrayList.add(titleModel3);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList);
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(1,2,4,"02 Sep 2019","4 day ago","VJYOH023–0008","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Call reminder","","","","","System generator");

        // title 1  -- Call Reminder
        LeadDetailsTitleModel titleModel21 = new LeadDetailsTitleModel("Reminder Details : Site Visit");
        LeadDetailsModel detailsModel22 = new LeadDetailsModel("Visit Date :","03 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel23 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList21 = new ArrayList<>();
        detailsModelArrayList21.add(detailsModel22);
        detailsModelArrayList21.add(detailsModel23);
        titleModel21.setLeadDetailsModels(detailsModelArrayList21);


        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList21 = new ArrayList<>();
        titleModelArrayList21.add(titleModel21);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList21);
        tempArrayList.add(homeFeedModel);

        homeFeedModel = new HomeFeedModel(2,2,3,"01 Sep 2019","5 day ago","VJ190831-0001","VJY1HA01","","Prashant sawant","9850332240","YashOne,Wakad","2BHK","","","","","","","","Lead assigned","","","","","R & L");

        // title 1  -- Call Reminder
        LeadDetailsTitleModel titleModel11 = new LeadDetailsTitleModel("Lead Details :");
        LeadDetailsModel detailsModel12 = new LeadDetailsModel("Lead Date  :","01 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel13 = new LeadDetailsModel("Lead Assign By : ","Samar patil");
        ArrayList<LeadDetailsModel> detailsModelArrayList11 = new ArrayList<>();
        detailsModelArrayList11.add(detailsModel12);
        detailsModelArrayList11.add(detailsModel13);
        titleModel11.setLeadDetailsModels(detailsModelArrayList11);


        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList11 = new ArrayList<>();
        titleModelArrayList11.add(titleModel11);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList11);
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(1,1,7,"01 Sep 2019","5 day ago","VJYOH023–0012","","","Swami Yadav","9552022525","YashOne,Wakad","A101|2BHK","","","","","","","","On hold","","","","","Direct");
        tempArrayList.add(homeFeedModel);


        delayRefresh();
        }


public void setNewTempData()
        {

        // 1. Unclaimed,  2. Claimed, 3. Assigned, 4. Own(Self)  5.Site Visited, 6. Token Generated, 7. Booked, 8.Hold Flat
        //1. Direct  2.  R & L  3. cp

        tempArrayList =new ArrayList<>();
        tempArrayList.clear();
        //Static Data for HomeFeedModel
        HomeFeedModel homeFeedModel = new HomeFeedModel(1,2,4,"06 Sep 2019","Just now","VJYOH023–0007","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Lead Added","","","","","R & L");
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(2,3,2,"04 Sep 2019","2 day ago","VJ190831-0001","VJY1HA01","","Maruti Kesarkar","9545943354","YashOne,Wakad","1BHK","","","","","","","","Claimed","","","","","CP lead");
        tempArrayList.add(homeFeedModel);
        homeFeedModel = new HomeFeedModel(1,2,6,"02 Sep 2019","4 day ago","VJYOH023–0009","VJY1HA01","","Rohan shinde","9545943354","YashOne,Wakad","2BHK","","","","","","","","Token generated","","","","","R & L");

        // title 1  -- R & L details
        LeadDetailsTitleModel titleModel = new LeadDetailsTitleModel("Reference Details :");
        LeadDetailsModel detailsModel = new LeadDetailsModel("Ref. By :","Mr. Sagar H. Patil");
        LeadDetailsModel detailsModel1 = new LeadDetailsModel("Ref. Mobile: ","9545943354");
        ArrayList<LeadDetailsModel> detailsModelArrayList = new ArrayList<>();
        detailsModelArrayList.add(detailsModel);
        detailsModelArrayList.add(detailsModel1);
        titleModel.setLeadDetailsModels(detailsModelArrayList);

        //title 2  Site Visit Details
        LeadDetailsTitleModel titleModel2 = new LeadDetailsTitleModel("Site Visit Details :");
        LeadDetailsModel detailsModel2 = new LeadDetailsModel("Visit Date :","03 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel3 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList1 = new ArrayList<>();
        detailsModelArrayList1.add(detailsModel2);
        detailsModelArrayList1.add(detailsModel3);
        titleModel2.setLeadDetailsModels(detailsModelArrayList1);



        //title 2  Site Visit Details
        LeadDetailsTitleModel titleModel3 = new LeadDetailsTitleModel("Token Details :");
        LeadDetailsModel detailsModel4 = new LeadDetailsModel("Generation date :","02 Sept 2019 at 10:30 am");
        LeadDetailsModel detailsModel5 = new LeadDetailsModel("Token Number : ",getString(R.string.def_token_number));
        LeadDetailsModel detailsModel6 = new LeadDetailsModel("Token Type : ","Priority Token");
        ArrayList<LeadDetailsModel> detailsModelArrayList2 = new ArrayList<>();
        detailsModelArrayList2.add(detailsModel4);
        detailsModelArrayList2.add(detailsModel5);
        detailsModelArrayList2.add(detailsModel6);
        titleModel3.setLeadDetailsModels(detailsModelArrayList2);



        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList = new ArrayList<>();
        titleModelArrayList.add(titleModel);
        titleModelArrayList.add(titleModel2);
        titleModelArrayList.add(titleModel3);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList);
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(1,2,4,"02 Sep 2019","4 day ago","VJYOH023–0008","","","Sachin Patil","9545943354","YashOne,Wakad","1BHK","","","","","","","","Call reminder","","","","","System generator");

        // title 1  -- Call Reminder
        LeadDetailsTitleModel titleModel21 = new LeadDetailsTitleModel("Reminder Details : Site Visit");
        LeadDetailsModel detailsModel22 = new LeadDetailsModel("Visit Date :","03 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel23 = new LeadDetailsModel("Conducted By : ","Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList21 = new ArrayList<>();
        detailsModelArrayList21.add(detailsModel22);
        detailsModelArrayList21.add(detailsModel23);
        titleModel21.setLeadDetailsModels(detailsModelArrayList21);


        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList21 = new ArrayList<>();
        titleModelArrayList21.add(titleModel21);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList21);
        tempArrayList.add(homeFeedModel);

        homeFeedModel = new HomeFeedModel(2,2,3,"01 Sep 2019","5 day ago","VJ190831-0001","VJY1HA01","","Prashant sawant","9850332240","YashOne,Wakad","2BHK","","","","","","","","Lead assigned","","","","","R & L");

        // title 1  -- Call Reminder
        LeadDetailsTitleModel titleModel11 = new LeadDetailsTitleModel("Lead Details :");
        LeadDetailsModel detailsModel12 = new LeadDetailsModel("Lead Date  :","01 Sept 2019 at 01:00 pm");
        LeadDetailsModel detailsModel13 = new LeadDetailsModel("Lead Assign By : ","Samar patil");
        ArrayList<LeadDetailsModel> detailsModelArrayList11 = new ArrayList<>();
        detailsModelArrayList11.add(detailsModel12);
        detailsModelArrayList11.add(detailsModel13);
        titleModel11.setLeadDetailsModels(detailsModelArrayList11);

        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList11 = new ArrayList<>();
        titleModelArrayList11.add(titleModel11);
        homeFeedModel.setDetailsTitleModelArrayList(titleModelArrayList11);
        tempArrayList.add(homeFeedModel);


        homeFeedModel = new HomeFeedModel(1,1,7,"01 Sep 2019","5 day ago","VJYOH023–0012","","","Swami Yadav","9552022525","YashOne,Wakad","A101|2BHK","","","","","","","","On hold","","","","","Direct");
        tempArrayList.add(homeFeedModel);

        delayRefresh();
        }

        */



   /* @SuppressLint("InflateParams")
    private void showClaimNowAlert()
    {
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        // View customView = inflater.inflate(R.layout.layout_claim_now_pop_up,null);
        Drawable button = getResources().getDrawable(R.drawable.claim_button_drawable, context.getTheme());

        LayoutInflater factory = LayoutInflater.from(context);
        View customView = factory.inflate(R.layout.layout_claim_now_pop_up, null);

        AppCompatTextView tv_cuId =  customView.findViewById(R.id.tv_claim_now_popup_cu_id);
        AppCompatTextView tv_lead_name =  customView.findViewById(R.id.tv_claim_now_popup_lead_name);
        AppCompatTextView tv_name =  customView.findViewById(R.id.tv_claim_now_popup_project_name);
        AppCompatTextView tv_date =  customView.findViewById(R.id.tv_claim_now_date);
        AppCompatButton claim_btn =  customView.findViewById(R.id.btn_claim_now_popup);
        ImageView close =  customView.findViewById(R.id.iv_close_claim);

        //claim_btn.setBackgroundColor(claim_btn.getContext().getResources().getColor(R.color.red));

        //claim_btn.setBackgroundColor(context.getColorStateList(context, R.color.colorPrimary));

        close.setOnClickListener(view -> mPopupWindow.dismiss());

        tv_date.setText(R.string.date_claim_text);
        tv_cuId.setText(R.string.claim_cuid_popup);
        tv_lead_name.setText(R.string.claim_popup_name);
        tv_name.setText(R.string.claim_popup_projectname);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        //int pixeldpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        //int width_dp = (width_px/pixeldpi)*160;
        //int height_dp = (height_px/pixeldpi)*160;

        *//*mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );*//*
 *//* mPopupWindow = new PopupWindow(
                customView,
                (int) width_dp -10,
                (int) height_dp-10
        );*//*

        width_px = width_px -50;
        height_px=height_px-300;
        mPopupWindow = new PopupWindow();

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        //popup should wrap content view
        //mPopupWindow.setWindowLayoutMode(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(height_px);
        mPopupWindow.setWidth(width_px);

        mPopupWindow.setContentView(customView);
        new Handler().postDelayed(() -> mPopupWindow.showAtLocation(recyclerView, Gravity.CENTER,0,0), 1000);

        claim_btn.setBackgroundDrawable(button);

        claim_btn.setOnClickListener(view -> {

            *//*Dialog Box*//*
            showConfirmDialog();

        });

        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();

    }



    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Claim Lead ?");
        builder.setMessage("Are you sure you want to claim this lead ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert v != null;
                v.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
            } else {
                assert v != null;
                v.vibrate(2000);  // Vibrate method for below API Level 26
            }
            showSuccessPopup();
            dialog.cancel();
            mPopupWindow.dismiss();

        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
            mPopupWindow.dismiss();
        });

        builder.show();
    }





    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessPopup()
    {
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        // View customView = inflater.inflate(R.layout.layout_claim_now_pop_up,null);

        LayoutInflater factory = LayoutInflater.from(context);
        View customView = factory.inflate(R.layout.layout_claim_now_success, null);

        AppCompatTextView tv_congrats = customView.findViewById(R.id.tv_congrats);
        AppCompatTextView tv_msg = customView.findViewById(R.id.tv_msg);

        tv_congrats.setText("Congratulations !");
        tv_msg.setText("This is your lead");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height_px =Resources.getSystem().getDisplayMetrics().heightPixels;
        // int pixeldpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        //int width_dp = (width_px/pixeldpi)*160;
        //int height_dp = (height_px/pixeldpi)*160;

        *//*mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );*//*
 *//* mPopupWindow = new PopupWindow(
                customView,
                (int) width_dp -10,
                (int) height_dp-10
        );*//*


        width_px = width_px -50;
        height_px=height_px-300;
        mPopupWindowSuccess = new PopupWindow();

        mPopupWindowSuccess.setOutsideTouchable(true);
        mPopupWindowSuccess.setFocusable(true);

        //popup should wrap content view
        //mPopupWindow.setWindowLayoutMode(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindowSuccess.setHeight(height_px);
        mPopupWindowSuccess.setWidth(width_px);

        mPopupWindowSuccess.setContentView(customView);
        mPopupWindowSuccess.showAtLocation(recyclerView, Gravity.CENTER,0,0);
        new Handler().postDelayed(() -> mPopupWindowSuccess.dismiss(), 2000);
    }*/