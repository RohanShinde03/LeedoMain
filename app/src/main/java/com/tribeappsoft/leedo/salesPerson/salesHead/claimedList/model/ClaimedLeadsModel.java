package com.tribeappsoft.leedo.salesPerson.salesHead.claimedList.model;

import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;

import java.util.ArrayList;

public class ClaimedLeadsModel {
    public ClaimedLeadsModel() {}

    private int lead_id;
    private String tagDate;
    private String unit_category;
    private String project_name;
    private String lead_uid;
    private String country_code;
    private String mobile_number;
    private String email;
    private String prefix;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String full_name;
    private int sales_person_id;
    private boolean isChecked;
    private String isFirstCall;
    private String noOfCallMade;
    private String call_Duration;
    private String avg_time;
    private int call_done;
    private int call_count;

    public String getCp_Name() {
        return cp_Name;
    }

    public void setCp_Name(String cp_Name) {
        this.cp_Name = cp_Name;
    }

    public String getCp_executive_name() {
        return cp_executive_name;
    }

    public void setCp_executive_name(String cp_executive_name) {
        this.cp_executive_name = cp_executive_name;
    }

    private String cp_Name;
    private String cp_executive_name;

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    private String sales_person_name;

    public String getTagDate() {
        return tagDate;
    }

    public void setTagDate(String tagDate) {
        this.tagDate = tagDate;
    }

    public String getAvg_time() {
        return avg_time;
    }

    public void setAvg_time(String avg_time) {
        this.avg_time = avg_time;
    }

    public int getCall_done() {
        return call_done;
    }

    public void setCall_done(int call_done) {
        this.call_done = call_done;
    }

    public int getCall_count() {
        return call_count;
    }

    public void setCall_count(int call_count) {
        this.call_count = call_count;
    }

    public boolean isExpandedOthersView() {
        return isExpandedOthersView;
    }

    public void setExpandedOthersView(boolean expandedOthersView) {
        isExpandedOthersView = expandedOthersView;
    }

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;

    public String getLead_Type() {
        return Lead_Type;
    }

    public void setLead_Type(String lead_Type) {
        Lead_Type = lead_Type;
    }

    private String Lead_Type;

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    private boolean isExpandedOwnView;
    private boolean isExpandedOthersView;

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getUnit_category() {
        return unit_category;
    }

    public void setUnit_category(String unit_category) {
        this.unit_category = unit_category;
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

    public int getSales_person_id() {
        return sales_person_id;
    }

    public void setSales_person_id(int sales_person_id) {
        this.sales_person_id = sales_person_id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getIsFirstCall() {
        return isFirstCall;
    }

    public void setIsFirstCall(String isFirstCall) {
        this.isFirstCall = isFirstCall;
    }

    public String getNoOfCallMade() {
        return noOfCallMade;
    }

    public void setNoOfCallMade(String noOfCallMade) {
        this.noOfCallMade = noOfCallMade;
    }

    public String getCall_Duration() {
        return call_Duration;
    }

    public void setCall_Duration(String call_Duration) {
        this.call_Duration = call_Duration;
    }
}
