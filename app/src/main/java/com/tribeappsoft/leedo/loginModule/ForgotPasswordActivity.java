package com.tribeappsoft.leedo.loginModule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class ForgotPasswordActivity extends AppCompatActivity {


    @BindView(R.id.cl_forgetPassword) CoordinatorLayout parent;
    @BindView(R.id.ll_forgotPass_1) LinearLayoutCompat llForgotPass1;
    @BindView(R.id.til_Forgot_mobNo) TextInputLayout textInputLayoutMobile;
    @BindView(R.id.edt_Forgot_mobileNo) TextInputEditText edtMobileNumber;
    @BindView(R.id.btn_forgotPass_Next) MaterialButton btnForgotPassNext;
    @BindView(R.id.ll_forgotPass_3) LinearLayoutCompat llForgotPass3;
    @BindView(R.id.tv_forgotPass_verifyOTP) TextInputLayout textInputLayoutOTP;
    @BindView(R.id.edt_forgotPass_verifyOTP) TextInputEditText edtOTP;
    @BindView(R.id.btn_forgotPass_VerifyOTP) MaterialButton btnVerifyOTP;
    @BindView(R.id.ll_resend_otp) LinearLayoutCompat llResendOTP;
    @BindView(R.id.btn_forgotPass_ResendOTP) MaterialButton btnResendOTP;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.tv_resendUserOtp_counter) AppCompatTextView tv_resendUserOtpCounter;


    private int apiOtp;
    private int user_id;
    private String TAG = "ForgotPasswordActivity", countryPhoneCode = "91",API_TOKEN_EXTERNAL="";
    private AppCompatActivity context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ButterKnife.bind(this);
        context = ForgotPasswordActivity.this;
        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null) {
            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.forgot_password));

            getSupportActionBar().setTitle(getString(R.string.forgot_password));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setElevation(0); // to disable action bar elevation
        }


        btnResendOTP.setTextColor(getResources().getColor(R.color.main_grey));
        btnResendOTP.setBackgroundColor(getResources().getColor(R.color.color_Gray));

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        API_TOKEN_EXTERNAL = sharedPreferences.getString("API_TOKEN_EXTERNAL", "");
        editor.apply();

        //hide pb
        hideProgressBar();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnClickEvent();
    }


    public void setOnClickEvent() {
        btnForgotPassNext.setOnClickListener(v -> checkEmailId());

        btnVerifyOTP.setOnClickListener(v -> verifyOTP());

        btnResendOTP.setOnClickListener(v -> resendOTP());
    }

    private void checkEmailId() {

        if (Objects.requireNonNull(edtMobileNumber.getText()).toString().trim().isEmpty()) {
            new Helper().showCustomToast(this, "Please enter your Mobile Number!");
        }
        else if (isValidPhone(edtMobileNumber))
        {


            if (isNetworkAvailable(this)) {
                showProgressBar(getString(R.string.please_wait));
                JsonObject postParameter = new JsonObject();
                postParameter.addProperty("username", countryPhoneCode + Objects.requireNonNull(edtMobileNumber.getText()).toString());
                postParameter.addProperty("api_token", API_TOKEN_EXTERNAL);

                ApiClient client = ApiClient.getInstance();
                Call<JsonObject> call = client.getApiService().ForgotPasswordSendOTP(postParameter);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().isJsonObject()) {
                                int isSuccess = 0;

                                if (response.body().has("success")) {
                                    isSuccess = response.body().get("success").getAsInt();
                                }
                                if (isSuccess == 1) {
                                    if (response.body().has("data")) {
                                        JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                        goToVerifyOTP();
                                        setJson(jsonObject);
                                    }
                                }else {
                                    String msg = null;
                                    if (response.body().has("msg"))
                                        msg = response.body().get("msg").getAsString();
                                    if (msg != null) showErrorLog(msg);
                                }
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
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
            } else NetworkError(this);

        } else {
            textInputLayoutMobile.setError("Please Enter Valid Mobile Number");
        }
    }

    private void verifyOTP() {

        if (!Objects.requireNonNull(edtOTP.getText()).toString().trim().isEmpty()) {
            try {
                int newOtp = Integer.parseInt(edtOTP.getText().toString());
                if (newOtp == apiOtp) {
                    goToUpdatePassword();
                } else new Helper().showCustomToast(context, "OTP did not match! Try again!");

            } catch (Exception ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "Please enter number only!");
            }
        } else new Helper().showCustomToast(context, "Please enter OTP number!");

    }


    private void resendOTP() {

        showProgressBar(getString(R.string.resending_otp));

        tv_resendUserOtpCounter.setVisibility(View.VISIBLE);

        startCountDownTimer(tv_resendUserOtpCounter);

        btnResendOTP.setEnabled(false);
        btnResendOTP.setTextColor(getResources().getColor(R.color.main_grey));
        btnResendOTP.setBackgroundColor(getResources().getColor(R.color.color_Gray));
        btnResendOTP.postDelayed(() -> {
            btnResendOTP.setEnabled(true);
            btnResendOTP.setTextColor(getResources().getColor(R.color.main_white));
            btnResendOTP.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }, 60000);

        JsonObject postParameter = new JsonObject();
        postParameter.addProperty("username", countryPhoneCode + Objects.requireNonNull(edtMobileNumber.getText()).toString());
        postParameter.addProperty("api_token", API_TOKEN_EXTERNAL);

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().ForgotPasswordSendOTP(postParameter);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isJsonObject()) {
                        int isSuccess = 0;

                        if (response.body().has("success")) {
                            isSuccess = response.body().get("success").getAsInt();
                        }
                        if (isSuccess == 1) {
                            if (response.body().has("data")) {
                                hideProgressBar();
                                JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                setJson(jsonObject);
                            }
                        }else {
                            String msg = "Error occurred during resend OTP";
                            showErrorLog(msg);

                        }
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
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
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
    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("otp")) apiOtp = !jsonObject.get("otp").isJsonNull() ? jsonObject.get("otp").getAsInt() : 0 ;
        if (jsonObject.has("user_id")) user_id = !jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 ;
        //final int finalOtp = apiOTP;

        showToast();

    }

    private void showToast() {
        if(context!=null)
        {
            runOnUiThread(() -> new Helper().showCustomToastLong(context, "OTP : "+""+apiOtp));
        }
    }

    private void startCountDownTimer(final AppCompatTextView tv_countDownTime)
    {

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_countDownTime.setText(hmsTimeFormatter(millisUntilFinished));
            }

            @Override
            public void onFinish() {

                tv_countDownTime.setText("00:00");
                new Handler().postDelayed(() -> tv_countDownTime.setVisibility(View.GONE), 1000);
            }

        }.start();
        countDownTimer.start();
    }

    private String hmsTimeFormatter(long milliSeconds)
    {

        /*return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));*/

        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }


    private void goToUpdatePassword() {

        startActivity(new Intent(context, ChangePasswordActivity.class)
                .putExtra("MobileNumber", countryPhoneCode + Objects.requireNonNull(edtMobileNumber.getText()).toString())
                .putExtra("user_id", user_id));

        finish();

    }


    private void goToVerifyOTP() {

        hideProgressBar();
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.trans_right_out);
        animation.setDuration(500);
        llForgotPass1.setAnimation(animation);
        llForgotPass1.animate();
        animation.start();
        llForgotPass1.setVisibility(View.GONE);

        Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.trans_right_in);
        animation2.setDuration(500);
        llForgotPass3.setAnimation(animation2);
        llForgotPass3.animate();
        animation2.start();
        llForgotPass3.setVisibility(View.VISIBLE);
        btnResendOTP.setEnabled(false);
        btnResendOTP.setTextColor(getResources().getColor(R.color.main_grey));
        btnResendOTP.setBackgroundColor(getResources().getColor(R.color.color_Gray));

        startCountDownTimer(tv_resendUserOtpCounter);
        btnResendOTP.postDelayed(() -> {btnResendOTP.setEnabled(true);
            btnResendOTP.setTextColor(getResources().getColor(R.color.main_white));
            btnResendOTP.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        }, 60000);


    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context, message);
        });

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


 /*   public static boolean isValidEmail(CharSequence target) {
        return (Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
*/
 private boolean isValidPhone(EditText phone)
 {
     boolean ret = true;
     if (!Validation.isPhoneNumber(phone, true)) ret = false;
     return ret;
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
