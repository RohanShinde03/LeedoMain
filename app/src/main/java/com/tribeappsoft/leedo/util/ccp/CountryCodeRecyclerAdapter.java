package com.tribeappsoft.leedo.util.ccp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.Animations;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CountryCodeRecyclerAdapter extends RecyclerView.Adapter<CountryCodeRecyclerAdapter.MyAdapterViewHolder> {

    // private Activity context;
    private Activity context;
    private List<Country> countryArrayList;
    private final CountryCodePicker mCountryCodePicker;
    private final Animations anim;
    private int lastPosition = -1;
    private final OnItemClickListener onItemClickListener;


    public CountryCodeRecyclerAdapter(Activity context, List<Country> countryArrayList, CountryCodePicker picker, OnItemClickListener listener) {
        this.context = context;
        this.countryArrayList = countryArrayList;
        this.anim = new Animations();
        this.mCountryCodePicker = picker;
        this.onItemClickListener = listener;
    }


    @NonNull
    @Override
    public MyAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ccp_item_country, parent, false);
        return new MyAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapterViewHolder holder, int position) {

        setAnimation(holder.cv_mainList, position);
        final Country myModel = countryArrayList.get(position);

        String name = myModel.getName();
        String iso = myModel.getIso().toUpperCase();
        String countryNameAndCode = context.getString(R.string.country_name_and_code, name, iso);

        holder.tv_name.setText(countryNameAndCode);
        holder.tv_code.setText(myModel.getPhoneCode());

        if (mCountryCodePicker.isHidePhoneCode()) {
            holder.tv_code.setVisibility(View.GONE);
        } else {
            holder.tv_code.setText(context.getString(R.string.phone_code, myModel.getPhoneCode()));
        }

        holder.iv_flag_img.setImageResource(CountryUtils.getFlagDrawableResId(myModel));


        /*Typeface typeface = mCountryCodePicker.getTypeFace();
        if (typeface != null) {
            holder.tv_code.setTypeface(typeface);
            holder.tv_name.setTypeface(typeface);
        }
        int color = mCountryCodePicker.getDialogTextColor();
        if (color != mCountryCodePicker.getDefaultContentColor()) {
            holder.tv_code.setTextColor(color);
            holder.tv_name.setTextColor(color);
        }*/
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }



    @Override
    public int getItemCount() {
        return (null !=  countryArrayList ? countryArrayList.size() : 0);
    }

    public class MyAdapterViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.cv_itemCountryList)
        MaterialCardView cv_mainList;
        @BindView(R.id.tv_country_name)
        AppCompatTextView tv_name;
        @BindView(R.id.tv_country_code)
        AppCompatTextView tv_code;
        @BindView(R.id.iv_flag_img)
        AppCompatImageView iv_flag_img;


        public MyAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            //set click listener
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(countryArrayList.get(getAdapterPosition())));

        }

//        void setData(int size, int position) {
//
//            Log.e("size", "size: " + size);
//            Log.e("position", "position: " + position);
//
//            //This is used for disabled bottom line of bottom card in recyclerview....
//            if (position < size - 1) {
//                colleagueview.setVisibility(View.VISIBLE);
//            } else {
//                colleagueview.setVisibility(View.GONE);
//            }
//        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(Country country);
    }

}
