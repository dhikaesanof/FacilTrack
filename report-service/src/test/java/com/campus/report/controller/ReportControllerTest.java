package com.campus.report.controller;

import com.campus.report.dto.*;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.ResourceNotFoundException;
import com.campus.report.exception.ServiceUnavailableException;
import com.campus.report.exception.UnprocessableEntityException;
import com.campus.report.service.ReportService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("POST /api/v1/reports - should return 201 on successful report creation")
    void shouldCreateReportSuccessfully() throws Exception {
        CreateReportRequest request = new CreateReportRequest("user-01", "fac-101", "PLUMBING", "Leakage in lab", Priority.HIGH);
        ReportResponse response = new ReportResponse(
                "rep-101", "user-01", "fac-101", "PLUMBING", "Leakage in lab",
                Priority.HIGH, ReportStatus.SUBMITTED, Instant.now(), Instant.now()
        );
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rep-101"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @DisplayName("POST /api/v1/reports - should return 422 when facility is inactive or not found")
    void shouldReturn422WhenFacilityInactiveOrNotFound() throws Exception {
        CreateReportRequest request = new CreateReportRequest("user-01", "fac-inactive", "PLUMBING", "Leakage in lab", Priority.HIGH);
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new UnprocessableEntityException("Facility with ID 'fac-inactive' is currently INACTIVE"));

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Facility with ID 'fac-inactive' is currently INACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/reports - should return 503 when facility-service is unavailable")
    void shouldReturn503WhenFacilityServiceUnavailable() throws Exception {
        CreateReportRequest request = new CreateReportRequest("user-01", "fac-101", "PLUMBING", "Leakage in lab", Priority.HIGH);
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new ServiceUnavailableException("Facility service is currently unreachable or timed out"));

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Facility service is currently unreachable or timed out"));
    }

    @Test
    @DisplayName("POST /api/v1/reports - should return 400 on blank required fields")
    void shouldReturn400OnInvalidBody() throws Exception {
        CreateReportRequest badRequest = new CreateReportRequest("", "", "", "", null);

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports - should return list of reports")
    void shouldReturnReportsList() throws Exception {
        ReportResponse response = new ReportResponse(
                "rep-101", "user-01", "fac-101", "PLUMBING", "Leakage",
                Priority.HIGH, ReportStatus.SUBMITTED, Instant.now(), Instant.now()
        );
        when(reportService.getReports(null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rep-101"));
    }

    @Test
    @DisplayName("GET /api/v1/reports/{id} - should return 404 when report not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(reportService.getReportById("rep-nonexistent"))
                .thenThrow(new ResourceNotFoundException("Complaint report not found with ID: rep-nonexistent"));

        mockMvc.perform(get("/api/v1/reports/{id}", "rep-nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Complaint report not found with ID: rep-nonexistent"));
    }

    @Test
    @DisplayName("DELETE /api/v1/reports/{id} - should return 204 on cancel when SUBMITTED")
    void shouldCancelSubmittedReport() throws Exception {
        doNothing().when(reportService).deleteReport("rep-101");

        mockMvc.perform(delete("/api/v1/reports/{id}", "rep-101"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/reports/{id} - should return 422 if report is already VERIFIED/ASSIGNED")
    void shouldRejectCancelWhenBeyondSubmitted() throws Exception {
        doThrow(new UnprocessableEntityException("Report cannot be cancelled because current status is VERIFIED. Only SUBMITTED reports can be cancelled."))
                .when(reportService).deleteReport("rep-101");

        mockMvc.perform(delete("/api/v1/reports/{id}", "rep-101"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(containsString("Only SUBMITTED reports can be cancelled")));
    }

    @Test
    @DisplayName("PATCH /api/v1/reports/{id}/verify - should verify report")
    void shouldVerifyReport() throws Exception {
        VerifyReportRequest verifyRequest = new VerifyReportRequest(ReportStatus.VERIFIED, "Confirmed");
        ReportResponse response = new ReportResponse(
                "rep-101", "user-01", "fac-101", "PLUMBING", "Leakage",
                Priority.HIGH, ReportStatus.VERIFIED, Instant.now(), Instant.now()
        );
        when(reportService.verifyReport(eq("rep-101"), any(VerifyReportRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/reports/{id}/verify", "rep-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }
}
