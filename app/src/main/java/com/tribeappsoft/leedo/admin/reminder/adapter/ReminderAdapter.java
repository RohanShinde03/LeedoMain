package com.tribeappsoft.leedo.admin.reminder.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.admin.reminder.AllReminderActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.ReminderModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;


public class ReminderAdapter extends  RecyclerView.Adapter<ReminderAdapter.AdapterViewHolder> {
    private ArrayList<ReminderModel> reminderArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private String api_token = "";
    private Activity context;
    private String TAG = "ReminderRecyclerAdapter";

    public ReminderAdapter(Activity context, ArrayList<ReminderModel> reminderArrayList) {
        this.context = context;
        this.reminderArrayList = reminderArrayList;
        this.anim = new Animations();
    }


    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_reminder, parent, false);

        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position) {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        //set Animation
        setAnimation(holder.cv_itemReminderList, position);

        final ReminderModel myModel = reminderArrayList.get(position);
        holder.tv_reminder.setText(myModel.getReminder_name() != null && !myModel.getReminder_name().trim().isEmpty() ? myModel.getReminder_name() : "");
        holder.tv_reminder_date.setText(myModel.getRemind_at_date() != null && !myModel.getRemind_at_date().trim().isEmpty() ? myModel.getRemind_at_date() : Helper.getDateTime());


       /* //if reminder is done  //set strikeThrough
        if (myModel.getMark_as_done()==1) holder.tv_reminder.setPaintFlags(holder.tv_reminder.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        //set visibility
        holder.iv_edit.setVisibility( myModel.getMark_as_done() ==1 ? View.GONE : View.VISIBLE);
        holder.mBtn_mark_as_done.setVisibility( myModel.getMark_as_done() ==1 ? View.GONE : View.VISIBLE);
        holder.iv_delete.setVisibility( myModel.getMark_as_done() ==1 ? View.VISIBLE : View.GONE);*/

        //For Update

            holder.iv_edit.setOnClickListener(view -> {
                Intent intent = new Intent(context, AddReminderActivity.class);
                intent.putExtra("reminder_name", myModel.getReminder_name());
                intent.putExtra("remind_at", myModel.getReminder_date());
                intent.putExtra("reminder_id", myModel.getReminder_id());
                intent.putExtra("remind_at_date_format", myModel.getRemind_at_date_format());
                intent.putExtra("remind_at_time_format", myModel.getRemind_at_date());
                intent.putExtra("remind_at_time_format1", myModel.getRemind_at_time_format1());
                intent.putExtra("fromOther", 2);


            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = holder.cv_itemReminderList;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());
        });


        holder.iv_delete.setOnClickListener(v -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
                holder.cv_itemReminderList.startAnimation(animZoomIn);
                Handler handle = new Handler();
                handle.postDelayed(() -> call_deleteReminder(myModel.getReminder_id(), position), 500);
            } else Helper.NetworkError(context);


        });

        //For Mark As Done
        holder.mBtn_mark_as_done.setOnClickListener(view -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {

                Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
                holder.cv_itemReminderList.startAnimation(animZoomIn);
                Handler handle = new Handler();
                handle.postDelayed(() -> {

                    call_markAsDone(myModel.getReminder_id(), position);
                    //TODO remove
//                    fragmentSalesPersonReminders.call_getAllReminder();
//                    fragmentSalesPersonReminders.delayRefresh();

                }, 500);
            } else Helper.NetworkError(context);
            //Log.d("ReminderAdapter", "onBindViewHolder: " + position);
        });

    }


    @Override
    public int getItemCount() {
        return (null != reminderArrayList ? reminderArrayList.size() : 0);
    }


    private void call_markAsDone(int reminder_id, int position) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("is_done", 1);
        jsonObject.addProperty("reminder_id",reminder_id );

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().markAsDoneReminder(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.body() != null) {
                    String success = response.body().get("success").toString();

                    switch (success) {
                        case "1":
                            onSuccessMarkAsDoneReminder(position);
                            new Helper().showCustomToast(context,"Mark as done Successfully");
                            break;
                        case "2":
                            showErrorLog("Error occurred during reminder!");
                            break;
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });


    }



    private void onSuccessMarkAsDoneReminder(int position)
    {
        context.runOnUiThread(() -> {

            Log.e(TAG, " Mark as Done Success:");
            reminderArrayList.remove(position);
            notifyDataSetChanged();
            ((AllReminderActivity) context).resetCallGetReminder();
            //salesPersonReminderActivity.resetCallGetReminder();
            //fragmentSalesPersonReminders.delayRefresh();
        });
    }


    private void call_deleteReminder(int reminder_id, int position)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("reminder_id", reminder_id );

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().deleteReminder(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.body() != null) {
                    String success = response.body().get("success").toString();
                    //ll_pb.setVisibility(View.GONE);
                    switch (success) {
                        case "1":
                            onSuccessDeleteReminder(position);
                            break;
                        case "2":
                            showErrorLog("Error occurred during deleting reminder!");
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });


    }


    private void onSuccessDeleteReminder(int position)
    {
        context.runOnUiThread(() -> {

            Log.e(TAG, " Mark as Done Success:");
            reminderArrayList.remove(position);
            notifyDataSetChanged();
            //fragmentSalesPersonReminders.delayRefresh();
            ((AllReminderActivity) context).resetCallGetReminder();
        });
    }



    /*Show ErrorLog*/
    private void showErrorLog(final String message)
    {
        context.runOnUiThread(() -> Helper.onErrorSnack(context,message));

    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

    static class AdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cv_itemReminder) MaterialCardView cv_itemReminderList;
        @BindView(R.id.tv_itemReminder_reminderText) AppCompatTextView tv_reminder;
        @BindView(R.id.tv_itemReminder_reminderDate) AppCompatTextView tv_reminder_date;
        @BindView(R.id.iv_itemReminder_editReminder) AppCompatImageView iv_edit;
        @BindView(R.id.iv_itemReminder_delete) AppCompatImageView iv_delete;
        @BindView(R.id.mBtn_mark_as_done) MaterialButton mBtn_mark_as_done;
        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }


    public void removeItemSwipe(int position) {
        reminderArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void restoreItem(ReminderModel item, int position) {
        reminderArrayList.add(position, item);
        notifyItemInserted(position);
    }

    public ArrayList<ReminderModel> getData() {
        return reminderArrayList;
    }


}







/*Mark As Done Api*/
   /* public void call_markAsDone(int reminder_id) {


        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("is_done", 1);
        jsonObject.addProperty("reminder_id",reminder_id );


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().markAsDoneReminder(jsonObject,reminder_id);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext((Response<JsonObject> jsonObjectResponse) -> {
            throw new IllegalStateException("doOnNextException");
        });
        responseObservable.doOnError((Throwable throwable) -> {
            throw new UnsupportedOperationException("onError exception");
        });
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.subscribe(new Subscriber<Response<JsonObject>>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted:");
                new Helper().showCustomToast(context,"Mark as done Successfully");

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onError(final Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                try {
                    //showAPIErrorLog(e.toString());
                }catch (Throwable ex)
                {
                    ex.printStackTrace();
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onNext(Response<JsonObject> JsonObjectResponse)
            {
                if (JsonObjectResponse.isSuccessful())
                {
                    if (JsonObjectResponse.body() != null)
                    {
                        if (!JsonObjectResponse.body().isJsonNull())
                        {
                            //  Log.e(TAG, "onNext: " + JsonObjectResponse.body().toString());

                            if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0 ;
                            if (isSuccess == 1)
                            {
                                Log.e(TAG, "onNext: Success As Completed" );
                            }
                            //showErrorLog(""+JsonObjectResponse.body().get("message").getAsString());

                        }
                    }
                }
                else {
                    // error case
                    switch (JsonObjectResponse.code())
                    {
                        case 404:
                            showErrorLog("Something went wrong try again");
                            break;
                        case 500:
                            showErrorLog("Server error occur");
                            break;
                        default:
                            showErrorLog("Unknown error please try again");
                            break;
                    }
                }

            }
        });

    }

*/
/*Delete Api*/
/*
public void call_deleteReminder(int reminder_id) {


    final JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("api_token", api_token);
    jsonObject.addProperty("reminder_id",reminder_id );


    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    ApiClient client = ApiClient.getInstance();
    Observable<Response<JsonObject>> responseObservable = client.getApiService().deleteReminder(jsonObject,reminder_id);
    responseObservable.subscribeOn(Schedulers.newThread());
    responseObservable.asObservable();
    responseObservable.doOnNext((Response<JsonObject> jsonObjectResponse) -> {
        throw new IllegalStateException("doOnNextException");
    });
    responseObservable.doOnError((Throwable throwable) -> {
        throw new UnsupportedOperationException("onError exception");
    });
    responseObservable.subscribeOn(Schedulers.newThread());
    responseObservable.asObservable();
    responseObservable.subscribe(new Subscriber<Response<JsonObject>>() {
        @SuppressLint("LongLogTag")
        @Override
        public void onCompleted() {
            Log.d(TAG, "onCompleted:");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onError(final Throwable e) {
            Log.e(TAG, "onError: " + e.toString());
            try {
                //showAPIErrorLog(e.toString());
            }catch (Throwable ex)
            {
                ex.printStackTrace();
            }
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onNext(Response<JsonObject> JsonObjectResponse)
        {
            if (JsonObjectResponse.isSuccessful())
            {
                if (JsonObjectResponse.body() != null)
                {
                    if (!JsonObjectResponse.body().isJsonNull())
                    {
                        //  Log.e(TAG, "onNext: " + JsonObjectResponse.body().toString());

                        if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0 ;
                        if (isSuccess == 1)
                        {
                            Log.e(TAG, "onNext: Success As Completed" );
                        }
                        //showErrorLog(""+JsonObjectResponse.body().get("message").getAsString());

                    }
                }
            }
            else {
                // error case
                switch (JsonObjectResponse.code())
                {
                    case 404:
                        showErrorLog("Something went wrong try again");
                        break;
                    case 500:
                        showErrorLog("Server error occur");
                        break;
                    default:
                        showErrorLog("Unknown error please try again");
                        break;
                }
            }

        }
    });

}*/
