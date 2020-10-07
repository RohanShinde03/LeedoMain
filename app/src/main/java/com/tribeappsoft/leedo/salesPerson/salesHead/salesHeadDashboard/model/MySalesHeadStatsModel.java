package com.tribeappsoft.leedo.salesPerson.salesHead.salesHeadDashboard.model;

public class MySalesHeadStatsModel {

    private int project_id;
    private String project_name;
    private String unclaimedleads;
    private String TotalLeads;
    private String sitevisit;
    private String ghp;
    private String ghpPlus;
    private String allotments;
    private String cancel_booking_count;
    private String cpregistered;
    private String projects;
    private String events;

    public String getGhpPaymentPending() {
        return ghpPaymentPending;
    }

    public void setGhpPaymentPending(String ghpPaymentPending) {
        this.ghpPaymentPending = ghpPaymentPending;
    }

    private String ghpPaymentPending;


    public MySalesHeadStatsModel()
    {
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

    public String getUnclaimedleads() {
        return unclaimedleads;
    }

    public void setUnclaimedleads(String unclaimedleads) {
        this.unclaimedleads = unclaimedleads;
    }

    public String getTotalLeads() {
        return TotalLeads;
    }

    public void setTotalLeads(String totalLeads) {
        this.TotalLeads = totalLeads;
    }

    public String getSitevisit() {
        return sitevisit;
    }

    public void setSitevisit(String sitevisit) {
        this.sitevisit = sitevisit;
    }

    public String getGhp() {
        return ghp;
    }

    public void setGhp(String ghp) {
        this.ghp = ghp;
    }

    public String getGhpPlus() {
        return ghpPlus;
    }

    public void setGhpPlus(String ghpPlus) {
        this.ghpPlus = ghpPlus;
    }

    public String getAllotments() {
        return allotments;
    }

    public void setAllotments(String allotments) {
        this.allotments = allotments;
    }

    public String getCancel_booking_count() {
        return cancel_booking_count;
    }

    public void setCancel_booking_count(String cancel_booking_count) {
        this.cancel_booking_count = cancel_booking_count;
    }

    public String getCpregistered() {
        return cpregistered;
    }

    public void setCpregistered(String cpregistered) {
        this.cpregistered = cpregistered;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

}