package com.tribeappsoft.leedo.models;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 12/9/19.
 */
public class UserPermissionsModel implements Serializable
{

    public int getPermission_id() {
        return permission_id;
    }

    public void setPermission_id(int permission_id) {
        this.permission_id = permission_id;
    }

    public String getPermission_name() {
        return permission_name;
    }

    public void setPermission_name(String permission_name) {
        this.permission_name = permission_name;
    }

    private int permission_id;
    private String permission_name;

    public UserPermissionsModel()
    {

    }
}
