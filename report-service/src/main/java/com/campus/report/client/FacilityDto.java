package com.campus.report.client;

import java.time.Instant;

public class FacilityDto {

    private String id;
    private String name;
    private String facilityType;
    private String building;
    private String room;
    private String operationalStatus;
    private Instant createdAt;

    public FacilityDto() {
    }

    public FacilityDto(String id, String name, String facilityType, String building, String room, String operationalStatus, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.facilityType = facilityType;
        this.building = building;
        this.room = room;
        this.operationalStatus = operationalStatus;
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.operationalStatus);
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

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
