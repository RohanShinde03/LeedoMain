package com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.model.BookingModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookingDetailsAdapter extends RecyclerView.Adapter<BookingDetailsAdapter.BookingDetailsViewHolder> {

    private Activity context;
    private ArrayList<BookingModel> itemArrayList;
    private final Animations anim = new Animations();

    public BookingDetailsAdapter(Activity context, ArrayList<BookingModel> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
    }

    @NonNull
    @Override
    public BookingDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_stat_booking_details, parent, false);
        return new BookingDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingDetailsViewHolder holder, int position) {

        final BookingModel myModel = itemArrayList.get(position);
        holder.mtvCuIdNumber.setText(myModel.getLead_uid() != null && !myModel.getLead_uid().trim().isEmpty() ? myModel.getLead_uid() : "");
        holder.mtvFullName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        holder.mtvMobileNumber.setText(myModel.getMobile_number() != null && !myModel.getMobile_number().trim().isEmpty() ? myModel.getMobile_number() : "");
        holder.mtvSalesPersonName.setText(myModel.getSales_person_name() != null && !myModel.getSales_person_name().trim().isEmpty() ? myModel.getSales_person_name() : "");

        if(myModel.getSales_person_name().trim().isEmpty() || myModel.getSales_person_name() == null){
            holder.llSalesPersonNameLayout.setVisibility(View.GONE);
        }

        if (myModel.getGhp_details() != null) {
            holder.mtvGhpProject.setText(myModel.getGhp_details().getProject_name() != null && !myModel.getGhp_details().getProject_name().trim().isEmpty() ? myModel.getGhp_details().getProject_name() : "");
            holder.mtvGHPNo.setText(myModel.getGhp_details().getGhp_no() != null && !myModel.getGhp_details().getGhp_no().trim().isEmpty() ? myModel.getGhp_details().getGhp_no() : "");
            holder.mtvGHPAmount.setText(myModel.getGhp_details().getGhp_amount() != null && !myModel.getGhp_details().getGhp_amount().trim().isEmpty() ? myModel.getGhp_details().getGhp_amount() : "");
            holder.mtvGHPDate.setText(myModel.getGhp_details().getGhp_date() != null && !myModel.getGhp_details().getGhp_date().trim().isEmpty() ? myModel.getGhp_details().getGhp_date() : "");
        }else {
            holder.llGhpView.setVisibility(View.GONE);
        }


        if (myModel.getSite_visits() != null) {
            if (myModel.getSite_visits().size() > 0) {
                addSiteVisits(holder, position, myModel);
            }
        }

        //set visibility
        holder.ll_bookingDetailsMain.setVisibility(myModel.getSite_visits()!=null && myModel.getSite_visits().size()>0 ?  View.VISIBLE : View.GONE);

        holder.mtvMobileNumber.setOnClickListener(v -> {
            if (myModel.getMobile_number()!=null) {
                new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getMobile_number());
            }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
        });


        holder.cvBookingDetails.setOnClickListener(v -> {

            //temp
            if (myModel.isExpand())  //expanded
            {
                // //do collapse View
                anim.toggleRotate(holder.iv_expandCollapseIcon, false);
                collapse(holder.llBookingDetailsMoreDetailsView);
                myModel.setExpand(false);
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(holder.iv_expandCollapseIcon, true);
                expandSubView(holder.llBookingDetailsMoreDetailsView);
                myModel.setExpand(true);
            }
        });
    }

    private void addSiteVisits(@NonNull BookingDetailsViewHolder holder, int position, BookingModel myModel) {
        holder.llSiteVisit.setVisibility(View.VISIBLE);
        holder.llAddSiteVisitView.removeAllViews();
        for (int i = 0; i < myModel.getSite_visits().size(); i++) {
            View rowView_sub = showBookingView(i, myModel);
            holder.llAddSiteVisitView.addView(rowView_sub);
        }
    }

    private View showBookingView(final int position, BookingModel myModel) {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_site_visit_view, null);

        final MaterialTextView mtvProjectName = rowView_sub.findViewById(R.id.mtv_itemGhpDetails_sv_projectName);
        final MaterialTextView mtvUnitType = rowView_sub.findViewById(R.id.mtv_itemLeadDetails_sv_unitType);
        final MaterialTextView mtvVisitDate = rowView_sub.findViewById(R.id.mtv_itemGhpDetails_visit_date);
        final MaterialTextView mtvRemark = rowView_sub.findViewById(R.id.mtv_itemGhpDetails_remark);

        mtvProjectName.setText(myModel.getSite_visits().get(position).getProject_name() != null && !myModel.getSite_visits().get(position).getProject_name().trim().isEmpty() ? myModel.getSite_visits().get(position).getProject_name() : "");
        mtvUnitType.setText(myModel.getSite_visits().get(position).getUnit_category() != null && !myModel.getSite_visits().get(position).getUnit_category().trim().isEmpty() ? myModel.getSite_visits().get(position).getUnit_category() : "");
        mtvVisitDate.setText(myModel.getSite_visits().get(position).getCheck_in_date_time() != null && !myModel.getSite_visits().get(position).getCheck_in_date_time().trim().isEmpty() ? myModel.getSite_visits().get(position).getCheck_in_date_time() : "");
        mtvRemark.setText(myModel.getSite_visits().get(position).getRemark() != null && !myModel.getSite_visits().get(position).getRemark().trim().isEmpty() ? myModel.getSite_visits().get(position).getRemark() : "");

        return rowView_sub;
    }


    /*Collapsing View*/
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
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
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
        return itemArrayList.size();
    }

    static class BookingDetailsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mtv_itemBookingDetails_cuIdNumber) MaterialTextView mtvCuIdNumber;
        @BindView(R.id.mtv_itemBookingDetails_name) MaterialTextView mtvFullName;
        @BindView(R.id.mtv_itemBookingDetails_mobileNumber) MaterialTextView mtvMobileNumber;
        @BindView(R.id.mtv_itemBookingDetails_sales_person_name_value) MaterialTextView mtvSalesPersonName;
        @BindView(R.id.mtv_itemBookingDetails_Booking_projectName) MaterialTextView mtvGhpProject;
        @BindView(R.id.mtv_itemBookingDetails_GHP_no) MaterialTextView mtvGHPNo;
        @BindView(R.id.mtv_itemBookingDetails_amount) MaterialTextView mtvGHPAmount;
        @BindView(R.id.mtv_itemBookingDetails_date) MaterialTextView mtvGHPDate;

        @BindView(R.id.ll_item_bookingDetailsMain) LinearLayoutCompat ll_bookingDetailsMain;
        @BindView(R.id.ll_BookingDetails_moreDetailsView) LinearLayoutCompat llBookingDetailsMoreDetailsView;
        @BindView(R.id.iv_item_statBookingDetails_ec) AppCompatImageView iv_expandCollapseIcon;
        @BindView(R.id.ll_add_site_visit_view) LinearLayoutCompat llAddSiteVisitView;
        @BindView(R.id.ll_BookingDetails_site_visit) LinearLayoutCompat llSiteVisit;
        @BindView(R.id.ll_bookingDetails_ghpDetails) LinearLayoutCompat llGhpView;
        @BindView(R.id.ll_itemBookingDetailsList) LinearLayoutCompat cvBookingDetails;
        @BindView(R.id.ll_itemSalesPersonNameLayout) LinearLayoutCompat llSalesPersonNameLayout;

        BookingDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
