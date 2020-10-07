package com.tribeappsoft.leedo.salesPerson.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;

import com.tribeappsoft.leedo.salesPerson.direct_allotment.AllottedFlatDetail_Activity;
import com.tribeappsoft.leedo.salesPerson.models.AllottedFlatListModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllottedFlatListAdapter extends RecyclerView.Adapter<AllottedFlatListAdapter.AdapterViewHolder>  {

    private ArrayList<AllottedFlatListModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private Activity context;
    private String TAG="HoldFlatListAdapter", api_token;


    public AllottedFlatListAdapter(Activity context, ArrayList<AllottedFlatListModel> itemArraylist, String api_token) {
        this.context = context;
        this.itemArrayList = itemArraylist;
        this.anim = new Animations();
        this.api_token = api_token;
    }


    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_allotted_flat_list, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {
        //set def
        setAnimation(holder.cv_allottedFlatList, position);

        final AllottedFlatListModel myModel = itemArrayList.get(position);
        holder.tv_unit_type.setText(String.format("%s-%s | %s | %s", myModel.getBlock_name(), myModel.getUnit_name(), myModel.getUnit_category(), myModel.getProject_name()));
        holder.tv_customer_name.setText(myModel.getFull_name());
        holder.tv_token.setText(myModel.getLead_uid());

        holder.tv_flatAmount.setText(myModel.getFlat_total());
        holder.tv_applicant_name.setText(myModel.getExecutive_name());
        holder.tv_ownDate.setText(Helper.getNotificationFormatDate(myModel.getDate_created()));
        holder.tv_mobile_number.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getMobile_number()));
      //  Log.e(TAG, "onBindViewHolder: countUp millis_1 "+ TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp()));
        //long startTime = TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp());
        //final long[] timeInMilliseconds = {TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp())};

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                timeInMilliseconds[0] = timeInMilliseconds[0] + 1000;
                //startTime =  startTime + 1000;
                //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
                //holder.tv_badge.setText(curTime);
                //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

                String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0]),
                        TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0])),
                        TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0])));

              //  context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                new Handler().postDelayed(this, 1000);
            }
        }, 0);*/


        //customHandler.postDelayed(updateTimerThread, 0);
        holder.cancelAllotment.setOnClickListener(view -> showCancelAllotmentDialog(position,myModel,holder.pb_itemAllotFlats));

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

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        //remove all callbacks
       // customHandler.removeCallbacks(updater);
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_allottedFlatList) CardView cv_allottedFlatList;
        //@BindView(R.id.tv_project_name) AppCompatTextView tv_project_name;
        @BindView(R.id.tv_allottedFlat_unitType) AppCompatTextView tv_unit_type;
        //@BindView(R.id.tv_sp_name) AppCompatTextView tv_sp_name;
        @BindView(R.id.tv_allottedFlat_customerName) AppCompatTextView tv_customer_name;
        @BindView(R.id.tv_allottedFlat_mobileNumber) AppCompatTextView tv_mobile_number;
        @BindView(R.id.tv_allottedFlat_tokenNumber) AppCompatTextView tv_token;
        @BindView(R.id.tv_allotted_ownDate) AppCompatTextView tv_ownDate;
        @BindView(R.id.tv_allottedFlat_flatAmount) AppCompatTextView tv_flatAmount;
        @BindView(R.id.tv_allottedFlat_applicant_name) AppCompatTextView tv_applicant_name;
        @BindView(R.id.mBtn_allottedFlat_cancelAllotment) MaterialButton cancelAllotment;
        @BindView(R.id.pb_itemAllotFlats) ProgressBar pb_itemAllotFlats;

        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(v ->
            {
                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(context, AllottedFlatDetail_Activity.class);
                intent.putExtra("booking_id", itemArrayList.get(getAdapterPosition()).getBooking_id());
                intent.putExtra("allotModel", itemArrayList.get(getAdapterPosition()));
                context.startActivity(intent);

            });

        }

    }


    private void showCancelAllotmentDialog(int position, AllottedFlatListModel myModel, ProgressBar pb_itemAllotFlats)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to cancel this allotment ?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            //call add sales api
            if (Helper.isNetworkAvailable(context))
            {
                pb_itemAllotFlats.setVisibility(View.VISIBLE);
                call_cancelAllotment(position,myModel,pb_itemAllotFlats);
            }else Helper.NetworkError(context);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        /*builder.setSingleChoiceItems(singleChoiceListItems,-1,(dialogInterface, i) -> {
            dialogInterface.dismiss();
        });*/
        builder.show();
    }


    private void call_cancelAllotment(int position, AllottedFlatListModel myModel, ProgressBar pb_itemAllotFlats)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("booking_id", myModel.getBooking_id());
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().cancelAllotment(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {

                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessAlert(position,pb_itemAllotFlats);
                                }
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLog(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }


    private void showErrorLog(final String message) {
        context.runOnUiThread(() -> Helper.onErrorSnack(context, message));

    }


    @SuppressLint("InflateParams")
    private void showSuccessAlert(int position, ProgressBar pb_itemAllotFlats)
    {
        context.runOnUiThread(() -> {

            //  onErrorSnack(context, "Flat released successfully!");
            itemArrayList.remove(position);
            new Helper().showCustomToast(context, "Allotment cancelled successfully!!");
            notifyDataSetChanged();
            pb_itemAllotFlats.setVisibility(View.GONE);
        });

    }


}

