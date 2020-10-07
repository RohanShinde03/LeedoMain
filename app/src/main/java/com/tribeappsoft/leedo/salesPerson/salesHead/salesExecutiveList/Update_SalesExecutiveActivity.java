package com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.leads.PersonNamePrefixModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class Update_SalesExecutiveActivity extends AppCompatActivity {

    @BindView(R.id.acTv_updateSalesExecutive_mrs) AutoCompleteTextView acTv_addTeamLead_mrs;
    @BindView(R.id.edt_updateSalesExecutive_FirstName) TextInputEditText edt_FirstName;
    @BindView(R.id.edt_updateSalesExecutive_middleName) TextInputEditText edt_middleName;
    @BindView(R.id.edt_updateSalesExecutive_lastName) TextInputEditText edt_lastName;
    @BindView(R.id.edt_updateSalesExecutive_MobileNo) TextInputEditText edt_MobileNo;
    @BindView(R.id.edt_updateSalesExecutive_Email) TextInputEditText edt_Email;
    @BindView(R.id.til_updateSalesExecutive_Email) TextInputLayout til_Email;
    @BindView(R.id.edt_updateSalesExecutive_password) TextInputEditText edt_password;
    @BindView(R.id.edt_updateSalesExecutive_confirmPassword) TextInputEditText edt_confirmPassword;
    @BindView(R.id.checkBox_AddAsTeamLead) AppCompatCheckBox checkBox_AddAsTeamLead;
    @BindView(R.id.mBtn_updateSalesExecutive_submitTeamLead) MaterialButton mBtn_submitTeamLead;

    Activity context;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private int selectedNamePrefixId =0, sales_lead_id=0, role_id=1, role_status_id=2;
    private String api_token="", TAG="AddTeamLeadActivity", selectedNamePrefix = "";
    private String countryPhoneCode = "91";
    private ArrayList<PersonNamePrefixModel> personNamePrefixModelArrayList;
    private ArrayList<String> namePrefixArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_sales_executive);
        ButterKnife.bind(this);
        context = Update_SalesExecutiveActivity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.update_sales_executive_title));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        //int user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        //get Intent
        if (getIntent()!=null) {
            sales_lead_id = getIntent().getIntExtra("sales_lead_id", 0);

            //data from update reminder
            String first_name = getIntent().getStringExtra("first_name");
            String middle_name = getIntent().getStringExtra("middle_name");
            String last_name = getIntent().getStringExtra("last_name");
            String mobile_number = getIntent().getStringExtra("mobile_number");
            String email_id = getIntent().getStringExtra("email_id");
            //TODO fromOther ==> 1 - Add TeamLead, 2- Edit/Update Team Lead Info
            int is_team_lead = getIntent().getIntExtra("is_team_lead", 0);


            //update team lead info
            if (first_name != null && !first_name.trim().isEmpty()) edt_FirstName.setText(first_name);
            if (middle_name != null && !middle_name.trim().isEmpty()) edt_middleName.setText(middle_name);
            if (last_name != null && !last_name.trim().isEmpty()) edt_lastName.setText(last_name);
            if (mobile_number != null && !mobile_number.trim().isEmpty()) edt_MobileNo.setText(mobile_number);
            if (email_id != null && !email_id.trim().isEmpty()) edt_Email.setText(email_id);

            if(is_team_lead == 1)
            {
                //already a team lead
                checkBox_AddAsTeamLead.setChecked(true);
                //set role id to 10
                role_id =10 ;
            }
            else
            {
                //sales executive
                checkBox_AddAsTeamLead.setChecked(false);
                //set role id to 1
                role_id =1;
            }
        }

        //checkEmail
        checkTeamLeadEmail();


        hideProgressBar();

        personNamePrefixModelArrayList=new ArrayList<>();
        namePrefixArrayList=new ArrayList<>();
        namePrefixArrayList=new ArrayList<>();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            showProgressBar("Please wait...");
            new Handler(getMainLooper()).postDelayed(this::getLeadData, 100);
        }
        else Helper.NetworkError(context);

        checkBox_AddAsTeamLead.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            role_id= (isChecked) ? 10 : 10;
            role_status_id= (isChecked) ? 1: 2;
            Log.e(TAG, "onCheckedChanged: "+role_id);
        });

        //submit team lead
        mBtn_submitTeamLead.setOnClickListener(view -> {

            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidation();
        });

/*
        checkBox_AddAsTeamLead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b)role_id=10;
                        else role_id =1;

            }
        });*/
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
                .subscribe(new Subscriber<Response<JsonObject>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        //setAdapterNamePrefix();
                        setNamePrefix();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject())
                                            {
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                setJson(jsonObject);
                                            }
                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
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

    private void setJson(JsonObject jsonObject)
    {
        // get person name prefix array
        if (jsonObject.has("person_name_prefix"))
        {
            if (!jsonObject.get("person_name_prefix").isJsonNull() && jsonObject.get("person_name_prefix").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("person_name_prefix").getAsJsonArray();
                //clear list
                personNamePrefixModelArrayList.clear();
                namePrefixArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setNamePrefixJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }


    private void setNamePrefixJson(JsonObject jsonObject)
    {
        PersonNamePrefixModel myModel = new PersonNamePrefixModel();
        if (jsonObject.has("name_prefix_id")) myModel.setName_prefix_id(!jsonObject.get("name_prefix_id").isJsonNull() ? jsonObject.get("name_prefix_id").getAsInt() : 0 );
        if (jsonObject.has("name_prefix"))
        {
            myModel.setName_prefix(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
            namePrefixArrayList.add(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
        }
        personNamePrefixModelArrayList.add(myModel);
    }

    private void setNamePrefix()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            setAdapterNamePrefix();

            //checkButtonEnabled
            checkButtonEnabled();

        });
    }

    private void setAdapterNamePrefix()
    {

        if (namePrefixArrayList.size() >0 && personNamePrefixModelArrayList.size()>0)
        {

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, namePrefixArrayList);
            //set def selected
            acTv_addTeamLead_mrs.setText(namePrefixArrayList.get(0));
            selectedNamePrefixId = personNamePrefixModelArrayList.get(0).getName_prefix_id();
            selectedNamePrefix = personNamePrefixModelArrayList.get(0).getName_prefix();

            acTv_addTeamLead_mrs.setAdapter(adapter);
            acTv_addTeamLead_mrs.setThreshold(0);


            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_addTeamLead_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (PersonNamePrefixModel pojo : personNamePrefixModelArrayList)
                {
                    if (pojo.getName_prefix().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedNamePrefixId = pojo.getName_prefix_id(); // This is the correct ID
                        selectedNamePrefix = pojo.getName_prefix();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Name prefix & id " + selectedNamePrefix +"\t"+ selectedNamePrefixId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }

    private void checkValidation() {
        //name prefix
        if (selectedNamePrefixId==0) new Helper().showCustomToast(context, "Please select name prefix !");
            //first name
        else if (Objects.requireNonNull(edt_FirstName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's  name!");
            //last name
        else if (Objects.requireNonNull(edt_lastName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's last name!");
            //email
        else if (Objects.requireNonNull(edt_Email.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter customer email!");
            // valid email
        else if (!isValidEmail(edt_Email)) new Helper().showCustomToast(context, "Please enter a valid email!");
            // mobile
        else if (Objects.requireNonNull(edt_MobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Team Lead's mobile number!");
            //valid mobile
        else if (!isValidPhone(edt_MobileNo)) new Helper().showCustomToast(context, "Please enter a valid mobile number!");
            //password
        //else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's  password!");
            //confirm password
       // else if (Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's confirm password!");
            // verified mobile
            //else if (!isLeadMobileVerified) new Helper().showCustomToast(context, "Please do verify Team Leads's mobile number!");
        //else if (!isSamePasswords()) new Helper().showCustomToast(context, "Passwords does not match!");
        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }

    private void showSubmitLeadAlertDialog()
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

        tv_msg.setText(R.string.update_salesExecutive_question);
        tv_desc.setText(role_status_id==1 ?getString(R.string.update_as_team_lead_confirmation):getString(R.string.update_as_sales_executive_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.updating_sales_executive));
                new Handler().postDelayed(this::call_salesTeamLead,500);

            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
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

    private void call_salesTeamLead()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("first_name", Objects.requireNonNull(edt_FirstName.getText()).toString());
        jsonObject.addProperty("middle_name", Objects.requireNonNull(edt_middleName.getText()).toString());
        jsonObject.addProperty("last_name", Objects.requireNonNull(edt_lastName.getText()).toString());
        jsonObject.addProperty("country_code",countryPhoneCode);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_MobileNo.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_Email.getText()).toString());
        jsonObject.addProperty("password", Objects.requireNonNull(edt_confirmPassword.getText()).toString());
        jsonObject.addProperty("user_id", sales_lead_id);
        jsonObject.addProperty("role_id", role_id);
        jsonObject.addProperty("role_status_id",role_status_id);
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().updateSalesExecutive(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        AddNewTeamLead(role_status_id);
                    } else {
                        showErrorLog("Failed to add as team lead!");
                    }
                }
                else
                {
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
                            showErrorLog(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void AddNewTeamLead(int role_status_id)
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            if((role_status_id==1)) new Helper().showSuccessCustomToast(context, "Sale's Executive updated as Team Lead Successfully...!");
            else  new Helper().showSuccessCustomToast(context, "Sale's Executive details updated Successfully...!");

            new Handler().postDelayed(this::onBackPressed, 1000);

        });
    }

    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
            context.runOnUiThread(() -> {

                //hide pb
                hideProgressBar();
                Helper.onErrorSnack(context, message);

            });
    }


    private boolean isSamePasswords()
    {
        final String newPasswordVal = Objects.requireNonNull(edt_password.getText()).toString();
        final String confirmPasswordVal = Objects.requireNonNull(edt_confirmPassword.getText()).toString();
        return confirmPasswordVal.equals(newPasswordVal);
    }

    private void checkTeamLeadEmail()
    {
        edt_Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (edt_Email.getText().toString().length()>4) {
                    if (!isValidEmail(edt_Email)) {
                        til_Email.setErrorEnabled(true);
                        til_Email.setError("Please enter valid email! eg.user@gamil.com");
                        //til_email.setHelperTextEnabled(true);
                        //til_email.setHelperText("Valid email eg. abc@gmail.com");
                    }
                    else {
                        til_Email.setErrorEnabled(false);
                        til_Email.setError(null);
                        //til_email.setHelperTextEnabled(false);
                        //til_email.setHelperText(null);
                    }

                    //checkButtonEnabled
                    checkButtonEnabled();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_FirstName.addTextChangedListener(new TextWatcher() {
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

        edt_lastName.addTextChangedListener(new TextWatcher() {
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

        edt_MobileNo.addTextChangedListener(new TextWatcher() {
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

        edt_confirmPassword.addTextChangedListener(new TextWatcher() {
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


    private void checkButtonEnabled()
    {
        //name prefix
        if (selectedNamePrefixId==0) setButtonDisabledView();
            //first name
        else if (Objects.requireNonNull(edt_FirstName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //last name
        else if (Objects.requireNonNull(edt_lastName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //email
        else if (Objects.requireNonNull(edt_Email.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // valid email
        else if (!isValidEmail(edt_Email)) setButtonDisabledView();
            // mobile
        else if (Objects.requireNonNull(edt_MobileNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //valid mobile
        else if (!isValidPhone(edt_MobileNo)) setButtonDisabledView();
            // verified mobile
            //else if (!isLeadMobileVerified) new Helper().showCustomToast(context, "Please do verify Team Leads's mobile number!");

            //password
        //else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty())setButtonDisabledView();
            //confirm password
       //else if (Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty())setButtonDisabledView();

       // else if (!isSamePasswords()) setButtonDisabledView();
        else
        {
            //set button enabled view
            setButtonEnabledView();
        }

    }



    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_submitTeamLead.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submitTeamLead.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit team lead

        mBtn_submitTeamLead.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submitTeamLead.setTextColor(getResources().getColor(R.color.main_white));
    }


    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    private boolean isValidPhone(EditText phone)
    {
        boolean ret = true;
        if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }

    private boolean isValidEmail(EditText email)
    {
        boolean ret = true;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        //if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
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
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
