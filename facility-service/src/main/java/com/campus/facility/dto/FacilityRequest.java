package com.campus.facility.dto;

import com.campus.facility.entity.OperationalStatus;
import jakarta.validation.constraints.NotBlank;

public class FacilityRequest {

    @NotBlank(message = "Facility name is required")
    private String name;

    @NotBlank(message = "Facility type is required")
    private String facilityType;

    @NotBlank(message = "Building is required")
    private String building;

    @NotBlank(message = "Room is required")
    private String room;

    private OperationalStatus operationalStatus = OperationalStatus.ACTIVE;

    public FacilityRequest() {
    }

    public FacilityRequest(String name, String facilityType, String building, String room, OperationalStatus operationalStatus) {
        this.name = name;
        this.facilityType = facilityType;
        this.building = building;
        this.room = room;
        this.operationalStatus = operationalStatus != null ? operationalStatus : OperationalStatus.ACTIVE;
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
