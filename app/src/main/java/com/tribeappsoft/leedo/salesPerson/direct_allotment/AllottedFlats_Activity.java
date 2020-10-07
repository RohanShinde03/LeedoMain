package com.tribeappsoft.leedo.salesPerson.direct_allotment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.adapter.AllottedFlatListAdapter;
import com.tribeappsoft.leedo.salesPerson.models.AllottedFlatListModel;
import com.tribeappsoft.leedo.util.Helper;

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

public class AllottedFlats_Activity extends AppCompatActivity {


    /*Project Name list*/
    @BindView(R.id.nsv_projectBrochures) NestedScrollView nsv;
    @BindView(R.id.ll_ProjectBrochuresActivity_projectNameMain) LinearLayoutCompat ll_projectNameMain;
    @BindView(R.id.tv_ProjectBrochuresActivity_select_project) AutoCompleteTextView tv_select_project;

    @BindView(R.id.swipeRefresh_ProjectBrochuresActivity) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.recycler_ProjectBrochuresActivity) RecyclerView recyclerView;
    @BindView(R.id.ll_allottedFlats_noData) LinearLayoutCompat ll_noDataFound;


    private Activity context;
    private AllottedFlatListAdapter recyclerAdapter;
    public ArrayList<ProjectModel> projectModelArraylist;
    private ArrayList<String> projectStringArrayList;

    private String TAG = "ProjectBrochuresActivity";
    public SharedPreferences sharedPreferences;
    private boolean isSalesHead = false;
    public int user_id = 0,selectedProjectId=0, myPosition =0;
    public String selectedProjectName="", api_token ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allotted_flats_);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = AllottedFlats_Activity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_allottedFlats));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();

        projectModelArraylist = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();


    }


    private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        //recyclerAdapter = new ProjectBrochuresRecyclerAdapter(context, projectModelArraylist.get(myPosition).getEventProjectDocsModelArrayList());
        //recyclerView.setAdapter(recyclerAdapter);
    }


    private void setSwipeRefresh()
    {

        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                swipeRefresh.setRefreshing(true);
                //call get api
                projectModelArraylist.clear();
                new Thread(this::getAllAllottedFlats).start();
            }
            else {
                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            }

        });

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary);


        //getOffline();

    }


    @Override
    public void onResume()
    {
        super.onResume();

        /*Get Project Brochures*/
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //new Thread(this::getProjectListData).start();
            swipeRefresh.setRefreshing(true);
            projectModelArraylist.clear();
            new Thread(this::getAllAllottedFlats).start();
        }
        else
        {
            Helper.NetworkError(context);
            swipeRefresh.setRefreshing(false);
            nsv.setVisibility(View.GONE);
            ll_projectNameMain.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            ll_noDataFound.setVisibility(View.VISIBLE);
        }
        //perform search
    }


    private void getAllAllottedFlats() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllAllottedFlats(user_id,api_token,isSalesHead ? 1 : 0);
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
                        setAllAllotedFlats();
                    }

                    @Override
                    public void onError(final Throwable e) {
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
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                projectModelArraylist.clear();
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


    private void setAllAllotedFlats()
    {
        if (context!=null)
        {
            runOnUiThread(() -> {


                //set adapter for project names
                setAdapterForProjectName();

                //set recyclerView
                delayRefresh();
            });
        }


    }

    private void setProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel projectModel = new ProjectModel();
        if (jsonObject.has("project_id")) projectModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) projectModel.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("alloted_units") && !jsonObject.get("alloted_units").isJsonNull())
        {
            if (jsonObject.get("alloted_units").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("alloted_units").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<AllottedFlatListModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setProjectAllottedFlatsJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    projectModel.setAllottedFlatListModelArrayList(arrayList);
                }
            }
        }
        projectModelArraylist.add(projectModel);
    }


    private void setProjectAllottedFlatsJson(JsonObject jsonObject, ArrayList<AllottedFlatListModel> arrayList)
    {
        AllottedFlatListModel allottedFlatListModel = new AllottedFlatListModel();

        if (jsonObject.has("booking_id")) allottedFlatListModel.setBooking_id(!jsonObject.get("booking_id").isJsonNull() ? jsonObject.get("booking_id").getAsInt() : 0 );
        if (jsonObject.has("lead_types_id")) allottedFlatListModel.setLead_type_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0 );
        if (jsonObject.has("date_created")) allottedFlatListModel.setDate_created(!jsonObject.get("date_created").isJsonNull() ? jsonObject.get("date_created").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("booking_amt")) allottedFlatListModel.setBooking_amt(!jsonObject.get("booking_amt").isJsonNull() ? jsonObject.get("booking_amt").getAsString() : "" );
        if (jsonObject.has("flat_total")) allottedFlatListModel.setFlat_total(!jsonObject.get("flat_total").isJsonNull() ? jsonObject.get("flat_total").getAsString() : "" );
        if (jsonObject.has("lead_uid")) allottedFlatListModel.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
        if (jsonObject.has("project_name")) allottedFlatListModel.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) allottedFlatListModel.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("block_name")) allottedFlatListModel.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "" );
        if (jsonObject.has("unit_name")) allottedFlatListModel.setUnit_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "" );
        if (jsonObject.has("token_no")) allottedFlatListModel.setToken_no(!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() : "" );
        if (jsonObject.has("unit_category")) allottedFlatListModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("token_type")) allottedFlatListModel.setToken_type(!jsonObject.get("token_type").isJsonNull() ? jsonObject.get("token_type").getAsString() : "" );
        if (jsonObject.has("event_title")) allottedFlatListModel.setEvent_title(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() : "" );
        if (jsonObject.has("lead_types_name")) allottedFlatListModel.setLead_types_name(!jsonObject.get("lead_types_name").isJsonNull() ? jsonObject.get("lead_types_name").getAsString() : "" );
        if (jsonObject.has("full_name")) allottedFlatListModel.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("sales_name")) allottedFlatListModel.setSales_name(!jsonObject.get("sales_name").isJsonNull() ? jsonObject.get("sales_name").getAsString() : "" );
        if (jsonObject.has("setSite_visit_date")) allottedFlatListModel.setSite_visit_date(!jsonObject.get("setSite_visit_date").isJsonNull() ? jsonObject.get("setSite_visit_date").getAsString() : Helper.getDateTime() );
        if (jsonObject.has("site_visit_verified_by")) allottedFlatListModel.setSite_visit_verified_by(!jsonObject.get("site_visit_verified_by").isJsonNull() ? jsonObject.get("site_visit_verified_by").getAsString() : "" );
        if (jsonObject.has("executive_name")) allottedFlatListModel.setExecutive_name(!jsonObject.get("executive_name").isJsonNull() ? jsonObject.get("executive_name").getAsString() : "" );

        if (jsonObject.has("booking_attachment"))
        {
            if (!jsonObject.get("booking_attachment").isJsonNull() && jsonObject.get("booking_attachment").isJsonArray())
            {
                JsonArray jsonArray =  jsonObject.get("booking_attachment").getAsJsonArray();
                ArrayList<String> stringArrayList=new ArrayList<>();
                stringArrayList.clear();

                for(int i=0;i<jsonArray.size();i++)
                {
                    setbookingImgs(jsonArray.get(i).getAsJsonObject(),stringArrayList);
                }

                allottedFlatListModel.setImgstringArrayList(stringArrayList);
            }
        }

        arrayList.add(allottedFlatListModel);
    }

    private void setbookingImgs(JsonObject jsonObject, ArrayList<String> stringArrayList)
    {
        if (jsonObject.has("media_path")) stringArrayList.add(!jsonObject.get("media_path").isJsonNull() ? jsonObject.get("media_path").getAsString() : "" );
    }

    private void setAdapterForProjectName()
    {

        if (projectStringArrayList.size()>0 && projectModelArraylist.size()>0)
        {
            //ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, projectStringArrayList);
            tv_select_project.setText(projectStringArrayList.get(0));
            tv_select_project.setAdapter(adapter);
            tv_select_project.setThreshold(0);

            tv_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);

                for (ProjectModel pojo : projectModelArraylist)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {

                        selectedProjectId= pojo.getProject_id();
                        selectedProjectName = pojo.getProject_name();
                        myPosition = position;
                        delayRefresh();
                        Log.e(TAG, "Project Name & ID " + selectedProjectName +"\t"+ selectedProjectId);

                        break;
                    }
                }
            });
        }


        tv_select_project.addTextChangedListener(new TextWatcher() {
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


    private void delayRefresh()
    {

        runOnUiThread(() ->
        {

            swipeRefresh.setRefreshing(false);

            if (projectModelArraylist.size()>0)
            {
                //ll_pb.setVisibility(View.GONE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new AllottedFlatListAdapter(context, projectModelArraylist.get(myPosition).getAllottedFlatListModelArrayList(),api_token);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    recyclerView.setVisibility(View.GONE);
                    ll_noDataFound.setVisibility(View.VISIBLE);
                }else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noDataFound.setVisibility(View.GONE);
                }

            }

        });


    }






    /*ShowErrorLog*/
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {
                //ll_pb.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                Helper.onErrorSnack(context,message);
                recyclerView.setVisibility(View.GONE);
                ll_projectNameMain.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);

            });
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
