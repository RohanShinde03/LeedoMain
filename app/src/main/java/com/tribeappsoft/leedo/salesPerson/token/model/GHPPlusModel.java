package com.tribeappsoft.leedo.salesPerson.token.model;

import com.tribeappsoft.leedo.salesPerson.models.TokensModel;

import java.util.ArrayList;

public class GHPPlusModel {
    public GHPPlusModel() {}

    private int token_id;
    private int token_type_id;
    private String token_no;
    private int event_id;
    private String event_title;
    private String default_amount;
    private String amount;
    private String token_type;
    private int lead_id;
    private String lead_uid;
    private String unit_category;
    private int project_id;
    private String project_name;
    private String country_code;
    private String mobile_number;
    private String email;
    private String full_name;

    public ArrayList<TokensModel> getTokensModelArrayList() {
        return tokensModelArrayList;
    }

    public void setTokensModelArrayList(ArrayList<TokensModel> tokensModelArrayList) {
        this.tokensModelArrayList = tokensModelArrayList;
    }

    private ArrayList<TokensModel> tokensModelArrayList;

    public int getToken_type_id() {
        return token_type_id;
    }

    public void setToken_type_id(int token_type_id) {
        this.token_type_id = token_type_id;
    }

    public int getToken_id() {
        return token_id;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    public String getToken_no() {
        return token_no;
    }

    public void setToken_no(String token_no) {
        this.token_no = token_no;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getDefault_amount() {
        return default_amount;
    }

    public void setDefault_amount(String default_amount) {
        this.default_amount = default_amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getUnit_category() {
        return unit_category;
    }

    public void setUnit_category(String unit_category) {
        this.unit_category = unit_category;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
}
