package com.campus.report.controller;

import com.campus.report.dto.ReportResponse;
import com.campus.report.dto.UpdateReportStatusRequest;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalReportController.class)
class InternalReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("PATCH /api/v1/internal/reports/{id}/status - should update status to ASSIGNED")
    void shouldUpdateInternalStatus() throws Exception {
        UpdateReportStatusRequest request = new UpdateReportStatusRequest(ReportStatus.ASSIGNED);
        ReportResponse response = new ReportResponse(
                "rep-101", "user-01", "fac-101", "PLUMBING", "Leakage",
                Priority.HIGH, ReportStatus.ASSIGNED, Instant.now(), Instant.now()
        );
        when(reportService.updateInternalStatus(eq("rep-101"), any(UpdateReportStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/internal/reports/{id}/status", "rep-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rep-101"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/internal/reports/{id}/status - should return 422 on illegal transition")
    void shouldReturn422OnIllegalTransition() throws Exception {
        UpdateReportStatusRequest request = new UpdateReportStatusRequest(ReportStatus.RESOLVED);
        when(reportService.updateInternalStatus(eq("rep-101"), any(UpdateReportStatusRequest.class)))
                .thenThrow(new UnprocessableEntityException("Invalid status transition from SUBMITTED to RESOLVED"));

        mockMvc.perform(patch("/api/v1/internal/reports/{id}/status", "rep-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Invalid status transition from SUBMITTED to RESOLVED"));
    }
}
