package com.tribeappsoft.leedo.salesPerson.models;

public class MyPerformanceModel {

    private int project_id;
    private String project_name;
    private String leads_site_visits;
    private String leads;
    private String lead_tokens;
    private String lead_plus_tokens;
    private String booking_master;
    private String project_units_sold;
    private String project_units_OnHold;
    private String project_units_Reserved;

    public MyPerformanceModel()
    {

    }

    public String getLead_plus_tokens() {
        return lead_plus_tokens;
    }

    public void setLead_plus_tokens(String lead_plus_tokens) {
        this.lead_plus_tokens = lead_plus_tokens;
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

    public String getLeads_site_visits() {
        return leads_site_visits;
    }

    public void setLeads_site_visits(String leads_site_visits) {
        this.leads_site_visits = leads_site_visits;
    }

    public String getLeads() {
        return leads;
    }

    public void setLeads(String leads) {
        this.leads = leads;
    }

    public String getLead_tokens() {
        return lead_tokens;
    }

    public void setLead_tokens(String lead_tokens) {
        this.lead_tokens = lead_tokens;
    }

    public String getBooking_master() {
        return booking_master;
    }

    public void setBooking_master(String booking_master) {
        this.booking_master = booking_master;
    }

    public String getProject_units_sold() {
        return project_units_sold;
    }

    public void setProject_units_sold(String project_units_sold) {
        this.project_units_sold = project_units_sold;
    }

    public String getProject_units_OnHold() {
        return project_units_OnHold;
    }

    public void setProject_units_OnHold(String project_units_OnHold) {
        this.project_units_OnHold = project_units_OnHold;
    }

    public String getProject_units_Reserved() {
        return project_units_Reserved;
    }

    public void setProject_units_Reserved(String project_units_Reserved) {
        this.project_units_Reserved = project_units_Reserved;
    }
}
