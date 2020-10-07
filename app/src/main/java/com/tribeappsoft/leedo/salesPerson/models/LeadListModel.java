package com.tribeappsoft.leedo.salesPerson.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class LeadListModel implements Serializable, Parcelable
{

    //TODO Lead Status id temps
    // 1. Unclaimed,  2. Claimed, 3. Assigned, 4. Own(Self)  5.Site Visited, 6. Token Generated, 7. Booked


    // TODO lead type
    //1. CP  2. Walk-in  3. R & L

    private String lead_date;
    private int lead_id;
    private String lead_name;
    private String country_code;
    private String lead_mobile;
    private String lead_project_name;
    private String lead_unit_type;
    private String lead_cuid_number;
    private String lead_tag;
    private int lead_type_id;
    private int lead_status_id;
    private String lead_status;
    private String elapsed_time;
    private String lead_token_number;
    private String hold_duration;
    private String full_name;
    private String lead_types_name;
    private String added_by;
    private String leads_created_at;
    private String leads_updated_at;
    private String cp_name;
    private String cp_executive_name;
    private String ref_name;
    private String ref_mobile;
    private String churn_sales_person_name;
    private String churn_assign_date;
    private int churn_count;

    private int lead_churn_id;
    private int churned_sales_person_id;
    private boolean isExpandedOwnView;


    public static Creator<LeadListModel> getCREATOR() {
        return CREATOR;
    }

    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;


    public LeadListModel(){

    }


    protected LeadListModel(Parcel in) {
        lead_date = in.readString();
        lead_id = in.readInt();
        lead_name = in.readString();
        country_code = in.readString();
        lead_mobile = in.readString();
        lead_project_name = in.readString();
        lead_unit_type = in.readString();
        lead_cuid_number = in.readString();
        lead_tag = in.readString();
        lead_type_id = in.readInt();
        lead_status_id = in.readInt();
        lead_status = in.readString();
        elapsed_time = in.readString();
        lead_token_number = in.readString();
        hold_duration = in.readString();
        full_name = in.readString();
        lead_types_name = in.readString();
        added_by = in.readString();
        leads_created_at = in.readString();
        leads_updated_at = in.readString();
        cp_name = in.readString();
        cp_executive_name = in.readString();
        lead_churn_id = in.readInt();
        churned_sales_person_id = in.readInt();
        tag_date = in.readString();
    }

    public static final Creator<LeadListModel> CREATOR = new Creator<LeadListModel>() {
        @Override
        public LeadListModel createFromParcel(Parcel in) {
            return new LeadListModel(in);
        }

        @Override
        public LeadListModel[] newArray(int size) {
            return new LeadListModel[size];
        }
    };

    public String getTag_date() {
        return tag_date;
    }

    public void setTag_date(String tag_date) {
        this.tag_date = tag_date;
    }

    private String tag_date;

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

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }
    public String getLead_date() {
        return lead_date;
    }

    public void setLead_date(String lead_date) {
        this.lead_date = lead_date;
    }

    public String getLead_name() {
        return lead_name;
    }

    public void setLead_name(String lead_name) {
        this.lead_name = lead_name;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getLead_mobile() {
        return lead_mobile;
    }

    public void setLead_mobile(String lead_mobile) {
        this.lead_mobile = lead_mobile;
    }

    public String getLead_project_name() {
        return lead_project_name;
    }

    public void setLead_project_name(String lead_project_name) {
        this.lead_project_name = lead_project_name;
    }

    public String getLead_unit_type() {
        return lead_unit_type;
    }


    public String getLead_cuid_number() {
        return lead_cuid_number;
    }

    public void setLead_cuid_number(String lead_cuid_number) {
        this.lead_cuid_number = lead_cuid_number;
    }

    public String getLead_tag() {
        return lead_tag;
    }

    public void setLead_tag(String lead_tag) {
        this.lead_tag = lead_tag;
    }

    public String getLead_status() {
        return lead_status;
    }

    public void setLead_status(String lead_status) {
        this.lead_status = lead_status;
    }

    public String getElapsed_time() {
        return elapsed_time;
    }

    public void setElapsed_time(String elapsed_time) {
        this.elapsed_time = elapsed_time;
    }

    public String getLead_token_number() {
        return lead_token_number;
    }

    public void setLead_token_number(String lead_token_number) {
        this.lead_token_number = lead_token_number;
    }

    public int getLead_type_id() {
        return lead_type_id;
    }

    public void setLead_type_id(int lead_type_id) {
        this.lead_type_id = lead_type_id;
    }



    public void setLead_unit_type(String lead_unit_type) {
        this.lead_unit_type = lead_unit_type;
    }

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    public String getHold_duration() {
        return hold_duration;
    }

    public void setHold_duration(String hold_duration) {
        this.hold_duration = hold_duration;
    }

    public String getAdded_by() {
        return added_by;
    }

    public void setAdded_by(String added_by) {
        this.added_by = added_by;
    }


    public String getLeads_created_at() {
        return leads_created_at;
    }

    public void setLeads_created_at(String leads_created_at) {
        this.leads_created_at = leads_created_at;
    }

    public String getLeads_updated_at() {
        return leads_updated_at;
    }

    public void setLeads_updated_at(String leads_updated_at) {
        this.leads_updated_at = leads_updated_at;
    }

    public String getCp_name() {
        return cp_name;
    }

    public void setCp_name(String cp_name) {
        this.cp_name = cp_name;
    }

    public String getCp_executive_name() {
        return cp_executive_name;
    }

    public void setCp_executive_name(String cp_executive_name) {
        this.cp_executive_name = cp_executive_name;
    }

    public int getLead_churn_id() {
        return lead_churn_id;
    }

    public void setLead_churn_id(int lead_churn_id) {
        this.lead_churn_id = lead_churn_id;
    }

    public int getChurned_sales_person_id() {
        return churned_sales_person_id;
    }

    public void setChurned_sales_person_id(int churned_sales_person_id) {
        this.churned_sales_person_id = churned_sales_person_id;
    }

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    public String getRef_name() {
        return ref_name;
    }

    public void setRef_name(String ref_name) {
        this.ref_name = ref_name;
    }

    public String getRef_mobile() {
        return ref_mobile;
    }

    public void setRef_mobile(String ref_mobile) {
        this.ref_mobile = ref_mobile;
    }

    public int getChurn_count() {
        return churn_count;
    }

    public void setChurn_count(int churn_count) {
        this.churn_count = churn_count;
    }


    public String getChurn_sales_person_name() {
        return churn_sales_person_name;
    }

    public void setChurn_sales_person_name(String churn_sales_person_name) {
        this.churn_sales_person_name = churn_sales_person_name;
    }

    public String getChurn_assign_date() {
        return churn_assign_date;
    }

    public void setChurn_assign_date(String churn_assign_date) {
        this.churn_assign_date = churn_assign_date;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(lead_date);
        parcel.writeInt(lead_id);
        parcel.writeString(lead_name);
        parcel.writeString(country_code);
        parcel.writeString(lead_mobile);
        parcel.writeString(lead_project_name);
        parcel.writeString(lead_unit_type);
        parcel.writeString(lead_cuid_number);
        parcel.writeString(lead_tag);
        parcel.writeInt(lead_type_id);
        parcel.writeInt(lead_status_id);
        parcel.writeString(lead_status);
        parcel.writeString(elapsed_time);
        parcel.writeString(lead_token_number);
        parcel.writeString(hold_duration);
        parcel.writeString(full_name);
        parcel.writeString(lead_types_name);
        parcel.writeString(added_by);
        parcel.writeString(leads_created_at);
        parcel.writeString(leads_updated_at);
        parcel.writeString(cp_name);
        parcel.writeString(cp_executive_name);
        parcel.writeInt(lead_churn_id);
        parcel.writeInt(churned_sales_person_id);
        parcel.writeString(tag_date);
    }
}
