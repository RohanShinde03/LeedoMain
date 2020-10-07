package com.tribeappsoft.leedo.salesPerson.direct_allotment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.models.AllottedFlatListModel;
import com.tribeappsoft.leedo.scaleImage.ScaleImageActivity;
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

public class AllottedFlatDetail_Activity extends AppCompatActivity
{

    private static final String TAG = "AllottedFlatDetail_Activity";
    @BindView(R.id.tv_allottedFlatDtl_tokenNumber)AppCompatTextView tv_tokenNumber;
    @BindView(R.id.tv_allottedFlatDtl_customerName)AppCompatTextView tv_customerName;
    @BindView(R.id.tv_allottedFlatDtl_mobileNumber)AppCompatTextView tv_mobileNumber;
    @BindView(R.id.tv_holdFlatDetails_email)AppCompatTextView tv_email;
    @BindView(R.id.tv_allottedFlatDtl_unitType)AppCompatTextView tv_unitType;
    @BindView(R.id.tv_allotmentDtl_sitevisit_unitType)AppCompatTextView tv_sitevisit_unitType;
    @BindView(R.id.ll_allotmentDtl_siteVisitDetails)LinearLayoutCompat ll_siteVisitDetails;
    @BindView(R.id.tv_allotmentDtl_siteVisit_conductedBy)AppCompatTextView tv_conductedBy;
    @BindView(R.id.tv_allottedFlatDtl_event)AppCompatTextView tv_event;
    @BindView(R.id.mBtn_allottedFlatDtl_cancelAllotment) MaterialButton btn_cancelAllotment;
    @BindView(R.id.tv_allottedFlatDtl_flatAmount) AppCompatTextView tv_flatAmount;
    @BindView(R.id.fl_flatAllotmentDtl_formImagesList) LinearLayoutCompat ll_formImagesList;
    @BindView(R.id.ll_flatAllotmentDtl_images) LinearLayoutCompat ll_flatAllotmentDtl_images;
    @BindView(R.id.ll_flatAllotmentDtl_ghpDetails) LinearLayoutCompat ll_ghpDetails;
    @BindView(R.id.tv_allottedFlat_noAttachments) AppCompatTextView tv_noAttachments;
    @BindView(R.id.hsv_flatAllotmentDtl_allotmentForm) HorizontalScrollView hsv_allotmentForm;

    @BindView(R.id.tv_allottedFlatDtl_leadTypeName) AppCompatTextView tv_leadTypeName;
    @BindView(R.id.tv_allottedFlatDtl_leadTypeNameDtl) AppCompatTextView tv_leadTypeNameDtl;
    @BindView(R.id.tv_allottedFlatDtl_ghp_number_) AppCompatTextView tv_ghp_number;
    @BindView(R.id.tv_allottedFlatDtl_ghp_type) AppCompatTextView tv_ghp_type;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    Activity context;
    private AllottedFlatListModel allotModel;
    private String api_token="";
    private ArrayList<String> bookingAttachmentStrings;
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allotted_flat_detail_);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        context = AllottedFlatDetail_Activity.this;
        ButterKnife.bind(this);
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.flat_allotment_dtl));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        bookingAttachmentStrings= new ArrayList<>();

        //def hide pb
        showProgressBar("Please wait...");


        if(getIntent()!=null) {
            allotModel=(AllottedFlatListModel)getIntent().getSerializableExtra("allotModel");
        }

        if(allotModel!=null)
        {
            tv_tokenNumber.setText(allotModel.getLead_uid());
            tv_customerName.setText(allotModel.getFull_name());
            tv_mobileNumber.setText(allotModel.getMobile_number());
            tv_flatAmount.setText(allotModel.getFlat_total());
            tv_leadTypeName.setText(allotModel.getLead_types_name());
            tv_leadTypeNameDtl.setText(allotModel.getLead_types_name());
            tv_unitType.setText(String.format("%s-%s | %s | %s", allotModel.getBlock_name(), allotModel.getUnit_name(), allotModel.getUnit_category(), allotModel.getProject_name()));
            tv_sitevisit_unitType.setText(String.format("%s | %s", allotModel.getProject_name(), allotModel.getSite_visit_date()!=null ? Helper.getNotificationFormatDate(allotModel.getSite_visit_date()) :""));
            tv_conductedBy.setText(allotModel.getSite_visit_verified_by());
            tv_ghp_number.setText(allotModel.getToken_no());
            tv_ghp_type.setText(allotModel.getToken_type());
            tv_event.setText(allotModel.getEvent_title());

            //set Visibility
            if(allotModel.getToken_no()!=null && !allotModel.getToken_no().trim().isEmpty()) {
                ll_ghpDetails.setVisibility(View.VISIBLE);
            }else ll_ghpDetails.setVisibility(View.GONE);

            if(allotModel.getSite_visit_verified_by()!=null && !allotModel.getSite_visit_verified_by().trim().isEmpty()) {
                ll_siteVisitDetails.setVisibility(View.VISIBLE);
            }else ll_siteVisitDetails.setVisibility(View.GONE);


            bookingAttachmentStrings.addAll(allotModel.getImgstringArrayList());
            Log.e(TAG, "onCreate: "+bookingAttachmentStrings);

            //set imagesView
            getBooking_attachment();

            //hide pb
            hideProgressBar();
        }
        else hideProgressBar();

        tv_mobileNumber.setOnClickListener(view -> new Helper().openPhoneDialer(context,  allotModel.getMobile_number()));
        btn_cancelAllotment.setOnClickListener(v -> showCancelAllotmentAlert(allotModel));


    }

    private void getBooking_attachment() {

        if(bookingAttachmentStrings.size()>0)
        {
            ll_formImagesList.removeAllViews();
            for (int i =0 ; i<bookingAttachmentStrings.size(); i++)
            {
                View rowView_sub = getAllotmentFormView(i, bookingAttachmentStrings.get(i));
                ll_formImagesList.addView(rowView_sub);
            }

            //gone def layout
            tv_noAttachments.setVisibility(View.GONE);
            //visible images layout
            hsv_allotmentForm.setVisibility(View.VISIBLE);
            ll_flatAllotmentDtl_images.setVisibility(View.VISIBLE);
        }
        else
        {
            //gone visibility
            hsv_allotmentForm.setVisibility(View.GONE);
            ll_flatAllotmentDtl_images.setVisibility(View.GONE);

            //show def image
            tv_noAttachments.setVisibility(View.VISIBLE);
        }
    }


    private View getAllotmentFormView(int position, String imagePath)
    {

        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_form_image, null );
        final RoundedImageView imageView = rowView_sub.findViewById(R.id.iv_flatAllotment_form);
        Glide.with(context)
                .load(imagePath)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().placeholder(R.drawable.def_image_no_attachment))
                .apply(new RequestOptions().error(R.drawable.def_image_no_attachment))
                .into(imageView);

        //call to image Zoom activity
        imageView.setOnClickListener(view -> startActivity(new Intent(context, ScaleImageActivity.class)
                .putExtra("banner_path", imagePath)
                .putExtra("event_title", "Allotted Flat Details")
        ));

        return rowView_sub;
    }

    public void showCancelAllotmentAlert(AllottedFlatListModel myModel)
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

        tv_msg.setText(getResources().getString(R.string.cancel_flat_allotment_que));
        tv_desc.setText(getString(R.string.cancel_allotment_text, allotModel.getFull_name()));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            if (Helper.isNetworkAvailable(this))
            {
                showProgressBar("Cancelling flat allotment...");
                call_cancelAllottment(myModel);

            } else Helper.NetworkError(this);

            alertDialog.dismiss();
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





    private void showCancelAllotmentDialog( AllottedFlatListModel myModel)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to cancel this allotment ?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            //call add sales api
            if (Helper.isNetworkAvailable(context))
            {
                call_cancelAllottment(myModel);
            }else Helper.NetworkError(context);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        /*builder.setSingleChoiceItems(singleChoiceListItems,-1,(dialogInterface, i) -> {
            dialogInterface.dismiss();
        });*/
        builder.show();
    }


    private void call_cancelAllottment( AllottedFlatListModel myModel)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("booking_id", myModel.getBooking_id());
        jsonObject.addProperty("api_token", api_token);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().cancelAllotment(jsonObject).enqueue(new Callback<JsonObject>()
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

                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {

                                    //JsonObject data = response.body().get("data").getAsJsonObject();
                                    //isLeadSubmitted = true;

                                    showSuccessAlert();
                                }
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLog(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(context.getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }


    private void showErrorLog(final String message) {
        context.runOnUiThread(() -> {

            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }


    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        context.runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            //  onErrorSnack(context, "Flat released successfully!");
            new Helper().showCustomToast(context, "Allotment cancelled successfully!!");
            onBackPressed();
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
