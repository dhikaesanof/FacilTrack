package com.campus.report.service;

import com.campus.report.client.FacilityClient;
import com.campus.report.client.FacilityDto;
import com.campus.report.dto.*;
import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.BadRequestException;
import com.campus.report.exception.ResourceNotFoundException;
import com.campus.report.exception.UnprocessableEntityException;
import com.campus.report.repository.ReportRepository;
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
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private FacilityClient facilityClient;

    @InjectMocks
    private ReportServiceImpl reportService;

    private ComplaintReport testReport;

    @BeforeEach
    void setUp() {
        testReport = new ComplaintReport("user-01", "fac-101", "PLUMBING", "Leaking pipe under sink", Priority.HIGH);
        testReport.setId("rep-101");
        testReport.setStatus(ReportStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should create complaint report when facility is valid and active")
    void shouldCreateReportSuccessfully() {
        CreateReportRequest request = new CreateReportRequest("user-01", "fac-101", "PLUMBING", "Leaking pipe", Priority.HIGH);
        FacilityDto activeFacility = new FacilityDto("fac-101", "Restroom B", "RESTROOM", "Main", "102", "ACTIVE", Instant.now());

        when(facilityClient.getFacilityById("fac-101")).thenReturn(activeFacility);
        when(reportRepository.save(any(ComplaintReport.class))).thenAnswer(inv -> {
            ComplaintReport r = inv.getArgument(0);
            r.setId("rep-new-1");
            return r;
        });

        ReportResponse response = reportService.createReport(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("rep-new-1");
        assertThat(response.getStatus()).isEqualTo(ReportStatus.SUBMITTED);
        verify(facilityClient).getFacilityById("fac-101");
        verify(reportRepository).save(any(ComplaintReport.class));
    }

    @Test
    @DisplayName("Should retrieve report by ID")
    void shouldGetReportById() {
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        ReportResponse response = reportService.getReportById("rep-101");

        assertThat(response.getId()).isEqualTo("rep-101");
        assertThat(response.getReporterId()).isEqualTo("user-01");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when report ID not found")
    void shouldThrowWhenReportNotFound() {
        when(reportRepository.findById("rep-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReportById("rep-unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Complaint report not found with ID: rep-unknown");
    }

    @Test
    @DisplayName("Should list reports filtered by status and category")
    void shouldListReportsWithFilters() {
        when(reportRepository.findByStatusAndCategory(ReportStatus.SUBMITTED, "PLUMBING"))
                .thenReturn(List.of(testReport));

        List<ReportResponse> result = reportService.getReports(ReportStatus.SUBMITTED, "PLUMBING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("PLUMBING");
    }

    @Test
    @DisplayName("Should update report when status is SUBMITTED")
    void shouldUpdateReportWhenSubmitted() {
        UpdateReportRequest updateRequest = new UpdateReportRequest("ELECTRICAL", "Flickering lights", Priority.CRITICAL);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(ComplaintReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportResponse response = reportService.updateReport("rep-101", updateRequest);

        assertThat(response.getCategory()).isEqualTo("ELECTRICAL");
        assertThat(response.getDescription()).isEqualTo("Flickering lights");
        assertThat(response.getPriority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    @DisplayName("Should reject update if report is NOT SUBMITTED")
    void shouldRejectUpdateWhenNotSubmitted() {
        testReport.setStatus(ReportStatus.VERIFIED);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        UpdateReportRequest updateRequest = new UpdateReportRequest("ELECTRICAL", "Flickering lights", Priority.CRITICAL);

        assertThatThrownBy(() -> reportService.updateReport("rep-101", updateRequest))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Only SUBMITTED reports can be modified");
    }

    @Test
    @DisplayName("Should patch report description and priority when status is SUBMITTED")
    void shouldPatchReportWhenSubmitted() {
        PatchReportRequest patchRequest = new PatchReportRequest("Updated pipe description", Priority.CRITICAL);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(ComplaintReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportResponse response = reportService.patchReport("rep-101", patchRequest);

        assertThat(response.getDescription()).isEqualTo("Updated pipe description");
        assertThat(response.getPriority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    @DisplayName("Should reject patch if report is NOT SUBMITTED")
    void shouldRejectPatchWhenNotSubmitted() {
        testReport.setStatus(ReportStatus.ASSIGNED);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        PatchReportRequest patchRequest = new PatchReportRequest("New desc", null);

        assertThatThrownBy(() -> reportService.patchReport("rep-101", patchRequest))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Only SUBMITTED reports can be modified");
    }

    @Test
    @DisplayName("Should cancel report when status is SUBMITTED")
    void shouldCancelReportWhenSubmitted() {
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        reportService.deleteReport("rep-101");

        verify(reportRepository).delete(testReport);
    }

    @Test
    @DisplayName("Should reject cancellation when status is VERIFIED or ASSIGNED")
    void shouldRejectCancellationWhenBeyondSubmitted() {
        testReport.setStatus(ReportStatus.VERIFIED);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        assertThatThrownBy(() -> reportService.deleteReport("rep-101"))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Only SUBMITTED reports can be cancelled");

        verify(reportRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should verify report transitions status to VERIFIED")
    void shouldVerifyReportSuccessfully() {
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(ComplaintReport.class))).thenAnswer(inv -> inv.getArgument(0));

        VerifyReportRequest verifyRequest = new VerifyReportRequest(ReportStatus.VERIFIED, "Confirmed issue");
        ReportResponse response = reportService.verifyReport("rep-101", verifyRequest);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.VERIFIED);
    }

    @Test
    @DisplayName("Should reject invalid verify target status")
    void shouldRejectInvalidVerifyTargetStatus() {
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        VerifyReportRequest verifyRequest = new VerifyReportRequest(ReportStatus.RESOLVED, "Invalid");

        assertThatThrownBy(() -> reportService.verifyReport("rep-101", verifyRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Verification target status must be VERIFIED or REJECTED");
    }

    @Test
    @DisplayName("Should synchronize internal status from VERIFIED to ASSIGNED")
    void shouldSyncInternalStatusToAssigned() {
        testReport.setStatus(ReportStatus.VERIFIED);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(ComplaintReport.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateReportStatusRequest syncRequest = new UpdateReportStatusRequest(ReportStatus.ASSIGNED);
        ReportResponse response = reportService.updateInternalStatus("rep-101", syncRequest);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should reject invalid internal status transition")
    void shouldRejectIllegalInternalStatusTransition() {
        testReport.setStatus(ReportStatus.REJECTED);
        when(reportRepository.findById("rep-101")).thenReturn(Optional.of(testReport));

        UpdateReportStatusRequest syncRequest = new UpdateReportStatusRequest(ReportStatus.RESOLVED);

        assertThatThrownBy(() -> reportService.updateInternalStatus("rep-101", syncRequest))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Invalid status transition from REJECTED to RESOLVED");
    }
}
