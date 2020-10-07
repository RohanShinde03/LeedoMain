package com.tribeappsoft.leedo.salesPerson.salesHead.churnedLeads.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class UnclaimedLeadsRecyclerAdapter extends RecyclerView.Adapter<UnclaimedLeadsRecyclerAdapter.AdapterViewHolder> {
    private Activity context;
    public ArrayList<LeadListModel> modelArrayList, multiSelect_list;
    private final Animations anim;
    private int lastPosition = -1;
    private String TAG = "UnclaimedLeadsRecyclerAdapter";


    public UnclaimedLeadsRecyclerAdapter(Activity activity, ArrayList<LeadListModel> modelArrayList, ArrayList<LeadListModel> multiSelect_list) {
        this.modelArrayList = modelArrayList;
        this.multiSelect_list = multiSelect_list;
        this.context = activity;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_un_claimed_leads, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {
        //set animations
        setAnimation(holder.ll_main, position);

        final LeadListModel myModel = modelArrayList.get(position);

        //if(myModel.getFeed_type_id() == 1)
        {
            //Own View

            //tag date
            holder.mTv_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty() ? myModel.getTag_date() : "");
            //tag icon
            holder.iv_tagIcon.setImageResource(R.drawable.ic_tag_general);
            //tag other_ids
            holder.mTv_tag.setText(myModel.getLead_types_name() != null && !myModel.getLead_types_name().trim().isEmpty() ? myModel.getLead_types_name() : "");
            //set churn count
            holder.mTv_churnedCount.setText(String.valueOf(myModel.getChurn_count()));
            //elapsed time
           // holder.mTv_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty() ? myModel.getTag_elapsed_time() : "");
            //cu_id number
            holder.mTv_cuIdNumber.setText(myModel.getLead_cuid_number() != null && !myModel.getLead_cuid_number().trim().isEmpty() ? myModel.getLead_cuid_number() : "");
            //lead name
            holder.mTv_leadName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
            //project name
            holder.mTv_projectName.setText(myModel.getLead_project_name() != null && !myModel.getLead_project_name().trim().isEmpty() ? myModel.getLead_unit_type()!=null ?  myModel.getLead_project_name() + " | "+myModel.getLead_unit_type() : myModel.getLead_project_name() : "");
            //status
            //holder.mTv_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty() ? myModel.getStatus_text() : "");
            //token number/sub status other_ids
            //holder.mTv_tokenNumber.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty() ? myModel.getStatus_sub_text() : "");
            //mobile number/call
            holder.iv_leadCall.setOnClickListener(v -> {
                if (myModel.getLead_mobile()!=null) {
                    new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getCountry_code() + myModel.getLead_mobile());
                }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer mobile number not found!");
            });

            //whatsApp
            holder.iv_leadWhatsApp.setOnClickListener(v -> {
                if (myModel.getLead_mobile()!=null) {
                    //send Message to WhatsApp Number
                    sendMessageToWhatsApp(myModel.getCountry_code() + myModel.getLead_mobile(), myModel.getFull_name());
                }
                else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer mobile number not found!");
            });

            //set own popup menu's
            // holder.iv_leadOptions.setOnClickListener(view -> showPopUpMenu(holder.iv_leadOptions, myModel));

            //set lead details
            if (myModel.getLead_type_id()==1) {
                //cp lead

                //set cp name
                holder.mTv_cpName.setText(myModel.getCp_name() != null && !myModel.getCp_name().trim().isEmpty() ? myModel.getCp_name() : "");
                //set cp executive name
                holder.mTv_cpExecutiveName.setText(myModel.getCp_executive_name() != null && !myModel.getCp_executive_name().trim().isEmpty() ? myModel.getCp_executive_name() : "");
                //visible cp layout
                holder.ll_cpDetails.setVisibility(View.VISIBLE);

            }
            else if (myModel.getLead_type_id()==2) {
                //walk in

                //hide cp layout
                holder.ll_cpDetails.setVisibility(View.GONE);

                //hide R&L layout
                holder.ll_rlDetails.setVisibility(View.GONE);

            }
            else if (myModel.getLead_type_id() ==3) {
                //R & L lead

                //set ref name
                holder.mTv_refName.setText(myModel.getRef_name() != null && !myModel.getRef_name().trim().isEmpty() ? myModel.getRef_name() : "");
                //set cp executive name
                holder.mTv_refMob.setText(myModel.getRef_mobile() != null && !myModel.getRef_mobile().trim().isEmpty() ? myModel.getRef_mobile() : "");

                //visible R&L layout
                holder.ll_rlDetails.setVisibility(View.VISIBLE);
            }

            if (myModel.getChurn_count()>0)
            {
                //lead churned already -- show churn details

                //set churn sales person name
                holder.mTv_churnSalesPersonName.setText(myModel.getChurn_sales_person_name() != null && !myModel.getChurn_sales_person_name().trim().isEmpty() ? myModel.getChurn_sales_person_name() : "");

                //set churn assign date
                holder.mTv_churnAssignDate.setText(myModel.getChurn_assign_date() != null && !myModel.getChurn_assign_date().trim().isEmpty() ? myModel.getChurn_assign_date() : "");

                //visible cp layout
                holder.ll_churnDetails.setVisibility(View.VISIBLE);
            }



            //set expand Collapse Own
            holder.iv_leadDetails_ec.setOnClickListener(view -> {

                if (myModel.isExpandedOwnView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(holder.iv_leadDetails_ec, false);
                    collapse(holder.ll_viewLeadDetails);
                    myModel.setExpandedOwnView(false);
                } else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(holder.iv_leadDetails_ec, true);
                    expandSubView(holder.ll_viewLeadDetails);
                    myModel.setExpandedOwnView(true);
                }
            });

            holder.cv_main.setOnClickListener(view -> {

                if (myModel.isExpandedOwnView())  //expanded
                {
                    // //do collapse View
                    new Animations().toggleRotate(holder.iv_leadDetails_ec, false);
                    collapse(holder.ll_viewLeadDetails);
                    myModel.setExpandedOwnView(false);
                }
                else    // collapsed
                {
                    //do expand view
                    new Animations().toggleRotate(holder.iv_leadDetails_ec, true);
                    expandSubView(holder.ll_viewLeadDetails);
                    myModel.setExpandedOwnView(true);
                }
            });


            //set visibility
            //holder.iv_leadOptions.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            //holder.iv_leadWhatsApp.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
            //holder.iv_leadCall.setVisibility( myModel.getLead_status_id()==1 ?  View.GONE : View.VISIBLE);
        }

        if(multiSelect_list.contains(modelArrayList.get(position))) {
            //holder.iv_bookImg.setImageResource(R.drawable.ic_icon_success);
            holder.ll_cardMain.setBackgroundColor(ContextCompat.getColor(context, R.color.color_ghp_plus_pending));
            holder.ll_main.setBackgroundColor(ContextCompat.getColor(context, R.color.color_other_feed_card_background));
        }
        else {
            holder.ll_cardMain.setBackgroundColor(ContextCompat.getColor(context, R.color.main_white));
            holder.ll_main.setBackgroundColor(ContextCompat.getColor(context, R.color.black_full_transparent));
        }

    }


    @Override
    public int getItemCount() {
        return (null != modelArrayList ? modelArrayList.size() : 0);
    }


    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }


    static class AdapterViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.ll_itemUnclaimedLead_main)  LinearLayoutCompat ll_main;
        @BindView(R.id.cv_itemUnclaimedLeads_main) MaterialCardView cv_main;
        @BindView(R.id.ll_itemUnclaimedLead_cardMain)  LinearLayoutCompat ll_cardMain;

        //Own View
        @BindView(R.id.mTv_itemUnclaimedLead_date) MaterialTextView mTv_date;
        @BindView(R.id.iv_itemUnclaimedLead_tagIcon)  AppCompatImageView iv_tagIcon;
        @BindView(R.id.mTv_itemUnclaimedLead_tag)  MaterialTextView mTv_tag;
        @BindView(R.id.mTv_itemUnclaimedLead_churnedCount)  MaterialTextView mTv_churnedCount;
        @BindView(R.id.iv_itemUnclaimedLead_leadWhatsApp)  AppCompatImageView iv_leadWhatsApp;
        @BindView(R.id.iv_itemUnclaimedLead_leadCall)  AppCompatImageView iv_leadCall;
        @BindView(R.id.iv_itemUnclaimedLead_leadOptions)  AppCompatImageView iv_leadOptions;
        @BindView(R.id.mTv_itemUnclaimedLead_cuIdNumber)  MaterialTextView mTv_cuIdNumber;
        @BindView(R.id.iv_itemUnclaimedLead_reminderIcon)  AppCompatImageView iv_reminderIcon;
        @BindView(R.id.mTv_itemUnclaimedLead_leadName)  MaterialTextView mTv_leadName;
        @BindView(R.id.mTv_itemUnclaimedLead_projectName)  MaterialTextView mTv_projectName;
        @BindView(R.id.ll_itemUnclaimedLead_leadDetailsMain)  LinearLayoutCompat ll_leadDetailsMain;
        @BindView(R.id.mTv_itemUnclaimedLead_status)  MaterialTextView mTv_status;
        @BindView(R.id.mTv_itemUnclaimedLead_tokenNumber)  MaterialTextView mTv_tokenNumber;
        @BindView(R.id.iv_itemUnclaimedLead_leadDetails_ec)  AppCompatImageView iv_leadDetails_ec;
        @BindView(R.id.mTv_itemUnclaimedLead_elapsedTime)  MaterialTextView mTv_elapsedTime;
        @BindView(R.id.ll_itemUnclaimedLead_viewLeadDetails)  LinearLayoutCompat ll_viewLeadDetails;
        @BindView(R.id.ll_itemUnclaimedLead_addLeadDetails)  LinearLayoutCompat ll_addLeadDetails;

        // cp details
        @BindView(R.id.ll_itemUnclaimedLead_cpDetails)  LinearLayoutCompat ll_cpDetails;
        @BindView(R.id.mTv_itemUnclaimedLead_cpName)  MaterialTextView mTv_cpName;
        @BindView(R.id.mTv_itemUnclaimedLead_cpExecutiveName)  MaterialTextView mTv_cpExecutiveName;

        // Ref details
        @BindView(R.id.ll_itemUnclaimedLead_rlDetails)  LinearLayoutCompat ll_rlDetails;
        @BindView(R.id.mTv_itemUnclaimedLead_refName)  MaterialTextView mTv_refName;
        @BindView(R.id.mTv_itemUnclaimedLead_refMob)  MaterialTextView mTv_refMob;

        // Churn details
        @BindView(R.id.ll_itemUnclaimedLead_churnDetails)  LinearLayoutCompat ll_churnDetails;
        @BindView(R.id.mTv_itemUnclaimedLead_churnSalesPersonName)  MaterialTextView mTv_churnSalesPersonName;
        @BindView(R.id.mTv_itemUnclaimedLead_churnAssignDate)  MaterialTextView mTv_churnAssignDate;


        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v ->
            {

            });
        }
    }


    private void sendMessageToWhatsApp(String number, String main_title)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String sales_person_name = sharedPreferences.getString("full_name", "");
        String sales_person_mobile = sharedPreferences.getString("mobile_number", "");
        String company_name =  sharedPreferences.getString("company_name", "");
        String company_name_short =  sharedPreferences.getString("company_name_short", "");
        editor.apply();


        String extra_text = context.getString(R.string.cim_std_welcome_msg, main_title, company_name_short, sales_person_name, company_name_short, sales_person_name, company_name, "+91-"+sales_person_mobile);


        String url = null;
        try {
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "Hello "+ main_title + ", Welcome to VJ family... Thank you for your registration." : "Hello", "UTF-8");
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? extra_text : "Hello", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            //new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

    }



    private void expandSubView(final View v)
    {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime==1) v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
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



    private void collapse(final View v)
    {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation)
            {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }




}
