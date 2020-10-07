package com.tribeappsoft.leedo.admin.users.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.users.AddNewUserActivity;
import com.tribeappsoft.leedo.admin.users.model.UserModel;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.util.Animations;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyHolder>{

    private Activity context;
    private ArrayList<UserModel> userModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private String TAG="UserListAdapter";


    public UserListAdapter(Activity context, ArrayList<UserModel> projectModelArrayList, ArrayList<Integer> projectNameIdArrayList) {
        this.context = context;
        this.userModelArrayList = projectModelArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_users_list, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final UserModel model= userModelArrayList.get(position);
        holder.mTv_UserName.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name() :"-");
        holder.mTv_UserEmail.setText(model.getEmail() != null && !model.getEmail().trim().isEmpty()? model.getEmail() :"-");
        holder.mTv_UserMob.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "");
        holder.mTv_AssignedProjects.setText(model.getAssigned_project() != null && !model.getAssigned_project().trim().isEmpty()? model.getAssigned_project(): "");
     //   holder.mTv_UserRole.setText(model.getUser_role() != null && !model.getUser_role().trim().isEmpty()? model.getUser_role(): "");

        // holder.ll_AssignedProjects.setVisibility(model.getAssigned_project() != null && !model.getAssigned_project().trim().isEmpty() ? View.VISIBLE :View.GONE);
       // holder.mTv_UserRole.setVisibility(model.getUser_role() != null && !model.getUser_role().trim().isEmpty() ? View.VISIBLE :View.GONE);

        if (model.getProjectModelArrayList()!=null && model.getProjectModelArrayList().size()>0)
        {
            holder.mTv_AssignedProjects.setVisibility(View.GONE);
            holder.ll_ProjectsClick.setVisibility(View.VISIBLE);
            holder.ll_addAssignedProjects.removeAllViews();

            for (int i = 0; i < model.getProjectModelArrayList().size(); i++) {
                View rowView_sub = getAssignedProjectView(i,model);
                holder.ll_addAssignedProjects.addView(rowView_sub);
            }

        } else
        {
            holder.ll_addAssignedProjects.setVisibility(View.GONE);
            holder.mTv_AssignedProjects.setVisibility(View.VISIBLE);
            holder.ll_ProjectsClick.setVisibility(View.GONE);
        }

        //to set AssignedRolesView
        if (model.getUserRoleModelArrayList()!=null && model.getUserRoleModelArrayList().size()>0)
        {
           // holder.mTv_AssignedProjects.setVisibility(View.GONE);
            holder.hs_Horizontal_scrollView.setVisibility(View.VISIBLE);
            holder.ll_AssignedRoles.setVisibility(View.VISIBLE);
           // holder.hs_Horizontal_scrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
            holder.ll_AssignedRoles.removeAllViews();

            for (int i = 0; i < model.getUserRoleModelArrayList().size(); i++) {
                View rowView_sub = getAssignedRolesView(i,model);
                holder.ll_AssignedRoles.addView(rowView_sub);
            }

        } else
        {
            holder.ll_AssignedRoles.setVisibility(View.GONE);
            holder.hs_Horizontal_scrollView.setVisibility(View.GONE);
           // holder.mTv_AssignedProjects.setVisibility(View.VISIBLE);
        }


        holder.ll_ProjectsClick.setOnClickListener(v -> {
            if (model.isExpand())  //expanded
            {
                // //do collapse View
                anim.toggleRotate(holder.iv_dropDown_projects, false);
                collapse(holder.ll_addAssignedProjects);
                model.setExpand(false);
            } else    // collapsed
            {
                //do expand view
                anim.toggleRotate(holder.iv_dropDown_projects, true);
                expandSubView(holder.ll_addAssignedProjects);
                model.setExpand(true);
            }
        });


        holder.cv_mainList.setOnClickListener(v -> {
            if (model.isExpand())  //expanded
            {
                // //do collapse View
                anim.toggleRotate(holder.iv_dropDown_projects, false);
                collapse(holder.ll_addAssignedProjects);
                model.setExpand(false);
            } else    // collapsed
            {
                //do expand view
                anim.toggleRotate(holder.iv_dropDown_projects, true);
                expandSubView(holder.ll_addAssignedProjects);
                model.setExpand(true);
            }
        });

        //For Update
        holder.iv_item_editUser.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddNewUserActivity.class);
            intent.putExtra("user_id", model.getUser_id());
            intent.putExtra("person_id", model.getPerson_id());
            intent.putExtra("user_prefix", model.getPrefix());
            intent.putExtra("user_prefix_id", model.getPrefix_id());
            intent.putExtra("user_name", model.getFirst_name()+" "+model.getLast_name());
            intent.putExtra("user_email", model.getEmail());
            intent.putExtra("user_mobile", model.getMobile_number());
            intent.putExtra("user_model", (Serializable) model);
            Log.e(TAG, "setAssignedRolesJson: "+ model.getUserRoleModelArrayList());
            intent.putExtra("user_role_type_id", model.getUser_role_id());
            intent.putExtra("user_role_type_id", model.getUser_role_id());
            intent.putExtra("projectArrayList", model.getAssignedProjectArrayList()!=null && model.getAssignedProjectArrayList().size()>0 ? model.getAssignedProjectArrayList() : new ArrayList<Integer>() );
            intent.putExtra("rolesArrayList",  model.getAssignedRolesArrayList()!=null && model.getAssignedRolesArrayList().size()>0 ? model.getAssignedRolesArrayList() : new ArrayList<Integer>());
            intent.putExtra("fromOther", 2);


            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = holder.cv_mainList;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());
        });


    }

    private View getAssignedProjectView(final int position, UserModel model) {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_assign_project, null);

        final AppCompatTextView tv_serviceName = rowView_sub.findViewById(R.id.tv_itemServices_serviceName);
        tv_serviceName.setText(String.valueOf(model.getProjectModelArrayList().get(position).getProject_name()));
        final AppCompatTextView tv_itemServices_dot = rowView_sub.findViewById(R.id.tv_itemServices_dot);
        tv_itemServices_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
        return rowView_sub;
    }

    private View getAssignedRolesView(final int position, UserModel model) {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_assign_roles, null);

        final AppCompatTextView tv_roleName = rowView_sub.findViewById(R.id.mTv_itemLayout_UserRole);
        tv_roleName.setText(String.valueOf(model.getUserRoleModelArrayList().get(position).getRole_name()));
        return rowView_sub;
    }


    private void collapse(final View v) {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    private void expandSubView(final View v) {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime == 1)
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    @Override
    public int getItemCount() {
        return (null != userModelArrayList ? userModelArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_UserList_mainList) MaterialCardView cv_mainList;
        @BindView(R.id.cIv_itemLayout_UserImg) CircleImageView cIv_UserImg;
        @BindView(R.id.iv_item_editUser) AppCompatImageView iv_item_editUser;
        @BindView(R.id.ll_itemList_AssignedProjects) LinearLayoutCompat ll_AssignedProjects;
        @BindView(R.id.hs_Horizontal_scrollView) HorizontalScrollView hs_Horizontal_scrollView;
        @BindView(R.id.ll_itemList_AssignedRoles) LinearLayoutCompat ll_AssignedRoles;
        @BindView(R.id.mTv_itemLayout_UserName) MaterialTextView mTv_UserName;
        @BindView(R.id.mTv_itemLayout_UserEmail) MaterialTextView mTv_UserEmail;
        @BindView(R.id.mTv_itemLayout_UserMob) MaterialTextView mTv_UserMob;
        @BindView(R.id.mTv_itemLayout_AssignedProjects) MaterialTextView mTv_AssignedProjects;
        @BindView(R.id.mTv_itemLayout_UserRole) MaterialTextView mTv_UserRole;
        @BindView(R.id.ll_itemList_addAssignedProjects) LinearLayoutCompat ll_addAssignedProjects;
        @BindView(R.id.ll_itemLayout_ProjectsClick) LinearLayoutCompat ll_ProjectsClick;
        @BindView(R.id.iv_itemLayout_dropDown_projects) AppCompatImageView iv_dropDown_projects;



        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
          /*  itemView.setOnClickListener(v ->
            {
                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, TeamLeadDetailsActivity.class);
                intent.putExtra("sales_lead_id", teamLeaderModelArrayList.get(getAdapterPosition()).getSales_lead_id());
                //context.startActivity(intent);

                Log.e(TAG, "MyHolder: " );
                // Get the transition name from the string
                String transitionName = context.getString(R.string.transition_string);

                // Define the view that the animation will start from
                View viewStart = cv_mainList;

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
                //Start the Intent
                ActivityCompat.startActivity(context, intent, options.toBundle());
            });
*/
        }
    }
}
