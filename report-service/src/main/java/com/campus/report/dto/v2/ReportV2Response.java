package com.campus.report.dto.v2;

import com.campus.report.dto.ReportResponse;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
import java.time.Instant;

public record ReportV2Response(String id, String reporterId, String facilityId,
        ReportCategory category, String description, Priority priority, ReportStatus status,
        Instant createdAt, Instant updatedAt) {
    public static ReportV2Response from(ReportResponse report) {
        return new ReportV2Response(report.getId(), report.getReporterId(), report.getFacilityId(),
                ReportCategory.fromStored(report.getCategory()), report.getDescription(), report.getPriority(),
                report.getStatus(), report.getCreatedAt(), report.getUpdatedAt());
    }
}
