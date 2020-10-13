package com.tribeappsoft.leedo.admin.leads;

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
import android.text.Html;
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
import android.widget.RadioButton;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.leads.model.BudgetLimitModel;
import com.tribeappsoft.leedo.admin.leads.model.LeadGenerationModel;
import com.tribeappsoft.leedo.admin.leads.model.LeadGenerationSecondModel;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.api.WebServer;
import com.tribeappsoft.leedo.models.UserModel;
import com.tribeappsoft.leedo.models.leads.IncomeRangesModel;
import com.tribeappsoft.leedo.models.leads.LeadBuyingDurationModel;
import com.tribeappsoft.leedo.models.leads.LeadCampaignsModel;
import com.tribeappsoft.leedo.models.leads.LeadProfession;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.models.leads.LeadsCampaignDetailsModel;
import com.tribeappsoft.leedo.models.leads.PersonNamePrefixModel;
import com.tribeappsoft.leedo.models.leads.PropertyBuyingForModel;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.formatEventDate;
import static com.tribeappsoft.leedo.util.Helper.getLongNextDateFromString;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;
import static com.tribeappsoft.leedo.util.Helper.setDatePickerFormatDateFromString;

public class AddNewLeadActivity extends AppCompatActivity
{

    @BindView(R.id.cl_addNewLead) CoordinatorLayout parent;
    @BindView(R.id.ll_newLead_main) LinearLayoutCompat ll_main;
    @BindView(R.id.tv_newLead_salesRepName) AppCompatTextView tv_salesRepName;

    @BindView(R.id.ll_newLead_refLeadMain) LinearLayoutCompat ll_refLeadMain;
    @BindView(R.id.iv_newLead_refLead_ec) AppCompatImageView iv_refLead_ec;
    @BindView(R.id.ll_newLead_addRefData) LinearLayoutCompat ll_viewRefData;

    @BindView(R.id.ll_newLead_refMobCcp) LinearLayoutCompat ll_refMobCcp;
    @BindView(R.id.iv_newLead_refCountryArrow) AppCompatImageView iv_refCountryArrow;
    @BindView(R.id.tv_newLead_refCountry) AppCompatTextView tv_refCountry;

    @BindView(R.id.sm_newLead_leadRefBy) SwitchMaterial sm_leadRefBy;
    @BindView(R.id.edt_newLead_refName) TextInputEditText edt_refName;
    @BindView(R.id.edt_newLead_refMobileNo) TextInputEditText edt_refMobileNo;
    @BindView(R.id.til_newLead_refMobile) TextInputLayout til_refMobile;
    @BindView(R.id.acTv_newLead_selectRefProject) AutoCompleteTextView acTv_selectRefProject;
    //@BindView(R.id.edt_newLead_refProjectName) TextInputEditText edt_refProjectName;
    @BindView(R.id.edt_newLead_refFlatNumber) TextInputEditText edt_refFlatNumber;

    @BindView(R.id.ll_newLead_leadDetailsMain) LinearLayoutCompat ll_leadDetailsMain;
    @BindView(R.id.iv_newLead_leadDetails_ec) AppCompatImageView iv_leadDetails_ec;
    @BindView(R.id.ll_newLead_viewLeadDetails) LinearLayoutCompat ll_viewLeadDetails;

    @BindView(R.id.acTv_newLead_mrs) AutoCompleteTextView acTv_mrs;
    //@BindView(R.id.spn_newLead_selectMrs) AppCompatSpinner spn_selectMrs;
    @BindView(R.id.til_newLead_leadFirstName) TextInputLayout til_leadFirstName;
    @BindView(R.id.edt_newLead_leadFirstName) TextInputEditText edt_leadFirstName;
    @BindView(R.id.edt_newLead_leadMiddleName) TextInputEditText edt_leadMiddleName;
    @BindView(R.id.edt_newLead_leadLastName) TextInputEditText edt_leadLastName;

    @BindView(R.id.ll_newLead_leadMobCcp) LinearLayoutCompat ll_leadMobCcp;
    @BindView(R.id.ll_newLead_leadOtherMobCcp) LinearLayoutCompat ll_leadOtherMobCcp;
    @BindView(R.id.flag_imv) AppCompatImageView flag_imv;
    @BindView(R.id.selected_country_tv) AppCompatTextView selected_country_tv;
    @BindView(R.id.selected_country_tv2) AppCompatTextView selected_country_tv2;

    @BindView(R.id.til_newLead_leadMobile) TextInputLayout til_leadMobile;
    @BindView(R.id.edt_newLead_leadMobileNo) TextInputEditText edt_leadMobileNo;
    @BindView(R.id.til_newLead_leadOtherMobile) TextInputLayout til_leadOtherMobile;
    @BindView(R.id.edt_newLead_leadOtherMobileNo) TextInputEditText edt_leadOtherMobileNo;
    @BindView(R.id.til_newLead_leadEmail) TextInputLayout til_leadEmail;
    @BindView(R.id.edt_newLead_leadEmail) TextInputEditText edt_leadEmail;
    @BindView(R.id.mdp_newLead_leadBirthDate) MyLocalDatePicker mdp_leadBirthDate;
    @BindView(R.id.edt_newLead_leadAddress) TextInputEditText edt_leadAddress;
    @BindView(R.id.edt_newLead_leadProfession) TextInputEditText edt_leadProfession;
    @BindView(R.id.acTv_newLead_selectAnnualIncome) AutoCompleteTextView acTv_selectAnnualIncome;
    @BindView(R.id.acTv_newLead_selectBudgetLimit) AutoCompleteTextView acTv_selectBudgetLimit;
    @BindView(R.id.acTv_newLead_leadProfession) AutoCompleteTextView acTv_leadProfession;
    @BindView(R.id.acTv_newLead_leadStage) AutoCompleteTextView acTv_leadStage;
    @BindView(R.id.edt_siteVisit_niReason) TextInputEditText edt_newLead_niReason;
    @BindView(R.id.til_siteVisit_niReason) TextInputLayout til_newLead_niReason;

    //verify Lead Mobile
    @BindView(R.id.tv_newLead_mobAlExists) AppCompatTextView tv_mobAlExists;
    @BindView(R.id.tv_newLead_mobVerifySuccess) AppCompatTextView tv_mobVerifySuccess;
    @BindView(R.id.til_newLead_leadOtp) TextInputLayout til_leadOtp;
    @BindView(R.id.edt_newLead_OTPNumber) TextInputEditText edt_OTPNumber;
    @BindView(R.id.mBtn_newLead_verifyLeadMob) MaterialButton mBtn_verifyLeadMob;
    @BindView(R.id.tv_newLead_resendOtp_msg) AppCompatTextView tv_resendOtp_msg;
    @BindView(R.id.tv_newLead_resendOtp_counter) AppCompatTextView tv_resendOtp_counter;
    @BindView(R.id.mBtn_newLead_resendOTP) MaterialButton mBtn_resendOTP;

    @BindView(R.id.ll_newLead_kycDocsMain) LinearLayoutCompat ll_kycDocsMain;
    @BindView(R.id.iv_newLead_kycDocs_ec) AppCompatImageView iv_kycDocs_ec;
    @BindView(R.id.ll_newLead_viewKycDoc) LinearLayoutCompat ll_viewKycDoc;
    @BindView(R.id.ll_newLead_addKYCDoc) LinearLayoutCompat ll_addKYCDoc;

    @BindView(R.id.ll_newLead_leadCampaignMain) LinearLayoutCompat ll_leadCampaignMain;
    @BindView(R.id.iv_newLead_leadCampaign_ec) AppCompatImageView iv_leadCampaign_ec;
    @BindView(R.id.ll_newLead_viewLeadCampaign) LinearLayoutCompat ll_viewLeadCampaign;
    @BindView(R.id.ll_newLead_addLeadCampaign) LinearLayoutCompat ll_addLeadCampaign;

    @BindView(R.id.ll_newLead_projectDetailsMain) LinearLayoutCompat ll_projectDetailsMain;
    @BindView(R.id.iv_newLead_viewProjectDetails_ec) AppCompatImageView iv_viewProjectDetails_ec;
    @BindView(R.id.ll_newLead_viewProjectDetails) LinearLayoutCompat ll_viewProjectDetails;
    @BindView(R.id.acTv_newLead_selectProjectName) AutoCompleteTextView acTv_selectProjectName;
    @BindView(R.id.acTv_newLead_selectUnitType) AutoCompleteTextView acTv_selectUnitType;
    @BindView(R.id.edt_newLead_prefSiteVisitFromDate) TextInputEditText edt_prefSiteVisitFromDate;
    @BindView(R.id.edt_newLead_prefSiteVisitToDate) TextInputEditText edt_prefSiteVisitToDate;

    @BindView(R.id.ll_newLead_otherDetailsMain) LinearLayoutCompat ll_otherDetailsMain;
    @BindView(R.id.iv_newLead_viewOtherDetails_ec) AppCompatImageView iv_viewOtherDetails_ec;
    @BindView(R.id.ll_newLead_viewOtherDetails) LinearLayoutCompat ll_viewOtherDetails;
    @BindView(R.id.acTv_newLead_selectWhenToBuy) AutoCompleteTextView acTv_selectWhenToBuy;

    @BindView(R.id.rdoGrp_newLead_propertyBuyFor) RadioGroup rdoGrp_propertyBuyFor;
    @BindView(R.id.rb_newLead_selfUse) AppCompatRadioButton rb_selfUse;
    @BindView(R.id.rb_newLead_investment) AppCompatRadioButton rb_investment;

    //@BindView(R.id.rb_newLead_other) AppCompatRadioButton rb_other;
    @BindView(R.id.edt_newLead_otherPropertyBuyPurpose) TextInputEditText edt_otherPropertyBuyPurpose;
    @BindView(R.id.tv_alreadyVisitedTitle) AppCompatTextView tv_alreadyVisitedTitle;
    @BindView(R.id.rdoGrp_newLead_alreadySiteVisited) RadioGroup rdoGrp_alreadySiteVisited;
    @BindView(R.id.rb_newLead_alreadySiteVisited_yes) AppCompatRadioButton rb_alreadySiteVisited_yes;
    @BindView(R.id.rb_newLead_alreadySiteVisited_no) AppCompatRadioButton rb_alreadySiteVisited_no;

    @BindView(R.id.edt_newLead_dob)TextInputEditText edt_newLead_dob;
    @BindView(R.id.acTv_newLead_selectLeadSource) AutoCompleteTextView acTv_selectLeadSource;
    // @BindView(R.id.ll_newLead_viewSubSource) LinearLayoutCompat ll_viewSubSource;
    @BindView(R.id.ll_newLead_alreadySiteVisit) LinearLayoutCompat ll_alreadySiteVisit;
    @BindView(R.id.edt_newLead_alreadySiteVisitDate) TextInputEditText edt_alreadySiteVisitDate;
    @BindView(R.id.edt_newLead_alreadySiteVisitTime) TextInputEditText edt_alreadySiteVisitTime;
    @BindView(R.id.edt_newLead_alreadySiteVisitRemark) TextInputEditText edt_alreadySiteVisitRemark;
    @BindView(R.id.acTv_newLead_selectVisitConductedBy) AutoCompleteTextView acTv_selectVisitConductedBy;
    @BindView(R.id.acTv_newLead_selectPropertyBuyFor) AutoCompleteTextView acTv_selectPropertyBuyFor;

    @BindView(R.id.edt_newLead_leadRemarks) TextInputEditText edt_leadRemarks;
    @BindView(R.id.mBtn_newLead_submitLead) MaterialButton mBtn_submitLead;
    @BindView(R.id.view_addLead_disableLayout) View viewDisableLayout;
    @BindView(R.id.ll_newLead_success) LinearLayoutCompat ll_success;
    @BindView(R.id.gif_newLead) GifImageView gif_newLead;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.mTv_addLead_LeadResourceNote) MaterialTextView mTv_LeadResourceNote;

    @BindView(R.id.ll_addLead_LeadSources_Dropdown) LinearLayoutCompat ll_LeadSources_Dropdown;
    @BindView(R.id.ll_addLead_LeadSources_Click) LinearLayoutCompat ll_LeadSources_Click;
    @BindView(R.id.tv_addLead_leadGen_thrw) MaterialTextView tv_leadGen_thrw;
    @BindView(R.id.iv_addLead_LeadSources_Dropdown) AppCompatImageView iv_LeadSources_Dropdown;
    @BindView(R.id.edt_addLead_fullName_referer) TextInputEditText edt_fullName_referer;
    @BindView(R.id.edt_addLead_refererMobile_no) TextInputEditText edt_refererMobile_no;
    @BindView(R.id.ll_Reference_main) LinearLayoutCompat ll_Reference_main;
    @BindView(R.id.til_newLead_refererMobile) TextInputLayout til_refererMobile;

    @BindView(R.id.ll_addLead_existUSer) LinearLayoutCompat ll_addLead_existUSer;
    @BindView(R.id.mTv_addLead_ExistProject) MaterialTextView mTv_ExistProject;
    @BindView(R.id.mTv_addLead_ExistLeadAddedBy) MaterialTextView mTv_ExistLeadAddedBy;
    @BindView(R.id.mTv_addLead_ExistUser) MaterialTextView mTv_ExistUser;
    @BindView(R.id.mTv_addLead_ExistLeadStatus) MaterialTextView mTv_ExistLeadStatus;

    @BindView(R.id.ll_addLead_existUser_OtherNo) LinearLayoutCompat ll_addLead_existUSer_OtherNo;
    @BindView(R.id.mTv_addLead_ExistProject_OtherNo) MaterialTextView mTv_ExistProject_OtherNo;
    @BindView(R.id.mTv_addLead_ExistLeadAddedBy_OtherNo) MaterialTextView mTv_ExistLeadAddedBy_OtherNo;
    @BindView(R.id.mTv_addLead_ExistUser_OtherNo) MaterialTextView mTv_ExistUser_OtherNo;
    @BindView(R.id.mTv_addLead_ExistStatus_OtherNo) MaterialTextView mTv_ExistStatus_OtherNo;

    @BindView(R.id.rb_newLead_FirstHome_no) AppCompatRadioButton rb_newLead_FirstHome_no;
    @BindView(R.id.rb_newLead_firstHome_yes) AppCompatRadioButton rb_newLead_firstHome_yes;
    @BindView(R.id.rdoGrp_newLead_firstHome) RadioGroup houseRadioGroup;

    private AppCompatActivity context;
    CustomerAdapter adapter = null;
    private Animations anim;
    private boolean viewLeadDetails =false, viewKycDocs =false, viewLeadCampaign =false, viewProjectDetails =false,
            viewOtherDetails =false, isAlreadySiteVisited = false, isRefLead = false, isLeadSubmitted = false,
            isLeadMobileVerified = false, idDocSelected=false, fromShortcut= false, isExpand =false;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_SEND_Date = "yyyy-MM-dd";
    private DatePickerDialog datePickerDialog;
    private ArrayList<EventProjectDocsModel> docsModelArrayList;
    private EventProjectDocsModel myUploadModel = null;

    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;

    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<UnitCategoriesModel> unitCategoriesModelArrayList;
    private ArrayList<IncomeRangesModel> rangesModelArrayList;
    private ArrayList<BudgetLimitModel> budgetLimitModelArrayList   ;
    private ArrayList<PropertyBuyingForModel> propertyBuyingForModelArrayList;
    private ArrayList<LeadBuyingDurationModel> leadBuyingDurationModelArrayList;
    private ArrayList<PersonNamePrefixModel> personNamePrefixModelArrayList;
    private ArrayList<UserModel> userModelArrayList;
    private ArrayList<LeadProfession> leadProfessionModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<LeadCampaignsModel> campaignsModelArrayList;
    private ArrayList<LeadGenerationModel> leadGenerationModelArrayList;
    private ArrayList<String> integerArrayList_str;
    private ArrayList<Integer> integerArrayList;
    private ArrayList<String> projectNamesArrayList,unitCategoriesArrayList, incomeRangesArrayList,budgetLimitArrayList,
            propertyBuyingArrayList, leadDurationArrayList, namePrefixArrayList, userArrayList, professionArrayList,leadGenerationStringArrayList, leadStageStringArrayList;
    //private ArrayList<Integer> catIntegerArrayList;

    private String TAG = "AddNewLeadActivity", sendBDate = null, sendPrefVisitFromDate= null, sendPrefVisitToDate= null, sendAlreadySiteVisitDate= null,
            sendAlreadySiteVisitTime = null, api_token ="", countryPhoneCode = "+91",countryPhoneCode_1 = "+91", countryPhoneCode_ref = "+91",
            selectedProjectName = "", selectedIncomeRange ="", selectedBudgetLimit ="", selectedUnitCategory ="", selectedPropertyBuyingReason ="", selectedBuyingDuration ="",
            sendNewsDate="", selectedNamePrefix ="", selectedUserName ="", lead_id=null, lead_OTP ="", selectedLeadProfessionName = "", selectedLeadStageName = "",
            sendDateOfBirth="";

    private int mYear, mMonth, mDay,nYear, nMonth, nDay,myPosition =0, selectedProjectId =0, selectedIncomeRangeId =0,selectedBudgetLimitId =0,
            selectedUnitId =0, selectedPropertyBuyingReasonId =1, selectedBuyingDurationId =0,documentCount=0,
            selectedNamePrefixId =0, selectedUserId =0, user_id =0, docAPICount=0, selectedProfessionId=0,SecLeadType_ID=0,edit_text_req_ID=0
            ,LeadType_ID=0, selectedLeadStageId=0,leadId =0, duplicate_offline_lead_id =0,person_id = 0;

    //private int fromOther = 1; //TODO fromOther ==> 1 - Add New Lead, 2- Edit/Update Lead Info
    private int FirstHomeID=0,isSuccessNumberExist=0,isSuccessNumberExistOther=0,current_lead_status_id=0;
    private String existLeadName="",existLeadProject="",existLeadAddedBy ="", existLeadStatus="",existLeadNameOtherNo="",existLeadStatusOtherNo="",existLeadProjectOtherNo="",existOtherLeadAddedBy ="",  existLeadUnitCategory="",existLeadUnitCategoryOtherNo="";
    private boolean flagNumduplicate=false,isExist_WhatsAppNo=false,isExist_OtherNo=false,isUpdate = false,isDuplicateLead = false;
    //private int check=1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean flagExit=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_lead);
        ButterKnife.bind(this);
        context = AddNewLeadActivity.this;
        //call method to hide keyBoard
        setupUI(parent);
        //anim
        anim = new Animations();

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.add_new_lead));

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
        //set tag to 0
        edt_leadOtherMobileNo.setTag(0);

        editor.apply();

        //get Intent
        if (getIntent()!=null) {
            fromShortcut = getIntent().getBooleanExtra("fromShortcut", false);
            isUpdate = getIntent().getBooleanExtra("isUpdateLead", false);
            isDuplicateLead = getIntent().getBooleanExtra("isDuplicateLead", false);
            if(getIntent().getIntExtra("lead_id",0)!= 0) leadId = getIntent().getIntExtra("lead_id",0);
            if(getIntent().getIntExtra("offline_id",0)!= 0) duplicate_offline_lead_id = getIntent().getIntExtra("offline_id",0);
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
            mTv_LeadResourceNote.setVisibility(View.GONE);
            edt_leadMobileNo.setEnabled(false);
            //TODO given alternate mobile number editable
            //edt_leadOtherMobileNo.setEnabled(false);
            ll_leadMobCcp.setVisibility(View.GONE);
            //ll_leadOtherMobCcp.setVisibility(View.GONE);

            til_leadMobile.setHelperText("Mobile Number is not editable!");
            til_leadMobile.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            //til_leadOtherMobile.setHelperText("Alternate Mobile Number is not editable!");
            //til_leadOtherMobile.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

            if (isNetworkAvailable(context)) {

                showProgressBar("Getting Lead Details...");
                getLeadDetails(leadId);

            } else NetworkError(context);

        }
        else if (isDuplicateLead) {

            if(getSupportActionBar()!= null){
                ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.update_lead));
            }
            mBtn_submitLead.setText(R.string.update_lead);
            rdoGrp_alreadySiteVisited.setVisibility(View.GONE);
            mTv_LeadResourceNote.setVisibility(isDuplicateLead ? View.VISIBLE :View.GONE);
            tv_alreadyVisitedTitle.setVisibility(View.GONE);

            selected_country_tv.setEnabled(true);
            selected_country_tv2.setEnabled(true);
            edt_leadMobileNo.setEnabled(true);
            edt_leadOtherMobileNo.setEnabled(true);
            acTv_selectLeadSource.setEnabled(false);
            acTv_selectLeadSource.setFocusable(false);
            ll_leadMobCcp.setVisibility(View.VISIBLE);
            ll_leadOtherMobCcp.setVisibility(View.VISIBLE);

            til_leadMobile.setHelperText("");
            til_leadOtherMobile.setHelperText("");

            if (isNetworkAvailable(context))
            {
                showProgressBar("Getting Lead Details...");
                getDuplicateLeadDetails(duplicate_offline_lead_id);

            }else NetworkError(context);

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
        set_radioButtons();

        //check lead mobile number //TODO removed after discussion
        //checkLeadMobile();

        //checkEmail
        checkLeadEmail();
        checkHouseType();

        // call get lead data
        if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            showProgressBar("Please wait...");
            new Handler(getMainLooper()).postDelayed(this::getLeadData, 100);
        }
        else NetworkError(context);


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

        edt_newLead_dob.setOnClickListener(v -> {
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

        edt_newLead_niReason.setVisibility(View.GONE);
        til_newLead_niReason.setVisibility(View.GONE);

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

        //initialise arrayLists
        docsModelArrayList = new ArrayList<>();
        projectModelArrayList = new ArrayList<>();
        rangesModelArrayList = new ArrayList<>();
        unitCategoriesModelArrayList = new ArrayList<>();
        budgetLimitModelArrayList = new ArrayList<>();
        propertyBuyingForModelArrayList = new ArrayList<>();
        leadBuyingDurationModelArrayList = new ArrayList<>();
        leadDurationArrayList = new ArrayList<>();
        campaignsModelArrayList = new ArrayList<>();
        personNamePrefixModelArrayList = new ArrayList<>();
        namePrefixArrayList = new ArrayList<>();
        projectNamesArrayList = new ArrayList<>();
        unitCategoriesArrayList = new ArrayList<>();
        propertyBuyingArrayList = new ArrayList<>();
        incomeRangesArrayList = new ArrayList<>();
        budgetLimitArrayList = new ArrayList<>();
        userModelArrayList = new ArrayList<>();
        userArrayList = new ArrayList<>();
        leadProfessionModelArrayList = new ArrayList<>();
        leadStagesModelArrayList = new ArrayList<>();
        professionArrayList = new ArrayList<>();
        leadGenerationStringArrayList = new ArrayList<>();
        //catIntegerArrayList = new ArrayList<>();
        //refProjectModelArrayList = new ArrayList<>();
        //refProjectNamesArrayList = new ArrayList<>();
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

    private void getLeadDetails(int lead_id)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getLeadDetails(api_token,lead_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null ) {
                        if (!response.body().isJsonNull() && response.body().isJsonObject()) {
                            int isSuccess=0;
                            if (response.body().has("success")){
                                isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            }
                            if (isSuccess == 1) {
                                if (response.body().has("data"))
                                {
                                    if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                    {
                                        JsonObject data  = response.body().get("data").getAsJsonObject();
                                        setUpdateData(data);
                                        new Handler().postDelayed(() -> checkButtonEnabled(),1000);

                                    }
                                }
                            }else{
                                showErrorLog(getString(R.string.something_went_wrong_try_again));
                            }
                            //else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setUpdateData(JsonObject jsonObject){
        if(context!=null)
        {
            runOnUiThread(() -> {
                if(jsonObject !=null){

                    if(jsonObject.has("sales_person_id")){
                        user_id = !jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0;
                    }

                    if(jsonObject.has("person_id")){
                        person_id = !jsonObject.get("person_id").isJsonNull() ? jsonObject.get("person_id").getAsInt() : 0;
                    }

                    if(jsonObject.has("full_name")){
                        edt_leadFirstName.setText(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
                    }

                    if(jsonObject.has("prefix")){
                        acTv_mrs.setText(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
                        selectedNamePrefix = !jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "";
                    }

                    if(jsonObject.has("email")){
                        edt_leadEmail.setText(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
                    }

                    if(jsonObject.has("country_code")) {
                        selected_country_tv.setText(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "+91");
                        countryPhoneCode = !jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "+91";
                    }

                    if(jsonObject.has("country_code_1")) {
                        selected_country_tv2.setText(!jsonObject.get("country_code_1").isJsonNull() ? jsonObject.get("country_code_1").getAsString() : "+91");
                        countryPhoneCode_1 = !jsonObject.get("country_code_1").isJsonNull() ? jsonObject.get("country_code_1").getAsString() : "+91";
                    }

                    if(jsonObject.has("mobile_number")){
                        edt_leadMobileNo.setText(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
                    }
                    if(jsonObject.has("alternate_mobile_number")){
                        edt_leadOtherMobileNo.setText(!jsonObject.get("alternate_mobile_number").isJsonNull() ? jsonObject.get("alternate_mobile_number").getAsString() : "" );
                    }

                    if(jsonObject.has("address_line_1")){
                        edt_leadAddress.setText(!jsonObject.get("address_line_1").isJsonNull() ? jsonObject.get("address_line_1").getAsString() : "" );
                    }

                    if(jsonObject.has("lead_profession")){
                        acTv_leadProfession.setText(!jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() : "" );
                        selectedLeadProfessionName = !jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() : "";
                    }

                    if(jsonObject.has("dob")){
                        edt_newLead_dob.setText(!jsonObject.get("dob").isJsonNull() ? formatEventDate(jsonObject.get("dob").getAsString()) : "");
                        sendDateOfBirth = !jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "" ;
                    }


                    if(jsonObject.has("lead_stage")){
                        acTv_leadStage.setText(!jsonObject.get("lead_stage").isJsonNull() ? jsonObject.get("lead_stage").getAsString() : "" );
                    }
                    if(jsonObject.has("lead_stage_id")){
                        selectedLeadStageId = !jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("income_range")){
                        acTv_selectAnnualIncome.setText(!jsonObject.get("income_range").isJsonNull() ? jsonObject.get("income_range").getAsString() : "" );
                    }
                    if(jsonObject.has("income_range_id")){
                        selectedIncomeRangeId = !jsonObject.get("income_range_id").isJsonNull() ? jsonObject.get("income_range_id").getAsInt() : 0;
                    }


                    if(jsonObject.has("project_name")){
                        acTv_selectProjectName.setText(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
                    }
                    if(jsonObject.has("project_id")){
                        selectedProjectId = !jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0;
                    }


                    if(jsonObject.has("unit_category")){
                        acTv_selectUnitType.setText(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
                    }
                    if(jsonObject.has("unit_category_id")){
                        selectedUnitId = !jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("budget_limit")){
                        acTv_selectBudgetLimit.setText(!jsonObject.get("budget_limit").isJsonNull() ? jsonObject.get("budget_limit").getAsString() : "" );
                    }
                    if(jsonObject.has("budget_limit_id")){
                        selectedBudgetLimitId = !jsonObject.get("budget_limit_id").isJsonNull() ? jsonObject.get("budget_limit_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("is_first_home")){
                        FirstHomeID = !jsonObject.get("is_first_home").isJsonNull() ? jsonObject.get("is_first_home").getAsInt() : 0;
                        if(FirstHomeID == 1){
                            rb_newLead_firstHome_yes.setChecked(true);
                        }else {
                            rb_newLead_FirstHome_no.setChecked(true);
                            FirstHomeID = 2;
                        }
                    }


                    if(jsonObject.has("lead_types_id")){
                        LeadType_ID = !jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0;
                    }
                    if(jsonObject.has("lead_type_extra_info")){
                        tv_leadGen_thrw.setText(!jsonObject.get("lead_type_extra_info").isJsonNull() ? jsonObject.get("lead_type_extra_info").getAsString() : "" );
                    }

                    if(LeadType_ID == 8){
                        ll_Reference_main.setVisibility(View.VISIBLE);
                        if(jsonObject.has("reference_name")){
                            edt_fullName_referer.setText(!jsonObject.get("reference_name").isJsonNull() ? jsonObject.get("reference_name").getAsString() : "" );
                        }
                        if(jsonObject.has("reference_mobile")){
                            edt_refererMobile_no.setText(!jsonObject.get("reference_mobile").isJsonNull() ? jsonObject.get("reference_mobile").getAsString() : "" );
                        }
                    }

                    if(jsonObject.has("lead_type_lvl2")){
                        JsonArray array = jsonObject.get("lead_type_lvl2").getAsJsonArray();
                        if(array != null){
                            integerArrayList.clear();
                            if(array.size() > 0){
                                for(int i=0;i<array.size();i++){
                                    if(!array.get(i).getAsString().trim().isEmpty()){
                                        integerArrayList.add(Integer.parseInt(array.get(i).getAsString()));
                                        Log.e(TAG,"array 1 "+integerArrayList.get(i));
                                    }
                                }
                            }
                        }
                    }

                    if(jsonObject.has("remarks")){
                        edt_leadRemarks.setText(!jsonObject.get("remarks").isJsonNull() ? jsonObject.get("remarks").getAsString() : "" );
                    }

                }

                checkButtonEnabled();
            });
        }


    }



    private void getDuplicateLeadDetails(int duplicate_offline_lead_id)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getDuplicateLeadDetails(api_token, duplicate_offline_lead_id).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null ) {
                        if (!response.body().isJsonNull() && response.body().isJsonObject()) {
                            int isSuccess=0;
                            if (response.body().has("success")){
                                isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            }
                            if (isSuccess == 1) {
                                if (response.body().has("data"))
                                {
                                    if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                    {
                                        JsonObject data  = response.body().get("data").getAsJsonObject();
                                        setUpdateDuplicateLeadDetails(data);
                                        new Handler().postDelayed(() -> checkButtonEnablesStatus(),100);

                                    }
                                }
                            }else{
                                showErrorLog(getString(R.string.something_went_wrong_try_again));
                            }
                            //else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void checkButtonEnablesStatus() {

        if(context!=null)
        {
            context.runOnUiThread(this::checkButtonEnabled);
        }
    }

    private void setUpdateDuplicateLeadDetails(JsonObject jsonObject){

        if(context!=null)
        {
            runOnUiThread(() -> {
                if(jsonObject !=null){
                    if(jsonObject.has("sales_person_id")){
                        user_id = !jsonObject.get("sales_person_id").isJsonNull() ? jsonObject.get("sales_person_id").getAsInt() : 0;
                    }

                    if(jsonObject.has("full_name")){
                        edt_leadFirstName.setText(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
                    }

                    if(jsonObject.has("prefix")){
                        acTv_mrs.setText(!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "" );
                        selectedNamePrefix = !jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString() : "";
                    }

                    if(jsonObject.has("email")){
                        edt_leadEmail.setText(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" );
                    }

                    if(jsonObject.has("country_code")) {
                        selected_country_tv.setText(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "+91");
                        countryPhoneCode = !jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "+91";
                    }

                    if(jsonObject.has("country_code_1")) {
                        selected_country_tv2.setText(!jsonObject.get("country_code_1").isJsonNull() ? jsonObject.get("country_code_1").getAsString() : "+91");
                        countryPhoneCode_1 = !jsonObject.get("country_code_1").isJsonNull() ? jsonObject.get("country_code_1").getAsString() : "+91";
                    }

                    if(jsonObject.has("mobile_number")){
                        edt_leadMobileNo.setText(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" );
                    }
                    if(jsonObject.has("alternate_mobile_number")){
                        edt_leadOtherMobileNo.setText(!jsonObject.get("alternate_mobile_number").isJsonNull() ? jsonObject.get("alternate_mobile_number").getAsString() : "" );
                    }

                    if(jsonObject.has("address_line_1")){
                        edt_leadAddress.setText(!jsonObject.get("address_line_1").isJsonNull() ? jsonObject.get("address_line_1").getAsString() : "" );
                    }


                    if(jsonObject.has("lead_profession_id")){
                        selectedProfessionId = !jsonObject.get("lead_profession_id").isJsonNull() ? jsonObject.get("lead_profession_id").getAsInt() : 0;
                    }

                    if(jsonObject.has("lead_profession")){
                        acTv_leadProfession.setText(!jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() : "" );
                        selectedLeadProfessionName = !jsonObject.get("lead_profession").isJsonNull() ? jsonObject.get("lead_profession").getAsString() : "";
                    }

                    if(jsonObject.has("dob")){
                        edt_newLead_dob.setText(!jsonObject.get("dob").isJsonNull() ? (jsonObject.get("dob").getAsString()) : "");
                        sendDateOfBirth = !jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "" ;
                    }


                    if(jsonObject.has("lead_ni_reason")){
                        edt_newLead_niReason.setText(!jsonObject.get("lead_ni_reason").isJsonNull() ? jsonObject.get("lead_ni_reason").getAsString() : "" );
                    }

                    if(jsonObject.has("lead_stage")){
                        acTv_leadStage.setText(!jsonObject.get("lead_stage").isJsonNull() ? jsonObject.get("lead_stage").getAsString() : "" );
                    }
                    if(jsonObject.has("lead_stage_id")){
                        selectedLeadStageId = !jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("income_range")){
                        acTv_selectAnnualIncome.setText(!jsonObject.get("income_range").isJsonNull() ? jsonObject.get("income_range").getAsString() : "" );
                    }
                    if(jsonObject.has("income_range_id")){
                        selectedIncomeRangeId = !jsonObject.get("income_range_id").isJsonNull() ? jsonObject.get("income_range_id").getAsInt() : 0;
                    }


                    if(jsonObject.has("project_name")){
                        acTv_selectProjectName.setText(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
                    }
                    if(jsonObject.has("project_id")){
                        selectedProjectId = !jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0;
                    }


                    if(jsonObject.has("unit_category")){
                        acTv_selectUnitType.setText(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
                    }
                    if(jsonObject.has("unit_category_id")){
                        selectedUnitId = !jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("budget_limit")){
                        acTv_selectBudgetLimit.setText(!jsonObject.get("budget_limit").isJsonNull() ? jsonObject.get("budget_limit").getAsString() : "" );
                    }
                    if(jsonObject.has("budget_limit_id")){
                        selectedBudgetLimitId = !jsonObject.get("budget_limit_id").isJsonNull() ? jsonObject.get("budget_limit_id").getAsInt() : 0;
                    }



                    if(jsonObject.has("is_first_home")){
                        FirstHomeID = !jsonObject.get("is_first_home").isJsonNull() ? jsonObject.get("is_first_home").getAsInt() : 0;
                        if(FirstHomeID == 1){
                            rb_newLead_firstHome_yes.setChecked(true);
                        }else {
                            rb_newLead_FirstHome_no.setChecked(true);
                            FirstHomeID = 2;
                        }
                    }


                    if(jsonObject.has("lead_types_id")){
                        LeadType_ID = !jsonObject.get("lead_types_id").isJsonNull() ? jsonObject.get("lead_types_id").getAsInt() : 0;
                    }
                    if(jsonObject.has("lead_types")){
                        tv_leadGen_thrw.setText(!jsonObject.get("lead_types").isJsonNull() ? jsonObject.get("lead_types").getAsString() : "" );
                    }

               /* if(LeadType_ID == 8){
                    ll_Reference_main.setVisibility(View.VISIBLE);
                    if(jsonObject.has("reference_name")){
                        edt_fullName_referer.setText(!jsonObject.get("reference_name").isJsonNull() ? jsonObject.get("reference_name").getAsString() : "" );
                    }
                    if(jsonObject.has("reference_mobile")){
                        edt_refererMobile_no.setText(!jsonObject.get("reference_mobile").isJsonNull() ? jsonObject.get("reference_mobile").getAsString() : "" );
                    }
                }

                if(jsonObject.has("lead_type_lvl2")){
                    JsonArray array = jsonObject.get("lead_type_lvl2").getAsJsonArray();
                    if(array != null){
                        integerArrayList.clear();
                        if(array.size() > 0){
                            for(int i=0;i<array.size();i++){
                                if(!array.get(i).getAsString().trim().isEmpty()){
                                    integerArrayList.add(Integer.parseInt(array.get(i).getAsString()));
                                    Log.e(TAG,"array 1 "+integerArrayList.get(i));
                                }
                            }
                        }
                    }
                }
*/
                    if(jsonObject.has("remarks")){
                        edt_leadRemarks.setText(!jsonObject.get("remarks").isJsonNull() ? jsonObject.get("remarks").getAsString() : "" );
                    }

                }
            });

        }

    }


    private void getLeadData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getLeadForm_Data(api_token,user_id);
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
                        setLeadDetails();
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonObject()) {
                                                JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                                setJson(jsonObject);
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

    private void setJson(JsonObject jsonObject)
    {
      /*  //get ref projects
        if (jsonObject.has("lead_ref_types"))
        {
            if (!jsonObject.get("lead_ref_types").isJsonNull() && jsonObject.get("lead_ref_types").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_ref_types").getAsJsonArray();
                //clear list
                refProjectModelArrayList.clear();
                refProjectNamesArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setRefProjectNamesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }*/

        // get projects array
        if (jsonObject.has("ref_projects")) {
            if (!jsonObject.get("ref_projects").isJsonNull() && jsonObject.get("ref_projects").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("ref_projects").getAsJsonArray();
                //clear list
                projectModelArrayList.clear();
                projectNamesArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setProjectNamesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

        // get income ranges array
        if (jsonObject.has("income_range_types"))
        {
            if (!jsonObject.get("income_range_types").isJsonNull() && jsonObject.get("income_range_types").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("income_range_types").getAsJsonArray();
                //clear list
                rangesModelArrayList.clear();
                incomeRangesArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setIncomeRangeJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

        // get income ranges array
        if (jsonObject.has("unit_categories"))
        {
            if (!jsonObject.get("unit_categories").isJsonNull() && jsonObject.get("unit_categories").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("unit_categories").getAsJsonArray();
                //clear list
                unitCategoriesModelArrayList.clear();
                unitCategoriesArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setUnitCategoriesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }


        // get income ranges array
        if (jsonObject.has("budget_limit_types"))
        {
            if (!jsonObject.get("budget_limit_types").isJsonNull() && jsonObject.get("budget_limit_types").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("budget_limit_types").getAsJsonArray();
                //clear list
                budgetLimitModelArrayList.clear();
                budgetLimitArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setBudgetLimitJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }



        // get lead_campaigns array
        if (jsonObject.has("lead_campaigns"))
        {
            if (!jsonObject.get("lead_campaigns").isJsonNull() && jsonObject.get("lead_campaigns").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_campaigns").getAsJsonArray();
                //clear list
                campaignsModelArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setLeadCampaignJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

        // get lead_campaigns array
        if (jsonObject.has("lead_types"))
        {
            if (!jsonObject.get("lead_types").isJsonNull() && jsonObject.get("lead_types").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_types").getAsJsonArray();

                //clear list
                leadGenerationModelArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setLeadResources(jsonArray.get(i).getAsJsonObject());
                }
            }
        }


        // get person name prefix array
        if (jsonObject.has("namePrefix"))
        {
            if (!jsonObject.get("namePrefix").isJsonNull() && jsonObject.get("namePrefix").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("namePrefix").getAsJsonArray();
                //clear list
                personNamePrefixModelArrayList.clear();
                namePrefixArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setNamePrefixJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }


        // get sales users array
        if (jsonObject.has("all_sales_users"))
        {
            if (!jsonObject.get("all_sales_users").isJsonNull() && jsonObject.get("all_sales_users").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("all_sales_users").getAsJsonArray();
                //clear list
                userModelArrayList.clear();
                userArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setUsersModelJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

        // get lead professions array
        if (jsonObject.has("professions"))
        {
            if (!jsonObject.get("professions").isJsonNull() && jsonObject.get("professions").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("professions").getAsJsonArray();
                //clear list
                leadProfessionModelArrayList.clear();
                professionArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setLeadProfessionJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

        // get lead stages array
        if (jsonObject.has("lead_stages"))
        {
            if (!jsonObject.get("lead_stages").isJsonNull() && jsonObject.get("lead_stages").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("lead_stages").getAsJsonArray();
                //clear list
                leadStagesModelArrayList.clear();
                leadStageStringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setLeadStagesJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }


        // get kyc docs array
        if (jsonObject.has("kyc_doc_types"))
        {
            if (!jsonObject.get("kyc_doc_types").isJsonNull() && jsonObject.get("kyc_doc_types").isJsonArray())
            {
                JsonArray jsonArray  = jsonObject.get("kyc_doc_types").getAsJsonArray();
                //clear list
                docsModelArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setKYCDocsJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }

    }

 /*   private void setRefProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("lead_ref_type_id")) model.setProject_id(!jsonObject.get("lead_ref_type_id").isJsonNull() ? jsonObject.get("lead_ref_type_id").getAsInt() : 0 );
        if (jsonObject.has("lead_ref_type"))
        {
            model.setProject_name(!jsonObject.get("lead_ref_type").isJsonNull() ? jsonObject.get("lead_ref_type").getAsString() : "" );
            refProjectNamesArrayList.add(!jsonObject.get("lead_ref_type").isJsonNull() ? jsonObject.get("lead_ref_type").getAsString() : "" );
        }
        refProjectModelArrayList.add(model);
    }*/

    private void setProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectNamesArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }

        /*if (jsonObject.has("unit_categories"))
        {
            if (!jsonObject.get("unit_categories").isJsonNull() && jsonObject.get("unit_categories").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("unit_categories").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<UnitCategoriesModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    unitCategoriesArrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setUnitCategoryJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setCategoriesModelArrayList(arrayList);
                }
            }
        }*/

        projectModelArrayList.add(model);
    }

   /* private void setUnitCategoryJson(JsonObject jsonObject, ArrayList<UnitCategoriesModel> arrayList)
    {

        UnitCategoriesModel myModel = new UnitCategoriesModel();
        if (jsonObject.has("unit_category_id")) myModel.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) myModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );

        arrayList.add(myModel);
    }*/

    private void setIncomeRangeJson(JsonObject jsonObject)
    {
        IncomeRangesModel myModel = new IncomeRangesModel();
        if (jsonObject.has("income_range_id")) myModel.setIncome_range_id(!jsonObject.get("income_range_id").isJsonNull() ? jsonObject.get("income_range_id").getAsInt() : 0 );
        if (jsonObject.has("type_name"))
        {
            myModel.setIncome_range(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
            incomeRangesArrayList.add(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
        }

        rangesModelArrayList.add(myModel);
    }
    private void setUnitCategoriesJson(JsonObject jsonObject)
    {
        UnitCategoriesModel myModel = new UnitCategoriesModel();
        if (jsonObject.has("unit_category_id")) myModel.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category"))
        {
            myModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
            unitCategoriesArrayList.add(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );
        }

        unitCategoriesModelArrayList.add(myModel);
    }

    private void setBudgetLimitJson(JsonObject jsonObject)
    {
        BudgetLimitModel myModel = new BudgetLimitModel();
        if (jsonObject.has("budget_limit_id")) myModel.setBudget_limit_id(!jsonObject.get("budget_limit_id").isJsonNull() ? jsonObject.get("budget_limit_id").getAsInt() : 0 );
        if (jsonObject.has("type_name"))
        {
            myModel.setBudget_limit(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
            budgetLimitArrayList.add(!jsonObject.get("type_name").isJsonNull() ? jsonObject.get("type_name").getAsString() : "" );
        }

        budgetLimitModelArrayList.add(myModel);
    }


    private void setNamePrefixJson(JsonObject jsonObject)
    {
        PersonNamePrefixModel myModel = new PersonNamePrefixModel();
        if (jsonObject.has("name_prefix_id")) myModel.setName_prefix_id(!jsonObject.get("name_prefix_id").isJsonNull() ? jsonObject.get("name_prefix_id").getAsInt() : 0 );
        if (jsonObject.has("name_prefix"))
        {
            myModel.setName_prefix(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
            namePrefixArrayList.add(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
        }

        personNamePrefixModelArrayList.add(myModel);
    }

    private void setUsersModelJson(JsonObject jsonObject)
    {
        UserModel myModel = new UserModel();
        if (jsonObject.has("user_id")) myModel.setUser_id(!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0 );
        if (jsonObject.has("full_name"))
        {
            myModel.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
            userArrayList.add(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" );
        }

        userModelArrayList.add(myModel);
    }

    private void setLeadProfessionJson(JsonObject jsonObject)
    {
        LeadProfession myModel = new LeadProfession();
        if (jsonObject.has("professions_id")) myModel.setLead_profession_id(!jsonObject.get("professions_id").isJsonNull() ? jsonObject.get("professions_id").getAsInt() : 0 );
        if (jsonObject.has("profession"))
        {
            myModel.setLead_profession(!jsonObject.get("profession").isJsonNull() ? jsonObject.get("profession").getAsString() : "" );
            professionArrayList.add(!jsonObject.get("profession").isJsonNull() ? jsonObject.get("profession").getAsString() : "" );
        }

        leadProfessionModelArrayList.add(myModel);
    }

    private void setLeadStagesJson(JsonObject jsonObject) {
        LeadStagesModel myModel = new LeadStagesModel();
        if (jsonObject.has("lead_stage_id")) myModel.setLead_stage_id(!jsonObject.get("lead_stage_id").isJsonNull() ? jsonObject.get("lead_stage_id").getAsInt() : 0 );
        if (jsonObject.has("lead_stage_name")) {
            myModel.setLead_stage_name(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
            leadStageStringArrayList.add(!jsonObject.get("lead_stage_name").isJsonNull() ? jsonObject.get("lead_stage_name").getAsString() : "" );
        }

        leadStagesModelArrayList.add(myModel);
    }

    private void setKYCDocsJson(JsonObject jsonObject)
    {
        EventProjectDocsModel myModel = new EventProjectDocsModel();
        if (jsonObject.has("doc_type_id")) myModel.setDoc_type_id(!jsonObject.get("doc_type_id").isJsonNull() ? jsonObject.get("doc_type_id").getAsInt() : 0 );
        if (jsonObject.has("docs_type")) myModel.setDocType(!jsonObject.get("docs_type").isJsonNull() ? jsonObject.get("docs_type").getAsString() : "" );

        docsModelArrayList.add(myModel);
    }

    private void setLeadResources(JsonObject jsonObject)
    {
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


    private void setLeadCampaignJson(JsonObject jsonObject)
    {
        LeadCampaignsModel myModel = new LeadCampaignsModel();
        if (jsonObject.has("lead_campaign_details_id")) myModel.setLead_campaign_details_id(!jsonObject.get("lead_campaign_details_id").isJsonNull() ? jsonObject.get("lead_campaign_details_id").getAsInt() : 0 );
        if (jsonObject.has("lead_campaign_type")) myModel.setLead_campaign_type(!jsonObject.get("lead_campaign_type").isJsonNull() ? jsonObject.get("lead_campaign_type").getAsString() : "" );
        // if (jsonObject.has("lead_campaign_details_description")) myModel.setLead_campaign_details_description(!jsonObject.get("lead_campaign_details_description").isJsonNull() ? jsonObject.get("lead_campaign_details_description").getAsString() : "" );

        if (jsonObject.has("leads_campaign_details"))
        {
            if (!jsonObject.get("leads_campaign_details").isJsonNull() && jsonObject.get("leads_campaign_details").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("leads_campaign_details").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<LeadsCampaignDetailsModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setLeadCampaignDetailsJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    myModel.setCampaignDetailsModelArrayList(arrayList);
                }
            }
        }

        campaignsModelArrayList.add(myModel);
    }

    private void setLeadCampaignDetailsJson(JsonObject jsonObject, ArrayList<LeadsCampaignDetailsModel> arrayList)
    {
        LeadsCampaignDetailsModel myModel = new LeadsCampaignDetailsModel();
        if (jsonObject.has("lead_campaign_details_id")) myModel.setLead_campaign_details_id(!jsonObject.get("lead_campaign_details_id").isJsonNull() ? jsonObject.get("lead_campaign_details_id").getAsInt() : 0 );
        if (jsonObject.has("lead_campaign_details_description")) myModel.setLead_campaign_details_description(!jsonObject.get("lead_campaign_details_description").isJsonNull() ? jsonObject.get("lead_campaign_details_description").getAsString() : "" );
        arrayList.add(myModel);
    }


    private void setLeadDetails()
    {

        runOnUiThread(() -> {

            hideProgressBar();

            //set adapter for ref project name
            setAdapterProjectNames();

            //set adapter for Unit Categories
            setAdapterUnitCategories();

            //set adapter for income range
            setAdapterIncomeRanges();

            //set adapter for budget limit
            setAdapterBudgetLimits();

            //set adapter for property buy
            setAdapterPropertyBuying();

            //set adapter for property duration
            setAdapterPropertyBuyingDuration();

            //set adapter for name prefix
            setAdapterNamePrefix();

            //set adapter for conducted by users
            setAdapterConductedByUsers();

            //set kyc documents
            setKYCDocuments();

            //set lead campaigns
            setLeadCampaigns();

            //set adapter for lead profession
            setAdapterLeadProfession();

            //set adapter for lead stage
            setAdapterLeadStages();

            //set adapter for lead sources
            // setAdapterLeadSources();

            //set adapter for lead resources
            setAdapterLeadResources();

        });
    }

    private void setAdapterLeadResources() {

        if (leadGenerationModelArrayList!=null && leadGenerationModelArrayList.size()>0 )
        {
//            tv_addEnquiry_leadGen_thrw.setText(generationModelArrayList.get(9).getType_name());
//            LeadType_ID = generationModelArrayList.get(9).getLead_type_id();

            ll_LeadSources_Dropdown.removeAllViews();
            for (int i = 0; i< leadGenerationModelArrayList.size(); i++)
            {
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
        SecLeadType_ID=SecLvlID;
        final int edit_text_req= leadGenerationModelArrayList.get(position).getEdit_text_req();
        edit_text_req_ID=edit_text_req;
        final String type_name= leadGenerationModelArrayList.get(position).getType_name();


        //lead click listener
        subView.setOnClickListener(v ->
        {
            integerArrayList.clear();

            //int typeId= leadGenerationModelArrayList.get(position).getLead_type_id();
            LeadType_ID = leadGenerationModelArrayList.get(position).getLead_type_id();
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

    /*   private void setAdapterRefProjectNames()
       {

           if (refProjectNamesArrayList.size() >0 &&  refProjectModelArrayList.size()>0)
           {
               //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
               ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, refProjectNamesArrayList);
               acTv_selectRefProject.setAdapter(adapter);
               acTv_selectRefProject.setThreshold(0);
               //tv_selectCustomer.setSelection(0);
               //autoComplete_firmName.setValidator(new Validator());
               //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

               acTv_selectRefProject.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
               {

                   String itemName = adapter.getItem(position);
                   for (ProjectModel pojo : refProjectModelArrayList)
                   {
                       if (pojo.getProject_name().equals(itemName))
                       {
                           //int id = pojo.getCompany_id(); // This is the correct ID
                           selectedRefProjectId = pojo.getProject_id(); // This is the correct ID
                           selectedRefProjectName = pojo.getProject_name();

                           //fixedEnquiryID+=2;
                           Log.e(TAG, "Ref Project name & id " + selectedRefProjectName +"\t"+ selectedRefProjectId);

                           //check button EnabledView
                           checkButtonEnabled();

                           break; // No need to keep looping once you found it.
                       }
                   }
               });

           }
       }
   */
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

    private void setAdapterPropertyBuying()
    {

        if (propertyBuyingArrayList.size()>0 && propertyBuyingForModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, propertyBuyingArrayList);
            acTv_selectPropertyBuyFor.setAdapter(adapter);
            acTv_selectPropertyBuyFor.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_selectPropertyBuyFor.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (PropertyBuyingForModel pojo : propertyBuyingForModelArrayList)
                {
                    if (pojo.getBuying_for_reason().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedPropertyBuyingReasonId = pojo.getBuying_for_reason_id(); // This is the correct ID
                        selectedPropertyBuyingReason = pojo.getBuying_for_reason();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Property Buying Reason & id " + selectedPropertyBuyingReason +"\t"+ selectedPropertyBuyingReasonId);

                        //set visibility of property buying for other
                        edt_otherPropertyBuyPurpose.setVisibility(itemName.equalsIgnoreCase(getString(R.string.other)) ? View.VISIBLE : View.GONE);

                        break; // No need to keep looping once you found it.
                    }
                }

            });

        }


    }

    private void setAdapterPropertyBuyingDuration()
    {

        if (leadDurationArrayList.size()>0 && leadBuyingDurationModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, leadDurationArrayList);
            acTv_selectWhenToBuy.setAdapter(adapter);
            acTv_selectWhenToBuy.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectWhenToBuy.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (LeadBuyingDurationModel pojo : leadBuyingDurationModelArrayList)
                {
                    if (pojo.getBuying_in_duration().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedBuyingDurationId = pojo.getBuying_in_duration_id(); // This is the correct ID
                        selectedBuyingDuration = pojo.getBuying_in_duration();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Buying & id " + selectedBuyingDuration +"\t"+ selectedBuyingDurationId);


                        break; // No need to keep looping once you found it.
                    }
                }

            });

        }

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

    private void setAdapterConductedByUsers()
    {

        if (userArrayList.size() > 0 &&  userModelArrayList.size()> 0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, userArrayList);
            acTv_selectVisitConductedBy.setAdapter(adapter);
            acTv_selectVisitConductedBy.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_selectVisitConductedBy.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (UserModel pojo : userModelArrayList)
                {
                    if (pojo.getFull_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedUserId = pojo.getUser_id(); // This is the correct ID
                        selectedUserName = pojo.getFull_name();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Conducted By Name & id " + selectedUserName +"\t"+ selectedUserId);

                        //check button EnabledView
                        checkButtonEnabled();

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

                        edt_newLead_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);
                        til_newLead_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Status name & id " + selectedLeadStageName +"\t"+ selectedLeadStageId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }


                }
            });
        /*    acTv_leadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                for (LeadStagesModel pojo : leadStagesModelArrayList)
                {
                    if (pojo.getLead_stage_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadStageId = pojo.getLead_stage_id(); // This is the correct ID
                        selectedLeadStageName = pojo.getLead_stage_name();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Stage name & id " + selectedLeadStageName +"\t"+ selectedLeadStageId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
*/
        }
    }

  /*  private void setAdapterLeadSources()
    {

        if (leadGenerationStringArrayList.size() >0 &&  leadGenerationModelArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadGenerationStringArrayList);
            acTv_selectLeadSource.setAdapter(adapter);
            acTv_selectLeadSource.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_selectLeadSource.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {

                String itemName = adapter.getItem(position);
                for (LeadGenerationModel pojo : leadGenerationModelArrayList) {
                    if (pojo.getType_name().equals(itemName)) {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadSourceId = pojo.getLead_type_id(); // This is the correct ID
                        selectedLeadSourceName = pojo.getType_name();



                        final int SecLvlID = leadGenerationModelArrayList.get(position).getSecLvl();
                        SecLeadType_ID = SecLvlID;
                        final int edit_text_req = leadGenerationModelArrayList.get(position).getEdit_text_req();
                        edit_text_req_ID = edit_text_req;
                        final String type_name = leadGenerationModelArrayList.get(position).getType_name();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Source name & id " + selectedLeadSourceName + "\t" + selectedLeadSourceId);


                        final int typeId = leadGenerationModelArrayList.get(position).getLead_type_id();
                        LeadType_ID = typeId;
                        Log.e(TAG, "LeadType_ID: " + LeadType_ID);

                        if (SecLvlID == 1 && edit_text_req == 2)
                        {
                            //select only chips ==> flow layout only
                            //    iv_AddEnq_LeadGen_.setImageResource(R.drawable.down_arrow);
                            //ll_Reference_main.setVisibility(View.GONE);
                            integerArrayList_str.clear();

                            showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(), edit_text_req);
                            // flagClick1=true;
                        } else if (edit_text_req == 1 && SecLvlID == 2) {
                            //select only edt ==> edt only
                            ll_Reference_main.setVisibility(View.GONE);
                            integerArrayList_str.clear();
                            showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(), edit_text_req);
                            // flagClick1=true;
                        } else if (edit_text_req == 1 && SecLvlID == 1) {
                            //select both  chips & edt  ==> flow layout & edt
                            ll_Reference_main.setVisibility(View.GONE);
                            integerArrayList_str.clear();
                            showSubmitLeadsAlertDialog(SecLvlID, position, leadGenerationModelArrayList.get(position).getEdit_text_title(), edit_text_req);
                            //  flagClick1=true;
                        } else if (type_name.equals("Reference")) {

                            //ref sep layout add
                            integerArrayList_str.clear();
                            // tv_addEnquiry_leadGen_thrw.setText(leadGenerationModelArrayList.get(position).getType_name());
                            // ll__AddEnq_LeadGen_Dropdown.setVisibility(View.GONE);
                            //    iv_AddEnq_LeadGen_.setImageResource(R.drawable.down_arrow);
                            ll_Reference_main.setVisibility(View.VISIBLE);
                            //  flagClick1=true;
                        } else {
                            //direct set text
                            integerArrayList_str.clear();
                            ll_Reference_main.setVisibility(View.GONE);
                            //   tv_addEnquiry_leadGen_thrw.setText(leadGenerationModelArrayList.get(position).getType_name());
                            //ll__AddEnq_LeadGen_Dropdown.setVisibility(View.GONE);
                            // iv_AddEnq_LeadGen_.setImageResource(R.drawable.down_arrow);
                            //  flagClick1=true;
                        }


                        break; // No need to keep looping once you found it.
                    }
                }

            });
            //check button EnabledView
            //checkButtonEnabled();


        }

    }*/

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


        String edt_LeadValue = "";
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
                        // getIntegerArraynm().clear();
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
            edt_LeadValue = edt_addEnquiry_other_news.getText().toString();
            Log.e(TAG, "news: "+ edt_LeadValue);


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
            edt_LeadValue = Objects.requireNonNull(edt_addEnquiry_other.getText()).toString();
            Log.e(TAG, "showAlert_LeadGenAlert: "+ edt_LeadValue);
            ll_lead_main1.setVisibility(View.VISIBLE);
            iv_LeadSources_Dropdown.setImageResource(R.drawable.ic_up_arrow_drop_up_24);
        }

        tv_leadGen_thrw.setText("");
        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (isNetworkAvailable(context))
            {
                if(!Objects.requireNonNull(edt_addEnquiry_other.getText()).toString().trim().isEmpty())
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

                /*final StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("");
                strBuffer.append( edt_addEnquiry_other.getText().toString());
                if(strBuffer!=null)
                {
                    tv_addEnquiry_leadGen_thrw.setText("");
                    tv_addEnquiry_leadGen_thrw.setText(strBuffer.toString());
                    LeadGeneration_otherType_info=edt_addEnquiry_other.getText().toString();
                    Log.e("TAG", strBuffer.toString());
                    alertDialog_filter.dismiss();
                    ll__AddEnq_LeadGen_Dropdown.setVisibility(View.GONE);
                }*/
                }
                else if(!Objects.requireNonNull(edt_addEnquiry_other_news.getText()).toString().trim().isEmpty())
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



               /* final StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("");
                strBuffer.append( edt_addEnquiry_other_news.getText().toString());
                if(strBuffer!=null)
                {
                    tv_addEnquiry_leadGen_thrw.setText("");
                    tv_addEnquiry_leadGen_thrw.setText(strBuffer.toString());
                    LeadGeneration_otherType_info=edt_addEnquiry_other.getText().toString();
                    Log.e("TAG", strBuffer.toString());
                    alertDialog_filter.dismiss();
                    ll__AddEnq_LeadGen_Dropdown.setVisibility(View.GONE);
                }*/

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



    public void addPostDoc()
    {
        if (docsModelArrayList!=null && docsModelArrayList.size()>0)
        {
            //call upload method
            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                for (int i =0; i<docsModelArrayList.size(); i++)
                {

                    if (docsModelArrayList.get(i).getDocPath()!=null && !docsModelArrayList.get(i).getDocPath().isEmpty() )
                    {
                        //ll_pb.setVisibility(View.VISIBLE);
                        //showProgressBar(getString(R.string.posting_doc)+" "+docsModelArrayList.get(i).getDocType()+"...");
                        showProgressBar(getString(R.string.posting_doc)+" KYC documents...");
                        add_KYCDocument(docsModelArrayList.get(i).getDocPath(), docsModelArrayList.get(i).getDoc_type_id());
                    }

                }

            }
            else NetworkError(context);
        }
    }


    private void add_KYCDocument(String filePath,int doc_id)
    {
        if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            File docFile = new File(filePath);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), docFile);
            MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file_uri", docFile.getName(), uploadFile);

            RequestBody api_token_ = RequestBody.create(MediaType.parse("text/plain"),api_token);
            RequestBody doc_type_id = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(doc_id));
            RequestBody lead_id_ = RequestBody.create(MediaType.parse("text/plain"),lead_id);

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
                                /*f (response.body().has("data"))
                                {
                                    if (response.body().get("data").isJsonArray())
                                    {
                                        JsonArray jsonArray  = response.body().get("data").getAsJsonArray();
                                        docsListModelArrayList.clear();
                                        for(int i=0;i<jsonArray.size();i++)
                                        {
                                            JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();
                                            setJson(jsonObject);
                                        }
                                    }
                                }*/

                                //set view
                                // setCategoryView();
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
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t)
                {
                    Log.e(TAG, "onError: " + t.toString());
                    showErrorLog(t.toString());
                }
            });



        }
        else NetworkError(context);
    }

    private void onDocumentUpload()
    {
        runOnUiThread(() -> {

                    if(documentCount==docAPICount)
                    {
                        Log.e(TAG, "documentCount: "+documentCount );
                        Log.e(TAG, "docAPICount: "+docAPICount );
                        hideProgressBar();
                        showSuccessAlert();
                    }
                }
        );
    }


    private void setKYCDocuments()
    {
        if (docsModelArrayList.size()>0)
        {
            ll_addKYCDoc.removeAllViews();
            for (int i =0 ; i< docsModelArrayList.size(); i++)
            {
                View rowView_sub = getDocumentsView(i);
                ll_addKYCDoc.addView(rowView_sub);
            }

        }
    }


    private View getDocumentsView(final int position)
    {

        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_kyc_documents, null );

        final AppCompatTextView tv_kyc_doc_name = rowView_sub.findViewById(R.id.tv_kyc_doc_name);
        final AppCompatTextView tv_kyc_doc_file = rowView_sub.findViewById(R.id.tv_kyc_doc_select_file);
        final AppCompatImageView iv_browse = rowView_sub.findViewById(R.id.iv_kyc_doc_browse);

        tv_kyc_doc_name.setText(docsModelArrayList.get(position).getDocType());
        tv_kyc_doc_name.setText(docsModelArrayList.get(position).getIsRequired()==1 ? Html.fromHtml(docsModelArrayList.get(position).getDocType()+"<font color='#b42726'><b>*</b></font>") : docsModelArrayList.get(position).getDocType());
        tv_kyc_doc_file.setText(docsModelArrayList.get(position).getDocPath()!=null ? getFileName_from_filePath(docsModelArrayList.get(position).getDocPath()) : getString(R.string.no_file_choose));

        iv_browse.setOnClickListener(view -> {

            // browse
            myUploadModel = docsModelArrayList.get(position);
            myPosition = position;
            Log.e(TAG, "onClick: pos & id "+ position+ " "+myUploadModel.getDoc_type_id());
            selectDocumentPopup();

        });


        tv_kyc_doc_name.setOnClickListener(view -> {


        });

        return rowView_sub;
    }


    private void setLeadCampaigns()
    {
        if (campaignsModelArrayList.size() >0)
        {
            ll_addLeadCampaign.removeAllViews();
            for (int i =0 ; i< campaignsModelArrayList.size(); i++)
            {
                View rowView_sub = getLeadCampaignView(i);
                ll_addLeadCampaign.addView(rowView_sub);
            }
        }
    }



    private View getLeadCampaignView(final int position)
    {

        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_layout_lead_campaign, null );

        final LinearLayoutCompat ll_itemLeadCampaignMain = rowView.findViewById(R.id.ll_itemLeadCampaignMain);
        final AppCompatTextView tv_itemLeadCampaign_type = rowView.findViewById(R.id.tv_itemLeadCampaign_type);
        final FlowLayout flowLayout = rowView.findViewById(R.id.flowLayout_leadCampaignDetails);

        final LeadCampaignsModel myModel = campaignsModelArrayList.get(position);
        tv_itemLeadCampaign_type.setText(myModel.getLead_campaign_type());

        final ArrayList<LeadsCampaignDetailsModel> arrayList = myModel.getCampaignDetailsModelArrayList();
        if (arrayList!=null && arrayList.size()>0)
        {
            flowLayout.removeAllViews();
            for (int i=0; i<arrayList.size(); i++)
            {

                @SuppressLint("InflateParams") View rowView_sub = LayoutInflater.from(context).inflate(R.layout.item_layout_lead_campaign_detail, null );
                //final Chip chip = rowView_sub.findViewById(R.id.chip_leadCampaignDetail);
                final AppCompatTextView tv_leadCampaignDetails = rowView_sub.findViewById(R.id.tv_leadCampaignDetails);
                tv_leadCampaignDetails.setText(arrayList.get(i).getLead_campaign_details_description());

                if (myModel.getCampaignDetailsModelArrayList().get(i).getIsSelected()==1)
                {
                    //show already selected

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected,context.getTheme()));
                        //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.main_dark_grey,context.getTheme()));
                        tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                    }else {

                        tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected));
                        //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.main_dark_grey));
                        tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_white));
                    }

                }
                else
                {
                    //show already deSelected
                    //chip.setChecked(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                        //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.light_grey,context.getTheme()));
                        tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_black,context.getTheme()));

                    }else {

                        tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                        //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                        tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_black));
                    }

                }


                final int finalI = i;
                rowView_sub.setOnClickListener(v -> {

                    if (myModel.getCampaignDetailsModelArrayList().get(finalI).getIsSelected()==1)
                    {
                        //already select -- do deselect
                        //chip.setChecked(false);


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect,context.getTheme()));
                            //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.light_grey,context.getTheme()));
                            tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_black,context.getTheme()));

                        }else {

                            tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_deselect));
                            //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                            tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_black));
                        }

                        //set deSelected
                        myModel.getCampaignDetailsModelArrayList().get(finalI).setIsSelected(0);
                        checkInsertRemoveSubCat(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_id(), false);
                        //checkInsertRemoveSubCat(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_description(), false);

                    }
                    else
                    {
                        //no selected -- do select
                        //chip.setChecked(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected,context.getTheme()));
                            //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.main_dark_grey,context.getTheme()));
                            tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_white,context.getTheme()));

                        }else {

                            tv_leadCampaignDetails.setBackground(context.getResources().getDrawable(R.drawable.ripple_cat_selected));
                            //tv_leadCampaignDetails.setBackgroundColor(context.getResources().getColor(R.color.main_dark_grey));
                            tv_leadCampaignDetails.setTextColor(context.getResources().getColor(R.color.main_white));
                        }

                        //set selected
                        myModel.getCampaignDetailsModelArrayList().get(finalI).setIsSelected(1);
                        checkInsertRemoveSubCat(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_id(), true);
                        //checkInsertRemoveSubCatName(myModel.getCampaignDetailsModelArrayList().get(finalI).getLead_campaign_details_description(), true);
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
            tv_noCat.setText(getText(R.string.no_lead_campaigns_available));
            flowLayout.addView(rowView_sub);
        }


        /*flowLayout.setVisibility(View.GONE);
        if (!myModel.isExpanded())
        {

            flowLayout.setVisibility(View.GONE);
            //  collapsesubview( holder.linear_subview2, holder.linear_search_devlopment_format2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                ll_cat_main.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_dark_grey_more_round,context.getTheme()));
            }else {
                ll_cat_main.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_dark_grey_more_round));
            }

        }
        else
        {
            anim.toggleRotate(iv_arrow, true);
            expandSubView( flowLayout,0,ll_cat_main,  tv_catNumber);
            flowLayout.setVisibility(View.VISIBLE);
        }*/



        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
        ll_itemLeadCampaignMain.setLayoutParams(params);

        //set Animation to the layout
        //setAnimation(ll_itemLeadCampaignMain, position);

        return rowView;
    }


    private void checkLeadEmail()
    {
        edt_leadEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!Objects.requireNonNull(edt_leadEmail.getText()).toString().isEmpty() && !isValidEmail(edt_leadEmail)) {
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
        edt_leadMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!isUpdate){
                    //check=1;
                    if(Objects.requireNonNull(edt_leadMobileNo.getText()).toString().length()>9) {

                        String mob_1 = Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim();
                        String mob_2 = Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().trim();
                        if (sameNumber(mob_1, mob_2, edt_leadMobileNo)) {
                            new Helper().showCustomToast(context, "Mobile Numbers is same as Alternative Mobile Number!");
                        }
                        else {
                            showProgressBar("Checking Mobile Number....");
                            String mobileNumber=edt_leadMobileNo.getText().toString();
                            checkMobileNumberExistWhatsApp(mobileNumber,true);
                        }
                        //sameNumber(edt_leadMobileNo.getText().toString(), Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
                    }
                    else ll_addLead_existUSer.setVisibility(View.GONE);
                }
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

                Integer selections = (Integer) edt_leadOtherMobileNo.getTag();
                if (selections > 0) {
                    //check=1;
                    if(Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().length()>9) {

                        String mob_1 = Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString().trim();
                        String mob_2 = Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim();
                        if (sameNumber(mob_1, mob_2, edt_leadOtherMobileNo)) {
                            new Helper().showCustomToast(context, "Alternative Mobile Numbers is same as Primary Mobile Number!");
                        }
                        else {
                            showProgressBar("Checking Mobile Number....");
                            String mobileNumber= Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString();
                            checkMobileNumberExistOtherNo(mobileNumber, false);
                        }
                        //sameNumber(edt_leadMobileNo.getText().toString(), Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
                    }
                    else ll_addLead_existUSer_OtherNo.setVisibility(View.GONE);

                }
                edt_leadOtherMobileNo.setTag(++selections); // (or even just '1')
                //checkButtonEnabled
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



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


        if (!isDuplicateLead) {
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

    }

    private void checkMobileNumberExistWhatsApp(String mobileNumber, boolean fromWhatsAppNo)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().checkMobileNumberExistWhatsApp(api_token,mobileNumber).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null ) {
                        if (!response.body().isJsonNull() && response.body().isJsonObject()) {

                            if (response.body().has("success")) isSuccessNumberExist = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccessNumberExist == 0) {
                                if (response.body().has("data"))
                                {
                                    if (!response.body().get("data").isJsonNull())
                                    {
                                        isExist_WhatsAppNo=true;
                                        JsonObject data  = response.body().get("data").getAsJsonObject();
                                        setJsonIfUserExistWhatsApp(data);
                                        onSuccessAdd(fromWhatsAppNo);
                                    }
                                }
                            }
                            else if (isSuccessNumberExist ==1) {
                                isExist_WhatsAppNo=false;
                                new Handler().postDelayed(() -> new Helper().showCustomToast(context, "New Number!"), 1000);
                                hidePB();
                            }
                            //else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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



    private void checkMobileNumberExistOtherNo(String mobileNumber, boolean fromWhatsAppNo)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().checkMobileNumberExistOther(api_token,mobileNumber).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull() && response.body().isJsonObject()) {
                            if (response.body().has("success")) isSuccessNumberExistOther = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccessNumberExistOther == 0) {
                                if (response.body().has("data")) {

                                    if (!response.body().get("data").isJsonNull()) {
                                        isExist_OtherNo=true;
                                        JsonObject data  = response.body().get("data").getAsJsonObject();
                                        setJsonIfUserExist(data);
                                        onSuccessAdd(fromWhatsAppNo);
                                    }
                                }
                            }
                            else if (isSuccessNumberExistOther ==1) {
                                isExist_OtherNo=false;
                                new Handler().postDelayed(() -> new Helper().showCustomToast(context, "New Number!"), 1000);
                                hidePB();
                            }
                            //else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void hidePB() { runOnUiThread(() -> {
        hideProgressBar();
        //checkButtonEnabled
        checkButtonEnabled();
    });}

    private void setJsonIfUserExistWhatsApp(JsonObject jsonObject) {

        //if (jsonObject.has("lead_id")) model.setUser_id((!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0));
        if (jsonObject.has("unit_category")) existLeadUnitCategory = !jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "";
        if (jsonObject.has("project_name")) existLeadProject = !jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "";
        if (jsonObject.has("sales_person_name")) existLeadAddedBy = !jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "";
        if (jsonObject.has("full_name")) existLeadName = !jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "";
        if (jsonObject.has("lead_status_name")) existLeadStatus = !jsonObject.get("lead_status_name").isJsonNull() ? jsonObject.get("lead_status_name").getAsString() : "";

    }

    private void setJsonIfUserExist(JsonObject jsonObject) {

        //if (jsonObject.has("lead_id")) model.setUser_id((!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0));
        if (jsonObject.has("unit_category")) existLeadUnitCategoryOtherNo=!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "";
        if (jsonObject.has("project_name"))existLeadProjectOtherNo=!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "";
        if (jsonObject.has("sales_person_name")) existOtherLeadAddedBy = !jsonObject.get("sales_person_name").isJsonNull() ? jsonObject.get("sales_person_name").getAsString() : "";
        if (jsonObject.has("full_name")) existLeadNameOtherNo=!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "";
        if (jsonObject.has("lead_status_name")) existLeadStatusOtherNo=!jsonObject.get("lead_status_name").isJsonNull() ? jsonObject.get("lead_status_name").getAsString() : "";

    }


    private void onSuccessAdd(boolean fromWhatsAppNo)
    {
        runOnUiThread(() -> {

            hideProgressBar();

            if(fromWhatsAppNo)
            {
              /*  til_leadMobile.setErrorEnabled(true);
                til_leadMobile.setError("Number already exits!");*/
                edt_leadMobileNo.setError("Number already exits!");
                mTv_ExistUser.setText(String.format("Belongs to: %s", existLeadName));
                mTv_ExistProject.setText(String.format("Belongs to project: %s", existLeadProject));
                mTv_ExistLeadAddedBy.setText(String.format("Added By: %s", existLeadAddedBy));
                mTv_ExistLeadStatus.setText(String.format("Lead Status: %s", existLeadStatus));
                ll_addLead_existUSer.setVisibility(existLeadName!=null && !existLeadName.trim().isEmpty()?View.VISIBLE : View.GONE);
            }
            else
            {
               /* til_leadOtherMobile.setErrorEnabled(true);
                til_leadMobile.setError("Number already exits!");*/
                edt_leadOtherMobileNo.setError("Number already exits!");
                mTv_ExistUser_OtherNo.setText(String.format("Belongs to: %s", existLeadNameOtherNo));
                mTv_ExistProject_OtherNo.setText(String.format("Belongs to project: %s", existLeadProjectOtherNo));
                mTv_ExistLeadAddedBy_OtherNo.setText(String.format("Added By: %s", existOtherLeadAddedBy));
                mTv_ExistStatus_OtherNo.setText(String.format("Lead Status: %s", existLeadStatusOtherNo));
                ll_addLead_existUSer_OtherNo.setVisibility(existLeadNameOtherNo!=null && !existLeadNameOtherNo.trim().isEmpty()?View.VISIBLE : View.GONE);

            }

            //checkButtonEnabled
            checkButtonEnabled();
        });


    }


    public void sameNumber(String num1,String num2)
    {
        if(num1!=null && num2!=null)
        {
            if(num1.equals(num2)) {
                flagNumduplicate=true;
                edt_leadOtherMobileNo.setError("mobile number already exits please enter another one!!");
            }
            else
            {
                edt_leadOtherMobileNo.setError(null);
                flagNumduplicate=false;
            }

        }
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
                        case R.id.rb_newLead_firstHome_yes:
                            // Toast.makeText(getApplicationContext(),""+rdo_btn_First_House_yes.getText().toString(), Toast.LENGTH_LONG).show();
                            FirstHomeID=1;

                            break;
                        case R.id.rb_newLead_FirstHome_no:
                            // Toast.makeText(getApplicationContext(),""+rdo_btn_First_House_no.getText().toString(),Toast.LENGTH_LONG).show();
                            FirstHomeID=2;
                            break;
                        default:
                            break;
                    }


                }
        );

    }

    private void checkLeadMobile()
    {
        edt_leadMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                tv_mobAlExists.setVisibility(View.GONE);
                if (Objects.requireNonNull(edt_leadMobileNo.getText()).length()>7)
                {
                    mBtn_verifyLeadMob.setText(getString(R.string.verify));
                    mBtn_verifyLeadMob.setVisibility(View.VISIBLE);
                    if (Objects.requireNonNull(edt_leadMobileNo.getText()).length()>9) hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
                }
                else
                {
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    mBtn_resendOTP.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    tv_mobVerifySuccess.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                }


                if (Objects.requireNonNull(edt_leadMobileNo.getText()).length()>9) checkButtonEnabled();
                else
                {
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    mBtn_resendOTP.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    tv_mobVerifySuccess.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mBtn_verifyLeadMob.setOnClickListener(v -> {

            if (mBtn_verifyLeadMob.getText().toString().contains(getString(R.string.verify)))
            {
                //do verify
                //if (isValidPhone(edt_authPMob))
                {
                    if (isNetworkAvailable(context))
                    {
                        //if (progressDialog!=null) progressDialog.show();
                        //if (checkAndRequestPermissions())
                        {
                            Log.e(TAG, "onClick: " );
                        }

                        //check all Permissions
                        mBtn_verifyLeadMob.setVisibility(View.GONE);
                        //tv_loadingMsg.setText(getString(R.string.sending_otp));
                        //if (ll_pb!=null) ll_pb.setVisibility(View.VISIBLE);
                        showProgressBar(getString(R.string.sending_otp));
                        call_sendOTP_leadMobile(edt_leadMobileNo.getText().toString());

                       /* else
                        {
                            //requestPermission
                            requestSMSPermission(edt_authPMob.getText().toString());
                        }*/
                    }
                    else NetworkError(context);
                }

            }
            else
            {
                //check entered otp and submit
                if (Objects.requireNonNull(edt_OTPNumber.getText()).toString().equals(lead_OTP))
                {
                    //opt match

                    edt_OTPNumber.setText("");
                    tv_mobVerifySuccess.setVisibility(View.VISIBLE);
                    runOnUiThread(() -> {

                        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.blinking_animation);

                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round,context.getTheme()));
                        //else tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round));
                        tv_mobVerifySuccess.startAnimation(expandIn);

                        tv_mobVerifySuccess.postDelayed(() -> {

                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round,context.getTheme()));
                            //else tv_companyMobVerifySuccess.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_main_grey_round));

                            tv_mobVerifySuccess.clearAnimation();
                            tv_mobVerifySuccess.invalidate();
                        }, 3000);
                    });

                    //once success gone the controls
                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    til_leadOtp.setVisibility(View.GONE);
                    edt_OTPNumber.setVisibility(View.GONE);
                    edt_leadMobileNo.setEnabled(false);
                    ll_leadMobCcp.setEnabled(false);

                    //gone visibilities
                    mBtn_resendOTP.setVisibility(View.GONE);
                    tv_resendOtp_counter.setVisibility(View.GONE);
                    tv_resendOtp_msg.setVisibility(View.GONE);

                    new Helper().showSuccessCustomToast(context, "Mobile number verified successfully!");

                    //set lead mobile Verified True
                    isLeadMobileVerified = true;

                    //check buttonEnabled
                    checkButtonEnabled();
                }
                else
                {
                    //not match
                    new Helper().showCustomToast(context, "OTP did not match!");
                }
            }
        });
    }

    private void call_sendOTP_leadMobile(String mobile)
    {
        ApiClient client = ApiClient.getInstance();
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("api_token", WebServer.API_TOKEN_EXTERNAL);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("mobile_number", mobile);

        client.getApiService().sendOTPLeadMobile(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject())
                                {
                                    JsonObject jsonObject1 = response.body().get("data").getAsJsonObject();
                                    setOTPJson(jsonObject1);
                                }
                            }else showErrorLog("Failed to send OTP!");
                        }
                        else if (isSuccess==2)
                        {
                            //mobile number already exists
                            set_leadMobExistsView();
                            if (response.body().has("msg")) showErrorLog(!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : getString(R.string.mob_number_exists));
                        }
                        else showErrorLog("Failed to send OTP!");
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
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
        });
    }

    void set_leadMobExistsView()
    {

        runOnUiThread(() -> {
            //if (ll_pb!=null) ll_pb.setVisibility(View.GONE);
            //if (pb_loading!=null) pb_loading.setVisibility(View.GONE);
            hideProgressBar();
            tv_mobAlExists.setVisibility(View.VISIBLE);
            edt_leadMobileNo.setEnabled(true);

            til_leadOtp.setVisibility(View.GONE);
            edt_OTPNumber.setVisibility(View.GONE);
            mBtn_verifyLeadMob.setVisibility(View.GONE);
        });
    }

    private void setOTPJson(final JsonObject jsonObject)
    {

        runOnUiThread(() -> {
            //if (ll_pb!=null) ll_pb.setVisibility(View.GONE);
            //if (pb_loading!=null) pb_loading.setVisibility(View.GONE);
            hideProgressBar();

            String OTP = "";
            if (jsonObject.has("otp")) OTP = !jsonObject.get("otp").isJsonNull() ? jsonObject.get("otp").getAsString() : "" ;
            Log.e(TAG, "otp "+ OTP);

            //new Helper().showCustomToast(context, "OTP "+OTP);
            lead_OTP = OTP;
            til_leadOtp.setVisibility(View.VISIBLE);
            edt_OTPNumber.setVisibility(View.VISIBLE);
            edt_OTPNumber.requestFocus();
            //edt_authPMob.setEnabled(false);
            mBtn_verifyLeadMob.setText(getString(R.string.submit_otp));
            mBtn_verifyLeadMob.setVisibility(View.VISIBLE);

            setResendLeadOTP();

            /*  edt_authPOTP.setVisibility(View.VISIBLE);
                edt_authPMob.setEnabled(false);
                btn_verifyOwnerMob.setText(getString(R.string.submit_otp));*/

            //if (jsonObject.has("msg")) myModel.setCity_name(!jsonObject.get("msg").isJsonNull() ? jsonObject.get("msg").getAsString() : "msg" );
        });
    }

    void setResendLeadOTP()
    {

        //visible controls
        tv_resendOtp_msg.setVisibility(View.VISIBLE);
        mBtn_resendOTP.setVisibility(View.VISIBLE);

        tv_resendOtp_counter.setVisibility(View.VISIBLE);
        startCountDownTimer(tv_resendOtp_counter);

        new Handler().postDelayed(() -> {

            mBtn_resendOTP.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mBtn_resendOTP.setTextColor(getResources().getColor(R.color.main_white));

            mBtn_resendOTP.setOnClickListener(v -> {

                if (isNetworkAvailable(context))
                {

                    //disabled resend-otp button
                    mBtn_resendOTP.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
                    mBtn_resendOTP.setTextColor(getResources().getColor(R.color.main_white));

                    mBtn_verifyLeadMob.setVisibility(View.GONE);
                    //tv_loadingMsg.setText(getString(R.string.re_sending_otp));
                    //if (ll_pb!=null) ll_pb.setVisibility(View.VISIBLE);
                    showProgressBar(getString(R.string.re_sending_otp));
                    call_sendOTP_leadMobile(Objects.requireNonNull(edt_leadMobileNo.getText()).toString().trim());
                }
                else NetworkError(context);
            });

        }, 120000);
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



  /*  private void checkInsertRemoveSubCatName(String subcatName, boolean value) {
        if (value) catStringArrayList.add(subcatName);
        else catStringArrayList.remove(new String(subcatName));
    }

    public ArrayList<String> getSubCatIds() {
        return catStringArrayList;
    } */


  /*  private void checkInsertRemoveSubCat(int id, boolean value) {
        if (value) catIntegerArrayList.add(id);
        else catIntegerArrayList.remove(new Integer(id));
    }

    public ArrayList<Integer> getSubCatIds() {
        return catIntegerArrayList;
    }*/



    private void set_radioButtons()
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


        //property Buying for purpose
        rdoGrp_propertyBuyFor.setOnCheckedChangeListener((group, checkedId) ->
        {

            int selectedId = rdoGrp_propertyBuyFor.getCheckedRadioButtonId();
            final RadioButton rBtn = rdoGrp_propertyBuyFor.findViewById(selectedId);
            final String btnText =  rBtn.getText().toString();
            //Toast.makeText(RegisterEventNewActivity.this, "Accommodation "+Accommodation, Toast.LENGTH_SHORT).show();

            // incomplete due to spares not available
            //incomplete due to quotation pending

            //todo static set self use 1, Investment 2
            selectedPropertyBuyingReasonId = btnText.contains(getString(R.string.self_use)) ? 1 : 2;

            if (btnText.contains(getString(R.string.other)))
            {
                // incomplete due to spares not available
                //isSpareOrQuot = true;
                edt_otherPropertyBuyPurpose.setVisibility(View.VISIBLE);
            }
            else
            {
                //incomplete due to quotation pending
                //isSpareOrQuot = false;
                edt_otherPropertyBuyPurpose.setVisibility(View.GONE);
            }

            //check button EnabledView
            checkButtonEnabled();

        });
    }

    private void selectDateOfBirth()
    {
        //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
        //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_newLead_dob.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));

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

                        setSelectedDoc(myUploadModel, finalFile.getAbsolutePath());
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


                        setSelectedDoc(myUploadModel, photoUrl);
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

                setSelectedDoc(myUploadModel, photoUrl);
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
        else if (requestCode == 121 && responseCode == RESULT_CANCELED)
        {
            new Helper().showCustomToast(context, "You cancelled!");
        }
        else if (requestCode == 122  && responseCode  == RESULT_OK)
        {
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
        else if (requestCode == 122 && responseCode == RESULT_CANCELED)
        {
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


    private void setSelectedDoc(EventProjectDocsModel myUploadModel, String absolutePath)
    {
        /*if (sharedPreferences != null) {
            editor = sharedPreferences.edit();

            editor.apply();
            String docsAry = null;
            //if (sharedPreferences.getString("docsAry", null) != null) docsAry = sharedPreferences.getString("docsAry", null);
            //JsonArray jsonArray = new Gson().fromJson(docsAry, JsonArray.class);

            Log.e(TAG, "docsAry: "+ docsAry);

        }*/

        EventProjectDocsModel model =new EventProjectDocsModel();
        model.setDoc_type_id(myUploadModel.getDoc_type_id());
        //model.setDocType(myUploadModel.getDocType());
        model.setIsRequired(myUploadModel.getIsRequired());
        model.setIsUploaded(0);
        model.setDocType(myUploadModel.getDocType());
        model.setDocPath(absolutePath!=null ? absolutePath : "");

        docsModelArrayList.set(myPosition, model);
        documentCount++;
        //set documents view
        setKYCDocuments();
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
        else if (selectedProjectId==0) new Helper().showCustomToast(context, "Please select project name!");
            // unit type
        else if (selectedUnitId==0) new Helper().showCustomToast(context, "Please select unit category!");
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

            if(isUpdate) {
                showUpdateLeadAlertDialog();
            }
            else if(isDuplicateLead) {
                showUpdateDuplicateLeadAlertDialog();
            }
            else {
                showSubmitLeadAlertDialog();
            }
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
        else if (selectedProjectId==0) setButtonDisabledView();
            // unit type
        else if (selectedUnitId==0) setButtonDisabledView();
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

    private void showUpdateDuplicateLeadAlertDialog()
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

        tv_msg.setText(getString(R.string.update_lead_question));
        tv_desc.setText(getString(R.string.update_lead_confirmation));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.submitting_lead_details));
                post_UpdateDuplicateLead();

            }else NetworkError(context);
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

    private void showUpdateLeadAlertDialog()
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

        tv_msg.setText(getString(R.string.update_lead_question));
        tv_desc.setText(getString(R.string.update_lead_confirmation));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.submitting_lead_details));
                post_UpdateLead();

            }else NetworkError(context);
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

        tv_msg.setText(getString(R.string.submit_lead_question));
        tv_desc.setText(getString(R.string.submit_lead_confirmation));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.submitting_lead_details));
                call_addSalesLead();

            }else NetworkError(context);
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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("full_name",  Objects.requireNonNull(edt_leadFirstName.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_leadEmail.getText()).toString());
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("country_code_1", countryPhoneCode_1);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_leadMobileNo.getText()).toString());
        jsonObject.addProperty("alternate_mobile_number", Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
        jsonObject.addProperty("address_line_1", Objects.requireNonNull(edt_leadAddress.getText()).toString());
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("unit_category_id", selectedUnitId);
        jsonObject.addProperty("lead_profession", selectedLeadProfessionName);
        jsonObject.addProperty("lead_ni_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("lead_ni_other_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("budget_limit_id", selectedBudgetLimitId);
        jsonObject.addProperty("income_range_id", selectedIncomeRangeId);
        jsonObject.addProperty("is_first_home", FirstHomeID);
        jsonObject.addProperty("lead_stage_id", selectedLeadStageId);
        jsonObject.addProperty("lead_status_id", isAlreadySiteVisited ? 2 : 1);
        jsonObject.addProperty("is_site_visited", isAlreadySiteVisited ? 1 : 0);
        jsonObject.addProperty("visit_date", sendAlreadySiteVisitDate);
        jsonObject.addProperty("visit_time", sendAlreadySiteVisitTime);
        jsonObject.addProperty("visit_remark", Objects.requireNonNull(edt_alreadySiteVisitRemark.getText()).toString());
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("dob", sendDateOfBirth!=null ? sendDateOfBirth : "");
        jsonObject.addProperty("is_first_home", FirstHomeID);
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("remarks", Objects.requireNonNull(edt_leadRemarks.getText()).toString());

        if(!(LeadType_ID == 0))
        {
            jsonObject.addProperty("lead_types_id", LeadType_ID);
            if(!(SecLeadType_ID == 0)||!(edit_text_req_ID == 0))
            {
                Log.e(TAG, "checkSubValidation: "+ Arrays.toString(getIntegerArray().toArray()));

                JsonArray jsonArray = new JsonArray();
                for (int i=0; i<getIntegerArray().size(); i++)
                {
                    jsonArray.add(getIntegerArray().get(i));
                }

                jsonObject.add("lead_type_lvl2", jsonArray);
//                    jsonObject.addProperty("lead_type_lvl2",Arrays.toString(getIntegerArray().toArray()));
                jsonObject.addProperty("lead_type_extra_info", tv_leadGen_thrw.getText().toString());
                jsonObject.addProperty("reference_name", edt_fullName_referer.getText().toString());
                jsonObject.addProperty("reference_mobile", edt_refererMobile_no.getText().toString());
            }
        }


        ApiClient client = ApiClient.getInstance();
        client.getApiService().addSalesLead(jsonObject).enqueue(new Callback<JsonObject>()
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
                                    onLeadSubmit();
                                    // showSuccessAlert();
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
        });
    }

    private void post_UpdateDuplicateLead()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("offline_id", duplicate_offline_lead_id);
        jsonObject.addProperty("full_name",  Objects.requireNonNull(edt_leadFirstName.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_leadEmail.getText()).toString());
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("country_code_1", countryPhoneCode_1);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_leadMobileNo.getText()).toString());
        jsonObject.addProperty("alternate_mobile_number", Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
        jsonObject.addProperty("address_line_1", Objects.requireNonNull(edt_leadAddress.getText()).toString());
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("project_name", selectedProjectName);
        jsonObject.addProperty("unit_category_id", selectedUnitId);
        jsonObject.addProperty("unit_category", selectedUnitCategory);
        jsonObject.addProperty("lead_profession", selectedLeadProfessionName);
        jsonObject.addProperty("lead_profession_id", selectedProfessionId);
        jsonObject.addProperty("lead_profession", selectedLeadProfessionName);
        jsonObject.addProperty("budget_limit_id", selectedBudgetLimitId);
        jsonObject.addProperty("budget_limit", selectedBudgetLimit);
        jsonObject.addProperty("income_range_id", selectedIncomeRangeId);
        jsonObject.addProperty("income_range", selectedIncomeRange);
        jsonObject.addProperty("is_first_home", FirstHomeID);
        jsonObject.addProperty("lead_stage_id", selectedLeadStageId);
        jsonObject.addProperty("lead_stage", selectedLeadStageName);
        jsonObject.addProperty("lead_ni_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("lead_ni_other_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("prefix_id", selectedNamePrefixId);
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("dob", sendDateOfBirth!=null && !sendDateOfBirth.trim().isEmpty() ? new Helper().formatUpdateDateDate(sendDateOfBirth) : "");
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("lead_status_id", 1);
        jsonObject.addProperty("lead_types_id", LeadType_ID);
        jsonObject.addProperty("remarks", Objects.requireNonNull(edt_leadRemarks.getText()).toString());


        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().updateDuplicateLeadDetails(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            showSuccessDuplicateUpdateAlert();

                        }
                        else showErrorLog("Failed to update customer details! Try again.");
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }



    private void post_UpdateLead()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id", leadId);
        jsonObject.addProperty("person_id", person_id);
        jsonObject.addProperty("full_name",  Objects.requireNonNull(edt_leadFirstName.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_leadEmail.getText()).toString());
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("country_code_1", countryPhoneCode_1);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_leadMobileNo.getText()).toString());
        jsonObject.addProperty("alternate_mobile_number", Objects.requireNonNull(edt_leadOtherMobileNo.getText()).toString());
        jsonObject.addProperty("address_line_1", Objects.requireNonNull(edt_leadAddress.getText()).toString());
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("unit_category_id", selectedUnitId);
        jsonObject.addProperty("lead_profession", selectedLeadProfessionName);
        jsonObject.addProperty("budget_limit_id", selectedBudgetLimitId);
        jsonObject.addProperty("income_range_id", selectedIncomeRangeId);
        jsonObject.addProperty("is_first_home", FirstHomeID);
        jsonObject.addProperty("lead_stage_id", selectedLeadStageId);
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("lead_ni_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("lead_ni_other_reason", Objects.requireNonNull(edt_newLead_niReason.getText()).toString());
        jsonObject.addProperty("dob", sendDateOfBirth!=null ? sendDateOfBirth : "");
        jsonObject.addProperty("sales_person_id", user_id);
        jsonObject.addProperty("lead_status_id", current_lead_status_id);
        jsonObject.addProperty("remarks", Objects.requireNonNull(edt_leadRemarks.getText()).toString());

        if(!(LeadType_ID == 0))
        {
            jsonObject.addProperty("lead_types_id", LeadType_ID);
            if(!(SecLeadType_ID == 0)||!(edit_text_req_ID == 0))
            {
                Log.e(TAG, "checkSubValidation: "+ Arrays.toString(getIntegerArray().toArray()));

                JsonArray jsonArray = new JsonArray();
                for (int i=0; i<getIntegerArray().size(); i++)
                {
                    jsonArray.add(getIntegerArray().get(i));
                }

                jsonObject.add("lead_type_lvl2", jsonArray);
//                    jsonObject.addProperty("lead_type_lvl2",Arrays.toString(getIntegerArray().toArray()));
                jsonObject.addProperty("lead_type_extra_info", tv_leadGen_thrw.getText().toString());
                jsonObject.addProperty("reference_name", edt_fullName_referer.getText().toString());
                jsonObject.addProperty("reference_mobile", edt_refererMobile_no.getText().toString());
            }
        }


        /*   jsonObject.addProperty("mobile_number", model.getCustomer_mobile());
        jsonObject.addProperty("email", model.getCustomer_email());*/

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().updateLeadDetails(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        String success = response.body().get("success").toString();
                        if(success.equals("1")) {
                            hideProgressBar();
                            showSuccessUpdateAlert();

                        }
                        else showErrorLog("Failed to update customer details! Try again.");
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof UnknownServiceException) showErrorLog(getString(R.string.cleartext_communication_not_permitted));
                else if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    @SuppressLint("InflateParams")
    private void showSuccessDuplicateUpdateAlert()
    {
        runOnUiThread(() -> {

            //open disabled View
            // openView();
            //set gif
            // gif_newLead.setImageResource(R.drawable.gif_success);
            //set animation
            //  new Animations().scaleEffect(ll_success);
            //visible view
            // ll_success.setVisibility(View.VISIBLE);
            //show success toast
            flagExit=true;
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isDuplicateLeadUpdated",1);
                editor.apply();
            }
            hideProgressBar();
            new Helper().showSuccessCustomToast(context, "Lead Updated Successfully");


            //do backPress
            //share_dialog.dismiss();
            // ll_success.setVisibility(View.GONE);
            //close view
            //closeView();
            //do backPress
            new Handler().postDelayed(this::onBackPressed, 1000);
        });

    }


    @SuppressLint("InflateParams")
    private void showSuccessUpdateAlert()
    {
        runOnUiThread(() -> {

            //open disabled View
            // openView();
            //set gif
            // gif_newLead.setImageResource(R.drawable.gif_success);
            //set animation
            //  new Animations().scaleEffect(ll_success);
            //visible view
            // ll_success.setVisibility(View.VISIBLE);
            //show success toast

            hideProgressBar();

            new Helper().showSuccessCustomToast(context, "Lead Updated Successfully");

            flagExit=true;
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isLeadUpdated",1);
                editor.apply();
            }

            //do backPress
            //share_dialog.dismiss();
            // ll_success.setVisibility(View.GONE);
            //close view
            //closeView();
            //do backPress
            new Handler().postDelayed(this::onBackPressed, 1000);
        });

    }


    private void parseReturnData(JsonObject jsonObject)
    {
        if (jsonObject.has("lead_id")) lead_id=!jsonObject.get("lead_id").isJsonNull()&&!jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsString() : "0";

    }

    public void onLeadSubmit()
    {
        runOnUiThread(() -> {
            hideProgressBar();
            if(isLeadSubmitted)
            {
                if(idDocSelected)
                {
//                        hideProgressBar();
                    addPostDoc();
                }
                else showSuccessAlert();
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
            gif_newLead.setImageResource(R.drawable.gif_success);
            //set animation
            new Animations().scaleEffect(ll_success);
            //visible view
            ll_success.setVisibility(View.VISIBLE);
            //show success toast
            new Helper().showSuccessCustomToast(context, getString(R.string.lead_added_successfully));

            flagExit=true;
            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isLeadAdd",1);
                editor.apply();
            }

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
                    startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
                    finish();
                }
            }
            else{
                super.onBackPressed();
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
