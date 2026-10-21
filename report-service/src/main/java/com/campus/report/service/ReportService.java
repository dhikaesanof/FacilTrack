package com.campus.report.service;

import com.campus.report.dto.*;
import com.campus.report.entity.ReportStatus;

import java.util.List;

public interface ReportService {

    ReportResponse createReport(CreateReportRequest request);

    ReportResponse getReportById(String id);

    List<ReportResponse> getReports(ReportStatus status, String category);

    ReportResponse updateReport(String id, UpdateReportRequest request);

    ReportResponse patchReport(String id, PatchReportRequest request);

    void deleteReport(String id);

    ReportResponse verifyReport(String id, VerifyReportRequest request);

    ReportResponse updateInternalStatus(String id, UpdateReportStatusRequest request);
}
