package com.tribeappsoft.leedo.models.leads;

public class LeadStagesModel
{
    private int lead_stage_id;
    private String lead_stage_name;

    public LeadStagesModel() {
    }

    public LeadStagesModel(int lead_stage_id, String lead_stage_name) {
        this.lead_stage_id = lead_stage_id;
        this.lead_stage_name = lead_stage_name;
    }

    public int getLead_stage_id() {
        return lead_stage_id;
    }

    public void setLead_stage_id(int lead_stage_id) {
        this.lead_stage_id = lead_stage_id;
    }

    public String getLead_stage_name() {
        return lead_stage_name;
    }

    public void setLead_stage_name(String lead_stage_name) {
        this.lead_stage_name = lead_stage_name;
    }
}
