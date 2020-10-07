package com.tribeappsoft.leedo.salesPerson.bookings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.BlocksModel;
import com.tribeappsoft.leedo.models.project.FlatUnitModel;
import com.tribeappsoft.leedo.models.project.FloorModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.FlowLayout;
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

public class AddHoldFlatActivity extends AppCompatActivity {


    //    @BindView(R.id.mBtn_holdFlat_view_inventory) MaterialButton mBtn_view_inventory;
    @BindView(R.id.acTv_addHoldFlat_select_project) AutoCompleteTextView acTV_select_project;
    @BindView(R.id.acTv_addHoldFlat_selectBlock) AutoCompleteTextView acTv_selectBlock;
    @BindView(R.id.ll_addHoldFlat_inventorySelection) LinearLayoutCompat ll_inventorySelection;

    @BindView(R.id.ll_addHoldFlat_viewFloorMain) LinearLayoutCompat ll_viewFloorMain;
    @BindView(R.id.ll_addHoldFlat_addFloor) LinearLayoutCompat ll_addFloor;
    @BindView(R.id.ll_addHoldFlat_proceed_layout)  LinearLayoutCompat ll_proceed_layout;
    @BindView(R.id.tv_addHoldFlat_selectedFlat) AppCompatTextView tv_selectedFlat;
    @BindView(R.id.iv_addHoldFlat_closeProceed) AppCompatImageView iv_closeProceed;
    @BindView(R.id.mBtn_addHoldFlat_proceed) MaterialButton btn_proceed;

    @BindView(R.id.ll_addHoldFlat_legends) LinearLayoutCompat ll_legends;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    @BindView(R.id.sfl_salesPerson_flatOnHold)ShimmerFrameLayout sfl;
    @BindView(R.id.nsv_addHoldFlat)NestedScrollView nev_AddflatOnHold;

    private Activity context;
    private String TAG = "AddHoldFlatActivity", api_token ="", selectedProjectName="",
            selectedBlockName ="", event_title ="";
    private int selectedProjectId =0, selectedBlockId =0, event_id =1;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<String> projectNamesArrayList, blockNamesArrayList;
    private ArrayList<FloorModel> floorModelArrayList;
    private ArrayList<Integer> catIntegerArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hold_flat);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );

        ButterKnife.bind(this);
        context = AddHoldFlatActivity.this;

        if (getSupportActionBar()!=null)
        {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.select_flat_on_hold));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 1);
            event_title = getIntent().getStringExtra("event_title");
        }

        //hide pb
        hideProgressBar();

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        projectModelArrayList = new ArrayList<>();
        projectNamesArrayList = new ArrayList<>();
        blockNamesArrayList = new ArrayList<>();
        floorModelArrayList = new ArrayList<>();
        catIntegerArrayList = new ArrayList<>();

        //Set temp Data
        //setTempData();
        //getBlockDynamicList();

        nev_AddflatOnHold.setVisibility(View.GONE);
        //hide proceed layout
        ll_proceed_layout.setVisibility(View.GONE);

        // call get lead data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            //showProgressBar("Please wait...");
            hideProgressBar();
            showShimmer();
            new Handler().postDelayed(this::getHoldFlatData, 1000);
        }
        else Helper.NetworkError(context);

    }


    private void getHoldFlatData()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllProjectWithBlocks(api_token, event_id);
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
                    public void onCompleted()
                    {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        setDetails();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {

                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray())
                                            {

                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                //clear list
                                                projectModelArrayList.clear();
                                                projectNamesArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++)
                                                {
                                                    setProjectNamesJson(jsonArray.get(i).getAsJsonObject());
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




    private void setProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectNamesArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }

        if (jsonObject.has("project_blocks"))
        {
            if (!jsonObject.get("project_blocks").isJsonNull() && jsonObject.get("project_blocks").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("project_blocks").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<BlocksModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    blockNamesArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setProjectBlocksJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setBlocksModelArrayList(arrayList);
                }
            }
        }

        projectModelArrayList.add(model);
    }

    private void setProjectBlocksJson(JsonObject jsonObject, ArrayList<BlocksModel> arrayList)
    {

        BlocksModel myModel = new BlocksModel();
        if (jsonObject.has("block_id")) myModel.setBlock_id(!jsonObject.get("block_id").isJsonNull() ? jsonObject.get("block_id").getAsInt() : 0 );
        if (jsonObject.has("block_name")) myModel.setBlock_name(!jsonObject.get("block_name").isJsonNull() ? jsonObject.get("block_name").getAsString() : "" );

        arrayList.add(myModel);
    }


    private void setDetails()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            hideShimmer();
            nev_AddflatOnHold.setVisibility(View.VISIBLE);
            //visible legends
            ll_legends.setVisibility(View.VISIBLE);

            //set adapter for project names
            setAdapterProjectNames();
        });
    }


    private void setAdapterProjectNames()
    {

        if(projectNamesArrayList.size()>0 && projectModelArrayList.size()>0)
        {

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, projectNamesArrayList);
            //set def first selected
            acTV_select_project.setText(projectNamesArrayList.get(0));
            selectedProjectId = projectModelArrayList.get(0).getProject_id(); // This is the correct ID
            selectedProjectName = projectModelArrayList.get(0).getProject_name();
            //set adapter
            acTV_select_project.setAdapter(adapter);
            acTV_select_project.setThreshold(0);
            //tv_selectCustomer.setSelection(0);

            //set def. first block selected from first project
            setAdapterBlockNames(projectModelArrayList.get(0).getBlocksModelArrayList());

            acTV_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                for (ProjectModel pojo : projectModelArrayList)
                {
                    if (pojo.getProject_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedProjectId = pojo.getProject_id(); // This is the correct ID
                        selectedProjectName = pojo.getProject_name();

                        //selectedCustomerModel = pojo;

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Project name & id " + selectedProjectName +"\t"+ selectedProjectId);

                        //set clear autoComplete textView
                        acTv_selectBlock.setText("");
                        //clear assignments
                        selectedBlockId = 0;
                        selectedBlockName = "";

                        //set adapter for unit categories
                        setAdapterBlockNames(projectModelArrayList.get(position).getBlocksModelArrayList());

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }


    private void setAdapterBlockNames(ArrayList<BlocksModel> blocksModelArrayList)
    {

        if (blocksModelArrayList!=null && blocksModelArrayList.size()>0)
        {
            //adding unit categories
            blockNamesArrayList.clear();
            for (int i =0; i<blocksModelArrayList.size(); i++)
            {
                blockNamesArrayList.add(blocksModelArrayList.get(i).getBlock_name());
            }

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, blockNamesArrayList);
            //set def. first block selected
            acTv_selectBlock.setText(blockNamesArrayList.get(0));
            selectedBlockId = blocksModelArrayList.get(0).getBlock_id();
            selectedBlockName = blocksModelArrayList.get(0).getBlock_name();
            acTv_selectBlock.setAdapter(adapter);
            acTv_selectBlock.setThreshold(0);

            //call api for
            //showProgressBar("Loading inventory...");
            new Thread(this::call_getAllInventoryData).start();
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectBlock.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (BlocksModel pojo : blocksModelArrayList)
                {
                    if (pojo.getBlock_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedBlockId = pojo.getBlock_id(); // This is the correct ID
                        selectedBlockName = pojo.getBlock_name();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Selected Block & id " + selectedBlockName +"\t"+ selectedBlockId);

                        //hide proceed layout if visible
                        new Animations().slideOutBottom(ll_proceed_layout);
                        ll_proceed_layout.setVisibility(View.GONE);

                        //showProgressBar("");
                        new Thread(this::call_getAllInventoryData).start();

                        break; // No need to keep looping once you found it.
                    }
                }

            });

        }
        else
        {
            //empty array
            new Helper().showCustomToast(context, "Flat types are empty!");
        }

    }


    private void call_getAllInventoryData()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllInventoryWithBlocks(api_token, selectedBlockId);
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
                    public void onCompleted()
                    {
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        setInventoryDetails();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

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
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {
                                    int isSuccess = 0;if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {

                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray())
                                            {

                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                //clear list
                                                floorModelArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++)
                                                {
                                                    setFloorsJson(jsonArray.get(i).getAsJsonObject());
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


    private void setFloorsJson(JsonObject jsonObject)
    {
        FloorModel model = new FloorModel();
        if (jsonObject.has("floor_id")) model.setFloor_id(!jsonObject.get("floor_id").isJsonNull() ? jsonObject.get("floor_id").getAsInt() : 0 );
        if (jsonObject.has("floor_name")) model.setFloor_name(!jsonObject.get("floor_name").isJsonNull() ? jsonObject.get("floor_name").getAsString() : "0" );

        if (jsonObject.has("project_units"))
        {
            if (!jsonObject.get("project_units").isJsonNull() && jsonObject.get("project_units").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("project_units").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<FlatUnitModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setUnitJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setFlatUnitModelArrayList(arrayList);
                }
            }
        }

        floorModelArrayList.add(model);
    }


    private void setUnitJson(JsonObject jsonObject, ArrayList<FlatUnitModel> arrayList)
    {

        FlatUnitModel myModel = new FlatUnitModel();
        if (jsonObject.has("unit_id")) myModel.setFlat_id(!jsonObject.get("unit_id").isJsonNull() ? jsonObject.get("unit_id").getAsInt() : 0 );
        if (jsonObject.has("unit_name")) myModel.setFlat_name(!jsonObject.get("unit_name").isJsonNull() ? jsonObject.get("unit_name").getAsString() : "" );
        if (jsonObject.has("unit_category")) myModel.setFlat_type(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        if (jsonObject.has("inventory_status_id")) myModel.setInventory_status_id(!jsonObject.get("inventory_status_id").isJsonNull() ? jsonObject.get("inventory_status_id").getAsInt() : 0 );

        arrayList.add(myModel);
    }

    private void setInventoryDetails()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            //clear arrayList
            catIntegerArrayList.clear();

            if (floorModelArrayList!=null && floorModelArrayList.size()>0) setFloorFlatView();
            else showErrorLog("No units are available for hold!");


            //set visibility
            ll_viewFloorMain.setVisibility(floorModelArrayList!=null && floorModelArrayList.size()>0 ? View.VISIBLE : View.GONE);

            btn_proceed.setOnClickListener(view -> {

                if (getSubCatIds().size()>0)
                {
                    //proceed
                    startActivity(new Intent(context, HoldScanInActivity.class)
                            .putExtra("event_id", event_id)
                            .putExtra("event_title", event_title)
                            .putExtra("unit_id", getSubCatIds().get(0))
                            .putExtra("unit_name", tv_selectedFlat.getText().toString())
                    );
                }
                else new Helper().showCustomToast(context, "please select flat number!");

            });

            iv_closeProceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //clear arrayList
                    catIntegerArrayList.clear();

                    //clear selection
                    setFloorFlatView();

                    //hide layout
                    new Animations().slideOutBottom(ll_proceed_layout);
                    ll_proceed_layout.setVisibility(View.GONE);

                }
            });
        });
    }


    private void setFloorFlatView()
    {
        ll_addFloor.removeAllViews();
        for (int i =0 ; i< floorModelArrayList.size(); i++)
        {
            View rowView_sub = getFloorFlatView(i);
            ll_addFloor.addView(rowView_sub);
        }
    }



    private View getFloorFlatView(final int position)
    {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_floors_view, null );

        final LinearLayoutCompat ll_itemFloors_main = rowView.findViewById(R.id.ll_itemFloors_main);
        final AppCompatTextView tv_floorName = rowView.findViewById(R.id.tv_itemFloor_floorName);
        final AppCompatTextView tv_flatCount = rowView.findViewById(R.id.tv_itemFloor_flatCount);
        final FlowLayout flowLayout = rowView.findViewById(R.id.fl_itemFloor_addFlats);

        final FloorModel myModel = floorModelArrayList.get(position);
        //set floor name
        tv_floorName.setText(String.format("%s - ", myModel.getFloor_name()));
        //set flats count
        tv_flatCount.setText(myModel.getFlatUnitModelArrayList()!=null ? myModel.getFlatUnitModelArrayList().size() +" Units"  : "0 Units");

        final ArrayList<FlatUnitModel> arrayList = myModel.getFlatUnitModelArrayList();
        if (arrayList!=null && arrayList.size()>0)
        {
            flowLayout.removeAllViews();
            for (int i=0; i<arrayList.size(); i++)
            {

                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_flat_number_view, null );
                //final Chip chip = rowView_sub.findViewById(R.id.chip_leadCampaignDetail);
                final AppCompatTextView tv_FlatNumber = rowView_sub.findViewById(R.id.tv_itemFlatNumber);
                tv_FlatNumber.setText(arrayList.get(i).getFlat_name());


                switch (arrayList.get(i).getInventory_status_id())
                {
                    case 1:
                        //Available

                        /*if (holdPosition == i)
                        {
                            //set selected
                            tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_available,context.getTheme()));
                            tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        }
                        else
                        {
                            //set de-selected
                            tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_inventory_slot_rectangle,context.getTheme()));
                            tv_FlatNumber.setTextColor(getResources().getColor(R.color.color_flat_available));
                        }*/

                        if (myModel.getFlatUnitModelArrayList().get(i).getIsSelected()==1)
                        {
                            //show already selected

                            tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_available,context.getTheme()));
                            tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        }
                        else
                        {
                            //show already deSelected
                            tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_inventory_slot_rectangle,context.getTheme()));
                            tv_FlatNumber.setTextColor(getResources().getColor(R.color.color_flat_available));
                        }

                        break;

                    case 2:
                        //Reserved
                        tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_reserved_rectangle,context.getTheme()));
                        tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        break;


                    case 3:
                        //onHold
                        tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_hold_slot_rectangle,context.getTheme()));
                        tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        break;

                    case 4:
                        //sold
                        tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_sold_rectangle,context.getTheme()));
                        tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        break;

                    case 5:
                        //locked
                        tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_locked_rectangle,context.getTheme()));
                        tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        break;

                    default:
                        //def sold
                        tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_sold_rectangle,context.getTheme()));
                        tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                        break;
                }


                final int finalI = i;
                rowView_sub.setOnClickListener(v -> {

                    switch (arrayList.get(finalI).getInventory_status_id())
                    {
                        case 1:
                            //Available

                           /* //set selected
                            tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_available,context.getTheme()));
                            tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));
                            //de-selects all
                            for (int s=0; s<floorModelArrayList.size(); s++ )
                            {
                                for (int a=0; a<floorModelArrayList.get(s).getFlatUnitModelArrayList().size(); a++)
                                {
                                    if (a==finalI)
                                    {
                                        //select only clicked one
                                        myModel.getFlatUnitModelArrayList().get(finalI).setIsSelected(1);
                                    }
                                    else myModel.getFlatUnitModelArrayList().get(a).setIsSelected(0);
                                }
                            }
                            //set floorsViewAgain
                            setFloorFlatView();*/

                            if (myModel.getFlatUnitModelArrayList().get(finalI).getIsSelected()==1)
                            {
                                //already selected -- do deselect

                                tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_inventory_slot_rectangle,context.getTheme()));
                                tv_FlatNumber.setTextColor(getResources().getColor(R.color.color_flat_available));

                                //set deSelected
                                myModel.getFlatUnitModelArrayList().get(finalI).setIsSelected(0);
                                checkInsertRemoveSubCat(myModel.getFlatUnitModelArrayList().get(finalI).getFlat_id(), false);
                                //checkInsertRemoveSubCat(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_description(), false);
                            }
                            else
                            {
                                //no selected -- do select


                                if (getSubCatIds().size()==0)
                                {
                                    //if already one selected -- then do not insert another
                                    //here condition is only flat at a time selectable

                                    tv_FlatNumber.setBackground(context.getResources().getDrawable(R.drawable.bg_available,context.getTheme()));
                                    tv_FlatNumber.setTextColor(getResources().getColor(R.color.main_white));

                                    //set selected
                                    myModel.getFlatUnitModelArrayList().get(finalI).setIsSelected(1);
                                    checkInsertRemoveSubCat(myModel.getFlatUnitModelArrayList().get(finalI).getFlat_id(), true);
                                    //checkInsertRemoveSubCatName(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_description(), true);

                                    //set to tv
                                    tv_selectedFlat.setText(String.format("%s - %s | %s", selectedBlockName, arrayList.get(finalI).getFlat_name(), arrayList.get(finalI).getFlat_type()));
                                }
                                else
                                {
                                    new Helper().showCustomToast(context, "You can select only one flat at a time!");
                                    new Handler().postDelayed(() -> new Helper().showCustomToast(context, "You can change your selection by removing your selected flat!"), 2000);
                                }
                            }

                            //check Button Enabled or not
                            checkFlatSelected();

                            break;
                        case 2:
                            //Reserved
                            new Helper().showCustomToast(this,"Unit "+ arrayList.get(finalI).getFlat_name() +" is already reserved!");
                            break;
                        case 3:
                            //onHold
                            new Helper().showCustomToast(this,"Unit "+ arrayList.get(finalI).getFlat_name() +" is on hold!");
                            break;
                        case 4:
                            //sold
                            new Helper().showCustomToast(this,"Unit "+ arrayList.get(finalI).getFlat_name() +" is sold out!");
                            break;
                        case 5:
                            //locked
                            new Helper().showCustomToast(this,"Unit "+ arrayList.get(finalI).getFlat_name() +" is locked by someone!");
                            break;
                        default:
                            //def sold
                            new Helper().showCustomToast(this,"Unit "+ arrayList.get(finalI).getFlat_name() +" is sold out!");
                            break;
                    }
                });

                flowLayout.addView(rowView_sub);
            }
        }
        else {

            //No sub categories Found in it.
            //ll_cat_main.setVisibility(View.GONE);
            flowLayout.removeAllViews();
            @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_text_view_no_cat, null );
            AppCompatTextView tv_noCat = rowView_sub.findViewById(R.id.tvText_noCat);
            tv_noCat.setText(getText(R.string.no_flat_available));
            flowLayout.addView(rowView_sub);
        }


        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        ll_itemFloors_main.setLayoutParams(params);

        //set Animation to the layout
        //setAnimation(ll_itemLeadCampaignMain, position);

        return rowView;
    }


    private void checkInsertRemoveSubCat(int id, boolean value) {
        if (value) catIntegerArrayList.add(id);
        else catIntegerArrayList.remove(new Integer(id));
    }

    public ArrayList<Integer> getSubCatIds() {
        return catIntegerArrayList;
    }


    private void checkFlatSelected()
    {

        //set animation
        if (getSubCatIds().size() > 0) {
            new Animations().slideInBottom(ll_proceed_layout);
        } else {
            new Animations().slideOutBottom(ll_proceed_layout);
        }
        ll_proceed_layout.setVisibility(getSubCatIds().size()>0 ? View.VISIBLE : View.GONE );
        //tv_selectedFlat.setText(String.format("%s - %s | %s", selectedBlockName, flat_name, flat_type));
    }


    private void showShimmer() {

        sfl.setVisibility(View.VISIBLE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
    }


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            sfl.setVisibility(View.GONE);
            Helper.onErrorSnack(context, message);
        });

    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

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
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }



}
