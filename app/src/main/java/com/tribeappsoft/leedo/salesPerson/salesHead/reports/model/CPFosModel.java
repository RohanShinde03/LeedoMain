package com.tribeappsoft.leedo.salesPerson.salesHead.reports.model;

import java.io.Serializable;

public class CPFosModel implements Serializable
{
    private int cp_executive_id;
    private int user_id;
    private int person_id;
    private String full_name;
    private String leads;
    private String leads_site_visits;
    private String lead_tokens;
    private String lead_tokens_ghp_plus;
    private String booking_master;

    public CPFosModel() {

    }


    public int getCp_executive_id() {
        return cp_executive_id;
    }

    public void setCp_executive_id(int cp_executive_id) {
        this.cp_executive_id = cp_executive_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getPerson_id() {
        return person_id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getLeads() {
        return leads;
    }

    public void setLeads(String leads) {
        this.leads = leads;
    }

    public String getLeads_site_visits() {
        return leads_site_visits;
    }

    public void setLeads_site_visits(String leads_site_visits) {
        this.leads_site_visits = leads_site_visits;
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



}
