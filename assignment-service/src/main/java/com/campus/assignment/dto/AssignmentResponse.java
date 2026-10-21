package com.campus.assignment.dto;

import com.campus.assignment.entity.MaintenanceAssignment;
import com.campus.assignment.entity.WorkStatus;

import java.time.Instant;

public class AssignmentResponse {

    private String id;
    private String reportId;
    private String technicianId;
    private WorkStatus workStatus;
    private String notes;
    private String resolutionNote;
    private Instant assignedAt;
    private Instant resolvedAt;

    public AssignmentResponse() {
    }

    public AssignmentResponse(String id, String reportId, String technicianId, WorkStatus workStatus,
                              String notes, String resolutionNote, Instant assignedAt, Instant resolvedAt) {
        this.id = id;
        this.reportId = reportId;
        this.technicianId = technicianId;
        this.workStatus = workStatus;
        this.notes = notes;
        this.resolutionNote = resolutionNote;
        this.assignedAt = assignedAt;
        this.resolvedAt = resolvedAt;
    }

    public static AssignmentResponse fromEntity(MaintenanceAssignment entity) {
        if (entity == null) {
            return null;
        }
        return new AssignmentResponse(
                entity.getId(),
                entity.getReportId(),
                entity.getTechnicianId(),
                entity.getWorkStatus(),
                entity.getNotes(),
                entity.getResolutionNote(),
                entity.getAssignedAt(),
                entity.getResolvedAt()
        );
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
}
