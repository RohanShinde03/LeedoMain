package com.tribeappsoft.leedo.salesPerson.salesHead.reports.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatBookingDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatGHPDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatLeadDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.StatSiteVisitDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.model.CPFosModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FOSReportRecyclerAdapter extends RecyclerView.Adapter<FOSReportRecyclerAdapter.CPReportViewHolder> {

    private Activity context;
    private ArrayList<CPFosModel> itemArrayList;
    //private String TAG="CPReportRecyclerAdapter";
    private final Animations anim;
    private int lastPosition = -1;
    private int cp_id = 0;


    public FOSReportRecyclerAdapter(Activity context, ArrayList<CPFosModel> itemArrayList,int cp_id) {
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.cp_id = cp_id;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public CPReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_cp_wise_report, parent, false);
        return new CPReportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CPReportViewHolder holder, int position) {

        //set animation
        setAnimation(holder.ll_cpReportMain, position);

        final CPFosModel myModel = itemArrayList.get(position);
        holder.mtvCPName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        holder.mtvCPLeads.setText(myModel.getLeads() != null && !myModel.getLeads().trim().isEmpty() ? myModel.getLeads() : "0");
        holder.mtvCPSiteVisits.setText(myModel.getLeads_site_visits() != null && !myModel.getLeads_site_visits().trim().isEmpty() ? myModel.getLeads_site_visits() : "0");
        holder.mtvCPGhpValue.setText(myModel.getLead_tokens() != null && !myModel.getLead_tokens().trim().isEmpty() ? myModel.getLead_tokens() : "0");
        holder.mtvCPGhpPlus.setText(myModel.getLead_tokens_ghp_plus() != null && !myModel.getLead_tokens_ghp_plus().trim().isEmpty() ? myModel.getLead_tokens_ghp_plus() : "0");
        holder.mtvCPAllotment.setText(myModel.getBooking_master() != null && !myModel.getBooking_master().trim().isEmpty() ? myModel.getBooking_master() : "0");


        holder.mtvCPLeads.setOnClickListener(v -> {

            if(Integer.parseInt(holder.mtvCPLeads.getText().toString()) > 0){
                Intent intent = new Intent(context, StatLeadDetailsActivity.class);
                intent.putExtra("cp_executive_id", myModel.getCp_executive_id());
                intent.putExtra("full_name",myModel.getFull_name());
                intent.putExtra("cp_id",cp_id);

                context.startActivity(intent);
            }else {
                new Helper().showCustomToast(context,"Details not Available!");
            }

        });

        holder.mtvCPSiteVisits.setOnClickListener(v -> {
            if(Integer.parseInt(holder.mtvCPSiteVisits.getText().toString()) > 0){
                Intent intent = new Intent(context, StatSiteVisitDetailsActivity.class);
                intent.putExtra("cp_executive_id", myModel.getCp_executive_id());
                intent.putExtra("full_name",myModel.getFull_name());
                intent.putExtra("cp_id",cp_id);
                context.startActivity(intent);
            }else {
                new Helper().showCustomToast(context,"Details not Available!");
            }
        });

        holder.mtvCPGhpValue.setOnClickListener(v -> {
            if(Integer.parseInt(holder.mtvCPGhpValue.getText().toString()) > 0){
                Intent intent = new Intent(context, StatGHPDetailsActivity.class);
                intent.putExtra("flag",1);
                intent.putExtra("cp_executive_id", myModel.getCp_executive_id());
                intent.putExtra("full_name",myModel.getFull_name());
                intent.putExtra("cp_id",cp_id);
                context.startActivity(intent);
            }else {
                new Helper().showCustomToast(context,"Details not Available!");
            }
        });

        holder.mtvCPGhpPlus.setOnClickListener(v -> {
            if(Integer.parseInt(holder.mtvCPGhpPlus.getText().toString()) > 0){
                Intent intent = new Intent(context, StatGHPDetailsActivity.class);
                intent.putExtra("flag",3);
                intent.putExtra("cp_executive_id", myModel.getCp_executive_id());
                intent.putExtra("full_name",myModel.getFull_name());
                intent.putExtra("cp_id",cp_id);
                context.startActivity(intent);
            }else {
                new Helper().showCustomToast(context,"Details not Available!");
            }
        });

        holder.mtvCPAllotment.setOnClickListener(v -> {
            if(Integer.parseInt(holder.mtvCPAllotment.getText().toString()) > 0){
                Intent intent = new Intent(context, StatBookingDetailsActivity.class);
                intent.putExtra("cp_executive_id", myModel.getCp_executive_id());
                intent.putExtra("full_name",myModel.getFull_name());
                intent.putExtra("cp_id",cp_id);
                context.startActivity(intent);
            }else {
                new Helper().showCustomToast(context,"Details not Available!");
            }
        });
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

    static class CPReportViewHolder extends RecyclerView.ViewHolder {

        // @BindView(R.id.cv_cpWise_report) MaterialCardView cv_report;
        @BindView(R.id.ll_cpWise_report) LinearLayoutCompat ll_cpReportMain;
        @BindView(R.id.mTv_cpReport_cp_name) MaterialTextView mtvCPName;
        @BindView(R.id.mTv_cpReport_leads_value) MaterialTextView mtvCPLeads;
        @BindView(R.id.mTv_cpReport_siteVisits_value) MaterialTextView mtvCPSiteVisits;
        @BindView(R.id.mTv_cpReport_ghp_value) MaterialTextView mtvCPGhpValue;
        @BindView(R.id.mTv_cpReport_ghpPlus_value) MaterialTextView mtvCPGhpPlus;
        @BindView(R.id.mTv_cpReport_allotments_value) MaterialTextView mtvCPAllotment;

        CPReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
