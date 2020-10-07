package com.tribeappsoft.leedo.salesPerson.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.GifImageView;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LeadsRecyclerAdapter extends RecyclerView.Adapter<LeadsRecyclerAdapter.AdapterViewHolder> {
    private Activity context;
    private ArrayList<LeadListModel> leadListModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private boolean isExpand=false;

    public LeadsRecyclerAdapter(Activity activity, ArrayList<LeadListModel> leadListModelArrayList) {

        this.leadListModelArrayList = leadListModelArrayList;
        this.context = activity;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_leads, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {

        setAnimation(holder.cv_leadList, position);

        final LeadListModel myModel = leadListModelArrayList.get(position);

        holder.tv_lead_date.setText(myModel.getLead_date() != null && !myModel.getLead_date().trim().isEmpty()? myModel.getLead_date(): "--");
        holder.tv_status.setText(myModel.getLead_status() != null && !myModel.getLead_status().trim().isEmpty()? myModel.getLead_status(): "--");
       // holder.tv_tokenNumber.setText(myModel.getLead_token_number() != null && !myModel.getLead_token_number().trim().isEmpty()? myModel.getLead_token_number(): "--");
        holder.tv_cuIdNumber.setText(myModel.getLead_cuid_number() != null && !myModel.getLead_cuid_number().trim().isEmpty()? myModel.getLead_cuid_number(): "--");
        holder.tv_lead_name.setText(myModel.getLead_name() != null && !myModel.getLead_name().trim().isEmpty()? myModel.getLead_name(): "--");

        holder.tv_lead_mobile.setText(String.format("+ %s - %s", myModel.getCountry_code(), myModel.getLead_mobile()));
        holder.tv_lead_mobile.setOnClickListener(v -> {
            if (myModel.getLead_mobile()!=null && !myModel.getLead_mobile().trim().isEmpty() ) new Helper().openPhoneDialer(context, "+"+ myModel.getCountry_code() + myModel.getLead_mobile().trim());
        });

        String project_name = myModel.getLead_project_name();
        String unitType = myModel.getLead_unit_type();
        String total = project_name!= null ? project_name + unitType!=null ? project_name + " | "+ unitType : ""  : "";
        holder.tv_lead_project_name.setText(total);
        holder.tv_lead_block_type.setText(myModel.getLead_unit_type() != null && !myModel.getLead_unit_type().trim().isEmpty()? myModel.getLead_unit_type(): "--");
        holder.tv_tag.setText(myModel.getLead_tag() != null && !myModel.getLead_tag().trim().isEmpty()? myModel.getLead_tag(): "--");

        //set status color
        if (myModel.getLead_status_id()==6) holder.tv_status.setTextColor(context.getResources().getColor(R.color.color_token_generated));
        if (myModel.getLead_status_id()==1) holder.tv_status.setTextColor(context.getResources().getColor(R.color.color_lead_generated));
        if (myModel.getLead_status_id()==4) holder.tv_status.setTextColor(context.getResources().getColor(R.color.color_lead_generated));


        if (myModel.getLead_type_id()==1) holder.iv_tagIcon.setImageResource(R.drawable.ic_tag_direct);
        if (myModel.getLead_type_id()==2) holder.iv_tagIcon.setImageResource(R.drawable.ic_tag_r_n_l);
        if (myModel.getLead_type_id()==3) holder.iv_tagIcon.setImageResource(R.drawable.ic_tag_general);


        //hide lead options till user claimed the lead
        if (myModel.getLead_status_id()==1) holder.iv_leadOptions.setVisibility(View.GONE);
        else holder.iv_leadOptions.setVisibility(View.VISIBLE);


        //set popup menu's
        holder.iv_leadOptions.setOnClickListener(view -> showPopUpMenu(holder, myModel));


        //Set Lead Details
        holder.ll_addLeadDetails.removeAllViews();
        if (myModel.getDetailsTitleModelArrayList()!=null && myModel.getDetailsTitleModelArrayList().size()>0)
        {
            for (int i =0 ; i< myModel.getDetailsTitleModelArrayList().size(); i++)
            {
                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_item_leads_title, null );
                final AppCompatTextView tv_leads_tag_details_title_text = rowView_sub.findViewById(R.id.tv_itemLeadDetails_title);
                final LinearLayoutCompat ll_addDetails = rowView_sub.findViewById(R.id.ll_itemLeadDetails_addDetails);
                tv_leads_tag_details_title_text.setText(myModel.getDetailsTitleModelArrayList().get(i).getLead_details_title());
                ll_addDetails.removeAllViews();
                ArrayList<LeadDetailsModel> detailsModelArrayList = myModel.getDetailsTitleModelArrayList().get(i).getLeadDetailsModels();
                if (detailsModelArrayList!=null && detailsModelArrayList.size()>0)
                {
                    for (int j=0; j<detailsModelArrayList.size(); j++)
                    {
                        @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null );
                        final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                        final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);

                        tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                        tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                        ll_addDetails.addView(rowView_subView);
                    }
                }

                holder.ll_addLeadDetails.addView(rowView_sub);
            }
        }

        holder.mBtn_claimNow.setOnClickListener(view -> {
            showSuccessAlert();
            leadListModelArrayList.get(position).setLead_status_id(2);
            leadListModelArrayList.get(position).setLead_status("Claimed");
            notifyDataSetChanged();
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert v != null;
                v.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
            } else {
                assert v != null;
                v.vibrate(3000);  // Vibrate method for below API Level 26
            }
            
        });



        //set expand Collapse
        holder.ll_leadDetailsMain.setOnClickListener(view -> {

            //temp
            if (myModel.getLead_status_id() != 1 && myModel.getLead_status_id() != 4  )
            {

                if (isExpand)  //expanded
                {
                    // //do collapse View
                    anim.toggleRotate(holder.iv_leadDetails_ec, false);
                    collapse(holder.ll_viewLeadDetails);
                    isExpand = false;
                }
                else    // collapsed
                {
                    //do expand view
                    anim.toggleRotate(holder.iv_leadDetails_ec, true);
                    expandSubView(holder.ll_viewLeadDetails);
                    isExpand = true;
                }
            }


        });


        /*Set Lead Tags*/
         /*holder.ll_add_lead_tags.removeAllViews();
        if (myModel.getDetailsTitleModelArrayList()!=null && myModel.getDetailsTitleModelArrayList().size()>0)
        {
            for (int i =0 ; i< myModel.getDetailsTitleModelArrayList().size(); i++)
            {
                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_item_leads_title, null );
                final LinearLayoutCompat ll_line_view = rowView_sub.findViewById(R.id.ll_line_view);
                final AppCompatTextView tv_lead_tags = rowView_sub.findViewById(R.id.tv_itemLead_tag);
                *//*Set Line View*//*
                if(i > 0){
                   ll_line_view.setVisibility(View.VISIBLE);
                }
                else {
                   ll_line_view.setVisibility(View.GONE);
                }
                tv_lead_tags.setText(myModel.getDetailsTitleModelArrayList().get(i).getLead_details_title());
                holder.ll_add_lead_tags.addView(rowView_sub);

            }
        }*/


        //ExpandCollapse(holder.iv_lead_tags_expandCollapse,holder.ll_add_lead_tags_details_list);


        //TODO temp assignments
        holder.mBtn_claimNow.setVisibility(myModel.getLead_status_id() ==1 ? View.VISIBLE  : View.GONE );
        //holder.tv_tokenNumber.setVisibility(myModel.getLead_status_id() == 6 ? View.VISIBLE  : View.GONE );
        holder.iv_leadDetails_ec.setVisibility(myModel.getLead_status_id() == 1 || myModel.getLead_status_id() == 4  ? View.GONE  : View.VISIBLE );

    }

    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.popup_layout, null);
        AppCompatTextView tv_message =  view.findViewById(R.id.popup_toast);

        GifImageView gifImageView =  view.findViewById(R.id.gif);
        gifImageView.setGifImageResource(R.drawable.gif_success);
        tv_message.setText("Claimed Successfully..");


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);

        final AlertDialog alertDialog= builder.create();
        alertDialog.setView(view);
        alertDialog.show();

        final Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            public void run() {
                alertDialog.dismiss();
                timer2.cancel(); //this will cancel the timer of the system
            }
        }, 4000); // the timer will count 4 seconds....

    }

    private void showPopUpMenu(AdapterViewHolder holder, LeadListModel myModel)
    {

        PopupMenu popupMenu = new PopupMenu(holder.iv_leadOptions.getContext(), holder.iv_leadOptions);

        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_leadOption_callNow, Menu.NONE, context.getString(R.string.call_now));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addReminder, Menu.NONE,context.getString(R.string.add_reminder));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addSiteVisit, Menu.NONE,context.getString(R.string.add_site_visit));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addToken, Menu.NONE,context.getString(R.string.generate_ghp));

        if (myModel.getLead_status_id() == 6)
        {
            //token generated
            //hide menu options - hide site Visit & token options
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
        }
        else if (myModel.getLead_status_id() == 5)
        {
            //site visit added
            //hide menu options - hide site Visit
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.menu_leadOption_callNow:
                    if (myModel.getLead_mobile()!=null && !myModel.getLead_mobile().trim().isEmpty() ) new Helper().openPhoneDialer(context, "+"+ myModel.getCountry_code() + myModel.getLead_mobile().trim());
                    return true;
                case R.id.menu_leadOption_addReminder:
                    /* context.startActivity(new Intent(context, AddReminderActivity.class)
                             .putExtra("fromOther", 3)
                             .putExtra("cu_id", myModel.getLead_uid())
                     .putExtra("lead_name", myModel.getLead_name() )
                     .putExtra("project_name",myModel.getLead_project_name() )
                     .putExtra("lead_id",2));*/

                    return true;
                case R.id.menu_leadOption_addSiteVisit:
                    context.startActivity(new Intent(context, AddSiteVisitActivity.class));
                    return true;
                case R.id.menu_leadOption_addToken:
                    context.startActivity(new Intent(context, GenerateTokenActivity.class));
                    return true;
                default:
                    return true;
            }

            //Toast.makeText(anchor.getContext(), item.getTitle() + "clicked", Toast.LENGTH_SHORT).show();
            //return true;
        });
        popupMenu.show();


    }


    /*Collapsing View*/
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
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

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
    /*Expandable View*/
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
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

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


    @Override
    public int getItemCount() {
        return (null != leadListModelArrayList ? leadListModelArrayList.size() : 0);
    }


    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }


    class AdapterViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_itemLeadList) MaterialCardView cv_leadList;
        @BindView(R.id.tv_itemLead_date) AppCompatTextView tv_lead_date;
        @BindView(R.id.tv_itemLead_elapsedTime) AppCompatTextView tv_elapsedTime;
        @BindView(R.id.tv_itemLead_status) AppCompatTextView tv_status;
        //@BindView(R.id.tv_itemLead_tokenNumber) AppCompatTextView tv_tokenNumber;
        @BindView(R.id.tv_itemLead_cuIdNumber) AppCompatTextView tv_cuIdNumber;
        @BindView(R.id.iv_itemLead_leadOptions) AppCompatImageView iv_leadOptions;

        @BindView(R.id.tv_itemLead_name) AppCompatTextView tv_lead_name;
        @BindView(R.id.tv_itemLead_mobile) AppCompatTextView tv_lead_mobile;
        @BindView(R.id.tv_itemLead_projectName) AppCompatTextView tv_lead_project_name;
        @BindView(R.id.tv_itemLead_unitType) AppCompatTextView tv_lead_block_type;
        @BindView(R.id.iv_itemLead_tagIcon) AppCompatImageView iv_tagIcon;
        @BindView(R.id.tv_itemLead_tag) AppCompatTextView tv_tag;
        @BindView(R.id.mBtn_itemLead_claimNow) MaterialButton mBtn_claimNow;

        @BindView(R.id.ll_itemLead_leadDetailsMain) LinearLayoutCompat ll_leadDetailsMain;
        @BindView(R.id.iv_itemLead_leadDetails_ec) AppCompatImageView iv_leadDetails_ec;
        @BindView(R.id.ll_itemLead_viewLeadDetails) LinearLayoutCompat ll_viewLeadDetails;
        @BindView(R.id.ll_itemLead_addLeadDetails) LinearLayoutCompat ll_addLeadDetails;


        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(v ->
            {

            });


        }

    }


}
