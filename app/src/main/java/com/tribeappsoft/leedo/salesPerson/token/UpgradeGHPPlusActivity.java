package com.tribeappsoft.leedo.salesPerson.token;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.tribeappsoft.leedo.BuildConfig;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.api.WebServer;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.PaymentModeModel;
import com.tribeappsoft.leedo.salesPerson.models.TokensModel;
import com.tribeappsoft.leedo.salesPerson.token.model.GHPPlusModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UpgradeGHPPlusActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    @BindView(R.id.cl_upgradeGHP) CoordinatorLayout parent;  @BindView(R.id.mTv_upgradeGHP_cuIdNumber) MaterialTextView mTv_cuIdNumber;
    @BindView(R.id.ll_upgradeGHP_main) LinearLayoutCompat ll_main;

    //lead details

    @BindView(R.id.mTv_upgradeGHP_leadName) MaterialTextView mTv_leadName;
    @BindView(R.id.mTv_upgradeGHP_projectName) MaterialTextView mTv_projectName;
    @BindView(R.id.mTv_upgradeGHP_leadMobile) MaterialTextView mTv_leadMobile;
    @BindView(R.id.mTv_upgradeGHP_eventName) MaterialTextView mTv_eventName;
    @BindView(R.id.mTv_upgradeGHP_ghpType) MaterialTextView mTv_ghpType;
    @BindView(R.id.mTv_upgradeGHP_ghpNumber) MaterialTextView mTv_ghpNumber;

    //payment mode
    @BindView(R.id.ll_upgradeGHP_paymentModeMain) LinearLayoutCompat ll_paymentModeMain;
    @BindView(R.id.iv_upgradeGHP_paymentMode_ec) AppCompatImageView iv_paymentMode_ec;
    @BindView(R.id.ll_upgradeGHP_paymentModes) LinearLayoutCompat ll_paymentModes;
    @BindView(R.id.edt_upgradeGHP_ghp_amount) TextInputEditText edt_ghp_amount;

    //select paid by
    @BindView(R.id.rGrp_upgradeGHP_payMode) RadioGroup rGrp_payMode;
    @BindView(R.id.rBtn_upgradeGHP_pay_manual)  RadioButton rBtn_pay_manual;
    @BindView(R.id.rBtn_upgradeGHP_pay_payGateway)  RadioButton rBtn_pay_payGateway;
    @BindView(R.id.rBtn_upgradeGHP_pay_viaPayLink)  RadioButton rBtn_pay_viaPayLink;
    //add manual pay modes
    @BindView(R.id.ll_upgradeGHP_payMode_manual) LinearLayoutCompat ll_payMode_manual;
    @BindView(R.id.ll_upgradeGHP_addPaymentMode) LinearLayoutCompat ll_addPaymentMode;
    //cheque
    @BindView(R.id.ll_upgradeGHP_cheque_payment) LinearLayoutCompat ll_cheque_payment;
    @BindView(R.id.edt_upgradeGHP_cheque_number) TextInputEditText edt_cheque_number;
    @BindView(R.id.edt_upgradeGHP_cheque_date) TextInputEditText edt_cheque_date;
    @BindView(R.id.edt_upgradeGHP_cheque_issuer_name) TextInputEditText edt_cheque_issuer_name;
    @BindView(R.id.edt_upgradeGHP_cheque_bank_name) TextInputEditText edt_cheque_bank_name;
    //online transfer
    @BindView(R.id.ll_upgradeGHP_online_payment) LinearLayoutCompat ll_online_payment;
    @BindView(R.id.edt_upgradeGHP_utr_number) TextInputEditText edt_utr_number;
    //card details
    @BindView(R.id.ll_upgradeGHP_card_payment) LinearLayoutCompat ll_card_payment;
    @BindView(R.id.edt_upgradeGHP_payment_transaction_number) TextInputEditText edt_transaction_number;
    @BindView(R.id.edt_upgradeGHP_payment_invoice_number) TextInputEditText edt_invoice_number;
    //remarks
    @BindView(R.id.edt_upgradeGHP_remarks) TextInputEditText edt_remarks;
    @BindView(R.id.mBtn_upgradeGHP) MaterialButton mBtn_upgradeGHP;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    //Share Token View
    @BindView(R.id.ll_upgradeGHP_shareToken_View) LinearLayoutCompat ll_shareToken_View;
    @BindView(R.id.iv_upgradeGHP_shareToken_close) AppCompatImageView iv_shareToken_close;
    @BindView(R.id.iv_upgradeGHP_shareToken_successImg) AppCompatImageView iv_shareToken_successImg;
    @BindView(R.id.tv_upgradeGHP_shareToken_successMsg) AppCompatTextView tv_shareToken_successMsg;
    @BindView(R.id.tv_upgradeGHP_shareToken_paymentPendingMsg) AppCompatTextView tv_shareToken_paymentPendingMsg;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_GHPNo) AppCompatTextView tv_Customer_GHPNo;

    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_name) AppCompatTextView tv_Customer_name;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_mobile) AppCompatTextView tv_Customer_mobile;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_email) AppCompatTextView tv_Customer_email;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_ProjectName) AppCompatTextView tv_Customer_ProjectName;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_EventName) AppCompatTextView tv_Customer_EventName;
    @BindView(R.id.view_upgradeGHP_ghpDetails) View view_ghpDetails;

    @BindView(R.id.ll_upgradeGHP_ShareToken_ghpDate) LinearLayoutCompat ll_ShareToken_ghpDate;
    @BindView(R.id.tv_upgradeGHP_ShareToken_ghp_date_text) AppCompatTextView tv_ghp_date_text;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_GHP_Date) AppCompatTextView tv_Customer_GHP_Date;
    @BindView(R.id.ll_upgradeGHP_ShareToken_ghpAmount) LinearLayoutCompat ll_ShareToken_ghpAmount;
    @BindView(R.id.tv_upgradeGHP_ShareToken_ghp_amount_text) AppCompatTextView tv_ghp_amount_text;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_GHP_Amount) AppCompatTextView tv_Customer_GHP_Amount;
    @BindView(R.id.ll_upgradeGHP_ShareToken_ghpPlusDate) LinearLayoutCompat ll_ShareToken_ghpPlusDate;
    @BindView(R.id.tv_upgradeGHP_ShareToken_ghp_plus_date_text) AppCompatTextView tv_ghp_plus_date_text;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_GHPPlus_Date) AppCompatTextView tv_Customer_GHPPlus_Date;
    @BindView(R.id.tv_upgradeGHP_ShareToken_remarks) AppCompatTextView tv_upgradeGHP_ShareToken_remarks;
    @BindView(R.id.ll_upgradeGHP_ShareToken_remarks) LinearLayoutCompat ll_upgradeGHP_ShareToken_remarks;
    @BindView(R.id.ll_upgradeGHP_ShareToken_ghpPlusAmount) LinearLayoutCompat ll_ShareToken_ghpPlusAmount;
    @BindView(R.id.tv_upgradeGHP_ShareToken_ghp_plus_amount_text) AppCompatTextView tv_ghp_plus_amount_text;
    @BindView(R.id.tv_upgradeGHP_ShareToken_itemCustomer_GHPPlus_Amount) AppCompatTextView tv_Customer_GHPPlus_Amount;

    //View Share Download
    @BindView(R.id.ll_upgradeGHP_ShareToken_View_share_download) LinearLayoutCompat ll_ShareToken_View_share_download;
    @BindView(R.id.pb_upgradeGHP_downloadDoc) ProgressBar pb_generateToken_downloadDoc;
    @BindView(R.id.mBtn_upgradeGHP_shareToken_download) MaterialButton mBtn_shareToken_download;
    @BindView(R.id.mBtn_upgradeGHP_shareToken_view) MaterialButton mBtn_shareToken_view;
    @BindView(R.id.mBtn_upgradeGHP_shareToken_share) MaterialButton mBtn_shareToken_share;
    @BindView(R.id.mBtn_upgradeGHP_shareToken_MoreShare) MaterialButton mBtn_shareToken_MoreShare;

    //ghp payment pending
    @BindView(R.id.ll_upgradeGHP_shareToken_paymentPending) LinearLayoutCompat ll_paymentPending;
    @BindView(R.id.mBtn_upgradeGHP_sharePayLink_waShare) MaterialButton mBtn_sharePayLink_waShare;
    @BindView(R.id.mBtn_upgradeGHP_sharePayLink_mailShare) MaterialButton mBtn_sharePayLink_mailShare;
    @BindView(R.id.mBtn_upgradeGHP_sharePayLink_moreShare) MaterialButton mBtn_sharePayLink_moreShare;

    @BindView(R.id.sfl_upgradeGHP) ShimmerFrameLayout sfl;
    @BindView(R.id.ll_upgradeGHP_noGHPPlus) LinearLayoutCompat ll_noGHPPlus;
    @BindView(R.id.ll_upgradeGHP_noData) LinearLayoutCompat ll_noData;

    public Activity context;
    public Animations anim;
    public boolean viewPaymentModes =false, ghp_plus_generated = false, isGHPPlusAvailable = true; // paidByManual = true;
    public ArrayList<PaymentModeModel> paymentModeModelArrayList;
    private GHPPlusModel ghpPlusModel = null;

    //public ArrayList<String> personPrefixStringArrayList, eventStringArrayList, projectStringArrayList, tokensStringArrayList, accountTypeStringArrayList;
    //public ArrayList<TokensModel> tokensModelArrayList;

    private int selectedEventId = 0,paymentMode=0,user_id=0, mYear, mMonth, mDay,
            lead_id=0,isDownloadViewShare=0, token_type_id=0,lead_status_id =0, // TODO lead_status_id == 13 GHP payment pending
            selectedProjectId=0,event_id=0,token_id=0, paidVia = 3;  // TODO paidVia ==> 1 - Manual, 2- Pay Gateway,  3 - Pay Link

    public String  selectedTokenAmount = "0",setChequeDate="",customer_name="", customer_mobile="", customer_email="",
            TAG = "UpgradeGHPPlusActivity",api_token="",country_code="91", order_id = null,
            transaction_id = "0", payment_link = "", payment_invoice_id = "",token_document=null, token_No ="",ghp_date="",ghp_amount="",ghp_plus_date="",
            ghp_plus_amount="",ghp_plus_remarks="";

    private static final int Permission_CODE_DOC_SHARE = 324;
    private File file;
    private ShareDialog shareDialog;
    public CallbackManager callbackManager;

    public CUIDModel cuidModel;
    //TODO fromOther ==> 1->Site Visit,  2->Generate Token, 3-> From feed to view Token Details
    // paymentMode ==> 1 -> Cheque, 2-> OT, 3-> Card
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_ghp_plus);
        ButterKnife.bind(this);
        context = UpgradeGHPPlusActivity.this;
        anim = new Animations();
        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.upgrade_to_ghp_plus));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //hide pb
        hideProgressBar();

        //hide main
        ll_main.setVisibility(View.GONE);

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        edt_ghp_amount.setText("0");
        paymentModeModelArrayList = new ArrayList<>();

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(context);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {

                Log.e("",  "postID "+result.getPostId());
                new Helper().showCustomToast(context, "Document Shared Successfully..!");
               /* if (result.getPostId()!=null) Toast.makeText(EventDetailActivity.this, "Event Shared Successfully..!", Toast.LENGTH_LONG).show();
                else Toast.makeText(EventDetailActivity.this, "You cancelled this share!", Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onCancel() {
                new Helper().showCustomToast(context, "You cancelled this share!");
            }

            @Override
            public void onError(FacebookException error) {
                new Helper().showCustomToast(context, "Error occurred during this share!");
            }
        });

        //Get From Other For Site Visit or Add Token
        if (getIntent()!=null)
        {
            //from feeds to view token details
            lead_status_id = getIntent().getIntExtra("lead_status_id", 0);

            //Get Customer Model
            cuidModel= (CUIDModel) getIntent().getSerializableExtra("cuidModel");
            if(cuidModel!=null)
            {
                //From Direct CuId Activity
                event_id= cuidModel.getEvent_id();
                token_id= cuidModel.getToken_id();
            }
        }


        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Leado_Sales/Tokens/");
        //file = new File(dirFile.getPath());

        //set required data expanded
        new Handler(getMainLooper()).postDelayed(() -> {

            //def set expandView to payment modes
            anim.toggleRotate(iv_paymentMode_ec, true);
            //expandSubView(ll_viewLeadDetails);
            viewPaymentModes = true;

        }, 100);



        //Get Tokens Data
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            //showProgressBar(getString(R.string.loading_content));
            showShimmer();
            new Thread(this::getTokenGHPInfo).start();
        }
        else {
            //hide shimmer
            hideShimmer();

            //hide main
            ll_main.setVisibility(View.GONE);

            //show no data
            ll_noData.setVisibility(View.VISIBLE);

            Helper.NetworkError(context);
        }


        //Set toggleViews
        setToggleView();

        //Set Cheque Date
        edt_cheque_date.setOnClickListener(view -> selectChequeDate());

        //Generate Token
        mBtn_upgradeGHP.setOnClickListener(view -> {

            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidation();
        });


    }


    @Override
    public void onResume()
    {
        super.onResume();
    }


    private void setToggleView()
    {


        //view payment modes
        ll_paymentModeMain.setOnClickListener(v -> {
            if (viewPaymentModes)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_paymentMode_ec, false);
                collapse(ll_paymentModes);
                viewPaymentModes = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(iv_paymentMode_ec, true);
                expandSubView(ll_paymentModes);
                viewPaymentModes = true;
            }
        });
    }


    private void selectChequeDate()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
                    setChequeDate = Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    edt_cheque_date.setText(setChequeDate);
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }


    private void getTokenGHPInfo()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().Get_TokenGHPInfo(api_token,event_id,token_id);
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
                        getTokenInformation();
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
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;
                                    if (isSuccess == 1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject()) {
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                setGeneratedTokensJson(jsonObject);
                                            }
                                        }
                                    } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setGeneratedTokensJson(JsonObject jsonObject)
    {
        if(context!=null)
        {
            runOnUiThread(() -> {
                ghpPlusModel=new GHPPlusModel();
                if (jsonObject.has("token_id")) ghpPlusModel.setToken_id(!jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0 );
                if (jsonObject.has("token_no")) ghpPlusModel.setToken_no(!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() : "" );
                if (jsonObject.has("token_type_id")) ghpPlusModel.setToken_type_id(!jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0 );
                if (jsonObject.has("event_id")) ghpPlusModel.setEvent_id(!jsonObject.get("event_id").isJsonNull() ? jsonObject.get("event_id").getAsInt() : 0 );
                if (jsonObject.has("event_title")) ghpPlusModel.setEvent_title(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() : "" );
                if (jsonObject.has("default_amount")) ghpPlusModel.setDefault_amount(!jsonObject.get("default_amount").isJsonNull() ? jsonObject.get("default_amount").getAsString() : "" );
                if (jsonObject.has("amount")) ghpPlusModel.setAmount(!jsonObject.get("amount").isJsonNull() ? jsonObject.get("amount").getAsString() : "" );
                if (jsonObject.has("token_type")) ghpPlusModel.setToken_type(!jsonObject.get("token_type").isJsonNull() ? jsonObject.get("token_type").getAsString() : "" );
                if (jsonObject.has("lead_id")) ghpPlusModel.setLead_id(!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 );
                if (jsonObject.has("lead_uid")) ghpPlusModel.setLead_uid(!jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" );
                if (jsonObject.has("unit_category")) ghpPlusModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
                if (jsonObject.has("project_id")) ghpPlusModel.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
                if (jsonObject.has("project_name")) ghpPlusModel.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
                if (jsonObject.has("country_code")) ghpPlusModel.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "" );
                if (jsonObject.has("mobile_number")) ghpPlusModel.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
                if (jsonObject.has("email")) ghpPlusModel.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
                if (jsonObject.has("full_name")) ghpPlusModel.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );

                //Payment Mode Status
                if (jsonObject.has("payment_modes") && !jsonObject.get("payment_modes").isJsonNull())
                {
                    if (jsonObject.get("payment_modes").isJsonArray())
                    {
                        JsonArray jsonArray =jsonObject.get("payment_modes").getAsJsonArray();
                        if (jsonArray.size()>0)
                        {
                            paymentModeModelArrayList.clear();
                            for (int j = 0; j < jsonArray.size(); j++) {
                                setPaymentModesJson(jsonArray.get(j).getAsJsonObject());
                            }
                        }
                    }
                }

                if (jsonObject.has("token_types") && !jsonObject.get("token_types").isJsonNull())
                {
                    if (jsonObject.get("token_types").isJsonArray())
                    {
                        JsonArray jsonArray = jsonObject.get("token_types").getAsJsonArray();
                        if (jsonArray.size()>0)
                        {
                            ArrayList<TokensModel> arrayList = new ArrayList<>();
                            arrayList.clear();
                            for (int j = 0; j < jsonArray.size(); j++) {
                                setTokensDataJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                            }
                            ghpPlusModel.setTokensModelArrayList(arrayList);

                            //check for ghp plus type
                            // ghpPlusModel.setSalesTeamLead(checkTokenType(ghpPlusModel.getTokensModelArrayList()));

                            //check for is GHP plus available
                            isGHPPlusAvailable =  isGHPPlusAvailable(arrayList);
                        }
                    }
                }


            });
        }

    }


    private void setTokensDataJson(JsonObject jsonObject, ArrayList<TokensModel> arrayList)
    {
        TokensModel myModel = new TokensModel();
        if (jsonObject.has("token_type_id")) myModel.setToken_type_id(!jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0 );
        if (jsonObject.has("token_type")) myModel.setToken_type(!jsonObject.get("token_type").isJsonNull() ? jsonObject.get("token_type").getAsString() : "" );
        if (jsonObject.has("short_code")) myModel.setShort_code(!jsonObject.get("short_code").isJsonNull() ? jsonObject.get("short_code").getAsString() : "" );
        if (jsonObject.has("is_approval")) myModel.setIs_approval(!jsonObject.get("is_approval").isJsonNull() ? jsonObject.get("is_approval").getAsInt() : 0 );
        if (jsonObject.has("default_amount")) myModel.setDefault_amount(!jsonObject.get("default_amount").isJsonNull() ? jsonObject.get("default_amount").getAsString() : "0" );
        if (jsonObject.has("amount")) myModel.setAmount(!jsonObject.get("amount").isJsonNull() ? jsonObject.get("amount").getAsString() : "0" );

        arrayList.add(myModel);
    }



    private void setPaymentModesJson(JsonObject jsonObject)
    {
        PaymentModeModel myModel = new PaymentModeModel();
        if (jsonObject.has("payment_modes_id")) myModel.setPayment_id(!jsonObject.get("payment_modes_id").isJsonNull() ? jsonObject.get("payment_modes_id").getAsInt() : 0 );
        if (jsonObject.has("payment_mode")) myModel.setPayment_mode(!jsonObject.get("payment_mode").isJsonNull() ? jsonObject.get("payment_mode").getAsString() : "" );
        paymentModeModelArrayList.add(myModel);
    }


    private void getTokenInformation()
    {
        if(context!=null)
        {
            runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                //hide shimmer
                hideShimmer();

                if (ghpPlusModel!=null)
                {

                    if (isGHPPlusAvailable)
                    {
                        //check if ghp plus option available for this event or not

                        //visible main
                        ll_main.setVisibility(View.VISIBLE);

                        //hide no ghp plus
                        ll_noGHPPlus.setVisibility(View.GONE);

                        //hide no data
                        ll_noData.setVisibility(View.GONE);

                        //set cuId number
                        if(ghpPlusModel.getLead_uid() != null && !ghpPlusModel.getLead_uid().trim().isEmpty()) {
                            mTv_cuIdNumber.setText(ghpPlusModel.getLead_uid());
                            mTv_cuIdNumber.setVisibility(View.VISIBLE);
                        }

                        //set customer name
                        if(ghpPlusModel.getFull_name() != null && !ghpPlusModel.getFull_name().trim().isEmpty()) {
                            mTv_leadName.setText(ghpPlusModel.getFull_name());
                            mTv_leadName.setVisibility(View.VISIBLE);
                        }

                        //customer mobile
                        if(ghpPlusModel.getMobile_number() != null && !ghpPlusModel.getMobile_number().trim().isEmpty()) {
                            mTv_leadMobile.setText(ghpPlusModel.getMobile_number());
                            mTv_leadMobile.setVisibility(View.GONE); // still gone -- not req
                        }else mTv_leadMobile.setVisibility(View.GONE);


                        //set GHP number
                        if(ghpPlusModel.getToken_no() != null && !ghpPlusModel.getToken_no().trim().isEmpty()) {
                            mTv_ghpNumber.setText(ghpPlusModel.getToken_no());
                            mTv_ghpNumber.setVisibility(View.VISIBLE);
                        }

                        //set project name
                        if(ghpPlusModel.getProject_name() != null && !ghpPlusModel.getProject_name().trim().isEmpty()) {
                            mTv_projectName.setText(ghpPlusModel.getProject_name());
                            mTv_projectName.setVisibility(View.VISIBLE);
                        }

                        //set event name
                        if(ghpPlusModel.getEvent_title() != null && !ghpPlusModel.getEvent_title().trim().isEmpty()) {
                            mTv_eventName.setText(ghpPlusModel.getEvent_title());
                            mTv_eventName.setVisibility(View.VISIBLE);
                        }

                        //set ghpType
                        if(ghpPlusModel.getToken_type() != null && !ghpPlusModel.getToken_type().trim().isEmpty()) {
                            mTv_ghpType.setText(ghpPlusModel.getToken_type() );
                            mTv_ghpType.setVisibility(View.VISIBLE);
                        }

                        if(ghpPlusModel.getProject_id() != 0) selectedProjectId =ghpPlusModel.getProject_id();
                        if(ghpPlusModel.getEvent_id() != 0) selectedEventId =ghpPlusModel.getEvent_id();
                        if(ghpPlusModel.getToken_id() != 0) token_id =ghpPlusModel.getToken_id();
                        if(ghpPlusModel.getLead_id() != 0) lead_id =ghpPlusModel.getLead_id();
                        if(ghpPlusModel.getEmail() != null) customer_email =ghpPlusModel.getEmail();
                        if(ghpPlusModel.getMobile_number() != null) customer_mobile =ghpPlusModel.getMobile_number();
                        if(ghpPlusModel.getFull_name() != null) customer_name =ghpPlusModel.getFull_name();

                        //Get Payment Mode list
                        getPaymentModeList();

                        //set payMode options
                        setPaymentModeOption();

                        //calculate ghp amount
                        if((ghpPlusModel.getAmount()!=null && !ghpPlusModel.getAmount().trim().isEmpty()&& !ghpPlusModel.getAmount().equalsIgnoreCase("0")))
                        {
                            Double cal_amount =  calculateGHPAMOUNT(ghpPlusModel.getAmount(),getGHP_plusAmount(ghpPlusModel.getTokensModelArrayList()));
                            selectedTokenAmount=String.valueOf(cal_amount);
                            edt_ghp_amount.setText(String.format("%s", new DecimalFormat("##.##").format(cal_amount)));
                        }
                        else  edt_ghp_amount.setText("0");

                    }
                    else
                    {
                        //show msg GHP plus not available and do backPress

                        //hide main
                        ll_main.setVisibility(View.GONE);

                        //show no ghp plus
                        ll_noGHPPlus.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(this::onBackPressed, 6000);

                    }
                }
                else
                {
                    //hide main
                    ll_main.setVisibility(View.GONE);

                    //show no data
                    ll_noData.setVisibility(View.VISIBLE);

                }

            });
        }

    }

    private void getPaymentModeList()
    {

        if (paymentModeModelArrayList.size()>0) {

            final RadioButton[] rb = new RadioButton[paymentModeModelArrayList.size()];
            RadioGroup rg = new RadioGroup(this); //create the RadioGroup
            rg.setOrientation(RadioGroup.VERTICAL);//or RadioGroup.VERTICAL
            ll_addPaymentMode.removeAllViews();
            for (int i =0; i< paymentModeModelArrayList.size(); i++) {
                rb[i]  = new RadioButton(context);
                rb[i].setText(paymentModeModelArrayList.get(i).getPayment_mode());
                rb[i].setId(i);
                rb[i].setTextSize(16);
                rb[i].setTextColor(getResources().getColor(R.color.colorAccent));
                rb[i].setButtonDrawable(getResources().getDrawable(R.drawable.radio_button_selector));
                rb[i].setPadding(30,15,30,15);
                rb[i].setGravity(Gravity.CENTER);
                rg.addView(rb[i]);
            }
            rb[0].setChecked(false);
            rg.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {

                int selectedId = rg.getCheckedRadioButtonId();
                paymentMode = paymentModeModelArrayList.get(selectedId).getPayment_id();
                final RadioButton rbtn = rg.findViewById(selectedId);
                final String selectedName =  rbtn.getText().toString();
                Log.e(TAG, "selectedName: "+ paymentMode +"\t "+ selectedName );

                if (selectedName.equalsIgnoreCase("Cheque")) {
                    //show cheque details
                    ll_cheque_payment.setVisibility(View.VISIBLE);
                    ll_card_payment.setVisibility(View.GONE);
                    ll_online_payment.setVisibility(View.GONE);
                }
                else if (selectedName.equalsIgnoreCase("Card")) {
                    //show card details
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.VISIBLE);
                    ll_online_payment.setVisibility(View.GONE);
                }
                else if (selectedName.equalsIgnoreCase("Online Transfer")) {
                    //Online Transfer
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.GONE);
                    ll_online_payment.setVisibility(View.VISIBLE);
                }

                /*if(paymentMode == 1)
                {
                    //cheque
                    ll_cheque_payment.setVisibility(View.VISIBLE);
                    ll_card_payment.setVisibility(View.GONE);
                    ll_online_payment.setVisibility(View.GONE);
                }else if (paymentMode == 2)
                {
                    //ot
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.GONE);
                    ll_online_payment.setVisibility(View.VISIBLE);
                }else if (paymentMode == 3)
                {
                    //card
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.VISIBLE);
                    ll_online_payment.setVisibility(View.GONE);
                }*/

            });


            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(15, 10, 0, 15);
            //ll_main.setLayoutParams(params);

            ll_addPaymentMode.addView(rg);//you add the whole RadioGroup to the layout
        }

    }


    private void setPaymentModeOption()
    {
        //edit contact
        rGrp_payMode.setOnCheckedChangeListener((radioGroup, i) -> {
            int id = rGrp_payMode.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rBtn_upgradeGHP_pay_manual:
                    paidVia = 1;
                    //generate GHP
                    mBtn_upgradeGHP.setText(getString(R.string.upgrade_to_ghp_plus));
                    expandSubView(ll_payMode_manual);
                    break;

                case R.id.rBtn_upgradeGHP_pay_payGateway:
                    paidVia = 2;
                    //pay and generate GHP
                    mBtn_upgradeGHP.setText(getString(R.string.pay_n_upgrade_ghp_plus));
                    collapse(ll_payMode_manual);
                    break;

                case R.id.rBtn_upgradeGHP_pay_viaPayLink:
                    paidVia = 3;
                    // generate GHP and paid via pay link
                    mBtn_upgradeGHP.setText(getString(R.string.upgrade_to_ghp_plus));
                    collapse(ll_payMode_manual);
                    break;

            }
        });
    }




    private String getGHP_plusAmount(ArrayList<TokensModel> tokensModelArrayList)
    {
        if(tokensModelArrayList!=null && tokensModelArrayList.size()>0)
        {
            //Check ghp plus type 3 in array
            for (TokensModel model: tokensModelArrayList)
            {
                if(model.getToken_type_id()==3) {
                    return model.getAmount();
                }
            }
        }
        return "0";
    }

    private boolean isGHPPlusAvailable(ArrayList<TokensModel> tokensModelArrayList)
    {
        if(tokensModelArrayList!=null && tokensModelArrayList.size()>0) {
            //Check ghp plus type 3 in array
            for (TokensModel model: tokensModelArrayList) {
                if(model.getToken_type_id()==3) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkValidation()
    {
        if(Double.parseDouble(Objects.requireNonNull(edt_ghp_amount.getText()).toString())<=0.0)new Helper().showCustomToast(this, "GHP+ Amount should be greater than zero!");
            //payment mode 0
        else if (paidVia ==1  && paymentMode == 0 ) new Helper().showCustomToast(this, "Please select payment mode!");
            //payment mode 1 && cheque no empty
        else if (paidVia ==1  && paymentMode ==1 && Objects.requireNonNull(edt_cheque_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Cheque Number");
            //cheque date empty
        else if (paidVia ==1  && paymentMode ==1 && Objects.requireNonNull(edt_cheque_date.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Select Cheque Date");
            //cheque_issuer_name empty
        else if (paidVia ==1  && paymentMode ==1 && Objects.requireNonNull(edt_cheque_issuer_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Cheque Issuer Name");
            //cheque_bank_name empty
        else if (paidVia ==1  && paymentMode ==1 && Objects.requireNonNull(edt_cheque_bank_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Bank Name");
            //utr_number empty
        else if (paidVia ==1  && paymentMode ==2 && Objects.requireNonNull(edt_utr_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter UTR Number");
            //transaction_number empty
        else if (paidVia ==1  && paymentMode ==3 && Objects.requireNonNull(edt_transaction_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Transaction Number");
            //invoice_number empty
        else if (paidVia ==1  && paymentMode ==3 && Objects.requireNonNull(edt_invoice_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Invoice Number");

            //Confirm Alert
        else tokenConfirmationAlert();

    }


    public void tokenConfirmationAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        AppCompatTextView tv_msg,tv_desc;
        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_desc.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        tv_msg.setText(getResources().getString(R.string.confirm_details));
        //tv_desc.setText(getString(R.string.ghp_generate_confirm_text, customer_name));
        tv_desc.setText(getString(R.string.ghp_upgrade_confirm_text, customer_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));


        btn_positiveButton.setOnClickListener(view -> {
            if (Helper.isNetworkAvailable(this)) {

                //kyc already done before

                // check for manual pay or payment gateway pay or pay link
                if (paidVia !=2) {
                    //manual pay - for manual pay get the payment details manually and Generate GHP
                    //or pay via link - for pay via link,  generate GHP only but show status payment is pending

                    // check for site visit done before or not
                    showProgressBar("Upgrading to GHP+ ...");
                    Post_GHPPlusDetails();
                }
                else {

                    //payment gateway pay
                    // first pay using customer's payment details and then generate the GHP
                    Double donatedAmt = Double.parseDouble(selectedTokenAmount!=null ? selectedTokenAmount : "0") * 100; //converting amount Rupee into Paise
                    if(donatedAmt > 0.0) {
                        showProgressBar("Upgrading to GHP+ ...");
                        new Handler().postDelayed(() -> call_generatePayOrderId(donatedAmt), 500);
                    }
                    else {
                        new Helper().showCustomToast(context, "GHP Amount Rs. 0.00! Try Another GHP Type.");
                    }
                }

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


    private void call_generatePayOrderId(Double donatedAmt) {
        ApiClient client = ApiClient.getInstance();
        String userName = context.getString(R.string.razorPay_live_key);
        String password = context.getString(R.string.razorPay_live_key_secrete);
        String base = userName + ":" + password;
        String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", donatedAmt);
        jsonObject.addProperty("currency", "INR");
        jsonObject.addProperty("receipt", "order_rcptid_11");
        jsonObject.addProperty("payment_capture", 1);

        client.getApiService().generatePayOrder(authHeader, jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {

                    if (response.body() != null && response.body().isJsonObject()) {

                        if (response.body().has("error")) {

                            //some error occurred generating Order Id
                            if (!response.body().get("error").isJsonNull() && response.body().get("error").isJsonObject()) {
                                JsonObject object = response.body().get("error").getAsJsonObject();
                                if (object.has("description")) showErrorLogRenewal(!object.get("description").isJsonNull() ? object.get("description").getAsString() : "Error occurred generating payment order_id!");
                            } else showErrorLogRenewal("Error occurred generating payment order_id!");
                        } else {
                            //order id generated successfully
                            if (response.body().has("id")) order_id = !response.body().get("id").isJsonNull() ? response.body().get("id").getAsString() : null;
                            if (order_id != null) startPayment(donatedAmt, order_id);
                            else showErrorLogRenewal("Failed to initiate payment! Please try again!");
                        }
                    }
                } else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogRenewal(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogRenewal(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogRenewal(context.getString(R.string.unknown_error_try_again) + " " + response.code());
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogRenewal(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogRenewal(context.getString(R.string.weak_connection));
                else showErrorLogRenewal(e.toString());
            }
        });
    }


    private void startPayment(Double renewalAmt, String order_id) {

        /*
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        /*
         * Set your logo here
         */
        //checkout.setImage(R.drawable.img_temp_app_icon_new);
        //Disable Checkout in Full screen
        checkout.setFullScreenDisable(true);

        /*
         * Reference to current activity
         * You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = context;

        try {
            /*
             * Pass your payment options to the Razorpay Checkout as a JSONObject
             */
            JSONObject options = new JSONObject();

            /*
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "VJ Developers");

            /*
             * Description can be anything
             * eg: Order #123123
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "GHP Amount");

            //You can omit the image option to fetch the image from dashboard
            //options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("image", "http://13.233.119.76/Private_school/public/img_app_icon.png");
            options.put("image", WebServer.paymentLogo);
            //options.put("image", getResources().getDrawable(R.drawable.img_app_icon));
            options.put("currency", "INR");
            /*
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            options.put("amount", renewalAmt);

            //for auto-capture payment
            options.put("payment_capture", 1);
            //order id auto-capture payment
            options.put("order_id", order_id);


            JSONObject preFill = new JSONObject();
//            preFill.put("email", "test@razorpay.com");
//            preFill.put("contact", "9876543210");

            // preFill.put("email", email);
            preFill.put("email", customer_email);
            preFill.put("contact", customer_mobile);

            options.put("prefill", preFill);

            checkout.open(activity, options);

            //Log.e(TAG, "startPayment: json "+ options.toString() );

        } catch (Exception e) {

            Log.e(TAG, "startPayment: " + e.toString());

            new Helper().showCustomToast(activity, "Error in payment: " + e.getMessage());
            //Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * The name of the function has to be
     * onPaymentSuccess
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */

    @Override
    public void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData) {
        try {


            //  Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

            Log.e(TAG, "razorPayPaymentID  " + razorpayPaymentID);
            if (razorpayPaymentID != null && !razorpayPaymentID.trim().isEmpty()) {
                transaction_id = razorpayPaymentID;

                //Log.e(TAG, "paymentData: "+ paymentData.getData().toString() );
                //Log.e(TAG, "pd paymentID: "+ paymentData.getPaymentId());
                //Log.e(TAG, "pd order_Id: "+paymentData.getOrderId());
                order_id = paymentData.getOrderId();

                if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

                    //TODO update
                    showProgressBar("Confirming Payment details...");
                    Post_GHPPlusDetails();

                } else Helper.NetworkError(context);

            } else showErrorLogRenewal("Error Occurred during payments!");

        } catch (Exception e) {

            //hide pb
            hideProgressBar();

            e.printStackTrace();
            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }
    }


    /**
     * The name of the function has to be
     * onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    @Override
    public void onPaymentError(int i, String response, PaymentData paymentData) {
        try {
            //  Toast.makeText(this, "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();
            new Helper().showCustomToast(context, " " + response);

            Log.e(TAG, "response  " + response);
            Log.e(TAG, "paymentData onError: " + paymentData.getData().toString());
            Log.e(TAG, "pd paymentID onError: " + paymentData.getPaymentId());

            //hide pb
            hideProgressBar();

        } catch (Exception e) {
            e.printStackTrace();

            //hide pb
            hideProgressBar();

            Log.e(TAG, "Exception in onPaymentError", e);
        }
    }

    private void Post_GHPPlusDetails()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id",user_id);
        jsonObject.addProperty("cancelled_token_id",token_id);
        jsonObject.addProperty("lead_id",lead_id);

        //if pay using payment gateway then payment mode id should be 4
        //if pay using pay link then payment mode id should be 5
        jsonObject.addProperty("payment_mode_id", paidVia ==1 ? paymentMode : paidVia ==2 ? 4 : 5);
        jsonObject.addProperty("project_id",selectedProjectId);
        jsonObject.addProperty("event_id",selectedEventId);
        jsonObject.addProperty("token_type_id",3);
        jsonObject.addProperty("remark", Objects.requireNonNull(edt_remarks.getText()).toString());
        jsonObject.addProperty("amount", Objects.requireNonNull(edt_ghp_amount.getText()).toString());

        if(paidVia ==1 &&  paymentMode == 1)//Cheque
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();
            JSONObject json3= new JSONObject();
            JSONObject json4= new JSONObject();
            try {
                json1.put("payment_mode_details_title","Check No");
                json1.put("payment_mode_details_description",edt_cheque_number.getText());
                json2.put("payment_mode_details_title","Check Date");
                json2.put("payment_mode_details_description",setChequeDate);
                json3.put("payment_mode_details_title","Check Issuer");
                json3.put("payment_mode_details_description",edt_cheque_issuer_name.getText());
                json4.put("payment_mode_details_title","Bank Name");
                json4.put("payment_mode_details_description",edt_cheque_bank_name.getText());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonArray.put(json2);
            jsonArray.put(json3);
            jsonArray.put(json4);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }
        else if(paidVia ==1 && paymentMode == 2)//Online
        {

            JSONObject json1= new JSONObject();

            try {
                json1.put("payment_mode_details_title","UTR No");
                json1.put("payment_mode_details_description",edt_utr_number.getText());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));

        }
        else if(paidVia ==1 && paymentMode == 3)//Card
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();

            try {
                json1.put("payment_mode_details_title","Transaction No");
                json1.put("payment_mode_details_description",edt_transaction_number.getText());
                json2.put("payment_mode_details_title","Invoice No");
                json2.put("payment_mode_details_description",edt_invoice_number.getText());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonArray.put(json2);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }
        else if (paidVia ==2)
        {
            //paid by payment gateway paymentMode == 4

            // payment modes json
            JSONObject json1 = new JSONObject();
            JSONObject json2 = new JSONObject();
            try {
                json1.put("payment_mode_details_title", "transaction_id");
                json1.put("payment_mode_details_description", transaction_id);

                json2.put("payment_mode_details_title", "order_id");
                json2.put("payment_mode_details_description", order_id != null ? order_id : "");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonArray.put(json2);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));

        }
        else {
            //paid via payment link, paymentMode ==5

            // payment modes json
            JSONObject json1 = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_updateToken(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1"))
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setTokenSuccessJson(data);
                                    onSuccessTokenGenerate();
                                }
                                else showErrorLogRenewal("Server response is empty!");

                            }else showErrorLogRenewal("Invalid response from server!");

                        }
                    }
                }
                else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLogRenewal(context.getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogRenewal(context.getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogRenewal(context.getString(R.string.unknown_error_try_again) + " " + response.code());
                            break;
                    }
                }


            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLogRenewal(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogRenewal(getString(R.string.weak_connection));
                else showErrorLogRenewal(e.toString());
            }
        });
    }


    private void setTokenSuccessJson(JsonObject jsonObject)
    {
        TokensModel tokensModel = new TokensModel();
        //if (jsonObject.has("lead_id")) lead_Id=!jsonObject.get("lead_id").isJsonNull()&&!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsString() : "0";
        if (jsonObject.has("token_type_id")) tokensModel.setToken_type_id((!jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0));
        if (jsonObject.has("token_type_id")) token_type_id = !jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0;
        if (jsonObject.has("token_no")) tokensModel.setToken_type((!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString().trim() : ""));
        if (jsonObject.has("token_no")) token_No = !jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString().trim() : "";
        if (jsonObject.has("payment_link")) tokensModel.setPayment_link((!jsonObject.get("payment_link").isJsonNull() ? jsonObject.get("payment_link").getAsString().trim() : ""));
        if (jsonObject.has("payment_link")) payment_link = !jsonObject.get("payment_link").isJsonNull() ? jsonObject.get("payment_link").getAsString().trim() : "";
        if (jsonObject.has("payment_invoice_id")) tokensModel.setPayment_invoice_id(!jsonObject.get("payment_invoice_id").isJsonNull() ? jsonObject.get("payment_invoice_id").getAsString().trim() : "");
        if (jsonObject.has("payment_invoice_id")) payment_invoice_id = !jsonObject.get("payment_invoice_id").isJsonNull() ? jsonObject.get("payment_invoice_id").getAsString().trim() : "";

        if (jsonObject.has("ghp_date"))ghp_date=!jsonObject.get("ghp_date").isJsonNull() ? jsonObject.get("ghp_date").getAsString() :"";
        if (jsonObject.has("ghp_amount"))ghp_amount=!jsonObject.get("ghp_amount").isJsonNull() ? jsonObject.get("ghp_amount").getAsString() :"";
        if (jsonObject.has("ghp_plus_date"))ghp_plus_date=!jsonObject.get("ghp_plus_date").isJsonNull() ? jsonObject.get("ghp_plus_date").getAsString() :"";
        if (jsonObject.has("ghp_plus_amount"))ghp_plus_amount=!jsonObject.get("ghp_plus_amount").isJsonNull() ? jsonObject.get("ghp_plus_amount").getAsString() :"";
        if (jsonObject.has("remarks"))ghp_plus_remarks=!jsonObject.get("remarks").isJsonNull() ? jsonObject.get("remarks").getAsString() :"";

        if (jsonObject.has("token_media_path")) {
            tokensModel.setToken_document_path((!jsonObject.get("token_media_path").isJsonNull() ? jsonObject.get("token_media_path").getAsString().trim() : ""));
            token_document = !jsonObject.get("token_media_path").isJsonNull() ? jsonObject.get("token_media_path").getAsString().trim() : null;
        }
        Log.e(TAG, "setTokenSuccessJson: "+token_document);
    }



    private void onSuccessTokenGenerate()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //set ghp plus generated flag to true
            ghp_plus_generated = true;

            if (paidVia==3){
                //show pending msg
                new Helper().showCustomToast(context, "GHP+ Pending!");

                //set lead status id to 13
                lead_status_id =13; //ghp pending
            }
            else {
                //show success msg
                new Helper().showSuccessCustomToast(context, "GHP+ Generated Successfully!");
            }

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

            // gotoHomeScreen();

            //set customer data
            setShareTokenCustomerData();
            //show/ hide layouts
            Objects.requireNonNull(getSupportActionBar()).hide();
            change_status_bar_color(R.color.main_white);
            ll_shareToken_View.setVisibility(View.VISIBLE);
            ll_main.setVisibility(View.GONE);

        });
    }


    private void setShareTokenCustomerData()
    {
        Log.e(TAG, "setShareTokenCustomerData: "+ghp_date+""+ghp_amount+""+ghp_plus_date+""+ghp_plus_amount );

        //check if lead_status_id =13 or paidVia =3  (GHP Pending case)
        if (lead_status_id==13)
        {
            // GHP payment pending

            //1. set pending image
            Glide.with(context)
                    .load(getResources().getDrawable(R.drawable.ic_report_52dp))
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .apply(new RequestOptions().fitCenter())
                    .apply(new RequestOptions().placeholder( R.drawable.ic_report_52dp ))
                    .apply(new RequestOptions().error( R.drawable.ic_report_52dp))
                    .priority(Priority.HIGH)
                    .into(iv_shareToken_successImg);

            //2. set message to GHP pending
            tv_shareToken_successMsg.setText(getString(R.string.ghp_plus_request_generated));
            tv_shareToken_successMsg.setTextColor(getResources().getColor(R.color.color_pending));
            //2.1 visible payment pending msg
            tv_shareToken_paymentPendingMsg.setText(getString(R.string.payment_pending));
            tv_shareToken_paymentPendingMsg.setVisibility(View.VISIBLE);
            //2.2 change title text
            //set GHP paid date and Paid Amount text
            tv_ghp_date_text.setText(getString(R.string.ghp_request_date));
            tv_ghp_amount_text.setText(getString(R.string.ghp_amount_payable));
            //set GHP+ request date and payable Amount
            tv_ghp_plus_date_text.setText(getString(R.string.ghp_plus_request_date));
            tv_ghp_plus_amount_text.setText(getString(R.string.ghp_plus_amount_payable));


            //3. hide token number
            tv_Customer_GHPNo.setVisibility(View.GONE);


            //5. hide share token buttons view
            ll_ShareToken_View_share_download.setVisibility(View.GONE);

            //6. visible payment pending view
            ll_paymentPending.setVisibility(View.VISIBLE);


        }
        else
        {
            // GHP generated successfully


            //1. set success image
            Glide.with(context)
                    .load(getResources().getDrawable(R.drawable.ic_success))
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .apply(new RequestOptions().fitCenter())
                    .apply(new RequestOptions().placeholder( R.drawable.ic_success ))
                    .apply(new RequestOptions().error( R.drawable.ic_success))
                    .priority(Priority.HIGH)
                    .into(iv_shareToken_successImg);

            //2. set message to GHP generated successfully
            tv_shareToken_successMsg.setText(getString(R.string.ghp_upgrade_success));
            tv_shareToken_successMsg.setTextColor(getResources().getColor(R.color.color_success));
            //2.1 hide payment pending msg
            tv_shareToken_paymentPendingMsg.setVisibility(View.GONE);
            //2.2 change title text
            //set GHP paid date and Paid Amount text
            tv_ghp_date_text.setText(getString(R.string.ghp_paid_date));
            tv_ghp_amount_text.setText(getString(R.string.ghp_paid_amount));
            //setGHP paid date and Paid Amount text
            tv_ghp_plus_date_text.setText(getString(R.string.ghp_plus_paid_date));
            tv_ghp_plus_amount_text.setText(getString(R.string.ghp_plus_paid_amount));


            //3. visible token number as per condition
            if(token_No != null && !token_No.trim().isEmpty()) {
                tv_Customer_GHPNo.setText(token_No);
                tv_Customer_GHPNo.setVisibility(View.VISIBLE);
            }


            //5. visible share token buttons view
            ll_ShareToken_View_share_download.setVisibility(View.VISIBLE);

            //6. hide payment pending view
            ll_paymentPending.setVisibility(View.GONE);

        }


        if(customer_name != null && !customer_name.trim().isEmpty()) {
            tv_Customer_name.setText(customer_name);
            tv_Customer_name.setVisibility(View.VISIBLE);
        }

        if(customer_mobile != null && !customer_mobile.trim().isEmpty()) {
            tv_Customer_mobile.setText(customer_mobile);
            tv_Customer_mobile.setVisibility(View.VISIBLE);
        }

        if(customer_email != null && !customer_email.trim().isEmpty())
        {
            tv_Customer_email.setText(customer_email);
            tv_Customer_email.setVisibility(View.VISIBLE);
        }

        if(ghpPlusModel.getProject_name() != null && !ghpPlusModel.getProject_name().trim().isEmpty()) {
            tv_Customer_ProjectName.setText(ghpPlusModel.getProject_name());
            tv_Customer_ProjectName.setVisibility(View.VISIBLE);
        }

        if(ghpPlusModel.getEvent_title() != null && !ghpPlusModel.getEvent_title().trim().isEmpty()) {
            tv_Customer_EventName.setText(ghpPlusModel.getEvent_title());
            tv_Customer_EventName.setVisibility(View.VISIBLE);
        }

        if(ghpPlusModel.getAmount()  != null && !ghpPlusModel.getAmount() .trim().isEmpty()) {
            tv_Customer_GHP_Amount.setText(ghpPlusModel.getAmount() );
            ll_ShareToken_ghpAmount.setVisibility(View.VISIBLE);
        }

        if(ghp_date != null && !ghp_date.trim().isEmpty()) {
            tv_Customer_GHP_Date.setText(ghp_date);
            ll_ShareToken_ghpDate.setVisibility(View.VISIBLE);
        }

        if(ghp_amount != null && !ghp_amount.trim().isEmpty()) {
            tv_Customer_GHP_Amount.setText(ghp_amount);
            ll_ShareToken_ghpAmount.setVisibility(View.VISIBLE);
        }

        if(ghp_plus_date != null && !ghp_plus_date.trim().isEmpty()) {
            tv_Customer_GHPPlus_Date.setText(ghp_plus_date);
            ll_ShareToken_ghpPlusDate.setVisibility(View.VISIBLE);
        }

        if(ghp_plus_amount != null && !ghp_plus_amount.trim().isEmpty()) {
            tv_Customer_GHPPlus_Amount.setText(ghp_plus_amount);
            ll_ShareToken_ghpPlusAmount.setVisibility(View.VISIBLE);
        }
        if(ghp_plus_remarks != null && !ghp_plus_remarks.trim().isEmpty()) {
            tv_upgradeGHP_ShareToken_remarks.setText(ghp_plus_remarks);
            //ll_upgradeGHP_ShareToken_remarks.setVisibility(View.VISIBLE);
        }


       /* if(ghp_date != null && !ghp_date.trim().isEmpty()) {
            tv_Customer_GHP_Date.setText(ghp_date);
            ll_ShareToken_ghpDate.setVisibility(View.VISIBLE);
        }*/



       /* if(ghp_plus_date != null && !ghp_plus_date.trim().isEmpty()) {
            tv_Customer_GHPPlus_Date.setText(ghp_plus_date);
            ll_ShareToken_ghpPlusDate.setVisibility(View.VISIBLE);
        }*/

        if(selectedTokenAmount != null && !selectedTokenAmount.trim().isEmpty()) {
            tv_Customer_GHPPlus_Amount.setText(selectedTokenAmount);
            ll_ShareToken_ghpPlusAmount.setVisibility(View.VISIBLE);
        }

        //close view Token data
        iv_shareToken_close.setOnClickListener(view -> gotoHomeScreen());

        //view_ghpDetails.setVisibility(token_type_id==1 || token_type_id==3 ?View.VISIBLE : View.GONE);
        view_ghpDetails.setVisibility(View.VISIBLE);
        tv_Customer_GHPNo.setTextColor(token_type_id==3  ? getResources().getColor(R.color.color_token_plus_generated) : getResources().getColor(R.color.color_token_generated));


        //MBtn Download Token
        mBtn_shareToken_download.setOnClickListener(view ->{
            isDownloadViewShare = 1;
            if (token_document!=null) isStoragePermissionGranted(token_document, 1);
            else new Helper().showCustomToast(context, getString(R.string.ghp_doc_not_found));
        } );


        //MBtn View Token
        mBtn_shareToken_view.setOnClickListener(view ->{
            isDownloadViewShare = 2;
            if (token_document!=null) isStoragePermissionGranted(token_document, 2);
            else new Helper().showCustomToast(context, getString(R.string.ghp_doc_not_found));

        });

        //MBtn Share Token
        mBtn_shareToken_share.setOnClickListener(view -> {
            isDownloadViewShare = 3;

            //directly send on whatsApp number
            sendMessageToWhatsApp(country_code+customer_mobile, token_document, token_No, ghpPlusModel.getProject_name());

            // if (token_document!=null) isStoragePermissionGranted(token_document, 3);
            // else new Helper().showCustomToast(context, getString(R.string.ghp_doc_not_found));
        });

        // MBtn share Token  more apps
        mBtn_shareToken_MoreShare.setOnClickListener(view -> {

            if (token_document!=null && !token_document.trim().isEmpty()) {

                sendGHPDetailsMoreApps(customer_name, token_document, token_No, ghpPlusModel.getProject_name());
            }
            else new Helper().showCustomToast(context, "GHP document not found!");
        });


        //share payLink on whatsApp
        mBtn_sharePayLink_waShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {
                //directly send link on customer's whatsApp number
                sendPayLinkOnWhatsApp(country_code+customer_mobile, payment_link, ghpPlusModel.getEvent_title(), ghpPlusModel.getProject_name());
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });


        //share payLink on mail
        mBtn_sharePayLink_mailShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {

                //directly send link on customer's email id
                //final String extra_text = "Hello "+customer_name +", \n\n" + "Thank you for showing your interest for Golden Hour Pass(GHP) of project " + ghpPlusModel.getProject_name() + ". \n You are just one step away from becoming a member of VJ Family! "+ "\n\n\n\n Kindly click on the below link and pay for your Golden Hour Pass. And get confirm your GHP for the event "+ ghpPlusModel.getEvent_title() +"." +"\n\n "+ payment_link+ "\n\n";
                final String extra_text = context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, ghpPlusModel.getProject_name(), ghpPlusModel.getEvent_title(), payment_link);
                new Helper().openEmailIntent(context,  customer_email, "Pay and Confirm your Golden Hour Pass(GHP)", extra_text);
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });

        //share payLink on more apps
        mBtn_sharePayLink_moreShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {
                //directly send link on customer's whatsApp number
                sendPayLinkOnMoreApps( payment_link, ghpPlusModel.getEvent_title(), ghpPlusModel.getProject_name());
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });

    }



    private void isStoragePermissionGranted(String path, int isDownloadViewShare)
    {

        //first create parent directory
        File parentDirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Leado_Sales/");//Tokens/
        //create parent directory
        parentDirFile.mkdir();

        //child directory
        File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Leado_Sales/Tokens/");
        //file = new File(dirFile.getPath());

        //StoredPath = DIRECTORY + pic_name + ".png";
        //File file = new File(dirFile, getFileName_from_filePath(path));
        file = new File(dirFile.getPath(), getFileName_from_filePath(path)+".pdf");
        //create file dir
        dirFile.mkdir();
        //Log.e(TAG, "dir_Path:- " +dirFile.getPath());

        if (file.exists())
        {
            //file exists in local -- do your operation directly
            showProgressBar("Opening your document...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkPermission())
                {
                    switch (isDownloadViewShare)
                    {
                        case 1:
                            //download
                            downloadFile(token_document, file, isDownloadViewShare);
                            break;
                        case 2:
                            //view
                            openFileIntent(file);
                            break;
                        case 3:
                            //share
                            shareDataForSocial(file);
                            break;
                    }
                }
                else requestPermissionViewFile(path, file,  isDownloadViewShare);
            }
            else{

                //check operation
                switch (isDownloadViewShare)
                {
                    case 1:
                        //download
                        downloadFile(token_document, file, isDownloadViewShare);
                        break;
                    case 2:
                        //view
                        openFileIntent(file);
                        break;
                    case 3:
                        //share
                        shareDataForSocial(file);
                        break;
                }
            }
        }
        else
        {
            //download and open
            //showProgressBar(getString(R.string.downloading_your_document));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermissionViewFile(path, file, isDownloadViewShare);
            else downloadFile(path, file, isDownloadViewShare);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissionViewFile(String path, File localFile, int isDownloadViewShare)
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {

            //download  //view  //share
            downloadFile(path, localFile, isDownloadViewShare);
            return;
        }
        if ((ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_EXTERNAL_STORAGE)))
        {
            new Helper().showCustomToast(context, context.getString(R.string.file_permissionRationale));
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, Permission_CODE_DOC_SHARE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //accessing permission when token generated
        if (requestCode == Permission_CODE_DOC_SHARE)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, context.getString(R.string.permission_grant_success));

                if (token_document!=null && file!=null)
                {
                    switch (isDownloadViewShare)
                    {
                        case 1:
                            //download
                            downloadFile(token_document, file, isDownloadViewShare);
                            break;
                        case 2:
                            //view
                            openFileIntent(file) ;
                            break;
                        case 3:
                            //share
                            break;
                    }
                }
            }
        }

    }



    private void downloadFile(String url, final File localFile, int isDownloadViewShare)
    {
        if (Helper.isNetworkAvailable(context))
        {
            //show pb
            showProgressBar("Getting your document...");

            try {
                ApiClient client = ApiClient.getInstance();
                Call<ResponseBody> call = client.getApiService().downloadFile(url);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                        if(response.isSuccessful()) {
                            try {
                                saveFile(Objects.requireNonNull(response.body()), localFile, isDownloadViewShare);
                            } catch (IOException e) {
                                e.printStackTrace();
                                context.runOnUiThread(() -> {
                                    //hide pb
                                    hideProgressBar();
                                    new Helper().showCustomToast(context, "Failed to save document!");
                                });
                            }
                        }else {
                            context.runOnUiThread(() -> {

                                //hide pb
                                hideProgressBar();
                                new Helper().showCustomToast(context, "No document found!");
                            });

                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable e) {
                        Log.e(TAG, "onFailure " + e.getMessage());

                        if (e instanceof SocketTimeoutException) showErrorLogRenewal(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLogRenewal(getString(R.string.weak_connection));
                        else showErrorLogRenewal(e.toString());

                        context.runOnUiThread(() -> {
                            //hide pb
                            hideProgressBar();
                            new Helper().showCustomToast(context, "Failed to open document!");

                        });

                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
                new Helper().showCustomToast(context, "Error! No document found!");
            }
        }
        else Helper.NetworkError(context);
    }

    private void saveFile(ResponseBody response, File localFile, int isDownloadViewShare) throws IOException {

        int count;
        byte[] data = new byte[1024 * 4];
        InputStream bis = new BufferedInputStream(response.byteStream(), 1024 * 8);
        OutputStream output = new FileOutputStream(localFile);

        while ((count = bis.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        bis.close();

        context.runOnUiThread(() -> {

//            new Helper().showCustomToast(context, "Receipt successfully downloaded!");
            switch (isDownloadViewShare)
            {
                case 1:
                    //download
                    context.runOnUiThread(() -> new Helper().showSuccessCustomToast(context, "Document downloaded successfully!"));
                    openFileIntent(localFile);
                    break;
                case 2:
                    //view
                    openFileIntent(localFile);
                    break;
                case 3:
                    //share
                    shareDataForSocial(localFile);
                    break;

                default:
                    openFileIntent(localFile);
                    break;
            }
        });
    }



    private void openFileIntent(File file)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        //File file = new File(path);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        //intent.setDataAndType(Uri.fromFile(file), type);
        //context.startActivity(intent);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        {
            //intent.setDataAndType(Uri.fromFile(file), "image/*");
            intent.setDataAndType(Uri.fromFile(file), type);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else
        {

//            Uri fileUri = FileProvider.getUriForFile(AllCustomerDetailActivity.this, "com.crm.crm.fileprovider", localFile);
            //target.setDataAndType(fileUri, "application/pdf");
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(fileUri, type);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        try
        {
            hideProgressBar();
            //pb_donationList.setVisibility(View.GONE);
            context.startActivity(intent);

        }catch(ActivityNotFoundException e)
        {
            hideProgressBar();
            //pb_donationList.setVisibility(View.GONE);
            e.printStackTrace();
            new Helper().showCustomToast(context, "No Application available to view this file!");
        }

    }


    private void shareDataForSocial(File dirFile)
    {
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        final Dialog builder_accept=new BottomSheetDialog(context);
        builder_accept.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder_accept.setContentView(R.layout.layout_share_options_popup);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayoutCompat ll_share_with_fb,ll_share_with_whats_app, ll_share_with_more;
        ll_share_with_whats_app= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option1);
        ll_share_with_fb= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option2);
        ll_share_with_more= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option3);


        //share on WhatsApp
        Objects.requireNonNull(ll_share_with_whats_app).setOnClickListener(view -> {

            shareOnWhatsApp(dirFile);
            builder_accept.dismiss();
        });


        //share on FB
        Objects.requireNonNull(ll_share_with_fb).setOnClickListener(view -> {
            shareOnFB(dirFile);
            builder_accept.dismiss();
        });


        //share on More
        Objects.requireNonNull(ll_share_with_more).setOnClickListener(view -> {

            doNormalShare(dirFile);
            builder_accept.dismiss();
        });

        hideProgressBar();
        builder_accept.show();
    }


    /*Share on Whats App*/
    private void shareOnWhatsApp(File dirFile) {

        final String extra_text = "GHP Document of "+ "\t\n" + customer_name + "\n\n" + "GHP Number :- \t"+token_No;

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setPackage(context.getResources().getString(R.string.pkg_whatsapp));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        {
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(dirFile));
            share.putExtra(Intent.EXTRA_TEXT, extra_text);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
            //share.setType("image/*");
        }
        else
        {
            // Uri photoUri = FileProvider.getUriForFile(AddNewVisitor.this, AddNewVisitor.this.getPackageName() + ".provider", imageFile);
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", dirFile);
            //share.setDataAndType(fileUri, "application/pdf");
            //share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM,fileUri);
            share.putExtra(Intent.EXTRA_TEXT, extra_text);
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
        }

        context.startActivity(Intent.createChooser(share, "Share Using"));
    }

    private void shareOnFB(File dirFile)
    {
        final String extra_text = "GHP Document of "+ "\t\n" + customer_name + "\n\n" + "GHP Number :- \t"+token_No;

        if (ShareDialog.canShow(ShareLinkContent.class))
        {

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.fromFile(dirFile))
                    .setQuote("GHP \n" +extra_text)
                    .setShareHashtag(new ShareHashtag.Builder()
                            //.setHashtag("#PrivateEducationEvents")
                            .build())
                    .build();
            if (Helper.isPackageExisted(context.getApplicationContext(), context.getString(R.string.pkg_fb)))
                shareDialog.show(linkContent);  // Show facebook ShareDialog
            else shareDialog.show(linkContent, ShareDialog.Mode.WEB);

        }
    }

    private void doNormalShare(File dirFile )
    {

        final String extra_text = "GHP Document of "+ "\t\n" + customer_name + "\n\n" + "GHP Number :- \t"+token_No;

        if (!dirFile.getPath().trim().isEmpty()) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            //shareIntent.setType("image/jpeg");
            shareIntent.setType("*/*");
            //shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            // shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, resource));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (android.content.ActivityNotFoundException ex) {
                new Helper().showCustomToast(context, "Apps not found!");
            }

        }
        else
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (android.content.ActivityNotFoundException ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "Apps not found!");
            }
        }


    }


    private void sendMessageToWhatsApp(String number, String main_title, String token_No, String project_name)
    {
        Log.e(TAG, "sendMessageToWhatsApp: "+ main_title );
        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "\n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n"  : "Hello", "UTF-8");
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode("Welcome to VJ family... Thank you for your registration.", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            new Helper().showCustomToast(context, "Error occurred while sharing!");
        }

    }


    private void sendPayLinkOnWhatsApp(String number, String payment_link, String event_name, String project_name)
    {

        //final String extra_text = "Hello "+customer_name +", \n\n" + "Thank you for showing your interest for *Golden Hour Pass(GHP)* of project *" + project_name + "*. \n You are just one step away from becoming a member of VJ Family! "+ "\n\n\n\n Kindly click on the below link and pay for your *Golden Hour Pass.* And get confirm your GHP for the event *"+ event_name +"*." +"\n\n "+ payment_link+ "\n\n";
        final String extra_text = context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link);

        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(payment_link!=null ? extra_text  : "Hello", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setPackage(context.getString(R.string.pkg_whatsapp));
        msgIntent.setData(Uri.parse(url));
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(msgIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            new Helper().showCustomToast(context, "WhatsApp not installed!");
        }
        catch (Exception e) {
            e.printStackTrace();
            new Helper().showCustomToast(context, "Error occurred while sharing!");
        }
    }


    private void sendPayLinkOnMoreApps(String payment_link, String event_name, String project_name)
    {
        //final String extra_text = "Hello "+customer_name +", \n\n" + "Thank you for showing your interest for *Golden Hour Pass(GHP)* of project *" + project_name + "*. \n You are just one step away from becoming a member of VJ Family! "+ "\n\n\n\n Kindly click on the below link and pay for your *Golden Hour Pass.* And get confirm your GHP for the event *"+ event_name +"*." +"\n\n "+ payment_link+ "\n\n";
        final String extra_text = context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share on"));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            new Helper().showCustomToast(Objects.requireNonNull(context), "Apps not found!");
        }
    }

    private void sendGHPDetailsMoreApps(String customer_name, String main_title, String token_No, String project_name)
    {

      /*  String extra_text = null;
        try {
            extra_text = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(main_title!=null ? "\n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n"  : "Hello", "UTF-8");
            extra_text = "Welcome to VJ family!"+ number +"&text=" + URLEncoder.encode(main_title!=null ? "\n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n"  : "Hello", "UTF-8");
            //url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode("Welcome to VJ family... Thank you for your registration.", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        //final String extra_text = "Hello "+"*"+customer_name+"*"+", \n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n";
        final String extra_text = "\n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share on"));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            new Helper().showCustomToast(Objects.requireNonNull(context), "Apps not found!");
        }

    }


    private String getFileName_from_filePath(String filePath)
    {
        //String path=":/storage/sdcard0/DCIM/Camera/1414240995236.jpg";//it contain your path of image..im using a temp string..
        //String filename=filePath.substring(filePath.lastIndexOf("/")+1);
        return filePath.substring(filePath.lastIndexOf("/")+1);
    }


    private void gotoHomeScreen() {

        startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }


    /*Collapsing View*/
    private void collapse(final View v)
    {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linearView.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }

    /*Expandable View*/
    private void expandSubView(final View v)
    {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime==1) v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round,context.getTheme()));
                }else {

                    linear.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_light_grey_more_round));
                }*/

                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);
    }

    /*calculate GHP AMOUNT*/
    private Double calculateGHPAMOUNT( String ghp_amt,String ghp_plus_amt)
    {
        double amount = 0.0;

        if (ghp_amt!=null&& ghp_plus_amt!=null)
        {
            Double ghp_Amt = Double.valueOf(ghp_amt);
            Double ghp_plus_Amt= Double.valueOf(ghp_plus_amt);

            amount= ghp_plus_Amt-ghp_Amt;
        }

        return amount;

    }

    private void change_status_bar_color( int color)
    {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(getResources().getColor(color));
        }
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

    private void showShimmer() {

        sfl.setVisibility(View.VISIBLE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
    }


    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
            context.runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                //hide shimmer
                hideShimmer();

                //hide main
                ll_main.setVisibility(View.GONE);

                //show no data
                ll_noData.setVisibility(View.VISIBLE);

                //ll_pb.setVisibility(View.GONE);
                Helper.onErrorSnack(context, message);

            });
    }



    private void showErrorLogRenewal(final String message) {

        context.runOnUiThread(() ->
        {
            //hide pb
            hideProgressBar();

            //hide shimmer
            hideShimmer();

            Helper.onErrorSnack(context, message);
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(context, view);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (ghp_plus_generated) {
            //goto home screen
            gotoHomeScreen();
        }
        else super.onBackPressed();
        //if (isTokenGenerated) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Token Generated!"));
    }
}
