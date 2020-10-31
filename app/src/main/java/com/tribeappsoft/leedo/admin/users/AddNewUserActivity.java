package com.tribeappsoft.leedo.admin.users;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;
import com.tribeappsoft.leedo.admin.users.model.UserRoleModel;
import com.tribeappsoft.leedo.api.ApiClient;
import com.tribeappsoft.leedo.models.leads.PersonNamePrefixModel;
import com.tribeappsoft.leedo.util.Helper;
import com.tribeappsoft.leedo.util.Validation;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tribeappsoft.leedo.util.Helper.NetworkError;
import static com.tribeappsoft.leedo.util.Helper.hideSoftKeyboard;
import static com.tribeappsoft.leedo.util.Helper.isNetworkAvailable;
import static com.tribeappsoft.leedo.util.Helper.onErrorSnack;


public class AddNewUserActivity extends AppCompatActivity {

    private String TAG="AddNewUserActivity";
    @BindView(R.id.edt_newUser_FullName) TextInputEditText edt_FullName;
    @BindView(R.id.edt_newUser_Email) TextInputEditText edt_Email;
    @BindView(R.id.til_newUser_Email) TextInputLayout til_Email;
    @BindView(R.id.til_newUser_MobileNo) TextInputLayout til_MobileNo;
    @BindView(R.id.edt_newUser_MobileNo) TextInputEditText edt_MobileNo;
    @BindView(R.id.til_newUser_password) TextInputLayout til_password;
    @BindView(R.id.til_newUser_confirmPassword) TextInputLayout til_confirmPassword;
    @BindView(R.id.acTv_newUser_mrs) AutoCompleteTextView acTv_newUser_mrs;
    @BindView(R.id.acTv_newUser_selectRoleType) AutoCompleteTextView acTv_selectRoleType;
    @BindView(R.id.edt_newUser_password) TextInputEditText edt_password;
    @BindView(R.id.edt_newUser_confirmPassword) TextInputEditText edt_confirmPassword;
    @BindView(R.id.til_newUser_change_password) TextInputLayout til_change_password;
    @BindView(R.id.edt_newUser_change_password) TextInputEditText edt_change_password;
    @BindView(R.id.til_newUser_change_confirmPassword) TextInputLayout til_change_confirmPassword;
    @BindView(R.id.edt_newUser_change_confirmPassword) TextInputEditText edt_change_confirmPassword;
    @BindView(R.id.mBtn_newUser_AddNewUser) MaterialButton mBtn_addNewUSer;
    @BindView(R.id.view_changePwd) View view_changePwd;

    @BindView(R.id.chk_select_all) CheckBox chk_select_all;
    @BindView(R.id.ll_select_all) LinearLayoutCompat ll_select_all;
    @BindView(R.id.ll_newUser_addAssignedProjects) LinearLayoutCompat ll_addAssignedProjects;
    @BindView(R.id.mTv_newUser_noAssignedProjects) MaterialTextView mTv_noAssignedProjects;


    @BindView(R.id.ll_newUser_addSalesRoles) LinearLayoutCompat ll_addSalesRoles;
    @BindView(R.id.mTv_newUser_noRoles) MaterialTextView mTv_noRoles;

    //set reminder
    @BindView(R.id.sm_addUser_changePassword) SwitchMaterial sm_changePassword;
    @BindView(R.id.ll_addUser_viewChangePasswordData) LinearLayoutCompat ll_viewScheduleCallData;
    @BindView(R.id.ll_addUser_changePassword) LinearLayoutCompat ll_addUser_changePassword;


    @BindView(R.id.ll_pbLayout) LinearLayoutCompat ll_pb;
    @BindView(R.id.tv_pbLoadingMsg) AppCompatTextView tv_loadingMsg;

    public AddNewUserActivity context;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<Integer> projectNameIdArrayList;

    private ArrayList<PersonNamePrefixModel> personNamePrefixModelArrayList;
    private ArrayList<String> namePrefixArrayList;

    private ArrayList<UserRoleModel> roleModelArrayList;
    private ArrayList<Integer> userRoleIdArrayList;
    //private UserModel model;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String api_token="",selectedNamePrefix = "",selectedRole = "",password = null,user_name="",user_email="",user_mobile="",country_code="91";
    private int selectedNamePrefixId =0, user_id=0,sales_person_id=0, person_id=0,fromOther = 1; //TODO fromOther ==> 1 - Add New User, 2- Edit/Update New User
    private boolean isChangePassword =false, isAdmin =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_user);
        context= AddNewUserActivity.this;
        ButterKnife.bind(this);

        //initialise ArrayList
        projectModelArrayList=new ArrayList<>();
        projectNameIdArrayList = new ArrayList<>();
        roleModelArrayList=new ArrayList<>();
        personNamePrefixModelArrayList=new ArrayList<>();
        namePrefixArrayList=new ArrayList<>();
        userRoleIdArrayList=new ArrayList<>();


        //initialise shared pref
        sharedPreferences = new Helper().getSharedPref(getApplicationContext());
        editor = sharedPreferences.edit();
        api_token = sharedPreferences.getString("api_token", "");
        sales_person_id = sharedPreferences.getInt("user_id", 0);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        Log.e(TAG, "onCreate: "+api_token);
        editor.apply();

        //get Intent
        if (getIntent()!=null) {
            fromOther = getIntent().getIntExtra("fromOther", 1);
            //data from update user info
            user_id = getIntent().getIntExtra("user_id", 0);
            person_id = getIntent().getIntExtra("person_id", 0);
            user_name = getIntent().getStringExtra("user_name");
            selectedNamePrefixId = getIntent().getIntExtra("user_prefix_id",0);
            selectedNamePrefix = getIntent().getStringExtra("user_prefix");
            user_email = getIntent().getStringExtra("user_email");
            user_mobile = getIntent().getStringExtra("user_mobile");
            selectedRole = getIntent().getStringExtra("user_role_type");
            /*           password = getIntent().getStringExtra("password");*/

            //
            if(fromOther==2){
                // projectNameIdArrayList=(ArrayList<Integer>) getIntent().getSerializableExtra("projectArrayList");
                userRoleIdArrayList.clear();
                userRoleIdArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("rolesArrayList")));

                projectNameIdArrayList.clear();

                projectNameIdArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("projectArrayList")));
                //model = (UserModel)getIntent().getSerializableExtra("user_model");

                // roleModelArrayList=(ArrayList<UserModel>) getIntent().getSerializableExtra("rolesArrayList");
                // cuidModel = (CUIDModel) getIntent().getSerializableExtra("cuidModel");

                //Log.e(TAG, "onCreate: model"+model );
                Log.e(TAG, "onCreate: userRoleIdArrayList"+userRoleIdArrayList.toString() );
                Log.e(TAG, "onCreate: projectNameIdArrayList"+projectNameIdArrayList.toString() );
                Log.e(TAG, "onCreate: projectNameIdArrayList"+projectNameIdArrayList.size() );
            }

        }

        if (getSupportActionBar()!=null)
        {
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.layout_ab_center);
            //((AppCompatTextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_abs_title)).setText(fromOther==2 ? getString(R.string.update_new_user): getString(R.string.add_new_user));

            getSupportActionBar().setTitle(fromOther==2 ? getString(R.string.update_new_user): getString(R.string.add_new_user));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mBtn_addNewUSer.setText(fromOther==2 ? getString(R.string.update_new_user): getString(R.string.add_new_user));
        ll_addUser_changePassword.setVisibility(fromOther==2 & isAdmin ? View.VISIBLE : View.GONE);
        view_changePwd.setVisibility(fromOther==2 & isAdmin ? View.VISIBLE : View.GONE);


        if (fromOther==2) {

            //update user details
            if (selectedNamePrefix != null && !selectedNamePrefix.trim().isEmpty()) acTv_newUser_mrs.setText(selectedNamePrefix);
            if (user_name != null && !user_name.trim().isEmpty()) edt_FullName.setText(user_name);
            if (user_email != null && !user_email.trim().isEmpty()) edt_Email.setText(user_email);
            if (user_mobile != null && !user_mobile.trim().isEmpty()) edt_MobileNo.setText(user_mobile);
            if (selectedRole != null && !selectedRole.trim().isEmpty()) acTv_selectRoleType.setText(selectedRole);

            til_password.setVisibility(View.GONE);
            edt_password.setVisibility(View.GONE);
            til_confirmPassword.setVisibility(View.GONE);
            edt_confirmPassword.setVisibility(View.GONE);

            checkButtonEnabled();
           /* if (password != null && !password.trim().isEmpty()) edt_password.setText(password);
            if (password != null && !password.trim().isEmpty()) edt_confirmPassword.setText(password);*/
        }


        hideProgressBar();
        toggleView();

        if (isNetworkAvailable(Objects.requireNonNull(context))) {
            showProgressBar("getting User Details...");
            getPrefixData();
            call_getAllProjectList();
            new Handler(getMainLooper()).postDelayed(this::getUserRoles, 100);
        }
        else { NetworkError(context);}

        checkUserDetails();

        //checkEmail
        checkLeadEmail();

        //submit NewUSer
        mBtn_addNewUSer.setOnClickListener(view -> {

            hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
            if (fromOther==2) checkUpdateValidation();
            else checkValidation();
        });

    }


    private void checkLeadEmail()
    {
        edt_Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Objects.requireNonNull(edt_Email.getText()).toString().length()>4) {
                    if (!isValidEmail(edt_Email)) {
                        til_Email.setErrorEnabled(true);
                        til_Email.setError("Please enter valid email! eg.user@gmail.com");
                        //til_email.setHelperTextEnabled(true);
                        //til_email.setHelperText("Valid email eg. abc@gmail.com");
                    }
                    else {
                        til_Email.setErrorEnabled(false);
                        til_Email.setError(null);
                        //til_email.setHelperTextEnabled(false);
                        //til_email.setHelperText(null);
                    }

                    //checkButtonEnabled
                    checkButtonEnabled();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void toggleView()
    {

        sm_changePassword.setOnCheckedChangeListener((compoundButton, b) -> {

            isChangePassword = b;
            if (b)  //checked
            {
                //do expand view
                //new Animations().toggleRotate(iv_refLead_ec, true);
                expandSubView(ll_viewScheduleCallData);
                //viewRefLead = true;
            }
            else {

                // //do collapse View
                //new Animations().toggleRotate(iv_refLead_ec, true);
                collapse(ll_viewScheduleCallData);
                //viewRefLead = false;
            }

            checkButtonEnabled();
        });


        edt_change_confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //check button EnabledView
                checkButtonEnabled();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void getPrefixData()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getAllPrefix(api_token);
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
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        //setAdapterNamePrefix();
                        setNamePrefix();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();

                                                personNamePrefixModelArrayList.clear();
                                                namePrefixArrayList.clear();

                                                for(int i=0;i<jsonArray.size();i++) {
                                                    setNamePrefixJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                            }
                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
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
                                    showErrorLog(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });
    }


    private void setNamePrefixJson(JsonObject jsonObject)
    {
        PersonNamePrefixModel myModel = new PersonNamePrefixModel();
        if (jsonObject.has("name_prefix_id")) myModel.setName_prefix_id(!jsonObject.get("name_prefix_id").isJsonNull() ? jsonObject.get("name_prefix_id").getAsInt() : 0 );
        if (jsonObject.has("name_prefix"))
        {
            myModel.setName_prefix(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
            namePrefixArrayList.add(!jsonObject.get("name_prefix").isJsonNull() ? jsonObject.get("name_prefix").getAsString() : "" );
        }
        personNamePrefixModelArrayList.add(myModel);
    }

    private void setNamePrefix()
    {
        runOnUiThread(() -> {

            hideProgressBar();

            setAdapterNamePrefix();

            //checkButtonEnabled
            checkButtonEnabled();

        });
    }

    private void setAdapterNamePrefix()
    {

        if (namePrefixArrayList.size() >0 && personNamePrefixModelArrayList.size()>0)
        {

            //ArrayList<String> stringList2 = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ary_project_name)));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, namePrefixArrayList);
            //set def selected
            //acTv_newUser_mrs.setText(namePrefixArrayList.get(0));
            // selectedNamePrefixId = personNamePrefixModelArrayList.get(0).getName_prefix_id();
            //selectedNamePrefix = personNamePrefixModelArrayList.get(0).getName_prefix();

            acTv_newUser_mrs.setAdapter(adapter);
            acTv_newUser_mrs.setThreshold(0);


            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_newUser_mrs.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (PersonNamePrefixModel pojo : personNamePrefixModelArrayList)
                {
                    if (pojo.getName_prefix().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedNamePrefixId = pojo.getName_prefix_id(); // This is the correct ID
                        selectedNamePrefix = pojo.getName_prefix();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "Name prefix & id " + selectedNamePrefix +"\t"+ selectedNamePrefixId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }


    private void getUserRoles()
    {
        ApiClient client = ApiClient.getInstance();
        Observable<Response<JsonObject>> responseObservable = client.getApiService().getUserRolesList(api_token,sales_person_id);
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
                        Log.d(TAG, "All ProjectList Getting Completed:");
                        //setNameUserRoles();
                    }

                    @Override
                    public void onError(final Throwable e)
                    {

                        Log.e(TAG, "onError: " + e.toString());
                        if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                        else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                        else showErrorLog(e.toString());
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
                                            if (!JsonObjectResponse.body().get("data").isJsonNull() && JsonObjectResponse.body().get("data").isJsonArray())
                                            {
                                                roleModelArrayList.clear();
                                                JsonArray jsonArray  = JsonObjectResponse.body().get("data").getAsJsonArray();
                                                for(int i=0;i<jsonArray.size();i++) {
                                                    setNameUserRolesJson(jsonArray.get(i).getAsJsonObject());
                                                }
                                            }
                                        }
                                    }
                                    else showErrorLog(getString(R.string.something_went_wrong_try_again));
                                }
                            }
                        }
                        else {
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
                                    showErrorLog(getString(R.string.unknown_error_try_again));
                                    break;
                            }
                        }
                    }
                });
    }//getUserRoles()

    private void setNameUserRolesJson(JsonObject jsonObject)
    {
        UserRoleModel myModel = new UserRoleModel();
        if (jsonObject.has("role_id")) myModel.setRole_id(!jsonObject.get("role_id").isJsonNull() ? jsonObject.get("role_id").getAsInt() : 0 );
        if (jsonObject.has("role_name")) myModel.setRole_name(!jsonObject.get("role_name").isJsonNull() ? jsonObject.get("role_name").getAsString() : "" );

        roleModelArrayList.add(myModel);
        setNameUserRoles();

    }//setNameUserRolesJson()


    private void setNameUserRoles()
    {
        runOnUiThread(() -> {

            hideProgressBar();
            //add checkboxes view
            if (roleModelArrayList!=null && roleModelArrayList.size()>0) {

                ll_addSalesRoles.removeAllViews();
                for (int i =0 ; i< roleModelArrayList.size(); i++) {
                    View rowView_sub = getUserRolesView(i, roleModelArrayList);
                    ll_addSalesRoles.addView(rowView_sub);
                }

                //visible blocks view
                ll_addSalesRoles.setVisibility(View.VISIBLE);

                //hide no blocks
                mTv_noRoles.setVisibility(View.GONE);
            }
            else {
                //hide block view
                ll_addSalesRoles.setVisibility(View.GONE);

                //visible no blocks
                mTv_noRoles.setVisibility(View.VISIBLE);
            }
        });


    }

    private View getUserRolesView(int i, ArrayList<UserRoleModel> userRoleModelArrayList)
    {
        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.layout_item_block_cb, null );

        final LinearLayoutCompat ll_mainList = rowView.findViewById(R.id.ll_itemBlockName);
        final MaterialCheckBox mCb_itemRolesName = rowView.findViewById(R.id.mCb_itemBlockName);
        //final MaterialTextView mTv_flats = rowView.findViewById(R.id.mTv_itemBlockName_flats);

        UserRoleModel model = userRoleModelArrayList.get(i);

        //set role name
        mCb_itemRolesName.setText(model.getRole_name());
        // mCb_itemRolesName.setEnabled(enabled);
        //check if already assigned role
        // mCb_itemRolesName.setChecked(model.getIsSelected() == 1);

        if(fromOther==2){
            for(int j=0 ; j < userRoleIdArrayList.size() ; j++)
            {
                if( userRoleModelArrayList.get(i).getRole_id()==userRoleIdArrayList.get(j))
                {
                    userRoleModelArrayList.get(i).setCheckedBox(true);
                    mCb_itemRolesName.setChecked(true);
                    break;
                }

            }
        }

        mCb_itemRolesName.setOnClickListener(v -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                mCb_itemRolesName.setChecked(false);

                //update model value
                model.setCheckedBox(false);

                //remove selected id from arrayList
                checkInsertRemoveUserRolesIds(model.getRole_id(), false);
                //check arrayList
                // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                mCb_itemRolesName.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveUserRolesIds(model.getRole_id(), true);
                //check arrayList
                // checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        ll_mainList.setOnClickListener(view -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                mCb_itemRolesName.setChecked(false);
                //update model value
                model.setCheckedBox(false);
                //remove selected id from arrayList
                checkInsertRemoveUserRolesIds(model.getRole_id(), false);
                //check arrayList
                // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                mCb_itemRolesName.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveUserRolesIds(model.getRole_id(), true);
                //check arrayList
                //  checkArrayList();
                //arrayListId.add(holder.member_id);
            }
        });


        return rowView;
    }


    private void checkInsertRemoveUserRolesIds(int userID, boolean value) {
        if (value) {
            userRoleIdArrayList.add(userID);
        }
        else {
            userRoleIdArrayList.remove(Integer.valueOf(userID));
        }
        Log.e(TAG, "checkInsertRemoveRoleIds: "+userID );
        Log.e(TAG, "userRoleIdArrayList: "+ userRoleIdArrayList.toString() );
    }

    public ArrayList<Integer> getUserRolesIdArrayList() {
        Log.e(TAG, "getUserRolesIdArrayList: "+ projectNameIdArrayList.toString() );
        return userRoleIdArrayList;
    }

    private void call_getAllProjectList()
    {
        ApiClient client = ApiClient.getInstance();
        client.getApiService().getUserWiseAllProjects(api_token, sales_person_id).enqueue(new Callback<JsonObject>()
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
                                //set json
                                setJson(response.body());

                                //delay refresh project names
                                delayRefresh();
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
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }


    private void setJson(JsonObject jsonObject)
    {
        if (jsonObject.has("data")) {
            if (!jsonObject.get("data").isJsonNull() && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArray  = jsonObject.get("data").getAsJsonArray();
                //clear list
                projectModelArrayList.clear();
                // projectStringArrayList.clear();
                for(int i=0;i<jsonArray.size();i++) {
                    setProjectJson(jsonArray.get(i).getAsJsonObject());
                }
            }
        }
    }


    private void setProjectJson(JsonObject jsonObject)
    {
        ProjectModel model = new ProjectModel();
        if (jsonObject.has("project_id")) model.setProject_id(!jsonObject.get("project_id").isJsonNull() ? jsonObject.get("project_id").getAsInt() : 0 );
        if (jsonObject.has("project_name")) model.setProject_name(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "" );
//            projectStringArrayList.add(!jsonObject.get("project_name").isJsonNull() ? jsonObject.get("project_name").getAsString() : "");
        projectModelArrayList.add(model);
    }

    private void delayRefresh() {

        runOnUiThread(() -> {

            hideProgressBar();

            chk_select_all.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked)
                {
                    if ((projectModelArrayList!=null && projectModelArrayList.size()>0)){
                        projectNameIdArrayList.clear();
                        ll_addAssignedProjects.removeAllViews();
                        for (int i =0 ; i< projectModelArrayList.size(); i++) {
                            projectModelArrayList.get(i).setCheckedBox(true);
                            projectNameIdArrayList.add(projectModelArrayList.get(i).getProject_id());
                            View rowView_sub = getAssignedProjectView(i, projectModelArrayList,true);
                            ll_addAssignedProjects.addView(rowView_sub);
                        }

                    }
                }
                else{
                    if ((projectModelArrayList!=null && projectModelArrayList.size()>0)) {
                        projectNameIdArrayList.clear();
                        ll_addAssignedProjects.removeAllViews();
                        for (int i =0 ; i< projectModelArrayList.size(); i++) {
                            projectModelArrayList.get(i).setCheckedBox(false);
                            View rowView_sub = getAssignedProjectView(i, projectModelArrayList,false);
                            ll_addAssignedProjects.addView(rowView_sub);
                        }
                    }

                }
            });

          /*  chk_select_all.setOnClickListener(v -> {

                if(model.isCheckedBox())
                {

                    if ((projectModelArrayList!=null && projectModelArrayList.size()>0)){
                        projectNameIdArrayList.clear();
                        ll_addAssignedProjects.removeAllViews();
                        for (int i =0 ; i< projectModelArrayList.size(); i++) {
                            projectModelArrayList.get(i).setCheckedBox(true);
                            projectNameIdArrayList.add(projectModelArrayList.get(i).getProject_id());
                            View rowView_sub = getAssignedProjectView(i, projectModelArrayList,true);
                            ll_addAssignedProjects.addView(rowView_sub);
                        }

                    }

                }else{

                    if ((projectModelArrayList!=null && projectModelArrayList.size()>0)) {

                        for (int i =0 ; i< projectModelArrayList.size(); i++) {
                            projectModelArrayList.get(i).setCheckedBox(false);
                            projectNameIdArrayList.clear();
                            View rowView_sub = getAssignedProjectView(i, projectModelArrayList,false);
                            ll_addAssignedProjects.addView(rowView_sub);
                        }
                    }

                }
            });*/



            //add checkboxes view
            if (projectModelArrayList!=null && projectModelArrayList.size()>0) {

                ll_addAssignedProjects.removeAllViews();
                for (int i =0 ; i< projectModelArrayList.size(); i++) {
                    View rowView_sub = getAssignedProjectView(i, projectModelArrayList, false);
                    ll_addAssignedProjects.addView(rowView_sub);
                }

                Log.e(TAG, "delayRefresh: projects available");

                //visible blocks view
                ll_select_all.setVisibility(View.VISIBLE);
                ll_addAssignedProjects.setVisibility(View.VISIBLE);

                //hide no blocks
                mTv_noAssignedProjects.setVisibility(View.GONE);
            }
            else {


                //hide block view
                ll_select_all.setVisibility(View.GONE);
                ll_addAssignedProjects.setVisibility(View.GONE);

                Log.e(TAG, "delayRefresh: projects not available");

                //visible no blocks
                mTv_noAssignedProjects.setVisibility(View.VISIBLE);
            }
        });
    }

    private View getAssignedProjectView(int i, ArrayList<ProjectModel> projectModelArrayList, boolean isSelected)
    {
        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.layout_item_block_cb, null );

        final LinearLayoutCompat ll_mainList = rowView.findViewById(R.id.ll_itemBlockName);
        final MaterialCheckBox mCb_itemRolesName = rowView.findViewById(R.id.mCb_itemBlockName);
        //final MaterialTextView mTv_flats = rowView.findViewById(R.id.mTv_itemBlockName_flats);

        ProjectModel model = projectModelArrayList.get(i);

        //set role name
        mCb_itemRolesName.setText(model.getProject_name());
        //mCb_itemRolesName.setEnabled(isSelected);
        //check if already assigned role
        mCb_itemRolesName.setChecked(isSelected);


        if(fromOther==2){
            for(int j=0 ; j < projectNameIdArrayList.size() ; j++)
            {
                if( projectModelArrayList.get(i).getProject_id()==projectNameIdArrayList.get(j))
                {
                    projectModelArrayList.get(i).setCheckedBox(true);
                    mCb_itemRolesName.setChecked(true);
                    break;
                }

            }
        }

        mCb_itemRolesName.setOnClickListener(v -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                mCb_itemRolesName.setChecked(false);

                //update model value
                model.setCheckedBox(false);

                //remove selected id from arrayList
                checkInsertRemoveAssignedProjectIds(model.getProject_id(), false);
                //check arrayList
                // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                mCb_itemRolesName.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveAssignedProjectIds(model.getProject_id(), true);
                //check arrayList
                // checkArrayList();
                //arrayListId.add(holder.member_id);
            }

        });


        ll_mainList.setOnClickListener(view -> {

            if(model.isCheckedBox())
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                mCb_itemRolesName.setChecked(false);
                //update model value
                model.setCheckedBox(false);
                //remove selected id from arrayList
                checkInsertRemoveAssignedProjectIds(model.getProject_id(), false);
                //check arrayList
                // checkArrayList();
                // arrayListId.remove(arrayListId.lastIndexOf(holder.member_id));
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
                mCb_itemRolesName.setChecked(true);
                //update model value
                model.setCheckedBox(true);
                //add selected id into an arrayList
                checkInsertRemoveAssignedProjectIds(model.getProject_id(), true);
                //check arrayList
                //  checkArrayList();
                //arrayListId.add(holder.member_id);
            }
        });


        return rowView;
    }


    private void checkInsertRemoveAssignedProjectIds(int userID, boolean value) {
        if (value)
        {
            projectNameIdArrayList.add(userID);
            Log.e(TAG, "checkInsertRemoveProjectIds: "+userID );
        }
        else
        {
            projectNameIdArrayList.remove(Integer.valueOf(userID));
            Log.e(TAG, "checkInsertRemoveProjectsIds: "+userID );
        }
        Log.e(TAG, "projectNameIdArrayList: "+ projectNameIdArrayList.toString() );
    }

    public ArrayList<Integer> getUserAssignedProjectArrayList() {
        Log.e(TAG, "projectNameIdArrayList: "+ projectNameIdArrayList.toString() );
        return projectNameIdArrayList;
    }


    private void showErrorLog(final String message)
    {
        if (context != null)
        {
            runOnUiThread(() -> {

                hideProgressBar();
                // swipeRefresh.setRefreshing(false);
                onErrorSnack(context, message);
            });
        }
    }

    private void checkValidation() {

        //first name
        if (Objects.requireNonNull(edt_FullName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please User's full name!");
            // valid email
        else if (!isValidEmail(edt_Email)) new Helper().showCustomToast(context, "Please enter a valid email!");
            // mobile
        else if (Objects.requireNonNull(edt_MobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's mobile number!");
            //valid mobile
        else if (!isValidPhone(edt_MobileNo)) new Helper().showCustomToast(context, "Please enter a valid mobile number!");
            //password
        else if (Objects.requireNonNull(edt_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's  password!");
            //confirm password
        else if (Objects.requireNonNull(edt_confirmPassword.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's confirm password!");
            // verified mobile
        else if (!isSamePasswords()) new Helper().showCustomToast(context, "Passwords does not match!");

            // else  if(recyclerAdapter!=null && recyclerAdapter.getGroupIdArrayList().size()>=1) new Helper().showCustomToast(context, "Please select at least one group!");

        else
        {
            //show confirmation dialog
            showSubmitUserAlertDialog();
        }
    }

    private void checkUpdateValidation() {

        //first name
        if (Objects.requireNonNull(edt_FullName.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please User's full name!");
            // valid email
        else if (!isValidEmail(edt_Email)) new Helper().showCustomToast(context, "Please enter a valid email!");
            // mobile
        else if (Objects.requireNonNull(edt_MobileNo.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's mobile number!");
            //valid mobile
        else if (!isValidPhone(edt_MobileNo)) new Helper().showCustomToast(context, "Please enter a valid mobile number!");

            //password
        else if (isChangePassword && Objects.requireNonNull(edt_change_password.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's  password!");
            //confirm password
        else if (isChangePassword && Objects.requireNonNull(edt_change_confirmPassword.getText()).toString().trim().isEmpty()) new Helper().showCustomToast(context, "Please enter User's confirm password!");

            // verified mobile
        else if (isChangePassword && !isSameChangedPasswords()) new Helper().showCustomToast(context, "Passwords does not match!");

            // else  if(recyclerAdapter!=null && recyclerAdapter.getGroupIdArrayList().size()>=1) new Helper().showCustomToast(context, "Please select at least one group!");

        else
        {
            //show confirmation dialog
            showSubmitUserAlertDialog();
        }
    }


    private void showSubmitUserAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View alertLayout = inflater != null ? inflater.inflate(R.layout.alert_layout_material, null) : null;
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        assert alertLayout != null;
        AppCompatTextView tv_msg =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_msg);
        AppCompatTextView tv_desc =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_desc);
        AppCompatButton btn_negativeButton =  alertLayout.findViewById(R.id.btn_custom_alert_negativeButton);
        AppCompatButton btn_positiveButton =  alertLayout.findViewById(R.id.btn_custom_alert_positiveButton);
        // tv_line =  alertLayout.findViewById(R.id.textView_layout_custom_alert_dialog_line);

        tv_msg.setText(fromOther==2 ? getString(R.string.update_user_question):getString(R.string.add_new_user_question));
        tv_desc.setText(fromOther==2 ?getString(R.string.update_user_confirmation):getString(R.string.add_new_user_confirmation));
        btn_negativeButton.setText(getString(R.string.no));
        btn_positiveButton.setText(getString(R.string.yes));

        btn_positiveButton.setOnClickListener(view -> {
            alertDialog.dismiss();

            //call create project
            if (isNetworkAvailable(context))
            {
                if (fromOther==2 ) {
                    // update project details
                    showProgressBar("Updating User Details...");
                    new Handler().postDelayed(this::call_UpdateUserDetails,500);
                }
                else {

                    showProgressBar(getString(R.string.submitting_user_details));

                    new Handler().postDelayed(this::call_newUserAdd,500);
                }
            }else NetworkError(context);

        });

        btn_negativeButton.setOnClickListener(view -> alertDialog.dismiss());

        //show alert dialog
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        //set the width and height to alert dialog
        int pixel= getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams wmlp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
        wmlp.gravity =  Gravity.CENTER;
        wmlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wmlp.width = pixel-100;
        //wmlp.x = 100;   //x position
        //wmlp.y = 100;   //y position

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_alert_background));
        //alertDialog.getWindow().setLayout(pixel-10, wmlp.height );
        alertDialog.getWindow().setAttributes(wmlp);
    }

    private void call_newUserAdd()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("full_name", Objects.requireNonNull(edt_FullName.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_Email.getText()).toString());
        jsonObject.addProperty("country_code", country_code);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_MobileNo.getText()).toString());
        jsonObject.add("roles", new Gson().toJsonTree(getUserRolesIdArrayList()).getAsJsonArray());
        jsonObject.add("projects", new Gson().toJsonTree(getUserAssignedProjectArrayList()).getAsJsonArray());
        jsonObject.addProperty("password", Objects.requireNonNull(edt_password.getText()).toString());
        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().addNewUser(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        AddNewUser();
                    }
                    else if ("0".equals(success)) {
                        showErrorLog("Mobile number already registered!");
                    }
                    else { showErrorLog("Failed to add new user!"); }
                }
                else
                {
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
                            showErrorLog(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void AddNewUser()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            new Helper().showSuccessCustomToast(context, "User Added Successfully...!");
            new Handler().postDelayed(this::onBackPressed, 500);

            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isUserCreateUpdate",1);
                editor.apply();
            }

        });
    }


    private void call_UpdateUserDetails()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", user_id);
        jsonObject.addProperty("api_token", api_token);
        jsonObject.addProperty("prefix", selectedNamePrefix);
        jsonObject.addProperty("person_id", person_id);
        jsonObject.addProperty("full_name", Objects.requireNonNull(edt_FullName.getText()).toString());
        jsonObject.addProperty("email", Objects.requireNonNull(edt_Email.getText()).toString());
        jsonObject.addProperty("country_code", country_code);
        jsonObject.addProperty("mobile_number", Objects.requireNonNull(edt_MobileNo.getText()).toString());
        jsonObject.add("roles", new Gson().toJsonTree(getUserRolesIdArrayList()).getAsJsonArray());
        jsonObject.add("projects", new Gson().toJsonTree(getUserAssignedProjectArrayList()).getAsJsonArray());
        jsonObject.addProperty("password", isChangePassword ? Objects.requireNonNull(edt_change_password.getText()).toString() : password);
       /* if(isChangePassword) jsonObject.addProperty("password",Objects.requireNonNull(edt_change_password.getText()).toString());
        else jsonObject.addProperty("password", password);*/

        ApiClient client = ApiClient.getInstance();
        Call<JsonObject> call = client.getApiService().UpdateUser(jsonObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String success = response.body().get("success").toString();
                    if ("1".equals(success)) {
                        UpdateUserDetails();
                    }
                    else if ("0".equals(success)) {
                        showErrorLog("Mobile number already registered!");
                    }
                    else { showErrorLog("Failed to add new user!"); }
                }
                else
                {
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
                            showErrorLog(getString(R.string.unknown_error_try_again));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
                if (e instanceof SocketTimeoutException) showErrorLog(getString(R.string.connection_time_out));
                else if (e instanceof IOException) showErrorLog(getString(R.string.weak_connection));
                else showErrorLog(e.toString());
            }
        });
    }

    private void UpdateUserDetails()
    {
        runOnUiThread(() -> {

            //hide pb
            hideProgressBar();

            new Helper().showSuccessCustomToast(context, "User Details Updated Successfully...!");
            new Handler().postDelayed(this::onBackPressed, 500);

            if(sharedPreferences!=null) {
                editor = sharedPreferences.edit();
                editor.putInt("isUserCreateUpdate",1);
                editor.apply();
            }

        });
    }


    private void checkUserDetails() {

        edt_FullName.addTextChangedListener(new TextWatcher() {
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

        edt_MobileNo.addTextChangedListener(new TextWatcher() {
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

        edt_confirmPassword.addTextChangedListener(new TextWatcher() {
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

        edt_change_confirmPassword.addTextChangedListener(new TextWatcher() {
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


    private void checkButtonEnabled()
    {

        //first name
        if (Objects.requireNonNull(edt_FullName.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            // valid email
        else if (!isValidEmail(edt_Email)) setButtonDisabledView();
            // mobile
        else if (Objects.requireNonNull(edt_MobileNo.getText()).toString().trim().isEmpty()) setButtonDisabledView();
            //valid mobile
        else if (!isValidPhone(edt_MobileNo)) setButtonDisabledView();

        if(fromOther==2 & isChangePassword)
        {
            //password
            if (Objects.requireNonNull(edt_change_password.getText()).toString().trim().isEmpty()) setButtonDisabledView();
                //confirm password
            else if (Objects.requireNonNull(edt_change_confirmPassword.getText()).toString().trim().isEmpty()) setButtonDisabledView();
                // verified mobile
            else if (!isSameChangedPasswords()) setButtonDisabledView();

            else
            {
                //set button enabled view
                setButtonEnabledView();
            }

            // else  if(recyclerAdapter!=null && recyclerAdapter.getGroupIdArrayList().size()>=1)setButtonDisabledView();
        }
        else
        {
            //set button enabled view
            setButtonEnabledView();
        }

    }

    private boolean isSamePasswords()
    {
        final String newPasswordVal = Objects.requireNonNull(edt_password.getText()).toString();
        final String confirmPasswordVal = Objects.requireNonNull(edt_confirmPassword.getText()).toString();
        return confirmPasswordVal.equals(newPasswordVal);
    }

    private boolean isSameChangedPasswords()
    {
        final String newPasswordVal = Objects.requireNonNull(edt_change_password.getText()).toString();
        final String confirmPasswordVal = Objects.requireNonNull(edt_change_confirmPassword.getText()).toString();
        return confirmPasswordVal.equals(newPasswordVal);
    }


    private void setButtonEnabledView()
    {

        // All validations are checked
        // enable btn for submit user
        mBtn_addNewUSer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtn_addNewUSer.setTextColor(getResources().getColor(R.color.main_white));

    }

    private void setButtonDisabledView()
    {

        // All validations are not checked
        // disable btn for submit user

        mBtn_addNewUSer.setBackgroundColor(getResources().getColor(R.color.main_light_grey));
        mBtn_addNewUSer.setTextColor(getResources().getColor(R.color.main_white));
    }


    private void expandSubView(final View v)
    {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime==1) v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(a);

    }



    private void collapse(final View v)
    {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation)
            {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }


    void hideProgressBar() {
        ll_pb.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void showProgressBar(String message) {
        hideSoftKeyboard(context, getWindow().getDecorView().getRootView());
        tv_loadingMsg.setText(message);
        ll_pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private boolean isValidEmail(EditText email)
    {
        boolean ret = true;
        if (!Validation.isEmailAddress(email, true)) ret = false;
        //if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
    }

    private boolean isValidPhone(EditText phone)
    {
        boolean ret = true;
        if (!Validation.isPhoneNumber(phone, true)) ret = false;
        return ret;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.no_change, R.anim.trans_slide_down );
    }
}


/*  private void setAdapterUserRoles()
    {

        if (userRoleArrayList.size() >0 && roleModelArrayList.size()>0)
        {

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.layout_spinner_item, userRoleArrayList);
            //set def selected
          // acTv_selectRoleType.setText(userRoleArrayList.get(0));
            //selectedRoleId = roleModelArrayList.get(0).getRole_id();
            //selectedRole = roleModelArrayList.get(0).getRole_name();

            acTv_selectRoleType.setAdapter(adapter);
            acTv_selectRoleType.setThreshold(0);


            //tv_selectCustomer.setSelection(0);
            //autoComplete_firmName.setValidator(new Validator());
            //autoComplete_firmName.setOnFocusChangeListener(new FocusListener());


            acTv_selectRoleType.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
            {
                String itemName = adapter.getItem(position);

                for (UserRoleModel pojo : roleModelArrayList)
                {
                    if (pojo.getRole_name().equals(itemName))
                    {
                        //int id = pojo.getCompany_id(); // This is the correct ID
                        selectedRoleId = pojo.getRole_id(); // This is the correct ID
                        selectedRole = pojo.getRole_name();
                        //fixedEnquiryID+=2;
                        Log.e(TAG, "User Role & id " + selectedRole +"\t"+ selectedRoleId);

                        //check button EnabledView
                        checkButtonEnabled();

                        break; // No need to keep looping once you found it.
                    }
                }
            });
        }

    }//setAdapterUserRoles()*/