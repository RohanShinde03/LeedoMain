package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamLeaderModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamMemberModel;
import com.tribeappsoft.leedo.util.GlideChip;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class TeamLeadDetailsActivity extends AppCompatActivity {

    Activity context;
    @BindView(R.id.ll_teamLeadDtl_Main) LinearLayoutCompat ll_Main;
    @BindView(R.id.tv_teamLeadDtl_member_name) AppCompatTextView tv_member_name;
    @BindView(R.id.tv_teamLeadDtl_member_mobile) AppCompatTextView tv_member_mobile;
    @BindView(R.id.tv_teamLeadDtl_member_email) AppCompatTextView tv_member_email;

    @BindView(R.id.tv_teamLeadDtl_ListTitle) AppCompatTextView tv_ListTitle;
    @BindView(R.id.ll_teamLeadDtl_members) LinearLayoutCompat ll_teamMembers;
    @BindView(R.id.chip_group_teamLeadDtl) ChipGroup chip_group;
    @BindView(R.id.tv_teamLeadDtl_noTeamMembers) AppCompatTextView tv_noTeamMembers;
    @BindView(R.id.mBtn_teamLeadDtl_addTeamMembers) MaterialButton btn_addTeamMembers;

    @BindView(R.id.ll_teamLeadDtl_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    // @BindView(R.id.ll_team_lead_details_teamMembers) LinearLayoutCompat ll_total_teamMembers;

    private TeamLeaderModel teamLeaderModel = null;
    private String api_token="";
    private int sales_lead_id=0;
    private ArrayList<Integer> teamMemberArrayList;
    private String TAG="TeamLeadDetailsActivity", sales_team_lead_name ="";
    //TeamMemberModel teamMemberModel=new TeamMemberModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_lead_details);
        //overridePendingTransition(R.anim.trans_slide_up, R.anim.no_change);
        ButterKnife.bind(this);

        context = TeamLeadDetailsActivity.this;
        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.team_lead_details));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString("api_token", null) != null) api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        teamMemberArrayList=new ArrayList<>();

        if(getIntent()!=null){
            sales_lead_id = getIntent().getIntExtra("sales_lead_id", 0);
        }

        // final ChipGroup entryChipGroup = findViewById(R.id.entry_chip_group);
        // final Chip entryChip = getChip(chip_group);
        /*final Chip entryChip2 = getChip(chip_group, "Omkar");
        final Chip entryChip3 = getChip(chip_group, "Gautami Lavhate");
        final Chip entryChip4 = getChip(chip_group, "Siddhi Patil");
        final Chip entryChip5 = getChip(chip_group, "Pooja");
        final Chip entryChip6 = getChip(chip_group, "Harshada  Chavan");*/
        //  chip_group.addView(entryChip);
      /*  chip_group.addView(entryChip2);
        chip_group.addView(entryChip3);
        chip_group.addView(entryChip4);
        chip_group.addView(entryChip5);
        chip_group.addView(entryChip6);
*/
        ll_Main.setVisibility(View.GONE);

        //hide pb
        hideProgressBar();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Helper.isNetworkAvailable(this)) {
            showProgressBar(getString(R.string.getting_teamLead_details));
            new Handler().postDelayed(this::call_getTeamMemberDetails,1000);
        }
        else {
            Helper.NetworkError(this);
        }
    }

    public void call_getTeamMemberDetails()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getSalesTeamLeadDetails(api_token,sales_lead_id);
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
                        Log.d(TAG, "onCompleted:");
                        delayRefresh();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if (JsonObjectResponse.body().isJsonObject())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();

                                    //hideLoading();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                            if (jsonObject!=null && !jsonObject.isJsonNull())
                                            {
                                                //detailModelArrayList.clear();
                                                setTeamLeadJson(jsonObject);
                                            }
                                            else showErrorLog("Empty Data from server!");                                        }
                                        else showErrorLog("Empty response from server!");
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }
                    }
                });
    }

    private void setTeamLeadJson(JsonObject asJsonObject) {

        teamLeaderModel =new TeamLeaderModel();
        if (asJsonObject.has("user_id")) teamLeaderModel.setUser_id(!asJsonObject.get("user_id").isJsonNull() ? asJsonObject.get("user_id").getAsInt() : 0 );
        if (asJsonObject.has("sales_lead_id")) teamLeaderModel.setSales_lead_id(!asJsonObject.get("sales_lead_id").isJsonNull() ? asJsonObject.get("sales_lead_id").getAsInt() : 0 );
        if (asJsonObject.has("person_id")) teamLeaderModel.setSales_lead_id(!asJsonObject.get("person_id").isJsonNull() ? asJsonObject.get("person_id").getAsInt() : 0 );
        if (asJsonObject.has("full_name")) teamLeaderModel.setFull_name(!asJsonObject.get("full_name").isJsonNull() ? asJsonObject.get("full_name").getAsString().trim() : "" );
        if (asJsonObject.has("full_name")) sales_team_lead_name = !asJsonObject.get("full_name").isJsonNull() ? asJsonObject.get("full_name").getAsString().trim() : "" ;
        if (asJsonObject.has("email")) teamLeaderModel.setEmail(!asJsonObject.get("email").isJsonNull() ? asJsonObject.get("email").getAsString().trim() : "");
        if (asJsonObject.has("mobile_number")) teamLeaderModel.setMobile_number(!asJsonObject.get("mobile_number").isJsonNull() ? asJsonObject.get("mobile_number").getAsString().trim() : "");


        if (!asJsonObject.get("teamMembers").isJsonNull() && asJsonObject.get("teamMembers").isJsonArray())
        {
            if (!asJsonObject.get("teamMembers").isJsonNull())
            {
                JsonArray jsonArray =  asJsonObject.get("teamMembers").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<TeamMemberModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setTeamMemberModel(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    teamLeaderModel.setMemberModelArrayList(arrayList);
                }
            }
        }

    }

    private void setTeamMemberModel(JsonObject jsonObject, ArrayList<TeamMemberModel> arrayList)
    {
        Log.e(TAG, "setTeamMemberModel:" );
        TeamMemberModel teamMemberModel = new TeamMemberModel();
        if (jsonObject.has("user_member_id")) teamMemberModel.setUser_member_id(!jsonObject.get("user_member_id").isJsonNull() ? jsonObject.get("user_member_id").getAsInt() : 0 );
        if (jsonObject.has("user_id")) teamMemberModel.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("full_name")) teamMemberModel.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        if (jsonObject.has("mobile_number")) teamMemberModel.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
        if (jsonObject.has("email")) teamMemberModel.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
        if (jsonObject.has("photopath")) teamMemberModel.setPhoto_path(!jsonObject.get("photopath").isJsonNull() ? jsonObject.get("photopath").getAsString() : "" );
        arrayList.add(teamMemberModel);

    }

    private void delayRefresh()
    {
        if (context != null) {
            context.runOnUiThread(() -> {

                hideProgressBar();
                if (teamLeaderModel !=null) {

                    tv_member_name.setText(teamLeaderModel.getFull_name());
                    tv_member_mobile.setText(Objects.requireNonNull(teamLeaderModel).getMobile_number());
                    tv_member_email.setText(teamLeaderModel.getEmail());

                    //set team members
                    setTeamMemberList();

                    btn_addTeamMembers.setOnClickListener(view ->
                            startActivity(new Intent(context, SelectTeamMembersActivity.class)
                                    .putExtra("sales_lead_id",sales_lead_id )
                                    .putExtra("sales_team_lead_name",sales_team_lead_name )
                            )
                    );

                    //visible main
                    ll_Main.setVisibility(View.VISIBLE);
                    //hide no details
                    ll_noData.setVisibility(View.GONE);
                }
                else {
                    //hide main
                    ll_Main.setVisibility(View.GONE);
                    //visible no details
                    ll_noData.setVisibility(View.VISIBLE);

                    //showErrorLog("failed to load team lead details!");
                }
            });
        }
    }




    //set TeamMembers data
    private void setTeamMemberList() {

        if (teamLeaderModel.getMemberModelArrayList()!=null && teamLeaderModel.getMemberModelArrayList().size()>0)
        {
            chip_group.removeAllViews();
            for (int i =0 ; i< teamLeaderModel.getMemberModelArrayList().size(); i++) {
                View rowView_sub = getChip(chip_group,i);
                chip_group.addView(rowView_sub);
            }


            //visible team members main
            ll_teamMembers.setVisibility(View.VISIBLE);

            //hide no team members
            tv_noTeamMembers.setVisibility(View.GONE);
        }else {

            //hide team members main
            ll_teamMembers.setVisibility(View.GONE);

            //visible no data
            tv_noTeamMembers.setVisibility(View.VISIBLE);
        }
    }

    private Chip getChip(final ChipGroup entryChipGroup,final int position) {
        /*  final Chip chip = new Chip(this);*/

        GlideChip chip = new GlideChip(this);
        chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.mychip));
       /* int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
*/
        // GlideChip chip = new GlideChip(getContext());

        //     tv_teamMemberName.setText(String.valueOf(teamLeaderModel.getMemberModelArrayList().get(position).getFull_name()));
        //   tv_teamMemberMobile.setText(String.valueOf(teamLeaderModel.getMemberModelArrayList().get(position).getMobile_number()));

        chip.setTextSize(18f);
        //chip.setIconUrl("https://cdn.wallpapersafari.com/85/97/pg7ZSn.jpg", getResources().getDrawable(R.drawable.ic_user_gray));
        chip.setIconUrl(teamLeaderModel.getMemberModelArrayList().get(position).getPhoto_path(), getResources().getDrawable(R.drawable.ic_user_gray));
        chip.setBackgroundResource(R.drawable.bg_gray_outline_border_more_round);
        chip.setBackgroundColor(getResources().getColor(R.color.main_white));
        chip.setPadding(10, 15, 10, 15);
        //chip.setChipMinHeight(120f);
        //chip.setChipMinHeight(110f);
        //chip.setText(teamLeaderModel.getMemberModelArrayList().get(position).getFull_name());
        chip.setText(teamLeaderModel.getMemberModelArrayList().get(position).getFull_name());
        chip.setIconStartPadding(10);
        //chip.setChipIconSize(95f);
        chip.setOnCloseIconClickListener(v -> showSubmitMemberAlertDialog(entryChipGroup,chip,position, (String) chip.getText()));
        return chip;
    }


    private void showSubmitMemberAlertDialog(final ChipGroup entryChipGroup, GlideChip chip, int position,String team_member)
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

        tv_msg.setText(getString(R.string.remove_member));
        tv_desc.setText(String.format("%s %s from team members list ?", getString(R.string.remove_member_confirmation), team_member));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                //  showProgressBar(getString(R.string.submitting_lead_details));
                //api_call
                // teamMemberArrayList.add(teamMemberModel.getUser_member_id());
                teamMemberArrayList.add(teamLeaderModel.getMemberModelArrayList().get(position).getUser_member_id());
                Log.e(TAG, "showSubmitMemberAlertDialog: "+teamLeaderModel.getMemberModelArrayList().get(position).getUser_member_id());
                showProgressBar(getString(R.string.removing_team_member));
                call_postAddRemove();

                entryChipGroup.removeView(chip);

            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
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

    private void call_postAddRemove()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id",sales_lead_id);
        jsonObject.addProperty("status_id", 2);
        //jsonObject.addProperty("teamMembers", String.valueOf(teamMemberArrayList));
        jsonObject.add("teamMembers", new Gson().toJsonTree(teamMemberArrayList).getAsJsonArray());


        ApiClient client = ApiClient.getInstance();
        client.getApiService().addRemoveTeamMembers(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1) {
                            onSuccessRemoveMember();
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


    private void onSuccessRemoveMember()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show success toast
            new Helper().showSuccessCustomToast(context, getString(R.string.team_member_removed_successfully));

            //on backPress
            //new Handler().postDelayed(this::onBackPressed, 1000);
        });
    }

/*    private View getTeamMembersView(final int position)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_team_member, null );
        final AppCompatTextView tv_teamMemberName = rowView_sub.findViewById(R.id.tvText_teamMemberName);
        final AppCompatTextView tv_teamMemberMobile = rowView_sub.findViewById(R.id.tvText_teamMemberMobile);

        tv_teamMemberName.setText(String.valueOf(teamLeaderModel.getMemberModelArrayList().get(position).getFull_name()));
        tv_teamMemberMobile.setText(String.valueOf(teamLeaderModel.getMemberModelArrayList().get(position).getMobile_number()));

        return rowView_sub;
    }*/


    private void showErrorLog(final String message)
    {
        if(context!=null)
        {
            runOnUiThread(() ->{
                //hide pb
                hideProgressBar();
                Helper.onErrorSnack(context,message);
                //hide main
                ll_Main.setVisibility(View.GONE);
                //visible no details
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
