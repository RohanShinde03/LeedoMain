package com.tribeappsoft.leedo.admin.notifications;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.booked_customers.BookedCustomersActivity;
import com.tribeappsoft.leedo.admin.callSchedule.CallScheduleMainActivity;
import com.tribeappsoft.leedo.admin.leads.AllLeadsActivity;
import com.tribeappsoft.leedo.admin.offlineLeads.DuplicateLeads_Activity;
import com.tribeappsoft.leedo.admin.project_brochures.ProjectBrochuresActivity;
import com.tribeappsoft.leedo.admin.project_floor_plans.ProjectFloorPlanActivity;
import com.tribeappsoft.leedo.admin.project_quotations.ProjectQuotationActivity;
import com.tribeappsoft.leedo.admin.reminder.AllReminderActivity;
import com.tribeappsoft.leedo.admin.site_visits.AllSiteVisitsActivity;
import com.tribeappsoft.leedo.admin.user_profile.UserProfileActivity;
import com.tribeappsoft.leedo.admin.users.AllUsersActivity;
import com.tribeappsoft.leedo.models.NotificationModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by ${ROHAN} on 9/4/18.
 */

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.NotificationsViewHolder>
{


    private Activity activity;
    public ArrayList<NotificationModel> modelArrayList, multiSelect_list;
    private final Animations anim;
    private int lastPosition = -1;
    private boolean isRegPending;

    NotificationsRecyclerAdapter(Activity activity, ArrayList<NotificationModel> modelArrayList, ArrayList<NotificationModel> multiSelect_list, boolean isRegPending)
    {
        super();
        this.activity = activity;
        this.modelArrayList = modelArrayList;
        this.multiSelect_list = multiSelect_list;
        this.anim = new Animations();
        this.isRegPending = isRegPending;
    }


    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        //return null;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_layout_temp, parent, false);
        return new NotificationsViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position)
    {
        setAnimation(holder.cv_itemNotifications_mainList, position);
        final NotificationModel model = modelArrayList.get(position);

        holder.iv_Picture.setVisibility(View.GONE);
        String picture  = model.getPicture();

        holder.tv_title.setText(model.getTitle());
        //holder.textView_notification_Body.setText(body);
        holder.tv_Body.setText(model.getContent());
        holder.tv_date.setText(model.getDate());

        if (picture!=null)
        {
            if (!picture.trim().isEmpty())
            {
                //if (!picture.contains(""))
                {
                    if (picture.length()>0)
                    {
                        final Context context = activity.getApplicationContext();
                        if (Helper.isValidContextForGlide(context))
                        {

                            Glide.with(context)
                                    .load(picture)
                                    //.thumbnail(0.5f)
                                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .apply(new RequestOptions().optionalCenterCrop())
                                    .apply(new RequestOptions().error(R.color.main_light_grey))
                                    .apply(new RequestOptions().placeholder(R.color.main_light_grey))
                                    //.skipMemoryCache(true)
                                    .into(holder.iv_Picture);
                        }
                        holder.iv_Picture.setVisibility(View.VISIBLE);
                    }
                }

            }
        } else holder.iv_Picture.setVisibility(View.GONE);

        if(multiSelect_list.contains(modelArrayList.get(position))) {
            //holder.iv_bookImg.setImageResource(R.drawable.ic_icon_success);
            holder.cv_itemNotifications_mainList.setBackgroundColor(ContextCompat.getColor(activity, R.color.black_half_transparent));
        }
        else
        {
            holder.cv_itemNotifications_mainList.setBackgroundColor(ContextCompat.getColor(activity, R.color.main_white));
        }

    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    @Override
    public int getItemCount() {
        return (null != modelArrayList ? modelArrayList.size() : 0);
    }


    class NotificationsViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_itemNotifications_mainList) MaterialCardView cv_itemNotifications_mainList;
        @BindView(R.id.mTv_notification_title) MaterialTextView tv_title;
        @BindView(R.id.mTv_notification_Body) MaterialTextView tv_Body;
        @BindView(R.id.iv_notification_Picture) AppCompatImageView iv_Picture;
        @BindView(R.id.mTv_notification_date) MaterialTextView tv_date;


        NotificationsViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(v -> {

                Intent intent;
                if (isRegPending)
                {
                    intent = new Intent(activity, SalesPersonHomeNavigationActivity.class);
                    intent.putExtra("isRegPending", true);

                }
                else
                {
                    int pos = getAdapterPosition();
                    Log.e("pg ", "onClick: "+modelArrayList.get(pos).getPage() );
                    switch (modelArrayList.get(pos).getPage())
                    {
                        case "new_lead":  // Add New Lead Notify
                            intent = new Intent(activity, AllLeadsActivity.class);
                            intent.putExtra("notifyPush_leads", true);
                            break;

                        case "new_visit":  // Add New Site Visit Notify
                            intent = new Intent(activity, AllSiteVisitsActivity.class);
                            intent.putExtra("notifyPush_siteVisit", true);
                            break;

                        case "new_allotment":  //Add New Allotment Notify
                            intent = new Intent(activity, BookedCustomersActivity.class);
                            intent.putExtra("notifyPush_booking", true);
                            break;

                        case "new_doc_brochure":  // Add New Brochure Doc Notify
                            intent = new Intent(activity, ProjectBrochuresActivity.class);
                            intent.putExtra("notifyPush_newBrochure", true);
                            break;

                        case "new_doc_quotation":  // Add New Quotation Doc Notify
                            intent = new Intent(activity, ProjectQuotationActivity.class);
                            intent.putExtra("notifyPush_newQuotation", true);  //GHP success
                            break;

                        case "new_doc_floor_plan":  // Add New Floor Plan Doc Notify
                            intent = new Intent(activity, ProjectFloorPlanActivity.class);
                            intent.putExtra("notifyPush_newFloorPlan", true);  //GHP success
                            break;

                        case "ReminderPage":
                            intent = new Intent(activity, AllReminderActivity.class);
                            intent.putExtra("notifyPush_newReminder", true);  //Notifications
                            break;

                        case "ScheduledCallReminder":  //Unit released by system
                            intent = new Intent(activity, CallScheduleMainActivity.class);
                            intent.putExtra("notifyPush_newScheduledCall", true);  //call schedule list
                            break;

                        case "ScheduledCallNow"://Lead Reassign from on eto other sales person
                            intent = new Intent(activity, CallScheduleMainActivity.class);
                            intent.putExtra("notifyPush_newScheduledCallNow", true); //unClaimed Leads
                            break;

                        case "new_lead_reassign"://Lead Reassign from on eto other sales person
                            intent = new Intent(activity, AllLeadsActivity.class);
                            intent.putExtra("notifyPush_newLeadReassigned", true); //unClaimed Leads
                            break;

                        case "profile_update"://profile updated
                            intent = new Intent(activity, UserProfileActivity.class);
                            intent.putExtra("notifyPush_userProfileUpdated", true);  //Notifications
                            break;

                        case "user_add"://new User Added
                            intent = new Intent(activity, AllUsersActivity.class);
                            intent.putExtra("notifyPush_newUserAdded", true);  //Notifications
                            break;

                        case "offline_leads_duplicate"://new duplicate lead found
                            intent = new Intent(activity, DuplicateLeads_Activity.class);
                            intent.putExtra("notifyPush_newDuplicateLeadAdded", true); //unClaimed Leads

                            break;

                        case "offline_leads_merge"://new offline lead Added
                            intent = new Intent(activity, AllLeadsActivity.class);
                            intent.putExtra("notifyPush_newOffLeadAdded", true); //unClaimed Leads
                            break;

                        case "new_offline_lead"://new offline duplicate lead Added
                            intent = new Intent(activity, AllLeadsActivity.class);
                            intent.putExtra("notifyPush_newDuplicateLeadAdded", true); //unClaimed Leads
                            break;


                        case "cancel_booking"://booking cancelled
                            intent = new Intent(activity, AllLeadsActivity.class);
                            intent.putExtra("notifyPush_bookingCancelled", true); //unClaimed Leads
                            break;
                        default:
                            intent = new Intent(activity, SalesPersonHomeNavigationActivity.class);
                            break;
                    }
                }

                // set the new task and clear flags
                // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if (activity!=null) activity.startActivity(intent);
            });
        }
    }
}
