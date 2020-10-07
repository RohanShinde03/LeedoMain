package com.tribeappsoft.leedo.admin.leads.model;

import java.io.Serializable;

public class LeadGenerationSecondModel implements Serializable {

    public LeadGenerationSecondModel() { }

    private int id;
    private int  lead_type_id;
    private String name;
    private int status_id;
    private int isSelected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLead_type_id() {
        return lead_type_id;
    }

    public void setLead_type_id(int lead_type_id) {
        this.lead_type_id = lead_type_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }
}
