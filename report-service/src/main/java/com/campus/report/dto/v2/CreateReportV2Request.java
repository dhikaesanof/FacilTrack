package com.campus.report.dto.v2;

import com.campus.report.dto.CreateReportRequest;
import com.campus.report.entity.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateReportV2Request(
        @NotBlank String reporterId,
        @NotBlank String facilityId,
        @NotBlank @Pattern(regexp = ReportCategory.INPUT_PATTERN)
        @Schema(implementation = ReportCategory.class) String category,
        @NotBlank String description,
        @NotNull Priority priority) {
    public CreateReportRequest toV1Request() {
        return new CreateReportRequest(reporterId, facilityId, category, description, priority);
    }
}
