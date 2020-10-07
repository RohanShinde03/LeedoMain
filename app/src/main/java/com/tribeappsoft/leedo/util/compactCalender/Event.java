package com.tribeappsoft.leedo.util.compactCalender;


import androidx.annotation.Nullable;

public class Event {

    private int color;
    private long timeInMillis;
    private Object data;
    private String event;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Event(int color, Object data, String event) {
        this.color = color;
        this.data = data;
        this.event = event;
    }

    public Event(int color, long timeInMillis) {
        this.color = color;
        this.timeInMillis = timeInMillis;
    }

    public Event(int color, long timeInMillis, Object data) {
        this.color = color;
        this.timeInMillis = timeInMillis;
        this.data = data;
    }

    public int getColor() {
        return color;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    @Nullable
    public Object getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (color != event.color) return false;
        if (timeInMillis != event.timeInMillis) return false;
        if (data != null ? !data.equals(event.data) : event.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = color;
        result = 31 * result + (int) (timeInMillis ^ (timeInMillis >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "color=" + color +
                ", timeInMillis=" + timeInMillis +
                ", data=" + data +
                '}';
    }
}
