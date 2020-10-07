package com.tribeappsoft.leedo.siteEngineer.models;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 12/8/19.
 */
public class SiteVisitsModel implements Serializable
{

    private int project_id;
    private String project_name;
    private String visit_type;
    private String visit_date;
    private String customer_name;
    private String countryCode;
    private String customer_mobile;
    private String customer_email;
    private String flat_type;
    private String flat_number;
    private String visit_count;
    private boolean isPaidVisit;
    private String check_in_time;
    private String check_out_time;

    public SiteVisitsModel()
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

    public String getVisit_type() {
        return visit_type;
    }

    public void setVisit_type(String visit_type) {
        this.visit_type = visit_type;
    }

    public String getVisit_date() {
        return visit_date;
    }

    public void setVisit_date(String visit_date) {
        this.visit_date = visit_date;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getCustomer_mobile() {
        return customer_mobile;
    }

    public void setCustomer_mobile(String customer_mobile) {
        this.customer_mobile = customer_mobile;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getFlat_type() {
        return flat_type;
    }

    public void setFlat_type(String flat_type) {
        this.flat_type = flat_type;
    }

    public String getFlat_number() {
        return flat_number;
    }

    public void setFlat_number(String flat_number) {
        this.flat_number = flat_number;
    }

    public String getVisit_count() {
        return visit_count;
    }

    public void setVisit_count(String visit_count) {
        this.visit_count = visit_count;
    }

    public boolean isPaidVisit() {
        return isPaidVisit;
    }

    public void setPaidVisit(boolean paidVisit) {
        isPaidVisit = paidVisit;
    }

    public String getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(String check_in_time) {
        this.check_in_time = check_in_time;
    }

    public String getCheck_out_time() {
        return check_out_time;
    }

    public void setCheck_out_time(String check_out_time) {
        this.check_out_time = check_out_time;
    }




}
