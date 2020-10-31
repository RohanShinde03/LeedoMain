package com.tribeappsoft.leedo.admin.site_visits;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.models.project.UnitCategoriesModel;
import com.tribeappsoft.leedo.salesPerson.adapter.CustomerAdapter;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AddSiteVisitActivity extends AppCompatActivity {

    @BindView(R.id.cl_siteVisit) CoordinatorLayout parent;
    @BindView(R.id.tv_select_customer_mobile) AppCompatTextView customerId;
    @BindView(R.id.tv_siteVisit_salesRepName) AppCompatTextView tv_sales_representative;
    @BindView(R.id.tv_customerName_site_visit) AppCompatTextView customerName;
    @BindView(R.id.tv_siteVisit_project_name) AutoCompleteTextView acTv_projectName;
    @BindView(R.id.tv_flat_type) AutoCompleteTextView acTv_flatType;
    @BindView(R.id.acTv_siteVisit_updateLeadStage) AutoCompleteTextView acTv_updateLeadStage;
    @BindView(R.id.edt_siteVisit_niReason) TextInputEditText edt_siteVisit_niReason;
    @BindView(R.id.til_siteVisit_niReason) TextInputLayout til_siteVisit_niReason;
    @BindView(R.id.edt_visit_date) TextInputEditText visitDate;
    @BindView(R.id.edt_visit_time) TextInputEditText visitTime;
    @BindView(R.id.edt_siteVisit_remarks) TextInputEditText edt_remarks;
    @BindView(R.id.iv_siteVisits_voiceRemarks) AppCompatImageView iv_voiceRemarks;
    @BindView(R.id.mBtn_siteVisit_submitSiteVisit) MaterialButton mBtn_submitSiteVisit;
    @BindView(R.id.rg) RadioGroup radiogroup;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    //@BindView(R.id.ll_site_visit_details) LinearLayoutCompat ll_site_visit_details;
    //@BindView(R.id.ll_siteVisit) LinearLayoutCompat ll_siteVisit;
    //@BindView(R.id.rb_yes) RadioButton rb_yes;
    //@BindView(R.id.rb_no) RadioButton rb_no;

    private AppCompatActivity context;
    CustomerAdapter adapter = null;
    private CUIDModel cuidModel = null;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<LeadStagesModel> leadStagesModelArrayList;
    private ArrayList<String> projectStringArrayList, flatStringArrayList, leadStageStringArrayList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String cuidNumber, customer_name="", selectedProjectName = "", selectedFlatType = "",sendAlreadySiteVisitDate= null,
            sendAlreadySiteVisitTime = null, full_name ="",sendAlreadySiteVisitTimeFormatted="",
            sendAlreadySiteFormattedVisitDate="",api_token="",  selectedLeadStageName = "";

    public int selectedProjectId = 0, selectedFlatId = 0,user_id=0, lead_id =0, mYear, mMonth, mDay, selectedLeadStageId=0;
    private String TAG = "AddSiteVisitActivity";
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 145;
    //private int fromOther=0;
    private boolean flagExit=false,fromHomeScreen_AddSiteVisit=false;


    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_visit);
        ButterKnife.bind(this);
        context = AddSiteVisitActivity.this;
        //call method to hide keyBoard
        setupUI(parent);

        if (getIntent() != null) {
            //get cuId model
            cuidModel = (CUIDModel) getIntent().getSerializableExtra("cuidModel");
            cuidNumber =  getIntent().getStringExtra("lead_cu_id");
            customer_name =  getIntent().getStringExtra("lead_name");
            fromHomeScreen_AddSiteVisit =  getIntent().getBooleanExtra("fromHomeScreen_AddSiteVisit",false);
            lead_id = getIntent().getIntExtra("lead_id", 0);
        }

        if (getSupportActionBar()!=null)
        {
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.add_site_visit));
            getSupportActionBar().setTitle(getString(R.string.add_site_visit));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        full_name =sharedPreferences.getString("full_name",getString(R.string.user_name));
        tv_sales_representative.setText(full_name);

        //hide pb
        hideProgressBar();


        if (cuidModel != null) {

            //From Direct CuId Activity && Feeds
            cuidNumber = cuidModel.getCustomer_mobile();
            customer_name = cuidModel.getCustomer_name();
            lead_id = cuidModel.getLead_id();
            customerId.setText(cuidNumber);
            customerName.setText(customer_name);

            //check Button Enabled View
            checkButtonEnabled();
        }
        /*else
        {
            //came from feeds
            customerId.setText(cuidNumber);
            customerName.setText(customer_name);

            //check Button Enabled View
            checkButtonEnabled();
        }*/


        //Initialize
        projectStringArrayList = new ArrayList<>();
        projectModelArrayList = new ArrayList<>();
        leadStagesModelArrayList = new ArrayList<>();
        leadStageStringArrayList = new ArrayList<>();
        flatStringArrayList = new ArrayList<>();

        //init Speech recognizer
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        /*  Get Project & Unit Type*/
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar(getString(R.string.please_wait));
            new Thread(this::getProjectListData).start();
        } else Helper.NetworkError(context);


        customerId.setOnClickListener(v ->{
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

            startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 1)
                    .putExtra("forId", 1));
            finish();
        });

        radiogroup.setOnCheckedChangeListener((radioGroup, i) -> {
            int id = radiogroup.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rb_yes:
                    mBtn_submitSiteVisit.setText(R.string.generate_ghp);
                    break;

                case R.id.rb_no:
                    mBtn_submitSiteVisit.setText(R.string.add_site_visit);
                    break;
            }
        });

        //visit date time
        visitDate.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select date
            selectVisitDate();
        });

        visitTime.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select time
            selectVisitTime();
        });


        mBtn_submitSiteVisit.setOnClickListener(view ->
        {
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //check validations
            checkValidations();

        });

        edt_siteVisit_niReason.setVisibility(View.GONE);
        til_siteVisit_niReason.setVisibility(View.GONE);

        //add Lead Stages
        //addStaticLeadStages();

        /*iv_voiceRemarks.setOnClickListener(view -> {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                 if (checkRecordAudioPermissions())initSpeechToText();
                 else requestPermissionAudio();
             }
             else initSpeechToText();
        });*/


        //start recording voice to text
        //initSpeechToText();

        iv_voiceRemarks.setOnTouchListener((view, motionEvent) -> {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:
                    mSpeechRecognizer.stopListening();
                    edt_remarks.setHint("You will see input here");
                    break;

                case MotionEvent.ACTION_DOWN:
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    edt_remarks.setText("");
                    edt_remarks.setHint("Listening...");
                    break;
            }

            return false;
        });
    }

    private void initSpeechToText() {

        //mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                //set remarks
                if (matches != null) edt_remarks.setText(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

 /*   private void addStaticLeadStages() {
        //add local lead stages

        //clear list
        leadStagesModelArrayList.clear();
        leadStageStringArrayList.clear();

        //add call status
        leadStagesModelArrayList.add(new LeadStagesModel(1, "Hot"));
        leadStagesModelArrayList.add(new LeadStagesModel(2, "Warm"));
        leadStagesModelArrayList.add(new LeadStagesModel(3, "Cold"));
        leadStagesModelArrayList.add(new LeadStagesModel(4, "Lost"));

        //add static string data
        leadStageStringArrayList.add("Hot");
        leadStageStringArrayList.add("Warm");
        leadStageStringArrayList.add("Cold");
        leadStageStringArrayList.add("Lost");


        //set adapter for lead stages
        setAdapterLeadStages();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
    }

    //check call permission
    private boolean checkRecordAudioPermissions() {
        return  (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    //request camera permission
    private void requestPermissionAudio()
    {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO))
                && (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        )
        {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            new Helper().showCustomToast(context, context.getString(R.string.audio_permissionRationale));

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(context, new String[]
                {
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, AUDIO_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE)  //handling camera permission
        {
            Log.e(TAG, "onRequestPermissionsResult:  "+ AUDIO_PERMISSION_REQUEST_CODE);
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Log.e(TAG, "onRequestPermissionsResult: permission grant success!");
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));

                //start recording voice to text
                initSpeechToText();
            }
            else Log.e(TAG, "onRequestPermissionsResult: permission grant failure!");
        }
        else Log.e(TAG, "onRequestPermissionsResult: Wrong Error Code" );
    }

    private void getProjectListData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getSiteVisitProjectsList(api_token,user_id );
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
                        delayRefresh();
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
                                                setProjectJson(jsonObject);
                                            }
                                        }
                                    } else
                                        showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setProjectJson(JsonObject jsonObject)
    {
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
    }

    private void setProjectNamesJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }


        if (jsonObject.has("unit_categories") && !jsonObject.get("unit_categories").isJsonNull())
        {
            if (jsonObject.get("unit_categories").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("unit_categories").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    ArrayList<UnitCategoriesModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setUnitDataJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setCategoriesModelArrayList(arrayList);
                }
            }
        }

        projectModelArrayList.add(model);
    }

    private void setUnitDataJson(JsonObject jsonObject, ArrayList<UnitCategoriesModel> arrayList)
    {

        UnitCategoriesModel myModel = new UnitCategoriesModel();
        if (jsonObject.has("unit_category_id")) myModel.setUnit_category_id(!jsonObject.get("unit_category_id").isJsonNull() ? jsonObject.get("unit_category_id").getAsInt() : 0 );
        if (jsonObject.has("unit_category")) myModel.setUnit_category(!jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" );

        arrayList.add(myModel);

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

    /*Delay Refresh*/
    private void delayRefresh()    {

        //reply
        if (context!=null) {
            context.runOnUiThread(() -> {

                hideProgressBar();

                //set adapter for ref project name
                setAdapterProjectNames();

                //set adapter for lead stage
                setAdapterLeadStages();
            });
        }
    }


    private void setAdapterLeadStages() {

        if (leadStagesModelArrayList.size() >0 &&  leadStageStringArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadStageStringArrayList);
            //acTv_updateLeadStage.setAdapter(adapter);
            //  acTv_updateLeadStage.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            adapter = new CustomerAdapter(context, leadStagesModelArrayList);
            acTv_updateLeadStage.setAdapter(adapter);

            acTv_updateLeadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                String itemName = adapter.getItem(position).getLead_stage_name();
                for (LeadStagesModel pojo : leadStagesModelArrayList) {
                    if (pojo.getLead_stage_name().equals(itemName)) {

                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedLeadStageId = pojo.getLead_stage_id(); // This is the correct ID
                        selectedLeadStageName = pojo.getLead_stage_name();
                        //acTv_updateLeadStage.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));

                        edt_siteVisit_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);
                        til_siteVisit_niReason.setVisibility(pojo.getLead_stage_id()==4 && pojo.getLead_stage_name().equals("Not Interested") ? View.VISIBLE :View.GONE);

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Lead Status name & id " + selectedLeadStageName +"\t"+ selectedLeadStageId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }


                }
            });
        /*    acTv_updateLeadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
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

/*
    private void setAdapterLeadStages() {

        if (leadStagesModelArrayList.size() >0 &&  leadStageStringArrayList.size()>0)
        {
            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, leadStageStringArrayList);
            acTv_updateLeadStage.setAdapter(adapter);
            acTv_updateLeadStage.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

            acTv_updateLeadStage.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (LeadStagesModel pojo : leadStagesModelArrayList) {

                    if (pojo.getLead_stage_name().equals(itemName)) {

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

        }
    }
*/

    private void setAdapterProjectNames()
    {

        //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectStringArrayList);
        //acTv_projectName.setText(projectStringArrayList.get(0));
        acTv_projectName.setAdapter(adapter);
        acTv_projectName.setThreshold(0);
        //tv_selectCustomer.setSelection(0);
        //autoComplete_firmName.setValidator(new Validator());
        //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

        acTv_projectName.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
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
                    acTv_flatType.setText("");
                    //set adapter for unit categories
                    setAdapterUnitCategories(projectModelArrayList.get(position).getCategoriesModelArrayList());

                    //check Button Enabled View
                    checkButtonEnabled();

                    break; // No need to keep looping once you found it.
                }
            }
        });
    }



    private void setAdapterUnitCategories(ArrayList<UnitCategoriesModel> categoriesModelArrayList)
    {

        if (categoriesModelArrayList!=null && categoriesModelArrayList.size()>0)
        {
            //adding unit categories
            flatStringArrayList.clear();
            for (int i =0; i<categoriesModelArrayList.size(); i++)
            {
                flatStringArrayList.add(categoriesModelArrayList.get(i).getUnit_category());
                //Log.e(TAG, "categoriesModelArrayList.get(i).getUnit_category(): "+categoriesModelArrayList.get(i).getUnit_category() );

            }
            Log.e(TAG, "categoriesModelArrayList: "+categoriesModelArrayList.size() );


            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.layout_spinner_item, flatStringArrayList);
            acTv_flatType.setAdapter(adapter);
            acTv_flatType.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_flatType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (UnitCategoriesModel pojo : categoriesModelArrayList)
                {
                    if (pojo.getUnit_category().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedFlatId = pojo.getUnit_category_id(); // This is the correct ID
                        selectedFlatType = pojo.getUnit_category();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Unit category & id " + selectedFlatType +"\t"+ selectedFlatId);

                        //check Button Enabled View
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



    private void selectVisitDate()
    {
        //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
        //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {


                    sendAlreadySiteFormattedVisitDate = Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    visitDate.setText(sendAlreadySiteFormattedVisitDate);

                    int mth = monthOfYear + 1;
                    sendAlreadySiteVisitDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "al_siteVisit_send Date:: " + sendAlreadySiteVisitDate);

                    //check Button Enabled View
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

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
                    sendAlreadySiteVisitTimeFormatted= String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                    visitTime.setText(sendAlreadySiteVisitTimeFormatted);
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendAlreadySiteVisitTime!=null) Log.e(TAG, "Al Visit Time: "+sendAlreadySiteVisitTime);

                    //set button view enabled
                    //if (isAssignLater && !tv_committedDate.getText().toString().trim().isEmpty()) setButtonEnabledView();

                    //check Button Enabled View
                    checkButtonEnabled();

                }, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode == 121 && responseCode == RESULT_OK) {
            CUIDModel cuidModel = (CUIDModel) data.getSerializableExtra("result");
            assert cuidModel != null;
            cuidNumber = cuidModel.getCustomer_mobile();
            customer_name = cuidModel.getCustomer_name();
            lead_id = cuidModel.getLead_id();
            Log.e("Display", "onActivityResult: countryCode " + cuidNumber);
            customerId.setText(cuidNumber);
            customerName.setText(customer_name);
            new Helper().showCustomToast(context, "selected " + cuidModel.getCu_id());
        } else if (requestCode == 121 && responseCode == RESULT_CANCELED) {
            new Helper().showCustomToast(context, "You cancelled!");
        }

        //check Button Enabled View
        checkButtonEnabled();
    }


    private void checkValidations() {

        //select customer
        if (cuidNumber==null) new Helper().showCustomToast(context, "Please select Customer!");
            //customer name
        else if (Objects.requireNonNull(customerName.getText()).toString().trim().isEmpty())  new Helper().showCustomToast(this, "Please select Customer!");
            //project name
        else if (selectedProjectId ==0) new Helper().showCustomToast(context, "Please select Project Name!");
            //flat type
        else if (selectedFlatId ==0) new Helper().showCustomToast(context, "Please select unit category!");
            //visit date
        else if (sendAlreadySiteVisitDate==null) new Helper().showCustomToast(context, "Please select site visited date!");
            // visit time
        else if (sendAlreadySiteVisitTime==null) new Helper().showCustomToast(context, "Please select site visited time!");
        else
        {

            int id = radiogroup.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rb_yes:
                    //send data to generate token
                    if (cuidModel!=null)
                    {
                        Intent intent = new Intent(context, GenerateTokenActivity.class);
                        intent.putExtra("CUID", cuidNumber);
                        intent.putExtra("lead_id", lead_id);
                        intent.putExtra("Customer_Name", customer_name);
                        intent.putExtra("prefix", cuidModel.getPrefix());
                        intent.putExtra("first_name", cuidModel.getFirst_name());
                        intent.putExtra("middle_name", cuidModel.getMiddle_name());
                        intent.putExtra("last_name", cuidModel.getLast_name());
                        intent.putExtra("country_code", cuidModel.getCountry_code());
                        intent.putExtra("Project_Id", selectedProjectId);
                        intent.putExtra("Project_Name", selectedProjectName);
                        intent.putExtra("Flat_Id", selectedFlatId);
                        intent.putExtra("Flat_Type", selectedFlatType);
                        intent.putExtra("Visit_Date", sendAlreadySiteFormattedVisitDate);
                        intent.putExtra("Visit_Time", sendAlreadySiteVisitTimeFormatted);
                        intent.putExtra("Visit_Date_api", sendAlreadySiteVisitDate);
                        intent.putExtra("Visit_Time_api", sendAlreadySiteVisitTime);
                        intent.putExtra("lead_stage_id", selectedLeadStageId);
                        intent.putExtra("fromOther", 1);
                        intent.putExtra("mobile_number", cuidModel.getCustomer_mobile());
                        intent.putExtra("email", cuidModel.getCustomer_email());
                        intent.putExtra("is_kyc_uploaded", cuidModel.getIs_kyc_uploaded());
                        intent.putExtra("verified_by_id", user_id);
                        intent.putExtra("remark", edt_remarks.getText().toString());
                        startActivity(intent);
                    }
                    else new Helper().showCustomToast(context, "Failed to get customer details!");
                    break;
                case R.id.rb_no:
                    showSiteVisitConfirmationAlert();
                    break;
            }
        }

    }

    private void checkButtonEnabled()
    {

        if (cuidNumber==null) setButtonDisabledView();
            //customer name
        else if (Objects.requireNonNull(customerName.getText()).toString().trim().isEmpty())  setButtonDisabledView();
            //project name
        else if (selectedProjectId ==0) setButtonDisabledView();
            //flat type
        else if (selectedFlatId ==0) setButtonDisabledView();
            //visit date
        else if (sendAlreadySiteVisitDate==null) setButtonDisabledView();
            // visit time
        else if (sendAlreadySiteVisitTime==null) setButtonDisabledView();

        else {
            setButtonEnabledView();
        }

    }

    public void showSiteVisitConfirmationAlert()
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
        tv_desc.setText(getString(R.string.site_visit_confirm_text, customer_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            if (Helper.isNetworkAvailable(this)) {

                showProgressBar("Adding site visit...");
                call_addSiteVisit();

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

    private void call_addSiteVisit()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("lead_id",lead_id);
        jsonObject.addProperty("project_id", selectedProjectId);
        jsonObject.addProperty("unit_category_id", selectedFlatId);
        jsonObject.addProperty("visit_date", sendAlreadySiteVisitDate);
        jsonObject.addProperty("visit_time", sendAlreadySiteVisitTime);
        jsonObject.addProperty("visit_remark", edt_remarks.getText().toString());
        jsonObject.addProperty("conducted_by_id", user_id);
        jsonObject.addProperty("lead_stage_id", selectedLeadStageId);
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().addSiteVisit(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        AddNewSiteVisit();
                    } else {
                        showErrorLog("Failed to add site visit!");
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

    private void AddNewSiteVisit()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            Log.e(TAG, "AddNewSiteVisit: " );

            showSuccessAlert();

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.putBoolean("fromHomeScreen_AddSiteVisit", fromHomeScreen_AddSiteVisit);
                editor.putInt("isSiteVisitAdd", 1);
                editor.apply();
            }

            Log.e(TAG, "AddNewSiteVisit: "+fromHomeScreen_AddSiteVisit);
        });
    }


    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessAlert()
    {
        new Helper().showSuccessCustomToast(context, "Site Visit Added Successfully...!");
        new Handler().postDelayed(this::onBackPressed, 1000);
        flagExit=true;
    }


    /*Show Error Log*/
    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }



    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_submitSiteVisit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submitSiteVisit.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit lead

        mBtn_submitSiteVisit.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submitSiteVisit.setTextColor(getResources().getColor(R.color.main_white));
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
    public void onBackPressed()
    {
        //if (isVisitSubmitted) setResult(Activity.RESULT_OK, new Intent().putExtra("result", "Site Visit Added"));
        if(!flagExit)
        {
            showBackPressedIcons();

        }else super.onBackPressed();
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
            super.onBackPressed();
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