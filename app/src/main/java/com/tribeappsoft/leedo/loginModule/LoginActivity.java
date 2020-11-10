package com.tribeappsoft.leedo.loginModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.SalesPersonHomeNavigationActivity;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.appUpdate.GooglePlayStoreAppVersionNameLoader;
import com.tribeappsoft.leedo.appUpdate.WSCallerVersionListener;
import com.tribeappsoft.leedo.models.SocialUserModel;
import com.tribeappsoft.leedo.models.UserModel;
import com.tribeappsoft.leedo.models.UserPermissionsModel;
import com.tribeappsoft.leedo.models.UserRolesModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;
import com.tribeappsoft.leedo.util.ccp.Country;
import com.tribeappsoft.leedo.util.ccp.CountryUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;

public class LoginActivity extends AppCompatActivity implements WSCallerVersionListener
{

    @BindView(R.id.cl_Login) CoordinatorLayout parent;
    @BindView(R.id.ll_ccpLogin) LinearLayoutCompat ll_ccp;
    @BindView(R.id.flag_imv) AppCompatImageView flag_imv;
    @BindView(R.id.selected_country_tv) AppCompatTextView selected_country_tv;

    @BindView(R.id.til_Login_mobNo) TextInputLayout til_mobNo;
    @BindView(R.id.til_Login_password)TextInputLayout til_password;
    @BindView(R.id.edt_Login_mobileNo) TextInputEditText edt_mobileNo;
    @BindView(R.id.edt_Login_password) TextInputEditText edt_password;

    @BindView(R.id.btn_Login) MaterialButton btn_Login;
    @BindView(R.id.btn_Login_forgotPassword) AppCompatTextView btn_forgot;


    @BindView(R.id.fb_login_button) LoginButton FB_login_button;
    @BindView(R.id.iv_studentLogin_loginFB) AppCompatImageView iv_loginFB;

    @BindView(R.id.sign_in_button) SignInButton Gmail_signIn_button;
    @BindView(R.id.iv_studentLogin_google) AppCompatImageView iv_google;

    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    private AppCompatActivity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "LoginActivity", socialID=null, countryPhoneCode = "91",android_id ="";
    //private int student_id=0, staff_id=0;


    private final int RC_SIGN_IN = 456;
    CallbackManager callbackManager;
    //List< String > permissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "public_profile");
    List< String > permissionNeeds = Arrays.asList("email",  "public_profile");
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isDiscard = false, isForceUpdate = true;
    private GoogleApiClient mGoogleApiClient;
    private int socialType = 1,AppID=0;
    private String API_TOKEN_EXTERNAL="",AppUser="";


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        context = LoginActivity.this;
        //call method to hide keyBoard
        setupUI(parent);


        if (getSupportActionBar()!=null)
        {
            ///getSupportActionBar().setTitle(s);

            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(getString(R.string.login_caps));

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_back_icon_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            //getSupportActionBar().setElevation(0); // to disable action bar elevation
        }


        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();

        //TODO temp set discardPost true
        isDiscard= true;

        //hide pb
        hideProgressBar();

        //get android id
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(TAG, "onCreate: android id "+android_id );

        //TODo select country code
        //ll_ccp.setOnClickListener(v -> startActivityForResult(new Intent(context, CountryCodeSelectActivity.class), 121));

        //initialiseFBLogin
        initialiseFBLogin();

        //Gmail Login
        initialiseGMailLogin();


        //checkEmail
        checkButtonEditTextChanged();

        //check Button Enabled View
        checkButtonEnabled();


        //call the method that navigates the UserFocus
        navigateFocus();

        //check validations and call login api
        btn_Login.setOnClickListener(v -> checkValidations());

        //goto forgot password activity
        btn_forgot.setOnClickListener(v -> startActivity(new Intent(context, ForgotPasswordActivity.class)));

        //login with fb
        iv_loginFB.setOnClickListener(v -> {

            if (isNetworkAvailable(context)) FB_login_button.performClick();
            else NetworkError(context);

        });


        //ll_loginFB.setOnClickListener(v -> FB_login_button.performClick());

        //login with Gmail
        iv_google.setOnClickListener(v ->
        {
            if (isNetworkAvailable(context))
            {
                //Gmail_signIn_button.performClick();
                if (mGoogleSignInClient!=null)
                {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }else NetworkError(context);

        });


        if (isNetworkAvailable(context))//check for app update
            new GooglePlayStoreAppVersionNameLoader(getApplicationContext(), LoginActivity.this).execute();
        else NetworkError(context);


    }

    private void checkButtonEditTextChanged() {

        edt_mobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  if (edt_mobileNo.getText().toString().length()>4) {
//                    if (!isValidPhone(edt_mobileNo)) {
//                        til_mobNo.setErrorEnabled(true);
//                        til_mobNo.setError("Please enter valid mobile number!");
//                        //til_email.setHelperTextEnabled(true);
//                        //til_email.setHelperText("Valid email eg. abc@gmail.com");
//
//                    }
//                    else {
//                        til_mobNo.setErrorEnabled(false);
//                        til_mobNo.setError(null);
//                        //til_email.setHelperTextEnabled(false);
//                        //til_email.setHelperText(null);
//                    }

                //checkButtonEnabled
                checkButtonEnabled();
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        edt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onResume() {

        super.onResume();
        //       get external token
        if (isNetworkAvailable(Objects.requireNonNull(context))) { call_getAppApiUser(false); }
        else { NetworkError(context); }
        //       get external token
        //if (isNetworkAvailable(Objects.requireNonNull(context))) { call_getAppApiUser(); }
        //else { NetworkError(context); }
    }

    @Override
    protected void onStart()
    {

        //for user signOut

        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }


    private void navigateFocus()
    {
        edt_mobileNo.setOnFocusChangeListener((v, hasFocus) -> edt_mobileNo.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edt_password, InputMethodManager.SHOW_IMPLICIT);
            }
        }));


        // call the same test cases on keyboards 'DONE' button click
        edt_password.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            {

                //test_case_and_call_to_api();
                if (isNetworkAvailable(context) )checkValidations();
                else NetworkError(context);
            }
            return false;
        });
    }

    private void call_getAppApiUser(boolean callToLoginApi)
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getAppApiUser(1).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (!response.body().isJsonNull()) {
                            int isSuccess = 0;
                            if (response.body().has("success")) isSuccess = !response.body().get("success").isJsonNull() ? response.body().get("success").getAsInt() : 0;
                            if (isSuccess == 1) {

                                if (response.body().has("data"))
                                {
                                    if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject()) {

                                        JsonObject jsonObject = response.body().get("data").getAsJsonObject();
                                        setJsonAppApiUser(jsonObject);
                                    }
                                }

                                //call to login api
                                if (callToLoginApi) {
                                    new Handler().postDelayed(() -> call_userLogin(), 500);
                                }

                                //set delayRefresh
                                //   new Handler().postDelayed(() -> delayRefresh(), 1000);
                            }
                            else showErrorLog(getString(R.string.something_went_wrong_try_again));
                        } else showErrorLog(getString(R.string.something_went_wrong_try_again));
                    }
                }
                else {

                    // error case
                    switch (response.code())
                    {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: App API USER " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void setJsonAppApiUser(JsonObject jsonObject)
    {
        if (jsonObject.has("id")) AppID=!jsonObject.get("id").isJsonNull() ?jsonObject.get("id").getAsInt() : 0;
        if (jsonObject.has("user")) AppUser=!jsonObject.get("user").isJsonNull() ? jsonObject.get("user").getAsString() : "";
        if (jsonObject.has("api_token")) API_TOKEN_EXTERNAL=!jsonObject.get("api_token").isJsonNull() ? jsonObject.get("api_token").getAsString() : "";

        if (sharedPreferences != null) {
            editor = sharedPreferences.edit();
            editor.putInt("id", AppID);
            editor.putString("AppUser", AppUser);
            editor.putString("API_TOKEN_EXTERNAL",API_TOKEN_EXTERNAL);
            editor.apply();
        }
    }


    private void initialiseFBLogin()
    {
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(this);
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo("com.vjd.salesapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        callbackManager = CallbackManager.Factory.create();
        FB_login_button.setReadPermissions(permissionNeeds);

        //handle callback
        checkFBLogin();
    }

    private void checkFBLogin()
    {
        FB_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {

                System.out.println("onSuccess");
                Log.e(TAG, "FB-LOGIN  onSuccess");
                String accessToken = loginResult.getAccessToken().getToken();
                Log.e(TAG, "Fb_accessToken "+accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        (object, response) -> {

                            Log.e(TAG, "FB_resp "+response.toString());
                            try
                            {
                                //String id = object.getString("id");
                                socialID = object.getString("id");
                                Log.e(TAG, "onSuccess: fb social id  " + socialID );

                                if (isNetworkAvailable(context))
                                {
                                    socialType = 3;
                                    call_checkSocialId(socialID, true, object, 3);
                                }
                                else NetworkError(context);


                                //set SocialUserModel
                                //setFBUserModel(object, profile_pic);
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

                //login
                //onSuccessLogin();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
                Log.e(TAG, "FB-LOGIN  onCancel");
            }

            @Override
            public void onError(FacebookException exception)
            {
                exception.printStackTrace();
                System.out.println("onError");
                new Helper().showCustomToast(context, "Failed to login! Try Again.");
                //Log.v("LoginActivity", exception.getCause().toString());
                //Log.e(TAG, "FB-LOGIN  onError" +exception.getCause().toString());
            }
        });

    }


    private void initialiseGMailLogin()
    {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try
        {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Log.e(TAG, "GMail Login resp =" + Objects.requireNonNull(account).toString());
            socialID =   account.getId();
            Log.e(TAG, "handleSignInResult: gm_id "+socialID );
            JSONObject object = new JSONObject();
            try {

                object.put("id", account.getId());
                object.put("name", account.getDisplayName());
                object.put("email", account.getEmail());
                object.put("photoPath", account.getPhotoUrl());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }


            if (isNetworkAvailable(context))
            {
                socialType = 2;
                call_checkSocialId(socialID, false, object, 2);
            }
            else NetworkError(context);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
            Log.e(TAG, "handleSignInResult: " + e.getStatusMessage());
            showErrorLog("SignIn Failed. Please try again with Registering Now!");
            //updateUI(null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        if (callbackManager!=null) callbackManager.onActivityResult(requestCode, responseCode, data);

        if (requestCode == 121  && responseCode  == RESULT_OK)
        {
            Country country = (Country) data.getSerializableExtra("result");
            if (country != null) {
                Log.e(TAG, "onActivityResult: "+country.getName() +" \n"+country.getPhoneCode() );
                countryPhoneCode = country.getPhoneCode();
                String iso = country.getIso().toUpperCase();
                flag_imv.setImageResource(CountryUtils.getFlagDrawableResId(country));
                selected_country_tv.setText(getString(R.string.country_code_and_phone_code, iso, countryPhoneCode));
                Log.e(TAG, "onActivityResult: countryCode "+countryPhoneCode );
                //ccp.setCountryForNameCode(country.getName());
                new Helper().showCustomToast(context, "selected "+ country.getName() );
            }
            //set country
            //ccp.setSelectedCountry(country);
        }
        else if (requestCode == 121 && responseCode == RESULT_CANCELED)
        {
            new Helper().showCustomToast(context, "You cancelled!");
        }

    }



    private void call_checkSocialId(final String socialID, final boolean isFBLogin, final JSONObject object, int socialType)
    {

        ApiClient client = ApiClient.getInstance();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("username", countryPhoneCode + Objects.requireNonNull(edt_mobileNo.getText()).toString());
        jsonObject.addProperty("password", Objects.requireNonNull(edt_password.getText()).toString());
        jsonObject.addProperty("login_provider_id", 1);
        jsonObject.addProperty("api_token", API_TOKEN_EXTERNAL);

        //if (isFBLogin) jsonObject.addProperty("provider_id", 2);
        //else jsonObject.addProperty("provider_id", 3);
        jsonObject.addProperty("provider_id", 2);
        jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("id", socialID);

        client.getApiService().salesLogin(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1)
                        {
                            //already registered
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull())
                                {
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setJson(data, socialType);
                                    onSuccessLogin();
                                }
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");
                        }
                        else
                        {
                            // not yet registered -- do registration

                            if (isFBLogin) setFBUserModel(object, socialID);
                            else setGMailUserModel(object, socialID);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }


            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void setFBUserModel(JSONObject jsonObject, String socialID)
    {
        if (jsonObject!=null)
        {
            SocialUserModel userModel = new SocialUserModel();

            try
            {
                Log.e(TAG, "FB_jsonObject "+jsonObject.toString());

              /* name = jsonObject.getString("name");
                email = jsonObject.getString("email");
                gender = jsonObject.getString("gender");
                birthday = jsonObject.getString("birthday");*/
                if (jsonObject.has("id")) userModel.setFacebookID(jsonObject.get("id")!=null ? jsonObject.getString("id") : "" );
                if (jsonObject.has("name")) userModel.setFirstName(jsonObject.get("name")!=null ? jsonObject.getString("name") : "" );
                if (jsonObject.has("last_name")) userModel.setLastName(jsonObject.get("last_name")!=null ? jsonObject.getString("last_name") : "" );
                if (jsonObject.has("mobile")) userModel.setMobile(jsonObject.get("mobile")!=null ? jsonObject.getString("mobile") : "" );
                if (jsonObject.has("email")) userModel.setEmail(jsonObject.get("email")!=null ? jsonObject.getString("email") : "" );
                if (jsonObject.has("fullName")) userModel.setFullName(jsonObject.get("name")!=null ? jsonObject.getString("name"): "" );

                URL profile_pic=  null;
                try
                {
                    profile_pic = new URL("http://graph.facebook.com/" + socialID + "/picture?type=large");
                    Log.e("profile_pic", profile_pic + "");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                userModel.setPhotoPath(profile_pic !=null  ? profile_pic.toString() : "" );

                //check mobile number and confirm social login
                checkConfirmSocialLogin(userModel, 3, socialID);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

    }

    private void setGMailUserModel(JSONObject jsonObject, String socialID)
    {
        if (jsonObject!=null)
        {
            SocialUserModel userModel = new SocialUserModel();

            try
            {
                Log.e(TAG, "GMail_jsonObject "+jsonObject.toString());

                if (jsonObject.has("id")) userModel.setGmailID(jsonObject.get("id")!=null ? jsonObject.getString("id") : "" );
                if (jsonObject.has("name")) userModel.setFirstName(jsonObject.get("name")!=null ? jsonObject.getString("name") : "" );
                if (jsonObject.has("email")) userModel.setEmail(jsonObject.get("email")!=null ? jsonObject.getString("email") : "" );
                if (jsonObject.has("photoPath")) userModel.setPhotoPath(jsonObject.get("photoPath")!=null ? jsonObject.getString("photoPath"): "" );

                //check mobile number and confirm social login
                checkConfirmSocialLogin(userModel, 2, socialID);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }

    }


    private void checkConfirmSocialLogin(final SocialUserModel userModel, final int socialType, final String socialID)
    {

        //profile_path
        //runOnUiThread(() -> selectStudentRegisterCategoryPopup(userModel, socialType, socialID));
    }


    private void checkValidations()
    {

        Log.e(TAG, "checkValidations: API_TOKEN_EXTERNAL"+API_TOKEN_EXTERNAL);

        // mobile
        if (Objects.requireNonNull(edt_mobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Your mobile number!");
            //valid mobile
        else if (!isValidPhone(edt_mobileNo))new Helper().showCustomToast(this, "Please enter valid mobile number");
            //password
        else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter Your password!");

        else
        {
            if (isNetworkAvailable(context))
            {
                if(!API_TOKEN_EXTERNAL.trim().isEmpty() && API_TOKEN_EXTERNAL!=null){
                    showProgressBar(getString(R.string.signing_in));
                    new Handler().postDelayed(this::call_userLogin,500);
                }
                else{
                    showProgressBar(getString(R.string.signing_in));
                    call_getAppApiUser(true);
                }

            }else NetworkError(context);

        }

     /*   if (!Objects.requireNonNull(edt_mobileNo.getText()).toString().trim().isEmpty())
        {
            if (isValidPhone(edt_mobileNo))
            {
                //mobile = edt_MobileNo.getText().toString();
                if (!Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty())
                {
                    if (isNetworkAvailable(this))
                    {
                        showProgressBar(getString(R.string.signing_in));
                        call_userLogin();
                        //TODO Replace Method call
                        //onSuccessLogin();
                    }else NetworkError(this);

                } else new Helper().showCustomToast(this, "Please enter your password!");

            }
            else
            {
                til_mobNo.setErrorEnabled(true);
                til_mobNo.setError("Please enter valid mobile number");
                //new Helper().showCustomToast(this, "Please enter a valid email address");
            }

        } else new Helper().showCustomToast(this, "Please enter your mobile number!");*/
    }


    private void call_userLogin()
    {

        JsonObject jsonObject = new JsonObject();
        //jsonObject.addProperty("mobile", Objects.requireNonNull(edt_mobileNo.getText()).toString());
        //jsonObject.addProperty("country_code", countryPhoneCode);
        jsonObject.addProperty("username", countryPhoneCode + Objects.requireNonNull(edt_mobileNo.getText()).toString());
        jsonObject.addProperty("password", Objects.requireNonNull(edt_password.getText()).toString());
        //   jsonObject.addProperty("login_provider_id", 1);
        //   jsonObject.addProperty("provider_id", 0);
        jsonObject.addProperty("device_id",android_id!=null ? android_id : "");
        jsonObject.addProperty("api_token", API_TOKEN_EXTERNAL);


        ApiClient client = ApiClient.getInstance();
        client.getApiService().salesLogin(jsonObject).enqueue(new Callback<JsonObject>()
        {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response)
            {
                Log.e("response", ""+response.toString());
                if (response.isSuccessful())
                {
                    if (response.body()!=null && response.body().isJsonObject())
                    {
                        int isSuccess = 0;
                        if (response.body().has("success")) isSuccess = response.body().get("success").getAsInt();

                        if (isSuccess==1)
                        {
                            if (response.body().has("data"))
                            {
                                if (!response.body().get("data").isJsonNull() && response.body().get("data").isJsonObject() )
                                {
                                    JsonObject data  = response.body().get("data").getAsJsonObject();
                                    setJson(data, 1);
                                    onSuccessLogin();
                                }
                                else showErrorLog("Server response is empty!");

                            }else showErrorLog("Invalid response from server!");
                        }
                        else if(isSuccess==2) {
                            if (response.body().has("msg")) showErrorLog(!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : "Sorry! your account does not exist!");
                            else showErrorLog("Sorry! your account does not exist!");
                        }
                        else if(isSuccess==3) {
                            if (response.body().has("msg")) showErrorLog(!response.body().get("msg").isJsonNull() ? response.body().get("msg").getAsString() : "You are already logged into an another device!");
                            else showErrorLog("You are already logged into an another device!");
                        }
                        else {

                            String msg = null;
                            if (response.body().has("msg")) msg = response.body().get("msg").getAsString();
                            if (msg!=null) showErrorLog(msg);
                        }
                    }
                }
                else
                {
                    // error case
                    switch (response.code()) {
                        case 404:
                            showErrorLog(getString(R.string.something_went_wrong_try_again));
                            break;
                        case 500:
                            showErrorLog(getString(R.string.server_error_msg));
                            break;
                        default:
                            showErrorLog(getString(R.string.unknown_error_try_again) + " "+response.code());
                            break;
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e)
            {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void setJson(JsonObject jsonObject, int socialType)
    {

        UserModel model = new UserModel();

        if (jsonObject.has("user_id")) model.setUser_id((!jsonObject.get("user_id").isJsonNull() ? jsonObject.get("user_id").getAsInt() : 0));
        if (jsonObject.has("username")) model.setUsername((!jsonObject.get("username").isJsonNull() ? jsonObject.get("username").getAsString().trim() : ""));
        if (jsonObject.has("api_token")) model.setApi_token((!jsonObject.get("api_token").isJsonNull() ? jsonObject.get("api_token").getAsString().trim() : ""));
        if (jsonObject.has("prefix")) model.setPrefix((!jsonObject.get("prefix").isJsonNull() ? jsonObject.get("prefix").getAsString().trim() : ""));
        if (jsonObject.has("first_name")) model.setFirst_name(!jsonObject.get("first_name").isJsonNull() ? jsonObject.get("first_name").getAsString().trim() : "");
        if (jsonObject.has("middle_name")) model.setMiddle_name(!jsonObject.get("middle_name").isJsonNull() ? jsonObject.get("middle_name").getAsString().trim() : "");
        if (jsonObject.has("last_name")) model.setLast_name(!jsonObject.get("last_name").isJsonNull() ? jsonObject.get("last_name").getAsString().trim() : "");
        if (jsonObject.has("full_name")) model.setFull_name(!jsonObject.get("full_name").isJsonNull() ? jsonObject.get("full_name").getAsString().trim() : "");
        if (jsonObject.has("gender")) model.setGender(!jsonObject.get("gender").isJsonNull() ? jsonObject.get("gender").getAsString() : "");
        if (jsonObject.has("dob")) model.setDob(!jsonObject.get("dob").isJsonNull() ? jsonObject.get("dob").getAsString() : "");
        if (jsonObject.has("email")) model.setEmail(!jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "");
        if (jsonObject.has("country_code")) model.setCountry_code(!jsonObject.get("country_code").isJsonNull() ? jsonObject.get("country_code").getAsString() : "");
        if (jsonObject.has("mobile_number")) model.setMobile_number(!jsonObject.get("mobile_number").isJsonNull() ? jsonObject.get("mobile_number").getAsString() : "");
        if (jsonObject.has("profile_photo")) model.setProfile_photo((!jsonObject.get("profile_photo").isJsonNull() ? jsonObject.get("profile_photo").getAsString() : ""));
        if (jsonObject.has("profile_photo_media_id")) model.setProfile_photo_media_id((!jsonObject.get("profile_photo_media_id").isJsonNull() ? jsonObject.get("profile_photo_media_id").getAsString() : ""));
        if (jsonObject.has("company_name")) model.setCompany_name((!jsonObject.get("company_name").isJsonNull() ? jsonObject.get("company_name").getAsString() : ""));
        if (jsonObject.has("company_name_short")) model.setCompany_name_short((!jsonObject.get("company_name_short").isJsonNull() ? jsonObject.get("company_name_short").getAsString() : ""));
        model.setUser_type_id(1);   //TODO for sales person -->1

        if (jsonObject.has("user_assigned_roles"))
        {
            if (!jsonObject.get("user_assigned_roles").isJsonNull() && jsonObject.get("user_assigned_roles").isJsonArray())
            {
                ArrayList<UserRolesModel> arrayList = new ArrayList<>();
                arrayList.clear();

                JsonArray jsonArray = jsonObject.get("user_assigned_roles").getAsJsonArray();
                if (jsonArray.size()>0)
                {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setUserRolesJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                    model.setRolesModelArrayList(arrayList);

                    //check for sales head role assigned for him
                    model.setSalesHead(checkSalesTeamHeadRole(model.getRolesModelArrayList()));

                    //check for sales Admin role
                    model.setAdmin(checkSalesAdmin(model.getRolesModelArrayList()));

                    //check for sales team lead role assigned for him
                    model.setSalesTeamLead(checkSalesTeamLeadRole(model.getRolesModelArrayList()));
                }else{
                    model.setRolesModelArrayList(arrayList);
                }
            }
        }

        if (jsonObject.has("user_assigned_permissions"))
        {
            if (!jsonObject.get("user_assigned_permissions").isJsonNull() && jsonObject.get("user_assigned_permissions").isJsonArray())
            {
                JsonArray jsonArray = jsonObject.get("user_assigned_permissions").getAsJsonArray();
                ArrayList<UserPermissionsModel> arrayList = new ArrayList<>();
                arrayList.clear();
                if (jsonArray.size()>0) {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setUserPermissionJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                }
                model.setPermissionsModelArrayList(arrayList);
            }
        }

        if (jsonObject.has("user_tagged_project"))
        {
            if (!jsonObject.get("user_tagged_project").isJsonNull() && jsonObject.get("user_tagged_project").isJsonArray())
            {
                ArrayList<ProjectModel> arrayList = new ArrayList<>();
                arrayList.clear();
                JsonArray jsonArray = jsonObject.get("user_tagged_project").getAsJsonArray();
                if (jsonArray.size()>0) {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        setUserProjectJson(jsonArray.get(j).getAsJsonObject(), arrayList);
                    }
                }
                model.setProjectsModelArrayList(arrayList);
            }
        }


        updateSharedPref(model, socialType);

    }

    private void setUserRolesJson(JsonObject jsonObject, ArrayList<UserRolesModel> arrayList)
    {
        UserRolesModel model = new UserRolesModel();
        if (jsonObject.has("role_id")) model.setRole_id((!jsonObject.get("role_id").isJsonNull() ? jsonObject.get("role_id").getAsInt() : 0));
        if (jsonObject.has("role_name")) model.setRole_name((!jsonObject.get("role_name").isJsonNull() ? jsonObject.get("role_name").getAsString().trim() : ""));
        arrayList.add(model);
    }
    private void setUserProjectJson(JsonObject jsonObject, ArrayList<ProjectModel> arrayList)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id((!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0));
        if (jsonObject.has("project_name")) model.setProject_name((!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString().trim() : ""));
        arrayList.add(model);
    }

    private void setUserPermissionJson(JsonObject jsonObject, ArrayList<UserPermissionsModel> arrayList)
    {
        UserPermissionsModel model = new UserPermissionsModel();
        if (jsonObject.has("permission_id")) model.setPermission_id((!jsonObject.get("permission_id").isJsonNull() ? jsonObject.get("permission_id").getAsInt() : 0));
        if (jsonObject.has("permission_name")) model.setPermission_name((!jsonObject.get("permission_name").isJsonNull() ? jsonObject.get("permission_name").getAsString().trim() : ""));
        arrayList.add(model);
    }

    private boolean checkSalesTeamHeadRole(ArrayList<UserRolesModel> rolesModelArrayList)
    {
        //Check role_id 2 in array  for sales head
        for (UserRolesModel rolesModel: rolesModelArrayList)
        {
            if(rolesModel.getRole_id()==2)
                return true;
        }
        return false;
    }

    private boolean checkSalesAdmin(ArrayList<UserRolesModel> rolesModelArrayList) {

        //Check role_id 1 in array for sales Admin
        for (UserRolesModel rolesModel: rolesModelArrayList) {
            if(rolesModel.getRole_id()==1)
                return true;
        }
        return false;
    }


    private boolean checkSalesTeamLeadRole(ArrayList<UserRolesModel> rolesModelArrayList)
    {
        //Check role_id 10 in array  for sales team lead
        for (UserRolesModel rolesModel: rolesModelArrayList)
        {
            if(rolesModel.getRole_id()==10)
                return true;
        }
        return false;
    }

    private void updateSharedPref(UserModel model, int socialType)
    {

        if (sharedPreferences != null)
        {
            editor = sharedPreferences.edit();
            editor.putInt("provider_id", 0);
            editor.putInt("user_id", model.getUser_id());
            editor.putInt("user_type_id", model.getUser_type_id());
            editor.putInt("socialType", socialType);
            editor.putString("username", model.getUsername());
            editor.putString("api_token", model.getApi_token());
            editor.putString("prefix", model.getPrefix());
            editor.putString("first_name", model.getFirst_name());
            editor.putString("middle_name", model.getMiddle_name());
            editor.putString("last_name", model.getLast_name());
            editor.putString("full_name", model.getFull_name());
            editor.putString("gender", model.getGender());
            editor.putString("dob", model.getDob());
            editor.putString("email", model.getEmail());
            editor.putString("country_code", model.getCountry_code());
            editor.putString("mobile_number", model.getMobile_number());
            editor.putString("profile_photo", model.getProfile_photo());
            editor.putString("profile_photo_media_id", model.getProfile_photo_media_id());
            editor.putString("company_name", model.getCompany_name());
            editor.putString("company_name_short", model.getCompany_name_short());
            editor.putBoolean("isAdmin", model.isAdmin());
            editor.putBoolean("isSalesHead", model.isSalesHead());
            editor.putBoolean("isSalesTeamLead", model.isSalesTeamLead());


            if(model.getRolesModelArrayList().size()>0 && model.getRolesModelArrayList()!=null){

                editor.putInt("number_of_roles",model.getRolesModelArrayList().size());
                for(int i=0; i<model.getRolesModelArrayList().size();i++){
                    editor.putString("role_"+i, model.getRolesModelArrayList().get(i).getRole_name());
                }
            }

            if(model.getProjectsModelArrayList().size()>0 && model.getProjectsModelArrayList()!=null){
                editor.putInt("number_of_projects",model.getProjectsModelArrayList().size());

                for(int i=0; i<model.getProjectsModelArrayList().size();i++){
                    editor.putString("project_"+i, model.getProjectsModelArrayList().get(i).getProject_name());
                }
            }

            editor.apply();
        }

    }


    private void onSuccessLogin() {
        runOnUiThread(() -> {

            hideProgressBar();
            new Helper().showSuccessCustomToast(this, "Login Successful..!");

            // Subscribe to the TOPIC
//             FirebaseMessaging.getInstance().subscribeToTopic("all");
            //        FirebaseMessaging.getInstance().subscribeToTopic("sales");

            //create shortcut
            new Helper().createShortCut(this, true);

            startActivity(
                    new Intent(context, SalesPersonHomeNavigationActivity.class)
                    // .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    // .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    //  .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            );

            //clearFields();
            finish();


        });
    }


    private boolean isValidPhone(EditText phone)
    {
        boolean ret = true;
        if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }


    private void checkButtonEnabled()
    {
        //project title
        if (Objects.requireNonNull(edt_mobileNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();

            //project address
        else  if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) setButtonDisabledView();

        else {
            //set button enabled view
            setButtonEnabledView();
        }
    }
    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit project
        btn_Login.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btn_Login.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit project

        btn_Login.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        btn_Login.setTextColor(getResources().getColor(R.color.main_white));
    }

    void showProgressBar(String message)
    {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void hideProgressBar()
    {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showErrorLog(final String message) {

        runOnUiThread(() ->
        {
            hideProgressBar();
            onErrorSnack(context,message);
        });

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

        //getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }

    public void onBackPressed()
    {

        if (socialType!=1)
        {
            if (isDiscardPost())
            {
                super.onBackPressed();
                //goToHomeScreen();
                //startActivity(new Intent(SubmitBidActivity.this, VendorHomeActivity.class));
                //finish();
            }
        }
        else super.onBackPressed();

    }



    private boolean isDiscardPost()
    {
        if (isDiscard)
        {
            showDiscardAlert();
            return false;
        }
        else return true;
    }


    private void showDiscardAlert()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.material_AlertDialogTheme);
        builder.setTitle("Discard Login!");
        builder.setMessage(getString(R.string.discard_reg_msg));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.discard), (dialog, which) -> {
            isDiscard = false;
            logout();
            onBackPressed();
        });
        builder.setNegativeButton(getString(R.string.keep), (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void logout()
    {
        if (socialType==2)  //Gmail
        {
            //gmail logout

            Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                /*Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>()
                        {
                            @Override
                            public void onResult(@NonNull Status status)
                            {
                                // ...
                                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                            }
                        });*/

        }
        else if (socialType==3)     //fb
        {
            //fb logout
            LoginManager.getInstance().logOut();
        }

    }


    @Override
    public void onGetResponse(boolean isUpdateAvailable) {

        Log.e("Result_APP_Update", String.valueOf(isUpdateAvailable));
        if (isUpdateAvailable) {

            runOnUiThread(this::showUpdateDialog);
        }

    }

    /**
     * Method to show update dialog
     */
    private void showUpdateDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(context), R.style.material_AlertDialogTheme);
        alertDialogBuilder.setTitle(context.getString(R.string.app_name));
        alertDialogBuilder.setMessage("App Update Available");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Update Now", (dialog, id) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            dialog.cancel();
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (isForceUpdate) {
                finish();
            }
            dialog.dismiss();
        });
        alertDialogBuilder.show();
    }

}
