package com.campus.assignment.controller;

import com.campus.assignment.dto.AssignmentResponse;
import com.campus.assignment.dto.CreateAssignmentRequest;
import com.campus.assignment.dto.UpdateAssignmentStatusRequest;
import com.campus.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Assignments", description = "Endpoints for technician dispatch, maintenance status, and repair history")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/api/v1/assignments")
    @Operation(summary = "Assign technician to report", description = "Assigns a technician to a complaint report and synchronizes status with report-service")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "422", description = "Report is in invalid status for assignment"),
            @ApiResponse(responseCode = "503", description = "Report service is unavailable")
    })
    public ResponseEntity<AssignmentResponse> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/assignments/{id}")
    @Operation(summary = "Retrieve assignment details", description = "Find maintenance assignment by unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignment found"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable String id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/v1/assignments/{id}/status")
    @Operation(summary = "Update assignment work status", description = "Update workStatus (IN_PROGRESS, RESOLVED). If RESOLVED, resolutionNote is strictly required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Work status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status requested"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "422", description = "Missing required resolutionNote when status is RESOLVED"),
            @ApiResponse(responseCode = "503", description = "Report service is unavailable")
    })
    public ResponseEntity<AssignmentResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAssignmentStatusRequest request) {
        AssignmentResponse response = assignmentService.updateAssignmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/reports/{reportId}/maintenance-history")
    @Operation(summary = "Get report maintenance history", description = "Retrieve full history and resolution records for a given report ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance history retrieved")
    })
    public ResponseEntity<List<AssignmentResponse>> getMaintenanceHistory(@PathVariable String reportId) {
        List<AssignmentResponse> history = assignmentService.getMaintenanceHistory(reportId);
        return ResponseEntity.ok(history);
    }
}
