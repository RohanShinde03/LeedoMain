package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class PropertyBuyingForModel implements Serializable
{


    public int getBuying_for_reason_id() {
        return buying_for_reason_id;
    }

    public void setBuying_for_reason_id(int buying_for_reason_id) {
        this.buying_for_reason_id = buying_for_reason_id;
    }

    public String getBuying_for_reason() {
        return buying_for_reason;
    }

    public void setBuying_for_reason(String buying_for_reason) {
        this.buying_for_reason = buying_for_reason;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }


    private int buying_for_reason_id;
    private String buying_for_reason;
    private int status_id;

    public PropertyBuyingForModel()
    {

    }
}
