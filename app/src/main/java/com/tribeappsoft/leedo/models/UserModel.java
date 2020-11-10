package com.tribeappsoft.leedo.models;

import com.tribeappsoft.leedo.admin.project_creation.model.ProjectModel;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 12/9/19.
 */
public class UserModel implements Serializable
{

    public UserModel() { }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApi_token() {
        return api_token;
    }

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

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getCompany_name_short() {
        return company_name_short;
    }

    public void setCompany_name_short(String company_name_short) {
        this.company_name_short = company_name_short;
    }


    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }


    public ArrayList<UserRolesModel> getRolesModelArrayList() {
        return rolesModelArrayList;
    }

    public void setRolesModelArrayList(ArrayList<UserRolesModel> rolesModelArrayList) {
        this.rolesModelArrayList = rolesModelArrayList;
    }

    public ArrayList<UserPermissionsModel> getPermissionsModelArrayList() {
        return permissionsModelArrayList;
    }

    public void setPermissionsModelArrayList(ArrayList<UserPermissionsModel> permissionsModelArrayList) {
        this.permissionsModelArrayList = permissionsModelArrayList;
    }

    public int getUser_type_id() {
        return user_type_id;
    }

    public void setUser_type_id(int user_type_id) {
        this.user_type_id = user_type_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public boolean isSalesTeamLead() {
        return isSalesTeamLead;
    }

    public void setSalesTeamLead(boolean salesTeamLead) {
        isSalesTeamLead = salesTeamLead;
    }
    public boolean isSalesHead() {
        return isSalesHead;
    }

    public void setSalesHead(boolean salesHead) {
        isSalesHead = salesHead;
    }

    public ArrayList<ProjectModel> getProjectsModelArrayList() {
        return projectsModelArrayList;
    }

    public void setProjectsModelArrayList(ArrayList<ProjectModel> projectsModelArrayList) {
        this.projectsModelArrayList = projectsModelArrayList;
    }


    private int user_id;

    public String getProfile_photo_media_id() {
        return profile_photo_media_id;
    }

    public void setProfile_photo_media_id(String profile_photo_media_id) {
        this.profile_photo_media_id = profile_photo_media_id;
    }

    private String profile_photo_media_id;
    private int user_type_id;  //TODO 1. -> Sales Person,   2-> Site Engineer  3-> Accounts Head
    private String username;
    private String api_token;
    private String prefix;
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
    private String company_name;
    private String company_name_short;
    private ArrayList<UserRolesModel> rolesModelArrayList;
    private ArrayList<ProjectModel> projectsModelArrayList;
    private ArrayList<UserPermissionsModel> permissionsModelArrayList;
    private boolean isAdmin;
    private boolean isSalesHead;
    private boolean isSalesTeamLead;


}
