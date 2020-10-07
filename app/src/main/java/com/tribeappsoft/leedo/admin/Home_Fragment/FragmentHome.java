package com.tribeappsoft.leedo.admin.Home_Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callSchedule.model.CallScheduleLogsModel;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.model.SalesExecutiveModel;
import com.tribeappsoft.leedo.util.CustomTabLayout;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.discreteScrollView.DiscreteScrollView;

import java.io.IOException;
import java.net.SocketTimeoutException;
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

import static android.os.Looper.getMainLooper;

public class FragmentHome extends Fragment implements DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder> {

    private String TAG="FragmentHome";
    @BindView(R.id.tabLayout_FragHome)
    CustomTabLayout mTabLayout;
    @BindView(R.id.viewPager_FragHome) ViewPager mViewPager;
    @BindView(R.id.mTv_FragHome_Today) MaterialTextView mTv_FragHome_Today;
    @BindView(R.id.mTv_FragHome_Yesterday) MaterialTextView mTv_FragHome_Yesterday;
    @BindView(R.id.mTv_FragHome_LastWeek) MaterialTextView mTv_FragHome_LastWeek;
    @BindView(R.id.mTv_FragHome_LastMonth) MaterialTextView mTv_FragHome_LastMonth;
    @BindView(R.id.mTv_FragHome_Custom) MaterialTextView mTv_FragHome_Custom;
    @BindView(R.id.mTv_fragHome_todayDate) MaterialTextView mTv_todayDate;
    @BindView(R.id.mTv_fragHome_DayName) MaterialTextView mTv_DayName;

    @BindView(R.id.acTv_SalesHome_selectProject) AutoCompleteTextView acTv_SalesHome_selectProject;
    @BindView(R.id.acTv_SalesHome_selectSalesPerson) AutoCompleteTextView acTv_SalesHome_selectSalesPerson;
    @BindView(R.id.ll_fragHome_salesPersonDropdown) LinearLayoutCompat ll_fragHome_salesPersonDropdown;

    private TextInputEditText editText_start,editText_end;
    SectionsPagerAdapter adapter;
    private Context context;
    private boolean doubleBackToExitPressedOnce=false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int selectedProjectId =0;
    private int selectedSalesPersonId = 0;
    private int user_id=0;
    private int lead_count = 0;
    private int site_visit_count = 0;
    private int call_schedule_count = 0;
    private int reminder_count = 0;
    private int mYear,mMonth,mDay;
    private ArrayList<ProjectModel> projectArrayList;
    private ArrayList<SalesExecutiveModel> salesExecutiveModelArrayList;
    private ArrayList<CallScheduleLogsModel> itemArrayList;
    private ArrayList<String> projectStringArrayList,salesPersonArrayList;
    private String selectedProjectName ="";
    private String selectedSalesPersonName ="";
    private String sendStartDate="";
    private String sendEndDate="";
    private String dayOfTheWeek="";
    private String selected_date="";
    private String api_token="";
    private static FragmentHome instance = null;
    private int tabAt=0;

    public FragmentHome() {
        projectStringArrayList =new ArrayList<>();
        salesPersonArrayList=new ArrayList<>();
        projectArrayList=new ArrayList<>();
        itemArrayList=new ArrayList<>();
        salesExecutiveModelArrayList=new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance=this;
        setHasOptionsMenu(true);
    }

    public static FragmentHome getInstance(){
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_sales_person_home, container, false);
        ButterKnife.bind(this,view);
        context=view.getContext();


        try {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        DrawerLayout drawer = Objects.requireNonNull(requireActivity()).findViewById(R.id.drawer_layout_salesPerson);
                        if (drawer.isDrawerOpen(GravityCompat.START)) {
                            drawer.closeDrawer(GravityCompat.START);
                        }
                        else {
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

        sharedPreferences = new Helper().getSharedPref(Objects.requireNonNull(requireActivity()));
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();

        if(!isSalesHead){
            ll_fragHome_salesPersonDropdown.setVisibility(View.GONE);
            selectedSalesPersonId = sharedPreferences.getInt("user_id", 0);
        }

        projectStringArrayList =new ArrayList<>();

        getScreenResolution(context);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        Date d = new Date();
        dayOfTheWeek = sdf.format(d);

        //set today's day
        mTv_DayName.setText(dayOfTheWeek);

        //set today's date
        mTv_todayDate.setText(Helper.getTodaysFormattedDateTime());

        //first get the current date
        //String setDate = formatHomeDate(getPrevDate(getCurDate()));
        selected_date = getCurDate();
        Log.e(TAG, "onCreateView: selected_date "+ selected_date);

        new Handler(getMainLooper()).postDelayed(() -> {
            setupViewPager(selected_date,selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
            mTabLayout.setupWithViewPager(mViewPager);
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }, 80);

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

            if(isSalesHead){
                new Handler(getMainLooper()).postDelayed(this::call_getAllProjectList, 20);
                new Handler(getMainLooper()).postDelayed(this::call_getAllUsers, 50);
            }else {
                new Handler(getMainLooper()).postDelayed(this::call_getUserWiseAllProjectList, 20);
            }
        }
        else {
            Helper.NetworkError(requireActivity());
            //hide main layouts
            //hideProgressBar();
        }


        //set today's date to UI and call getAll counts api
        setTodaysDate();

       /* if (isNetworkAvailable(getActivity())) {
            //call get total count
            //getHomeCallScheduleListCount();

            //get date wise count
            call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);

        }
        else {
            //network error
            NetworkError(getActivity());

            //set up view pager for each Tabs
            //setupViewPager(formatHomeDate(getTodaysDateStringToDo()), selectedProjectId, selectedSalesPersonId,"","");

            Log.e(TAG, "onCreateView: setupViewPager" );
            // Set Tabs inside Toolbar
            //mTabLayout.setupWithViewPager(mViewPager);
        }*/


        //set up recyclerViews
        //setupRecycleView();

        //setup swipeRefresh
        //setSwipeRefresh();


        return view;
    }



    private void call_getAllProjectList()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAllProjects(api_token).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {

                                projectArrayList.clear();
                                ProjectModel model = new ProjectModel();
                                model.setProject_id(0);
                                model.setProject_name("All");
                                projectStringArrayList.add("All");
                                projectArrayList.add(model);
                                //set json
                                setJsonProjects(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() ->{
                                    if(projectArrayList!=null && projectArrayList.size()>0)
                                    {
                                        acTv_SalesHome_selectProject.setText(projectStringArrayList.get(0));
                                        selectedProjectId = projectArrayList.get(0).getProject_id();
                                        selectedProjectName = projectArrayList.get(0).getProject_name();
                                    }

                                    setProjectAdapter();
                                } , 1000);
                            }
                            else showErrorLog(getString(R.string.no_project_assigned));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else {

                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void call_getUserWiseAllProjectList()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getUserWiseAllProjects(api_token,user_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {

                                //clear list
                                projectArrayList.clear();

                                ProjectModel model = new ProjectModel();
                                model.setProject_id(0);
                                model.setProject_name("All");
                                projectStringArrayList.add("All");
                                projectArrayList.add(model);

                                //set json
                                setJsonProjects(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() ->{
                                    if(projectArrayList!=null && projectArrayList.size()>0)
                                    {
                                        acTv_SalesHome_selectProject.setText(projectStringArrayList.get(0));
                                        selectedProjectId = projectArrayList.get(0).getProject_id();
                                        selectedProjectName = projectArrayList.get(0).getProject_name();
                                    }

                                    setProjectAdapter();
                                } , 1000);
                            }else showErrorLog(getString(R.string.no_project_assigned));

                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else {

                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void setJsonProjects(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                // projectStringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setProjectJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }

    private void setProjectJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        }
        projectArrayList.add(model);
    }

    private void setProjectAdapter()
    {

        //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
        //acTv_projectName.setText(projectStringArrayList.get(0));
        acTv_SalesHome_selectProject.setAdapter(adapter);
        acTv_SalesHome_selectProject.setThreshold(0);

        //tv_selectCustomer.setSelection(0);
        //autoComplete_firmName.setValidator(new Validator());
        //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

        acTv_SalesHome_selectProject.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);
            for (ProjectModel pojo : projectArrayList)
            {
                if (pojo.getProject_name().equals(itemName))
                {
                    //int id = pojo.getCompany_id(); // This is the correct ID
                    selectedProjectId = pojo.getProject_id(); // This is the correct ID
                    selectedProjectName = pojo.getProject_name();

                    //clearFlags();

                    //insert current date
                    insertCurDate(selected_date,sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

                    new Handler().postDelayed(() -> {
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                            call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);

                        } else {
                            Helper.NetworkError(requireActivity());

                            //set up view pager
                            //setupViewPager("",selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                            // Set Tabs inside Toolbar
                            //mTabLayout.setupWithViewPager(mViewPager);
                        }
                    }, 500);

                    //fixedEnquiryID+=2;
                    Log.e(TAG, "Project name & id " + selectedProjectName +"\t"+ selectedProjectId);



                    break; // No need to keep looping once you found it.
                }
            }
        });
    }

    private void call_getAllUsers()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAllUser(api_token,user_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {

                                //clear list
                                salesExecutiveModelArrayList.clear();

                                SalesExecutiveModel model=new SalesExecutiveModel();
                                model.setUser_id(0);
                                model.setFull_name("All");
                                salesPersonArrayList.add("All");
                                salesExecutiveModelArrayList.add(model);

                                //set json
                                setJsonSalesList(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() ->{

                                    if(salesPersonArrayList!=null && salesPersonArrayList.size()>0)
                                    {
                                        acTv_SalesHome_selectSalesPerson.setText(salesPersonArrayList.get(0));
                                        selectedSalesPersonId = salesExecutiveModelArrayList.get(0).getUser_id();
                                        selectedSalesPersonName = salesExecutiveModelArrayList.get(0).getFull_name();
                                    }
                                    setSalesPersonAdapter();
                                } , 1000);
                            }
                            else showErrorLog(getString(R.string.no_users_assigned));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else {

                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void setJsonSalesList(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                for(int i=0;i<jsonArray.size();i++) {
                    setSalesPersonJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }

    private void setSalesPersonJson(JsonObject jsonObject)
    {
        SalesExecutiveModel model=new SalesExecutiveModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("person_id")) model.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("is_team_lead")) model.setIs_team_lead(!jsonObject.get("is_team_lead").isJsonNull() ? jsonObject.get("is_team_lead").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setPhotopath(!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : "");

        if (jsonObject.has("full_name"))
        {
            model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
            salesPersonArrayList.add(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        }
        salesExecutiveModelArrayList.add(model);
    }

    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {

                // hideProgressBar();
                Helper.onErrorSnack(requireActivity(), message);
            });
        }
    }

    private void setSalesPersonAdapter()
    {

        //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, salesPersonArrayList);
        //acTv_projectName.setText(projectStringArrayList.get(0));
        acTv_SalesHome_selectSalesPerson.setAdapter(adapter);
        acTv_SalesHome_selectSalesPerson.setThreshold(0);


        //tv_selectCustomer.setSelection(0);
        //autoComplete_firmName.setValidator(new Validator());
        //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

        acTv_SalesHome_selectSalesPerson.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);
            for (SalesExecutiveModel pojo : salesExecutiveModelArrayList)
            {
                if (pojo.getFull_name().equals(itemName))
                {
                    //int id = pojo.getCompany_id(); // This is the correct ID
                    selectedSalesPersonId = pojo.getUser_id(); // This is the correct ID
                    selectedSalesPersonName = pojo.getFull_name();

                    //clearFlags();

                    //insert current date
                    insertCurDate(selected_date,sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

                    new Handler().postDelayed(() -> {
                        if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                            call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);

                        } else { Helper.NetworkError(requireActivity());

                            //set up view pager
                            //setupViewPager("",selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                            // Set Tabs inside Toolbar
                            //mTabLayout.setupWithViewPager(mViewPager);
                        }
                    }, 500);

                    //fixedEnquiryID+=2;
                    Log.e(TAG, "SalesPerson name & id " + selectedSalesPersonName +"\t"+ selectedSalesPersonId);
                    break; // No need to keep looping once you found it.
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: ");

        setNewestPage();
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        int tabAt = sharedPreferences.getInt("tabAt",0);

        if (sharedPreferences.getBoolean("isFilter", false)) {

            Log.e(TAG, "onResume: isFilter "+ sharedPreferences.getBoolean("isFilter", false));
            Log.e(TAG, "onResume: tabAt : "+ tabAt);
            try{
                Objects.requireNonNull(mTabLayout.getTabAt(tabAt)).select();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        editor.apply();


      /*  mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });*/



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

    public void onSetTabsViewPager( int CallListCount, int SiteVisitCount, int LeadsCount, int RemindersCount,
                                    int tabAt, boolean visible, boolean fromHomeFrag)
    {
        // Setting ViewPager for each Tabs
        // setupViewPager(date, CallListCount, SiteVisitCount,LeadsCount,RemindersCount);

        // Set Tabs inside Toolbar
        //mTabLayout.setupWithViewPager(mViewPager);

        Log.e(TAG, "onSetTabsViewPager: tabAt "+tabAt );

        if(!fromHomeFrag) Objects.requireNonNull(mTabLayout.getTabAt(tabAt)).select();

        BadgeDrawable badge_callList = Objects.requireNonNull(mTabLayout.getTabAt(0)).getOrCreateBadge();
        badge_callList.setVisible(visible);
        // Optionally show a number.
        badge_callList.setNumber(CallListCount);
        badge_callList.setBadgeTextColor(getResources().getColor(R.color.main_white));
        badge_callList.setBackgroundColor(getResources().getColor(R.color.colorAccent));


        BadgeDrawable badge_siteVisits = Objects.requireNonNull(mTabLayout.getTabAt(1)).getOrCreateBadge();
        badge_siteVisits.setVisible(visible);
        // Optionally show a number.
        badge_siteVisits.setNumber(SiteVisitCount);
        badge_siteVisits.setBadgeTextColor(getResources().getColor(R.color.main_white));
        badge_siteVisits.setBackgroundColor(getResources().getColor(R.color.colorAccent));


        BadgeDrawable badge_Leads = Objects.requireNonNull(mTabLayout.getTabAt(2)).getOrCreateBadge();
        badge_Leads.setVisible(visible);
        // Optionally show a number.
        badge_Leads.setNumber(LeadsCount);
        badge_Leads.setBadgeTextColor(getResources().getColor(R.color.main_white));
        badge_Leads.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        BadgeDrawable badge_Reminders = Objects.requireNonNull(mTabLayout.getTabAt(3)).getOrCreateBadge();
        badge_Reminders.setVisible(visible);
        // Optionally show a number.
        badge_Reminders.setNumber(RemindersCount);
        badge_Reminders.setBadgeTextColor(getResources().getColor(R.color.main_white));
        badge_Reminders.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }


    // Add Fragments to Tabs
    private void setupViewPager(String todoDate , int selectedProjectId, int selectedSalesPersonId,String startDate,String endDate)
    {
        //String serviceDate = getSendFormatDateForToDo(todoDate);

        Log.e(TAG, "setupViewPager : "+"todoDate : "+todoDate+"  startDate : "+startDate+"  endDate : "+endDate);
        Fragment fragment_CallList = new Fragment_CallList();
      /*  Bundle bundle_cl = new Bundle();
        bundle_cl.putString("todo_date", serviceDate);
        bundle_cl.putString("startDate", startDate);
        bundle_cl.putString("endDate", endDate);
        fragment_CallList.setArguments(bundle_cl);
*/
        Fragment fragment_SiteVisits = new Fragment_SiteVisits();
     /*   Bundle bundle_sv = new Bundle();
        bundle_sv.putString("todo_date", serviceDate);
        bundle_sv.putString("startDate", startDate);
        bundle_sv.putString("endDate", endDate);
        bundle_sv.putInt("project_id", selectedProjectId);
        bundle_sv.putInt("sales_person_id", selectedSalesPersonId);
        fragment_SiteVisits.setArguments(bundle_sv);
*/
        Fragment fragment_Leads = new Fragment_Leads();
       /* Bundle bundle_leads = new Bundle();
        bundle_leads.putString("todo_date", serviceDate);
        bundle_leads.putString("startDate", startDate);
        bundle_leads.putString("endDate", endDate);
        bundle_leads.putInt("project_id", selectedProjectId);
        bundle_leads.putInt("sales_person_id", selectedSalesPersonId);
        fragment_Leads.setArguments(bundle_leads);*/

        Fragment fragment_Reminders = new Fragment_Reminders();
        /*Bundle bundle_reminder = new Bundle();
        bundle_reminder.putString("todo_date", serviceDate);
        bundle_reminder.putString("startDate", startDate);
        bundle_reminder.putString("endDate", endDate);
        bundle_reminder.putInt("project_id", selectedProjectId);
        bundle_reminder.putInt("sales_person_id", selectedSalesPersonId);
        fragment_Reminders.setArguments(bundle_reminder);*/

        adapter = new SectionsPagerAdapter(getChildFragmentManager());
        adapter.addFragment(fragment_CallList,  "Scheduled Calls  ");
        adapter.addFragment(fragment_SiteVisits,  "Site Visits  ");
        adapter.addFragment(fragment_Leads,  "Leads  ");
        adapter.addFragment(fragment_Reminders,  "Reminders  ");
        mViewPager.setAdapter(adapter);

       /* mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

                //Fragment fragment = adapter.getItem(position);
                switch (position)
                {
                    case  0:
                        Fragment_CallList.getInstance().onPageChange();
                        break;

                    case  1:
                        // ((Fragment_SiteVisits) fragment).onPageChange();
                        Fragment_SiteVisits.getInstance().onPageChange();
                        break;
                    case  2:
                        Fragment_Leads.getInstance().onPageChange();
                        break;
                    case  3:
                        Fragment_Reminders.getInstance().onPageChange();
                        break;
                }

                //onTabChangeInterface.callOnTabChangedMethod();
            }
        });
    }


    public static class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        SectionsPagerAdapter(FragmentManager manager) {
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


    private void call_getCallLogCount(String serviceDate,String startDate,String endDate,int tabAt)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getHomeAllCounts(api_token, selectedSalesPersonId,selectedProjectId, serviceDate,startDate,endDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {

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
                            }
                        }
                    }
                }

                // set up view pager and tabLayout
                setTabCountViewPager(serviceDate,startDate,endDate,tabAt);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());

                // set up view pager and tabLayout
                setTabCountViewPager(serviceDate,startDate,endDate,tabAt);
            }
        });
    }


    private void setTabCountViewPager(String serviceDate,String startDate,String endDate,int tabAt)
    {
        Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {

            int position = mTabLayout.getSelectedTabPosition();

            switch (position)
            {
                case  0:
                    Fragment_CallList.getInstance().onPageChange();
                    break;

                case  1:
                    // ((Fragment_SiteVisits) fragment).onPageChange();
                    Fragment_SiteVisits.getInstance().onPageChange();
                    break;
                case  2:
                    Fragment_Leads.getInstance().onPageChange();
                    break;
                case  3:
                    Fragment_Reminders.getInstance().onPageChange();
                    break;
            }


            // Fragment_SiteVisits.getInstance().onPageChange();
            //  Fragment_Leads.getInstance().onPageChange();
            // Fragment_Reminders.getInstance().onPageChange();

            // Setting ViewPager for each Tabs
            // setupViewPager(serviceDate, selectedProjectId, selectedSalesPersonId,startDate,endDate);

            // Set Tabs inside Toolbar
            // mTabLayout.setupWithViewPager(mViewPager);

            //set up view pager for each Tabs
            //setupViewPager(getFormatDateForToDo(getTodaysDateStringToDo()), scheduledCount, completedCount);

            // Set Tabs inside Toolbar
            // mTabLayout.setupWithViewPager(mViewPager);

            //set tabs badgeCount
            onSetTabsViewPager( call_schedule_count, site_visit_count, lead_count, reminder_count, tabAt, true,true);
        });
    }


    private void getHomeCallScheduleListCount()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getHomeCallScheduleListCount(api_token, user_id).enqueue(new Callback<JsonObject>() {
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
        if (jsonObject.has("call_schedule_date")) model.setCall_schedule_date(!jsonObject.get("call_schedule_date").isJsonNull() ? jsonObject.get("call_schedule_date").getAsString() : Helper.getTodaysDateString());
        if (jsonObject.has("schedules_count")) model.setSchedules_count(!jsonObject.get("schedules_count").isJsonNull() ? jsonObject.get("schedules_count").getAsInt() : 0);
        if (jsonObject.has("complete_count")) model.setComplete_count(!jsonObject.get("complete_count").isJsonNull() ? jsonObject.get("complete_count").getAsInt() : 0 );
        itemArrayList.add(model);
    }


    private void setNewestPage()
    {
        mTv_FragHome_Today.setOnClickListener(v -> setTodaysDate());
        mTv_FragHome_Yesterday.setOnClickListener(v -> setYesterdaysData());
        mTv_FragHome_LastWeek.setOnClickListener(v -> setLastWeekData());
        mTv_FragHome_LastMonth.setOnClickListener(v -> setLastMonthData());
        mTv_FragHome_Custom.setOnClickListener(v -> setCustomData());
    }

    private void setTodaysDate()
    {
        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("startDate");
        editor.remove("endDate");
        editor.apply();

        sendStartDate=sendEndDate="";

        /*put tab value*/
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            tabAt = sharedPreferences.getInt("tabAt", 0);
            editor.apply();
        }

        //first get the current date
        String setDate = Helper.formatHomeDate(Helper.getTodaysDateStringToDo());
        selected_date=setDate;

        //insert current date
        insertCurDate(setDate,sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

        new Handler().postDelayed(() -> {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                call_getCallLogCount(setDate,sendStartDate,sendEndDate,tabAt);

            } else {
                Helper.NetworkError(requireActivity());
                //set up view pager
                //setupViewPager(setDate,selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                // Set Tabs inside Toolbar
                // mTabLayout.setupWithViewPager(mViewPager);
            }
        }, 1000);


        mTv_FragHome_Today.setTextColor(getResources().getColor(R.color.main_white));
        mTv_FragHome_Today.setTextAppearance(requireActivity(), R.style.styleFontBold);
        mTv_FragHome_Yesterday.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Yesterday.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastMonth.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastMonth.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastWeek.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastWeek.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Custom.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Custom.setTextAppearance(requireActivity(), R.style.styleFontMedium);

    }

    private void setYesterdaysData()
    {

        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("startDate");
        editor.remove("endDate");
        editor.apply();

        sendStartDate=sendEndDate="";

        /*put tab value*/
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            tabAt = sharedPreferences.getInt("tabAt", 0);
            editor.apply();
        }
        Log.e(TAG,"Previous Date : "+ Helper.getPrevDate(Helper.getTodaysDateStringToDo())+"   Current date : "+ Helper.getTodaysDateStringToDo());

        //second insert current date as an updated/changed prev date
        String setDate = Helper.formatHomeDate(Helper.getPrevDate(Helper.getTodaysDateStringToDo()));
        selected_date=setDate;
        //insert current date
        insertCurDate(setDate,sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

        new Handler().postDelayed(() -> {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
                call_getCallLogCount(setDate,sendStartDate,sendEndDate,tabAt);

            } else {
                Helper.NetworkError(requireActivity());
                //set up view pager
                Log.e(TAG, "setYesterdaysData: "+setDate);
                // setupViewPager(setDate,selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                // Set Tabs inside Toolbar
                //  mTabLayout.setupWithViewPager(mViewPager);
            }
        }, 1000);

        mTv_FragHome_Today.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Today.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Yesterday.setTextColor(getResources().getColor(R.color.main_white));
        mTv_FragHome_Yesterday.setTextAppearance(requireActivity(), R.style.styleFontBold);
        mTv_FragHome_LastMonth.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastMonth.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastWeek.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastWeek.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Custom.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Custom.setTextAppearance(requireActivity(), R.style.styleFontMedium);

    }

    private void setLastMonthData()
    {
        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("todoDate");
        editor.apply();

        /*put tab value*/
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            tabAt = sharedPreferences.getInt("tabAt", 0);
            editor.apply();
        }

        String MonthStartDate = Helper.getCalculatedDate("yyyy-MM-dd", -29);
        String MonthEndDate = Helper.formatHomeDate(Helper.getTodaysDateStringToDo());

       /* //set last month start date & end date
        Calendar aCalendar = Calendar.getInstance();
        // add -1 month to current month
        aCalendar.add(Calendar.MONTH, -1);
        // set DATE to 1, so first date of previous month
        aCalendar.set(Calendar.DATE, 1);

        Date firstDateOfPreviousMonth = aCalendar.getTime();

        // set actual maximum date of previous month
        aCalendar.set(Calendar.DATE,     aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        //read it
        Date lastDateOfPreviousMonth = aCalendar.getTime();

        sendStartDate= Helper.simpleDateformat(firstDateOfPreviousMonth.toString());
        sendEndDate= Helper.simpleDateformat(lastDateOfPreviousMonth.toString());*/

        sendStartDate= MonthStartDate;
        sendEndDate= MonthEndDate;
        selected_date ="";

        //insert current date
        insertCurDate("",sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

        new Handler().postDelayed(() -> {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {

                call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);

            } else { Helper.NetworkError(requireActivity());

                //set up view pager
                // setupViewPager("",selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                // Set Tabs inside Toolbar
                //  mTabLayout.setupWithViewPager(mViewPager);
            }
        }, 1000);

        Log.e(TAG, "setLastMonthData: "+sendStartDate + "   "+sendEndDate );

        mTv_FragHome_Today.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Today.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Yesterday.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Yesterday.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastMonth.setTextColor(getResources().getColor(R.color.main_white));
        mTv_FragHome_LastMonth.setTextAppearance(requireActivity(), R.style.styleFontBold);
        mTv_FragHome_LastWeek.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastWeek.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Custom.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Custom.setTextAppearance(requireActivity(), R.style.styleFontMedium);

    }

    private void setLastWeekData()
    {
        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("todoDate");
        editor.apply();

        /*put tab value*/
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            tabAt = sharedPreferences.getInt("tabAt", 0);
            editor.apply();
        }

        String WeekStartDate = Helper.getCalculatedDate("yyyy-MM-dd", -6);
        String WeekEndDate = Helper.formatHomeDate(Helper.getTodaysDateStringToDo());

        Log.e(TAG, "setLastWeekData: WeekStartDate "+WeekStartDate);
        Log.e(TAG, "setLastWeekData: WeekEndDate "+WeekEndDate);

   /*     //set last week start date & end date
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
        c.add(Calendar.DATE, -i - 7);
        Date start = c.getTime();
        c.add(Calendar.DATE, 6);
        Date end = c.getTime();

        sendStartDate= Helper.simpleDateformat(start.toString());
        sendEndDate= Helper.simpleDateformat(end.toString());*/

        sendStartDate=WeekStartDate;
        sendEndDate=WeekEndDate;

        selected_date = "";

        //insert current date
        insertCurDate("",sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

        new Handler().postDelayed(() -> {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
                call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);
            } else { Helper.NetworkError(requireActivity());

                //set up view pager
                // setupViewPager("",selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                Log.e(TAG, "setLastWeekData: setupViewPager" );
                //   // Set Tabs inside Toolbar
                //   mTabLayout.setupWithViewPager(mViewPager);
            }
        }, 1000);

        mTv_FragHome_Today.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Today.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Yesterday.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Yesterday.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastMonth.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastMonth.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastWeek.setTextColor(getResources().getColor(R.color.main_white));
        mTv_FragHome_LastWeek.setTextAppearance(requireActivity(), R.style.styleFontBold);
        mTv_FragHome_Custom.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Custom.setTextAppearance(requireActivity(), R.style.styleFontMedium);

    }
    private void setCustomData()
    {
        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("todoDate");
        editor.apply();

        /*put tab value*/
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            tabAt = sharedPreferences.getInt("tabAt", 0);
            editor.apply();
        }

        if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
            customAlert(tabAt);
        }
        mTv_FragHome_Today.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Today.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Yesterday.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_Yesterday.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastMonth.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastMonth.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_LastWeek.setTextColor(getResources().getColor(R.color.GrayLight));
        mTv_FragHome_LastWeek.setTextAppearance(requireActivity(), R.style.styleFontMedium);
        mTv_FragHome_Custom.setTextColor(getResources().getColor(R.color.main_white));
        mTv_FragHome_Custom.setTextAppearance(requireActivity(), R.style.styleFontBold);

    }

    private void customAlert(int tabAt){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_custom_timeoff_month_alert, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        //MaterialTextView mTv_start=alertLayout.findViewById(R.id.mTv_customAlert_startDate);
        //MaterialTextView mTv_end=alertLayout.findViewById(R.id.mTv_customAlert_endDate);

        editText_start=alertLayout.findViewById(R.id.edt_customAlert_startDate);
        editText_end=alertLayout.findViewById(R.id.edt_customAlert_endDate);
        MaterialButton mBtn_cancel=alertLayout.findViewById(R.id.btn_custom_month_alert_negativeButton);
        MaterialButton mBtn_submit=alertLayout.findViewById(R.id.btn_custom_month_alert_positiveButton);

        editText_start.setOnClickListener(view13 -> selectStartDate());
        editText_end.setOnClickListener(view14 -> selectEndDate());

        mBtn_submit.setOnClickListener(view1 -> {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity())))
            {
                //  if (sr_customerHome!=null) sr_customerHome.setRefreshing(true);
                Log.d(TAG,"startDate Date"+ sendStartDate);
                Log.d(TAG,"endDate"+ sendEndDate);
                selected_date = "";
                if(!Objects.requireNonNull(editText_start.getText()).toString().trim().isEmpty())
                {
                    if(!Objects.requireNonNull(editText_end.getText()).toString().trim().isEmpty()){

                        //insert current date
                        insertCurDate("",sendStartDate,sendEndDate,selectedProjectId,selectedSalesPersonId);

                        new Handler().postDelayed(() -> {
                            if (Helper.isNetworkAvailable(Objects.requireNonNull(requireActivity()))) {
                                call_getCallLogCount(selected_date,sendStartDate,sendEndDate,tabAt);
                            } else { Helper.NetworkError(requireActivity());

                                //set up view pager
                                //setupViewPager("",selectedProjectId,selectedSalesPersonId,sendStartDate,sendEndDate);
                                // Set Tabs inside Toolbar
                                //  mTabLayout.setupWithViewPager(mViewPager);
                            }
                        }, 1000);

                        alertDialog.dismiss();

                    }else  new Helper().showCustomToast(Objects.requireNonNull(requireActivity()), "Please enter End Date!");

                }else  new Helper().showCustomToast(Objects.requireNonNull(requireActivity()), "Please enter Start Date!");
            }
            else Helper.NetworkError(requireActivity());
        });

        mBtn_cancel.setOnClickListener(view12 -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
    }

    private void selectStartDate() {
        Calendar calendar=Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    if(editText_start!=null) editText_start.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    int mth=monthOfYear+1;
                    sendStartDate = year + "-" + mth + "-" +dayOfMonth;
                    Log.e(TAG, "Task Date " + sendStartDate);

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

    private void selectEndDate() {
        Calendar calendar=Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), R.style.MyDatePicker, (view, year, monthOfYear, dayOfMonth) -> {

            if(editText_end!=null) editText_end.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
            int mth=monthOfYear+1;
            sendEndDate = year + "-" + mth + "-" + dayOfMonth;
            Log.e(TAG, "Task Date " + sendStartDate);

        }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

    private void insertCurDate(String todoDate,String startDate,String endDate,int project_id,int sales_person_id) {
        if (sharedPreferences!=null)
        {
            Log.e(TAG, "insertCurDate : todoDate :"+todoDate+"  startDate:"+startDate+"  endDate:"+ endDate);

            editor = sharedPreferences.edit();
            editor.putString("todoDate",todoDate);
            editor.putString("startDate",startDate);
            editor.putString("endDate",endDate);
            editor.putInt("project_id",project_id);
            editor.putInt("sales_person_id",sales_person_id);
            editor.apply();
            //2019-01-08
        }
    }

    private String getCurDate() {
        //String curDate = Helper.formatHomeDate(Helper.getTodaysDateStringToDo());
       /* if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            curDate = sharedPreferences.getString("curDate",getTodaysDateStringToDo());
        }*/

        return Helper.formatHomeDate(Helper.getTodaysDateStringToDo());
    }


    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {

    }



    private void getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        }
        DisplayMetrics metrics = new DisplayMetrics();
        if (display != null) {
            display.getMetrics(metrics);
        }
        // int height = metrics.heightPixels;
    }
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {

            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.menu_blank, menu);
    }*/

    private void doOnBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if (getActivity() != null) getActivity().onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        new Helper().showCustomToast(Objects.requireNonNull(requireActivity()), getResources().getString(R.string.app_exit_msg));
        new Handler(getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //viewDestroyed = true;
    }

    public void clearFlags()
    {
        //clear filters
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.remove("todoDate");
        editor.remove("startDate");
        editor.remove("endDate");
        editor.remove("endDate");
        editor.remove("isFilter");
        editor.remove("isFilterCC");
        editor.remove("tabAt");
        editor.remove("project_id");
        editor.remove("sales_person_id");
        editor.apply();

    }
}
