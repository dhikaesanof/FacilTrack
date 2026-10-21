package com.campus.facility.controller;

import com.campus.facility.dto.FacilityPatchRequest;
import com.campus.facility.dto.FacilityRequest;
import com.campus.facility.dto.FacilityResponse;
import com.campus.facility.entity.OperationalStatus;
import com.campus.facility.exception.ResourceNotFoundException;
import com.campus.facility.service.FacilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacilityController.class)
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FacilityService facilityService;

    @Test
    @DisplayName("GET /api/v1/facilities - should return 200 with list of facilities")
    void shouldReturnFacilitiesList() throws Exception {
        FacilityResponse response = new FacilityResponse(
                "fac-1", "Main Auditorium", "AUDITORIUM", "Main Hall", "Room 1",
                OperationalStatus.ACTIVE, Instant.now()
        );
        when(facilityService.getAllFacilities(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/facilities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("fac-1"))
                .andExpect(jsonPath("$[0].name").value("Main Auditorium"))
                .andExpect(jsonPath("$[0].operationalStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/facilities/{id} - should return 200 when facility found")
    void shouldReturnFacilityById() throws Exception {
        FacilityResponse response = new FacilityResponse(
                "fac-1", "Main Auditorium", "AUDITORIUM", "Main Hall", "Room 1",
                OperationalStatus.ACTIVE, Instant.now()
        );
        when(facilityService.getFacilityById("fac-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/facilities/{id}", "fac-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("fac-1"))
                .andExpect(jsonPath("$.name").value("Main Auditorium"));
    }

    @Test
    @DisplayName("GET /api/v1/facilities/{id} - should return 404 when facility not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(facilityService.getFacilityById("unknown-id"))
                .thenThrow(new ResourceNotFoundException("Facility not found with ID: unknown-id"));

        mockMvc.perform(get("/api/v1/facilities/{id}", "unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Facility not found with ID: unknown-id"))
                .andExpect(jsonPath("$.path").value("/api/v1/facilities/unknown-id"));
    }

    @Test
    @DisplayName("POST /api/v1/facilities - should return 201 when valid")
    void shouldCreateFacility() throws Exception {
        FacilityRequest request = new FacilityRequest("Computer Lab", "LAB", "Tech Wing", "Room 205", OperationalStatus.ACTIVE);
        FacilityResponse createdResponse = new FacilityResponse(
                "fac-100", "Computer Lab", "LAB", "Tech Wing", "Room 205",
                OperationalStatus.ACTIVE, Instant.now()
        );
        when(facilityService.createFacility(any(FacilityRequest.class))).thenReturn(createdResponse);

        mockMvc.perform(post("/api/v1/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("fac-100"))
                .andExpect(jsonPath("$.name").value("Computer Lab"));
    }

    @Test
    @DisplayName("POST /api/v1/facilities - should return 400 when missing required fields")
    void shouldReturn400WhenInvalidPayload() throws Exception {
        FacilityRequest invalidRequest = new FacilityRequest("", "", "", "", null);

        mockMvc.perform(post("/api/v1/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/facilities/{id} - should return 200 on update")
    void shouldUpdateFacility() throws Exception {
        FacilityRequest request = new FacilityRequest("Updated Lab", "LAB", "Tech Wing", "Room 206", OperationalStatus.ACTIVE);
        FacilityResponse updated = new FacilityResponse(
                "fac-100", "Updated Lab", "LAB", "Tech Wing", "Room 206",
                OperationalStatus.ACTIVE, Instant.now()
        );
        when(facilityService.updateFacility(eq("fac-100"), any(FacilityRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/facilities/{id}", "fac-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Lab"))
                .andExpect(jsonPath("$.room").value("Room 206"));
    }

    @Test
    @DisplayName("PATCH /api/v1/facilities/{id} - should return 200 on partial patch")
    void shouldPatchFacility() throws Exception {
        FacilityPatchRequest patch = new FacilityPatchRequest(null, null, null, null, OperationalStatus.INACTIVE);
        FacilityResponse patched = new FacilityResponse(
                "fac-100", "Updated Lab", "LAB", "Tech Wing", "Room 206",
                OperationalStatus.INACTIVE, Instant.now()
        );
        when(facilityService.patchFacility(eq("fac-100"), any(FacilityPatchRequest.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/v1/facilities/{id}", "fac-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationalStatus").value("INACTIVE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/facilities/{id} - should return 204 No Content")
    void shouldDeactivateFacility() throws Exception {
        doNothing().when(facilityService).deleteFacility("fac-100");

        mockMvc.perform(delete("/api/v1/facilities/{id}", "fac-100"))
                .andExpect(status().isNoContent());
    }
}
