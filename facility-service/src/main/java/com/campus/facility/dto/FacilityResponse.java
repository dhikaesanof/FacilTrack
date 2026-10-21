package com.campus.facility.dto;

import com.campus.facility.entity.Facility;
import com.campus.facility.entity.OperationalStatus;

import java.time.Instant;

public class FacilityResponse {

    private String id;
    private String name;
    private String facilityType;
    private String building;
    private String room;
    private OperationalStatus operationalStatus;
    private Instant createdAt;

    public FacilityResponse() {
    }

    public FacilityResponse(String id, String name, String facilityType, String building, String room, OperationalStatus operationalStatus, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.facilityType = facilityType;
        this.building = building;
        this.room = room;
        this.operationalStatus = operationalStatus;
        this.createdAt = createdAt;
    }

    public static FacilityResponse fromEntity(Facility facility) {
        if (facility == null) {
            return null;
        }
        return new FacilityResponse(
                facility.getId(),
                facility.getName(),
                facility.getFacilityType(),
                facility.getBuilding(),
                facility.getRoom(),
                facility.getOperationalStatus(),
                facility.getCreatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
