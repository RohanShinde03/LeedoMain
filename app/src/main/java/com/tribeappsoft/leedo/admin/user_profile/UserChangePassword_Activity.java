package com.tribeappsoft.leedo.admin.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.loginModule.LoginActivity;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class UserChangePassword_Activity extends AppCompatActivity {

    private String TAG = "UserChangePassword_Activity";
    @BindView(R.id.cl_user_changePassword) CoordinatorLayout parent;
    @BindView(R.id.til_user_currentPassword) TextInputLayout til_currentPassword;
    @BindView(R.id.edt_user_currentPassword) TextInputEditText edt_currentPassword;
    @BindView(R.id.til_user_new_password) TextInputLayout til_new_password;
    @BindView(R.id.edt_user_new_password) TextInputEditText edt_new_password;
    @BindView(R.id.til_user_confirm_password) TextInputLayout til_confirm_password;
    @BindView(R.id.edt_user_confirm_password) TextInputEditText edt_confirm_password;
    @BindView(R.id.btn_user_change_password) MaterialButton btn_change_password;
    @BindView(R.id.mTv_passwordStrength) MaterialTextView mTv_passwordStrength;
    @BindView(R.id.ll_passwordStrength) LinearLayoutCompat ll_passwordStrength;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    private AppCompatActivity context;
    private int user_id=0;
    private String API_TOKEN_EXTERNAL="";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_password_);
        ButterKnife.bind(this);
        context = UserChangePassword_Activity.this;
        if (getSupportActionBar()!=null) {
            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.change_password));

            getSupportActionBar().setTitle(getString(R.string.change_password));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setElevation(0); // to disable action bar elevation
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        API_TOKEN_EXTERNAL = sharedPreferences.getString("API_TOKEN_EXTERNAL", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        Log.e(TAG, "onCreate: "+ user_id );

        final Typeface face;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            face = getResources().getFont(R.font.metropolis_medium);
        }
        else
        {
            //or to support all versions use
            face=  ResourcesCompat.getFont(this, R.font.metropolis_medium);
        }

        til_currentPassword.setTypeface(face);
        til_new_password.setTypeface(face);
        til_confirm_password.setTypeface(face);

        //hide pb
        hideProgressBar();

        checkButtonEditTextChanged();

        //check Button Enabled View
        checkButtonEnabled();

    }

    private void checkButtonEditTextChanged() {

        edt_currentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  if (edt_mobileNo.getText().toString().length()>4) {
//                    if (!isValidPhone(edt_mobileNo)) {
//                        til_mobNo.setErrorEnabled(true);
//                        til_mobNo.setError("Please enter valid mobile number!");
//                        //til_email.setHelperTextEnabled(true);
//                        //til_email.setHelperText("Valid email eg. abc@gmail.com");
//
//                    }
//                    else {
//                        til_mobNo.setErrorEnabled(false);
//                        til_mobNo.setError(null);
//                        //til_email.setHelperTextEnabled(false);
//                        //til_email.setHelperText(null);
//                    }

                //checkButtonEnabled
                checkButtonEnabled();
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_new_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  if (edt_mobileNo.getText().toString().length()>4) {
//                    if (!isValidPhone(edt_mobileNo)) {
//                        til_mobNo.setErrorEnabled(true);
//                        til_mobNo.setError("Please enter valid mobile number!");
//                        //til_email.setHelperTextEnabled(true);
//                        //til_email.setHelperText("Valid email eg. abc@gmail.com");
//
//                    }
//                    else {
//                        til_mobNo.setErrorEnabled(false);
//                        til_mobNo.setError(null);
//                        //til_email.setHelperTextEnabled(false);
//                        //til_email.setHelperText(null);
//                    }

                //checkButtonEnabled
                checkButtonEnabled();
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Calculate password strength
                calculateStrength(s.toString());
            }
        });


        edt_confirm_password.addTextChangedListener(new TextWatcher() {
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
    @Override
    protected void onResume() {
        super.onResume();
        btn_change_password.setOnClickListener(v -> checkValidations());
    }


    private void checkValidations() {

        if (!Objects.requireNonNull(edt_currentPassword.getText()).toString().trim().isEmpty())
        {
            if (!Objects.requireNonNull(edt_new_password.getText()).toString().trim().isEmpty())
            {

                if (!Objects.requireNonNull(edt_confirm_password.getText()).toString().trim().isEmpty())
                {

                    final String currentPasswordVal = edt_currentPassword.getText().toString();
                    final String newPasswordVal = edt_new_password.getText().toString();
                    final String confirmPasswordVal = edt_confirm_password.getText().toString();
                    if (confirmPasswordVal.equals(newPasswordVal))
                    {
                        //call api and change Password
                        final JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("api_token",  API_TOKEN_EXTERNAL);
                        jsonObject.addProperty("current_password",currentPasswordVal);
                        jsonObject.addProperty("password",confirmPasswordVal);
                        jsonObject.addProperty("user_id",user_id);

                        if (isNetworkAvailable(context))
                        {
                            showProgressBar(getString(R.string.updating_password));

                            new Handler().postDelayed(() -> call_changePassword(jsonObject),1000);


                        }else NetworkError(context);

                    }
                    else new Helper().showCustomToast(this , "The new and retype passwords do not match!");

                }
                else new Helper().showCustomToast(this, "Please retype your new  password!");

            }else new Helper().showCustomToast(this, "Please type your new password!");

        }else new Helper().showCustomToast(this, "Please type your current password!");


    }

    private void call_changePassword(JsonObject jsonObject) {

        ApiClient client = ApiClient.getInstance();
        client.getApiService().ChangePassword(jsonObject).enqueue(new Callback<JsonObject>()
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
                            onchangePassword();
                        }
                        else if(isSuccess==2) {
                            if (response.body().has("msg"))
                            {
                                showResponseToast();
                                new Helper().showCustomToast(context,!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : "Sorry! Your Current Password doesn't match with our records!");
                            }
                            else{
                                showResponseToast();
                                new Helper().showCustomToast(context, "Sorry! Your Current Password doesn't match with our records!");
                            }

                        }
                        else {
                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLog(msg);
                        }
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

    private void showResponseToast() {
        if(context!=null)
        {
            runOnUiThread(() -> {
                hideProgressBar();
                til_currentPassword.setHelperText("Current Password is Wrong!");
                til_currentPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            });
        }
    }

    private void showErrorLog(final String message)
    {
        runOnUiThread(() -> {
            hideProgressBar();
            onErrorSnack(context,message);
        });

    }

    private void onchangePassword()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            til_currentPassword.setHelperText("");

            if(sharedPreferences!=null)
            {
                //normal logout
                editor = sharedPreferences.edit();

                //remove donation shortcut
                new Helper().createShortCut(this,false);

                //clear the user data
                //clear the sharedPref data and save/commit
                editor.clear();
                editor.apply();
            }

            //clear/delete user payment data from app
            //Checkout.clearUserData(context);

            // [START Unsubscribe_topics]
            FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("sales");
            //show success toast
            new Helper().showSuccessCustomToast(context, getString(R.string.password_updated_successfully));

            new Handler().postDelayed(() -> {
                startActivity(new Intent(context, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            },1000);

        });

    }
    private void calculateStrength(String passwordText) {
        int upperChars = 0, lowerChars = 0, numbers = 0,
                specialChars = 0, otherChars = 0, strengthPoints = 0;
        char c;

        int passwordLength = passwordText.length();

        if (passwordLength ==0)
        {
          /*  mTv_passwordStrength.setText("Invalid Password");
            mTv_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_low_red));*/
            ll_passwordStrength.setVisibility(View.GONE);
            return;
        }

        //If password length is <= 5 set strengthPoints=1
        if (passwordLength <= 5) {
            strengthPoints =1;
        }
        //If password length is >5 and <= 10 set strengthPoints=2
        else if (passwordLength <= 10) {
            strengthPoints = 2;
        }
        //If password length is >10 set strengthPoints=3
        else
            strengthPoints = 3;
        // Loop through the characters of the password
        for (int i = 0; i < passwordLength; i++) {
            c = passwordText.charAt(i);
            // If password contains lowercase letters
            // then increase strengthPoints by 1
            if (c >= 'a' && c <= 'z') {
                if (lowerChars == 0) strengthPoints++;
                lowerChars = 1;
            }
            // If password contains uppercase letters
            // then increase strengthPoints by 1
            else if (c >= 'A' && c <= 'Z') {
                if (upperChars == 0) strengthPoints++;
                upperChars = 1;
            }
            // If password contains numbers
            // then increase strengthPoints by 1
            else if (c >= '0' && c <= '9') {
                if (numbers == 0) strengthPoints++;
                numbers = 1;
            }
            // If password contains _ or @
            // then increase strengthPoints by 1
            else if (c == '_' || c == '@') {
                if (specialChars == 0) strengthPoints += 1;
                specialChars = 1;
            }
            // If password contains any other special chars
            // then increase strengthPoints by 1
            else {
                if (otherChars == 0) strengthPoints += 2;
                otherChars = 1;
            }
        }

        if (strengthPoints <= 3)
        {
            mTv_passwordStrength.setText("Password Strength : LOW");
            mTv_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_low_red));
            // ll_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_low_red));
            ll_passwordStrength.setVisibility(View.VISIBLE);

        }
        else if (strengthPoints <= 6) {
            mTv_passwordStrength.setText("Password Strength : MEDIUM");
            mTv_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_medium_yellow));
            // ll_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_medium_yellow));
            ll_passwordStrength.setVisibility(View.VISIBLE);

        }
        else if (strengthPoints <= 9){
            mTv_passwordStrength.setText("Password Strength : HIGH");
            mTv_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_high_green));
            /*  ll_passwordStrength.setBackgroundColor(getResources().getColor(R.color.color_high_green));*/
            ll_passwordStrength.setVisibility(View.VISIBLE);

        }
    }

    private boolean isSamePasswords()
    {
        final String newPasswordVal = Objects.requireNonNull(edt_new_password.getText()).toString();
        final String confirmPasswordVal = Objects.requireNonNull(edt_confirm_password.getText()).toString();
        return confirmPasswordVal.equals(newPasswordVal);
    }

    private void checkButtonEnabled()
    {
        //current password
        if (Objects.requireNonNull(edt_currentPassword.getText()).toString().trim().isEmpty()) setButtonDisabledView();

        //new password
        if (Objects.requireNonNull(edt_new_password.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_confirm_password.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            // verified password
        else if (!isSamePasswords()) setButtonDisabledView();

        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }
    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit project
        btn_change_password.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btn_change_password.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit project

        btn_change_password.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        btn_change_password.setTextColor(getResources().getColor(R.color.main_white));
    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        super.onBackPressed();
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


}

