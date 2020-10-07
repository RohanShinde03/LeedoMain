package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead;

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

public class AddTeamLeadActivity extends AppCompatActivity {

    @BindView(R.id.acTv_addTeamLead_mrs) AutoCompleteTextView acTv_addTeamLead_mrs;
    @BindView(R.id.edt_addTeamLead_FirstName) TextInputEditText edt_FirstName;
    @BindView(R.id.edt_addTeamLead_middleName) TextInputEditText edt_middleName;
    @BindView(R.id.edt_addTeamLead_lastName) TextInputEditText edt_lastName;
    @BindView(R.id.edt_addTeamLead_MobileNo) TextInputEditText edt_MobileNo;
    @BindView(R.id.edt_addTeamLead_Email) TextInputEditText edt_Email;
    @BindView(R.id.til_addTeamLead_leadFirstName) TextInputLayout til_leadFirstName;
    @BindView(R.id.til_addTeamLead_Email) TextInputLayout til_Email;
    @BindView(R.id.edt_addTeamLead_password) TextInputEditText edt_password;
    @BindView(R.id.edt_addTeamLead_confirmPassword) TextInputEditText edt_confirmPassword;
    @BindView(R.id.mBtn_addTeamLead_submitTeamLead) MaterialButton mBtn_submitTeamLead;

    Activity context;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    private int selectedNamePrefixId =0,sales_lead_id=0,fromOther = 1; //TODO fromOther ==> 1 - Add TeamLead, 2- Edit/Update Team Lead Info
    private String api_token="",TAG="AddTeamLeadActivity", selectedNamePrefix = "",countryPhoneCode = "91",first_name="",middle_name="",last_name="",mobile_number="",email_id="";
    private ArrayList<PersonNamePrefixModel> personNamePrefixModelArrayList;
    private ArrayList<String> namePrefixArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_team_lead);
        ButterKnife.bind(this);
        context = AddTeamLeadActivity.this;

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        //user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        //get Intent
        if (getIntent()!=null) {
            fromOther = getIntent().getIntExtra("fromOther", 1);
            sales_lead_id = getIntent().getIntExtra("sales_lead_id", 0);

            //data from update reminder
            first_name = getIntent().getStringExtra("first_name");
            middle_name = getIntent().getStringExtra("middle_name");
            last_name = getIntent().getStringExtra("last_name");
            mobile_number = getIntent().getStringExtra("mobile_number");
            email_id = getIntent().getStringExtra("email_id");

        }

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromOther==2 ? getString(R.string.update_team_lead_details): getString(R.string.add_team_lead_details));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        hideProgressBar();

        personNamePrefixModelArrayList=new ArrayList<>();
        namePrefixArrayList=new ArrayList<>();
        namePrefixArrayList=new ArrayList<>();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            showProgressBar("Please wait...");
            new Handler(getMainLooper()).postDelayed(this::getLeadData, 100);
        }
        else Helper.NetworkError(context);


        //check lead mobile number
        // checkTeamLeadMobile();


        if (fromOther==2) {

            //update team lead info
            if (first_name != null && !first_name.trim().isEmpty()) edt_FirstName.setText(first_name);
            if (middle_name != null && !middle_name.trim().isEmpty()) edt_middleName.setText(middle_name);
            if (last_name != null && !last_name.trim().isEmpty()) edt_lastName.setText(last_name);
            if (mobile_number != null && !mobile_number.trim().isEmpty()) edt_MobileNo.setText(mobile_number);
            if (email_id != null && !email_id.trim().isEmpty()) edt_Email.setText(email_id);
        }

        //checkEmail
        checkTeamLeadEmail();

        mBtn_submitTeamLead.setText(fromOther==2 ? getString(R.string.update_team_lead_details): getString(R.string.add_team_lead_details));

        //submit team lead
        mBtn_submitTeamLead.setOnClickListener(view -> {

            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            if (fromOther==2) checkUpdateValidation();
            else checkValidation();
        });

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
        else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's  password!");
            //confirm password
        else if (Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's confirm password!");
            // verified mobile
            //else if (!isLeadMobileVerified) new Helper().showCustomToast(context, "Please do verify Team Leads's mobile number!");
        else if (!isSamePasswords()) new Helper().showCustomToast(context, "Passwords does not match!");
        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }

    private void checkUpdateValidation() {
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
            // else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's  password!");
            //confirm password
            //   else if (Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Sales Team Lead's confirm password!");
            // verified mobile
            //else if (!isLeadMobileVerified) new Helper().showCustomToast(context, "Please do verify Team Leads's mobile number!");
            //  else if (!isSamePasswords()) new Helper().showCustomToast(context, "Passwords does not match!");
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

        tv_msg.setText(fromOther==2 ? getString(R.string.update_team_lead_question):getString(R.string.add_team_lead_question));
        tv_desc.setText(fromOther==2 ?getString(R.string.update_team_lead_confirmation):getString(R.string.add_team_lead_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                if (fromOther==2 ) {
                    // update reminder
                    showProgressBar("Updating Team Lead Details...");
                    Call_UpdateTeamLead();
                }
                else {

                    showProgressBar(getString(R.string.submitting_teamLead_details));

                    new Handler().postDelayed(this::call_salesTeamLead,500);
                }
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
        jsonObject.addProperty("password", Objects.requireNonNull(edt_password.getText()).toString());
        jsonObject.addProperty("role_id", 10);
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().addSalesTeamLead(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        AddNewTeamLead();
                    }
                    else if ("2".equals(success)) {
                        //mobile number already exists
                        showErrorLog(context.getString(R.string.mob_number_exists));
                    }
                    else {
                        showErrorLog("Failed to add team lead!");
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

    private void AddNewTeamLead()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            new Helper().showSuccessCustomToast(context, "Team Lead Added Successfully...!");
            new Handler().postDelayed(this::onBackPressed, 1000);

        });
    }

    private void Call_UpdateTeamLead() {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("first_name", Objects.requireNonNull(edt_FirstName.getText()).toString());
        jsonObject.addProperty("middle_name", Objects.requireNonNull(edt_middleName.getText()).toString());
        jsonObject.addProperty("last_name", Objects.requireNonNull(edt_lastName.getText()).toString());
        jsonObject.addProperty("country_code",countryPhoneCode);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_MobileNo.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_Email.getText()).toString());
        jsonObject.addProperty("password", Objects.requireNonNull(edt_password.getText()).toString());
        jsonObject.addProperty("user_id", sales_lead_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().updateTeamLeadDetails(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success"))
                            isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess == 1) {
                            UpdateTeamLead();
                        }
                        else showErrorLog("Error Occurred during update Team Lead!");
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });

    }

    private void UpdateTeamLead() {
        runOnUiThread(() -> {

            Log.e(TAG, "UpdateTeamLead");
            hideProgressBar();

            //do backPress
            onBackPressed();
            //show success toast
            new Helper().showSuccessCustomToast(context, "Team Lead Details Updated Successfully!" );

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



    private void checkTeamLeadEmail()
    {
        edt_Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Objects.requireNonNull(edt_Email.getText()).toString().length()>4) {
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
        else if (fromOther==1 && Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty())setButtonDisabledView();
            //confirm password
        else if (fromOther ==1 && Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty())setButtonDisabledView();
            //check for same password
        else if (fromOther ==1 && !isSamePasswords()) setButtonDisabledView();
        else
        {
            //set button enabled view
            setButtonEnabledView();
        }

    }


    private boolean isSamePasswords()
    {
        final String newPasswordVal = Objects.requireNonNull(edt_password.getText()).toString();
        final String confirmPasswordVal = Objects.requireNonNull(edt_confirmPassword.getText()).toString();
        return confirmPasswordVal.equals(newPasswordVal);
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




    /*
    private void checkTeamLeadMobile()
    {
        edt_MobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //tv_mobAlExists.setVisibility(View.GONE);
                if (Objects.requireNonNull(edt_MobileNo.getText()).length()>7)
                {
                    mBtn_verifyLeadMob.setText(getString(R.string.verify));
                    mBtn_verifyLeadMob.setVisibility(View.VISIBLE);
                    if (Objects.requireNonNull(edt_MobileNo.getText()).length()>9) hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
                }
                else
                {
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    mBtn_resendOTP.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    tv_mobVerifySuccess.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                }


                if (Objects.requireNonNull(edt_MobileNo.getText()).length()>9) checkButtonEnabled();
                else
                {
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    mBtn_resendOTP.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    tv_mobVerifySuccess.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mBtn_verifyLeadMob.setOnClickListener(v -> {

            if (mBtn_verifyLeadMob.getText().toString().contains(getString(R.string.verify)))
            {
                //do verify
                //if (isValidPhone(edt_authPMob))
                {
                    if (isNetworkAvailable(context))
                    {
                        //if (progressDialog!=null) progressDialog.show();
                        //if (checkAndRequestPermissions())
                        {
                            Log.e(TAG, "onClick: " );
                        }

                        //check all Permissions
                        mBtn_verifyLeadMob.setVisibility(View.GONE);
                        //tv_loadingMsg.setText(getString(R.string.sending_otp));
                        //if (ll_pb!=null) ll_pb.setVisibility(View.VISIBLE);
                        showProgressBar(getString(R.string.sending_otp));
                        call_sendOTP_leadMobile(edt_MobileNo.getText().toString());

                       */
/* else
                        {
                            //requestPermission
                            requestSMSPermission(edt_authPMob.getText().toString());
                        }*//*

                    }
                    else NetworkError(context);
                }

            }
            else
            {
                //check entered otp and submit
                if (Objects.requireNonNull(edt_OTPNumber.getText()).toString().equals(lead_OTP))
                {
                    //opt match

                    edt_OTPNumber.setText("");
                    tv_mobVerifySuccess.setVisibility(View.VISIBLE);
                    runOnUiThread(() -> {

                        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.blinking_animation);

                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round,context.getTheme()));
                        //else tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round));
                        tv_mobVerifySuccess.startAnimation(expandIn);

                        tv_mobVerifySuccess.postDelayed(() -> {

                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round,context.getTheme()));
                            //else tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round));

                            tv_mobVerifySuccess.clearAnimation();
                            tv_mobVerifySuccess.invalidate();
                        }, 3000);
                    });

                    //once success gone the controls
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    edt_leadMobileNo.setEnabled(false);
                    ll_leadMobCcp.setEnabled(false);

                    //gone visibilities
                    mBtn_resendOTP.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);

                    new Helper().showSuccessCustomToast(context, "Mobile number verified successfully!");

                    //set lead mobile Verified True
                    isLeadMobileVerified = true;

                    //check buttonEnabled
                    checkButtonEnabled();
                }
                else
                {
                    //not match
                    new Helper().showCustomToast(context, "OTP did not match!");
                }
            }
        });
    }

    private void call_sendOTP_leadMobile(String mobile)
    {
        ApiClient client = ApiClient.getInstance();
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("api_token", WebServer.API_TOKEN_EXTERNAL);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("mobile_number", mobile);

        client.getApiService().sendOTPLeadMobile(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject())
                                {
                                    JsonObject jsonObject1 = response.body().get("data").getAsJsonObject();
                                    setOTPJson(jsonObject1);
                                }
                            }else showErrorLog("Failed to send OTP!");
                        }
                        else if (isSuccess==2)
                        {
                            //mobile number already exists
                            set_leadMobExistsView();
                            if (response.body().has("msg")) showErrorLog(!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : getString(R.string.mob_number_exists));
                        }
                        else showErrorLog("Failed to send OTP!");
                    }
                }
                else {
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

    void set_leadMobExistsView()
    {

        runOnUiThread(() -> {
            //if (ll_pb!=null) ll_pb.setVisibility(View.GONE);
            //if (pb_loading!=null) pb_loading.setVisibility(View.GONE);
            hideProgressBar();
            tv_mobAlExists.setVisibility(View.VISIBLE);
            edt_leadMobileNo.setEnabled(true);

            til_leadOtp.setVisibility(View.GONE);
            edt_OTPNumber.setVisibility(View.GONE);
            mBtn_verifyLeadMob.setVisibility(View.GONE);
        });
    }

    private void setOTPJson(final JsonObject jsonObject)
    {

        runOnUiThread(() -> {
            //if (ll_pb!=null) ll_pb.setVisibility(View.GONE);
            //if (pb_loading!=null) pb_loading.setVisibility(View.GONE);
            hideProgressBar();

            String OTP = "";
            if (jsonObject.has("otp")) OTP = !jsonObject.get("otp").isJsonNull() ? jsonObject.get("otp").getAsString() : "" ;
            Log.e(TAG, "otp "+ OTP);

            //new Helper().showCustomToast(context, "OTP "+OTP);
            lead_OTP = OTP;
            til_leadOtp.setVisibility(View.VISIBLE);
            edt_OTPNumber.setVisibility(View.VISIBLE);
            edt_OTPNumber.requestFocus();
            //edt_authPMob.setEnabled(false);
            mBtn_verifyLeadMob.setText(getString(R.string.submit_otp));
            mBtn_verifyLeadMob.setVisibility(View.VISIBLE);

            setResendLeadOTP();

            */
/*  edt_authPOTP.setVisibility(View.VISIBLE);
                edt_authPMob.setEnabled(false);
                btn_verifyOwnerMob.setText(getString(R.string.submit_otp));*//*


            //if (jsonObject.has("msg")) myModel.setCity_name(!jsonObject.get("msg").isJsonNull() ? jsonObject.get("msg").getAsString() : "msg" );
        });
    }

    void setResendLeadOTP()
    {

        //visible controls
        tv_resendOtp_msg.setVisibility(View.VISIBLE);
        mBtn_resendOTP.setVisibility(View.VISIBLE);

        tv_resendOtp_counter.setVisibility(View.VISIBLE);
        startCountDownTimer(tv_resendOtp_counter);

        new Handler().postDelayed(() -> {

            mBtn_resendOTP.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mBtn_resendOTP.setTextColor(getResources().getColor(R.color.main_white));

            mBtn_resendOTP.setOnClickListener(v -> {

                if (isNetworkAvailable(context))
                {

                    //disabled resend-otp button
                    mBtn_resendOTP.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
                    mBtn_resendOTP.setTextColor(getResources().getColor(R.color.main_white));

                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    //tv_loadingMsg.setText(getString(R.string.re_sending_otp));
                    //if (ll_pb!=null) ll_pb.setVisibility(View.VISIBLE);
                    showProgressBar(getString(R.string.re_sending_otp));
                    call_sendOTP_leadMobile(Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim());
                }
                else NetworkError(context);
            });

        }, 120000);
    }


    private void startCountDownTimer(final AppCompatTextView tv_countDownTime)
    {

        CountDownTimer countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_countDownTime.setText(new Helper().hmsTimeFormatter(millisUntilFinished));
            }
            @Override
            public void onFinish() {
                tv_countDownTime.setText("00:00");

                new Handler().postDelayed(() -> tv_countDownTime.setVisibility(View.GONE), 1000);
            }

        }.start();
        countDownTimer.start();
    }
*/