package com.campus.report.dto;

import com.campus.report.entity.Priority;

public class PatchReportRequest {

    private String description;
    private Priority priority;

    public PatchReportRequest() {
    }

    public PatchReportRequest(String description, Priority priority) {
        this.description = description;
        this.priority = priority;
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
