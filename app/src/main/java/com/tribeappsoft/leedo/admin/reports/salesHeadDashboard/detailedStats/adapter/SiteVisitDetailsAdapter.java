package com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;

import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.model.SiteVisitDetailsModel;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SiteVisitDetailsAdapter extends RecyclerView.Adapter<SiteVisitDetailsAdapter.SiteVisitDetailsViewHolder> {

    private Activity context;
    private ArrayList<SiteVisitDetailsModel> itemArrayList;

    public SiteVisitDetailsAdapter(Activity context, ArrayList<SiteVisitDetailsModel> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
    }

    @NonNull
    @Override
    public SiteVisitDetailsAdapter.SiteVisitDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_site_visit_details, parent, false);
        return new SiteVisitDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteVisitDetailsAdapter.SiteVisitDetailsViewHolder holder, int position) {
        final SiteVisitDetailsModel myModel = itemArrayList.get(position);

        holder.mtvLeadDetailsDate.setText(myModel.getCheck_in_date_time() != null && !myModel.getCheck_in_date_time().trim().isEmpty() ? myModel.getCheck_in_date_time() : "");
        holder.mtvLeadDetailsTag.setText(myModel.getLead_types_name() != null && !myModel.getLead_types_name().trim().isEmpty() ? myModel.getLead_types_name() : "");
        holder.mtvLeadDetailsCuIdNumber.setText(myModel.getLead_uid() != null && !myModel.getLead_uid().trim().isEmpty() ? myModel.getLead_uid() : "");
        holder.mtvLeadName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        holder.mtvMobileNumber.setText(myModel.getMobile_number() != null && !myModel.getMobile_number().trim().isEmpty() ? myModel.getMobile_number() : "");
        holder.mtvEmailId.setText(myModel.getEmail() != null && !myModel.getEmail().trim().isEmpty() ? myModel.getEmail() : "");
        holder.mtvProjectName.setText(myModel.getProject_name() != null && !myModel.getProject_name().trim().isEmpty() ? myModel.getProject_name() : "");
        holder.mtvUnitType.setText(myModel.getUnit_category() != null && !myModel.getUnit_category().trim().isEmpty() ? myModel.getUnit_category() : "");
        holder.mtvCpName.setText(myModel.getCp_name() != null && !myModel.getCp_name().trim().isEmpty() ? myModel.getCp_name() : "");

        if(myModel.getCp_name().trim().isEmpty() || myModel.getCp_name() == null){
            holder.llCpNameLayout.setVisibility(View.GONE);
        }


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

    static class SiteVisitDetailsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mtv_itemSiteVisit_date) MaterialTextView mtvLeadDetailsDate;
        @BindView(R.id.mtv_itemSiteVisit_tag) MaterialTextView mtvLeadDetailsTag;
        @BindView(R.id.mtv_itemSiteVisit_cuIdNumber) MaterialTextView mtvLeadDetailsCuIdNumber;
        @BindView(R.id.mtv_itemSiteVisit_name) MaterialTextView mtvLeadName;
        @BindView(R.id.mtv_itemSiteVisitDetails_mobileNumber) MaterialTextView mtvMobileNumber;
        @BindView(R.id.mtv_itemSiteVisitDetails_email) MaterialTextView mtvEmailId;
        @BindView(R.id.mtv_itemSiteVisitDetails_projectName) MaterialTextView mtvProjectName;
        @BindView(R.id.mtv_itemSiteVisitDetails_unitType) MaterialTextView mtvUnitType;
        @BindView(R.id.mtv_itemSiteVisitDetails_cpName) MaterialTextView mtvCpName;
        @BindView(R.id.ll_CpNameLayout)
        LinearLayoutCompat llCpNameLayout;

        public SiteVisitDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}


