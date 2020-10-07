package com.tribeappsoft.leedo.admin.leads.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LeadGenerationModel  implements Serializable {

    private int lead_type_id;
    private String type_name;
    private int edit_text_req;
    private String edit_text_title;
    private int status_id;
    private int secLvl;
    private ArrayList<LeadGenerationSecondModel> generationModelArrayList;

    public LeadGenerationModel() {
    }

    public int getLead_type_id() {
        return lead_type_id;
    }

    public void setLead_type_id(int lead_type_id) {
        this.lead_type_id = lead_type_id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public int getEdit_text_req() {
        return edit_text_req;
    }

    public void setEdit_text_req(int edit_text_req) {
        this.edit_text_req = edit_text_req;
    }

    public String getEdit_text_title() {
        return edit_text_title;
    }

    public void setEdit_text_title(String edit_text_title) {
        this.edit_text_title = edit_text_title;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public int getSecLvl() {
        return secLvl;
    }

    public void setSecLvl(int secLvl) {
        this.secLvl = secLvl;
    }

    public ArrayList<LeadGenerationSecondModel> getGenerationModelArrayList() {
        return generationModelArrayList;
    }

    public void setGenerationModelArrayList(ArrayList<LeadGenerationSecondModel> generationModelArrayList) {
        this.generationModelArrayList = generationModelArrayList;
    }
}
