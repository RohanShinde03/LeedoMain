package com.tribeappsoft.leedo.admin.project_creation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.project_creation.adapter.ProjectListAdapter;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AllProjectActivity extends AppCompatActivity {

    private String TAG="AllProjectActivity";
    @BindView(R.id.sr_projectList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_projectList) RecyclerView recyclerView;
    @BindView(R.id.ll_projectList_noData) LinearLayoutCompat ll_noData;

    @BindView(R.id.ll_projectList_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_projectList_search) AppCompatEditText edt_search;
    @BindView(R.id.iv_projectList_clearSearch) AppCompatImageView iv_clearSearch;

    @BindView(R.id.pb_projectList) ContentLoadingProgressBar pb_projectList;
    @BindView(R.id.exFab_projectList_createProject) ExtendedFloatingActionButton exFab_createProject;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    private Activity context;

    private ProjectListAdapter recyclerAdapter;
    private ArrayList<ProjectModel> projectModelArrayList,temp_projectModelArrayList;
    private String api_token="";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_project);

        ButterKnife.bind(this);
        context=AllProjectActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.all_projects));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        Log.e(TAG, "onCreate: "+api_token);
        editor.apply();

        projectModelArrayList=new ArrayList<>();
        temp_projectModelArrayList=new ArrayList<>();


        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                hideViews();
                exFab_createProject.shrink();
            }

            @Override
            public void onShow() {
                showViews();
                exFab_createProject.extend();
            }
        });

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showPB("getting project list...");
                // swipeRefresh.setRefreshing(true);
                new Handler(getMainLooper()).postDelayed(this::call_getAllProjectList, 100);
        }
        else {
            Helper.NetworkError(context);
        }

        //exFab_createProject.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        exFab_createProject.setOnClickListener(v -> startActivity(new Intent(context, CreateProjectActivity.class)));

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
        recyclerAdapter = new  ProjectListAdapter(context, projectModelArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getAllProjectList();
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

        int  isProjectCreateUpdate;
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        isProjectCreateUpdate = sharedPreferences.getInt("isProjectCreateUpdate",0);
        editor.apply();

        if(isProjectCreateUpdate == 1) {
            editor.remove("isProjectCreateUpdate");
            editor.apply();

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                showPB("getting project list...");
              //  swipeRefresh.setRefreshing(true);
                new Handler(getMainLooper()).postDelayed(this::call_getAllProjectList, 100);
            }
            else {
                Helper.NetworkError(context);
            }
        }

        perform_search();

    }

    private void call_getAllProjectList()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getProjectList(api_token, user_id);
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
                                                projectModelArrayList.clear();
                                                temp_projectModelArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                                temp_projectModelArrayList.addAll(projectModelArrayList);

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

    }//call_getAllProjectList

    private void setJson(JsonObject jsonObject)
    {
        ProjectModel model=new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "0" );
        if (jsonObject.has("created_at")) model.setCreated_at(!jsonObject.get("created_at").isJsonNull() ? jsonObject.get("created_at").getAsString() : "" );
        if (jsonObject.has("updated_at")) model.setUpdated_at(!jsonObject.get("updated_at").isJsonNull() ? jsonObject.get("updated_at").getAsString() : "" );
        if (jsonObject.has("company_id")) model.setCompany_id(!jsonObject.get("company_id").isJsonNull() ? jsonObject.get("company_id").getAsInt() : 0 );
        if (jsonObject.has("project_type")) model.setProject_type(!jsonObject.get("project_type").isJsonNull() ? jsonObject.get("project_type").getAsString() : "" );
        if (jsonObject.has("address")) model.setAddress(!jsonObject.get("address").isJsonNull() ? jsonObject.get("address").getAsString() : "");
        if (jsonObject.has("description")) model.setDescription(!jsonObject.get("description").isJsonNull() ? jsonObject.get("description").getAsString() : "");
        if (jsonObject.has("latitude")) model.setLatitude(!jsonObject.get("latitude").isJsonNull() ? jsonObject.get("latitude").getAsString() : "");
        if (jsonObject.has("longitude")) model.setLongitude(!jsonObject.get("longitude").isJsonNull() ? jsonObject.get("longitude").getAsString() : "");
        if (jsonObject.has("reg_no")) model.setReg_no(!jsonObject.get("reg_no").isJsonNull() ? jsonObject.get("reg_no").getAsString() : "");
        if (jsonObject.has("cs_no")) model.setCs_no(!jsonObject.get("cs_no").isJsonNull() ? jsonObject.get("cs_no").getAsString() : "");
        if (jsonObject.has("permission_date")) model.setPermission_date(!jsonObject.get("permission_date").isJsonNull() ? jsonObject.get("permission_date").getAsString() : "");
        if (jsonObject.has("end_date")) model.setEnd_date(!jsonObject.get("end_date").isJsonNull() ? jsonObject.get("end_date").getAsString() : "");
        if (jsonObject.has("status_id")) model.setStatus_id(!jsonObject.get("status_id").isJsonNull() ? jsonObject.get("status_id").getAsInt() : 0);
        projectModelArrayList.add(model);

    }

    private void delayRefresh()
    {
        if (context!= null)
        {
            runOnUiThread(() -> {

                hidePB();
                swipeRefresh.setRefreshing(false);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter= new ProjectListAdapter(context, projectModelArrayList);
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

    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                // hideProgressBar();
                hidePB();
                swipeRefresh.setRefreshing(false);
                Helper.onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
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
                        String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                        // regex to match any number of spaces
                        filterText = filterText.trim().replaceAll("\\s+", " ");
                        Log.e(TAG, "perform_search: filterText "+filterText);
                     /*   if(filterText!=null && !filterText.trim().isEmpty()){

                        }*/
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
                   /* if(filterText!=null && !filterText.trim().isEmpty()) {

                    }*/
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
            //call_getHospitalModelList();
            //call_searchHospital("");
            doFilter("");
        });
    }


    private void doFilter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        projectModelArrayList.clear();

        if (query.length() == 0)
        {
            projectModelArrayList.addAll(temp_projectModelArrayList);
        }
        else
        {
            for (ProjectModel _obj: temp_projectModelArrayList)
            {
                if (_obj.getProject_name().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getProject_type().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getAddress().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getDescription().trim().toLowerCase(Locale.getDefault()).contains(query))
                {
                    projectModelArrayList.add(_obj);
                }
            }
        }
        delayRefresh();
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