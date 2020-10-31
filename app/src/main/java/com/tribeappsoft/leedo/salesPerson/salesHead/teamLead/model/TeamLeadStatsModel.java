package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model;

import com.tribeappsoft.leedo.admin.reports.teamStats.model.TeamStatsModel;

import java.util.ArrayList;

public class TeamLeadStatsModel
{

    private int user_id;
    private int sales_team_lead_id;
    private String full_name;
    private ArrayList<TeamStatsModel> teamStatsModelArrayList;
    private String leads;
    private String leads_site_visits;
    private String lead_tokens_ghp;
    private String lead_tokens_ghp_plus;
    private String booking_master;



    public TeamLeadStatsModel() {

    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getSales_team_lead_id() {
        return sales_team_lead_id;
    }

    public void setSales_team_lead_id(int sales_team_lead_id) {
        this.sales_team_lead_id = sales_team_lead_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public ArrayList<TeamStatsModel> getTeamStatsModelArrayList() {
        return teamStatsModelArrayList;
    }

    public void setTeamStatsModelArrayList(ArrayList<TeamStatsModel> teamStatsModelArrayList) {
        this.teamStatsModelArrayList = teamStatsModelArrayList;
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

    public String getLead_tokens_ghp() {
        return lead_tokens_ghp;
    }

    public void setLead_tokens_ghp(String lead_tokens_ghp) {
        this.lead_tokens_ghp = lead_tokens_ghp;
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
