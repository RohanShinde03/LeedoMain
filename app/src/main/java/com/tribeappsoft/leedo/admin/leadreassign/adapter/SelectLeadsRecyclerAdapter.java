package com.tribeappsoft.leedo.admin.leadreassign.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leadreassign.SelectLeadsActivity;
import com.tribeappsoft.leedo.admin.leadreassign.model.AssignLeadsModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectLeadsRecyclerAdapter extends RecyclerView.Adapter<SelectLeadsRecyclerAdapter.MyHolder> {
    public SelectLeadsActivity context;
    private ArrayList<AssignLeadsModel> itemArrayList;
    private ArrayList<Integer> leadIdArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    // private int count=0;

    public SelectLeadsRecyclerAdapter(SelectLeadsActivity context, ArrayList<AssignLeadsModel> modelArrayList) {
        this.context = context;
        this.itemArrayList = modelArrayList;
        this.leadIdArrayList = new ArrayList<>();
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_select_leads_list, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final AssignLeadsModel model= itemArrayList.get(position);
        holder.mCb_itemSelectLeads.setChecked(model.isChecked());
        holder.mTv_ProjectName.setText(model.getProject_name() != null && !model.getProject_name().trim().isEmpty()? model.getProject_name(): "--");
        holder.mTv_leadName.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name(): "--");
        holder.mTv_leadMob.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");


        holder.mCb_itemSelectLeads.setOnClickListener(v -> {

            if(model.isChecked())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.mCb_itemSelectLeads.setChecked(false);
                //update model value
                model.setChecked(false);
                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getLead_id(), false);
                //check arrayList
                checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.mCb_itemSelectLeads.setChecked(true);
                //update model value
                model.setChecked(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getLead_id(), true);
                //check arrayList
                checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        holder.cv_mainList.setOnClickListener(view -> {

            if(model.isChecked())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.mCb_itemSelectLeads.setChecked(false);
                //update model value
                model.setChecked(false);
                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getLead_id(), false);
                //check arrayList
                checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.mCb_itemSelectLeads.setChecked(true);
                //update model value
                model.setChecked(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getLead_id(), true);
                //check arrayList
                checkArrayList();
                //arrayListId.add(holder.member_id);
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

    private void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value) leadIdArrayList.add(userID);
            //else catStringArrayList.remove(new String(subcatName));
        else leadIdArrayList.remove(new Integer(userID));
    }

    public ArrayList<Integer> getLeadIdArrayList() {
        return leadIdArrayList;
    }


    private void checkArrayList()
    {
        if (getLeadIdArrayList()!=null && getLeadIdArrayList().size()>0) {
            context.showButton();
        }
        else context.hideButton();
        //if (!arrayListId.isEmpty()) context.showButton();
        Log.e("TAG", "checkArrayList: "+ Arrays.toString(getLeadIdArrayList().toArray()));
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_itemSelectLeads_list) MaterialCardView cv_mainList;
        @BindView(R.id.mTv_itemSelectLeads_projectName) MaterialTextView mTv_ProjectName;
        @BindView(R.id.mTv_itemSelectLeads_leadName) MaterialTextView mTv_leadName;
        @BindView(R.id.mTv_itemSelectLeads_leadMob) MaterialTextView mTv_leadMob;
        @BindView(R.id.mCb_itemSelectLeads) MaterialCheckBox mCb_itemSelectLeads;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
