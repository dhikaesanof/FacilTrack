package com.campus.report.dto;

import com.campus.report.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateReportStatusRequest {

    @NotNull(message = "Report status is required")
    private ReportStatus status;

    public UpdateReportStatusRequest() {
    }

    public UpdateReportStatusRequest(ReportStatus status) {
        this.status = status;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
