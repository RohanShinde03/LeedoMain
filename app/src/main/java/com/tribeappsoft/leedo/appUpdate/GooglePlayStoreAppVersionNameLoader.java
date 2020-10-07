package com.tribeappsoft.leedo.appUpdate;



import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;


public class GooglePlayStoreAppVersionNameLoader extends AsyncTask<String, Void, String> {

    private String newVersion = "", TAG = "GooglePlayStoreAppVersionNameLoader";
    private String currentVersion = "";
    private WSCallerVersionListener mWsCallerVersionListener;
    private boolean isVersionAvailabel;
    private boolean isAvailableInPlayStore;
    private Context mContext;
    private String mStringCheckUpdate = "";

    public GooglePlayStoreAppVersionNameLoader(Context mContext, WSCallerVersionListener callback) {
        mWsCallerVersionListener = callback;
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {
        try {

            Log.e(TAG, "doInBackground: " );
            isAvailableInPlayStore = true;
            if (isNetworkAvailable(mContext)) {
                Log.e(TAG, "doInBackground: isNetworkAvailable " );

                /*mStringCheckUpdate = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContext.getPackageName())
                        .timeout(10000)
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        //.text();
                        .ownText();
                return mStringCheckUpdate;*/

                mStringCheckUpdate = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContext.getPackageName()+ "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText();
                return mStringCheckUpdate;
            }

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(TAG, "doInBackground: Exception" );
            isAvailableInPlayStore = false;
            return mStringCheckUpdate;
        } catch (Throwable e) {

            e.printStackTrace();
            Log.e(TAG, "doInBackground: Throwable" );
            isAvailableInPlayStore = false;
            return mStringCheckUpdate;
        }
        return mStringCheckUpdate;
    }

    @Override
    protected void onPostExecute(String string) {
        if (isAvailableInPlayStore == true)
        {
            newVersion = string;
            Log.e("onPostExe: new_Version", newVersion);
            checkApplicationCurrentVersion();
            if (currentVersion.equalsIgnoreCase(newVersion)) {
                isVersionAvailabel = false;
                //Toast.makeText(mContext, "App Upto Date", Toast.LENGTH_LONG).show();
            } else {
                isVersionAvailabel = true;
            }
            mWsCallerVersionListener.onGetResponse(isVersionAvailabel);
        }
        else
        {
            Log.e(TAG, "onPostExecute: old version " );
        }
      /*  else  {
            newVersion = "2.0";
            Log.e("new Version", newVersion);
            checkApplicationCurrentVersion();
            if (currentVersion.equalsIgnoreCase(newVersion)) {
                isVersionAvailabel = false;
                Toast.makeText(mContext, "App Upto Date", Toast.LENGTH_LONG).show();
            } else {
                isVersionAvailabel = true;
            }
            mWsCallerVersionListener.onGetResponse(isVersionAvailabel);
        }*/
    }

    /**
     * Method to check current app version
     */
    public void checkApplicationCurrentVersion() {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        currentVersion = packageInfo.versionName;
        Log.e("currentVersion", currentVersion);
    }

    /**
     * Method to check internet connection
     * @param context
     * @return
     */
    private boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}