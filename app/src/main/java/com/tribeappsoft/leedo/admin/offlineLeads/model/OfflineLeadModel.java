package com.tribeappsoft.leedo.admin.offlineLeads.model;

import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.salesPerson.models.PaymentModeModel;

import java.io.Serializable;
import java.util.ArrayList;

public class OfflineLeadModel implements Serializable {
    public OfflineLeadModel() { }

    private int lead_id;
    private int lead_site_visit_id;
    private int lead_types_id;
    private int prefix_id;
    private String prefix;
    private String api_token;
    private String customer_name;
    private String mobile_number;
    private String alternate_mobile_number;
    private String address_line_1;
    private String customer_email;
    private String country_code;
    private String country_code_1;
    private int project_id;
    private int unit_category_id;
    private String customer_project_name;
    private String customer_unit_type;
    private String lead_profession;
    private int lead_profession_id;
    private String lead_ni_reason;
    private String lead_ni_other_reason;
    private int budget_limit_id;
    private String budget_limit;
    private int income_range_id ;
    private String income_range;
    private int is_first_home;
    private int lead_stage_id;
    private String lead_stage;
    private int lead_status_id;
    private String dob;
    private String lead_types;
    private int sales_person_id;
    private String remarks;
    private int is_site_visited;
    private String visit_date;
    private String visit_time;
    private String visit_remark;
    private boolean isExpandedOwnView;

    public int getOffline_id() {
        return offline_id;
    }

    public void setOffline_id(int offline_id) {
        this.offline_id = offline_id;
    }

    private int offline_id;

    public int getIs_site_visited() {
        return is_site_visited;
    }

    public void setIs_site_visited(int is_site_visited) {
        this.is_site_visited = is_site_visited;
    }

    public String getVisit_date() {
        return visit_date;
    }

    public void setVisit_date(String visit_date) {
        this.visit_date = visit_date;
    }

    public String getVisit_time() {
        return visit_time;
    }

    public void setVisit_time(String visit_time) {
        this.visit_time = visit_time;
    }

    public String getVisit_remark() {
        return visit_remark;
    }

    public void setVisit_remark(String visit_remark) {
        this.visit_remark = visit_remark;
    }


    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }


    public String getLead_types() {
        return lead_types;
    }

    public void setLead_types(String lead_types) {
        this.lead_types = lead_types;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public int getLead_site_visit_id() {
        return lead_site_visit_id;
    }

    public void setLead_site_visit_id(int lead_site_visit_id) {
        this.lead_site_visit_id = lead_site_visit_id;
    }

    public int getLead_types_id() {
        return lead_types_id;
    }

    public void setLead_types_id(int lead_types_id) {
        this.lead_types_id = lead_types_id;
    }

    public int getPrefix_id() {
        return prefix_id;
    }

    public void setPrefix_id(int prefix_id) {
        this.prefix_id = prefix_id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getApi_token() {
        return api_token;
    }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getAlternate_mobile_number() {
        return alternate_mobile_number;
    }

    public void setAlternate_mobile_number(String alternate_mobile_number) {
        this.alternate_mobile_number = alternate_mobile_number;
    }

    public String getAddress_line_1() {
        return address_line_1;
    }

    public void setAddress_line_1(String address_line_1) {
        this.address_line_1 = address_line_1;
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

    public String getCountry_code_1() {
        return country_code_1;
    }

    public void setCountry_code_1(String country_code_1) {
        this.country_code_1 = country_code_1;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getUnit_category_id() {
        return unit_category_id;
    }

    public void setUnit_category_id(int unit_category_id) {
        this.unit_category_id = unit_category_id;
    }

    public String getCustomer_project_name() {
        return customer_project_name;
    }

    public void setCustomer_project_name(String customer_project_name) {
        this.customer_project_name = customer_project_name;
    }

    public String getCustomer_unit_type() {
        return customer_unit_type;
    }

    public void setCustomer_unit_type(String customer_unit_type) {
        this.customer_unit_type = customer_unit_type;
    }

    public String getLead_profession() {
        return lead_profession;
    }

    public void setLead_profession(String lead_profession) {
        this.lead_profession = lead_profession;
    }

    public String getLead_ni_reason() {
        return lead_ni_reason;
    }

    public void setLead_ni_reason(String lead_ni_reason) {
        this.lead_ni_reason = lead_ni_reason;
    }

    public String getLead_ni_other_reason() {
        return lead_ni_other_reason;
    }

    public void setLead_ni_other_reason(String lead_ni_other_reason) {
        this.lead_ni_other_reason = lead_ni_other_reason;
    }

    public int getBudget_limit_id() {
        return budget_limit_id;
    }

    public void setBudget_limit_id(int budget_limit_id) {
        this.budget_limit_id = budget_limit_id;
    }

    public String getBudget_limit() {
        return budget_limit;
    }

    public void setBudget_limit(String budget_limit) {
        this.budget_limit = budget_limit;
    }

    public int getIncome_range_id() {
        return income_range_id;
    }

    public void setIncome_range_id(int income_range_id) {
        this.income_range_id = income_range_id;
    }

    public String getIncome_range() {
        return income_range;
    }

    public void setIncome_range(String income_range) {
        this.income_range = income_range;
    }

    public int getIs_first_home() {
        return is_first_home;
    }

    public void setIs_first_home(int is_first_home) {
        this.is_first_home = is_first_home;
    }

    public int getLead_stage_id() {
        return lead_stage_id;
    }

    public void setLead_stage_id(int lead_stage_id) {
        this.lead_stage_id = lead_stage_id;
    }

    public String getLead_stage() {
        return lead_stage;
    }

    public void setLead_stage(String lead_stage) {
        this.lead_stage = lead_stage;
    }

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getSales_person_id() {
        return sales_person_id;
    }

    public void setSales_person_id(int sales_person_id) {
        this.sales_person_id = sales_person_id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public int getLead_profession_id() {
        return lead_profession_id;
    }

    public void setLead_profession_id(int lead_profession_id) {
        this.lead_profession_id = lead_profession_id;
    }
}
