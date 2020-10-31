package com.tribeappsoft.leedo.admin.reports.callLogStats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.reports.callLogStats.detailedStatsList.FilterCallCompletedDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.callLogStats.detailedStatsList.FilterCallStatsDetailsActivity;
import com.tribeappsoft.leedo.admin.reports.callLogStats.filter.Filter_SalesHeadCallLogs;
import com.tribeappsoft.leedo.admin.reports.callLogStats.model.CallLogesModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AllCallLogStats_Activity extends AppCompatActivity {

    @BindView(R.id.tv_salesHeadCallLogStats_project_name) AutoCompleteTextView acTv_select_project;
    @BindView(R.id.tv_salesHeadCallLogStats_select_SalesPerson) AutoCompleteTextView acTv__select_SalesPerson;


    @BindView(R.id.ll_salesHeadCallLogStats_scheduled_callsBlock) LinearLayoutCompat ll_scheduled_callsBlock;
    @BindView(R.id.tv_salesHeadCallLogStats_scheduledCalls) AppCompatTextView tv_scheduledCalls;

    @BindView(R.id.ll_salesHeadCallLogStats_rescheduled_callsBlock) LinearLayoutCompat ll_rescheduled_callsBlock;
    @BindView(R.id.tv_salesHeadCallLogStats_rescheduledCalls) AppCompatTextView tv_rescheduledCalls;

    @BindView(R.id.ll_salesHeadCallLogStats_completed_callsBlock) LinearLayoutCompat ll_completed_callsBlock;
    @BindView(R.id.tv_salesHeadCallLogStats_CompletedCalls) AppCompatTextView tv_CompletedCalls;

    @BindView(R.id.ll_salesHeadCallLogStats_cancelled_callsBlock) LinearLayoutCompat ll_cancelled_callsBlock;
    @BindView(R.id.tv_salesHeadCallLogStats_cancelledCalls) AppCompatTextView tv_cancelledCalls;

    @BindView(R.id.ll_salesHeadCallLogStats_pb) LinearLayoutCompat ll_pb;
    @BindView(R.id.nsv_salesHeadCallLogStats) NestedScrollView nsv_stats;
    @BindView(R.id.sfl_salesHeadCallLogStats_stats) ShimmerFrameLayout sfl;
    @BindView(R.id.ll_salesHeadCallLogStats_noData) LinearLayoutCompat ll_noData;

    private Activity context;
    private String TAG = "salesHeadCallLogStats_Activity",api_token ="",from_date="",to_date="",sales_person_name="",project_name="";
    private CallLogesModel callLogesModel = null;
    // private ArrayList<MySalesHeadStatsModel> myPerformanceModelArrayList;
    private int user_id=0,sales_person_id=0,filterCount_calls=0,project_id=0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActionMode mActionMode;
    private AppCompatTextView tvFilterItemCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_head_call_logs_);
        //overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context= AllCallLogStats_Activity.this;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(getString(R.string.menu_salesHeadCallLogStats));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        sales_person_id = sharedPreferences.getInt("user_id", 0);
        user_id = sharedPreferences.getInt("user_id", 0);
        //isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();

        //sr_achievements.setRefreshing(false);
        nsv_stats.setVisibility(View.VISIBLE);
        hideProgressBar();

       /* if (isNetworkAvailable(Objects.requireNonNull(context))) {

            // showProgressBar("Please wait...");
            showShimmer();
            new Handler().postDelayed(this::getMyCallLogesCount, 100);
        }
        else
        {
            //hide pb
            hideProgressBar();
            NetworkError(context);

            nsv_stats.setVisibility(View.GONE);
            //show no data
            ll_noData.setVisibility(View.VISIBLE);
        }*/

        // showProgressBar();
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            showProgressBar();
            //showShimmer();
            new Handler().postDelayed(this::getMyCallLogesCount, 100);
        }
        else
        {
            //hide pb
            hideProgressBar();
            Helper.NetworkError(context);
            nsv_stats.setVisibility(View.VISIBLE);
            //show no data
            ll_noData.setVisibility(View.GONE);
        }


    }

    //On Resume
    @Override
    public void onResume() {
        super.onResume();

        nsv_stats.getParent().requestChildFocus(nsv_stats, nsv_stats);

        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            boolean isFilter = sharedPreferences.getBoolean("isFilter", false);
            boolean clearFilter = sharedPreferences.getBoolean("clearFilter", false);
            editor.apply();

            if(isFilter) {

                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    project_id = sharedPreferences.getInt("project_id", 0);
                    sales_person_id = sharedPreferences.getInt("sales_person_id", 0);
                    if (sales_person_id==0) sales_person_id = sharedPreferences.getInt("user_id", 0);
                    filterCount_calls = sharedPreferences.getInt("filterCount_calls", 0);
                    from_date = sharedPreferences.getString("sendFromDate","");
                    to_date = sharedPreferences.getString("sendToDate", "");
                    sales_person_name = sharedPreferences.getString("sales_person_name","");
                    project_name = sharedPreferences.getString("project_name","");
                    editor.apply();
                }

                Log.e(TAG, "onResume:Filter project_id:- "+project_id+"\n from_date:- " +from_date+"\n to_date:- "+to_date+"\n sales_person_name:- "+sales_person_name+"\n project_name:- "+project_name );

                //reset api call
                // showProgressBar();
                if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                   // showProgressBar();
                    //showShimmer();
                    new Handler().postDelayed(this::getMyCallLogesCount, 100);
                }
                else
                {
                    //hide pb
                    hideProgressBar();
                    Helper.NetworkError(context);
                    nsv_stats.setVisibility(View.VISIBLE);
                    //show no data
                    ll_noData.setVisibility(View.GONE);
                }

            }
            else if (clearFilter) {

                //all filters are cleared
                Log.e(TAG, "onResume:clearFilter  ");

                if (sharedPreferences!=null) {
                    editor = sharedPreferences.edit();
                    sales_person_id = sharedPreferences.getInt("user_id", 0);
                    editor.remove("clearFilter");
                    editor.apply();
                }

                //clear fields
                project_id = filterCount_calls = 0;
                from_date = to_date = "";
                //reset api call

                resetData();
            }
        }



        //setupBadge
        setupBadge();
    }


    private void getMyCallLogesCount() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllCallLogesStats(api_token,project_id,sales_person_id,from_date,to_date, user_id == sales_person_id);
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
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");
                        runOnUIThread();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getResources().getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getResources().getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            Log.e(TAG, "onNext: in data" );
                                            if (/*!JsonObjectResponse.body().get("data").isJsonNull() &&*/ JsonObjectResponse.body().get("data").isJsonObject()) {

                                                Log.e(TAG, "onNext: " +JsonObjectResponse.body().get("data").getAsJsonObject().toString());
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                Log.e(TAG, "onNext: " +JsonObjectResponse.body().get("data").getAsJsonObject());
                                                setDataJson(jsonObject);
                                            }
                                            Log.e(TAG, "onNext: out data" );
                                        }
                                    } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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
                                    showErrorLog(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });
    }

    private void setDataJson(JsonObject jsonObject)
    {
        callLogesModel = new CallLogesModel();

        if (jsonObject.has("call_schedule_count")) callLogesModel.setSchedulesCalls(!jsonObject.get("call_schedule_count").isJsonNull() ? jsonObject.get("call_schedule_count").getAsString() : "0" );
        if (jsonObject.has("call_reschedule_count")) callLogesModel.setReschedulesCalls(!jsonObject.get("call_reschedule_count").isJsonNull() ? jsonObject.get("call_reschedule_count").getAsString() : "0" );
        if (jsonObject.has("call_completed_count")) callLogesModel.setCompletedCalls(!jsonObject.get("call_completed_count").isJsonNull() ? jsonObject.get("call_completed_count").getAsString() : "0" );
        if (jsonObject.has("call_cancelled_count")) callLogesModel.setCancelledCalls(!jsonObject.get("call_cancelled_count").isJsonNull() ? jsonObject.get("call_cancelled_count").getAsString() : "0");
    }

    private void runOnUIThread()
    {
        // sr_achievements.setRefreshing(false);

        if(context!= null)
        {
            Log.e(TAG, "runOnUIThread out out: "+callLogesModel.getSchedulesCalls()+callLogesModel.getReschedulesCalls()+callLogesModel.getCompletedCalls()+callLogesModel.getCancelledCalls());

            runOnUiThread(() -> {
                //sr_achievements.setRefreshing(false);
                hideShimmer();
                hideProgressBar();

                nsv_stats.setVisibility(View.VISIBLE);
                ll_pb.setVisibility(View.GONE);

                //set adapter for project names
                //setAdapterForProjectName();

                Log.e(TAG, "runOnUIThread out: "+callLogesModel.getSchedulesCalls()+callLogesModel.getReschedulesCalls()+callLogesModel.getCompletedCalls()+callLogesModel.getCancelledCalls());

                if(callLogesModel !=null)
                {
                    Log.e(TAG, "runOnUIThread: "+callLogesModel.getSchedulesCalls()+callLogesModel.getReschedulesCalls()+callLogesModel.getCompletedCalls()+callLogesModel.getCancelledCalls());
                    tv_scheduledCalls.setText(callLogesModel.getSchedulesCalls());
                    tv_rescheduledCalls.setText(callLogesModel.getReschedulesCalls());
                    tv_CompletedCalls.setText(callLogesModel.getCompletedCalls());
                    tv_cancelledCalls.setText(callLogesModel.getCancelledCalls());
                }
                else {
                    nsv_stats.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }


                //scheduled_calls click
                ll_scheduled_callsBlock.setOnClickListener(view -> {
                    // scheduled list
                    openDetailsActivity(FilterCallStatsDetailsActivity.class, ll_scheduled_callsBlock, 1);
                });

                //rescheduled Callsclick
                ll_rescheduled_callsBlock.setOnClickListener(view -> {
                    // scheduled_calls list
                    openDetailsActivity(FilterCallStatsDetailsActivity.class, ll_rescheduled_callsBlock, 2);
                });

                //completed Calls  click
                ll_completed_callsBlock.setOnClickListener(view -> {
                    // Completed Calls
                    openDetailsActivity(FilterCallCompletedDetailsActivity.class, ll_completed_callsBlock, 3);
                });

                //cancelled Calls click
                ll_cancelled_callsBlock.setOnClickListener(view -> {
                    //cancelled Calls
                    openDetailsActivity(FilterCallStatsDetailsActivity.class, ll_cancelled_callsBlock, 4);
                });
            });

        }
    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }


    private void openDetailsActivity(Class aClass,  LinearLayoutCompat linearLayoutCompat, int schedule_status_id)
    {

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(context, aClass);
       /*     if (flag ==2) {
                //lead token status id
                intent.putExtra("lead_token_status_id",3);
                intent.putExtra("flag",0);
            }
            else {
                intent.putExtra("flag",flag);
            }*/

            intent.putExtra("sales_team_lead_stats",user_id == sales_person_id);
            intent.putExtra("sales_person_id",sales_person_id);
            intent.putExtra("project_id", project_id);
            intent.putExtra("schedule_status_id", schedule_status_id);
            intent.putExtra("from_date", from_date);
            intent.putExtra("to_date", to_date);
            intent.putExtra("Project_name", project_name);
            intent.putExtra("full_name", sales_person_name);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, linearLayoutCompat, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());

        }, 600);

    }

    private void resetData()
    {
        if (Helper.isNetworkAvailable(context))
        {
            //gone visibility
            ll_noData.setVisibility(View.GONE);

            //5. clear action mode
            if (mActionMode != null) {
                mActionMode.setTitle("");
                mActionMode.finish();
            }
            mActionMode = null;

            //clear filter if applied from sharedPref
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.remove("project_id");
                editor.remove("sales_person_id");
                editor.remove("from_date");
                editor.remove("to_date");
                editor.remove("sales_person_name");
                editor.remove("project_name");
                editor.remove("isFilter");
                editor.putBoolean("clearFilter", false);
                sales_person_id = sharedPreferences.getInt("user_id", 0);
                editor.apply();
            }
            //clear filter fields
            project_id = filterCount_calls = 0;
            from_date = to_date = sales_person_name = project_name = "";


            // showProgressBar();
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                 showProgressBar();
                //showShimmer();
                new Handler().postDelayed(this::getMyCallLogesCount, 100);
            }
            else
            {
                //hide pb
                hideProgressBar();
                Helper.NetworkError(context);

                nsv_stats.setVisibility(View.VISIBLE);
                //show no data
                ll_noData.setVisibility(View.GONE);
            }

            //set up badge count
            setupBadge();



        } else Helper.NetworkError(context);
    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar() {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        //tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showShimmer() {
        sfl.setVisibility(View.VISIBLE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_sales_head_filter, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_filter_);
        View actionView = menuItem.getActionView();
        tvFilterItemCount = actionView.findViewById(R.id.cart_badge);
        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case (android.R.id.home):
                onBackPressed();
                break;

            case (R.id.action_filter_):
                startActivity(new Intent(context, Filter_SalesHeadCallLogs.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBadge() {

        if (tvFilterItemCount != null) {
            Log.e(TAG, "setupBadge: "+filterCount_calls );
            if (filterCount_calls == 0) {
                if (tvFilterItemCount.getVisibility() != View.GONE) {
                    tvFilterItemCount.setVisibility(View.GONE);
                }
            } else {
                tvFilterItemCount.setText(String.valueOf(Math.min(filterCount_calls, 99)));
                if (tvFilterItemCount.getVisibility() != View.VISIBLE) {
                    tvFilterItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );

        //clear filter if applied from sharedPref
        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.remove("project_id");
            editor.remove("sales_person_id");
            editor.remove("from_date");
            editor.remove("to_date");
            editor.remove("isFilter");
            editor.putBoolean("clearFilter", false);
            sales_person_id = sharedPreferences.getInt("user_id", 0);
            editor.apply();
        }

        //clear fields
        project_id = filterCount_calls = 0;
        from_date = to_date = sales_person_name = project_name = "";
    }
}
