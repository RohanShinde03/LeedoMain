package com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.model;

import java.util.ArrayList;

public class GHPDetailsModel {

    private int token_id;
    private int lead_id;
    private String token_no;
    private String amount;
    private int token_type_id;
    private String token_type;
    private String event_type;
    private String event_title;
    private String project_name;
    private String lead_uid;
    private String full_name;
    private String sales_person_name;
    private String cp_name;
    private String email;
    private String mobile_number;
    private ArrayList<SiteVisitModel> site_visits;
    private GHPModel ghp_details;
    private boolean isExpand;

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public int getToken_id() {
        return token_id;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getToken_no() {
        return token_no;
    }

    public void setToken_no(String token_no) {
        this.token_no = token_no;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getToken_type_id() {
        return token_type_id;
    }

    public void setToken_type_id(int token_type_id) {
        this.token_type_id = token_type_id;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    public String getCp_name() {
        return cp_name;
    }

    public void setCp_name(String cp_name) {
        this.cp_name = cp_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public ArrayList<SiteVisitModel> getSite_visits() {
        return site_visits;
    }

    public void setSite_visits(ArrayList<SiteVisitModel> site_visits) {
        this.site_visits = site_visits;
    }

    public GHPModel getGhp_details() {
        return ghp_details;
    }

    public void setGhp_details(GHPModel ghp_details) {
        this.ghp_details = ghp_details;
    }
}
