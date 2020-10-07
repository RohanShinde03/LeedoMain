package com.tribeappsoft.leedo.salesPerson.bookings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.adapter.EventSliderAdapter;
import com.tribeappsoft.leedo.salesPerson.bookings.adapter.ProjectDocsRecyclerAdapter;
import com.tribeappsoft.leedo.salesPerson.models.EventsModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/*
 * Created by ${ROHAN} on 21/8/19.
 */
public class BookingEventDetailsActivity extends AppCompatActivity
{
    private static final String TAG = "BookingEventDetails";
    @BindView(R.id.tv_content_bookingEventDtl_Title) AppCompatTextView tv_EventDtl_Title;
    @BindView(R.id.tv_content_bookingEventDtl_DateTime) AppCompatTextView tv_EventDtl_DateTime;
    @BindView(R.id.tv_content_bookingEventDtl_Location) AppCompatTextView tv_EventDtl_Location;
    @BindView(R.id.tv_content_bookingEventDtl_AboutEvent_text) AppCompatTextView tv_EventDtl_AboutEvent;
    @BindView(R.id.tv_content_bookingEventDtl_totalRegistrationCount) AppCompatTextView tv_totalRegistrationCount;
    @BindView(R.id.tv_content_bookingEventDtl_totalBookingCount) AppCompatTextView tv_totalBookingCount;
    @BindView(R.id.tv_content_bookingEventDtl_totalOnHoldCount) AppCompatTextView tv_totalOnHoldCount;
    @BindView(R.id.tv_my_leads) AppCompatTextView tv_my_leads;
    @BindView(R.id.tv_my_booking) AppCompatTextView tv_my_booking;
    @BindView(R.id.tv_my_holds) AppCompatTextView tv_my_holds;
    @BindView(R.id.eventDetails_imageSlider) SliderView sliderView;
    @BindView(R.id.iv_bookingEventDtl_thumbnail) AppCompatImageView iv_thumbnail;
    @BindView(R.id.view_stats) View view_stats;
    @BindView(R.id.sfl_salesPerson_eventDtl)ShimmerFrameLayout sfl;


    @BindView(R.id.ll_bookingEventDtl_bannerImg) LinearLayoutCompat ll_bannerImg;
    @BindView(R.id.ll_stats) LinearLayoutCompat ll_stats;
    // @BindView(R.id.iv_bookingEventDtl_thumbnail) AppCompatImageView iv_eventDtl_thumbnail;
    @BindView(R.id.fab_bookingEventDtl_share) FloatingActionButton fab_eventDtl_share;


    @BindView(R.id.btn_bookingEventDtl_viewHoldFlats) MaterialButton btn_viewHoldFlats;
    @BindView(R.id.app_bar_bookingEventDtl) AppBarLayout app_bar;
    @BindView(R.id.collapsing_toolbarLayout_bookingEventDtl) CollapsingToolbarLayout collapsingToolbar;


    @BindView(R.id.ll_bookingEventDtl_noDetails) LinearLayoutCompat ll_noDetails;
    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;
    @BindView(R.id.nsv_bookingEventDetails) NestedScrollView nsv_eventDetails;
    @BindView(R.id.rv_bookingEvent_projectDocuments)RecyclerView rv_subEvent_MainList;
    @BindView(R.id.ll_bookingEvents_noData)LinearLayoutCompat ll_noDocumentsData;
    @BindView(R.id.ll_content_bookingEventDetail) LinearLayoutCompat ll_contentEventDetail;


    // @BindView(R.id.sfl_bookingEventDtl) ShimmerFrameLayout sfl;
    // @BindView(R.id.ll_main) CoordinatorLayout ll_main;

    Activity context;
    private String api_token = "",event_title="";
    private EventsModel eventModel = null;

    //private ArrayList<StudentEventDetailModel> detailModelArrayList;
    //private ArrayList<SubEventModel> subEventModelArrayList;
    private int event_id=0,event_status=0;

    private ProjectDocsRecyclerAdapter projectDocsRecyclerAdapter;
    private ShareDialog shareDialog;
    //0private Animations anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_event_details);
        ButterKnife.bind(this);
        context =  BookingEventDetailsActivity.this;

        //Helper.StatusBarTrans(context);
       // anim = new Animations();
        nsv_eventDetails.getParent().requestChildFocus(nsv_eventDetails, nsv_eventDetails);

        Toolbar toolbar =findViewById(R.id.toolbar_bookingEventDetails);
        setSupportActionBar(toolbar);
        this.setTitle("");

        //iv_eventDtl_thumbnail.setImageURI(Uri.parse("https://javdekars.com/images/banner/its-time-project-banner.jpg"));
        //homeUp button
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }



       /* Typeface typeface = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = context.getResources().getFont(R.font.bebas_neue);
        }
        else {
            //or to support all versions use
            typeface=  ResourcesCompat.getFont(this, R.font.bebas_neue);
        }
        collapsingToolbar.setCollapsedTitleTypeface(typeface);
        collapsingToolbar.setExpandedTitleTypeface(typeface);*/

        //call method to hide keyBoard


        SharedPreferences sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();

        //editor = sharedPreferences.edit();
        //editor.apply();
        if (sharedPreferences.getString("api_token", null) != null) api_token = sharedPreferences.getString("api_token", "");

        //set fab color
        //setFabIconColor();


//        if (getIntent()!=null) {
//            event_id = getIntent().getIntExtra("event_id", 0);
//        }

        if (getIntent()!=null)
        {
            if (getIntent().hasExtra("webUri")) {
                Uri data = getIntent().getParcelableExtra("webUri");
                boolean isWebUri = getIntent().getBooleanExtra("isWebUri", false);
                if (data != null) {
                    String url = data.toString(); //.substring(19 , data.toString().length());
                    if (url != null) Log.e("url", " " + url);

                    List<String> path = data.getPathSegments(); //getIntent().getData().getPathSegments();

                    String weblabel = path.get(0);
                    if (weblabel != null) Log.e("weblabel", " " + weblabel);

                    String eventId = path.get(1);
                    if (eventId != null) {
                        Log.e("eventId", eventId);
                        event_id = Integer.parseInt(eventId);
                    }
                }
            } else {
                //get data from intent
                event_id = getIntent().getIntExtra("event_id", 0);
                event_title = getIntent().getStringExtra("event_title");
                event_status = getIntent().getIntExtra("event_status", 0);
                Log.d("bundle", "id  " + event_id);
            }
        }

        collapsingToolbar.setTitle(event_title!=null && !event_title.isEmpty() ? event_title : "Event Details");//Set Title over collapsing toolbar layout
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbar.setContentScrimColor(Color.parseColor("#000000"));
        collapsingToolbar.setCollapsedTitleTextColor(Color.parseColor("#FFFFFF"));


        //set up project docs recycler adapter
        setUpRecycler();

        //delayRefresh();

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(this);

        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.vjd.salesapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        CallbackManager callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>()
        {
            @Override
            public void onSuccess(Sharer.Result result) {

                Log.e(TAG,  "postID "+result.getPostId());
                new Helper().showCustomToast(context, "Event Shared Successfully..!");
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

        //TODO -> unComment hiding event main layout --
        nsv_eventDetails.setVisibility(View.GONE);
        app_bar.setVisibility(View.GONE);

        if (Helper.isNetworkAvailable(this))
        {
            showProgressBar(getString(R.string.getting_event_details));
            showShimmer();
            new Handler().postDelayed(this::call_getEventDetails,1000);
        }
        else
        {
            Helper.NetworkError(this);
            onErrorLoadEventDetails();
        }

        getKeyHash();
        //setUpShareEvents();
    }

    void getKeyHash()
    {
        // GOOGLE PLAY APP SIGNING SHA-1 KEY:- 65:5D:66:A1:C9:31:85:AB:92:C6:A2:60:87:5B:1A:DA:45:6E:97:EA
        //SHA1: 13:7B:B3:8F:2F:2F:50:BF:D1:FB:E9:B7:04:EE:AC:C3:6E:0E:0B:A4

        byte[] sha1 =
                {
                        //0x65, 0x5D, 0x66, (byte)0xA1, (byte)0xC9, 0x31, (byte)0x85, (byte)0xAB, (byte)0x92, (byte)0xC6, (byte)0xA2, 0x60, (byte)0x87, 0x5B, 0x1A, (byte)0xDA, 0x45, 0x6E, (byte)0x97, (byte)0xEA
                        0x13, 0x7B, (byte)0xB3,(byte)0x8F, (byte)0x2F, 0x2F, (byte)0x50, (byte)0xBF, (byte)0xD1, (byte)0xFB, (byte)0xE9, (byte)0xB7, (byte)0x04, (byte)0xEE, (byte)0xAC, (byte)0xC3, 0x6E, 0x0E, (byte)0x0B, (byte)0xA4
                };

        System.out.println("keyhashGooglePlaySignIn:"+ Base64.encodeToString(sha1, Base64.NO_WRAP));

    }

//    private void setFabIconColor()
//    {
//        //get the drawable
//        Drawable myFabSrc = getResources().getDrawable(R.drawable.ic_share_white_24dp);
//        //copy it in a new one
//        Drawable willBeWhite = Objects.requireNonNull(myFabSrc.getConstantState()).newDrawable();
//        //set the color filter, you can use also Mode.SRC_ATOP
//        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//        //set it to your fab button initialized before
//        fab_eventDtl_share.setImageDrawable(willBeWhite);
//    }



    void setUpRecycler()
    {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        rv_subEvent_MainList.setLayoutManager(manager);
        rv_subEvent_MainList.setHasFixedSize(true);
        rv_subEvent_MainList.setNestedScrollingEnabled(false);
      /*  projectDocsRecyclerAdapter = new ProjectDocsRecyclerAdapter(context, eventModel.getEventProjectDocsModelArrayList());
        rv_subEvent_MainList.setAdapter(projectDocsRecyclerAdapter);*/
    }



    //    private void delayRefresh() {
//
//        if (context != null) {
//            runOnUiThread(() ->
//            {
//
//
//                if (isValidContextForGlide(context))
//                {
//                    Glide.with(context)//getActivity().this
//                            .load("https://javdekars.com/images/banner/its-time-project-banner.jpg")
//                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
//                            .apply(new RequestOptions().centerCrop())
//                            .apply(new RequestOptions().placeholder(R.color.primaryColor))
//                            .apply(new RequestOptions().error(R.color.primaryColor))
//                            .into(iv_eventDtl_thumbnail);
//                }
//
//                //ll_pb.setVisibility(View.GONE);
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
//                rv_subEvent_MainList.setLayoutManager(linearLayoutManager);
//                rv_subEvent_MainList.setHasFixedSize(true);
//                projectDocsRecyclerAdapter = new ProjectDocsRecyclerAdapter(this, projectDocsModelArrayList);
//                rv_subEvent_MainList.setAdapter(projectDocsRecyclerAdapter);
//                projectDocsRecyclerAdapter.notifyDataSetChanged();
//                int count = Objects.requireNonNull(rv_subEvent_MainList.getAdapter()).getItemCount();
//                if (count == 0) {
//                    //no VIDEOS
//                    rv_subEvent_MainList.setVisibility(View.GONE);
//                    //exFab.setVisibility(View.GONE);
//                } else {
//                    //Registrations are available
//                    rv_subEvent_MainList.setVisibility(View.VISIBLE);
//                    //exFab.setVisibility(View.VISIBLE);
//                }
//            });
//
//        }
//    }

    private void showShimmer() {
        btn_viewHoldFlats.setVisibility(View.GONE);
        ll_bannerImg.setVisibility(View.GONE);
        fab_eventDtl_share.setVisibility(View.GONE);
        sfl.setVisibility(View.VISIBLE);
        app_bar.setVisibility(View.GONE);
        sfl.startShimmer();
    }

    private void hideShimmer() {
        sfl.stopShimmer();
        sfl.setVisibility(View.GONE);
        app_bar.setVisibility(View.VISIBLE);
        fab_eventDtl_share.setVisibility(View.VISIBLE);
        ll_bannerImg.setVisibility(View.VISIBLE);
    }


    /**For Event**/
    public void call_getEventDetails()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getBookingEventDetails(api_token,event_id);
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
                        Log.d(TAG, "onCompleted:");
                        delayRefresh();
                        setSubEventListAdapter();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {
                        try {
                            Log.e(TAG, "onError: " + e.toString());
                            if (e instanceof SocketTimeoutException) showErrorLog("Socket Time out. Please try again!");
                            else if (e instanceof IOException) showErrorLog("Weak Internet Connection! Please try again!");
                            else showErrorLog(e.toString());
                        }
                        catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                        onErrorLoadEventDetails();
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {
                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if (JsonObjectResponse.body().isJsonObject())
                                {
                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();

                                    //hideLoading();
                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            JsonObject jsonObject  = JsonObjectResponse.body().get("data").getAsJsonObject();
                                            if (jsonObject!=null && !jsonObject.isJsonNull())
                                            {
                                                //detailModelArrayList.clear();
                                                setEventJson(jsonObject);
                                            }
                                            else
                                            {
                                                showErrorLog("Empty Data from server!");
                                                onErrorLoadEventDetails();
                                            }

                                        }
                                        else
                                        {
                                            showErrorLog("Empty response from server!");
                                            onErrorLoadEventDetails();
                                        }
                                    }
                                    else
                                    {
                                        showErrorLog(getString(R.string.something_went_wrong_try_again));
                                        onErrorLoadEventDetails();
                                    }
                                }

                            }
                        }
                        else {
                            // error case
                            switch (JsonObjectResponse.code())
                            {
                                case 404:
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    onErrorLoadEventDetails();
                                    break;
                                case 500:
                                    showErrorLog(getString(R.string.server_error_msg));
                                    onErrorLoadEventDetails();
                                    break;
                                default:
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    onErrorLoadEventDetails();
                                    break;
                            }
                        }

                    }
                });
    }

    private void setEventJson(JsonObject asJsonObject) {

        eventModel =new EventsModel();

        if (asJsonObject.has("event_id")) eventModel.setEvent_id(!asJsonObject.get("event_id").isJsonNull() ? asJsonObject.get("event_id").getAsInt() : 0 );
        if (asJsonObject.has("event_location")) eventModel.setEvent_venue(!asJsonObject.get("event_location").isJsonNull() ? asJsonObject.get("event_location").getAsString().trim() : "event_location" );
        if (asJsonObject.has("event_banner_path"))eventModel.setEvent_banner_path(!asJsonObject.get("event_banner_path").isJsonNull() ? asJsonObject.get("event_banner_path").getAsString().trim() : "");
        if (asJsonObject.has("event_start_date"))eventModel.setStart_date(!asJsonObject.get("event_start_date").isJsonNull() ? asJsonObject.get("event_start_date").getAsString().trim() : Helper.getTodaysDateString());
        if (asJsonObject.has("event_end_date"))eventModel.setEnd_date(!asJsonObject.get("event_end_date").isJsonNull() ? asJsonObject.get("event_end_date").getAsString().trim() : Helper.getTodaysDateString());
        if (asJsonObject.has("event_title"))eventModel.setEvent_title(!asJsonObject.get("event_title").isJsonNull() ? asJsonObject.get("event_title").getAsString().trim() : "event_title");
        if (asJsonObject.has("event_description"))eventModel.setEvent_description(!asJsonObject.get("event_description").isJsonNull() ? asJsonObject.get("event_description").getAsString().trim() : "event_description");
        if (asJsonObject.has("event_fee"))eventModel.setEvent_fee(!asJsonObject.get("event_fee").isJsonNull() ? asJsonObject.get("event_fee").getAsString().trim() : "0.00");
        if (asJsonObject.has("status"))eventModel.setStatus(!asJsonObject.get("status").isJsonNull() ? asJsonObject.get("status").getAsString().trim() : "status");
        if (asJsonObject.has("event_status"))eventModel.setEvent_status(!asJsonObject.get("event_status").isJsonNull() ? asJsonObject.get("event_status").getAsString().trim() : "event_status");
        if (asJsonObject.has("event_status_id"))eventModel.setEvent_status_id(String.valueOf(!asJsonObject.get("event_status_id").isJsonNull() ? asJsonObject.get("event_status_id").getAsInt() : 0));
        if (asJsonObject.has("is_registered"))eventModel.setIs_registered(!asJsonObject.get("is_registered").isJsonNull() ? asJsonObject.get("is_registered").getAsInt() : 0);


        if (asJsonObject.has("event_photos"))
        {
            if (!asJsonObject.get("event_photos").isJsonNull() && asJsonObject.get("event_photos").isJsonArray())
            {
                JsonArray jsonArray =  asJsonObject.get("event_photos").getAsJsonArray();
                ArrayList<String> stringArrayList=new ArrayList<>();
                stringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++)
                {
                    setMainEventJson(jsonArray.get(i).getAsString(),stringArrayList);
                }
                eventModel.setEventPhotoArrayList(stringArrayList);
            }
        }

        if (asJsonObject.has("attendance_stats"))
        {
            if (!asJsonObject.get("attendance_stats").isJsonNull() && asJsonObject.get("attendance_stats").isJsonObject())
            {
                JsonObject object1 = asJsonObject.get("attendance_stats").getAsJsonObject();
                if (object1.has("total_stats"))
                {
                    if (!object1.get("total_stats").isJsonNull() && object1.get("total_stats").isJsonObject())
                    {
                        JsonObject object = object1.get("total_stats").getAsJsonObject();

                        if (object.has("registrations"))eventModel.setTotalLeads(!object.get("registrations").isJsonNull() ? object.get("registrations").getAsInt() : 0);
                        if (object.has("Bookings"))eventModel.setTotalBookings(!object.get("Bookings").isJsonNull() ? object.get("Bookings").getAsInt() : 0);
                        if (object.has("On Holds"))eventModel.setTotalonHolds(!object.get("On Holds").isJsonNull() ? object.get("On Holds").getAsInt() : 0);
                    }

                    if (!object1.get("my_stats").isJsonNull() && object1.get("my_stats").isJsonObject())
                    {
                        JsonObject object = object1.get("my_stats").getAsJsonObject();

                        if (object.has("leads"))eventModel.setMyLeads(!object.get("leads").isJsonNull() ? object.get("leads").getAsInt() : 0);
                        if (object.has("Bookings"))eventModel.setMyBookings(!object.get("Bookings").isJsonNull() ? object.get("Bookings").getAsInt() : 0);
                        if (object.has("On Holds"))eventModel.setMyonHolds(!object.get("On Holds").isJsonNull() ? object.get("On Holds").getAsInt() : 0);
                    }
                }
            }

        }

        if (!asJsonObject.get("event_shareable_docs").isJsonNull() && asJsonObject.get("event_shareable_docs").isJsonArray())
        {
            if (!asJsonObject.get("event_shareable_docs").isJsonNull())
            {
                JsonArray jsonArray =  asJsonObject.get("event_shareable_docs").getAsJsonArray();

                if (jsonArray.size()>0)
                {
                    ArrayList<EventProjectDocsModel> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (int j = 0; j < jsonArray.size(); j++)
                    {
                        setProjectDocsJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    eventModel.setEventProjectDocsModelArrayList(arrayList);
                }
            }
        }

    }

    private void setMainEventJson(String imgpath, ArrayList<String> stringArrayList)
    {
        stringArrayList.add(imgpath);
    }

    private void setProjectDocsJson(JsonObject jsonObject, ArrayList<EventProjectDocsModel> arrayList)
    {
        EventProjectDocsModel eventProjectDocsModel = new EventProjectDocsModel();
        if (jsonObject.has("media_id")) eventProjectDocsModel.setDocId(!jsonObject.get("media_id").isJsonNull() ? jsonObject.get("media_id").getAsInt() : 0 );
        if (jsonObject.has("title")) eventProjectDocsModel.setDocName(!jsonObject.get("title").isJsonNull() ? jsonObject.get("title").getAsString() : "" );
        if (jsonObject.has("media_path")) eventProjectDocsModel.setDocPath(!jsonObject.get("media_path").isJsonNull() ? jsonObject.get("media_path").getAsString() : "" );
        if (jsonObject.has("description")) eventProjectDocsModel.setDocText(!jsonObject.get("description").isJsonNull() ? jsonObject.get("description").getAsString() : "" );
        arrayList.add(eventProjectDocsModel);

    }

    private void delayRefresh()
    {

        if (context != null) {
            context.runOnUiThread(() -> {

                hideProgressBar();
                hideShimmer();

                if (eventModel!=null)
                {

                    nsv_eventDetails.setVisibility(View.VISIBLE);
                    app_bar.setVisibility(View.VISIBLE);

                    //StudentEventDetailModel detailModel = detailModelArrayList.get(0);

                    tv_EventDtl_Title.setText(eventModel.getEvent_title());

                    //set date
                    if (eventModel.getStart_date()!=null && eventModel.getEnd_date()!=null) tv_EventDtl_DateTime.setText(String.format("%s - %s", Helper.formatEventDate(eventModel.getStart_date()), Helper.formatEventDate(eventModel.getEnd_date())));

                    tv_EventDtl_Location.setText(eventModel.getEvent_venue());
                    tv_EventDtl_AboutEvent.setText(eventModel.getEvent_description());

                    tv_totalRegistrationCount.setText(String.valueOf(eventModel.getTotalLeads()));
                    tv_totalBookingCount.setText(String.valueOf(eventModel.getTotalBookings()));
                    tv_totalOnHoldCount.setText(String.valueOf(eventModel.getTotalonHolds()));
                    tv_my_leads.setText(String.valueOf(eventModel.getMyLeads()));
                    tv_my_booking.setText(String.valueOf(eventModel.getMyBookings()));
                    tv_my_holds.setText(String.valueOf(eventModel.getMyonHolds()));

                    //Log.e(TAG, "delayRefresh: "+eventModel.getEventPhotoArrayList().toString() );

                    if (eventModel.getEventPhotoArrayList()!=null)
                    {
                        EventSliderAdapter adapter = new EventSliderAdapter(context,eventModel.getEventPhotoArrayList(),eventModel.getEvent_title());
                        sliderView.setSliderAdapter(adapter);
                        sliderView.setIndicatorAnimation(IndicatorAnimations.THIN_WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        //sliderView.setAutoCycleDirection(SliderView.SCROLLBAR_POSITION_RIGHT);
                        sliderView.setIndicatorVisibility(true);
                        sliderView.setIndicatorSelectedColor(getResources().getColor(R.color.white));
                        sliderView.setIndicatorUnselectedColor(getResources().getColor(R.color.GrayLight));
                        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
                        sliderView.startAutoCycle();

                        iv_thumbnail.setVisibility(View.GONE);
                        sliderView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        sliderView.setVisibility(View.GONE);
                        iv_thumbnail.setVisibility(View.VISIBLE);
                    }

                    //TODO hidden button - removed hold flat option from booking events
                    //hide when upcoming events
                    //btn_viewHoldFlats.setVisibility(event_status==2? View.GONE :View.VISIBLE);


                    //Todo hidden becoz not live status
                    //ll_stats.setVisibility(event_status==2? View.GONE :View.VISIBLE);
                    //view_stats.setVisibility(event_status==2? View.GONE :View.VISIBLE);

                    //set up share events
                    setUpShareEvents(eventModel);
                    btn_viewHoldFlats.setOnClickListener(v -> {

                        if (eventModel!=null)
                        {
                            Intent intent= new Intent(context, HoldFlatListActivity.class);
                            intent.putExtra("event_id", event_id);
                            intent.putExtra("event_title",eventModel.getEvent_title());
                            context.startActivity(intent);
                        }else  new Helper().showCustomToast(context, "Failed to load event details!");

                    });

                }
                else
                {
                    ll_noDetails.setVisibility(View.VISIBLE);
                    nsv_eventDetails.setVisibility(View.GONE);
                    btn_viewHoldFlats.setVisibility(View.GONE);
                    sliderView.setVisibility(View.GONE);
                    sfl.setVisibility(View.GONE);
                    app_bar.setVisibility(View.VISIBLE);
                    showErrorLog("failed to load event details!");
                }

            });
        }

    }

    private void setSubEventListAdapter()
    {
        if (context != null) {
            context.runOnUiThread(() -> {
                //progressbar_loading.setVisibility(View.GONE);

                if(eventModel!=null && eventModel.getEventProjectDocsModelArrayList()!=null)
                {
                    LinearLayoutManager manager = new LinearLayoutManager(context);
                    rv_subEvent_MainList.setLayoutManager(manager);
                    rv_subEvent_MainList.setHasFixedSize(true);
                    projectDocsRecyclerAdapter = new ProjectDocsRecyclerAdapter(context, eventModel.getEventProjectDocsModelArrayList());
                    rv_subEvent_MainList.setAdapter(projectDocsRecyclerAdapter);
                    projectDocsRecyclerAdapter.notifyDataSetChanged();
                    int count = Objects.requireNonNull(rv_subEvent_MainList.getAdapter()).getItemCount();
                    if (count==0)
                    {
                        //no Registrations
                        rv_subEvent_MainList.setVisibility(View.GONE);
                        ll_noDocumentsData.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        //Registrations are available
                        rv_subEvent_MainList.setVisibility(View.VISIBLE);
                        ll_noDocumentsData.setVisibility(View.GONE);
                    }
                }
                else
                {
                    //no documents
                    rv_subEvent_MainList.setVisibility(View.GONE);
                    ll_noDocumentsData.setVisibility(View.VISIBLE);
                }
            });
        }

    }


    private void onErrorLoadEventDetails()
    {
        runOnUiThread(() -> {

            //hide layouts
            ll_contentEventDetail.setVisibility(View.GONE);
            btn_viewHoldFlats.setVisibility(View.GONE);
            fab_eventDtl_share.setVisibility(View.GONE);
            sfl.setVisibility(View.GONE);
            app_bar.setVisibility(View.VISIBLE);
            ll_noDetails.setVisibility(View.VISIBLE);
        });

    }


    void setUpShareEvents(EventsModel eventModel) //(final StudentEventDetailModel detailModel)
    {

        FloatingActionButton fab = findViewById(R.id.fab_bookingEventDtl_share);
        fab.setOnClickListener(view -> shareVideo(eventModel));
    }



    private void showErrorLog(final String message)
    {
        if(context!=null)
        {
            runOnUiThread(() ->{
                hideProgressBar();
                Helper.onErrorSnack(context,message);
            });
        }
    }




    public void shareVideo(EventsModel eventModel) //(final StudentEventDetailModel studentEventDetailModel)
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

        final Dialog builder_accept=new BottomSheetDialog(this);
        builder_accept.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder_accept.setContentView(R.layout.layout_share_options_popup);
        Objects.requireNonNull(builder_accept.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayoutCompat linearLayout_option1,linearLayout_option2, linearLayout_option3;
        linearLayout_option1= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option1);
        linearLayout_option2= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option2);
        linearLayout_option3= builder_accept.findViewById(R.id.linearlayout_layout_select_popup_option3);

        //share on WhatsApp
        Objects.requireNonNull(linearLayout_option1).setOnClickListener(view -> {
            builder_accept.dismiss();
            shareOnWhatsApp(eventModel);
        });

        //share on FB
        Objects.requireNonNull(linearLayout_option2).setOnClickListener(view -> {
            builder_accept.dismiss();
            shareOnFB(eventModel);
        });

        //normal share for all apps
        assert linearLayout_option3 != null;
        linearLayout_option3.setOnClickListener(view -> {
            builder_accept.dismiss();
            doNormalShare(eventModel);
        });
        builder_accept.show();

    }


    private void shareOnWhatsApp(final EventsModel EventsModel)
    {
        //final String source= "https://privateedukolhapur.org/studentevent/"+ studentEventDetailModel.getEvent_id();
        final String source= "http://vjsalesapp.org/bookingevent/"+ EventsModel.getEvent_id();


        String title = EventsModel.getEvent_title();
        String author = EventsModel.getEvent_description();
        final String extra_text = '*'+title +'*'+ "\n About- \t"+author +"\n\n"+ source ;

        if (!eventModel.getEventPhotoArrayList().isEmpty())
        {
            if (Helper.isValidContextForGlide(context))
            {
                Glide
                        .with(context)
                        .asBitmap()
                        .load(eventModel.getEventPhotoArrayList().get(0))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .apply(new RequestOptions().placeholder(R.color.primaryColor))
                        .apply(new RequestOptions().error(R.color.primaryColor))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/jpeg");
                                shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
                                shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Helper.getLocalBitmapUri(context, resource));
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    startActivity(Intent.createChooser(shareIntent, "Share Event on"));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    ex.printStackTrace();
                                    new Helper().showCustomToast(context, "WhatsApp not installed!!");
                                }

                            }
                        });
            }
        }
        else
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(shareIntent, "Share Event on"));
            } catch (android.content.ActivityNotFoundException ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "WhatsApp not installed!");
            }
        }
    }


    private void shareOnFB(final EventsModel EventsModel)
    {
        if (ShareDialog.canShow(ShareLinkContent.class))
        {

            final String source= "https://vjsalesapp.org/bookingevent/"+EventsModel.getEvent_id();
            String title = EventsModel.getEvent_title();
            String author =EventsModel.getEvent_description();
            final String extra_text = '*'+title +'*'+ "\n About- \t"+author +"\n\n"+ source ;


            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(source))
                    .setQuote("Leedo Event \n" +extra_text)
                    .setShareHashtag(new ShareHashtag.Builder()
                            .setHashtag("#Leedo")
                            .build())
                    .build();
            if (Helper.isPackageExisted(getApplicationContext(), getString(R.string.pkg_fb)))
                shareDialog.show(linkContent);  // Show facebook ShareDialog
            else shareDialog.show(linkContent, ShareDialog.Mode.WEB);

        }
    }

    private void doNormalShare(final EventsModel EventsModel)
    {

        final String source= "https://vjsalesapp.org/bookingevent/"+EventsModel.getEvent_id();
        String title =  EventsModel.getEvent_title();
        String author = EventsModel.getEvent_description();
        final String extra_text = '*'+title +'*'+ "\n About- \t"+author +"\n\n"+ source ;

        if (!eventModel.getEventPhotoArrayList().isEmpty())
        {
            if (Helper.isValidContextForGlide(context))
            {
                Glide
                        .with(context)
                        .asBitmap()
                        .load(eventModel.getEventPhotoArrayList().get(0))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/jpeg");
                                //shareIntent.setPackage(getResources().getString(R.string.pkg_whatsapp));
                                shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Helper.getLocalBitmapUri(context, resource));
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    startActivity(Intent.createChooser(shareIntent, "Share Event on"));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    ex.printStackTrace();
                                    new Helper().showCustomToast(context, "Apps not found!");
                                }

                            }
                        });
            }
        }
        else
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(shareIntent, "Share Event on"));
            } catch (android.content.ActivityNotFoundException ex) {
                ex.printStackTrace();
                new Helper().showCustomToast(context, "Apps not found!");
            }
        }

    }


    void hideProgressBar()
    {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }



    /*   @Override
       public void onBackPressed() {
           super.onBackPressed();
           //context.overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
       }
   *//*

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (isWebUri) {
                startActivity(new Intent(context, SalesPersonBottomNavigationActivity.class));
                finish();
            } else onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }

        return false;
    }
*//*
     */
    void showProgressBar(String message)
    {
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        //ll_main.setVisibility(View.GONE);
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
    public boolean onOptionsItemSelected(MenuItem item)
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
