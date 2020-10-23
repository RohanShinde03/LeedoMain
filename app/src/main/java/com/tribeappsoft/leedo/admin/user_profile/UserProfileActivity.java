package com.tribeappsoft.leedo.admin.user_profile;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.util.FlowLayout;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.TouchImageView;
import com.tribeappsoft.leedo.util.crop_image.CropImage;
import com.tribeappsoft.leedo.util.crop_image.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.cl_main_layout) CoordinatorLayout clMainLayout;
    @BindView(R.id.ll_main_layout) LinearLayoutCompat llMainLayout;
    @BindView(R.id.cl_image_layout) CoordinatorLayout clImageLayout;
    @BindView(R.id.iv_UpdateProfileActivity_userImg) CircleImageView ivUserImage;
    @BindView(R.id.iv_UpdateProfileActivity_changeImage) AppCompatImageView ivChangeImageIcon;
    @BindView(R.id.pb_editProfileActivity) ProgressBar progressBar;
    @BindView(R.id.mtv_userProfileActivity_UserName) MaterialTextView mtvUserName;
    @BindView(R.id.ll_itemList_AssignedRoles) LinearLayoutCompat llAssignedRoles;
    @BindView(R.id.mtv_UserProfileActivity_phone) MaterialTextView mtvUserPhone;
    @BindView(R.id.mtv_UserProfileActivity_email) MaterialTextView mtvUserEmail;
    @BindView(R.id.ll_UpdateProfile_expandedImage) LinearLayoutCompat llExpandedImage;
    @BindView(R.id.iv_UpdateProfile_expandedImage) TouchImageView iv_expandedImage;
    @BindView(R.id.iv_close) AppCompatImageView iv_close;
    @BindView(R.id.fl_itemList_AssignedProjects) FlowLayout flAssignedProjects;
    @BindView(R.id.ll_assigned_project_layout) LinearLayoutCompat ll_assigned_project_layout;
    @BindView(R.id.ll_change_password_layout) LinearLayoutCompat ll_change_password_layout;

    private String TAG = "UserProfileActivity";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Animator currentAnimator;
    private int shortAnimationDuration;
    Activity context;
    String profile = null;
    private boolean notifyProfile=false;
    private static final int Permission_CODE_STORAGE = 321;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        context = UserProfileActivity.this;
        ButterKnife.bind(this);

        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();


        if(getIntent()!=null)
        {
            notifyProfile=getIntent().getBooleanExtra("notifyProfile",false);
        }

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        /*//click image to zoom
        ivUserImage.setOnClickListener(view -> {

            if (profile!=null) zoomImageFromThumb(ivUserImage);
        });

        ivChangeImageIcon.setOnClickListener(view -> selectUploadProfilePic());*/

        iv_close.setOnClickListener(v -> onBackPressed());

        ll_change_password_layout.setOnClickListener(v -> startActivity(new Intent(context, UserChangePassword_Activity.class)));
    }

    private View getAssignedRolesView(String roleName) {
        @SuppressLint("InflateParams")
        View rowView_sub = LayoutInflater.from(context).inflate(R.layout.layout_assign_roles, null);
        final AppCompatTextView tv_roleName = rowView_sub.findViewById(R.id.mTv_itemLayout_UserRole);
        tv_roleName.setText(roleName);
        return rowView_sub;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUser();
    }

    private void updateUser() {

        if (sharedPreferences != null) {
            mtvUserName.setText(sharedPreferences.getString("full_name", ""));
            mtvUserPhone.setText(sharedPreferences.getString("mobile_number", ""));
            mtvUserEmail.setText(sharedPreferences.getString("email",""));
            if(sharedPreferences.getInt("number_of_roles",0) > 0){
                llAssignedRoles.removeAllViews();
                int numberOfRoles = sharedPreferences.getInt("number_of_roles",0);
                for(int i=0 ;i < numberOfRoles ; i++){
                    Log.e(TAG,sharedPreferences.getString("role_"+i,""));
                    View rowView_sub = getAssignedRolesView(sharedPreferences.getString("role_"+i,""));
                    llAssignedRoles.addView(rowView_sub);
                }
            }
            if(sharedPreferences.getInt("number_of_projects",0) > 0){

                ll_assigned_project_layout.setVisibility(View.VISIBLE);
                flAssignedProjects.removeAllViews();
                int numberOfProjects = sharedPreferences.getInt("number_of_projects",0);
                for(int i=0 ;i < numberOfProjects ; i++){
                    Log.e(TAG,sharedPreferences.getString("project_"+i,""));
                    View rowView_sub = getAssignedRolesView(sharedPreferences.getString("project_"+i,""));
                    flAssignedProjects.addView(rowView_sub);
                }
            }
        }
        //call method to set image
        progressBar.setVisibility(View.VISIBLE);
        setProfilePic(profile);
    }

    void selectUploadProfilePic() {
        //anim.clickEffect(user_profile_upload_image_btn);
        //selectImage();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission())
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(context);
            else requestPermissionWriteStorage();
        } else CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionWriteStorage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
            return;
        }
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }//new Helper().showCustomToast(this, getString(R.string.file_permissionRationale));

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, Permission_CODE_STORAGE);
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

                Log.e(TAG, "onActivityResult: " + Objects.requireNonNull(result).getUri());
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
                    profile=finalFile.getPath();
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

                    ivUserImage.setImageBitmap(resized);


                    //call method to upload profile pic
                    callUploadImage(profile);

                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorLog("Error while setting an profile picture!");
                }


                //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                new Helper().showCustomToast(context, "Failed to set profile pic: " + Objects.requireNonNull(result).getError());
            }
        }

        /*if (requestCode == REQUEST_CAMERA_CODE && resultCode == Activity.RESULT_OK)
        {
            String filePath = cameraFileUri.getPath();
            Log.e("URi", filePath);
            //callUploadImage(filePath);
        }*/
    }

    public String getRealPathFromURI(Uri uri) {
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
        Date currentTime;
        currentTime = Calendar.getInstance().getTime();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title"+currentTime, null);
        return Uri.parse(path);
    }

    private void callUploadImage(String filePath) {

        Log.e(TAG,"PhotoPath : "+filePath);
       /* if (isNetworkAvailable(Objects.requireNonNull(context)))
        {
            progressBar.setVisibility(View.VISIBLE);
            File profile_path_part = new File(filePath);
            RequestBody uploadFile = RequestBody.create(MediaType.parse("image/*"), profile_path_part);
            MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("photoPath", profile_path_part.getName(), uploadFile);
            RequestBody memberIDPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(memberID));

            ApiClient client = ApiClient.getInstance();
            client.getApiService().uploadUserProfilePicture(client_base_url + WebServer.POST_userProfilePhotoUpload, fileUpload,memberIDPart).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    Log.e("response", "" + response.toString());
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (!response.body().isJsonNull() && response.body().isJsonObject()) {

                                int isSuccess = 0;
                                if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();
                                if(isSuccess==1) {

                                    if (response.body().has("data")) {

                                        if (!response.body().get("data").isJsonNull() &&  response.body().get("data").isJsonObject()) {
                                            JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                            if (jsonObject.has("photoPath")) profile = !jsonObject.get("photoPath").isJsonNull() ?  jsonObject.get("photoPath").getAsString() : sharedPreferences.getString("photoPath", "");

                                            // photoPath
                                            if (sharedPreferences != null) {
                                                editor = sharedPreferences.edit();
                                                editor.putString("photoPath", profile);
                                                editor.apply();
                                                //model.setProfile(profile);
                                            }
                                            setProfilePic(profile);
                                            showSuccessAlertImage();
                                        }
                                    } else new Helper().showCustomToast(context, "Invalid data received from server! Try Again.");
                                }
                                else showErrorLog("Failed to update profile picture! Try Again.");
                                //  profile = !response.body().get("profile").isJsonNull() ? response.body().get("profile").getAsString() : "";

                                //set Success image upload msg
                            }
                        }
                    } else {
                        // error case
                        switch (response.code()) {
                            case 404:
                                showErrorLog(context.getString(R.string.something_went_wrong_try_again));
                                break;
                            case 500:
                                showErrorLog(context.getString(R.string.server_error_msg));
                                break;
                            default:
                                showErrorLog(context.getString(R.string.unknown_error_try_again) + " " + response.code());
                                break;
                        }
                    }

                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                    Log.e(TAG, "onError: " + e.toString());
                    if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.weak_connection));
                    else if (e instanceof IOException) showErrorLog(getString(R.string.connection_time_out));
                    else showErrorLog(e.toString());
                }
            });

        } else {
            new Helper().showCustomToast(context, "Network not available! Please turn on your network.");
        }*/
    }


    private void setProfilePic(String photopath) {
        context.runOnUiThread(() -> {

            Log.e(TAG, "setProfilePic: "+photopath);
            if (Helper.isValidContextForGlide(context)) {
                Glide.with(context)
                        .load(photopath)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .priority(Priority.HIGH)
                        .apply(new RequestOptions().centerCrop())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_profile_default_user_icon))
                        .apply(new RequestOptions().error(R.drawable.ic_profile_default_user_icon))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })

                        .into(ivUserImage);
            }
            //pb_ViewProfileActivity.setVisibility(View.GONE);
        });
    }

    private void showSuccessAlertImage() {
        runOnUiThread(() -> {

            //call method that sets an image to imageView
            //  setProfilePic();
            //show success toast
            new Helper().showSuccessCustomToast(context, "Profile picture updated successfully!");

        });

    }

    private void showErrorLog(final String message)
    {
        runOnUiThread(() ->
        {
            //  hideProgressBar();
            Helper.onErrorSnack(context, message);
        });
    }

    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        //final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        //expanded_image.setImageResource(imageResId);

        if (Helper.isValidContextForGlide(context))
        {
            Glide.with(context)//getActivity().this
                    .load(profile)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .apply(new RequestOptions().fitCenter())
                    .apply(new RequestOptions().placeholder( R.drawable.ic_profile_default_user_icon ))
                    .apply(new RequestOptions().error( R.drawable.ic_profile_default_user_icon))
                    .into(iv_expandedImage);
        }


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
        findViewById(R.id.cl_main_layout).getGlobalVisibleRect(finalBounds, globalOffset);
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

        iv_expandedImage.setVisibility(View.VISIBLE);
        llExpandedImage.setVisibility(View.VISIBLE);
        //iv_expandedImage.setBackgroundColor(getResources().getColor(R.color.main_black));
        //container_updateProfile.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        llMainLayout.setVisibility(View.GONE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        iv_expandedImage.setPivotX(0f);
        llExpandedImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(iv_expandedImage, View.X,
                startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(iv_expandedImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(iv_expandedImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(iv_expandedImage,
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
        iv_expandedImage.setOnClickListener(view -> {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                    .ofFloat(iv_expandedImage, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(iv_expandedImage,
                                    View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(iv_expandedImage,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(iv_expandedImage,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(shortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    iv_expandedImage.setVisibility(View.GONE);
                    llExpandedImage.setVisibility(View.GONE);
                    //container_updateProfile.setBackgroundColor(Color.TRANSPARENT);
                    llMainLayout.setVisibility(View.VISIBLE);
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    iv_expandedImage.setVisibility(View.GONE);
                    llExpandedImage.setVisibility(View.GONE);
                    //container_updateProfile.setBackgroundColor(Color.TRANSPARENT);
                    llMainLayout.setVisibility(View.VISIBLE);
                    currentAnimator = null;
                }
            });
            set1.start();
            currentAnimator = set1;
        });
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
        if(notifyProfile) {
            startActivity(new Intent(context, SalesPersonHomeNavigationActivity.class));
            finish();
        }
        else {
            super.onBackPressed();
            // overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
        }
    }
}