package com.tribeappsoft.leedo.admin.users;

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
import android.view.Menu;
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
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.admin.users.adapter.UserListAdapter;
import com.tribeappsoft.leedo.admin.users.model.UserModel;
import com.tribeappsoft.leedo.admin.users.model.UserRoleModel;
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

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class AllUsersActivity extends AppCompatActivity {

    private String TAG="AllUsersActivity";
    @BindView(R.id.sr_userList) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_userList) RecyclerView recyclerView;
    @BindView(R.id.ll_userList_noData) LinearLayoutCompat ll_noData;

    @BindView(R.id.ll_userList_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_userList_search) AppCompatEditText edt_search;
    @BindView(R.id.iv_userList_clearSearch) AppCompatImageView iv_clearSearch;

    @BindView(R.id.pb_userList) ContentLoadingProgressBar pb_userList;
    @BindView(R.id.exFab_userList_createProject) ExtendedFloatingActionButton exFab_createProject;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private UserListAdapter recyclerAdapter;
    private ArrayList<UserModel> userModelArrayList,temp_userModelArrayList;
    //private ArrayList<Integer> projectNameIdArrayList;
    //private ArrayList<Integer> AssignedRoleIdArrayList;
    private String api_token="";
    private Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id=0;
    private boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        ButterKnife.bind(this);
        context= AllUsersActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.menu_user_list));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+api_token +"user_id : "+user_id);
        editor.apply();

        if(getIntent()!=null)
        {
            notify=getIntent().getBooleanExtra("notify",false);
        }

        userModelArrayList =new ArrayList<>();
        temp_userModelArrayList =new ArrayList<>();
        //projectNameIdArrayList =new ArrayList<>();
        //AssignedRoleIdArrayList =new ArrayList<>();


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

        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            showPB();
            //  swipeRefresh.setRefreshing(true);
            new Handler(getMainLooper()).postDelayed(this::call_getAllUsersList, 100);
        }
        else {
            NetworkError(context);
        }

        exFab_createProject.setOnClickListener(v -> startActivity(new Intent(context, AddNewUserActivity.class)));

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
        recyclerAdapter = new UserListAdapter(context, userModelArrayList);
        recyclerView.setAdapter(recyclerAdapter);

    }
    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getAllUsersList();
            }
            else {
                NetworkError(context);
                swipeRefresh.setRefreshing(false);
                ll_noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
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
        //Get meetings data

        int  isUserCreateUpdate;
        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        isUserCreateUpdate = sharedPreferences.getInt("isUserCreateUpdate",0);
        editor.apply();

        if(isUserCreateUpdate == 1) {
            editor.remove("isUserCreateUpdate");
            editor.apply();

            if (isNetworkAvailable(Objects.requireNonNull(context))) {
                showPB();
                //  swipeRefresh.setRefreshing(true);
                new Handler(getMainLooper()).postDelayed(this::call_getAllUsersList, 100);
            }
            else {
                NetworkError(context);
            }
        }

        perform_search();

    }

    private void call_getAllUsersList()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllUsers(api_token,user_id);
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
                                                userModelArrayList.clear();
                                                temp_userModelArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                                temp_userModelArrayList.addAll(userModelArrayList);
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

    }//call_getAllUsersList

    private void setJson(JsonObject jsonObject)
    {
        UserModel model=new UserModel();

        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("username")) model.setUsername(!jsonObject.get("username").isJsonNull() ? jsonObject.get("username").getAsString() : "0" );
        if (jsonObject.has("api_token")) model.setApi_token(!jsonObject.get("api_token").isJsonNull() ? jsonObject.get("api_token").getAsString() : "0" );

        if (jsonObject.has("person_id")) model.setPerson_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("projects_assign_type")) model.setProjects_assign_type_id(!jsonObject.get("projects_assign_type").isJsonNull() ? jsonObject.get("projects_assign_type").getAsInt() : 0 );
        if (jsonObject.has("projects_assign_type")) model.setProjects_assign_type(!jsonObject.get("projects_assign_type").isJsonNull() ? jsonObject.get("projects_assign_type").getAsString() : "");
        if (jsonObject.has("prefix")) model.setPrefix(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "");

        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : "");
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString() : "");
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : "");
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("gender")) model.setGender(!jsonObject.get("gender").isJsonNull() ? jsonObject.get("gender").getAsString() : "");
        if (jsonObject.has("dob")) model.setDob(!jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setProfile_photo(!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : "");
        if (jsonObject.has("password")) model.setPwd(!jsonObject.get("password").isJsonNull() ? jsonObject.get("password").getAsString() : "");

        if (jsonObject.has("user_assigned_roles") && !jsonObject.get("user_assigned_roles").isJsonNull())
        {
            if (jsonObject.get("user_assigned_roles").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("user_assigned_roles").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<UserRoleModel> arrayList = new ArrayList<>();
                    ArrayList<Integer> assignRoleIdArrayList = new ArrayList<>();
                    arrayList.clear();
                    assignRoleIdArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setAssignedRolesJson(jsonArray.get(j).getAsJsonObject(), arrayList,assignRoleIdArrayList);
                    }
                    model.setUserRoleModelArrayList(arrayList);
                    model.setAssignedRolesArrayList(assignRoleIdArrayList);


                }
            }
        }


        if (jsonObject.has("user_tagged_project") && !jsonObject.get("user_tagged_project").isJsonNull())
        {
            if (jsonObject.get("user_tagged_project").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("user_tagged_project").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<ProjectModel> arrayList = new ArrayList<>();
                    ArrayList<Integer> integerArrayListList = new ArrayList<>();
                    arrayList.clear();
                    integerArrayListList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setAssignedProjectJson(jsonArray.get(j).getAsJsonObject(), arrayList,integerArrayListList);
                    }
                    model.setProjectModelArrayList(arrayList);
                    model.setAssignedProjectArrayList(integerArrayListList);

                }
            }
        }
        userModelArrayList.add(model);


    }

    private void setAssignedRolesJson(JsonObject jsonObject, ArrayList<UserRoleModel> arrayList, ArrayList<Integer> integerArrayListList)
    {
        UserRoleModel model = new UserRoleModel();
        if (jsonObject.has("role_id")){
            model.setRole_id(!jsonObject.get("role_id").isJsonNull() ? jsonObject.get("role_id").getAsInt() : 0 );
            integerArrayListList.add(!jsonObject.get("role_id").isJsonNull() ? jsonObject.get("role_id").getAsInt() : 0 );
            //AssignedRoleIdArrayList.add(!jsonObject.get("role_id").isJsonNull() ? jsonObject.get("role_id").getAsInt() : 0 );
        }
        if (jsonObject.has("role_name")) model.setRole_name(!jsonObject.get("role_name").isJsonNull() ? jsonObject.get("role_name").getAsString() : "" );
        arrayList.add(model);
        //  userModel.setUserRoleModel(model);

        //userModel.setUserRolesModel(model);
    }

    private void setAssignedProjectJson(JsonObject jsonObject, ArrayList<ProjectModel> arrayList, ArrayList<Integer> integerArrayListList)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")){
            model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
            integerArrayListList.add(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
            //AssignedRoleIdArrayList.add(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        }
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("is_project_assigned")) model.setIs_project_assigned(!jsonObject.get("is_project_assigned").isJsonNull() ? jsonObject.get("project_name").getAsInt() : 0 );
        arrayList.add(model);
        // userModel.setProjectModel(model);
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
                recyclerAdapter= new UserListAdapter(context, userModelArrayList);
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
                onErrorSnack(context, message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }

    private void showPB()
    {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(R.string.getting_user_list);
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
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    if (isNetworkAvailable(context))
                    {
                        String filterText = Objects.requireNonNull(edt_search.getText()).toString().trim();
                        // regex to match any number of spaces
                        filterText = filterText.trim().replaceAll("\\s+", " ");
                        Log.e(TAG, "perform_search: filterText "+filterText);
                     /*   if(filterText!=null && !filterText.trim().isEmpty()){

                        }*/
                        doFilter(filterText);

                    }
                    else NetworkError(Objects.requireNonNull(context));
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
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
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
        userModelArrayList.clear();

        if (query.length() == 0)
        {
            userModelArrayList.addAll(temp_userModelArrayList);
        }
        else
        {
            for (UserModel _obj: temp_userModelArrayList)
            {
                if (_obj.getUsername().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getFull_name().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getMobile_number().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getEmail().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getFirst_name().trim().toLowerCase(Locale.getDefault()).contains(query))
                {
                    userModelArrayList.add(_obj);
                }
            }
        }
        delayRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
        if (notify) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        super.onBackPressed();
    }
}