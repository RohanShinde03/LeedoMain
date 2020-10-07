package com.tribeappsoft.leedo.salesPerson.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.homeFragments.FragmentSalesPersonHomeFeeds;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.models.FeedsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.GifImageView;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFeedRecyclerAdapter extends RecyclerView.Adapter<HomeFeedRecyclerAdapter.AdapterViewHolder> {
    private Activity context;
    private ArrayList<FeedsModel> modelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private boolean isExpand=false,isExpand2=false;
    private FragmentSalesPersonHomeFeeds fragmentSalesPersonHomeFeeds;

    public HomeFeedRecyclerAdapter(Activity activity, ArrayList<FeedsModel> homeFeedandLeadModelArrayList, FragmentSalesPersonHomeFeeds fragmentSalesPersonHomeFeeds) {

        this.modelArrayList = homeFeedandLeadModelArrayList;
        this.context = activity;
        this.anim = new Animations();
        this.fragmentSalesPersonHomeFeeds= fragmentSalesPersonHomeFeeds;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_home_feeds, parent,false);
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
        final FeedsModel myModel = modelArrayList.get(position);


        if(myModel.getFeed_type_id() == 1)
        {}
            //Own View

            //tag date
            holder.tv_own_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty()? myModel.getTag_date(): "");
            //tag icon
            holder.iv_own_tagIcon.setImageResource(R.drawable.ic_tag_direct);
            //tag text
            holder.tv_own_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty()? myModel.getTag(): "");
            //elapsed time
            holder.tv_own_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty()? myModel.getTag_elapsed_time(): "");
            //cu_id number
            holder.tv_own_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty()?myModel.getSmall_header_title(): "");
            //lead name
            holder.tv_own_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty()? myModel.getMain_title(): "");
            //project name
            holder.tv_own_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty()? myModel.getDescription(): "");
            //status
            holder.tv_own_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty()? myModel.getStatus_text(): "");
            //token number/sub status text
           // holder.tv_own_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty()? myModel.getStatus_sub_text(): "");
            //mobile number/call
            holder.iv_own_Lead_call.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getCall()));


            //set own popup menu's
            holder.iv_own_leadOptions.setOnClickListener(view -> showPopUpMenu(holder.iv_own_leadOptions, myModel));


            if (myModel.getDetailsTitleModelArrayList()!=null && myModel.getDetailsTitleModelArrayList().size()>0)
            {

                holder.iv_own_leadDetails_ec.setVisibility(View.VISIBLE);
                //Set Lead Details
                holder.ll_own_addLeadDetails.removeAllViews();
                for (int i =0 ; i< myModel.getDetailsTitleModelArrayList().size(); i++)
                {
                    //Log.e("ll_HomeFeed_own_", "onBindViewHolder: "+myModel.getDetailsTitleModelArrayList().size());

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
                            //Log.e("ll_HomeFeed_own_", "detailsModelArrayList.get(j).getLead_details_text() "+detailsModelArrayList.get(j).getLead_details_text());

                            @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null );
                            final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                            final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);

                            tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                            tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                            ll_addDetails.addView(rowView_subView);
                        }
                    }

                    holder.ll_own_addLeadDetails.addView(rowView_sub);
                }
            }


            //set expand Collapse Own
            holder.iv_own_leadDetails_ec.setOnClickListener(view -> {

                if (isExpand)  //expanded
                {
                    // //do collapse View
                    anim.toggleRotate(holder.iv_own_leadDetails_ec, false);
                    collapse(holder.ll_own_viewLeadDetails);
                    isExpand = false;
                }
                else    // collapsed
                {
                    //do expand view
                    anim.toggleRotate(holder.iv_own_leadDetails_ec, true);
                    expandSubView(holder.ll_own_viewLeadDetails);
                    isExpand = true;
                }
            });

            //visible view
            holder.ll_HomeFeed_own_view.setVisibility(View.VISIBLE);


        /*else if(myModel.getFeed_type_id() == 2)
        {

            //Others View

            //tag date
            holder.tv_others_date.setText(myModel.getTag_date() != null && !myModel.getTag_date().trim().isEmpty()? myModel.getTag_date(): "");
            //tag icon
            holder.iv_others_tagIcon.setImageResource(R.drawable.ic_tag_direct);
            //tag text
            holder.tv_others_tag.setText(myModel.getTag() != null && !myModel.getTag().trim().isEmpty()? myModel.getTag(): "");
            //elapsed time
            holder.tv_others_elapsedTime.setText(myModel.getTag_elapsed_time() != null && !myModel.getTag_elapsed_time().trim().isEmpty()? myModel.getTag_elapsed_time(): "");
            //cu_id number
            holder.tv_others_cuIdNumber.setText(myModel.getSmall_header_title() != null && !myModel.getSmall_header_title().trim().isEmpty()?myModel.getSmall_header_title(): "");
            //lead name
            holder.tv_others_Lead_name.setText(myModel.getMain_title() != null && !myModel.getMain_title().trim().isEmpty()? myModel.getMain_title(): "");
            //project name
            holder.tv_others_projectName.setText(myModel.getDescription() != null && !myModel.getDescription().trim().isEmpty()? myModel.getDescription(): "");
            //status
            holder.tv_others_Lead_status.setText(myModel.getStatus_text() != null && !myModel.getStatus_text().trim().isEmpty()? myModel.getStatus_text(): "");
            //token number/sub status text
            holder.tv_others_token_number.setText(myModel.getStatus_sub_text() != null && !myModel.getStatus_sub_text().trim().isEmpty()? myModel.getStatus_sub_text(): "");
            //mobile number/call
            holder.iv_others_Lead_call.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getCall()));

            //set others popup menu's
            holder.iv_others_leadOptions.setOnClickListener(view -> showPopUpMenu(holder.iv_others_leadOptions, myModel));


            //Set Lead Details
            if (myModel.getDetailsTitleModelArrayList()!=null && myModel.getDetailsTitleModelArrayList().size()>0)
            {

                holder.iv_others_leadDetails_ec.setVisibility(View.VISIBLE);
                holder.ll_others_addLeadDetails.removeAllViews();
                for (int i =0 ; i< myModel.getDetailsTitleModelArrayList().size(); i++)
                {
                    //Log.e("ll_HomeFeed_own_", "onBindViewHolder: "+myModel.getDetailsTitleModelArrayList().size());

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
                            //Log.e("ll_HomeFeed_own_", "detailsModelArrayList.get(j).getLead_details_text() "+detailsModelArrayList.get(j).getLead_details_text());
                            @SuppressLint("InflateParams") View rowView_subView = LayoutInflater.from(context).inflate(R.layout.layout_item_lead_details_text, null );
                            final AppCompatTextView tv_text = rowView_subView.findViewById(R.id.tv_itemLeadDetails_text);
                            final AppCompatTextView tv_value = rowView_subView.findViewById(R.id.tv_itemLeadDetails_value);

                            tv_text.setText(detailsModelArrayList.get(j).getLead_details_text());
                            tv_value.setText(detailsModelArrayList.get(j).getLead_details_value());

                            ll_addDetails.addView(rowView_subView);
                        }
                    }
                    holder.ll_others_addLeadDetails.addView(rowView_sub);
                }

            } else holder.iv_others_leadDetails_ec.setVisibility(View.GONE);



            //set expand Collapse Others
            holder.iv_others_leadDetails_ec.setOnClickListener(view -> {

                if (isExpand2)  //expanded
                {
                    // //do collapse View
                    anim.toggleRotate(holder.iv_others_leadDetails_ec, false);
                    collapse(holder.ll_others_viewLeadDetails);
                    isExpand2 = false;
                }
                else    // collapsed
                {
                    //do expand view
                    anim.toggleRotate(holder.iv_others_leadDetails_ec, true);
                    expandSubView(holder.ll_others_viewLeadDetails);
                    isExpand2 = true;
                }
            });


            //visible other view
            holder.ll_HomeFeed_others_view.setVisibility(View.VISIBLE);
        }*/


        //hide lead options till user claimed the lead
        /*if (myModel.getLead_status_id()==1 || myModel.getLead_status_id()==7 || myModel.getLead_status_id()==8 )
        {
            holder.iv_others_leadOptions.setVisibility(View.GONE);
            holder.iv_own_leadOptions.setVisibility(View.GONE);
        }
        else
        {
            holder.iv_others_leadOptions.setVisibility(View.VISIBLE);
            holder.iv_own_leadOptions.setVisibility(View.VISIBLE);
        }*/




        /*
        //visible only when lead is unclaimed -- lead status id ==1
        holder.mBtn__HomeFeed_Others_claimNow.setVisibility(myModel.getLead_status_id() ==1 ? View.VISIBLE  : View.GONE );

        if(myModel.getLead_status_id() ==1)
        {
            Log.e("ll_tag_others", "onBindViewHolder: "+holder.ll_tag_others_elapsed_time );
            LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams)holder.ll_tag_others_elapsed_time.getLayoutParams();
            params.setMargins(0, 25, 0, 0);
            holder.ll_tag_others_elapsed_time.setLayoutParams(params);
        }else if (myModel.getLead_status_id() ==7)
        {
            LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams)holder.ll_tag_own_elapsed_time.getLayoutParams();
            params.setMargins(0, 10, 0, 0);
            holder.ll_tag_own_elapsed_time.setLayoutParams(params);
        }
        //hide lead status when lead is unclaimed
        holder.ll_others_leadDetailsMain.setVisibility(myModel.getLead_status_id()==1 ? View.GONE : View.VISIBLE);
    */

        /*holder.mBtn__HomeFeed_Others_claimNow.setOnClickListener(view ->
        {

            showSuccessAlert();
            modelArrayList.clear();
            //this.fragmentSalesPersonHomeFeeds.setNewTempData();
            notifyDataSetChanged();
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert v != null;
                v.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
            } else {
                assert v != null;
                v.vibrate(3000);  // Vibrate method for below API Level 26
            }

        });*/


        //unclaimed
       // if (myModel.getLead_status_id() == 1) holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
        // lead claimed
       // if (myModel.getLead_status_id() == 2)  holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_claimed));
        // lead assigned
       // if (myModel.getLead_status_id() == 3)  holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_assigned));
        //self/ lead added
       // if (myModel.getLead_status_id() == 4)  holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_lead_added));
        //site visited
       // if (myModel.getLead_status_id() == 5) holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_site_visit));
        //token generated
       // if (myModel.getLead_status_id() == 6) holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_token_generated));
        //on hold
       // if (myModel.getLead_status_id() == 7) holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_onHold));
        //booked
       // if (myModel.getLead_status_id() == 8) holder.tv_own_status.setBackgroundColor(context.getResources().getColor(R.color.color_flat_booked));

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

    private void showPopUpMenu(View  view, FeedsModel myModel)
    {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        //add popup menu options
        popupMenu.getMenu().add(1, R.id.menu_leadOption_callNow, Menu.NONE, context.getString(R.string.call_now));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addReminder, Menu.NONE,context.getString(R.string.add_reminder));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addSiteVisit, Menu.NONE,context.getString(R.string.add_site_visit));
        popupMenu.getMenu().add(1, R.id.menu_leadOption_addToken, Menu.NONE,context.getString(R.string.generate_ghp));



        //unclaimed
        /*if (myModel.getLead_status_id() == 1)
        {
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(false);
        }
        // lead claimed
        if (myModel.getLead_status_id() == 2)
        {
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
        }
        // lead assigned
        if (myModel.getLead_status_id() == 3)
        {
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);
        }
        //self/ lead added
        if (myModel.getLead_status_id() == 4)
        {
            //Lead Added
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(true);

        }
        //site visited
        if (myModel.getLead_status_id() == 5)
        {
            //site visit added
            //hide menu options - hide site Visit
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
        }
        //token generated
        if (myModel.getLead_status_id() == 6)
        {
            //token generated
            //hide menu options - hide site Visit & token options
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
        }
        //on hold
        if (myModel.getLead_status_id() == 7)
        {
            //call & reminder  option only
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(true);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(true);

        }
        //booked
        if (myModel.getLead_status_id() == 8)
        {
            popupMenu.getMenu().findItem(R.id.menu_leadOption_callNow).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addToken).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addSiteVisit).setVisible(false);
            popupMenu.getMenu().findItem(R.id.menu_leadOption_addReminder).setVisible(false);
        }*/


        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.menu_leadOption_callNow:
                    if (myModel.getCall()!=null && !myModel.getCall().trim().isEmpty() ) new Helper().openPhoneDialer(context,  myModel.getCall());
                    return true;
                case R.id.menu_leadOption_addReminder:
                   /* context.startActivity(new Intent(context, AddReminderActivity.class)
                            .putExtra("fromOther", 3)
                            .putExtra("cu_id", myModel.getSmall_header_title())
                            .putExtra("lead_name", myModel.getMain_title() )
                            .putExtra("project_name",myModel.getDescription() )
                            .putExtra("lead_id",myModel.getLead_id()));*/

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


    class AdapterViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.ll_itemHomeFeed_main)  LinearLayoutCompat ll_main;

        //Own View
        @BindView(R.id.ll_homeFeed_ownView)  LinearLayoutCompat ll_HomeFeed_own_view;
        @BindView(R.id.tv_homeFeed_ownDate)  AppCompatTextView tv_own_date;
        @BindView(R.id.iv_homeFeed_ownTagIcon)  AppCompatImageView iv_own_tagIcon;
        @BindView(R.id.tv_homeFeed_ownTag)  AppCompatTextView tv_own_tag;
        @BindView(R.id.tv_homeFeed_ownElapsedTime)  AppCompatTextView tv_own_elapsedTime;
        @BindView(R.id.tv_homeFeed_ownCuIdNumber)  AppCompatTextView tv_own_cuIdNumber;
        @BindView(R.id.tv_homeFeed_ownLeadName)  AppCompatTextView tv_own_Lead_name;
        @BindView(R.id.tv_homeFeed_ownProjectName)  AppCompatTextView tv_own_projectName;
        @BindView(R.id.iv_homeFeed_ownLeadCall)  AppCompatImageView iv_own_Lead_call;
        @BindView(R.id.iv_homeFeed_ownLeadOptions)  AppCompatImageView iv_own_leadOptions;
        @BindView(R.id.tv_homeFeed_ownStatus)  AppCompatTextView tv_own_status;
       // @BindView(R.id.tv_homeFeed_ownTokenNumber)  AppCompatTextView tv_own_token_number;
        @BindView(R.id.ll_homeFeed_own_leadDetailsMain)  LinearLayoutCompat ll_own_leadDetailsMain;
        @BindView(R.id.iv_homeFeed_own_leadDetails_ec)  AppCompatImageView iv_own_leadDetails_ec;
        @BindView(R.id.ll_homeFeed_ownViewLeadDetails)  LinearLayoutCompat ll_own_viewLeadDetails;
        @BindView(R.id.ll_homeFeed_ownAddLeadDetails)  LinearLayoutCompat ll_own_addLeadDetails;

        /*@BindView(R.id.ll_tag_own_elapsed_time)  LinearLayoutCompat ll_tag_own_elapsed_time;
        @BindView(R.id.tv_HomeFeed_own_Lead_mobile)  AppCompatTextView tv_HomeFeed_own_Lead_mobile;
        @BindView(R.id.tv_HomeFeed_own_unitType)  AppCompatTextView tv_HomeFeed_own_unitType;*/

     /*   //others view
        @BindView(R.id.ll_homeFeed_othersView)  LinearLayoutCompat ll_HomeFeed_others_view;
        @BindView(R.id.tv_homeFeed_othersDate)  AppCompatTextView tv_others_date;
        @BindView(R.id.iv_homeFeed_othersTagIcon)  AppCompatImageView iv_others_tagIcon;
        @BindView(R.id.tv_homeFeed_othersTag)  AppCompatTextView tv_others_tag;
        @BindView(R.id.tv_homeFeed_othersElapsedTime)  AppCompatTextView tv_others_elapsedTime;
        @BindView(R.id.tv_homeFeed_othersCuIdNumber)  AppCompatTextView tv_others_cuIdNumber;
        @BindView(R.id.tv_homeFeed_othersLeadName)  AppCompatTextView tv_others_Lead_name;
        @BindView(R.id.tv_homeFeed_othersProjectName)  AppCompatTextView tv_others_projectName;
        @BindView(R.id.tv_homeFeed_othersLeadStatus)  AppCompatTextView tv_others_Lead_status;
        @BindView(R.id.tv_homeFeed_othersTokenNumber)  AppCompatTextView tv_others_token_number;
        @BindView(R.id.iv_homeFeedOthersLeadCall)  AppCompatImageView iv_others_Lead_call;
        @BindView(R.id.iv_homeFeed_othersLeadOptions)  AppCompatImageView iv_others_leadOptions;

        @BindView(R.id.mBtn_homeFeed_othersClaimNow) MaterialButton mBtn__HomeFeed_Others_claimNow;

        @BindView(R.id.tv_homeFeed_othersLeadMobile)  AppCompatTextView tv_HomeFeed_Others_Lead_mobile;
        @BindView(R.id.tv_homeFeed_othersUnitType)  AppCompatTextView tv_HomeFeed_Others_unitType;
        @BindView(R.id.iv_homeFeed_others_leadDetails_ec)  AppCompatImageView iv_others_leadDetails_ec;
        @BindView(R.id.ll_homeFeed_othersViewLeadDetails)  LinearLayoutCompat ll_others_viewLeadDetails;
        @BindView(R.id.ll_homeFeed_othersAddLeadDetails)  LinearLayoutCompat ll_others_addLeadDetails;
        //@BindView(R.id.ll_homeFeed_others_leadDetailsMain)  LinearLayoutCompat ll_others_leadDetailsMain;
       // @BindView(R.id.ll_tag_others_elapsed_time)  LinearLayoutCompat ll_tag_others_elapsed_time;

*/
        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(v ->
            {

            });
        }

    }


}
