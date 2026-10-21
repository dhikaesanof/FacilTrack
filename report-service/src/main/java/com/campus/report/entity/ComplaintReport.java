package com.campus.report.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "complaint_reports")
public class ComplaintReport {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "reporter_id", nullable = false)
    private String reporterId;

    @Column(name = "facility_id", nullable = false)
    private String facilityId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ComplaintReport() {
    }

    public ComplaintReport(String reporterId, String facilityId, String category, String description, Priority priority) {
        this.id = UUID.randomUUID().toString();
        this.reporterId = reporterId;
        this.facilityId = facilityId;
        this.category = category;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = ReportStatus.SUBMITTED;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = ReportStatus.SUBMITTED;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplaintReport that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
