package com.tribeappsoft.leedo.salesPerson.salesHead.reports.cp_filter.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelectProjectRecyclerAdapter extends RecyclerView.Adapter<SelectProjectRecyclerAdapter.AdapterViewHolder> {

    //private final String TAG="SelectProjectRecyclerAdapter";
    private ArrayList<ProjectModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private Activity context;
    private final OnItemClickListener onItemClickListener;


    public SelectProjectRecyclerAdapter(Activity context, ArrayList<ProjectModel> modelArrayList, OnItemClickListener listener) {
        this.context = context;
        this.itemArrayList = modelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = listener;
    }


    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_all_projects, parent, false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position) {

        setAnimation(holder.cv_itemProjectsFilter, position);
        final ProjectModel myModel = itemArrayList.get(position);
        holder.mTv_projectName.setText(myModel.getProject_name() != null && !myModel.getProject_name().trim().isEmpty() ? myModel.getProject_name() : "--");
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


    class AdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_itemProjectsFilter) MaterialCardView cv_itemProjectsFilter;
        @BindView(R.id.mTv_itemProjectsFilter_projectName) MaterialTextView mTv_projectName;

        @SuppressLint("LongLogTag")
        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            //set click listener
            //Log.e(TAG, "delayRefresh: "+itemArrayList.size()+""+itemArrayList.get(getAdapterPosition()).getFull_name()+""+itemArrayList.get(getAdapterPosition()).getUser_id() );
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(itemArrayList.get(getAdapterPosition())));

        }

    }

    public interface OnItemClickListener {
        void onItemClick(ProjectModel model);
    }

}