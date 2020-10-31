package com.tribeappsoft.leedo.admin.offlineLeads.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.model.OfflineLeadModel;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DuplicateLeadListAdapter extends RecyclerView.Adapter<DuplicateLeadListAdapter.MyHolder>{

    private Activity context;
    private ArrayList<OfflineLeadModel> offlineLeadModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    //private String TAG="ProjectListAdapter";


    public DuplicateLeadListAdapter(Activity context, ArrayList<OfflineLeadModel> projectModelArrayList) {
        this.context = context;
        this.offlineLeadModelArrayList = projectModelArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_synced_duplicate_leads, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_offlineLeads, position);

        //initialise shared pref
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //String api_token = sharedPreferences.getString("api_token", "");
       // int user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        final OfflineLeadModel model= offlineLeadModelArrayList.get(position);
        holder.tv_LeadName.setText(model.getCustomer_name() != null && !model.getCustomer_name().trim().isEmpty()? model.getCustomer_name() :"-");
        holder.tv_ProjectName.setText(model.getCustomer_project_name() != null && !model.getCustomer_project_name().trim().isEmpty()? model.getCustomer_project_name()+" | "+model.getCustomer_unit_type() :"-");

        holder.tv_LeadSource.setText(model.getLead_types() != null && !model.getLead_types().trim().isEmpty()? " | "+model.getLead_types(): "");
        holder.tv_LeadSource.setVisibility(model.getLead_types() != null && !model.getLead_types().trim().isEmpty()? View.VISIBLE :View.GONE);

        holder.tv_ownLeadStage.setText(model.getLead_stage() != null && !model.getLead_stage().trim().isEmpty()? model.getLead_stage(): "");
        holder.tv_ownLeadStage_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
        holder.ll_leadStage_dot.setVisibility(model.getLead_stage_id()==0? View.GONE :View.VISIBLE);

        holder.mTv_full_name.setText(model.getCustomer_name() != null && !model.getCustomer_name().trim().isEmpty()? model.getCustomer_name() :"-");
        holder.mTv_Mobile_Number.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number() :"-");
        holder.mTv_AlternativeNumber.setText(model.getAlternate_mobile_number() != null && !model.getAlternate_mobile_number().trim().isEmpty()? model.getAlternate_mobile_number() :"-");
        holder.mTv_DateOfBirth.setText(model.getDob() != null && !model.getDob().trim().isEmpty()? model.getDob() :"-");
        holder.mTv_Budget.setText(model.getBudget_limit() != null && !model.getBudget_limit().trim().isEmpty()? model.getBudget_limit() :"-");
        holder.mTv_leadProfession.setText(model.getLead_profession() != null && !model.getLead_profession().trim().isEmpty()? model.getLead_profession() :"-");
        holder.mTv_IncomeRange.setText(model.getIncome_range() != null && !model.getIncome_range().trim().isEmpty()? model.getIncome_range() :"-");
        holder.mTv_IsFirstHome.setText(model.getIs_first_home() ==1? "Yes" :"No");
        holder.mTv_Address.setText(model.getAddress_line_1() != null && !model.getAddress_line_1().trim().isEmpty()? model.getAddress_line_1() :"-");
        holder.mTv_LeadSource.setText(model.getLead_types() != null && !model.getLead_types().trim().isEmpty()? model.getLead_types() :"-");
        holder.mTv_LeadStage.setText(model.getLead_stage() != null && !model.getLead_stage().trim().isEmpty()? model.getLead_stage() :"-");
        holder.mTv_NIReason.setText(model.getLead_ni_reason() != null && !model.getLead_ni_reason().trim().isEmpty()? model.getLead_ni_reason() :"-");
        holder.ll_NiRemarks.setVisibility(model.getLead_stage_id() == 4 && !model.getLead_types().trim().isEmpty()? View.VISIBLE :View.GONE);
        holder.mTv_LeadRemarks.setText(model.getRemarks() != null && !model.getRemarks().trim().isEmpty()? model.getRemarks() :"-");


        switch (model.getLead_stage_id()) {
            case 1:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorhot));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
                break;
            case 2:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorwarm));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
                break;
            case 3:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorcold));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
                break;
            case 4:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.colorni));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.colorni));
                break;
            case 5:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
                break;
            default:
                holder.tv_ownLeadStage.setTextColor(context.getResources().getColor(R.color.BlackLight));
                holder.tv_ownLeadStage_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));

        }


        //set expand Collapse Own
        holder.cv_offlineLeads.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_leadDetails_ec, false);
                collapse(holder.ll_ViewLeadDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_leadDetails_ec, true);
                expandSubView(holder.ll_ViewLeadDetails);
                model.setExpandedOwnView(true);
            }
        });

        //set expand Collapse Own
        holder.iv_leadDetails_ec.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_leadDetails_ec, false);
                collapse(holder.ll_ViewLeadDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_leadDetails_ec, true);
                expandSubView(holder.ll_ViewLeadDetails);
                model.setExpandedOwnView(true);
            }
        });

        holder.iv_update_LeadName.setOnClickListener(v -> context.startActivity(new Intent(context, AddNewLeadActivity.class)
                .putExtra("isUpdateLead",false)
                .putExtra("isDuplicateLead",true)
                .putExtra("lead_id",model.getLead_id())
                .putExtra("offline_id",model.getOffline_id())
                .putExtra("current_lead_status_id",1)
                .putExtra("salesPersonName",model.getSales_person_id())));

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

    @Override
    public int getItemCount() {
        return (null != offlineLeadModelArrayList ? offlineLeadModelArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_offlineLeads_mainList) MaterialCardView cv_offlineLeads;
        @BindView(R.id.tv_itemList_LeadName) AppCompatTextView tv_LeadName;
        @BindView(R.id.tv_itemList_ProjectName) AppCompatTextView tv_ProjectName;
        @BindView(R.id.tv_itemList_LeadSource) AppCompatTextView tv_LeadSource;
        @BindView(R.id.tv_itemList_ownLeadStage) AppCompatTextView tv_ownLeadStage;
        @BindView(R.id.tv_itemList_ownLeadStage_dot) AppCompatTextView tv_ownLeadStage_dot;
        @BindView(R.id.ll_iteList_leadStage_dot) LinearLayoutCompat ll_leadStage_dot;
        @BindView(R.id.iv_itemList_leadDetails_ec) AppCompatImageView iv_leadDetails_ec;
        @BindView(R.id.iv_itemList_update_LeadName) AppCompatImageView iv_update_LeadName;
        @BindView(R.id.ll_itemList_ViewLeadDetails) LinearLayoutCompat ll_ViewLeadDetails;

        /*Lead Details*/
        @BindView(R.id.mTv_itemList_full_name) MaterialTextView mTv_full_name;
        @BindView(R.id.mTv_itemList_Mobile_Number) MaterialTextView mTv_Mobile_Number;
        @BindView(R.id.mTv_itemList_AlternativeNumber) MaterialTextView mTv_AlternativeNumber;
        @BindView(R.id.mTv_itemList_DateOfBirth) MaterialTextView mTv_DateOfBirth;
        @BindView(R.id.mTv_itemList_Budget) MaterialTextView mTv_Budget;
        @BindView(R.id.mTv_itemList_leadProfession) MaterialTextView mTv_leadProfession;
        @BindView(R.id.mTv_itemList_IncomeRange) MaterialTextView mTv_IncomeRange;
        @BindView(R.id.mTv_itemList_IsFirstHome) MaterialTextView mTv_IsFirstHome;
        @BindView(R.id.mTv_itemList_Address) MaterialTextView mTv_Address;
        @BindView(R.id.mTv_itemList_LeadSource) MaterialTextView mTv_LeadSource;
        @BindView(R.id.mTv_itemList_LeadStage) MaterialTextView mTv_LeadStage;
        @BindView(R.id.mTv_itemList_NIReason) MaterialTextView mTv_NIReason;
        @BindView(R.id.mTv_itemList_LeadRemarks) MaterialTextView mTv_LeadRemarks;
        @BindView(R.id.ll_itemList_NiRemarks) LinearLayoutCompat ll_NiRemarks;


        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
