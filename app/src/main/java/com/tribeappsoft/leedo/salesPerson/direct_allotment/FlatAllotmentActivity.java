package com.tribeappsoft.leedo.salesPerson.direct_allotment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.SalesPersonBottomNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.scaleImage.ScaleImageActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;
import com.tribeappsoft.leedo.util.filepicker.MaterialFilePicker;
import com.tribeappsoft.leedo.util.filepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlatAllotmentActivity extends AppCompatActivity {


    @BindView(R.id.cl_flatAllotment) CoordinatorLayout parent;
    @BindView(R.id.ll_flatAllotment_main) LinearLayoutCompat ll_main;

    //sales representative
    @BindView(R.id.tv_flatAllotment_salesRepresentative) AppCompatTextView tv_salesRepresentative;
    @BindView(R.id.fl_flatAllotment_formImagesList) LinearLayoutCompat ll_formImagesList;

    //customer details main
    @BindView(R.id.ll_flatAllotment_customerDetailsMain) LinearLayoutCompat ll_customerDetailsMain;
    @BindView(R.id.iv_flatAllotment_customerDetails_ec) AppCompatImageView iv_customerDetails_ec;
    @BindView(R.id.ll_flatAllotment_viewCustomerInfo) LinearLayoutCompat ll_viewCustomerInfo;
    @BindView(R.id.tv_flatAllotment_customerId) AppCompatTextView tv_customerId;
    @BindView(R.id.tv_flatAllotment_customerName) AppCompatTextView tv_customerName;
    @BindView(R.id.tv_flatAllotment_customerMobile) AppCompatTextView tv_customerMobile;
    @BindView(R.id.tv_flatAllotment_customerEmail) AppCompatTextView tv_customerEmail;
    //applicant details
    @BindView(R.id.mCbx_flatAllotment_applicantDetails) MaterialCheckBox mCbx_applicantDetails;
    @BindView(R.id.edt_flatAllotment_applicant_name) TextInputEditText edt_applicant_name;
    @BindView(R.id.edt_flatAllotment_applicant_mobile) TextInputEditText edt_applicant_mobile;
    @BindView(R.id.edt_flatAllotment_applicant_email) TextInputEditText edt_applicant_email;

    //Source details
    @BindView(R.id.ll_flatAllotment_sourceDetailsMain) LinearLayoutCompat ll_sourceDetailsMain;
    @BindView(R.id.iv_flatAllotment_sourceDetails_ec) AppCompatImageView iv_sourceDetails_ec;
    @BindView(R.id.ll_flatAllotment_viewSourceInfo) LinearLayoutCompat ll_viewSourceInfo;
    @BindView(R.id.tv_flatAllotment_sourceMainName) AppCompatTextView tv_sourceMainName;
    @BindView(R.id.tv_flatAllotment_sourceSubName) AppCompatTextView tv_sourceSubName;

    //project flat details
    @BindView(R.id.ll_flatAllotment_flatDetailsMain) LinearLayoutCompat ll_flatDetailsMain;
    @BindView(R.id.iv_flatAllotment_flatDetails_ec) AppCompatImageView iv_flatDetails_ec;
    @BindView(R.id.ll_flatAllotment_viewFlatInfo) LinearLayoutCompat ll_viewFlatInfo;
    @BindView(R.id.tv_flatAllotment_flatType) AppCompatTextView tv_flatType;
    @BindView(R.id.tv_flatAllotment_projectName) AppCompatTextView tv_projectName;
    @BindView(R.id.edt_flatAllotment_flatAmount) TextInputEditText edt_flatAmount;

    //Allotment form attachment
    @BindView(R.id.ll_flatAllotment_allotmentDetailsMain) LinearLayoutCompat ll_allotmentDetailsMain;
    @BindView(R.id.iv_flatAllotment_allotmentDetails_ec) AppCompatImageView iv_allotmentDetails_ec;
    @BindView(R.id.ll_flatAllotment_viewAllotmentFormDetails) LinearLayoutCompat ll_viewAllotmentFormDetails;
    @BindView(R.id.iv_flatAllotment_allotmentDetails_defImg) AppCompatImageView iv_allotmentDetails_defImg;
    @BindView(R.id.hsv_flatAllotment_allotmentForm) HorizontalScrollView hsv_allotmentForm;
    @BindView(R.id.mBtn_flatAllotment_form) MaterialButton btn_form;

    //payment details
    @BindView(R.id.ll_flatAllotment_paymentDetailsMain) LinearLayoutCompat ll_paymentDetailsMain;
    @BindView(R.id.iv_flatAllotment_paymentDetails_ec) AppCompatImageView iv_paymentDetails_ec;
    @BindView(R.id.ll_flatAllotment_viewPaymentDetails) LinearLayoutCompat ll_viewPaymentDetails;
    @BindView(R.id.edt_flatAllotment_bookingAmount) TextInputEditText edt_bookingAmount;


    // pay slip attachments
    @BindView(R.id.ll_flatAllotment_paySleepAttachmentMain) LinearLayoutCompat ll_paySleepAttachmentMain;
    @BindView(R.id.iv_flatAllotment_paySleepAttachment_ec) AppCompatImageView iv_paySleepAttachment_ec;
    @BindView(R.id.ll_flatAllotment_viewPaySlipDetails) LinearLayoutCompat ll_viewPaySlipDetails;
    @BindView(R.id.mBtn_flatAllotment_addPaySlip) MaterialButton mBtn_addPaySlip;
    @BindView(R.id.iv_flatAllotment_paySlipDetails_defImg) AppCompatImageView iv_paySlipDetails_defImg;
    @BindView(R.id.hsv_flatAllotment_paySlip) HorizontalScrollView hsv_paySlip;
    @BindView(R.id.ll_flatAllotment_paySlipImagesList) LinearLayoutCompat fl_paySlipImagesList;


    //payment mode
    @BindView(R.id.ll_flatAllotment_paymentModeMain) LinearLayoutCompat ll_paymentModeMain;
    @BindView(R.id.iv_flatAllotment_paymentMode_ec) AppCompatImageView iv_paymentMode_ec;
    @BindView(R.id.ll_flatAllotment_viewPaymentMode) LinearLayoutCompat ll_viewPaymentMode;
    @BindView(R.id.rdoGrp_flatAllotment_paymentType) RadioGroup paymentType;
    @BindView(R.id.rb_flatAllotment_cheque) AppCompatRadioButton cheque;
    @BindView(R.id.rb_flatAllotment_card) AppCompatRadioButton card;
    @BindView(R.id.rb_flatAllotment_online) AppCompatRadioButton online;
    @BindView(R.id.ll_flatAllotment_cardPayment) LinearLayoutCompat ll_cardPayment;
    @BindView(R.id.ll_flatAllotment_chequePayment) LinearLayoutCompat ll_chequePayment;
    @BindView(R.id.ll_flatAllotment_onlinePayment) LinearLayoutCompat ll_onlinePayment;
    @BindView(R.id.edt_flatAllotment_bankName) TextInputEditText edt_bank_name;
    @BindView(R.id.edt_flatAllotment_accHolderName) TextInputEditText edt_account_holder_name;
    @BindView(R.id.edt_flatAllotment_chequeNumber) TextInputEditText edt_cheque_no;
    @BindView(R.id.edt_flatAllotment_chequeDate) TextInputEditText edt_cheque_date;
    @BindView(R.id.edt_flatAllotment_utrNo) TextInputEditText edt_utr_no;
    @BindView(R.id.edt_flatAllotment_transactionNo) TextInputEditText edt_transaction_no;
    @BindView(R.id.edt_flatAllotment_invoiceNo) TextInputEditText edt_invoice_no;
    @BindView(R.id.edt_flatAllotment_remark) TextInputEditText edt_remark;

    //confirm allotment
    @BindView(R.id.mBtn_flatAllotment_confirmAllotment) MaterialButton mBtn_confirmAllotment;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.view_flatAllotment_disableLayout) View viewDisableLayout;
    @BindView(R.id.ll_flatAllotment_success) LinearLayoutCompat ll_success;
    @BindView(R.id.gif_flatAllotment) GifImageView gif;

    // Booking Documents
    @BindView(R.id.ll_flatAllotment_bookingDocuments) LinearLayoutCompat ll_bookingDocuments;
    @BindView(R.id.ll_flatAllotment_bookingDocumentsMain) LinearLayoutCompat ll_bookingDocumentsMain;
    @BindView(R.id.iv_flatAllotment_bookingDocuments_ec) AppCompatImageView iv_bookingDocuments_ec;
    @BindView(R.id.ll_flatAllotment_viewBookingDocuments) LinearLayoutCompat ll_viewBookingDocuments;
    //Allotment Document
    @BindView(R.id.tv_flatAllotment_allotmentDocument_name) AppCompatTextView tv_allotmentDocument_name;
    @BindView(R.id.iv_flatAllotment_browseAllotmentDocument) AppCompatImageView iv_browseAllotmentDocument;
     //KYC  Document
    @BindView(R.id.tv_flatAllotment_kycDocument_name) AppCompatTextView tv_kycDocument_name;
    @BindView(R.id.iv_flatAllotment_browseKycDocument) AppCompatImageView iv_browseKycDocument;
     //Pay Slip  Document
    @BindView(R.id.tv_flatAllotment_playSlipDocument_name) AppCompatTextView tv_playSlipDocument_name;
    @BindView(R.id.iv_flatAllotment_browsePaySlipDocument) AppCompatImageView iv_browsePaySlipDocument;



    private AppCompatActivity context;
    private String TAG ="FlatAllotmentActivity", api_token ="", unit_name ="", project_name ="",setChequeDate="",
            country_code="91", mobile_number ="", email ="", customer_name="",cuidNumber="";

    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;

    private ArrayList<String> allotmentFormImagesArrayList, paySlipAttachmentsStringArrayList;
    private boolean fromAddHoldFlat = false, fromHoldList = false, isFormOrPaySlip = true, viewCustomerDetails =false, viewSourceDetails =false,
            viewFlatDetails =false, viewAllotmentDetails =false,isLeadSubmitted = false, viewPaymentDetails =false,
            viewPaymentSlipDetails =false, viewPaymentModeDetails =false, viewBookingDocuments = false,
            isDocumentSelected= false;
    private int user_id = 0, unit_id =0, lead_id = 0, project_id =0,block_id =0,floor_id =0,
            unit_hold_release_id=0, formAttachAPICount =0,paySlipAttachAPICount =0,documentCountAppln=0,documentCountPaySlip=0,paymentMode=1,
            mYear, mMonth, mDay, booking_id =0, booking_attachment_id = 0, //TODO booking_attachment_id 1 -> Allotment Document, 2-> KYC Document, 3 -> Pay Slip Document
            documentCount =0, docAPICount=0; //TODO doc_type_id = 4 -> KYC document, 5 -> Allotment Form, 6 -> Pay Slip
    private CUIDModel cuidModel = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flat_allotment);
        ButterKnife.bind(this);
        context = FlatAllotmentActivity.this;
        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null)
        {
            ///getSupportActionBar().setTitle(s);

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.flat_allotment));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setElevation(0); // to disable action bar elevation
        }

        if (getIntent()!=null)
        {
            fromAddHoldFlat = getIntent().getBooleanExtra("fromAddHoldFlat", false);
            fromHoldList = getIntent().getBooleanExtra("fromHoldList", false);
            unit_hold_release_id=getIntent().getIntExtra("unit_hold_release_id",0);
            unit_id = getIntent().getIntExtra("unit_id", 0);
            unit_name = getIntent().getStringExtra("unit_name");
            project_id = getIntent().getIntExtra("project_id", 0);
            project_name = getIntent().getStringExtra("project_name");
            block_id = getIntent().getIntExtra("block_id", 0);
            floor_id = getIntent().getIntExtra("floor_id", 0);
            cuidModel = (CUIDModel) getIntent().getSerializableExtra("cuidModel");
        }

        //hidden pb
        hideProgressBar();

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //set sales representative name
        tv_salesRepresentative.setText(sharedPreferences.getString("full_name", ""));

        eventProjectDocsModelArrayList = new ArrayList<>();
        eventProjectDocsModelArrayList.clear();

        //get customer details from cuId model
        if (cuidModel != null) {

            cuidNumber = cuidModel.getCu_id();
            country_code = cuidModel.getCountry_code();
            customer_name = cuidModel.getCustomer_name();
            mobile_number = cuidModel.getCustomer_mobile();
            email = cuidModel.getCustomer_email();
            lead_id = cuidModel.getLead_id();

            //check button enabled
            checkButtonEnabled();
        }

//        Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //initialise defaults
        init();

        //add form images
        btn_form.setOnClickListener(v -> {
            isFormOrPaySlip = true;
            askPermissionForCamera();
        });

        //add pay slip images
        mBtn_addPaySlip.setOnClickListener(v -> {
            isFormOrPaySlip = false;
            askPermissionForCamera();
        });

        //set imagesView
        getAllotmentFormImages();

        //set imagesView
        getPaySlipImages();

        //set payment modes
        setPaymentMode();

        //Set Cheque Date
        edt_cheque_date.setOnClickListener(view -> selectChequeDate());

        //confirm Allotment
        mBtn_confirmAllotment.setOnClickListener(view -> {

            //hide keyboard if opened
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidations();
        });


        //click on View
        viewDisableLayout.setOnClickListener(v -> {
            //hide success layout
            ll_success.setVisibility(View.GONE);
            //close View
            closeView();
            //start activity
            Intent intent=new Intent(context, SalesPersonBottomNavigationActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });


        //select Allotment document
        iv_browseAllotmentDocument.setOnClickListener(view -> {
            //set booking_attachment_id to 1
            booking_attachment_id = 1;
            selectDocumentPopup();
        });

        //select KYC document
        iv_browseKycDocument.setOnClickListener(view -> {
            //set booking_attachment_id to 2
            booking_attachment_id = 2;
            selectDocumentPopup();
        });

        //select Pay Slip document
        iv_browsePaySlipDocument.setOnClickListener(view -> {
            //set booking_attachment_id to 3
            booking_attachment_id = 3;
            selectDocumentPopup();
        });

    }



    private void init()
    {

        allotmentFormImagesArrayList = new ArrayList<>();
        paySlipAttachmentsStringArrayList = new ArrayList<>();

        //set customer details
        tv_customerId.setText(cuidNumber);
        tv_customerName.setText(customer_name);
        tv_customerMobile.setText(String.format("%s - %s", country_code, mobile_number));
        tv_customerEmail.setText(email);

        //set customer details as applicant details
        edt_applicant_name.setText(customer_name);
        edt_applicant_mobile.setText(mobile_number);
        edt_applicant_email.setText(email);

        //set source details
        //tv_sourceMainName.setText();

        //set project details
        tv_flatType.setText(unit_name);
        tv_projectName.setText(project_name);


        //set required data expanded
        new Handler(getMainLooper()).postDelayed(() -> {

            //def set expandView to lead details & project details & kyc documents
            new Animations().toggleRotate(iv_customerDetails_ec, true);
            viewCustomerDetails = true;

            new Animations().toggleRotate(iv_sourceDetails_ec, true);
            viewSourceDetails = true;

            new Animations().toggleRotate(iv_flatDetails_ec, true);
            viewFlatDetails = true;

            new Animations().toggleRotate(iv_allotmentDetails_ec, true);
            viewAllotmentDetails = true;

            new Animations().toggleRotate(iv_paymentDetails_ec, true);
            viewPaymentDetails = true;

            new Animations().toggleRotate(iv_paySleepAttachment_ec, true);
            viewPaymentSlipDetails = true;

            new Animations().toggleRotate(iv_paymentMode_ec, true);
            viewPaymentModeDetails = true;

            new Animations().toggleRotate(iv_bookingDocuments_ec, true);
            viewBookingDocuments = true;

        }, 100);


    }

    private void selectChequeDate()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
                    setChequeDate = Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    edt_cheque_date.setText(setChequeDate);

                    //check button enabled
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //set toggle views
        toggleViews();
    }



    private void toggleViews() {

        //cb applicant details
        mCbx_applicantDetails.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //checked editable false
                edt_applicant_name.setEnabled(false); //non editable
                edt_applicant_mobile.setEnabled(false); //non editable
                edt_applicant_email.setEnabled(false); //non editable

                //set customer details as applicant details
                edt_applicant_name.setText(customer_name);
                edt_applicant_mobile.setText(mobile_number);
                edt_applicant_email.setText(email);
            }
            else {

                //un_checked  editable true
                edt_applicant_name.setEnabled(true); //editable
                edt_applicant_mobile.setEnabled(true); //editable
                edt_applicant_email.setEnabled(true); //editable

                //set applicant details empty
                edt_applicant_name.setText("");
                edt_applicant_mobile.setText("");
                edt_applicant_email.setText("");
            }

            //check button enabled
            checkButtonEnabled();
        });


        //customer_details_main
        ll_customerDetailsMain.setOnClickListener(v -> {

            if (viewCustomerDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_customerDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewCustomerInfo);
                viewCustomerDetails = false;
            } else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_customerDetails_ec, true);
                expandSubView(ll_viewCustomerInfo);
                viewCustomerDetails = true;
            }
        });


        //source_details_main
        ll_sourceDetailsMain.setOnClickListener(v -> {

            if (viewSourceDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_sourceDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewSourceInfo);
                viewSourceDetails = false;
            } else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_sourceDetails_ec, true);
                expandSubView(ll_viewSourceInfo);
                viewSourceDetails = true;
            }
        });

        //flat_details_main
        ll_flatDetailsMain.setOnClickListener(v -> {

            if (viewFlatDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_flatDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewFlatInfo);
                viewFlatDetails = false;
            } else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_flatDetails_ec, true);
                expandSubView(ll_viewFlatInfo);
                viewFlatDetails = true;
            }
        });

        //allotment_form_details_main
        ll_allotmentDetailsMain.setOnClickListener(v -> {

            if (viewAllotmentDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_allotmentDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewAllotmentFormDetails);
                viewAllotmentDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_allotmentDetails_ec, true);
                expandSubView(ll_viewAllotmentFormDetails);
                viewAllotmentDetails = true;
            }
        });


        //payment_details_main
        ll_paymentDetailsMain.setOnClickListener(v -> {

            if (viewPaymentDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_paymentDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewPaymentDetails);
                viewPaymentDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_paymentDetails_ec, true);
                expandSubView(ll_viewPaymentDetails);
                viewPaymentDetails = true;
            }
        });


        //pay_slip_attachments_main
        ll_paySleepAttachmentMain.setOnClickListener(v -> {

            if (viewPaymentSlipDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_paySleepAttachment_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewPaySlipDetails);
                viewPaymentSlipDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_paySleepAttachment_ec, true);
                expandSubView(ll_viewPaySlipDetails);
                viewPaymentSlipDetails = true;
            }
        });

        //payment_mode_main
        ll_paymentModeMain.setOnClickListener(v -> {

            if (viewPaymentModeDetails)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_paymentMode_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewPaymentMode);
                viewPaymentModeDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_paymentMode_ec, true);
                expandSubView(ll_viewPaymentMode);
                viewPaymentModeDetails = true;
            }
        });


        //booking_documents_main
        ll_bookingDocumentsMain.setOnClickListener(v -> {

            if (viewBookingDocuments)  //expanded
            {
                // //do collapse View
                new Animations().toggleRotate(iv_bookingDocuments_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewBookingDocuments);
                viewBookingDocuments = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                new Animations().toggleRotate(iv_bookingDocuments_ec, true);
                expandSubView(ll_viewBookingDocuments);
                viewBookingDocuments = true;
            }
        });


        edt_applicant_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edt_applicant_mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_applicant_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_flatAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_bookingAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_bank_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_account_holder_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_cheque_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_cheque_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_utr_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_transaction_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_invoice_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edt_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }


    private void setPaymentMode() {

        paymentType.setOnCheckedChangeListener((RadioGroup group,int checkedId)->
        {
            int selectedId=paymentType.getCheckedRadioButtonId();
            final AppCompatRadioButton rbtn = paymentType.findViewById(selectedId);
            final String selectedName =  rbtn.getText().toString();
            if (selectedName.equalsIgnoreCase("Card")) {
                //show card details
                paymentMode=3;
                ll_cardPayment.setVisibility(View.VISIBLE);
                ll_onlinePayment.setVisibility(View.GONE);
                ll_chequePayment.setVisibility(View.GONE);
                edt_transaction_no.requestFocus();
            }
            else if(selectedName.equalsIgnoreCase("Online")){
                //online
                paymentMode=2;
                ll_cardPayment.setVisibility(View.GONE);
                ll_onlinePayment.setVisibility(View.VISIBLE);
                ll_chequePayment.setVisibility(View.GONE);
                edt_utr_no.requestFocus();
            }
            else {
                //cheque
                paymentMode=1;
                ll_cardPayment.setVisibility(View.GONE);
                ll_onlinePayment.setVisibility(View.GONE);
                ll_chequePayment.setVisibility(View.VISIBLE);
                edt_bank_name.requestFocus();
            }

            //check button enabled
            checkButtonEnabled();
        });

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
            builder_accept.dismiss();
            askPermissionForCamera();
        });

        //gallery
        Objects.requireNonNull(linearLayout_option2).setOnClickListener(view -> {
            builder_accept.dismiss();
            askPermissionForGallery();
        });

        //documents
        Objects.requireNonNull(linearLayout_option3).setOnClickListener(view -> {
            //doNormalShare(videoDetail);
            builder_accept.dismiss();
            askPermissionForDocuments();
        });

        builder_accept.setOnDismissListener(dialog -> {
            // TODO Auto-generated method stub

        });
        builder_accept.show();

    }


    void askPermissionForCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkCameraPermission()) openCamera();
            else requestPermission_for_Camera();
        }
        else openCamera();
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


    private boolean checkWriteStoragePermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkReadPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }



    //camera needs WRITE_STORAGE && CAMERA permission
    private boolean checkCameraPermission() {
        return  (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }


    //open camera
    private void openCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
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


    //request camera permission
    private void requestPermission_for_Camera()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(this, getString(R.string.camera_permission_rationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), new String[]
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open camera once permission is granted
                openCamera();
            }
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
        }

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {

        super.onActivityResult(requestCode, responseCode, data);
        //Log.e(TAG, "onActivityResult: myPos " + myPosition);

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
                        Uri tempUri = getImageUri(getApplicationContext(), photo);

                        // CALL THIS METHOD TO GET THE ACTUAL PATH
                        File finalFile = new File(getRealPathFromURI(tempUri));

                        Log.e("finalFile", finalFile.getAbsolutePath());
                        Log.e("finalFile", "ab path");
                        Log.e("finalFile", finalFile.getPath());
                        Log.e("finalFile", finalFile.toString());

                        Log.e(TAG, "onActivityResult: " + getFileName_from_filePath(finalFile.getAbsolutePath()));
                        //tv_UploadedDoc_Name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                        if (isFormOrPaySlip) {

                            //add path in arrayList
                            allotmentFormImagesArrayList.add(finalFile.getAbsolutePath());
                            //set imagesView
                            getAllotmentFormImages();
                            //increment count
                            documentCountAppln++;
                        }
                        else
                        {
                            //add in payslip
                            paySlipAttachmentsStringArrayList.add(finalFile.getAbsolutePath());
                            //set imagesView
                            getPaySlipImages();
                            //increment count
                            documentCountPaySlip++;
                        }

                        //set document path to the text field
                        if (booking_attachment_id==1) {
                            //set Allotment document
                            tv_allotmentDocument_name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                            //set document
                            setSelectedDoc(0, 5, finalFile.getAbsolutePath());
                            //set flag to true
                            isDocumentSelected = true;
                        }
                        else if (booking_attachment_id ==2) {
                            //set kyc document
                            tv_kycDocument_name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                            //set document
                            setSelectedDoc(1, 4, finalFile.getAbsolutePath());

                            //set flag to true
                            isDocumentSelected = true;


                        }
                        else if (booking_attachment_id==3) {
                            //set pay slip document
                            tv_playSlipDocument_name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                            //set document
                            setSelectedDoc(2, 6, finalFile.getAbsolutePath());

                            //set flag to true
                            isDocumentSelected = true;


                        }


                    } else Log.e("myDocument & pos ", "data null");

                    //call the method that upload the profile pic
                    //call_uploadImage(photoUrl);

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

                        //call the method that upload the profile pic
                        //call_uploadImage(photoUrl);

                        //set document path to the text field
                        if (booking_attachment_id==1) {
                            //set Allotment document
                            tv_allotmentDocument_name.setText(getFileName_from_filePath(photoUrl));

                            //set document
                            setSelectedDoc(0, 5, photoUrl);

                            //set flag to true
                            isDocumentSelected = true;

                        }
                        else if (booking_attachment_id ==2) {
                            //set kyc document
                            tv_kycDocument_name.setText(getFileName_from_filePath(photoUrl));

                            //set document
                            setSelectedDoc(1, 4, photoUrl);

                            //set flag to true
                            isDocumentSelected = true;

                        }
                        else if (booking_attachment_id==3) {
                            //set pay slip document
                            tv_playSlipDocument_name.setText(getFileName_from_filePath(photoUrl));
                            //set document
                            setSelectedDoc(2, 6, photoUrl);
                            //set flag to true
                            isDocumentSelected = true;
                        }

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


                //set document path to the text field
                if (booking_attachment_id==1) {
                    //set Allotment document
                    tv_allotmentDocument_name.setText(getFileName_from_filePath(photoUrl));

                    //set document
                    setSelectedDoc(0, 5, photoUrl);

                    //set flag to true
                    isDocumentSelected = true;

                }
                else if (booking_attachment_id ==2) {
                    //set kyc document
                    tv_kycDocument_name.setText(getFileName_from_filePath(photoUrl));

                    //set document
                    setSelectedDoc(1, 4, photoUrl);

                    //set flag to true
                    isDocumentSelected = true;

                }
                else if (booking_attachment_id==3) {
                    //set pay slip document
                    tv_playSlipDocument_name.setText(getFileName_from_filePath(photoUrl));

                    //set document
                    setSelectedDoc(2, 6, photoUrl);

                    //set flag to true
                    isDocumentSelected = true;
                }

            }
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


    private void setSelectedDoc(int position, int doc_type_id, String absolutePath)
    {

        EventProjectDocsModel model =new EventProjectDocsModel();
        model.setDoc_type_id(doc_type_id);
        model.setIsUploaded(0);
        model.setDocPath(absolutePath!=null ? absolutePath : "");

        if (containsId(eventProjectDocsModelArrayList, doc_type_id)) {
            //check if already value exists then override the existed value (path)
            Log.e(TAG, "setSelectedDoc: value exists");
            int index =  getIndexOfList(eventProjectDocsModelArrayList, doc_type_id);
            Log.e(TAG, "setSelectedDoc: index "+index);

            if (index>=0) eventProjectDocsModelArrayList.set(index, model);
            else eventProjectDocsModelArrayList.add(model);
        }
        else {
            //already not exists -- add new value
            Log.e(TAG, "setSelectedDoc: value does not exists");
            eventProjectDocsModelArrayList.add(model);
        }

        // documentCount++;
        documentCount = eventProjectDocsModelArrayList.size(); //set size as a count
        Log.e(TAG, "setSelectedDoc: count "+documentCount );
    }

   /* public boolean containsName(final List<MyObject> list, final String name){
        return list.stream().anyMatch(o -> o.getName().equals(name));
    } */

    public boolean containsId(final List<EventProjectDocsModel> list, final int doc_type_id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().anyMatch(o -> o.getDoc_type_id()== doc_type_id);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getDoc_type_id() == doc_type_id)
                    return true;
            return false;
           // return list.stream().filter(o -> o.getDoc_type_id()== doc_type_id).findFirst().isPresent();
        }
    }

    private int getIndexOfList(List<EventProjectDocsModel> list, int doc_type_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  IntStream.range(0, list.size())
                    .filter(i -> list.get(i).getDoc_type_id() == doc_type_id)
                    .findFirst().orElse(-1);
        }
        else {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getDoc_type_id() == doc_type_id)
                    return i;
            return -1;
        }
    }

    private void getAllotmentFormImages()
    {
        if(allotmentFormImagesArrayList.size()>0) {
            ll_formImagesList.removeAllViews();
            for (int i =0 ; i<allotmentFormImagesArrayList.size(); i++) {
                View rowView_sub = getAllotmentFormViews(i,allotmentFormImagesArrayList.get(i));
                ll_formImagesList.addView(rowView_sub);
            }
            //gone def layout
            iv_allotmentDetails_defImg.setVisibility(View.GONE);
            //visible images layout
            hsv_allotmentForm.setVisibility(View.VISIBLE);
        }
        else {
            //gone visibility
            hsv_allotmentForm.setVisibility(View.GONE);
            //show def image
            iv_allotmentDetails_defImg.setVisibility(View.VISIBLE);
        }
    }

    private void getPaySlipImages()
    {
        if(paySlipAttachmentsStringArrayList.size()>0)
        {
            fl_paySlipImagesList.removeAllViews();
            for (int i =0 ; i<paySlipAttachmentsStringArrayList.size(); i++) {
                View rowView_sub = getPaySlipImagesView(i, paySlipAttachmentsStringArrayList.get(i));
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.size());
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.toString());
                fl_paySlipImagesList.addView(rowView_sub);
            }
            //gone def layout
            iv_paySlipDetails_defImg.setVisibility(View.GONE);
            //visible images layout
            hsv_paySlip.setVisibility(View.VISIBLE);
        }
        else
        {
            //gone visibility
            hsv_paySlip.setVisibility(View.GONE);
            //show def image
            iv_paySlipDetails_defImg.setVisibility(View.VISIBLE);
        }
    }


    private View getAllotmentFormViews(int position, String imagePath)
    {

        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_attachment_form_image, null );
        final RoundedImageView imageView = rowView_sub.findViewById(R.id.iv_flatAttachment_form);
        final AppCompatImageView iv_close = rowView_sub.findViewById(R.id.iv_flatAttachment_close);
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
                .putExtra("event_title", "Allotment form attachments")
        ));

        iv_close.setOnClickListener(v -> {

            //remove from position allotment form
            allotmentFormImagesArrayList.remove(position);
            //decrement count
            documentCountAppln--;
            //reset array
            getAllotmentFormImages();

        });

        return rowView_sub;
    }



    private View getPaySlipImagesView(int position, String imagePath)
    {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_attachment_form_image, null );
        final RoundedImageView imageView = rowView_sub.findViewById(R.id.iv_flatAttachment_form);
        final AppCompatImageView iv_close = rowView_sub.findViewById(R.id.iv_flatAttachment_close);
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
                .putExtra("event_title", "Allotment form attachments")
        ));

        iv_close.setOnClickListener(v -> {

            //remove from position payslip form
            paySlipAttachmentsStringArrayList.remove(position);
            //decrement count
            documentCountPaySlip--;
            //reset array
            getPaySlipImages();

        });

        return rowView_sub;
    }


    private void checkValidations()
    {
        //customer details
        if (cuidModel==null) new Helper().showCustomToast(context, "Please enter customer details !");
            //applicant name
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter applicant name!");
            //applicant mobile
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_mobile.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter applicant mobile number!");
            //applicant email
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_email.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter applicant email!");
            // valid email
        else if (!isValidEmail(edt_applicant_email)) new Helper().showCustomToast(context, "Please enter a valid email!");
            //flat amount
        else if (Objects.requireNonNull(edt_flatAmount.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter unit price");
            // flat booking amount
        else if (Objects.requireNonNull(edt_bookingAmount.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter flat booking amount");
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_bank_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter bank name");
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_account_holder_name.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Account Holder Name");
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_cheque_no.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Cheque Number");
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_cheque_date.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Cheque Date");
            // payment mode online
        else if (online.isChecked() && Objects.requireNonNull(edt_utr_no.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter UTR Number");
            //payment mode card
        else if (card.isChecked() && Objects.requireNonNull(edt_transaction_no.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Transaction Number");
            //payment mode card
        else if (card.isChecked() && Objects.requireNonNull(edt_invoice_no.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Invoice Number");
        else
        {
            //show confirmation dialog
            showFlatAllotmentAlertDialog();
        }
    }

    private void checkButtonEnabled()
    {
        //customer details
        if (cuidModel==null) setButtonDisabledView();
            //applicant name
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_name.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //applicant mobile
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_mobile.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //applicant email
        else if (!mCbx_applicantDetails.isChecked()&& Objects.requireNonNull(edt_applicant_email.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // valid email
        else if (!isValidEmail(edt_applicant_email)) setButtonDisabledView();
            //flat amount
        else if (Objects.requireNonNull(edt_flatAmount.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // flat booking amount
        else if (Objects.requireNonNull(edt_bookingAmount.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_bank_name.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_account_holder_name.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_cheque_no.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode cheque
        else if (cheque.isChecked() && Objects.requireNonNull(edt_cheque_date.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // payment mode online
        else if (online.isChecked() && Objects.requireNonNull(edt_utr_no.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode card
        else if (card.isChecked() && Objects.requireNonNull(edt_transaction_no.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //payment mode card
        else if (card.isChecked() && Objects.requireNonNull(edt_invoice_no.getText()).toString().trim().isEmpty()) setButtonDisabledView();

        else
        {
            //set Button enabled View
            setButtonEnabledView();
        }
    }


    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_confirmAllotment.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_confirmAllotment.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit lead

        mBtn_confirmAllotment.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_confirmAllotment.setTextColor(getResources().getColor(R.color.main_white));
    }


    private void showFlatAllotmentAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(getString(R.string.submit_flat_allotment));
        tv_desc.setText(getString(R.string.submit_flat_allotment_confirmation, unit_name , customer_name));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.submitting_flat_allotment_details));
                call_unitAllotment();
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


    private void call_unitAllotment()
    {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id", lead_id);
        jsonObject.addProperty("flat_total", edt_flatAmount.getText().toString());
        jsonObject.addProperty("booking_amt", edt_bookingAmount.getText().toString());
        jsonObject.addProperty("payment_mode_id", paymentMode);
        jsonObject.addProperty("user_type_id", user_id);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("first_name", cuidModel.getFirst_name());
        jsonObject.addProperty("middle_name", cuidModel.getMiddle_name());
        jsonObject.addProperty("last_name", cuidModel.getLast_name());
        jsonObject.addProperty("prefix", cuidModel.getPrefix());
        jsonObject.addProperty("country_code", country_code);
        jsonObject.addProperty("mobile_number", mobile_number);

        jsonObject.addProperty("applicant_name", Objects.requireNonNull(edt_applicant_name.getText()).toString());
        jsonObject.addProperty("applicant_mobile", Objects.requireNonNull(edt_applicant_mobile.getText()).toString());
        jsonObject.addProperty("applicant_email", Objects.requireNonNull(edt_applicant_email.getText()).toString());

        jsonObject.addProperty("unit_hold_release_id", unit_hold_release_id);
        jsonObject.addProperty("project_id", project_id);
        jsonObject.addProperty("block_id", block_id);
        jsonObject.addProperty("floor_id", floor_id);
        jsonObject.addProperty("unit_id", unit_id);
        jsonObject.addProperty("remarks", Objects.requireNonNull(edt_remark.getText()).toString());

        if(paymentMode == 1)//Cheque
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();
            JSONObject json3= new JSONObject();
            JSONObject json4= new JSONObject();
            try {
                json1.put("payment_mode_details_title","Check No");
                json1.put("payment_mode_details_description",edt_cheque_no.getText());
                json2.put("payment_mode_details_title","Check Date");
                json2.put("payment_mode_details_description",setChequeDate);
                json3.put("payment_mode_details_title","Check Issuer");
                json3.put("payment_mode_details_description",edt_account_holder_name.getText());
                json4.put("payment_mode_details_title","Bank Name");
                json4.put("payment_mode_details_description",edt_bank_name.getText());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonArray.put(json2);
            jsonArray.put(json3);
            jsonArray.put(json4);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        } else if(paymentMode == 2)//Online
        {

            JSONObject json1= new JSONObject();

            try {
                json1.put("payment_mode_details_title","UTR No");
                json1.put("payment_mode_details_description",edt_utr_no.getText());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));

        } else if(paymentMode == 3)//Card
        {
            JSONObject json1= new JSONObject();
            JSONObject json2= new JSONObject();

            try {
                json1.put("payment_mode_details_title","Transaction No");
                json1.put("payment_mode_details_description",edt_transaction_no.getText());
                json2.put("payment_mode_details_title","Invoice No");
                json2.put("payment_mode_details_description",edt_invoice_no.getText());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(json1);
            jsonArray.put(json2);
            jsonObject.add("payment_mode_details",new Gson().fromJson(jsonArray.toString(), JsonArray.class));
        }


        ApiClient client = ApiClient.getInstance();
        client.getApiService().addUnitAllotment(jsonObject).enqueue(new Callback<JsonObject>()
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

                                    JsonObject data = response.body().get("data").getAsJsonObject();
                                    parseReturnData(data);
                                    //JsonObject data  = response.body().get("data").getAsJsonObject();
                                    //setJson(data, 1);
                                    isLeadSubmitted = true;
                                    onAllotmentSuccess();
                                    // showReleaseSuccessAlert();
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void parseReturnData(JsonObject jsonObject)
    {
        if (jsonObject.has("booking_id")) booking_id =!jsonObject.get("booking_id").isJsonNull()&&!jsonObject.get("booking_id").isJsonNull() ? jsonObject.get("booking_id").getAsInt() : 0;
    }

    public void onAllotmentSuccess() {

        runOnUiThread(() -> {
            hideProgressBar();

            if(isLeadSubmitted) {
                //check for documents uploaded
                //addPostDoc();

                if (isDocumentSelected) {
                    //check for document selected
                    addPostDoc();
                }
                else {
                    //show success alert
                    showAllotSuccessAlert();
                }
            }
            else showErrorLog("Failed to flat allotment..! Try again!");


            //set Feed Action Added to true
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }
        });
    }


    public void addPostDoc()
    {
       /* //check if form images are uploaded
        if (allotmentFormImagesArrayList!=null && allotmentFormImagesArrayList.size()>0)
        {
            //call upload method

            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                for (int i =0; i<allotmentFormImagesArrayList.size(); i++)
                {
                    //ll_pb.setVisibility(View.VISIBLE);
                    showProgressBar(getString(R.string.posting_doc)+" "+allotmentFormImagesArrayList.get(i)+"...");
                    addBookingAttachments(allotmentFormImagesArrayList.get(i),1);
                }

            }
            else NetworkError(context);
        }
        // check if pay slip uploaded
        else if (paySlipAttachmentsStringArrayList!=null && paySlipAttachmentsStringArrayList.size()>0)
        {
            //call upload method

            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                for (int i =0; i<paySlipAttachmentsStringArrayList.size(); i++)
                {
                    //ll_pb.setVisibility(View.VISIBLE);
                    showProgressBar(getString(R.string.posting_doc)+" "+paySlipAttachmentsStringArrayList.get(i)+"...");

                    addBookingAttachments(paySlipAttachmentsStringArrayList.get(i),2);

                }

            }
            else NetworkError(context);
        }
        else*/ if (eventProjectDocsModelArrayList!=null && eventProjectDocsModelArrayList.size()>0)
        {
            Log.e(TAG, "addPostDoc: booking documents upload ");
            //call upload method
            if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
            {
                for (int i =0; i<eventProjectDocsModelArrayList.size(); i++) {
                    if (eventProjectDocsModelArrayList.get(i).getDocPath()!=null && !eventProjectDocsModelArrayList.get(i).getDocPath().isEmpty() ) {
                        //ll_pb.setVisibility(View.VISIBLE);
                        //showProgressBar(getString(R.string.posting_doc)+" "+eventProjectDocsModelArrayList.get(i).getDocType()+"...");
                        showProgressBar(getString(R.string.posting_doc)+" Documents...");
                        addBookingAttachments(eventProjectDocsModelArrayList.get(i).getDocPath(), eventProjectDocsModelArrayList.get(i).getDoc_type_id());
                    }
                }

            }
            else Helper.NetworkError(context);
        }

        else showAllotSuccessAlert(); //show success alert
    }





    public void showConfirmReleaseAlert()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);

        tv_msg.setText(getString(R.string.cancel_flat_allotment_que));
        tv_desc.setText(getString(R.string.cancel_allotment_and_release_confirmation, unit_name, customer_name));
        btn_negativeButton.setText(getString(R.string.release_confirm));
        btn_positiveButton.setText(getString(R.string.keep_on_hold));

        btn_positiveButton.setOnClickListener(view -> {

            alertDialog.dismiss();
            if (fromHoldList) {
                //do on backPressed
                super.onBackPressed();
            }
            else {
                startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
                finish();
            }


        });
        btn_negativeButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            showProgressBar("Releasing hold flat...");
            call_markAsReleased();
        });

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

    private void call_markAsReleased()
    {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("unit_hold_release_id", unit_hold_release_id);

        ApiClient client = ApiClient.getInstance();
        client.getApiService().directReleaseFlat(jsonObject).enqueue(new Callback<JsonObject>()
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

                                    showReleaseSuccessAlert();
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

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }



    private void addBookingAttachments(String filePath,int attachment_type_id)
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            File docFile = new File(filePath);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), docFile);
            MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file_uri", docFile.getName(), uploadFile);
            RequestBody bookingID = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(booking_id));
            RequestBody attachment_typeID = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(attachment_type_id));
            RequestBody api_token_ = RequestBody.create(MediaType.parse("text/plain"),api_token);
            ApiClient client = ApiClient.getInstance();
            client.getApiService().addBookingAttachments(fileUpload,bookingID,attachment_typeID,api_token_).enqueue(new Callback<JsonObject>()
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

                            if (isSuccess==1) {

                                /*if(attachment_type_id==1) {
                                    //form image attachment
                                    formAttachAPICount = formAttachAPICount +1;
                                    onDocumentUpload();
                                }
                                else if (attachment_type_id ==2){
                                    //pay slip attachments
                                    paySlipAttachAPICount = paySlipAttachAPICount +1;
                                    onDocumentUploadPaySlip();
                                }
                                else*/ {

                                    Log.e(TAG, "onResponse:  attachment_type_id "+attachment_type_id);

                                    // increase the count by 1
                                    docAPICount=docAPICount+1;
                                    onDocumentUploadBookingAttachments();
                                }
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
                                showErrorLog(getString(R.string.unknown_error_try_again));
                                break;
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                    else showErrorLog(e.toString());
                }
            });



        }
        else Helper.NetworkError(context);
    }



    private boolean isValidEmail(EditText email)
    {
        boolean ret = true;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        //if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }


    private void showErrorLog(final String message) {

        context.runOnUiThread(() ->
        {
            //hide pb
            hideProgressBar();
            //show error log
            Helper.onErrorSnack(context, message);
        });

    }


    private void onDocumentUpload()
    {
        runOnUiThread(() -> {

                    if(documentCountAppln == formAttachAPICount)
                    {
                        Log.e(TAG, "documentCountApp: "+documentCountAppln );
                        Log.e(TAG, "formAttachAPICount: "+ formAttachAPICount);

                        //hide pb
                        hideProgressBar();
                        //clear arrayList
                        allotmentFormImagesArrayList.clear();

                        //check if pay slip attachment if any
                        if (paySlipAttachmentsStringArrayList!=null && paySlipAttachmentsStringArrayList.size()>0) {

                            //call upload method
                            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                                for (int i =0; i<paySlipAttachmentsStringArrayList.size(); i++)
                                {
                                    //ll_pb.setVisibility(View.VISIBLE);
                                    showProgressBar(getString(R.string.posting_doc)+" "+paySlipAttachmentsStringArrayList.get(i)+"...");
                                    addBookingAttachments(paySlipAttachmentsStringArrayList.get(i),2);
                                }
                            }
                            else Helper.NetworkError(context);
                        }
                        else showAllotSuccessAlert(); //show success alert
                    }
                    else showAllotSuccessAlert(); //show success alert
                }
        );
    }
    private void onDocumentUploadPaySlip()
    {
        runOnUiThread(() -> {

                    if( documentCountPaySlip== paySlipAttachAPICount)
                    {
                        Log.e(TAG, "documentCountPaySlip: "+  documentCountPaySlip);
                        Log.e(TAG, "paySlipAttachAPICount: "+ paySlipAttachAPICount);

                        //hide pb
                        hideProgressBar();
                        //clear arrayList
                        paySlipAttachmentsStringArrayList.clear();
                        //show success alert
                        showAllotSuccessAlert();
                    }
                    else showAllotSuccessAlert(); //show success alert
                }
        );
    }

    private void onDocumentUploadBookingAttachments()
    {
        runOnUiThread(() -> {

                    //condition for number documents selected were all uploaded or not
                    //here documentCount is for selected number of documents
                    // docAPICount is for number of times api called
                    if (documentCount == docAPICount) {

                        Log.e(TAG, "onDocumentUploadBookingAttachments: docAPICount "+docAPICount );
                        //hide pb
                        hideProgressBar();
                        //clear arrayList
                        eventProjectDocsModelArrayList.clear();
                        //show success alert
                        showAllotSuccessAlert();

                    }
                    else {
                        //
                        Log.e(TAG, "onDocumentUploadBookingAttachments: documentCount "+documentCount );
                        //showAllotSuccessAlert(); //show success alert
                    }

                });
    }

    @SuppressLint("InflateParams")
    private void showReleaseSuccessAlert()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show toast
            new Helper().showSuccessCustomToast(context, getString(R.string.flat_released_successfully));

            //start activity
            Intent intent=new Intent(context,SalesPersonBottomNavigationActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

        });

    }

    private void showAllotSuccessAlert()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //open disabled View
            openView();

            //set gif
            gif.setImageResource(R.drawable.gif_success);
            //set animation
            new Animations().scaleEffect(ll_success);
            //visible view
            ll_success.setVisibility(View.VISIBLE);
            //show success toast
            new Helper().showSuccessCustomToast(context, getString(R.string.flat_allotted_successfully));
            //do backPress
            new Handler().postDelayed(() -> {
                //share_dialog.dismiss();
                ll_success.setVisibility(View.GONE);
                //close view
                closeView();

                //start activity
                Intent intent=new Intent(context,SalesPersonBottomNavigationActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }, 4000);

        });

    }


    private void openView()
    {
        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.VISIBLE);
    }

    private void closeView()
    {

        // Hide the Panel
        Animation bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.GONE);
    }

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
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation)
            {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

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

        if (fromAddHoldFlat) {
            //if directly came from add hold flat
            //startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
            //finish();
            showConfirmReleaseAlert();
        }
        else {
            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }

}



/*

  private void getAllotmentFormImages()
    {
        if(allotmentFormImagesArrayList.size()>0)
        {
            //DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
            int dp;
            if (allotmentFormImagesArrayList.size() == 1) {
                int count = allotmentFormImagesArrayList.size();
                dp =pixel/count;
            }
            else dp = pixel/2;

            int new_dp = dp-10;
            ll_formImagesList.removeAllViews();
            for (int i =0 ; i<allotmentFormImagesArrayList.size(); i++) {

                View rowView_sub = getAllotmentFormViews(i,allotmentFormImagesArrayList.get(i));
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.size());
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.toString());

                ll_formImagesList.addView(rowView_sub);
            }

            //gone def layout
            iv_allotmentDetails_defImg.setVisibility(View.GONE);
            //visible images layout
            hsv_allotmentForm.setVisibility(View.VISIBLE);
        }
        else
        {
            //gone visibility
            hsv_allotmentForm.setVisibility(View.GONE);
            //show def image
            iv_allotmentDetails_defImg.setVisibility(View.VISIBLE);
        }
    }



    private View getAllotmentFormView(int position, int new_dp, String imagePath, int dp)
    {

        AppCompatImageView imageView;
        if (imagePath !=null)
        {
            //set imagePath to imageView
            imageView = new AppCompatImageView(context);
            Glide.with(context)
                    .load(imagePath)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(imageView);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(new_dp, new_dp));
            //imageView.setPadding(5,10 , 5, 0);

            RelativeLayout rLayout = new RelativeLayout(context);
            RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //tParams.setMargins(5,10,0,0);
            tParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            //tParams.addRule(dp,dp);
            //imageView.setLayoutParams(rlParams);

            AppCompatImageView imgClose = new AppCompatImageView(context);
            imgClose.setImageResource(R.drawable.ic_close_image_icon);
            imgClose.setLayoutParams(tParams);
            //text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //text.setGravity(Gravity.TOP | Gravity.END);
            //text.setLayoutParams(new GridView.LayoutParams(160, 160));
            if (position<=2) imgClose.setPadding(20, 30, 20, 20);
            else imgClose.setPadding(2, 2, 2, 2);
            //imgClose.setBackgroundColor(Color.parseColor("#48000000"));  //black transparent color

            rLayout.addView(imageView);
            rLayout.addView(imgClose);
            ViewGroup.LayoutParams rlParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rLayout.setLayoutParams(rlParams);
            //rLayout.setLayoutParams(new RelativeLayout.LayoutParams(dp, dp));
            rLayout.setPadding(10, 10, 10, 0);

            imgClose.setOnClickListener(v -> {

                //remove from position allotment form
                allotmentFormImagesArrayList.remove(position);
                //decrement count
                documentCountAppln--;
                //reset array
                getAllotmentFormImages();
            });

            return rLayout;
        }
        else
        {
            imageView = new AppCompatImageView(context);
            Glide.with(context)
                    .load(R.drawable.ic_camera_icon_black_01)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(imageView);

            imageView.setFitsSystemWindows(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //int dp =pixel/imagecount;
            imageView.setLayoutParams(new GridView.LayoutParams(120, 150));
            imageView.setPadding(5, 0, 5, 5);


            RelativeLayout rLayout = new RelativeLayout(context);
            ViewGroup.LayoutParams rlParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            rLayout.setLayoutParams(rlParams);

            RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            tParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            tParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            tParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rLayout.setBackgroundColor(Color.parseColor("#F9F9F9"));  //toolbar color
            imageView.setLayoutParams(tParams);

            rLayout.addView(imageView);

            return rLayout;
        }

        //return imageView;
    }


     private void getPaySlipImages()
    {
        if(paySlipAttachmentsStringArrayList.size()>0)
        {
            //DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int pixel= context.getWindowManager().getDefaultDisplay().getWidth();
            int dp = pixel / 2;
//            if (paySlipAttachmentsStringArrayList.size() == 1) {
//                int count = paySlipAttachmentsStringArrayList.size();
//                dp =pixel/count;
//            }
//            else dp = pixel/2;

            int new_dp = dp-10;
            fl_paySlipImagesList.removeAllViews();
            for (int i =0 ; i<paySlipAttachmentsStringArrayList.size(); i++) {
                View rowView_sub = getPaySlipImagesView(i, paySlipAttachmentsStringArrayList.get(i));
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.size());
                //Log.e("TAG", "getImage: image" +allotmentFormImagesArrayList.toString());
                fl_paySlipImagesList.addView(rowView_sub);
            }

            //gone def layout
            iv_paySlipDetails_defImg.setVisibility(View.GONE);
            //visible images layout
            hsv_paySlip.setVisibility(View.VISIBLE);
        }
        else
        {
            //gone visibility
            hsv_paySlip.setVisibility(View.GONE);
            //show def image
            iv_paySlipDetails_defImg.setVisibility(View.VISIBLE);
        }
    }


    private View getPaySlipImagesView(int position, int new_dp, String imagePath, int dp)
    {

        AppCompatImageView imageView;
        if (imagePath !=null)
        {
            //set imagePath to imageView
            imageView = new AppCompatImageView(context);
            Glide.with(context)
                    .load(imagePath)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(imageView);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(new_dp, new_dp));
            //imageView.setPadding(5,10 , 5, 0);

            RelativeLayout rLayout = new RelativeLayout(context);
            RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //tParams.setMargins(5,10,0,0);
            tParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            //tParams.addRule(dp,dp);
            //imageView.setLayoutParams(rlParams);

            AppCompatImageView imgClose = new AppCompatImageView(context);
            imgClose.setImageResource(R.drawable.ic_close_image_icon);
            imgClose.setLayoutParams(tParams);
            //text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //text.setGravity(Gravity.TOP | Gravity.END);
            //text.setLayoutParams(new GridView.LayoutParams(160, 160));
            if (position<=2) imgClose.setPadding(20, 30, 20, 20);
            else imgClose.setPadding(2, 2, 2, 2);
            //imgClose.setBackgroundColor(Color.parseColor("#48000000"));  //black transparent color

            rLayout.addView(imageView);
            rLayout.addView(imgClose);
            ViewGroup.LayoutParams rlParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLayout.setLayoutParams(rlParams);
            //rLayout.setLayoutParams(new RelativeLayout.LayoutParams(dp, dp));
            rLayout.setPadding(10, 10, 10, 0);

            imgClose.setOnClickListener(v -> {

                //remove from position payslip form
                paySlipAttachmentsStringArrayList.remove(position);
                //decrement count
                documentCountPaySlip--;
                //reset array
                getPaySlipImages();
            });

            return rLayout;
        }
        else
        {
            imageView = new AppCompatImageView(context);
            Glide.with(context)
                    .load(R.drawable.ic_camera_icon_black_01)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(imageView);

            imageView.setFitsSystemWindows(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //int dp =pixel/imagecount;
            imageView.setLayoutParams(new GridView.LayoutParams(120, 150));
            imageView.setPadding(5, 0, 5, 5);


            RelativeLayout rLayout = new RelativeLayout(context);
            ViewGroup.LayoutParams rlParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            rLayout.setLayoutParams(rlParams);

            RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            tParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            tParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            tParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rLayout.setBackgroundColor(Color.parseColor("#F9F9F9"));  //toolbar color
            imageView.setLayoutParams(tParams);

            rLayout.addView(imageView);

            return rLayout;
        }

        //return imageView;
    }


    */
