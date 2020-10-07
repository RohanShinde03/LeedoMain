package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class LeadCampaignsModel implements Serializable
{


    public int getLead_campaign_details_id() {
        return lead_campaign_details_id;
    }

    public void setLead_campaign_details_id(int lead_campaign_details_id) {
        this.lead_campaign_details_id = lead_campaign_details_id;
    }

    public String getLead_campaign_type() {
        return lead_campaign_type;
    }

    public void setLead_campaign_type(String lead_campaign_type) {
        this.lead_campaign_type = lead_campaign_type;
    }

    public String getLead_campaign_details_description() {
        return lead_campaign_details_description;
    }

    public void setLead_campaign_details_description(String lead_campaign_details_description) {
        this.lead_campaign_details_description = lead_campaign_details_description;
    }

    public ArrayList<LeadsCampaignDetailsModel> getCampaignDetailsModelArrayList() {
        return campaignDetailsModelArrayList;
    }

    public void setCampaignDetailsModelArrayList(ArrayList<LeadsCampaignDetailsModel> campaignDetailsModelArrayList) {
        this.campaignDetailsModelArrayList = campaignDetailsModelArrayList;
    }


    private int lead_campaign_details_id;
    private String lead_campaign_type;
    private String lead_campaign_details_description;
    private ArrayList<LeadsCampaignDetailsModel> campaignDetailsModelArrayList;

    public LeadCampaignsModel()
    {

    }
}
