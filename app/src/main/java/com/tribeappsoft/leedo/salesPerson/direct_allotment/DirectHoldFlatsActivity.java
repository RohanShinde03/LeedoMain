package com.tribeappsoft.leedo.salesPerson.direct_allotment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.firebase.FireBaseMessageService;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.salesPerson.adapter.DirectHoldFlatListAdapter;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.HoldFlatModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class DirectHoldFlatsActivity extends AppCompatActivity {


    @BindView(R.id.sr_holdFlatList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.ll_holdFlatList_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.rv_holdFlat) RecyclerView recyclerView;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private DirectHoldFlatListAdapter recyclerAdapter;
    private String TAG = "HoldFlatListActivity", api_token ="";
    private Activity context;
    private ArrayList<HoldFlatModel> itemArrayList;
    private int user_id =0, event_id = 0;
    private boolean fromAddHoldFlat = false, isSalesHead = false, notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_holdflat_list);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        //Helper.StatusBarTrans(context);
        //anim = new Animations();
        context= DirectHoldFlatsActivity.this;

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
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        if (getIntent()!=null)
        {
            //showLeads = getIntent().getBooleanExtra("showLeads", false);
            notify = getIntent().getBooleanExtra("notify", false);
        }

        //init
        itemArrayList = new ArrayList<>();

        //hide pb
        hideProgressBar();

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 0);
            //event_title = getIntent().getStringExtra("event_title");
            fromAddHoldFlat = getIntent().getBooleanExtra("fromAddHoldFlat", false);
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

            }

            @Override
            public void onShow() {
                //showViews();
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

    }

    @Override
    public void onResume() {
        super.onResume();

        //register broadCast receiver
        if (broadcastReceiver!=null) registerReceiver(broadcastReceiver, new IntentFilter(FireBaseMessageService.BROADCAST_ACTION_HOLD_FLAT));

        // call get lead data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            //showProgressBar("Please wait...");
            refreshApiCall();
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
        recyclerAdapter = new DirectHoldFlatListAdapter(this, itemArrayList, api_token);
        recyclerView.setAdapter(recyclerAdapter);
    }


    private void setSwipeRefresh() {

        //getOffline();
        //refresh API call
        swipeRefresh.setOnRefreshListener(this::refreshApiCall);

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //updateUI(intent);

            Log.e(TAG, "onReceive: broadcastReceiver ");

            if (intent!=null){
                String page  = intent.getStringExtra("page");
                Log.e(TAG, "intent page "+page);

                if (Objects.requireNonNull(page).equalsIgnoreCase("unClaimedLead")) {
                    //refresh API call
                    refreshApiCall();
                }
            }
        }
    };


    public void refreshApiCall()
    {
        //getOffline();
        if (Helper.isNetworkAvailable(context))
        {
            Log.e(TAG, "refreshApiCall: ");
            swipeRefresh.setRefreshing(true);
            //showProgressBar("Getting batchMates list...");
            getHoldFlatList();

        }else Helper.NetworkError(context);
    }


    private void getHoldFlatList()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getDirectHoldFlats(api_token,event_id,user_id, isSalesHead ? 1 : 0);
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
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
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
        if (jsonObject.has("unit_hold_release_id")) model.setFlat_hold_release_id(!jsonObject.get("unit_hold_release_id").isJsonNull() ? jsonObject.get("unit_hold_release_id").getAsInt() : 0 );
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

        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("sales_person_name")) model.setSales_person_name(!jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "" );
        if (jsonObject.has("is_kyc_uploaded")) model.setIs_kyc_uploaded(!jsonObject.get("is_kyc_uploaded").isJsonNull() ? jsonObject.get("is_kyc_uploaded").getAsInt() : 0 );
        if (jsonObject.has("expire_datetime")) model.setExpire_datetime(!jsonObject.get("expire_datetime").isJsonNull() ? jsonObject.get("expire_datetime").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("expireCountUp")) model.setExpireCountUp(!jsonObject.get("expireCountUp").isJsonNull() ? jsonObject.get("expireCountUp").getAsLong() : 0 );
        if (jsonObject.has("can_allot")) model.setCan_allot(!jsonObject.get("can_allot").isJsonNull() ? jsonObject.get("can_allot").getAsInt() : 0 );


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



    private void delayRefresh()    {

        //reply
        if (context!=null)
        {
            context.runOnUiThread(() -> {


                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);

                LinearLayoutManager manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new DirectHoldFlatListAdapter(this, itemArrayList,api_token);
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

    public void callToExtendTime(int unit_hold_release_id, String extendMins)
    {
        if (Helper.isNetworkAvailable(context))
        {
            showProgressBar(getString(R.string.please_wait));
            call_extendHoldTime(unit_hold_release_id, extendMins);
        }
        else Helper.NetworkError(context);
    }


    private void call_extendHoldTime(int unit_hold_release_id, String extendMins)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",user_id);
        jsonObject.addProperty("unit_hold_release_id",unit_hold_release_id);
        jsonObject.addProperty("extendMins",extendMins);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().extendHoldTime(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body()!=null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1) {
                            if (response.body().has("data")) {
                                // String msg = response.body().get("data").getAsString();
                                onSuccessExtendTime();
                            }else showErrorLog("Invalid response from server!");
                        }
                        else {
                           showErrorLog("Failed to extend hold time!");
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void onSuccessExtendTime()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.hold_time_extend_success));

            //do refresh api
            //reset api call
            swipeRefresh.setRefreshing(true);
            getHoldFlatList();
        });
    }





    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            //hide pb
            hideProgressBar();
            swipeRefresh.setRefreshing(false);
            Helper.onErrorSnack(context, message);
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (broadcastReceiver!=null) unregisterReceiver(broadcastReceiver);
    }


    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        if (fromAddHoldFlat) {
            //if directly came from add hold flat
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            finish();
        }
        else if(notify)
        {
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }

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

