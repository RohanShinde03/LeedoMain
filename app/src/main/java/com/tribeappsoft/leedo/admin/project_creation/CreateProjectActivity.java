package com.tribeappsoft.leedo.admin.project_creation;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectTypeModel;
import com.tribeappsoft.leedo.api.ApiClient;
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

public class CreateProjectActivity extends AppCompatActivity {

    @BindView(R.id.cl_createProject) CoordinatorLayout parent;
    @BindView(R.id.edt_createProject_projectTitle) TextInputEditText edt_projectTitle;
    @BindView(R.id.edt_createProject_ProjectAddress) TextInputEditText edt_ProjectAddress;
    @BindView(R.id.edt_createProject_projectDescription) TextInputEditText edt_projectDescription;
    @BindView(R.id.edt_createProject_projectRegistrationNo) TextInputEditText edt_projectRegistrationNo;
    @BindView(R.id.til_createProject_ProjectCSNo) TextInputLayout til_ProjectCSNo;
    @BindView(R.id.acTv_createProject_selectProjectType) AutoCompleteTextView acTv_selectProjectType;
    @BindView(R.id.til_createProject_ProjectLatitude) TextInputLayout til_ProjectLatitude;
    @BindView(R.id.edt_createProject_ProjectLatitude) TextInputEditText edt_ProjectLatitude;
    @BindView(R.id.til_createProject_ProjectLongitude) TextInputLayout til_ProjectLongitude;
    @BindView(R.id.edt_createProject_ProjectLongitude) TextInputEditText edt_ProjectLongitude;
    @BindView(R.id.til_createProject_PermissionDate) TextInputLayout til_PermissionDate;
    @BindView(R.id.edt_createProject_PermissionDate) TextInputEditText edt_PermissionDate;
    @BindView(R.id.edt_createProject_ProjectCSNo) TextInputEditText edt_ProjectCSNo;
    @BindView(R.id.mBtn_createProject_submitProject) MaterialButton mBtn_submitProject;

    private ArrayList<ProjectTypeModel> projectTypeModelArrayList;
    private ArrayList<String> projectTypesStringArrayList;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    private AppCompatActivity context;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String api_token="";
    private int mYear, mMonth, mDay,fromOther = 1; //TODO fromOther ==> 1 - Create Project, 2- Edit/Update Project Info
    private String TAG="CreateProjectActivity",sendProjectPermissionDate=null,
            project_title="",project_address="",project_description="",project_RERA_no="",project_cs_no="",
            project_permission_date="", project_type ="",project_latitude="",project_longitude="",selectedProjectTypeName="";
    private int project_id=0, project_type_id =0;
    private int selectedProjectTypeId=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);
        ButterKnife.bind(this);
        context = CreateProjectActivity.this;
        //call method to hide keyBoard
        setupUI(parent);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        hideProgressBar();

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //get Intent
        if (getIntent()!=null) {
            fromOther = getIntent().getIntExtra("fromOther", 1);
            //data from update project
            project_id = getIntent().getIntExtra("project_id", 0);
            project_title = getIntent().getStringExtra("project_title");
            project_address = getIntent().getStringExtra("project_address");
            project_description = getIntent().getStringExtra("project_description");
            project_RERA_no = getIntent().getStringExtra("project_RERA_no");
            project_cs_no = getIntent().getStringExtra("project_cs_no");
            project_permission_date = getIntent().getStringExtra("project_permission_date");
            project_type_id = getIntent().getIntExtra("project_type_id",0);
            project_type = getIntent().getStringExtra("project_type");
            project_latitude = getIntent().getStringExtra("project_latitude");
            project_longitude = getIntent().getStringExtra("project_longitude");
        }

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromOther==2 ? getString(R.string.update_project_): getString(R.string.create_project_));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        projectTypeModelArrayList =new ArrayList<>();
        projectTypesStringArrayList =new ArrayList<>();

        mBtn_submitProject.setText(fromOther==2 ? getString(R.string.update_project_): getString(R.string.create_project));

        if (fromOther==2) {

            //update project details
            if (project_title != null && !project_title.trim().isEmpty()) edt_projectTitle.setText(project_title);
            if (project_address != null && !project_address.trim().isEmpty()) edt_ProjectAddress.setText(project_address);
            if (project_description != null && !project_description.trim().isEmpty()) edt_projectDescription.setText(project_description);
            if (project_RERA_no != null && !project_RERA_no.trim().isEmpty()) edt_projectRegistrationNo.setText(project_RERA_no);
            if (project_cs_no != null && !project_cs_no.trim().isEmpty()) edt_ProjectCSNo.setText(project_cs_no);
            if (project_permission_date != null && !project_permission_date.trim().isEmpty()) edt_PermissionDate.setText(project_permission_date);
            if (project_type != null && !project_type.trim().isEmpty()) acTv_selectProjectType.setText(project_type);
            if (project_latitude != null && !project_latitude.trim().isEmpty()) edt_ProjectLatitude.setText(project_latitude);
            if (project_longitude != null && !project_longitude.trim().isEmpty()) edt_ProjectLongitude.setText(project_longitude);
            sendProjectPermissionDate=project_permission_date;
            selectedProjectTypeId=project_type_id;
        }


        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar("getting project details...");
            new Handler(getMainLooper()).postDelayed(this::getProjectTypes, 100);
        }
        else { Helper.NetworkError(context);}

        //select call schedule date
        edt_PermissionDate.setOnClickListener(view -> selectProjectDate());

        //submit submit project
        mBtn_submitProject.setOnClickListener(view -> {

            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            if (fromOther==2) checkUpdateValidation();
            else checkValidation();
        });

        //checkEmail
        checkButtonEditTextChanged();

        //check Button Enabled View
        checkButtonEnabled();


    }

    private void getProjectTypes()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getProjectTypes(api_token);
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
                        setProjectTypes();
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
                                                projectTypeModelArrayList.clear();
                                                projectTypesStringArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++) {
                                                    setProjectTypesJson(jsonArray.get(i).getAsJsonObject());
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
    }//getProjectTypes()


    private void setProjectTypesJson(JsonObject jsonObject)
    {
        ProjectTypeModel myModel = new ProjectTypeModel();
        if (jsonObject.has("project_type_id")) myModel.setProject_type_id(!jsonObject.get("project_type_id").isJsonNull() ? jsonObject.get("project_type_id").getAsInt() : 0 );
        if (jsonObject.has("project_type"))
        {
            myModel.setProject_type(!jsonObject.get("project_type").isJsonNull() ? jsonObject.get("project_type").getAsString() : "" );
            projectTypesStringArrayList.add(!jsonObject.get("project_type").isJsonNull() ? jsonObject.get("project_type").getAsString() : "" );
        }
        projectTypeModelArrayList.add(myModel);
    }//setNameUserRolesJson()

    private void setProjectTypes()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            setAdapterProjectTypes();

            //checkButtonEnabled
            checkButtonEnabled();

        });
    }

    private void setAdapterProjectTypes()
    {

        if (projectTypesStringArrayList.size() >0 && projectTypeModelArrayList.size()>0)
        {
            Log.e(TAG, "setAdapterProjectTypes: " );
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, projectTypesStringArrayList);
            //set def selected
            // acTv_selectProjectType.setText(projectTypesStringArrayList.get(0));

            acTv_selectProjectType.setAdapter(adapter);
            acTv_selectProjectType.setThreshold(0);

            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());
            acTv_selectProjectType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (ProjectTypeModel pojo : projectTypeModelArrayList)
                {
                    if (pojo.getProject_type().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedProjectTypeId = pojo.getProject_type_id(); // This is the correct ID
                        selectedProjectTypeName = pojo.getProject_type();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Project Type Name & id " + selectedProjectTypeName +"\t"+ selectedProjectTypeId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }//setAdapterProjectTypes()


    private void checkButtonEditTextChanged() {

        edt_projectTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        edt_ProjectAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_ProjectCSNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void selectProjectDate()
    {
        //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
        //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
        //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
        //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
        //check button EnabledView
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_PermissionDate.setText(Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear + 1;
                    sendProjectPermissionDate = year + "-" + mth + "-" + dayOfMonth;


                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, " sendProjectPermission From Date: " + sendProjectPermissionDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }


    private void checkValidation() {
        //project name
        if (Objects.requireNonNull(edt_projectTitle.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project Title!");

            //project address
        else if (Objects.requireNonNull(edt_ProjectAddress.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project Address!");

            //project description
        else if (Objects.requireNonNull(edt_ProjectCSNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project CS No!");

            //project head
        else if (Objects.requireNonNull(acTv_selectProjectType.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please select Project Type!");

        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }

    private void checkUpdateValidation() {
        //project name
        if (Objects.requireNonNull(edt_projectTitle.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project Title!");

            //project address
        else if (Objects.requireNonNull(edt_ProjectAddress.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project Address!");

            //project description
        else if (Objects.requireNonNull(edt_ProjectCSNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Project CS No!");

            //project head
        else if (Objects.requireNonNull(acTv_selectProjectType.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please select Project Type!");

        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }


    private void showSubmitLeadAlertDialog()
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

        tv_msg.setText(fromOther==2 ? getString(R.string.update_project_question):getString(R.string.create_project_question));
        tv_desc.setText(fromOther==2 ?getString(R.string.update_project_confirmation):getString(R.string.create_project_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call create project
            if (Helper.isNetworkAvailable(context))
            {
                if (fromOther==2 ) {
                    // update project details
                    showProgressBar("Updating Project Details...");
                    new Handler().postDelayed(this::Call_UpdateProjectDetails,500);
                }
                else {

                    showProgressBar(getString(R.string.submitting_project_details));
                    new Handler().postDelayed(this::call_CreateProject,500);
                }
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

    private void call_CreateProject()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("project_type_id",selectedProjectTypeId);
        jsonObject.addProperty("project_name", Objects.requireNonNull(edt_projectTitle.getText()).toString());
        jsonObject.addProperty("address", Objects.requireNonNull(edt_ProjectAddress.getText()).toString());
        jsonObject.addProperty("description", Objects.requireNonNull(edt_projectDescription.getText()).toString());
        jsonObject.addProperty("reg_no", Objects.requireNonNull(edt_projectRegistrationNo.getText()).toString());
        jsonObject.addProperty("cs_no", Objects.requireNonNull(edt_ProjectCSNo.getText()).toString());
        jsonObject.addProperty("start_date",sendProjectPermissionDate!=null ? sendProjectPermissionDate : "" );
        jsonObject.addProperty("latitude", Objects.requireNonNull(edt_ProjectLatitude.getText()).toString());
        jsonObject.addProperty("longitude", Objects.requireNonNull(edt_ProjectLongitude.getText()).toString());

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().createProject(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        CreateNewProject();
                    }
                    /*else if ("2".equals(success)) {
                        //mobile number already exists
                        showErrorLog(context.getString(R.string.mob_number_exists));
                    }*/
                    else {
                        showErrorLog("Failed to create new project!");
                    }
                }
                else
                {
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
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void CreateNewProject()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            new Helper().showSuccessCustomToast(context, "New Project Created Successfully...!");
            new Handler().postDelayed(this::onBackPressed, 500);

            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isProjectCreateUpdate",1);
                editor.apply();
            }

        });
    }

    private void Call_UpdateProjectDetails() {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("project_id", project_id);
        jsonObject.addProperty("project_type_id",selectedProjectTypeId);
        jsonObject.addProperty("project_name", Objects.requireNonNull(edt_projectTitle.getText()).toString());
        jsonObject.addProperty("address", Objects.requireNonNull(edt_ProjectAddress.getText()).toString());
        jsonObject.addProperty("description", Objects.requireNonNull(edt_projectDescription.getText()).toString());
        jsonObject.addProperty("reg_no", Objects.requireNonNull(edt_projectRegistrationNo.getText()).toString());
        jsonObject.addProperty("cs_no", Objects.requireNonNull(edt_ProjectCSNo.getText()).toString());
        jsonObject.addProperty("start_date",sendProjectPermissionDate!=null ? sendProjectPermissionDate : "" );
        jsonObject.addProperty("latitude", Objects.requireNonNull(edt_ProjectLatitude.getText()).toString());
        jsonObject.addProperty("longitude", Objects.requireNonNull(edt_ProjectLongitude.getText()).toString());

        ApiClient client = ApiClient.getInstance();
        client.getApiService().updateProjectDetails(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e("response", "" + response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isJsonObject()) {
                        int isSuccess = 0;
                        if (response.body().has("success"))
                            isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess == 1) {
                            UpdateProjectDetails();
                        }
                        else showErrorLog("Error Occurred during update project details!");
                    }
                } else {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " " + response.code());
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

    private void UpdateProjectDetails() {
        runOnUiThread(() -> {

            Log.e(TAG, "UpdateProjectDetails");
            hideProgressBar();


            //show success toast
            new Helper().showSuccessCustomToast(context, "Project Details Updated Successfully!" );
            new Handler().postDelayed(this::onBackPressed, 500);

            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isProjectCreateUpdate",1);
                editor.apply();
            }
        });
    }



    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
            context.runOnUiThread(() -> {

                //hide pb
                hideProgressBar();

                Helper.onErrorSnack(context, message);

            });
    }

    private void checkButtonEnabled()
    {
        //project title
        if (Objects.requireNonNull(edt_projectTitle.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_projectTitle.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_ProjectCSNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project head
        else  if (Objects.requireNonNull(acTv_selectProjectType.getText()).toString().trim().isEmpty()) setButtonDisabledView();
        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit project
        mBtn_submitProject.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submitProject.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit project

        mBtn_submitProject.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submitProject.setTextColor(getResources().getColor(R.color.main_white));
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
