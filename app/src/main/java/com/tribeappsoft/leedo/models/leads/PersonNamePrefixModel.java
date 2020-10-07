package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class PersonNamePrefixModel implements Serializable
{

    public int getName_prefix_id() {
        return name_prefix_id;
    }

    public void setName_prefix_id(int name_prefix_id) {
        this.name_prefix_id = name_prefix_id;
    }

    public String getName_prefix() {
        return name_prefix;
    }

    public void setName_prefix(String name_prefix) {
        this.name_prefix = name_prefix;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }


    private int name_prefix_id;
    private String name_prefix;
    private int status_id;

    public PersonNamePrefixModel()
    {

    }

}
