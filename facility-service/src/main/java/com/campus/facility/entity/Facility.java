package com.campus.facility.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "facility_type", nullable = false)
    private String facilityType;

    @Column(name = "building", nullable = false)
    private String building;

    @Column(name = "room", nullable = false)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false)
    private OperationalStatus operationalStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Facility() {
    }

    public Facility(String name, String facilityType, String building, String room, OperationalStatus operationalStatus) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.facilityType = facilityType;
        this.building = building;
        this.room = room;
        this.operationalStatus = operationalStatus != null ? operationalStatus : OperationalStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.operationalStatus == null) {
            this.operationalStatus = OperationalStatus.ACTIVE;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility facility)) return false;
        return Objects.equals(id, facility.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
