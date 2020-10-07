package com.tribeappsoft.leedo.models.leads;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class LeadBuyingDurationModel
{


    public int getBuying_in_duration_id() {
        return buying_in_duration_id;
    }

    public void setBuying_in_duration_id(int buying_in_duration_id) {
        this.buying_in_duration_id = buying_in_duration_id;
    }

    public String getBuying_in_duration() {
        return buying_in_duration;
    }

    public void setBuying_in_duration(String buying_in_duration) {
        this.buying_in_duration = buying_in_duration;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    private int buying_in_duration_id;
    private String buying_in_duration;
    private int status_id;

    public LeadBuyingDurationModel()
    {

    }
}
