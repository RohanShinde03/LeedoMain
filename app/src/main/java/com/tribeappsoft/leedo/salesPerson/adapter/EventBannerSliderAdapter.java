package com.tribeappsoft.leedo.salesPerson.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.bookings.BookingEventDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventBannerSliderAdapter extends SliderViewAdapter<EventBannerSliderAdapter.SliderViewHolder> {

    private Context context;
    private ArrayList<EventsModel>eventsModelArrayList;


    public EventBannerSliderAdapter(Context context, ArrayList<EventsModel> eventsModelArrayList) {

        this.context = context;
        this.eventsModelArrayList = eventsModelArrayList;
    }

    @Override
    public SliderViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_feed_slider, null);
        return new SliderViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SliderViewHolder viewHolder, int position)
    {
        viewHolder.cv_itemFeedSlider.setOnClickListener(view -> context.startActivity(new Intent(context, BookingEventDetailsActivity.class).putExtra("event_id",eventsModelArrayList.get(position).getEvent_id())));
        viewHolder.tv_FeedSlider_EventName.setText(eventsModelArrayList.get(position).getEvent_title());
        if (Helper.isValidContextForGlide(context)) {
            Glide.with(context)//getActivity().this
                    .load(eventsModelArrayList.get(position).getEvent_banner_path())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().fitCenter())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(viewHolder.iv_itemFeedSlider_Image);
        }

    }

    @Override
    public int getCount() {
        return (null != eventsModelArrayList ? eventsModelArrayList.size() : 0);
    }


    class SliderViewHolder extends SliderViewAdapter.ViewHolder {

        View itemView;
        @BindView(R.id.cv_itemFeedSlider) CardView cv_itemFeedSlider;
        @BindView(R.id.iv_itemFeedSlider_Image) AppCompatImageView iv_itemFeedSlider_Image;
        @BindView(R.id.tv_FeedSlider_EventName) AppCompatTextView tv_FeedSlider_EventName;

        SliderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
           /* itemView.setOnClickListener(view -> {
                context.startActivity(new Intent(context, BookingEventDetailsActivity.class).putExtra("event_id", 1));
            });*/
        }
    }
}