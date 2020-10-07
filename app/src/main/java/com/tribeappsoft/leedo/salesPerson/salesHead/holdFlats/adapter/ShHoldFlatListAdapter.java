package com.tribeappsoft.leedo.salesPerson.salesHead.holdFlats.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.FlatAllotmentActivity;
import com.tribeappsoft.leedo.salesPerson.models.HoldFlatModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ShHoldFlatListAdapter extends RecyclerView.Adapter<ShHoldFlatListAdapter.AdapterViewHolder>  {
    private ArrayList<HoldFlatModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private Activity context;
    //private long  updatedTime = 0L , timeSwapBuff =0L,timeInMilliseconds =0L;
    private final Handler customHandler = new Handler();
    //private Runnable updater;
    private String TAG="HoldFlatListAdapter", api_token;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;



    public ShHoldFlatListAdapter(Activity context, ArrayList<HoldFlatModel> itemArraylist, String api_token) {
        this.context = context;
        this.itemArrayList = itemArraylist;
        this.anim = new Animations();
        this.api_token = api_token;
    }

    @NonNull
    @Override
    public ShHoldFlatListAdapter.AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_hold_flat_list, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        setAnimation(holder.cv_holdflatlist, position);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        //hide allot button for sales head
        holder.btn_allot.setVisibility(View.GONE);
        //visible added by sales person
        holder.ll_addedBySalesPerson.setVisibility(View.VISIBLE);


        final HoldFlatModel myModel = itemArrayList.get(position);
        holder.tv_unit_type.setText(String.format("%s-%s | %s | %s", myModel.getBlock_name(), myModel.getUnit_name(), myModel.getUnit_category(),myModel.getProject_name()));
        holder.tv_customer_name.setText(myModel.getFull_name());
        holder.tv_token.setText(myModel.getLead_uid());
        holder.tv_mobile_number.setText(myModel.getMobile_number());
        holder.tv_dateTime.setText(Helper.getNotificationFormatDate(myModel.getHold_datetime()));
        holder.tv_mobile_number.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getMobile_number()));
        Log.e(TAG, "onBindViewHolder: countUp millis_1 "+ TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp()));


        //long startTime = TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp());
        final long[] timeInMilliseconds = {TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp())};

        new Handler().postDelayed(new Runnable() {
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

                context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                new Handler().postDelayed(this, 1000);
            }
        }, 0);


        /*final Handler customHandler = new Handler();
        Runnable runnablel = new Runnable() {

            public void run() {

                long startTim =  startTime + 1000;
                //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
                //holder.tv_badge.setText(curTime);
                //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

                String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTim),
                        TimeUnit.MILLISECONDS.toMinutes(startTim) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTim)),
                        TimeUnit.MILLISECONDS.toSeconds(startTim) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTim)));

                context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                customHandler.postDelayed(this, 1000);
            }
        };
        customHandler.postDelayed(runnablel, 0);*/



         /*Runnable updater = () -> {

            startTime =  startTime + 1000;
            //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
            //holder.tv_badge.setText(curTime);
            //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

            String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTime),
                    TimeUnit.MILLISECONDS.toMinutes(startTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTime)),
                    TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

            context.runOnUiThread(() -> holder.tv_badge.setText(hms));
            customHandler.postDelayed(this, 1000);
        };
        customHandler.postDelayed(updater, 0);*/


        //customHandler.postDelayed(updateTimerThread, 0);
        holder.btn_release.setOnClickListener(view -> showConfirmReleaseDialog(position,myModel, holder));

        // direct allotment
        holder.btn_allot.setOnClickListener(v -> context.startActivity(new Intent(context, FlatAllotmentActivity.class)
                .putExtra("unit_hold_release_id",myModel.getFlat_hold_release_id())
                .putExtra("unit_id", myModel.getUnit_id())
                .putExtra("unit_name", myModel.getUnit_name())
                .putExtra("project_name", myModel.getProject_name())
                .putExtra("project_id", myModel.getProject_id())
                .putExtra("cuidModel", myModel.getCuidModel())
                .putExtra("block_id", myModel.getBlock_id())
                .putExtra("floor_id", myModel.getFloor_id())
                .putExtra("fromAddHoldFlat", true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        //set hold duration
        //customHandler.postDelayed(getUpdateTimerThread(holder, startTime), 0);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }




    private Runnable getUpdateTimerThread(AdapterViewHolder holder, long startTime)
    {

        return new Runnable() {

            public void run() {


                long startTim =  startTime + 1000;
                //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
                //holder.tv_badge.setText(curTime);
                //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

                String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTim),
                        TimeUnit.MILLISECONDS.toMinutes(startTim) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTim)),
                        TimeUnit.MILLISECONDS.toSeconds(startTim) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTim)));

                context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                customHandler.postDelayed(this, 1000);
            }
        };
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

    static class AdapterViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_holdFlatList) CardView cv_holdflatlist;
        //@BindView(R.id.tv_project_name) AppCompatTextView tv_project_name;
        @BindView(R.id.tv_itemHoldFlat_unitType) AppCompatTextView tv_unit_type;
        //@BindView(R.id.tv_sp_name) AppCompatTextView tv_sp_name;
        @BindView(R.id.tv_itemHoldFlat_customerName) AppCompatTextView tv_customer_name;
        @BindView(R.id.tv_itemHoldFlat_mobileNumber) AppCompatTextView tv_mobile_number;
        @BindView(R.id.tv_itemHoldFlat_tokenNumber) AppCompatTextView tv_token;
        @BindView(R.id.tv_itemHoldFlat_holdDuration) AppCompatTextView tv_badge;
        @BindView(R.id.mBtn_itemHoldFlat_release) MaterialButton btn_release;
        @BindView(R.id.tv_holdFlatDetails_Date)AppCompatTextView tv_dateTime;
        @BindView(R.id.mBtn_itemHoldFlat_allot)MaterialButton btn_allot;
        @BindView(R.id.pb_itemHoldFlats) ProgressBar pb;

        @BindView(R.id.ll_itemHoldFlat_addedBySalesPerson) LinearLayoutCompat ll_addedBySalesPerson;
        @BindView(R.id.mTv_itemHoldFlat_flatHoldBy) MaterialTextView mTv_flatHoldBy;
        @BindView(R.id.ll_itemHoldFlat_remainingTime) LinearLayoutCompat ll_remainingTime;
        @BindView(R.id.mTv_itemHoldFlat_timeToAutoRelease) MaterialTextView mTv_timeToAutoRelease;

        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

    }


    private void showConfirmReleaseDialog(int position, HoldFlatModel myModel, AdapterViewHolder holder)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to release ?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            //call add sales api
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar(holder);
                call_markAsReleased(position,myModel, holder);
            }else Helper.NetworkError(context);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        /*builder.setSingleChoiceItems(singleChoiceListItems,-1,(dialogInterface, i) -> {
            dialogInterface.dismiss();
        });*/
        builder.show();
    }


    private void call_markAsReleased(int position, HoldFlatModel myModel, AdapterViewHolder holder)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("unit_hold_release_id", myModel.getFlat_hold_release_id());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().directReleaseFlat(jsonObject).enqueue(new Callback<JsonObject>()
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

                                    showSuccessAlert(position, holder);
                                }
                                else showErrorLog("Server response is empty!", holder);

                            }else showErrorLog("Invalid response from server!", holder);
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLog(msg, holder);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(context.getString(R.string.something_went_wrong_try_again), holder);
                            break;
                        case 500:
                            showErrorLog(context.getString(R.string.server_error_msg), holder);
                            break;
                        default:
                            showErrorLog(context.getString(R.string.unknown_error_try_again) + " "+response.code(), holder);
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out), holder);
                    else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection), holder);
                    else showErrorLog(e.toString(), holder);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }


    private void showErrorLog(final String message, AdapterViewHolder holder) {

        context.runOnUiThread(() -> {

            //hide pb
            hideProgressBar(holder);

            //show error snack
            Helper.onErrorSnack(context, message);
        });

    }

    private void hideProgressBar(AdapterViewHolder holder) {
        holder.pb.setVisibility(View.GONE);
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showProgressBar(AdapterViewHolder holder) {
        Helper.hideSoftKeyboard(context, context.getWindow().getDecorView().getRootView());
        holder.pb.setVisibility(View.VISIBLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @SuppressLint("InflateParams")
    private void showSuccessAlert(int position, AdapterViewHolder holder)
    {
        context.runOnUiThread(() -> {

            //  onErrorSnack(context, "Flat released successfully!");
            //remove item from arrayList
            itemArrayList.remove(position);

            //hide pb
            hideProgressBar(holder);


            //show toast
            new Helper().showSuccessCustomToast(context, context.getString(R.string.flat_released_successfully));
            notifyDataSetChanged();

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

        });

    }


}



/* new Thread(() -> {
         try {

         while (!Thread.interrupted())
         {
         Thread.sleep(10);

         //int hrs, minutes, seconds;
         Calendar c = Calendar.getInstance();   //2019-10-09 10:42:15
         //hrs = c.get(Calendar.HOUR_OF_DAY);
         //minutes = c.get(Calendar.MINUTE);
         int seconds = c.get(Calendar.SECOND);
         startTime =  startTime + seconds;

         //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
         //holder.tv_badge.setText(curTime);
         //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

         String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTime),
         TimeUnit.MILLISECONDS.toMinutes(startTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTime)),
         TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

         context.runOnUiThread(() -> holder.tv_badge.setText(hms));
         }
         }
         catch (Exception e)
         {
         e.printStackTrace();
         }

         }).start();*/

//startTime = SystemClock.uptimeMillis();
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                // Log.e(TAG, "run:startTime "+startTime);
               // Log.e(TAG, "run:SystemClock.uptimeMillis() " + SystemClock.uptimeMillis());
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                //timeInMilliseconds = elapsedTime - startTime;
                updatedTime = timeSwapBuff + timeInMilliseconds;

                //Log.e(TAG, "run: "+ timeInMilliseconds );
                int secs = (int) (updatedTime / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                int hours = secs / 3600;
                int milliseconds = (int) (updatedTime % 1000);
                *//*holder.tv_badge.setText("" + mins + " m : "
                        + String.format("%02d", secs) + " s : "
                        + String.format("%02d", milliseconds) + " ms");*//*
                //holder.tv_badge.setText(String.format("%d m : %s s ", mins, String.format("%02d", secs), Locale.getDefault()));
                //holder.tv_badge.setText(String.format(Locale.getDefault(), "%02d h :%02d m :%02d s", hours, mins, secs));


                //int hrs, minutes, seconds;
                Calendar c = Calendar.getInstance();   //2019-10-09 10:42:15
                //hrs = c.get(Calendar.HOUR_OF_DAY);
                //minutes = c.get(Calendar.MINUTE);
                int seconds = c.get(Calendar.SECOND);
                startTime =  startTime + seconds;

                //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
                //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));
                //holder.tv_badge.setText(curTime);


                String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTime),
                        TimeUnit.MILLISECONDS.toMinutes(startTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTime)),
                        TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

                holder.tv_badge.setText(hms);

                new Handler().postDelayed(this, 0);

            }
        }, 0);
*/


       /* new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                //timer.setText(timeString);
                //int hrs, minutes, seconds;
                Calendar c = Calendar.getInstance();   //2019-10-09 10:42:15
                //hrs = c.get(Calendar.HOUR_OF_DAY);
                //minutes = c.get(Calendar.MINUTE);
                int seconds = c.get(Calendar.SECOND);
                startTime =  startTime + seconds;

                //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
                //holder.tv_badge.setText(curTime);
                //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

                String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTime),
                        TimeUnit.MILLISECONDS.toMinutes(startTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTime)),
                        TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

                context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                //customHandler.postDelayed(updater,1000);

            }
        }, 0, 100);*/ //1000 is a Refreshing Time (1second)

