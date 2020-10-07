package com.tribeappsoft.leedo.models.project;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 30/8/19.
 */
public class FlatUnitModel implements Serializable
{

    private int project_id;
    private String project_name;
    private String project_location;
    private int block_id;
    private String block_name;
    private int floor_id;
    private String floor_name;
    private int flat_id;
    private String flat_name;
    private String flat_type;
    private int inventory_status_id;
    private int isSelected;


    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_location() {
        return project_location;
    }

    public void setProject_location(String project_location) {
        this.project_location = project_location;
    }

    public int getBlock_id() {
        return block_id;
    }

    public void setBlock_id(int block_id) {
        this.block_id = block_id;
    }

    public String getBlock_name() {
        return block_name;
    }

    public void setBlock_name(String block_name) {
        this.block_name = block_name;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public String getFloor_name() {
        return floor_name;
    }

    public void setFloor_name(String floor_name) {
        this.floor_name = floor_name;
    }

    public int getFlat_id() {
        return flat_id;
    }

    public void setFlat_id(int flat_id) {
        this.flat_id = flat_id;
    }

    public String getFlat_name() {
        return flat_name;
    }

    public void setFlat_name(String flat_name) {
        this.flat_name = flat_name;
    }

    public String getFlat_type() {
        return flat_type;
    }

    public void setFlat_type(String flat_type) {
        this.flat_type = flat_type;
    }

    public int getInventory_status_id() {
        return inventory_status_id;
    }

    public void setInventory_status_id(int inventory_status_id) {
        this.inventory_status_id = inventory_status_id;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }


}
