package com.tribeappsoft.leedo.salesPerson.models;

public class ReminderModel
{


    public ReminderModel() {
    }


    private int mark_as_done;
    private int reminder_id;
    private String reminder_name;
    private String reminder_date;
    private String reminder_time;
    private String reminder_details;
    private String remind_at_date_format;
    private String remind_at_time_format;
    private String remind_at_time_format1;
    private String done_at_date;
    private String sales_person_name;

    public String getSales_person_name() {
        return sales_person_name;
    }

    public void setSales_person_name(String sales_person_name) {
        this.sales_person_name = sales_person_name;
    }

    public String getDone_at_date() {
        return done_at_date;
    }

    public void setDone_at_date(String done_at_date) {
        this.done_at_date = done_at_date;
    }

    public String getRemind_at_time_format1() {
        return remind_at_time_format1;
    }

    public void setRemind_at_time_format1(String remind_at_time_format1) {
        this.remind_at_time_format1 = remind_at_time_format1;
    }

    private String remind_at_date;

    public String getRemind_at_date() {
        return remind_at_date;
    }

    public void setRemind_at_date(String remind_at_date) {
        this.remind_at_date = remind_at_date;
    }

    public String getRemind_at_date_format() {
        return remind_at_date_format;
    }

    public void setRemind_at_date_format(String remind_at_date_format) {
        this.remind_at_date_format = remind_at_date_format;
    }

    public String getRemind_at_time_format() {
        return remind_at_time_format;
    }

    public void setRemind_at_time_format(String remind_at_time_format) {
        this.remind_at_time_format = remind_at_time_format;
    }

    public int getMark_as_done() {
        return mark_as_done;
    }

    public void setMark_as_done(int mark_as_done) {
        this.mark_as_done = mark_as_done;
    }

    public int getReminder_id() {
        return reminder_id;
    }

    public void setReminder_id(int reminder_id) {
        this.reminder_id = reminder_id;
    }

    public String getReminder_name() {
        return reminder_name;
    }

    public void setReminder_name(String reminder_name) {
        this.reminder_name = reminder_name;
    }

    public String getReminder_date() {
        return reminder_date;
    }

    public void setReminder_date(String reminder_date) {
        this.reminder_date = reminder_date;
    }

    public String getReminder_time() {
        return reminder_time;
    }

    public void setReminder_time(String reminder_time) {
        this.reminder_time = reminder_time;
    }

    public String getReminder_details() {
        return reminder_details;
    }

    public void setReminder_details(String reminder_details) {
        this.reminder_details = reminder_details;
    }


}
