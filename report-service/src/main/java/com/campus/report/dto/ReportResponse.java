package com.campus.report.dto;

import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;

import java.time.Instant;

public class ReportResponse {

    private String id;
    private String reporterId;
    private String facilityId;
    private String category;
    private String description;
    private Priority priority;
    private ReportStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public ReportResponse() {
    }

    public ReportResponse(String id, String reporterId, String facilityId, String category,
                          String description, Priority priority, ReportStatus status,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.facilityId = facilityId;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReportResponse fromEntity(ComplaintReport report) {
        if (report == null) {
            return null;
        }
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getFacilityId(),
                report.getCategory(),
                report.getDescription(),
                report.getPriority(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
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
}
