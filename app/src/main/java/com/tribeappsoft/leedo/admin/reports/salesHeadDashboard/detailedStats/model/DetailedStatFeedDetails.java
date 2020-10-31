package com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.model;

import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;
import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;

import java.util.ArrayList;

public class DetailedStatFeedDetails {

    private String lead_uid;
    private String person_id;
    private int lead_status_id;
    private String email;
    private String mobile_number;
    private String full_name;
    private String unit_category;
    private String project_name;
    private int lead_type_id;
    private String lead_type;
    private String lead_status_name;
    private boolean isExpandedOthersView;
    private boolean isExpandedOwnView;
    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;
    private String country_code;
    private String tag_elapsed_time;
    private String tag_date;
    private String description;
    private CUIDModel cuidModel;
    private int unit_hold_release_id;
    private int unit_id;
    private String unit_name;
    private int block_id;
    private int floor_id;
    private String status_text;
    private int is_reminder;
    private int call_log_count;
    private int site_visit_count;
    private int call_schedule_count;
    private int offline_lead_synced;

    public int getIs_call_scheduled() {
        return is_call_scheduled;
    }

    public void setIs_call_scheduled(int is_call_scheduled) {
        this.is_call_scheduled = is_call_scheduled;
    }

    private int is_call_scheduled;

    public int getOffline_lead_synced() {
        return offline_lead_synced;
    }

    public void setOffline_lead_synced(int offline_lead_synced) {
        this.offline_lead_synced = offline_lead_synced;
    }


    public int getIs_reminder() {
        return is_reminder;
    }

    public void setIs_reminder(int is_reminder) {
        this.is_reminder = is_reminder;
    }

    public int getCall_log_count() {
        return call_log_count;
    }

    public void setCall_log_count(int call_log_count) {
        this.call_log_count = call_log_count;
    }

    public int getSite_visit_count() {
        return site_visit_count;
    }

    public void setSite_visit_count(int site_visit_count) {
        this.site_visit_count = site_visit_count;
    }

    public int getCall_schedule_count() {
        return call_schedule_count;
    }

    public void setCall_schedule_count(int call_schedule_count) {
        this.call_schedule_count = call_schedule_count;
    }

    public int getLead_type_id() {
        return lead_type_id;
    }

    public void setLead_type_id(int lead_type_id) {
        this.lead_type_id = lead_type_id;
    }


    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    private String sales_person_name;

    public String getStatus_text() {
        return status_text;
    }

    public void setStatus_text(String status_text) {
        this.status_text = status_text;
    }

    public int getUnit_hold_release_id() {
        return unit_hold_release_id;
    }

    public void setUnit_hold_release_id(int unit_hold_release_id) {
        this.unit_hold_release_id = unit_hold_release_id;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public int getBlock_id() {
        return block_id;
    }

    public void setBlock_id(int block_id) {
        this.block_id = block_id;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public CUIDModel getCuidModel() {
        return cuidModel;
    }

    public void setCuidModel(CUIDModel cuidModel) {
        this.cuidModel = cuidModel;
    }

    public String getTag_elapsed_time() {
        return tag_elapsed_time;
    }

    public void setTag_elapsed_time(String tag_elapsed_time) {
        this.tag_elapsed_time = tag_elapsed_time;
    }

    public String getTag_date() {
        return tag_date;
    }

    public void setTag_date(String tag_date) {
        this.tag_date = tag_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
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

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
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

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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

    public String getLead_type() {
        return lead_type;
    }

    public void setLead_type(String lead_type) {
        this.lead_type = lead_type;
    }

    public String getLead_status_name() {
        return lead_status_name;
    }

    public void setLead_status_name(String lead_status_name) {
        this.lead_status_name = lead_status_name;
    }
}
