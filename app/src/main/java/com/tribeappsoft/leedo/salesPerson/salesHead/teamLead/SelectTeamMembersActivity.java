package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.adapter.TeamMemberListAdapter;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamListModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class SelectTeamMembersActivity extends AppCompatActivity {

    @BindView(R.id.sr_team_member_list) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_team_member_list) RecyclerView recyclerView;
    @BindView(R.id.ll_team_memberActivity_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_team_memberActivity_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_team_memberActivity_search) AppCompatEditText edt_search;
    @BindView(R.id.iv_team_memberActivity_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.mBtn_cpFOSList_submit) MaterialButton mBtn_submit;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    public SelectTeamMembersActivity context;
    private TeamMemberListAdapter recyclerAdapter;
    private ArrayList<TeamListModel> itemArrayList, tempArrayList;
    private ArrayList<Integer> salesUserIdArrayList;
    private String TAG = "SelectTeamMembersActivity",api_token="",sales_team_lead_name = "";
    private int sales_lead_id =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_members_list);
        ButterKnife.bind(this);

        context= SelectTeamMembersActivity.this;

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.select_team_members));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if(getIntent()!=null){
            sales_lead_id = getIntent().getIntExtra("sales_lead_id", 0);
            sales_team_lead_name = getIntent().getStringExtra("sales_team_lead_name");
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        int user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+ api_token + "\n User id"+ user_id);
        editor.apply();

        itemArrayList = new ArrayList<>();
        tempArrayList = new ArrayList<>();
        salesUserIdArrayList = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        hideProgressBar();

        //set swipe refresh
        setSwipeRefresh();

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getSalesExecutiveList();
        }
        else {
            Helper.NetworkError(context);
        }


        mBtn_submit.setOnClickListener(v -> checkValidation());

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
        recyclerAdapter = new TeamMemberListAdapter(context,itemArrayList,salesUserIdArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }
    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getSalesExecutiveList();
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

    private void call_getSalesExecutiveList()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getSalesExecutives(api_token);
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
                                if (!JsonObjectResponse.body().isJsonNull() && JsonObjectResponse.body().isJsonObject()) {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();

                                                itemArrayList = new ArrayList<>();
                                                itemArrayList.clear();
                                                tempArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                                tempArrayList.addAll(itemArrayList);

                                            } else showErrorLog("Server response is empty!");

                                        }else showErrorLog("Invalid response from server!");
                                    } else {
                                        // error from server
                                        showErrorLog("Unknown error occurred from server! Try again.");
                                    }
                                }
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

    }//call team list

    private void setJson(JsonObject jsonObject)
    {
        TeamListModel model=new TeamListModel();
        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("person_id")) model.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        itemArrayList.add(model);
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
                recyclerAdapter= new TeamMemberListAdapter(context,itemArrayList, salesUserIdArrayList);
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


    //NotifyRecyclerDataChange
    private void notifyRecyclerDataChange()
    {
        //if (context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                swipeRefresh.setRefreshing(false);
                hideProgressBar();

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

    public void checkInsertRemoveUserIds(int userID, boolean value) {
        if (value) salesUserIdArrayList.add(userID);
            //else catStringArrayList.remove(new String(subcatName));
        else salesUserIdArrayList.remove(new Integer(userID));
    }

    public ArrayList<Integer> getSalesUserIdArrayList() {
        return salesUserIdArrayList;
    }


    public void checkArrayList()
    {
        if (getSalesUserIdArrayList()!=null && getSalesUserIdArrayList().size()>0) {
            context.showButton();
        }
        else context.hideButton();
        //if (!arrayListId.isEmpty()) context.showButton();
        Log.e("TAG", "checkArrayList: "+ Arrays.toString(getSalesUserIdArrayList().toArray()));
    }


    public void showButton()
    {
        //mBtn_submit.setVisibility(View.VISIBLE);
        mBtn_submit.setEnabled(true);
        mBtn_submit.setBackgroundColor(getResources().getColor(R.color.main_black));
    }

    public void hideButton()
    {
        // mBtn_submit.setVisibility(View.GONE);
        mBtn_submit.setEnabled(false);
        mBtn_submit.setBackgroundColor(getResources().getColor(R.color.main_medium_grey));
    }

    private void checkValidation() {

        if(recyclerAdapter!=null && salesUserIdArrayList.size()>=1&&salesUserIdArrayList!=null) {

            //call api method
            Log.e(TAG, "checkValidation: "+salesUserIdArrayList.size());

            showSubmitMemberAlertDialog();
        }
        else new Helper().showCustomToast(context, "Please select at least one sales executive !");
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

        tv_msg.setText(getString(R.string.submit_member_question));
        //  String str = TextUtils.join(", ", recyclerAdapter.getSalesUserNames());
        tv_desc.setText(String.format("%s to %s ?", getString(R.string.submit_member_confirmation),sales_team_lead_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.assigning_team_members));
                //api_call
                call_postAddRemoveMembers();
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

    private void call_postAddRemoveMembers()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",sales_lead_id);
        jsonObject.addProperty("status_id", 1);
        jsonObject.add("teamMembers", new Gson().toJsonTree(salesUserIdArrayList).getAsJsonArray());

        //jsonObject.addProperty("teamMembers", String.valueOf(recyclerAdapter.getSalesUserIdArrayList()));
        ///JsonArray myCustomArray = new Gson().toJsonTree(recyclerAdapter.getSalesUserIdArrayList()).getAsJsonArray();


        ApiClient client = ApiClient.getInstance();
        client.getApiService().addRemoveTeamMembers(jsonObject).enqueue(new Callback<JsonObject>()
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

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.team_members_added_successfully));

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

                        itemArrayList = new ArrayList<>();
                        //1. clear arrayList
                        itemArrayList.clear();
                        //2. reset page flag to 1
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

                    String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                    // regex to match any number of spaces
                    filterText = filterText.trim().replaceAll("\\s+", " ");

                    //call reset api
                    doFilter(filterText);

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
            doFilter("");
        });
    }

    private void doFilter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        itemArrayList.clear();

        if (query.length() == 0)
        {
            itemArrayList.addAll(tempArrayList);
        }
        else
        {
            for (TeamListModel _obj: tempArrayList)
            {
                if (_obj.getFull_name().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getMobile_number().trim().toLowerCase(Locale.getDefault()).contains(query))
                {
                    itemArrayList.add(_obj);
                }
            }
        }

        delayRefresh();
        recyclerAdapter.notifyDataSetChanged();
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
    }
}
