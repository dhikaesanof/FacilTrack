package com.campus.report.dto;

import com.campus.report.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateReportRequest {

    @NotBlank(message = "Reporter ID is required")
    private String reporterId;

    @NotBlank(message = "Facility ID is required")
    private String facilityId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    public CreateReportRequest() {
    }

    public CreateReportRequest(String reporterId, String facilityId, String category, String description, Priority priority) {
        this.reporterId = reporterId;
        this.facilityId = facilityId;
        this.category = category;
        this.description = description;
        this.priority = priority;
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
}
