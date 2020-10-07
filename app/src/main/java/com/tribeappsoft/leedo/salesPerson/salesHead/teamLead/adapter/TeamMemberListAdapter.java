package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.SelectTeamMembersActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamListModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamMemberListAdapter extends RecyclerView.Adapter<TeamMemberListAdapter.MyHolder> {
    public SelectTeamMembersActivity context;
    private ArrayList<TeamListModel> cpFosListModelArrayList;
    private ArrayList<Integer> salesUserIdArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    // private int count=0;

    public TeamMemberListAdapter(SelectTeamMembersActivity context, ArrayList<TeamListModel> cpFosListModelArrayList, ArrayList<Integer> salesUserIdArrayList) {
        this.context = context;
        this.cpFosListModelArrayList = cpFosListModelArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_team_member_list, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final TeamListModel model= cpFosListModelArrayList.get(position);

        holder.chb_cp_fos.setChecked(cpFosListModelArrayList.get(position).isCheckedBox());
        holder.mTv_Name.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name(): "--");
        holder.mTv_mob.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "--");
        //holder.member_id=model.getCp_executive_id();

      /*  holder.chb_cp_fos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               // checkArrayList();
            }
        });*/

        holder.chb_cp_fos.setOnClickListener(v -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.chb_cp_fos.setChecked(false);
                //update model value
                model.setCheckedBox(false);
                //remove selected id from arrayList
                context.checkInsertRemoveUserIds(model.getUser_id(), false);
                //check arrayList
                context.checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.chb_cp_fos.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                context.checkInsertRemoveUserIds(model.getUser_id(), true);
                //check arrayList
                context.checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        holder.cv_mainList.setOnClickListener(view -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.chb_cp_fos.setChecked(false);
                //update model value
                model.setCheckedBox(false);
                //remove selected id from arrayList
               context.checkInsertRemoveUserIds(model.getUser_id(), false);
                //check arrayList
                context.checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.chb_cp_fos.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                context.checkInsertRemoveUserIds(model.getUser_id(), true);
                //check arrayList
                context.checkArrayList();
                //arrayListId.add(holder.member_id);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != cpFosListModelArrayList ? cpFosListModelArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

   /* private void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value) salesUserIdArrayList.add(userID);
            //else catStringArrayList.remove(new String(subcatName));
        else salesUserIdArrayList.remove(new Integer(userID));
    }

    public ArrayList<Integer> getSalesUserIdArrayList() {
        return salesUserIdArrayList;
    }


    private void checkArrayList()
    {
        if (getSalesUserIdArrayList()!=null && getSalesUserIdArrayList().size()>0) {
            context.showButton();
        }
        else context.hideButton();
        //if (!arrayListId.isEmpty()) context.showButton();
        Log.e("TAG", "checkArrayList: "+ Arrays.toString(getSalesUserIdArrayList().toArray()));
    }
*/
    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_cp_fos_list) MaterialCardView cv_mainList;
        @BindView(R.id.mTv_cp_fos_list_name) MaterialTextView mTv_Name;
        @BindView(R.id.mTv_mTv_cp_fos_list_mob) MaterialTextView mTv_mob;
        @BindView(R.id.chb_cp_fos_list) AppCompatCheckBox chb_cp_fos;
        //private int member_id=0;
        //int count=0;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            /*  if(chb_cp_fos.isChecked())*/

            //  count++;

           /* if(count>0)
            {
                cpFoslistActivity.showButton();
            }*/

            /*itemView.setOnClickListener(v ->
            {
                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, HospitalDetailsActivity.class);
                intent.putExtra("hospital_id", hospitalModelArrayList.get(getAdapterPosition()).getHospital_id());
                context.startActivity(intent);

                // Get the transition name from the string
                //String transitionName = context.getString(R.string.transition_string);

                // Define the view that the animation will start from
                //View viewStart = cv_mainList;

                //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
                //Start the Intent
                //ActivityCompat.startActivity(context, intent, options.toBundle());
            });*/

        }
    }
}
