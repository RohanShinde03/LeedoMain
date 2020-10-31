package com.tribeappsoft.leedo.admin.reports.salesHeadDashboard.detailedStats.model;

import java.util.ArrayList;

public class BookingModel {

    private String lead_uid;
    private String lead_id;
    private String booking_id;
    private String email;
    private String mobile_number;
    private String booking_amt;
    private String booking_date;
    private String full_name;
    private String sales_person_name;
    private ArrayList<SiteVisitModel> site_visits;
    private GHPModel ghp_details;
    private boolean isExpand;

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getLead_id() {
        return lead_id;
    }

    public void setLead_id(String lead_id) {
        this.lead_id = lead_id;
    }

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
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

    public String getBooking_amt() {
        return booking_amt;
    }

    public void setBooking_amt(String booking_amt) {
        this.booking_amt = booking_amt;
    }

    public String getBooking_date() {
        return booking_date;
    }

    public void setBooking_date(String booking_date) {
        this.booking_date = booking_date;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }



    public ArrayList<SiteVisitModel> getSite_visits() {
        return site_visits;
    }

    public void setSite_visits(ArrayList<SiteVisitModel> site_visits) {
        this.site_visits = site_visits;
    }

    public GHPModel getGhp_details() {
        return ghp_details;
    }

    public void setGhp_details(GHPModel ghp_details) {
        this.ghp_details = ghp_details;
    }
}
