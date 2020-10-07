package com.tribeappsoft.leedo.admin.project_creation.model;

import java.io.Serializable;

public class ProjectTypeModel implements Serializable {
    public ProjectTypeModel() { }

    private int project_type_id;
    private String  project_type;

    public int getProject_type_id() {
        return project_type_id;
    }

    public void setProject_type_id(int project_type_id) {
        this.project_type_id = project_type_id;
    }

    public String getProject_type() {
        return project_type;
    }

    public void setProject_type(String project_type) {
        this.project_type = project_type;
    }
}
