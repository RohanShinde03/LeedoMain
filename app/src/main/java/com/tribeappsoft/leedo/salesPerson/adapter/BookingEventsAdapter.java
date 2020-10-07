package com.tribeappsoft.leedo.salesPerson.adapter;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.bookings.BookingEventDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BookingEventsAdapter extends RecyclerView.Adapter<BookingEventsAdapter.AdapterViewHolder> {

    private Activity context;
    private ArrayList<EventsModel> eventModelArrayList;
    private final Animations anim;
    private int lastPosition = -1,event_status;


    public BookingEventsAdapter(Activity activity, ArrayList<EventsModel> eventModelArrayList, int event_status) {

        this.eventModelArrayList = eventModelArrayList;
        this.context = activity;
        this.anim = new Animations();
        this.event_status =event_status;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_event, parent,false);
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

        final EventsModel myModel = eventModelArrayList.get(position);

        eventModelArrayList.size();

        holder.tv_title.setText(myModel.getEvent_title());
        holder.tv_date.setText(String.format("%s to %s", Helper.formatEventDate(myModel.getStart_date()), Helper.formatEventDate(myModel.getEnd_date())));
        holder.tv_des.setText(myModel.getEvent_description());
        holder.tv_location.setText(myModel.getEvent_venue());


        if (Helper.isValidContextForGlide(context))
        {
            Glide.with(context)//getActivity().this
                    .load(myModel.getEvent_banner_path())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(holder.iv_eventImage);
        }


    }

    @Override
    public int getItemCount() {
        return (null != eventModelArrayList ? eventModelArrayList.size() : 0);
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

        @BindView(R.id.cv_eventList) CardView cv_itemList;
        @BindView(R.id.iv_event_eventImage) AppCompatImageView iv_eventImage;
        @BindView(R.id.tv_event_title) AppCompatTextView tv_title;
        @BindView(R.id.tv_event_date) AppCompatTextView tv_date;
        @BindView(R.id.tv_event_location) AppCompatTextView tv_location;
        @BindView(R.id.tv_event_des) AppCompatTextView tv_des;

        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(v ->
            {
                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, BookingEventDetailsActivity.class);
                intent.putExtra("event_id", eventModelArrayList.get(getAdapterPosition()).getEvent_id());
                intent.putExtra("event_status", event_status);
                intent.putExtra("event_title", eventModelArrayList.get(getAdapterPosition()).getEvent_title());

                // Get the transition name from the string
                String transitionName = context.getString(R.string.transition_string);

                // Define the view that the animation will start from
                View viewStart = cv_itemList;

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
                //Start the Intent
                ActivityCompat.startActivity(context, intent, options.toBundle());


            });


        }

    }


}