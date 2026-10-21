package com.campus.report.controller;

import com.campus.report.dto.ReportResponse;
import com.campus.report.dto.UpdateReportStatusRequest;
import com.campus.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/reports")
@Tag(name = "Internal Report Sync", description = "Internal service-to-service endpoints for status synchronization")
public class InternalReportController {

    private final ReportService reportService;

    public InternalReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Internal status sync", description = "Called by assignment-service to synchronize report status (ASSIGNED, IN_PROGRESS, RESOLVED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report status synchronized"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Illegal status transition")
    })
    public ResponseEntity<ReportResponse> updateInternalStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        ReportResponse response = reportService.updateInternalStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
