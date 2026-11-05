package com.campus.report.dto.v2;

import com.campus.report.dto.UpdateReportRequest;
import com.campus.report.entity.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateReportV2Request(
        @NotBlank @Pattern(regexp = ReportCategory.INPUT_PATTERN)
        @Schema(implementation = ReportCategory.class) String category,
        @NotBlank String description,
        @NotNull Priority priority) {
    public UpdateReportRequest toV1Request() {
        return new UpdateReportRequest(category, description, priority);
    }
}
