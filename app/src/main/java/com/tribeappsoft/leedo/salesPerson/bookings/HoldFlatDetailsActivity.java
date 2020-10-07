package com.tribeappsoft.leedo.salesPerson.bookings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
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

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class HoldFlatDetailsActivity extends AppCompatActivity
{

    @BindView(R.id.mBtn_holdFlatDetails_holdFlat) MaterialButton btn_hold;
    @BindView(R.id.tv_holdFlatDetails_cuIdNumber) AppCompatTextView tv_cuIdNumber;
    @BindView(R.id.tv_holdFlatDetails_customerName) AppCompatTextView tv_customerName;
    @BindView(R.id.tv_holdFlatDetails_mobileNumber) AppCompatTextView tv_mobileNumber;
    @BindView(R.id.tv_holdFlatDetails_email) AppCompatTextView tv_email;
    @BindView(R.id.tv_holdFlatDetails_tokenNumber) AppCompatTextView tv_tokenNumber;
    @BindView(R.id.tv_holdFlatDetails_unit_type) AppCompatTextView tv_unit_type;
    @BindView(R.id.tv_holdFlatDetails_project_name) AppCompatTextView tv_project_name;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private AppCompatActivity context;
    private String TAG ="HoldFlatDetailsActivity", api_token ="", event_title ="", unit_name ="",
            token_no ="", lead_uid ="", unit_category ="",project_name ="", country_code="91", mobile_number ="",
            email ="", full_name ="";
    private int event_id =0, user_id =0, unit_id =0, token_id = 0, lead_id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holdflat_details);
        ButterKnife.bind(this);
        context = HoldFlatDetailsActivity.this;

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.confirm_hold_flat));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 0);
            event_title = getIntent().getStringExtra("event_title");
            unit_id = getIntent().getIntExtra("unit_id", 0);
            unit_name = getIntent().getStringExtra("unit_name");
            token_id = getIntent().getIntExtra("token_id", 0);
            token_no = getIntent().getStringExtra("token_no");
            lead_id = getIntent().getIntExtra("lead_id", 0);
            lead_uid = getIntent().getStringExtra("lead_uid");
            unit_category = getIntent().getStringExtra("unit_category");
            project_name = getIntent().getStringExtra("project_name");
            country_code = getIntent().getStringExtra("country_code");
            mobile_number = getIntent().getStringExtra("mobile_number");
            email = getIntent().getStringExtra("email");
            full_name = getIntent().getStringExtra("full_name");
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //hide pb
        hideProgressBar();


        //set details
        tv_cuIdNumber.setText(lead_uid);
        tv_customerName.setText(full_name);
        tv_mobileNumber.setText(String.format("+%s - %s", country_code, mobile_number));
        tv_email.setText(email);
        tv_tokenNumber.setText(token_no);
        tv_unit_type.setText(unit_name);
        tv_project_name.setText(project_name);

        tv_email.setVisibility(email!=null && !email.isEmpty() ? View.VISIBLE : View.GONE);
        tv_mobileNumber.setVisibility(mobile_number!=null && !mobile_number.isEmpty() ? View.VISIBLE : View.GONE);


        btn_hold.setOnClickListener(view -> showConfirmationAlert());

    }




    //show confirmation dialog
    public void showConfirmationAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);


        tv_msg.setText(getString(R.string.hold_flat_question));
        tv_desc.setText(getString(R.string.hold_flat_confirmation, full_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar();
                new Thread(this::call_holdFlat).start();

            }else Helper.NetworkError(context);

        });
        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
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



    private void call_holdFlat()
    {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event_id", event_id);
        jsonObject.addProperty("token_id", token_id);
        jsonObject.addProperty("unit_id", unit_id);
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("token_no", token_no);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("api_token",api_token);


        ApiClient client = ApiClient.getInstance();
        client.getApiService().markAsHoldFlat(jsonObject).enqueue(new Callback<JsonObject>()
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
                            onSuccessHoldFlat();
                            /*if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setJson(data, 1);
                                }
                            }*/

                        }
                        else showErrorLog("Failed to add flat onHold!");
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

    private void onSuccessHoldFlat()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            new Helper().showCustomToast(context, "Hold flat successful!");

            //start activity
            startActivity(new Intent(context, HoldFlatListActivity.class)
                    .addFlags(FLAG_ACTIVITY_CLEAR_TOP |  FLAG_ACTIVITY_SINGLE_TOP)
            );
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

    void showProgressBar() {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(getString(R.string.please_wait));
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
    public void onBackPressed()
    {
        super.onBackPressed();
    }

}
