package com.tribeappsoft.leedo.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.UserModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;
import com.tribeappsoft.leedo.util.crop_image.CropImage;
import com.tribeappsoft.leedo.util.crop_image.CropImageView;
import com.tribeappsoft.leedo.util.customDatePicker.MyLocalDatePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.isValidContextForGlide;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class EditProfileActivity extends AppCompatActivity {

    @BindView(R.id.iv_EditProfileActivity_close) AppCompatImageView iv_close;
    @BindView(R.id.iv_EditProfileActivity_edtProf_userImg) CircleImageView iv_edtProf_userImg;
    @BindView(R.id.iv_EditProfileActivity_edtProf_changeImage) AppCompatImageView iv_edtProf_changeImage;

    @BindView(R.id.til_EditProfileActivity_user_firstName) TextInputLayout til_EditProfileActivity_user_firstName ;
    @BindView(R.id.edt_EditProfileActivity_user_firstName) TextInputEditText edt_EditProfileActivity_user_firstName;

    @BindView(R.id.til_EditProfileActivity_user_middleName) TextInputLayout til_EditProfileActivity_user_middleName ;
    @BindView(R.id.edt_EditProfileActivity_user_middleName) TextInputEditText edt_EditProfileActivity_user_middleName;

    @BindView(R.id.til_EditProfileActivity_user_lastName) TextInputLayout til_EditProfileActivity_user_lastName ;
    @BindView(R.id.edt_EditProfileActivity_user_lastName) TextInputEditText edt_EditProfileActivity_user_lastName;


    @BindView(R.id.mdp_profileUpdate_leadBirthDate)
    MyLocalDatePicker mdp_leadBirthDate;

    @BindView(R.id.til_EditProfileActivity_user_mobile) TextInputLayout til_EditProfileActivity_user_mobile ;
    @BindView(R.id.edt_EditProfileActivity_user_mobile) TextInputEditText edt_EditProfileActivity_user_mobile;

    @BindView(R.id.til_EditProfileActivity_user_email) TextInputLayout til_EditProfileActivity_user_email ;
    @BindView(R.id.edt_EditProfileActivity_user_email) TextInputEditText edt_EditProfileActivity_user_email;

    @BindView(R.id.mBtn_EditProfile_submit) MaterialButton btn_EditProfile_submit;
    @BindView(R.id.pb_editProfileActivity)ProgressBar pb_ViewProfileActivity;

    @BindView(R.id.ll_pbLayout)LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    // Gender select
    @BindView(R.id.rb_editProf_male) MaterialRadioButton rb_male;
    @BindView(R.id.rb_editProf_female) MaterialRadioButton rb_female;
    @BindView(R.id.rg_editProf_radioGroup) RadioGroup rg_radioGroup;


    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_SEND_Date = "yyyy-MM-dd";

    private Activity context;
    private String sendBDate=null, api_token ="", countryPhoneCode = "91",genderId="",prefix="",
            profile_path,firstName="",middle_name="",lastName="",mobile="",email="", dob =null;
    private String TAG="EditProfileActivity";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int user_id=0;
    private static final int Permission_CODE_STORAGE = 321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        context= EditProfileActivity.this;

        //set status bar color
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_white));

        iv_close.setOnClickListener(view -> onBackPressed());

        //hidden pb
        hideProgressBar();
        init();

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        prefix = sharedPreferences.getString("prefix", "");
        user_id = sharedPreferences.getInt("user_id", 0);

        //set local profile data
        setLocalProfile();

        rg_radioGroup.setOnCheckedChangeListener((group, checkedId) ->
        {
            //int id = group.getCheckedRadioButtonId();

            RadioButton rb = findViewById(checkedId);

            String radioText=rb.getText().toString();

            if(radioText.equals("Male")) genderId="Male";
            else genderId="Female";
        });


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
         //  checkButtonEnabled();
            //Toast.makeText(MainLocalDateActivity.this, "Selected date: " + MyLocalDatePicker.dateToString(dateSelected, DATE_FORMAT), Toast.LENGTH_SHORT).show();
        });

        mdp_leadBirthDate.setOnLocalDateSelectedListener(dateSelected -> Log.e("",  "onLocalDateSelected: " + dateSelected));


        /*Update Profile*/
        iv_edtProf_changeImage.setOnClickListener(view -> uploadProfile());


        btn_EditProfile_submit.setOnClickListener(view -> {

            if (Objects.requireNonNull(edt_EditProfileActivity_user_firstName.getText()).toString().trim().isEmpty())  new Helper().showCustomToast(this, "Please enter First Name!");
            else if (Objects.requireNonNull(edt_EditProfileActivity_user_lastName.getText()).toString().trim().isEmpty())   new Helper().showCustomToast(this, "Please enter Last Name!");
            else if (Objects.requireNonNull(edt_EditProfileActivity_user_email.getText()).toString().trim().isEmpty())   new Helper().showCustomToast(this, "Please enter Email Address!");
            else checkValidations();
        });
    }


    private void checkValidations()
    {
        //check validation
        // String mobile  = "";
        if (!Objects.requireNonNull(edt_EditProfileActivity_user_mobile.getText()).toString().trim().isEmpty())
        {
            if (isValidPhone(edt_EditProfileActivity_user_mobile))
            {

                if (isValidEmail(edt_EditProfileActivity_user_email))
                {
                    showEditProfileConfirmationAlert();
                }
                else
                {
                    til_EditProfileActivity_user_email.setErrorEnabled(true);
                    til_EditProfileActivity_user_email.setError("Please enter a valid email!");
                    //new Helper().showCustomToast(this, "Please enter a valid email address");
                }
            }
            else
            {
                til_EditProfileActivity_user_mobile.setErrorEnabled(true);
                til_EditProfileActivity_user_mobile.setError("Please enter valid mobile number");
                //new Helper().showCustomToast(this, "Please enter a valid email address");
            }

        } else new Helper().showCustomToast(this, "Please enter your mobile number!");
    }

    private boolean isValidPhone(EditText phone)
    {
        boolean ret = true;
        if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }


    private boolean isValidEmail(EditText email)
    {
        boolean ret = true;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        return ret;
    }

    private void init() {

        edt_EditProfileActivity_user_mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    /*Upload Profile Photo*/
    void uploadProfile()
    {
        //anim.clickEffect(user_profile_upload_image_btn);
        //selectImage();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkPermission()) CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(context);
            else requestPermissionWriteStorage();
        }
        else CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissionWriteStorage()
    {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
            return;
        }
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)))
        {
            //new Helper().showCustomToast(this, getString(R.string.file_permissionRationale));
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, Permission_CODE_STORAGE);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request
        if (requestCode == Permission_CODE_STORAGE)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast
                new Helper().showCustomToast(context, getString(R.string.permission_grant_success));
                //open documents once permission is granted
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            else
            {
                //Displaying another toast if permission is not granted
                new Helper().showCustomToast(context, getString(R.string.file_permissionRationale));
            }
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

       /* if (requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            String filePath = getRealPathFromURIPath(data.getData(), this);
            uploadImage(filePath);

        }*/

        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                //((CircleImageView) findViewById(R.id.iv_studentProfile)).setImageURI(result.getUri());

                Log.e(TAG, "onActivityResult: " + result.getUri());
                Log.e(TAG, "path : " + result.getUri().getPath());
                Log.e(TAG, "onActivityResult: " + result.getSampleSize());


                //String filePath =result.getUri().getPath();
                //Log.e("URi", filePath);


                //Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());

                    Uri tempUri = getImageUri(getApplicationContext(), bitmap);

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    File finalFile = new File(getRealPathFromURI(tempUri));

                    Log.e(TAG, "path 5: " + finalFile.getPath());
                    //Log.e(TAG, "path 6: "+ finalFile.getAbsolutePath());
                    //Log.e(TAG, "path 7: "+ finalFile.toString());


                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int newWidth = 120;
                    float scaleWidth = ((float) newWidth) / width;
                    int newHeight = 120;
                    float scaleHeight = ((float) newHeight) / height;
                    // CREATE A MATRIX FOR THE MANIPULATION
                    Matrix matrix = new Matrix();

                    // RESIZE THE BIT MAP
                    matrix.postScale(scaleWidth, scaleHeight);
                    // RECREATE THE NEW BITMAP
                    Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

                    iv_edtProf_userImg.setImageBitmap(resized);


                    //call method to upload profile pic
                    callUploadImage(finalFile.getPath());

                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorLog("Error while setting an profile picture!");
                }


                //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                new Helper().showCustomToast(context, "Failed to set profile pic: " + result.getError());
            }
        }

        /*if (requestCode == REQUEST_CAMERA_CODE && resultCode == Activity.RESULT_OK)
        {
            String filePath = cameraFileUri.getPath();
            Log.e("URi", filePath);
            //callUploadImage(filePath);
        }*/
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private void callUploadImage(String filePath)
    {
        if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            pb_ViewProfileActivity.setVisibility(View.VISIBLE);

            String api_token = ""; int user_id =0;
            if (sharedPreferences!=null)
            {
                editor = sharedPreferences.edit();

                if (sharedPreferences.getString("api_token", null) != null) api_token = sharedPreferences.getString("api_token", "");
                if (sharedPreferences.getInt("user_id", 0) != 0) user_id = sharedPreferences.getInt("user_id", 0);

                editor.apply();
            }

            File profile_path_part = new File(filePath);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), profile_path_part);
            MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file_uri", profile_path_part.getName(), uploadFile);

            RequestBody api_token_id = RequestBody.create(MediaType.parse("text/plain"), api_token);
            RequestBody staff_id = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(user_id));

            ApiClient client = ApiClient.getInstance();
            client.getApiService().updateSalesPersonsProfileImage(fileUpload,api_token_id,staff_id).enqueue(new Callback<JsonObject>()
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
                                        //isLeadSubmitted = true;
                                        setJson(data);
                                        showSuccessAlertImage();
                                    }
                                    else showErrorLog("Server response is empty!");

                                }else showErrorLog("Invalid response from server!");

                            }else showErrorLog("Failed to upload profile picture!");

                            /*if (isSuccess==1)
                            {
                                if (response.body().has("profile_path")) if (!response.body().get("profile_path").isJsonNull()) profile_path =  response.body().get("profile_path").getAsString();

                                //save profile pic in sharedPref
                                if (sharedPreferences!=null)
                                {
                                    editor = sharedPreferences.edit();
                                    editor.putString("profile_path", profile_path);
                                    editor.apply();
                                    Log.e(TAG, "setProfilePic: saved  "+ profile_path );
                                    runOnUiThread(() -> new Helper().showCustomToast(context, "Profile picture updated!"));
                                }

                                //call method to set image
                                setProfilePic();
                            }*/

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
                    try {
                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                        else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                        else showErrorLog(e.toString());
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }

                }
            });



        }
        else NetworkError(context);
    }

    private void showSuccessAlertImage()
    {
        runOnUiThread(() -> {
            new Helper().showCustomToast(context, "Profile picture updated!");
            new Handler().postDelayed(this:: onBackPressed,1000);

        });

    }

    private void setProfilePic()
    {
        context.runOnUiThread(() -> {

            if (isValidContextForGlide(context))
            {
                Glide.with(context)
                        .load(profile_path)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .priority(Priority.HIGH)
                        .apply(new RequestOptions().centerCrop())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_profile_default_image))
                        .apply(new RequestOptions().error(R.drawable.ic_profile_default_image))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource)
                            {
                                pb_ViewProfileActivity.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                pb_ViewProfileActivity.setVisibility(View.GONE);
                                return false;
                            }
                        })

                        .into(iv_edtProf_userImg);
            }

            pb_ViewProfileActivity.setVisibility(View.GONE);

        });

    }

    private void setLocalProfile()
    {
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();

            if (sharedPreferences.getString("first_name", null) != null) firstName = sharedPreferences.getString("first_name", "");
            if (sharedPreferences.getString("middle_name", null) != null) middle_name = sharedPreferences.getString("middle_name", "");
            if (sharedPreferences.getString("last_name", null) != null) lastName = sharedPreferences.getString("last_name", "");
            if (sharedPreferences.getString("email", null) != null) email = sharedPreferences.getString("email", "");
            if (sharedPreferences.getString("mobile_number", null) != null) mobile = sharedPreferences.getString("mobile_number", "");
            if (sharedPreferences.getString("profile_photo", null) != null) profile_path = sharedPreferences.getString("profile_photo", "");
            if (sharedPreferences.getString("dob", null) != null) dob = sharedPreferences.getString("dob", null);

            Log.e(TAG, "setLocalProfile: mobile_number"+mobile);
            Log.e(TAG, "setLocalProfile: first_name"+firstName);
            Log.e(TAG, "setLocalProfile: last_name"+lastName);
            Log.e(TAG, "setLocalProfile: email"+email);
            Log.e(TAG, "setLocalProfile: profile_path"+profile_path);
            Log.e(TAG, "setLocalProfile: b'date "+dob);

            edt_EditProfileActivity_user_firstName.setText(String.format("%s", firstName));
            edt_EditProfileActivity_user_middleName.setText(String.format("%s", middle_name));
            edt_EditProfileActivity_user_lastName.setText(String.format("%s", lastName));
            edt_EditProfileActivity_user_mobile.setText(mobile);
            edt_EditProfileActivity_user_email.setText(email);


            // Define min & max date for sample
            //Date localDate = MyLocalDatePicker.stringToDate("01-01-1900", DATE_FORMAT);
            //b'date
            //mdp_leadBirthDate.setDate(localDate);


            /*mdp_leadBirthDate.setOnLocalDatePickListener(dateSelected -> {

            });*/

            //call method to set image
            pb_ViewProfileActivity.setVisibility(View.VISIBLE);
            setProfilePic();

        }
    }



    public void showEditProfileConfirmationAlert()
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

        tv_msg.setText(getString(R.string.change_profile));
        tv_desc.setText(getString(R.string.edit_profile_confirm_text));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {

            alertDialog.dismiss();
            if (isNetworkAvailable(context))
            {
                showProgressBar(getString(R.string.submitting_profile_details));
                call_profileUpdate();
            }else NetworkError(context);

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


    private void call_profileUpdate()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("user_id", user_id);
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("mobile_number", edt_EditProfileActivity_user_mobile.getText().toString());
        jsonObject.addProperty("email",  edt_EditProfileActivity_user_email.getText().toString());
        jsonObject.addProperty("prefix", prefix);
        jsonObject.addProperty("first_name", edt_EditProfileActivity_user_firstName.getText().toString());
        jsonObject.addProperty("middle_name", edt_EditProfileActivity_user_middleName.getText().toString());
        jsonObject.addProperty("last_name", edt_EditProfileActivity_user_lastName.getText().toString());
        jsonObject.addProperty("dob", sendBDate);
        jsonObject.addProperty("gender", genderId);


        ApiClient client = ApiClient.getInstance();
        client.getApiService().addUpdateUserDetails(jsonObject).enqueue(new Callback<JsonObject>()
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
                                    //isLeadSubmitted = true;
                                    setJson(data);
                                    showSuccessAlert();
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
                try {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                    else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                    else showErrorLog(e.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setJson(JsonObject jsonObject) {

        UserModel model = new UserModel();

        if (jsonObject.has("user_id")) model.setUser_id((!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0));
        if (jsonObject.has("username")) model.setUsername((!jsonObject.get("username").isJsonNull() ? jsonObject.get("username").getAsString().trim() : ""));
        if (jsonObject.has("api_token")) model.setApi_token((!jsonObject.get("api_token").isJsonNull() ? jsonObject.get("api_token").getAsString().trim() : ""));
        if (jsonObject.has("prefix")) model.setPrefix((!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString().trim() : ""));
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString().trim() : "");
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString().trim() : "");
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString().trim() : "");
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString().trim() : "");
        if (jsonObject.has("gender")) model.setGender(!jsonObject.get("gender").isJsonNull() ? jsonObject.get("gender").getAsString() : "");
        if (jsonObject.has("dob")) model.setDob(!jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setProfile_photo((!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : ""));
        model.setUser_type_id(1);   //TODO for sales person -->1
//call method to set image
        pb_ViewProfileActivity.setVisibility(View.VISIBLE);
        //setProfilePic();

        updateSharedPref(model);
    }

    private void updateSharedPref(UserModel model)
    {

        if (sharedPreferences != null)
        {
            editor = sharedPreferences.edit();
            editor.putInt("provider_id", 0);
            editor.putInt("user_id", model.getUser_id());
            editor.putInt("user_type_id", model.getUser_type_id());
            editor.putString("username", model.getUsername());
            editor.putString("api_token", model.getApi_token());
            editor.putString("prefix", model.getPrefix());
            editor.putString("first_name", model.getFirst_name());
            editor.putString("middle_name", model.getMiddle_name());
            editor.putString("last_name", model.getLast_name());
            editor.putString("full_name", model.getFull_name());
            editor.putString("gender", model.getGender());
            editor.putString("dob", model.getDob());
            editor.putString("email", model.getEmail());
            editor.putString("country_code", model.getCountry_code());
            editor.putString("mobile_number", model.getMobile_number());
            editor.putString("profile_photo", model.getProfile_photo());
            editor.apply();

            //call method to set image
            pb_ViewProfileActivity.setVisibility(View.VISIBLE);
            setProfilePic();

        }

    }


    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context, message);
        });

    }

    @SuppressLint("InflateParams")
    private void showSuccessAlert()
    {
        runOnUiThread(() -> {

           /* LayoutInflater factory = LayoutInflater.from(context);
            View view = factory.inflate(R.layout.popup_layout, null);
            AppCompatTextView tv_message =  view.findViewById(R.id.popup_toast);

            GifImageView gifImageView =  view.findViewById(R.id.gif);
            gifImageView.setGifImageResource(R.drawable.gif_success);
            tv_message.setText(getString(R.string.edit_profile_successfully));

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            final AlertDialog share_dialog;
            share_dialog = alertDialogBuilder.create();
            share_dialog.setView(view);
            share_dialog.show();*/

           new Helper().showSuccessCustomToast(context, "Profile Updated successfully!");

            //share_dialog.dismiss();
            new Handler().postDelayed(this::onBackPressed, 1000);

        });

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
    public boolean onCreateOptionsMenu(Menu menu)
    {

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
    }
}
