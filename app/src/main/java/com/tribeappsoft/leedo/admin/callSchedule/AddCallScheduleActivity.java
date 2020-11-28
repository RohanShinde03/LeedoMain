package com.tribeappsoft.leedo.admin.callSchedule;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCallScheduleActivity extends AppCompatActivity {

    @BindView(R.id.cl_addCallSchedule) CoordinatorLayout parent;
    @BindView(R.id.ll_addCallSchedule_main) LinearLayoutCompat ll_main;
    @BindView(R.id.mTv_addCallSchedule_salesRepName) MaterialTextView mTv_salesRepName;

    @BindView(R.id.mTv_addCallSchedule_selectCustomer_id) MaterialTextView mTv_selectCustomer_id;
    @BindView(R.id.mTv_addCallSchedule_customerName) MaterialTextView mTv_customerName;
    @BindView(R.id.til_addCallSchedule_callScheduleDate) TextInputLayout til_callScheduleDate;
    @BindView(R.id.edt_addCallSchedule_callScheduleDate) TextInputEditText edt_callScheduleDate;
    @BindView(R.id.til_addCallSchedule_callScheduleTime) TextInputLayout til_callScheduleTime;
    @BindView(R.id.edt_addCallSchedule_callScheduleTime) TextInputEditText edt_callScheduleTime;
    @BindView(R.id.til_addCallSchedule_remarks) TextInputLayout til_remarks;
    @BindView(R.id.edt_addCallSchedule_remarks) TextInputEditText edt_remarks;

    @BindView(R.id.mBtn_addCallSchedule_submit) MaterialButton mBtn_submit;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private AppCompatActivity context;
    private String TAG = "AddCallScheduleActivity", api_token="",customer_name =null, lead_cu_id = null, sendScheduleDate = null, sendCallScheduleTime = null;
    public int user_id=0, lead_id =0,lead_status_id =0, prev_call_schedule_id =0, mYear, mMonth, mDay, selectedHour, selectedMinute, selectedSec;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean fromReschedule = false,isGreaterThanCurrent= false, fromFeed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_call_schedule);
        ButterKnife.bind(this);
        context = AddCallScheduleActivity.this;
        //call method to hide keyBoard
        setupUI(parent);

        if (getIntent() != null) {
            //get cu_id model
            fromFeed = getIntent().getBooleanExtra("fromFeed", false);
            customer_name = getIntent().getStringExtra("customer_name");
            lead_cu_id = getIntent().getStringExtra("lead_cu_id");
            lead_id = getIntent().getIntExtra("lead_id", 0);
            lead_status_id = getIntent().getIntExtra("lead_status_id", 0);
            prev_call_schedule_id = getIntent().getIntExtra("prev_call_schedule_id", 0);
            fromReschedule = getIntent().getBooleanExtra("fromReschedule", false);
        }


        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromReschedule ? getString(R.string.menu_reschedule_call_log) : getString(R.string.menu_schedule_call_log));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //init
        init();

        //hide pb
        hideProgressBar();
    }

    private void init()
    {
        //hide pb
        hideProgressBar();

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        String full_name =sharedPreferences.getString("full_name",getString(R.string.user_name));

        //set sales rep name
        mTv_salesRepName.setText(full_name);

        //From Direct CuId Activity && Feeds
        mTv_selectCustomer_id.setText(lead_cu_id);
        mTv_customerName.setText(customer_name);

        if (fromReschedule) {
            //set reschedule call details

            til_callScheduleDate.setHint(getString(R.string.call_reschedule_date));
            til_callScheduleTime.setHint(getString(R.string.call_reschedule_time));
            til_remarks.setHint(getString(R.string.call_reschedule_remarks));
            mBtn_submit.setText(getString(R.string.add_call_reschedule));
        }
        else {

            //select customer id iff add call schedule only
            mTv_selectCustomer_id.setOnClickListener(v ->{

                //hide keyboard
                Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

                startActivity(new Intent(context, CustomerIdActivity.class)
                        .putExtra("fromSiteVisit_or_token", 4)
                        // to get view all the leads
                        .putExtra("forId", 3));
                finish();
            });
        }


        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        //set current date time
        selectedHour = c.get(Calendar.HOUR_OF_DAY);
        selectedMinute = c.get(Calendar.MINUTE);
        selectedSec = c.get(Calendar.SECOND);

        //set today's date def
        edt_callScheduleDate.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date()));
        //set send date def
        sendScheduleDate = Helper.getDateTime();

        //select call schedule date
        edt_callScheduleDate.setOnClickListener(view -> selectScheduleDate());

        //select call schedule time
        edt_callScheduleTime.setOnClickListener(view -> selectScheduleTime());

        //submit call schedule
        mBtn_submit.setOnClickListener(view -> {
            //check validation
            checkValidations();
        });


        //check Button Enabled View
        checkButtonEnabled();
    }


  /*  private void selectCallScheduleDate()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_callScheduleDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendScheduleDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "Send Schedule  Date: "+ sendScheduleDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }*/

    private void selectScheduleDate() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    mDay =  dayOfMonth;
                    mMonth = monthOfYear;
                    mYear = year;

                    edt_callScheduleDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendScheduleDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "sendScheduleDate Date: "+ sendScheduleDate);

                    //show popup for select time
                    selectScheduleTime();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

    private void selectScheduleTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;
                    boolean isPM = (hourOfDay >= 12);

                    //check if selected date is today's date
                    if (Helper.isToday(Helper.getDatefromString(sendScheduleDate)))
                    {
                        //selected reminder date is today's date

                        //check for the selected time -- should be greater than current time

                        Calendar datetime = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minute);
                        if(datetime.getTimeInMillis() >c.getTimeInMillis()){
                            //it's after current
                            isGreaterThanCurrent=false;
                            edt_callScheduleTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                            sendCallScheduleTime = hourOfDay + ":" + minute + ":" +"00";
                        }else{

                            //it's before current'
                            isGreaterThanCurrent=true;
                            new Helper().showCustomToast(context, "Schedule time should be greater than current time!");
                            edt_callScheduleTime.setText("");
                            sendCallScheduleTime=null;
                        }
                    }
                    else {
                        //selected reminder date is greater than today's date

                        //no need to check for the current time if date is greater than today's date
                        sendCallScheduleTime = hourOfDay + ":" + minute + ":" +"00";
                        edt_callScheduleTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                    }


                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                    if (sendCallScheduleTime!=null) Log.e(TAG, "sendCallScheduleTime: "+sendCallScheduleTime);

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();

    }

    /*private void selectCallScheduleTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;

                    sendCallScheduleTime = hourOfDay + ":" + minute + ":" +"00";
                    boolean isPM = (hourOfDay >= 12);
                    edt_callScheduleTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendCallScheduleTime!=null) Log.e(TAG, "Call Schedule Time: "+sendCallScheduleTime);

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }*/


    private void checkValidations()
    {
        //customer lead id
        if (lead_cu_id ==  null) new Helper().showCustomToast(context, "Please select the customer lead id!");
            //customer name
        else if (customer_name.trim().isEmpty())  new Helper().showCustomToast(this, "Please select Customer Name!");
            //call schedule date
        else if (sendScheduleDate ==  null) new Helper().showCustomToast(context, fromReschedule ? "Please select call Reschedule date!" : "Please select call schedule date!" );
            // call schedule time
        else if (sendCallScheduleTime ==  null) new Helper().showCustomToast(context, fromReschedule ? "Please select call Reschedule time!" : "Please select call schedule time!");

        else if (isGreaterThanCurrent) new Helper().showCustomToast(context, "Please select time less than current time!");
        else {

            //check add call schedule api here
            if (Helper.isNetworkAvailable(context)) {

                if (fromReschedule) {
                    //call reschedule api here
                    showProgressBar(getString(R.string.rescheduling_call_log));
                    callREScheduleCallLog();
                }
                else {
                    // call add call schedule api here
                    showProgressBar(getString(R.string.adding_call_log));
                    callScheduleCallLog();
                }
            }else Helper.NetworkError(context);
        }
    }

    private void checkButtonEnabled()
    {
        //customer lead id
        if (lead_cu_id ==  null) setButtonDisabledView();
            //customer name
        else if (customer_name.trim().isEmpty()) setButtonDisabledView();
            //call schedule date
        else if (sendScheduleDate ==  null) setButtonDisabledView();
            // call schedule time
        else if (sendCallScheduleTime ==  null) setButtonDisabledView();
        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView() {
        // All validations are checked
        // enable btn for submit lead
        mBtn_submit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submit.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void setButtonDisabledView() {
        // All validations are not checked
        // disable btn for submit lead
        mBtn_submit.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submit.setTextColor(getResources().getColor(R.color.main_white));
    }


    private void callScheduleCallLog()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("call_schedule_date", sendScheduleDate!=null ? sendScheduleDate : "");
        jsonObject.addProperty("call_schedule_time", sendCallScheduleTime);
        jsonObject.addProperty("lead_status_id", lead_status_id);
        jsonObject.addProperty("lead_uid", lead_cu_id);
        jsonObject.addProperty("call_schedule_remarks", Objects.requireNonNull(edt_remarks.getText()).toString());
        jsonObject.addProperty("call_schedule_by_user_id", user_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addCallSchedule(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()  && response.body().isJsonObject()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //setJson(response.body());
                                onAddCallLogSuccess();
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }



    private void callREScheduleCallLog()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("call_schedule_date", sendScheduleDate!=null ? sendScheduleDate : "");
        jsonObject.addProperty("call_schedule_time", sendCallScheduleTime);
        jsonObject.addProperty("lead_status_id", lead_status_id);
        jsonObject.addProperty("lead_uid", lead_cu_id);
        jsonObject.addProperty("call_schedule_remarks", Objects.requireNonNull(edt_remarks.getText()).toString());
        jsonObject.addProperty("call_schedule_by_user_id", user_id);
        jsonObject.addProperty("prev_call_schedule_id", prev_call_schedule_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addCallRESchedule(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()  && response.body().isJsonObject()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //setJson(response.body());
                                onAddCallLogSuccess();
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void onAddCallLogSuccess()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show success toast
            new Helper().showSuccessCustomToast(context, fromReschedule ? "Call log ReScheduled successfully!" : "Call log Scheduled successfully!");

            //set Feed Action Added to true
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("callScheduleAdded", true);
                editor.putInt("callScheduled",1);
                editor.apply();
            }

            if (fromFeed) {

                //added call schedule from feed then go to All Call Logs Screen
                startActivity(new Intent(context, CallScheduleMainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //finish this activity
                finish();
            }
            else {
                //do back press
                onBackPressed();
            }
        });
    }

    private void showErrorLog(final String message) {
        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });
    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
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
        //if (isVisitSubmitted) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Site Visit Added"));
        super.onBackPressed();
    }

}
