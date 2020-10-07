package com.tribeappsoft.leedo.loginModule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
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

public class ChangePasswordActivity extends AppCompatActivity {

    @BindView(R.id.cl_changePassword) CoordinatorLayout parent;
    @BindView(R.id.til_new_password) TextInputLayout tilStudentNewPassword;
    @BindView(R.id.edt_new_password) TextInputEditText edtStudentNewPassword;
    @BindView(R.id.til_confirm_password) TextInputLayout tilStudentConfirmPassword;
    @BindView(R.id.edt_confirm_password) TextInputEditText edtStudentConfirmPassword;
    @BindView(R.id.btn_change_password) MaterialButton btnStudentChangePassword;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private AppCompatActivity context;
    private int userId=0;
    private String TAG = "ChangePasswordActivity",API_TOKEN_EXTERNAL="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ButterKnife.bind(this);
        context = ChangePasswordActivity.this;

        //call method to hide keyBoard
        setupUI(parent);

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

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        API_TOKEN_EXTERNAL = sharedPreferences.getString("API_TOKEN_EXTERNAL", "");
        editor.apply();

        if (getIntent()!=null) {
            userId = getIntent().getIntExtra("user_id",0);
        }

        Log.e(TAG, "onCreate: "+ userId );
        final Typeface face;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            face = getResources().getFont(R.font.metropolis_light);
        }
        else
        {
            //or to support all versions use
            face=  ResourcesCompat.getFont(this, R.font.metropolis_light);
        }

        tilStudentConfirmPassword.setTypeface(face);
        tilStudentNewPassword.setTypeface(face);

        //hide pb
        hideProgressBar();

    }

    @Override
    protected void onResume() {
        super.onResume();
        btnStudentChangePassword.setOnClickListener(v -> checkValidations());
    }


    private void checkValidations() {

        if (!Objects.requireNonNull(edtStudentNewPassword.getText()).toString().trim().isEmpty())
        {

            if (!Objects.requireNonNull(edtStudentConfirmPassword.getText()).toString().trim().isEmpty())
            {

                final String newPasswordVal = edtStudentNewPassword.getText().toString();
                final String confirmPasswordVal = edtStudentConfirmPassword.getText().toString();
                if (confirmPasswordVal.equals(newPasswordVal))
                {
                    //call APi and change Password
                    final JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("api_token",  API_TOKEN_EXTERNAL);
                    jsonObject.addProperty("password",confirmPasswordVal);
                    jsonObject.addProperty("user_id",userId);

                    if (isNetworkAvailable(context))
                    {
                        showProgressBar(getString(R.string.updating_password));

                        call_changePassword(jsonObject);
                        //onchangePassword();
                    }else NetworkError(context);

                }
                else new Helper().showCustomToast(this , "Passwords does not match!");

            }
            else new Helper().showCustomToast(this, "Please enter confirm password!");

        }else new Helper().showCustomToast(this, "Please enter your new password!");


    }

    private void call_changePassword(JsonObject jsonObject) {

        ApiClient client = ApiClient.getInstance();
        client.getApiService().UpdatePassword(jsonObject).enqueue(new Callback<JsonObject>()
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
                        else showErrorLog("Error Occurred during password change !");
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
                    if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                    else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

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
            //show success toast
            new Helper().showSuccessCustomToast(context , "Password Updated!");
            startActivity(new Intent(context, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
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
