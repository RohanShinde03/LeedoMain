package com.tribeappsoft.leedo.models;




/*
 * Created by ${ROHAN} on 22-May-19.
 */


public class NotificationModel
{

    private int notification_id;
    private String created_at;
    private String updated_at;
    private int student_id;
    private int student_type;
    private int staff_id;
    private int staff_type;
    private String title;
    private String content;
    private String page;
    private String data;
    private int status_id;
    private String picture;
    private String date;


    public NotificationModel()
    {

    }


    public int getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(int notification_id) {
        this.notification_id = notification_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getStudent_type() {
        return student_type;
    }

    public void setStudent_type(int student_type) {
        this.student_type = student_type;
    }

    public int getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(int staff_id) {
        this.staff_id = staff_id;
    }

    public int getStaff_type() {
        return staff_type;
    }

    public void setStaff_type(int staff_type) {
        this.staff_type = staff_type;
    }


}
