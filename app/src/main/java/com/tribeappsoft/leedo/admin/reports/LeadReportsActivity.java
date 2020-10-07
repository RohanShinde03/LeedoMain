package com.tribeappsoft.leedo.admin.reports;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.salesPerson.salesHead.callLogStats.AllCallLogStats_Activity;
import com.tribeappsoft.leedo.salesPerson.salesHead.salesHeadDashboard.SalesHeadDashboard_Activity;
import com.tribeappsoft.leedo.salesPerson.teamStats.TeamStatsActivity;
import com.tribeappsoft.leedo.util.Helper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LeadReportsActivity extends AppCompatActivity {

    @BindView(R.id.report_one) MaterialButton mbtn_reportOne;
    @BindView(R.id.report_two) MaterialButton mbtn_reportTwo;
    @BindView(R.id.report_three) MaterialButton mbtn_reportThree;
    @BindView(R.id.mTv_leadReport_export) MaterialTextView mTv_leadReport_export;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leado_reports);
        ButterKnife.bind(this);
        context= LeadReportsActivity.this;

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.reports));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        mbtn_reportOne.setOnClickListener(v -> startActivity(new Intent(context, SalesHeadDashboard_Activity.class)));
        mbtn_reportTwo.setOnClickListener(v -> startActivity(new Intent(context, AllCallLogStats_Activity.class)));
        mbtn_reportThree.setOnClickListener(v -> startActivity(new Intent(context, TeamStatsActivity.class)));

        SharedPreferences sharedPreferences = new Helper().getSharedPref(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        editor.apply();
        mTv_leadReport_export.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        //to view to marquee animation
        //mTv_leadReport_export.setSelected(true);
        mTv_leadReport_export.setOnClickListener(v -> startActivity(new Intent(context, ExportReportActivity.class)));


    /*    mBtn_reportTwo.setOnClickListener(v -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(LeadReportsActivity.this, Uri.parse("https://upstox.com/open-demat-account/?utm_expid=.D0HWlQuTRcWjQzMM2f2bcQ.0&utm_referrer=https%3A%2F%2Fupstox.com%2F"));
        });

        mBtn_reportThree.setOnClickListener(v -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(LeadReportsActivity.this, Uri.parse("https://upstox.com/open-demat-account/?utm_expid=.D0HWlQuTRcWjQzMM2f2bcQ.0&utm_referrer=https%3A%2F%2Fupstox.com%2F"));
        });
*/
        /*mBtn_reportThree.setOnClickListener(v -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            setStatusBarColor(R.color.colorPrimary);
            customTabsIntent.launchUrl(LeadReportsActivity.this, Uri.parse("https://upstox.com/open-demat-account/?utm_expid=.D0HWlQuTRcWjQzMM2f2bcQ.0&utm_referrer=https%3A%2F%2Fupstox.com%2F"));
        });*/

    }

    private void setStatusBarColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, colorId));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            //on backPressed
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

}