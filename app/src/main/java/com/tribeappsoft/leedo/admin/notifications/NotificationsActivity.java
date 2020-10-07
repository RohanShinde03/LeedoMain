package com.tribeappsoft.leedo.admin.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.models.NotificationModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsActivity extends AppCompatActivity {

    private Activity context;
    @BindView(R.id.swipeRefresh_studentNotifications) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.recycler_studentNotifications) RecyclerView recyclerView;
    @BindView(R.id.ll_student_noNotifications) LinearLayoutCompat ll_no_notifications;

    private String TAG="NotificationsActivity";
    private ArrayList<NotificationModel> modelArrayList, multiSelect_list;
    private NotificationsRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isRegPending = false, notifyNotifications = false, isMultiSelect = false;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        //overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        ButterKnife.bind(this);
        context= NotificationsActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.notifications));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.notifications));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (getIntent()!=null)
        {
            isRegPending = getIntent().getBooleanExtra("isRegPending", false);
            notifyNotifications = getIntent().getBooleanExtra("notifyNotifications", false);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.remove("NotificationCount");
        editor.apply();

        modelArrayList=  new ArrayList<>();
        multiSelect_list = new ArrayList<>();

        //set up RecyclerView
        setUpRecyclerView();

        if (swipeRefresh!=null) swipeRefresh.setRefreshing(true);
        new Handler().postDelayed(this::setNotifications, 500);

        //set SwipeRefresh
        setSwipeRefresh();

        //do multi select
        doMultiSelect();

    }

   /* //setup recycler view
    private void setUpRecyclerView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new NotificationsRecyclerAdapter(context, modelArrayList, multiSelect_list, isRegPending);
        recyclerView.setAdapter(recyclerAdapter);
    }*/


    //setup recycler view
    private void setUpRecyclerView()
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
        recyclerAdapter = new  NotificationsRecyclerAdapter(context, modelArrayList, multiSelect_list, isRegPending);
        recyclerView.setAdapter(recyclerAdapter);

    }

    public void setSwipeRefresh() {

        //getOffline();
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(true);

            mActionMode = null;
            isMultiSelect = false;
            modelArrayList = new ArrayList<>();
            multiSelect_list = new ArrayList<>();
            //refreshAdapter();

            //call API
            setNotifications();

        });

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }



    private void setNotifications()
    {
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            String notification = null;
            if (sharedPreferences.getString("NotificationModel", null)!=null) notification = sharedPreferences.getString("NotificationModel", null);
            if (notification!=null) Log.e("Notification", " "+notification);

            if (notification!=null)
            {
                modelArrayList.clear();
                //notifications available
                Gson gson  = new Gson();
                JsonArray jsonArray = gson.fromJson(notification, JsonArray.class);
                for (int i=0; i<jsonArray.size(); i++) {
                    setJson(jsonArray.get(i).getAsJsonObject());
                    //Log.e("NewAry", jsonArray.get(i).toString());
                }
                //set the adapter for Notifications
                delayRefresh();
            }
            else
            {
                //not available
                if (swipeRefresh!=null) swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_no_notifications.setVisibility(View.VISIBLE);
            }
        }
    }


    private void setJson(JsonObject jsonObject)
    {
        NotificationModel myModel = new NotificationModel();
        if (jsonObject.has("notification_id")) myModel.setNotification_id(!jsonObject.get("notification_id").isJsonNull() ? jsonObject.get("notification_id").getAsInt() : 0 );
        if (jsonObject.has("created_at")) myModel.setCreated_at(Helper.getNotificationFormatDate(!jsonObject.get("hh:mm aaahh:mm aaa").isJsonNull() ? jsonObject.get("created_at").getAsString().trim() : Helper.getDateTime() ));
        if (jsonObject.has("updated_at")) myModel.setUpdated_at(Helper.getNotificationFormatDate(!jsonObject.get("updated_at").isJsonNull() ? jsonObject.get("updated_at").getAsString().trim() : Helper.getDateTime()));
        if (jsonObject.has("student_id")) myModel.setStudent_id(!jsonObject.get("student_id").isJsonNull() ? jsonObject.get("student_id").getAsInt() : 0 );
        if (jsonObject.has("title")) myModel.setTitle(!jsonObject.get("title").isJsonNull() ? jsonObject.get("title").getAsString() : "" );
        if (jsonObject.has("content")) myModel.setContent(!jsonObject.get("content").isJsonNull() ? jsonObject.get("content").getAsString().trim() : "content" );
        if (jsonObject.has("page")) myModel.setPage(!jsonObject.get("page").isJsonNull() ? jsonObject.get("page").getAsString().trim() : "page" );
        if (jsonObject.has("data")) myModel.setData(!jsonObject.get("data").isJsonNull() ? jsonObject.get("data").getAsString().trim() : null );
        if (jsonObject.has("date")) myModel.setDate(!jsonObject.get("date").isJsonNull() ? jsonObject.get("date").getAsString().trim() : Helper.getDateTime());
        if (jsonObject.has("status_id")) myModel.setStatus_id(!jsonObject.get("status_id").isJsonNull() ? jsonObject.get("status_id").getAsInt() : 0 );

        modelArrayList.add(myModel);
    }


    private void delayRefresh()
    {
        //reply
        if (context != null)
        {
            runOnUiThread(() -> {

                if (swipeRefresh!=null) swipeRefresh.setRefreshing(false);
                LinearLayoutManager manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setHasFixedSize(true);

                //Log.e(TAG,"count"+vdoCommentModels.size());

                recyclerAdapter  = new NotificationsRecyclerAdapter(context, modelArrayList, multiSelect_list, isRegPending);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no list
                    recyclerView.setVisibility(View.GONE);
                    ll_no_notifications.setVisibility(View.VISIBLE);
                } else {
                    //list items are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_no_notifications.setVisibility(View.GONE);
                }
            });
        }
    }


    private void doMultiSelect()
    {
        //RecyclerView ItemTouch Method
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect) multi_select(position);
                //else Toast.makeText(getApplicationContext(), "Details Page", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiSelect_list = new ArrayList<>();
                    isMultiSelect = true;
                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);
            }
        }));
    }


//    private void showErrorLog(final String message)
//    {
//        runOnUiThread(() -> {
//            if (swipeRefresh!=null) swipeRefresh.setRefreshing(false);
//            //no list
//            recyclerView.setVisibility(View.GONE);
//            ll_no_notifications.setVisibility(View.VISIBLE);
//            onErrorSnack(context,message);
//        });
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_delete_notifications, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if (notifyNotifications)
                {
                    if (isRegPending) {
                        startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                        finish();
                    }
                    else {
                        startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                        finish();
                    }
                }
                else onBackPressed();
                break;
            case R.id.action_clear_notifications:
                if (recyclerView.getAdapter()!=null) if (recyclerView.getAdapter().getItemCount()>0) showClearNotificationsAlert();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;

    }

    private void showClearNotificationsAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog;
        alertDialog = alertDialogBuilder.create();
        AppCompatTextView tv_msg,tv_desc;
        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getString(R.string.delete_notifications));
        tv_desc.setText(getString(R.string.notification_clear_alert));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.ok));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //clear all notifications
            clearNotifications();
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }

/*
    private void showClearNotificationsAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.material_AlertDialogTheme);
        builder.setTitle("Delete Notifications?");
        builder.setMessage(getString(R.string.notification_clear_alert));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) ->{

            //clear all notifications
            clearNotifications();
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
*/


    //MultiSelect Items
    public void multi_select(int position) {
        if (mActionMode != null) {

            position = position < modelArrayList.size() ?  position : 0;

            if (multiSelect_list.contains(modelArrayList.get(position)))
                multiSelect_list.remove(modelArrayList.get(position));
            else
                multiSelect_list.add(modelArrayList.get(position));

            if (multiSelect_list.size() > 0) mActionMode.setTitle("" + multiSelect_list.size());
            else
            {
                if (mActionMode != null) {
                    mActionMode.setTitle("");
                    mActionMode.finish();
                }
            }

            refreshAdapter();

        }
    }

    /*Menus for Item Deleting*/
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_delete_notifications, menu);
            //context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_clear_notifications) {//alertDialogHelper.showAlertDialog("","Delete Contact","DELETE","CANCEL",1,false);
                //deleteItems();
                showSelectedNotificationsDeleteAlert();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiSelect_list = new ArrayList<>();
            refreshAdapter();
        }
    };

    private void showSelectedNotificationsDeleteAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog alertDialog;
        alertDialog = alertDialogBuilder.create();
        AppCompatTextView tv_msg,tv_desc;
        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getString(R.string.delete_notifications));
        tv_desc.setText(getString(R.string.selected_notification_clear_alert, String.valueOf(multiSelect_list.size())));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.ok));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            deleteItems();
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }


   /* private void showSelectedNotificationsDeleteAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.material_AlertDialogTheme);
        builder.setTitle("Delete Notifications?");
        builder.setMessage(getString(R.string.selected_notification_clear_alert, String.valueOf(multiSelect_list.size())));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) ->{

            //delete selected notifications
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }*/


    private void deleteItems() {
        if(multiSelect_list.size()>0)
        {

            //String notification =null;
            //if (sharedPreferences.getString("NotificationModel", null)!=null) notification = sharedPreferences.getString("NotificationModel", null);
            //if (notification!=null) jsonArray = new Gson().fromJson(notification, JSONArray.class);


            for(int i=0;i<multiSelect_list.size();i++) {
                modelArrayList.remove(multiSelect_list.get(i));

                //if (jsonArray!=null)
                //if (isKeyExists(jsonArray, multiSelect_list.get(i).getContent())) {
                //delete from shared pref
                // Log.e("ary1", jsonArray.toString());
                //}
                // else Log.e(TAG, "NotificationModel: key Not Present");
            }
            recyclerAdapter.notifyDataSetChanged();

            //save to local array
            if (sharedPreferences!=null)
            {

                JSONArray jsonArray = new JSONArray();
                for (int i =0; i<modelArrayList.size(); i++) {
                    setJson(modelArrayList.get(i), jsonArray);
                }

//                try {
//                    jsonArray = new JSONArray(modelArrayList.toString());
//                    Log.e("ary", jsonArray.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }


                editor= sharedPreferences.edit();
                editor.putString("NotificationModel", jsonArray.toString());
                editor.putInt("NotificationCount", jsonArray.length());
                editor.apply();
                Log.e("NewAry", jsonArray.toString());
                //Log.e("jsnObj", jsonObject.toString());
            }

            if (mActionMode != null) {
                mActionMode.finish();
            }
            new Helper().showCustomToast(this,"Deleted Successfully");
        }
    }

    private void setJson(NotificationModel model, JSONArray jsonArray)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {

            jsonObject.put("title", model.getTitle());
            jsonObject.put("content", model.getContent());
            jsonObject.put("picture", model.getPicture());
            jsonObject.put("page", model.getPage());
            jsonObject.put("date", model.getDate());
            jsonObject.put("data", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jsonObject);
    }

    public void refreshAdapter()
    {
        recyclerAdapter.multiSelect_list = multiSelect_list;
        recyclerAdapter.modelArrayList = modelArrayList;
        recyclerAdapter.notifyDataSetChanged();
    }

    private boolean isKeyExists(JSONArray jsonArray, String contents)
    {
        boolean found = false;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {

                //JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("content")) {
                    if (jsonObject.getString("content").equalsIgnoreCase(contents)) {
                        //found = true;
                        Log.e(TAG, "isKeyExists: true");
                        return true;
                    }
                }
                   /*if (jsonArray.getInt(i) == bid_details_id )
                   {
                       //found = true;
                       Log.e(TAG, "isKeyExists: true");
                       return true;
                   }*/
            }
            catch (Exception ex)
            {
                found = false;
                ex.printStackTrace();
            }

        }
        return found;
    }


    private void clearNotifications()
    {
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            //final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splashFadeout);
            //listView_notification.startAnimation(animation);

            runOnUiThread(() -> {
                //stuff that updates ui

                // TODO Auto-generated method stub
                editor.remove("NotificationModel");
                editor.remove("NotificationCount");
                editor.apply();
                //if (notificationsAdapter!=null) notificationsAdapter.notifyDataSetChanged();

                Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
                recyclerView.startAnimation(animZoomIn);

                if (recyclerAdapter!=null) recyclerAdapter.notifyDataSetChanged();
                modelArrayList.clear();
                //listView_notification.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                ll_no_notifications.setVisibility(View.VISIBLE);
                //animation.cancel();
            });

           /* Handler handle = new Handler();
            handle.postDelayed(new Runnable() {

                @Override
                public void run()
                {

                }
            }, 100);*/

            //setNotifications();
        }

    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
     //   overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}
