package com.tribeappsoft.leedo.salesPerson.salesHead.siteVistStat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.model.SalesExecutiveModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SiteVisitStatActivity extends AppCompatActivity {

    @BindView(R.id.mtv_todayNewSiteVisit)
    MaterialTextView mtvTodayNewSiteVisit;
    @BindView(R.id.mtv_todaysRevisits)
    MaterialTextView mtvTodaysRevisits;
    @BindView(R.id.mtv_filteredSiteVisit)
    MaterialTextView mtvFilteredSiteVisit;
    @BindView(R.id.mtv_filteredRevisits)
    MaterialTextView mtvFilteredRevisits;
    @BindView(R.id.mtv_filteredGhpVisits)
    MaterialTextView mtvFilteredGhpVisits;
    @BindView(R.id.acTv_siteVisitStat_selectProject)
    AutoCompleteTextView actv_SelectProject;
    @BindView(R.id.acTv_siteVisitStats_selectSalesManager)
    AutoCompleteTextView actv_SelectManager;
    @BindView(R.id.edt_siteVisitStat_fromDate)
    TextInputEditText edt_FromDate;
    @BindView(R.id.edt_siteVisitStats_toDate)
    TextInputEditText edt_ToDate;
    @BindView(R.id.ll_pbLayout)
    LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private DatePickerDialog datePickerDialog;
    private String TAG = "SiteVisitActivity",api_token="",sendFromDate = null, sendToDate = null;
    private AppCompatActivity context;
    private int sales_person_id = 0,project_id=-1,mYear, mMonth, mDay;

    private ArrayList<String> projectStringArrayList;
    private ArrayList<ProjectModel> itemArrayList;
    private ArrayList<SalesExecutiveModel> salesExecutiveModelArrayList;
    private ArrayList<String> salesPersonArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_visit_stat);
        ButterKnife.bind(this);
        context = SiteVisitStatActivity.this;


        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(R.string.site_visit_stats);

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        init();


    }

    private void init(){

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        itemArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();
        salesExecutiveModelArrayList=new ArrayList<>();
        salesPersonArrayList=new ArrayList<>();

        //select from date
        edt_FromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        //select to date
        edt_ToDate.setOnClickListener(view -> {
            if (sendFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(context, "Please select from date first!");
        });

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar(getString(R.string.please_wait));

            new Handler(getMainLooper()).postDelayed(this::call_getAllProjects, 100);
            new Handler(getMainLooper()).postDelayed(this::call_getAllSalesPersonList, 100);
            new Handler(getMainLooper()).postDelayed(this::callSiteVisitStats,100);
        }
        else {
            Helper.NetworkError(context);
            //hide main layouts
            hideProgressBar();
        }
    }

    private void callSiteVisitStats()
    {

        ApiClient client = ApiClient.getInstance();
        client.getApiService().getSiteVisitStats(api_token, sendFromDate,sendToDate,sales_person_id,project_id).enqueue(new Callback<JsonObject>()
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
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setSiteVisitStatsJson(data);

                                }
                            }
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
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
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setSiteVisitStatsJson(JsonObject jsonObject){
        if (!jsonObject.isJsonNull() && jsonObject.get("todays_stats").isJsonObject() )
        {
            JsonObject obj1  = jsonObject.get("todays_stats").getAsJsonObject();
            mtvTodayNewSiteVisit.setText(obj1.has("site_visits") ? obj1.get("site_visits").toString() : "0");
            mtvTodaysRevisits.setText(obj1.has("re_visits") ? obj1.get("re_visits").toString() : "0");


        }
        if (!jsonObject.isJsonNull() && jsonObject.get("filtered_stats").isJsonObject() ) {
            JsonObject obj = jsonObject.get("filtered_stats").getAsJsonObject();
            mtvFilteredSiteVisit.setText(obj.has("site_visits") ? obj.get("site_visits").toString() : "0");
            mtvFilteredRevisits.setText(obj.has("re_visits") ? obj.get("re_visits").toString() : "0");
            mtvFilteredGhpVisits.setText(obj.has("ghp_visits") ? obj.get("ghp_visits").toString() : "0");
        }
        hideProgressBar();
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
                hideProgressBar();
                //set adapter for project names
                setAdapterForProjectName();

                //check button enabled
               // checkButtonEnabled();

            });
        }
    }

    private void setAdapterForProjectName()
    {
        if (projectStringArrayList.size()>0 && itemArrayList.size()>0 )
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
            //acTv_selectProject.setText(projectStringArrayList.get(myPosition));
            actv_SelectProject.setAdapter(adapter);
            actv_SelectProject.setThreshold(0);

            //def set
            //selectedProjectId = itemArrayList.get(0).getProject_id();
            //selectedProjectName = itemArrayList.get(0).getProject_name();

            actv_SelectProject.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (ProjectModel pojo : itemArrayList)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        project_id = pojo.getProject_id(); // This is the correct ID
                        showProgressBar("Please wait");
                        callSiteVisitStats();
                        //selectedProjectName = pojo.getProject_name();

                                                //set filter count-- if dates are not selected then 1 otherwise 2
                        //  filterCount= sendFromDate==null && sendToDate==null ? 1 :2;

                        //check button EnabledView
                        //checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }
        else {
            actv_SelectProject.setText(R.string.nothing_to_select);
        }
    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }

    private void call_getAllSalesPersonList()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAllSalesPersonsList(api_token).enqueue(new Callback<JsonObject>()
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
            //set adapter for project names
            setAdapterSalesPersonsNames();

            //check button enabled
           // checkButtonEnabled();
        });
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

                actv_SelectManager.setAdapter(adapter);
                actv_SelectManager.setThreshold(0);

                //tv_selectCustomer.setSelection(0);
                //autoComplete_firmName.setValidator(new Validator());
                //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

                actv_SelectManager.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                {
                    String itemName = adapter.getItem(position);

                    for (SalesExecutiveModel pojo : salesExecutiveModelArrayList)
                    {
                        if (pojo.getFull_name().equals(itemName))
                        {
                            //int id = pojo.getCompany_id(); // This is the correct ID
                            sales_person_id = pojo.getUser_id(); // This is the correct ID
                            showProgressBar("Please wait");
                            callSiteVisitStats();
                            //selectedSalesPersonsName = pojo.getFull_name();
                            //fixedEnquiryID+=2;

                            //  filterCount_dash = selectedProjectId == -1 &&  (sendFromDate ==null && sendToDate ==null) ? 1 : selectedProjectId >= 0 && (sendFromDate != null && sendToDate !=null) ?  3 :  2;

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
                            //checkButtonEnabled();

                            break; // No need to keep looping once you found it.
                        }
                    }
                });
            }else {
                actv_SelectManager.setText(R.string.nothing_to_select);
            }

        });

    }

    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_FromDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendFromDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "From Date: "+ sendFromDate);

                    //set to date null or empty
                    sendToDate = null;
                    edt_ToDate.setText("");

                    /*if(sendFromDate!=null &sendToDate!=null) {
                        //set filter count if projectID is not selected then 1 otherwise 2

                        //filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId == -1 && sales_person_id > 0 ? 2 : selectedProjectId >= 0 && sales_person_id > 0 ? 3 : 1;
                        //  filterCount_dash = selectedProjectId == -1 && sales_person_id == 0 ? 1 : selectedProjectId >=0  && sales_person_id > 0 ? 3 : 2;
                    }*/


                    //check button EnabledView
                  //  checkButtonEnabled();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_ToDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendToDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendToDate);



                    if(sendFromDate!=null &sendToDate!=null) {

                        showProgressBar("Please wait");
                        callSiteVisitStats();
                    }

                    //check button EnabledView
                   // checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(Helper.getLongNextDateFromString(sendFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
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
