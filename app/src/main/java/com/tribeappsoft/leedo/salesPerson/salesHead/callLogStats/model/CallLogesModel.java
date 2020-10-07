package com.tribeappsoft.leedo.salesPerson.salesHead.callLogStats.model;

public class CallLogesModel {
    public CallLogesModel() { }

    private String schedulesCalls;
    private String reschedulesCalls;
    private String completedCalls;
    private String cancelledCalls;

    public String getSchedulesCalls() {
        return schedulesCalls;
    }

    public void setSchedulesCalls(String schedulesCalls) {
        this.schedulesCalls = schedulesCalls;
    }

    public String getReschedulesCalls() {
        return reschedulesCalls;
    }

    public void setReschedulesCalls(String reschedulesCalls) {
        this.reschedulesCalls = reschedulesCalls;
    }

    public String getCompletedCalls() {
        return completedCalls;
    }

    public void setCompletedCalls(String completedCalls) {
        this.completedCalls = completedCalls;
    }

    public String getCancelledCalls() {
        return cancelledCalls;
    }

    public void setCancelledCalls(String cancelledCalls) {
        this.cancelledCalls = cancelledCalls;
    }
}
