package com.tribeappsoft.leedo.salesPerson.models;

import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;

import java.util.ArrayList;

public class EventsModel {
    public EventsModel() {
    }


    private int event_id;
    private String reg_start_date;
    private String reg_end_date;
    private String start_date;
    private String end_date;
    private String event_venue;
    private String event_banner_path;
    private String event_title;
    private String event_fee;
    private String event_status_id;
    private String status;
    private String event_status;
    private String registeredOn;
    private String attending_user_id;
    private String attending_status;
    private String event_description;
    private ArrayList<TokensModel> tokensModelArrayList;
    private ArrayList<String>eventPhotoArrayList;
    private int is_registered;
    private int totalLeads;
    private int totalBookings;
    private int totalonHolds;
    private int myLeads;
    private int myBookings;
    private int myonHolds;


    public ArrayList<TokensModel> getTokensModelArrayList() {
        return tokensModelArrayList;
    }

    public void setTokensModelArrayList(ArrayList<TokensModel> tokensModelArrayList) {
        this.tokensModelArrayList = tokensModelArrayList;
    }


    public ArrayList<String> getEventPhotoArrayList() {
        return eventPhotoArrayList;
    }

    public void setEventPhotoArrayList(ArrayList<String> eventPhotoArrayList) {
        this.eventPhotoArrayList = eventPhotoArrayList;
    }



    public ArrayList<EventProjectDocsModel> getEventProjectDocsModelArrayList() {
        return eventProjectDocsModelArrayList;
    }

    public void setEventProjectDocsModelArrayList(ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList) {
        this.eventProjectDocsModelArrayList = eventProjectDocsModelArrayList;
    }

    private ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList;

    public int getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(int totalLeads) {
        this.totalLeads = totalLeads;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getTotalonHolds() {
        return totalonHolds;
    }

    public void setTotalonHolds(int totalonHolds) {
        this.totalonHolds = totalonHolds;
    }

    public int getMyLeads() {
        return myLeads;
    }

    public void setMyLeads(int myLeads) {
        this.myLeads = myLeads;
    }

    public int getMyBookings() {
        return myBookings;
    }

    public void setMyBookings(int myBookings) {
        this.myBookings = myBookings;
    }

    public int getMyonHolds() {
        return myonHolds;
    }

    public void setMyonHolds(int myonHolds) {
        this.myonHolds = myonHolds;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public String getReg_start_date() {
        return reg_start_date;
    }

    public void setReg_start_date(String reg_start_date) {
        this.reg_start_date = reg_start_date;
    }

    public String getReg_end_date() {
        return reg_end_date;
    }

    public void setReg_end_date(String reg_end_date) {
        this.reg_end_date = reg_end_date;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getEvent_venue() {
        return event_venue;
    }

    public void setEvent_venue(String event_venue) {
        this.event_venue = event_venue;
    }

    public String getEvent_banner_path() {
        return event_banner_path;
    }

    public void setEvent_banner_path(String event_banner_path) {
        this.event_banner_path = event_banner_path;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getEvent_fee() {
        return event_fee;
    }

    public void setEvent_fee(String event_fee) {
        this.event_fee = event_fee;
    }

    public String getEvent_status_id() {
        return event_status_id;
    }

    public void setEvent_status_id(String event_status_id) {
        this.event_status_id = event_status_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEvent_status() {
        return event_status;
    }

    public void setEvent_status(String event_status) {
        this.event_status = event_status;
    }

    public String getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(String registeredOn) {
        this.registeredOn = registeredOn;
    }

    public String getAttending_user_id() {
        return attending_user_id;
    }

    public void setAttending_user_id(String attending_user_id) {
        this.attending_user_id = attending_user_id;
    }

    public String getAttending_status() {
        return attending_status;
    }

    public void setAttending_status(String attending_status) {
        this.attending_status = attending_status;
    }

    public String getEvent_description() {
        return event_description;
    }

    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public int getIs_registered() {
        return is_registered;
    }

    public void setIs_registered(int is_registered) {
        this.is_registered = is_registered;
    }


}
