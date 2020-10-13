package com.tribeappsoft.leedo.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.leads.AddNewLeadActivity;
import com.tribeappsoft.leedo.admin.reminder.AddReminderActivity;
import com.tribeappsoft.leedo.sharedPreferences.SharedPref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class Helper {

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isNetworkAvailableContext(Context activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void onErrorSnack(Activity activity, String errorMsg) {
        //Snackbar.make(activity.findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG).show();
        Snackbar snack = Snackbar.make(activity.findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.color_claim_now_red));
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snack.getView().getLayoutParams();
        params.setMargins(15, 10, 15, 30);
        snack.show();

    }

    public static void NetworkError(Activity activity) {
        //Snackbar.make(activity.findViewById(android.R.id.content), "Network is not Available", Snackbar.LENGTH_LONG).show();

        Snackbar snack = Snackbar.make(activity.findViewById(android.R.id.content), "Network not available!", Snackbar.LENGTH_LONG);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snack.getView().getLayoutParams();
        params.setMargins(15, 10, 15, 20);
        snack.show();

    }

    public void onSnackForHome(Activity activity, String errorMsg)
    {

        Snackbar snackbar = Snackbar.make(activity.getWindow().getDecorView().getRootView(), errorMsg, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setTranslationY(-(convertDpToPixel(110, activity)));
        snackbar.show();
    }

    public void onSnackForHomeLeadSync(Activity activity, String errorMsg)
    {
        Snackbar snackbar = Snackbar.make(activity.getWindow().getDecorView().getRootView(), errorMsg, Snackbar.LENGTH_LONG);
        //snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.colorcold));
        View snackBarView = snackbar.getView();
        snackBarView.setTranslationY(-(convertDpToPixel(110, activity)));
        /*TextView textView=(TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(activity, R.color.colorcold));*/
        snackbar.show();
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public SharedPreferences getSharedPref(Context context) {
        SharedPref.sharedPref_vjSales = context.getSharedPreferences("Leado_Sales", Context.MODE_PRIVATE);
        return SharedPref.sharedPref_vjSales;
    }


    public static void StausBarTransp(Activity activity) {
        Window window = activity.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black_full_transparent));
    }

    public void setStatusBarColor(Activity activity, int color)
    {
        Window window = activity.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity, color));
    }


    public static void showHideInput(Context context, boolean val, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (val) {
                assert imm != null;
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                assert imm != null;
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static String getTodaysFormattedDateTime()
    {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return formatter.format(today);
    }
    public static String getDateTime() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        return formatter.format(today);
    }

    public static String getDateTime24HrsFormat() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return formatter.format(today);
    }

    public static String simpleDateformat(String inputDate){

        //Fri Feb 15 11:03:09 GMT+05:30 2019
        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) { }
        return df_output.format(parsed);
    }
    public static String getTodaysDateString() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(today);
    }

    public static String formatDateFromString(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String getDateFromDateTimeString(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public String formatUpdateDateDate(String inputDate) {
        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String getTimeFromDateTimeString(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String formatTimeFromString(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String setDatePickerFormatDateFromString(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }


    public static String getNotificationFormatDate(String inputDate) {
        Date parsed = null; //2019-01-10 13:28:28
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy 'at' hh:mm a", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }


    public static long getLongDateFromString(String inputDate) {
        long longDate = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(inputDate);
            longDate = date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return longDate;
    }

    public static long getLongNextDateFromString(String inputDate) {
        long longDate = 0;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(inputDate);

            //next date
            final Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            longDate = calendar.getTimeInMillis();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return longDate;

    }


    public static void hideSoftKeyboard(Activity activity, View view) {
        //InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showSoftKeyboard(Activity activity, View view) {
        try {

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Date getDatefromString(String dateString) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            if (dateString.trim().isEmpty()) date = null;
            else date = format.parse(dateString);
            Log.e("ipDate", "" + dateString);
            if (date != null) {
                Log.e("opDate", "" + date.toString());
            }
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    public static String getStringFromDate() {
        String dateTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        dateTime = dateFormat.format(date);
        System.out.println("Current Date Time : " + dateTime);

        return dateTime;
    }

    public static String getStringDateFromDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        return formatter.format(date);
    }


    public static String getTodaysDateStringToDo() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        //yyyy-MMMM-dd
        return formatter.format(today);
    }


    public static String getNextDate(String curDate) {
        //final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        Date date = new Date();
        try {
            date = format.parse(curDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return format.format(calendar.getTime());

    }

    public static String getPrevDate(String curDate) {

        //yyyy-MM-dd
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        Date date = new Date();
        try {
            date = format.parse(curDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return format.format(calendar.getTime());

        /*final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date myDate = dateFormat.parse(input);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(myDate);
        cal1.add(Calendar.DAY_OF_YEAR, -1);
        Date previousDate = cal1.getTime();*/
    }


    public static String getSendFormatDateForToDo(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // //2019-01-08
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String formatEventDate(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }

    public static String formatHomeDate(String inputDate) {

        Date parsed = null;//yyyy-MMMM-dd
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }


    public static String getFormatDateForToDo(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MMMM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("EEEE-dd-MMMM-yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {}
        return df_output.format(parsed);
    }


    public static String getCurDateForToDo(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("EEEE-dd-MMMM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String getCurDayForToDo(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("EEEE-dd-MMMM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("EEEE", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }

    public static String getCurMonthForToDo(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("EEEE-dd-MMMM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("MMMM", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }

    public static String getSetFormattedDateServiceEng(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }


    public static String setDatePickerFormatDateStringReminder(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }

    public static String setDatePickerFormatDateFromReminder(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }


    public static String getFormatDateEngCal(String inputDate) {
        //yyyy-MM-dd => yyyy-MMMM-dd
        // yyyy-MM-dd => yyyy-MMMM-dd"  EEEE-dd-MMMM-yyyy

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("EEEE-dd-MMMM-yyyy", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);

    }

    public static String formatTime(String inputDate) {

        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) {
        }
        return df_output.format(parsed);
    }


    public static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
                && today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    /**
     * calculates age from birth date
     *
     * @param selectedMilli
     */
    public boolean getAge(long selectedMilli) {
        Date dateOfBirth = new Date(selectedMilli);
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob
                .get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        String str_age = age + "";
        Log.d("", getClass().getSimpleName() + ": Age in year= " + age);

        if (age <= 5) {

            //for 1st standard age
            //age less than 1st std boy's age
            return true;
        }
        //else if (age < 18) {
        //do something
        //}
        else {

            return false;
        }
    }


    //If you're OK with "1st", "2nd", "3rd" etc, here's some simple code that will correctly handle any integer:
    public static String getOrdinalStringFromDecimal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }


    public static String getINRFormatString(Double inputNumber) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(inputNumber);
    }


    public static String getINRDecimalFormatString(Double inputNumber) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        //String yourFormattedString = formatter.format(inputNumber);
        return formatter.format(inputNumber);
    }


    public static String getIndianCurrencyFormat(String amount) {
        StringBuilder stringBuilder = new StringBuilder();
        char amountArray[] = amount.toCharArray();
        int a = 0, b = 0;
        for (int i = amountArray.length - 1; i >= 0; i--) {
            if (a < 3) {
                stringBuilder.append(amountArray[i]);
                a++;
            } else if (b < 2) {
                if (b == 0) {
                    stringBuilder.append(",");
                    stringBuilder.append(amountArray[i]);
                    b++;
                } else {
                    stringBuilder.append(amountArray[i]);
                    b = 0;
                }
            }
        }
        return stringBuilder.reverse().toString();
    }


    // validating email id
    public static boolean isValidEmail(String email) {
        return !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String convertDate_to_anotherFormat(String date_string) {
        Calendar calendar = Calendar.getInstance();
        try {
            //Log.e(TAG, date_string); //2018-04-20 05:20:05
            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            // parse the date string into Date object
            Date date = srcDf.parse(date_string);

            //DateFormat destDf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
            DateFormat destDf = new SimpleDateFormat("dd MMM yyyy hh:mm aaa", Locale.US);

            // format the date into another format
            date_string = destDf.format(date);
            //System.out.println("Converted date is : " + date_string); 20 Apr 2018 5:20 PM
        } catch (ParseException e) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm aaa", Locale.US);
            date_string = sdf.format(calendar.getTime());
            e.printStackTrace();
        }

        return date_string;

    }


    //check for context null or not -- Glide
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    public static boolean isPackageExisted(Context c, String targetPackage) {
        if (c == null) return false;

        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage,
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public void createShortCut(Context activity, boolean createShortCut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = activity.getSystemService(ShortcutManager.class);
            ShortcutInfo shortcut = new ShortcutInfo.Builder(activity, "id1")
                    .setRank(1)
                    .setShortLabel("New Lead")
                    .setLongLabel("Add New Lead")
                    .setIcon(Icon.createWithResource(activity, R.drawable.ic_person_add_black_24dp_ca))
                    //.setIntent(new Intent(this, isStaff ? StaffDonateActivity.class : StudentDonateActivity.class))
                    .setIntents(
                            new Intent[]{
                                    //new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, activity, AddNewLeadActivity.class)
                                            .putExtra("fromShortcut", true)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            })
                    .build();

            ShortcutInfo shortcut_2 = new ShortcutInfo.Builder(activity, "id2")
                    .setRank(2)
                    .setShortLabel("Reminder")
                    .setLongLabel("Add New Reminder")
                    .setIcon(Icon.createWithResource(activity, R.drawable.ic_add_reminder_ca))
                    //.setIntent(new Intent(this, isStaff ? StaffDonateActivity.class : StudentDonateActivity.class))
                    .setIntents(
                            new Intent[]{
                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, activity, AddReminderActivity.class)
                                            .putExtra("fromShortcut", true)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            })
                    .build();

            if (shortcutManager != null) {
                //create shortcuts only if user is logged in otherwise remove it

                //if (createShortCut) shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
                //else  shortcutManager.removeDynamicShortcuts(Collections.singletonList(shortcut.getId()));

                if (createShortCut)
                    shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut, shortcut_2));
                else shortcutManager.removeAllDynamicShortcuts();
            }
        }
    }

    public String getRealPathFromURI(Context activity, Uri uri) {
        String path = "";
        if (activity.getContentResolver() != null) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }


    public String getRealPathFromURI_2(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = 0;
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getFileName_from_filePath(String filePath) {
        //String path=":/storage/sdcard0/DCIM/Camera/1414240995236.jpg";//it contain your path of image..im using a temp string..
        //String filename=filePath.substring(filePath.lastIndexOf("/")+1);
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public void openPhoneDialerIntent(Activity activity, String phone) {
        activity.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + phone)));
    }

    public void openPhoneDialer(Activity activity, String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        activity.startActivity(intent);
    }

    public void makePhoneCall(Activity activity, String phone) {

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            showCustomToast(activity, activity.getString(R.string.call_permissionRationale));
            return;
        }
        activity.startActivity(intent);
    }

    public void openMapsIntent(Activity activity, String location_name, String location_address)
    {
        Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+location_name+","+location_address));
        activity.startActivity(intent);
    }


    public void openCalendarIntent(Activity activity, String title, String description,  String startDate, String endDate)
    {

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title",title!=null ? title: "-- Add Event title here");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate!=null ? startDate : getTodaysDateString());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate!=null ? endDate : getTodaysDateString());
        intent.putExtra(CalendarContract.Events.ALL_DAY, false);// periodicity
        intent.putExtra(CalendarContract.Events.DESCRIPTION,description!=null ? description : "-- Add Description here");
        //intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.HAS_ALARM, true);
        activity.startActivity(intent);
    }

    public void openReminderIntent(Activity activity, String title, String description, String startDate, String endDate)
    {
        Calendar cal_startDate = Calendar.getInstance();
        Calendar cal_endDate = Calendar.getInstance();
        try {
            cal_startDate.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault()).parse(startDate));
            cal_endDate.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault()).parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title",title!=null ? title: "-- Remind me about...");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal_startDate.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal_endDate.getTimeInMillis());
        intent.putExtra(CalendarContract.Reminders.EVENT_ID, CalendarContract.Events._ID);
        intent.putExtra(CalendarContract.Reminders.ALL_DAY, true);// periodicity
        intent.putExtra(CalendarContract.Reminders.DESCRIPTION,description!=null ? description : "-- Add Description here");
        intent.putExtra(CalendarContract.Events.HAS_ALARM, true);
        //intent.setData(CalendarContract.Events.CONTENT_URI);
        activity.startActivity(intent);
    }

    public void openEmailIntent(Activity activity, String emailAddress, String subject, String bodyText, String intentTitle)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);//common intent
        intent.setData(Uri.parse("mailto:"+emailAddress)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, Uri.encode(subject)); //your subject goes here
        intent.putExtra(Intent.EXTRA_TEXT, Uri.encode(bodyText)); //your email body goes here
        activity.startActivity(Intent.createChooser(intent, intentTitle!=null ? intentTitle : "Send feedback"));
    }

    public void openEmailIntent(Activity activity, String emailAddress, String subject, String bodyText)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);//common intent
        intent.setData(Uri.parse("mailto:"+emailAddress)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT,subject); //your subject goes here
        intent.putExtra(Intent.EXTRA_TEXT, bodyText); //your email body goes here
        activity.startActivity(Intent.createChooser(intent,"Send email using"));
        // startActivity(Intent.createChooser(intent, intentTitle!=null ? intentTitle : "Send feedback"));
    }

    public void addReminderInCalendar(Activity activity, String title, String startDate, String endDate)
    {

        DateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy, h:mmaa", Locale.getDefault());
        long lnsTime=0, lneTime=0;

        Date dateObject;

        try{
            //String dob_var = "March 06, 2020, 11:10AM";

            dateObject = formatter.parse(startDate);

            lnsTime = dateObject.getTime();
            Log.e(null, Long.toString(lnsTime));

            //dob_var = "March 06, 2020, 05:10PM";

            dateObject = formatter.parse(endDate);

            lneTime = dateObject.getTime();
            Log.e(null, Long.toString(lneTime));
        }

        catch (java.text.ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("Date Parsing Error", e.toString());
        }

        ///Calendar cal_startDate = Calendar.getInstance();
        //Calendar cal_endDate = Calendar.getInstance();
        Uri EVENTS_URI = Uri.parse(getCalendarUriBase(true) + "events");
        ContentResolver cr = activity.getContentResolver();
        TimeZone timeZone = TimeZone.getDefault();

        /* * Inserting an event in calendar.*/
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, title);
        values.put(CalendarContract.Events.ALL_DAY, 0);
        // event starts at 11 minutes from now
        values.put(CalendarContract.Events.DTSTART,lnsTime);
        // ends 60 minutes from now
        values.put(CalendarContract.Events.DTEND, lneTime);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.EVENT_COLOR, Color.BLACK);
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        Uri event = cr.insert(EVENTS_URI, values);

        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(true) + "reminders");
        values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, Long.parseLong(event.getLastPathSegment()));
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, 10);
        cr.insert(REMINDERS_URI, values);
    }

    //** Returns Calendar Base URI, supports both new and old OS. //
    private String getCalendarUriBase(boolean eventUri) {
        Uri calendarURI = null;
        try {
            calendarURI = (eventUri) ? Uri.parse("content://com.android.calendar/") : Uri
                    .parse("content://com.android.calendar/calendars");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert calendarURI != null;
        return calendarURI.toString();
    }

    public static String formatReminderDateFromString(String inputDate){
//        2020-03-04 11:13:00
        Date parsed = null;
        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("MMMM dd, yyyy, h:mmaa", Locale.getDefault());
        try {
            parsed = df_input.parse(inputDate);
        } catch (ParseException ignored) { }
        return df_output.format(parsed);
    }


    public void insertCalendarEvent(Activity activity, String title,String description, String startDate, String endDate )
    {
        // get calendar
        Calendar cal_startDate = Calendar.getInstance();
        Calendar cal_endDate = Calendar.getInstance();
        try {
            cal_startDate.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault()).parse(startDate));
            cal_endDate.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault()).parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Calendar cal = Calendar.getInstance();
        Uri EVENTS_URI = Uri.parse(getCalendarUriBase(activity) + "events");
        ContentResolver cr = activity.getContentResolver();

        // event insert
        ContentValues values = new ContentValues();
        values.put("calendar_id", 1);
        values.put("title", title!=null ? title: "-- Remind me about...");
        values.put(CalendarContract.Events.ALL_DAY, false);// periodicity
        values.put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal_startDate.getTimeInMillis() + 11*60*1000); // event starts at 11 minutes from now
        values.put(CalendarContract.EXTRA_EVENT_END_TIME, cal_endDate.getTimeInMillis()); // ends 60 minutes from now
        values.put(CalendarContract.Events.DESCRIPTION, description!=null ? description : "-- Add Description here");
        values.put("visibility", 0);
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        Uri event = cr.insert(EVENTS_URI, values);

        // reminder insert
        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(activity) + "reminders");
        values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, Long.parseLong(Objects.requireNonNull(event.getLastPathSegment())));
        values.put("method", 1 );
        values.put("minutes", 10 );
        cr.insert( REMINDERS_URI, values );
    }



    private String getCalendarUriBase(Activity act) {

        String calendarUriBase = null;
        Uri calendars = Uri.parse("content://calendar/calendars");
        Cursor managedCursor = null;
        try {
            managedCursor = act.managedQuery(calendars, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (managedCursor != null) {
            calendarUriBase = "content://calendar/";
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars");
            try {
                managedCursor = act.managedQuery(calendars, null, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (managedCursor != null) {
                calendarUriBase = "content://com.android.calendar/";
            }
        }
        return calendarUriBase;
    }


    private boolean isTelephonyEnabled(Context context){

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY;
    }


    public static Bitmap getBitmapFromURL(String src)
    {
        try
        {
            //avoids android.os.NetworkOnMainThreadException
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            //Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    Bitmap drawable_from_url(String url) throws java.net.MalformedURLException, IOException
    {

        HttpURLConnection connection = (HttpURLConnection)new URL(url) .openConnection();
        connection.setRequestProperty("User-agent","Mozilla/4.0");

        connection.connect();
        InputStream input = connection.getInputStream();

        return BitmapFactory.decodeStream(input);
    }

    //get drawable from url
    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }


    public static Uri getLocalBitmapUri(Context context, Bitmap bmp) {
        Uri bmpUri = null;
        try {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            File file =  new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


 /*   public static void hideSoftKeyboard(Activity activity, View view)
    {
//        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) {
//            inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);


            try {
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }*/



    //get minutes and seconds from long
    public static String millisecondsToTime(long milliseconds)
    {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        String secondsStr = Long.toString(seconds);
        String secs;
        if (secondsStr.length() >= 2) {
            secs = secondsStr.substring(0, 2);
        } else {
            secs = "0" + secondsStr;
        }

        return minutes + ":" + secs;
    }


    public String hmsTimeFormatter(long milliSeconds)
    {

        /*return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));*/

        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }


    @SuppressLint("NewApi")
    public static Bitmap blurRenderScript(Activity activity, Bitmap smallBitmap, int radius) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
            Log.e("blurRenderScript","RGB565toARGB888="+smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.e("blurRenderScript","smallBitmap="+smallBitmap);
        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(activity.getApplicationContext());

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    public static Bitmap RGB565toARGB888(Bitmap img) {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }




    public static void adjustFontScale(Context context, Configuration configuration) {

        if (configuration.fontScale != 1)
        {
            configuration.fontScale = 1;
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            context.getResources().updateConfiguration(configuration, metrics);
        }
    }
    public  void adjustFontScale( Context context,Configuration configuration,float scale) {

        configuration.fontScale = scale;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);

    }


    public void showSimpleAlert(final Activity activity, String message,
                                String positiveButtonText, String negativeButtonText,
                                boolean isNegativeButton,  final AlertAction alertAction)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.MyCustomAnim);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater != null ? inflater.inflate(R.layout.layout_custom_alert_dialog, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog;

        alertDialog = alertDialogBuilder.create();

        AppCompatTextView tv_msg, tv_line;


        assert alertLayout != null;
        tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        MaterialButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        MaterialButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(message);
        btn_negativeButton.setText(negativeButtonText);
        btn_positiveButton.setText(positiveButtonText);

        if (isNegativeButton)
        {
            tv_line.setVisibility(View.VISIBLE);
            btn_negativeButton.setVisibility(View.VISIBLE);
        }
        else
        {
            tv_line.setVisibility(View.GONE);
            btn_negativeButton.setVisibility(View.GONE);
        }


        //call the method to handel the positive action
        alertAction.onPositiveButtonAction(btn_positiveButton, alertDialog);

        //call the method to handel the negative action
        alertAction.onNegativeButtonAction(btn_negativeButton, alertDialog);

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //set the width and height to alert dialog
        int pixel= activity.getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_alert_rectangle_white));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);

    }





    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()
                );
    }


    public void showCustomToast(Activity activity, String msg)
    {

        LayoutInflater inflater = activity.getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.content_custom_toast, activity.findViewById(R.id.llCustom));
        AppCompatTextView textView =  toastLayout.findViewById(R.id.tv_customToastText);
        textView.setText(msg);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        //toast.setGravity(Gravity.BOTTOM, 0, 10);
        // Set the Gravity to Bottom and Right
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 150);
        toast.show();
    }

    public void showCustomToastLong(Activity activity, String msg)
    {

        LayoutInflater inflater = activity.getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.content_custom_toast, activity.findViewById(R.id.llCustom));
        AppCompatTextView textView =  toastLayout.findViewById(R.id.tv_customToastText);
        textView.setText(msg);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //toast.setGravity(Gravity.BOTTOM, 0, 10);
        // Set the Gravity to Bottom and Right
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 150);
        toast.show();
    }


    public void showSuccessCustomToast(Activity activity, String msg)
    {

        LayoutInflater inflater = activity.getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.content_custom_success_toast, activity.findViewById(R.id.ll_successCustomToast));
        AppCompatTextView textView =  toastLayout.findViewById(R.id.tv_customSuccessToastText);
        textView.setText(msg);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        //toast.setGravity(Gravity.BOTTOM, 0, 10);
        // Set the Gravity to Bottom and Right
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 150);
        toast.show();
    }



    public static Date GetItemDate(final String date)
    {
        final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setCalendar(cal);

        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static long getSecondsDiff(Date earlierDate, Date laterDate) {

        if( earlierDate == null || laterDate == null ) return 0;
        return (laterDate.getTime() - earlierDate.getTime()) / 1000;
    }

    public static int minutesDiff(Date earlierDate, Date laterDate)
    {
        if( earlierDate == null || laterDate == null ) return 0;

        return (int)((laterDate.getTime()/60000) - (earlierDate.getTime()/60000));
    }

    private static int getHoursDifference (Date earlierDate, Date laterDate)
    {
        long milliToHour = 1000 * 60 * 60;
        if(earlierDate == null || laterDate == null) return 0;
        return (int) ((earlierDate.getTime() - laterDate.getTime()) / milliToHour);
    }

    public String getFileSizeKiloBytes(File file) {
        return String.format(Locale.getDefault(), "%.2f", (double) file.length() / 1024) + "  KB";
    }

}
