package com.tribeappsoft.leedo.salesPerson.salesHead.reports.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.salesHead.reports.FOSReportActivity;
import com.tribeappsoft.leedo.admin.reports.teamStats.model.TeamStatsModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CPReportRecyclerAdapter extends RecyclerView.Adapter<CPReportRecyclerAdapter.CPReportViewHolder> {

    private Activity context;
    private ArrayList<TeamStatsModel> itemArrayList;
    private String TAG="CPReportRecyclerAdapter";
    private final Animations anim;
    private int lastPosition = -1;


    public CPReportRecyclerAdapter(Activity context, ArrayList<TeamStatsModel> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public CPReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_cp_wise_report, parent, false);
        return new CPReportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CPReportViewHolder holder, int position  ){

        //set animation
        setAnimation(holder.ll_cpReportMain, position);

        final TeamStatsModel myModel = itemArrayList.get(position);

        //holder.mtvCPName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "");
        holder.mtvCPName.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFos_count()!=null ? myModel.getFull_name() +  " ("+ myModel.getFos_count() + ")" : myModel.getFull_name() : "");
        holder.mtvCPLeads.setText(myModel.getLeads() != null && !myModel.getLeads().trim().isEmpty() ? myModel.getLeads() : "0");
        holder.mtvCPSiteVisits.setText(myModel.getLeads_site_visits() != null && !myModel.getLeads_site_visits().trim().isEmpty() ? myModel.getLeads_site_visits() : "0");
        holder.mtvCPGhpValue.setText(myModel.getLead_tokens() != null && !myModel.getLead_tokens().trim().isEmpty() ? myModel.getLead_tokens() : "0");
        holder.mtvCPGhpPlus.setText(myModel.getLead_tokens_ghp_plus() != null && !myModel.getLead_tokens_ghp_plus().trim().isEmpty() ? myModel.getLead_tokens_ghp_plus() : "0");
        holder.mtvCPAllotment.setText(myModel.getBooking_master() != null && !myModel.getBooking_master().trim().isEmpty() ? myModel.getBooking_master() : "0");
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

     class CPReportViewHolder extends RecyclerView.ViewHolder {

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

            itemView.setOnClickListener(v ->
            {
                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, FOSReportActivity.class);
                intent.putExtra("cp_id", itemArrayList.get(getAdapterPosition()).getSales_person_id());
                //context.startActivity(intent);

                Log.e(TAG, "MyHolder: " );
                // Get the transition name from the string
                String transitionName = context.getString(R.string.transition_string);

                // Define the view that the animation will start from
                View viewStart = ll_cpReportMain;

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
                //Start the Intent
                ActivityCompat.startActivity(context, intent, options.toBundle());
            });

        }
    }
}
