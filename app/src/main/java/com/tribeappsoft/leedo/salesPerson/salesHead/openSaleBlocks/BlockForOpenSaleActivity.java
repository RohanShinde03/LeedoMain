package com.tribeappsoft.leedo.salesPerson.salesHead.openSaleBlocks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.BlocksModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.openSaleBlocks.adapter.OpenBlocksRecyclerAdapter;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.lang.reflect.Field;
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

public class BlockForOpenSaleActivity extends AppCompatActivity {

    @BindView(R.id.sr_blockForOpenSale) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_blockForOpenSale) RecyclerView recyclerView;
    @BindView(R.id.ll_blockForOpenSale_search) LinearLayoutCompat ll_search;
    @BindView(R.id.edt_blockForOpenSale_search) TextInputEditText edt_search;
    @BindView(R.id.iv_blockForOpenSale_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.ll_blockForOpenSale_noData) LinearLayoutCompat ll_noData;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private Activity context;
    private ArrayList<ProjectModel> itemArrayList, tempArrayList;
    private OpenBlocksRecyclerAdapter recyclerAdapter;
    private String TAG = "BlockForOpenSaleActivity", api_token ="";
    private int user_id = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_for_open_sale);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context= BlockForOpenSaleActivity.this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+ getString(R.string.menu_blocks_for_open_sale) + "</font>"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        Log.e(TAG, "onCreate: "+ api_token + "\n User id"+ user_id);
        editor.apply();

        //init ArrayList
        itemArrayList = new ArrayList<>();
        tempArrayList = new ArrayList<>();

        //setup recyclerView
        setUpRecyclerView();

        //hide pb
        hideProgressBar();

        //set up swipe refresh
        setSwipeRefresh();


        //Get meetings data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            swipeRefresh.setRefreshing(true);
            call_getAllProjectWiseBlocks();
        }
        else {
            Helper.NetworkError(context);
            //hide main layouts
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            //visible no data
            ll_noData.setVisibility(View.VISIBLE);

        }


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


    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new OpenBlocksRecyclerAdapter(this, itemArrayList);
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
                call_getAllProjectWiseBlocks();
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


    private void call_getAllProjectWiseBlocks()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllProjectWithBlocks(api_token);
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
                .subscribe(new Subscriber<Response<JsonObject>>()
                {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        delayRefresh();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful() && JsonObjectResponse.body()!=null)
                        {
                            if(!JsonObjectResponse.body().isJsonNull()) {
                                int isSuccess = 0;
                                if (JsonObjectResponse.body().has("success"))
                                    isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                if (isSuccess == 1) {
                                    if (JsonObjectResponse.body().has("data")) {
                                        if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                            JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                            //clear list
                                            itemArrayList.clear();
                                            tempArrayList.clear();
                                            for (int i = 0; i < jsonArray.size(); i++) {
                                                setProjectNamesJson(jsonArray.get(i).getAsJsonObject());
                                            }
                                            tempArrayList.addAll(itemArrayList);
                                        }
                                    } else
                                        showErrorLog(getString(R.string.something_went_wrong_try_again));
                                } else
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                            }
                        }
                        else {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
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
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );

        if (jsonObject.has("project_blocks")) {
            if (!jsonObject.get("project_blocks").isJsonNull() && jsonObject.get("project_blocks").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("project_blocks").getAsJsonArray();
                if (jsonArray.size()>0) {
                    ArrayList<BlocksModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setProjectBlocksJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setBlocksModelArrayList(arrayList);
                }
            }
        }

        itemArrayList.add(model);
    }

    private void setProjectBlocksJson(JsonObject jsonObject, ArrayList<BlocksModel> arrayList)
    {

        BlocksModel myModel = new BlocksModel();
        if (jsonObject.has("block_id")) myModel.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );
        if (jsonObject.has("block_name")) myModel.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "" );
        if (jsonObject.has("is_ofs")) myModel.setIsOpenForSale(!jsonObject.get("is_ofs").isJsonNull() ? jsonObject.get("is_ofs").getAsInt() : 0 );
        if (jsonObject.has("total_units")) myModel.setTotal_units_count(!jsonObject.get("total_units").isJsonNull() ? jsonObject.get("total_units").getAsInt() : 0 );

        arrayList.add(myModel);
    }

    //DelayRefresh
    public  void delayRefresh()
    {
        //if (context != null)
        {

            runOnUiThread(() ->
            {
                //hide pb
                swipeRefresh.setRefreshing(false);
                //hideProgressBar();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new OpenBlocksRecyclerAdapter(this, itemArrayList);
                recyclerView.setAdapter(recyclerAdapter);

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                Log.e(TAG, "count: "+count );

                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.GONE);
                } else {

                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });

        }
    }



    public void showAlertDialog(ArrayList<Integer> blockIdsArrayList, ArrayList<Integer> removedBlockIdsArrayList, int project_id)
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

        tv_msg.setText(getString(R.string.que_open_block_for_sustenance_sale));
        //  String str = TextUtils.join(", ", recyclerAdapter.getSalesUserNames());
        tv_desc.setText(getString(R.string.msg_open_block_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context)) {

                //check added block id's arrayList
                if (blockIdsArrayList!=null && blockIdsArrayList.size()>0) {
                    Log.e(TAG, "Added Blocks Array: "+ Arrays.toString(blockIdsArrayList.toArray()));
                }
                else Log.e(TAG, "Added Blocks Array: null" );

                //check removed block id's arrayList
                if (removedBlockIdsArrayList!=null && removedBlockIdsArrayList.size()>0) {
                    Log.e(TAG, "Removed Blocks Array: "+ Arrays.toString(removedBlockIdsArrayList.toArray()));
                }
                else Log.e(TAG, "Removed Blocks Array: null" );


                //api_call
                showProgressBar(getString(R.string.updating_blocks_for_open_sale));
                call_postAddRemoveBlocks(project_id);

            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmLp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmLp.gravity =  Gravity.CENTER;
        wmLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmLp.width = pixel-100;
        //wmLp.x = 100;   //x position
        //wmLp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmLp.height );
        alertDialog.getWindow().setAttributes(wmLp);
    }

    private void call_postAddRemoveBlocks(int project_id)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",user_id);
        jsonObject.addProperty("project_id",project_id);
        jsonObject.add("addedBlockIDs", new Gson().toJsonTree(recyclerAdapter.getAddedBlockIdsArrayList()).getAsJsonArray());
        jsonObject.add("removedBlockIDs", new Gson().toJsonTree(recyclerAdapter.getRemovedBlockIdsArrayList()).getAsJsonArray());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().addRemoveBlocks(jsonObject).enqueue(new Callback<JsonObject>()
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
                                onSuccessUpdateBlocks();
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


    private void onSuccessUpdateBlocks()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show success msg
            new Helper().showSuccessCustomToast(context, getString(R.string.blocks_updated_successfully));

            //do refresh api

            //set swipe refreshing to true
            swipeRefresh.setRefreshing(true);
            //reset api call
            call_getAllProjectWiseBlocks();
        });
    }



    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            context.runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);

                hideProgressBar();

                Helper.onErrorSnack(context,message);
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_self, menu);

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search_self);
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(context).getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null)
        {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(true);  //false -- to open searchView by default
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint(getString(R.string.search));

            //Code for changing the search icon
            ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            icon.setColorFilter(Color.WHITE);
            //icon.setImageResource(R.drawable.ic_home_search);

            //AutoCompleteTextView searchTextView =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            AutoCompleteTextView searchTextView =  searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            /// Code for changing the textColor and hint color for the search view
            searchTextView.setHintTextColor(getResources().getColor(R.color.main_white));
            searchTextView.setTextColor(getResources().getColor(R.color.main_white));

            //Code for changing the voice search icon
            //ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            //voiceIcon.setImageResource(R.drawable.my_voice_search_icon);

            //Code for changing the close search icon
            ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            closeIcon.setColorFilter(Color.WHITE);
//            closeIcon.setImageResource(R.drawable.ic_search_close_icon);

            /*closeIcon.setOnClickListener(view -> {

                searchTextView.setText("");
                //clear search text reset all
                doFilter("");
            });*/

            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.set(searchTextView, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
            } catch (Exception e) {
                e.printStackTrace();
            }


            //searchView.setOnQueryTextListener(FragmentVisitors.this);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    doFilter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(newText.trim().isEmpty())
                        doFilter(newText);
                    return false;
                }
            });


            searchView.setOnCloseListener(() -> {
                Log.e(TAG, "onCreateOptionsMenu: onClose ");
                //doFilter("");
                //resetApiCall();
                return false;
            });
        }
        if (searchView != null)
        {
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
            }
        }
        return true;
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
            for (ProjectModel _obj: tempArrayList)
            {
                if (_obj.getProject_name().trim().toLowerCase(Locale.getDefault()).contains(query) )
                        //||_obj.get().trim().toLowerCase(Locale.getDefault()).contains(query))
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
