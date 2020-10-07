package com.tribeappsoft.leedo.util.filepicker.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.filepicker.filter.CompositeFilter;
import com.tribeappsoft.leedo.util.filepicker.filter.PatternFilter;
import com.tribeappsoft.leedo.util.filepicker.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;


/**
 * Created by Dimorinny on 24.10.15.
 */
public class FilePickerActivity extends AppCompatActivity implements DirectoryFragment.FileClickListener {
    public static final String ARG_START_PATH = "arg_start_path";
    public static final String ARG_CURRENT_PATH = "arg_current_path";

    public static final String ARG_FILTER = "arg_filter";
    public static final String ARG_CLOSEABLE = "arg_closeable";
    public static final String ARG_TITLE = "arg_title";

    public static final String STATE_START_PATH = "state_start_path";
    private static final String STATE_CURRENT_PATH = "state_current_path";

    public static final String RESULT_FILE_PATH = "result_file_path";
    private static final int HANDLE_CLICK_DELAY = 150;

    private Toolbar mToolbar;
    private String mStartPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mCurrentPath = mStartPath;
    private CharSequence mTitle;

    private Boolean mCloseable;

    private CompositeFilter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fp_activity_file_picker);
        //Toolbar toolbar =  findViewById(R.id.toolbar_filePicker);
        initArguments(savedInstanceState);
        //initViews();
        initToolbar();
        initBackStackState();
        initFragment();

        if (getSupportActionBar()!=null)
        {

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.selectDocument));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        /*//change_status_bar_color(this);
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            ((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.selectDocument));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }*/

        //setSupportActionBar(toolbar);
        //toolbar.setTitleTextColor(getResources().getColor(R.color.main_white));
        //toolbar.setTitle("Select Document");


    }

    @SuppressWarnings("unchecked")
    private void initArguments(Bundle savedInstanceState) {
        if (getIntent().hasExtra(ARG_FILTER)) {
            Serializable filter = getIntent().getSerializableExtra(ARG_FILTER);

            if (filter instanceof Pattern) {
                ArrayList<FileFilter> filters = new ArrayList<>();
                filters.add(new PatternFilter((Pattern) filter, false));
                mFilter = new CompositeFilter(filters);
            } else {
                mFilter = (CompositeFilter) filter;
            }
        }

        if (savedInstanceState != null) {
            mStartPath = savedInstanceState.getString(STATE_START_PATH);
            mCurrentPath = savedInstanceState.getString(STATE_CURRENT_PATH);
            updateTitle();
        } else {
            if (getIntent().hasExtra(ARG_START_PATH)) {
                mStartPath = getIntent().getStringExtra(ARG_START_PATH);
                Log.d("hi","hhhh=="+mStartPath);
                mCurrentPath = mStartPath;
            }

            if (getIntent().hasExtra(ARG_CURRENT_PATH)) {
                String currentPath = getIntent().getStringExtra(ARG_CURRENT_PATH);

                if (currentPath.startsWith(mStartPath)) {
                    mCurrentPath = currentPath;
                    Log.d("hi","hhhh1=="+mCurrentPath);
                }
            }
        }

        if (getIntent().hasExtra(ARG_TITLE)) {
            mTitle = getIntent().getCharSequenceExtra(ARG_TITLE);
        }

        if (getIntent().hasExtra(ARG_CLOSEABLE)) {
            mCloseable = getIntent().getBooleanExtra(ARG_CLOSEABLE, true);
        }
    }

    private void initToolbar() {
        //setSupportActionBar(mToolbar);

        // Show back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Truncate start of path
        try {
            Field f;
            if (TextUtils.isEmpty(mTitle)) {
                f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            } else {
                f = mToolbar.getClass().getDeclaredField("mSubtitleTextView");
            }

            f.setAccessible(true);
            TextView textView = (TextView) f.get(mToolbar);
            textView.setEllipsize(TextUtils.TruncateAt.START);
        } catch (Exception ignored) {
        }

        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }
        updateTitle();
    }

    private void initViews() {
        mToolbar = findViewById(R.id.toolbar_filePicker);
    }

    private void initFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, DirectoryFragment.getInstance(
                        mCurrentPath, mFilter))
                .addToBackStack(null)
                .commit();
    }

    private void initBackStackState() {
        String pathToAdd = mCurrentPath;
        ArrayList<String> separatedPaths = new ArrayList<>();

        while (!pathToAdd.equals(mStartPath)) {
            pathToAdd = FileUtils.cutLastSegmentOfPath(pathToAdd);
            separatedPaths.add(pathToAdd);
        }

        Collections.reverse(separatedPaths);

        for (String path : separatedPaths) {
            addFragmentToBackStack(path);
        }
    }

    private void updateTitle() {
        if (getSupportActionBar() != null) {

            if(mCurrentPath.equals(mStartPath))
            {
                getSupportActionBar().setTitle("Storage");
            }
            else
            {
                String titlePath = mCurrentPath.isEmpty() ? "/" : mCurrentPath;
                int n = mCurrentPath.lastIndexOf('/');
                String val=mCurrentPath.substring(n+1);
                if (TextUtils.isEmpty(mTitle)) {
                    // getSupportActionBar().setTitle(titlePath);
                    getSupportActionBar().setTitle(val);
                } else {
                    //getSupportActionBar().setSubtitle(titlePath);
                    getSupportActionBar().setSubtitle(val);
                }
            }


        }
    }

    private void addFragmentToBackStack(String path) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, DirectoryFragment.getInstance(
                        path, mFilter))
                .addToBackStack(null)
                .commit();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else  {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();

        if (!mCurrentPath.equals(mStartPath)) {
            fm.popBackStack();
            mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
            updateTitle();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_PATH, mCurrentPath);
        outState.putString(STATE_START_PATH, mStartPath);
    }

    @Override
    public void onFileClicked(final File clickedFile) {
        new Handler().postDelayed(() -> handleFileClicked(clickedFile), HANDLE_CLICK_DELAY);
    }

    private void handleFileClicked(final File clickedFile) {
        if (clickedFile.isDirectory()) {
            mCurrentPath = clickedFile.getPath();
            // If the user wanna go to the emulated directory, he will be taken to the
            // corresponding user emulated folder.
            if (mCurrentPath.equals(""))
                mCurrentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            addFragmentToBackStack(mCurrentPath);
            updateTitle();
        } else {
            setResultAndFinish(clickedFile.getPath());
        }
    }

    private void setResultAndFinish(String filePath) {
        Intent data = new Intent();
        data.putExtra(RESULT_FILE_PATH, filePath);
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



}
