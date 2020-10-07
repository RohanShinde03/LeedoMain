package com.tribeappsoft.leedo.siteEngineer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.siteEngineer.models.SiteVisitsModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AllSiteVisitsRecyclerAdapter extends RecyclerView.Adapter<AllSiteVisitsRecyclerAdapter.StateAdapterViewHolder> {

    // private Activity context;
    private Activity context;
    private ArrayList<SiteVisitsModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;


    public AllSiteVisitsRecyclerAdapter(Activity context, ArrayList<SiteVisitsModel> batchmateModelArrayList) {
        this.context = context;
        this.itemArrayList = batchmateModelArrayList;
        this.anim = new Animations();
    }


    @NonNull
    @Override
    public StateAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_site_visits, parent, false);
        return new StateAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StateAdapterViewHolder holder, int position)
    {
        setAnimation(holder.cv_mainList, position);
        final SiteVisitsModel myModel = itemArrayList.get(position);
        //itemArrayList.size();

        holder.tv_visitType.setText(myModel.getVisit_type());
        holder.tv_timeSlot.setText(myModel.getVisit_date());
        holder.tv_customerName.setText(myModel.getCustomer_name());
        holder.tv_customerUnitType.setText(myModel.getFlat_type());
        holder.tv_customerMobile.setText(myModel.getCustomer_mobile());
        holder.tv_customerMobile.setText(String.format("+ %s - %s", myModel.getCountryCode(), myModel.getCustomer_mobile()));
        holder.tv_customerMobile.setOnClickListener(v -> {
            if (myModel.getCustomer_mobile()!=null && !myModel.getCustomer_mobile().trim().isEmpty() ) new Helper().openPhoneDialer(context, "+"+ myModel.getCountryCode() + myModel.getCustomer_mobile().trim());
        });

        holder.tv_checkInTime.setText(myModel.getCheck_in_time());
        holder.tv_checkOutTime.setText(myModel.getCheck_out_time());

        //itemArrayList.get(position);
        //holder.setData(itemArrayList.size(), position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

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
        return (null != itemArrayList ? itemArrayList.size() : 0);
    }

    public class StateAdapterViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.cv_siteVisits) MaterialCardView cv_mainList;
        @BindView(R.id.tv_siteVisit_visitType) AppCompatTextView tv_visitType;
        @BindView(R.id.tv_siteVisit_timeSlot) AppCompatTextView tv_timeSlot;
        @BindView(R.id.tv_siteVisit_customerName) AppCompatTextView tv_customerName;
        @BindView(R.id.tv_siteVisit_customerUnitType) AppCompatTextView tv_customerUnitType;
        @BindView(R.id.tv_siteVisit_customerMobile) AppCompatTextView tv_customerMobile;

        @BindView(R.id.mBtn_siteVisit_scanIn) MaterialButton mBtn_scanIn;
        @BindView(R.id.ll_siteVisit_checkIn) LinearLayoutCompat ll_checkIn;
        @BindView(R.id.tv_siteVisit_checkInTime) AppCompatTextView tv_checkInTime;


        @BindView(R.id.mBtn_siteVisit_scanOut) MaterialButton mBtn_scanOut;
        @BindView(R.id.ll_siteVisit_checkOut) LinearLayoutCompat ll_checkOut;
        @BindView(R.id.tv_siteVisit_checkOutTime) AppCompatTextView tv_checkOutTime;

        public StateAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }

//        void setData(int size, int position) {
//
//            Log.e("size", "size: " + size);
//            Log.e("position", "position: " + position);
//
//            //This is used for disabled bottom line of bottom card in recyclerview....
//            if (position < size - 1) {
//                batchmateview.setVisibility(View.VISIBLE);
//            } else {
//                batchmateview.setVisibility(View.GONE);
//            }
//        }
    }


}


