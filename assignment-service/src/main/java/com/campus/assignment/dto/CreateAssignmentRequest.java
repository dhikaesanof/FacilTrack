package com.campus.assignment.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateAssignmentRequest {

    @NotBlank(message = "Report ID is required")
    private String reportId;

    @NotBlank(message = "Technician ID is required")
    private String technicianId;

    private String notes;

    public CreateAssignmentRequest() {
    }

    public CreateAssignmentRequest(String reportId, String technicianId, String notes) {
        this.reportId = reportId;
        this.technicianId = technicianId;
        this.notes = notes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
