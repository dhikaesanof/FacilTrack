package com.campus.assignment;

import com.campus.assignment.client.ReportClient;
import com.campus.assignment.dto.CreateAssignmentRequest;
import com.campus.assignment.dto.UpdateAssignmentStatusRequest;
import com.campus.assignment.entity.WorkStatus;
import com.campus.assignment.repository.AssignmentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssignmentLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @MockBean
    private ReportClient reportClient;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-End Assignment Lifecycle: Assign -> In Progress -> Resolve with validation -> History Check")
    void testAssignmentLifecycle() throws Exception {
        String reportId = "rep-e2e-500";
        String techId = "tech-99";

        // Step 1: Create assignment (starts at ASSIGNED, triggers report sync)
        CreateAssignmentRequest createRequest = new CreateAssignmentRequest(reportId, techId, "Urgent repair assigned");
        MvcResult createResult = mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.technicianId").value(techId))
                .andReturn();

        verify(reportClient, times(1)).syncReportStatus(reportId, "ASSIGNED");

        String assignmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
        assertThat(assignmentId).isNotBlank();

        // Step 2: Transition to IN_PROGRESS
        UpdateAssignmentStatusRequest inProgressRequest = new UpdateAssignmentStatusRequest(WorkStatus.IN_PROGRESS, "Diagnosing issue", null);
        mockMvc.perform(patch("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workStatus").value("IN_PROGRESS"));

        verify(reportClient, times(1)).syncReportStatus(reportId, "IN_PROGRESS");

        // Step 3: Transition to RESOLVED WITHOUT resolutionNote must fail with 422
        UpdateAssignmentStatusRequest invalidResolve = new UpdateAssignmentStatusRequest(WorkStatus.RESOLVED, "Done", "");
        mockMvc.perform(patch("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResolve)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        // Step 4: Transition to RESOLVED WITH resolutionNote must succeed
        UpdateAssignmentStatusRequest validResolve = new UpdateAssignmentStatusRequest(
                WorkStatus.RESOLVED, "Completed successfully", "Fixed pipe connector and pressurized line"
        );
        mockMvc.perform(patch("/api/v1/assignments/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResolve)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workStatus").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Fixed pipe connector and pressurized line"))
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

        verify(reportClient, times(1)).syncReportStatus(reportId, "RESOLVED");

        // Step 5: Query maintenance history for the report
        mockMvc.perform(get("/api/v1/reports/{reportId}/maintenance-history", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(assignmentId))
                .andExpect(jsonPath("$[0].reportId").value(reportId))
                .andExpect(jsonPath("$[0].workStatus").value("RESOLVED"))
                .andExpect(jsonPath("$[0].resolutionNote").value("Fixed pipe connector and pressurized line"));
    }
}
