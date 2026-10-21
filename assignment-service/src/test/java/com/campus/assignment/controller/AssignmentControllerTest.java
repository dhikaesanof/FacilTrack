package com.campus.assignment.controller;

import com.campus.assignment.dto.AssignmentResponse;
import com.campus.assignment.dto.CreateAssignmentRequest;
import com.campus.assignment.dto.UpdateAssignmentStatusRequest;
import com.campus.assignment.entity.WorkStatus;
import com.campus.assignment.exception.ResourceNotFoundException;
import com.campus.assignment.exception.UnprocessableEntityException;
import com.campus.assignment.service.AssignmentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssignmentService assignmentService;

    @Test
    @DisplayName("POST /api/v1/assignments - should return 201 when assignment created")
    void shouldCreateAssignment() throws Exception {
        CreateAssignmentRequest request = new CreateAssignmentRequest("rep-100", "tech-1", "Inspect valves");
        AssignmentResponse response = new AssignmentResponse(
                "asg-1", "rep-100", "tech-1", WorkStatus.ASSIGNED,
                "Inspect valves", null, Instant.now(), null
        );
        when(assignmentService.createAssignment(any(CreateAssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("asg-1"))
                .andExpect(jsonPath("$.workStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.reportId").value("rep-100"));
    }

    @Test
    @DisplayName("POST /api/v1/assignments - should return 400 on missing required fields")
    void shouldReturn400OnInvalidBody() throws Exception {
        CreateAssignmentRequest badRequest = new CreateAssignmentRequest("", "", "");

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/assignments/{id} - should return 200 when found")
    void shouldReturnAssignmentById() throws Exception {
        AssignmentResponse response = new AssignmentResponse(
                "asg-1", "rep-100", "tech-1", WorkStatus.ASSIGNED,
                "Inspect valves", null, Instant.now(), null
        );
        when(assignmentService.getAssignmentById("asg-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/assignments/{id}", "asg-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("asg-1"))
                .andExpect(jsonPath("$.technicianId").value("tech-1"));
    }

    @Test
    @DisplayName("GET /api/v1/assignments/{id} - should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(assignmentService.getAssignmentById("unknown"))
                .thenThrow(new ResourceNotFoundException("Maintenance assignment not found with ID: unknown"));

        mockMvc.perform(get("/api/v1/assignments/{id}", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Maintenance assignment not found with ID: unknown"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assignments/{id}/status - should update status to RESOLVED")
    void shouldUpdateStatusToResolved() throws Exception {
        UpdateAssignmentStatusRequest request = new UpdateAssignmentStatusRequest(
                WorkStatus.RESOLVED, "Fixed", "Replaced parts"
        );
        AssignmentResponse response = new AssignmentResponse(
                "asg-1", "rep-100", "tech-1", WorkStatus.RESOLVED,
                "Fixed", "Replaced parts", Instant.now(), Instant.now()
        );
        when(assignmentService.updateAssignmentStatus(eq("asg-1"), any(UpdateAssignmentStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/assignments/{id}/status", "asg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workStatus").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Replaced parts"));
    }

    @Test
    @DisplayName("PATCH /api/v1/assignments/{id}/status - should return 422 if RESOLVED has no resolutionNote")
    void shouldReturn422WhenMissingResolutionNote() throws Exception {
        UpdateAssignmentStatusRequest request = new UpdateAssignmentStatusRequest(WorkStatus.RESOLVED, "Done", null);
        when(assignmentService.updateAssignmentStatus(eq("asg-1"), any(UpdateAssignmentStatusRequest.class)))
                .thenThrow(new UnprocessableEntityException("Resolution note is strictly required when completing an assignment (status RESOLVED)"));

        mockMvc.perform(patch("/api/v1/assignments/{id}/status", "asg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Resolution note is strictly required when completing an assignment (status RESOLVED)"));
    }

    @Test
    @DisplayName("GET /api/v1/reports/{reportId}/maintenance-history - should return history")
    void shouldReturnMaintenanceHistory() throws Exception {
        AssignmentResponse response = new AssignmentResponse(
                "asg-1", "rep-100", "tech-1", WorkStatus.RESOLVED,
                "Done", "Fixed leak", Instant.now(), Instant.now()
        );
        when(assignmentService.getMaintenanceHistory("rep-100")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/reports/{reportId}/maintenance-history", "rep-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("asg-1"))
                .andExpect(jsonPath("$[0].reportId").value("rep-100"));
    }
}
