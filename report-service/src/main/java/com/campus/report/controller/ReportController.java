package com.campus.report.controller;

import com.campus.report.dto.*;
import com.campus.report.entity.ReportStatus;
import com.campus.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Endpoints for student/staff complaint reports and status lifecycles")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Create new complaint report", description = "Submits a new complaint and validates facility with facility-service")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "422", description = "Facility is inactive or not found"),
            @ApiResponse(responseCode = "503", description = "Facility service is unavailable")
    })
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List complaints", description = "List complaints with optional filters by status and category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reports")
    })
    public ResponseEntity<List<ReportResponse>> getReports(
            @Parameter(description = "Filter by report status")
            @RequestParam(required = false) ReportStatus status,
            @Parameter(description = "Filter by category (e.g. ELECTRICAL, PLUMBING)")
            @RequestParam(required = false) String category) {
        List<ReportResponse> reports = reportService.getReports(status, category);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint details", description = "Retrieve complaint report by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report found"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<ReportResponse> getReportById(@PathVariable String id) {
        ReportResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of complaint", description = "Full update of report details (allowed only in SUBMITTED state)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input payload"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Report is not in SUBMITTED state")
    })
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable String id,
            @Valid @RequestBody UpdateReportRequest request) {
        ReportResponse updated = reportService.updateReport(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update of complaint", description = "Update description or priority (allowed only in SUBMITTED state)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report patched successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Report is not in SUBMITTED state")
    })
    public ResponseEntity<ReportResponse> patchReport(
            @PathVariable String id,
            @RequestBody PatchReportRequest request) {
        ReportResponse patched = reportService.patchReport(id, request);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel report", description = "Cancel complaint report (allowed only when status is SUBMITTED)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Report is beyond SUBMITTED and cannot be cancelled")
    })
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verify complaint report", description = "Admin verifications: transitions status from SUBMITTED to VERIFIED or REJECTED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report status verified/rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid target status"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Report is not in SUBMITTED state")
    })
    public ResponseEntity<ReportResponse> verifyReport(
            @PathVariable String id,
            @Valid @RequestBody VerifyReportRequest request) {
        ReportResponse verified = reportService.verifyReport(id, request);
        return ResponseEntity.ok(verified);
    }
}
