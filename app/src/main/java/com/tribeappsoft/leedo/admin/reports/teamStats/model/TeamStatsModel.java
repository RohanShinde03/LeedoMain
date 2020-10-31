package com.tribeappsoft.leedo.admin.reports.teamStats.model;

public class TeamStatsModel
{
    private int sales_person_id;
    private String full_name;
    private int project_id;
    private String project_name;
    private String leads_site_visits;
    private String leads;
    private String lead_tokens;
    private String lead_tokens_ghp_plus;
    private String booking_master;
    private String cancel_booking;
    private String fos_count;

    public TeamStatsModel() {

    }

    public int getSales_person_id() {
        return sales_person_id;
    }

    public void setSales_person_id(int sales_person_id) {
        this.sales_person_id = sales_person_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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

    public String getLead_tokens_ghp_plus() {
        return lead_tokens_ghp_plus;
    }

    public void setLead_tokens_ghp_plus(String lead_tokens_ghp_plus) {
        this.lead_tokens_ghp_plus = lead_tokens_ghp_plus;
    }

    public String getBooking_master() {
        return booking_master;
    }

    public void setBooking_master(String booking_master) {
        this.booking_master = booking_master;
    }

    public String getFos_count() {
        return fos_count;
    }

    public void setFos_count(String fos_count) {
        this.fos_count = fos_count;
    }

    public String getCancel_booking() {
        return cancel_booking;
    }

    public void setCancel_booking(String cancel_booking) {
        this.cancel_booking = cancel_booking;
    }

}
