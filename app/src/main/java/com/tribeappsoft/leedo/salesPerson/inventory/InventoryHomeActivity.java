package com.tribeappsoft.leedo.salesPerson.inventory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.BlocksModel;
import com.tribeappsoft.leedo.models.project.FlatUnitModel;
import com.tribeappsoft.leedo.models.project.FloorModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.ZoomLinearLayout;
import com.tribeappsoft.leedo.util.stickyScrollView.StickyScrollView;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class InventoryHomeActivity extends AppCompatActivity {
    /*Block Stats*/
   // @BindView(R.id.ll_Inventory_Stat) LinearLayoutCompat ll_Inventory_Stat;
    @BindView(R.id.ll_inventory_main) LinearLayoutCompat ll_main;
    @BindView(R.id.ll_inventory_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_inventory_pb) LinearLayoutCompat ll_inventory_pb;

    @BindView(R.id.hsv_inventory) HorizontalScrollView hsv_inventory;
    @BindView(R.id.tv_inventory_noWings) AppCompatTextView tv_noWings;
    @BindView(R.id.tv_inventory_blockName) AppCompatTextView tv_defBlockName;
    @BindView(R.id.ll_inventory_project_wings) LinearLayoutCompat ll_project_wings;
    @BindView(R.id.ll_InventorySystemActivity_blockStats) LinearLayoutCompat ll_blockStatsMain;
    @BindView(R.id.ll_InventorySystemActivity_total_wings) LinearLayoutCompat ll_total_wings;
    @BindView(R.id.zll_inventoryHome)
    ZoomLinearLayout zll_inventoryHome;

    @BindView(R.id.tv_inventoryHome_totalAllFlatsCount) AppCompatTextView tv_inventoryHome_allFlatsCount;
    @BindView(R.id.tv_inventory_total_floors) AppCompatTextView tv_total_floors;
    @BindView(R.id.tv_inventory_total_available) AppCompatTextView tv_total_available;
    @BindView(R.id.tv_inventory_total_sold) AppCompatTextView tv_total_sold;
    @BindView(R.id.tv_inventory_totalFlats) AppCompatTextView tv_totalFlats;
    //@BindView(R.id.tv_inventory_total_hold) AppCompatTextView tv_total_hold;
    @BindView(R.id.tv_inventory_total_reserved) AppCompatTextView tv_total_reserved;
   // @BindView(R.id.ll_inventory_floorsMain) LinearLayoutCompat ll_floorsMain;
    @BindView(R.id.ll_inventory_tableHeads) LinearLayoutCompat ll_tableHeads;
    @BindView(R.id.ll_inventory_tableRows) LinearLayoutCompat ll_tableRows;
    @BindView(R.id.ll_add_floors) LinearLayoutCompat ll_add_floors;
    @BindView(R.id.tv_inventory_noFloors) AppCompatTextView tv_noFloors;

    @BindView(R.id.ll_pb_dialogue) LinearLayoutCompat ll_pb_dialogue;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.sfl_salesPerson_inventory)ShimmerFrameLayout sfl;
    @BindView(R.id.ll_Inventory_Stat_sfl)LinearLayoutCompat ll_Inventory_Stat_sfl;
    @BindView(R.id.nsv_salesperson_inventory)
    StickyScrollView nsv_inventory;

    /*Project Name list*/
    @BindView(R.id.ll_InventorySystemActivity_projectNameMain) LinearLayoutCompat ll_projectNameMain;
    @BindView(R.id.tv_InventorySystemActivity_select_project) AutoCompleteTextView tv_select_project;


    private Activity context;
    private String TAG = "InventoryHomeActivity",api_token ="",selectedProjectName="";
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<String> projectStringArrayList;
    private int selectedProjectId=0,blockPosition=0;
    private boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_home);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context = InventoryHomeActivity.this;
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.inventory));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null)
        {
            //showLeads = getIntent().getBooleanExtra("showLeads", false);
            notify = getIntent().getBooleanExtra("notify", false);
        }


        ll_pb_dialogue.setVisibility(View.GONE);

        //initialise
        init();

        // private ProjectModel projectModel;
        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        //api token
        Log.e(TAG, "onCreate: api_token "+ sharedPreferences.getString("api_token", ""));

        nsv_inventory.setVisibility(View.INVISIBLE);
        ll_project_wings.setVisibility(View.INVISIBLE);
        // call get lead data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            hideProgressBar();
            showShimmer();

            //call get Inventory data
            getInventoryData();
        }
        else
        {
            //show network error
            Helper.NetworkError(context);
            //hide main layout
            ll_main.setVisibility(View.GONE);
            //hide nsv
            nsv_inventory.setVisibility(View.INVISIBLE);
            //visible no data
            ll_noData.setVisibility(View.VISIBLE);
        }



    }

    private void init() {

        projectModelArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();

        nsv_inventory.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            //scrolling up and down
            if(scrollY<oldScrollY){
                //vertical scrolling down
                Log.d(TAG, "onCreateView: scrolling down" );

                //show views
                //showViews();

            }else{

                //vertical scrolling upp
                Log.d(TAG, "onCreateView: scrolling up" );

                //hide views
                //hideViews();
            }

            //reached at top of scrollView
            if (!nsv_inventory.canScrollVertically(-1)) {

                Log.d(TAG, "onCreateView: TOP of scrollView" );
                // top of scroll view

                //showViews();
            }
        });

    }

    private void hideViews()
    {
        ll_project_wings.animate().translationY(-ll_project_wings.getHeight()).setInterpolator(new AccelerateInterpolator(3));
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_project_wings.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }


    private void getInventoryData()
    {
        //showProgressBar("loading inventory...");
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().GET_AllInventory(api_token);
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
                        setProjectsData();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    }
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null) {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {

                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                projectModelArrayList.clear();
                                                projectStringArrayList.clear();

                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setInventoryDetailsJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                            }
                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
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

    private void setProjectsData() {

        runOnUiThread(() -> {
            hideProgressBar();

            //set adapter for project names
            setAdapterForProjectName();

            //set wing by project
            setWingsByProject(0);

            hideShimmer();
            nsv_inventory.setVisibility(View.VISIBLE);
            ll_project_wings.setVisibility(View.VISIBLE);

            // zll inventoryHome
            zll_inventoryHome.setOnTouchListener((v, event) -> {
                zll_inventoryHome.init(InventoryHomeActivity.this);
                return false;
            });

        });
    }

    private void setAdapterForProjectName()
    {

        if (projectStringArrayList.size()>0 && projectModelArrayList.size()>0)
        {
            //ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, projectStringArrayList);
            tv_select_project.setText(projectStringArrayList.get(0));
            tv_select_project.setAdapter(adapter);
            tv_select_project.setThreshold(0);

            tv_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                //selectedPosition = position;
                for (ProjectModel pojo : projectModelArrayList)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {
                        selectedProjectId= pojo.getProject_id();
                        selectedProjectName = pojo.getProject_name();

                        Log.e(TAG, "Project Name & ID " + selectedProjectName +"\t"+ selectedProjectId);

                        //selectedPosition = position;
                        //set wing by project
                        ll_pb_dialogue.setVisibility(View.VISIBLE);
                        Handler handler=new Handler();
                        handler.postDelayed(() -> setWingsByProject(position),1000);

                        break;
                    }
                }
            });
        }
        else showErrorLog("Failed to get project details!");


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

    private void setInventoryDetailsJson(JsonObject jsonObject) {

        ProjectModel model =new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }

        if (jsonObject.has("project_blocks") && !jsonObject.get("project_blocks").isJsonNull())
        {
            if (!jsonObject.get("project_blocks").isJsonNull() && jsonObject.get("project_blocks").isJsonArray()) {
                ArrayList<BlocksModel> blocksModelArrayList = new ArrayList<>();
                blocksModelArrayList.clear();
                for (int j = 0; j < jsonObject.get("project_blocks").getAsJsonArray().size(); j++) {
                    setJsonBlock_all(jsonObject.get("project_blocks").getAsJsonArray(), j, blocksModelArrayList);
                }

                model.setBlocksModelArrayList(blocksModelArrayList);
            }

        }

        projectModelArrayList.add(model);

    }


    private void setJsonBlock_all(JsonArray project_blocks, int j, ArrayList<BlocksModel> blocksModelArrayList)
    {

        JsonObject jsonObject =  project_blocks.get(j).getAsJsonObject();
        BlocksModel model =new BlocksModel();

        if (jsonObject.has("block_id")) model.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );
        if (jsonObject.has("block_name")) model.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "0" );
        if (jsonObject.has("Total")) model.setTotal(!jsonObject.get("Total").isJsonNull() ? jsonObject.get("Total").getAsString() : "0" );
        if (jsonObject.has("Available")) model.setAvailable(!jsonObject.get("Available").isJsonNull() ? jsonObject.get("Available").getAsString() : "0" );
        if (jsonObject.has("Reserved")) model.setReserved(!jsonObject.get("Reserved").isJsonNull() ? jsonObject.get("Reserved").getAsString() : "0" );
        if (jsonObject.has("On Hold")) model.setOnHold(!jsonObject.get("On Hold").isJsonNull() ? jsonObject.get("On Hold").getAsString() : "0" );
        if (jsonObject.has("Sold")) model.setSold(!jsonObject.get("Sold").isJsonNull() ? jsonObject.get("Sold").getAsString() : "0" );

        if (jsonObject.has("project_floors") && !jsonObject.get("project_floors").isJsonNull())
        {
            if (!jsonObject.get("project_floors").isJsonNull() && jsonObject.get("project_floors").isJsonArray()) {
                ArrayList<FloorModel> floorModelArrayList = new ArrayList<>();
                floorModelArrayList.clear();
                for (int k = 0; k < jsonObject.get("project_floors").getAsJsonArray().size(); k++)
                {
                    setJsonFloor_all(jsonObject.get("project_floors").getAsJsonArray(), k, floorModelArrayList);
                }
                model.setFloorModelArrayList(floorModelArrayList);
            }
        }
        blocksModelArrayList.add(model);
    }

    private void setJsonFloor_all(JsonArray project_blocks, int j, ArrayList<FloorModel> floorModelArrayList)
    {

        JsonObject jsonObject =  project_blocks.get(j).getAsJsonObject();
        FloorModel model =new FloorModel();

        if (jsonObject.has("floor_id")) model.setFloor_id(!jsonObject.get("floor_id").isJsonNull() ? jsonObject.get("floor_id").getAsInt() : 0 );
        if (jsonObject.has("floor_name")) model.setFloor_name(!jsonObject.get("floor_name").isJsonNull() ? jsonObject.get("floor_name").getAsString() : "0" );
        if (jsonObject.has("Total")) model.setTotal(!jsonObject.get("Total").isJsonNull() ? jsonObject.get("Total").getAsInt() : 0 );


        if (!jsonObject.get("Available").isJsonNull() && jsonObject.get("Available").isJsonArray()) {
            ArrayList<FlatUnitModel> flatUnitModelArrayList = new ArrayList<>();
            flatUnitModelArrayList.clear();
            for (int k = 0; k < jsonObject.get("Available").getAsJsonArray().size(); k++) {
                setAvailFlatJson_all(jsonObject.get("Available").getAsJsonArray(), k, flatUnitModelArrayList);
            }
            model.setAvailFlatUnitModelArrayList(flatUnitModelArrayList);
        }

        if (!jsonObject.get("Reserved").isJsonNull() && jsonObject.get("Reserved").isJsonArray()) {
            ArrayList<FlatUnitModel> rflatUnitModelArrayList = new ArrayList<>();
            rflatUnitModelArrayList.clear();
            for (int k = 0; k < jsonObject.get("Reserved").getAsJsonArray().size(); k++) {
                setReservedFlatJson_all(jsonObject.get("Reserved").getAsJsonArray(), k, rflatUnitModelArrayList);
            }
            model.setReservedFlatUnitModelArrayList(rflatUnitModelArrayList);
        }

        if (!jsonObject.get("On Hold").isJsonNull() && jsonObject.get("On Hold").isJsonArray()) {
            ArrayList<FlatUnitModel> ohflatUnitModelArrayList = new ArrayList<>();
            ohflatUnitModelArrayList.clear();
            for (int k = 0; k < jsonObject.get("On Hold").getAsJsonArray().size(); k++) {
                setOnHoldFlatJson_all(jsonObject.get("On Hold").getAsJsonArray(), k, ohflatUnitModelArrayList);
            }
            model.setHoldFlatUnitModelArrayList(ohflatUnitModelArrayList);
        }

        if (!jsonObject.get("Sold").isJsonNull() && jsonObject.get("Sold").isJsonArray()) {
            ArrayList<FlatUnitModel> sflatUnitModelArrayList = new ArrayList<>();
            sflatUnitModelArrayList.clear();
            for (int k = 0; k < jsonObject.get("Sold").getAsJsonArray().size(); k++) {
                setSoldFlatJson_all(jsonObject.get("Sold").getAsJsonArray(), k, sflatUnitModelArrayList);
            }
            model.setSoldFlatUnitModelArrayList(sflatUnitModelArrayList);
        }

        floorModelArrayList.add(model);
    }

    private void setSoldFlatJson_all(JsonArray sold, int k, ArrayList<FlatUnitModel> sflatUnitModelArrayList) {


        JsonObject jsonObject =  sold.get(k).getAsJsonObject();
        FlatUnitModel model =new FlatUnitModel();

        if (jsonObject.has("unit_id")) model.setFlat_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setFlat_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "0" );

        sflatUnitModelArrayList.add(model);
    }

    private void setOnHoldFlatJson_all(JsonArray on_hold, int k, ArrayList<FlatUnitModel> ohflatUnitModelArrayList) {

        JsonObject jsonObject =  on_hold.get(k).getAsJsonObject();
        FlatUnitModel model =new FlatUnitModel();
        if (jsonObject.has("unit_id")) model.setFlat_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setFlat_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "0" );
        ohflatUnitModelArrayList.add(model);
    }

    private void setReservedFlatJson_all(JsonArray reserved, int k, ArrayList<FlatUnitModel> rflatUnitModelArrayList) {

        JsonObject jsonObject =  reserved.get(k).getAsJsonObject();
        FlatUnitModel model =new FlatUnitModel();
        if (jsonObject.has("unit_id")) model.setFlat_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setFlat_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "0" );
        rflatUnitModelArrayList.add(model);
    }

    private void setAvailFlatJson_all(JsonArray available, int k, ArrayList<FlatUnitModel> flatUnitModelArrayList) {

        JsonObject jsonObject =  available.get(k).getAsJsonObject();
        FlatUnitModel model =new FlatUnitModel();
        if (jsonObject.has("unit_id")) model.setFlat_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) model.setFlat_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "0" );
        flatUnitModelArrayList.add(model);
    }



    private void setWingsByProject(int position)
    {
        if (projectModelArrayList.size()>0)
        {
            position = position < projectModelArrayList.size() ? position : 0;
            Log.e(TAG, "setWingsByProject:_pos "+position );

            if (projectModelArrayList.get(position).getBlocksModelArrayList()!=null && projectModelArrayList.get(position).getBlocksModelArrayList().size()>0) {

                ll_total_wings.removeAllViews();
                for (int i =0 ; i< projectModelArrayList.get(position).getBlocksModelArrayList().size(); i++)
                {
                    View rowView_sub = getWingsView(i, position);
                    ll_total_wings.addView(rowView_sub);
                }

                //visible hsv wings
                hsv_inventory.setVisibility(View.VISIBLE);
                //visible inventory nsv
                nsv_inventory.setVisibility(View.VISIBLE);
                //hide no wings
                tv_noWings.setVisibility(View.GONE);
                //hide def block name
                tv_defBlockName.setVisibility(View.GONE);
            }
            else {

                //handle no wings here

                //hide dialog
                ll_pb_dialogue.setVisibility(View.GONE);

                //hide hsv wings
                hsv_inventory.setVisibility(View.GONE);
                //hide inventory nsv
                //nsv_inventory.setVisibility(View.GONE);
                //visible no wings
                tv_noWings.setVisibility(View.VISIBLE);
                //hide def block name
                tv_defBlockName.setVisibility(View.GONE);

                //hide add floors
                //ll_floorsMain.setVisibility(View.INVISIBLE);
                ll_tableHeads.setVisibility(View.INVISIBLE);
                ll_tableRows.setVisibility(View.INVISIBLE);

                //visible no floors
                //tv_noFloors.setVisibility(View.VISIBLE);
            }
        }
    }

    private View getWingsView(final int position, int selectedPosition)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_inventory_blocks, null );

        final AppCompatTextView tv_blockName = rowView_sub.findViewById(R.id.tv_inventoryBlock_blockName);
        final AppCompatTextView tv_blockTotalFlats_ = rowView_sub.findViewById(R.id.tv_inventoryBlock_totalFlats);
        //final LinearLayoutCompat ll_totalFlats = rowView_sub.findViewById(R.id.ll_inventoryBlock_totalFlats);
        //Todo size  error
        ArrayList<BlocksModel> blocksArrayList = projectModelArrayList.get(selectedPosition).getBlocksModelArrayList().size()>0 ? projectModelArrayList.get(selectedPosition).getBlocksModelArrayList() : new ArrayList<>();
        tv_blockName.setText(blocksArrayList.get(position).getBlock_name());

        //set block position
        blockPosition = blockPosition < blocksArrayList.size() ? blockPosition : 0;
        tv_blockTotalFlats_.setText(String.valueOf(blocksArrayList.get(blockPosition).getTotal()));

        tv_total_floors.setText(String.valueOf(blocksArrayList.get(blockPosition ).getFloorModelArrayList().size()));
        tv_inventoryHome_allFlatsCount.setText(String.valueOf(blocksArrayList.get(blockPosition).getTotal()));
        tv_total_available.setText(String.valueOf(blocksArrayList.get(blockPosition).getAvailable()));
        tv_total_sold.setText(String.valueOf(blocksArrayList.get(blockPosition).getSold()));
        tv_totalFlats.setText(String.valueOf(blocksArrayList.get(blockPosition).getTotal()));
        tv_total_reserved.setText(String.valueOf(blocksArrayList.get(blockPosition).getReserved()));

        //   final AppCompatTextView tv_kyc_doc_name = rowView_sub.findViewById(R.id.tv_kyc_doc_name);
        if (blockPosition == position) {
            //active
            tv_blockName.setBackground(getResources().getDrawable(R.drawable.ic_fill_rounded_circle));
            tv_blockName.setTextColor(getResources().getColor(R.color.white));
            //ll_totalFlats.setVisibility(View.VISIBLE);
        }
        else {

            //in-active
            tv_blockName.setBackground(getResources().getDrawable(R.drawable.ic_outline_round_circle));
            tv_blockName.setTextColor(getResources().getColor(R.color.black));
            //ll_totalFlats.setVisibility(View.GONE);
        }

        //set floors
        ArrayList<FloorModel> floorModelArrayList = blocksArrayList.get(blockPosition).getFloorModelArrayList();
        setFloorsByBlocks(floorModelArrayList, selectedPosition);

        //int finalI = position;
        tv_blockName.setOnClickListener(view ->
        {

            ll_inventory_pb.setVisibility(View.VISIBLE);
            ll_Inventory_Stat_sfl.setVisibility(View.VISIBLE);
            Timer buttonTimer = new Timer();
            buttonTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(() -> {
                        Log.e(TAG, "block finalI: "+position );
                        blockPosition = position;
                        // setTempData();
                        ll_inventory_pb.setVisibility(View.GONE);
                        setWingsByProject(selectedPosition);

                    });
                }
            }, 1000);

        });

        return rowView_sub;
    }

    private void setFloorsByBlocks(ArrayList<FloorModel> floorModelArrayList, int selectedPosition)
    {
        if (floorModelArrayList.size()>0)
        {
            ll_add_floors.removeAllViews();
            for (int i =0 ; i< floorModelArrayList.size(); i++) {
                View rowView_sub = getFloorView(i, selectedPosition);
                ll_add_floors.addView(rowView_sub);
            }

            //visible add floors
            ll_add_floors.setVisibility(View.VISIBLE);

            //visible add floors
            //ll_floorsMain.setVisibility(View.VISIBLE);
            ll_tableHeads.setVisibility(View.VISIBLE);
            ll_tableRows.setVisibility(View.VISIBLE);

            //hide no floors
            tv_noFloors.setVisibility(View.GONE);
        }
        else
        {
            //no floors handle case

            //hide dialog
            ll_pb_dialogue.setVisibility(View.GONE);
            //hide add floors
            ll_add_floors.setVisibility(View.GONE);
            //visible no floors
            tv_noFloors.setVisibility(View.VISIBLE);
        }
    }

    private View getFloorView(final int position, int selectedPosition)
    {

        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_inventory_home_stats_floors, null );

        final AppCompatTextView tv_inventory_total_floor_name = rowView_sub.findViewById(R.id.tv_inventory_total_floor_name);
        final AppCompatTextView tv_available_floor_Name = rowView_sub.findViewById(R.id.tv_available_floor_Name);
        final AppCompatTextView tv_sold_floor_Name = rowView_sub.findViewById(R.id.tv_sold_floor_Name);
        final AppCompatTextView tv_hold_floor_Name = rowView_sub.findViewById(R.id.tv_hold_floor_Name);
        final AppCompatTextView tv_total_floorName = rowView_sub.findViewById(R.id.tv_total_floorName);
        final AppCompatTextView tv_reserved_floor_Name = rowView_sub.findViewById(R.id.tv_reserved_floor_Name);
        final View view_line = rowView_sub.findViewById(R.id.view_line);

        ArrayList<FloorModel> floorModelArrayList = projectModelArrayList.get(selectedPosition).getBlocksModelArrayList().get(blockPosition).getFloorModelArrayList();
        //set floor name
        // if it is having only integer values then set floor name with its ordinal string eg. 1 -> 1st
        // otherwise set it as directly
        tv_inventory_total_floor_name.setText(floorModelArrayList.get(position).getFloor_name().matches("[0-9]+") ? Helper.getOrdinalStringFromDecimal(Integer.parseInt(floorModelArrayList.get(position).getFloor_name())) :  floorModelArrayList.get(position).getFloor_name());

        //set total flats
        tv_total_floorName.setText(String.valueOf(floorModelArrayList.get(position).getTotal()));

        ArrayList<String> availFlatArrayList=new ArrayList<>();
        ArrayList<String> soldFlatArrayList=new ArrayList<>();
        ArrayList<String> onHoldFlatArrayList=new ArrayList<>();
        ArrayList<String> reservedFlatArrayList=new ArrayList<>();

        //avail flats
        for(int i=0;i<floorModelArrayList.get(position).getAvailFlatUnitModelArrayList().size();i++) {
            availFlatArrayList.add(floorModelArrayList.get(position).getAvailFlatUnitModelArrayList().get(i).getFlat_name());
        }
        String availFlat = availFlatArrayList.size()>0 ? TextUtils.join(", ", availFlatArrayList) : "NA";
        tv_available_floor_Name.setText(availFlat);


        //sold flats
        for(int i=0;i<floorModelArrayList.get(position).getSoldFlatUnitModelArrayList().size();i++) {
            soldFlatArrayList.add(floorModelArrayList.get(position).getSoldFlatUnitModelArrayList().get(i).getFlat_name());
        }
        String soldFlat = soldFlatArrayList.size()>0 ? TextUtils.join(", ", soldFlatArrayList) : "NIL";
        tv_sold_floor_Name.setText(soldFlat);


        //onHold flats
        for(int i=0;i<floorModelArrayList.get(position).getHoldFlatUnitModelArrayList().size();i++) {
            onHoldFlatArrayList.add(floorModelArrayList.get(position).getHoldFlatUnitModelArrayList().get(i).getFlat_name());
        }
        String holdFlat =  onHoldFlatArrayList.size()>0 ? TextUtils.join(", ", onHoldFlatArrayList) : "NIL";
        tv_hold_floor_Name.setText(holdFlat);

        //        reserved flats
        for(int i=0;i<floorModelArrayList.get(position).getReservedFlatUnitModelArrayList().size();i++) {
            reservedFlatArrayList.add(floorModelArrayList.get(position).getReservedFlatUnitModelArrayList().get(i).getFlat_name());
        }
        String revFlat = reservedFlatArrayList.size()>0 ? TextUtils.join(", ", reservedFlatArrayList) : "NIL";
        tv_reserved_floor_Name.setText(revFlat);

        ll_Inventory_Stat_sfl.setVisibility(View.GONE);
        ll_pb_dialogue.setVisibility(View.GONE);

        if(position==floorModelArrayList.size()-1) {

            //Log.e("i:", "getTotalAllFloors: "+i );
            view_line.setVisibility(View.GONE);
        }

        return rowView_sub;
    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

//    void showProgressBar(String message) {
//        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
//        tv_loadingMsg.setText(message);
//        ll_pb.setVisibility(View.VISIBLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//    }


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            sfl.setVisibility(View.GONE);
            Helper.onErrorSnack(context, message);

            //hide main layout
            ll_main.setVisibility(View.GONE);
            //hide nsv
            nsv_inventory.setVisibility(View.INVISIBLE);
            //visible no data
            ll_noData.setVisibility(View.VISIBLE);
        });

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
    public void onBackPressed()
    {
        if(notify) {
            startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
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


}



  /*

    private void getTotalAllFloors(LinearLayoutCompat ll_add_floors)
    {

        //set blocks
        ll_add_floors.removeAllViews();
        if (inventoryWingsModel.getFloorBlockNamesModel().size()>0)
        {

            for (int i =0 ; i< inventoryWingsModel.getFloorBlockNamesModel().size(); i++)
            {
                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_inventory_home_stats_floors, null );
                final AppCompatTextView tv_inventory_total_floor_name = rowView_sub.findViewById(R.id.tv_inventory_total_floor_name);
                final AppCompatTextView tv_available_floor_Name = rowView_sub.findViewById(R.id.tv_available_floor_Name);
                final AppCompatTextView tv_sold_floor_Name = rowView_sub.findViewById(R.id.tv_sold_floor_Name);
                final AppCompatTextView tv_hold_floor_Name = rowView_sub.findViewById(R.id.tv_hold_floor_Name);
                final AppCompatTextView tv_reserved_floor_Name = rowView_sub.findViewById(R.id.tv_reserved_floor_Name);
                final View view_line = rowView_sub.findViewById(R.id.view_line);

                tv_inventory_total_floor_name.setText(inventoryWingsModel.getFloorBlockNamesModel().get(i).getFloorblocks());
                tv_available_floor_Name.setText(inventoryWingsModel.getFloorBlockNamesModel().get(i).getFloorFlats());
                tv_sold_floor_Name.setText(inventoryWingsModel.getFloorBlockNamesModel().get(i).getFloorFlats());
                tv_hold_floor_Name.setText(inventoryWingsModel.getFloorBlockNamesModel().get(i).getFloorFlats());
                tv_reserved_floor_Name.setText(inventoryWingsModel.getFloorBlockNamesModel().get(i).getFloorFlats());

                if(i==inventoryWingsModel.getFloorBlockNamesModel().size()-1)
                {
                    Log.e("i:", "getTotalAllFloors: "+i);
                    view_line.setVisibility(View.GONE);
                }


                ll_add_floors.addView(rowView_sub);
            }
        }

    }

    private void getTotalWings()
    {

        //Set Blocks
        ll_total_wings.removeAllViews();
        if (inventoryWingsModelArraylist.size()>0)
        {
            for (int i =0 ; i< inventoryWingsModelArraylist.size(); i++)
            {
                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_all_blocks_scrolls, null );
                final AppCompatTextView tv_Total_Inventory_block_name = rowView_sub.findViewById(R.id.tv_Total_Inventory_block_name);

                tv_Total_Inventory_block_name.setText(inventoryWingsModelArraylist.get(i).getWingName());


                Log.e(TAG, "selectedPosition: "+selectedPosition );

                if (selectedPosition ==i)
                {
                    //active
                    tv_Total_Inventory_block_name.setBackground(getResources().getDrawable(R.drawable.ic_fill_rounded_circle));
                    tv_Total_Inventory_block_name.setTextColor(getResources().getColor(R.color.white));
                }
                else
                {
                    //in-active
                    tv_Total_Inventory_block_name.setBackground(getResources().getDrawable(R.drawable.ic_outline_round_circle));
                    tv_Total_Inventory_block_name.setTextColor(getResources().getColor(R.color.black));
                }

                int finalI = i;
                tv_Total_Inventory_block_name.setOnClickListener(view ->
                {

                    ll_inventory_pb.setVisibility(View.VISIBLE);
                    Timer buttonTimer = new Timer();
                    buttonTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                Log.e(TAG, "finalI: "+finalI );
                                selectedPosition = finalI;
                                //setTempData();
                                getTotalWings();
                                ll_inventory_pb.setVisibility(View.GONE);
                            });
                        }
                    }, 1000);

                });


                ll_total_wings.addView(rowView_sub);
            }
        }

    }

     private void getTotalAllWings()
    {

        if (inventoryWingsModelArraylist.size()>0)
        {
            for (int i =0 ; i< inventoryWingsModelArraylist.size(); i++)
            {
                tv_total_floors.setText(inventoryWingsModelArraylist.get(i).getTotal_Floors());
                tv_total_available.setText(inventoryWingsModelArraylist.get(i).getTotal_available());
                tv_total_sold.setText(inventoryWingsModelArraylist.get(i).getTotal_sold());
                tv_total_hold.setText(inventoryWingsModelArraylist.get(i).getTotal_onhold());
                tv_total_reserved.setText(inventoryWingsModelArraylist.get(i).getTotal_reserved());

                getTotalAllFloors(ll_add_floors);

            }
        }
        else
        {
            ll_Inventory_Stat.setVisibility(View.GONE);
            ll_inventory_noData.setVisibility(View.VISIBLE);
        }

    }

   private void setTempData()
    {

        inventoryWingsModelArraylist.clear();
        blocksArrayList.clear();
        floorBlockNamesArrayList.clear();


        *//*Static Data for inventoryWingsModel *//*
        inventoryWingsModel = new InventoryWingsModel("A","20","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);
        inventoryWingsModel = new InventoryWingsModel("B","13","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);
        inventoryWingsModel = new InventoryWingsModel("C","13","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);
        inventoryWingsModel = new InventoryWingsModel("D","04","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);
        inventoryWingsModel = new InventoryWingsModel("E","04","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);
        inventoryWingsModel = new InventoryWingsModel("F","04","30","27","17","22","15");
        inventoryWingsModelArraylist.add(inventoryWingsModel);



        *//*Static Data for Blocks*//*

        floorBlockNamesModel=new FloorBlockNamesModel("1st","101,102,103");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("2nd","201,205,206,207,208,209");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("3rd","301,302,303");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("4th","401,402,403,405,406");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("5th","501,508,509");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("6th","601,602,605,607,608,609");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("7th","701,703,704");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("8th","801,802,805");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("9th","902,903,904");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("10th","1001,1002,1005");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("11th","1101,1102,1103");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("12th","1201,1203,1204");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("13th","1303,1305,1307");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("14th","1402,1404,1406");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

        floorBlockNamesModel=new FloorBlockNamesModel("15th","1501,1502,1503");
        floorBlockNamesArrayList.add(floorBlockNamesModel);
        inventoryWingsModel.setFloorBlockNamesModel(floorBlockNamesArrayList);

    }

    private void getProjectNameList()
    {
        //Set ProjectName
        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, stringList);
        tv_select_project.setText(stringList.get(0));
        tv_select_project.setAdapter(adapter);
        tv_select_project.setThreshold(0);


        tv_select_project.setOnItemClickListener((parent, view, position, id) ->
    {
        //int pos = position-1;
        String selectedId = stringList.get(position);
        //selectedCompanyName= itemArrayList.get(pos).getCompany_name();
        Log.e("Selected ",  "\t"+ selectedId);

    });


        tv_select_project.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d(TAG,"false");
            //selectedOtherEmpId=0;
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    });


}

    */