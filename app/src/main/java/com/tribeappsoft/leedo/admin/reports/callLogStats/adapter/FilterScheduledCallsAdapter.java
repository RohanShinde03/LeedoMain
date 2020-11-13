package com.tribeappsoft.leedo.admin.reports.callLogStats.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.callLog.TelephonyCallService;
import com.tribeappsoft.leedo.admin.callSchedule.AddCallScheduleActivity;
import com.tribeappsoft.leedo.admin.callSchedule.model.ScheduledCallsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FilterScheduledCallsAdapter extends RecyclerView.Adapter<FilterScheduledCallsAdapter.MyHolder> {
    private static final String TAG ="FilterSchCallsAdapter" ;
    public Activity context;
    public ArrayList<ScheduledCallsModel> itemArrayList,multiSelect_list;
    private final Animations anim;
    private int lastPosition = -1;
    private boolean isFromSchedule;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    // private int count=0;

    public FilterScheduledCallsAdapter(Activity context, ArrayList<ScheduledCallsModel> modelArrayList, boolean isFromSchedule) {
        this.context = context;
        this.itemArrayList = modelArrayList;
        this.isFromSchedule = isFromSchedule;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_scheduled_calls, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        /*SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();*/
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        int user_id = sharedPreferences.getInt("user_id", 0);
        String api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        //set animation
        setAnimation(holder.ll_claimedLeads_main, position);

        final ScheduledCallsModel model= itemArrayList.get(position);
        //holder.mCb_itemSelectLeads.setChecked(model.isChecked());
        //tag date
        holder.tv_ownDate.setText(model.getCreated_at() != null && !model.getCreated_at().trim().isEmpty()? model.getCreated_at(): "");
        holder.tvMobileNumber.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        holder.tv_LeadName.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name(): "--");
        holder.tv_LeadMobile.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        holder.tv_ProjectName.setText((model.getProject_name() != null && !model.getProject_name().trim().isEmpty()) || (model.getUnit_category() != null && !model.getUnit_category().trim().isEmpty()) ? model.getProject_name()+" | "+model.getUnit_category() : "--");
        holder.tv_SalesPersonName.setText((model.getSales_person_name() != null && !model.getSales_person_name().trim().isEmpty())  ? model.getSales_person_name() : "--");
        holder.tv_LeadsOwnTag.setText(model.getLead_types_name() != null && !model.getLead_types_name().trim().isEmpty()? model.getLead_types_name(): "--");
        holder.tv_LeadMobile.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        holder.tv_ScheduledBy.setText(model.getSales_person_name() != null && !model.getSales_person_name().trim().isEmpty()? model.getSales_person_name(): "--");
        holder.tv_ScheduledOnDate.setText(model.getScheduled_on() != null && !model.getScheduled_on().trim().isEmpty()? model.getScheduled_on(): "--");
        // holder.tv_CallStatus.setText(model.getStatus() != null && !model.getStatus().trim().isEmpty()? model.getStatus(): "Scheduled");
        // holder.tv_Remarks.setText(model.getRemark() != null && !model.getRemark().trim().isEmpty()? model.getRemark(): "No Remarks");
        //  holder.mTv_TotalCallsCount.setText(model.getTotalCalls() != null && !model.getTotalCalls().trim().isEmpty()? model.getTotalCalls(): "4");
        holder.tv_LeadsOwnTag.setText(model.getLead_types_name() != null && !model.getLead_types_name().trim().isEmpty()? model.getLead_types_name(): "");
        //holder.ll_LeadType.setVisibility(model.getLead_types_name() != null && !model.getLead_types_name().trim().isEmpty()? View.VISIBLE :View.GONE);

        holder.ll_scheduledBy.setVisibility(isSalesHead || isAdmin ? View.VISIBLE : View.GONE);
        holder.mBtn_reschedule.setVisibility(isFromSchedule && model.getSales_person_id() == user_id ? View.GONE : View.GONE);
        holder.view_callScheduleMain.setVisibility(isFromSchedule ? View.GONE : View.VISIBLE);


        if(model.getSchedule_status_id()==1) {
            holder.tv_ScheduledStatus.setText(R.string.scheduled);
        }
        else if(model.getSchedule_status_id()==2) {
            holder.tv_ScheduledStatus.setText(R.string.rescheduled);
        }
        else if(model.getSchedule_status_id()==4) {
            holder.tv_ScheduledStatus.setText(R.string.cancelled);
        }
        else  holder.tv_ScheduledStatus.setText("--");

        //holder.mBtn_reschedule.setVisibility(isSalesHead ? View.VISIBLE : View.GONE);

        //
        holder.mBtn_reschedule.setOnClickListener(view -> {

            Intent intent = new Intent(context, AddCallScheduleActivity.class);
            intent.putExtra("customer_name", model.getFull_name());
            intent.putExtra("lead_cu_id", model.getLead_uid());
            intent.putExtra("lead_id", model.getLead_id());
            intent.putExtra("lead_status_id", model.getLead_status_id());
            intent.putExtra("prev_call_schedule_id", model.getPrev_call_schedule_id());
            intent.putExtra("fromReschedule",true);
            context.startActivity(intent);
        });

        //mobile number/call
        holder.iv_ownLeadCall.setOnClickListener(v -> {
            if (model.getMobile_number()!=null) {
                //new Helper().openPhoneDialer(Objects.requireNonNull(context), model.getMobile_number());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkCallPermissions()) prepareToMakePhoneCall(api_token,user_id, model.getLead_id(), model.getLead_uid(), model.getLead_status_id(), model.getPrev_call_schedule_id(), model.getMobile_number(), model.getFull_name(),model.getProject_name());
                    else requestPermissionCall();
                }
                else prepareToMakePhoneCall(api_token,user_id, model.getLead_id(), model.getLead_uid(), model.getLead_status_id(), model.getPrev_call_schedule_id(), model.getMobile_number(), model.getFull_name(),model.getProject_name());

            }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
        });


        //Set Lead Details
        if (model.getDetailsTitleModelArrayList()!=null && model.getDetailsTitleModelArrayList().size()>0)
        {

            holder.ll_leadDetailsMain.setVisibility(View.VISIBLE);
            holder.ll_ownAddLeadDetails.removeAllViews();
            for (int i =0 ; i< model.getDetailsTitleModelArrayList().size(); i++)
            {
                //Log.e("ll_HomeFeed_own_", "onBindViewHolder: "+myModel.getDetailsTitleModelArrayList().size());

                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_item_leads_title, null );
                final AppCompatTextView tv_leads_tag_details_title_text = rowView_sub.findViewById(R.id.tv_itemLeadDetails_title);
                final LinearLayoutCompat ll_addDetails = rowView_sub.findViewById(R.id.ll_itemLeadDetails_addDetails);
                tv_leads_tag_details_title_text.setText( model.getDetailsTitleModelArrayList().get(i).getLead_details_title());
                ll_addDetails.removeAllViews();
                ArrayList<LeadDetailsModel> detailsModelArrayList =  model.getDetailsTitleModelArrayList().get(i).getLeadDetailsModels();
                if (detailsModelArrayList!=null && detailsModelArrayList.size()>0)
                {
                    for (int j=0; j<detailsModelArrayList.size(); j++)
                    {
                        //Log.e("ll_HomeFeed_own_", "detailsModelArrayList.get(j).getLead_details_text() "+detailsModelArrayList.get(j).getLead_details_text());
                        @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null );
                        final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                        final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);

                        tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                        tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                        ll_addDetails.addView(rowView_subView);
                    }
                }
                holder.ll_ownAddLeadDetails.addView(rowView_sub);
            }

        } else holder.ll_leadDetailsMain.setVisibility(View.VISIBLE);




        //set expand Collapse Own
        holder.ll_leadDetailsMain.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_leadDetails_ec, false);
                collapse(holder.ll_ownViewLeadDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_leadDetails_ec, true);
                expandSubView(holder.ll_ownViewLeadDetails);
                model.setExpandedOwnView(true);
            }
        });

        //set expand Collapse Own
        holder.cv_main.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_leadDetails_ec, false);
                collapse(holder.ll_ownViewLeadDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_leadDetails_ec, true);
                expandSubView(holder.ll_ownViewLeadDetails);
                model.setExpandedOwnView(true);
            }
        });

    }

    private void expandSubView(final View v) {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime == 1)
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }


    private void collapse(final View v) {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    //check call permission
    private boolean checkCallPermissions() {
        return  (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    //request camera permission
    private void requestPermissionCall()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CALL_PHONE)
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_PHONE_STATE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.PROCESS_OUTGOING_CALLS))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, context.getString(R.string.call_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, CALL_PERMISSION_REQUEST_CODE);
    }

    private void prepareToMakePhoneCall(String api_token, int user_id, int call_lead_id, String call_cuID, int call_lead_status_id, int call_schedule_id, String customer_mobile, String lead_name, String project_name) {

        Log.e(TAG, "prepareToMakePhoneCall: call_lead_id"+call_lead_id );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG,"Project Name 1: "+project_name +" lead name : "+lead_name+" leadcuid : "+customer_mobile);
            //start the startForegroundService first
            context.startForegroundService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("lead_cu_id", customer_mobile)
                    .putExtra("lead_name", lead_name)
                    .putExtra("project_name",project_name)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );

        } else {

            Log.e(TAG,"Project Name 1: "+project_name +" lead name : "+lead_name+" leadcuid : "+customer_mobile);
            //start the service first
            context.startService(new Intent(context, TelephonyCallService.class)
                    .putExtra("call_lead_id",call_lead_id)
                    .putExtra("lead_status_id",call_lead_status_id)
                    .putExtra("call_schedule_id",call_schedule_id)
                    .putExtra("user_id",user_id)
                    .putExtra("lead_cu_id", customer_mobile)
                    .putExtra("lead_name", lead_name)
                    .putExtra("project_name",project_name)
                    .putExtra("api_token",api_token)
                    .putExtra("from_make_phone_Call",true)
                    .putExtra("callCompletedAdded",true)
            );
        }


        new Helper().showSuccessCustomToast(context, "Calling from Lead Management App...!");
        new Handler().postDelayed(() -> new Helper().makePhoneCall(context, "+91"+customer_mobile), 2000);

/*
        //initialise shared pref
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //put the call_lead_id and call_cuID in sharedPref
        editor.putInt("call_lead_id", call_lead_id);
        editor.putString("call_cuID", call_cuID);
        editor.putInt("lead_status_id", call_lead_status_id);
        editor.putInt("call_schedule_id", call_schedule_id);
        editor.putBoolean("from_make_phone_Call", true);
        editor.putBoolean("callCompletedAdded", true);
        editor.apply();*/

    }

    @Override
    public int getItemCount() {
        return (null != itemArrayList ? itemArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }



    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_CallList_LeadWhatsApp) AppCompatImageView iv_ownLeadWhatsApp;
        @BindView(R.id.iv_CallList_LeadCall) AppCompatImageView iv_ownLeadCall;
        @BindView(R.id.iv_CallList_Remark_ec) AppCompatImageView iv_leadDetails_ec;
        @BindView(R.id.ll_CallList_leadDetailsMain) LinearLayoutCompat ll_leadDetailsMain;
        @BindView(R.id.ll_CallList_ownAddLeadDetails)  LinearLayoutCompat ll_ownAddLeadDetails;
        @BindView(R.id.view_callScheduleMain)  View view_callScheduleMain;
        @BindView(R.id.ll_CallList_ownViewLeadDetails) LinearLayoutCompat ll_ownViewLeadDetails;
        @BindView(R.id.ll_cardMain) LinearLayoutCompat ll_cardMain;
        @BindView(R.id.cv_CallList_main) MaterialCardView cv_main;
        @BindView(R.id.ll_CallList_main) LinearLayoutCompat ll_claimedLeads_main;
        @BindView(R.id.ll_CallList_scheduledBy) LinearLayoutCompat ll_scheduledBy;
        //@BindView(R.id.ll_CallList_LeadType) LinearLayoutCompat ll_LeadType;
        @BindView(R.id.tv_CallList_Tag) AppCompatTextView tv_LeadsOwnTag;
        @BindView(R.id.tv_CallList_SalesPersonName) AppCompatTextView tv_SalesPersonName;
        @BindView(R.id.tv_CallList_MobileNumber) AppCompatTextView tvMobileNumber;
        @BindView(R.id.tv_CallList_ownDate) AppCompatTextView tv_ownDate;
        @BindView(R.id.tv_CallList_Remarks) AppCompatTextView tv_Remarks;
        @BindView(R.id.tv_CallList_LeadName) AppCompatTextView tv_LeadName;
        @BindView(R.id.tv_CallList_LeadMobile) AppCompatTextView tv_LeadMobile;
        @BindView(R.id.tv_CallList_ScheduledBy) AppCompatTextView tv_ScheduledBy;
        @BindView(R.id.tv_CallList_ScheduledOnDate) AppCompatTextView tv_ScheduledOnDate;
        @BindView(R.id.tv_CallList_ProjectName) AppCompatTextView tv_ProjectName;
        @BindView(R.id.mBtn_callList_reschedule) MaterialButton mBtn_reschedule;
        @BindView(R.id.mTv_CallList_TotalCallsCount) MaterialTextView mTv_TotalCallsCount;
        @BindView(R.id.tv_CallList_ScheduledStatus) AppCompatTextView tv_ScheduledStatus;

        /*@BindView(R.id.mTv_itemSelectLeads_leadName) MaterialTextView mTv_leadName;
        @BindView(R.id.mTv_itemSelectLeads_leadMob) MaterialTextView mTv_leadMob;
        @BindView(R.id.mCb_itemSelectLeads) MaterialCheckBox mCb_itemSelectLeads;*/

        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
