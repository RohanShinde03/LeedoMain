package com.tribeappsoft.leedo.salesPerson.salesHead.salesExecutiveList.model;

public class SalesExecutiveModel {

    private int user_id;
    private int person_id;
    private int is_team_lead;
    private String last_name;
    private String first_name;
    private String full_name;
    private String middle_name;
    private String mobile_number;
    private String email;

    public String getPhotopath() {
        return Photopath;
    }

    public void setPhotopath(String photopath) {
        Photopath = photopath;
    }

    private String Photopath;

    public SalesExecutiveModel() {
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

    public int getIs_team_lead() {
        return is_team_lead;
    }

    public void setIs_team_lead(int is_team_lead) {
        this.is_team_lead = is_team_lead;
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

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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
}
