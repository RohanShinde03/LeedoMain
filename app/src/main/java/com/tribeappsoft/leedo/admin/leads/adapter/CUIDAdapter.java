package com.tribeappsoft.leedo.admin.leads.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.util.Animations;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CUIDAdapter extends RecyclerView.Adapter<CUIDAdapter.AdapterViewHolder>  {

    private ArrayList<CUIDModel> cuidModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private Activity context;
    private final OnItemClickListener onItemClickListener;


    public CUIDAdapter(Activity context, ArrayList<CUIDModel> cuidModelArrayList, OnItemClickListener listener) {
        this.context = context;
        this.cuidModelArrayList = cuidModelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = listener;
    }



    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_cu_id, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {

        setAnimation(holder.cv_itemList, position);

        final CUIDModel myModel = cuidModelArrayList.get(position);
        holder.tv_cuid_number.setText(myModel.getCustomer_mobile() != null && !myModel.getCustomer_mobile().trim().isEmpty()? myModel.getCustomer_mobile(): "--");
        holder.tv_name.setText(myModel.getCustomer_name() != null && !myModel.getCustomer_name().trim().isEmpty()? myModel.getCustomer_name(): "--");
        holder.tv_assigned_by.setText(myModel.getAssigned_by() != null && !myModel.getAssigned_by().trim().isEmpty()? String.format("Added by : %s", myModel.getAssigned_by()) : "--");


        if (myModel.getIsMyLead() == 1) holder.tv_assigned_by.setVisibility(View.GONE);
        else holder.tv_assigned_by.setVisibility(View.GONE);


        Log.e("myModel.getIsMyLead()", "onBindViewHolder: "+myModel.getIsMyLead() );

    }

    @Override
    public int getItemCount() {
        return (null != cuidModelArrayList ? cuidModelArrayList.size() : 0);
    }


    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }


    class AdapterViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_cuidlist) CardView cv_itemList;
        @BindView(R.id.tv_cuid_number) AppCompatTextView tv_cuid_number;
        @BindView(R.id.tv_customer_name) AppCompatTextView tv_name;
        @BindView(R.id.tv_CUID_assigned_by) AppCompatTextView tv_assigned_by;

        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            //set click listener
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(cuidModelArrayList.get(getAdapterPosition())));

        }

    }

    public interface OnItemClickListener
    {
        void onItemClick(CUIDModel cuidModel);
    }

}














                                        /*Commented Code*/
/*   itemView.setOnClickListener(v -> onItemClickListener.onItemClick(countryArrayList.get(getAdapterPosition())));*/

           /* itemView.setOnClickListener(v ->
            {
                *//*Intent intent=new Intent(context, Student_EventDetails_Activity.class);
                intent.putExtra("event_id", eventModelArrayList.get(getAdapterPosition()).getEvent_id());
                context.startActivity(intent);*//*

                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, BookingEventDetailsActivity.class);
                intent.putExtra("cuid_id", cuidModelArrayList.get(getAdapterPosition()).getLead_id());

                // Get the transition name from the string
                String transitionName = context.getString(R.string.transition_string);

                // Define the view that the animation will start from
                View viewStart = cv_itemList;

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
                //Start the Intent
                ActivityCompat.startActivity(context, intent, options.toBundle());


            });
*/




 /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(context, SiteVisitActivity.class);
                    mIntent.putExtra("cuidnumber", cuidModelArrayList.get(getAdapterPosition()).getCuId());
                    mIntent.putExtra("customername", cuidModelArrayList.get(getAdapterPosition()).getFull_name());
                    context.startActivity(mIntent);
                }
            });*/

//set click listener
           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cuidModelArrayList.get(getAdapterPosition());
                }
            });*/

/*




 * *//* public CUIDAdapter(CustomeridActivity customeridActivity, ArrayList<CUIDModel> cuidModelArrayList, CUIDAdapter.OnItemClickListener listener) {

        this.context = customeridActivity;
        this.cuidModelArrayList = cuidModelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = listener;
    }*//*

    public CUIDAdapter(Activity context, ArrayList<CUIDModel> cuidModelArrayList, CUIDAdapter.OnItemClickListener listener) {
        this.context = context;
        this.cuidModelArrayList = cuidModelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = listener;
    }

   *//* public CUIDAdapter(Activity context, ArrayList<CUIDModel> mFilteredCountries, OnItemClickListener result) {
        this.context = context;
        this.cuidModelArrayList = cuidModelArrayList;
        this.anim = new Animations();
        this.onItemClickListener = result;
    }
*/