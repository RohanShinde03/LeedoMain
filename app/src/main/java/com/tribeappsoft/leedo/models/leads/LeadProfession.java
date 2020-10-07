package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;

public class LeadProfession implements Serializable
{


    private int lead_profession_id;
    private String lead_profession;


    public LeadProfession() {

    }

    public int getLead_profession_id() {
        return lead_profession_id;
    }

    public void setLead_profession_id(int lead_profession_id) {
        this.lead_profession_id = lead_profession_id;
    }

    public String getLead_profession() {
        return lead_profession;
    }

    public void setLead_profession(String lead_profession) {
        this.lead_profession = lead_profession;
    }
}
