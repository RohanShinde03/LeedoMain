package com.tribeappsoft.leedo.util.compactCalender;

import java.util.Date;

public class CalenderDay {


    private Date Commited_date;
    private Date Assigned_date;
    private String events;

    public CalenderDay()
    {


    }

    public CalenderDay(Date commited_date, Date assigned_date, String events) {
        Commited_date = commited_date;
        Assigned_date = assigned_date;
        this.events = events;
    }

    public Date getCommited_date() {
        return Commited_date;
    }

    public void setCommited_date(Date commited_date) {
        Commited_date = commited_date;
    }

    public Date getAssigned_date() {
        return Assigned_date;
    }

    public void setAssigned_date(Date assigned_date) {
        Assigned_date = assigned_date;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }
}
