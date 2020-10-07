package com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model;

public class TeamListModel {

    private int user_id;
    private int person_id;
    private String full_name;
    private String mobile_number;
    private String email;
    private boolean isCheckedBox;



    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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


    public boolean isCheckedBox() {
        return isCheckedBox;
    }

    public void setCheckedBox(boolean checkedBox) {
        isCheckedBox = checkedBox;
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
}
