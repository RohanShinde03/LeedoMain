package com.tribeappsoft.leedo.admin.callSchedule.model;

import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;

import java.util.ArrayList;

public class ScheduledCallsModel {
    public ScheduledCallsModel() {}

    private int call_schedule_id;
    private int prev_call_schedule_id;
    private String call_schedule_date;
    private String call_schedule_time;
    private int lead_id;
    private String lead_uid;
    private String unit_category;
    private String country_code;
    private String project_name;
    private String mobile_number;
    private String email;
    private String prefix;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String full_name;
    private String cp_name;
    private String cp_executive_name;
    private int sales_person_id;
    private String sales_person_name;
    private int lead_churn_id;
    private int churned_sales_person_id;
    private int lead_types_id;
    private String lead_types_name;
    private int lead_status_id;
    private int lead_status_name;
    private String schedule_by;
    private String scheduled_on;
    private int schedule_status_id;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    private String created_at;

    private boolean isExpandedOwnView;
    private boolean isExpandedOthersView;

    public int getCall_schedule_id() {
        return call_schedule_id;
    }

    public void setCall_schedule_id(int call_schedule_id) {
        this.call_schedule_id = call_schedule_id;
    }

    public int getPrev_call_schedule_id() {
        return prev_call_schedule_id;
    }

    public void setPrev_call_schedule_id(int prev_call_schedule_id) {
        this.prev_call_schedule_id = prev_call_schedule_id;
    }

    public String getCall_schedule_date() {
        return call_schedule_date;
    }

    public void setCall_schedule_date(String call_schedule_date) {
        this.call_schedule_date = call_schedule_date;
    }

    public String getCall_schedule_time() {
        return call_schedule_time;
    }

    public void setCall_schedule_time(String call_schedule_time) {
        this.call_schedule_time = call_schedule_time;
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

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
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

    public String getCp_name() {
        return cp_name;
    }

    public void setCp_name(String cp_name) {
        this.cp_name = cp_name;
    }

    public String getCp_executive_name() {
        return cp_executive_name;
    }

    public void setCp_executive_name(String cp_executive_name) {
        this.cp_executive_name = cp_executive_name;
    }

    public int getSales_person_id() {
        return sales_person_id;
    }

    public void setSales_person_id(int sales_person_id) {
        this.sales_person_id = sales_person_id;
    }

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    public int getLead_churn_id() {
        return lead_churn_id;
    }

    public void setLead_churn_id(int lead_churn_id) {
        this.lead_churn_id = lead_churn_id;
    }

    public int getChurned_sales_person_id() {
        return churned_sales_person_id;
    }

    public void setChurned_sales_person_id(int churned_sales_person_id) {
        this.churned_sales_person_id = churned_sales_person_id;
    }

    public int getLead_types_id() {
        return lead_types_id;
    }

    public void setLead_types_id(int lead_types_id) {
        this.lead_types_id = lead_types_id;
    }

    public String getLead_types_name() {
        return lead_types_name;
    }

    public void setLead_types_name(String lead_types_name) {
        this.lead_types_name = lead_types_name;
    }

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
    }

    public int getLead_status_name() {
        return lead_status_name;
    }

    public void setLead_status_name(int lead_status_name) {
        this.lead_status_name = lead_status_name;
    }

    public String getSchedule_by() {
        return schedule_by;
    }

    public void setSchedule_by(String schedule_by) {
        this.schedule_by = schedule_by;
    }

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    public boolean isExpandedOthersView() {
        return isExpandedOthersView;
    }

    public void setExpandedOthersView(boolean expandedOthersView) {
        isExpandedOthersView = expandedOthersView;
    }

    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;

    public ArrayList<LeadDetailsTitleModel> getCallDetailsTitleModelArrayList() {
        return callDetailsTitleModelArrayList;
    }

    public void setCallDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> callDetailsTitleModelArrayList) {
        this.callDetailsTitleModelArrayList = callDetailsTitleModelArrayList;
    }

    private ArrayList<LeadDetailsTitleModel> callDetailsTitleModelArrayList;

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    public String getScheduled_on() {
        return scheduled_on;
    }

    public void setScheduled_on(String scheduled_on) {
        this.scheduled_on = scheduled_on;
    }

    public int getSchedule_status_id() {
        return schedule_status_id;
    }

    public void setSchedule_status_id(int schedule_status_id) {
        this.schedule_status_id = schedule_status_id;
    }
}
