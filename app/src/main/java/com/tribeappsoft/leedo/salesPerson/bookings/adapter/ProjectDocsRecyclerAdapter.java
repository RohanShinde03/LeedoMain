package com.tribeappsoft.leedo.salesPerson.bookings.adapter;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.tribeappsoft.leedo.BuildConfig;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
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


public class ProjectDocsRecyclerAdapter extends RecyclerView.Adapter<ProjectDocsRecyclerAdapter.AdapterViewHolder> {

    private Activity context;
    private ArrayList<EventProjectDocsModel> projectDocsModelArrayList;
    private final Animations anim;
    private int lastPosition = -1;
    private ShareDialog shareDialog;
    private CallbackManager callbackManager;
    private static final int Permission_CODE_DOC = 657;


    public ProjectDocsRecyclerAdapter(Activity activity, ArrayList<EventProjectDocsModel> eventModelArrayList) {

        this.projectDocsModelArrayList = eventModelArrayList;
        this.context = activity;
        this.anim = new Animations();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_project_docs, parent,false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position)
    {

        setAnimation(holder.cv_itemList, position);

        final EventProjectDocsModel myModel = projectDocsModelArrayList.get(position);

        holder.tv_docName.setText(myModel.getDocName());

        //final File locFile = new File(context.getExternalFilesDir(null), "docs" + File.separator + myModel.getDocId() + getFileName_from_filePath(myModel.getDocPath()));
        if (Helper.isValidContextForGlide(context))
        {
            Glide.with(context)//getActivity().this
                    .load(myModel.getDocPath())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().placeholder(R.color.primaryColor))
                    .apply(new RequestOptions().error(R.color.primaryColor))
                    .into(holder.iv_docImage);
        }


      /*  if (file.exists())
        {
            //file exists, set directly View option
           // holder.tv_download.setText("View Document");
            holder.iv_downloadIcon.setImageResource(R.drawable.ic_remove_red_eye_black_24dp);
        }
*/
        holder.iv_downloadIcon.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(myModel.getDocPath()));
            context.startActivity(intent);
        });


        holder.tv_share.setOnClickListener(v -> isStoragePermissionGranted(myModel,myModel.getDocPath(), holder.pb_downloadDoc,true));

        //holder.tv_download.setOnClickListener(v -> isStoragePermissionGranted(file, myModel.getDocPath(), holder.pb_downloadDoc));
        holder.cv_itemList.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(myModel.getDocPath()));
            context.startActivity(intent);
        });


        callbackManager = CallbackManager.Factory.create();
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
        builder_accept.setContentView(R.layout.layout_share_options_popup);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayoutCompat ll_share_with_fb,ll_share_with_whats_app, ll_share_with_more;
        ll_share_with_whats_app= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option1);
        ll_share_with_fb= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option2);
        ll_share_with_more= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option3);


        //share on WhatsApp
        Objects.requireNonNull(ll_share_with_whats_app).setOnClickListener(view -> {

            shareOnWhatsApp(myModel,dirFile);
            builder_accept.dismiss();
        });


        //share on FB
        Objects.requireNonNull(ll_share_with_fb).setOnClickListener(view -> {
            shareOnFB(myModel);
            builder_accept.dismiss();
        });


        //share on More
        Objects.requireNonNull(ll_share_with_more).setOnClickListener(view -> {

            doNormalShare(myModel);
            builder_accept.dismiss();
        });

        builder_accept.show();
    }


    /*Share on Whats App*/
    private void shareOnWhatsApp(final EventProjectDocsModel mymodel, File dirFile) {

        String title = "", description = "", path = "";
        if (!(mymodel.getDocName() == null)) title = mymodel.getDocName();
        if (!(mymodel.getDocText() == null)) description = mymodel.getDocText();
        if (!(mymodel.getDocPath() == null)) path = mymodel.getDocPath();

        final String extra_text = title + "\t\n" + description + "\n\n" + path;

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setPackage(context.getResources().getString(R.string.pkg_whatsapp));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        {
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(dirFile));
            share.putExtra(Intent.EXTRA_TEXT, extra_text);
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
            share.putExtra(Intent.EXTRA_TEXT, extra_text);
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

            final String extra_text = title + "\t\n" + description + "\n\n" + path;

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(mymodel.getDocPath()))
                    .setQuote("Project Document \n" +extra_text)
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


        final String extra_text = title + "\t\n" + description + "\n\n" + path;


        if (!mymodel.getDocPath().trim().isEmpty()) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            //shareIntent.setType("image/jpeg");
            shareIntent.setType("*/*");
            //shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            // shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, resource));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (android.content.ActivityNotFoundException ex) {
                new Helper().showCustomToast(context, "Apps not found!");
            }

        }
        else
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Document on"));
            } catch (android.content.ActivityNotFoundException ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "Apps not found!");
            }
        }


    }


    class AdapterViewHolder extends RecyclerView.ViewHolder
    {

        @BindView(R.id.cv_itemProjectDocs) MaterialCardView cv_itemList;
        @BindView(R.id.iv_itemProjectDocs_docImage) AppCompatImageView iv_docImage;
        @BindView(R.id.tv_itemProjectDocs_docName) AppCompatTextView tv_docName;
        @BindView(R.id.pb_itemProjectDocs_downloadDoc) ProgressBar pb_downloadDoc;
        @BindView(R.id.iv_itemProjectDocs_downloadIcon) AppCompatImageView iv_downloadIcon;
        //@BindView(R.id.tv_itemProjectDocs_download) AppCompatTextView tv_download;
        @BindView(R.id.tv_itemProjectDocs_share) AppCompatTextView tv_share;


        AdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private void isStoragePermissionGranted(EventProjectDocsModel myModel, String path, ProgressBar pb_donationList, boolean isShare)
    {
        File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Leado_Sales/");
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

                        try {
                            if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                            else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                            else showErrorLog(e.toString());
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }

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

    private void saveFile(ResponseBody response, File localFile, ProgressBar pb_donationList, boolean isShare, EventProjectDocsModel myModel) throws IOException {

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


}