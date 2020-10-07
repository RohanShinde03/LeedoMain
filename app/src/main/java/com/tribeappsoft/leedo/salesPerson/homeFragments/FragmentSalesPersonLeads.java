package com.tribeappsoft.leedo.salesPerson.homeFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.salesPerson.adapter.LeadsRecyclerAdapter;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.salesPerson.token.GenerateTokenActivity;
import com.tribeappsoft.leedo.admin.site_visits.AddSiteVisitActivity;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadListModel;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragmentSalesPersonLeads extends Fragment
{

    private static final String TAG = "FragSalesPersonLeads";
    //@BindView(R.id.cl_staffColleagues) CoordinatorLayout parent;
    @BindView(R.id.rl_fragSalesPersonLeads) RelativeLayout parent;
    @BindView(R.id.rv_fragSalesPersonLeads) RecyclerView recyclerView;
    @BindView(R.id.edt_fragSalesPersonLeads) TextInputEditText ed_colleague;

    @BindView(R.id.ll_fragSalesPersonLeads_searchBar) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.ll_fragSalesPersonLeads_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.iv_fragSalesPersonLeads_clearSearch) AppCompatImageView iv_clearSearch;
    @BindView(R.id.sr_fragSalesPersonLeads) SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.view_fragSalesPerson_disableLayout) View viewDisableLayout;
    @BindView(R.id.fab_fragSalesPerson_add) FloatingActionButton fab_add;
    @BindView(R.id.tv_frag_salesPersonLeads_titleAddLead) AppCompatTextView tv_titleAddLead;
    @BindView(R.id.fab_fragSalesPerson_addSiteVisit) FloatingActionButton fab_addSiteVisit;
    @BindView(R.id.tv_frag_salesPersonLeads_titleAddSiteVisit) AppCompatTextView tv_titleAddSiteVisit;
    @BindView(R.id.fab_fragSalesPerson_addToken) FloatingActionButton fab_addToken;
    @BindView(R.id.tv_frag_salesPersonLeads_titleAddToken) AppCompatTextView tv_titleAddToken;
    @BindView(R.id.fab_fragSalesPerson_addReminder) FloatingActionButton fab_addReminder;
    @BindView(R.id.tv_frag_salesPersonLeads_titleAddReminder) AppCompatTextView tv_titleAddReminder;


    @BindView(R.id.exFab_fragSalesPersonLeads_add) ExtendedFloatingActionButton exFab;


    private Context context;
    //private boolean doubleBackToExitPressedOnce = false;
    //private ArrayList<LeadsModel> itemArrayList = new ArrayList<>();
    //private ArrayList<LeadsModel> tempArrayList = new ArrayList<>();
    //private StaffColleagueAdapter staffColleagueAdapter;

    private LeadsRecyclerAdapter recyclerAdapter;
    private ArrayList<LeadListModel> itemArrayList, tempArrayList;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int staff_id = 0;
    private String api_token = "";
    private Boolean isFabOpen = false, isClickEvent = false;
    private Animation fab_open,fab_close,rotate_forward, rotate_backward;



    public FragmentSalesPersonLeads() {
        // Required empty public constructor
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(R.string.vendors);
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_sales_person_leads, container, false);
        View rootView = inflater.inflate(R.layout.fragment_sales_person_leads, container, false);
        context = rootView.getContext();
        //setHasOptionsMenu(true);
        ButterKnife.bind(this, rootView);
        //call method to hide keyBoard
        setupUI(parent);


        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();


        if (sharedPreferences!=null) {
            editor = sharedPreferences.edit();
            editor.apply();

            staff_id = sharedPreferences.getInt("staff_id", 0);
            api_token  = sharedPreferences.getString("api_token","");
        }

        itemArrayList = new ArrayList<>();
        tempArrayList = new ArrayList<>();

        //set Animation to Fab
        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_rotate_backward);

        //set fab animate
        new Handler().postDelayed(this::animateExtendedFab, 500);

        //setup recyclerView
        setupRecycleView();

        //
        setSwipeRefresh();


        fab_add.setOnClickListener(view -> {

            if (isFabOpen) {
                isClickEvent = true;
                //animateFAB();
                startActivity(new Intent(getActivity(), AddNewLeadActivity.class));
                //new Handler().postDelayed(this::animateFAB, 100);
            } else {
                animateFAB();
            }
        });


        //site visit
        fab_addSiteVisit.setOnClickListener(view ->{
            isClickEvent = true;
            startActivity(new Intent(getActivity(), AddSiteVisitActivity.class));
        });

        //token
        fab_addToken.setOnClickListener(view ->{
            isClickEvent = true;
            startActivity(new Intent(getActivity(), GenerateTokenActivity.class));
        });

        //add reminder
        fab_addReminder.setOnClickListener(view ->{
            isClickEvent = true;
            //startActivity(new Intent(getActivity(), AddReminderActivity.class));
        });

        //fab_add.setOnClickListener(view -> animateFAB());

        viewDisableLayout.setOnClickListener(v -> animateFAB());


        //exFab.setOnClickListener(v -> doNormalShare());
        exFab.setOnClickListener(view -> startActivity(new Intent(context, AddNewLeadActivity.class)));


        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                exFab.shrink();
                hideViews();
            }

            @Override
            public void onShow() {
                exFab.extend();
                showViews();
            }
        });

        return rootView;

    }


    private void animateFAB()
    {

        if(isFabOpen)
        {

            //fab_share.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_white));
            //fab_share.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close_white));
//            fab_share.startAnimation(rotate_backward);

            fab_add.startAnimation(rotate_backward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.main_black)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add_black_24dp));

            //close view
            closeView();

            fab_addSiteVisit.startAnimation(fab_close);
            fab_addSiteVisit.setClickable(false);
            fab_addToken.startAnimation(fab_close);
            fab_addToken.setClickable(false);
            fab_addReminder.startAnimation(fab_close);
            fab_addReminder.setClickable(false);

            /*fab_ni.startAnimation(fab_close);
            fab_cold.startAnimation(fab_close);
            fab_hot.setClickable(false);
            fab_ni.setClickable(false);
            fab_cold.setClickable(false);*/

            new Animations().slideOutBottom(tv_titleAddToken);
            new Animations().slideOutBottom(tv_titleAddSiteVisit);
            new Animations().slideOutBottom(tv_titleAddLead);
            new Animations().slideOutBottom(tv_titleAddReminder);

            //hide textView
            tv_titleAddLead.setVisibility(View.GONE);
            tv_titleAddSiteVisit.setVisibility(View.GONE);
            tv_titleAddToken.setVisibility(View.GONE);
            tv_titleAddReminder.setVisibility(View.GONE);

            viewDisableLayout.setVisibility(View.GONE);
            isFabOpen = false;


        }
        else
        {

            fab_add.startAnimation(rotate_forward);
            fab_add.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.secondaryColor)));
            fab_add.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_person_add_rotated_24px));

            //open view
            openView();

            fab_addSiteVisit.startAnimation(fab_open);
            fab_addSiteVisit.setClickable(true);
            fab_addToken.startAnimation(fab_open);
            fab_addToken.setClickable(true);
            fab_addReminder.startAnimation(fab_open);
            fab_addReminder.setClickable(true);

            /*fab_ni.startAnimation(fab_open);
            fab_cold.startAnimation(fab_open);
            fab_ni.setClickable(true);
            fab_cold.setClickable(true);*/

            //visible textView
            tv_titleAddLead.setVisibility(View.VISIBLE);
            tv_titleAddSiteVisit.setVisibility(View.VISIBLE);
            tv_titleAddToken.setVisibility(View.VISIBLE);
            tv_titleAddReminder.setVisibility(View.VISIBLE);

            new Animations().slideInBottom(tv_titleAddToken);
            new Animations().slideInBottom(tv_titleAddSiteVisit);
            new Animations().slideInBottom(tv_titleAddLead);
            new Animations().slideInBottom(tv_titleAddReminder);

            viewDisableLayout.setVisibility(View.VISIBLE);
            isFabOpen = true;

        }
    }



    private void openView()
    {
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.VISIBLE);
    }

    private void closeView()
    {

        // Hide the Panel
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_down);
        //ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.viewDisableLayout);
        bottomUp.setDuration(100);
        viewDisableLayout.startAnimation(bottomUp);
        viewDisableLayout.setVisibility(View.GONE);
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    private void hideViews()
    {

        ll_searchBar.animate().translationY(-ll_searchBar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {

        ll_searchBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //ll_studentEventList.setVisibility(ll_studentEventList.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE );
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void animateExtendedFab()
    {
        exFab.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(200);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                //exFab_donate.setBackgroundTintList(getResources().getColorStateList(colorIntArray[position]));


                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(200);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                exFab.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        exFab.startAnimation(shrink);
    }


    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new LeadsRecyclerAdapter(getActivity(), itemArrayList);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void setSwipeRefresh() {

        //getOffline();
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
            swipeRefresh.setOnRefreshListener(() ->
            {
                swipeRefresh.setRefreshing(true);
                //call_getColleague();
                setTempData();

            });

            swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

        } else Helper.NetworkError(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: " );

        //close fab open when returning back from activities opened by fab click
        if (isClickEvent) animateFAB();


        //perform search
        perform_search();

        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
            swipeRefresh.setRefreshing(true);
            //TODO replace method call
            //call_getColleague();
            setTempData();
        } else {

            Helper.NetworkError(getActivity());
            recyclerView.setVisibility(View.GONE);
            ll_noData.setVisibility(View.VISIBLE);
        }

    }


    private void setTempData()
    {
        //clear
        itemArrayList.clear();

        /*Static Data for LeadListModel*/
        LeadListModel listModel_1 = new LeadListModel();
        listModel_1.setLead_name("Mahesh Tavande");
        listModel_1.setLead_date("22 Aug 2019 at 11:30 am");
        listModel_1.setCountry_code("91");
        listModel_1.setLead_mobile("9876543210");
        listModel_1.setLead_status_id(6);
        listModel_1.setLead_type_id(1);
        listModel_1.setLead_status("Token Generated");
        listModel_1.setLead_token_number(getString(R.string.def_ghp_number));
        listModel_1.setLead_cuid_number(getString(R.string.def_cuID_number));
        listModel_1.setLead_project_name("YashOne Wakad");
        listModel_1.setLead_unit_type("2 BHK");
        listModel_1.setLead_tag("R & L");

        // title 1  -- R & L details
        LeadDetailsTitleModel titleModel = new LeadDetailsTitleModel();
        titleModel.setLead_details_title("Reference Details :");
        //detail 1
        LeadDetailsModel detailsModel = new LeadDetailsModel();
        detailsModel.setLead_details_text("Ref. By :");
        detailsModel.setLead_details_value("Mr. Sachin H. Patil");
        //detail 2
        LeadDetailsModel detailsModel1 = new LeadDetailsModel();
        detailsModel1.setLead_details_text("Ref. Mobile: ");
        detailsModel1.setLead_details_value("+91-9876543210");
        ArrayList<LeadDetailsModel> detailsModelArrayList = new ArrayList<>();
        detailsModelArrayList.add(detailsModel);
        detailsModelArrayList.add(detailsModel1);
        //set to details model
        titleModel.setLeadDetailsModels(detailsModelArrayList);


        // title 2  -- Site Visit details
        LeadDetailsTitleModel titleModel2 = new LeadDetailsTitleModel();
        titleModel2.setLead_details_title("Site Visit Details :");
        //detail 1
        LeadDetailsModel detailsModel2 = new LeadDetailsModel();
        detailsModel2.setLead_details_text("Visit Date :");
        detailsModel2.setLead_details_value("01 Sept 2019 at 01:00 pm");
        //detail 2
        LeadDetailsModel detailsModel3 = new LeadDetailsModel();
        detailsModel3.setLead_details_text("Conducted By : ");
        detailsModel3.setLead_details_value("Shweta Uplap");
        ArrayList<LeadDetailsModel> detailsModelArrayList1 = new ArrayList<>();
        detailsModelArrayList1.add(detailsModel2);
        detailsModelArrayList1.add(detailsModel3);
        //set to details model
        titleModel2.setLeadDetailsModels(detailsModelArrayList1);


        // title 2  -- Token details
        LeadDetailsTitleModel titleModel3 = new LeadDetailsTitleModel();
        titleModel3.setLead_details_title("Token Details :");
        //detail 1
        LeadDetailsModel detailsModel4 = new LeadDetailsModel();
        detailsModel4.setLead_details_text("Generation date :");
        detailsModel4.setLead_details_value("05 Sept 2019 at 10:30 am");
        //detail 2
        LeadDetailsModel detailsModel5 = new LeadDetailsModel();
        detailsModel5.setLead_details_text("Token Number : ");
        detailsModel5.setLead_details_value(getString(R.string.def_ghp_number));
        //detail 3
        LeadDetailsModel detailsModel6 = new LeadDetailsModel();
        detailsModel6.setLead_details_text("Token Type : ");
        detailsModel6.setLead_details_value("Priority Token");
        //detail 4
        LeadDetailsModel detailsModel7 = new LeadDetailsModel();
        detailsModel7.setLead_details_text("Event Name : ");
        detailsModel7.setLead_details_value("Yashone Wakad,Token Event");
        ArrayList<LeadDetailsModel> detailsModelArrayList2 = new ArrayList<>();
        detailsModelArrayList2.add(detailsModel4);
        detailsModelArrayList2.add(detailsModel5);
        detailsModelArrayList2.add(detailsModel6);
        detailsModelArrayList2.add(detailsModel7);
        //set to details model
        titleModel3.setLeadDetailsModels(detailsModelArrayList2);


        //set to lead list model
        ArrayList<LeadDetailsTitleModel> titleModelArrayList = new ArrayList<>();
        titleModelArrayList.add(titleModel);
        titleModelArrayList.add(titleModel2);
        titleModelArrayList.add(titleModel3);
        listModel_1.setDetailsTitleModelArrayList(titleModelArrayList);


        // unclaimed
        LeadListModel listModel_2 = new LeadListModel();
        listModel_2.setLead_name("Rohan Shinde");
        listModel_2.setLead_date("24 Aug 2019 at 11:30 am");
        listModel_2.setCountry_code("91");
        listModel_2.setLead_mobile("9876543210");
        listModel_2.setLead_status_id(1);
        listModel_2.setLead_type_id(2);
        listModel_2.setLead_status("Unclaimed");
        listModel_2.setLead_token_number(getString(R.string.def_ghp_number));
        listModel_2.setLead_cuid_number(getString(R.string.def_cuID_number));
        listModel_2.setLead_project_name("YashOne Wakad");
        listModel_2.setLead_unit_type("1 BHK");
        listModel_2.setLead_tag("General");


        // newAdded/ self
        LeadListModel listModel_3 = new LeadListModel();
        listModel_3.setLead_name("Sachin H. Patil");
        listModel_3.setLead_date("22 Aug 2019 at 11:30 am");
        listModel_3.setCountry_code("91");
        listModel_3.setLead_mobile("9876543210");
        listModel_3.setLead_status_id(4);
        listModel_3.setLead_type_id(3);
        listModel_3.setLead_status("New Lead");
        listModel_3.setLead_token_number("");
        listModel_3.setLead_cuid_number("VJYOH023–0007");
        listModel_3.setLead_project_name("YashOne Wakad");
        listModel_3.setLead_unit_type("1 BHK");
        listModel_3.setLead_tag("Direct");


        itemArrayList.add(listModel_2);
        itemArrayList.add(listModel_3);
        itemArrayList.add(listModel_1);
        itemArrayList.add(listModel_2);
        itemArrayList.add(listModel_3);
        itemArrayList.add(listModel_1);

        delayRefresh();

    }


    private void call_getColleague()
    {

        tempArrayList.clear();
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getStaffColleague(api_token, staff_id);
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
                .subscribe(new Subscriber<Response<JsonObject>>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted:");
                        delayRefresh();

                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onError(final Throwable e) {
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

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse) {
                        if (JsonObjectResponse.isSuccessful()) {
                            if (JsonObjectResponse.body() != null) {
                                if (!JsonObjectResponse.body().isJsonNull()) {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success"))
                                        isSuccess = !JsonObjectResponse.body().get("success").isJsonNull() ? JsonObjectResponse.body().get("success").getAsInt() : 0;

                                    if (isSuccess == 1) {
                                        if (JsonObjectResponse.body().has("data")) {
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray()) {
                                                JsonArray jsonArray = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                itemArrayList.clear();
                                                for (int i = 0; i < jsonArray.size(); i++) {
                                                    setJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                                tempArrayList.addAll(itemArrayList);


                                                //TODO testung sukrut
                                                /*getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        tv_loadingMsg.setText("Sukrut");

                                                    }
                                                });*/

                                            }
                                        }
                                    } else
                                        showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        } else {
                            // error case
                            switch (JsonObjectResponse.code()) {
                                case 404:
                                    showErrorLog(getString(R.string.something_went_wrong_try_again));
                                    break;
                                case 500:
                                    showErrorLog(getString(R.string.server_error_msg));
                                    break;
                                default:
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }

                    }
                });
    }

    private void setJson(JsonObject jsonObject) {
        LeadListModel model = new LeadListModel();

        if (jsonObject.has("country_code")) model.setLead_name(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile")) model.setLead_mobile(!jsonObject.get("mobile").isJsonNull() ? jsonObject.get("mobile").getAsString() : "");
        if (jsonObject.has("full_name")) model.setLead_project_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString() : "");
        if (jsonObject.has("profile_path")) model.setLead_date(!jsonObject.get("profile_path").isJsonNull() ? jsonObject.get("profile_path").getAsString() : "");

        itemArrayList.add(model);
    }


    private void delayRefresh() {

        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
            {
                swipeRefresh.setRefreshing(false);

                //ll_pb.setVisibility(View.GONE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new LeadsRecyclerAdapter(getActivity(), itemArrayList);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count == 0) {
                    //no VIDEOS
                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    //exFab.setVisibility(View.GONE);
                } else {
                    //Registrations are available
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                    //exFab.setVisibility(View.VISIBLE);
                }
            });
        }

    }


    private void perform_search() {

        //or you can search by the editTextFiler
        ed_colleague.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0)
            {

                //TODO uncomment
                //if (staffColleagueAdapter != null)
                {
                    String text = Objects.requireNonNull(ed_colleague.getText()).toString().toLowerCase(Locale.getDefault());
                    doFilter(text);

                    if (ed_colleague.getText().length() < 1) {
                        ed_colleague.clearFocus();
                        Helper.hideSoftKeyboard(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView());
                        iv_clearSearch.setVisibility(View.GONE);
                    } else {
                        //visible empty search ll
                        iv_clearSearch.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                iv_clearSearch.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }
        });


        //clear searchText
        iv_clearSearch.setOnClickListener(v -> {
            ed_colleague.setText("");
            // recyclerView.setVisibility(View.GONE);
//            ll_effectiveSearch.setVisibility(View.VISIBLE);
        });


    }


    // Filter Class
    private void doFilter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        itemArrayList.clear();

        if (charText.length() == 0) {
            itemArrayList.addAll(tempArrayList);
        } else {
            for (LeadListModel _obj : tempArrayList) {
                if (_obj.getLead_name().toLowerCase(Locale.getDefault()).contains(charText)
                        || _obj.getLead_mobile().toLowerCase(Locale.getDefault()).contains(charText)) {
                    itemArrayList.add(_obj);
                }

            }
        }
        delayRefresh();
    }

    private void showErrorLog(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);

                Helper.onErrorSnack(getActivity(), message);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
            });
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener((v, event) -> {
                Helper.hideSoftKeyboard(Objects.requireNonNull(getActivity()), view);
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
    public void onDetach() {
        super.onDetach();
    }

}
