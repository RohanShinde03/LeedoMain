package com.tribeappsoft.leedo.admin.offlineLeads;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;

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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.model.BudgetLimitModel;
import com.tribeappsoft.leedo.admin.leads.model.LeadGenerationModel;
import com.tribeappsoft.leedo.admin.leads.model.LeadGenerationSecondModel;
import com.tribeappsoft.leedo.admin.offlineLeads.model.OfflineLeadModel;
import com.tribeappsoft.leedo.models.leads.IncomeRangesModel;
import com.tribeappsoft.leedo.models.leads.LeadProfession;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.models.leads.PersonNamePrefixModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.models.project.UnitCategoriesModel;
import com.tribeappsoft.leedo.salesPerson.adapter.CustomerAdapter;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.FlowLayout;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;
import com.tribeappsoft.leedo.util.ccp.Country;
import com.tribeappsoft.leedo.util.ccp.CountryCodeSelectActivity;
import com.tribeappsoft.leedo.util.ccp.CountryUtils;
import com.tribeappsoft.leedo.util.customDatePicker.MyLocalDatePicker;
import com.tribeappsoft.leedo.util.filepicker.MaterialFilePicker;
import com.tribeappsoft.leedo.util.filepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.getLongNextDateFromString;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;
import static com.tribeappsoft.leedo.util.Helper.setDatePickerFormatDateFromString;

public class AddNewOfflineLeadActivity extends AppCompatActivity
{

    @BindView(R.id.cl_newOfflineLead) CoordinatorLayout parent;
    @BindView(R.id.ll_newOfflineLead_main) LinearLayoutCompat ll_main;
    @BindView(R.id.tv_newOfflineLead_salesRepName) AppCompatTextView tv_salesRepName;

    @BindView(R.id.ll_newOfflineLead_refLeadMain) LinearLayoutCompat ll_refLeadMain;
    @BindView(R.id.iv_newOfflineLead_refLead_ec) AppCompatImageView iv_refLead_ec;
    @BindView(R.id.ll_newOfflineLead_addRefData) LinearLayoutCompat ll_viewRefData;

    @BindView(R.id.ll_newOfflineLead_refMobCcp) LinearLayoutCompat ll_refMobCcp;
    @BindView(R.id.iv_newOfflineLead_refCountryArrow) AppCompatImageView iv_refCountryArrow;
    @BindView(R.id.tv_newOfflineLead_refCountry) AppCompatTextView tv_refCountry;

    @BindView(R.id.sm_newOfflineLead_leadRefBy) SwitchMaterial sm_leadRefBy;
    @BindView(R.id.edt_newOfflineLead_refName) TextInputEditText edt_refName;
    @BindView(R.id.edt_newOfflineLead_refMobileNo) TextInputEditText edt_refMobileNo;
    @BindView(R.id.til_newOfflineLead_refMobile) TextInputLayout til_refMobile;
    @BindView(R.id.acTv_newOfflineLead_selectRefProject) AutoCompleteTextView acTv_selectRefProject;
    @BindView(R.id.acTv_newOfflineLead_selectResourceTypes) AutoCompleteTextView acTv_selectResourceTypes;
    //@BindView(R.id.edt_newOfflineLead_refProjectName) TextInputEditText edt_refProjectName;
    @BindView(R.id.edt_newOfflineLead_refFlatNumber) TextInputEditText edt_refFlatNumber;

    @BindView(R.id.ll_newOfflineLead_leadDetailsMain) LinearLayoutCompat ll_leadDetailsMain;
    @BindView(R.id.iv_newOfflineLead_leadDetails_ec) AppCompatImageView iv_leadDetails_ec;
    @BindView(R.id.ll_newOfflineLead_viewLeadDetails) LinearLayoutCompat ll_viewLeadDetails;

    @BindView(R.id.acTv_newOfflineLead_mrs) AutoCompleteTextView acTv_mrs;
    //@BindView(R.id.spn_newOfflineLead_selectMrs) AppCompatSpinner spn_selectMrs;
    @BindView(R.id.til_newOfflineLead_leadFirstName) TextInputLayout til_leadFirstName;
    @BindView(R.id.edt_newOfflineLead_leadFirstName) TextInputEditText edt_leadFirstName;
    @BindView(R.id.edt_newOfflineLead_leadMiddleName) TextInputEditText edt_leadMiddleName;
    @BindView(R.id.edt_newOfflineLead_leadLastName) TextInputEditText edt_leadLastName;

    @BindView(R.id.ll_newOfflineLead_leadMobCcp) LinearLayoutCompat ll_leadMobCcp;
    @BindView(R.id.ll_newOfflineLead_leadOtherMobCcp) LinearLayoutCompat ll_leadOtherMobCcp;
    @BindView(R.id.flag_imv) AppCompatImageView flag_imv;
    @BindView(R.id.selected_country_tv) AppCompatTextView selected_country_tv;
    @BindView(R.id.selected_country_tv2) AppCompatTextView selected_country_tv2;

    @BindView(R.id.til_newOfflineLead_leadMobile) TextInputLayout til_leadMobile;
    @BindView(R.id.edt_newOfflineLead_leadMobileNo) TextInputEditText edt_leadMobileNo;
    @BindView(R.id.til_newOfflineLead_leadOtherMobile) TextInputLayout til_leadOtherMobile;
    @BindView(R.id.edt_newOfflineLead_leadOtherMobileNo) TextInputEditText edt_leadOtherMobileNo;
    @BindView(R.id.til_newOfflineLead_leadEmail) TextInputLayout til_leadEmail;
    @BindView(R.id.edt_newOfflineLead_leadEmail) TextInputEditText edt_leadEmail;
    @BindView(R.id.mdp_newOfflineLead_leadBirthDate) MyLocalDatePicker mdp_leadBirthDate;
    @BindView(R.id.edt_newOfflineLead_leadAddress) TextInputEditText edt_leadAddress;
    @BindView(R.id.edt_newOfflineLead_leadProfession) TextInputEditText edt_leadProfession;
    @BindView(R.id.acTv_newOfflineLead_selectAnnualIncome) AutoCompleteTextView acTv_selectAnnualIncome;
    @BindView(R.id.acTv_newOfflineLead_selectBudgetLimit) AutoCompleteTextView acTv_selectBudgetLimit;
    @BindView(R.id.acTv_newOfflineLead_leadProfession) AutoCompleteTextView acTv_leadProfession;
    @BindView(R.id.acTv_newOfflineLead_leadStage) AutoCompleteTextView acTv_leadStage;
    @BindView(R.id.edt_newOfflineLead_siteVisit_niReason) TextInputEditText edt_newOfflineLead_niReason;
    @BindView(R.id.til_newOfflineLead_siteVisit_niReason) TextInputLayout til_newOfflineLead_niReason;

    //verify Lead Mobile
    @BindView(R.id.tv_newOfflineLead_mobAlExists) AppCompatTextView tv_mobAlExists;
    @BindView(R.id.tv_newOfflineLead_mobVerifySuccess) AppCompatTextView tv_mobVerifySuccess;
    @BindView(R.id.til_newOfflineLead_leadOtp) TextInputLayout til_leadOtp;
    @BindView(R.id.edt_newOfflineLead_OTPNumber) TextInputEditText edt_OTPNumber;
    @BindView(R.id.mBtn_newOfflineLead_verifyLeadMob) MaterialButton mBtn_verifyLeadMob;
    @BindView(R.id.tv_newOfflineLead_resendOtp_msg) AppCompatTextView tv_resendOtp_msg;
    @BindView(R.id.tv_newOfflineLead_resendOtp_counter) AppCompatTextView tv_resendOtp_counter;
    @BindView(R.id.mBtn_newOfflineLead_resendOTP) MaterialButton mBtn_resendOTP;

    @BindView(R.id.ll_newOfflineLead_kycDocsMain) LinearLayoutCompat ll_kycDocsMain;
    @BindView(R.id.iv_newOfflineLead_kycDocs_ec) AppCompatImageView iv_kycDocs_ec;
    @BindView(R.id.ll_newOfflineLead_viewKycDoc) LinearLayoutCompat ll_viewKycDoc;
    @BindView(R.id.ll_newOfflineLead_addKYCDoc) LinearLayoutCompat ll_addKYCDoc;

    @BindView(R.id.ll_newOfflineLead_leadCampaignMain) LinearLayoutCompat ll_leadCampaignMain;
    @BindView(R.id.iv_newOfflineLead_leadCampaign_ec) AppCompatImageView iv_leadCampaign_ec;
    @BindView(R.id.ll_newOfflineLead_viewLeadCampaign) LinearLayoutCompat ll_viewLeadCampaign;
    @BindView(R.id.ll_newOfflineLead_LeadCampaign) LinearLayoutCompat ll_addLeadCampaign;

    @BindView(R.id.ll_newOfflineLead_projectDetailsMain) LinearLayoutCompat ll_projectDetailsMain;
    @BindView(R.id.iv_newOfflineLead_viewProjectDetails_ec) AppCompatImageView iv_viewProjectDetails_ec;
    @BindView(R.id.ll_newOfflineLead_viewProjectDetails) LinearLayoutCompat ll_viewProjectDetails;
    @BindView(R.id.acTv_newOfflineLead_selectProjectName) AutoCompleteTextView acTv_selectProjectName;
    @BindView(R.id.acTv_newOfflineLead_selectUnitType) AutoCompleteTextView acTv_selectUnitType;
    @BindView(R.id.edt_newOfflineLead_prefSiteVisitFromDate) TextInputEditText edt_prefSiteVisitFromDate;
    @BindView(R.id.edt_newOfflineLead_prefSiteVisitToDate) TextInputEditText edt_prefSiteVisitToDate;

    @BindView(R.id.ll_newOfflineLead_otherDetailsMain) LinearLayoutCompat ll_otherDetailsMain;
    @BindView(R.id.iv_newOfflineLead_viewOtherDetails_ec) AppCompatImageView iv_viewOtherDetails_ec;
    @BindView(R.id.ll_newOfflineLead_viewOtherDetails) LinearLayoutCompat ll_viewOtherDetails;
    @BindView(R.id.acTv_newOfflineLead_selectWhenToBuy) AutoCompleteTextView acTv_selectWhenToBuy;

    @BindView(R.id.rdoGrp_newOfflineLead_propertyBuyFor) RadioGroup rdoGrp_propertyBuyFor;
    @BindView(R.id.rb_newOfflineLead_selfUse) AppCompatRadioButton rb_selfUse;
    @BindView(R.id.rb_newOfflineLead_investment) AppCompatRadioButton rb_investment;

    //@BindView(R.id.rb_newOfflineLead_other) AppCompatRadioButton rb_other;
    @BindView(R.id.edt_newOfflineLead_otherPropertyBuyPurpose) TextInputEditText edt_otherPropertyBuyPurpose;
    @BindView(R.id.tv_alreadyVisitedTitle) AppCompatTextView tv_alreadyVisitedTitle;
    @BindView(R.id.rdoGrp_newOfflineLead_alreadySiteVisited) RadioGroup rdoGrp_alreadySiteVisited;
    @BindView(R.id.rb_newOfflineLead_alreadySiteVisited_yes) AppCompatRadioButton rb_alreadySiteVisited_yes;
    @BindView(R.id.rb_newOfflineLead_alreadySiteVisited_no) AppCompatRadioButton rb_alreadySiteVisited_no;

    @BindView(R.id.edt_newOfflineLead_dob)TextInputEditText edt_newOfflineLead_dob;
    @BindView(R.id.acTv_newOfflineLead_selectLeadSource) AutoCompleteTextView acTv_selectLeadSource;
    // @BindView(R.id.ll_newOfflineLead_viewSubSource) LinearLayoutCompat ll_viewSubSource;
    @BindView(R.id.ll_newOfflineLead_alreadySiteVisit) LinearLayoutCompat ll_alreadySiteVisit;
    @BindView(R.id.edt_newOfflineLead_alreadySiteVisitDate) TextInputEditText edt_alreadySiteVisitDate;
    @BindView(R.id.edt_newOfflineLead_alreadySiteVisitTime) TextInputEditText edt_alreadySiteVisitTime;
    @BindView(R.id.edt_newOfflineLead_alreadySiteVisitRemark) TextInputEditText edt_alreadySiteVisitRemark;


    @BindView(R.id.edt_newOfflineLead_leadRemarks) TextInputEditText edt_leadRemarks;
    @BindView(R.id.mBtn_newOfflineLead_submitLead) MaterialButton mBtn_submitLead;
    @BindView(R.id.view_newOfflineLead_disableLayout) View viewDisableLayout;
    @BindView(R.id.ll_newOfflineLead_success) LinearLayoutCompat ll_success;
    @BindView(R.id.gif_newOfflineLead) GifImageView gif_newOfflineLead;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    @BindView(R.id.ll_newOfflineLead_LeadSources_Dropdown) LinearLayoutCompat ll_LeadSources_Dropdown;
    @BindView(R.id.ll_newOfflineLead_LeadSources_Click) LinearLayoutCompat ll_LeadSources_Click;
    @BindView(R.id.tv_newOfflineLead_leadGen_thrw) MaterialTextView tv_leadGen_thrw;
    @BindView(R.id.iv_newOfflineLead_LeadSources_Dropdown) AppCompatImageView iv_LeadSources_Dropdown;
    @BindView(R.id.edt_newOfflineLead_fullName_referer) TextInputEditText edt_fullName_referer;
    @BindView(R.id.edt_newOfflineLead_refererMobile_no) TextInputEditText edt_refererMobile_no;
    @BindView(R.id.ll_newOfflineLead_Reference_main) LinearLayoutCompat ll_Reference_main;
    @BindView(R.id.til_newOfflineLead_refererMobile) TextInputLayout til_refererMobile;

    @BindView(R.id.ll_newOfflineLead_existUSer) LinearLayoutCompat ll_newOfflineLead_existUSer;
    @BindView(R.id.mTv_newOfflineLead_ExistProject) MaterialTextView mTv_ExistProject;
    @BindView(R.id.mTv_newOfflineLead_ExistUser) MaterialTextView mTv_ExistUser;
    @BindView(R.id.mTv_newOfflineLead_ExistLeadStatus) MaterialTextView mTv_ExistLeadStatus;

    @BindView(R.id.ll_newOfflineLead_existUser_OtherNo) LinearLayoutCompat ll_newOfflineLead_existUSer_OtherNo;
    @BindView(R.id.mTv_newOfflineLead_ExistProject_OtherNo) MaterialTextView mTv_ExistProject_OtherNo;
    @BindView(R.id.mTv_newOfflineLead_ExistUser_OtherNo) MaterialTextView mTv_ExistUser_OtherNo;
    @BindView(R.id.mTv_newOfflineLead_ExistStatus_OtherNo) MaterialTextView mTv_ExistStatus_OtherNo;

    @BindView(R.id.rb_newOfflineLead_FirstHome_no) AppCompatRadioButton rb_newOfflineLead_FirstHome_no;
    @BindView(R.id.rb_newOfflineLead_firstHome_yes) AppCompatRadioButton rb_newOfflineLead_firstHome_yes;
    @BindView(R.id.rdoGrp_newOfflineLead_firstHome) RadioGroup houseRadioGroup;

    private AppCompatActivity context;
    CustomerAdapter adapter = null;
    private Animations anim;
    private boolean viewLeadDetails =false, viewKycDocs =false, viewLeadCampaign =false, viewProjectDetails =false,
            viewOtherDetails =false, isAlreadySiteVisited = false, isRefLead = false, isLeadSubmitted = false,idDocSelected=false, fromShortcut= false, isExpand =false;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_SEND_Date = "yyyy-MM-dd";
    private DatePickerDialog datePickerDialog;
    //private ArrayList<EventProjectDocsModel> docsModelArrayList;
    //private EventProjectDocsModel myUploadModel = null;

    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;

    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<UnitCategoriesModel> unitCategoriesModelArrayList;
    private ArrayList<IncomeRangesModel> rangesModelArrayList;
    private ArrayList<BudgetLimitModel> budgetLimitModelArrayList;

    private ArrayList<PersonNamePrefixModel> personNamePrefixModelArrayList;
    private ArrayList<LeadProfession> leadProfessionModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<LeadGenerationModel> leadGenerationModelArrayList;
    private ArrayList<String> integerArrayList_str;
    private ArrayList<Integer> integerArrayList;
    private ArrayList<String> projectNamesArrayList,unitCategoriesArrayList, incomeRangesArrayList,budgetLimitArrayList, namePrefixArrayList, professionArrayList,leadGenerationStringArrayList, leadStageStringArrayList;

    private String TAG = "AddNewOfflineLeadActivity",  sendBDate = null, sendPrefVisitFromDate= null,sendPrefVisitToDate= null,
            sendAlreadySiteVisitDate= null, sendAlreadySiteVisitTime = null, api_token ="", countryPhoneCode = "+91",countryPhoneCode_1 = "+91",
            countryPhoneCode_ref = "+91", selectedProjectName = "", selectedIncomeRange ="",selectedBudgetLimit ="",edt_LeadValue="",
            selectedUnitCategory ="",selectedLeadSource ="",sendNewsDate="",
            selectedNamePrefix ="", selectedLeadProfessionName = "", selectedLeadStageName = "",sendDateOfBirth="";

    private int mYear, mMonth, mDay,nYear, nMonth, nDay,myPosition =0, selectedProjectId =0, selectedIncomeRangeId =0,selectedBudgetLimitId =0,selectedLeadSourceId=0,
            selectedUnitId =0, selectedNamePrefixId =0, user_id =0, selectedProfessionId=0, LeadType_ID=0, selectedLeadStageId=0;

    private int fromOther = 1; //TODO fromOther ==> 1 - Add New Lead, 2- Edit/Update Lead Info
    private int FirstHomeID=0,current_lead_status_id=0;
    private boolean flagNumduplicate=false,isExist_WhatsAppNo=false,isExist_OtherNo=false,isUpdate = false;
    //private int check=1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean flagExit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_offline_lead);
        ButterKnife.bind(this);
        context = AddNewOfflineLeadActivity.this;
        //call method to hide keyBoard
        setupUI(parent);
        //anim
        anim = new Animations();

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromOther==2 ? getString(R.string.add_offline_lead): getString(R.string.add_offline_lead));
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setTextColor(context.getResources().getColor(R.color.primary_gray));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //hidden pb
        hideProgressBar();

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        //tv_salesRepName.setText(sharedPreferences.getString("full_name", ""));
        String firstName = sharedPreferences.getString("first_name", null);
        String lastName = sharedPreferences.getString("last_name", null);
        String user_name = lastName!=null ? firstName + " "+ lastName : firstName;
        tv_salesRepName.setText(user_name);

        editor.apply();

        //get Intent
        if (getIntent()!=null) {
            fromShortcut = getIntent().getBooleanExtra("fromShortcut", false);
            isUpdate = getIntent().getBooleanExtra("isUpdateLead", false);
            //   tv_salesRepName.setText(getIntent().getStringExtra("salesPersonName"));
            current_lead_status_id = getIntent().getIntExtra("current_lead_status_id", 1);
            Log.e(TAG,"current_lead_status_id: "+current_lead_status_id);
        }

        if(isUpdate){
            if(getSupportActionBar()!= null){
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.update_lead));
            }

            mBtn_submitLead.setText(R.string.update_lead);
            rdoGrp_alreadySiteVisited.setVisibility(View.GONE);
            tv_alreadyVisitedTitle.setVisibility(View.GONE);
            selected_country_tv.setEnabled(false);
            selected_country_tv2.setEnabled(false);
            edt_leadMobileNo.setEnabled(false);
            edt_leadOtherMobileNo.setEnabled(false);
            ll_leadMobCcp.setVisibility(View.GONE);
            ll_leadOtherMobCcp.setVisibility(View.GONE);

            til_leadMobile.setHelperText("Mobile Number is not editable!");
            til_leadMobile.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            til_leadOtherMobile.setHelperText("Alternate Mobile Number is not editable!");
            til_leadOtherMobile.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));


        }

        //api token
        Log.e(TAG, "onCreate: api_token "+ sharedPreferences.getString("api_token", ""));
        Log.e(TAG, "onCreate: fromShortcut "+ fromShortcut);

        //init
        init();

        // Get Current Date
        final Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR,1993);
        c.set(Calendar.MONTH,Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH,3);

        mYear = c.get((Calendar.YEAR));
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        final Calendar c1 = Calendar.getInstance();
        nYear = c1.get((Calendar.YEAR));
        nMonth = c1.get(Calendar.MONTH);
        nDay = c1.get(Calendar.DAY_OF_MONTH);


        // Define min & max date for sample
        Date minDate = MyLocalDatePicker.stringToDate("01-01-1900", DATE_FORMAT);
        //LocalDate maxDate = MyLocalDatePicker.stringToLocalDate("12-31-2018", DATE_FORMAT);
        Date maxDate = MyLocalDatePicker.stringToDate(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), DATE_FORMAT);

        // Init LazyLocalDatePicker
        mdp_leadBirthDate.setMinDate(minDate);
        mdp_leadBirthDate.setMaxDate(maxDate);

        mdp_leadBirthDate.setOnLocalDatePickListener(dateSelected -> {
            sendBDate = MyLocalDatePicker.dateToString(dateSelected, DATE_FORMAT_SEND_Date);
            Log.e(TAG, "onLocalDatePick: "+ sendBDate );

            //check button enabled
            checkButtonEnabled();
            //Toast.makeText(MainLocalDateActivity.this, "Selected date: " + MyLocalDatePicker.dateToString(dateSelected, DATE_FORMAT), Toast.LENGTH_SHORT).show();
        });

        mdp_leadBirthDate.setOnLocalDateSelectedListener(dateSelected -> Log.e("",  "onLocalDateSelected: " + dateSelected));

        //select country code
        ll_leadMobCcp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 121));
        ll_leadOtherMobCcp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 123));
        ll_refMobCcp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 122));

        //set radio Buttons
        //set_radioButtons();
        rb_alreadySiteVisited_yes.setClickable(false);
        rb_alreadySiteVisited_yes.setFocusable(false);
        rb_alreadySiteVisited_no.setClickable(false);
        rb_alreadySiteVisited_no.setFocusable(false);

        //checkEmail
        checkLeadEmail();
        checkHouseType();


        //set required data expanded
        new Handler(getMainLooper()).postDelayed(() -> {

            //def set expandView to lead details & project details & kyc documents
            anim.toggleRotate(iv_leadDetails_ec, true);
            //expandSubView(ll_viewLeadDetails);
            viewLeadDetails = true;

            anim.toggleRotate(iv_viewProjectDetails_ec, true);
            viewProjectDetails = true;

            anim.toggleRotate(iv_kycDocs_ec, true);
            viewKycDocs = true;

        }, 100);

        edt_newOfflineLead_dob.setOnClickListener(v -> {
            //hide keyboard
            hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select date
            selectDateOfBirth();
        });

        //set visit date time
        edt_prefSiteVisitFromDate.setOnClickListener(view -> selectVisitPrefFromDate());
        edt_prefSiteVisitToDate.setOnClickListener(view -> {
            if (sendPrefVisitFromDate!=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(context, "Please select site visit from date first!");
        });

        edt_alreadySiteVisitDate.setOnClickListener(view -> selectVisitDate());
        edt_alreadySiteVisitTime.setOnClickListener(view -> selectVisitTime());

        edt_newOfflineLead_niReason.setVisibility(View.GONE);
        til_newOfflineLead_niReason.setVisibility(View.GONE);

        mBtn_submitLead.setOnClickListener(view -> {

            //hide keyboard if opened
            hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            checkValidations();
        });

        //click on View
        viewDisableLayout.setOnClickListener(v -> {
            //hide success layout
            ll_success.setVisibility(View.GONE);
            //close View
            closeView();
            // do onBackPress
            onBackPressed();
        });

    }


    private void init()
    {
        //docsModelArrayList = new ArrayList<>();
        projectModelArrayList = new ArrayList<>();
        rangesModelArrayList = new ArrayList<>();
        unitCategoriesModelArrayList = new ArrayList<>();
        budgetLimitModelArrayList = new ArrayList<>();
        personNamePrefixModelArrayList = new ArrayList<>();
        namePrefixArrayList = new ArrayList<>();
        projectNamesArrayList = new ArrayList<>();
        unitCategoriesArrayList = new ArrayList<>();
        incomeRangesArrayList = new ArrayList<>();
        budgetLimitArrayList = new ArrayList<>();
        leadProfessionModelArrayList = new ArrayList<>();
        leadStagesModelArrayList = new ArrayList<>();
        professionArrayList = new ArrayList<>();
        leadGenerationStringArrayList = new ArrayList<>();
        leadStageStringArrayList = new ArrayList<>();
        leadGenerationModelArrayList = new ArrayList<>();
        integerArrayList_str = new ArrayList<>();
        integerArrayList = new ArrayList<>();

        //set required fields
        //til_leadFirstName.setHint(Html.fromHtml(getString(R.string.first_name)+"<font color='#b42726'><b>*</b></font>") );
        // edt_leadFirstName.setHint(Html.fromHtml(getString(R.string.first_name)+"<font color='#b42726'><b>*</b></font>") );

        //set first word caps
        //edt_leadFirstName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //edt_leadMiddleName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //edt_leadLastName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        //edt_refName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //edt_refProjectName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edt_leadProfession.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edt_otherPropertyBuyPurpose.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        //def visibility gone for Property Buy Purpose
        edt_otherPropertyBuyPurpose.setVisibility(View.GONE);

        //def hide all views
        mBtn_verifyLeadMob.setVisibility(View.GONE);
        mBtn_resendOTP.setVisibility(View.GONE);
        til_leadOtp.setVisibility(View.GONE);
        edt_OTPNumber.setVisibility(View.GONE);
        tv_mobVerifySuccess.setVisibility(View.GONE);
        tv_resendOtp_msg.setVisibility(View.GONE);
        tv_resendOtp_counter.setVisibility(View.GONE);

        checkButtonEditTextChanged();

        showProgressBar("Please wait...");
        getLeadFormData();

    }

    private void getLeadFormData() {

        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();

            //set Prefix
            String JsonObjArray_namePrefix = null;
            if (sharedPreferences.getString("namePrefix", null) != null) JsonObjArray_namePrefix = sharedPreferences.getString("namePrefix", null);
            if (JsonObjArray_namePrefix!=null) setPrefixData(JsonObjArray_namePrefix);

            //set lead stages
            String JsonObjArray_lead_stages = null;
            if (sharedPreferences.getString("lead_stages", null) != null) JsonObjArray_lead_stages = sharedPreferences.getString("lead_stages", null);
            if (JsonObjArray_lead_stages!=null) setLeadStagesData(JsonObjArray_lead_stages);

            //set income range
            String JsonObjArray_income_range_types = null;
            if (sharedPreferences.getString("income_range_types", null) != null) JsonObjArray_income_range_types = sharedPreferences.getString("income_range_types", null);
            if (JsonObjArray_income_range_types!=null) setIncomeRangeData(JsonObjArray_income_range_types);

            //set budget type
            String JsonObjArray_budget_limit_types = null;
            if (sharedPreferences.getString("budget_limit_types", null) != null) JsonObjArray_budget_limit_types = sharedPreferences.getString("budget_limit_types", null);
            if (JsonObjArray_budget_limit_types!=null) setBudgetRangeData(JsonObjArray_budget_limit_types);

            //set professions type
            String JsonObjArray_professions = null;
            if (sharedPreferences.getString("professions", null) != null) JsonObjArray_professions = sharedPreferences.getString("professions", null);
            if (JsonObjArray_professions!=null) setProfessionData(JsonObjArray_professions);

            //set project type
            String JsonObjArray_ref_projects = null;
            if (sharedPreferences.getString("ref_projects", null) != null) JsonObjArray_ref_projects = sharedPreferences.getString("ref_projects", null);
            if (JsonObjArray_ref_projects!=null) setProjectData(JsonObjArray_ref_projects);

            //set unit categories
            String JsonObjArray_unit_categories = null;
            if (sharedPreferences.getString("unit_categories", null) != null) JsonObjArray_unit_categories = sharedPreferences.getString("unit_categories", null);
            if (JsonObjArray_unit_categories!=null) setUnitCategoriesData(JsonObjArray_unit_categories);

            //set lead source types
            String JsonObjArray_lead_types = null;
            if (sharedPreferences.getString("lead_types", null) != null) JsonObjArray_lead_types = sharedPreferences.getString("lead_types", null);
            if (JsonObjArray_lead_types!=null) setLeadSourceTypesData(JsonObjArray_lead_types);

            hideProgressBar();
        }
    }

    private void setPrefixData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        personNamePrefixModelArrayList=new ArrayList<>();
        personNamePrefixModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setNamePrefixJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterNamePrefix);
    }

    private void setLeadStagesData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        leadStagesModelArrayList=new ArrayList<>();
        leadStagesModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++) {
            setLeadStagesJsonArray(jsonArray,i);
        }

        //set lead stages adapter
        context.runOnUiThread(this::setAdapterLeadStages);
    }

    private void setIncomeRangeData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        rangesModelArrayList=new ArrayList<>();
        rangesModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setIncomeRangeJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterIncomeRanges);
    }

    private void setBudgetRangeData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        budgetLimitModelArrayList=new ArrayList<>();
        budgetLimitModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setBudgetLimitJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterBudgetLimits);
    }

    private void setProfessionData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        leadProfessionModelArrayList=new ArrayList<>();
        leadProfessionModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setProfessionJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterLeadProfession);
    }

    private void setProjectData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        projectModelArrayList=new ArrayList<>();
        projectModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setProjectJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterProjectNames);
    }
    private void setUnitCategoriesData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        unitCategoriesModelArrayList=new ArrayList<>();
        unitCategoriesModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setUnitCategoryJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterUnitCategories);

    }

    private void setLeadSourceTypesData(String data) {

        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        leadGenerationModelArrayList=new ArrayList<>();
        leadGenerationModelArrayList.clear();

        for(int i=0;i<jsonArray.size();i++)
        {
            setLeadSourceTypesJsonArray(jsonArray,i);
        }

        //set prefix adapter
        context.runOnUiThread(this::setAdapterResourceTypes);
    }



    private void setNamePrefixJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        PersonNamePrefixModel myModel = new PersonNamePrefixModel();
        if (jsonObject.has("name_prefix_id")) myModel.setName_prefix_id(!jsonObject.get("name_prefix_id").isJsonNull() ? jsonObject.get("name_prefix_id").getAsInt() : 0 );
        if (jsonObject.has("name_prefix"))
        {
            myModel.setName_prefix(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
            namePrefixArrayList.add(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
        }
        personNamePrefixModelArrayList.add(myModel);
    }

    private void setLeadStagesJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        LeadStagesModel myModel = new LeadStagesModel();
        if (jsonObject.has("lead_stage_id")) myModel.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage_name")) {
            myModel.setLead_stage_name(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
            leadStageStringArrayList.add(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
        }

        leadStagesModelArrayList.add(myModel);
    }

    private void setIncomeRangeJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        IncomeRangesModel myModel = new IncomeRangesModel();
        if (jsonObject.has("income_range_id")) myModel.setIncome_range_id(!jsonObject.get("income_range_id").isJsonNull() ? jsonObject.get("income_range_id").getAsInt() : 0 );
        if (jsonObject.has("type_name"))
        {
            myModel.setIncome_range(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
            incomeRangesArrayList.add(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
        }
        rangesModelArrayList.add(myModel);
    }

    private void setBudgetLimitJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        BudgetLimitModel myModel = new BudgetLimitModel();
        if (jsonObject.has("budget_limit_id")) myModel.setBudget_limit_id(!jsonObject.get("budget_limit_id").isJsonNull() ? jsonObject.get("budget_limit_id").getAsInt() : 0 );
        if (jsonObject.has("type_name"))
        {
            myModel.setBudget_limit(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
            budgetLimitArrayList.add(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
        }

        budgetLimitModelArrayList.add(myModel);
    }

    private void setProfessionJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        LeadProfession myModel = new LeadProfession();
        if (jsonObject.has("professions_id")) myModel.setLead_profession_id(!jsonObject.get("professions_id").isJsonNull() ? jsonObject.get("professions_id").getAsInt() : 0 );
        if (jsonObject.has("profession"))
        {
            myModel.setLead_profession(!jsonObject.get("profession").isJsonNull() ? jsonObject.get("profession").getAsString() : "" );
            professionArrayList.add(!jsonObject.get("profession").isJsonNull() ? jsonObject.get("profession").getAsString() : "" );
        }

        leadProfessionModelArrayList.add(myModel);
    }

    private void setProjectJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectNamesArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }
        projectModelArrayList.add(model);

    }

    private void setUnitCategoryJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        UnitCategoriesModel myModel = new UnitCategoriesModel();
        if (jsonObject.has("unit_category_id")) myModel.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category"))
        {
            myModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
            unitCategoriesArrayList.add(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        }

        unitCategoriesModelArrayList.add(myModel);

    }

    private void setLeadSourceTypesJsonArray(JsonArray jsonArray, int i) {

        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();

        LeadGenerationModel myModel = new LeadGenerationModel();

        if (jsonObject.has("lead_types_id")) myModel.setLead_type_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0 );
        if (jsonObject.has("secLvl")) myModel.setSecLvl(!jsonObject.get("secLvl").isJsonNull() ? jsonObject.get("secLvl").getAsInt() : 0 );
        if (jsonObject.has("edit_text_req")) myModel.setEdit_text_req(!jsonObject.get("edit_text_req").isJsonNull() ? jsonObject.get("edit_text_req").getAsInt() : 0 );
        if (jsonObject.has("edit_text_title")) myModel.setEdit_text_title(!jsonObject.get("edit_text_title").isJsonNull() ? jsonObject.get("edit_text_title").getAsString() : "" );
        if (jsonObject.has("type_name")) {
            myModel.setType_name(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
            leadGenerationStringArrayList.add(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
        }

        if (jsonObject.has("lead_subtypes"))
        {
            if (!jsonObject.get("lead_subtypes").isJsonNull() && jsonObject.get("lead_subtypes").isJsonArray())
            {
                JsonArray jsonArraylvl2 = jsonObject.get("lead_subtypes").getAsJsonArray();
                if (jsonArraylvl2.size()>0)
                {
                    ArrayList<LeadGenerationSecondModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArraylvl2.size(); j++)
                    {
                        setSubCatJson(jsonArraylvl2.get(j).getAsJsonObject(), arrayList);
                    }
                    myModel.setGenerationModelArrayList(arrayList);
                }
            }
        }

        leadGenerationModelArrayList.add(myModel);

    }

    private void setSubCatJson(JsonObject jsonObject, ArrayList<LeadGenerationSecondModel> arrayList)
    {
        LeadGenerationSecondModel myModel =new LeadGenerationSecondModel();

        if (jsonObject.has("lead_subtypes_id")) myModel.setId(!jsonObject.get("lead_subtypes_id").isJsonNull() ? jsonObject.get("lead_subtypes_id").getAsInt() : 0 );
        if (jsonObject.has("lead_types_id")) myModel.setLead_type_id(!jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0 );
        if (jsonObject.has("status_id")) myModel.setStatus_id(!jsonObject.get("status_id").isJsonNull() ? jsonObject.get("status_id").getAsInt() : 0 );
        if (jsonObject.has("name")) myModel.setName(!jsonObject.get("name").isJsonNull() ? jsonObject.get("name").getAsString().trim() : "name" );

        arrayList.add(myModel);
    }



    private void checkButtonEditTextChanged() {

        edt_refererMobile_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  if (edt_mobileNo.getText().toString().length()>4) {
                if (!isValidPhone(edt_refererMobile_no)) {
                    til_refererMobile.setErrorEnabled(true);
                    til_refererMobile.setError("Please enter valid mobile number!");
                    //til_email.setHelperTextEnabled(true);
                    //til_email.setHelperText("Valid email eg. abc@gmail.com");

                }
                else {
                    til_refererMobile.setErrorEnabled(false);
                    til_refererMobile.setError(null);
                    //til_email.setHelperTextEnabled(false);
                    //til_email.setHelperText(null);
                }

                //checkButtonEnabled
                checkButtonEnabled();
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //setOnClickEvent();

        //set Toggle Views
        toggleViews();

        //set adapters
        //setAdapterForMrs();
    }


    private void toggleViews()
    {

        //lead referenced by
        sm_leadRefBy.setOnCheckedChangeListener((compoundButton, b) -> {

            isRefLead = b;
            if (b)  //checked
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_refLead_ec, true);
                expandSubView(ll_viewRefData);
                //viewRefLead = true;
            }
            else {

                // //do collapse View
                anim.toggleRotate(iv_refLead_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewRefData);
                //viewRefLead = false;
            }

            checkButtonEnabled();
        });

        //lead referenced by
		/*ll_refLeadMain.setOnClickListener(v -> {

		    if (viewRefLead)  //expanded
		    {
		        // //do collapse View
		        anim.toggleRotate(iv_refLead_ec, false);
		        //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
		        collapse(ll_viewRefData);
		        viewRefLead = false;
		    }
		    else    // collapsed
		    {
		        //do expand view
		        // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
		        anim.toggleRotate(iv_refLead_ec, true);
		        expandSubView(ll_viewRefData);
		        viewRefLead = true;
		    }
		});*/


        //lead_details_main
        ll_leadDetailsMain.setOnClickListener(v -> {

            if (viewLeadDetails)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_leadDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewLeadDetails);
                viewLeadDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_leadDetails_ec, true);
                expandSubView(ll_viewLeadDetails);
                viewLeadDetails = true;
            }
        });


        ll_kycDocsMain.setOnClickListener(v -> {

            if (viewKycDocs)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_kycDocs_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewKycDoc);
                viewKycDocs = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_kycDocs_ec, true);
                expandSubView(ll_viewKycDoc);
                viewKycDocs = true;
            }
        });



        ll_leadCampaignMain.setOnClickListener(v -> {

            if (viewLeadCampaign)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_leadCampaign_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewLeadCampaign);
                viewLeadCampaign = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_leadCampaign_ec, true);
                expandSubView(ll_viewLeadCampaign);
                viewLeadCampaign = true;
            }
        });


        ll_projectDetailsMain.setOnClickListener(v -> {

            if (viewProjectDetails)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_viewProjectDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewProjectDetails);
                viewProjectDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_viewProjectDetails_ec, true);
                expandSubView(ll_viewProjectDetails);
                viewProjectDetails = true;
            }
        });


        //view other details
        ll_otherDetailsMain.setOnClickListener(v -> {

            if (viewOtherDetails)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_viewOtherDetails_ec, false);
                //iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.down_arrow);
                collapse(ll_viewOtherDetails);
                viewOtherDetails = false;
            }
            else    // collapsed
            {
                //do expand view
                // iv_selectFlatSchemeType_expandCollapse.setImageResource(R.drawable.up_arrow);
                anim.toggleRotate(iv_viewOtherDetails_ec, true);
                expandSubView(ll_viewOtherDetails);
                viewOtherDetails = true;
            }
        });



        edt_leadFirstName.addTextChangedListener(new TextWatcher() {
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
        edt_leadLastName.addTextChangedListener(new TextWatcher() {
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
        edt_refName.addTextChangedListener(new TextWatcher() {
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
        edt_refMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //if (countryPhoneCode.equalsIgnoreCase(countryPhoneCode_ref) && Objects.requireNonNull(edt_refMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))
                //{
                //  new Helper().showCustomToast(context, "Reference mobile number and Lead mobile number should be different!");
                //}

                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
		/*edt_refProjectName.addTextChangedListener(new TextWatcher() {
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
		});*/
        edt_refFlatNumber.addTextChangedListener(new TextWatcher() {
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

    private void setAdapterResourceTypes()
    {

        if (leadGenerationModelArrayList!=null && leadGenerationModelArrayList.size()>0)
        {
            //adding unit categories
            leadGenerationStringArrayList.clear();
            for (int i =0; i<leadGenerationModelArrayList.size(); i++)
            {
                leadGenerationStringArrayList.add(leadGenerationModelArrayList.get(i).getType_name());
            }

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, leadGenerationStringArrayList);
            acTv_selectResourceTypes.setAdapter(adapter);
            acTv_selectResourceTypes.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectResourceTypes.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (LeadGenerationModel pojo : leadGenerationModelArrayList)
                {
                    if (pojo.getType_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadSourceId = pojo.getLead_type_id(); // This is the correct ID
                        selectedLeadSource = pojo.getType_name();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Resource Type category & id " + selectedLeadSource +"\t"+ selectedLeadSourceId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }

            });

        }
        else
        {
            //empty array
            new Helper().showCustomToast(context, "Lead Sources are empty!");
        }

    }

    private void setAdapterLeadResources() {

        if (leadGenerationModelArrayList!=null && leadGenerationModelArrayList.size()>0 )
        {
            //            tv_addEnquiry_leadGen_thrw.setText(generationModelArrayList.get(9).getType_name());
            //            LeadType_ID = generationModelArrayList.get(9).getLead_type_id();

            ll_LeadSources_Dropdown.removeAllViews();
            for (int i = 0; i< leadGenerationModelArrayList.size(); i++) {
                View view = getSelectCatView(i);
                ll_LeadSources_Dropdown.addView(view);
                //Log.e(TAG, "Tv_title: "+Tv_title);
            }
        }
    }

    View getSelectCatView(final int position)
    {

        @SuppressLint("InflateParams") View subView = LayoutInflater.from(context).inflate(R.layout.item_layout_lead_source, null );
        AppCompatTextView textView = subView.findViewById(R.id.Dropdown_textView);

        textView.setText(leadGenerationModelArrayList.get(position).getType_name());

        final LinearLayoutCompat.LayoutParams layoutparams = (LinearLayoutCompat.LayoutParams) textView.getLayoutParams();
        layoutparams.setMargins(0,5,0,5);
        textView.setLayoutParams(layoutparams);
        //textView.setText(String.valueOf(position+1));

        final int SecLvlID= leadGenerationModelArrayList.get(position).getSecLvl();
        //SecLeadType_ID=SecLvlID;
        final int edit_text_req= leadGenerationModelArrayList.get(position).getEdit_text_req();
        //edit_text_req_ID=edit_text_req;
        final String type_name= leadGenerationModelArrayList.get(position).getType_name();


        //lead click listener
        subView.setOnClickListener(v ->
        {
            integerArrayList.clear();

            LeadType_ID = leadGenerationModelArrayList.get(position).getLead_type_id();
            //LeadType_ID= typeId;

            Log.e(TAG, "LeadType_ID: "+LeadType_ID);

            if(SecLvlID==1 && edit_text_req==2)
            {
                //select only chips ==> flow layout only
                iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_down_arrow_drop_down_24);
                ll_Reference_main.setVisibility(View.GONE);
                integerArrayList_str.clear();

                final ArrayList<LeadGenerationSecondModel> arrayList =  leadGenerationModelArrayList.get(position).getGenerationModelArrayList();
                if (arrayList!=null && arrayList.size()>0) { for (int i = 0; i < arrayList.size(); i++) { arrayList.get(i).setIsSelected(0); } }

                showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(),edit_text_req);
                isExpand =false;
            }
            else if (edit_text_req==1 && SecLvlID==2)
            {
                //select only edt ==> edt only
                ll_Reference_main.setVisibility(View.GONE);
                integerArrayList_str.clear();
                showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(),edit_text_req);
                isExpand =false;
            }
            else if (edit_text_req==1 && SecLvlID==1)
            {
                //select both  chips & edt  ==> flow layout & edt
                ll_Reference_main.setVisibility(View.GONE);
                integerArrayList_str.clear();

                final ArrayList<LeadGenerationSecondModel> arrayList =  leadGenerationModelArrayList.get(position).getGenerationModelArrayList();
                if (arrayList!=null && arrayList.size()>0) { for (int i = 0; i < arrayList.size(); i++) { arrayList.get(i).setIsSelected(0); } }

                showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(),edit_text_req);
                isExpand =false;
            }
            else if(type_name.equals("Reference"))
            {

                //ref sep layout add
                integerArrayList_str.clear();
                tv_leadGen_thrw.setText(leadGenerationModelArrayList.get(position).getType_name());
                ll_LeadSources_Dropdown.setVisibility(View.GONE);
                iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
                ll_Reference_main.setVisibility(View.VISIBLE);
                isExpand =false;
            }
            else
            {
                //direct set text
                integerArrayList_str.clear();
                ll_Reference_main.setVisibility(View.GONE);
                tv_leadGen_thrw.setText(leadGenerationModelArrayList.get(position).getType_name());
                ll_LeadSources_Dropdown.setVisibility(View.GONE);
                iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
                isExpand =false;
            }
        });

	       /* final ArrayList<ItemSubCategoryModel> arrayList = modelArrayList.get(position).getSubCategoryArrayList();
		if (arrayList!=null && arrayList.size()>0)
		{
		    flowLayout_subCat.removeAllViews();
		    for (int i=0; i<arrayList.size(); i++)
		    {
		        @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_text_view, null );
		        final AppCompatTextView tv_subcat_name = rowView_sub.findViewById(R.id.tvText);
		        tv_subcat_name.setText(arrayList.get(i).getSubCatName());
		        flowLayout_subCat.addView(rowView_sub);
		    }
		}
	*/
        //set Animation to the layout
        //setAnimation(ll_cat_main, position);



        return subView;
    }

    private void setAdapterNamePrefix()
    {

        if (namePrefixArrayList.size() >0 && personNamePrefixModelArrayList.size()>0)
        {

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, namePrefixArrayList);
            //set def selected
            acTv_mrs.setText(namePrefixArrayList.get(0));
            selectedNamePrefixId = personNamePrefixModelArrayList.get(0).getName_prefix_id();
            selectedNamePrefix = personNamePrefixModelArrayList.get(0).getName_prefix();

            acTv_mrs.setAdapter(adapter);
            acTv_mrs.setThreshold(0);


            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (PersonNamePrefixModel pojo : personNamePrefixModelArrayList)
                {
                    if (pojo.getName_prefix().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedNamePrefixId = pojo.getName_prefix_id(); // This is the correct ID
                        selectedNamePrefix = pojo.getName_prefix();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Name prefix & id " + selectedNamePrefix +"\t"+ selectedNamePrefixId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }

    private void setAdapterProjectNames()
    {

        if (projectNamesArrayList.size() >0 &&  projectModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectNamesArrayList);
            acTv_selectProjectName.setAdapter(adapter);
            acTv_selectProjectName.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_selectProjectName.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
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
                        acTv_selectUnitType.setText("");
		             /*   //clear assignments
		                selectedUnitId = 0;
		                selectedUnitCategory = "";
		                unitCategoriesArrayList.clear();*/

                        //set adapter for unit categories
                        //  setAdapterUnitCategories(projectModelArrayList.get(position).getCategoriesModelArrayList());

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });

        }

        acTv_selectProjectName.addTextChangedListener(new TextWatcher() {
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


    private void setAdapterUnitCategories()
    {

        if (unitCategoriesModelArrayList!=null && unitCategoriesModelArrayList.size()>0)
        {
            //adding unit categories
            unitCategoriesArrayList.clear();
            for (int i =0; i<unitCategoriesModelArrayList.size(); i++)
            {
                unitCategoriesArrayList.add(unitCategoriesModelArrayList.get(i).getUnit_category());
            }

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, unitCategoriesArrayList);
            acTv_selectUnitType.setAdapter(adapter);
            acTv_selectUnitType.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectUnitType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (UnitCategoriesModel pojo : unitCategoriesModelArrayList)
                {
                    if (pojo.getUnit_category().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedUnitId = pojo.getUnit_category_id(); // This is the correct ID
                        selectedUnitCategory = pojo.getUnit_category();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Unit category & id " + selectedUnitCategory +"\t"+ selectedUnitId);

                        //check button EnabledView
                        checkButtonEnabled();

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



    private void setAdapterIncomeRanges()
    {

        if (incomeRangesArrayList.size()>0 && rangesModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, incomeRangesArrayList);
            acTv_selectAnnualIncome.setAdapter(adapter);
            acTv_selectAnnualIncome.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectAnnualIncome.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (IncomeRangesModel pojo : rangesModelArrayList)
                {
                    if (pojo.getIncome_range().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedIncomeRangeId = pojo.getIncome_range_id(); // This is the correct ID
                        selectedIncomeRange = pojo.getIncome_range();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Income Range & id " + selectedIncomeRange +"\t"+ selectedIncomeRangeId);

                        break; // No need to keep looping once you found it.
                    }
                }
            });

        }

    }

    private void setAdapterBudgetLimits()
    {

        if (budgetLimitArrayList.size()>0 && budgetLimitModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, budgetLimitArrayList);
            acTv_selectBudgetLimit.setAdapter(adapter);
            acTv_selectBudgetLimit.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectBudgetLimit.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (BudgetLimitModel pojo : budgetLimitModelArrayList)
                {
                    if (pojo.getBudget_limit().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedBudgetLimitId = pojo.getBudget_limit_id(); // This is the correct ID
                        selectedBudgetLimit = pojo.getBudget_limit();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Budget Limit Range & id " + selectedBudgetLimit +"\t"+ selectedBudgetLimitId);

                        break; // No need to keep looping once you found it.
                    }
                }
            });

        }

    }


    private void setAdapterLeadProfession()
    {

        if (professionArrayList.size() >0 &&  leadProfessionModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, professionArrayList);
            acTv_leadProfession.setAdapter(adapter);
            acTv_leadProfession.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_leadProfession.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                for (LeadProfession pojo : leadProfessionModelArrayList)
                {
                    if (pojo.getLead_profession().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedProfessionId = pojo.getLead_profession_id(); // This is the correct ID
                        selectedLeadProfessionName = pojo.getLead_profession();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Profession name & id " + selectedLeadProfessionName +"\t"+ selectedProfessionId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });

        }
    }

    private void setAdapterLeadStages() {

        if (leadStagesModelArrayList.size() >0 &&  leadStageStringArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadStageStringArrayList);
            //acTv_leadStage.setAdapter(adapter);
            //  acTv_leadStage.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            adapter = new CustomerAdapter(context, leadStagesModelArrayList);
            acTv_leadStage.setAdapter(adapter);

            acTv_leadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                String itemName = adapter.getItem(position).getLead_stage_name();
                for (LeadStagesModel pojo : leadStagesModelArrayList) {
                    if (pojo.getLead_stage_name().equals(itemName)) {

                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadStageId = pojo.getLead_stage_id(); // This is the correct ID
                        selectedLeadStageName = pojo.getLead_stage_name();
                        //acTv_leadStage.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));

                        edt_newOfflineLead_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);
                        til_newOfflineLead_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Status name & id " + selectedLeadStageName +"\t"+ selectedLeadStageId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }


                }
            });

        }
    }


    private void showSubmitLeadsAlertDialog(int SecLvlID, int position, String tv_title,int edit_text_req)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material_lead_sources, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        final AppCompatTextView  tv_setTitle = alertLayout.findViewById(R.id.tv_title_name);
        final TextInputEditText edt_addEnquiry_other=alertLayout.findViewById(R.id.edt_addEnquiry_other);

        final AppCompatTextView  tv_setTitle_news = alertLayout.findViewById(R.id.tv_title_name_news);
        final TextInputEditText edt_addEnquiry_other_news=alertLayout.findViewById(R.id.edt_addEnquiry_other_news);

        LinearLayoutCompat ll_lead_mainNews = alertLayout.findViewById(R.id.ll_lead_mainNews);
        LinearLayoutCompat ll_lead_main1 = alertLayout.findViewById(R.id.ll_lead_main1);
        LinearLayoutCompat ll_lead_main2 = alertLayout.findViewById(R.id.ll_lead_main2);
        FlowLayout flowLayout_subCat = alertLayout.findViewById(R.id.flowLayout_subCat);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        isExpand = true;
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.yes));

        //set today's date def
        //    edt_addEnquiry_other_news.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date()));

        edt_addEnquiry_other_news.setOnClickListener(v -> selectScheduleDate(edt_addEnquiry_other_news));



        if(SecLvlID == 1 && edit_text_req == 1)
        {
            ll_lead_main1.setVisibility(View.GONE);
            ll_lead_mainNews.setVisibility(View.VISIBLE);
            final ArrayList<LeadGenerationSecondModel> arrayList =  leadGenerationModelArrayList.get(position).getGenerationModelArrayList();
            if (arrayList!=null && arrayList.size()>0)
            {
                flowLayout_subCat.removeAllViews();
                for (int i=0; i<arrayList.size(); i++)
                {

                    @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_text_view, null );
                    final AppCompatTextView tv_subcat_name = rowView_sub.findViewById(R.id.tvText);
                    tv_subcat_name.setText(arrayList.get(i).getName());

                    if (arrayList.get(i).getIsSelected()==1)
                    {
                        //show already selected
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent,context.getTheme()));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                        }else {
                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white));
                        }

                    }
                    else
                    {
                        //show already deSelected
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent,context.getTheme()));

                        }else {
                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                        }

                    }

                    final int finalI = i;
                    rowView_sub.setOnClickListener(v -> {
                        //final ItemSubCategoryModel subCategoryModel = arrayList.get(finalI);
                        //getIntegerArraynm().clear();
                        if (arrayList.get(finalI).getIsSelected()==1)
                        {
                            //already select -- do deselect
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent,context.getTheme()));

                            }else {
                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                            }

                            //set deSelected
                            arrayList.get(finalI).setIsSelected(0);
                            checkInsertRemoveSubCat(arrayList.get(finalI).getId(), false);
                            checkInsertRemoveSubCat_Nm(arrayList.get(finalI).getName(), false);

                        }
                        else
                        {
                            //no selected -- do select
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent,context.getTheme()));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                            }else {
                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white));
                            }

                            //set selected
                            arrayList.get(finalI).setIsSelected(1);
                            checkInsertRemoveSubCat(arrayList.get(finalI).getId(), true);
                            checkInsertRemoveSubCat_Nm(arrayList.get(finalI).getName(), true);

                        }
                        iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
                        // isExpand = false;

                    });

                    flowLayout_subCat.addView(rowView_sub);
                }
            }


            ll_lead_main2.setVisibility(View.VISIBLE);
            tv_setTitle_news.setText(tv_title);
            edt_LeadValue= edt_addEnquiry_other_news.getText().toString();
            Log.e(TAG, "news: "+edt_LeadValue);
        }
        else if(SecLvlID == 1)
        {
            //select lead gen cat only

            ll_lead_main1.setVisibility(View.GONE);
            final ArrayList<LeadGenerationSecondModel> arrayList =  leadGenerationModelArrayList.get(position).getGenerationModelArrayList();
            if (arrayList!=null && arrayList.size()>0)
            {
                flowLayout_subCat.removeAllViews();
                for (int i=0; i<arrayList.size(); i++)
                {
                    @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_text_view, null );
                    final AppCompatTextView tv_subcat_name = rowView_sub.findViewById(R.id.tvText);
                    tv_subcat_name.setText(arrayList.get(i).getName());


                    if (arrayList.get(i).getIsSelected()==1)
                    {
                        //show already selected
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent,context.getTheme()));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                        }else {
                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white));
                        }

                    }
                    else
                    {
                        //show already deSelected
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent,context.getTheme()));

                        }else {
                            tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                            tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                        }

                    }


                    final int finalI = i;
                    rowView_sub.setOnClickListener(v -> {

                        //final ItemSubCategoryModel subCategoryModel = arrayList.get(finalI);

                        if (arrayList.get(finalI).getIsSelected()==1)
                        {
                            //already select -- do deselect

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent,context.getTheme()));

                            }else {
                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                            }

                            //set deSelected
                            arrayList.get(finalI).setIsSelected(0);
                            checkInsertRemoveSubCat(arrayList.get(finalI).getId(), false);
                            checkInsertRemoveSubCat_Nm(arrayList.get(finalI).getName(), false);

                        }
                        else
                        {
                            //no selected -- do select
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent,context.getTheme()));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                            }else
                            {
                                tv_subcat_name.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected_accent));
                                tv_subcat_name.setTextColor(context.getResources().getColor(R.color.main_white));
                            }

                            //set selected
                            arrayList.get(finalI).setIsSelected(1);
                            checkInsertRemoveSubCat(arrayList.get(finalI).getId(), true);
                            checkInsertRemoveSubCat_Nm(arrayList.get(finalI).getName(), true);

                        }
                        iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
                        // isExpand = false;

                    });

                    flowLayout_subCat.addView(rowView_sub);
                }
            }


            ll_lead_main2.setVisibility(View.VISIBLE);
            //            tv_addEnquiry_leadGen_thrw.setText(getIntegerArraynm().toString());

        }
        else if(SecLvlID == 2)
        {

            ll_lead_main2.setVisibility(View.GONE);
            Log.e(TAG, "showAlert_LeadGenAlert: "+tv_title);
            tv_setTitle.setText(tv_title);
            edt_LeadValue= edt_addEnquiry_other.getText().toString();
            Log.e(TAG, "showAlert_LeadGenAlert: "+edt_LeadValue);
            ll_lead_main1.setVisibility(View.VISIBLE);
            iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
        }

        tv_leadGen_thrw.setText("");
        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (isNetworkAvailable(context))
            {
                if(!edt_addEnquiry_other.getText().toString().trim().isEmpty())
                {
                    StringBuilder sb = new StringBuilder();
                    if (getIntegerArraynm()!=null && getIntegerArraynm().size()>0 )
                    {
                        String prefix = "";
                        for (int i =0 ; i< getIntegerArraynm().size(); i++)
                        {
                            //Append indent no's.
                            sb.append(prefix);
                            prefix = ",";
                            sb.append(getIntegerArraynm().get(i));
                        }
                    }

                    sb.append(" ");
                    sb.append(edt_addEnquiry_other.getText().toString());
                    tv_leadGen_thrw.setText(sb);
                    //LeadGeneration_otherType_info=edt_addEnquiry_other.getText().toString();
                    alertDialog.dismiss();
                    ll_LeadSources_Dropdown.setVisibility(View.GONE);

                }
                else if(!edt_addEnquiry_other_news.getText().toString().trim().isEmpty())
                {

                    StringBuilder sb = new StringBuilder();
                    if (getIntegerArraynm()!=null && getIntegerArraynm().size()>0 )
                    {
                        String prefix = "";
                        for (int i =0 ; i< getIntegerArraynm().size(); i++)
                        {
                            //Append indent no's.
                            sb.append(prefix);
                            prefix = ",";
                            sb.append(getIntegerArraynm().get(i));
                        }
                    }

                    sb.append(" ,");
                    sb.append(edt_addEnquiry_other_news.getText().toString());
                    tv_leadGen_thrw.setText(sb);
                    //LeadGeneration_otherType_info=edt_addEnquiry_other_news.getText().toString();
                    alertDialog.dismiss();
                    ll_LeadSources_Dropdown.setVisibility(View.GONE);



                }
                else
                {
                    //chips only

                    alertDialog.dismiss();
                    ll_LeadSources_Dropdown.setVisibility(View.GONE);

                    StringBuilder sb = new StringBuilder();
                    if (getIntegerArraynm()!=null && getIntegerArraynm().size()>0 )
                    {
                        String prefix = "";
                        for (int i =0 ; i< getIntegerArraynm().size(); i++)
                        {
                            //Append indent no's.
                            sb.append(prefix);
                            prefix = ",";
                            sb.append(getIntegerArraynm().get(i));
                        }
                    }

                    tv_leadGen_thrw.setText(sb);
                    //tv_addEnquiry_leadGen_thrw.setText(getIntegerArraynm().toString());
                }

            }else NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view ->
        {
            alertDialog.dismiss();
        });

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

    private void selectScheduleDate(TextInputEditText edt_addEnquiry_other_news) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //set selected date
                    mDay =  dayOfMonth;
                    mMonth = monthOfYear;
                    mYear = year;

                    edt_addEnquiry_other_news.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendNewsDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "sendNewsDate Date: "+ sendNewsDate);

                }, nYear, nMonth, nDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

	  /*  private void checkInsertRemoveSubCatLR(int leadID, boolean value)
	    {
		if (value) integerArrayList.add(leadID);
		else integerArrayList.remove(new Integer(leadID));
	    }
	*/

    //TODO pass arraylist to Json
    public ArrayList<Integer> getIntegerArray()
    {
        return integerArrayList;
    }

    private void checkInsertRemoveSubCat(int leadID, boolean value)
    {
        if (value) integerArrayList.add(leadID);
        else integerArrayList.remove(new Integer(leadID));
    }


    private void checkInsertRemoveSubCat_Nm(String leadName, boolean value)
    {
        if (value) integerArrayList_str.add(leadName);
        else integerArrayList_str.remove(leadName);
    }


    //TODO pass arraylist to Json
    public ArrayList<String> getIntegerArraynm()
    {
        return integerArrayList_str;
    }





    private void checkLeadEmail()
    {
        edt_leadEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!edt_leadEmail.getText().toString().isEmpty() && !isValidEmail(edt_leadEmail)) {
                    til_leadEmail.setErrorEnabled(true);
                    til_leadEmail.setError("Please enter valid email! eg.user@gamil.com");
                    //til_email.setHelperTextEnabled(true);
                    //til_email.setHelperText("Valid email eg. abc@gmail.com");
                }
                else {
                    til_leadEmail.setErrorEnabled(false);
                    til_leadEmail.setError(null);
                    //til_email.setHelperTextEnabled(false);
                    //til_email.setHelperText(null);
                }

                //checkButtonEnabled
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //to check lead  mobile duplicate number
        if(!isUpdate){
            edt_leadMobileNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //check=1;
                    if(Objects.requireNonNull(edt_leadMobileNo.getText()).toString().length()>9) {

                        String mob_1 = Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim();
                        String mob_2 = Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().trim();
                        if (sameNumber(mob_1, mob_2, edt_leadMobileNo)) {
                            new Helper().showCustomToast(context, "Mobile Numbers is same as Alternative Mobile Number!");
                        }
                        else {
                            String mobileNumber=edt_leadMobileNo.getText().toString();
                            Log.e(TAG, "onTextChanged: "+mobileNumber );
                        }
                        //sameNumber(edt_leadMobileNo.getText().toString(), Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
                    }
                    else ll_newOfflineLead_existUSer.setVisibility(View.GONE);

                    //checkButtonEnabled
                    checkButtonEnabled();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            edt_leadOtherMobileNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //check=1;
                    if(Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().length()>9) {

                        String mob_1 = Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().trim();
                        String mob_2 = Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim();
                        if (sameNumber(mob_1, mob_2, edt_leadOtherMobileNo)) {
                            new Helper().showCustomToast(context, "Alternative Mobile Numbers is same as Primary Mobile Number!");
                        }
                        else {
                            String mobileNumber= Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString();
                            Log.e(TAG, "onTextChanged: "+mobileNumber);
                        }
                        //sameNumber(edt_leadMobileNo.getText().toString(), Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
                    }
                    else ll_newOfflineLead_existUSer_OtherNo.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }



        //to check lead  mobile duplicate number



	/*        ll_LeadSources_Click.setOnClickListener(v -> {
		    isExpand=true;

		    if (isExpand)
		    {
		        ll_LeadSources_Dropdown.setVisibility(View.VISIBLE);
	//                scrollToBottom();
		        iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
		        isExpand=false;
		       *//* ll__AddEnq_LeadGen_Dropdown.setFocusableInTouchMode(true);
		        ll__AddEnq_LeadGen_Dropdown.requestFocus();*//*
		    }
		    else {
		        iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_down_arrow_drop_down_24);
		        // scrollToBottom();
		        ll_LeadSources_Dropdown.setVisibility(View.GONE);
		        isExpand=true;

		    }
		});*/

        //lead sources
        ll_LeadSources_Click.setOnClickListener(v -> {
            //temp
            if (isExpand)  //expanded
            {
                // //do collapse View
                anim.toggleRotate(iv_LeadSources_Dropdown, false);
                collapse(ll_LeadSources_Dropdown);
                isExpand = false;
            }
            else    // collapsed
            {
                //do expand view
                anim.toggleRotate(iv_LeadSources_Dropdown, true);
                expandSubView(ll_LeadSources_Dropdown);
                isExpand = true;
            }
        });

    }

    private boolean sameNumber(String num1,String num2, TextInputEditText textInputEditText)
    {
        if(num1!=null && num2!=null)
        {
            if(num1.equalsIgnoreCase(num2)) {
                flagNumduplicate=true;
                textInputEditText.setError("Mobile number already exits! Please enter another number!!");
                return  true;
            }
            else {
                textInputEditText.setError(null);
                flagNumduplicate=false;
                return  false;
            }
        }

        return  false;
    }
    private void checkHouseType()
    {
        FirstHomeID=1;
        houseRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {

                    switch (checkedId)
                    {
                        case R.id.rb_newOfflineLead_firstHome_yes:
                            // Toast.makeText(getApplicationContext(),""+rdo_btn_First_House_yes.getText().toString(), Toast.LENGTH_LONG).show();
                            FirstHomeID=1;

                            break;
                        case R.id.rb_newOfflineLead_FirstHome_no:
                            // Toast.makeText(getApplicationContext(),""+rdo_btn_First_House_no.getText().toString(),Toast.LENGTH_LONG).show();
                            FirstHomeID=2;
                            break;
                        default:
                            break;
                    }


                }
        );

    }


    private void startCountDownTimer(final AppCompatTextView tv_countDownTime)
    {

        CountDownTimer countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_countDownTime.setText(new Helper().hmsTimeFormatter(millisUntilFinished));
            }
            @Override
            public void onFinish() {
                tv_countDownTime.setText("00:00");

                new Handler().postDelayed(() -> tv_countDownTime.setVisibility(View.GONE), 1000);
            }

        }.start();
        countDownTimer.start();
    }

   /* private void set_radioButtons()
    {
        //property Buying for purpose
        rdoGrp_alreadySiteVisited.setOnCheckedChangeListener((group, checkedId) ->
        {

            int selectedId = rdoGrp_alreadySiteVisited.getCheckedRadioButtonId();
            final RadioButton rBtn = rdoGrp_alreadySiteVisited.findViewById(selectedId);
            final String btnText =  rBtn.getText().toString();
            //Toast.makeText(RegisterEventNewActivity.this, "Accommodation "+Accommodation, Toast.LENGTH_SHORT).show();

            // incomplete due to spares not available
            //incomplete due to quotation pending
            isAlreadySiteVisited = btnText.contains(getString(R.string.yes));
            if (btnText.contains(getString(R.string.yes)))
            {
                // incomplete due to spares not available
                //isSpareOrQuot = true;
                ll_alreadySiteVisit.setVisibility(View.VISIBLE);
            }
            else
            {
                //incomplete due to quotation pending
                //isSpareOrQuot = false;
                ll_alreadySiteVisit.setVisibility(View.GONE);
            }

            //check button EnabledView
            checkButtonEnabled();

        });


    }*/

    private void selectDateOfBirth()
    {
        //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
        //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_newOfflineLead_dob.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));

                    int mth = monthOfYear + 1;
                    sendDateOfBirth = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "lead Date of Birth:: " + sendDateOfBirth);
                    //check Button Enabled View
                    // checkButtonEnabled();

                }, mYear, mMonth, mDay);

	      /*  Date date = new Date();
		@SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		try {
		    date = format.parse("01/01/"+(Calendar.getInstance().get(Calendar.YEAR)-28));
		} catch (ParseException e) {
		    e.printStackTrace();
		}

		datePickerDialog.getDatePicker();
		assert date != null;
		datePickerDialog.getDatePicker().setMaxDate(date.getTime());
		datePickerDialog.show();*/

        //  datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();


    }

    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_prefSiteVisitFromDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPrefVisitFromDate = year + "-" + mth + "-" + dayOfMonth;


                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "pref Visit_send From Date: "+ sendPrefVisitFromDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, nYear, nMonth, nDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_prefSiteVisitToDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendPrefVisitToDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "pref Visit_send To Date: "+ sendPrefVisitToDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, nYear, nMonth, nDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(getLongNextDateFromString(sendPrefVisitFromDate));
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }

    private void selectVisitDate()
    {
        datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_alreadySiteVisitDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendAlreadySiteVisitDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);


                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "al_siteVisit_send Date: "+ sendAlreadySiteVisitDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, nYear, nMonth, nDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();

    }


    private void selectVisitTime()
    {
        final Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.MyDatePicker,
                (TimePicker view, int hourOfDay, int minute) -> {

                    sendAlreadySiteVisitTime = hourOfDay + ":" + minute + ":" +"00";
                    boolean isPM = (hourOfDay >= 12);
                    edt_alreadySiteVisitTime.setText(String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendAlreadySiteVisitTime!=null) Log.e(TAG, "Al Visit Time: "+sendAlreadySiteVisitTime);

                    //set button view enabled
                    //if (isAssignLater && !tv_committedDate.getText().toString().trim().isEmpty()) setButtonEnabledView();

                    //check button EnabledView
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
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

        //final String eventSource= "https://credaimaharashtra.org/eventDetails/"+events_id;

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
                        Uri tempUri = getImageUri(getApplicationContext(), photo);

                        // CALL THIS METHOD TO GET THE ACTUAL PATH
                        File finalFile = new File(getRealPathFromURI(tempUri));

                        Log.e("finalFile", finalFile.getAbsolutePath());
                        Log.e("finalFile", "ab path");
                        Log.e("finalFile", finalFile.getPath());
                        Log.e("finalFile", finalFile.toString());

                        Log.e(TAG, "onActivityResult: " + getFileName_from_filePath(finalFile.getAbsolutePath()));
                        //tv_UploadedDoc_Name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                        //setSelectedDoc(myUploadModel, finalFile.getAbsolutePath());
                        idDocSelected=true;
                    } else Log.e("myDocument & pos ", "data null");


		            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
		            {
		                // Get the resultant image's URI.
		                //final Uri selectedImageUri = (data == null) ? mCapturedImageURI : data.getData();

		                //get the path from file
		                File file = new File(imageFile.getPath());
		                Log.e("file", file.toString());
		                String photoUrl = file.toString();
		                //iv_tribeUser_profilePic.setImageURI(Uri.parse(imageFile.getPath()));

		            }
		            else
		            {
		                //iv_tribeUser_profilePic.setImageURI(Uri.parse("file://"+ Uri.parse(imageFile.getAbsolutePath())));
		                String photoUrl =   imageFile.getAbsolutePath();
		                Log.e("rURL_camera", ""+photoUrl);
		                //iv_tribeUser_profilePic.buildDrawingCache();
		            }*/


                    //call the method that upload the profile pic
                    //call_uploadImage(photoUrl);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } else if (requestCode == 2)  //From Gallery
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


                        //setSelectedDoc(myUploadModel, photoUrl);
                        idDocSelected=true;
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
                Log.e(TAG, photoUrl);
                Log.e(TAG, "ext " + ext);
                Log.e(TAG, "mimetype " + mimetype);

                //setSelectedDoc(myUploadModel, photoUrl);
                idDocSelected=true;
            }

        }
        else if (requestCode == 121  && responseCode  == RESULT_OK)
        {
            Country country = (Country) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+country.getName() +" \n"+country.getPhoneCode() );
            //set country
            //ccp.setSelectedCountry(country);

            countryPhoneCode = country.getPhoneCode();
            String iso = country.getIso().toUpperCase();
            flag_imv.setImageResource(CountryUtils.getFlagDrawableResId(country));
            selected_country_tv.setText(getString(R.string.country_code_and_phone_code, iso, countryPhoneCode));
            Log.e(TAG, "onActivityResult: countryCode "+countryPhoneCode );
            //ccp.setCountryForNameCode(country.getName());
            new Helper().showCustomToast(context, "selected "+ country.getName() );

            if ( isRefLead && ( countryPhoneCode.equalsIgnoreCase(countryPhoneCode_ref) && Objects.requireNonNull(edt_refMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))) new Helper().showCustomToast(context, "Reference mobile number and Lead mobile number should be different!");
        }
        else if (requestCode == 121 && responseCode == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }
        else if (requestCode == 122  && responseCode  == RESULT_OK) {
            Country country = (Country) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+country.getName() +" \n"+country.getPhoneCode() );
            //set country
            //ccp.setSelectedCountry(country);

            countryPhoneCode_ref = country.getPhoneCode();
            String iso = country.getIso().toUpperCase();
            //iv_refCountryArrow.setImageResource(CountryUtils.getFlagDrawableResId(country));
            tv_refCountry.setText(getString(R.string.country_code_and_phone_code, iso, countryPhoneCode_ref));
            Log.e(TAG, "onActivityResult: ref countryCode "+countryPhoneCode_ref );
            //ccp.setCountryForNameCode(country.getName());
            new Helper().showCustomToast(context, "selected "+ country.getName() );
            if ( isRefLead && ( countryPhoneCode.equalsIgnoreCase(countryPhoneCode_ref) && Objects.requireNonNull(edt_refMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))) new Helper().showCustomToast(context, "Reference mobile number and Lead mobile number should be different!");
        }
        else if (requestCode == 122 && responseCode == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }

        else if (requestCode == 123  && responseCode  == RESULT_OK)
        {
            Country country = (Country) data.getSerializableExtra("result");
            Log.e(TAG, "onActivityResult: "+country.getName() +" \n"+country.getPhoneCode() );
            //set country
            //ccp.setSelectedCountry(country);

            countryPhoneCode_1 = country.getPhoneCode();
            String iso = country.getIso().toUpperCase();
            //iv_refCountryArrow.setImageResource(CountryUtils.getFlagDrawableResId(country));
            selected_country_tv2.setText(getString(R.string.country_code_and_phone_code, iso, countryPhoneCode_1));
            Log.e(TAG, "onActivityResult: ref countryCode "+countryPhoneCode_1 );
            //ccp.setCountryForNameCode(country.getName());
            new Helper().showCustomToast(context, "selected "+ country.getName() );
            if (( countryPhoneCode.equalsIgnoreCase(countryPhoneCode_1) && Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))) new Helper().showCustomToast(context, "Alternative mobile number and Lead mobile number should be different!");
        }
        else if (requestCode == 123 && responseCode == RESULT_CANCELED)
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



    private void checkValidations()
    {
        //name prefix
        if (selectedNamePrefixId==0) new Helper().showCustomToast(context, "Please select name prefix!");
            //lead name
        else if (Objects.requireNonNull(edt_leadFirstName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter customer name!");
            //email
            //   else if (Objects.requireNonNull(edt_leadEmail.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter customer email!");
            // valid email
            // else if (!isValidEmail(edt_leadEmail)) new Helper().showCustomToast(context, "Please enter a valid email!");
            //last name
            //else if (Objects.requireNonNull(edt_leadLastName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter customer last name!");
            // mobile
        else if (Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter customer mobile number!");
            //valid mobile
            // else if (!isValidPhone(edt_mobile)) new Helper().showCustomToast(context, "Please enter a valid mobile number!");
            // verified mobile
            //else if (!isLeadMobileVerified) new Helper().showCustomToast(context, "Please do verify customer's mobile number!");

            //mobile number exist
        else if (isExist_WhatsAppNo) new Helper().showCustomToast(context, "Mobile number already exits! Please enter another Number!!");

            //other mobile number exist
        else if (isExist_OtherNo) new Helper().showCustomToast(context, "Mobile number already exits! Please enter another number!!");

            //same number exist
        else if (flagNumduplicate) new Helper().showCustomToast(context, "Duplicate number found enter another number!");
            //b_date
            // else if (Objects.requireNonNull(edt_birthday.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please select your birth date!");
            //else if (sendBDate==null) new Helper().showCustomToast(context, "Please enter customer birth date!");
            //lead stage
        else if (selectedLeadStageId==0) new Helper().showCustomToast(context, "Please select lead status!");
            //project
       // else if (selectedProjectId==0) new Helper().showCustomToast(context, "Please select project name!");
            // unit type
       // else if (selectedUnitId==0) new Helper().showCustomToast(context, "Please select unit category!");
            //pref. visit dates
            //  else if (sendPrefVisitFromDate==null && sendPrefVisitToDate== null) new Helper().showCustomToast(context, "Please select preferred site visit dates!");
            //pref. visit from date
            //  else if (sendPrefVisitFromDate==null) new Helper().showCustomToast(context, "Please select preferred site visit From date!");
            //pref. visit to date
            //  else if (sendPrefVisitToDate==null) new Helper().showCustomToast(context, "Please select preferred site visit To date!");
            //if already site visited //then ask for visit date
            // else if (isAlreadySiteVisited && sendAlreadySiteVisitDate==null ) new Helper().showCustomToast(context, "Please select site visit date!");
            //if already site visited //then ask for visit date
            //  else if (isAlreadySiteVisited && sendAlreadySiteVisitTime==null) new Helper().showCustomToast(context, "Please select site visit time!");
            //site visit conducted by
            //    else if (isAlreadySiteVisited && selectedUserId==0) new Helper().showCustomToast(context, "Please select site visit conducted by!");
            // if ref lead && ref name
            // else if ( isRefLead &&  Objects.requireNonNull(edt_refName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Reference person name!");
            // if ref lead && ref mobile
            //  else if ( isRefLead &&  Objects.requireNonNull(edt_refMobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Reference mobile number!");
            //if ref lead && ref mobile && lead mobile both are same
            //  else if ( isRefLead && ( countryPhoneCode.equalsIgnoreCase(countryPhoneCode_ref) && Objects.requireNonNull(edt_refMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))) new Helper().showCustomToast(context, "Reference mobile number and Lead mobile number should be different!");
            // if ref lead && ref project name
            //  else if ( isRefLead && selectedRefProjectId ==0 ) new Helper().showCustomToast(context, "Please select reference from!");
            // if ref lead && ref flat number
            //   else if ( isRefLead &&  Objects.requireNonNull(edt_refFlatNumber.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please Enter Reference Remarks!");
        else
        {
            if(Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().trim().isEmpty()){
                countryPhoneCode_1 ="";
            }
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }


    }


    private void checkButtonEnabled()
    {
        //name prefix
        if (selectedNamePrefixId==0) setButtonDisabledView();
            //lead name
        else if (Objects.requireNonNull(edt_leadFirstName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //lead email
            //   else if (Objects.requireNonNull(edt_leadEmail.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //valid email
            //  else if (!isValidEmail(edt_leadEmail)) setButtonDisabledView();
            //last name
            //else if (Objects.requireNonNull(edt_leadLastName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // mobile
        else if (Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // verified mobile
            //else if (!isLeadMobileVerified) setButtonDisabledView();
            //mobile number exist
        else if (isExist_WhatsAppNo) setButtonDisabledView();

            //other mobile number exist
        else if (isExist_OtherNo) setButtonDisabledView();

            //duplicate number exist
        else if (flagNumduplicate) setButtonDisabledView();
            //b_date
            //else if (sendBDate==null) setButtonDisabledView();
            //lead stage
        else if (selectedLeadStageId==0) setButtonDisabledView();
            //project
       // else if (selectedProjectId==0) setButtonDisabledView();
            // unit type
       // else if (selectedUnitId==0) setButtonDisabledView();
            //pref. visit dates
            //   else if (sendPrefVisitFromDate==null && sendPrefVisitToDate== null) setButtonDisabledView();
            //pref. visit from date
            //  else if (sendPrefVisitFromDate==null) setButtonDisabledView();
            //pref. visit to date
            //  else if (sendPrefVisitToDate==null) setButtonDisabledView();
            //if already site visited //then ask for visit date
            //   else if (isAlreadySiteVisited && sendAlreadySiteVisitDate==null ) setButtonDisabledView();
            //if already site visited //then ask for visit date
            //   else if (isAlreadySiteVisited && sendAlreadySiteVisitTime==null) setButtonDisabledView();
            //site visit conducted by
            //  else if (isAlreadySiteVisited && selectedUserId==0) setButtonDisabledView();
            // if ref lead && ref name
            //else if ( isRefLead &&  Objects.requireNonNull(edt_refName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // if ref lead && ref mobile
            //else if ( isRefLead &&  Objects.requireNonNull(edt_refMobileNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //if ref lead && ref mobile && lead mobile both are same
            // else if ( isRefLead && ( countryPhoneCode.equalsIgnoreCase(countryPhoneCode_ref) && Objects.requireNonNull(edt_refMobileNo.getText()).toString().equalsIgnoreCase(Objects.requireNonNull(edt_leadMobileNo.getText()).toString()))) setButtonDisabledView();
            // if ref lead && ref project name
            // else if ( isRefLead && selectedRefProjectId ==0 ) setButtonDisabledView();
            // if ref lead && ref flat number
            //  else if ( isRefLead &&  Objects.requireNonNull(edt_refFlatNumber.getText()).toString().trim().isEmpty()) setButtonDisabledView();
        else
        {
            //set button enabled view
            setButtonEnabledView();
        }

    }



    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_submitLead.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submitLead.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit lead

        mBtn_submitLead.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submitLead.setTextColor(getResources().getColor(R.color.main_white));
    }


    private void showSubmitLeadAlertDialog()
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

        tv_msg.setText(getString(R.string.submit_offline_lead_question));
        tv_desc.setText(getString(R.string.submit_offline_lead_confirmation));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add offline lead api
            showProgressBar(getString(R.string.submitting_offline_lead_details));
            call_addSalesLead();
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

    private void call_addSalesLead()
    {
        OfflineLeadModel offlineLeadModel=new OfflineLeadModel();
        offlineLeadModel.setApi_token(api_token);
        offlineLeadModel.setPrefix_id(selectedNamePrefixId);
        offlineLeadModel.setPrefix(selectedNamePrefix);
        offlineLeadModel.setCustomer_name(Objects.requireNonNull(edt_leadFirstName.getText()).toString());
        offlineLeadModel.setCustomer_email( Objects.requireNonNull(edt_leadEmail.getText()).toString());
        offlineLeadModel.setCountry_code(countryPhoneCode);
        offlineLeadModel.setCountry_code_1(countryPhoneCode_1);
        offlineLeadModel.setMobile_number(Objects.requireNonNull(edt_leadMobileNo.getText()).toString());
        offlineLeadModel.setAlternate_mobile_number(Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
        offlineLeadModel.setAddress_line_1(Objects.requireNonNull(edt_leadAddress.getText()).toString());
        offlineLeadModel.setProject_id(selectedProjectId);
        offlineLeadModel.setCustomer_project_name(selectedProjectName);
        offlineLeadModel.setUnit_category_id(selectedUnitId);
        offlineLeadModel.setCustomer_unit_type(selectedUnitCategory);
        offlineLeadModel.setLead_profession(selectedLeadProfessionName);
        offlineLeadModel.setLead_ni_reason(Objects.requireNonNull(edt_newOfflineLead_niReason.getText()).toString());
        offlineLeadModel.setLead_ni_other_reason(Objects.requireNonNull(edt_newOfflineLead_niReason.getText()).toString());
        offlineLeadModel.setBudget_limit_id(selectedBudgetLimitId);
        offlineLeadModel.setBudget_limit(selectedBudgetLimit);
        offlineLeadModel.setIncome_range_id(selectedIncomeRangeId);
        offlineLeadModel.setIncome_range(selectedIncomeRange);
        offlineLeadModel.setLead_profession_id(selectedProfessionId);
        offlineLeadModel.setLead_profession(selectedLeadProfessionName);
        offlineLeadModel.setIs_first_home(FirstHomeID);
        offlineLeadModel.setLead_stage_id(selectedLeadStageId);
        offlineLeadModel.setLead_stage(selectedLeadStageName);
        offlineLeadModel.setLead_status_id(isAlreadySiteVisited ? 2 : 1);
        offlineLeadModel.setDob(sendDateOfBirth!=null ? sendDateOfBirth : "");
        offlineLeadModel.setLead_types_id(selectedLeadSourceId);
        offlineLeadModel.setLead_types(selectedLeadSource);
        offlineLeadModel.setSales_person_id(user_id);
        offlineLeadModel.setIs_site_visited(isAlreadySiteVisited ? 1 : 0);
        offlineLeadModel.setVisit_date(sendAlreadySiteVisitDate);
        offlineLeadModel.setVisit_time(sendAlreadySiteVisitTime);
        offlineLeadModel.setVisit_remark( Objects.requireNonNull(edt_alreadySiteVisitRemark.getText()).toString());
        offlineLeadModel.setRemarks(Objects.requireNonNull(edt_leadRemarks.getText()).toString());
        SaveOfflineLeadData(offlineLeadModel);

        isLeadSubmitted = true;
        onLeadSubmit();
    }

    private void SaveOfflineLeadData(OfflineLeadModel model) {
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();


        if (sharedPreferences != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("prefix_id", model.getPrefix_id());
                jsonObject.put("prefix", model.getPrefix());
                jsonObject.put("full_name", model.getCustomer_name());
                jsonObject.put("email", model.getCustomer_email());
                jsonObject.put("country_code", model.getCountry_code());
                jsonObject.put("country_code_1", model.getCountry_code_1());
                jsonObject.put("mobile_number", model.getMobile_number());
                jsonObject.put("alternate_mobile_number", model.getAlternate_mobile_number());
                jsonObject.put("address_line_1", model.getAddress_line_1());
                jsonObject.put("project_id", model.getProject_id());
                jsonObject.put("project_name", model.getCustomer_project_name());
                jsonObject.put("unit_category_id", model.getUnit_category_id());
                jsonObject.put("unit_type", model.getCustomer_unit_type());
                jsonObject.put("lead_profession_id", model.getLead_profession_id());
                jsonObject.put("lead_profession", model.getLead_profession());
                jsonObject.put("lead_ni_reason", model.getLead_ni_reason());
                jsonObject.put("lead_ni_other_reason", model.getLead_ni_other_reason());
                jsonObject.put("budget_limit_id", model.getBudget_limit_id());
                jsonObject.put("budget_limit", model.getBudget_limit());
                jsonObject.put("income_range_id", model.getIncome_range_id());
                jsonObject.put("income_range", model.getIncome_range());
                jsonObject.put("lead_profession_id", model.getLead_profession_id());
                jsonObject.put("lead_profession", model.getLead_profession());
                jsonObject.put("is_first_home", model.getIs_first_home());
                jsonObject.put("lead_stage_id", model.getLead_stage_id());
                jsonObject.put("lead_stage", model.getLead_stage());
                jsonObject.put("lead_status_id", model.getLead_status_id());
                jsonObject.put("dob", model.getDob());
                jsonObject.put("sales_person_id", model.getSales_person_id());
                jsonObject.put("lead_types_id", model.getLead_types_id());
                jsonObject.put("lead_types", model.getLead_types());
                jsonObject.put("is_site_visited", model.getIs_site_visited());
                jsonObject.put("visit_date", model.getVisit_date());
                jsonObject.put("visit_time", model.getVisit_time());
                jsonObject.put("visit_remark", model.getVisit_remark());
                jsonObject.put("remarks", model.getRemarks());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String notification = null;
            if (sharedPreferences.getString("DownloadModel", null) != null)
                notification = sharedPreferences.getString("DownloadModel", null);

            Log.e(TAG, "Save enquiries: " + notification);

            if (notification != null) {

                try {
                    int count = 0;

                    JSONArray newJsonArray = new JSONArray();
                    newJsonArray.put(jsonObject);

                    JSONArray oldJsonArray = new JSONArray(notification);
                    try {
                        for (int i = 0; i < oldJsonArray.length(); i++) {
                            // prev json objects
                            newJsonArray.put(oldJsonArray.get(i));
                        }
                        count = newJsonArray.length();
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    if (sharedPreferences != null) {
                        editor = sharedPreferences.edit();
                        editor.putString("DownloadModel", newJsonArray.toString());
                        editor.putInt("DownloadModelcount", count);
                        editor.apply();
                        Log.e("NewAry", newJsonArray.toString());
                        Log.e(TAG, "offline lead count: "+count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);

                if (sharedPreferences != null) {
                    editor = sharedPreferences.edit();
                    editor.putString("DownloadModel", jsonArray.toString());
                    editor.apply();
                }
            }
        }
        editor.apply();

    }

    public void onLeadSubmit()
    {
        runOnUiThread(() -> {
            hideProgressBar();
            if(isLeadSubmitted)
            {
                showSuccessAlert();
            }
            else showErrorLog("Lead failed to submit..");
        });
    }
    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        runOnUiThread(() -> {

            //open disabled View
            openView();
            //set gif
            gif_newOfflineLead.setImageResource(R.drawable.gif_success);
            //set animation
            new Animations().scaleEffect(ll_success);
            //visible view
            ll_success.setVisibility(View.VISIBLE);
            //show success toast
            new Helper().showSuccessCustomToast(context, getString(R.string.offline_lead_added_successfully));

            flagExit=true;
           /* if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isLeadAdd",1);
                editor.apply();
            }*/

            //do backPress
            new Handler().postDelayed(() -> {
                //share_dialog.dismiss();
                ll_success.setVisibility(View.GONE);
                //close view
                closeView();
                //do backPress
                onBackPressed();
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


    private boolean isValidEmail(EditText email)
    {
        boolean ret = true;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        //if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(context, view);
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


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context, message);
        });

    }

    private boolean isValidPhone(EditText phone)
    {
        boolean ret = true;
        if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }

    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
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

            Log.e(TAG, "onOptionsItemSelected: fromShortcut"+fromShortcut );
            if (fromShortcut)
            {
                startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                finish();
            }
            else onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }



    @Override
    public void onBackPressed() {

        if (isLeadSubmitted) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Lead Submitted"));

        if(!flagExit)
        {

            showBackPressedIcons();
        }
        else
        {
            if(fromShortcut)
            {
                {
                    startActivity(new Intent(context, AllOfflineLeads_Activity.class));
                    finish();
                }
            }
            else{
                super.onBackPressed();
                Intent intent = new Intent(this,AllOfflineLeads_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
            }

        }

    }

    private void showBackPressedIcons()
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

        tv_msg.setText(getString(R.string.exit_form));
        tv_desc.setText(getString(R.string.do_you_want_to_exit_form));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.ok));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            if(fromShortcut)
            {
                startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                finish();
            }
            else
            {
                super.onBackPressed();
            }
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );

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

}

