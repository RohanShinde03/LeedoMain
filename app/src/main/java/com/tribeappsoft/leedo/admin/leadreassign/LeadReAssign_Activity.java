package com.tribeappsoft.leedo.admin.leadreassign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.button.MaterialButton;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leadreassign.model.SalesPersonModel;
import com.tribeappsoft.leedo.util.Helper;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LeadReAssign_Activity extends AppCompatActivity {

    Activity context;
    @BindView(R.id.tv_leadReassign_FromSalesPerson) AppCompatTextView tv_FromSalesPerson;
    @BindView(R.id.tv_leadReassign_ToSalesPerson) AppCompatTextView tv_ToSalesPerson;
    @BindView(R.id.mBtn_leadReassign_SearchLeads) MaterialButton mBtn_SearchLeads;
    private int from_sales_person_id =0, to_sales_person_id =0;
    //private SalesPersonModel salesPersonModel = null;
    private String TAG = "LeadReAssign_Activity", from_sales_person_name ="", to_sales_person_name ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_re_assign_);
      //  overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context=LeadReAssign_Activity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_lead_re_assign));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        /* if (getIntent()!=null){
            int  from_or_to =  getIntent().getIntExtra("from_or_to",1);
            Log.e(TAG, "onCreate: from_or_to "+ from_or_to );
            salesPersonModel = (SalesPersonModel) getIntent().getSerializableExtra("salesPersonModel");

            //From SalesPerson selection
            if (salesPersonModel != null) {

                if(from_or_to==1) {
                    //From Sales Person Activity
                    FromSalesPerson_ID = salesPersonModel.getUser_id();
                    FromsalesPerson_Name = salesPersonModel.getFull_name();

                    Log.e(TAG, "onResume From: "+salesPersonModel.getUser_id()+""+salesPersonModel.getFull_name()+"from_or_to"+from_or_to );
                    tv_FromSalesPerson.setText(FromsalesPerson_Name!=null && !FromsalesPerson_Name.trim().isEmpty() ? FromsalesPerson_Name : "Sales Person");
                }
                else if(from_or_to==2) {
                    //From Sales Person Activity
                    ToSalesPerson_ID = salesPersonModel.getUser_id();
                    TosalesPerson_Name = salesPersonModel.getFull_name();

                    Log.e(TAG, "onResume: To "+salesPersonModel.getUser_id()+""+salesPersonModel.getFull_name()+"from_or_to"+from_or_to);
                    tv_ToSalesPerson.setText(TosalesPerson_Name!=null && !TosalesPerson_Name.trim().isEmpty() ? TosalesPerson_Name :"Sales Person");
                }

                //check Button Enabled View
                checkButtonEnabled();
            }
        }*/

        //From Sales Person ->1
        tv_FromSalesPerson.setOnClickListener(view -> {

            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            startActivityForResult(new Intent(context, SelectSalesPersonActivity.class)
                    .putExtra("repeat_sales_person_id",to_sales_person_id)
                    .putExtra("from_or_to", 1), 191);
            //finish();
        });


        //To Sales Person ->2
        tv_ToSalesPerson.setOnClickListener(view -> {

            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            startActivityForResult(new Intent(context, SelectSalesPersonActivity.class)
                    .putExtra("repeat_sales_person_id",from_sales_person_id)
                    .putExtra("from_or_to", 2), 192);
            //finish();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 191  && resultCode  == RESULT_OK) {
            /// From sales person
            Objects.requireNonNull(data).getStringExtra("result");

            SalesPersonModel salesPersonModel = (SalesPersonModel) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+ Objects.requireNonNull(salesPersonModel).getUser_id() +" \n"+salesPersonModel.getFull_name() );

            //new Helper().showCustomToast(context, "selected "+ salesPersonModel.getFull_name() );

            from_sales_person_id = salesPersonModel.getUser_id();
            from_sales_person_name = salesPersonModel.getFull_name();

            Log.e(TAG, "onResume From: "+salesPersonModel.getUser_id()+" "+salesPersonModel.getFull_name());
            tv_FromSalesPerson.setText(from_sales_person_name !=null && !from_sales_person_name.trim().isEmpty() ? from_sales_person_name : "Sales Person");

        }
        else if ( requestCode == 192  && resultCode  == RESULT_OK) {
            // To sales person
            Objects.requireNonNull(data).getStringExtra("result");
            SalesPersonModel salesPersonModel = (SalesPersonModel) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("result") );

           // assert salesPersonModel != null;
            //new Helper().showCustomToast(context, "selected "+ salesPersonModel.getFull_name() );

            //To Sales Person Activity
            to_sales_person_id = Objects.requireNonNull(salesPersonModel).getUser_id();
            to_sales_person_name = salesPersonModel.getFull_name();

            Log.e(TAG, "onResume: To "+salesPersonModel.getUser_id()+" "+salesPersonModel.getFull_name());
            tv_ToSalesPerson.setText(to_sales_person_name !=null && !to_sales_person_name.trim().isEmpty() ? to_sales_person_name :"Sales Person");

        }

       /* else if (requestCode == 191 && resultCode == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }
        else if (requestCode == 192  && resultCode  == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }*/
        else if (requestCode == 193  && resultCode  == RESULT_OK)
        {
            //lead assigned successfully
            from_sales_person_id = 0;
            from_sales_person_name = "";

            to_sales_person_id = 0;
            to_sales_person_name ="";

            tv_FromSalesPerson.setText("");
            tv_ToSalesPerson.setText("");
        }

        //check button enabled
        checkButtonEnabled();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //Search Sales Person when from and to ids not equal
        mBtn_SearchLeads.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

            if(from_sales_person_id !=0 && to_sales_person_id !=0)
            {
                if(from_sales_person_id != to_sales_person_id)
                {
                    startActivityForResult(new Intent(context, SelectLeadsActivity.class)
                        .putExtra("from_sales_person_id", from_sales_person_id)
                        .putExtra("from_sales_person_name", from_sales_person_name)
                        .putExtra("to_sales_person_id", to_sales_person_id)
                        .putExtra("to_sales_person_name", to_sales_person_name), 193);
                    //finish();
                } else new Helper().showCustomToast(context, "Both sales persons are same! Please select different one!");

            }else new Helper().showCustomToast(context, "Please select Sales Person!");
        });
    }

    private void checkButtonEnabled()
    {
        //name From sales id
        if (from_sales_person_id ==0) setButtonDisabledView();

            //name To sales id
        else if (to_sales_person_id ==0) setButtonDisabledView();
        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_SearchLeads.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_SearchLeads.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {
        // All validations are not checked
        // disable btn for submit team lead

        mBtn_SearchLeads.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_SearchLeads.setTextColor(getResources().getColor(R.color.main_white));
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
       // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
