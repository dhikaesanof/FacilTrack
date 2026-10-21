package com.campus.assignment.service;

import com.campus.assignment.client.ReportClient;
import com.campus.assignment.dto.AssignmentResponse;
import com.campus.assignment.dto.CreateAssignmentRequest;
import com.campus.assignment.dto.UpdateAssignmentStatusRequest;
import com.campus.assignment.entity.MaintenanceAssignment;
import com.campus.assignment.entity.WorkStatus;
import com.campus.assignment.exception.ResourceNotFoundException;
import com.campus.assignment.exception.UnprocessableEntityException;
import com.campus.assignment.repository.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ReportClient reportClient;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private MaintenanceAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testAssignment = new MaintenanceAssignment("rep-100", "tech-55", "Check the pipe joints");
        testAssignment.setId("asg-1");
        testAssignment.setWorkStatus(WorkStatus.ASSIGNED);
        testAssignment.setAssignedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create assignment and sync status ASSIGNED to report-service")
    void shouldCreateAssignmentSuccessfully() {
        CreateAssignmentRequest request = new CreateAssignmentRequest("rep-100", "tech-55", "Initial inspection");
        when(assignmentRepository.save(any(MaintenanceAssignment.class))).thenAnswer(inv -> {
            MaintenanceAssignment saved = inv.getArgument(0);
            saved.setId("asg-generated");
            return saved;
        });

        AssignmentResponse response = assignmentService.createAssignment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("asg-generated");
        assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.ASSIGNED);
        verify(reportClient).syncReportStatus("rep-100", "ASSIGNED");
    }

    @Test
    @DisplayName("Should update status to IN_PROGRESS and sync to report-service")
    void shouldUpdateStatusToInProgress() {
        when(assignmentRepository.findById("asg-1")).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(MaintenanceAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAssignmentStatusRequest updateRequest = new UpdateAssignmentStatusRequest(WorkStatus.IN_PROGRESS, "Work started", null);
        AssignmentResponse response = assignmentService.updateAssignmentStatus("asg-1", updateRequest);

        assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.IN_PROGRESS);
        assertThat(response.getNotes()).isEqualTo("Work started");
        verify(reportClient).syncReportStatus("rep-100", "IN_PROGRESS");
    }

    @Test
    @DisplayName("Should update status to RESOLVED with valid resolutionNote and set resolvedAt")
    void shouldUpdateStatusToResolvedWithNote() {
        when(assignmentRepository.findById("asg-1")).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(MaintenanceAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAssignmentStatusRequest updateRequest = new UpdateAssignmentStatusRequest(
                WorkStatus.RESOLVED, "Job finished", "Replaced worn rubber gasket"
        );
        AssignmentResponse response = assignmentService.updateAssignmentStatus("asg-1", updateRequest);

        assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.RESOLVED);
        assertThat(response.getResolutionNote()).isEqualTo("Replaced worn rubber gasket");
        assertThat(response.getResolvedAt()).isNotNull();
        verify(reportClient).syncReportStatus("rep-100", "RESOLVED");
    }

    @Test
    @DisplayName("Should reject transition to RESOLVED if resolutionNote is missing")
    void shouldRejectResolvedWithoutResolutionNote() {
        when(assignmentRepository.findById("asg-1")).thenReturn(Optional.of(testAssignment));

        UpdateAssignmentStatusRequest updateRequest = new UpdateAssignmentStatusRequest(
                WorkStatus.RESOLVED, "Job finished", null
        );

        assertThatThrownBy(() -> assignmentService.updateAssignmentStatus("asg-1", updateRequest))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Resolution note is strictly required");

        verify(reportClient, never()).syncReportStatus(any(), any());
    }

    @Test
    @DisplayName("Should retrieve maintenance history for report")
    void shouldRetrieveMaintenanceHistory() {
        when(assignmentRepository.findByReportIdOrderByAssignedAtDesc("rep-100"))
                .thenReturn(List.of(testAssignment));

        List<AssignmentResponse> history = assignmentService.getMaintenanceHistory("rep-100");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getReportId()).isEqualTo("rep-100");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when assignment not found")
    void shouldThrowWhenAssignmentNotFound() {
        when(assignmentRepository.findById("asg-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getAssignmentById("asg-unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Maintenance assignment not found with ID: asg-unknown");
    }
}
