package com.campus.report;

import com.campus.report.client.FacilityClient;
import com.campus.report.client.FacilityDto;
import com.campus.report.dto.CreateReportRequest;
import com.campus.report.dto.UpdateReportStatusRequest;
import com.campus.report.dto.VerifyReportRequest;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
import com.campus.report.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportRepository reportRepository;

    @MockBean
    private FacilityClient facilityClient;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-End Report Lifecycle: Submit -> Verify -> Internal Sync (ASSIGNED -> IN_PROGRESS -> RESOLVED)")
    void testCompleteReportLifecycle() throws Exception {
        // Step 1: Mock facility validation
        String facilityId = "fac-auditorium-1";
        FacilityDto facility = new FacilityDto(facilityId, "Auditorium Main", "AUDITORIUM", "Admin Block", "101", "ACTIVE", Instant.now());
        when(facilityClient.getFacilityById(facilityId)).thenReturn(facility);

        // Step 2: Submit report (SUBMITTED)
        CreateReportRequest createRequest = new CreateReportRequest("student-44", facilityId, "HVAC", "AC unit leaking water", Priority.HIGH);
        MvcResult createResult = mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.category").value("HVAC"))
                .andReturn();

        String reportId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
        assertThat(reportId).isNotBlank();

        // Step 3: Admin verification (transitions to VERIFIED)
        VerifyReportRequest verifyRequest = new VerifyReportRequest(ReportStatus.VERIFIED, "Confirmed HVAC malfunction");
        mockMvc.perform(patch("/api/v1/reports/{id}/verify", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        // Step 4: Verify cancellation is rejected after verification
        mockMvc.perform(delete("/api/v1/reports/{id}", reportId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        // Step 5: Internal synchronization to ASSIGNED
        UpdateReportStatusRequest assignRequest = new UpdateReportStatusRequest(ReportStatus.ASSIGNED);
        mockMvc.perform(patch("/api/v1/internal/reports/{id}/status", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        // Step 6: Internal synchronization to IN_PROGRESS
        UpdateReportStatusRequest inProgressRequest = new UpdateReportStatusRequest(ReportStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/v1/internal/reports/{id}/status", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Step 7: Internal synchronization to RESOLVED
        UpdateReportStatusRequest resolvedRequest = new UpdateReportStatusRequest(ReportStatus.RESOLVED);
        mockMvc.perform(patch("/api/v1/internal/reports/{id}/status", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolvedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // Step 8: Confirm final report state via GET
        mockMvc.perform(get("/api/v1/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId))
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
