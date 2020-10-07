package com.tribeappsoft.leedo.siteEngineer.homeFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.appUpdate.GooglePlayStoreAppVersionNameLoader;
import com.tribeappsoft.leedo.appUpdate.WSCallerVersionListener;
import com.tribeappsoft.leedo.admin.notifications.NotificationsActivity;
import com.tribeappsoft.leedo.siteEngineer.SiteEngineerHomeNavigationActivity;
import com.tribeappsoft.leedo.util.Helper;

import java.util.Objects;

import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;


/*
 * Created by ${ROHAN} on 14/5/19.
 */
public class FragmentSiteEngineerHome extends Fragment implements WSCallerVersionListener {



    private Context context;
    private boolean doubleBackToExitPressedOnce = false, isForceUpdate = true;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static int NotificationCount =0;
    private Menu MyMenu = null;


    public FragmentSiteEngineerHome() {

        // Required empty public constructor
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        //setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
        //Objects.requireNonNull(getActivity()).setTitle(R.string.menu_home);

        //initialise sharedPref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        Toolbar toolbar = ((SiteEngineerHomeNavigationActivity) Objects.requireNonNull(getActivity())).findViewById(R.id.toolbar_siteEngineer);
        AppCompatTextView tv_home_title = toolbar.findViewById(R.id.tv_siteEngineerHome_title);
        //set school name as a title
        tv_home_title.setText(getString(R.string.menu_home));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_site_engineer_home, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

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
                        else
                        {
                            doOnBackPressed();
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

        //initialise sharedPref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();


        if (Helper.isNetworkAvailable(Objects.requireNonNull(getActivity())))
            //check for app update
            new GooglePlayStoreAppVersionNameLoader(context.getApplicationContext(), this).execute();


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }


    @Override
    public void onGetResponse(boolean isUpdateAvailable) {

        Log.e("Result_APP_Update", String.valueOf(isUpdateAvailable));
        if (isUpdateAvailable) {

            if (getActivity()!=null) getActivity().runOnUiThread(this::showUpdateDialog);
        }

    }

/*

    @Override
    public void onResume()
    {
        super.onResume();

        if (getActivity()!=null) getActivity().invalidateOptionsMenu();

        if ( sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();

            if (sharedPreferences.getInt("NotificationCount", 0) != 0) NotificationCount = sharedPreferences.getInt("NotificationCount", 0);
            else NotificationCount =0;
        }

        if (MyMenu !=null)
        {
            if (NotificationCount>0)
            {
                try {
                    MyMenu.findItem(R.id.action_notificationBadge).setVisible(true);
                    MyMenu.findItem(R.id.action_notification).setVisible(false);
                }
                catch (Exception ex){ex.printStackTrace();}
            }
            else clearIcon(MyMenu);
        }


    }
*/







    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        //NotificationCount
        menu.clear();
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.menu_site_engineer_home_navigation, menu);
        MyMenu = menu;

        if (sharedPreferences!=null)
        {
            editor = sharedPreferences.edit();
            editor.apply();
            if (sharedPreferences.getInt("NotificationCount", 0) != 0)
                NotificationCount = sharedPreferences.getInt("NotificationCount", 0);
        }

      /*  if (NotificationCount>0)
        {
            menu.findItem(R.id.action_notificationBadge).setVisible(true);
            menu.findItem(R.id.action_notification).setVisible(false);
        }*/
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId())
        {

            case (R.id.action_siteEngineer_calendar):
                break;

            case (R.id.action_siteEngineer_notification):
                gotoNotifications();
                break;

            case (R.id.action_siteEngineer_notificationBadge):
                gotoNotifications();
                break;

        }

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    private void gotoNotifications()
    {
        if (getActivity()!=null)
        {
            context.startActivity(new Intent(getActivity(), NotificationsActivity.class));
            //clearIcon(menu);
        }
    }

  /*  private void clearIcon(final Menu menu)
    {
        if (menu!=null && getActivity()!=null)
        {
            getActivity().runOnUiThread(() -> {
                try {
                    //TODO handle nullPointer exp
                    menu.findItem(R.id.action_notificationBadge).setVisible(false);
                    menu.findItem(R.id.action_notification).setVisible(true);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
    }*/

    public void doOnBackPressed()
    {
        if (doubleBackToExitPressedOnce)
        {
            if (getActivity()!=null) getActivity().onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        new Helper().showCustomToast(getActivity(),getResources().getString(R.string.app_exit_msg) );
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    /**
     * Method to show update dialog
     */
    private void showUpdateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.material_AlertDialogTheme);

        alertDialogBuilder.setTitle(getActivity().getString(R.string.app_name));
        alertDialogBuilder.setMessage("App Update Available");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Update Now", (dialog, id) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
            dialog.cancel();
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (isForceUpdate) {
                getActivity().finish();
            }
            dialog.dismiss();
        });
        alertDialogBuilder.show();
    }
}
