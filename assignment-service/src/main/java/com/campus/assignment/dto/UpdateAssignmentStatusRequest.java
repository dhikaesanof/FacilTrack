package com.campus.assignment.dto;

import com.campus.assignment.entity.WorkStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAssignmentStatusRequest {

    @NotNull(message = "Work status is required (IN_PROGRESS or RESOLVED)")
    private WorkStatus workStatus;

    private String notes;

    private String resolutionNote;

    public UpdateAssignmentStatusRequest() {
    }

    public UpdateAssignmentStatusRequest(WorkStatus workStatus, String notes, String resolutionNote) {
        this.workStatus = workStatus;
        this.notes = notes;
        this.resolutionNote = resolutionNote;
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
}
