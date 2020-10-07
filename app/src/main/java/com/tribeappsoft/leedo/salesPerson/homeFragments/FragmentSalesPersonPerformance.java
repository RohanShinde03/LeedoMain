package com.tribeappsoft.leedo.salesPerson.homeFragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.salesPerson.filterFeed.FeedFilterActivity;
import com.tribeappsoft.leedo.salesPerson.models.MyPerformanceModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatBookingDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatGHPDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatLeadDetailsActivity;
import com.tribeappsoft.leedo.salesPerson.salesHead.detailedStats.StatSiteVisitDetailsActivity;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragmentSalesPersonPerformance extends Fragment  {

    private Context context;
    private String TAG = "FragmentSalesPersonPerformance",selectedProjectName="",api_token="",
            sendPerformanceFromDate = null, sendPerformanceToDate = null, full_name = "";
    private ArrayList<MyPerformanceModel> myPerformanceModelArrayList;
    //String ProjectArrayList
    private DatePickerDialog datePickerDialog;
    private ArrayList<String> projectStringArrayList;
    private int user_id=0, myPosition =0,selectedProjectId=0, mYear, mMonth, mDay;

    //@BindView(R.id.sr_sales_person_achievements) SwipeRefreshLayout sr_achievements;
    @BindView(R.id.toolbar_achievements) MaterialToolbar toolbar;
    @BindView(R.id.tv_performance_project_name) AutoCompleteTextView tv_performance_project_name;
    @BindView(R.id.tv_performance_site_Visit) AppCompatTextView tv_performance_site_Visit;
    @BindView(R.id.tv_performance_token_generated) AppCompatTextView tv_performance_token_generated;
    @BindView(R.id.tv_performance_token_plus_generated) AppCompatTextView tv_performance_token_plus_generated;
    @BindView(R.id.tv_performance_leads) AppCompatTextView tv_performance_leads;
    @BindView(R.id.tv_performance_bookings) AppCompatTextView tv_performance_bookings;
    @BindView(R.id.tv_status_sold) AppCompatTextView tv_status_sold;
    @BindView(R.id.tv_performance_hold) AppCompatTextView tv_status_onhold;
    @BindView(R.id.tv_status_reserved) AppCompatTextView tv_status_reserved;
    @BindView(R.id.edt_performance_FromDate) TextInputEditText edt_performanceFromDate;
    @BindView(R.id.edt_performance_ToDate) TextInputEditText edt_performanceToDate;

    @BindView(R.id.ll_performance_sold) LinearLayoutCompat ll_performance_sold;
    @BindView(R.id.ll_perforamnce_onhold) LinearLayoutCompat ll_perforamnce_onhold;
    @BindView(R.id.ll_performance_reserved) LinearLayoutCompat ll_performance_reserved;
    @BindView(R.id.ll_achievements_pb) LinearLayoutCompat ll_pb;
    @BindView(R.id.ll_myPerformance_main) LinearLayoutCompat ll_main;
    @BindView(R.id.ll_myPerformance_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.sfl_myPerformance_stats) ShimmerFrameLayout sfl;
    @BindView(R.id.nsv_salesPersonStats) NestedScrollView nsv_stats;
    @BindView(R.id.ll_lead_block) LinearLayoutCompat ll_lead_block;
    @BindView(R.id.ll_site_visit_block) LinearLayoutCompat ll_site_visit_block;
    @BindView(R.id.ll_token_generated_block) LinearLayoutCompat ll_token_generated_block;
    @BindView(R.id.ll_ghp_plus_generated_block) LinearLayoutCompat ll_ghp_plus_generated_block;
    @BindView(R.id.ll_booking_block) LinearLayoutCompat ll_booking_block;
    @BindView(R.id.ll_flat_holds) LinearLayoutCompat ll_flat_holds;


    public FragmentSalesPersonPerformance() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_sales_person_bookings, container, false);

        View rootView = inflater.inflate(R.layout.fragment_sales_person_performace, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);
        toolbar.setTitle(getString(R.string.my_performance));

        try {
            rootView.setFocusableInTouchMode(true);
            rootView.requestFocus();
            rootView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //goto Home Fragment
                        openFragment(0, "", "");
                        //doOnBackPressed();
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        full_name = sharedPreferences.getString("full_name", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        myPerformanceModelArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        //sr_achievements.setRefreshing(false);
        nsv_stats.setVisibility(View.GONE);
        /*Get Project Brochures*/
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
        {
            // sr_achievements.setRefreshing(true);
            //  ll_pb.setVisibility(View.VISIBLE);
            showShimmer();
            new Handler().postDelayed(this::getMyPerformance, 100);
        }
        else
        {
            //show network error
            Helper.NetworkError(getActivity());
            //hide main layout
            nsv_stats.setVisibility(View.GONE);
            ll_main.setVisibility(View.GONE);
            //show no data
            ll_noData.setVisibility(View.VISIBLE);
        }


        //set visit date time
        edt_performanceFromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        edt_performanceToDate.setOnClickListener(view -> {
            if (sendPerformanceFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(getActivity(), "Please select from date first!");
        });

        //ll_performance_sold.setOnClickListener(view -> openFragment(5));
        //ll_perforamnce_onhold.setOnClickListener(view -> openFragment(6));
        //ll_performance_reserved.setOnClickListener(view -> openFragment(7));


        return rootView;
    }


    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_performanceFromDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPerformanceFromDate = year + "-" + mth + "-" + dayOfMonth;

                    if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
                    {
                        //  showShimmer();
                        ll_pb.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(this::getMyPerformance, 100);
                    }

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "From Date: "+ sendPerformanceFromDate);


                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_performanceToDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPerformanceToDate = year + "-" + mth + "-" + dayOfMonth;

                    if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
                    {
                        // showShimmer();
                        ll_pb.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(this::getMyPerformance, 100);
                    }

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendPerformanceToDate);

                    //check button EnabledView
                    // checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(Helper.getLongNextDateFromString(sendPerformanceFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }



    private void openFragment(int openFlag, String other_ids, String display_text)
    {
        startActivity(new Intent(getActivity(), SalesPersonBottomNavigationActivity.class)
                        .putExtra("notifyPerformance", true)
                        .putExtra("openFlag", openFlag)
                        .putExtra("other_ids", other_ids)
                        .putExtra("display_text", display_text)
                //.addFlags(FLAG_ACTIVITY_CLEAR_TOP |  FLAG_ACTIVITY_SINGLE_TOP)
        );
        Objects.requireNonNull(getActivity()).finish();
    }

    private void openFilterActivity(int openFlag, String other_ids, String display_text, LinearLayoutCompat linearLayoutCompat)
    {

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(getActivity(), FeedFilterActivity.class);
            intent.putExtra("openFlag",openFlag);
            intent.putExtra("other_ids", other_ids);
            intent.putExtra("display_text", display_text);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = linearLayoutCompat;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(Objects.requireNonNull(getActivity()), viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());

        }, 600);

    }

    private void getMyPerformance() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().GetPerformance(api_token,user_id,sendPerformanceFromDate,sendPerformanceToDate);
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
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                myPerformanceModelArrayList.clear();
                                                projectStringArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setProjectNamesJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                            }
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

    private void setProjectNamesJson(JsonObject jsonObject)
    {
        MyPerformanceModel myPerformanceModel = new MyPerformanceModel();
        if (jsonObject.has("project_id")) myPerformanceModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) myPerformanceModel.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("leads_site_visits")) myPerformanceModel.setLeads_site_visits(!jsonObject.get("leads_site_visits").isJsonNull() ? jsonObject.get("leads_site_visits").getAsString() : "0" );
        if (jsonObject.has("leads")) myPerformanceModel.setLeads(!jsonObject.get("leads").isJsonNull() ? jsonObject.get("leads").getAsString() : "0" );
        if (jsonObject.has("lead_tokens")) myPerformanceModel.setLead_tokens(!jsonObject.get("lead_tokens").isJsonNull() ? jsonObject.get("lead_tokens").getAsString() : "0" );
        if (jsonObject.has("lead_tokens_ghp_plus")) myPerformanceModel.setLead_plus_tokens(!jsonObject.get("lead_tokens_ghp_plus").isJsonNull() ? jsonObject.get("lead_tokens_ghp_plus").getAsString() : "0");
        if (jsonObject.has("booking_master")) myPerformanceModel.setBooking_master(!jsonObject.get("booking_master").isJsonNull() ? jsonObject.get("booking_master").getAsString() : "0" );
        if (jsonObject.has("unit_hold_release")) myPerformanceModel.setProject_units_OnHold(!jsonObject.get("unit_hold_release").isJsonNull() ? jsonObject.get("unit_hold_release").getAsString() : "0" );
        if (jsonObject.has("project_units") && !jsonObject.get("project_units").isJsonNull()) {
            if (jsonObject.get("project_units").isJsonObject()) {
                JsonObject jsonObjectUnits = jsonObject.get("project_units").getAsJsonObject();
                if (jsonObjectUnits.has("Sold")) myPerformanceModel.setProject_units_sold(!jsonObjectUnits.get("Sold").isJsonNull() ? jsonObjectUnits.get("Sold").getAsString() : "" );
                if (jsonObjectUnits.has("Reserved")) myPerformanceModel.setProject_units_Reserved(!jsonObjectUnits.get("Reserved").isJsonNull() ? jsonObjectUnits.get("Reserved").getAsString() : "" );
            }
        }
        myPerformanceModelArrayList.add(myPerformanceModel);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    private void runOnUIThread()
    {
        // sr_achievements.setRefreshing(false);

        if(getActivity()!= null)
        {
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                //sr_achievements.setRefreshing(false);
                hideShimmer();
                nsv_stats.setVisibility(View.VISIBLE);
                ll_pb.setVisibility(View.GONE);

                //set adapter for project names
                setAdapterForProjectName();

                //openFragments
               /* ll_lead_block.setOnClickListener(view -> openFragment(1, getFilterJson(selectedProjectId, 4,0), selectedProjectName));
                ll_site_visit_block.setOnClickListener(view -> openFragment(2, getFilterJson(selectedProjectId, 5,0), selectedProjectName));
                ll_token_generated_block.setOnClickListener(view -> openFragment(3, getFilterJson(selectedProjectId, 6,0), selectedProjectName));
                //TODO change flag and status
                ll_ghp_plus_generated_block.setOnClickListener(view -> openFragment(3, getFilterJson(selectedProjectId, 6,1), selectedProjectName));
                ll_flat_holds.setOnClickListener(view -> openFragment(4, getFilterJson(selectedProjectId, 8,0), selectedProjectName));
                ll_booking_block.setOnClickListener(view -> openFragment(5, getFilterJson(selectedProjectId, 9,0), selectedProjectName));*/

                //openActivity
              /*  ll_lead_block.setOnClickListener(view -> openFilterActivity(1, getFilterJson(selectedProjectId, 4,0), selectedProjectName, ll_lead_block));
                ll_site_visit_block.setOnClickListener(view -> openFilterActivity(2, getFilterJson(selectedProjectId, 5,0), selectedProjectName, ll_site_visit_block));
                ll_token_generated_block.setOnClickListener(view -> openFilterActivity(3, getFilterJson(selectedProjectId, 6,0), selectedProjectName, ll_token_generated_block));
                //TODO change flag and status
                ll_ghp_plus_generated_block.setOnClickListener(view -> openFilterActivity(3, getFilterJson(selectedProjectId, 6,1), selectedProjectName, ll_ghp_plus_generated_block));
                ll_flat_holds.setOnClickListener(view -> openFilterActivity(4, getFilterJson(selectedProjectId, 8,0), selectedProjectName, ll_flat_holds));
                ll_booking_block.setOnClickListener(view -> openFilterActivity(5, getFilterJson(selectedProjectId, 9,0), selectedProjectName, ll_booking_block));*/


                //openActivity
                ll_lead_block.setOnClickListener(view -> openDetailsActivity(StatLeadDetailsActivity.class, ll_lead_block, 0, 0));
                ll_site_visit_block.setOnClickListener(view -> openDetailsActivity(StatSiteVisitDetailsActivity.class, ll_site_visit_block,0, 5));
                ll_token_generated_block.setOnClickListener(view -> openDetailsActivity(StatGHPDetailsActivity.class, ll_token_generated_block,1,0));
                //TODO change flag and status
                ll_ghp_plus_generated_block.setOnClickListener(view -> openDetailsActivity(StatGHPDetailsActivity.class,  ll_ghp_plus_generated_block, 3, 0));
                ll_flat_holds.setOnClickListener(view -> openFilterActivity(4, getFilterJson(selectedProjectId, 8,0), selectedProjectName, ll_flat_holds));
                ll_booking_block.setOnClickListener(view -> openDetailsActivity(StatBookingDetailsActivity.class, ll_booking_block, 0, 0));


            });
            Log.e(TAG, "Outside runOnUIThread: ");
        }
    }


    private String getFilterJson(int selectedProjectId, int lead_status_id,int ghp_plus)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("lead_status_id", lead_status_id);
        if (lead_status_id ==6) {
            //if ghp generated check for ghp_type
            jsonObject.addProperty("token_type_id", ghp_plus==1 ? 3 : 1);
        }
        Log.e(TAG, "jsonObject"+jsonObject.toString());
        return jsonObject!=null ? jsonObject.toString() : "";
    }


    private void openDetailsActivity(Class aClass,  LinearLayoutCompat linearLayoutCompat, int flag, int lead_status_id)
    {
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(context, aClass);
            if (flag ==2) {
                //lead token status id
                intent.putExtra("lead_token_status_id",3);
                intent.putExtra("flag",0);
            }
            else if(flag == 4){
                intent.putExtra("flag",1);
                intent.putExtra("lead_status_id",1);
            }
            else {
                intent.putExtra("flag",flag);
                intent.putExtra("lead_status_id",lead_status_id);
            }

            intent.putExtra("sales_person_id",user_id);
            intent.putExtra("project_id", selectedProjectId);
            intent.putExtra("from_date", sendPerformanceFromDate);
            intent.putExtra("to_date", sendPerformanceToDate);
            intent.putExtra("Project_name", selectedProjectName);
            intent.putExtra("full_name", full_name);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = linearLayoutCompat;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(Objects.requireNonNull(getActivity()), viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());

        }, 600);
    }



    private void setAdapterForProjectName()
    {
        projectStringArrayList.clear();
        for (int i =0; i< myPerformanceModelArrayList.size(); i++) {
            projectStringArrayList.add(myPerformanceModelArrayList.get(i).getProject_name());

        }
        // Log.e(TAG, "projectStringArrayList: "+projectStringArrayList.size());

        if (projectStringArrayList.size()>0 && myPerformanceModelArrayList.size()>0 )
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
            tv_performance_project_name.setText(projectStringArrayList.get(myPosition));
            tv_performance_project_name.setAdapter(adapter);
            //tv_performance_project_name.setThreshold(0);

            //def set
            selectedProjectId = myPerformanceModelArrayList.get(0).getProject_id();
            selectedProjectName = myPerformanceModelArrayList.get(0).getProject_name();
            setPerformanceData(myPosition);

            tv_performance_project_name.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                myPosition = position;
                for (MyPerformanceModel pojo : myPerformanceModelArrayList)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedProjectId = pojo.getProject_id(); // This is the correct ID
                        selectedProjectName = pojo.getProject_name();
                        Log.e(TAG, "project name & id: "+selectedProjectName + " \t" +selectedProjectId);

                        ll_pb.setVisibility(View.VISIBLE);
                        Timer buttonTimer = new Timer();
                        buttonTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                    setPerformanceData(myPosition);
                                    ll_pb.setVisibility(View.GONE);
                                });
                            }
                        }, 1000);

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }

    private void setPerformanceData(int position)
    {
        tv_performance_site_Visit.setText(myPerformanceModelArrayList.get(position).getLeads_site_visits());
        tv_performance_token_generated.setText(myPerformanceModelArrayList.get(position).getLead_tokens());
        tv_performance_token_plus_generated.setText(myPerformanceModelArrayList.get(position).getLead_plus_tokens());
        tv_performance_leads.setText(myPerformanceModelArrayList.get(position).getLeads());
        tv_performance_bookings.setText(myPerformanceModelArrayList.get(position).getBooking_master());
        tv_status_onhold.setText(myPerformanceModelArrayList.get(position).getProject_units_OnHold());
        tv_status_sold.setText(myPerformanceModelArrayList.get(position).getProject_units_sold());
        tv_status_reserved.setText(myPerformanceModelArrayList.get(position).getProject_units_Reserved());
    }



    private void showErrorLog(final String message)
    {
        if (getActivity()!=null)
        {
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {

                //
                ll_pb.setVisibility(View.GONE);

                //hide shimmer effect
                hideShimmer();

                Helper.onErrorSnack(getActivity(),message);
                //hide main layout
                nsv_stats.setVisibility(View.GONE);
                ll_main.setVisibility(View.GONE);
                //show no data
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
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
    public void onDetach() {
        super.onDetach();
    }


}


 /*Bundle bundle = new Bundle();
        bundle.putBoolean("fromAchievements", true);
        bundle.putInt("openFlag", openFlag);
        Fragment defaultFragment = new FragmentSalesPersonHomeFeeds();
        defaultFragment.setArguments(bundle);
        FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_salesPerson_homeContainer, defaultFragment);
        ft.addToBackStack("FRAGMENT_OTHER");
        ft.commit();*/
//getActivity().getSupportFragmentManager().popBackStack("FRAGMENT_OTHER", POP_BACK_STACK_INCLUSIVE);
