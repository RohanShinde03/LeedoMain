package com.tribeappsoft.leedo.admin.users.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;

import java.io.Serializable;
import java.util.ArrayList;

public class UserModel  implements Serializable, Parcelable {

    public UserModel() { }

    private int user_id;
    private int user_type_id;
    private int user_role_id;
    private int projects_assign_type_id;
    private int person_id;

    private String username;
    private String api_token;
    private String prefix;
    private String prefix_id;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String full_name;
    private String gender;
    private String dob;
    private String email;
    private String country_code;
    private String mobile_number;
    private String profile_photo;
    private String user_role;
    private String projects_assign_type;
    private String assigned_project;
    private String pwd;
    private boolean isExpand;
    private boolean isCheckedBox;
    private ProjectModel projectModel;
    private UserRoleModel userRoleModel;
    private ArrayList<ProjectModel> projectModelArrayList;
    private ArrayList<UserRoleModel> userRoleModelArrayList;
    private ArrayList<Integer> AssignedProjectArrayList;
    private ArrayList<Integer> AssignedRolesArrayList;



    protected UserModel(Parcel in) {
        user_id = in.readInt();
        user_type_id = in.readInt();
        user_role_id = in.readInt();
        projects_assign_type_id = in.readInt();
        person_id = in.readInt();
        username = in.readString();
        api_token = in.readString();
        prefix = in.readString();
        prefix_id = in.readString();
        first_name = in.readString();
        middle_name = in.readString();
        last_name = in.readString();
        full_name = in.readString();
        gender = in.readString();
        dob = in.readString();
        email = in.readString();
        country_code = in.readString();
        mobile_number = in.readString();
        pwd = in.readString();
        profile_photo = in.readString();
        user_role = in.readString();
        projects_assign_type = in.readString();
        assigned_project = in.readString();
        isExpand = in.readByte() != 0;
        isCheckedBox = in.readByte() != 0;
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public ArrayList<UserRoleModel> getUserRoleModelArrayList() {
        return userRoleModelArrayList;
    }

    public void setUserRoleModelArrayList(ArrayList<UserRoleModel> userRoleModelArrayList) {
        this.userRoleModelArrayList = userRoleModelArrayList;
    }

    public ArrayList<Integer> getAssignedRolesArrayList() {
        return AssignedRolesArrayList;
    }

    public void setAssignedRolesArrayList(ArrayList<Integer> assignedRolesArrayList) {
        AssignedRolesArrayList = assignedRolesArrayList;
    }


    public ArrayList<Integer> getAssignedProjectArrayList() {
        return AssignedProjectArrayList;
    }

    public void setAssignedProjectArrayList(ArrayList<Integer> assignedProjectArrayList) {
        AssignedProjectArrayList = assignedProjectArrayList;
    }
    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public UserRoleModel getUserRoleModel() {
        return userRoleModel;
    }

   // private ArrayList<UserRoleModel> userRoleModelArrayList;


    public void setUserRoleModel(UserRoleModel userRoleModel) {
        this.userRoleModel = userRoleModel;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }


    public int getPerson_id() {
        return person_id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }


    public ProjectModel getProjectModel() {
        return projectModel;
    }

    public void setProjectModel(ProjectModel projectModel) {
        this.projectModel = projectModel;
    }

    public ArrayList<ProjectModel> getProjectModelArrayList() {
        return projectModelArrayList;
    }

    public void setProjectModelArrayList(ArrayList<ProjectModel> projectModelArrayList) {
        this.projectModelArrayList = projectModelArrayList;
    }

    public int getProjects_assign_type_id() {
        return projects_assign_type_id;
    }

    public void setProjects_assign_type_id(int projects_assign_type_id) {
        this.projects_assign_type_id = projects_assign_type_id;
    }

    public String getProjects_assign_type() {
        return projects_assign_type;
    }

    public void setProjects_assign_type(String projects_assign_type) {
        this.projects_assign_type = projects_assign_type;
    }

    public String getPrefix_id() {
        return prefix_id;
    }

    public void setPrefix_id(String prefix_id) {
        this.prefix_id = prefix_id;
    }
    public int getUser_role_id() { return user_role_id; }

    public void setUser_role_id(int user_role_id) {
        this.user_role_id = user_role_id;
    }

    public String getUser_role() {
        return user_role;
    }

    public void setUser_role(String user_role) {
        this.user_role = user_role;
    }

    public String getAssigned_project() {
        return assigned_project;
    }

    public void setAssigned_project(String assigned_project) {
        this.assigned_project = assigned_project;
    }

    public boolean isCheckedBox() { return isCheckedBox; }

    public void setCheckedBox(boolean checkedBox) { isCheckedBox = checkedBox; }

    public int getUser_id() { return user_id; }

    public void setUser_id(int user_id) { this.user_id = user_id; }

    public int getUser_type_id() { return user_type_id; }

    public void setUser_type_id(int user_type_id) { this.user_type_id = user_type_id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getApi_token() { return api_token; }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(user_id);
        parcel.writeInt(user_type_id);
        parcel.writeInt(user_role_id);
        parcel.writeInt(projects_assign_type_id);
        parcel.writeInt(person_id);
        parcel.writeString(username);
        parcel.writeString(api_token);
        parcel.writeString(prefix);
        parcel.writeString(prefix_id);
        parcel.writeString(first_name);
        parcel.writeString(middle_name);
        parcel.writeString(last_name);
        parcel.writeString(full_name);
        parcel.writeString(gender);
        parcel.writeString(dob);
        parcel.writeString(email);
        parcel.writeString(country_code);
        parcel.writeString(mobile_number);
        parcel.writeString(profile_photo);
        parcel.writeString(user_role);
        parcel.writeString(projects_assign_type);
        parcel.writeString(assigned_project);
        parcel.writeString(pwd);
        parcel.writeByte((byte) (isExpand ? 1 : 0));
        parcel.writeByte((byte) (isCheckedBox ? 1 : 0));
    }
}
