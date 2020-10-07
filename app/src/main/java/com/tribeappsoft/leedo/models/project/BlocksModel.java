package com.tribeappsoft.leedo.models.project;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 30/8/19.
 */
public class BlocksModel implements Serializable
{

    private int project_id;
    private String project_name;
    private String project_location;
    private String Total;
    private String Available;
    private String Reserved;
    private String OnHold;
    private String Sold;
    private int block_id;
    private String block_name;
    private int total_floor_count;
    private int total_units_count;
    private ArrayList<FloorModel> floorModelArrayList;
    private int isOpenForSale;



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

    public int getTotal_floor_count() {
        return total_floor_count;
    }

    public void setTotal_floor_count(int total_floor_count) {
        this.total_floor_count = total_floor_count;
    }

    public int getTotal_units_count() {
        return total_units_count;
    }

    public void setTotal_units_count(int total_units_count) {
        this.total_units_count = total_units_count;
    }

    public ArrayList<FloorModel> getFloorModelArrayList() {
        return floorModelArrayList;
    }

    public void setFloorModelArrayList(ArrayList<FloorModel> floorModelArrayList) {
        this.floorModelArrayList = floorModelArrayList;
    }



    public String getAvailable() {
        return Available;
    }

    public void setAvailable(String available) {
        Available = available;
    }

    public String getReserved() {
        return Reserved;
    }

    public void setReserved(String reserved) {
        Reserved = reserved;
    }

    public String getOnHold() {
        return OnHold;
    }

    public void setOnHold(String onHold) {
        OnHold = onHold;
    }

    public String getSold() {
        return Sold;
    }

    public void setSold(String sold) {
        Sold = sold;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public int getIsOpenForSale() {
        return isOpenForSale;
    }

    public void setIsOpenForSale(int isOpenForSale) {
        this.isOpenForSale = isOpenForSale;
    }

}
