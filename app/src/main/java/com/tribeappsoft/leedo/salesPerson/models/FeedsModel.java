package com.tribeappsoft.leedo.salesPerson.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.tribeappsoft.leedo.admin.leads.model.CUIDModel;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by ${ROHAN} on 30/9/19.
 */
public class FeedsModel implements Serializable, Parcelable
{

    //TODO Lead Status id temps
    // 1. Unclaimed,  2. Claimed, 3. Assigned, 4. Own(Self)/ Lead Added/Generated  5.Site Visited, 6. Token Generated,
    // 7. Token/GHP cancelled  8. Hold flat,  9. Booked  10. booked cancelled  13. GHP  pending


    //TODO lead type
    // 1. Direct/CP  2.  R & L  3. General

    //TODO feed type id
    // 1. Own , 2. Other's


    private int feed_type_id;
    private int lead_id;
    private int lead_status_id;
    private int booking_id;
    private String tag;
    private String tag_date;
    private String tag_elapsed_time;
    private String small_header_title;
    private String main_title;
    private String description;
    private String status_text;
    private String status_sub_text;
    private String status_timestamp;
    private String call;
    private boolean isExpandedOwnView;
    private boolean isExpandedOthersView;
    private CUIDModel cuidModel;
    private ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList;
    private int unit_hold_release_id;
    private int unit_id;
    private String unit_name;
    private int block_id;
    private int floor_id;

    public FeedsModel() {

    }


    protected FeedsModel(Parcel in) {
        feed_type_id = in.readInt();
        lead_id = in.readInt();
        lead_status_id = in.readInt();
        booking_id = in.readInt();
        tag = in.readString();
        tag_date = in.readString();
        tag_elapsed_time = in.readString();
        small_header_title = in.readString();
        main_title = in.readString();
        description = in.readString();
        status_text = in.readString();
        status_sub_text = in.readString();
        status_timestamp = in.readString();
        call = in.readString();
        isExpandedOwnView = in.readByte() != 0;
        isExpandedOthersView = in.readByte() != 0;
        unit_hold_release_id = in.readInt();
        unit_id = in.readInt();
        unit_name = in.readString();
        block_id = in.readInt();
        floor_id = in.readInt();
    }

    public static final Creator<FeedsModel> CREATOR = new Creator<FeedsModel>() {
        @Override
        public FeedsModel createFromParcel(Parcel in) {
            return new FeedsModel(in);
        }

        @Override
        public FeedsModel[] newArray(int size) {
            return new FeedsModel[size];
        }
    };

    public CUIDModel getCuidModel() {
        return cuidModel;
    }

    public void setCuidModel(CUIDModel cuidModel) {
        this.cuidModel = cuidModel;
    }

    public int getFeed_type_id() {
        return feed_type_id;
    }

    public void setFeed_type_id(int feed_type_id) {
        this.feed_type_id = feed_type_id;
    }

    public int getLead_id() {
        return lead_id;
    }

    public void setLead_id(int lead_id) {
        this.lead_id = lead_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag_date() {
        return tag_date;
    }

    public void setTag_date(String tag_date) {
        this.tag_date = tag_date;
    }

    public String getTag_elapsed_time() {
        return tag_elapsed_time;
    }

    public void setTag_elapsed_time(String tag_elapsed_time) {
        this.tag_elapsed_time = tag_elapsed_time;
    }

    public String getSmall_header_title() {
        return small_header_title;
    }

    public void setSmall_header_title(String small_header_title) {
        this.small_header_title = small_header_title;
    }

    public String getMain_title() {
        return main_title;
    }

    public void setMain_title(String main_title) {
        this.main_title = main_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus_text() {
        return status_text;
    }

    public void setStatus_text(String status_text) {
        this.status_text = status_text;
    }

    public String getStatus_sub_text() {
        return status_sub_text;
    }

    public void setStatus_sub_text(String status_sub_text) {
        this.status_sub_text = status_sub_text;
    }

    public String getStatus_timestamp() {
        return status_timestamp;
    }

    public void setStatus_timestamp(String status_timestamp) {
        this.status_timestamp = status_timestamp;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public ArrayList<LeadDetailsTitleModel> getDetailsTitleModelArrayList() {
        return detailsTitleModelArrayList;
    }

    public void setDetailsTitleModelArrayList(ArrayList<LeadDetailsTitleModel> detailsTitleModelArrayList) {
        this.detailsTitleModelArrayList = detailsTitleModelArrayList;
    }

    public int getLead_status_id() {
        return lead_status_id;
    }

    public void setLead_status_id(int lead_status_id) {
        this.lead_status_id = lead_status_id;
    }

    public boolean isExpandedOwnView() {
        return isExpandedOwnView;
    }

    public void setExpandedOwnView(boolean expandedOwnView) {
        isExpandedOwnView = expandedOwnView;
    }

    public boolean isExpandedOthersView() {
        return isExpandedOthersView;
    }

    public void setExpandedOthersView(boolean expandedOthersView) {
        isExpandedOthersView = expandedOthersView;
    }

    public int getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }

    public int getUnit_hold_release_id() {
        return unit_hold_release_id;
    }

    public void setUnit_hold_release_id(int unit_hold_release_id) {
        this.unit_hold_release_id = unit_hold_release_id;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public int getBlock_id() {
        return block_id;
    }

    public void setBlock_id(int block_id) {
        this.block_id = block_id;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(feed_type_id);
        parcel.writeInt(lead_id);
        parcel.writeInt(lead_status_id);
        parcel.writeInt(booking_id);
        parcel.writeString(tag);
        parcel.writeString(tag_date);
        parcel.writeString(tag_elapsed_time);
        parcel.writeString(small_header_title);
        parcel.writeString(main_title);
        parcel.writeString(description);
        parcel.writeString(status_text);
        parcel.writeString(status_sub_text);
        parcel.writeString(status_timestamp);
        parcel.writeString(call);
        parcel.writeByte((byte) (isExpandedOwnView ? 1 : 0));
        parcel.writeByte((byte) (isExpandedOthersView ? 1 : 0));
        parcel.writeInt(unit_hold_release_id);
        parcel.writeInt(unit_id);
        parcel.writeString(unit_name);
        parcel.writeInt(block_id);
        parcel.writeInt(floor_id);
    }
}
