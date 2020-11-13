package com.tribeappsoft.leedo.admin.leadreassign;

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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leadreassign.adapter.SelectSalesPersonRecyclerAdapter;
import com.tribeappsoft.leedo.admin.leadreassign.model.SalesPersonModel;
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

public class SelectSalesPersonActivity extends AppCompatActivity {


    @BindView(R.id.sr_selectSalesPerson) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_selectSalesPerson) RecyclerView recyclerView;
    @BindView(R.id.ll_selectSalesPerson_searchBar) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_selectSalesPerson_search) TextInputEditText edt_search;
    @BindView(R.id.iv_selectSalesPerson_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.ll_selectSalesPerson_noData) LinearLayoutCompat ll_noData;


    private Activity context;
    private String TAG = "SelectSalesPersonActivity",api_token ="";
    private int from_or_to =1,user_id=0,repeat_sales_person_id=0; // TODO from -> 1 , to -> 2
    //private SelectSalesPersonRecyclerAdapter recyclerAdapter;
    private ArrayList<SalesPersonModel> itemArrayList, tempArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sales_person);
        ButterKnife.bind(this);
        context = SelectSalesPersonActivity.this;
        if (getIntent()!=null){
            repeat_sales_person_id =  getIntent().getIntExtra("repeat_sales_person_id",0);
            from_or_to =  getIntent().getIntExtra("from_or_to",1);
            Log.e(TAG, "onCreate: from_or_to "+ from_or_to );
        }


        if (getSupportActionBar()!=null) {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(from_or_to ==1 ?  getString(R.string.menu_from_sales_person) :  getString(R.string.menu_to_sales_person));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        //init
        init();

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

        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getSalesExecutives();
        }
        else {
            Helper.NetworkError(context);
        }


    }


    private void init()
    {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //user_id = sharedPreferences.getInt("user_id", 0);

        itemArrayList = new ArrayList<>();
        tempArrayList = new ArrayList<>();

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
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
        if (verticalDivider != null) {
            dividerItemDecoration.setDrawable(verticalDivider);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(new SelectSalesPersonRecyclerAdapter(context, itemArrayList, model -> {

            if (from_or_to == 1) {

                //select from Sales person

                setResult(Activity.RESULT_OK, new Intent()
                        .putExtra("result", model)
                        .putExtra("from_or_to",from_or_to)
                );

                Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                onBackPressed();

               /* startActivity(new Intent(context, SiteVisitActivity.class)
                        .putExtra("salesPersonModel",model)
                        .putExtra("from_or_to",from_or_to)
                );
                finish();*/
            }
            else if (from_or_to ==2) {

                setResult(Activity.RESULT_OK, new Intent()
                        .putExtra("result", model)
                        .putExtra("from_or_to",from_or_to)
                );

                Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                onBackPressed();


                //select to Sales person
               /* startActivity(new Intent(context, GenerateTokenActivity.class)
                        .putExtra("salesPersonModel",model)
                        .putExtra("from_or_to",from_or_to)
                );
                finish();*/
            }
        }));
    }
    private void setSwipeRefresh()
    {
        swipeRefresh.setOnRefreshListener(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                //call api
                swipeRefresh.setRefreshing(true);
                call_getSalesExecutives();
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


    private void call_getSalesExecutives()
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
                                                //tempArrayList.addAll(salesPersonModelArrayList);

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
        SalesPersonModel model=new SalesPersonModel();

        if (jsonObject.has("user_id")) model.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("person_id")) model.setUser_member_id(!jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );


        //add user only iff user id is not repeated
        if(model.getUser_id()!= repeat_sales_person_id) {
            itemArrayList.add(model);
        }

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
                recyclerView.setAdapter(new SelectSalesPersonRecyclerAdapter(context, itemArrayList, model -> {

                    if (from_or_to == 1) {
                        /*//select from Sales person
                        startActivity(new Intent(context, SiteVisitActivity.class)
                                .putExtra("salesPersonModel",model)
                                .putExtra("from_or_to",from_or_to)
                        );
                        finish();*/

                        setResult(Activity.RESULT_OK, new Intent()
                                .putExtra("result", model)
                                .putExtra("from_or_to",from_or_to)
                        );

                        Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                        onBackPressed();

                    }
                    else if (from_or_to ==2) {

                      /*  //select to Sales person
                        startActivity(new Intent(context, GenerateTokenActivity.class)
                                .putExtra("salesPersonModel",model)
                                .putExtra("from_or_to",from_or_to)
                        );
                        finish();*/

                        setResult(Activity.RESULT_OK, new Intent()
                                .putExtra("result", model)
                                .putExtra("from_or_to",from_or_to)
                        );

                        Helper.hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                        onBackPressed();

                    }
                }));
                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();

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
            for (SalesPersonModel _obj: tempArrayList)
            {
                if (_obj.getFull_name().trim().toLowerCase(Locale.getDefault()).contains(query)
                        ||_obj.getMobile_number().trim().toLowerCase(Locale.getDefault()).contains(query))
                {
                    itemArrayList.add(_obj);
                }
            }
        }
        delayRefresh();
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
