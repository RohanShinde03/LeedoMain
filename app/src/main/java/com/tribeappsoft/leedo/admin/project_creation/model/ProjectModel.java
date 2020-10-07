package com.tribeappsoft.leedo.admin.project_creation.model;
import java.io.Serializable;

public class ProjectModel implements Serializable
{
    public ProjectModel() {
    }

    private int project_id;
    private String created_at;
    private String updated_at;
    private String project_name;
    private int company_id;
    private String project_type;
    private int project_type_id;
    private String address;
    private String description;
    private String reg_no;
    private String cs_no;
    private String permission_date;
    private String end_date;
    private int status_id;
    private String latitude;
    private String longitude;
    private boolean isExpandedOwnView;
    private boolean isCheckedBox;
    private int is_project_assigned;

    public int getIs_project_assigned() {
        return is_project_assigned;
    }

    public void setIs_project_assigned(int is_project_assigned) {
        this.is_project_assigned = is_project_assigned;
    }



    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }
    public int getProject_type_id() {
        return project_type_id;
    }

    public void setProject_type_id(int project_type_id) {
        this.project_type_id = project_type_id;
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

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getProject_type() {
        return project_type;
    }

    public void setProject_type(String project_type) {
        this.project_type = project_type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReg_no() {
        return reg_no;
    }

    public void setReg_no(String reg_no) {
        this.reg_no = reg_no;
    }

    public String getCs_no() {
        return cs_no;
    }

    public void setCs_no(String cs_no) {
        this.cs_no = cs_no;
    }

    public String getPermission_date() {
        return permission_date;
    }

    public void setPermission_date(String permission_date) {
        this.permission_date = permission_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    public boolean isCheckedBox() {
        return isCheckedBox;
    }

    public void setCheckedBox(boolean checkedBox) {
        isCheckedBox = checkedBox;
    }
}
