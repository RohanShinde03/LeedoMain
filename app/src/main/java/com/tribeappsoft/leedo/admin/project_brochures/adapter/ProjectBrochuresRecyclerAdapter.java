package com.tribeappsoft.leedo.admin.project_brochures.adapter;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.BuildConfig;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.admin.project_brochures.AddNewBrochureActivity;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;
import static com.facebook.FacebookSdk.getApplicationContext;


public class ProjectBrochuresRecyclerAdapter extends RecyclerView.Adapter<ProjectBrochuresRecyclerAdapter.AdapterViewHolder> {

    private Activity context;
    private ArrayList<EventProjectDocsModel> projectDocsModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private static final int Permission_CODE_DOC = 657;
    private ShareDialog shareDialog;
    private String api_token = "";

    public ProjectBrochuresRecyclerAdapter(Activity activity, ArrayList<EventProjectDocsModel> eventModelArrayList) {

        this.projectDocsModelArrayList = eventModelArrayList;
        this.context = activity;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_project_docs_temp, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {
        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isSalesHead = sharedPreferences.getBoolean("isSalesHead", false);
        editor.apply();
        api_token = sharedPreferences.getString("api_token", "");

        //set fab visibility only for sales head option
        holder.tv_delete.setVisibility(isSalesHead ? View.VISIBLE : View.GONE);
        holder.tv_update.setVisibility(isSalesHead ? View.VISIBLE : View.GONE);

        setAnimation(holder.cv_itemList, position);
        final EventProjectDocsModel myModel = projectDocsModelArrayList.get(position);

        //if(myModel.getDate() != null && !myModel.getDate().trim().isEmpty()) holder.tv_itemProjectDocs_date.setText(myModel.getDate() != null && !myModel.getDate().trim().isEmpty()? myModel.getDate():"--");
        //else  holder.tv_itemProjectDocs_date.setVisibility(View.GONE);
        //if(myModel.getDocText() != null && !myModel.getDocText().trim().isEmpty()) holder.tv_docDescription.setText(myModel.getDocText() != null && !myModel.getDocText().trim().isEmpty()? myModel.getDocText():"--");
        //else  holder.tv_docDescription.setVisibility(View.GONE);

        holder.tv_docName.setText(myModel.getDocName() != null && !myModel.getDocName().trim().isEmpty()? myModel.getDocName():"--");
        holder.tv_docProjectName.setText(myModel.getProjectName() != null && !myModel.getProjectName().trim().isEmpty()? myModel.getProjectName():"--");
        holder.tv_docDesc.setText(myModel.getBrochure_description() != null && !myModel.getBrochure_description().trim().isEmpty()? myModel.getBrochure_description():"--");

        String extension = myModel.getDocPath().substring(myModel.getDocPath().lastIndexOf(".")+1);
       // String extension = ff.getAbsolutePath().substring(ff.getAbsolutePath().lastIndexOf("."));
        holder.tv_DocType.setText(extension);
        holder.tv_DocType.setVisibility(extension!=null && !extension.trim().isEmpty() ? View.VISIBLE :View.GONE );

        //first create parent directory
        File parentDirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Lead_Management/");
        //create parent directory
        parentDirFile.mkdir();

        File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Lead_Management/ProjectBrochures/");
        //StoredPath = DIRECTORY + pic_name + ".png";
        //File file = new File(dirFile, getFileName_from_filePath(path));
        File file = new File(dirFile.getPath(), Helper.getFileName_from_filePath(myModel. getDocPath()));

        //create file dir
        dirFile.mkdir();

       /* if (file.exists())
        {
            //file exists, set directly View option
            //holder.tv_download.setText("View Document");
            holder.iv_downloadIcon.setImageResource(R.drawable.ic_remove_red_eye_black_24dp);
        }
*/
        // holder.iv_downloadIcon.setOnClickListener(v -> isStoragePermissionGranted(file, myModel.getDocPath(), holder.pb_downloadDoc));


        holder.iv_downloadIcon.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(myModel. getDocPath()));
            context.startActivity(intent);
        });

        holder.tv_share.setOnClickListener(v -> isStoragePermissionGranted(myModel,myModel.getDocPath(), holder.pb_downloadDoc,true));

        //For Update
        holder.tv_update.setOnClickListener(view -> {

            context.startActivity(new Intent(context, AddNewBrochureActivity.class)
                    .putExtra("project_id",myModel.getProject_id())
                    .putExtra("project_name",myModel.getProjectName())
                    .putExtra("project_brochure_id", myModel. getDocId())
                    .putExtra("brochure_title", myModel. getDocName())
                    .putExtra("media_path", myModel. getDocPath())
                    .putExtra("media_type_id",myModel.getMedia_type_id())
                    .putExtra("project_brochure_desc", myModel. getDocText())
                    .putExtra("fromOther", 2)
            );

            /*Intent intent = new Intent(context, AddNewBrochureActivity.class);
            intent.putExtra("project_id",myModel.getProject_id());
            intent.putExtra("project_name",myModel.getProjectName());
            intent.putExtra("project_brochure_id", myModel. getDocId());
            intent.putExtra("brochure_title", myModel. getDocName());
            intent.putExtra("media_path", myModel. getDocPath());
            intent.putExtra("project_brochure_desc", myModel. getDocText());
            intent.putExtra("fromOther", 2);

            // Get the transition name from the string
            String transitionName = context.getString(R.string.transition_string);
            // Define the view that the animation will start from
            View viewStart = holder.cv_itemList;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, viewStart, transitionName);
            //Start the Intent
            ActivityCompat.startActivity(context, intent, options.toBundle());*/

        });

       /* holder.tv_delete.setOnClickListener(v -> {

            if (isNetworkAvailable(Objects.requireNonNull(context)))
            {
                Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
                holder.cv_itemList.startAnimation(animZoomIn);
                Handler handle = new Handler();
                handle.postDelayed(() -> call_deleteProjectBrochure(myModel. getDocId(), position), 500);
            } else NetworkError(context);
        });*/


        holder.tv_delete.setOnClickListener(v -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(context))) {
                //show confirmation dialog
                showSubmitLeadAlertDialog(myModel.getDocId(), position, holder);
            } else Helper.NetworkError(context);
        });


       /* holder.tv_share.setOnClickListener(v -> {
            shareDataForSocial(myModel,file);
        });*/


        holder.iv_downloadIcon.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(myModel. getDocPath()));
            context.startActivity(intent);
        });


        CallbackManager callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(context);


        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>()
        {
            @Override
            public void onSuccess(Sharer.Result result) {

                Log.e("",  "postID "+result.getPostId());
                new Helper().showCustomToast(context, "Document Shared Successfully..!");
               /* if (result.getPostId()!=null) Toast.makeText(EventDetailActivity.this, "Event Shared Successfully..!", Toast.LENGTH_LONG).show();
                else Toast.makeText(EventDetailActivity.this, "You cancelled this share!", Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onCancel() {
                new Helper().showCustomToast(context, "You cancelled this share!");
            }

            @Override
            public void onError(FacebookException error) {
                new Helper().showCustomToast(context, "Error occurred during this share!");
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != projectDocsModelArrayList ? projectDocsModelArrayList.size() : 0);
    }


    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }


    static class AdapterViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.cv_itemProjectDocs_mainList) MaterialCardView cv_itemList;
        @BindView(R.id.tv_itemProjectDocs_docName) AppCompatTextView tv_docName;
        @BindView(R.id.tv_itemProjectDocs_docDesc) AppCompatTextView tv_docDesc;
        @BindView(R.id.tv_itemProjectDocs_projectName) AppCompatTextView tv_docProjectName;
        @BindView(R.id.tv_itemProjectDocs_DocType) AppCompatTextView tv_DocType;
        @BindView(R.id.pb_itemProjectDocs_downloadDoc) ProgressBar pb_downloadDoc;
        @BindView(R.id.mBtn_itemProjectDocs_view) MaterialButton iv_downloadIcon;
        @BindView(R.id.iv_itemProjectDocs_delete) AppCompatImageView tv_delete;
        @BindView(R.id.iv_itemProjectDocs_update) AppCompatImageView tv_update;
        @BindView(R.id.mBtn_itemProjectDocs_share) MaterialButton tv_share;

        //@BindView(R.id.tv_itemProjectDocs_docDescription) AppCompatTextView tv_docDescription;
        //@BindView(R.id.tv_itemProjectDocs_date) AppCompatTextView tv_itemProjectDocs_date;



        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void showSubmitLeadAlertDialog(int project_brochure_id, int position, AdapterViewHolder holder)
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

        tv_msg.setText(context.getString(R.string.delete_project_brochure_question));
        tv_desc.setText(context.getString(R.string.delete_brochure_confirmation));
        btn_negativeButton.setText(context.getString(R.string.no));
        btn_positiveButton.setText(context.getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            //call add lead api
            if (Helper.isNetworkAvailable(context))
            {
                call_deleteProjectBrochure(project_brochure_id, position);

                Handler handle = new Handler();
                handle.postDelayed(() -> {


                    Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_left_out);
                    holder.cv_itemList.startAnimation(animZoomIn);

                }, 0);
            }else Helper.NetworkError(context);
        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
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


    /*Share Data*/
    private void shareDataForSocial(EventProjectDocsModel myModel, File dirFile)
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
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayoutCompat ll_share_with_fb,ll_share_with_whats_app, ll_share_with_more,ll_share_with_mail;
        MaterialButton mBtn_share_with_whats_app,mBtn_share_with_mail, mBtn_share_with_more;
        ll_share_with_whats_app= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option1);
        mBtn_share_with_whats_app= builder_accept.findViewById(R.id.mBtn_shareBrochure_waShare);
        ll_share_with_mail= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option2);
        mBtn_share_with_mail= builder_accept.findViewById(R.id.mBtn_shareBrochure_mailShare);
        ll_share_with_fb= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option4);
        ll_share_with_more= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option3);
        mBtn_share_with_more= builder_accept.findViewById(R.id.mBtn_shareBrochure_moreShare);


        //share on WhatsApp
        Objects.requireNonNull(mBtn_share_with_whats_app).setOnClickListener(view -> {

            shareOnWhatsApp(myModel,dirFile);
            builder_accept.dismiss();
        });


        //share on mail
        Objects.requireNonNull(mBtn_share_with_mail).setOnClickListener(view -> {
            shareOnEmail(myModel,dirFile);
            builder_accept.dismiss();

        });


        //share on FB
        Objects.requireNonNull(ll_share_with_fb).setOnClickListener(view -> {
            shareOnFB(myModel);
            builder_accept.dismiss();
        });


        //share on More
        Objects.requireNonNull(mBtn_share_with_more).setOnClickListener(view -> {

            doNormalShare(myModel);
            builder_accept.dismiss();
        });

        builder_accept.show();
    }

    /* *//*Share on Whats App*//*
    private void shareOnWhatsApp(final EventProjectDocsModel mymodel) {

        String title = "", description = "", path = "", thumbnail = "https://javdekars.com/images/banner/its-time-project-banner.jpg";

        if (!(myModel. getDocName() == null)) title = mymodel.getDocName();
        if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
        if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();
        if (!(mymodel.getDocThumbnail() == null)) thumbnail = mymodel.getDocThumbnail();

        final String extra_text = title + "\t\n" + description + "\n\n" + path;


        if (!mymodel.getDocPath().trim().isEmpty()) {

            if (isValidContextForGlide(context)) {
                Glide
                        .with(context)
                        .asBitmap()
                        .load(thumbnail)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/jpeg");
                                shareIntent.setPackage(context.getResources().getString(R.string.pkg_whatsapp));
                                shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, resource));
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Event on"));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    new Helper().showCustomToast(context, "WhatsApp not installed!");
                                }

                            }
                        });
            }

        }
    }*/

    private void shareOnEmail(final EventProjectDocsModel mymodel, File dirFile) {

        String title = "", description = "", path = "";
        if (!(mymodel.getDocName() == null)) title = mymodel.getDocName();
        if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
        if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();

        final String extra_text = "Project Brochure from Leedo App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;


        if (mymodel!=null) {
            new Helper().openEmailIntent(context,  "Enter Email id here", "Project Brochure from Leedo App", extra_text);
        }
        else new Helper().showCustomToast(context, "Project brochure document might be null or empty!");

    }

    /*Share on Whats App*/
    private void shareOnWhatsApp(final EventProjectDocsModel mymodel, File dirFile) {

        String title = "", description = "", path = "";
        if (!(mymodel.getDocName() == null)) title = mymodel.getDocName();
        if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
        if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();

        final String extra_text = "Project Brochure from Leedo App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setPackage(context.getResources().getString(R.string.pkg_whatsapp));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        {
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(dirFile));
           // share.putExtra(Intent.EXTRA_TEXT, extra_text);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
            //share.setType("image/*");
        }
        else
        {
            // Uri photoUri = FileProvider.getUriForFile(AddNewVisitor.this, AddNewVisitor.this.getPackageName() + ".provider", imageFile);
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", dirFile);
            //share.setDataAndType(fileUri, "application/pdf");
            //share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM,fileUri);
            //share.putExtra(Intent.EXTRA_TEXT, extra_text);
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            share.setType("*/*");
        }

        context.startActivity(Intent.createChooser(share, "Share Using"));
    }

    private void shareOnFB(final EventProjectDocsModel mymodel)
    {
        if (ShareDialog.canShow(ShareLinkContent.class))
        {
            String title = "", description = "", path = "";
            if (!(mymodel.getDocName() == null)) title = mymodel.getDocName();
            if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
            if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();

            final String extra_text = "Project Brochure from Leedo App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(mymodel.getDocPath()))
                    //.setQuote("Project Document \n" +extra_text)
                    .setShareHashtag(new ShareHashtag.Builder()
                            //.setHashtag("#PrivateEducationEvents")
                            .build())
                    .build();
            if (Helper.isPackageExisted(context.getApplicationContext(), context.getString(R.string.pkg_fb)))
                shareDialog.show(linkContent);  // Show facebook ShareDialog
            else shareDialog.show(linkContent, ShareDialog.Mode.WEB);

        }
    }

    private void doNormalShare(final EventProjectDocsModel mymodel)
    {

        String title = "", description = "", path = "";
        if (!(mymodel.getDocName() == null)) title = mymodel.getDocName();
        if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
        if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();


        final String extra_text = "Project Brochure from Leedo App *"+ title + "*.\t\n\n" + description + "\n\n Ref. Link:- \n" + path;

        if (!mymodel.getDocPath().trim().isEmpty()) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            //shareIntent.setType("image/jpeg");
            shareIntent.setType("*/*");
            //shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
           // shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            // shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, resource));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (ActivityNotFoundException ex) {
                new Helper().showCustomToast(context, "Apps not found!");
            }

        }
        else
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            //shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (ActivityNotFoundException ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "Apps not found!");
            }
        }


    }


    private void isStoragePermissionGranted(EventProjectDocsModel myModel, String path, ProgressBar pb_donationList, boolean isShare)
    {
        File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Lead_Management/Project_Brochures/");
        //StoredPath = DIRECTORY + pic_name + ".png";
        //File file = new File(dirFile, getFileName_from_filePath(path));
        File file = new File(dirFile.getPath(), Helper.getFileName_from_filePath(path));
        //create file dir
        dirFile.mkdir();
        //Log.e(TAG, "dir_Path:- " +dirFile.getPath());

        if (file.exists())
        {
            //open file directly
//          showProgressBar(getString(R.string.opening_your_document));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkPermission())
                {
                    if(isShare)shareDataForSocial(myModel,file);
                    else openFileIntent(file, pb_donationList);
                }
                else requestPermissionViewFile(myModel,path, file,  false, pb_donationList,isShare);
            } else{
                if(isShare)shareDataForSocial(myModel,file);
                else openFileIntent(file, pb_donationList);
            }

        }
        else
        {
            //download and open
            //showProgressBar(getString(R.string.downloading_your_document));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermissionViewFile(myModel, path, file, true, pb_donationList, isShare);
            else downloadFile(myModel,path, file, pb_donationList,isShare);
        }

    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(context), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissionViewFile(EventProjectDocsModel myModel, String path, File localFile, boolean isDownload, ProgressBar pb_donationList, boolean isShare)
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {

            if (isDownload) downloadFile(myModel, path, localFile, pb_donationList, isShare);
            else openFileIntent(localFile, pb_donationList);
            return;
        }
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

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Checking the request code of our request

        if (requestCode == Permission_CODE_DOC)  //handling documents permission
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Displaying a toast

                new Helper().showCustomToast(context, context.getString(R.string.permission_grant_success));
                //open documents once permission is granted
                //OpenDocuments();
            }
        }

    }*/


    private void downloadFile(EventProjectDocsModel myModel, String url, final File localFile, ProgressBar pb_donationList, boolean isShare)
    {
        if (Helper.isNetworkAvailable(context))
        {
            pb_donationList.setVisibility(View.VISIBLE);
            new Helper().showCustomToast(context, "Opening your document...");

            try {
                ApiClient client = ApiClient.getInstance();
                Call<ResponseBody> call = client.getApiService().downloadFile(url);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                        if(response.isSuccessful()) {
                            try {
                                saveFile(Objects.requireNonNull(response.body()), localFile, pb_donationList,isShare,myModel);
                            } catch (IOException e) {
                                e.printStackTrace();
                                context.runOnUiThread(() -> {

                                    pb_donationList.setVisibility(View.GONE);
                                    new Helper().showCustomToast(context, "Failed to save document!");
                                });
                            }
                        }else {
                            context.runOnUiThread(() -> {

                                pb_donationList.setVisibility(View.GONE);
                                new Helper().showCustomToast(context, "No document found!");
                            });

                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable e) {
                        Log.e("StudentDonationAdapter", "onFailure " + e.getMessage());

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                        else showErrorLog(e.toString());

                        context.runOnUiThread(() -> {

                            pb_donationList.setVisibility(View.GONE);
                            new Helper().showCustomToast(context, "Failed to open document!");

                        });

                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
                new Helper().showCustomToast(context, "Error! No document found!");
            }
        }
        else Helper.NetworkError(context);
    }
    private void saveFile(ResponseBody response, File localFile, ProgressBar pb_donationList, boolean isShare, EventProjectDocsModel myModel) throws IOException
    {

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

        context.runOnUiThread(() -> {

//            new Helper().showCustomToast(context, "Receipt successfully downloaded!");
            pb_donationList.setVisibility(View.GONE);

            if (isShare)shareDataForSocial(myModel,localFile);
            else openFileIntent(localFile, pb_donationList);
        });

        //        hideProgressBar();
        //open

        //if (isSHare ==0 ) openFileIntent(localFile);
    }

    private void openFileIntent(File file, ProgressBar pb_donationList)
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else
        {

//            Uri fileUri = FileProvider.getUriForFile(AllCustomerDetailActivity.this, "com.crm.crm.fileprovider", localFile);
            //target.setDataAndType(fileUri, "application/pdf");
//TODO
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(fileUri, type);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        try
        {
//            hideProgressBar();
            pb_donationList.setVisibility(View.GONE);
            context.startActivity(intent);

        }catch(ActivityNotFoundException e)
        {
//            hideProgressBar();
            e.printStackTrace();
            pb_donationList.setVisibility(View.GONE);
            new Helper().showCustomToast(context, "No Application available to view this file!");
        }

    }



    private void showErrorLog(final String message)
    {
        context.runOnUiThread(() -> Helper.onErrorSnack(context, message));

    }


    private void call_deleteProjectBrochure(int project_brochure_id, int position)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("project_doc_id", project_brochure_id );

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().deleteProjectDocs(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.body() != null) {
                    String success = response.body().get("success").toString();
                    //ll_pb.setVisibility(View.GONE);
                    switch (success) {
                        case "1":
                            onSuccessDeleteBrochure(position);
                            break;
                        case "2":
                            showErrorLog("Error occurred during deleting project brochure!");
                            break;
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(context.getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(context.getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });


    }


    private void onSuccessDeleteBrochure(int position)
    {
        context.runOnUiThread(() -> {

            Log.e(TAG, " Mark as Done Success:");
            projectDocsModelArrayList.remove(position);
            notifyDataSetChanged();
            //fragmentSalesPersonReminders.delayRefresh();
        });
    }

}