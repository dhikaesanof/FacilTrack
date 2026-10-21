package com.campus.assignment.client;

public class UpdateReportStatusRequest {

    private String status;

    public UpdateReportStatusRequest() {
    }

    public UpdateReportStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
