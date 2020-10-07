package com.tribeappsoft.leedo.admin.leadreassign.model;

import java.io.Serializable;

public class SalesPersonModel implements Serializable
{

    private  int  user_member_id;
    private  int  user_id;
    private  String  full_name;
    private  String  mobile_number;
    private  String  email;
    private  String  photo_path;
    private boolean isChecked;

    public SalesPersonModel() {

    }

    public int getUser_member_id() {
        return user_member_id;
    }

    public void setUser_member_id(int user_member_id) {
        this.user_member_id = user_member_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
    }
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }



}
