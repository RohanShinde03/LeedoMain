package com.tribeappsoft.leedo.salesPerson.bookings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.adapter.HoldFlatListAdapter;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.HoldFlatModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class HoldFlatListActivity extends AppCompatActivity {

    @BindView(R.id.sr_holdFlat) SwipeRefreshLayout swipeRefresh;
    //@BindView(R.id.nsv_holdFlat) NestedScrollView nsv;
    @BindView(R.id.rv_holdFlat_list) RecyclerView recyclerView;
    //@BindView(R.id.ll_holdFlat_addHoldFlat) LinearLayoutCompat ll_addHoldFlat;
    @BindView(R.id.ll_holdFlat_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.exFab_holdFlat_add) ExtendedFloatingActionButton exFab_add;
    // @BindView(R.id.ll_name_block_button) LinearLayoutCompat ll_name_block_button;

    private HoldFlatListAdapter recyclerAdapter;
    private String TAG = "HoldFlatListActivity", api_token ="", event_title ="";
    private Activity context;
    private ArrayList<HoldFlatModel> itemArrayList;
    private int user_id =0, event_id = 0, lastPosition = -1;
    private long startTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_flat_list);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        //Helper.StatusBarTrans(context);
        //anim = new Animations();
        context= HoldFlatListActivity.this;

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.hold_flats));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //init
        itemArrayList = new ArrayList<>();

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 0);
            event_title = getIntent().getStringExtra("event_title");
        }



        //Set RecyclerView
        setupRecycleView();

        //setup swipeRefresh
        setSwipeRefresh();


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                //hideViews();
                exFab_add.shrink();
            }

            @Override
            public void onShow() {
                //showViews();
                exFab_add.extend();
            }
        });
//        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//
//            if (!nsv.canScrollVertically(1))
//            {
//                // bottom of scroll view
//                exFab_add.shrink();
//            }
//            if (!nsv.canScrollVertically(-1)) {
//                // top of scroll view
//                exFab_add.extend();
//            }
//
//        });


        //check validations
        exFab_add.setOnClickListener(view ->
                startActivity(
                        new Intent(context, AddHoldFlatActivity.class)
                                .putExtra("event_id", event_id)
                                .putExtra("event_title", event_title)
                ));

    }



    @Override
    public void onResume() {
        super.onResume();

        // call get lead data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //showProgressBar("Please wait...");
            swipeRefresh.setRefreshing(true);
            new Thread(this::getHoldFlatList).start();
        }
        else Helper.NetworkError(context);


    }

   /*private void hideViews()
    {

        ll_inventorySelection.animate().translationY(-ll_inventorySelection.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_inventorySelection.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE );
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }*/



    //SetUpRecycler
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new HoldFlatListAdapter(this, itemArrayList, api_token);
        recyclerView.setAdapter(recyclerAdapter);
    }


    private void setSwipeRefresh() {

        //getOffline();
        if (Helper.isNetworkAvailable(context))
        {
            swipeRefresh.setOnRefreshListener(() ->
            {
                swipeRefresh.setRefreshing(true);
                //showProgressBar("Getting batchMates list...");
                getHoldFlatList();
            });

            swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

        }else Helper.NetworkError(context);
    }


    private void getHoldFlatList()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getFlatOnHoldBySalesPerson(api_token,event_id,user_id);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonObjectResponse -> {
            throw new IllegalStateException("doOnNextException");
        });
        responseObservable.doOnError(throwable -> {
            throw new UnsupportedOperationException("onError exception");
        });
        responseObservable.subscribeOn(Schedulers.newThread())
                .asObservable()
                .subscribe(new Subscriber<Response<JsonObject>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        delayRefresh();
                        //setHoldFlats();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

                        try {
                            Log.e(TAG, "onError: " + e.toString());
                            if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                            else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                            else showErrorLog(e.toString());
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {

                                            JsonArray data = JsonObjectResponse.body().get("data").getAsJsonArray();
                                            if (data != null && !data.isJsonNull())
                                            {
                                                itemArrayList.clear();
                                                for (int i = 0; i < data.size(); i++) {
                                                    setJson(data.get(i).getAsJsonObject());
                                                }
                                            }

                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
                                case 404:
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLog(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLog(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }

                    }
                });

    }



    private void setJson(JsonObject jsonObject)
    {
        HoldFlatModel model = new HoldFlatModel();
        if (jsonObject.has("flat_hold_release_id")) model.setFlat_hold_release_id(!jsonObject.get("flat_hold_release_id").isJsonNull() ? jsonObject.get("flat_hold_release_id").getAsInt() : 0 );
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("lead_uid")) model.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        if (jsonObject.has("token_id")) model.setToken_id(!jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0 );
        if (jsonObject.has("token_no")) model.setToken_number(!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() : "" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "91" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("block_name")) model.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "" );
        if (jsonObject.has("unit_id")) model.setUnit_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setUnit_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "" );
        if (jsonObject.has("unit_category")) model.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("hold_datetime")) model.setHold_datetime(!jsonObject.get("hold_datetime").isJsonNull() ? jsonObject.get("hold_datetime").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("countUp")) model.setCountUp(!jsonObject.get("countUp").isJsonNull() ? jsonObject.get("countUp").getAsLong() : 0 );
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("floor_id")) model.setFloor_id(!jsonObject.get("floor_id").isJsonNull() ? jsonObject.get("floor_id").getAsInt() : 0 );
        if (jsonObject.has("block_id")) model.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );
        if (jsonObject.has("unit_type_id")) model.setUnit_type_id(!jsonObject.get("unit_type_id").isJsonNull() ? jsonObject.get("unit_type_id").getAsInt() : 0 );

        CUIDModel cuidModel = new CUIDModel();
        if (jsonObject.has("lead_id"))cuidModel.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0);
        if (jsonObject.has("lead_uid"))cuidModel.setCu_id(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "");
        if (jsonObject.has("country_code"))cuidModel.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number"))cuidModel.setCustomer_mobile(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("email"))cuidModel.setCustomer_email(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("prefix"))cuidModel.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "");
        if (jsonObject.has("first_name"))cuidModel.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "");
        if (jsonObject.has("middle_name"))cuidModel.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "");
        if (jsonObject.has("last_name"))cuidModel.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "");
        if (jsonObject.has("full_name"))cuidModel.setCustomer_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("is_kyc_uploaded"))cuidModel.setIs_kyc_uploaded(!jsonObject.get("is_kyc_uploaded").isJsonNull() ? jsonObject.get("is_kyc_uploaded").getAsInt() : 0);
        if (jsonObject.has("is_reminder"))cuidModel.setIs_reminder_set(!jsonObject.get("is_reminder").isJsonNull() ? jsonObject.get("is_reminder").getAsInt() : 0);
        model.setCuidModel(cuidModel);


        itemArrayList.add(model);
    }


    /*private void setHoldFlats()
    {

        if (context!=null)
        {
            runOnUiThread(() -> {

                if (itemArrayList != null && itemArrayList.size() > 0) {
                    //having data

                    ll_noData.setVisibility(View.GONE);
                    ll_addHoldFlat.removeAllViews();
                    for (int i = 0; i < itemArrayList.size(); i++) {
                        View rowView_sub = getHoldFlatsView(i);
                        ll_addHoldFlat.addView(rowView_sub);
                    }
                    ll_addHoldFlat.setVisibility(View.VISIBLE);
                } else {
                    //empty feed
                    ll_noData.setVisibility(View.VISIBLE);
                    nsv.setVisibility(View.GONE);
                    ll_addHoldFlat.setVisibility(View.GONE);
                }

                swipeRefresh.setRefreshing(false);
            });
        }
    }*/


    private View getHoldFlatsView(int position)
    {
        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_hold_flat_list, null);

        MaterialCardView cv = rowView.findViewById(R.id.cv_holdFlatList);
        setAnimation(cv, position);

        AppCompatTextView tv_holdDuration = rowView.findViewById(R.id.tv_itemHoldFlat_holdDuration);
        AppCompatTextView tv_tokenNumber = rowView.findViewById(R.id.tv_itemHoldFlat_tokenNumber);
        AppCompatTextView tv_customerName = rowView.findViewById(R.id.tv_itemHoldFlat_customerName);
        AppCompatTextView tv_mobileNumber = rowView.findViewById(R.id.tv_itemHoldFlat_mobileNumber);
        AppCompatTextView tv_unitType = rowView.findViewById(R.id.tv_itemHoldFlat_unitType);
        MaterialButton mBtn_release = rowView.findViewById(R.id.mBtn_itemHoldFlat_release);


        final HoldFlatModel myModel = itemArrayList.get(position);
        tv_unitType.setText(String.format("%s-%s | %s | %s ", myModel.getBlock_name(), myModel.getUnit_name(), myModel.getUnit_category(),myModel.getProject_name()));
        tv_customerName.setText(myModel.getFull_name());
        tv_tokenNumber.setText(myModel.getToken_number());
        tv_mobileNumber.setText(myModel.getMobile_number());
        tv_mobileNumber.setOnClickListener(view -> new Helper().openPhoneDialer(context,  myModel.getMobile_number()));
        //Log.e(TAG, "onBindViewHolder: countUp millis_1 "+ TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp()));
        startTime = TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp());

        Runnable updater = null;
        Handler customHandler = new Handler();
        Runnable finalUpdater = updater;
        updater = () -> {

            startTime =  TimeUnit.MICROSECONDS.toMillis(myModel.getCountUp()) + 1000;
            //String curTime = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", hrs, minutes, seconds);
            //holder.tv_badge.setText(curTime);
            //holder.tv_badge.setText(new SimpleDateFormat("HH : mm : ss ", Locale.US).format(new Date()));

            String hms = String.format(Locale.getDefault(), "%02d h : %02d m : %02d s", TimeUnit.MILLISECONDS.toHours(startTime),
                    TimeUnit.MILLISECONDS.toMinutes(startTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startTime)),
                    TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

            context.runOnUiThread(() -> tv_holdDuration.setText(hms));
            customHandler.postDelayed(finalUpdater,1000);
        };
        customHandler.postDelayed(updater, 0);

        //customHandler.postDelayed(updateTimerThread, 0);
        mBtn_release.setOnClickListener(view -> showConfirmReleaseDialog(position,myModel));

        return rowView;
    }


    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            new Animations().slideInBottom(v);
            lastPosition = p;
        }
    }


   private void delayRefresh()    {

        //reply
        if (context!=null)
        {
            context.runOnUiThread(() -> {


                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);

                LinearLayoutManager manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new HoldFlatListAdapter(this, itemArrayList,api_token);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                Log.e(TAG,"count "+ itemArrayList.size());
                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                } else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //setClickable(recyclerView, false);
                }
            });
        }
    }



    private void showConfirmReleaseDialog(int position, HoldFlatModel myModel)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to release ?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            //call add sales api
            if (Helper.isNetworkAvailable(context))
            {
                call_markAsReleased(position,myModel);
            }else Helper.NetworkError(context);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        /*builder.setSingleChoiceItems(singleChoiceListItems,-1,(dialogInterface, i) -> {
            dialogInterface.dismiss();
        });*/
        builder.show();
    }


    private void call_markAsReleased(int position, HoldFlatModel myModel)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("flat_hold_release_id", myModel.getFlat_hold_release_id());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addReleasedFlat(jsonObject).enqueue(new Callback<JsonObject>()
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

                                    showSuccessAlert(position);
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
                    if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                    else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }



    @SuppressLint("InflateParams")
    private void showSuccessAlert(int position)
    {
        context.runOnUiThread(() -> {

            //  onErrorSnack(context, "Flat released successfully!");
            itemArrayList.remove(position);
            new Helper().showSuccessCustomToast(context, "Flat released successfully!!");

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //showProgressBar("Please wait...");
                swipeRefresh.setRefreshing(true);
                new Thread(this::getHoldFlatList).start();
            }
            else Helper.NetworkError(context);

        });

    }


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            swipeRefresh.setRefreshing(false);
            Helper.onErrorSnack(context, message);
        });

    }


    // Overflow Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}


 /*  private void setadapterforprojectname()
    {

        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, stringList);
        acTV_select_project.setAdapter(adapter);
        acTV_select_project.setThreshold(0);

        //tv_selectCustomer.setSelection(0);

        //autoComplete_firmName.setValidator(new Validator());
        //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


        acTV_select_project.setOnItemClickListener((parent, view, position, id) ->
        {
            //int pos = position-1;
            String selectedProject = stringList.get(position);
            //selectedCompanyName= itemArrayList.get(pos).getCompany_name();

            Log.e("SelectedProject ",  "\t"+ selectedProject);
            Log.d(TAG, "setadapterforprojectname: "+ acTV_select_project);
            editor.putString("ProjectName",selectedProject);

        });

       *//* acTv_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);

            for (OtherEmployeeListModel pojo : otherEmployeeListModelArrayList)
            {
                if (pojo.getName().equals(itemName))
                {
                    //int idss = pojo.getCompany_id(); // This is the correct ID
                    selectedOtherEmpId = pojo.getFeed_type_id(); // This is the correct ID
                    selectedOtherEmp = pojo.getName();

                    //selectedCustomerModel = pojo;

                    //fixedEnquiryID+=2;
                    Log.e(TAG, "other Emp name & id " + selectedOtherEmp +"\t"+ selectedOtherEmpId);

                    break; // No need to keep looping once you found it.
                }
            }
        });*//*

        acTV_select_project.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("SITE vISIT","false");
                //selectedOtherEmpId=0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void setadapterforflattype()
    {

        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_block)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, stringList);
        acTv_selectBlock.setAdapter(adapter);
        acTv_selectBlock.setThreshold(0);


        //autoComplete_firmName.setValidator(new Validator());
        //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


        acTv_selectBlock.setOnItemClickListener((parent, view, position, id) ->
        {
            //int pos = position-1;
            String selectedBlock = stringList.get(position);
            editor.putString("BlockName",selectedBlock);
            //selectedCompanyName= itemArrayList.get(pos).getCompany_name();
            Log.e("Selected ",  "\t"+ selectedBlock);

            // ll_layout_flat.setVisibility(View.VISIBLE);



        });

       *//* acTv_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);

            for (OtherEmployeeListModel pojo : otherEmployeeListModelArrayList)
            {
                if (pojo.getName().equals(itemName))
                {
                    //int idss = pojo.getCompany_id(); // This is the correct ID
                    selectedOtherEmpId = pojo.getFeed_type_id(); // This is the correct ID
                    selectedOtherEmp = pojo.getName();

                    //selectedCustomerModel = pojo;

                    //fixedEnquiryID+=2;
                    Log.e(TAG, "other Emp name & id " + selectedOtherEmp +"\t"+ selectedOtherEmpId);

                    break; // No need to keep looping once you found it.
                }
            }
        });*//*

        acTv_selectBlock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("SITE vISIT","false");
                //selectedOtherEmpId=0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //checkVisibility();

    }*/

  /*  private void checkVisibility()
    {

        selectedProject = sharedPreferences.getString("ProjectName", null);
        selectedBlockName = sharedPreferences.getString("BlockName", null);
        Log.e(TAG, "Project "+selectedProject);
        Log.e(TAG, "Block "+selectedBlockName);

        if (!Objects.requireNonNull(selectedProject).trim().isEmpty()) {
            if (!Objects.requireNonNull(selectedBlockName).trim().isEmpty()) {
                ll_layout_flat.setVisibility(View.VISIBLE);
            } else {
                ll_layout_flat.setVisibility(View.GONE);
            }
        }else{
            ll_layout_flat.setVisibility(View.GONE);
        }
    }*/
