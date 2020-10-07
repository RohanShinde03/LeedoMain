package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class LeadsCampaignDetailsModel implements Serializable
{

    public int getLead_campaign_details_id() {
        return lead_campaign_details_id;
    }

    public void setLead_campaign_details_id(int lead_campaign_details_id) {
        this.lead_campaign_details_id = lead_campaign_details_id;
    }

    public String getLead_campaign_details_description() {
        return lead_campaign_details_description;
    }

    public void setLead_campaign_details_description(String lead_campaign_details_description) {
        this.lead_campaign_details_description = lead_campaign_details_description;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }



    private int lead_campaign_details_id;
    private String lead_campaign_details_description;
    private int isSelected;

    public LeadsCampaignDetailsModel()
    {

    }
}
