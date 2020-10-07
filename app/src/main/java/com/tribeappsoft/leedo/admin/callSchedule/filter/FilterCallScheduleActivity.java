package com.tribeappsoft.leedo.admin.callSchedule.filter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.model.SalesExecutiveModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.formatDateFromString;
import static com.tribeappsoft.leedo.util.Helper.getLongNextDateFromString;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;
import static com.tribeappsoft.leedo.util.Helper.setDatePickerFormatDateFromString;


public class FilterCallScheduleActivity extends AppCompatActivity {


    @BindView(R.id.cl_filterCallSchedule) CoordinatorLayout parent;
    @BindView(R.id.ll_filterCallSchedule_main) LinearLayoutCompat ll_main;
    @BindView(R.id.ll_filterCallSchedule_spFilter) LinearLayoutCompat ll_spFilter;
    @BindView(R.id.ll_filterCallSchedule_dtFilter) LinearLayoutCompat ll_dtFilter;

    @BindView(R.id.acTv_filterCallSchedule_selectProject) AutoCompleteTextView acTv_selectProject;
    @BindView(R.id.tv_filterCallSchedule_select_SalesPerson) AutoCompleteTextView acTv__select_SalesPerson;

    @BindView(R.id.edt_filterCallSchedule_fromDate) TextInputEditText edt_fromDate;
    @BindView(R.id.edt_filterCallSchedule_toDate) TextInputEditText edt_toDate;
    @BindView(R.id.mBtn_filterCallSchedule_clearFilter) MaterialButton mBtn_clearFilter;
    @BindView(R.id.mBtn_filterCallSchedule_applyFilter) MaterialButton mBtn_applyFilter;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private AppCompatActivity context;
    private DatePickerDialog datePickerDialog;
    private ArrayList<String> projectStringArrayList;
    private ArrayList<ProjectModel> itemArrayList;

    private ArrayList<SalesExecutiveModel> salesExecutiveModelArrayList;
    private ArrayList<String> salesPersonArrayList;

    private int selectedProjectId=-1,sales_person_id=0,tabAt =0, mYear, mMonth, mDay;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "FilterCallScheduleActivity",api_token = "", selectedProjectName="", sendFromDate = null, sendToDate = null,selectedSalesPersonsName="";
    private int filterCount_dash=0, user_id =0;
    private boolean fromCallRecordings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_call_schedule);

        ButterKnife.bind(this);
        context= FilterCallScheduleActivity.this;

        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_filter_leads_by));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null) {
            tabAt = getIntent().getIntExtra("tabAt", 0);
            user_id = getIntent().getIntExtra("user_id", 0);
            fromCallRecordings = getIntent().getBooleanExtra("fromCallRecordings", false);
            Log.e(TAG, "onCreate: tabAt "+tabAt );
        }

        //init
        init();

    }


    private void init()
    {
        //hide pb
        hideProgressBar();

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        boolean isSalesTeamLead = sharedPreferences.getBoolean("isSalesTeamLead", false);
        //user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        itemArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();
        salesExecutiveModelArrayList=new ArrayList<>();
        salesPersonArrayList=new ArrayList<>();

        //set visible sales person filter only if sales head or team lead
        ll_spFilter.setVisibility(isSalesHead || isSalesTeamLead ? View.VISIBLE : View.GONE);
        ll_dtFilter.setVisibility(fromCallRecordings ? View.VISIBLE : View.GONE);

        //select from date
        edt_fromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        //select to date
        edt_toDate.setOnClickListener(view -> {
            if (sendFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(context, "Please select from date first!");
        });

        //Get meetings data
        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar(getString(R.string.please_wait));

            new Handler().postDelayed(() -> {
                call_getAllProjects();
                call_getAllUsers();
            },800);

            /*if (isSalesTeamLead) new Handler(getMainLooper()).postDelayed(this::call_getSalesPersonListByTeamLead, 100);
            else new Handler(getMainLooper()).postDelayed(this::call_getAllSalesPersonList, 100);*/
        }
        else {
            NetworkError(context);
            //hide main layouts
            hideProgressBar();
        }


        //apply filter
        mBtn_applyFilter.setOnClickListener(view -> {
            //check validation
            checkValidations();
        });

        //clear filter
        mBtn_clearFilter.setOnClickListener(view -> {
            //clear filter
            clearFilter();
        });

    }


    private void call_getAllProjects()
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
                                //set json
                                setJson(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() -> delayRefresh(), 1000);
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                //clear list
                itemArrayList.clear();
                projectStringArrayList.clear();
                ProjectModel model = new ProjectModel();
                model.setProject_id(0);
                model.setProject_name("All");
                projectStringArrayList.add("All");
                itemArrayList.add(model);
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
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        }
        itemArrayList.add(model);
    }

    //Delay Refresh
    private void delayRefresh() {

        if (context != null) {

            runOnUiThread(() ->
            {
                //hide pb
                hideProgressBar();

                //set already selected if isFilter is true
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
                    if (isFilter)
                    {
                        //
                        selectedProjectId = sharedPreferences.getInt("project_id", -1);
                        sales_person_id = sharedPreferences.getInt("sales_person_id", 0);
                        filterCount_dash = sharedPreferences.getInt("filterCount_dash", 0);
                        sendFromDate = sharedPreferences.getString("sendFromDate",null);
                        sendToDate = sharedPreferences.getString("sendToDate", null);

                        //set project name
                        if (selectedProjectId!=-1 && itemArrayList!=null&& itemArrayList.size()>0) {

                            Log.e(TAG, "delayRefresh: selected Project id "+selectedProjectId);
                            Log.e(TAG, "delayRefresh: selected Project index "+getIndexOfList(itemArrayList, selectedProjectId));


                            acTv_selectProject.setText(itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name());
                            selectedProjectName = itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name();

                            //acTv__select_SalesPerson.setText(salesExecutiveModelArrayList.get(getIndexOfListSalesPerson(salesExecutiveModelArrayList, sales_person_id)).getFull_name());
                            //selectedProjectName = salesExecutiveModelArrayList.get(getIndexOfListSalesPerson(salesExecutiveModelArrayList, selectedProjectId)).getFull_name();
                        }

                        //set from date if not null
                        if (sendFromDate!=null && !sendFromDate.trim().isEmpty()) {
                            edt_fromDate.setText(formatDateFromString(sendFromDate));
                        }

                        //set to date if not null
                        if (sendToDate!=null && !sendToDate.trim().isEmpty()) {
                            edt_toDate.setText(formatDateFromString(sendToDate));
                        }

                    }
                    editor.apply();
                }

                //set adapter for project names
                setAdapterForProjectName();

                //check button enabled
                checkButtonEnabled();

            });
        }
    }

    private int getIndexOfList(List<ProjectModel> list, int project_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  IntStream.range(0, list.size())
                    .filter(i -> list.get(i).getProject_id() == project_id)
                    .findFirst().orElse(-1);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getProject_id() == project_id)
                    return i;
            return -1;
        }
    }

    private void setAdapterForProjectName()
    {

        if (projectStringArrayList.size()>0 && itemArrayList.size()>0 )
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
            //acTv_selectProject.setText(projectStringArrayList.get(myPosition));
            acTv_selectProject.setAdapter(adapter);
            acTv_selectProject.setThreshold(0);

            //def set
            //selectedProjectId = itemArrayList.get(0).getProject_id();
            //selectedProjectName = itemArrayList.get(0).getProject_name();

            acTv_selectProject.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (ProjectModel pojo : itemArrayList)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedProjectId = pojo.getProject_id(); // This is the correct ID
                        selectedProjectName = pojo.getProject_name();
                        Log.e(TAG, "project name & id: "+selectedProjectName + " \t" +selectedProjectId);

                        //set filter count-- if dates are not selected then 1 otherwise 2
                        //  filterCount= sendFromDate==null && sendToDate==null ? 1 :2;

                        filterCount_dash = sales_person_id == 0 && (sendFromDate ==null && sendToDate ==null) ? 1 : sales_person_id > 0 && (sendFromDate != null && sendToDate !=null) ? 3 : 2;
                        Log.e(TAG, "setAdapterForProjectName: "+filterCount_dash );

                       /* if(sales_person_id==0 && sendFromDate==null && sendToDate==null ) {
                            filterCount_dash=1;
                        }
                        else if(sales_person_id==0 && sendFromDate!=null && sendToDate!=null ) {
                            filterCount_dash=1;
                        }
                        else if( sales_person_id>0 && sendFromDate==null && sendToDate==null) {
                            filterCount_dash=2;
                        }
                        else filterCount_dash=3;*/

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }


    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context), R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_fromDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendFromDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "From Date: "+ sendFromDate);

                    //set to date null or empty
                    sendToDate = null;
                    edt_toDate.setText("");

                    if(sendFromDate!=null &sendToDate!=null) {
                        //set filter count if projectID is not selected then 1 otherwise 2

                        //filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId == -1 && sales_person_id > 0 ? 2 : selectedProjectId >= 0 && sales_person_id > 0 ? 3 : 1;
                        filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId >=0  && sales_person_id > 0 ? 3 : 2;
                    }

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context), R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_toDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendToDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendToDate);


                    if(sendFromDate!=null &sendToDate!=null) {
                        //set filter count if projectID is not selected then 1 otherwise 2

                        //filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId >=0  && sales_person_id == 0 ? 2 :  selectedProjectId >= 0 && sales_person_id > 0 ? 3 : 1;

                        filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId >=0  && sales_person_id > 0 ? 3 : 2;
                        Log.e(TAG, "selectVisitPrefToDate: "+filterCount_dash );

                        /*if(selectedProjectId< 0 && sales_person_id<=0)
                        {
                            filterCount_dash=1;
                        }
                        else if(selectedProjectId==-1 && sales_person_id>0)
                        {
                            filterCount_dash=2;
                        }
                        else if(selectedProjectId>-1 && sales_person_id==0)
                        {
                            filterCount_dash=2;
                        }
                        else filterCount_dash=3;*/

                    }

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(getLongNextDateFromString(sendFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
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
                                //set json
                                setJsonSalesList(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() -> setSalesPersonsName(), 1000);
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

                //clear list
                salesExecutiveModelArrayList.clear();
                SalesExecutiveModel model=new SalesExecutiveModel();
                model.setUser_id(0);
                model.setFull_name("All");
                salesPersonArrayList.add("All");
                salesExecutiveModelArrayList.add(model);

                for(int i=0;i<jsonArray.size();i++) {
                    setSalesPersonJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }



    private void call_getSalesPersonListByTeamLead()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getSalesPersonsByTeamLead(api_token, user_id).enqueue(new Callback<JsonObject>()
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

                                if (response.body().has("data")) {
                                    if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject()) {
                                        JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                        //set json
                                        setJsonTeam(jsonObject);
                                    }
                                }

                                //set delayRefresh
                                new Handler().postDelayed(() -> setSalesPersonsName(), 1000);

                            } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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


    private void setJsonTeam(JsonObject jsonObject)
    {
        if (jsonObject.has("teamMembers")) {
            if (!jsonObject.get("teamMembers").isJsonNull() && jsonObject.get("teamMembers").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("teamMembers").getAsJsonArray();
                //clear list
                salesExecutiveModelArrayList.clear();
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


    private void setSalesPersonsName()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            //set already selected if isFilter is true
            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
                if (isFilter)
                {
                    //
                    selectedProjectId = sharedPreferences.getInt("project_id", -1);
                    sales_person_id = sharedPreferences.getInt("sales_person_id", 0);
                    filterCount_dash = sharedPreferences.getInt("filterCount_dash", 0);
                    sendFromDate = sharedPreferences.getString("sendFromDate",null);
                    sendToDate = sharedPreferences.getString("sendToDate", null);


                    //set project name
                    if ( sales_person_id!=0 && salesExecutiveModelArrayList!=null&& salesExecutiveModelArrayList.size()>0) {
                        // acTv_selectProject.setText(itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name());
                        //selectedProjectName = itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name();

                        acTv__select_SalesPerson.setText(salesExecutiveModelArrayList.get(getIndexOfListSalesPerson(salesExecutiveModelArrayList, sales_person_id)).getFull_name());
                        selectedSalesPersonsName = salesExecutiveModelArrayList.get(getIndexOfListSalesPerson(salesExecutiveModelArrayList, sales_person_id)).getFull_name();
                    }

                    //set from date if not null
                    if (sendFromDate!=null && !sendFromDate.trim().isEmpty()) {
                        edt_fromDate.setText(formatDateFromString(sendFromDate));
                    }

                    //set to date if not null
                    if (sendToDate!=null && !sendToDate.trim().isEmpty()) {
                        edt_toDate.setText(formatDateFromString(sendToDate));
                    }

                }
                editor.apply();
            }


            //set adapter for project names
            setAdapterSalesPersonsNames();

            //check button enabled
            checkButtonEnabled();


        });
    }

    private int getIndexOfListSalesPerson(List<SalesExecutiveModel> list, int sales_person_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  IntStream.range(0, list.size())
                    .filter(i -> list.get(i).getUser_id() == sales_person_id)
                    .findFirst().orElse(-1);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getUser_id() == sales_person_id)
                    return i;
            return -1;
        }
    }


    private void setAdapterSalesPersonsNames()
    {

        runOnUiThread(() -> {

            if (salesPersonArrayList.size() >0 && salesExecutiveModelArrayList.size()>0)
            {
                //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, salesPersonArrayList);
                //set def selected
                // acTv__select_SalesPerson.setText(salesPersonArrayList.get(0));
                //sales_person_id = salesExecutiveModelArrayList.get(0).getUser_id();
                //selectedSalesPersonsName = salesExecutiveModelArrayList.get(0).getFull_name();

                acTv__select_SalesPerson.setAdapter(adapter);
                acTv__select_SalesPerson.setThreshold(0);

                //tv_selectCustomer.setSelection(0);
                //autoComplete_firmName.setValidator(new Validator());
                //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

                acTv__select_SalesPerson.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                {
                    String itemName = adapter.getItem(position);

                    for (SalesExecutiveModel pojo : salesExecutiveModelArrayList)
                    {
                        if (pojo.getFull_name().equals(itemName))
                        {
                            //int id = pojo.getCompany_id(); // This is the correct ID
                            sales_person_id = pojo.getUser_id(); // This is the correct ID
                            selectedSalesPersonsName = pojo.getFull_name();
                            //fixedEnquiryID+=2;
                            Log.e(TAG, "Sales person Name & id " + selectedSalesPersonsName +"\t"+ sales_person_id);


                            filterCount_dash = selectedProjectId == -1 &&  (sendFromDate ==null && sendToDate ==null) ? 1 : selectedProjectId >= 0 && (sendFromDate != null && sendToDate !=null) ?  3 :  2;

                            Log.e(TAG, "setAdapterSalesPersonsNames: "+filterCount_dash);
                           /* if(selectedProjectId==-1 && sendFromDate==null && sendToDate==null )
                            {
                                filterCount_dash=1;
                            }
                            else if(selectedProjectId==-1 && sendFromDate!=null && sendToDate!=null )
                            {
                                filterCount_dash=1;
                            }
                            else if( selectedProjectId>-1 && sendFromDate==null && sendToDate==null)
                            {
                                filterCount_dash=2;
                            }
                            else filterCount_dash=3;*/

                            //check button EnabledView
                            checkButtonEnabled();

                            break; // No need to keep looping once you found it.
                        }
                    }
                });
            }

        });

    }



    private void checkValidations()
    {

        //project
        if (selectedProjectId==-1  && sales_person_id==0  && sendFromDate ==null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select at least one filter type!");

            //check if only from date selected
        else if (sendFromDate !=null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select To-Date!");

        else {

            //apply filter

           /* if (selectedProjectId!=-1)
            {
                //project id select
                filterCount_dash =1 ;
            }

            if (selectedProjectId!=1 && sales_person_id >0) {
               //project id and sales person id selected
               filterCount_dash =2;
            }

            if (selectedProjectId!=-1 && sales_person_id >0  && sendFromDate!=null && sendToDate!=null ) {
                //project id and sales person id and dates are selected
                filterCount_dash =3;
            }*/

            //filterCount_dash = selectedProjectId!=-1  && sales_person_id==0  && sendFromDate ==null && sendToDate ==  null
            //filterCount_dash = selectedProjectId == -1 && sendFromDate ==null && sendToDate ==null ? 1 : selectedProjectId >= 0 && sendFromDate == null && sendToDate ==null ? 2 :  sales_person_id > 0 && sendFromDate != null && sendToDate !=null ? 3 : 1;
            Log.e(TAG,"Salesperson id : "+ sales_person_id +"  project id : "+selectedProjectId);
            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("project_id", selectedProjectId == -1 ? 0 : selectedProjectId);
                editor.putString("sendFromDate", sendFromDate);
                editor.putString("sendToDate", sendToDate);
                editor.putString("project_name", selectedProjectName);
                editor.putString("sales_person_name", selectedSalesPersonsName);
                editor.putInt("sales_person_id", sales_person_id);
                editor.putInt("filterCount", filterCount_dash);
                editor.putInt("tabAt", tabAt);
                editor.putBoolean("isFilter", true);
                editor.putBoolean("isFilterCC", tabAt == 1);
                editor.apply();

                //do onBackPressed
                onBackPressed();
            }

        }

    }


    private void checkButtonEnabled()
    {

        //project id and dates are null
        if (selectedProjectId==-1 && sales_person_id==0 && sendFromDate ==null && sendToDate == null) setButtonDisabledView();

            //check if only from date selected
        else if (sendFromDate !=null && sendToDate ==  null) setButtonDisabledView();

        else {
            //set button enabled view
            setButtonEnabledView();
        }

    }



    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_applyFilter.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_applyFilter.setTextColor(getResources().getColor(R.color.main_white));
        mBtn_clearFilter.setTextColor(getResources().getColor(R.color.colorAccent));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit team lead
        mBtn_applyFilter.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_applyFilter.setTextColor(getResources().getColor(R.color.main_white));
        mBtn_clearFilter.setTextColor(getResources().getColor(R.color.main_medium_grey));
    }


    private void clearFilter()
    {

        if (selectedProjectId==-1  && sales_person_id ==0  && sendFromDate ==null && sendToDate == null) {
            //nothing to clear filter

            //show toast nothing to clear
            new Helper().showCustomToast(context, "Nothing to clear filters!");
        }
        else {

            //any one filter is applied -- do clear all filter

            //clear project filter
            selectedProjectId = -1;
            selectedProjectName="";
            acTv_selectProject.setText("");


            //clear salesPerson filter
            sales_person_id = 0;
            selectedSalesPersonsName="";
            acTv__select_SalesPerson.setText("");

            //clear dates filter
            sendFromDate =  sendToDate = null;
            edt_fromDate.setText("");
            edt_toDate.setText("");


            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("sales_person_name");
                editor.remove("project_name");
                editor.remove("isFilter");
                editor.remove("filterCount");
                editor.putBoolean("clearFilter", true);
                editor.apply();
            }

            new Helper().showSuccessCustomToast(context, "All filters cleared successfully!");

            //check button enabled
            checkButtonEnabled();

            //do onBackPressed
            new Handler().postDelayed(this::onBackPressed, 1000);
        }
    }





    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                onErrorSnack(context,message);

            });
        }
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




    @SuppressLint("SetTextI18n")
    private void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
