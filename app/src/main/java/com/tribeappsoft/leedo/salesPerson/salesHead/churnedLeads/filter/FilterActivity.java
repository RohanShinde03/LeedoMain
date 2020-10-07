package com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads.filter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
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
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
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

public class FilterActivity extends AppCompatActivity {


    @BindView(R.id.cl_filter) CoordinatorLayout parent;
    @BindView(R.id.ll_filter_main) LinearLayoutCompat ll_main;

    @BindView(R.id.acTv_filter_selectProject) AutoCompleteTextView acTv_selectProject;
    @BindView(R.id.edt_filter_fromDate) TextInputEditText edt_fromDate;
    @BindView(R.id.edt_filter_toDate) TextInputEditText edt_toDate;
    @BindView(R.id.mBtn_filter_clearFilter) MaterialButton mBtn_clearFilter;
    @BindView(R.id.mBtn_filter_applyFilter) MaterialButton mBtn_applyFilter;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private AppCompatActivity context;
    private DatePickerDialog datePickerDialog;
    private ArrayList<String> projectStringArrayList;
    private ArrayList<ProjectModel> itemArrayList;
    private int selectedProjectId=-1, mYear, mMonth, mDay;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "FilterActivity",api_token = "", selectedProjectName="", sendFromDate = null, sendToDate = null;
    private int filterCount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);
        context= FilterActivity.this;

        //call method to hide keyBoard
        setupUI(parent);


        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_filter_leads_by));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
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
        //user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        itemArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        //select from date
        edt_fromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        //select to date
        edt_toDate.setOnClickListener(view -> {
            if (sendFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(context, "Please select from date first!");
        });

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar(getString(R.string.please_wait));
            call_getAllProjects();
        }
        else {
            Helper.NetworkError(context);
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
                        filterCount = sharedPreferences.getInt("filterCount", 0);
                        sendFromDate = sharedPreferences.getString("sendFromDate",null);
                        sendToDate = sharedPreferences.getString("sendToDate", null);

                        //set project name
                        if (selectedProjectId!=-1 && itemArrayList!=null&& itemArrayList.size()>0) {
                            acTv_selectProject.setText(itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name());
                            selectedProjectName = itemArrayList.get(getIndexOfList(itemArrayList, selectedProjectId)).getProject_name();
                        }

                        //set from date if not null
                        if (sendFromDate!=null && !sendFromDate.trim().isEmpty()) {
                            edt_fromDate.setText(Helper.formatDateFromString(sendFromDate));
                        }

                        //set to date if not null
                        if (sendToDate!=null && !sendToDate.trim().isEmpty()) {
                            edt_toDate.setText(Helper.formatDateFromString(sendToDate));
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
                        filterCount= sendFromDate==null && sendToDate==null ? 1 :2;

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
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_fromDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
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

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_toDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendToDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendToDate);

                    if(sendFromDate!=null &sendToDate!=null) {
                        //set filter count if projectID is not selected then 1 otherwise 2
                        filterCount= selectedProjectId==-1 ? 1 :2;
                    }
                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(Helper.getLongNextDateFromString(sendFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }


    private void checkValidations()
    {

        //project
        if (selectedProjectId==-1  && sendFromDate ==null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select at least one filter type!");

            //check if only from date selected
        else if (sendFromDate !=null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select To-Date!");

        else {

            //apply filter

            if (sharedPreferences!=null) {

                editor = sharedPreferences.edit();
                editor.putInt("project_id",  selectedProjectId == -1 ? 0 : selectedProjectId);
                editor.putString("sendFromDate", sendFromDate);
                editor.putString("sendToDate", sendToDate);
                editor.putInt("filterCount", filterCount);
                editor.putBoolean("isFilter", true);
                editor.apply();

                //do onBackPressed
                onBackPressed();
            }
        }

    }


    private void checkButtonEnabled()
    {

        //project id and dates are null
        if (selectedProjectId==-1  && sendFromDate ==null && sendToDate == null) setButtonDisabledView();

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

        if (selectedProjectId==-1  && sendFromDate ==null && sendToDate == null) {
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

            //clear dates filter
            sendFromDate =  sendToDate = null;
            edt_fromDate.setText("");
            edt_toDate.setText("");


            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("isFilter");
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



    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                Helper.onErrorSnack(context,message);

            });
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(context, view);
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
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
