package com.campus.report.dto;

import com.campus.report.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class VerifyReportRequest {

    @NotNull(message = "Verification status is required (VERIFIED or REJECTED)")
    private ReportStatus status;

    private String reason;

    public VerifyReportRequest() {
    }

    public VerifyReportRequest(ReportStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
