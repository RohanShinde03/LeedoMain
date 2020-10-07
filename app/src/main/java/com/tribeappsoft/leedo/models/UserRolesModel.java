package com.tribeappsoft.leedo.models;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 12/9/19.
 */
public class UserRolesModel implements Serializable
{

    public UserRolesModel()
    {

    }

    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    private int role_id;
    private String role_name;



}
