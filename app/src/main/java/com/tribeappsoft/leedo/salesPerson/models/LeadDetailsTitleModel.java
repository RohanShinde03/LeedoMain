package com.tribeappsoft.leedo.salesPerson.models;

import java.util.ArrayList;

public class LeadDetailsTitleModel {
    private String lead_details_title;
    private ArrayList<LeadDetailsModel> leadDetailsModels;



    public LeadDetailsTitleModel()
    {

    }

    public LeadDetailsTitleModel(String lead_detailsTitle)
    {
        this.lead_details_title = lead_detailsTitle;
    }

    public String getLead_details_title() {
        return lead_details_title;
    }

    public void setLead_details_title(String lead_details_title) {
        this.lead_details_title = lead_details_title;
    }

    public ArrayList<LeadDetailsModel> getLeadDetailsModels() {
        return leadDetailsModels;
    }

    public void setLeadDetailsModels(ArrayList<LeadDetailsModel> leadDetailsModels) {
        this.leadDetailsModels = leadDetailsModels;
    }


}
