package com.tribeappsoft.leedo.admin.reports;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.BuildConfig;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.getFileName_from_filePath;
import static com.tribeappsoft.leedo.util.Helper.getLongNextDateFromString;
import static com.tribeappsoft.leedo.util.Helper.getTodaysFormattedDateTime;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;
import static com.tribeappsoft.leedo.util.Helper.setDatePickerFormatDateFromString;

public class ExportReportActivity extends AppCompatActivity {

    @BindView(R.id.cl_expExport) CoordinatorLayout parent;
    @BindView(R.id.edt_expReport_fromDate) TextInputEditText edt_fromDate;
    @BindView(R.id.edt_expReport_toDate) TextInputEditText edt_toDate;
    @BindView(R.id.mBtn_expReport_exportReport) MaterialButton mBtn_exportReport;

    // Exported excel view
    @BindView(R.id.ll_expReport_viewReportMain) LinearLayoutCompat ll_viewReportMain;
    @BindView(R.id.ll_expReport_viewReport) LinearLayoutCompat ll_viewReport;
    @BindView(R.id.mBtn_expReport_shareReport) MaterialButton mBtn_shareReport;
    @BindView(R.id.mTv_expReport_docName) MaterialTextView mTv_docName;
    @BindView(R.id.mTv_expReport_docSize) MaterialTextView mTv_docSize;

    //pb
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private AppCompatActivity context;
    private DatePickerDialog datePickerDialog;
    private int mYear, mMonth, mDay;
    private String TAG = "ExportReportActivity", api_token = "", sendFromDate = null, sendToDate = null;
    private int user_id=0;
    private static final int Permission_CODE_DOC = 657;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 456;
    private File dirFileUpload = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_report);

        ButterKnife.bind(this);
        context= ExportReportActivity.this;

        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(getString(R.string.menu_export_report));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //init
        init();
    }

    private void init()
    {
        //hide pb
        hideProgressBar();

        //initialise shared pref
        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        editor.apply();

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //init Gmail Login
        initialiseGMailLogin();

        //select from date
        edt_fromDate.setOnClickListener(view -> selectVisitPrefFromDate());

        //select to date
        edt_toDate.setOnClickListener(view -> {
            if (sendFromDate !=null) selectVisitPrefToDate();
            else new Helper().showCustomToast(context, "Please select from date first!");
        });

        //apply filter
        mBtn_exportReport.setOnClickListener(view -> {
            //check validation
            checkValidations();
        });

    }

    private void selectVisitPrefFromDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_fromDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendFromDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "From Date: "+ sendFromDate);

                    //set to date null or empty
                    sendToDate = null;
                    edt_toDate.setText("");

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void selectVisitPrefToDate()
    {
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(context),
                (view, year, monthOfYear, dayOfMonth) -> {

                    edt_toDate.setText(setDatePickerFormatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year)));
                    //startDate = formatDateFromString(String.format(Locale.getDefault(), "%d-%d-%d", dayOfMonth, monthOfYear + 1, year));

                    //sendUnderObsDate = String.format(Locale.getDefault(), "%d-%d-%d",  year, monthOfYear + 1, dayOfMonth);
                    int mth = monthOfYear+1;
                    sendToDate = year + "-" + mth + "-" + dayOfMonth;

                    //startDate = MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year);
                    //tv_startDate.setText(MessageFormat.format("{0}-{1}-{2}", dayOfMonth, monthOfYear + 1, year));
                    Log.e(TAG, "To Date: "+ sendToDate);

                    //check button EnabledView
                    checkButtonEnabled();

                }, mYear, mMonth, mDay);

        //set min date as site visit from date
        datePickerDialog.getDatePicker().setMinDate(getLongNextDateFromString(sendFromDate));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    private void checkValidations()
    {
            //project
        if (sendFromDate ==null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select at report dates!");

            //check if only from date selected
        else if (sendFromDate !=null && sendToDate ==  null) new Helper().showCustomToast(context, "Please select To-Date!");

        else {
            //apply filter

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkWriteStoragePermission()) callToExportExcelAPI();
                else requestPermission_for_Documents();
            }
            else callToExportExcelAPI();
        }
    }

    private void callToExportExcelAPI()
    {
        //Get meetings data
        if (isNetworkAvailable(Objects.requireNonNull(context))) {

            showProgressBar(getString(R.string.please_wait));
            new Handler(getMainLooper()).postDelayed(this::call_getLeadSummaryReport, 100);
        }
        else {
            NetworkError(context);
            //hide main layouts
            hideProgressBar();
        }
    }


    private boolean checkWriteStoragePermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission_for_Documents()
    {

      /*  if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {

            //download  //view  //share
            downloadFile(path, localFile, isDownloadViewShare);
            return;
        }*/
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
                }, Permission_CODE_DOC);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Checking the request code of our request
        if (requestCode == Permission_CODE_DOC)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open documents once permission is granted
                callToExportExcelAPI();
            }
//            else
//            {
//                //Displaying another toast if permission is not granted
//                permission_grant_docs =0;
//            }

        }

    }



    private void call_getLeadSummaryReport()
    {
        ApiClient client = ApiClient.getInstance();
        Call<ResponseBody> call = client.getApiService().getExportToExcel(api_token, 0, user_id, sendFromDate, sendToDate, "xlsx");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                if(response.isSuccessful()) {
                    onSuccessExportReport(response.body());
                }else {

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

                    context.runOnUiThread(() -> {
                        //hide pb
                        hideProgressBar();
                        new Helper().showCustomToast(context, "Failed to generate the report! Please try again.");
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

                context.runOnUiThread(() -> {
                    //hide pb
                    hideProgressBar();
                    new Helper().showCustomToast(context, "Unable to generate the Report! Try again.");
                });
            }
        });
    }


    private void onSuccessExportReport(ResponseBody body)
    {
        runOnUiThread(() -> {

            try {

                //first create parent directory
                File parentDirFile = new File(Environment.getExternalStorageDirectory() + File.separator + "/LeadManagement/"); //Tokens/
                //create parent directory
                parentDirFile.mkdir();

                //child directory
                File dirFile = new File(Environment.getExternalStorageDirectory() + File.separator +  "/LeadManagement/Reports/");

                //out file name
                String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_", Locale.getDefault()).format(new Date());

                File file = new File(dirFile.getPath(), "Report_"+fileName +".xlsx"); //xlsx
                //create file dir
                dirFile.mkdir();

                saveFile(Objects.requireNonNull(body), file);

            } catch (IOException e) {
                e.printStackTrace();
                context.runOnUiThread(() -> {
                    //hide pb
                    hideProgressBar();
                    new Helper().showCustomToast(context, "Failed to save the report!");
                });
            }


        });
    }

    private void saveFile(ResponseBody response, File localFile) throws IOException {

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

        //update file to UI
        updateReportFile(localFile);

        context.runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            //show toast msg
            Toast.makeText(context, "Report Exported and Downloaded Successfully into your file storage!", Toast.LENGTH_LONG).show();
            //new Helper().showCustomToast(context, "Report Exported and Downloaded Successfully into your file storage!");

            //share
            //shareDataForSocial(localFile);
        });

    }

    private void updateReportFile(File reportFile)
    {
        //set file name
        mTv_docName.setText(reportFile!=null ? getFileName_from_filePath(reportFile.getAbsolutePath()) : "");
        //set doc size
        mTv_docSize.setText(reportFile!=null ? new Helper().getFileSizeKiloBytes(reportFile) + " | "+ getTodaysFormattedDateTime() : "0 KB | " + getTodaysFormattedDateTime());
        //visible main layout
        ll_viewReportMain.setVisibility(View.VISIBLE);

        //click event
        mBtn_shareReport.setOnClickListener(v -> {
            //share
            shareDataForSocial(reportFile);
            //openFolder();
        });

        //click event
        ll_viewReport.setOnClickListener(v -> {
            //share
            openFileIntent(reportFile);
            //openFolder();
        });
    }

    public void openFolder(){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() +  File.separator + "/LeadManagement/Reports/" + File.separator );  //+ File.separator
        //intent.setDataAndType(uri, "text/csv");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        startActivity(Intent.createChooser(intent, "Open folder"));


        //File dirFile = new File(Environment.getExternalStorageDirectory() + File.separator +  "/LeadManagement/Reports/");
    }


    //Share Data
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
        builder_accept.setContentView(R.layout.layout_share_options_popup_new);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


        MaterialButton mBtn_share_with_whats_app,mBtn_share_with_mail, mBtn_share_with_more;
        //ll_share_with_whats_app= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option1);
        TextView textView_popup_title= builder_accept.findViewById(R.id.textView_layout_select_popup_title);
        mBtn_share_with_whats_app= builder_accept.findViewById(R.id.mBtn_shareBrochure_waShare);
        //ll_share_with_mail= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option2);
        mBtn_share_with_mail= builder_accept.findViewById(R.id.mBtn_shareBrochure_mailShare);
        LinearLayoutCompat ll_share_with_fb= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option4);
        //ll_share_with_more= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option3);
        mBtn_share_with_more= builder_accept.findViewById(R.id.mBtn_shareBrochure_moreShare);
        textView_popup_title.setText(getString(R.string.share_report_using));

        //share on WhatsApp
        Objects.requireNonNull(mBtn_share_with_whats_app).setOnClickListener(view -> {
            shareOnWhatsApp(dirFile);
            builder_accept.dismiss();
        });

        //share on mail
        Objects.requireNonNull(mBtn_share_with_mail).setOnClickListener(view -> {
            shareOnEmail(dirFile);
            builder_accept.dismiss();
        });

        //share on FB
        Objects.requireNonNull(ll_share_with_fb).setOnClickListener(view -> {
            //shareOnFB(myModel);
            dirFileUpload = dirFile;
            builder_accept.dismiss();

            //Gmail_signIn_button.performClick();
            if (mGoogleSignInClient!=null) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        //share on More
        Objects.requireNonNull(mBtn_share_with_more).setOnClickListener(view -> {
            doNormalShare(dirFile);
            builder_accept.dismiss();
        });

        builder_accept.show();
    }


    //Share on Whats App
    private void shareOnWhatsApp(File dirFile) {

        //final String extra_text = "Lead Summary Report from Lead Management App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;
        try {

            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setPackage(context.getResources().getString(R.string.pkg_whatsapp));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dirFile));
                // share.putExtra(Intent.EXTRA_TEXT, extra_text);
                //share.setType("image/*");
            }
            else {
                // Uri photoUri = FileProvider.getUriForFile(AddNewVisitor.this, AddNewVisitor.this.getPackageName() + ".provider", imageFile);
                Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", dirFile);
                //share.setDataAndType(fileUri, "application/pdf");
                //share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM,fileUri);
                //share.putExtra(Intent.EXTRA_TEXT, extra_text);
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
            context.startActivity(Intent.createChooser(share, "Share Using"));

        }catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }
    }


    //Share on Email App
    private void shareOnEmail(File dirFile) {

        //final String extra_text = "Lead Summary Report from Lead Management App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;

        try{
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setPackage(context.getResources().getString(R.string.pkg_gmail));
            share.putExtra(Intent.EXTRA_EMAIL, "add your recipients");
            share.putExtra(Intent.EXTRA_SUBJECT,"Lead Summary Report from Lead Management App");
            share.putExtra(Intent.EXTRA_TEXT," your text here...");
            share.putExtra(Intent.EXTRA_CC,"");
            share.putExtra(Intent.EXTRA_BCC, "");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dirFile));
                // share.putExtra(Intent.EXTRA_TEXT, extra_text);
                //share.setType("image/*");
            }
            else {
                // Uri photoUri = FileProvider.getUriForFile(AddNewVisitor.this, AddNewVisitor.this.getPackageName() + ".provider", imageFile);
                Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", dirFile);
                //share.setDataAndType(fileUri, "application/pdf");
                //share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM,fileUri);
                //share.putExtra(Intent.EXTRA_TEXT, extra_text);
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");

            context.startActivity(Intent.createChooser(share, "Send Email"));
        }
        catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            Toast.makeText(context, "Gmail App not installed!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }
    }

    private void doNormalShare(File dirFile)
    {

        try {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dirFile));
                // share.putExtra(Intent.EXTRA_TEXT, extra_text);
                //share.setType("image/*");
            }
            else {
                // Uri photoUri = FileProvider.getUriForFile(AddNewVisitor.this, AddNewVisitor.this.getPackageName() + ".provider", imageFile);
                Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", dirFile);
                //share.setDataAndType(fileUri, "application/pdf");
                //share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM,fileUri);
                //share.putExtra(Intent.EXTRA_TEXT, extra_text);
                share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
            context.startActivity(Intent.createChooser(share, "Share Using"));

        }catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            Toast.makeText(context, "Apps not installed!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error occurred!", Toast.LENGTH_SHORT).show();
        }

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
        }
        else
        {

//            Uri fileUri = FileProvider.getUriForFile(AllCustomerDetailActivity.this, "com.crm.crm.fileprovider", localFile);
            //target.setDataAndType(fileUri, "application/pdf");
//TODO
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(fileUri, type);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try
        {
//            hideProgressBar();
            //pb_donationList.setVisibility(View.GONE);
            context.startActivity(intent);

        }catch(ActivityNotFoundException e)
        {
//            hideProgressBar();
            e.printStackTrace();
            //pb_donationList.setVisibility(View.GONE);
            new Helper().showCustomToast(context, "No Application available to view this file!");
        }

    }


    private void initialiseGMailLogin()
    {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try
        {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Log.e(TAG, "GMail Login resp =" + Objects.requireNonNull(account).toString());
            String socialID =   account.getId();
            Log.e(TAG, "handleSignInResult: gm_id "+socialID );
            JSONObject object = new JSONObject();

            try {
                object.put("id", account.getId());
                object.put("name", account.getDisplayName());
                object.put("email", account.getEmail());
                object.put("photoPath", account.getPhotoUrl());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }


            if (isNetworkAvailable(context)) {
                //socialType = 2;
                //call_checkSocialId(socialID, false, object, 2);
                call_uploadReportToDrive(dirFileUpload);
            }
            else NetworkError(context);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
            Log.e(TAG, "handleSignInResult: " + e.getStatusMessage());
            showErrorLog("SignIn Failed. Please try again with Registering Now!");
            //updateUI(null);
        }
    }


    private void call_uploadReportToDrive(File dirFile)
    {
        //File profile_path_part = new File(BookingApplicationUrl);
        RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), dirFile);
        MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("fileMedia", dirFile.getName(), uploadFile);
        RequestBody api_tokenPart = RequestBody.create(MediaType.parse("text/plain"), api_token);
        RequestBody uploadType_part = RequestBody.create(MediaType.parse("text/plain"), "resumable");
        RequestBody upload_id_part = RequestBody.create(MediaType.parse("text/plain"), "xa298sd_sdlkj2");

        ApiClient client = ApiClient.getInstance();
        client.getApiService().uploadFileToDrive(fileUpload,api_tokenPart,uploadType_part, upload_id_part).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        //int isSuccess = 0;
                        //if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        new Helper().showCustomToast(context, "Upload Success!");

                        //if (isSuccess==1) {
                            //show success popup
                         //   MarkAsBookSuccess();
                       // }
                       // else showErrorLog("Error occurred during book unit! Please try again!");
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





    private void checkButtonEnabled()
    {
            //project id and dates are null
        if (sendFromDate ==null && sendToDate == null) setButtonDisabledView();

            //check if only from date selected
        else if (sendFromDate !=null && sendToDate ==  null) setButtonDisabledView();

        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }

    private void setButtonEnabledView()
    {
        // All validations are checked
        // enable btn for submit lead
        mBtn_exportReport.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_exportReport.setTextColor(getResources().getColor(R.color.main_white));
    }

    private void setButtonDisabledView()
    {
        // All validations are not checked
        // disable btn for submit team lead
        mBtn_exportReport.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_exportReport.setTextColor(getResources().getColor(R.color.main_white));
    }



    //ShowErrorLog
    private void showErrorLog(final String message)
    {
        if (context!=null)
        {
            runOnUiThread(() -> {
                //hide pb
                hideProgressBar();

                onErrorSnack(context,message);

                //hide view report view
                ll_viewReportMain.setVisibility(View.GONE);
            });
        }
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


    @SuppressLint("SetTextI18n")
    private void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}