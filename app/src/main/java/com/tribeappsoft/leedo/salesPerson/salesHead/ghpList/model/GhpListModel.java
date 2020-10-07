package com.tribeappsoft.leedo.salesPerson.salesHead.ghpList.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.tribeappsoft.leedo.salesPerson.models.LeadDetailsTitleModel;

import java.io.Serializable;
import java.util.ArrayList;

public class GhpListModel implements Serializable, Parcelable {

    private int lead_id;
    private String tagDate;
    private String unit_category;
    private String project_name;
    private String lead_uid;
    private String country_code;
    private String mobile_number;
    private String email;
    private String prefix;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String full_name;
    private int sales_person_id;
    private boolean isChecked;
    private String isFirstCall;
    private String noOfCallMade;
    private String call_Duration;

    public String getMin_call_duration() {
        return min_call_duration;
    }

    public void setMin_call_duration(String min_call_duration) {
        this.min_call_duration = min_call_duration;
    }

    public String getMax_call_duration() {
        return max_call_duration;
    }

    public void setMax_call_duration(String max_call_duration) {
        this.max_call_duration = max_call_duration;
    }

    private String min_call_duration;
    private String max_call_duration;
    private String avg_time;
    private int call_done;
    private int call_count;

    public GhpListModel() {}
    protected GhpListModel(Parcel in) {
        lead_id = in.readInt();
        tagDate = in.readString();
        unit_category = in.readString();
        project_name = in.readString();
        lead_uid = in.readString();
        country_code = in.readString();
        mobile_number = in.readString();
        email = in.readString();
        prefix = in.readString();
        first_name = in.readString();
        middle_name = in.readString();
        last_name = in.readString();
        full_name = in.readString();
        sales_person_id = in.readInt();
       // isChecked = in.readBoolean();;
        isFirstCall = in.readString();
        noOfCallMade = in.readString();
        call_Duration = in.readString();
        avg_time = in.readString();
        call_done = in.readInt();
        call_count = in.readInt();
        cp_Name = in.readString();
        cp_executive_name = in.readString();
        sales_person_name = in.readString();
        Lead_Type = in.readString();
        min_call_duration = in.readString();
        max_call_duration = in.readString();

    }

    public static Creator<GhpListModel> getCREATOR() {
        return CREATOR;
    }

    public String getCp_Name() {
        return cp_Name;
    }

    public void setCp_Name(String cp_Name) {
        this.cp_Name = cp_Name;
    }

    public String getCp_executive_name() {
        return cp_executive_name;
    }

    public void setCp_executive_name(String cp_executive_name) {
        this.cp_executive_name = cp_executive_name;
    }

    private String cp_Name;
    private String cp_executive_name;

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    private String sales_person_name;

    public String getTagDate() {
        return tagDate;
    }

    public void setTagDate(String tagDate) {
        this.tagDate = tagDate;
    }

    public String getAvg_time() {
        return avg_time;
    }

    public void setAvg_time(String avg_time) {
        this.avg_time = avg_time;
    }

    public int getCall_done() {
        return call_done;
    }

    public void setCall_done(int call_done) {
        this.call_done = call_done;
    }

    public int getCall_count() {
        return call_count;
    }

    public void setCall_count(int call_count) {
        this.call_count = call_count;
    }

    public boolean isExpandedOthersView() {
        return isExpandedOthersView;
    }

    public void setExpandedOthersView(boolean expandedOthersView) {
        isExpandedOthersView = expandedOthersView;
    }

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;

    public static final Creator<GhpListModel> CREATOR = new Creator<GhpListModel>() {
        @Override
        public GhpListModel createFromParcel(Parcel in) {
            return new GhpListModel(in);
        }

        @Override
        public GhpListModel[] newArray(int size) {
            return new GhpListModel[size];
        }
    };

    public String getLead_Type() {
        return Lead_Type;
    }

    public void setLead_Type(String lead_Type) {
        Lead_Type = lead_Type;
    }

    private String Lead_Type;

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    private boolean isExpandedOwnView;
    private boolean isExpandedOthersView;

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getUnit_category() {
        return unit_category;
    }

    public void setUnit_category(String unit_category) {
        this.unit_category = unit_category;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getLead_uid() {
        return lead_uid;
    }

    public void setLead_uid(String lead_uid) {
        this.lead_uid = lead_uid;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public int getSales_person_id() {
        return sales_person_id;
    }

    public void setSales_person_id(int sales_person_id) {
        this.sales_person_id = sales_person_id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getIsFirstCall() {
        return isFirstCall;
    }

    public void setIsFirstCall(String isFirstCall) {
        this.isFirstCall = isFirstCall;
    }

    public String getNoOfCallMade() {
        return noOfCallMade;
    }

    public void setNoOfCallMade(String noOfCallMade) {
        this.noOfCallMade = noOfCallMade;
    }

    public String getCall_Duration() {
        return call_Duration;
    }

    public void setCall_Duration(String call_Duration) {
        this.call_Duration = call_Duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(lead_id);
        parcel.writeString(tagDate);
        parcel.writeString(unit_category);
        parcel.writeString(project_name);
        parcel.writeString(lead_uid);
        parcel.writeString(country_code);
        parcel.writeString(mobile_number);
        parcel.writeString(email);
        parcel.writeString(prefix);
        parcel.writeString(first_name);
        parcel.writeString(middle_name);
        parcel.writeString(last_name);
        parcel.writeString(full_name);
        parcel.writeInt(sales_person_id);
        // isChecked = in.readBoolean();;
        parcel.writeString(isFirstCall);
        parcel.writeString(noOfCallMade);
        parcel.writeString(call_Duration);
        parcel.writeString(avg_time);
        parcel.writeInt(call_done);
        parcel.writeInt(call_count);
        parcel.writeString(cp_Name);
        parcel.writeString(cp_executive_name);
        parcel.writeString(sales_person_name);
        parcel.writeString(Lead_Type);
        parcel.writeString(min_call_duration);
        parcel.writeString(max_call_duration);

    }
}
