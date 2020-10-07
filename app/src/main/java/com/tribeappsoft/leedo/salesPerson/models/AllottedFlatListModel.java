package com.tribeappsoft.leedo.salesPerson.models;

import java.io.Serializable;
import java.util.ArrayList;

public class AllottedFlatListModel implements Serializable {

    int booking_id;
    String date_created;
    String booking_amt;
    String flat_total;
    String carpet_area;
    String  terrace_area;
    int lead_id;
    int lead_type_id;//todo leadtype id -1)cp lead 2)walkin 3)direct 4)R&L
    String lead_uid;
    String project_name;
    String mobile_number;
    String block_name;
    String unit_name;
    String token_no;
    String unit_category;
    String token_type;
    String event_title;
    String lead_types_name;
    String full_name;

    public String getSite_visit_date() {
        return site_visit_date;
    }

    public void setSite_visit_date(String site_visit_date) {
        this.site_visit_date = site_visit_date;
    }

    String site_visit_date;
    String sales_name;
    String site_visit_verified_by;
    String executive_name;

    public ArrayList<String> getImgstringArrayList() {
        return imgstringArrayList;
    }

    public void setImgstringArrayList(ArrayList<String> imgstringArrayList) {
        this.imgstringArrayList = imgstringArrayList;
    }

    private ArrayList<String> imgstringArrayList;


    public int getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getBooking_amt() {
        return booking_amt;
    }

    public void setBooking_amt(String booking_amt) {
        this.booking_amt = booking_amt;
    }

    public String getFlat_total() {
        return flat_total;
    }

    public void setFlat_total(String flat_total) {
        this.flat_total = flat_total;
    }

    public String getCarpet_area() {
        return carpet_area;
    }

    public void setCarpet_area(String carpet_area) {
        this.carpet_area = carpet_area;
    }

    public String getTerrace_area() {
        return terrace_area;
    }

    public void setTerrace_area(String terrace_area) {
        this.terrace_area = terrace_area;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public int getLead_type_id() {
        return lead_type_id;
    }
    public void setLead_type_id(int lead_type_id) {
        this.lead_type_id = lead_type_id;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getBlock_name() {
        return block_name;
    }

    public void setBlock_name(String block_name) {
        this.block_name = block_name;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public String getToken_no() {
        return token_no;
    }

    public void setToken_no(String token_no) {
        this.token_no = token_no;
    }

    public String getUnit_category() {
        return unit_category;
    }

    public void setUnit_category(String unit_category) {
        this.unit_category = unit_category;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getLead_types_name() {
        return lead_types_name;
    }

    public void setLead_types_name(String lead_types_name) {
        this.lead_types_name = lead_types_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getSales_name() {
        return sales_name;
    }

    public void setSales_name(String sales_name) {
        this.sales_name = sales_name;
    }

    public String getSite_visit_verified_by() {
        return site_visit_verified_by;
    }

    public void setSite_visit_verified_by(String site_visit_verified_by) {
        this.site_visit_verified_by = site_visit_verified_by;
    }

    public String getExecutive_name() {
        return executive_name;
    }

    public void setExecutive_name(String executive_name) {
        this.executive_name = executive_name;
    }
}
