package com.tribeappsoft.leedo.admin.callSchedule.model;

import java.io.Serializable;

public class CallScheduleLogsModel implements Serializable
{

    private String call_schedule_date;
    private int schedules_count;
    private int complete_count;

    public CallScheduleLogsModel() {

    }

    public String getCall_schedule_date() {
        return call_schedule_date;
    }

    public void setCall_schedule_date(String call_schedule_date) {
        this.call_schedule_date = call_schedule_date;
    }

    public int getSchedules_count() {
        return schedules_count;
    }

    public void setSchedules_count(int schedules_count) {
        this.schedules_count = schedules_count;
    }

    public int getComplete_count() {
        return complete_count;
    }

    public void setComplete_count(int complete_count) {
        this.complete_count = complete_count;
    }
}
