package com.campus.report.controller;

import com.campus.report.dto.PatchReportRequest;
import com.campus.report.dto.VerifyReportRequest;
import com.campus.report.dto.v2.*;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.BadRequestException;
import com.campus.report.service.ReportService;
import com.campus.report.service.ReportV2QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/reports")
@Tag(name = "Reports V2", description = "Paginated reports with exact official category input; legacy categories are mapped on reads")
@ApiResponse(responseCode = "400", description = "Invalid pagination, category, status or request body")
@ApiResponse(responseCode = "404", description = "Report not found")
@ApiResponse(responseCode = "422", description = "Existing facility or report lifecycle rule rejected the operation")
@ApiResponse(responseCode = "503", description = "Facility service unavailable")
public class ReportV2Controller {
    private final ReportService reports;
    private final ReportV2QueryService queries;

    public ReportV2Controller(ReportService reports, ReportV2QueryService queries) {
        this.reports = reports;
        this.queries = queries;
    }

    @GetMapping
    @Operation(summary = "List reports V2", description = "Filters run in the database before counting and pagination. Order: createdAt DESC, id ASC. Empty/out-of-range pages return 200. No matches: totalItems=0, totalPages=0.")
    public ReportPageResponse list(
            @RequestParam(required = false) ReportStatus status,
            @Parameter(schema = @Schema(implementation = ReportCategory.class))
            @RequestParam(required = false) String category,
            @Parameter(schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(required = false) String page,
            @Parameter(schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100"))
            @RequestParam(required = false) String size) {
        return queries.list(status, category == null ? null : ReportCategory.parseInput(category),
                integer(page, 0), integer(size, 10));
    }

    private int integer(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Pagination parameters must be integers");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report V2")
    public ReportV2Response detail(@PathVariable String id) {
        return ReportV2Response.from(reports.getReportById(id));
    }

    @PostMapping
    @Operation(summary = "Create report V2", description = "Validates facility through Facility Service; category must be an exact official code, never an alias.")
    @ApiResponse(responseCode = "201", description = "Report created")
    public ResponseEntity<ReportV2Response> create(@Valid @RequestBody CreateReportV2Request request) {
        return ResponseEntity.status(201).body(ReportV2Response.from(reports.createReport(request.toV1Request())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update report V2", description = "Only SUBMITTED reports; exact official category required.")
    public ReportV2Response update(@PathVariable String id, @Valid @RequestBody UpdateReportV2Request request) {
        return ReportV2Response.from(reports.updateReport(id, request.toV1Request()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch report V2", description = "Only description and priority, only while SUBMITTED. Does not update category.")
    public ReportV2Response patch(@PathVariable String id, @RequestBody PatchReportRequest request) {
        return ReportV2Response.from(reports.patchReport(id, request));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verify report V2", description = "SUBMITTED to VERIFIED or REJECTED, using existing lifecycle rules.")
    public ReportV2Response verify(@PathVariable String id, @Valid @RequestBody VerifyReportRequest request) {
        return ReportV2Response.from(reports.verifyReport(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel report V2", description = "Only SUBMITTED reports can be deleted.")
    @ApiResponse(responseCode = "204", description = "Report cancelled")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        reports.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
