package com.campus.assignment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "maintenance_assignments")
public class MaintenanceAssignment {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "technician_id", nullable = false)
    private String technicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    private WorkStatus workStatus;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public MaintenanceAssignment() {
    }

    public MaintenanceAssignment(String reportId, String technicianId, String notes) {
        this.id = UUID.randomUUID().toString();
        this.reportId = reportId;
        this.technicianId = technicianId;
        this.notes = notes;
        this.workStatus = WorkStatus.ASSIGNED;
        this.assignedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.workStatus == null) {
            this.workStatus = WorkStatus.ASSIGNED;
        }
        if (this.assignedAt == null) {
            this.assignedAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public WorkStatus getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(WorkStatus workStatus) {
        this.workStatus = workStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceAssignment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
