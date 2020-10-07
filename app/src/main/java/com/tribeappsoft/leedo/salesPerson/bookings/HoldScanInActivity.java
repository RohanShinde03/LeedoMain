package com.tribeappsoft.leedo.salesPerson.bookings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.tribeappsoft.leedo.BuildConfig;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.zxingScanner.ZXingScannerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;


public class HoldScanInActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{

    @BindView(R.id.tv_holdScanIn_eventTitle) AppCompatTextView tv_eventTitle;
    @BindView(R.id.tv_holdScanIn_selectedFlat) AppCompatTextView tv_selectedFlat;
    @BindView(R.id.tv_holdScanIn_scanBody) AppCompatTextView tv_scanBody;
    @BindView(R.id.scannerView_holdFlatScanIn) ZXingScannerView mScannerView;
    @BindView(R.id.iv_holdFlatScanIn_flashOn) AppCompatImageView iv_flashOn;
    @BindView(R.id.iv_holdFlatScanIn_camera) AppCompatImageView iv_camera;
    @BindView(R.id.iv_holdFlatScanIn_gallery) AppCompatImageView iv_gallery;
    @BindView(R.id.ll_holdScanIn_scannerOptions) LinearLayoutCompat ll_scannerOptions;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;


    private AppCompatActivity context;
    private boolean mFlash, isCameraOrScan = false;
    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int CAMERA_REQUEST = 101;
    private static final int Permission_CODE_Gallery= 567;
    private static final int SELECT_PHOTO = 100;

    private static final String TAG = "HoldScanInActivity";
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    //for easy manipulation of the result
    private String scanResult ="",api_token ="", event_title ="", unit_name ="",
            token_no ="", lead_uid ="", unit_category ="",project_name ="", country_code="91", mobile_number ="",
            email ="", full_name ="";
    private int event_id =0, user_id =0, unit_id =0, token_id = 0, lead_id = 0;
    //private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_scan_in);
        overridePendingTransition( R.anim.trans_slide_up, R.anim.no_change );
        context = HoldScanInActivity.this;
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.hold_flat_scan_in));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //hide pb
        hideProgressBar();

        if (getIntent()!=null)
        {
            event_id = getIntent().getIntExtra("event_id", 0);
            event_title = getIntent().getStringExtra("event_title");
            unit_id = getIntent().getIntExtra("unit_id", 0);
            unit_name = getIntent().getStringExtra("unit_name");
        }

        //set event Title
        tv_eventTitle.setText(event_title!=null ? event_title : "Booking Event" );
        tv_selectedFlat.setText(unit_name!=null ? unit_name : "Unit");


        //For Camera
        if (savedInstanceState != null)
        {
            if (imageUri != null)
            {
                imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
                tv_scanBody.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
            }
        }

        //initialise detector
        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            tv_scanBody.setText(getString(R.string.detector_initialisation_failed));
            return;
        }


        //check for camera permission granted for Scanner View
        askPermissionForCamera();


        //Flash Button
        iv_flashOn.setOnClickListener(view -> {

            if (mFlash) {
                //flash is on

                //set flash off
                mScannerView.setFlash(false);
                mFlash = false;
                iv_flashOn.setImageResource(R.drawable.ic_flash_on_black_24dp);
            } else {
                //flash is off

                //set flash on
                mScannerView.setFlash(true);
                mFlash = true;
                iv_flashOn.setImageResource(R.drawable.ic_flash_off_black_24dp);
            }

        });


        //For Camera Open
        iv_camera.setOnClickListener(view -> {

            //open camera for capture image and show scan result of captured one
            isCameraOrScan = true;
            askPermissionForCamera();

            /*ActivityCompat.requestPermissions(HoldScanInActivity.this, new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);*/
        });

        //For Gallery Open
        iv_gallery.setOnClickListener(view -> {

            //launch gallery via intent
            askPermissionForGallery();
        });



    }

    //Initial Views

   /*private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG,"Permission not available requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG,"Permission has already granted");
        }
    }*/



    /*On Resume Method*/
    @Override
    public void onResume() {
        super.onResume();

        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setAutoFocus(true);

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


    void askPermissionForCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkCameraPermission() && checkWriteStoragePermission()) checkIsCameraOrScanner();
            else requestPermission_for_Camera();
        }
        else checkIsCameraOrScanner();
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

    void checkIsCameraOrScanner()
    {
        //method check whether permission requested for camera capture or scanner view
        if (isCameraOrScan) takeBarcodePicture();
        else launchMediaScanIntent();
    }


    private void requestPermission_for_Camera()
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //permission granted already -- directly open the camera
            checkIsCameraOrScanner();

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
                }, REQUEST_CAMERA_PERMISSION);

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



    void OpenGallery()
    {
        //launch intent to
        Intent photoPic = new Intent(Intent.ACTION_PICK);
        photoPic.setType("image/*");
        startActivityForResult(photoPic, SELECT_PHOTO);

    }

    //Barcode Picture
    private void takeBarcodePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
        imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }


    //LaunchMedia
    private void launchMediaScanIntent() {
        //visible
        ll_scannerOptions.setVisibility(View.VISIBLE);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }


    /*Permission Request*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                    checkIsCameraOrScanner();
                } else {
                    new Helper().showCustomToast(context, getString(R.string.camera_permission_rationale));
                }
                break;

            case Permission_CODE_Gallery:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                    //open gallery once permission is granted
                    OpenGallery();
                } else {
                    new Helper().showCustomToast(context, getString(R.string.gallery_permission_rationale));
                }

                break;
        }

    }



    //Activity Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //For Camera Open Result
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            //launchMediaScanIntent();
            //set def false once camera is opened
            isCameraOrScan = false;

            try
            {

                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null)
                {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for (int index = 0; index < barcodes.size(); index++)
                    {
                        Barcode code = barcodes.valueAt(index);

                        tv_scanBody.setText(String.format("%s\n%s\n", tv_scanBody.getText(), code.displayValue));

                        int type = barcodes.valueAt(index).valueFormat;
                        switch (type) {
                            case Barcode.CONTACT_INFO:
                                Log.i(TAG, code.contactInfo.title);
                                break;
                            case Barcode.EMAIL:
                                Log.i(TAG, code.displayValue);
                                break;
                            case Barcode.ISBN:
                                Log.i(TAG, code.rawValue);
                                break;
                            case Barcode.PHONE:
                                Log.i(TAG, code.phone.number);
                                break;
                            case Barcode.PRODUCT:
                                Log.i(TAG, code.rawValue);
                                break;
                            case Barcode.SMS:
                                Log.i(TAG, code.sms.message);
                                break;
                            case Barcode.TEXT:
                                Log.i(TAG, code.displayValue);
                                break;
                            case Barcode.URL:
                                Log.i(TAG, "url: " + code.displayValue);
                                break;
                            case Barcode.WIFI:
                                Log.i(TAG, code.wifi.ssid);
                                break;
                            case Barcode.GEO:
                                Log.i(TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                                break;
                            case Barcode.CALENDAR_EVENT:
                                Log.i(TAG, code.calendarEvent.description);
                                break;
                            case Barcode.QR_CODE:
                                Log.i(TAG, code.rawValue);
                                break;
                            case Barcode.DRIVER_LICENSE:
                                Log.i(TAG, code.driverLicense.licenseNumber);
                                break;
                            default:
                                Log.i(TAG, code.rawValue);
                                break;
                        }

                        scanResult = code.rawValue;
                        Log.e(TAG, "onActivityResult: Result Camera "+ code.rawValue );

                        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
                        {
                            showProgressBar();
                            new Thread(this::call_getTokenInfo).start();
                        }
                        else
                        {
                            Helper.NetworkError(context);
                            mScannerView.resumeCameraPreview(this);
                        }

                        //startActivity(new Intent(this, HoldFlatDetailsActivity.class));
                    }
                    if (barcodes.size() == 0) {
                        tv_scanBody.setText(getString(R.string.no_barcode_detected_try_again));
                        //new Helper().showCustomToast(this,"No scanResult could be detected. Please try again.");
                    }
                } else {
                    tv_scanBody.setText(getString(R.string.detector_initialisation_failed));
                    // new Helper().showCustomToast(this,"Detector initialisation failed");
                }
            } catch (Exception e) {

                new Helper().showCustomToast(context, "Failed to load Image");
                //new Helper().showCustomToast(this,"Failed to load Image");
                Log.e(TAG, e.toString());
            }
        }


        //For Gallery Open Result
        else if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK)
        {
            Uri selectedImage = data.getData();
            InputStream imageStream = null;
            try {
                //getting the image
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {

                new Helper().showCustomToast(context, "File not found");
                e.printStackTrace();
            }
            //decoding bitmap
            Bitmap bMap = BitmapFactory.decodeStream(imageStream);
            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
            // copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
                    bMap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
                    bMap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();// use this otherwise
            // ChecksumException
            try {
                Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<>();
                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                Result result = reader.decode(bitmap, decodeHints);
                //*I have created a global string variable by the name of scanResult to easily manipulate data across the application*//
                scanResult = result.getText();
                Log.e(TAG, "onActivityResult: Result Gallery "+ scanResult);

                if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
                {
                    showProgressBar();
                    new Thread(this::call_getTokenInfo).start();
                }
                else
                {
                    Helper.NetworkError(context);
                    mScannerView.resumeCameraPreview(this);
                }


                //do something with the results for demo i created a popup dialog
                //new Helper().showCustomToast(this,"Result"+ scanResult);
                //the end of do something with the button statement.


            } catch (NotFoundException e) {
                new Helper().showCustomToast(context, "Nothing Found");
                e.printStackTrace();
            } catch (ChecksumException e) {
                new Helper().showCustomToast(context, "Something weird happen, i was probably tired to solve this issue");
                e.printStackTrace();
            } catch (FormatException e) {
                new Helper().showCustomToast(context, "Wrong Barcode/QR format");
                e.printStackTrace();
            } catch (NullPointerException e) {
                new Helper().showCustomToast(context, "Something weird happen, i was probably tired to solve this issue!");
                e.printStackTrace();
            }

        }

    }



    /*SaveInstance*/
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, tv_scanBody.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }




    /*Handle Result*/
    @Override
    public void handleResult(Result rawResult) {

        //vibrate when scan completed
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)); // New vibrate method for API Level 26 or higher
        } else {
            v.vibrate(500);  // Vibrate method for below API Level 26
        }

        //play sound when Scan Completed
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        // AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Do something with the result here
        // Prints scan results
        Log.e("result", rawResult.getText());
        // Prints the scan format (qrcode, pdf417 etc.)
        Log.e("result", rawResult.getBarcodeFormat().toString());
        scanResult = rawResult.getText();

        // Toast.makeText(this, "Result " + rawResult.getText(), Toast.LENGTH_SHORT).show();
        new Helper().showCustomToast(this,"Result"+" " +rawResult.getText());
        // mScannerView.stopCamera();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(context)))
        {
            showProgressBar();
            new Thread(this::call_getTokenInfo).start();
        }
        else
        {
            Helper.NetworkError(context);
            mScannerView.resumeCameraPreview(this);
        }
        //If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);

        //Intent intent = new Intent();
        //intent.putExtra(AppConstants.KEY_QR_CODE, rawResult.getText());
        //setResult(RESULT_OK, intent);
    }


    private void call_getTokenInfo()
    {

        ApiClient client = ApiClient.getInstance();
        client.getApiService().getTokenInfo(api_token, event_id, unit_id, user_id, scanResult).enqueue(new Callback<JsonObject>()
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
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setJson(data);
                                    //set
                                    new Handler().postDelayed(() -> onSuccessTokenScan(), 1000);

                                }
                            }
                        }
                        else if (isSuccess==2) showErrorLog("Time out! This flat is already booked by someone!");
                        else showErrorLog("Token is not valid!");
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

    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("token_id")) token_id = !jsonObject.get("token_id").isJsonNull() ? jsonObject.get("token_id").getAsInt() : 0 ;
        if (jsonObject.has("token_no")) token_no = !jsonObject.get("token_no").isJsonNull() ? jsonObject.get("token_no").getAsString() : "" ;
        if (jsonObject.has("lead_id")) lead_id = !jsonObject.get("lead_id").isJsonNull() ? jsonObject.get("lead_id").getAsInt() : 0 ;
        if (jsonObject.has("lead_uid")) lead_uid = !jsonObject.get("lead_uid").isJsonNull() ? jsonObject.get("lead_uid").getAsString() : "" ;
        if (jsonObject.has("unit_category")) unit_category = !jsonObject.get("unit_category").isJsonNull() ? jsonObject.get("unit_category").getAsString() : "" ;
        if (jsonObject.has("project_name")) project_name = !jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" ;
        if (jsonObject.has("country_code")) country_code = !jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "91" ;
        if (jsonObject.has("mobile_number")) mobile_number = !jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "" ;
        if (jsonObject.has("email")) email = !jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "" ;
        if (jsonObject.has("full_name")) full_name = !jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "" ;

    }

    private void onSuccessTokenScan()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            goToHoldConfirmScreen();
        });
    }


    void goToHoldConfirmScreen()
    {

        startActivity(new Intent(context, HoldFlatDetailsActivity.class)
                .putExtra("scanResult", scanResult)
                .putExtra("unit_id", unit_id)
                .putExtra("unit_name", unit_name)
                .putExtra("event_id", event_id)
                .putExtra("token_id", token_id)
                .putExtra("token_no", token_no)
                .putExtra("lead_id", lead_id)
                .putExtra("lead_uid", lead_uid)
                .putExtra("unit_category", unit_category)
                .putExtra("project_name", project_name)
                .putExtra("country_code", country_code)
                .putExtra("mobile_number", mobile_number)
                .putExtra("email", email)
                .putExtra("full_name", full_name)
                .addFlags(FLAG_ACTIVITY_CLEAR_TOP |  FLAG_ACTIVITY_SINGLE_TOP)
        );
        finish();
    }


    /*On Pause Method*/
    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }



    /*Decode Bitmap  Image*/
    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }



    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            mScannerView.resumeCameraPreview(this);

            Helper.onErrorSnack(context, message);
        });

    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar() {
        Helper.hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(getString(R.string.getting_ghp_details));
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    /*Overflow Menu*/
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
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }

}




