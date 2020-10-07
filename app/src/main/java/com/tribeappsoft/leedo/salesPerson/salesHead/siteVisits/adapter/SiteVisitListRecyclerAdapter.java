package com.tribeappsoft.leedo.salesPerson.salesHead.siteVisits.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.siteVisits.SiteVisitList_Activity;
import com.tribeappsoft.leedo.salesPerson.salesHead.siteVisits.model.SiteVisitListModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SiteVisitListRecyclerAdapter extends RecyclerView.Adapter<SiteVisitListRecyclerAdapter.MyHolder> {
    public SiteVisitList_Activity context;
    public ArrayList<SiteVisitListModel> itemArrayList,multiSelect_list;
    private ArrayList<Integer> leadIdArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    // private int count=0;

    public SiteVisitListRecyclerAdapter(SiteVisitList_Activity context, ArrayList<SiteVisitListModel> modelArrayList, ArrayList<SiteVisitListModel> multiSelect_list) {
        this.context = context;
        this.itemArrayList = modelArrayList;
        this.multiSelect_list = multiSelect_list;
        this.leadIdArrayList = new ArrayList<>();
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_site_visit_list, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.ll_siteVisits_main, position);

        final SiteVisitListModel model= itemArrayList.get(position);
        //holder.mCb_itemSelectLeads.setChecked(model.isChecked());
        //tag date
        holder.tv_ownDate.setText(model.getTagDate() != null && !model.getTagDate().trim().isEmpty()? Helper.getNotificationFormatDate(model.getTagDate()): "");
        holder.tv_CuIdNumber.setText(model.getLead_uid() != null && !model.getLead_uid().trim().isEmpty()? model.getLead_uid(): "--");
        holder.tv_LeadName.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name(): "--");
        holder.tv_LeadMobile.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        holder.tv_ProjectName.setText((model.getProject_name() != null && !model.getProject_name().trim().isEmpty()) || (model.getUnit_category() != null && !model.getUnit_category().trim().isEmpty()) ? model.getProject_name()+" | "+model.getUnit_category() : "--");
        holder.tv_SalesPersonName.setText((model.getSales_person_name() != null && !model.getSales_person_name().trim().isEmpty())  ? model.getSales_person_name() : "--");
        holder.tv_LeadsOwnTag.setText(model.getLead_Type() != null && !model.getLead_Type().trim().isEmpty()? model.getLead_Type(): "--");
        holder.tv_LeadMobile.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        holder.tv_FirstCallMade.setText(model.getCall_done() ==1 ? "Yes" : "No");
        holder.tv_NoOfCallMade.setText(model.getCall_count()!=0 ? String.valueOf(model.getCall_count()): "0");
        holder.tv_AvgCallDuration.setText(model.getAvg_time() != null && !model.getAvg_time().trim().isEmpty()? model.getAvg_time(): "0sec");
        holder.tv_MinCallDuration.setText(model.getMin_call_duration() != null && !model.getMin_call_duration().trim().isEmpty()? model.getMin_call_duration(): "0sec");
        holder.tv_MaxCallDuration.setText(model.getMax_call_duration() != null && !model.getMax_call_duration().trim().isEmpty()? model.getMax_call_duration(): "0sec");
        holder.tv_LeadsOwnTag.setText(model.getLead_Type() != null && !model.getLead_Type().trim().isEmpty()? model.getLead_Type(): "");
        holder.ll_LeadType.setVisibility(model.getLead_Type() != null && !model.getLead_Type().trim().isEmpty()? View.VISIBLE :View.GONE);
        //holder.tv_claimedLeads_cpName.setText(model.getCp_Name() != null && !model.getCp_Name().trim().isEmpty()? model.getCp_Name() :"--");
       // holder.ll_leadDetailsMain.setVisibility(model.getCp_Name() != null && !model.getCp_Name().trim().isEmpty()? View.VISIBLE :View.GONE);
     //   holder.tv_cp_executive_name.setText(model.getCp_executive_name() != null && !model.getCp_executive_name().trim().isEmpty()? model.getCp_executive_name() :"--");
        holder.ll_LeadType.setVisibility(model.getLead_Type() != null && !model.getLead_Type().trim().isEmpty()? View.VISIBLE :View.GONE);

        //mobile number/call
        holder.iv_ownLeadCall.setOnClickListener(v -> {
            if (model.getMobile_number()!=null) {
                new Helper().openPhoneDialer(Objects.requireNonNull(context), model.getMobile_number());
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

        } else holder.ll_leadDetailsMain.setVisibility(View.GONE);




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


        if(multiSelect_list.contains(itemArrayList.get(position))) {
            //holder.iv_bookImg.setImageResource(R.drawable.ic_icon_success);
            holder.ll_cardMain.setBackgroundColor(ContextCompat.getColor(context, R.color.color_ghp_plus_pending));
            holder.ll_siteVisits_main.setBackgroundColor(ContextCompat.getColor(context, R.color.color_other_feed_card_background));
        }
        else {
            holder.ll_cardMain.setBackgroundColor(ContextCompat.getColor(context, R.color.main_white));
            holder.ll_siteVisits_main.setBackgroundColor(ContextCompat.getColor(context, R.color.black_full_transparent));
        }


/*
        if(multiSelect_list.contains(itemArrayList.get(position))) {
            //holder.iv_bookImg.setImageResource(R.drawable.ic_icon_success);
            holder.claimedLeads_list.setBackgroundColor(ContextCompat.getColor(context, R.color.color_ghp_plus_pending));
            holder.ll_ownViewLeadDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.color_token_cancelled));
        }
        else
        {
            holder.claimedLeads_list.setBackgroundColor(ContextCompat.getColor(context, R.color.main_white));
            holder.ll_ownViewLeadDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.main_white));
        }*/

      /*  holder.mCb_itemSelectLeads.setOnClickListener(v -> {

            if(model.isChecked())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.mCb_itemSelectLeads.setChecked(false);
                //update model value
                model.setChecked(false);
                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getLead_id(), false);
                //check arrayList
                checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.mCb_itemSelectLeads.setChecked(true);
                //update model value
                model.setChecked(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getLead_id(), true);
                //check arrayList
                checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        holder.claimedLeads_list.setOnClickListener(view -> {

            if(model.isChecked())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.mCb_itemSelectLeads.setChecked(false);
                //update model value
                model.setChecked(false);
                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getLead_id(), false);
                //check arrayList
                checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.mCb_itemSelectLeads.setChecked(true);
                //update model value
                model.setChecked(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getLead_id(), true);
                //check arrayList
                checkArrayList();
                //arrayListId.add(holder.member_id);
            }
        });*/

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
        return (null != itemArrayList ? itemArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    private void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value) leadIdArrayList.add(userID);
            //else catStringArrayList.remove(new String(subcatName));
        else leadIdArrayList.remove(new Integer(userID));
    }

    public ArrayList<Integer> getLeadIdArrayList() {
        return leadIdArrayList;
    }


    private void checkArrayList()
    {
        if (getLeadIdArrayList()!=null && getLeadIdArrayList().size()>0) {
            context.showButton();
        }
        else context.hideButton();
        //if (!arrayListId.isEmpty()) context.showButton();
        Log.e("TAG", "checkArrayList: "+ Arrays.toString(getLeadIdArrayList().toArray()));
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_siteVisits_TagIcon) AppCompatImageView iv_ownTagIcon;
        @BindView(R.id.iv_siteVisits_LeadWhatsApp) AppCompatImageView iv_ownLeadWhatsApp;
        @BindView(R.id.iv_siteVisits_LeadCall) AppCompatImageView iv_ownLeadCall;
        @BindView(R.id.iv_siteVisits_ownReminderIcon) AppCompatImageView iv_ownReminderIcon;
        @BindView(R.id.iv_siteVisits_leadDetails_ec) AppCompatImageView iv_leadDetails_ec;
        @BindView(R.id.ll_siteVisits_leadDetailsMain) LinearLayoutCompat ll_leadDetailsMain;
        @BindView(R.id.ll_siteVisits_ownAddLeadDetails)  LinearLayoutCompat ll_ownAddLeadDetails;
        @BindView(R.id.ll_siteVisits_ownViewLeadDetails) LinearLayoutCompat ll_ownViewLeadDetails;
        @BindView(R.id.ll_cardMain) LinearLayoutCompat ll_cardMain;
        @BindView(R.id.cv_itemsiteVisits_main) MaterialCardView cv_main;
        @BindView(R.id.ll_siteVisits_main) LinearLayoutCompat ll_siteVisits_main;
        @BindView(R.id.ll_siteVisits_LeadType) LinearLayoutCompat ll_LeadType;
        @BindView(R.id.tv_siteVisitsTag) AppCompatTextView tv_LeadsOwnTag;
        @BindView(R.id.tv_siteVisits_SalesPersonName) AppCompatTextView tv_SalesPersonName;
        @BindView(R.id.tv_siteVisitsCuIdNumber) AppCompatTextView tv_CuIdNumber;
        @BindView(R.id.tv_siteVisits_ownDate) AppCompatTextView tv_ownDate;
        @BindView(R.id.tv_siteVisits_LeadName) AppCompatTextView tv_LeadName;
        @BindView(R.id.tv_siteVisits_LeadMobile) AppCompatTextView tv_LeadMobile;
        @BindView(R.id.tv_siteVisits_ProjectName) AppCompatTextView tv_ProjectName;
        @BindView(R.id.tv_siteVisits_FirstCallMade) AppCompatTextView tv_FirstCallMade;
        @BindView(R.id.tv_siteVisits_NoOfCallMade) AppCompatTextView tv_NoOfCallMade;
        @BindView(R.id.tv_siteVisits_cpName) AppCompatTextView tv_siteVisits_cpName;
        @BindView(R.id.tv_cp_executive_name) AppCompatTextView tv_cp_executive_name;
        @BindView(R.id.tv_siteVisits_AvgCallDuration) AppCompatTextView tv_AvgCallDuration;
        @BindView(R.id.tv_siteVisits_MinCallDuration) AppCompatTextView tv_MinCallDuration;
        @BindView(R.id.tv_siteVisits_MaxCallDuration) AppCompatTextView tv_MaxCallDuration;

        /*@BindView(R.id.mTv_itemSelectLeads_leadName) MaterialTextView mTv_leadName;
        @BindView(R.id.mTv_itemSelectLeads_leadMob) MaterialTextView mTv_leadMob;
        @BindView(R.id.mCb_itemSelectLeads) MaterialCheckBox mCb_itemSelectLeads;*/

        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
