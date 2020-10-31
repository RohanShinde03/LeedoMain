package com.tribeappsoft.leedo.admin.leads.model;


import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.salesPerson.models.PaymentModeModel;

import java.io.Serializable;
import java.util.ArrayList;

public class CUIDModel implements Serializable {

    public CUIDModel() {
    }

    private int lead_id;
    private int lead_site_visit_id;
    private int isMyLead;
    private int lead_status_id;
    private int lead_types_id;
    private String cu_id;
    private String customer_name;
    private String customer_mobile;
    private String customer_email;
    private String customer_token_status;
    private int project_id;
    private String customer_project_name;
    private String customer_flat_type;
    private String assigned_by;
    private ArrayList<EventProjectDocsModel> eventProjectDocsModel;
    private ArrayList<PaymentModeModel> paymentModeModel;
    private int is_kyc_uploaded;
    private int is_reminder_set;
    private int is_call_scheduled;
    private String country_code;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String lead_status_name;
    private String token_media_path;
    private String token_no;
    private String prefix;
    private String eventName;
    private String token_type;
    private int event_id;
    private int site_visit_count;
    private int token_type_id;
    private String ghp_date;
    private String ghp_amount;
    private String ghp_plus_date;
    private String ghp_plus_amount;
    private String payment_link;
    private String payment_invoice_id;
    private String ghp_remark;
    private int lead_stage_id;
    private String lead_stage_name;
    private int booking_id;
    private int call_log_count;
    private int site_visit_count1;
    private int call_schedule_count;
    private int offline_lead_synced;

    public int getOffline_lead_synced() {
        return offline_lead_synced;
    }

    public void setOffline_lead_synced(int offline_lead_synced) {
        this.offline_lead_synced = offline_lead_synced;
    }

    public int getCall_log_count() {
        return call_log_count;
    }

    public void setCall_log_count(int call_log_count) {
        this.call_log_count = call_log_count;
    }

    public int getSite_visit_count1() {
        return site_visit_count1;
    }

    public void setSite_visit_count1(int site_visit_count1) {
        this.site_visit_count1 = site_visit_count1;
    }

    public int getCall_schedule_count() {
        return call_schedule_count;
    }

    public void setCall_schedule_count(int call_schedule_count) {
        this.call_schedule_count = call_schedule_count;
    }

    public int getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }

    public int getSite_visit_count() {
        return site_visit_count;
    }

    public void setSite_visit_count(int site_visit_count) {
        this.site_visit_count = site_visit_count;
    }

    public int getLead_site_visit_id() {
        return lead_site_visit_id;
    }

    public void setLead_site_visit_id(int lead_site_visit_id) {
        this.lead_site_visit_id = lead_site_visit_id;
    }
    public String getGhp_date() {
        return ghp_date;
    }

    public void setGhp_date(String ghp_date) {
        this.ghp_date = ghp_date;
    }

    public String getGhp_amount() {
        return ghp_amount;
    }

    public void setGhp_amount(String ghp_amount) {
        this.ghp_amount = ghp_amount;
    }

    public String getGhp_plus_date() {
        return ghp_plus_date;
    }

    public void setGhp_plus_date(String ghp_plus_date) {
        this.ghp_plus_date = ghp_plus_date;
    }

    public String getGhp_plus_amount() {
        return ghp_plus_amount;
    }

    public void setGhp_plus_amount(String ghp_plus_amount) {
        this.ghp_plus_amount = ghp_plus_amount;
    }

    public int getToken_id() {
        return token_id;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    private int token_id;

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }


    public int getToken_type_id() {
        return token_type_id;
    }

    public void setToken_type_id(int token_type_id) {
        this.token_type_id = token_type_id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLead_status_name() {
        return lead_status_name;
    }

    public void setLead_status_name(String lead_status_name) {
        this.lead_status_name = lead_status_name;
    }


    public int getLead_types_id() {
        return lead_types_id;
    }

    public void setLead_types_id(int lead_types_id) {
        this.lead_types_id = lead_types_id;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getCu_id() {
        return cu_id;
    }

    public void setCu_id(String cu_id) {
        this.cu_id = cu_id;
    }

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_mobile() {
        return customer_mobile;
    }

    public void setCustomer_mobile(String customer_mobile) {
        this.customer_mobile = customer_mobile;
    }

    public String getCustomer_token_status() {
        return customer_token_status;
    }

    public void setCustomer_token_status(String customer_token_status) {
        this.customer_token_status = customer_token_status;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }


    public String getCustomer_project_name() {
        return customer_project_name;
    }

    public void setCustomer_project_name(String customer_project_name) {
        this.customer_project_name = customer_project_name;
    }

    public String getCustomer_flat_type() {
        return customer_flat_type;
    }

    public void setCustomer_flat_type(String customer_flat_type) {
        this.customer_flat_type = customer_flat_type;
    }

    public ArrayList<EventProjectDocsModel> getEventProjectDocsModel() {
        return eventProjectDocsModel;
    }

    public void setEventProjectDocsModel(ArrayList<EventProjectDocsModel> eventProjectDocsModel) {
        this.eventProjectDocsModel = eventProjectDocsModel;
    }

    public ArrayList<PaymentModeModel> getPaymentModeModel() {
        return paymentModeModel;
    }

    public void setPaymentModeModel(ArrayList<PaymentModeModel> paymentModeModel) {
        this.paymentModeModel = paymentModeModel;
    }

    public int getIsMyLead() {
        return isMyLead;
    }

    public void setIsMyLead(int isAssigned) {
        this.isMyLead = isAssigned;
    }

    public String getAssigned_by() {
        return assigned_by;
    }

    public void setAssigned_by(String assigned_by) {
        this.assigned_by = assigned_by;
    }
    public void Set_Sales_person_Name(String assigned_by) {
        this.assigned_by = assigned_by;
    }

    public int getIs_kyc_uploaded() {
        return is_kyc_uploaded;
    }

    public void setIs_kyc_uploaded(int is_kyc_uploaded) {
        this.is_kyc_uploaded = is_kyc_uploaded;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
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

    public String getToken_media_path() {
        return token_media_path;
    }

    public void setToken_media_path(String token_media_path) {
        this.token_media_path = token_media_path;
    }

    public String getToken_no() {
        return token_no;
    }

    public void setToken_no(String token_no) {
        this.token_no = token_no;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getIs_reminder_set() {
        return is_reminder_set;
    }

    public void setIs_reminder_set(int is_reminder_set) {
        this.is_reminder_set = is_reminder_set;
    }

    public String getPayment_link() {
        return payment_link;
    }

    public void setPayment_link(String payment_link) {
        this.payment_link = payment_link;
    }

    public String getPayment_invoice_id() {
        return payment_invoice_id;
    }

    public void setPayment_invoice_id(String payment_invoice_id) {
        this.payment_invoice_id = payment_invoice_id;
    }

    public String getGhp_remark() {
        return ghp_remark;
    }

    public void setGhp_remark(String ghp_remark) {
        this.ghp_remark = ghp_remark;
    }


    public int getLead_stage_id() {
        return lead_stage_id;
    }

    public void setLead_stage_id(int lead_stage_id) {
        this.lead_stage_id = lead_stage_id;
    }

    public String getLead_stage_name() {
        return lead_stage_name;
    }

    public void setLead_stage_name(String lead_stage_name) {
        this.lead_stage_name = lead_stage_name;
    }

    public int getIs_call_scheduled() {
        return is_call_scheduled;
    }

    public void setIs_call_scheduled(int is_call_scheduled) {
        this.is_call_scheduled = is_call_scheduled;
    }
}

