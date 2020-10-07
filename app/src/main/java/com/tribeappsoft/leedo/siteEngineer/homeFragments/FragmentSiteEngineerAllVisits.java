package com.tribeappsoft.leedo.siteEngineer.homeFragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.siteEngineer.adapter.AllSiteVisitsRecyclerAdapter;
import com.tribeappsoft.leedo.siteEngineer.models.SiteVisitsModel;
import com.tribeappsoft.leedo.util.Helper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSiteEngineerAllVisits extends Fragment {

    @BindView(R.id.sr_fragSiteEngineerAllVisits) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_fragSiteEngineerAllVisits) RecyclerView recyclerView;
    @BindView(R.id.ll_fragSiteEngineerAllVisits_noData) LinearLayoutCompat ll_noData;
    private Context context;

    private String api_token = "", TAG = "FragmentSiteEngineerAllVisits";
    private int student_id = 0;
    private int student_type = 0;


    private ArrayList<SiteVisitsModel> modelArrayList;
    private AllSiteVisitsRecyclerAdapter recyclerAdapter;

    public FragmentSiteEngineerAllVisits() {
        // Required empty public constructor
        modelArrayList = new ArrayList<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
        //Objects.requireNonNull(getActivity()).setTitle(R.string.event_registrations);
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar!=null)
        {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) actionBar.getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.event_registrations));
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_site_engineer_all_visits, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

//        Bundle bundle = this.getArguments();
//        if (bundle != null)
//        {
//            isRegPending = bundle.getBoolean("isRegPending",false);
//        }

        try
        {
            rootView.setFocusableInTouchMode(true);
            rootView.requestFocus();
            rootView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                    {

                            DrawerLayout drawer = Objects.requireNonNull(getActivity()).findViewById(R.id.drawer_layout_siteEngineer);
                            if (drawer.isDrawerOpen(GravityCompat.START)) {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                            else {

                                Fragment fragment = new FragmentSiteEngineerHome();
                                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.trans_left_in, R.anim.trans_left_out)
                                        .replace(R.id.content_siteEngineer_home,fragment)
                                        .commit();
                            }


                        return true;
                    }
                }
                return false;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = new Helper().getSharedPref(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();


        api_token = sharedPreferences.getString("api_token","");
        student_id = sharedPreferences.getInt("student_id",0);
        student_type = sharedPreferences.getInt("student_type",0);

        setUpRecycler();

        setSwipeRefresh();

        return rootView;
    }

    @Override
    public void onResume()
    {

        super.onResume();

        new Handler().postDelayed(() -> {

            if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            {
                swipeRefresh.setRefreshing(true);
                getAllRegisteredEvents();
            }
            else Helper.NetworkError(getActivity());

        }, 500);

    }

    private void setUpRecycler()
    {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new AllSiteVisitsRecyclerAdapter(getActivity(), modelArrayList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void setSwipeRefresh()
    {
        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
        {
            swipeRefresh.setOnRefreshListener(() -> {
                swipeRefresh.setRefreshing(true);
                getAllRegisteredEvents();
            });

            swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

        } else Helper.NetworkError(getActivity());

    }

    private void getAllRegisteredEvents() {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getStudentRegisteredEventList(api_token, student_id,student_type);
        responseObservable.subscribeOn(Schedulers.newThread());
        responseObservable.asObservable();
        responseObservable.doOnNext(jsonArrayResponse -> {
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
                        Log.d("TAG", "onCompleted:");
                        delayRefresh();
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
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onNext(Response<JsonObject> JsonObjectResponse)
                    {

                        if(JsonObjectResponse.isSuccessful())
                        {
                            if(JsonObjectResponse.body()!=null)
                            {
                                if(!JsonObjectResponse.body().isJsonNull())
                                {

                                    int isSuccess = 0;
                                    if (JsonObjectResponse.body().has("success")) isSuccess = JsonObjectResponse.body().get("success").getAsInt();

                                    if (isSuccess==1)
                                    {
                                        if (JsonObjectResponse.body().has("data"))
                                        {
                                            if (JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                modelArrayList.clear();
                                                for(int i=0;i<jsonArray.size();i++)
                                                {
                                                    setJson(jsonArray,i);
                                                }


                                            }
                                        }
                                    }

                                }

                            }

                        }
                        else
                        {
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
                                    showErrorLog(getString(R.string.unknown_error_try_again) + " "+JsonObjectResponse.code());
                                    break;
                            }
                        }
                    }
                });

    }

    private void setJson(JsonArray jsonArray, int i)
    {
        JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();
        SiteVisitsModel model =new SiteVisitsModel();

        if (jsonObject.has("event_participant_id")) model.setProject_id(!jsonObject.get("event_participant_id").isJsonNull() ? jsonObject.get("event_participant_id").getAsInt() : 0 );
        if (jsonObject.has("event_title")) model.setCustomer_name(!jsonObject.get("event_title").isJsonNull() ? jsonObject.get("event_title").getAsString() : "" );
        if (jsonObject.has("event_venue")) model.setCustomer_mobile(!jsonObject.get("event_venue").isJsonNull() ? jsonObject.get("event_venue").getAsString() : "");
        if (jsonObject.has("event_banner_path")) model.setCustomer_email(!jsonObject.get("event_banner_path").isJsonNull() ? jsonObject.get("event_banner_path").getAsString() : null );
        if (jsonObject.has("registerd_on")) model.setFlat_type(!jsonObject.get("registerd_on").isJsonNull() ? jsonObject.get("registerd_on").getAsString() : "");
        modelArrayList.add(model);
    }

    private void delayRefresh()
    {
        if (getActivity()!=null)
        {

            getActivity().runOnUiThread(() ->
            {
                swipeRefresh.setRefreshing(false);
                LinearLayoutManager manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setHasFixedSize(true);
                recyclerAdapter = new AllSiteVisitsRecyclerAdapter(getActivity(), modelArrayList);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

                int count = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                if (count==0)
                {

                    recyclerView.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                }
                else
                {
                    recyclerView.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showErrorLog(final String message)
    {
        if (getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {

                swipeRefresh.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                ll_noData.setVisibility(View.VISIBLE);
                Helper.onErrorSnack(Objects.requireNonNull(getActivity()),message);
            });

        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {

        menu.clear();
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.menu_blank, menu);
    }
}
