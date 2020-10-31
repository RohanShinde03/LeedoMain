package com.tribeappsoft.leedo.admin.callSchedule;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.callSchedule.fragments.FragmentCompletedCalls;
import com.tribeappsoft.leedo.admin.callSchedule.fragments.FragmentScheduledCalls;
import com.tribeappsoft.leedo.admin.callSchedule.model.CallScheduleLogsModel;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.compactCalender.Event;
import com.tribeappsoft.leedo.util.compactCalender.view.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.getCurDateForToDo;
import static com.tribeappsoft.leedo.util.Helper.getCurDayForToDo;
import static com.tribeappsoft.leedo.util.Helper.getCurMonthForToDo;
import static com.tribeappsoft.leedo.util.Helper.getFormatDateForToDo;
import static com.tribeappsoft.leedo.util.Helper.getLongDateFromString;
import static com.tribeappsoft.leedo.util.Helper.getNextDate;
import static com.tribeappsoft.leedo.util.Helper.getPrevDate;
import static com.tribeappsoft.leedo.util.Helper.getSendFormatDateForToDo;
import static com.tribeappsoft.leedo.util.Helper.getStringDateFromDate;
import static com.tribeappsoft.leedo.util.Helper.getTodaysDateString;
import static com.tribeappsoft.leedo.util.Helper.getTodaysDateStringToDo;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;

public class CallScheduleMainActivity extends AppCompatActivity {

    @BindView(R.id.mBtn_callScheduleMain_prevDate) MaterialButton mBtn_prevDate;
    @BindView(R.id.ll_callScheduleMain_curDate) LinearLayoutCompat ll_curDate;
    @BindView(R.id.mTv_callScheduleMain_todayDay) MaterialTextView mTv_todayDay;
    @BindView(R.id.mTv_callScheduleMain_todayDate) MaterialTextView mTv_todayDate;
    @BindView(R.id.mTv_callScheduleMain_todayMonth) MaterialTextView mTv_todayMonth;
    @BindView(R.id.mBtn_callScheduleMain_nextDate) MaterialButton mBtn_nextDate;
    @BindView(R.id.mTB_callScheduleMain) Toolbar mTB_callScheduleMain;
    @BindView(R.id.tabLayout_callScheduleMain) TabLayout mTabLayout;
    @BindView(R.id.viewPager_callScheduleMain) ViewPager mViewPager;
    @BindView(R.id.fab_callScheduleMain_add) FloatingActionButton fab_add;

    private Activity context;
    private ArrayList<CallScheduleLogsModel> itemArrayList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id = 0, scheduledCount = 0, completedCount = 0;
    private String TAG = "CallScheduleMainActivity",api_token = "";
    private boolean notify = false;
    private Calendar currentCalender;
    private SimpleDateFormat dateFormatForDisplaying, dateFormatForMonth;
    SectionsPagerAdapter adapter;
    //private onTabChangeInterface onTabChangeInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_schedule_main);
        //  overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = CallScheduleMainActivity.this;

        // custom toolbar settings
        setSupportActionBar(mTB_callScheduleMain);

        if (getIntent()!=null) {
            //showLeads = getIntent().getBooleanExtra("showLeads", false);
            notify = getIntent().getBooleanExtra("notify", false);
        }

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setTitle(R.string.menu_all_call_logs);
        }

        new Helper().setStatusBarColor(context,R.color.colorPrimaryDark);
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

       /* onTabChangeInterface = () -> {
            Log.e(TAG, "onCreate: init Interface");
        };*/
        itemArrayList = new ArrayList<>();

        // Compact Calendar
        currentCalender = Calendar.getInstance(Locale.getDefault());
        dateFormatForDisplaying = new SimpleDateFormat("dddd-MMMM-yyyy hh:mm:ss a", Locale.getDefault());
        dateFormatForMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());


        //insert current date
        insertCurDate(getTodaysDateStringToDo());//yyyy-MMMM-dd

        //set date to UI
        setDateToUI(getFormatDateForToDo(getTodaysDateStringToDo()));

        setupViewPager(getFormatDateForToDo(getTodaysDateStringToDo()));

        mTabLayout.setupWithViewPager(mViewPager);

        //def call api
        if (isNetworkAvailable(context)) {
            //call get total count
            call_getCallScheduleLogCount();

            //get date wise count
            call_getCallLogCount(getCurDate(), false);
        }
        else {
            //network error
            NetworkError(context);

            //set up view pager for each Tabs
            //setupViewPager(getFormatDateForToDo(getTodaysDateStringToDo()));

            // Set Tabs inside Toolbar
            // mTabLayout.setupWithViewPager(mViewPager);

        }


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mViewPager.setCurrentItem(tab.getPosition());
                /*switch (tab.getPosition()) {
                    case 0:
                        // TODO
                        break;
                }*/
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });



        //add new call schedule
        fab_add.setOnClickListener(view -> startActivity(new Intent(context, CustomerIdActivity.class)
                .putExtra("fromSiteVisit_or_token", 4)
                .putExtra("forId", 3)));



        mBtn_prevDate.setOnClickListener(v -> {
            //anim.clickEffect(ib_prevDate);

            //first get the current date
            String setDate = getFormatDateForToDo(getPrevDate(getCurDate()));

            //second insert current date as an updated/changed prev date
            insertCurDate(getPrevDate(getCurDate()));

            //slide in left
            // Animation animation = AnimationUtils.loadAnimation(context, R.anim.trans_left_in);
            //animation.setDuration(500);
            //ll_curDate.setAnimation(animation);
            //ll_curDate.animate();
            //animation.start();

            //set date to UI
            setDateToUI(setDate);

            new Handler().postDelayed(() -> {
                if (isNetworkAvailable(context)) call_getCallLogCount(getCurDate(), true);
                else {

                    NetworkError(context);
                    //set up view pager
                    //setupViewPager(setDate);
                    // Set Tabs inside Toolbar
                    // mTabLayout.setupWithViewPager(mViewPager);
                }
            }, 500);





            //new Helper().showCustomToast(getActivity(), getFormatDateForToDo(getPrevDate(getTodaysDateStringToDo())));
            //new Helper().showCustomToast(getActivity(), setDate);

        });

        mBtn_nextDate.setOnClickListener(v -> {
            //anim.clickEffect(ib_nextDate);

            //first get current date
            String setDate = getFormatDateForToDo(getNextDate(getCurDate()));

            //second insert current date as an updated/changed next date
            insertCurDate(getNextDate(getCurDate()));

            //Animation animation = AnimationUtils.loadAnimation(context, R.anim.trans_right_in);
            //animation.setDuration(500);
            //ll_curDate.setAnimation(animation);
            //ll_curDate.animate();
            //animation.start();

            //set date to UI
            setDateToUI(setDate);

            new Handler().postDelayed(() -> {
                if (isNetworkAvailable(context)) call_getCallLogCount(getCurDate(), true);
                else {
                    NetworkError(context);

                    //set up view pager
                    // setupViewPager(setDate);
                    // Set Tabs inside Toolbar
                    //  mTabLayout.setupWithViewPager(mViewPager);
                }
            }, 500);


            //new Helper().showCustomToast(getActivity(), getFormatDateForToDo(getNextDate(getTodaysDateStringToDo())));
            //new Helper().showCustomToast(getActivity(), setDate);

        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: ");

        int callScheduled;
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        callScheduled = sharedPreferences.getInt("callScheduled",0);

        int tabAt = sharedPreferences.getInt("tabAt",0);
        if (sharedPreferences.getBoolean("isFilter", false)) {

            Log.e(TAG, "onResume: isFilter "+ sharedPreferences.getBoolean("isFilter", false));
            Objects.requireNonNull(mTabLayout.getTabAt(tabAt)).select();
        }

        editor.apply();
        if(callScheduled == 1){
            editor.remove("callScheduled");
            editor.apply();
            if (isNetworkAvailable(context)) {
                //call get total count
                call_getCallScheduleLogCount();
            }
            else {
                //network error
                NetworkError(context);
            }
        }


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //do stuff here
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });




       /* if (sharedPreferences!=null) {

            editor = sharedPreferences.edit();
            boolean isSetDateToUI = sharedPreferences.getBoolean("isSetDateToUI", false);
            editor.apply();

            Log.e(TAG, "onResume: isSetDateToUI "+isSetDateToUI);

            if (isSetDateToUI) {
                //get the date from sharedPref
                String strDate = sharedPreferences.getString("setDateToUI",getFormatDateForToDo(getTodaysDateStringToDo()));
                //set date to UI
                setDateToUI(strDate);
                //remove date from sharedPref
                editor = sharedPreferences.edit();
                editor.remove("isSetDateToUI");
                editor.remove("setDateToUI");
                editor.apply();
            }
        }*/
    }


    public void onSetTabsViewPager(String date, int scheduledCount, int completedCount, int tabAt, boolean visible)
    {
        // Setting ViewPager for each Tabs
        //setupViewPager(date, scheduledCount, completedCount);

        // Set Tabs inside Toolbar
        //mTabLayout.setupWithViewPager(mViewPager);

        Log.e(TAG, "onSetTabsViewPager: date "+date);
        Objects.requireNonNull(mTabLayout.getTabAt(tabAt)).select();

        BadgeDrawable badge_scheduled = Objects.requireNonNull(mTabLayout.getTabAt(0)).getOrCreateBadge();
        badge_scheduled.setVisible(visible);
        // Optionally show a number.
        badge_scheduled.setNumber(scheduledCount);
        badge_scheduled.setBadgeTextColor(getResources().getColor(R.color.main_white));


        BadgeDrawable badge_completed = Objects.requireNonNull(mTabLayout.getTabAt(1)).getOrCreateBadge();
        badge_completed.setVisible(visible);
        // Optionally show a number.
        badge_completed.setNumber(completedCount);
        badge_completed.setBadgeTextColor(getResources().getColor(R.color.main_white));
    }


    // Add Fragments to Tabs
    private void setupViewPager(String serviceDate)
    {
        Fragment frag_scheduledCalls = new FragmentScheduledCalls();
        Bundle bundle = new Bundle();
        bundle.putString("scheduledDate", serviceDate);
        //bundle.putSerializable("RoundModel_1", roundModel_1ArrayList);
        frag_scheduledCalls.setArguments(bundle);


        Fragment frag_completedCalls = new FragmentCompletedCalls();
        Bundle bundle1 = new Bundle();
        bundle1.putString("scheduledDate", serviceDate);
        //bundle2.putSerializable("RoundModel_2", roundModel_2ArrayList);
        frag_completedCalls.setArguments(bundle1);

        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(frag_scheduledCalls, scheduledCount!=0 ? "Scheduled ("+scheduledCount+")" : "Scheduled");
        //adapter.addFragment(frag_completedCalls, completedCount!=0 ? "Completed ("+completedCount+")" : "Completed");
        adapter.addFragment(frag_scheduledCalls,  "Scheduled");
        adapter.addFragment(frag_completedCalls,  "Completed");
        mViewPager.setAdapter(adapter);

        /*mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //adapter.getItem(position).listUpdated();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {

            }
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            public void onPageSelected(int position) {
                //myInterface.callMethod();
                Log.e(TAG, "onPageSelected: pos "+position);

                Fragment fragment = adapter.getItem(position);
                switch (position)
                {
                    case  0:
                        ((FragmentScheduledCalls) fragment).onResume();
                        break;

                    case  1:
                        ((FragmentCompletedCalls) fragment).onResume();
                        break;
                }

                //Fragment fragment_reg = adapter.getRegisteredFragment(position);

                //TabLayout tabLayout = (TabLayout)findViewById(R.id.tabL);
                //Objects.requireNonNull(mTabLayout.getTabAt(position)).select();
                //onTabChangeInterface.callOnTabChangedMethod();
            }
        });


    }


    public static class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private void call_getCallLogCount(String serviceDate, boolean isCallToResumeMethod)
    {
        String todoDate = getSendFormatDateForToDo(serviceDate);
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getCallLogCounts(api_token, user_id, todoDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {

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
                        }
                    }
                }

                // set up view pager and tabLayout
                setTabCountViewPager(serviceDate, isCallToResumeMethod);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());

                // set up view pager and tabLayout
                setTabCountViewPager(serviceDate, isCallToResumeMethod);
            }
        });
    }


    private void setTabCountViewPager(String serviceDate, boolean isCallToResumeMethod)
    {
        runOnUiThread(() -> {

            // Setting ViewPager for each Tabs
            //setupViewPager(serviceDate);
            // Set Tabs inside Toolbar
            // mTabLayout.setupWithViewPager(mViewPager);

            if (isCallToResumeMethod) {
                FragmentScheduledCalls.getInstance().onResume();
                FragmentCompletedCalls.getInstance().onResume();
            }

            //set up view pager for each Tabs
            //setupViewPager(getFormatDateForToDo(getTodaysDateStringToDo()), scheduledCount, completedCount);

            // Set Tabs inside Toolbar
            // mTabLayout.setupWithViewPager(mViewPager);

            //set tabs badgeCount
            onSetTabsViewPager(serviceDate, scheduledCount, completedCount, 0, true);
        });
    }


    private void call_getCallScheduleLogCount()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getCallLogCountsMonthWise(api_token, user_id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {

                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonArray()) {
                                    JsonArray  jsonArray = response.body().get("data").getAsJsonArray();
                                    itemArrayList.clear();
                                    for (int i =0; i<jsonArray.size(); i++) {
                                        setJson(jsonArray.get(i).getAsJsonObject());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
            }
        });
    }

    private void setJson(JsonObject jsonObject) {
        CallScheduleLogsModel model = new CallScheduleLogsModel();
        if (jsonObject.has("call_schedule_date")) model.setCall_schedule_date(!jsonObject.get("call_schedule_date").isJsonNull() ? jsonObject.get("call_schedule_date").getAsString() : getTodaysDateString());
        if (jsonObject.has("schedules_count")) model.setSchedules_count(!jsonObject.get("schedules_count").isJsonNull() ? jsonObject.get("schedules_count").getAsInt() : 0);
        if (jsonObject.has("complete_count")) model.setComplete_count(!jsonObject.get("complete_count").isJsonNull() ? jsonObject.get("complete_count").getAsInt() : 0 );
        itemArrayList.add(model);
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

    private String getCurDate() {
        String curDate = getTodaysDateStringToDo();
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            curDate = sharedPreferences.getString("curDate",getTodaysDateStringToDo());
        }

        return curDate;
    }

    private void setDateToUI(String date) {
        mTv_todayDate.setText(getCurDateForToDo(date));
        mTv_todayDay.setText(getCurDayForToDo(date));
        mTv_todayMonth.setText(getCurMonthForToDo(date));
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public void showCustomCalendarAlertDialog()
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

        //load events iff arrayList is not null nor empty
        if (itemArrayList!=null && itemArrayList.size()>0) loadEvents(cv_CompactCalendar);

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
                insertCurDate(getStringDateFromDate(dateClicked));

                //set date to UI
                setDateToUI(getFormatDateForToDo(getStringDateFromDate(dateClicked)));

                //dismiss the dialog
                alertDialog.dismiss();

                if (isNetworkAvailable(context)) call_getCallLogCount(getStringDateFromDate(dateClicked), true);
                else {
                    //network error
                    NetworkError(context);

                    // Setting ViewPager for each Tabs
                    //setupViewPager(getFormatDateForToDo(getStringDateFromDate(dateClicked)));

                    // Set Tabs inside Toolbar
                    //  mTabLayout.setupWithViewPager(mViewPager);
                }


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
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
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
        //addEvents(compactCalendarView, getLongDateFromString("2020-05-12"), 3, 1);
        //addEvents(compactCalendarView, getLongDateFromString("2020-05-13"), 1, 1);
        //addEvents(compactCalendarView, getLongDateFromString("2020-05-14"), 25, 10);
        //addEvents(compactCalendarView, getLongDateFromString("2020-05-15"), 47, 1);

        //set load events
        for (int i=0; i<itemArrayList.size(); i++) {
            addEvents(compactCalendarView, getLongDateFromString(itemArrayList.get(i).getCall_schedule_date()), itemArrayList.get(i).getSchedules_count(), itemArrayList.get(i).getComplete_count());
        }
    }


    private void addEvents(CompactCalendarView compactCalendarView, long timeMillis, int schedules_count, int complete_count) {
        currentCalender.setTime(new Date());

        /*if (day > -1) {
            currentCalender.set(Calendar.DAY_OF_MONTH,day);
        }
        if (month > -1) {
            currentCalender.set(Calendar.MONTH, month);
        }
        if (year > -1) {
            currentCalender.set(Calendar.ERA, GregorianCalendar.AD);
            currentCalender.set(Calendar.YEAR, year);
        }*/
        //setToMidnight(currentCalender);

        currentCalender.setTimeInMillis(timeMillis);
        long timeInMillis = currentCalender.getTimeInMillis();
        List<Event> events = getEvents(timeInMillis, schedules_count, complete_count);
        compactCalendarView.addEvents(events);
    }

    private List<Event> getEvents(long timeInMillis, int schedules_count, int complete_count)
    {

        ArrayList<Event> arrayList = new ArrayList<>();
        for (int i =0; i< schedules_count; i++) {

            //return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis, itemArrayList));
            //return Arrays.asList(new Event(Color.argb(255, 51, 153, 255), timeInMillis));
            Event event = new Event(Color.argb(255, 51, 153, 255), timeInMillis);
            arrayList.add(event);
        }

        //add completed count
        for (int i =0; i<complete_count; i++) {
            Event event1 = new Event(Color.argb(255, 21, 140, 85), timeInMillis);
            arrayList.add(event1);
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

    /*private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public interface onTabChangeInterface{
        void callOnTabChangedMethod();
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case (android.R.id.home):
                onBackPressed();
                break;

            case (R.id.action_eventCalendar):
                showCustomCalendarAlertDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearFilters() {

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("isFilter");
        editor.remove("isFilterCC");
        editor.apply();

    }

    @Override
    public void onBackPressed() {
        if(notify) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
        clearFilters();
    }


}
