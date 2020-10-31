package com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.model.LeadDetailsStatsModel;
import com.tribeappsoft.leedo.util.Helper;


import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LeadDetailsAdapter extends RecyclerView.Adapter<LeadDetailsAdapter.LeadDetailsViewHolder>  {

    private Activity context;
    private ArrayList<LeadDetailsStatsModel> itemArrayList;

    public LeadDetailsAdapter(Activity context, ArrayList<LeadDetailsStatsModel> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
    }


    @NonNull
    @Override
    public LeadDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_lead_details, parent, false);
        return new LeadDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LeadDetailsViewHolder holder, int position) {

        final LeadDetailsStatsModel myModel = itemArrayList.get(position);

        holder.mtvLeadType.setText(myModel.getLead_type() != null && !myModel.getLead_type().trim().isEmpty() ? myModel.getLead_type() : "");
        holder.mtvLeadDetailsCuIdNumber.setText(myModel.getLead_uid() != null && !myModel.getLead_uid().trim().isEmpty() ? myModel.getLead_uid() : "");
        holder.mtvLeadName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        holder.mtvMobileNumber.setText(myModel.getMobile_number() != null && !myModel.getMobile_number().trim().isEmpty() ? myModel.getMobile_number() : "");
        holder.mtvEmailId.setText(myModel.getEmail() != null && !myModel.getEmail().trim().isEmpty() ? myModel.getEmail() : "");
        holder.mtvProjectName.setText(myModel.getProject_name() != null && !myModel.getProject_name().trim().isEmpty() ? myModel.getProject_name() : "");
        holder.mtvUnitType.setText(myModel.getUnit_category() != null && !myModel.getUnit_category().trim().isEmpty() ? myModel.getUnit_category() : "");

        holder.mtvMobileNumber.setOnClickListener(v -> {
            if (myModel.getMobile_number()!=null) {
                new Helper().openPhoneDialer(Objects.requireNonNull(context), myModel.getMobile_number());
            }else new Helper().showCustomToast(Objects.requireNonNull(context), "Customer number not found!");
        });
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    static class LeadDetailsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mtv_itemLeadDetails_tag) MaterialTextView mtvLeadType;
        @BindView(R.id.mtv_itemLeadDetails_cuIdNumber) MaterialTextView mtvLeadDetailsCuIdNumber;
        @BindView(R.id.mtv_itemLeadDetails_name) MaterialTextView mtvLeadName;
        @BindView(R.id.mtv_itemLeadDetails_mobileNumber) MaterialTextView mtvMobileNumber;
        @BindView(R.id.mtv_itemLeadDetails_email) MaterialTextView mtvEmailId;
        @BindView(R.id.mtv_itemLeadDetails_projectName) MaterialTextView mtvProjectName;
        @BindView(R.id.mtv_itemLeadDetails_unitType) MaterialTextView mtvUnitType;

        @BindView(R.id.cv_itemLeadDetailsList)
        CardView cvLeadDetails;
        LeadDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
