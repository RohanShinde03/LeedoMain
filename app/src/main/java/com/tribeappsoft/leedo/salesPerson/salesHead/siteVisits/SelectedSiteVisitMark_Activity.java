package com.tribeappsoft.leedo.salesPerson.salesHead.siteVisits;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leadreassign.model.SalesPersonModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.siteVisits.model.SiteVisitListModel;
import com.tribeappsoft.leedo.util.FlowLayout;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;


public class SelectedSiteVisitMark_Activity extends AppCompatActivity {

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
        private int sales_person_id=0;
        private String TAG = "SelectAssignmentLogicActivity",api_token = "",sales_person_name="";
        private ArrayList<SiteVisitListModel> multiSelect_list;
       // private ArrayList<Integer> userIdsArrayList;
        private ArrayList<Integer> leadIdsArrayList;
        private ArrayList<Integer> PrevSalesPersonIdArrayList;
        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_select_single_sales_person);
            ButterKnife.bind(this);
            context= SelectedSiteVisitMark_Activity.this;

            if (getSupportActionBar()!=null) {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.layout_ab_center);
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_leads_assignments));

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
            //user_id = sharedPreferences.getInt("user_id", 0);
            //boolean applicationCreated = sharedPreferences.getBoolean("applicationCreated", false);
            editor.apply();

            multiSelect_list = new ArrayList<>();
            //userIdsArrayList = new ArrayList<>();
            leadIdsArrayList = new ArrayList<>();
            PrevSalesPersonIdArrayList = new ArrayList<>();

            if (getIntent()!=null) {
                multiSelect_list.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("multiSelect_list")));
                leadIdsArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("leadIdsArrayList")));
                PrevSalesPersonIdArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("PrevSalesPersonIdArrayList")));
            }

            //set selected leads
            setSelectedLeads();

            //select multi-sales persons


           // mTv_selectMultiSalesPerson.setOnClickListener(view -> startActivityForResult(new Intent(context, SelectSalesPersonsActivity.class), 193));
            mTv_selectMultiSalesPerson.setOnClickListener(view -> {

                //hide keyboard
                Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

               /* startActivity(new Intent(context, CustomerIdActivity.class)
                        .putExtra("fromSiteVisit_or_token", 1)
                        .putExtra("forId", 1));
                finish();*/



               // startActivityForResult(new Intent(context, SelectSalesPersonsActivity.class), 193);

                Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
                startActivityForResult(new Intent(context, SelectSalesPersonsActivity.class)
                        .putExtra("from_or_to", 2), 192);
                //finish();
            });



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
            mTv_cuIdNumber.setText(multiSelect_list.get(position).getLead_uid());

            return rowView;
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

    /*        if (requestCode == 192  && resultCode  == RESULT_OK)
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

            }*/
                if ( requestCode == 192  && resultCode  == RESULT_OK) {
                // To sales person
                Objects.requireNonNull(data).getStringExtra("result");
                SalesPersonModel salesPersonModel = (SalesPersonModel) data.getSerializableExtra("result");
                Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );

                // assert salesPersonModel != null;
                //new Helper().showCustomToast(context, "selected "+ salesPersonModel.getFull_name() );


                //To Sales Person Activity
                sales_person_id = Objects.requireNonNull(salesPersonModel).getUser_id();
                sales_person_name = salesPersonModel.getFull_name();

                Log.e(TAG, "onResume: To "+salesPersonModel.getUser_id()+" "+salesPersonModel.getFull_name());
                mTv_selectMultiSalesPerson.setText(sales_person_name !=null && !sales_person_name.trim().isEmpty() ? sales_person_name :"Sales Person");

                //check button enabled
                checkButtonEnabled();
            }

        }


        private void checkValidations()
        {

            //check if sales persons selected
            Log.e(TAG, "checkValidations: "+sales_person_id);
            if (sales_person_id==0) new Helper().showCustomToast(context, "Please select Sales Person!");

            else {
                //show confirmation dialog
                showSubmitLeadsAlertDialog();
            }
        }


        private void checkButtonEnabled() {

            //project id and dates are null
            if (sales_person_id==0 ) setButtonDisabledView();

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


    private void showSubmitLeadsAlertDialog()
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

        tv_msg.setText(getString(R.string.que_re_assign_leads));
        //  String str = TextUtils.join(", ", recyclerAdapter.getSalesUserNames());
        tv_desc.setText(getString(R.string.re_assign_leads_confirmation, sales_person_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.assigning_leads_to_sales_person));
                //api_call
                call_postMarksAsUnclaimedLeads();
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

    private void call_postMarksAsUnclaimedLeads()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id", sales_person_id);
        jsonObject.add("prev_sales_persons", new Gson().toJsonTree(PrevSalesPersonIdArrayList).getAsJsonArray());
        jsonObject.add("leads", new Gson().toJsonTree(leadIdsArrayList).getAsJsonArray());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().siteVisitLeadsTransfer(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1) {
                            onSuccessAssignment();
                        }
                        else {
                            showErrorLog("Failed to Reassign the leads!");
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
                new Helper().showSuccessCustomToast(context, getString(R.string.lead_re_assigned_successfully));

                //put an update flag into sharedPref
                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("isSiteLeadTransfer", true);
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

