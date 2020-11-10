package com.tribeappsoft.leedo.admin.booked_customers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TimePicker;

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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.CustomerIdActivity;
import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.models.project.UnitCategoriesModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.filepicker_ss.Constant;
import com.tribeappsoft.leedo.util.filepicker_ss.activity.ImagePickActivity;
import com.tribeappsoft.leedo.util.filepicker_ss.activity.NormalFilePickActivity;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.AudioFile;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.ImageFile;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.NormalFile;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.VideoFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.filepicker_ss.activity.BaseActivity.IS_NEED_FOLDER_LIST;
import static com.tribeappsoft.leedo.util.filepicker_ss.activity.ImagePickActivity.IS_NEED_CAMERA;

public class MarkAsBook_Activity extends AppCompatActivity {

    @BindView(R.id.cl_siteVisit) CoordinatorLayout parent;
    @BindView(R.id.tv_markAsBook_salesRepName) AppCompatTextView tv_sales_representative;
    @BindView(R.id.tv_markAsBook_select_customer_mobile) AppCompatTextView customerId;
    @BindView(R.id.tv_customerName_markAsBook) AppCompatTextView customerName;
    @BindView(R.id.acTv_markAsBook_project_name) AutoCompleteTextView acTv_projectName;
    @BindView(R.id.acTv_markAsBook_selectUnitType) AutoCompleteTextView acTv_selectUnitType;
    @BindView(R.id.edt_markAsBook_unitNo) TextInputEditText edt_unitNo;
    @BindView(R.id.tv_markAsBook_select_file) AppCompatTextView tv_addBrochure_select_file;
    @BindView(R.id.iv_markAsBook_browse) AppCompatImageView iv_markAsBook_browse;
    @BindView(R.id.mBtn_markAsBook_markAsBook) MaterialButton mBtn_markAsBook;
    @BindView(R.id.edt_markAsBook_date) TextInputEditText BookDate;
    @BindView(R.id.edt_markAsBook_time) TextInputEditText BookTime;
    @BindView(R.id.edt_markAsBook_remarks) TextInputEditText edt_remarks;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private static final int  Permission_CODE_Camera= 1234;
    private static final int  Permission_CODE_Gallery= 567;
    private static final int Permission_CODE_DOC = 657;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "MarkAsBook_Activity";
    private AppCompatActivity context;
    private CUIDModel cuidModel = null;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<String> projectStringArrayList, flatStringArrayList;

    private String cuidNumber, customer_name="", selectedProjectName = "", selectedFlatType = "",filePath="",
            sendAlreadySiteVisitTime = null, full_name ="",api_token="",BookingApplicationUrl=null, sendBookingDate = null, sendBookingTimeFormatted ="",
            sendAlreadySiteFormattedVisitDate="";

    public int selectedProjectId  = 0,user_id=0, lead_id =0, mYear, mMonth, mDay, selectedFlatTypeId =0;
    private boolean fromHomeScreen_AddBooking=false,UnitBooked=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_as_book_);
        ButterKnife.bind(this);
        context = MarkAsBook_Activity.this;
        //call method to hide keyBoard
        setupUI(parent);

        if (getIntent() != null) {
            //get cuId model
            cuidModel = (CUIDModel) getIntent().getSerializableExtra("cuidModel");
            cuidNumber =  getIntent().getStringExtra("lead_cu_id");
            customer_name =  getIntent().getStringExtra("lead_name");
            fromHomeScreen_AddBooking =  getIntent().getBooleanExtra("fromHomeScreen_AddBooking",false);
            lead_id = getIntent().getIntExtra("lead_id", 0);
        }

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.mark_as_book));

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


        if (getIntent() != null) {
            //get cuId model
            cuidModel = (CUIDModel) getIntent().getSerializableExtra("cuidModel");
            cuidNumber =  getIntent().getStringExtra("lead_cu_id");
            customer_name =  getIntent().getStringExtra("lead_name");
            lead_id = getIntent().getIntExtra("lead_id", 0);
        }


        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //hide pb
        hideProgressBar();


        projectStringArrayList = new ArrayList<>();
        projectModelArrayList = new ArrayList<>();
        flatStringArrayList = new ArrayList<>();


        if (cuidModel != null) {

            //From Direct CuId Activity && Feeds
            cuidNumber = cuidModel.getCustomer_mobile();
            customer_name = cuidModel.getCustomer_name();
            lead_id = cuidModel.getLead_id();
            customerId.setText(cuidNumber);
            customerName.setText(customer_name);

            //check Button Enabled View
            // checkButtonEnabled();
        }

        //visit date time
        BookDate.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select date
            selectVisitDate();
        });

        BookTime.setOnClickListener(view -> {
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            //select time
            selectVisitTime();
        });

        /*  Get Project & Unit Type*/
        if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar(getString(R.string.please_wait));
            new Thread(this::getProjectListData).start();
        } else Helper.NetworkError(context);


        customerId.setOnClickListener(v ->{
            //hide keyboard
            Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());

            startActivity(new Intent(context, CustomerIdActivity.class)
                    .putExtra("fromSiteVisit_or_token", 9)
                    .putExtra("forId", 9));
            finish();
        });


        edt_unitNo.addTextChangedListener(new TextWatcher() {
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


    private void getProjectListData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getBookFormData(api_token,user_id);
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

    /*Delay Refresh*/
    private void delayRefresh()    {

        //reply
        if (context!=null) {
            context.runOnUiThread(() -> {

                hideProgressBar();

                //set adapter for ref project name
                setAdapterProjectNames();

                iv_markAsBook_browse.setOnClickListener(view -> selectDocumentPopup());

                mBtn_markAsBook.setOnClickListener(view -> {
                    Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
                    checkValidations();
                });


            });
        }
    }

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
                    acTv_selectUnitType.setText("");
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
            acTv_selectUnitType.setAdapter(adapter);
            acTv_selectUnitType.setThreshold(0);
            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectUnitType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);
                for (UnitCategoriesModel pojo : categoriesModelArrayList)
                {
                    if (pojo.getUnit_category().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedFlatTypeId = pojo.getUnit_category_id(); // This is the correct ID
                        selectedFlatType = pojo.getUnit_category();

                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Unit category & id " + selectedFlatType +"\t"+ selectedFlatTypeId);

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

    /*Show Error Log*/
    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            Helper.onErrorSnack(context, message);
        });

    }


    private void selectVisitDate()
    {
        //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
        //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.MyDatePicker,
                (view, year, monthOfYear, dayOfMonth) -> {


                    sendAlreadySiteFormattedVisitDate = Helper.setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    BookDate.setText(sendAlreadySiteFormattedVisitDate);

                    int mth = monthOfYear + 1;
                    sendBookingDate = year + "-" + (mth < 10 ? "0" + mth : mth) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "al_siteVisit_send Date:: " + sendBookingDate);

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
                    sendBookingTimeFormatted = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                    BookTime.setText(sendBookingTimeFormatted);
                    //starTime = String.format( Locale.getDefault(), "%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");

                    if (sendAlreadySiteVisitTime!=null) Log.e(TAG, "Al Visit Time: "+sendAlreadySiteVisitTime);

                    //set button view enabled
                    //if (isAssignLater && !tv_committedDate.getText().toString().trim().isEmpty()) setButtonEnabledView();

                    //check Button Enabled View
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
        Intent intent1 = new Intent(this, ImagePickActivity.class);
        intent1.putExtra(IS_NEED_CAMERA, true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        intent1.putExtra(IS_NEED_FOLDER_LIST, true);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_IMAGE);

        /*Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);*/
    }

    private void OpenDocuments() {
        Intent intent4 = new Intent(context, NormalFilePickActivity.class);
        intent4.putExtra(Constant.MAX_NUMBER, 1);
        intent4.putExtra(IS_NEED_FOLDER_LIST, true);
        intent4.putExtra(NormalFilePickActivity.SUFFIX,
                new String[] {"xlsx", "xls", "doc", "dOcX", "ppt", "pptx", "pdf","csv","txt","docx"});
        startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);
    }

/*    void OpenGallery()
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

    }*/


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

        switch (requestCode) {
            case Constant.REQUEST_CODE_PICK_IMAGE:
                if (responseCode == RESULT_OK) {
                    ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
                    StringBuilder builder = new StringBuilder();
                    for (ImageFile file : list) {
                        String path = file.getPath();
                        BookingApplicationUrl=path;
                        builder.append(path + "\n");
                    }
                    tv_addBrochure_select_file.setText(!builder.toString().trim().isEmpty() && builder.toString()!=null ? getFileName_from_filePath(builder.toString()) :"No file chosen");
                    //check button Enabled
                    checkButtonEnabled();
                }
                break;
            case Constant.REQUEST_CODE_PICK_VIDEO:
                if (responseCode == RESULT_OK) {
                    ArrayList<VideoFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_VIDEO);
                    StringBuilder builder = new StringBuilder();
                    for (VideoFile file : list) {
                        String path = file.getPath();
                        builder.append(path + "\n");
                    }
                    tv_addBrochure_select_file.setText(!builder.toString().trim().isEmpty() && builder.toString()!=null ? getFileName_from_filePath(builder.toString()):"No file chosen");
                    //check button Enabled
                    checkButtonEnabled();
                }
                break;
            case Constant.REQUEST_CODE_PICK_AUDIO:
                if (responseCode == RESULT_OK) {
                    ArrayList<AudioFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_AUDIO);
                    StringBuilder builder = new StringBuilder();
                    for (AudioFile file : list) {
                        String path = file.getPath();
                        builder.append(path + "\n");
                    }
                    tv_addBrochure_select_file.setText(!builder.toString().trim().isEmpty() && builder.toString()!=null ? getFileName_from_filePath(builder.toString()) :"No file chosen");
                    //check button Enabled
                    checkButtonEnabled();
                }
                break;
            case Constant.REQUEST_CODE_PICK_FILE:
                if (responseCode == RESULT_OK) {
                    ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    StringBuilder builder = new StringBuilder();
                    for (NormalFile file : list) {
                        String path = file.getPath();
                        filePath=path;
                        BookingApplicationUrl=path;
                        builder.append(path + "\n");
                    }
                    tv_addBrochure_select_file.setText(!builder.toString().trim().isEmpty() && builder.toString()!=null ? getFileName_from_filePath(builder.toString()) :"No file chosen");
                    Log.e(TAG, "onActivityResult: filePath"+filePath);
                    //check button Enabled
                    checkButtonEnabled();
                }
                break;
        }

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

        }
     /*   else if (requestCode == 2)  //From Gallery
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

        }*/

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

        tv_addBrochure_select_file.setText(absolutePath!=null?absolutePath : getString(R.string.no_file_choose));
        BookingApplicationUrl=absolutePath;
        Log.e(TAG, "setSelectedDoc: BookingApplicationUrl"+BookingApplicationUrl );

        //check button Enabled
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
        else if (selectedFlatTypeId ==0) new Helper().showCustomToast(context, "Please select unit category!");
            //unit number
        else if (Objects.requireNonNull(edt_unitNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter unit number!");
            //booking date
        else if (sendBookingDate ==null) new Helper().showCustomToast(context, "Please select booking date!");
            // booking time
        else if (sendAlreadySiteVisitTime==null) new Helper().showCustomToast(context, "Please select booking time!");
        else
        {
            showMArkAsBookConfirmationAlert();
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
        else if (selectedFlatTypeId ==0) setButtonDisabledView();
            //unit no
        else if (Objects.requireNonNull(edt_unitNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //visit date
        else if (sendBookingDate ==null) setButtonDisabledView();
            // visit time
        else if (sendAlreadySiteVisitTime==null) setButtonDisabledView();

        else {
            setButtonEnabledView();
        }

    }
    public void showMArkAsBookConfirmationAlert()
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
        tv_desc.setText(getString(R.string.site_visit_confirm_mark_book, customer_name));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            //call add site visit api
            if (Helper.isNetworkAvailable(this)) {

                showProgressBar("Submitting booking details...");
                call_MarkAsBook();

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

    private void call_MarkAsBook()
    {
        MultipartBody.Part fileUpload=null;
        if(BookingApplicationUrl!=null && !BookingApplicationUrl.trim().isEmpty())
        {
            File profile_path_part = new File(BookingApplicationUrl);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), profile_path_part);
            fileUpload = MultipartBody.Part.createFormData("file_url", profile_path_part.getName(), uploadFile);
        }
        RequestBody api_tokenPart = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody booking_datetime = RequestBody.create(MediaType.parse("text/plain"), sendBookingDate +" "+ sendAlreadySiteVisitTime);
        RequestBody project_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedProjectId));
        RequestBody lead_id_ = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lead_id));
        RequestBody unit_category_id_ = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedFlatTypeId));
        RequestBody unit_name_ = RequestBody.create(MediaType.parse("text/plain") ,Objects.requireNonNull(edt_unitNo.getText()).toString() );
        RequestBody book_remark = RequestBody.create(MediaType.parse("text/plain") ,Objects.requireNonNull(edt_remarks.getText()).toString() );
        RequestBody media_type_id = RequestBody.create(MediaType.parse("text/plain") , String.valueOf(1));
        RequestBody sales_person_id_ = RequestBody.create(MediaType.parse("text/plain") , String.valueOf(user_id));

        ApiClient client = ApiClient.getInstance();
        client.getApiService().call_addBooking(fileUpload,api_tokenPart,lead_id_,project_id,unit_category_id_,booking_datetime,unit_name_,book_remark,media_type_id,sales_person_id_).enqueue(new Callback<JsonObject>() {
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
                            MarkAsBookSuccess();
                        }
                        else showErrorLog("Error occurred during book unit! Please try again!");
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

    private void MarkAsBookSuccess()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            Log.e(TAG, "Mark As Booked" );

            UnitBooked=true;
            showSuccessAlert();

            //set Feed Action Added to true
            if(sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();
                editor.putBoolean("feedActionAdded", true);
                editor.apply();
            }

        });
    }


    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showSuccessAlert()
    {
        new Helper().showSuccessCustomToast(context, "Unit Booked Successfully...!");
        new Handler().postDelayed(this::onBackPressed, 1000);
    }



    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit lead
        mBtn_markAsBook.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_markAsBook.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit lead

        mBtn_markAsBook.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_markAsBook.setTextColor(getResources().getColor(R.color.main_white));
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
        if(fromHomeScreen_AddBooking && UnitBooked) {
            startActivity(new Intent(context, BookedCustomersActivity.class));
            finish();
        }
        else{
            super.onBackPressed();
            overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }
}