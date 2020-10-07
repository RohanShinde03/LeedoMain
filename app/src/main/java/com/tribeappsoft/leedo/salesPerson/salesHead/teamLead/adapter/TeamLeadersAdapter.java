package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.AddTeamLeadActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.TeamLeadDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamLeaderModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeamLeadersAdapter extends RecyclerView.Adapter<TeamLeadersAdapter.MyHolder>{

    private Activity context;
    private ArrayList<TeamLeaderModel> teamLeaderModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private String TAG="TeamLeadersAdapter";


    public TeamLeadersAdapter(Activity context, ArrayList<TeamLeaderModel> teamLeaderModelArrayList) {
        this.context = context;
        this.teamLeaderModelArrayList = teamLeaderModelArrayList;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_team_lead_list_temp, parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //set animation
        setAnimation(holder.cv_mainList, position);

        final TeamLeaderModel model= teamLeaderModelArrayList.get(position);
        holder.mTv_teamMembers.setText(model.getTeamMembersCount() != null && !model.getTeamMembersCount().trim().isEmpty()? model.getTeamMembersCount() + " Team Members" : "0 Team Members");
        holder.mTv_Name.setText(model.getFull_name() != null && !model.getFull_name().trim().isEmpty()? model.getFull_name() :"-");
        holder.mTv_email.setText(model.getEmail() != null && !model.getEmail().trim().isEmpty()? model.getEmail(): "-");
        holder.mTv_mob.setText(model.getMobile_number() != null && !model.getMobile_number().trim().isEmpty()? model.getMobile_number(): "-");

       /* holder.cv_mainList.setOnClickListener(view -> {
            String transitionName = context.getString(R.string.transition_string);
            Intent intent = new Intent(context, TeamLeadDetailsActivity.class);
            // Define the view that the animation will start from
            View viewStart = holder.cv_mainList;

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());

        });*/

        if (Helper.isValidContextForGlide(context))
        {
            Glide.with(context)
                    .load(model.getPhotopath()!=null ?model.getPhotopath() : context.getResources().getDrawable(R.drawable.img_def_image_small_120))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.drawable.img_def_image_small_120))
                    .apply(new RequestOptions().error(R.drawable.img_def_image_small_120))
                    .into(holder.cIv_LeadImg);
        }


        //For Update
        holder.iv_editTeamLead.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddTeamLeadActivity.class);
            intent.putExtra("first_name", model.getFirst_name());
            Log.e(TAG, "onBindViewHolder: "+model.getFirst_name()+""+model.getMiddle_name()+""+model.getLast_name());
            intent.putExtra("middle_name", model.getMiddle_name());
            intent.putExtra("last_name", model.getLast_name());
            intent.putExtra("mobile_number", model.getMobile_number());
            intent.putExtra("email_id", model.getEmail());
            intent.putExtra("sales_lead_id", model.getSales_lead_id());
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

    @Override
    public int getItemCount() {
        return (null != teamLeaderModelArrayList ? teamLeaderModelArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_itemTeamLeadList_mainList) MaterialCardView cv_mainList;
        @BindView(R.id.mTv_itemLayout_leadName) MaterialTextView mTv_Name;
        @BindView(R.id.mTv_itemLayout_leadMob) MaterialTextView mTv_mob;
        @BindView(R.id.mTv_itemLayout_teamMembers) MaterialTextView mTv_teamMembers;
        @BindView(R.id.mTv_itemLayout_leadEmail) MaterialTextView mTv_email;
        @BindView(R.id.iv_item_editTeamLead) AppCompatImageView iv_editTeamLead;
        @BindView(R.id.cIv_itemLayout_LeadImg) CircleImageView cIv_LeadImg;


        MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(v ->
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

        }
    }
}
