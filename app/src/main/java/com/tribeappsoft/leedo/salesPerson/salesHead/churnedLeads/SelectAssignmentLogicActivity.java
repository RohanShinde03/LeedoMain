package com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.util.FlowLayout;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectAssignmentLogicActivity extends AppCompatActivity {

    @BindView(R.id.cl_selectAssignmentLogic) CoordinatorLayout parent;
    @BindView(R.id.ll_selectAssignmentLogic_main) LinearLayoutCompat ll_main;

    @BindView(R.id.rdoGrp_selectAssignmentLogic) RadioGroup rdoGrp_selectAssignmentLogic;
    @BindView(R.id.mRb_selectAssignmentLogic_manual) MaterialRadioButton mRb_manual;
    @BindView(R.id.mRb_selectAssignmentLogic_auto) MaterialRadioButton mRb_auto;

    @BindView(R.id.ll_selectAssignmentLogic_auto) LinearLayoutCompat ll_auto;
    @BindView(R.id.acTv_selectAssignmentLogic_selectAutoLogicType) AutoCompleteTextView acTv_selectAutoLogicType;
    @BindView(R.id.mTv_selectAssignmentLogic_selectMultiSalesPerson) MaterialTextView mTv_selectMultiSalesPerson;

    @BindView(R.id.ll_selectAssignmentLogic_manual) LinearLayoutCompat ll_manual;
    @BindView(R.id.mTv_selectAssignmentLogic_selectSingleSalesPerson) MaterialTextView mTv_selectSingleSalesPerson;

    @BindView(R.id.ll_selectAssignmentLogic_selectedLeads) LinearLayoutCompat ll_selectedLeads;
    @BindView(R.id.flowLayout_selectAssignmentLogic)
    FlowLayout flowLayout;

    @BindView(R.id.mBtn_selectAssignmentLogic_submit) MaterialButton mBtn_submit;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private AppCompatActivity context;
    private int user_id = 0;
    private String TAG = "SelectAssignmentLogicActivity",api_token = "";
    private ArrayList<LeadListModel>  multiSelect_list;
    private ArrayList<Integer> userIdsArrayList;
    private ArrayList<Integer> leadIdsArrayList;
    private  SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_assignment_logic);

        ButterKnife.bind(this);
        context= SelectAssignmentLogicActivity.this;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_manual_assignment));

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
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
        editor.apply();

        multiSelect_list = new ArrayList<>();
        userIdsArrayList = new ArrayList<>();
        leadIdsArrayList = new ArrayList<>();

        if (getIntent()!=null) {
            multiSelect_list.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("multiSelect_list")));
            leadIdsArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("leadIdsArrayList")));
        }

        //set selected leads
        setSelectedLeads();

        //select multi-sales persons
        mTv_selectMultiSalesPerson.setOnClickListener(view -> startActivityForResult(new Intent(context, SelectMultiSalesPersonsActivity.class), 193));

        mBtn_submit.setOnClickListener(view -> {
            //check validation
            checkValidations();
        });

    }


    private void setSelectedLeads()
    {
        if (multiSelect_list!=null && multiSelect_list.size()>0) {
            flowLayout.removeAllViews();
            for (int i = 0; i < multiSelect_list.size(); i++) {
                View rowView_sub = getLeadsView(i);
                flowLayout.addView(rowView_sub);
            }

            //visible layout
            ll_selectedLeads.setVisibility(View.VISIBLE);
        }
        else  ll_selectedLeads.setVisibility(View.GONE);

    }

    private View getLeadsView(int position) {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_selected_lead, null );
        final MaterialTextView mTv_cuIdNumber = rowView.findViewById(R.id.mTv_selectedLead_cuIdNumber);
        mTv_cuIdNumber.setText(multiSelect_list.get(position).getLead_cuid_number());

        return rowView;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 193  && resultCode  == RESULT_OK)
        {
            //sales person's selected
            Objects.requireNonNull(data).getStringExtra("result");
            Objects.requireNonNull(data).getStringExtra("result_names");

            userIdsArrayList =  new ArrayList<>(Objects.requireNonNull(data.getParcelableArrayListExtra("result")));
            ArrayList<String> userNamesArrayList = new ArrayList<>(Objects.requireNonNull(data.getParcelableArrayListExtra("result_names")));

            //String user_name  = Arrays.toString(Objects.requireNonNull(userNamesArrayList).toArray());
            String user_name = TextUtils.join(", ", userNamesArrayList);
            mTv_selectMultiSalesPerson.setText(!user_name.trim().isEmpty() ? user_name : "Sales Person");

            //check button enabled
            checkButtonEnabled();

        }
    }


    private void checkValidations()
    {

        //check if sales persons selected
        if (userIdsArrayList!=null && userIdsArrayList.size()==0 ) new Helper().showCustomToast(context, "Please select Sales Executive!");

        else {
            //show confirmation dialog
            showManualChurnAlertDialog();
        }
    }


    private void checkButtonEnabled() {

        //project id and dates are null
        if (userIdsArrayList!=null && userIdsArrayList.size()==0 ) setButtonDisabledView();

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

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit team lead
        mBtn_submit.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submit.setTextColor(getResources().getColor(R.color.main_white));
    }


    public void showManualChurnAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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

        tv_msg.setText(getString(R.string.que_auto_churn));
        tv_desc.setText(getString(R.string.msg_manual_churn_leads_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //call api
            if (Helper.isNetworkAvailable(context)) {
                showProgressBar(getString(R.string.manually_assigning_leads));
                call_ManualAssignment();
            }
            else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmLp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmLp.gravity =  Gravity.CENTER;
        wmLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmLp.width = pixel-100;
        //wmLp.x = 100;   //x position
        //wmLp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmLp.height );
        alertDialog.getWindow().setAttributes(wmLp);
    }


    private void call_ManualAssignment()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",user_id);
        jsonObject.add("sales_persons", new Gson().toJsonTree(userIdsArrayList).getAsJsonArray());
        jsonObject.add("leads", new Gson().toJsonTree(leadIdsArrayList).getAsJsonArray());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().leadChurnedByManual(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1) {
                            // String msg = response.body().get("data").getAsString();
                            onSuccessAssignment();
                        }
                        else {
                            showErrorLog("Failed to manually churn the leads!");
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void onSuccessAssignment()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.leads_manually_assigned_successfully));

            //put an update flag into sharedPref
            if (sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putBoolean("isManualUpdate", true);
                editor.apply();
            }

            //do onBackPress
            onBackPressed();
        });
    }



    private void showErrorLog(final String message) {
        if (context != null) {
            context.runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                Helper.onErrorSnack(context, message);

            });
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
