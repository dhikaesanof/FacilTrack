package com.campus.report.dto;

import com.campus.report.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateReportRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    public UpdateReportRequest() {
    }

    public UpdateReportRequest(String category, String description, Priority priority) {
        this.category = category;
        this.description = description;
        this.priority = priority;
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
