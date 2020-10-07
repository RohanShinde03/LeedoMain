package com.tribeappsoft.leedo.admin.leadreassign.adapter;

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
import com.tribeappsoft.leedo.admin.leadreassign.model.SalesPersonModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelectSalesPersonRecyclerAdapter extends RecyclerView.Adapter<SelectSalesPersonRecyclerAdapter.AdapterViewHolder> {

   // private final String TAG="SelectSalesPersonRecyclerAdapter";
    private ArrayList<SalesPersonModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private Activity context;
    private final OnItemClickListener onItemClickListener;


    public SelectSalesPersonRecyclerAdapter(Activity context, ArrayList<SalesPersonModel> modelArrayList, OnItemClickListener listener) {
        this.context = context;
        this.itemArrayList = modelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = listener;
    }


    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_sales_persons_list, parent, false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position) {

        setAnimation(holder.cv_itemSalesPerson, position);

        final SalesPersonModel myModel = itemArrayList.get(position);
        holder.mTv_name.setText(myModel.getFull_name() != null && !myModel.getFull_name().trim().isEmpty() ? myModel.getFull_name() : "--");
        holder.mTv_mobile.setText(myModel.getMobile_number() != null && !myModel.getMobile_number().trim().isEmpty() ? myModel.getMobile_number() : "--");

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

        @BindView(R.id.cv_itemSalesPerson) MaterialCardView cv_itemSalesPerson;
        @BindView(R.id.mTv_itemSalesPerson_name) MaterialTextView mTv_name;
        @BindView(R.id.mTv_itemSalesPerson_mobile) MaterialTextView mTv_mobile;

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
        void onItemClick(SalesPersonModel model);
    }

}