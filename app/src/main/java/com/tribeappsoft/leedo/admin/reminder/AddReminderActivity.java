package com.tribeappsoft.leedo.admin.reminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReminderActivity extends AppCompatActivity {

    @BindView(R.id.edt_reminderText) TextInputEditText edt_reminderText;
    @BindView(R.id.edt_reminder_date) TextInputEditText edt_reminder_date;
    @BindView(R.id.edt_reminder_time) TextInputEditText edt_reminder_time;
    @BindView(R.id.mbtn_add_reminders) MaterialButton mBtn_add_reminder;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private String sendReminderDate = null, sendReminderTime = null,  api_token = "", reminder_name = "", lead_name = "",
            updateDateTime = null,remind_at_time_format="",remind_at_time_format1="",remind_at_date_format="";
    private int fromOther = 1, user_id = 0, reminderId = 0, lead_id =0 ;
    //TODO fromOther ==> 1 - Add Reminder, 2- Edit/Update , 3 - from Lead/Feed
    private AppCompatActivity context;
    private String TAG = "AddReminderActivity";
    private int mYear, mMonth, mDay, selectedDay, selectedMonth, selectedYear, selectedHour, selectedMinute, selectedSec ;
    private boolean fromShortcut = false, isReminderAdded= false, isGreaterThanCurrent= false;
    private final static int RQS_1 = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final int Permission_CODE_CALENDAR = 311;
    //final int callbackId = 42;


    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
     //   overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = AddReminderActivity.this;

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //get Intent
        if (getIntent()!=null)
        {
            fromShortcut = getIntent().getBooleanExtra("fromShortcut", false);
            fromOther = getIntent().getIntExtra("fromOther", 1);

            //data from update reminder
            reminder_name = getIntent().getStringExtra("reminder_name");
            remind_at_date_format = getIntent().getStringExtra("remind_at_date_format");
            //remind_at_time_format_temp = getIntent().getStringExtra("remind_at_time_format_temp");
            remind_at_time_format = getIntent().getStringExtra("remind_at_time_format");
            remind_at_time_format1 = getIntent().getStringExtra("remind_at_time_format1");
            updateDateTime = getIntent().getStringExtra("remind_at");
            //sendReminderTime = getIntent().getStringExtra("reminder_time");
            reminderId = getIntent().getIntExtra("reminder_id", 0);

            //data from feeds
            lead_name = getIntent().getStringExtra("lead_name");
            //cu_id = getIntent().getStringExtra("cu_id");
            //project_name = getIntent().getStringExtra("project_name");
            lead_id = getIntent().getIntExtra("lead_id", 0);
        }


        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_add_reminders));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
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

        //select reminder date
        edt_reminder_date.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select date
            selectReminderDate();
        });
        //select reminder time
        edt_reminder_time.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select time
            selectReminderTime();
        });

        if (fromOther==2) {

            if(getSupportActionBar()!= null){
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.updateReminder));
            }

            //update reminder
            if (reminder_name!=null) edt_reminderText.setText(reminder_name);
            if(remind_at_time_format1 !=null){
                if(!remind_at_time_format1.trim().isEmpty())
                {
                    if (remind_at_time_format!=null) edt_reminder_time.setText(Helper.formatTime(remind_at_time_format));
                    sendReminderTime=remind_at_time_format1;
                }//else  new Helper().showCustomToast(context, "Empty time!");
            }

            if(remind_at_date_format !=null){
                if (remind_at_date_format!=null)
                {
                    edt_reminder_date.setText(Helper.formatDateFromString(remind_at_date_format));
                    sendReminderDate=remind_at_date_format;
                }//else  new Helper().showCustomToast(context, "Empty date!");

            }



            Log.e(TAG, "onCreate date time: "+remind_at_date_format+""+ remind_at_time_format1);

            if (updateDateTime!=null)
            {
                //edt_reminder_date.setText(formatDateFromString(updateDateTime));
                //edt_reminder_time.setText(formatTimeFromString(updateDateTime));
                //sendReminderDate = getDateFromDateTimeString(updateDateTime);
                //sendReminderTime = getTimeFromDateTimeString(updateDateTime);
                Log.e(TAG, "onCreate: dt & time "+ updateDateTime + " "+ updateDateTime);
            }
        }
        else if (fromOther ==3) {
            //from feeds
            edt_reminderText.setText("Remind me about "+lead_name);
        }

        //set request focus to editText
        edt_reminderText.requestFocus();

        //check validation
        mBtn_add_reminder.setOnClickListener(view -> {
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidations();
        });

        hideProgressBar();

        checkButtonEditTextChanged();
        checkButtonEnabled();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void selectReminderDate() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    selectedDay =  dayOfMonth;
                    selectedMonth = monthOfYear;
                    selectedYear = year;



                    edt_reminder_date.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));


                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendReminderDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "Reminder _send Date: "+ sendReminderDate);

                    checkButtonEnabled();

                    //show popup for select time
                    selectReminderTime();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }


    private void selectReminderTime() {

        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    //set selected time
                    selectedHour = hourOfDay;
                    //set min -1 for remind me at
                    selectedMinute = minute - 1;
                    boolean isPM = (hourOfDay >= 12);

                    //check if selected date is today's date
                    if (Helper.isToday(Helper.getDatefromString(sendReminderDate)))
                    {
                        //selected reminder date is today's date

                        //check for the selected time -- should be greater than current time

                        Calendar datetime = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minute);
                        if(datetime.getTimeInMillis() >c.getTimeInMillis()){
                            //it's after current
                            isGreaterThanCurrent=false;
                            edt_reminder_time.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                            sendReminderTime = hourOfDay + ":" + minute + ":" +"00";
                            checkButtonEnabled();
                        }else{

                            //it's before current'
                            isGreaterThanCurrent=true;
                            new Helper().showCustomToast(context, "Reminder time should be greater than current time!");
                            edt_reminder_time.setText("");
                            sendReminderTime=null;
                            checkButtonEnabled();
                        }
                    }
                    else {
                        //selected reminder date is greater than today's date

                        //no need to check for the current time if date is greater than today's date
                        sendReminderTime = hourOfDay + ":" + minute + ":" +"00";
                        edt_reminder_time.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                        checkButtonEnabled();
                    }

                    checkButtonEnabled();


                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                    if (sendReminderTime!=null) Log.e(TAG, "Reminder Time: "+sendReminderTime);

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();

    }

    private void checkValidations() {

        //reminder text
        if (Objects.requireNonNull(edt_reminderText.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter reminder text!");
            //reminder date
        else if (sendReminderDate==null) new Helper().showCustomToast(context, "Please select reminder date!");
            //reminder time
        else if (sendReminderTime==null) new Helper().showCustomToast(context, "Please select reminder time!");

        else if (isGreaterThanCurrent) new Helper().showCustomToast(context, "Please select time less than current time!");

        else showConfirmDialog();
    }

    private boolean checkCalendarReadPermission() {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCalendarWritePermission() {
        int result= ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionWriteStorage()
    {

        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)))
        {
            new Helper().showCustomToast(this, getString(R.string.calendar_permissionRationale));
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                }, Permission_CODE_CALENDAR);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == Permission_CODE_CALENDAR)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open documents once permission is granted

               /* new Helper().addReminderInCalendar(context, reminder_title, formatReminderDateFromString(reminder_dateTime), formatReminderDateFromString(reminder_dateTime));
                Log.e(TAG, "showShowCalendarDialog: "+reminder_dateTime +" Formated"+formatReminderDateFromString(reminder_dateTime));

                //show success sync toast
                new Helper().showSuccessCustomToast(context, getString(R.string.reminder_synced_successfully) );*/
                showProgressBar("Adding reminder...");
                new Handler().postDelayed(this::Call_AddReminder,1000);


            }
            else
            {
                //Displaying another toast if permission is not granted
                new Helper().showCustomToast(context, getString(R.string.calendar_permissionRationale));
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void showConfirmDialog()
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
        AppCompatImageView iv_google_calendar_icon =  alertLayout.findViewById(R.id.iv_google_calendar_icon);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);
        iv_google_calendar_icon.setVisibility(fromOther==2 ? View.GONE : View .VISIBLE);
        tv_msg.setText( "Sync with Google?");//fromOther==2 ? "Update Reminder?" :
        tv_desc.setText( getString(R.string.submit_calendar_remainder_confirmation));//fromOther ==2 ? "Are you sure you want to update this reminder?" :
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            // showSuccessPopup();
            alertDialog.dismiss();

            if (Helper.isNetworkAvailable(context))
            {
                if (fromOther==2 ) {
                    // update reminder

                    if (checkCalendarReadPermission() && checkCalendarWritePermission())
                    {
                        showProgressBar("Updating reminder...");
                        Call_UpdateReminder();
                    }else requestPermissionWriteStorage();

                    /*new Helper().addReminderInCalendar(context, String.valueOf(edt_reminderText.getText()), sendReminderDate, sendReminderTime)*/
                }
                else {

                    if (checkCalendarReadPermission() && checkCalendarWritePermission())
                    {
                        showProgressBar("Adding reminder...");
                        new Handler().postDelayed(this::Call_AddReminder,1000);
                    }else requestPermissionWriteStorage();
                }

            } else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getWidth();
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


    private void Call_AddReminder() {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("remind_at", sendReminderDate + " " + sendReminderTime);
        jsonObject.addProperty("reminder_comments", Objects.requireNonNull(edt_reminderText.getText()).toString());
        jsonObject.addProperty("is_done", 0);
        jsonObject.addProperty("user_id", user_id);
        jsonObject.addProperty("lead_id", lead_id);
        ApiClient client = ApiClient.getInstance();
        client.getApiService().addReminder(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success"))
                            isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess == 1) {
                            onAddReminder(Objects.requireNonNull(edt_reminderText.getText()).toString(),sendReminderDate + " " + sendReminderTime);
                        } else showErrorLog("Error Occurred during password change !");
                    }
                } else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " " + response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });



    }

    private void onAddReminder(String Title, String dateTime) {
        runOnUiThread(() -> {

            hideProgressBar();
            isReminderAdded = true;
            //reminder_title = Title;
            //reminder_dateTime = dateTime;
            //set local reminder
            //setLocalAlarm();


            if (Helper.isNetworkAvailable(context))
            {
                /*if (checkCalendarReadPermission() && checkCalendarWritePermission())
                {*/
                new Helper().addReminderInCalendar(context, Title, Helper.formatReminderDateFromString(dateTime), Helper.formatReminderDateFromString(dateTime));
                Log.e(TAG, "showShowCalendarDialog: "+dateTime +" Formated"+ Helper.formatReminderDateFromString(dateTime));

                //show success sync toast
                new Helper().showSuccessCustomToast(context, getString(R.string.reminder_synced_successfully) );
               /* }
                else requestPermissionWriteStorage();*/

            } else Helper.NetworkError(context);


            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("isReminderAdded", true);
                editor.apply();
            }

            /*if (sharedPreferences != null) {
                editor = sharedPreferences.edit();
                editor.putBoolean("isSyncGoogleCalendar", true);
                editor.putString("reminder_title",Title);
                editor.putString("reminder_dateTime",dateTime);
                editor.apply();
            }*/

            //do backPress
            onBackPressed();
            //show success toast
            //   new Helper().showSuccessCustomToast(context, getString(R.string.reminder_added_successfully) );


        });

    }

    private void Call_UpdateReminder() {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("remind_at", sendReminderDate + " " + sendReminderTime);
        jsonObject.addProperty("reminder_comments", Objects.requireNonNull(edt_reminderText.getText()).toString());
        jsonObject.addProperty("reminder_id", reminderId);


        ApiClient client = ApiClient.getInstance();
        client.getApiService().updateReminder(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success"))
                            isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess == 1) {
                            UpdateReminder(Objects.requireNonNull(edt_reminderText.getText()).toString(),sendReminderDate + " " + sendReminderTime);
                        }
                        else showErrorLog("Error Occurred during updating reminder!");
                    }
                } else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " " + response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    private void UpdateReminder(String Title, String dateTime) {
        runOnUiThread(() -> {

            Log.e(TAG, "UpdateReminder: ");
            hideProgressBar();
            //set Local Reminder
            //setLocalAlarm();

            //show success toast
            if (Helper.isNetworkAvailable(context))
            {
                /*if (checkCalendarReadPermission() && checkCalendarWritePermission())
                {*/
                new Helper().addReminderInCalendar(context, Title, Helper.formatReminderDateFromString(dateTime), Helper.formatReminderDateFromString(dateTime));
                Log.e(TAG, "showShowCalendarDialog: "+dateTime +" Formated"+ Helper.formatReminderDateFromString(dateTime));

                //show success sync toast
                new Helper().showSuccessCustomToast(context, getString(R.string.reminder_synced_successfully) );
               /* }
                else requestPermissionWriteStorage();*/

            } else Helper.NetworkError(context);

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }
            //do backPress
            onBackPressed();

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
        alarmManager.set(AlarmManager.RTC_WAKEUP, calNow.getTimeInMillis(),
                pendingIntent);

    }


    //Show Error Log
    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }

    private void checkButtonEnabled()
    {
        //project title
        if (Objects.requireNonNull(edt_reminderText.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_reminder_date.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_reminder_time.getText()).toString().trim().isEmpty()) setButtonDisabledView();

        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit project
        mBtn_add_reminder.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_add_reminder.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit project

        mBtn_add_reminder.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_add_reminder.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void checkButtonEditTextChanged() {

        edt_reminderText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        edt_reminder_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_reminder_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    //Show Soft Keyboard
    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //Hide Progress Bar
    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (fromShortcut)
            {
                startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                finish();
            } else onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        if (isReminderAdded) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Reminder added"));

        if(fromShortcut)
        {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else{

            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }

}