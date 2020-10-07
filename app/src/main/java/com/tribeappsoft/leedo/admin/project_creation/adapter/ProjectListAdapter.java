package com.tribeappsoft.leedo.admin.project_creation.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.project_creation.CreateProjectActivity;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.MyHolder>{

    private Activity context;
    private ArrayList<ProjectModel> projectModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private String TAG="ProjectListAdapter";


    public ProjectListAdapter(Activity context, ArrayList<ProjectModel> projectModelArrayList) {
        this.context = context;
        this.projectModelArrayList = projectModelArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_project_list_new, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final ProjectModel model= projectModelArrayList.get(position);
        holder.mTv_ProjectName.setText(model.getProject_name() != null && !model.getProject_name().trim().isEmpty()? model.getProject_name() :"-");
        holder.mTv_ProjectAddress.setText(model.getAddress() != null && !model.getAddress().trim().isEmpty()? model.getAddress() :"-");
        holder.mtv_ProjectDescription.setText(model.getDescription() != null && !model.getDescription().trim().isEmpty()? model.getDescription(): "");
        holder.mTv_ReraRegNo.setText(model.getReg_no() != null && !model.getReg_no().trim().isEmpty()? model.getReg_no(): "");
        holder.mTv_ProjectCSNo.setText(model.getCs_no() != null && !model.getCs_no().trim().isEmpty()? model.getCs_no(): "");
        holder.mTv_ProjectPermissionDate.setText(model.getPermission_date() != null && !model.getPermission_date().trim().isEmpty()? model.getPermission_date(): "");
        holder.mTv_ProjectType.setText(model.getProject_type() != null && !model.getProject_type().trim().isEmpty()? model.getProject_type(): "");
        holder.mTv_ProjectLatitude.setText(model.getLatitude() != null && !model.getLatitude().trim().isEmpty()? model.getLatitude(): "");
        holder.mTv_ProjectLongitude.setText(model.getLongitude() != null && !model.getLongitude().trim().isEmpty()? model.getLongitude(): "");

        holder.ll_ProjectDescription.setVisibility(model.getDescription() != null && !model.getDescription().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ReraRegNo.setVisibility(model.getReg_no() != null && !model.getReg_no().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ProjectCSNo.setVisibility(model.getCs_no() != null && !model.getCs_no().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ProjectPermissionDate.setVisibility(model.getPermission_date() != null && !model.getPermission_date().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ProjectHead.setVisibility(model.getProject_type() != null && !model.getProject_type().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ProjectLatitude.setVisibility(model.getLatitude() != null && !model.getLatitude().trim().isEmpty() ? View.VISIBLE :View.GONE);
        holder.ll_ProjectLongitude.setVisibility(model.getLongitude() != null && !model.getLongitude().trim().isEmpty() ? View.VISIBLE :View.GONE);

        //For Update
        holder.iv_editProjectDetails.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateProjectActivity.class);
            intent.putExtra("project_id", model.getProject_id());
            intent.putExtra("project_title", model.getProject_name());
            intent.putExtra("project_address", model.getAddress());
            intent.putExtra("project_description", model.getDescription());
            intent.putExtra("project_RERA_no", model.getReg_no());
            intent.putExtra("project_cs_no", model.getCs_no());
            intent.putExtra("project_permission_date", model.getPermission_date());
            intent.putExtra("project_type", model.getProject_type());
            intent.putExtra("project_type_id", model.getProject_type_id());
            intent.putExtra("project_latitude", model.getLatitude());
            intent.putExtra("project_longitude", model.getLongitude());
            intent.putExtra("fromOther", 2);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = holder.cv_mainList;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());
        });


        //set expand Collapse Own
        holder.ll_ProjectDetailsMain.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_ProjectDetails_ec, false);
                collapse(holder.ll_ViewProjectDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_ProjectDetails_ec, true);
                expandSubView(holder.ll_ViewProjectDetails);
                model.setExpandedOwnView(true);
            }
        });

        //set expand Collapse Own
        holder.cv_mainList.setOnClickListener(view -> {

            if (model.isExpandedOwnView())  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate( holder.iv_ProjectDetails_ec, false);
                collapse(holder.ll_ViewProjectDetails);
                model.setExpandedOwnView(false);
            }
            else    // collapsed
            {
                //do expand view
                new Animations().toggleRotate( holder.iv_ProjectDetails_ec, true);
                expandSubView(holder.ll_ViewProjectDetails);
                model.setExpandedOwnView(true);
            }
        });
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

    class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_ProjectList_mainList) MaterialCardView cv_mainList;
        @BindView(R.id.ll_itemList_ProjectDescription) LinearLayoutCompat ll_ProjectDescription;
        @BindView(R.id.ll_itemList_ReraRegNo) LinearLayoutCompat ll_ReraRegNo;
        @BindView(R.id.ll_itemList_ProjectCSNo) LinearLayoutCompat ll_ProjectCSNo;
        @BindView(R.id.ll_itemList_ProjectPermissionDate) LinearLayoutCompat ll_ProjectPermissionDate;
        @BindView(R.id.ll_itemList_ProjectHead) LinearLayoutCompat ll_ProjectHead;
        @BindView(R.id.ll_itemList_ProjectLatitude) LinearLayoutCompat ll_ProjectLatitude;
        @BindView(R.id.ll_itemList_ProjectLongitude) LinearLayoutCompat ll_ProjectLongitude;
        @BindView(R.id.ll_itemList_projectDetailsMain) LinearLayoutCompat ll_ProjectDetailsMain;
        @BindView(R.id.ll_itemList_ViewProjectDetails) LinearLayoutCompat ll_ViewProjectDetails;
        @BindView(R.id.iv_itemList_ProjectDetails_ec) AppCompatImageView iv_ProjectDetails_ec;
        @BindView(R.id.tv_itemList_ProjectName) MaterialTextView mTv_ProjectName;
        @BindView(R.id.mTv_itemList_ProjectAddress) MaterialTextView mTv_ProjectAddress;
        @BindView(R.id.mTv_itemList_ProjectDescription) MaterialTextView mtv_ProjectDescription;
        @BindView(R.id.mTv_itemList_ReraRegNo) MaterialTextView mTv_ReraRegNo;
        @BindView(R.id.mTv_itemList_ProjectCSNo) MaterialTextView mTv_ProjectCSNo;
        @BindView(R.id.mTv_itemList_ProjectPermissionDate) MaterialTextView mTv_ProjectPermissionDate;
        @BindView(R.id.mTv_itemList_ProjectType) MaterialTextView mTv_ProjectType;
        @BindView(R.id.mTv_itemList_ProjectLatitude) MaterialTextView mTv_ProjectLatitude;
        @BindView(R.id.mTv_itemList_ProjectLongitude) MaterialTextView mTv_ProjectLongitude;
        @BindView(R.id.iv_itemList_editProjectDetails) AppCompatImageView iv_editProjectDetails;



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
