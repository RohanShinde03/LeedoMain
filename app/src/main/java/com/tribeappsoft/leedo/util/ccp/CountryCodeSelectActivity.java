package com.tribeappsoft.leedo.util.ccp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.MyRecyclerScroll;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;


public class CountryCodeSelectActivity extends AppCompatActivity {

    //private static final String TAG = "CountryCodeSelectActivity";
    @BindView(R.id.ccp_selectCountry) CountryCodePicker mCountryCodePicker;
    @BindView(R.id.rl_countryCode) RelativeLayout parent;
    @BindView(R.id.rv_countryCode) RecyclerView recyclerView;
    @BindView(R.id.edt_searchCountry) TextInputEditText ed_colleague;

    @BindView(R.id.ll_countryCode_searchBar) LinearLayoutCompat ll_searchBar;
    @BindView(R.id.ll_countryCode_noData) LinearLayoutCompat ll_noData;
    @BindView(R.id.iv_countryCode_clearSearch) AppCompatImageView iv_clearSearch;

    private Activity context;
    private List<Country> itemArrayList, tempArrayList;
    private List<Country> mFilteredCountries;
    //private CountryCodeRecyclerAdapter recyclerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ccp_activity_country_code_select);
        context = CountryCodeSelectActivity.this;
        //setHasOptionsMenu(true);
        ButterKnife.bind(this);
        //call method to hide keyBoard
        setupUI(parent);

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.select_country));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        itemArrayList  = new ArrayList<>();
        tempArrayList  = new ArrayList<>();

        //setup recyclerView
        setupRecycleView();

        //set data
        setupData();

        //setting up our OnScrollListener
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

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


    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
        if (verticalDivider != null) {
            dividerItemDecoration.setDrawable(verticalDivider);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void setupData() {

        itemArrayList.clear();
        if (mCountryCodePicker.getTypeFace() != null) {
            Typeface typeface = mCountryCodePicker.getTypeFace();
            //mTvTitle.setTypeface(typeface);
            ed_colleague.setTypeface(typeface);
            //mTvNoResult.setTypeface(typeface);
        }

        mCountryCodePicker.refreshCustomMasterList();
        mCountryCodePicker.refreshPreferredCountries();
        itemArrayList = mCountryCodePicker.getCustomCountries(mCountryCodePicker);
        mFilteredCountries = getFilteredCountries();

        //set data to recyclerView
        delayRefresh();

        //perform Search
        perform_search();
    }

    private void delayRefresh() {

        if (context != null) {
            runOnUiThread(() ->
            {

                //ll_pb.setVisibility(View.GONE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setHasFixedSize(true);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                Drawable verticalDivider = ContextCompat.getDrawable(Objects.requireNonNull(context), R.drawable.rv_divider_line);
                if (verticalDivider != null) {
                    dividerItemDecoration.setDrawable(verticalDivider);
                }
                recyclerView.addItemDecoration(dividerItemDecoration);
                //recyclerAdapter = new CountryCodeRecyclerAdapter(context, itemArrayList, mCountryCodePicker);

                recyclerView.setAdapter(new CountryCodeRecyclerAdapter(context, mFilteredCountries, mCountryCodePicker, country -> {

                    mCountryCodePicker.setSelectedCountry(country);

                    setResult(Activity.RESULT_OK, new Intent().putExtra("result", country));
                    hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
                    onBackPressed();

                }));

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
            public void afterTextChanged(Editable arg0) {

                if (recyclerView.getAdapter() != null) {
                    String text = Objects.requireNonNull(ed_colleague.getText()).toString().toLowerCase(Locale.getDefault());
                    doFilter(text);
                    //applyQuery(text);

                    if (ed_colleague.getText().length() < 1) {
                        ed_colleague.clearFocus();
                        hideSoftKeyboard(context, Objects.requireNonNull(context).getWindow().getDecorView().getRootView());
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
        mFilteredCountries.clear();

        if (charText.length() == 0) {
            mFilteredCountries.addAll(itemArrayList);
        } else {
            for (Country _obj : itemArrayList) {
                if (_obj.getName().toLowerCase(Locale.getDefault()).contains(charText)
                        || _obj.getPhoneCode().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mFilteredCountries.add(_obj);
                }

            }
        }
        delayRefresh();
    }


    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private void applyQuery(String query) {
        ll_noData.setVisibility(View.GONE);
        query = query.toLowerCase();

        //if query started from "+" ignore it
        if (query.length() > 0 && query.charAt(0) == '+') {
            query = query.substring(1);
        }

        mFilteredCountries = getFilteredCountries(query);

        if (mFilteredCountries.size() == 0) {
            ll_noData.setVisibility(View.VISIBLE);
        }

        //mArrayAdapter.notifyDataSetChanged();
    }

    private List<Country> getFilteredCountries() {
        return getFilteredCountries("");
    }

    private List<Country> getFilteredCountries(String query) {
        if (tempArrayList == null) {
            tempArrayList = new ArrayList<>();
        } else {
            tempArrayList.clear();
        }

        List<Country> preferredCountries = mCountryCodePicker.getPreferredCountries();
        if (preferredCountries != null && preferredCountries.size() > 0) {
            for (Country country : preferredCountries) {
                if (country.isEligibleForQuery(query)) {
                    tempArrayList.add(country);
                }
            }

            if (tempArrayList.size() > 0) { //means at least one preferred country is added.
                tempArrayList.add(null); // this will add separator for preference countries.
            }
        }

        for (Country country : itemArrayList) {
            if (country.isEligibleForQuery(query)) {
                tempArrayList.add(country);
            }
        }
        return tempArrayList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED, new Intent().putExtra("result", ""));
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return false;
    }



    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
