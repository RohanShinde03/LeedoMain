package com.tribeappsoft.leedo.models.project;

import com.tribeappsoft.leedo.admin.models.EventProjectDocsModel;
import com.tribeappsoft.leedo.salesPerson.models.AllottedFlatListModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.teamLead.model.TeamLeadStatsModel;
import com.tribeappsoft.leedo.admin.reports.teamStats.model.TeamStatsModel;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 30/8/19.
 */
public class ProjectModel implements Serializable
{

    private int project_id;
    private String project_name;
    private String project_location;
    private String project_creation_date;
    private int total_block_count;
    private int total_floor_count;
    private int total_units_count;
    private ArrayList<BlocksModel> blocksModelArrayList;
    private ArrayList<FlatUnitModel> flatUnitModelArrayList;
    private ArrayList<UnitCategoriesModel> categoriesModelArrayList;
    private ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList;
    private ArrayList<AllottedFlatListModel> allottedFlatListModelArrayList;
    private ArrayList<TeamStatsModel> teamStatsModelArrayList;
    private ArrayList<TeamLeadStatsModel> teamLeadStatsModelArrayList;
    private String totalLeads;
    private String totalSite_visits;
    private String totalGhp;
    private String totalGhp_plus;
    private String totalAllotments;
    private String cancel_booking;
    private boolean isExpandedView;
    private ArrayList<Integer> addedBlockIdsArrayList;
    private ArrayList<Integer> removedBlockIdsArrayList;



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

    public String getProject_creation_date() {
        return project_creation_date;
    }

    public void setProject_creation_date(String project_creation_date) {
        this.project_creation_date = project_creation_date;
    }

    public int getTotal_block_count() {
        return total_block_count;
    }

    public void setTotal_block_count(int total_block_count) {
        this.total_block_count = total_block_count;
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

    public ArrayList<BlocksModel> getBlocksModelArrayList() {
        return blocksModelArrayList;
    }

    public void setBlocksModelArrayList(ArrayList<BlocksModel> blocksModelArrayList) {
        this.blocksModelArrayList = blocksModelArrayList;
    }


    public ArrayList<FlatUnitModel> getFlatUnitModelArrayList() {
        return flatUnitModelArrayList;
    }

    public void setFlatUnitModelArrayList(ArrayList<FlatUnitModel> flatUnitModelArrayList) {
        this.flatUnitModelArrayList = flatUnitModelArrayList;
    }

    public ArrayList<UnitCategoriesModel> getCategoriesModelArrayList() {
        return categoriesModelArrayList;
    }

    public void setCategoriesModelArrayList(ArrayList<UnitCategoriesModel> categoriesModelArrayList) {
        this.categoriesModelArrayList = categoriesModelArrayList;
    }

    public ArrayList<EventProjectDocsModel> getEventProjectDocsModelArrayList() {
        return eventProjectDocsModelArrayList;
    }

    public void setEventProjectDocsModelArrayList(ArrayList<EventProjectDocsModel> eventProjectDocsModelArrayList) {
        this.eventProjectDocsModelArrayList = eventProjectDocsModelArrayList;
    }

    public ArrayList<TeamStatsModel> getTeamStatsModelArrayList() {
        return teamStatsModelArrayList;
    }

    public void setTeamStatsModelArrayList(ArrayList<TeamStatsModel> teamStatsModelArrayList) {
        this.teamStatsModelArrayList = teamStatsModelArrayList;
    }


    public String getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(String totalLeads) {
        this.totalLeads = totalLeads;
    }

    public String getTotalSite_visits() {
        return totalSite_visits;
    }

    public void setTotalSite_visits(String totalSite_visits) {
        this.totalSite_visits = totalSite_visits;
    }

    public String getTotalGhp() {
        return totalGhp;
    }

    public void setTotalGhp(String totalGhp) {
        this.totalGhp = totalGhp;
    }

    public String getTotalGhp_plus() {
        return totalGhp_plus;
    }

    public void setTotalGhp_plus(String totalGhp_plus) {
        this.totalGhp_plus = totalGhp_plus;
    }

    public String getTotalAllotments() {
        return totalAllotments;
    }

    public void setTotalAllotments(String totalAllotments) {
        this.totalAllotments = totalAllotments;
    }


    public ArrayList<TeamLeadStatsModel> getTeamLeadStatsModelArrayList() {
        return teamLeadStatsModelArrayList;
    }

    public void setTeamLeadStatsModelArrayList(ArrayList<TeamLeadStatsModel> teamLeadStatsModelArrayList) {
        this.teamLeadStatsModelArrayList = teamLeadStatsModelArrayList;
    }


    public boolean isExpandedView() {
        return isExpandedView;
    }

    public void setExpandedView(boolean expandedView) {
        isExpandedView = expandedView;
    }

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

    public ArrayList<AllottedFlatListModel> getAllottedFlatListModelArrayList() {
        return allottedFlatListModelArrayList;
    }

    public void setAllottedFlatListModelArrayList(ArrayList<AllottedFlatListModel> allottedFlatListModelArrayList) {
        this.allottedFlatListModelArrayList = allottedFlatListModelArrayList;
    }


    public String getCancel_booking() {
        return cancel_booking;
    }

    public void setCancel_booking(String cancel_booking) {
        this.cancel_booking = cancel_booking;
    }



}
