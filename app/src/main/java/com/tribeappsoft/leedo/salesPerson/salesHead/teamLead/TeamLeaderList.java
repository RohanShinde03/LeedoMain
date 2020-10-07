package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.SalesExecutivesActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.adapter.TeamLeadersAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamLeaderModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class TeamLeaderList extends AppCompatActivity {

    @BindView(R.id.sr_teamLeadActivity) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_teamLeadActivity) RecyclerView recyclerView;
    @BindView(R.id.ll_teamLeadActivity_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.exFab_TeamLeadList_addTeamLead) ExtendedFloatingActionButton exFab_addTeamLead;

    private Activity context;
    private TeamLeadersAdapter recyclerAdapter;
    private ArrayList<TeamLeaderModel> teamLeadArrayList;
    private String TAG = "TeamLeadList",api_token="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_leader_list);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context=TeamLeaderList.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_team_list));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        Log.e(TAG, "onCreate: "+api_token);
        editor.apply();

        teamLeadArrayList=new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                //hideViews();
                exFab_addTeamLead.shrink();
            }

            @Override
            public void onShow() {
                //showViews();
                exFab_addTeamLead.extend();
            }
        });


        exFab_addTeamLead.setOnClickListener(view -> showAlertDialog());

    }

   /* private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new TeamLeadersAdapter(context, teamLeadArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }
*/
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
        recyclerAdapter = new TeamLeadersAdapter(context, teamLeadArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getSalesTeamLeadList();
            }
            else {
                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                ll_noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    //On Resume
    @Override
    public void onResume() {
        super.onResume();
        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getSalesTeamLeadList();
        }
        else {
            Helper.NetworkError(context);
        }
    }

    private void call_getSalesTeamLeadList()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getTeamLeadList(api_token);
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
                        delayRefresh();
                        // swipeRefresh.setRefreshing(false);
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
                                if (!JsonObjectResponse.body().isJsonNull() && JsonObjectResponse.body().isJsonObject())
                                {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess == 1) {

                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                teamLeadArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }

                                            }

                                        }
                                    }else showErrorLog("Invalid response from server!");

                                }else showErrorLog("Server response is empty!");
                            } else {
                                // error from server
                                showErrorLog("Unknown error occurred from server! Try again.");
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

    }//call_getHospitalModelList

    private void setJson(JsonObject jsonObject)
    {
        TeamLeaderModel model=new TeamLeaderModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("sales_lead_id")) model.setSales_lead_id(!jsonObject.get("sales_lead_id").isJsonNull() ? jsonObject.get("sales_lead_id").getAsInt() : 0 );
        if (jsonObject.has("teamMembersCount")) model.setTeamMembersCount(!jsonObject.get("teamMembersCount").isJsonNull() ? jsonObject.get("teamMembersCount").getAsString() : "0" );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "" );
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "" );
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setPhotopath(!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : "");

        teamLeadArrayList.add(model);

       // Log.e(TAG, "setJson: "+itemArrayList );
    }

    private void delayRefresh()
    {
        if (context!= null)
        {
            runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new TeamLeadersAdapter(context, teamLeadArrayList);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

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

    }//delayRefresh


    private void showAlertDialog()
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

        tv_msg.setText(getString(R.string.que_new_team_lead));
        tv_desc.setText(getString(R.string.msg_add_existing_sales_executive_as_team_lead));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //goto add existing sales person
            startActivity(new Intent(context, SalesExecutivesActivity.class));
        });

        btn_negativeButton.setOnClickListener(view -> {

            //goto add new team lead
            alertDialog.dismiss();
            startActivity(new Intent(context, AddTeamLeadActivity.class));
        });

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


    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                // hideProgressBar();
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }
}
