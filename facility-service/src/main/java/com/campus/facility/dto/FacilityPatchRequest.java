package com.campus.facility.dto;

import com.campus.facility.entity.OperationalStatus;

public class FacilityPatchRequest {

    private String name;
    private String facilityType;
    private String building;
    private String room;
    private OperationalStatus operationalStatus;

    public FacilityPatchRequest() {
    }

    public FacilityPatchRequest(String name, String facilityType, String building, String room, OperationalStatus operationalStatus) {
        this.name = name;
        this.facilityType = facilityType;
        this.building = building;
        this.room = room;
        this.operationalStatus = operationalStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(OperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}
