package com.tribeappsoft.leedo.admin.project_quotations;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.filepicker.MaterialFilePicker;
import com.tribeappsoft.leedo.util.filepicker.ui.FilePickerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewQuotationActivity extends AppCompatActivity {

    private String TAG = "AddNewQuotationActivity";
    @BindView(R.id.edt_addQuotation_title) TextInputEditText edt_addQuotation_title;
    @BindView(R.id.edt_addQuotation_description) TextInputEditText edt_addQuotation_description;
    @BindView(R.id.tv_addQuotation_select_file) AppCompatTextView tv_addQuotation_select_file;
    @BindView(R.id.iv_addQuotation_browse) AppCompatImageView iv_addQuotation_browse;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.mBtn_addQuotation_submitQuotation) MaterialButton mBtn_submitQuotation;
    @BindView(R.id.acTv_newQuotation_select_project) AutoCompleteTextView acTv_select_project;

    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;
    public SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ArrayList<ProjectModel> projectNameModelArrayList;
    private ArrayList<String> nameProjectArrayList;

    //private EventProjectDocsModel myUploadModel = null;
    Activity context;
    private int selectedProjectID=0,project_quotation_id=0,fromOther = 1; //TODO fromOther ==> 1 - Add project Quotation,2.update project Quotation,
    private String api_token="",quotationUrl=null,selectedProjectName="",quotation_title="",media_path=null,project_quotation_desc="";
    private int project_docType_id = 3,mediaTypeId = 0;
    //project_docType_id : 1 =>Brochures, 2=>floor plan, 3=>Quotations
    //mediaTypeId : 1=>document, 2=>video, 3=>audio, 4=>photo


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_quotation);
        ButterKnife.bind(this);
        context = AddNewQuotationActivity.this;

        //get Intent
        if (getIntent()!=null) {
            fromOther = getIntent().getIntExtra("fromOther", 1);
            project_quotation_id = getIntent().getIntExtra("project_brochure_id", 0);
            selectedProjectID = getIntent().getIntExtra("project_id", 0);

            //data from update brochure
            quotation_title = getIntent().getStringExtra("brochure_title");
            selectedProjectName = getIntent().getStringExtra("project_name");
            mediaTypeId = getIntent().getIntExtra("media_type_id",0);

            Log.e(TAG, "onCreate: "+""+selectedProjectName+""+project_quotation_id);
            media_path = getIntent().getStringExtra("media_path");
            project_quotation_desc = getIntent().getStringExtra("project_brochure_desc");
        }

        hideProgressBar();

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromOther==2 ? getString(R.string.title_update_quotation):getString(R.string.title_add_quotation));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        //user_id = sharedPreferences.getInt("user_id", 0);
        api_token = sharedPreferences.getString("api_token", "");
        editor.apply();

        projectNameModelArrayList=new ArrayList<>();
        nameProjectArrayList=new ArrayList<>();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {

            showProgressBar("Please wait...");
            new Handler(getMainLooper()).postDelayed(this::call_getAllProjects, 100);
        }
        else
        {
            //hide pb
            hideProgressBar();

            Helper.NetworkError(context);
        }

        if (fromOther==2) {

            //update team lead info
            if (quotation_title != null && !quotation_title.trim().isEmpty()) edt_addQuotation_title.setText(quotation_title);
            if (media_path != null && !media_path.trim().isEmpty()) tv_addQuotation_select_file.setText(media_path);
            if (project_quotation_desc != null && !project_quotation_desc.trim().isEmpty()) edt_addQuotation_description.setText(project_quotation_desc);
            if (selectedProjectName != null && !selectedProjectName.trim().isEmpty()) acTv_select_project.setText(selectedProjectName);

        }

        mBtn_submitQuotation.setText(fromOther==2 ? getString(R.string.update_quotation): getString(R.string.submit_quotation));

        edt_addQuotation_description.addTextChangedListener(new TextWatcher() {
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

        edt_addQuotation_description.addTextChangedListener(new TextWatcher() {
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

        //set keyboard open
        Helper.showSoftKeyboard(context, edt_addQuotation_title);
    }



    private void call_getAllProjects()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAllProjects(api_token).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {
                                //set json
                                setJson(response.body());
                                //set delayRefresh
                                new Handler().postDelayed(() -> setProjectNames(), 1000);
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
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

    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                //clear list
                projectNameModelArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setProjectJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }


    private void setProjectJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        if (jsonObject.has("project_name"))
        {
            model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
            nameProjectArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
        }
        projectNameModelArrayList.add(model);
    }


    private void setProjectNames()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            //set adapter for project names
            setAdapterProjectNames();

            //check button enabled
            checkButtonEnabled();

            iv_addQuotation_browse.setOnClickListener(view -> selectDocumentPopup());

            mBtn_submitQuotation.setOnClickListener(view -> {
                Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

                if (fromOther ==2) checkUpdateValidation();
                else checkValidation();
            });

            if (fromOther==2)
            {
                //set request focus for update only

                //set def edit text request focused
                edt_addQuotation_title.requestFocus();
                //set def selection
                edt_addQuotation_title.setSelection(Objects.requireNonNull(edt_addQuotation_title.getText()).toString().length());
                //set keyboard open
                Helper.showSoftKeyboard(context, edt_addQuotation_title);
            }

        });
    }

    private void setAdapterProjectNames()
    {

        runOnUiThread(() -> {

            if (nameProjectArrayList.size() >0 && projectNameModelArrayList.size()>0)
            {
                //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, nameProjectArrayList);
                //set def selected
                //acTv_select_project.setText(nameProjectArrayList.get(0));
                // selectedProjectID = projectNameModelArrayList.get(0).getProject_id();
                //selectedProjectName = projectNameModelArrayList.get(0).getProject_name();

                acTv_select_project.setAdapter(adapter);
                acTv_select_project.setThreshold(0);

                //tv_selectCustomer.setSelection(0);
                //autoComplete_firmName.setValidator(new Validator());
                //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());

                acTv_select_project.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                {
                    String itemName = adapter.getItem(position);

                    for (ProjectModel pojo : projectNameModelArrayList)
                    {
                        if (pojo.getProject_name().equals(itemName))
                        {
                            //int id = pojo.getCompany_id(); // This is the correct ID
                            selectedProjectID = pojo.getProject_id(); // This is the correct ID
                            selectedProjectName = pojo.getProject_name();
                            //fixedEnquiryID+=2;
                            Log.e(TAG, "Project Name & id " + selectedProjectName +"\t"+ selectedProjectID);

                            //check button EnabledView
                            checkButtonEnabled();

                            break; // No need to keep looping once you found it.
                        }
                    }
                });
            }

        });

    }


    private void checkValidation() {
        //project id
        if (selectedProjectID==0) new Helper().showCustomToast(context, "Please select project name!");

            //project title
        else if (Objects.requireNonNull(edt_addQuotation_title.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter project title!");
            //project description
            // else if (Objects.requireNonNull(edt_addBrochure_description.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter project description!");

        else if (quotationUrl==null) new Helper().showCustomToast(context, "Please select project quotation!");

        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }

    private void checkUpdateValidation() {
        //project id
        if (selectedProjectID==0) new Helper().showCustomToast(context, "Please select project name!");

            //project title
        else if (Objects.requireNonNull(edt_addQuotation_title.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter project title!");
            //project description
            //else if (Objects.requireNonNull(edt_addBrochure_description.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter project description!");

            //else if (brochureUrl==null) new Helper().showCustomToast(context, "Please select project brochure!");

        else
        {
            //show confirmation dialog
            showSubmitLeadAlertDialog();
        }
    }


    private void checkButtonEnabled()
    {

        //project id
        Log.e(TAG, "checkButtonEnabled: selectedProjectID"+selectedProjectID );
        if (selectedProjectID==0)setButtonDisabledView();

            //project title
        else if (Objects.requireNonNull(edt_addQuotation_title.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project description
            // else if (Objects.requireNonNull(edt_addBrochure_description.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //check brochure url selected only for Add new brochure
        else if (fromOther==1 && quotationUrl==null)setButtonDisabledView();

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
        mBtn_submitQuotation.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_submitQuotation.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit team lead

        mBtn_submitQuotation.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_submitQuotation.setTextColor(getResources().getColor(R.color.main_white));
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
                .withFilter(Pattern.compile(".*\\.(txt|pdf|doc|docx|odt|xls|xlsx)$"))
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

        if (requestCode == 1)   //From Camera
        {

            if (responseCode == RESULT_OK) {
                try {

                    if (data != null) {


                        //String myDocument = Objects.requireNonNull(data.getExtras()).getString("myDocument");
                        //int  position  = data.getExtras().getInt("position", 0);
                        //Log.e("myDocument & pos ", myDocument +" "+position);

                        mediaTypeId = 4;
                        Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                        Uri tempUri = getImageUri(getApplicationContext(), Objects.requireNonNull(photo));

                        // CALL THIS METHOD TO GET THE ACTUAL PATH
                        File finalFile = new File(getRealPathFromURI(tempUri));

                        Log.e("finalFile", finalFile.getAbsolutePath());
                        Log.e("finalFile", "ab path");
                        Log.e("finalFile", finalFile.getPath());
                        Log.e("finalFile", finalFile.toString());

                        Log.e(TAG, "onActivityResult: " + getFileName_from_filePath(finalFile.getAbsolutePath()));
                        //tv_UploadedDoc_Name.setText(getFileName_from_filePath(finalFile.getAbsolutePath()));

                        setSelectedDoc( finalFile.getAbsolutePath());
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

                    mediaTypeId = 4;

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


                        setSelectedDoc( photoUrl);
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

                mediaTypeId = 1;

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
                Log.e(TAG, ""+photoUrl);
                Log.e(TAG, "ext " + ext);
                Log.e(TAG, "mimetype " + mimetype);

                setSelectedDoc(photoUrl);
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


    private void setSelectedDoc(String absolutePath) {

        //hide pb
        hideProgressBar();

        tv_addQuotation_select_file.setText(absolutePath!=null?absolutePath : getString(R.string.no_file_choose));
        quotationUrl=absolutePath;
        Log.e(TAG, "setSelectedDoc: brochureUrl"+quotationUrl );

        //check button Enabled
        checkButtonEnabled();
    }




    private void showSubmitLeadAlertDialog()
    {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(fromOther==2 ? getString(R.string.update_project_quotation_question) : getString(R.string.submit_project_quotation_question));
        tv_desc.setText(fromOther==2 ? getString(R.string.update_project_quotation_confirmation): getString(R.string.submit_project_quotation_confirmation));
        btn_negativeButton.setText(getString(R.string.cancel));
        btn_positiveButton.setText(getString(R.string.submit));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                if (fromOther==2 ) {
                    // update project brochure
                    showProgressBar(getString(R.string.updating_projectQuotation_details));
                    call_updateProjectQuotation();
                }
                else {

                    showProgressBar(getString(R.string.submitting_projectQuotation_details));
                    new Handler().postDelayed(() -> call_addProjectQuotation(quotationUrl),500);
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


    private void call_addProjectQuotation(String path)
    {
        MultipartBody.Part fileUpload=null;
        if(path!=null && !path.trim().isEmpty())
        {
            File profile_path_part = new File(path);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), profile_path_part);
            fileUpload = MultipartBody.Part.createFormData("file_url", profile_path_part.getName(), uploadFile);
        }

        RequestBody api_tokenPart = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody projectID = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedProjectID));
        RequestBody quotation_title = RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNull(edt_addQuotation_title.getText()).toString());
        RequestBody quotation_description = RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNull(edt_addQuotation_description.getText()).toString());
        RequestBody project_doc_type_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(project_docType_id));
        RequestBody media_type_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(mediaTypeId));

        ApiClient client = ApiClient.getInstance();
        client.getApiService().call_addProjectDocs(api_tokenPart,projectID,project_doc_type_id,quotation_title,quotation_description,media_type_id,fileUpload).enqueue(new Callback<JsonObject>() {
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
                            //show success popup
                            onSuccessDetailsSubmit();
                        }
                        else showErrorLog("Error occurred during registration! Please try again!");
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void onSuccessDetailsSubmit()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("IsBrochureAdded", true);
                editor.apply();
            }

            //do backPress
            onBackPressed();

            //show success toast
            new Helper().showSuccessCustomToast(context, "Project Quotation Uploaded Successfully!" );
        });
    }


    private void call_updateProjectQuotation()
    {
        MultipartBody.Part fileUpload=null;
        if(quotationUrl!=null && !quotationUrl.trim().isEmpty())
        {
            File profile_path_part = new File(quotationUrl);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), profile_path_part);
            fileUpload = MultipartBody.Part.createFormData("file_url", profile_path_part.getName(), uploadFile);
        }

        RequestBody api_tokenPart = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody projectID = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedProjectID));
        RequestBody brochure_title = RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNull(edt_addQuotation_title.getText()).toString());
        RequestBody brochure_description = RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNull(edt_addQuotation_description.getText()).toString());
        RequestBody project_doc_type_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(project_docType_id));
        RequestBody project_doc_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(project_quotation_id));
        RequestBody media_type_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(mediaTypeId));

        ApiClient client = ApiClient.getInstance();
        client.getApiService().call_updateProjectDocs(api_tokenPart,projectID,project_doc_type_id,brochure_title,brochure_description,media_type_id,project_doc_id,fileUpload).enqueue(new Callback<JsonObject>() {
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
                            //show success popup
                            UpdateProjectQuotation();
                        }
                        else showErrorLog("Error occurred during update Quotation! Please try again!");
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
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }



    private void UpdateProjectQuotation() {
        runOnUiThread(() -> {

            hideProgressBar();

            //do backPress
            onBackPressed();

            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("IsBrochureAdded", true);
                editor.apply();
            }

            //show success toast
            new Helper().showSuccessCustomToast(context, "Project Quotation updated Successfully!" );

        });
    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
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
