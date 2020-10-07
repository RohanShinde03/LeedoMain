package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model;

import java.util.ArrayList;

public class TeamLeaderModel {

    private String last_name;
    private String first_name;
    private String full_name;
    private String middle_name;
    private int is_team_lead;
    private int cp_executive_id;
    private int sales_lead_id;
    private int user_id;
    private String teamMembersCount;

    public String getPhotopath() {
        return Photopath;
    }

    public void setPhotopath(String photopath) {
        Photopath = photopath;
    }

    private String Photopath;
    private String  mobile_number;
    private String email;
    private String tv_teamLeadList_memberCount;

    public ArrayList<TeamMemberModel> getMemberModelArrayList() {
        return memberModelArrayList;
    }

    public void setMemberModelArrayList(ArrayList<TeamMemberModel> memberModelArrayList) {
        this.memberModelArrayList = memberModelArrayList;
    }

    private ArrayList<TeamMemberModel> memberModelArrayList;


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
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

    public int getIs_team_lead() {
        return is_team_lead;
    }

    public void setIs_team_lead(int is_team_lead) {
        this.is_team_lead = is_team_lead;
    }

    public int getCp_executive_id() {
        return cp_executive_id;
    }

    public void setCp_executive_id(int cp_executive_id) {
        this.cp_executive_id = cp_executive_id;
    }

    public int getSales_lead_id() {
        return sales_lead_id;
    }

    public void setSales_lead_id(int sales_lead_id) {
        this.sales_lead_id = sales_lead_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }


    public String getTv_teamLeadList_memberCount() {
        return tv_teamLeadList_memberCount;
    }

    public void setTv_teamLeadList_memberCount(String tv_teamLeadList_memberCount) {
        this.tv_teamLeadList_memberCount = tv_teamLeadList_memberCount;
    }

    public String getTeamMembersCount() {
        return teamMembersCount;
    }

    public void setTeamMembersCount(String teamMembersCount) {
        this.teamMembersCount = teamMembersCount;
    }
}
