package com.tribeappsoft.leedo.models.project;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 30/8/19.
 */
public class FloorModel implements Serializable
{


    private int Total;
    private int project_id;
    private String project_name;
    private String project_location;
    private int block_id;
    private String block_name;
    private int floor_id;
    private String floor_name;
    private int total_unit_count;
    private ArrayList<FlatUnitModel> flatUnitModelArrayList;
    private ArrayList<FlatUnitModel> availFlatUnitModelArrayList;
    private ArrayList<FlatUnitModel> holdFlatUnitModelArrayList;
    private ArrayList<FlatUnitModel> soldFlatUnitModelArrayList;
    private ArrayList<FlatUnitModel> reservedFlatUnitModelArrayList;

    public ArrayList<FlatUnitModel> getAvailFlatUnitModelArrayList() {
        return availFlatUnitModelArrayList;
    }

    public void setAvailFlatUnitModelArrayList(ArrayList<FlatUnitModel> availFlatUnitModelArrayList) {
        this.availFlatUnitModelArrayList = availFlatUnitModelArrayList;
    }

    public ArrayList<FlatUnitModel> getHoldFlatUnitModelArrayList() {
        return holdFlatUnitModelArrayList;
    }

    public void setHoldFlatUnitModelArrayList(ArrayList<FlatUnitModel> holdFlatUnitModelArrayList) {
        this.holdFlatUnitModelArrayList = holdFlatUnitModelArrayList;
    }

    public ArrayList<FlatUnitModel> getSoldFlatUnitModelArrayList() {
        return soldFlatUnitModelArrayList;
    }

    public void setSoldFlatUnitModelArrayList(ArrayList<FlatUnitModel> soldFlatUnitModelArrayList) {
        this.soldFlatUnitModelArrayList = soldFlatUnitModelArrayList;
    }

    public ArrayList<FlatUnitModel> getReservedFlatUnitModelArrayList() {
        return reservedFlatUnitModelArrayList;
    }

    public void setReservedFlatUnitModelArrayList(ArrayList<FlatUnitModel> reservedFlatUnitModelArrayList) {
        this.reservedFlatUnitModelArrayList = reservedFlatUnitModelArrayList;
    }

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

    public int getTotal_unit_count() {
        return total_unit_count;
    }

    public void setTotal_unit_count(int total_unit_count) {
        this.total_unit_count = total_unit_count;
    }

    public ArrayList<FlatUnitModel> getFlatUnitModelArrayList() {
        return flatUnitModelArrayList;
    }

    public void setFlatUnitModelArrayList(ArrayList<FlatUnitModel> flatUnitModelArrayList) {
        this.flatUnitModelArrayList = flatUnitModelArrayList;
    }

    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }





}
