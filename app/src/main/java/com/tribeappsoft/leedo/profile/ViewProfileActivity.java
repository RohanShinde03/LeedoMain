package com.tribeappsoft.leedo.profile;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.TouchImageView;
import com.tribeappsoft.leedo.util.crop_image.CropImage;
import com.tribeappsoft.leedo.util.crop_image.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
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
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.isValidContextForGlide;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class ViewProfileActivity extends AppCompatActivity {

    @BindView(R.id.iv_ViewProfileActivity_edtProf_userImg) CircleImageView iv_edtProf_userImg;
    @BindView(R.id.iv_ViewProfileActivity_edtProf_changeImage) AppCompatImageView iv_edtProf_changeImage;
    @BindView(R.id.iv_edit_profile) AppCompatImageView iv_edit_profile;
    @BindView(R.id.tv_ViewProfileActivity_User_name) AppCompatTextView tv_User_name;
    @BindView(R.id.tv_ViewProfileActivity_User_position) AppCompatTextView tv_User_position;
    @BindView(R.id.tv_ViewProfileActivity_User_mobile) AppCompatTextView tv_User_mobile;
    @BindView(R.id.tv_ViewProfileActivity_User_email) AppCompatTextView tv_User_email;
    @BindView(R.id.pb_ViewProfileActivity) ProgressBar pb_ViewProfileActivity;
    @BindView(R.id.container_profile) FrameLayout container_profile;

    @BindView(R.id.cl_viewProfile) CoordinatorLayout cl_viewProfile;
    @BindView(R.id.ll_viewProfile_expandedImage) LinearLayoutCompat ll_expandedImage;
    @BindView(R.id.iv_expanded_image) TouchImageView expanded_image;

    private Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public String api_token = "",firstName="",lastName="",mobile="",email="",profile_path =null;
    public int user_id = 0,provider_id=0;
    private String TAG ="ViewProfileActivity";
    private static final int Permission_CODE_STORAGE = 321;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator currentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int shortAnimationDuration;
    private boolean notifyProfile=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        context= ViewProfileActivity.this;


        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable( getResources().getColor(R.color.main_white)));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //set status bar color
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_white));

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");
        user_id = sharedPreferences.getInt("user_id", 0);
        provider_id = sharedPreferences.getInt("provider_id", 0);

        if(getIntent()!=null)
        {
            notifyProfile=getIntent().getBooleanExtra("notifyProfile",false);
        }

        setLocalProfile();

        //Get Profile Data
        /*if (isNetworkAvailable(context)) call_getProfile();
        else setLocalProfile();*/

        //Update Profile
        iv_edtProf_changeImage.setOnClickListener(view -> {
            uploadProfile();
        });

        //Edit Profile
        iv_edit_profile.setOnClickListener(view -> startActivity(new Intent(context,EditProfileActivity.class)));

        iv_edtProf_userImg.setOnClickListener(view -> {

            if (profile_path!=null && !profile_path.trim().isEmpty()) zoomImageFromThumb(iv_edtProf_userImg, R.drawable.ic_profile_default_image);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        setLocalProfile();
    }

    private void call_getProfile()
    {

        ApiClient client = ApiClient.getInstance();
        client.getApiService().getUserProfileInfo(api_token, user_id).enqueue(new Callback<JsonObject>()
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
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject())
                                {
                                    JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                    setJson(jsonObject);
                                }
                            }

                        }
                        else setLocalProfile();
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

    private void setJson(JsonObject jsonObject)
    {

        if (context!=null)
        {
            context.runOnUiThread(() -> {

                if (jsonObject.has("first_name")) firstName = !jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString() : sharedPreferences.getString("firstName", "");
                if (jsonObject.has("last_name")) lastName = !jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString() : sharedPreferences.getString("lastName", "");
                if (jsonObject.has("mobile")) mobile = !jsonObject.get("mobile").isJsonNull() ? jsonObject.get("mobile").getAsString() : sharedPreferences.getString("mobile", "");
                if (jsonObject.has("email")) email = !jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : sharedPreferences.getString("email", "");
                if (jsonObject.has("profile_path")) profile_path = !jsonObject.get("profile_path").isJsonNull() ? jsonObject.get("profile_path").getAsString() : sharedPreferences.getString("profile_path", "");


                tv_User_name.setText(String.format("%s %s", firstName, lastName));
                tv_User_mobile.setText(mobile);
                tv_User_email.setText(email);

                if(provider_id == 1)
                {
                    tv_User_position.setText("Sales Engineer");
                }


                //call method to set image
                pb_ViewProfileActivity.setVisibility(View.VISIBLE);
                setProfilePic();

            });
        }
    }

    private void setLocalProfile()
    {
        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();

            if (sharedPreferences.getString("first_name", null) != null) firstName = sharedPreferences.getString("first_name", "");
            if (sharedPreferences.getString("last_name", null) != null) lastName = sharedPreferences.getString("last_name", "");
            if (sharedPreferences.getString("email", null) != null) email = sharedPreferences.getString("email", "");
            if (sharedPreferences.getString("mobile_number", null) != null) mobile = sharedPreferences.getString("mobile_number", "");
            if (sharedPreferences.getString("profile_photo", null) != null) profile_path = sharedPreferences.getString("profile_photo", "");
            if (sharedPreferences.getInt("provider_id", 0) != 0) provider_id = sharedPreferences.getInt("provider_id", 0);

            Log.e(TAG, "setLocalProfile: mobile_number"+mobile);
            Log.e(TAG, "setLocalProfile: first_name"+firstName);
            Log.e(TAG, "setLocalProfile: last_name"+lastName);
            Log.e(TAG, "setLocalProfile: email"+email);
            Log.e(TAG, "setLocalProfile: profile_path"+profile_path);

            tv_User_name.setText(String.format("%s %s", firstName, lastName));
            tv_User_mobile.setText(mobile);
            tv_User_email.setText(email);
            tv_User_position.setText("Sales Engineer");

            //call method to set image
            pb_ViewProfileActivity.setVisibility(View.VISIBLE);
            setProfilePic();

        }
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
                        .apply(new RequestOptions().placeholder(R.drawable.default_user_icon))
                        .apply(new RequestOptions().error(R.drawable.default_user_icon))
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
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {

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
                            }
                            else showErrorLog("Failed to upload profile picture!");
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


    private void showErrorLog(final String message)
    {
        if(context!=null)
        {
            runOnUiThread(() ->{

                pb_ViewProfileActivity.setVisibility(View.GONE);
                onErrorSnack(context,message);
            });
        }
    }

    private void zoomImageFromThumb(final View thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        //final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        //expanded_image.setImageResource(imageResId);

        Glide.with(context)
                .load(profile_path)
                .thumbnail(0.5f)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .apply(new RequestOptions().fitCenter())
                .apply(new RequestOptions().placeholder( R.drawable.ic_user ))
                .apply(new RequestOptions().error( R.drawable.ic_user))
                .priority(Priority.HIGH)
                .into(expanded_image);



        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container_profile).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expanded_image.setVisibility(View.VISIBLE);
        //container_profile.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        ll_expandedImage.setVisibility(View.VISIBLE);
        //iv_expandedImage.setBackgroundColor(getResources().getColor(R.color.main_black));
        //container_updateProfile.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        cl_viewProfile.setVisibility(View.GONE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expanded_image.setPivotX(0f);
        expanded_image.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expanded_image, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expanded_image, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expanded_image, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expanded_image,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expanded_image.setOnClickListener(view -> {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                    .ofFloat(expanded_image, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expanded_image,
                                    View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expanded_image,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expanded_image,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(shortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    expanded_image.setVisibility(View.GONE);
                    ll_expandedImage.setVisibility(View.GONE);
                    //container_updateProfile.setBackgroundColor(Color.TRANSPARENT);
                    cl_viewProfile.setVisibility(View.VISIBLE);
                    //container_profile.setBackgroundColor(Color.TRANSPARENT);
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    expanded_image.setVisibility(View.GONE);
                    ll_expandedImage.setVisibility(View.GONE);
                    //container_updateProfile.setBackgroundColor(Color.TRANSPARENT);
                    cl_viewProfile.setVisibility(View.VISIBLE);
                    //container_profile.setBackgroundColor(Color.TRANSPARENT);
                    currentAnimator = null;
                }
            });
            set1.start();
            currentAnimator = set1;
        });
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
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onBackPressed() {

        if (expanded_image.getVisibility() == View.VISIBLE)
        {
            iv_edtProf_userImg.setAlpha(1f);
            expanded_image.setVisibility(View.GONE);
            ll_expandedImage.setVisibility(View.GONE);
            //container_updateProfile.setBackgroundColor(Color.TRANSPARENT);
            cl_viewProfile.setVisibility(View.VISIBLE);
        }
       else if(notifyProfile) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }
}
