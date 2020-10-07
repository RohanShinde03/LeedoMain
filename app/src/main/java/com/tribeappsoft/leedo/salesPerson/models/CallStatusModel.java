package com.tribeappsoft.leedo.salesPerson.models;

import java.io.Serializable;

public class CallStatusModel implements Serializable
{

    private int call_status_id;
    private String call_status;

    public CallStatusModel() {
    }

    public CallStatusModel(int call_status_id, String call_status) {
        this.call_status_id = call_status_id;
        this.call_status = call_status;
    }

    public int getCall_status_id() {
        return call_status_id;
    }

    public void setCall_status_id(int call_status_id) {
        this.call_status_id = call_status_id;
    }

    public String getCall_status() {
        return call_status;
    }

    public void setCall_status(String call_status) {
        this.call_status = call_status;
    }

}
