package com.tribeappsoft.leedo.admin.users.adapter;

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
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.admin.users.AddNewUserActivity;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProjectSelectionAdapter extends RecyclerView.Adapter<ProjectSelectionAdapter.MyHolder> {
    private static final String TAG ="ProjectSelectionAdapter" ;
    public AddNewUserActivity context;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<Integer> projectNameIdArrayList;
    private final Animations anim;
    private int lastPosition = -1;

    public ProjectSelectionAdapter(AddNewUserActivity context, ArrayList<ProjectModel> projectModelArrayList, ArrayList<Integer> projectNameIdArrayList) {
        this.context = context;
        this.projectModelArrayList = projectModelArrayList;
       // this.groupIdArrayList = new ArrayList<>();
        this.projectNameIdArrayList = projectNameIdArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_project, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final ProjectModel model= projectModelArrayList.get(position);

        holder.chb_chatGroup.setChecked(projectModelArrayList.get(position).isCheckedBox());
        holder.mTv_Name.setText(model.getProject_name() != null && !model.getProject_name().trim().isEmpty()? model.getProject_name(): "--");

      /*  holder.chb_cp_fos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               // checkArrayList();
            }
        });*/

        holder.chb_chatGroup.setOnClickListener(v -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.chb_chatGroup.setChecked(false);
                //update model value
                model.setCheckedBox(false);

                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getProject_id(), false);
                //check arrayList
               // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.chb_chatGroup.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getProject_id(), true);
                //check arrayList
               // checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        holder.cv_mainList.setOnClickListener(view -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                holder.chb_chatGroup.setChecked(false);
                //update model value
                model.setCheckedBox(false);
                //remove selected id from arrayList
                checkInsertRemoveUserIds(model.getProject_id(), false);
                //check arrayList
               // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                holder.chb_chatGroup.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveUserIds(model.getProject_id(), true);
                //check arrayList
              //  checkArrayList();
                //arrayListId.add(holder.member_id);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != projectModelArrayList ? projectModelArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    private void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value)
        {
            projectNameIdArrayList.add(userID);
            Log.e(TAG, "checkInsertRemoveUserIds: "+userID );
            Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.toString() );
            Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.size() );
        }
            //else catStringArrayList.remove(new String(subcatName));
        else
        {
            projectNameIdArrayList.remove(Integer.valueOf(userID));

            Log.e(TAG, "checkInsertRemoveUserIds: "+userID );
            Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.toString() );
            Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.size() );
        }
    }

    public ArrayList<Integer> getProjectIdArrayList() {
        Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.toString() );
        Log.e(TAG, "getProjectIdArrayList: "+ projectNameIdArrayList.size() );
        return projectNameIdArrayList;
    }



    static class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_projects_list) MaterialCardView cv_mainList;
        @BindView(R.id.mTv_projects_list_name) MaterialTextView mTv_Name;
        @BindView(R.id.mTv_projects_title) MaterialTextView mTv_ChatGroup_title;
        @BindView(R.id.chb_projects_list) MaterialCheckBox chb_chatGroup;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }
}
