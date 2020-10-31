package com.tribeappsoft.leedo.admin.project_quotations;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.admin.project_quotations.adapter.ProjectQuotationRecyclerAdapter;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
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

public class ProjectQuotationActivity extends AppCompatActivity {

    /*Project Name list*/
    @BindView(R.id.nsv_projectQuotation) NestedScrollView nsv;
    @BindView(R.id.ll_ProjectQuotationActivity_projectNameMain) LinearLayoutCompat ll_projectNameMain;
    @BindView(R.id.tv_ProjectQuotationActivity_select_project) AutoCompleteTextView tv_select_project;

    @BindView(R.id.swipeRefresh_ProjectQuotationActivity) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.recycler_ProjectQuotationActivity) RecyclerView recyclerView;
    @BindView(R.id.ll_ProjectQuotationActivity_noData) LinearLayoutCompat ll_noDataFound;
    @BindView(R.id.exFab_ProjectQuotationActivity_addQuotation) ExtendedFloatingActionButton exFab_addQuotation;


    private Activity context;
    private ProjectQuotationRecyclerAdapter recyclerAdapter;
    public ArrayList<ProjectModel> projectModelArraylist;
    private ArrayList<String> projectStringArrayList;

    private String TAG = "ProjectQuotationActivity";
    public SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public int user_id = 0,selectedProjectId=0, myPosition =0;
    public String selectedProjectName="", api_token ="";
    private int project_doc_type_id = 3;//project_docType_id : 1 =>Brochures, 2=>floor plan, 3=>Quotations
    private boolean isSalesHead=false, isAdmin = false, notify=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_quotation);
        //  overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context = ProjectQuotationActivity.this;

        if (getSupportActionBar()!=null)
        {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.quotation));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        projectModelArraylist = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        if(getIntent()!=null)
        {
            notify=getIntent().getBooleanExtra("notify",false);
        }

        exFab_addQuotation .setVisibility(isSalesHead || isAdmin ? View.VISIBLE : View.GONE);

        //setup recyclerView
        setupRecycleView();

        //set swipe refresh
        setSwipeRefresh();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //new Thread(this::getProjectListData).start();
            swipeRefresh.setRefreshing(true);
            //  projectModelArraylist.clear();
            new Thread(this::getProjectQuotations).start();
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

        //set fab visibility only for sales head option
        //exFab_addBrochure.setVisibility(isSalesHead ? View.VISIBLE : View.GONE);

        exFab_addQuotation.setOnClickListener(view -> startActivity(new Intent(context, AddNewQuotationActivity.class).putExtra("selectedProjectId",selectedProjectId).putExtra("selectedProjectName",selectedProjectName)));


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                //hideViews();
                exFab_addQuotation.shrink();
            }

            @Override
            public void onShow() {
                //showViews();
                exFab_addQuotation.extend();
            }
        });


        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
            View view = v.getChildAt(v.getChildCount() - 1);
            // Calculate the scroll_diff
            int diff = (view.getBottom() - (v.getHeight() + v.getScrollY()));
            // if diff is zero, then the bottom has been reached
            if (diff == 0)
            {
                // notify that we have reached the bottom
                Log.e(TAG, "MyScrollView: Bottom has been reached");
            }



            //scrolling up and down
            if(scrollY<oldScrollY){
                //vertical scrolling down
                Log.d(TAG, "onCreateView: scrolling down" );

                //show views
                exFab_addQuotation.extend();

            }else{

                //vertical scrolling upp
                Log.d(TAG, "onCreateView: scrolling up" );
                //hide views
                exFab_addQuotation.shrink();
            }

        });

    }



    @Override
    public void onResume()
    {
        super.onResume();

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        boolean IsBrochureAdded = sharedPreferences.getBoolean("IsBrochureAdded", false);
        editor.apply();

        exFab_addQuotation.setVisibility(isSalesHead || isAdmin ? View.VISIBLE : View.GONE);

        if(IsBrochureAdded)
        {
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                //new Thread(this::getProjectListData).start();
                swipeRefresh.setRefreshing(true);
                //call get api
                projectModelArraylist.clear();
                //set position to zero
                myPosition = 0;
                //call api
                getProjectQuotations();
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

            //update flag to false
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("IsBrochureAdded", false);
                editor.apply();
            }

        }

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
//        recyclerAdapter = new ProjectBrochuresRecyclerAdapter(context,projectModelArraylist.get(myPosition).getEventProjectDocsModelArrayList());
        // recyclerView.setAdapter(recyclerAdapter);
    }

   /* private void setupRecycleView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        //recyclerAdapter = new ProjectBrochuresRecyclerAdapter(context, projectModelArraylist.get(myPosition).getEventProjectDocsModelArrayList());
        //recyclerView.setAdapter(recyclerAdapter);
    }*/


    private void setSwipeRefresh()
    {

        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                swipeRefresh.setRefreshing(true);
                //call get api
                projectModelArraylist.clear();
                //set position to zero
                myPosition = 0;
                new Thread(this::getProjectQuotations).start();
            }
            else {
                Helper.NetworkError(context);
                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
            }

        });

        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //getOffline();

    }

    private void getProjectQuotations() {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getProjectDocs(api_token,project_doc_type_id,user_id);
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
                        setProjectQuotations();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
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


    private void setProjectQuotations()
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
        if (jsonObject.has("docs") && !jsonObject.get("docs").isJsonNull())
        {
            if (jsonObject.get("docs").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("docs").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<EventProjectDocsModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setProjectQuotationsJson(jsonArray.get(j).getAsJsonObject(), arrayList,projectModel);
                    }
                    projectModel.setEventProjectDocsModelArrayList(arrayList);
                }
            }
        }
        projectModelArraylist.add(projectModel);
    }


    private void setProjectQuotationsJson(JsonObject jsonObject, ArrayList<EventProjectDocsModel> arrayList, ProjectModel projectModel)
    {
        EventProjectDocsModel eventProjectDocsModel = new EventProjectDocsModel();

        if (jsonObject.has("project_doc_id")) eventProjectDocsModel.setDocId(!jsonObject.get("project_doc_id").isJsonNull() ? jsonObject.get("project_doc_id").getAsInt() : 0 );
        if (jsonObject.has("doc_title")) eventProjectDocsModel.setDocName(!jsonObject.get("doc_title").isJsonNull() ? jsonObject.get("doc_title").getAsString() : "" );
        if (jsonObject.has("project_id")) eventProjectDocsModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) eventProjectDocsModel.setProjectName(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        /* eventProjectDocsModel.setProject_id(projectModel.getProject_id());
        eventProjectDocsModel.setProjectName(projectModel.getProject_name()!=null && !projectModel.getProject_name().isEmpty()? projectModel.getProject_name() : "");*/
        Log.e(TAG, "setProjectBrochuresJson: "+eventProjectDocsModel.getProject_id() +"-"+eventProjectDocsModel.getProjectName());
        if (jsonObject.has("media_type_id")) eventProjectDocsModel.setMedia_type_id(!jsonObject.get("media_type_id").isJsonNull() ? jsonObject.get("media_type_id").getAsInt() : 0 );
        if (jsonObject.has("media_path")) eventProjectDocsModel.setDocPath(!jsonObject.get("media_path").isJsonNull() ? jsonObject.get("media_path").getAsString() : "" );
        if (jsonObject.has("media_Thumbnail")) eventProjectDocsModel.setDocThumbnail(!jsonObject.get("media_Thumbnail").isJsonNull() ? jsonObject.get("media_Thumbnail").getAsString() : "" );
        if (jsonObject.has("doc_description")) eventProjectDocsModel.setDocText(!jsonObject.get("doc_description").isJsonNull() ? jsonObject.get("doc_description").getAsString() : "" );
        if (jsonObject.has("doc_description")) eventProjectDocsModel.setBrochure_description(!jsonObject.get("doc_description").isJsonNull() ? jsonObject.get("doc_description").getAsString() : "" );
        if (jsonObject.has("date")) eventProjectDocsModel.setDate(!jsonObject.get("date").isJsonNull() ? jsonObject.get("date").getAsString() : "" );

        arrayList.add(eventProjectDocsModel);
    }


    private void setAdapterForProjectName()
    {
        if (projectStringArrayList.size()>0 && projectModelArraylist.size()>0)
        {
            //ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
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
                recyclerAdapter = new ProjectQuotationRecyclerAdapter(context, projectModelArraylist.get(myPosition).getEventProjectDocsModelArrayList());
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
            else
            {
                recyclerView.setVisibility(View.GONE);
                ll_noDataFound.setVisibility(View.VISIBLE);
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
        if(notify) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }

}