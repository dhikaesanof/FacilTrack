package com.campus.facility.controller;

import com.campus.facility.dto.FacilityPatchRequest;
import com.campus.facility.dto.FacilityRequest;
import com.campus.facility.dto.FacilityResponse;
import com.campus.facility.entity.OperationalStatus;
import com.campus.facility.service.FacilityService;
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
@RequestMapping("/api/v1/facilities")
@Tag(name = "Facilities", description = "Endpoints for managing campus facilities and buildings")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all facilities", description = "Retrieve all facilities with optional operational status filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved facilities")
    })
    public ResponseEntity<List<FacilityResponse>> getAllFacilities(
            @Parameter(description = "Filter by operational status (ACTIVE, INACTIVE)")
            @RequestParam(required = false) OperationalStatus status) {
        List<FacilityResponse> facilities = facilityService.getAllFacilities(status);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve single facility details", description = "Find facility by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility found"),
            @ApiResponse(responseCode = "404", description = "Facility not found")
    })
    public ResponseEntity<FacilityResponse> getFacilityById(@PathVariable String id) {
        FacilityResponse facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    @PostMapping
    @Operation(summary = "Register new facility", description = "Register a new campus facility (defaults to ACTIVE status)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Facility successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<FacilityResponse> createFacility(@Valid @RequestBody FacilityRequest request) {
        FacilityResponse created = facilityService.createFacility(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of facility details", description = "Replace all facility attributes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Facility not found")
    })
    public ResponseEntity<FacilityResponse> updateFacility(
            @PathVariable String id,
            @Valid @RequestBody FacilityRequest request) {
        FacilityResponse updated = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update of facility", description = "Partially update facility attributes or operational status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility successfully patched"),
            @ApiResponse(responseCode = "404", description = "Facility not found")
    })
    public ResponseEntity<FacilityResponse> patchFacility(
            @PathVariable String id,
            @RequestBody FacilityPatchRequest request) {
        FacilityResponse updated = facilityService.patchFacility(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/soft-delete facility", description = "Marks a facility as INACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Facility deactivated"),
            @ApiResponse(responseCode = "404", description = "Facility not found")
    })
    public ResponseEntity<Void> deleteFacility(@PathVariable String id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
