package com.tribeappsoft.leedo.admin.users.model;

import java.util.ArrayList;

public class UserRoleModel  {

    public UserRoleModel() { }

    private int role_id;
    private String role_name;
    private int isSelected;

    public boolean isCheckedBox() {
        return isCheckedBox;
    }

    public void setCheckedBox(boolean checkedBox) {
        isCheckedBox = checkedBox;
    }

    private boolean isCheckedBox;

    public ArrayList<Integer> getAddedBlockIdsArrayList() {
        return addedBlockIdsArrayList;
    }

    public void setAddedBlockIdsArrayList(ArrayList<Integer> addedBlockIdsArrayList) {
        this.addedBlockIdsArrayList = addedBlockIdsArrayList;
    }

    public ArrayList<Integer> getRemovedBlockIdsArrayList() {
        return removedBlockIdsArrayList;
    }

    public void setRemovedBlockIdsArrayList(ArrayList<Integer> removedBlockIdsArrayList) {
        this.removedBlockIdsArrayList = removedBlockIdsArrayList;
    }

    private ArrayList<Integer> addedBlockIdsArrayList;
    private ArrayList<Integer> removedBlockIdsArrayList;

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
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
}
