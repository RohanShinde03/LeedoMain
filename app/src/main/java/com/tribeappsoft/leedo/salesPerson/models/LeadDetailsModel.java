package com.tribeappsoft.leedo.salesPerson.models;

public class LeadDetailsModel {
    
    private String lead_details_text;
    private String lead_details_value;

    public LeadDetailsModel() {}

    public LeadDetailsModel(String lead_tags_details_title, String lead_tags_details_subtitle1)
    {
        this.lead_details_text = lead_tags_details_title;
        this.lead_details_value = lead_tags_details_subtitle1;
    }

    public String getLead_details_text() {
        return lead_details_text;
    }

    public void setLead_details_text(String lead_details_text) {
        this.lead_details_text = lead_details_text;
    }

    public String getLead_details_value() {
        return lead_details_value;
    }

    public void setLead_details_value(String lead_details_value) {
        this.lead_details_value = lead_details_value;
    }


}
