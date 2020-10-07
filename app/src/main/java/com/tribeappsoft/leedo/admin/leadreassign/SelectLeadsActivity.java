package com.tribeappsoft.leedo.admin.leadreassign;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.admin.leadreassign.adapter.SelectLeadsRecyclerAdapter;
import com.tribeappsoft.leedo.admin.leadreassign.model.AssignLeadsModel;
import com.tribeappsoft.leedo.util.Animations;
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

public class SelectLeadsActivity extends AppCompatActivity {

    @BindView(R.id.sr_selectLeads) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_selectLeads) RecyclerView recyclerView;
    @BindView(R.id.ll_selectLeads_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_selectLeads_search) TextInputEditText edt_search;
    @BindView(R.id.iv_selectLeads_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.mBtn_selectLeads_assign) MaterialButton mBtn_assignLeads;

    @BindView(R.id.ll_selectLeads_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.pb_selectLeads)
    ContentLoadingProgressBar pb_selectLeads;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    public SelectLeadsActivity context;
    private SelectLeadsRecyclerAdapter recyclerAdapter;
    private ArrayList<AssignLeadsModel> itemArrayList;
    private String TAG = "SelectLeadsActivity",api_token="", search_text = "", from_sales_person_name = "", to_sales_person_name = "";
    private int from_sales_person_id =0, to_sales_person_id =0, user_id = 0, current_page =1, last_page =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_leads);
        ButterKnife.bind(this);
        context= SelectLeadsActivity.this;


        if (getSupportActionBar()!=null) {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_select_leads));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        if(getIntent()!=null){
            from_sales_person_id = getIntent().getIntExtra("from_sales_person_id", 0);
            to_sales_person_id = getIntent().getIntExtra("to_sales_person_id", 0);
            from_sales_person_name = getIntent().getStringExtra("from_sales_person_name");
            to_sales_person_name = getIntent().getStringExtra("to_sales_person_name");
            Log.e(TAG, "onCreate: from_sales_person_id "+ from_sales_person_id );
            Log.e(TAG, "onCreate: to_sales_person_id "+ to_sales_person_id );
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+ api_token + "\n User id"+ user_id);
        editor.apply();

        itemArrayList = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //hide pb
        hideProgressBar();
        hidePB();

        //set swipe refresh
        setSwipeRefresh();

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getAllLeadsBySalesPerson();
        }
        else {
            Helper.NetworkError(context);
        }


        mBtn_assignLeads.setOnClickListener(v -> checkValidation());

        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }

        });


        //Use This For Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE)
                {

                    Log.d("-----","end");


                    //TODO Rohan 16-09-2019
                    if (!swipeRefresh.isRefreshing())
                    {
                        //if swipe refreshing is on means user has done swipe-refreshed
                        //and already api call is running, still user scrolls to bottom then it is adding duplicate deal/entry in arraylist
                        //to avoid this, Have added below api call within this block

                        Log.e(TAG, "onScrollStateChanged: current_page "+current_page );
                        if (current_page <= last_page)  //
                        {
                            if (Helper.isNetworkAvailable(context))
                            {
                                //swipeRefresh.setRefreshing(true);
                                new Animations().slideOutBottom(mBtn_assignLeads);
                                mBtn_assignLeads.setVisibility(View.GONE);

                                showProgressBar();
                                call_getAllLeadsBySalesPerson();
                            } else Helper.NetworkError(context);

                        } else Log.e(TAG, "Last page");
                    }
                }
                /*else
                {
                    new Animations().slideOutBottom(ll_backToTop);
                    ll_backToTop.setVisibility(View.GONE);
                }*/
            }

            int currentScrollPosition = 0;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentScrollPosition += dy;

                if (dy > 0) {

                    // Scrolling up
                    Log.d(TAG, "onScrolled: upp " );
                    //new Animations().slideOutBottom(mBtn_assignLeads);
                    //mBtn_assignLeads.setVisibility(View.GONE);

                } else {
                    // Scrolling down
                    Log.d(TAG, "onScrolled: down " );
                    mBtn_assignLeads.setVisibility(View.VISIBLE);
                    new Animations().slideInBottom(mBtn_assignLeads);
                }

                if( currentScrollPosition == 0 ) {
                    // We're at the top
                    Log.d(TAG, "onScrolled: top " );
                    //hide pb
                    hideProgressBar();

                    //visible button
                    new Animations().slideOutBottom(mBtn_assignLeads);
                    mBtn_assignLeads.setVisibility(View.VISIBLE);
                }
            }

        });

    }


    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
        if (verticalDivider != null) {
            dividerItemDecoration.setDrawable(verticalDivider);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerAdapter = new SelectLeadsRecyclerAdapter(context,itemArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }


    //SetUpSwipeRefresh
    private void setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener(() -> {
            //list swipe refreshed

            if (Helper.isNetworkAvailable(context)) {

                //set swipe refreshing to true
                swipeRefresh.setRefreshing(true);
                //reset api call
                resetApiCall();

            }
            else {

                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            }
        });
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }


    private void hideViews()
    {
        ll_search.animate().translationY(-ll_search.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews()
    {

        ll_search.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }


    //On Resume
    @Override
    public void onResume() {
        super.onResume();

        perform_search();

    }


    private void call_getAllLeadsBySalesPerson()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllLeadsBySalesPerson(api_token, from_sales_person_id, current_page, search_text);
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
                .subscribe(new Subscriber<Response<JsonObject>>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");
                        //set
                        if (current_page ==2 ) delayRefresh();
                        else notifyRecyclerDataChange();
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onError(final Throwable e) {

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if (JsonObjectResponse.isSuccessful())
                        {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject())
                                            {
                                                JsonObject jsonObject = JsonObjectResponse.body().get("data").getAsJsonObject();

                                                //set page number
                                                if (jsonObject.has("current_page")) current_page = !jsonObject.get("current_page").isJsonNull() ? jsonObject.get("current_page").getAsInt() : 0;
                                                if (jsonObject.has("last_page")) last_page = !jsonObject.get("last_page").isJsonNull() ? jsonObject.get("last_page").getAsInt() : 0;
                                                //if (JsonObjectResponse.body().has("per_page")) per_page = !JsonObjectResponse.body().get("per_page").isJsonNull() ? JsonObjectResponse.body().get("per_page").getAsInt() : 0;

                                                setLeadsJson(jsonObject.getAsJsonObject());
                                            }
                                        }
                                    }else Log.e(TAG, "Outside Data: ");
                                } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                            }

                        } else {
                            // error case
                            switch (JsonObjectResponse.code()) {
                                case 404:
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLog(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }

                    }
                });

    }


    private void setLeadsJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull())
        {
            if (jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                }

                //increment current page counter
                current_page =  current_page +1;
            }
        }
    }


    private void setJson(JsonObject jsonObject)
    {
        AssignLeadsModel model=new AssignLeadsModel();
        if (jsonObject.has("lead_id")) model.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
        if (jsonObject.has("sales_person_id")) model.setSales_person_id(!jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) model.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("prefix")) model.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("lead_uid")) model.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        itemArrayList.add(model);
    }


    //delayRefresh
    private void delayRefresh()
    {
        //if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                hidePB();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new SelectLeadsRecyclerAdapter(context,itemArrayList);
                recyclerView.setAdapter(recyclerAdapter);


                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no data
                    swipeRefresh.setRefreshing(false);
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.GONE);
                } else {
                    //data available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });
        }

    }



    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        //if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();
                hidePB();

                if (recyclerAdapter!=null)
                {
                    //recyclerView adapter
                    recyclerAdapter.notifyDataSetChanged();

                    int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                    if (count == 0) {
                        //no data
                        recyclerView.setVisibility(View.GONE);
                        ll_noData.setVisibility(View.VISIBLE);
                        //exFab.setVisibility(View.GONE);
                    } else {
                        //data available
                        recyclerView.setVisibility(View.VISIBLE);
                        ll_noData.setVisibility(View.GONE);

                    }

                }

            });
        }

    }


    public void showButton()
    {
        //mBtn_submit.setVisibility(View.VISIBLE);
        mBtn_assignLeads.setEnabled(true);
        mBtn_assignLeads.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void hideButton()
    {
        // mBtn_submit.setVisibility(View.GONE);
        mBtn_assignLeads.setEnabled(false);
        mBtn_assignLeads.setBackgroundColor(getResources().getColor(R.color.main_medium_grey));
    }


    private void checkValidation() {
        if(recyclerAdapter!=null && recyclerAdapter.getLeadIdArrayList().size()>0) {
            //call api method
            showSubmitMemberAlertDialog();
        }
        else new Helper().showCustomToast(context, "Please select at least one lead!");
    }


    private void showSubmitMemberAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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

        tv_msg.setText(getString(R.string.que_re_assign_team_members));
        //  String str = TextUtils.join(", ", recyclerAdapter.getSalesUserNames());
        tv_desc.setText(getString(R.string.re_assign_team_members_confirmation, from_sales_person_name, to_sales_person_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showPB(getString(R.string.assigning_team_members));
                //api_call
                call_postAddRemoveLeads();
            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
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

    private void call_postAddRemoveLeads()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",user_id);
        jsonObject.addProperty("sales_person_id", from_sales_person_id);
        jsonObject.addProperty("assign_sales_person_id", to_sales_person_id);
        jsonObject.add("leadIDs", new Gson().toJsonTree(recyclerAdapter.getLeadIdArrayList()).getAsJsonArray());

        //jsonObject.addProperty("teamMembers", String.valueOf(recyclerAdapter.getSalesUserIdArrayList()));
        ///JsonArray myCustomArray = new Gson().toJsonTree(recyclerAdapter.getSalesUserIdArrayList()).getAsJsonArray();


        ApiClient client = ApiClient.getInstance();
        client.getApiService().reAssignLeadsToSalesPersons(jsonObject).enqueue(new Callback<JsonObject>()
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
                                onSuccessAssignTeam();
                            }else showErrorLogAssignTeamMembers("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLogAssignTeamMembers(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogAssignTeamMembers(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogAssignTeamMembers(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogAssignTeamMembers(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogAssignTeamMembers(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogAssignTeamMembers(context.getString(R.string.weak_connection));
                else showErrorLogAssignTeamMembers(e.toString());
            }
        });
    }


    private void onSuccessAssignTeam()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            hidePB();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.lead_re_assigned_successfully));

            //set result OK
            setResult(Activity.RESULT_OK, new Intent());


            //do back pressed
            new Handler().postDelayed(this::onBackPressed, 500);

        });
    }




    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                hideProgressBar();
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void showErrorLogAssignTeamMembers(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                //hide pb
                hideProgressBar();
                //hide pb
                hidePB();

                //hide swipe refresh if refreshing
                swipeRefresh.setRefreshing(false);
                //show error log
                Helper.onErrorSnack(context, message);

            });
        }
    }


    private void perform_search() {

        //or you can search by the editTextFiler

        //search ime action click
        edt_search.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (recyclerView.getAdapter() != null)
                {
                    edt_search.clearFocus();
                    Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    if (Helper.isNetworkAvailable(context))
                    {

                        //set swipe refreshing to true
                        swipeRefresh.setRefreshing(true);

                        String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                        // regex to match any number of spaces
                        filterText = filterText.trim().replaceAll("\\s+", " ");
                        Log.e(TAG, "perform_search: filterText "+filterText);
                        doFilter(filterText);

                    }
                    else Helper.NetworkError(Objects.requireNonNull(context));
                }

                return true;
            }
            return false;
        });


        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                //if (recyclerAdapter != null) {
                //  String text = Objects.requireNonNull(edt_search.getText()).toString().toLowerCase(Locale.getDefault());
                //  doFilter(text);

                if (Objects.requireNonNull(edt_search.getText()).length() < 1)
                {
                    edt_search.clearFocus();
                    Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    iv_clearSearch.setVisibility(View.GONE);

                   /* //set swipe refreshing to true
                    swipeRefresh.setRefreshing(true);

                    String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                    // regex to match any number of spaces
                    filterText = filterText.trim().replaceAll("\\s+", " ");

                    //call reset api
                    doFilter(filterText);*/

                } else {
                    //visible empty search ll
                    iv_clearSearch.setVisibility(View.VISIBLE);
                }
                //}
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                iv_clearSearch.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }
        });


        //clear searchText
        iv_clearSearch.setOnClickListener(v -> {
            edt_search.setText("");
            //call reset api
            // doFilter("");
            resetApiCall();
        });
    }


    private void doFilter(String query) {

        itemArrayList = new ArrayList<>();
        //1. clear arrayList
        itemArrayList.clear();
        //2. reset page flag to 1
        current_page = 1;
        last_page = 1;
        //3. Get search text
        search_text = query;
        //4. notify adapter
        recyclerAdapter.notifyDataSetChanged();
//        recyclerAdapter.notify();
        swipeRefresh.setRefreshing(true);
        //call the api
        call_getAllLeadsBySalesPerson();
    }

    private void resetApiCall()
    {
        if (Helper.isNetworkAvailable(context))
        {
            swipeRefresh.setRefreshing(true);

            //Clear Search --> reset all params
            //1. clear arrayList
            itemArrayList = new ArrayList<>();
            itemArrayList.clear();
            //2. reset page flag to 0
            current_page = 1;
            last_page = 1;
            //3. Set search text clear
            search_text = "";
            edt_search.setText("");
            //4. notify adapter
            recyclerAdapter.notifyDataSetChanged();
            Log.e(TAG, "resetApiCall: ");
            swipeRefresh.setRefreshing(true);
            //call the api
            call_getAllLeadsBySalesPerson();
        }
        else
        {
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);

        }
    }




    void showProgressBar() {
        pb_selectLeads.setVisibility(View.VISIBLE);
    }

    void hideProgressBar() {
        pb_selectLeads.setVisibility(View.GONE);
    }

    private void showPB(String message)
    {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hidePB()
    {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, new Intent().putExtra("result", ""));
            onBackPressed();

        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
