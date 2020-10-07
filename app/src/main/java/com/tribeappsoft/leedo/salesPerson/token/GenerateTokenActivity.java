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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Html;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.tribeappsoft.leedo.models.leads.PersonNamePrefixModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.salesPerson.models.AccountTypeModel;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.salesPerson.models.PaymentModeModel;
import com.tribeappsoft.leedo.salesPerson.models.TokensModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;
import com.tribeappsoft.leedo.util.ccp.Country;
import com.tribeappsoft.leedo.util.ccp.CountryCodePicker;
import com.tribeappsoft.leedo.util.ccp.CountryCodeSelectActivity;
import com.tribeappsoft.leedo.util.ccp.CountryUtils;
import com.tribeappsoft.leedo.util.filepicker.MaterialFilePicker;
import com.tribeappsoft.leedo.util.filepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class GenerateTokenActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    //Generate Token View
    @BindView(R.id.cl_generateToken) CoordinatorLayout parent;
    @BindView(R.id.ll_generateToken_View) LinearLayoutCompat ll_generateToken_View;
    @BindView(R.id.tv_generateToken_select_customer_id) AppCompatTextView tv_generateToken_select_customer_id;
    @BindView(R.id.iv_generateToken_searchCustomerId) AppCompatImageView iv_generateToken_searchCustomerId;
    @BindView(R.id.tv_generateToken_select_customer_name) AppCompatTextView tv_generateToken_select_customer_name;
    @BindView(R.id.tv_generateToken_select_project) AutoCompleteTextView tv_select_project;
    @BindView(R.id.tv_generateToken_select_event) AutoCompleteTextView tv_select_event;
    @BindView(R.id.tv_generateToken_select_token) AutoCompleteTextView tv_select_token;
    @BindView(R.id.ll_cheque_payment) LinearLayoutCompat ll_cheque_payment;
    @BindView(R.id.ll_online_payment) LinearLayoutCompat ll_online_payment;
    @BindView(R.id.ll_card_payment) LinearLayoutCompat ll_card_payment;

    //Cheque Details
    @BindView(R.id.til_generateToken_cheque_date) TextInputLayout til_generateToken_cheque_date;
    @BindView(R.id.edt_generateToken_cheque_date) TextInputEditText edt_generateToken_cheque_date;
    @BindView(R.id.til_generateToken_cheque_number) TextInputLayout til_generateToken_cheque_number;
    @BindView(R.id.edt_generateToken_cheque_number) TextInputEditText edt_generateToken_cheque_number;
    @BindView(R.id.til_generateToken_cheque_issuer_name) TextInputLayout til_generateToken_cheque_issuer_name;
    @BindView(R.id.edt_generateToken_cheque_issuer_name) TextInputEditText edt_generateToken_cheque_issuer_name;
    @BindView(R.id.til_generateToken_cheque_bank_name) TextInputLayout til_generateToken_cheque_bank_name;
    @BindView(R.id.edt_generateToken_cheque_bank_name) TextInputEditText edt_generateToken_cheque_bank_name;
    //Online Details

    @BindView(R.id.til_generateToken_utr_number) TextInputLayout til_generateToken_utr_number;
    @BindView(R.id.edt_generateToken_utr_number) TextInputEditText edt_generateToken_utr_number;

    //Card Details
    @BindView(R.id.til_generateToken_payment_transaction_number) TextInputLayout til_generateToken_payment_transaction_number;
    @BindView(R.id.edt_generateToken_payment_transaction_number) TextInputEditText edt_generateToken_payment_transaction_number;
    @BindView(R.id.til_generateToken_payment_invoice_number) TextInputLayout til_generateToken_payment_invoice_number;
    @BindView(R.id.edt_generateToken_payment_invoice_number) TextInputEditText edt_generateToken_payment_invoice_number;

    //KYC Documents list
    @BindView(R.id.ll_GenerateToken_kycDocsMain) LinearLayoutCompat ll_GenerateToken_kycDocsMain;
    @BindView(R.id.ll_generateToken_kycDocsMain) LinearLayoutCompat ll_kycDocsMain;
    @BindView(R.id.iv_generateToken_kycDocs_ec) AppCompatImageView iv_kycDocs_ec;
    @BindView(R.id.ll_generateToken_viewKycDoc) LinearLayoutCompat ll_viewKycDoc;
    @BindView(R.id.ll_generateToken_addKYCDoc) LinearLayoutCompat ll_addKYCDoc;

    //Payment Mode
    @BindView(R.id.ll_generateToken_paymentModeMain) LinearLayoutCompat ll_paymentModeMain;
    @BindView(R.id.iv_generateToken_paymentMode_ec) AppCompatImageView iv_paymentMode_ec;
    @BindView(R.id.ll_generateToken_paymentModes) LinearLayoutCompat ll_paymentModes;
    @BindView(R.id.ll_generateToken_addPaymentMode) LinearLayoutCompat ll_addPaymentMode;
    @BindView(R.id.edt_generateToken_remarks) TextInputEditText edt_remarks;
    @BindView(R.id.mBtn_generateToken) MaterialButton mBtn_generateToken;
    @BindView(R.id.ll_paymentModeFocus) LinearLayoutCompat ll_paymentModeFocus;

    //Site Visit
    @BindView(R.id.ll_site_visit_generate_token) LinearLayoutCompat ll_site_visit_generate_token;
    @BindView(R.id.ll_generate_token_projectDetailsMain) LinearLayoutCompat ll_generate_token_projectDetailsMain;
    @BindView(R.id.iv_generate_token_viewProjectDetails_ec) AppCompatImageView iv_generate_token_viewProjectDetails_ec;
    @BindView(R.id.ll_generate_token_viewProjectDetails) LinearLayoutCompat ll_generate_token_viewProjectDetails;
    @BindView(R.id.edt_generate_token_project_name) TextInputEditText tv_generate_token_project_name;
    @BindView(R.id.edt_generate_token_flat_type) TextInputEditText edt_generate_token_flat_type;
    @BindView(R.id.edt_generate_token_visit_date) TextInputEditText edt_generate_token_visit_date;
    @BindView(R.id.edt_generate_token_visit_time) TextInputEditText edt_generate_token_visit_time;
    @BindView(R.id.edt_generate_token_siteVisitRemarks) TextInputEditText edt_siteVisitRemarks;

    //Refund Details
    @BindView(R.id.ll_generateToken_refundDetailsMain) LinearLayoutCompat ll_refundDetailsMain;
    @BindView(R.id.iv_generateToken_refundDetails_ec) AppCompatImageView iv_refundDetails_ec;
    @BindView(R.id.ll_generateToken_addRefundDetails) LinearLayoutCompat ll_addRefundDetails;
    @BindView(R.id.edt_generateToken_refundAcHolderName) TextInputEditText edt_refundAcHolderName;
    @BindView(R.id.edt_generateToken_refundBankName) TextInputEditText edt_refundBankName;
    @BindView(R.id.edt_generateToken_refundBranchName) TextInputEditText edt_refundBranchName;
    @BindView(R.id.edt_generateToken_refundIFSCNumber) TextInputEditText edt_refundIFSCNumber;
    @BindView(R.id.acTv_generateToken_acType) AutoCompleteTextView acTv_acType;
    @BindView(R.id.edt_generateToken_refundACNumber) TextInputEditText edt_refundACNumber;

    //Share Token View
    @BindView(R.id.ll_shareToken_View) LinearLayoutCompat ll_shareToken_View;
    @BindView(R.id.iv_shareToken_close) AppCompatImageView iv_shareToken_close;
    @BindView(R.id.iv_shareToken_successImg) AppCompatImageView iv_shareToken_successImg;
    @BindView(R.id.tv_shareToken_successMsg) AppCompatTextView tv_shareToken_successMsg;
    @BindView(R.id.tv_shareToken_paymentPendingMsg) AppCompatTextView tv_shareToken_paymentPendingMsg;
    @BindView(R.id.tv_ShareToken_itemCustomer_name) AppCompatTextView tv_Customer_name;
    @BindView(R.id.tv_ShareToken_itemCustomer_mobile) AppCompatTextView tv_Customer_mobile;
    @BindView(R.id.tv_ShareToken_itemCustomer_email) AppCompatTextView tv_Customer_email;
    @BindView(R.id.edt_shareToken_Customer_First_Name) TextInputEditText edt_First_Name;
    @BindView(R.id.edt_shareToken_Customer_Middle_Name) TextInputEditText edt_Middle_Name;
    @BindView(R.id.ll_ccp_generateToken) LinearLayoutCompat ll_ccp;
    @BindView(R.id.ccp_generateToken)
    CountryCodePicker ccp;
    @BindView(R.id.flag_imv) AppCompatImageView flag_imv;
    @BindView(R.id.selected_country_tv) AppCompatTextView selected_country_tv;
    @BindView(R.id.tv_ShareToken_itemCustomer_GHPNo) AppCompatTextView tv_Customer_GHPNo;
    @BindView(R.id.tv_ShareToken_itemCustomer_ProjectName) AppCompatTextView tv_Customer_ProjectName;
    @BindView(R.id.tv_ShareToken_itemCustomer_EventName) AppCompatTextView tv_Customer_EventName;
    @BindView(R.id.edt_shareToken_Customer_Last_Name) TextInputEditText edt_Last_Name;
    @BindView(R.id.edt_shareToken_Customer_MobileNo) TextInputEditText edt_shareToken_Customer_MobileNo;
    @BindView(R.id.edt_shareToken_Customer_Email) TextInputEditText edt_shareToken_Customer_Email;
    @BindView(R.id.tv_ShareToken_ghp_date_text) AppCompatTextView tv_ghp_date_text;
    @BindView(R.id.tv_ShareToken_itemCustomer_GHP_Date) AppCompatTextView tv_Customer_GHP_Date;
    @BindView(R.id.tv_ShareToken_ghp_amount_text) AppCompatTextView tv_ghp_amount_text;
    @BindView(R.id.tv_ShareToken_itemCustomer_GHP_Amount) AppCompatTextView tv_Customer_GHP_Amount;
    @BindView(R.id.tv_ShareToken_ghp_plus_date_text) AppCompatTextView tv_ghp_plus_date_text;
    @BindView(R.id.tv_ShareToken_itemCustomer_GHPPlus_Date) AppCompatTextView tv_Customer_GHPPlus_Date;
    @BindView(R.id.tv_ShareToken_ghp_plus_amount_text) AppCompatTextView tv_ghp_plus_amount_text;
    @BindView(R.id.tv_ShareToken_itemCustomer_GHPPlus_Amount) AppCompatTextView tv_Customer_GHPPlus_Amount;
    @BindView(R.id.tv_ShareToken_remarks) AppCompatTextView tv_ShareToken_remarks;
    @BindView(R.id.view_ghpDetails) View view_ghpDetails;

    @BindView(R.id.ll_ShareToken_remarks) LinearLayoutCompat ll_ShareToken_remarks;
    @BindView(R.id.ll_ShareToken_ghpDate) LinearLayoutCompat ll_ShareToken_ghpDate;
    @BindView(R.id.ll_ShareToken_ghpAmount) LinearLayoutCompat ll_ShareToken_ghpAmount;
    @BindView(R.id.ll_ShareToken_ghpPlusDate) LinearLayoutCompat ll_ShareToken_ghpPlusDate;
    @BindView(R.id.ll_ShareToken_ghpPlusAmount) LinearLayoutCompat ll_ShareToken_ghpPlusAmount;

    //Confirmed Edit Contact Details
    @BindView(R.id.rgrp_customer_edit_contact) RadioGroup rdGrp_edit_contact;
    @BindView(R.id.rbtn_customer_edit_contact_yes)  RadioButton rBtn_yes;
    @BindView(R.id.rbtn_customer_edit_contact_no)  RadioButton rBtn_no;


    //Edit Contact View
    @BindView(R.id.ll_ShareToken_edit_customerInfo) LinearLayoutCompat ll_ShareToken_edit_customerInfo;
    @BindView(R.id.btn_shareToken_submit) MaterialButton btn_shareToken_submit;
    @BindView(R.id.acTv__shareToken_Customer_mrs) AutoCompleteTextView acTv_Customer_mrs;

    //View Share Download
    @BindView(R.id.ll_ShareToken_View_share_download) LinearLayoutCompat ll_ShareToken_View_share_download;
    @BindView(R.id.mBtn_generateToken_upgradeToGHPPlus) MaterialButton mBtn_upgradeToGHPPlus;
    @BindView(R.id.mBtn_shareToken_share) MaterialButton mBtn_shareToken_share;
    @BindView(R.id.mBtn_shareToken_view) MaterialButton mBtn_shareToken_view;
    @BindView(R.id.mBtn_shareToken_MoreShare) MaterialButton mBtn_shareToken_MoreShare;
    @BindView(R.id.mBtn_shareToken_download) MaterialButton mBtn_shareToken_download;
    @BindView(R.id.pb_generateToken_downloadDoc) ProgressBar pb_generateToken_downloadDoc;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    @BindView(R.id.edt_generate_token_ghp_amount) TextInputEditText edt_ghp_amount;
    //select paid by
    @BindView(R.id.rGrp_generate_token_payMode) RadioGroup rGrp_payMode;
    @BindView(R.id.rBtn_generateToken_pay_manual)  RadioButton rBtn_pay_manual;
    @BindView(R.id.rBtn_generateToken_pay_payGateway)  RadioButton rBtn_pay_payGateway;
    @BindView(R.id.rBtn_generateToken_pay_viaPayLink)  RadioButton rBtn_pay_viaPayLink;
    @BindView(R.id.ll_generate_token_payMode_manual) LinearLayoutCompat ll_payMode_manual;

    //ghp payment pending
    @BindView(R.id.ll_shareToken_paymentPending) LinearLayoutCompat ll_paymentPending;
    @BindView(R.id.mBtn_sharePayLink_waShare) MaterialButton mBtn_sharePayLink_waShare;
    @BindView(R.id.mBtn_sharePayLink_mailShare) MaterialButton mBtn_sharePayLink_mailShare;
    @BindView(R.id.mBtn_sharePayLink_moreShare) MaterialButton mBtn_sharePayLink_moreShare;

    public Activity context;
    public Animations anim;
    public boolean viewKYCDocuments =false,viewPaymentModes =false, isDocSelected =false,
            viewRefundDetails = false, isEditCustomerDetails = false; // paidByManual = true;
    public EventProjectDocsModel eventProjectDocsModel = null;
    public ArrayList<CUIDModel> cuidModelArrayList;
    public ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList;
    public ArrayList<PaymentModeModel> paymentModeModelArrayList;
    public ArrayList<PersonNamePrefixModel> personPrefixModelArrayList;
    public ArrayList<AccountTypeModel> accountTypeModelArrayList;
    public ArrayList<String> personPrefixStringArrayList, eventStringArrayList, projectStringArrayList, tokensStringArrayList, accountTypeStringArrayList;
    public ArrayList<EventsModel> eventsModelArrayList;
    public ArrayList<ProjectModel> projectModelArrayList;
    public ArrayList<TokensModel> tokensModelArrayList;

    private int selectedEventId = 0, selectedTokenId = 0,paymentMode=0,myPosition =0,user_id=0,selectedPrefixId=0,token_type_id=0,
            fromOther=0,mYear, mMonth, mDay,docAPICount=0,documentCount, lead_status_id =0, // TODO lead_status_id == 13 GHP payment pending
            is_kyc_uploaded=0, //TODO is_kyc_uploaded ==> 1 - Uploaded , 0 -> Not uploaded yet
            lead_id=0,project_id=0,flat_id=0,verified_by_id=0, isDownloadViewShare=0,selectedProjectId=0,selectedAcTypeId =0,
            paidVia = 3, lead_stage_id =0;  // TODO paidVia ==> 1 - Manual, 2- Pay Gateway,  3 - Pay Link

    public String selectedEvent = "",selectedProject = "", selectedToken = "",selectedTokenAmount = "0",selectedPrefix="";
    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;
    private static final int Permission_CODE_DOC_SHARE = 324;
    public String setChequeDate="",token_document=null, token_No ="", selected_country="",
            visit_date_api="",visit_time_api="",cuidNumber="",customer_name="", customer_mobile="",
            customer_email="", project_name="",event_name="", flat_type="", visit_date="",visit_time="",
            TAG = "GenerateTokenActivity",api_token="",country_code="91",order_id = null,transaction_id = "0",
            first_name="",middle_name="",last_name="", remark ="", selectedAcType="",ghp_date="",ghp_amount="",ghp_plus_date="",
            ghp_plus_amount="",ghp_remark="",payment_link = "", payment_invoice_id = "";

    public CUIDModel cuidModel;
    //TODO fromOther ==> 1->Site Visit,  2->Generate Token, 3-> From feed to view Token Details
    // paymentMode ==> 1 -> Cheque, 2-> OT, 3-> Card
    private File file;
    private ShareDialog shareDialog;
    public CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token);
        ButterKnife.bind(this);
        context = GenerateTokenActivity.this;
        anim = new Animations();
        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.generate_ghp));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(context);
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //hide pb
        hideProgressBar();

        //Get From Other For Site Visit or Add Token
        if (getIntent()!=null)
        {
            fromOther = getIntent().getIntExtra("fromOther", 0);
            lead_status_id = getIntent().getIntExtra("lead_status_id", 0);

            if (fromOther==1)
            {
                //Get Data From Site visit Module
                cuidNumber = getIntent().getStringExtra("CUID");
                lead_id = getIntent().getIntExtra("lead_id", 0);
                verified_by_id = getIntent().getIntExtra("verified_by_id", 0);
                customer_name = getIntent().getStringExtra("Customer_Name");
                country_code = getIntent().getStringExtra("country_code");
                customer_mobile = getIntent().getStringExtra("mobile_number");
                customer_email = getIntent().getStringExtra("email");
                project_name = getIntent().getStringExtra("Project_Name");
                event_name = getIntent().getStringExtra("Event_Name");
                flat_type = getIntent().getStringExtra("Flat_Type");
                project_id = getIntent().getIntExtra("Project_Id", 0);
                flat_id = getIntent().getIntExtra("Flat_Id", 0);
                is_kyc_uploaded = getIntent().getIntExtra("is_kyc_uploaded", 0);
                lead_stage_id = getIntent().getIntExtra("lead_stage_id", 0);
                visit_date = getIntent().getStringExtra("Visit_Date");
                visit_time = getIntent().getStringExtra("Visit_Time");
                visit_date_api = getIntent().getStringExtra("Visit_Date_api");
                visit_time_api = getIntent().getStringExtra("Visit_Time_api");
                //selectedPrefix = getIntent().getStringExtra("prefix");
                first_name = getIntent().getStringExtra("first_name");
                middle_name = getIntent().getStringExtra("middle_name");
                last_name = getIntent().getStringExtra("last_name");
                remark = getIntent().getStringExtra("remark");


                //Set Customer Details
                tv_generateToken_select_customer_name.setText(customer_name);
                tv_generateToken_select_customer_id.setText(cuidNumber);
                tv_generate_token_project_name.setText(project_name);
                edt_generate_token_flat_type.setText(flat_type);
                edt_generate_token_visit_date.setText(visit_date);
                edt_generate_token_visit_time.setText(visit_time);
                edt_siteVisitRemarks.setText(remark);
                //visible
                ll_site_visit_generate_token.setVisibility(View.VISIBLE);

            }
            else if (fromOther==3)
            {

                //from feeds to view token details
                //Get Customer Model
                cuidModel= (CUIDModel) getIntent().getSerializableExtra("cuidModel");
                if(cuidModel!=null)
                {
                    //From Direct CuId Activity
                    cuidNumber= cuidModel.getCu_id();
                    lead_id= cuidModel.getLead_id();
                    customer_name= cuidModel.getCustomer_name();
                    customer_mobile= cuidModel.getCustomer_mobile();
                    customer_email= cuidModel.getCustomer_email();
                    project_name= cuidModel.getCustomer_project_name();
                    event_name= cuidModel.getEventName();
                    token_type_id= cuidModel.getToken_type_id();
                    flat_type= cuidModel.getCustomer_flat_type();
                    is_kyc_uploaded= cuidModel.getIs_kyc_uploaded();
                    country_code=cuidModel.getCountry_code();
                    first_name = cuidModel.getFirst_name();
                    middle_name = cuidModel.getMiddle_name();
                    last_name = cuidModel.getLast_name();
                    token_document = cuidModel.getToken_media_path();
                    token_No = cuidModel.getToken_no();

                    ghp_date = cuidModel.getGhp_date();
                    ghp_amount = cuidModel.getGhp_amount();
                    ghp_plus_date = cuidModel.getGhp_plus_date();
                    ghp_plus_amount = cuidModel.getGhp_plus_amount();
                    ghp_remark = cuidModel.getGhp_remark();
                    Log.e(TAG, "onCreate: "+ghp_remark );

                    //get the pay link
                    payment_link = cuidModel.getPayment_link();
                    payment_invoice_id = cuidModel.getPayment_invoice_id();

                    //set customer detail
                    Log.e(TAG, "cuidNumber: "+cuidNumber);
                    tv_generateToken_select_customer_id.setText(cuidNumber);
                    tv_generateToken_select_customer_name.setText(customer_name);

                    //set customer data
                    setShareTokenCustomerData();
                    //show/ hide layouts
                    Objects.requireNonNull(getSupportActionBar()).hide();
                    change_status_bar_color(R.color.main_white);
                    ll_shareToken_View.setVisibility(View.VISIBLE);
                    ll_generateToken_View.setVisibility(View.GONE);
                }
            }
            else
            {
                //get data from feed-direct add token and from add token cu-id list
                //Get Data From Direct Add Token Module
                ll_site_visit_generate_token.setVisibility(View.GONE);
                //Get Customer Model
                cuidModel= (CUIDModel) getIntent().getSerializableExtra("cuidModel");
                if(cuidModel!=null)
                {
                    //From Direct CuId Activity
                    cuidNumber= cuidModel.getCu_id();
                    lead_id= cuidModel.getLead_id();
                    customer_name= cuidModel.getCustomer_name();
                    customer_mobile= cuidModel.getCustomer_mobile();
                    customer_email= cuidModel.getCustomer_email();
                    project_name= cuidModel.getCustomer_project_name();
                    ghp_remark= cuidModel.getGhp_remark();
                    Log.e(TAG, "onCreate: "+ cuidModel.getGhp_remark());
                    event_name= cuidModel.getEventName();
                    flat_type= cuidModel.getCustomer_flat_type();
                    is_kyc_uploaded= cuidModel.getIs_kyc_uploaded();
                    country_code=cuidModel.getCountry_code();
                    first_name = cuidModel.getFirst_name();
                    middle_name = cuidModel.getMiddle_name();
                    last_name = cuidModel.getLast_name();

                    Log.e(TAG, "onCreate: kyc uploaded "+ is_kyc_uploaded);

                    /*Set Data for Customer id and  Name*/
                    Log.e(TAG, "cuidNumber: "+cuidNumber);
                    tv_generateToken_select_customer_id.setText(cuidNumber);
                    tv_generateToken_select_customer_name.setText(customer_name);

                }
            }
        }

        cuidModelArrayList = new ArrayList<>();
        eventProjectDocsModelArrayList = new ArrayList<>();
        eventsModelArrayList = new ArrayList<>();
        projectModelArrayList = new ArrayList<>();
        projectStringArrayList = new ArrayList<>();
        eventStringArrayList = new ArrayList<>();
        tokensModelArrayList = new ArrayList<>();
        tokensStringArrayList = new ArrayList<>();
        paymentModeModelArrayList = new ArrayList<>();
        personPrefixModelArrayList = new ArrayList<>();
        personPrefixStringArrayList = new ArrayList<>();
        accountTypeModelArrayList = new ArrayList<>();
        accountTypeStringArrayList = new ArrayList<>();

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



        if (fromOther!=3)
        {
            //Get Tokens Data
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                showProgressBar(getString(R.string.loading_content));
                new Thread(this:: getTokensData).start();
            }
            else Helper.NetworkError(context);
        }


        //Set Customer Id
        iv_generateToken_searchCustomerId.setOnClickListener(v -> {
            startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 2)
                    //.putExtra("forId", 2) TODO for update S.E can generate GHP WO site visit so forID = 3
                    .putExtra("forId", 3)
            );
            finish(); });


        //Set toggleViews
        setToggleView();

        //Set Cheque Date
        edt_generateToken_cheque_date.setOnClickListener(view -> selectChequeDate());

        //Generate Token
        mBtn_generateToken.setOnClickListener(view -> {

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

        //view kyc doc main
        ll_kycDocsMain.setOnClickListener(v -> {
            if (viewKYCDocuments)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_kycDocs_ec, false);
                collapse(ll_viewKycDoc);
                viewKYCDocuments = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(iv_kycDocs_ec, true);
                expandSubView(ll_viewKycDoc);
                viewKYCDocuments = true;
            }
        });


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


        //view refund details
        ll_refundDetailsMain.setOnClickListener(v -> {

            if (viewRefundDetails)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_refundDetails_ec, false);
                collapse(ll_addRefundDetails);
                viewRefundDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(iv_refundDetails_ec, true);
                expandSubView(ll_addRefundDetails);
                viewRefundDetails = true;
            }
        });


        ll_generate_token_projectDetailsMain.setOnClickListener(v -> {
            if (viewPaymentModes)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_generate_token_viewProjectDetails_ec, false);
                collapse(ll_generate_token_viewProjectDetails);
                viewPaymentModes = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(iv_generate_token_viewProjectDetails_ec, true);
                expandSubView(ll_generate_token_viewProjectDetails);
                viewPaymentModes = true;
            }
        });

    }


    private void getTokensData()
    {

        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().GET_TokenData(api_token);
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
                        getTokenInformation();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                        else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
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
        //Project
        if (jsonObject.has("projects") && !jsonObject.get("projects").isJsonNull())
        {
            if (jsonObject.get("projects").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("projects").getAsJsonArray();
                if (jsonArray.size()>0)
                {

                    projectModelArrayList.clear();
                    projectStringArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setProjectNamesJson(jsonArray.get(j).getAsJsonObject());
                    }
                }
            }
        }

        //Events
        if (jsonObject.has("events") && !jsonObject.get("events").isJsonNull())
        {
            if (jsonObject.get("events").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("events").getAsJsonArray();
                if (jsonArray.size()>0)
                {

                    eventsModelArrayList.clear();
                    eventStringArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setEventNamesJson(jsonArray.get(j).getAsJsonObject());
                    }
                }
            }
        }

        //KYC Documents
        if (jsonObject.has("kyc_doc_types") && !jsonObject.get("kyc_doc_types").isJsonNull())
        {
            if (jsonObject.get("kyc_doc_types").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("kyc_doc_types").getAsJsonArray();
                if (jsonArray.size()>0)
                {

                    eventProjectDocsModelArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setKYCDocumentsJson(jsonArray.get(j).getAsJsonObject());
                    }
                }
            }
        }

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


        //Person Name Prefix
        if (jsonObject.has("person_name_prefix") && !jsonObject.get("person_name_prefix").isJsonNull())
        {
            if (jsonObject.get("person_name_prefix").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("person_name_prefix").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    personPrefixModelArrayList.clear();
                    personPrefixStringArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setPersonNamesFixJson(jsonArray.get(j).getAsJsonObject());
                    }
                }
            }
        }


        //Account type
        if (jsonObject.has("bank_account_types") && !jsonObject.get("bank_account_types").isJsonNull())
        {
            if (jsonObject.get("bank_account_types").isJsonArray())
            {
                JsonArray jsonArray =jsonObject.get("bank_account_types").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    accountTypeModelArrayList.clear();
                    accountTypeStringArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setAccountTypeJson(jsonArray.get(j).getAsJsonObject());
                    }
                }
            }
        }

    }

    private void setProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        }

        projectModelArrayList.add(model);
    }

    private void setEventNamesJson(JsonObject jsonObject)
    {
        EventsModel eventsModel = new EventsModel();
        if (jsonObject.has("event_id")) eventsModel.setEvent_id(!jsonObject.get("event_id").isJsonNull() ? jsonObject.get("event_id").getAsInt() : 0 );
        if (jsonObject.has("event_title"))
        {
            eventsModel.setEvent_title(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() : "" );
            eventStringArrayList.add(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() : "");
        }

        if (jsonObject.has("tokens") && !jsonObject.get("tokens").isJsonNull())
        {
            if (jsonObject.get("tokens").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("tokens").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<TokensModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setTokensDataJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    eventsModel.setTokensModelArrayList(arrayList);
                }
            }
        }
        eventsModelArrayList.add(eventsModel);
    }

    private void setTokensDataJson(JsonObject jsonObject, ArrayList<TokensModel> arrayList)
    {
        TokensModel myModel = new TokensModel();
        if (jsonObject.has("token_type_id")) myModel.setToken_type_id(!jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0 );
        if (jsonObject.has("token_type")) myModel.setToken_type(!jsonObject.get("token_type").isJsonNull() ? jsonObject.get("token_type").getAsString() : "" );
        if (jsonObject.has("amount")) myModel.setAmount(!jsonObject.get("amount").isJsonNull() ? jsonObject.get("amount").getAsString() : "" );
            //todo temp part remove it once testing
        else if (jsonObject.has("default_amount")) myModel.setAmount(!jsonObject.get("default_amount").isJsonNull() ? jsonObject.get("default_amount").getAsString() : "" );
        arrayList.add(myModel);
    }

    private void setKYCDocumentsJson(JsonObject jsonObject)
    {
        EventProjectDocsModel myModel = new EventProjectDocsModel();
        if (jsonObject.has("doc_type_id")) myModel.setDoc_type_id(!jsonObject.get("doc_type_id").isJsonNull() ? jsonObject.get("doc_type_id").getAsInt() : 0 );
        if (jsonObject.has("docs_type")) myModel.setDocType(!jsonObject.get("docs_type").isJsonNull() ? jsonObject.get("docs_type").getAsString() : "" );
        eventProjectDocsModelArrayList.add(myModel);
    }

    private void setPaymentModesJson(JsonObject jsonObject)
    {
        PaymentModeModel myModel = new PaymentModeModel();
        if (jsonObject.has("payment_modes_id")) myModel.setPayment_id(!jsonObject.get("payment_modes_id").isJsonNull() ? jsonObject.get("payment_modes_id").getAsInt() : 0 );
        if (jsonObject.has("payment_mode")) myModel.setPayment_mode(!jsonObject.get("payment_mode").isJsonNull() ? jsonObject.get("payment_mode").getAsString() : "" );
        paymentModeModelArrayList.add(myModel);
    }

    private void setPersonNamesFixJson(JsonObject jsonObject)
    {
        PersonNamePrefixModel myModel = new PersonNamePrefixModel();
        if (jsonObject.has("name_prefix_id")) myModel.setName_prefix_id(!jsonObject.get("name_prefix_id").isJsonNull() ? jsonObject.get("name_prefix_id").getAsInt() : 0 );
        if (jsonObject.has("status_id")) myModel.setStatus_id(!jsonObject.get("status_id").isJsonNull() ? jsonObject.get("status_id").getAsInt() : 0 );
        if (jsonObject.has("name_prefix"))
        {
            myModel.setName_prefix(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
            personPrefixStringArrayList.add(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "");
        }
        personPrefixModelArrayList.add(myModel);
    }

    private void setAccountTypeJson(JsonObject jsonObject)
    {
        AccountTypeModel myModel = new AccountTypeModel();
        if (jsonObject.has("bank_account_type_id")) myModel.setAc_type_id(!jsonObject.get("bank_account_type_id").isJsonNull() ? jsonObject.get("bank_account_type_id").getAsInt() : 0 );
        if (jsonObject.has("bank_account_type"))
        {
            myModel.setAc_type(!jsonObject.get("bank_account_type").isJsonNull() ? jsonObject.get("bank_account_type").getAsString() : "" );
            accountTypeStringArrayList.add(!jsonObject.get("bank_account_type").isJsonNull() ? jsonObject.get("bank_account_type").getAsString() : "");
        }
        accountTypeModelArrayList.add(myModel);
    }


    private void getTokenInformation()
    {

        runOnUiThread(() -> {

            hideProgressBar();

            //hide kyc view if already kyc done
            ll_GenerateToken_kycDocsMain.setVisibility( is_kyc_uploaded==0? View.VISIBLE : View.GONE );

            if (is_kyc_uploaded==0)
            {
                //set required data expanded
                new Handler(getMainLooper()).postDelayed(() -> {

                    //def set expandView to payment modes
                    anim.toggleRotate(iv_kycDocs_ec, true);
                    expandSubView(ll_viewKycDoc);
                    viewKYCDocuments = true;

                }, 100);
            }

            //Set Project
            setAdapterProjectNames();

            //Set Event
            setAdapterEventNames();

            //Get Document list
            setKYCDocuments();

            //Get Payment Mode list
            getPaymentModeList();

            //set adapters Prefix
            setAdapterForMrs();

            //set adapter for Ac type
            setAdapterForACType();

            //set edit customer option
            setCustomerEditOption();

            //set payMode options
            setPaymentModeOption();
        });
    }

    //Adapter for Getting projects
    private void setAdapterProjectNames()
    {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
        tv_select_project.setAdapter(adapter);
        tv_select_project.setThreshold(0);

        tv_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);
            for (ProjectModel pojo : projectModelArrayList)
            {
                if (pojo.getProject_name().equals(itemName))
                {
                    //int id = pojo.getCompany_id(); // This is the correct ID
                    selectedProjectId = pojo.getProject_id(); // This is the correct ID
                    selectedProject = pojo.getProject_name();

                    Log.e(TAG, "Selected EVent name & id " + selectedProject +"\t"+ selectedProjectId);

                    break; // No need to keep looping once you found it.
                }
            }
        });

    }


    //Adapter for Getting Events
    private void setAdapterEventNames()
    {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, eventStringArrayList);
        tv_select_event.setAdapter(adapter);
        tv_select_event.setThreshold(0);

        tv_select_event.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {
            String itemName = adapter.getItem(position);
            for (EventsModel pojo : eventsModelArrayList)
            {
                if (pojo.getEvent_title().equals(itemName))
                {
                    //int id = pojo.getCompany_id(); // This is the correct ID
                    selectedEventId = pojo.getEvent_id(); // This is the correct ID
                    selectedEvent = pojo.getEvent_title();
                    event_name = pojo.getEvent_title();
                    Log.e(TAG, "Selected EVent name & id " + selectedEvent +"\t"+ selectedEventId);

                    //clear token type
                    tv_select_token.setText("");
                    selectedTokenId = 0; // This is the correct ID
                    selectedToken = "";
                    setAdapterTokens(eventsModelArrayList.get(position).getTokensModelArrayList());

                    break; // No need to keep looping once you found it.
                }
            }
        });

    }


    private void setAdapterTokens(ArrayList<TokensModel> tokensModelArrayList)
    {

        if (tokensModelArrayList!=null && tokensModelArrayList.size()>0)
        {
            //adding unit categories
            tokensStringArrayList.clear();
            for (int i =0; i<tokensModelArrayList.size(); i++)
            {
                tokensStringArrayList.add(tokensModelArrayList.get(i).getToken_type());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, tokensStringArrayList);
            tv_select_token.setAdapter(adapter);
            tv_select_token.setThreshold(0);

            //tv_Customer_email.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
            tv_select_token.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (TokensModel pojo : tokensModelArrayList)
                {
                    if (pojo.getToken_type().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedTokenId = pojo.getToken_type_id(); // This is the correct ID
                        selectedToken = pojo.getToken_type();
                        selectedTokenAmount = pojo.getAmount();

                        //edt set selected token amount
                        edt_ghp_amount.setText(selectedTokenAmount!=null ? selectedTokenAmount : "0");

                        //mBtn_generateToken.setText(selectedTokenId ==3 ? paidByManual ? getString(R.string.generate_ghp)  : "" : getString(R.string.generate_ghp_plus));

                        if (paidVia!=2){
                            //paid via either manual or via pay link
                            mBtn_generateToken.setText(  selectedTokenId ==3 ? getString(R.string.generate_ghp_plus) : getString(R.string.generate_ghp) );
                        }
                        else {
                            //paid via payment gateway
                            mBtn_generateToken.setText(selectedTokenId ==3 ? getString(R.string.pay_n_generate_ghp_plus): getString(R.string.pay_n_generate_ghp));
                        }

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "selectedToken " + selectedToken +"\t"+ selectedTokenId);
                        Log.e(TAG, "selectedTokenAmount " + selectedTokenAmount);

                        break; // No need to keep looping once you found it.
                    }
                }

            });
        }
        else {
            //empty array
            new Helper().showCustomToast(context, "GHP events are empty!");
        }

    }




    private void setKYCDocuments()
    {
        ll_addKYCDoc.removeAllViews();
        for (int i =0 ; i< eventProjectDocsModelArrayList.size(); i++)
        {
            View rowView_sub = getDocumentsView(i);
            ll_addKYCDoc.addView(rowView_sub);
        }
    }

    private View getDocumentsView(final int position)
    {

        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_kyc_documents, null );

        final AppCompatTextView tv_kyc_doc_name = rowView_sub.findViewById(R.id.tv_kyc_doc_name);
        final AppCompatTextView tv_kyc_doc_file = rowView_sub.findViewById(R.id.tv_kyc_doc_select_file);
        final AppCompatImageView iv_browse = rowView_sub.findViewById(R.id.iv_kyc_doc_browse);

        tv_kyc_doc_name.setText(eventProjectDocsModelArrayList.get(position).getDocType());
        tv_kyc_doc_name.setText(eventProjectDocsModelArrayList.get(position).getIsRequired()==1 ? Html.fromHtml(eventProjectDocsModelArrayList.get(position).getDocType()+"<font color='#b42726'><b>*</b></font>") : eventProjectDocsModelArrayList.get(position).getDocType());
        tv_kyc_doc_file.setText(eventProjectDocsModelArrayList.get(position).getDocPath()!=null ? getFileName_from_filePath(eventProjectDocsModelArrayList.get(position).getDocPath()) : getString(R.string.no_file_choose));

        iv_browse.setOnClickListener(view -> {

            // browse
            eventProjectDocsModel = eventProjectDocsModelArrayList.get(position);
            myPosition = position;
            Log.e(TAG, "onClick: selected doc & id "+ eventProjectDocsModelArrayList.get(position).getDocType() + " "+eventProjectDocsModel.getDoc_type_id());
            selectDocumentPopup();

        });

        tv_kyc_doc_name.setOnClickListener(view -> {

        });

        return rowView_sub;
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

                    ll_paymentModeFocus.requestFocus();
                    ll_paymentModeFocus.requestFocus(View.FOCUS_DOWN);
                    ll_paymentModeFocus.setFocusableInTouchMode(true);
                }
                else if (selectedName.equalsIgnoreCase("Card")) {
                    //show card details
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.VISIBLE);
                    ll_online_payment.setVisibility(View.GONE);
                    ll_paymentModeFocus.requestFocus();
                    ll_paymentModeFocus.requestFocus(View.FOCUS_DOWN);
                    ll_paymentModeFocus.setFocusableInTouchMode(true);
                }
                else if (selectedName.equalsIgnoreCase("Online Transfer")) {
                    //Online Transfer
                    ll_cheque_payment.setVisibility(View.GONE);
                    ll_card_payment.setVisibility(View.GONE);
                    ll_online_payment.setVisibility(View.VISIBLE);
                    ll_paymentModeFocus.requestFocus();
                    ll_paymentModeFocus.requestFocus(View.FOCUS_DOWN);
                    ll_paymentModeFocus.setFocusableInTouchMode(true);
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

    public void selectDocumentPopup()
    {
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        Dialog builder_accept=new BottomSheetDialog(context);
        builder_accept.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder_accept.setContentView(R.layout.layout_upload_doc_options_popup);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayoutCompat linearLayout_option1,linearLayout_option2, linearLayout_option3;
        linearLayout_option1= builder_accept.findViewById(R.id.ll_layout_select_popup_option1);
        linearLayout_option2= builder_accept.findViewById(R.id.ll_layout_select_popup_option2);
        linearLayout_option3= builder_accept.findViewById(R.id.ll_layout_select_popup_option3);

        //camera
        Objects.requireNonNull(linearLayout_option1).setOnClickListener(view -> {
            askPermissionForCamera();
            builder_accept.dismiss();
        });

        //gallery
        Objects.requireNonNull(linearLayout_option2).setOnClickListener(view -> {
            askPermissionForGallery();
            builder_accept.dismiss();
        });

        //documents
        Objects.requireNonNull(linearLayout_option3).setOnClickListener(view -> {
            //doNormalShare(videoDetail);
            askPermissionForDocuments();
            builder_accept.dismiss();
        });

        builder_accept.setOnDismissListener(dialog -> {
            // TODO Auto-generated method stub

        });
        builder_accept.show();

    }


    void askPermissionForCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkCameraPermission() && checkWriteStoragePermission()) OpenCamera();
            else requestPermission_for_Camera();
        }
        else OpenCamera();
    }

    void askPermissionForGallery()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkReadPermission()) OpenGallery();
            else requestPermission_for_Gallery();
        }
        else OpenGallery();
    }

    void askPermissionForDocuments()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkWriteStoragePermission()) OpenDocuments();
            else requestPermission_for_Documents();
        }
        else OpenDocuments();
    }


    private boolean checkCameraPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkWriteStoragePermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkReadPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void OpenCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);

    /*   Bundle bundle = new Bundle();
       bundle.putInt("position", position);
       bundle.putString("myDocument", "SSY");
       intent.putExtras(bundle);
       intent.putExtra(MediaStore.EXTRA_OUTPUT, bundle);*/
    }


    void OpenGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    private void OpenDocuments()
    {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(3)
                .withFilter(Pattern.compile(".*\\.(txt|pdf|doc|odt)$"))
                // .withFilter(Pattern.compile(".*\\.pdf$")) // Filtering files and directories by file name using regexp
                // .withFilter(Pattern.compile(".*\\.directory$")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(false) // Set directories filterable (false by default)
                .withHiddenFiles(false)
                .withTitle("Sample title")
                .start();

    }


    private void requestPermission_for_Camera()
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //permission granted already -- directly open the camera
            OpenCamera();
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(context), Manifest.permission.CAMERA)
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, getString(R.string.camera_permission_rationale));
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(Objects.requireNonNull(context), new String[]
                {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, Permission_CODE_Camera);

    }

    private void requestPermission_for_Gallery()
    {

        if ( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //permission granted already -- directly open the gallery
            OpenGallery();
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(context), Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, getString(R.string.gallery_permission_rationale));
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(Objects.requireNonNull(context), new String[]
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, Permission_CODE_Gallery);
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
    }

    private void requestPermission_for_Documents()
    {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {

            OpenDocuments();
            return;
        }
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)))
        {
            new Helper().showCustomToast(this, getString(R.string.file_permissionRationale));
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, Permission_CODE_DOC);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == Permission_CODE_Camera)  //handling camera permission
        {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open camera once permission is granted
                OpenCamera();
            }
//            else
//            {
//                //Displaying another toast if permission is not granted
//
//            }
        }


        if (requestCode == Permission_CODE_Gallery)  //handling gallery permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open gallery once permission is granted
                OpenGallery();
            }
//            else
//            {
//                //Displaying another toast if permission is not granted
//                permission_grant_gallery =0;
//            }

        }

        if (requestCode == Permission_CODE_DOC)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open documents once permission is granted
                OpenDocuments();
            }
//            else
//            {
//                //Displaying another toast if permission is not granted
//                permission_grant_docs =0;
//            }

        }

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


    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {

        super.onActivityResult(requestCode, responseCode, data);
        Log.e(TAG, "onActivityResult: myPos " + myPosition);

        if (requestCode == 1)   //From Camera
        {

            if (responseCode == RESULT_OK) {
                try {

                    if (data != null) {


                        //String myDocument = Objects.requireNonNull(data.getExtras()).getString("myDocument");
                        //int  position  = data.getExtras().getInt("position", 0);
                        //Log.e("myDocument & pos ", myDocument +" "+position);

                        Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                        Uri tempUri = getImageUri(getApplicationContext(), Objects.requireNonNull(photo));

                        // CALL THIS METHOD TO GET THE ACTUAL PATH
                        File finalFile = new File(getRealPathFromURI(tempUri));

                        Log.e("finalFile", finalFile.getAbsolutePath());
                        Log.e("finalFile", "ab path");
                        Log.e("finalFile", finalFile.getPath());
                        Log.e("finalFile", finalFile.toString());
                        setSelectedDoc(eventProjectDocsModel, finalFile.getAbsolutePath());
                        isDocSelected =true;
                        Log.e(TAG, "onActivityResult: " + getFileName_from_filePath(finalFile.getAbsolutePath()));
                        //tv_UploadedDoc_Name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                    } else Log.e("myDocument & pos ", "data null");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        else if (requestCode == 2)  //From Gallery
        {
            if (responseCode == RESULT_OK) {
                try {

                    //String myDocument = Objects.requireNonNull(data.getExtras()).getString("myDocument");
                    //int  position  = data.getExtras().getInt("position", 0);
                    // Log.e("Gallery myDocument & pos ", myDocument +" "+position);


                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = null;
                    if (selectedImage != null) {
                        c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
                    }
                    if (c != null) {
                        c.moveToFirst();
                        int columnIndex = c.getColumnIndex(filePath[0]);

                        File imageFile = new File(c.getString(columnIndex));
                        String photoUrl = imageFile.getAbsolutePath();
                        Log.e("url_gallery", "" + photoUrl);
                        c.close();
                        setSelectedDoc(eventProjectDocsModel, photoUrl);
                        isDocSelected =true;

                        //call the method that upload the profile pic
                        //call_uploadImage(photoUrl);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == 3)  //From Docs
        {

            if (responseCode == RESULT_OK) {
                //isMediaSelected =  true;
                String photoUrl = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

                if (photoUrl != null) {
                    Log.e("Path: ", photoUrl);
                }
                File file = null;
                if (photoUrl != null) {
                    file = new File(photoUrl);
                }
                String imageName = null;
                if (file != null) {
                    imageName = file.getName();
                }
                StringTokenizer st = new StringTokenizer(imageName, ".");
                //String filenameVal = st.nextToken();
                String ext = st.nextToken();
                String mimetype = URLConnection.guessContentTypeFromName(imageName);
                //String photourl = filePath;
                Log.e(TAG, Objects.requireNonNull(photoUrl));
                Log.e(TAG, "ext " + ext);
                Log.e(TAG, "mimetype " + mimetype);
                setSelectedDoc(eventProjectDocsModel, photoUrl);
                isDocSelected =true;


            }

        }
        if (requestCode == 121  && responseCode  == RESULT_OK)
        {
            Country country = (Country) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+ Objects.requireNonNull(country).getName() +" \n"+country.getPhoneCode() );
            //set country
            ccp.setSelectedCountry(country);

            country_code = country.getPhoneCode();
            String iso = country.getIso().toUpperCase();
            flag_imv.setImageResource(CountryUtils.getFlagDrawableResId(country));
            selected_country = getString(R.string.country_code_and_phone_code, iso, country_code);
            //selected_country_tv.setText(getString(R.string.country_code_and_phone_code, iso, countryPhoneCode));
            selected_country_tv.setText(String.format("+%s", country_code));
            Log.e(TAG, "onActivityResult: countryCode "+country_code );
            ccp.setCountryForNameCode(country.getName());
            new Helper().showCustomToast(context, "selected "+ country.getName() );
        }
        else if (requestCode == 121 && responseCode == RESULT_CANCELED)
        {
            new Helper().showCustomToast(context, "You cancelled!");
        }

    }


    private String getFileName_from_filePath(String filePath)
    {
        //String path=":/storage/sdcard0/DCIM/Camera/1414240995236.jpg";//it contain your path of image..im using a temp string..
        //String filename=filePath.substring(filePath.lastIndexOf("/")+1);
        return filePath.substring(filePath.lastIndexOf("/")+1);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public String getRealPathFromURI(Uri uri)
    {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    private void setSelectedDoc(EventProjectDocsModel myUploadModel, String absolutePath)
    {

        EventProjectDocsModel model =new EventProjectDocsModel();
        model.setDoc_type_id(myUploadModel.getDoc_type_id());
        //model.setDocType(myUploadModel.getDocType());
        model.setIsRequired(myUploadModel.getIsRequired());
        model.setIsUploaded(0);
        model.setDocType(myUploadModel.getDocType());
        model.setDocPath(absolutePath!=null ? absolutePath : "");

        eventProjectDocsModelArrayList.set(myPosition, model);
        documentCount++;
        //set documents view
        setKYCDocuments();
    }


    private void selectChequeDate()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
                    setChequeDate = Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    edt_generateToken_cheque_date.setText(setChequeDate);
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }



    private void checkValidation()
    {

        //select customer
        if (cuidNumber==null) new Helper().showCustomToast(context, "Please select Customer!");
            //customer name
        else if (Objects.requireNonNull(tv_Customer_name.getText()).toString().trim().isEmpty())  new Helper().showCustomToast(this, "Please select Customer!");
            //project name
        else if (selectedProjectId ==0) new Helper().showCustomToast(context, "Please select Project!");

        else if (selectedEventId ==0) new Helper().showCustomToast(context, "Please select Event!");
            //flat type
        else if (selectedTokenId ==0) new Helper().showCustomToast(context, "Please select GHP type!");

            //TODO updated customer edit details option while generating GHP
            //name prefix
        else if (isEditCustomerDetails && selectedPrefixId==0) new Helper().showCustomToast(this, "Please select name prefix!");
            //first name
        else if (isEditCustomerDetails && Objects.requireNonNull(edt_First_Name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer First Name!");
            //last name
        else if (isEditCustomerDetails && Objects.requireNonNull(edt_Last_Name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer Last Name!");
            //mobile number
        else if (isEditCustomerDetails && Objects.requireNonNull(edt_shareToken_Customer_MobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer Mobile number!");

            //isKyC uploaded
            //else if (is_kyc_uploaded != 1 && !isDocSelected ) new Helper().showCustomToast(context, "Please select KYC document to upload!");
        else if (is_kyc_uploaded == 0 && !isDocSelected ) new Helper().showCustomToast(context, "Please select KYC document to upload!");
            //payment mode 0
        else if (paidVia ==1 && paymentMode == 0 ) new Helper().showCustomToast(this, "Please select payment mode!");
            //payment mode 1 && cheque no empty
        else if (paidVia ==1 && paymentMode ==1 && Objects.requireNonNull(edt_generateToken_cheque_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Cheque Number");
            //cheque date empty
        else if (paidVia ==1 && paymentMode ==1 && Objects.requireNonNull(edt_generateToken_cheque_date.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Select Cheque Date");
            //cheque_issuer_name empty
        else if (paidVia ==1 && paymentMode ==1 && Objects.requireNonNull(edt_generateToken_cheque_issuer_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Cheque Issuer Name");
            //cheque_bank_name empty
        else if (paidVia ==1 && paymentMode ==1 && Objects.requireNonNull(edt_generateToken_cheque_bank_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Bank Name");
            //utr_number empty
        else if (paidVia ==1 && paymentMode ==2 && Objects.requireNonNull(edt_generateToken_utr_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter UTR Number");
            //transaction_number empty
        else if (paidVia ==1 && paymentMode ==3 && Objects.requireNonNull(edt_generateToken_payment_transaction_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Transaction Number");
            //invoice_number empty
        else if (paidVia ==1 && paymentMode ==3 && Objects.requireNonNull(edt_generateToken_payment_invoice_number.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Invoice Number");

            //refund AC holder name
            //else if (Objects.requireNonNull(edt_refundAcHolderName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please enter Account holder name for refund payment!");
            //refund bank name
            //else if (Objects.requireNonNull(edt_refundBankName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please enter bank name for refund payment!");
            //refund branch name
            //else if (Objects.requireNonNull(edt_refundBranchName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please enter branch name for refund payment!");
            //refund ifsc code
            //else if (Objects.requireNonNull(edt_refundIFSCNumber.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please enter IFSC code for refund payment!");
            //valid ifsc code
            //else if (!isValidIFSC(edt_refundIFSCNumber)) new Helper().showCustomToast(this, "Please enter valid IFSC code!");
            //refund ac type
            //else if (selectedAcTypeId ==0) new Helper().showCustomToast(context, "Please select Account type!");
            //refund account number
            //else if (Objects.requireNonNull(edt_refundACNumber.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please enter account number for refund payment!");


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
        tv_desc.setText(getString(R.string.ghp_generate_confirm_text, customer_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            if (Helper.isNetworkAvailable(this))
            {
                //if (is_kyc_uploaded!=1 ) {
                if (is_kyc_uploaded==0 ) {

                    //if kyc not done before then add kyc documents first then call post site visit
                    //call to add kyc document first
                    showProgressBar("Generating GHP...");
                    addPostDoc();
                }
                else {

                    //kyc already done before

                    // check for manual pay or payment gateway pay or pay link
                    if (paidVia !=2) {
                        //manual pay - for manual pay get the payment details manually and Generate GHP
                        //or pay via link - for pay via link,  generate GHP only but show status payment is pending

                        // check for site visit done before or not
                        showProgressBar("Generating GHP...");
                        if(fromOther==1) PostAddSiteVisitToken();
                        else PostAddToken();
                    }
                    else {

                        //payment gateway pay
                        // first pay using customer's payment details and then generate the GHP

                        Double donatedAmt = Double.parseDouble(selectedTokenAmount!=null ? selectedTokenAmount : "0") * 100; //converting amount Rupee into Paise
                        if(donatedAmt > 0.0) {
                            showProgressBar("Generating GHP...");
                            new Handler().postDelayed(() -> call_generatePayOrderId(donatedAmt), 500);
                        }
                        else {
                            new Helper().showCustomToast(context, "GHP Amount Rs. 0.00! Try Another GHP Type.");
                        }
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


    public void addPostDoc()
    {
        if (eventProjectDocsModelArrayList!=null && eventProjectDocsModelArrayList.size()>0) {
            //call upload method
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                for (int i =0; i<eventProjectDocsModelArrayList.size(); i++) {

                    if (eventProjectDocsModelArrayList.get(i).getDocPath()!=null && !eventProjectDocsModelArrayList.get(i).getDocPath().isEmpty() ) {
                        //ll_pb.setVisibility(View.VISIBLE);
                        //showProgressBar(getString(R.string.posting_doc)+" "+eventProjectDocsModelArrayList.get(i).getDocType()+"...");
                        showProgressBar(getString(R.string.posting_doc)+" KYC documents...");
                        add_KYCDocument(eventProjectDocsModelArrayList.get(i).getDocPath(), eventProjectDocsModelArrayList.get(i).getDoc_type_id());
                    }
                }
            }
            else Helper.NetworkError(context);
        }
    }


    private void add_KYCDocument(String filePath,int doc_id)
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            File docFile = new File(filePath);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), docFile);
            MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file_uri", docFile.getName(), uploadFile);

            RequestBody api_token_ = RequestBody.create(MediaType.parse("text/plain"),api_token);
            RequestBody doc_type_id = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(doc_id));
            RequestBody lead_id_ = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(lead_id));
            // RequestBody projectID = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(selectedProjectId));

            ApiClient client = ApiClient.getInstance();
            client.getApiService().add_KYCDocument(fileUpload,api_token_,doc_type_id,lead_id_).enqueue(new Callback<JsonObject>()
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
                            //  if (response.body().has("doc_upload_status")) doc_upload_status = response.body().get("doc_upload_status").getAsInt();

                            if (isSuccess==1)
                            {
                                docAPICount=docAPICount+1;
                                onDocumentUpload();
                            }
                        }
                    }
                    else {
                        // error case
                        switch (response.code())
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

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
                {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                    else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
            });
        }
        else Helper.NetworkError(context);
    }

    private void onDocumentUpload()
    {
        runOnUiThread(() -> {

                    //condition for number documents selected were all uploaded or not
                    //here documentCount is for selected number of documents
                    // docAPICount is for number of times api called
                    if(documentCount==docAPICount)
                    {
                        Log.e(TAG, "documentCount: "+documentCount );
                        Log.e(TAG, "docAPICount: "+docAPICount );
                        //hide pb
                        hideProgressBar();

                        //set document uploaded to 1
                        is_kyc_uploaded = 1;

                        // check for manual pay or payment gateway pay or pay link
                        if (paidVia !=2) {
                            //manual pay - for manual pay get the payment details manually and Generate GHP
                            //or pay via link - for pay via link,  generate GHP only but show status payment is pending

                            // check for site visit done before or not
                            showProgressBar("Generating GHP...");
                            if(fromOther==1) PostAddSiteVisitToken();
                            else PostAddToken();
                        }
                        else {

                            //payment gateway pay
                            // first pay using customer's payment details and then generate the GHP

                            Double donatedAmt = Double.parseDouble(selectedTokenAmount!=null ? selectedTokenAmount :  "0") * 100; //converting amount Rupee into Paise
                            if(donatedAmt > 0.0) {
                                showProgressBar("Generating GHP...");
                                new Handler().postDelayed(() -> call_generatePayOrderId(donatedAmt), 500);
                            }
                            else {
                                new Helper().showCustomToast(context, "GHP Amount Rs. 0.00! Try Another GHP Type.");
                            }
                        }
                    }
                }
        );
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
                if (e instanceof UnknownServiceException) showErrorLogRenewal(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLogRenewal(context.getString(R.string.connection_time_out));
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
            options.put("name", "Developers");

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
                    // check for site visit done before or not
                    if(fromOther==1) PostAddSiteVisitToken();
                    else PostAddToken();

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

    private void PostAddToken()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("sales_person_id",user_id);
        jsonObject.addProperty("lead_id",lead_id);
        //if pay using payment gateway then payment mode id should be 4
        //if pay using pay link then payment mode id should be 5
        jsonObject.addProperty("payment_mode_id", paidVia ==1 ? paymentMode : paidVia ==2 ? 4 : 5);
        jsonObject.addProperty("project_id",selectedProjectId);
        jsonObject.addProperty("event_id",selectedEventId);
        jsonObject.addProperty("token_type_id",selectedTokenId);
        jsonObject.addProperty("amount", selectedTokenAmount);
        jsonObject.addProperty("lead_stage_id", lead_stage_id);
        jsonObject.addProperty("remark", Objects.requireNonNull(edt_remarks.getText()).toString());


        //refund bank details
        jsonObject.addProperty("ac_holder_name", Objects.requireNonNull(edt_refundAcHolderName.getText()).toString());
        jsonObject.addProperty("bank_name", Objects.requireNonNull(edt_refundBankName.getText()).toString());
        jsonObject.addProperty("branch", Objects.requireNonNull(edt_refundBranchName.getText()).toString());
        jsonObject.addProperty("ifsc_no", Objects.requireNonNull(edt_refundIFSCNumber.getText()).toString());
        jsonObject.addProperty("bank_account_type",selectedAcType);
        jsonObject.addProperty("ac_no", Objects.requireNonNull(edt_refundACNumber.getText()).toString());


        if(paidVia ==1 && paymentMode == 1)//Cheque
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();
            JSONObject json3= new JSONObject();
            JSONObject json4= new JSONObject();
            try {
                json1.put("payment_mode_details_title","Check No");
                json1.put("payment_mode_details_description",edt_generateToken_cheque_number.getText());
                json2.put("payment_mode_details_title","Check Date");
                json2.put("payment_mode_details_description",setChequeDate);
                json3.put("payment_mode_details_title","Check Issuer");
                json3.put("payment_mode_details_description",edt_generateToken_cheque_issuer_name.getText());
                json4.put("payment_mode_details_title","Bank Name");
                json4.put("payment_mode_details_description",edt_generateToken_cheque_bank_name.getText());


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
                json1.put("payment_mode_details_description",edt_generateToken_utr_number.getText());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));

        }
        else if(paidVia ==1 &&  paymentMode == 3)//Card
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();

            try {
                json1.put("payment_mode_details_title","Transaction No");
                json1.put("payment_mode_details_description",edt_generateToken_payment_transaction_number.getText());
                json2.put("payment_mode_details_title","Invoice No");
                json2.put("payment_mode_details_description",edt_generateToken_payment_invoice_number.getText());


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
        Call<JsonObject> call = client.getApiService().Post_generate_AddToken(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful())
                {
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
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");

                        }
                    }
                }
                else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }



    private void setTokenSuccessJson(JsonObject jsonObject)
    {
        TokensModel tokensModel = new TokensModel();
        //if (jsonObject.has("lead_id")) lead_Id=!jsonObject.get("lead_id").isJsonNull()&&!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsString() : "0";
        if (jsonObject.has("token_id")) tokensModel.setToken_type_id((!jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0));
        if (jsonObject.has("token_type_id")) token_type_id = !jsonObject.get("token_type_id").isJsonNull() ? jsonObject.get("token_type_id").getAsInt() : 0;
        if (jsonObject.has("token_no")) tokensModel.setToken_type((!jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString().trim() : ""));
        if (jsonObject.has("payment_link")) tokensModel.setPayment_link((!jsonObject.get("payment_link").isJsonNull() ? jsonObject.get("payment_link").getAsString().trim() : ""));
        if (jsonObject.has("payment_link")) payment_link = !jsonObject.get("payment_link").isJsonNull() ? jsonObject.get("payment_link").getAsString().trim() : "";
        if (jsonObject.has("payment_invoice_id")) tokensModel.setPayment_invoice_id(!jsonObject.get("payment_invoice_id").isJsonNull() ? jsonObject.get("payment_invoice_id").getAsString().trim() : "");
        if (jsonObject.has("payment_invoice_id")) payment_invoice_id = !jsonObject.get("payment_invoice_id").isJsonNull() ? jsonObject.get("payment_invoice_id").getAsString().trim() : "";

        if (jsonObject.has("ghp_date"))ghp_date=!jsonObject.get("ghp_date").isJsonNull() ? jsonObject.get("ghp_date").getAsString() :"";
        if (jsonObject.has("ghp_amount"))ghp_amount=!jsonObject.get("ghp_amount").isJsonNull() ? jsonObject.get("ghp_amount").getAsString() :"";
        if (jsonObject.has("ghp_plus_date"))ghp_plus_date=!jsonObject.get("ghp_plus_date").isJsonNull() ? jsonObject.get("ghp_plus_date").getAsString() :"";
        if (jsonObject.has("ghp_plus_amount"))ghp_plus_amount=!jsonObject.get("ghp_plus_amount").isJsonNull() ? jsonObject.get("ghp_plus_amount").getAsString() :"";
        if (jsonObject.has("remark"))ghp_remark=!jsonObject.get("remark").isJsonNull() ? jsonObject.get("remark").getAsString() :"";
        if (jsonObject.has("token_media_path")) {
            tokensModel.setToken_document_path((!jsonObject.get("token_media_path").isJsonNull() ? jsonObject.get("token_media_path").getAsString().trim() : ""));
            token_document = !jsonObject.get("token_media_path").isJsonNull() ? jsonObject.get("token_media_path").getAsString().trim() : null;
        }
        token_No =tokensModel.getToken_type().trim();
        //  Log.e(TAG, "setTokenSuccessJson: "+token_document);
    }


    private void PostAddSiteVisitToken()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",lead_id);
        jsonObject.addProperty("check_in_date_time", visit_date_api +" "+ visit_time_api);
        jsonObject.addProperty("verified_by_id",verified_by_id);
        jsonObject.addProperty("project_id", project_id);
        jsonObject.addProperty("unit_category_id", flat_id);
        jsonObject.addProperty("sales_person_id",user_id);
        jsonObject.addProperty("lead_stage_id", lead_stage_id);

        //if pay using payment gateway then payment mode id should be 4
        //if pay using pay link then payment mode id should be 5
        jsonObject.addProperty("payment_mode_id", paidVia ==1 ? paymentMode : paidVia ==2 ? 4 : 5);
        jsonObject.addProperty("event_id",selectedEventId);
        jsonObject.addProperty("token_type_id",selectedTokenId);
        jsonObject.addProperty("amount", selectedTokenAmount);
        jsonObject.addProperty("remark",remark);

        //bank details
        jsonObject.addProperty("ac_holder_name", Objects.requireNonNull(edt_refundAcHolderName.getText()).toString());
        jsonObject.addProperty("bank_name", Objects.requireNonNull(edt_refundBankName.getText()).toString());
        jsonObject.addProperty("branch", Objects.requireNonNull(edt_refundBranchName.getText()).toString());
        jsonObject.addProperty("ifsc_no", Objects.requireNonNull(edt_refundIFSCNumber.getText()).toString());
        jsonObject.addProperty("bank_account_type",selectedAcType);
        jsonObject.addProperty("ac_no", Objects.requireNonNull(edt_refundACNumber.getText()).toString());
        jsonObject.addProperty("remark", Objects.requireNonNull(edt_remarks.getText()).toString());
        //jsonObject.addProperty("remark", !Objects.requireNonNull(edt_remarks.getText()).toString().trim().isEmpty() ? Objects.requireNonNull(edt_remarks.getText()).toString() :"No Remarks!" );

        if(paidVia ==1 &&  paymentMode == 1)//Cheque
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();
            JSONObject json3= new JSONObject();
            JSONObject json4= new JSONObject();
            try {
                json1.put("payment_mode_details_title","Check No");
                json1.put("payment_mode_details_description",edt_generateToken_cheque_number.getText());
                json2.put("payment_mode_details_title","Check Date");
                json2.put("payment_mode_details_description",setChequeDate);
                json3.put("payment_mode_details_title","Check Issuer");
                json3.put("payment_mode_details_description",edt_generateToken_cheque_issuer_name.getText());
                json4.put("payment_mode_details_title","Bank Name");
                json4.put("payment_mode_details_description",edt_generateToken_cheque_bank_name.getText());


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
        else if(paidVia ==1 &&  paymentMode == 2)//Online
        {
            JSONObject json1= new JSONObject();
            try {
                json1.put("payment_mode_details_title","UTR No");
                json1.put("payment_mode_details_description",edt_generateToken_utr_number.getText());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }
        else if(paidVia ==1 &&  paymentMode == 3)//Card
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();

            try {
                json1.put("payment_mode_details_title","Transaction No");
                json1.put("payment_mode_details_description",edt_generateToken_payment_transaction_number.getText());
                json2.put("payment_mode_details_title","Invoice No");
                json2.put("payment_mode_details_description",edt_generateToken_payment_invoice_number.getText());


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
            //paid by payment gateway == payment mode - 4

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
        else
        {
            //paid via payment link, paymentMode ==5

            // payment modes json
            JSONObject json1 = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_generate_AddToken_siteVisit(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1"))
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setTokenSuccessAddSiteVisitJson(data);
                                    onSuccessTokenGenerate();
                                }
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");

                        }
                    }
                }
                else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void setTokenSuccessAddSiteVisitJson(JsonObject jsonObject)
    {

        if (jsonObject.has("token") && !jsonObject.get("token").isJsonNull())
        {
            if (jsonObject.get("token").isJsonObject())
            {
                JsonObject asJsonObject = jsonObject.get("token").getAsJsonObject();
                TokensModel tokensModel = new TokensModel();

                if (asJsonObject.has("token_type_id")) tokensModel.setToken_type_id((!asJsonObject.get("token_type_id").isJsonNull() ? asJsonObject.get("token_type_id").getAsInt() : 0));
                if (asJsonObject.has("token_type_id")) token_type_id = !asJsonObject.get("token_type_id").isJsonNull() ? asJsonObject.get("token_type_id").getAsInt() : 0;
                if (asJsonObject.has("token_no")) tokensModel.setToken_type((!asJsonObject.get("token_no").isJsonNull() ? asJsonObject.get("token_no").getAsString().trim() : ""));
                if (asJsonObject.has("payment_link")) tokensModel.setPayment_link((!asJsonObject.get("payment_link").isJsonNull() ? asJsonObject.get("payment_link").getAsString().trim() : ""));
                if (asJsonObject.has("payment_link")) payment_link = !asJsonObject.get("payment_link").isJsonNull() ? asJsonObject.get("payment_link").getAsString().trim() : "";
                if (asJsonObject.has("payment_invoice_id")) tokensModel.setPayment_invoice_id(!asJsonObject.get("payment_invoice_id").isJsonNull() ? asJsonObject.get("payment_invoice_id").getAsString().trim() : "");
                if (asJsonObject.has("payment_invoice_id")) payment_invoice_id = !asJsonObject.get("payment_invoice_id").isJsonNull() ? asJsonObject.get("payment_invoice_id").getAsString().trim() : "";

                if (asJsonObject.has("ghp_date"))ghp_date=!asJsonObject.get("ghp_date").isJsonNull() ? asJsonObject.get("ghp_date").getAsString() :"";
                if (asJsonObject.has("ghp_amount"))ghp_amount=!asJsonObject.get("ghp_amount").isJsonNull() ? asJsonObject.get("ghp_amount").getAsString() :"";
                if (asJsonObject.has("ghp_plus_date"))ghp_plus_date=!asJsonObject.get("ghp_plus_date").isJsonNull() ? asJsonObject.get("ghp_plus_date").getAsString() :"";
                if (asJsonObject.has("ghp_plus_amount"))ghp_plus_amount=!asJsonObject.get("ghp_plus_amount").isJsonNull() ? asJsonObject.get("ghp_plus_amount").getAsString() :"";
                if (asJsonObject.has("remark"))ghp_remark=!asJsonObject.get("remark").isJsonNull() ? asJsonObject.get("remark").getAsString() :"";

                if (asJsonObject.has("token_media_path")) {
                    tokensModel.setToken_document_path((!asJsonObject.get("token_media_path").isJsonNull() ? asJsonObject.get("token_media_path").getAsString().trim() : ""));
                    token_document = !asJsonObject.get("token_media_path").isJsonNull() ? asJsonObject.get("token_media_path").getAsString().trim() : null;
                }
                token_No =tokensModel.getToken_type().trim();
                //  Log.e(TAG, "setTokenSuccessJson: "+token_document);
            }
        }

    }


    private void onSuccessTokenGenerate()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            if (paidVia==3){
                //show pending msg
                new Helper().showCustomToast(context, "GHP Pending!");

                //set lead status id to 13
                lead_status_id =13; //ghp pending

            }
            else {
                //show success msg
                new Helper().showSuccessCustomToast(context, "GHP Generated Successfully!");
            }

            //TODO call update customer details api
            if (isEditCustomerDetails) {

                //call update customer details api
                if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                    showProgressBar("Updating customer info...");
                    post_UpdateLead();
                }
                else  Helper.NetworkError(context);
            }
            else
            {
                //show token generate success msg and data

                //set customer data
                setShareTokenCustomerData();
                //show/ hide layouts
                Objects.requireNonNull(getSupportActionBar()).hide();
                change_status_bar_color(R.color.main_white);
                ll_shareToken_View.setVisibility(View.VISIBLE);
                ll_generateToken_View.setVisibility(View.GONE);
            }


            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

        });
    }


    private void setShareTokenCustomerData()
    {

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
            tv_shareToken_successMsg.setText(token_type_id==3 || selectedTokenId ==3 ? getString(R.string.ghp_plus_request_generated) : getString(R.string.ghp_request_generated));
            tv_shareToken_successMsg.setTextColor(getResources().getColor(R.color.color_pending));
            //2.1 visible payment pending msg
            tv_shareToken_paymentPendingMsg.setText(getString(R.string.payment_pending));
            tv_shareToken_paymentPendingMsg.setVisibility(View.VISIBLE);
            //2.2 change title text
            //set GHP paid date and Paid Amount text
            tv_ghp_date_text.setText(selectedTokenId ==3 ? getString(R.string.ghp_date) : token_type_id ==3 ?   getString(R.string.ghp_paid_date) : getString(R.string.ghp_request_date));
            tv_ghp_amount_text.setText(selectedTokenId ==3 ? getString(R.string.ghp_amount_) : token_type_id ==3 ?   getString(R.string.ghp_paid_amount) :  getString(R.string.ghp_amount_payable));
            //set GHP+ request date and payable Amount
            tv_ghp_plus_date_text.setText(getString(R.string.ghp_plus_request_date));
            tv_ghp_plus_amount_text.setText(getString(R.string.ghp_plus_amount_payable));

            //3. hide token number
            tv_Customer_GHPNo.setVisibility(View.GONE);

            //4. hide upgrade to GHP button
            mBtn_upgradeToGHPPlus.setVisibility(View.GONE);

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
            tv_shareToken_successMsg.setText(token_type_id==3 || selectedTokenId ==3 ? getString(R.string.sales_ghp_plus_success) : getString(R.string.sales_ghp_success));
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

            //4. visible upgrade to GHP button as per conditions
            mBtn_upgradeToGHPPlus.setVisibility(token_type_id==1 ?View.VISIBLE : View.GONE);

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

        if(project_name != null && !project_name.trim().isEmpty()) {
            tv_Customer_ProjectName.setText(project_name);
            tv_Customer_ProjectName.setVisibility(View.VISIBLE);
        }

        if(event_name != null && !event_name.trim().isEmpty()) {
            tv_Customer_EventName.setText(event_name);
            tv_Customer_EventName.setVisibility(View.VISIBLE);
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

        if(ghp_remark != null && !ghp_remark.trim().isEmpty()) {
            tv_ShareToken_remarks.setText(ghp_remark);
            // ll_ShareToken_remarks.setVisibility(View.VISIBLE);
        }

        //close view Token data
        iv_shareToken_close.setOnClickListener(view -> gotoSalesFeed());

        //view_ghpDetails.setVisibility(token_type_id==1 || token_type_id==3 ?View.VISIBLE : View.GONE);
        view_ghpDetails.setVisibility(View.VISIBLE);
        tv_Customer_GHPNo.setTextColor(token_type_id==3 || selectedTokenId ==3 ? getResources().getColor(R.color.color_token_plus_generated) : getResources().getColor(R.color.color_token_generated));

        //upgrade to GHP +
        mBtn_upgradeToGHPPlus.setOnClickListener(v -> startActivity(new Intent(context, UpgradeGHPPlusActivity.class)
                .putExtra("cuidModel", cuidModel)));

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

        // MBtn share Token  more apps
        mBtn_shareToken_MoreShare.setOnClickListener(view -> {

            if (token_document!=null && !token_document.trim().isEmpty()) {

                sendGHPDetailsMoreApps(customer_name, token_document, token_No, project_name);
            }
            else new Helper().showCustomToast(context, "GHP document not found!");
        });


        //MBtn Share Token
        mBtn_shareToken_share.setOnClickListener(view -> {
            isDownloadViewShare = 3;

            //directly send on whatsApp number
            sendMessageToWhatsApp(country_code+customer_mobile, token_document, token_No, project_name);

            // if (token_document!=null) isStoragePermissionGranted(token_document, 3);
            // else new Helper().showCustomToast(context, getString(R.string.ghp_doc_not_found));
        });


        //share payLink on whatsApp
        mBtn_sharePayLink_waShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {
                //directly send link on customer's whatsApp number
                sendPayLinkOnWhatsApp(country_code+customer_mobile, payment_link, event_name, project_name);
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });


        //share payLink on mail
        mBtn_sharePayLink_mailShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {

                //directly send link on customer's email id
                //final String extra_text = "Hello "+customer_name +", \n\n" + "Thank you for showing your interest for Golden Hour Pass(GHP) of project " + project_name + ". \n You are just one step away from becoming a member of Leado Family! "+ "\n\n\n\n Kindly click on the below link and pay for your Golden Hour Pass. And get confirm your GHP for the event "+ event_name +"." +"\n\n "+ payment_link+ "\n\n";
                //final String extra_text =  token_type_id==3 || selectedTokenId ==3  ? context.getString(R.string.cim_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link) : context.getString(R.string.cim_ghp_pending_msg, customer_name, project_name, event_name, payment_link);
                final String extra_text =  selectedTokenId ==3  ? context.getString(R.string.cim_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                        : token_type_id == 3 ? context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                        : context.getString(R.string.cim_ghp_pending_msg, customer_name, project_name, event_name, payment_link);

                new Helper().openEmailIntent(context,  customer_email, "Pay and Confirm your Golden Hour Pass(GHP)", extra_text);
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });

        //share payLink on more apps
        mBtn_sharePayLink_moreShare.setOnClickListener(view -> {

            if (payment_link!=null && !payment_link.trim().isEmpty()) {
                //directly send link on customer's whatsApp number
                sendPayLinkOnMoreApps( payment_link, event_name, project_name);
            }
            else new Helper().showCustomToast(context, "Payment link might be null or empty!");
        });

    }



    private void setCustomerEditOption()
    {
        edt_First_Name.setText(first_name);
        edt_Middle_Name.setText(middle_name);
        edt_Last_Name.setText(last_name);
        selected_country_tv.setText(country_code);
        edt_shareToken_Customer_MobileNo.setText(customer_mobile);
        edt_shareToken_Customer_Email.setText(customer_email);

        //Country Code Selection
        ccp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 121));
        ll_ccp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 121));

        //edit contact
        rdGrp_edit_contact.setOnCheckedChangeListener((radioGroup, i) -> {
            int id = rdGrp_edit_contact.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rbtn_customer_edit_contact_yes:
                    isEditCustomerDetails = true;
                    //ll_ShareToken_edit_customerInfo.setVisibility(View.VISIBLE);
                    expandSubView(ll_ShareToken_edit_customerInfo);
                    break;

                case R.id.rbtn_customer_edit_contact_no:
                    isEditCustomerDetails = false;
                    collapse(ll_ShareToken_edit_customerInfo);
                    //ll_ShareToken_edit_customerInfo.setVisibility(View.GONE);
                    break;
            }
        });


        //Lead update details
        btn_shareToken_submit.setOnClickListener(view -> UpdateLeadValidation());

    }


    private void setPaymentModeOption()
    {
        //edit contact
        rGrp_payMode.setOnCheckedChangeListener((radioGroup, i) -> {
            int id = rGrp_payMode.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rBtn_generateToken_pay_manual:
                    paidVia = 1;
                    //generate GHP
                    mBtn_generateToken.setText(selectedTokenId ==3 ? getString(R.string.generate_ghp_plus) : getString(R.string.generate_ghp) );
                    expandSubView(ll_payMode_manual);
                    break;

                case R.id.rBtn_generateToken_pay_payGateway:
                    paidVia = 2;
                    //pay and generate GHP
                    mBtn_generateToken.setText(selectedTokenId ==3 ? getString(R.string.pay_n_generate_ghp_plus): getString(R.string.pay_n_generate_ghp));
                    collapse(ll_payMode_manual);
                    break;
                case R.id.rBtn_generateToken_pay_viaPayLink:
                    paidVia = 3;
                    // generate GHP and paid via pay link
                    mBtn_generateToken.setText(selectedTokenId ==3 ? getString(R.string.generate_ghp_plus) : getString(R.string.generate_ghp) );
                    collapse(ll_payMode_manual);
                    break;
            }
        });
    }


    private void setAdapterForMrs()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, personPrefixStringArrayList);
        acTv_Customer_mrs.setAdapter(adapter);
        acTv_Customer_mrs.setThreshold(0);
        //acTv_Customer_mrs.setText(personPrefixStringArrayList.indexOf());

        acTv_Customer_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);
            for (PersonNamePrefixModel pojo : personPrefixModelArrayList)
            {
                if (pojo.getName_prefix().equals(itemName))
                {

                    selectedPrefixId = pojo.getName_prefix_id(); // This is the correct ID
                    selectedPrefix = pojo.getName_prefix();

                    Log.e(TAG, "Project name & id " + selectedPrefixId +"\t"+ selectedPrefix);
                    break; // No need to keep looping once you found it.
                }
            }
        });

    }

    private void setAdapterForACType()
    {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, accountTypeStringArrayList);
        acTv_acType.setAdapter(adapter);
        acTv_acType.setThreshold(0);
        //acTv_Customer_mrs.setText(personPrefixStringArrayList.indexOf());

        acTv_acType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {

            String itemName = adapter.getItem(position);
            for (AccountTypeModel pojo : accountTypeModelArrayList)
            {
                if (pojo.getAc_type().equals(itemName))
                {

                    selectedAcTypeId = pojo.getAc_type_id(); // This is the correct ID
                    selectedAcType = pojo.getAc_type();

                    Log.e(TAG, "Ac type & id " + selectedAcType +"\t"+ selectedAcTypeId);
                    break; // No need to keep looping once you found it.
                }
            }
        });

    }


    private void UpdateLeadValidation()
    {
        //name prefix
        if (selectedPrefixId==0) new Helper().showCustomToast(this, "Please select name prefix!");
            //first name
        else if (Objects.requireNonNull(edt_First_Name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer First Name!");
            //last name
        else if (Objects.requireNonNull(edt_Last_Name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer Last Name!");
            //mobile number
        else if (Objects.requireNonNull(edt_shareToken_Customer_MobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(this, "Please Enter Customer Mobile number!");
        else
        {
            showCustomerInfoConfirmationAlert();
        }
    }

    public void showCustomerInfoConfirmationAlert()
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

        tv_msg.setText(getResources().getString(R.string.confirm_details));
        tv_desc.setText(getString(R.string.edit_customer_info_confirm_text));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call api
            //ll_ShareToken_edit_customerInfo.setVisibility(View.GONE);

            /*Get Event List*/
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                showProgressBar("Updating customer info...");
                post_UpdateLead();
            }
            else  Helper.NetworkError(context);

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


    private void post_UpdateLead()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",lead_id);
        jsonObject.addProperty("country_code",country_code);
        jsonObject.addProperty("prefix",acTv_Customer_mrs.getText().toString());
        jsonObject.addProperty("first_name", Objects.requireNonNull(edt_First_Name.getText()).toString());
        jsonObject.addProperty("middle_name", Objects.requireNonNull(edt_Middle_Name.getText()).toString());
        jsonObject.addProperty("last_name", Objects.requireNonNull(edt_Last_Name.getText()).toString());
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_shareToken_Customer_MobileNo.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_shareToken_Customer_Email.getText()).toString());

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().Post_updateLead(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            onSuccessUpdateInfo();
                        }
                        else showErrorLogUpdateLead("Failed to update customer details! Try again.");
                    }
                }
                else {
                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLogUpdateLead(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLogUpdateLead(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLogUpdateLead(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLogUpdateLead(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLogUpdateLead(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLogUpdateLead(getString(R.string.weak_connection));
                else showErrorLogUpdateLead(e.toString());
            }
        });
    }

    private void onSuccessUpdateInfo()
    {
        runOnUiThread(() -> {

            hideProgressBar();
            new Helper().showSuccessCustomToast(context,getResources().getString(R.string.contact_edit_success));

            //if updated customer details while GHP generation then show token data
            if (isEditCustomerDetails) {

                //call update customer details api

                //set customer data
                setShareTokenCustomerData();
                //show/ hide layouts
                Objects.requireNonNull(getSupportActionBar()).hide();
                change_status_bar_color(R.color.main_white);
                ll_shareToken_View.setVisibility(View.VISIBLE);
                ll_generateToken_View.setVisibility(View.GONE);
            }
            else {
                // goto sales feed
                new Handler().postDelayed(this::gotoSalesFeed, 1000);
            }


        });
    }

    private void gotoSalesFeed() {

        startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class)
                .addFlags(FLAG_ACTIVITY_CLEAR_TOP |  FLAG_ACTIVITY_SINGLE_TOP));
        finish();
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

                        if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                        else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());

                         /*if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {

                                throw new IOException("Cleartext HTTP traffic to " + host + " not permitted");
                            }*/


                        context.runOnUiThread(() -> {
                            //hide pb
                            hideProgressBar();
                            new Helper().showCustomToast(context, "Unable to download the document!");

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
        final String extra_text =  selectedTokenId ==3  ? context.getString(R.string.cim_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                : token_type_id == 3 ? context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                : context.getString(R.string.cim_ghp_pending_msg, customer_name, project_name, event_name, payment_link);

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
        final String extra_text =  selectedTokenId ==3  ? context.getString(R.string.cim_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                : token_type_id == 3 ? context.getString(R.string.cim_upgrade_to_ghp_plus_pending_msg, customer_name, project_name, event_name, payment_link)
                : context.getString(R.string.cim_ghp_pending_msg, customer_name, project_name, event_name, payment_link);

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
        final String extra_text = "Hello "+"*"+customer_name+"*"+", \n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n";
        //final String extra_text = "\n\n Welcome to VJ family! \n\nThank you for *Golden Hour Pass( "+ token_No + " )* of " + project_name + ". \n\n\nDownload your GHP here \n\n "+ main_title+ "\n\n";

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


    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //ll_pb.setVisibility(View.GONE);
            Helper.onErrorSnack(context, message);

        });
    }

    private void showErrorLogRenewal(final String message) {

        context.runOnUiThread(() ->
        {
            //hide pb
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });
    }


    private void showErrorLogUpdateLead(final String message)
    {
        if (context!=null)
            context.runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                //ll_pb.setVisibility(View.GONE);
                Helper.onErrorSnack(context, message);

                if (isEditCustomerDetails) {

                    //set customer data
                    setShareTokenCustomerData();
                    //show/ hide layouts
                    Objects.requireNonNull(getSupportActionBar()).hide();
                    change_status_bar_color(R.color.main_white);
                    ll_shareToken_View.setVisibility(View.VISIBLE);
                    ll_generateToken_View.setVisibility(View.GONE);
                }

            });
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


    private boolean isValidIFSC(EditText email)
    {
        boolean ret = true;
        if (!Validation.isIFSCCode(email, true)) ret = false;
        //if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
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
        super.onBackPressed();
        //if (isTokenGenerated) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Token Generated!"));
    }


}
