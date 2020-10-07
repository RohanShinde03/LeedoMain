package com.tribeappsoft.leedo.models.project;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class UnitCategoriesModel implements Serializable
{

    public int getUnit_category_id() {
        return unit_category_id;
    }

    public void setUnit_category_id(int unit_category_id) {
        this.unit_category_id = unit_category_id;
    }

    public String getUnit_category() {
        return unit_category;
    }

    public void setUnit_category(String unit_category) {
        this.unit_category = unit_category;
    }


    public UnitCategoriesModel()
    {
    }

    private int unit_category_id;
    private String unit_category;



}
