package com.tribeappsoft.leedo.salesPerson.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.DirectHoldFlatsActivity;
import com.tribeappsoft.leedo.salesPerson.direct_allotment.FlatAllotmentActivity;
import com.tribeappsoft.leedo.salesPerson.models.HoldFlatModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

public class DirectHoldFlatListAdapter extends RecyclerView.Adapter<DirectHoldFlatListAdapter.AdapterViewHolder>  {
    private ArrayList<HoldFlatModel> itemArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private DirectHoldFlatsActivity context;
    private String TAG="HoldFlatListAdapter", api_token;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;



    public DirectHoldFlatListAdapter(DirectHoldFlatsActivity context, ArrayList<HoldFlatModel> itemArrayList, String api_token) {
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.anim = new Animations();
        this.api_token = api_token;
    }

    @NonNull
    @Override
    public DirectHoldFlatListAdapter.AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_hold_flat_list, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        setAnimation(holder.cv_holdflatlist, position);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();

        final HoldFlatModel myModel = itemArrayList.get(position);

        holder.mTv_flatHoldBy.setText(isSalesHead ?  "Me" : myModel.getSales_person_name()!=null ? myModel.getSales_person_name() : "Sales Executive" );
        holder.btn_allot.setVisibility(isSalesHead ? myModel.getCan_allot() ==1 ? View.VISIBLE : View.GONE : View.VISIBLE);

        if (isSalesHead) {
            //set hold by in front of project name
            holder.tv_unit_type.setText(String.format("%s-%s | %s | %s | Hold by : %s", myModel.getBlock_name(), myModel.getUnit_name(), myModel.getUnit_category(),myModel.getProject_name(), myModel.getCan_allot() ==1 ? "Me" :  myModel.getSales_person_name()!=null && !myModel.getSales_person_name().trim().isEmpty()? myModel.getSales_person_name() : "Sales Executive" ));
            //visible extend time only for Sales Head
            holder.mBtn_extendTime.setVisibility(View.VISIBLE);
        }
        else {
            holder.tv_unit_type.setText(String.format("%s-%s | %s | %s ", myModel.getBlock_name(), myModel.getUnit_name(), myModel.getUnit_category(),myModel.getProject_name() ));
            //hide extend time btn
            holder.mBtn_extendTime.setVisibility(View.GONE);
        }

        holder.tv_customer_name.setText(myModel.getFull_name());
        holder.tv_token.setText(myModel.getLead_uid());
        holder.tv_mobile_number.setText(myModel.getMobile_number());
        holder.tv_dateTime.setText(Helper.getNotificationFormatDate(myModel.getHold_datetime()));
        holder.tv_mobile_number.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getMobile_number()));
        //make a phone call
        holder.iv_call.setOnClickListener(view -> {
            if (myModel.getMobile_number()!=null && !myModel.getMobile_number().trim().isEmpty()) new Helper().openPhoneDialer(context, myModel.getMobile_number());
            else new Helper().showCustomToast(context, "Customer Mobile number is empty!");
        });
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

                String hms = String.format(Locale.getDefault(), "%02d Hrs : %02d Min", TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0]),
                        TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0])));

               /* if (TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0])>0) {

                hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0]),
                            TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0])),
                            TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0])));
                }
                else {

                    hms = String.format(Locale.getDefault(), "%02d m : %02d s",
                            TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds[0])),
                            TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds[0])));
                }*/

                context.runOnUiThread(() -> holder.tv_badge.setText(hms));
                new Handler().postDelayed(this, 1000);
            }
        }, 0);


       /* //set elapsed time
        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() { handler.post(() -> timeLaps(holder, myModel)); }
        };
        //new Timer().scheduleAtFixedRate(doAsynchronousTask, 0, 60000);
        new Timer().scheduleAtFixedRate(doAsynchronousTask, 0, 1000);*/

        //set remaining time to auto release
        long expireCountUp = TimeUnit.MICROSECONDS.toMillis(myModel.getExpireCountUp());
        if (expireCountUp>0) {
            //visible layout
            holder.ll_remainingTime.setVisibility(View.VISIBLE);
            //set countdown timer
            startCountDownTimer(holder, myModel);
        }
        else holder.ll_remainingTime.setVisibility(View.GONE);


        //release hold flat
        holder.btn_release.setOnClickListener(view -> showReleaseHoldAlert(position,myModel, holder));

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
                .putExtra("fromHoldList", true))
        );

        //extend hold time
        holder.mBtn_extendTime.setOnClickListener(view -> showExtendAlert(myModel));

        //set hold duration
        //customHandler.postDelayed(getUpdateTimerThread(holder, startTime), 0);
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    private void timeLaps(AdapterViewHolder holder, HoldFlatModel myModel) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date startDate = new Date(), endDate = new Date();
        try {
            //startDate = simpleDateFormat.parse(getDateTime());
            if (myModel.getHold_datetime() != null)endDate = simpleDateFormat.parse(myModel.getHold_datetime());
        } catch (ParseException e) { e.printStackTrace(); }

        //mTv_timeLaps.setText(String.format("%d Hours %d Minutes ", Math.round(Math.floor(minutes / 60)), minutes % 60));
        int minutes = Helper.minutesDiff(endDate,startDate);
        if (minutes>=60) {
            //show h : m : s
            holder.tv_badge.setText(String.format(Locale.getDefault(), "%d Hrs %d Min %d Sec", Math.round(Math.floor(minutes / 60)), minutes % 60, minutes * 60));
        }
        else {
            //show m: s
            holder.tv_badge.setText(String.format(Locale.getDefault(), "%d Min %d Sec",  minutes % 60, minutes * 60));
        }

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
        @BindView(R.id.iv_itemHoldFlat_call) AppCompatImageView iv_call;
        @BindView(R.id.tv_itemHoldFlat_mobileNumber) AppCompatTextView tv_mobile_number;
        @BindView(R.id.tv_itemHoldFlat_tokenNumber) AppCompatTextView tv_token;
        @BindView(R.id.tv_itemHoldFlat_holdDuration) AppCompatTextView tv_badge;
        @BindView(R.id.ll_itemHoldFlat_allotNRelease) LinearLayoutCompat ll_allotNRelease;
        @BindView(R.id.mBtn_itemHoldFlat_release) MaterialButton btn_release;
        @BindView(R.id.tv_holdFlatDetails_Date)AppCompatTextView tv_dateTime;
        @BindView(R.id.mBtn_itemHoldFlat_allot)MaterialButton btn_allot;
        @BindView(R.id.pb_itemHoldFlats) ProgressBar pb;

        @BindView(R.id.ll_itemHoldFlat_addedBySalesPerson) LinearLayoutCompat ll_addedBySalesPerson;
        @BindView(R.id.mTv_itemHoldFlat_flatHoldBy) MaterialTextView mTv_flatHoldBy;
        @BindView(R.id.ll_itemHoldFlat_holdSince) LinearLayoutCompat ll_holdSince;
        @BindView(R.id.mTv_itemHoldFlat_holdSince) MaterialTextView mTv_holdSince;
        @BindView(R.id.ll_itemHoldFlat_remainingTime) LinearLayoutCompat ll_remainingTime;
        @BindView(R.id.mTv_itemHoldFlat_timeToAutoRelease) MaterialTextView mTv_timeToAutoRelease;
        @BindView(R.id.mBtn_itemHoldFlat_extendTime) MaterialButton mBtn_extendTime;


        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }




    private void startCountDownTimer(final AdapterViewHolder holder, HoldFlatModel myModel)
    {
        long timeInMilliseconds = TimeUnit.MICROSECONDS.toMillis(myModel.getExpireCountUp());

        CountDownTimer countDownTimer = new CountDownTimer(timeInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                holder.mTv_timeToAutoRelease.setText(hmsTimeFormatter(millisUntilFinished));
            }

            @Override
            public void onFinish() {

                holder.mTv_timeToAutoRelease.setText("00:00");
                //refresh api call
                context.refreshApiCall();
                new Handler().postDelayed(() -> {
                    //holder.mTv_timeToAutoRelease.setText(R.string.msg_auto_release_time_passed_away);
                    holder.ll_allotNRelease.setVisibility(View.GONE);
                }, 1000);
            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to convert millisecond to time format
     *
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds)
    {

        if (TimeUnit.MILLISECONDS.toHours(milliSeconds)>0) {

            return String.format(Locale.getDefault(), "%02d h :%02d m :%02d s",
                    TimeUnit.MILLISECONDS.toHours(milliSeconds),
                    TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        }
        else {
             return String.format(Locale.getDefault(), "%02d m :%02d s",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        }
    }





    /*private void showConfirmReleaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reason for release ?")
                .setItems(R.array.single_choice_item_release, (dialog, item) -> {
                    // The 'which' argument contains the index position
                    // of the selected item
                    //new Helper().showCustomToast(context,singleChoiceListItems[item]);
                    dialog.dismiss();
                });
        builder.show();
    }*/

    /*private void showConfirmReleaseDialog(int position, HoldFlatModel myModel, AdapterViewHolder holder)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.que_release_flat);
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            //call add sales api
            if (isNetworkAvailable(context))
            {
                showProgressBar(holder);
                call_markAsReleased(position,myModel, holder);
            }else NetworkError(context);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        *//*builder.setSingleChoiceItems(singleChoiceListItems,-1,(dialogInterface, i) -> {
            dialogInterface.dismiss();
        });*//*
        builder.show();
    }*/


    private void showReleaseHoldAlert(int position, HoldFlatModel myModel, AdapterViewHolder holder)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(context.getString(R.string.release_flat_question));
        tv_desc.setText(context.getString(R.string.que_release_flat, myModel.getFull_name()));
        btn_negativeButton.setText(context.getString(R.string.no));
        btn_positiveButton.setText(context.getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                showProgressBar(holder);
                call_markAsReleased(position,myModel, holder);

            } else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out), holder);
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection), holder);
                else showErrorLog(e.toString(), holder);
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


    private void showExtendAlert(HoldFlatModel myModel)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;

        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(context.getString(R.string.extend_hold_time_question));
        tv_desc.setText(context.getString(R.string.que_extend_hold_flat, myModel.getFull_name()));
        btn_negativeButton.setText(context.getString(R.string.no));
        btn_positiveButton.setText(context.getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            alertDialog.dismiss();

            new Handler().postDelayed(() -> showExtendTimePopup(myModel), 300);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= Objects.requireNonNull(context).getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }



    private void showExtendTimePopup(HoldFlatModel myModel)
    {
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        final Dialog builder_accept=new BottomSheetDialog(context);
        builder_accept.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder_accept.setContentView(R.layout.layout_extend_hold_popup);
        builder_accept.setCancelable(false);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        AppCompatImageView iv_close = builder_accept.findViewById(R.id.iv_extendHold_close);
        TextInputEditText edt_time = builder_accept.findViewById(R.id.edt_extendHold_time);
        MaterialButton mBtn_ok= builder_accept.findViewById(R.id.mBtn_extendHold_ok);

        Objects.requireNonNull(edt_time).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (Objects.requireNonNull(edt_time.getText()).toString().trim().isEmpty()) {
                    //set disabled
                    Objects.requireNonNull(mBtn_ok).setBackgroundColor(context.getResources().getColor(R.color.main_light_grey));
                    mBtn_ok.setTextColor(context.getResources().getColor(R.color.main_white));
                }
                else if (Integer.parseInt(Objects.requireNonNull(edt_time.getText()).toString())<=0) {
                    //set disabled
                    Objects.requireNonNull(mBtn_ok).setBackgroundColor(context.getResources().getColor(R.color.main_light_grey));
                    mBtn_ok.setTextColor(context.getResources().getColor(R.color.main_white));
                }
                else {
                    //set enabled
                    Objects.requireNonNull(mBtn_ok).setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                    mBtn_ok.setTextColor(context.getResources().getColor(R.color.main_white));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //share on WhatsApp
        Objects.requireNonNull(mBtn_ok).setOnClickListener(view -> {

            //check for time entered or not
            if (Objects.requireNonNull(Objects.requireNonNull(edt_time).getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter hold time!");

            else if (Integer.parseInt(Objects.requireNonNull(edt_time.getText()).toString())<=0) new Helper().showCustomToast(context, "Hold time minutes must be greater than 0!");

            else {

                //hide keyboard
                Helper.hideSoftKeyboard(context, edt_time);

                //dismiss dialog
                builder_accept.dismiss();

                //call method in the activity
                context.callToExtendTime(myModel.getFlat_hold_release_id(), Objects.requireNonNull(edt_time.getText()).toString());
            }

        });


        //close popup
        Objects.requireNonNull(iv_close).setOnClickListener(view -> {

            //hide keyboard
            Helper.hideSoftKeyboard(context, edt_time);

            builder_accept.dismiss();
        });

        builder_accept.show();
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

            //refresh api call
            context.refreshApiCall();

            //notifyDataSetChanged();

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

