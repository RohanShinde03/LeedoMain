package com.tribeappsoft.leedo.admin.callLog;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reminder.AlarmReceiver;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.CallStatusModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.formatTimeFromString;
import static com.tribeappsoft.leedo.util.Helper.getDateTime24HrsFormat;
import static com.tribeappsoft.leedo.util.Helper.getDatefromString;
import static com.tribeappsoft.leedo.util.Helper.getTimeFromDateTimeString;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.isToday;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;
import static com.tribeappsoft.leedo.util.Helper.setDatePickerFormatDateFromString;


public class CallLogActivity extends AppCompatActivity {

    //@BindView(R.id.ll_callLog_main) LinearLayoutCompat ll_main;
    @BindView(R.id.edt_callLog_dateTime) TextInputEditText edt_dateTime;
    @BindView(R.id.acTv_callLog_callStatus) AutoCompleteTextView acTv_callStatus;
    @BindView(R.id.edt_callLog_callDetails) TextInputEditText edt_callDetails;
    @BindView(R.id.edt_callLog_startTime) TextInputEditText edt_startTime;
    @BindView(R.id.edt_callLog_endTime) TextInputEditText edt_endTime;

    @BindView(R.id.edt_addCallSchedule_callScheduleDate) TextInputEditText edt_callScheduleDate;
    @BindView(R.id.edt_addCallSchedule_callScheduleTime) TextInputEditText edt_callScheduleTime;
    @BindView(R.id.edt_addCallSchedule_remarks) TextInputEditText edt_remarks;

    //set reminder
    @BindView(R.id.sm_callLog_setScheduleCall) SwitchMaterial sm_setScheduleCall;
    @BindView(R.id.ll_callLog_viewScheduleCallData) LinearLayoutCompat ll_viewScheduleCallData;

    @BindView(R.id.sm_callLog_setReminder) SwitchMaterial sm_setReminder;
    @BindView(R.id.ll_callLog_setReminderMain) LinearLayoutCompat ll_setReminderMain;
    @BindView(R.id.ll_callLog_viewReminderData) LinearLayoutCompat ll_viewReminderData;
    @BindView(R.id.edt_callLog_reminder_date) TextInputEditText edt_reminder_date;
    @BindView(R.id.edt_callLog_reminder_time) TextInputEditText edt_reminder_time;
    @BindView(R.id.edt_callLog_reminderText) TextInputEditText edt_reminderText;

    //pb
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    //mBtn add call Log
    @BindView(R.id.mBtn_callLog_addCallLog) MaterialButton mBtn_addCallLog;

    private AppCompatActivity context;
    private String TAG ="CallLogActivity", api_token ="", lead_cu_id = "", lead_name = "", lead_project_name = "", sendLogDateTime= null, selectedCallStatus ="",
            sendCallStartTime = null, sendCallEndTime = null, sendReminderDate = null, sendReminderTime = null,sendScheduleDate = null, sendCallScheduleTime = null,
            call_start_time = null, call_end_time = null, record_file_path = null;

    private int user_id =0, selectedCallStatusId =0, lead_id =0, lead_status_id =0, call_schedule_id =0;
    private int mYear, mMonth, mDay, selectedDay, selectedMonth, selectedYear, selectedHour, selectedMinute, selectedSec ;
    private ArrayList<CallStatusModel> callStatusModelArrayList;
    private ArrayList<String> statusArrayList;
    private boolean isSetReminder = false,isSetScheduleCall = false, fromPhoneCall = false,isGreaterThanCurrent= false;
    private final static int RQS_1 = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isSuccessSubmit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= CallLogActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.add_call_log));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null){
            //came from outgoing phone call
            fromPhoneCall =  getIntent().getBooleanExtra("fromPhoneCall",false);
            //callCompletedAdded =  getIntent().getBooleanExtra("callCompletedAdded",false);
            lead_id =  getIntent().getIntExtra("call_lead_id",0);
            lead_status_id =  getIntent().getIntExtra("lead_status_id",0);
            call_schedule_id =  getIntent().getIntExtra("call_schedule_id",0);
            lead_cu_id =  getIntent().getStringExtra("cu_id");
            lead_name =  getIntent().getStringExtra("lead_name");
            lead_project_name =  getIntent().getStringExtra("project_name");
            call_start_time =  getIntent().getStringExtra("call_start_time");
            call_end_time =  getIntent().getStringExtra("call_end_time");
            record_file_path =  getIntent().getStringExtra("record_file_path");

            Log.e(TAG, "onCreate: project name :"+lead_project_name+" call_schedule_id : "+call_schedule_id+"lead_id : "+lead_id+"lead_status_id : "+lead_status_id+"lead_cu_id : "+lead_cu_id );
        }

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        //set current date time
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH);
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedHour = c.get(Calendar.HOUR_OF_DAY);
        selectedMinute = c.get(Calendar.MINUTE);
        selectedSec = c.get(Calendar.SECOND);

        if (!fromPhoneCall){
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            sendCallStartTime=currentTime;
            edt_startTime.setText(currentTime);
            edt_dateTime.setOnClickListener(view -> selectScheduleDate());
        }
        //init
        init();

        //toggle View
        toggleView();

        //TODO stop getting data from api
        // call get lead data
        //if (isNetworkAvailable(Objects.requireNonNull(context))) {
        //    showProgressBar("Please wait...");
         //   getCallStatus();
        // }
        // else NetworkError(context);
        //TODO add data from local
        addStaticData();

        //select reminder date
        edt_reminder_date.setOnClickListener(view -> selectReminderDate());
        //select reminder time
        edt_reminder_time.setOnClickListener(view -> selectReminderTime());

        //select call schedule date
        edt_callScheduleDate.setOnClickListener(view -> selectCallScheduleDate());

        //select call schedule time
        edt_callScheduleTime.setOnClickListener(view -> selectCallScheduleTime());

        //add call log
        mBtn_addCallLog.setOnClickListener(view -> {
            //hide keyboard if opened
            hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidations();
        });

        //set the local data
        new Handler().postDelayed(this::setCallLogDetails, 100);
    }

    private void init()
    {
        //hidden pb
        hideProgressBar();

        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //initialise arrayList
        callStatusModelArrayList = new ArrayList<>();
        statusArrayList = new ArrayList<>();


        //set enabled if coming from manually add call log or disabled iff came from outgoing phone call
        edt_startTime.setEnabled(!fromPhoneCall);
        edt_startTime.setClickable(!fromPhoneCall);

        edt_endTime.setEnabled(!fromPhoneCall);
        edt_endTime.setClickable(!fromPhoneCall);

        /*if(fromPhoneCall) {
            //coming from outgoing call log
            edt_startTime.setEnabled(false);
            edt_startTime.setClickable(false);

            edt_endTime.setEnabled(false);
            edt_endTime.setClickable(false);
        }*/

        //select call start time
        edt_startTime.setOnClickListener(view -> {
            //select date time only if manually adding call log
            if (!fromPhoneCall) selectCallStartTime();
        });
        //select call end time
        edt_endTime.setOnClickListener(view -> {
            //select date time only if manually adding call log
            if (!fromPhoneCall) selectCallEndTime();
        });

        //set today's date time
        edt_dateTime.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date()));

        //set send date Time
       // sendLogDateTime = getDateTime();
        sendLogDateTime = getDateTime24HrsFormat();
    }


    private void addStaticData() {

        //clear list
        callStatusModelArrayList.clear();
        statusArrayList.clear();

        //add call status
        callStatusModelArrayList.add(new CallStatusModel(1, "Call Completed"));
        callStatusModelArrayList.add(new CallStatusModel(2, "Call Not Received"));
        callStatusModelArrayList.add(new CallStatusModel(3, "Busy Call again"));
        callStatusModelArrayList.add(new CallStatusModel(4, "Not Reachable"));

        //add static string data
        statusArrayList.add("Call Completed");
        statusArrayList.add("Call Not Received");
        statusArrayList.add("Busy Call again");
        statusArrayList.add("Not Reachable");
    }

    private void selectScheduleDate() {


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    mDay =  dayOfMonth;
                    mMonth = monthOfYear;
                    mYear = year;

                    edt_dateTime.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendLogDateTime = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    //    Log.e(TAG, "sendScheduleDate Date: "+ sendScheduleDate);

                }, mYear, mMonth, mDay);

        //datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }


    @Override
    protected void onResume() {

        super.onResume();

        /*Log.e(TAG, "onResume: "+isSuccessSubmit );
        if(isSuccessSubmit)
        {
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }*/
    }


    private void toggleView()
    {
        //lead referenced by
        sm_setReminder.setOnCheckedChangeListener((compoundButton, b) -> {

            isSetReminder = b;
            if (b)  //checked
            {
                //do expand view
                //new Animations().toggleRotate(iv_refLead_ec, true);
                expandSubView(ll_viewReminderData);
                //viewRefLead = true;
            }
            else {

                // //do collapse View
                //new Animations().toggleRotate(iv_refLead_ec, true);
                collapse(ll_viewReminderData);
                //viewRefLead = false;
            }

            checkButtonEnabled();
        });

        sm_setScheduleCall.setOnCheckedChangeListener((compoundButton, b) -> {

            isSetScheduleCall = b;
            if (b)  //checked
            {
                //do expand view
                //new Animations().toggleRotate(iv_refLead_ec, true);
                expandSubView(ll_viewScheduleCallData);
                //viewRefLead = true;
            }
            else {

                // //do collapse View
                //new Animations().toggleRotate(iv_refLead_ec, true);
                collapse(ll_viewScheduleCallData);
                //viewRefLead = false;
            }

            checkButtonEnabled();
        });

        edt_callDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //check button EnabledView
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_reminderText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //check button EnabledView
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_remarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //check button EnabledView
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void getCallStatus()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getCallStatus(api_token, user_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {

                                setJson(response.body());
                                new Handler().postDelayed(() -> setCallLogDetails(), 1000);
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
        if (jsonObject.has("data"))
        {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray())
            {
                //JsonObject jsonObject1 = jsonObject.get("data").getAsJsonObject();
                //setJson(jsonObject1);
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                //clear list
                callStatusModelArrayList.clear();
                statusArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setCallStatusJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }

    private void setCallStatusJson(JsonObject jsonObject)
    {
        CallStatusModel model = new CallStatusModel();
        if (jsonObject.has("call_status_id")) model.setCall_status_id(!jsonObject.get("call_status_id").isJsonNull() ? jsonObject.get("call_status_id").getAsInt() : 0 );
        if (jsonObject.has("call_status"))
        {
            model.setCall_status(!jsonObject.get("call_status").isJsonNull() ? jsonObject.get("call_status").getAsString() : "" );
            statusArrayList.add(!jsonObject.get("call_status").isJsonNull() ? jsonObject.get("call_status").getAsString() : "" );
        }
        callStatusModelArrayList.add(model);
    }


    private void setCallLogDetails()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            if (fromPhoneCall)
            {
                //intent came form direct phone call
                //set call status id 1 ==> call completed

                //set call status
                if (callStatusModelArrayList!=null&& callStatusModelArrayList.size()>0) {
                    selectedCallStatusId =1;
                    acTv_callStatus.setText(callStatusModelArrayList.get(0).getCall_status());
                    selectedCallStatus = callStatusModelArrayList.get(0).getCall_status();
                }

                //set start time if not null
                if (call_start_time!=null && !call_start_time.trim().isEmpty()) {
                    edt_startTime.setText(formatTimeFromString(call_start_time));
                    //set send start time
                    sendCallStartTime = getTimeFromDateTimeString(call_start_time);
                    Log.e(TAG, "setCallLogDetails: sendCallStartTime "+sendCallStartTime );
                }

                //set end time if not null
                if (call_end_time!=null && !call_end_time.trim().isEmpty()) {
                    edt_endTime.setText(formatTimeFromString(call_end_time));
                    //set send end time
                    sendCallEndTime = getTimeFromDateTimeString(call_end_time);
                    Log.e(TAG, "setCallLogDetails: sendCallEndTime "+sendCallEndTime );
                }

                //print recording file path
                Log.e(TAG, "setCallLogDetails: record_file_path "+record_file_path);


          /*      //get lead id and cuId from shared pref
                if (sharedPreferences!=null) {

                    editor = sharedPreferences.edit();
                    lead_cu_id = sharedPreferences.getString("cu_id", "");
                    lead_id = sharedPreferences.getInt("call_lead_id", 0);
                    editor.apply();
                    Log.e(TAG, "sharedPreferences: lead_id "+lead_id+"lead_cu_id:"+lead_cu_id);
                }*/

            }

            //set adapter for call status
            setAdapterCallStatus();

            //check button Enabled
            checkButtonEnabled();

        });
    }

    private void setAdapterCallStatus()
    {

        if (statusArrayList.size() >0 &&  callStatusModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, statusArrayList);
            acTv_callStatus.setAdapter(adapter);
            acTv_callStatus.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_callStatus.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                for (CallStatusModel pojo : callStatusModelArrayList)
                {
                    if (pojo.getCall_status().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedCallStatusId = pojo.getCall_status_id(); // This is the correct ID
                        selectedCallStatus = pojo.getCall_status();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Call status & id " + selectedCallStatus +"\t"+ selectedCallStatusId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });

        }
    }

    private void selectReminderDate() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    selectedDay =  dayOfMonth;
                    selectedMonth = monthOfYear;
                    selectedYear = year;

                    edt_reminder_date.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendReminderDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "Reminder _send Date: "+ sendReminderDate);

                    //show popup for select time
                    selectReminderTime();

                    //check button EnabledView
                    checkButtonEnabled();


                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }


    private void selectCallStartTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;

                    sendCallStartTime = hourOfDay + ":" + minute + ":" +"00";
                    boolean isPM = (hourOfDay >= 12);
                    edt_startTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendCallStartTime!=null) Log.e(TAG, "Call Start Time: "+sendCallStartTime);

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }


    private void selectCallEndTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;

                    sendCallEndTime = hourOfDay + ":" + minute + ":" +"00";
                    boolean isPM = (hourOfDay >= 12);
                    edt_endTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendCallEndTime!=null) Log.e(TAG, "Call End Time: "+sendCallEndTime);

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    private void selectReminderTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;

                    sendReminderTime = hourOfDay + ":" + minute + ":" +"00";
                    boolean isPM = (hourOfDay >= 12);
                    edt_reminder_time.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "pm" : "am"));
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendReminderTime!=null) Log.e(TAG, "Reminder Time: "+sendReminderTime);

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    private void selectCallScheduleDate() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    mDay =  dayOfMonth;
                    mMonth = monthOfYear;
                    mYear = year;

                    edt_callScheduleDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendScheduleDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "sendScheduleDate Date: "+ sendScheduleDate);

                    //show popup for select time
                    selectCallScheduleTime();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

    private void selectCallScheduleTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;
                    boolean isPM = (hourOfDay >= 12);

                    //check if selected date is today's date
                    if (isToday(getDatefromString(sendScheduleDate)))
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

    private void checkValidations()
    {
        //call log date time
        if (sendLogDateTime==null) new Helper().showCustomToast(context, "Please select call log date time!");
            //call status id
        else if (selectedCallStatusId==0) new Helper().showCustomToast(context, "Please select call status!");
            //check call start time
        else if (sendCallStartTime==null) new Helper().showCustomToast(context, "Please select call start time!");
            //check call end time
        else if (sendCallEndTime==null) new Helper().showCustomToast(context, "Please select call end time!");
            //call remarks
        else if (Objects.requireNonNull(edt_callDetails.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter call remarks!");

        //check schedule call date
        else if (isSetScheduleCall && sendScheduleDate==null) new Helper().showCustomToast(context, "Please select call schedule date!");
            //check schedule call time
        else if (isSetScheduleCall && sendCallScheduleTime==null) new Helper().showCustomToast(context, "Please select call schedule time!");

        else if (isSetScheduleCall && isGreaterThanCurrent)new Helper().showCustomToast(context, "Please select time less than current time!");

            //check reminder date
        else if (isSetReminder && sendReminderDate==null) new Helper().showCustomToast(context, "Please select reminder date!");
            //check reminder time
        else if (isSetReminder && sendReminderTime==null) new Helper().showCustomToast(context, "Please select reminder time!");
            //reminder text
            //else if (isSetReminder && Objects.requireNonNull(edt_reminderText.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter reminder about!");
            //else show confirmation dialog
        else {

            //call add call log api here
            if (isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.adding_call_log));
                if (record_file_path!=null) {
                    //call multi-part api upload
                    callAddCallLogMultiPart();
                }
                else {
                    //call simple add call log
                    callAddCallLog();
                }

            }else NetworkError(context);
        }

    }

    private void checkButtonEnabled()
    {

        if (sendLogDateTime==null) setButtonDisabledView();
            //call status id
        else if (selectedCallStatusId==0) setButtonDisabledView();
            //check call start time
        else if (sendCallStartTime==null) setButtonDisabledView();
            //check call end time
        else if (sendCallEndTime==null) setButtonDisabledView();
            //call remarks
        else if (Objects.requireNonNull(edt_callDetails.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //check schedule call date
        else if (isSetScheduleCall && sendScheduleDate==null) setButtonDisabledView();

            //check schedule call time
        else if (isSetScheduleCall && sendCallScheduleTime==null) setButtonDisabledView();

            //check reminder date
        else if (isSetReminder && sendReminderDate==null) setButtonDisabledView();
            //check reminder time
        else if (isSetReminder && sendReminderTime==null) setButtonDisabledView();
            //reminder text
            //else if (isSetReminder && Objects.requireNonNull(edt_reminderText.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //else show confirmation dialog
        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView()
    {
        // All validations are checked
        // enable btn for submit lead
        mBtn_addCallLog.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_addCallLog.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void setButtonDisabledView()
    {
        // All validations are not checked
        // disable btn for submit lead
        mBtn_addCallLog.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_addCallLog.setTextColor(getResources().getColor(R.color.main_white));
    }


    private void callAddCallLog()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("call_log_status_id", selectedCallStatusId);
        jsonObject.addProperty("call_log_date", sendLogDateTime!=null ? sendLogDateTime : "");
        jsonObject.addProperty("start_time", sendCallStartTime);
        jsonObject.addProperty("end_time", sendCallEndTime);
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("lead_status_id", lead_status_id);
        jsonObject.addProperty("call_schedule_id", call_schedule_id);
        jsonObject.addProperty("call_remarks", Objects.requireNonNull(edt_callDetails.getText()).toString());
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("api_token", api_token);

        jsonObject.addProperty("call_schedule_date", sendScheduleDate!=null ? sendScheduleDate : "");
        jsonObject.addProperty("call_schedule_time", sendCallScheduleTime);
        jsonObject.addProperty("lead_uid", lead_cu_id);
        jsonObject.addProperty("call_schedule_remarks", Objects.requireNonNull(edt_remarks.getText()).toString());
        jsonObject.addProperty("call_schedule_by_user_id", user_id);
        jsonObject.addProperty("is_schedule", isSetScheduleCall ? 1 : 0);


        //send reminder fields if user is adding reminder
        jsonObject.addProperty("remind_at", isSetReminder ? sendReminderDate + " " + sendReminderTime : "");
        jsonObject.addProperty("reminder_comments", isSetReminder ? "Had a call with customer "+lead_name +"(" +lead_cu_id+ ") belongs to project "+ lead_project_name + ", set reminder about -- " + Objects.requireNonNull(edt_callDetails.getText()).toString() : "");
        jsonObject.addProperty("is_reminder", isSetReminder ?  1 : 0);
        jsonObject.addProperty("is_done", 0);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addCallLog(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()  && response.body().isJsonObject()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //setJson(response.body());
                                onAddCallLogSuccess();
                            }
                            else showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogAddCallLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogAddCallLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogAddCallLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogAddCallLog(getString(R.string.weak_connection));
                else showErrorLogAddCallLog(e.toString());
            }
        });
    }


    private void callAddCallLogMultiPart()
    {
       /* MultipartBody.Part fileUpload=null;
        File file_path_part = null;
        if(record_file_path!=null && !record_file_path.trim().isEmpty()) {

            file_path_part = new File(record_file_path);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), file_path_part);
            fileUpload = MultipartBody.Part.createFormData("recording_file_uri", file_path_part.getName(), uploadFile);
        }*/

        //RequestBody api_tokenPart = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody call_status_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedCallStatusId));
        RequestBody call_log_purpose_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));
        RequestBody call_log_date = RequestBody.create(MediaType.parse("text/plain"), sendLogDateTime);
        RequestBody start_time = RequestBody.create(MediaType.parse("text/plain"), sendCallStartTime);
        RequestBody end_time = RequestBody.create(MediaType.parse("text/plain"), sendCallEndTime);
        RequestBody lead_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_id));
        RequestBody lead_status_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_status_id));
        RequestBody cu_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_cu_id));
        RequestBody call_remarks = RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNull(edt_callDetails.getText()).toString());
        RequestBody sales_person_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user_id));
        RequestBody call_schedule_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(call_schedule_id));
       // RequestBody media_file_size_part = RequestBody.create(MediaType.parse("text/plain"), new Helper().getFileSizeKiloBytes(file_path_part));
        RequestBody media_type_id_part = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(3));
        RequestBody api_token_part = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody remind_at = RequestBody.create(MediaType.parse("text/plain"), isSetReminder ? sendReminderDate + " " + sendReminderTime : "");
        RequestBody reminder_comments = RequestBody.create(MediaType.parse("text/plain"), isSetReminder ? "Had a call with customer "+lead_name +"(" +lead_cu_id+ ") belongs to project "+ lead_project_name + ", set reminder about -- " +  Objects.requireNonNull(edt_callDetails.getText()).toString() : "");
        RequestBody is_reminder = RequestBody.create(MediaType.parse("text/plain"), isSetReminder ? "1" : "0");
        RequestBody is_done = RequestBody.create(MediaType.parse("text/plain"), "0");

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addCallLog( call_status_id, call_log_date, start_time, end_time,
                lead_id_part, lead_status_id_part,  call_remarks, sales_person_id,call_schedule_id_part,
                api_token_part, remind_at, reminder_comments,  is_reminder, is_done ).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()  && response.body().isJsonObject()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //setJson(response.body());
                                onAddCallLogSuccess();
                            }
                            else showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogAddCallLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogAddCallLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogAddCallLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogAddCallLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogAddCallLog(getString(R.string.weak_connection));
                else showErrorLogAddCallLog(e.toString());
            }
        });
    }

    private void onAddCallLogSuccess()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            isSuccessSubmit=true;
            //set local alarm if reminder added
            if (isSetReminder) setLocalAlarm();

            //show success toast
            new Helper().showSuccessCustomToast(context, "Call log added successfully!");

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("callCompletedAdded", true);

                if (fromPhoneCall) {
                    //remove lead id and cuId after adding call log

                    editor.remove("call_lead_id");
                    editor.remove("lead_status_id");
                    editor.remove("call_schedule_id");
                    editor.remove("cu_id");
                    editor.remove("from_make_phone_Call");
                }
                editor.apply();

                //Log.e(TAG, "onAddCallLogSuccess: call_schedule_id : "+call_schedule_id+"lead_id : "+lead_id+"lead_status_id : "+lead_status_id );
                Log.e(TAG, "onAddCallLogSuccess: removedFrom SharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                        + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                        + "\n\t from_make_phone_Call "+ sharedPreferences.getBoolean("from_make_phone_Call", false)
                );

            }


            // do back press after adding call log
            //do back press
            new Handler().postDelayed(this::onBackPressed, 2000);
        });
    }


    private void setLocalAlarm()
    {
        Calendar calNow = Calendar.getInstance();
        //Calendar calSet = (Calendar) calNow.clone();

        calNow.set(Calendar.YEAR, selectedYear);
        calNow.set(Calendar.MONTH, selectedMonth);
        calNow.set(Calendar.DAY_OF_MONTH, selectedDay);

        calNow.set(Calendar.HOUR_OF_DAY, selectedHour);
        calNow.set(Calendar.MINUTE, selectedMinute);
        calNow.set(Calendar.SECOND, selectedSec);
        calNow.set(Calendar.MILLISECOND, 0);
        // Today Set time passed, count to tomorrow
        //calNow.add(Calendar.DATE, 1);

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("reminderText",  Objects.requireNonNull(edt_reminderText.getText()).toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, calNow.getTimeInMillis(),
                pendingIntent);

    }



    private void showErrorLog(final String message) {
        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context, message);
        });
    }

    private void showErrorLogAddCallLog(final String message) {
        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context, message);

            //make phone call false
            isSuccessSubmit=true;

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("callCompletedAdded", true);
                //remove lead id and cuId after adding call log
                editor.remove("call_lead_id");
                editor.remove("lead_status_id");
                editor.remove("call_schedule_id");
                editor.remove("cu_id");
                editor.remove("from_make_phone_Call");
                editor.apply();

                Log.e(TAG, "onAddCallLogSuccess: removedFrom SharedPref lead_id "+sharedPreferences.getInt("call_lead_id", 0)
                        + "\n\t call_schedule_id "+sharedPreferences.getInt("call_schedule_id", 0)
                        + "\n\t from_make_phone_Call "+ sharedPreferences.getBoolean("from_make_phone_Call", false)
                );
            }

        });
    }



    private void expandSubView(final View v)
    {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime==1) v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
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



    private void collapse(final View v)
    {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation)
            {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

            //remove id's from shared pref
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                if (fromPhoneCall) {
                    //remove lead id and cuId after adding call log
                    editor.remove("cu_id");
                    editor.remove("call_lead_id");
                    editor.remove("lead_status_id");
                    editor.remove("call_lead_status_id");
                    editor.remove("call_schedule_id");
                    editor.remove("from_make_phone_Call");
                }
                editor.apply();
            }

            //on backPressed
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }


    @Override
    public void onBackPressed() {

//        if (fromPhoneCall) {
//            if(isSuccessSubmit) {
//                super.onBackPressed();
//                overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
//            }
//            else new Helper().showCustomToast(context, "Please submit the call details!");
//        }
//        else {
//            super.onBackPressed();
//
//            fromPhoneCall=false;
//            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
//
//        }

        super.onBackPressed();
        fromPhoneCall=false;
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
