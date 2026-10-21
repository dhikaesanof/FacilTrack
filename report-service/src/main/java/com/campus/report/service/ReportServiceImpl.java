package com.campus.report.service;

import com.campus.report.client.FacilityClient;
import com.campus.report.client.FacilityDto;
import com.campus.report.dto.*;
import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.BadRequestException;
import com.campus.report.exception.ResourceNotFoundException;
import com.campus.report.exception.UnprocessableEntityException;
import com.campus.report.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final FacilityClient facilityClient;

    public ReportServiceImpl(ReportRepository reportRepository, FacilityClient facilityClient) {
        this.reportRepository = reportRepository;
        this.facilityClient = facilityClient;
    }

    @Override
    public ReportResponse createReport(CreateReportRequest request) {
        log.info("Creating complaint report for facility: {}", request.getFacilityId());

        // Inter-service validation with facility-service
        FacilityDto facility = facilityClient.getFacilityById(request.getFacilityId());
        log.info("Facility validated successfully: {} (Status: {})", facility.getName(), facility.getOperationalStatus());

        ComplaintReport report = new ComplaintReport(
                request.getReporterId(),
                request.getFacilityId(),
                request.getCategory(),
                request.getDescription(),
                request.getPriority()
        );

        ComplaintReport saved = reportRepository.save(report);
        log.info("Created complaint report ID: {}", saved.getId());
        return ReportResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(String id) {
        log.info("Fetching complaint report ID: {}", id);
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));
        return ReportResponse.fromEntity(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(ReportStatus status, String category) {
        log.info("Listing complaint reports with filters - status: {}, category: {}", status, category);
        List<ComplaintReport> reports;

        if (status != null && category != null && !category.isBlank()) {
            reports = reportRepository.findByStatusAndCategory(status, category);
        } else if (status != null) {
            reports = reportRepository.findByStatus(status);
        } else if (category != null && !category.isBlank()) {
            reports = reportRepository.findByCategory(category);
        } else {
            reports = reportRepository.findAll();
        }

        return reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ReportResponse updateReport(String id, UpdateReportRequest request) {
        log.info("Updating complaint report ID: {}", id);
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new UnprocessableEntityException("Report cannot be updated because current status is "
                    + report.getStatus() + ". Only SUBMITTED reports can be modified.");
        }

        report.setCategory(request.getCategory());
        report.setDescription(request.getDescription());
        report.setPriority(request.getPriority());
        report.setUpdatedAt(Instant.now());

        ComplaintReport updated = reportRepository.save(report);
        return ReportResponse.fromEntity(updated);
    }

    @Override
    public ReportResponse patchReport(String id, PatchReportRequest request) {
        log.info("Patching complaint report ID: {}", id);
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new UnprocessableEntityException("Report cannot be patched because current status is "
                    + report.getStatus() + ". Only SUBMITTED reports can be modified.");
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            report.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            report.setPriority(request.getPriority());
        }
        report.setUpdatedAt(Instant.now());

        ComplaintReport updated = reportRepository.save(report);
        return ReportResponse.fromEntity(updated);
    }

    @Override
    public void deleteReport(String id) {
        log.info("Canceling complaint report ID: {}", id);
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new UnprocessableEntityException("Report cannot be cancelled because current status is "
                    + report.getStatus() + ". Only SUBMITTED reports can be cancelled.");
        }

        reportRepository.delete(report);
        log.info("Report ID: {} has been cancelled", id);
    }

    @Override
    public ReportResponse verifyReport(String id, VerifyReportRequest request) {
        log.info("Verifying complaint report ID: {} to status: {}", id, request.getStatus());
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new UnprocessableEntityException("Report cannot be verified because current status is "
                    + report.getStatus() + ". Only SUBMITTED reports can be verified or rejected.");
        }

        if (request.getStatus() != ReportStatus.VERIFIED && request.getStatus() != ReportStatus.REJECTED) {
            throw new BadRequestException("Verification target status must be VERIFIED or REJECTED");
        }

        report.setStatus(request.getStatus());
        report.setUpdatedAt(Instant.now());

        ComplaintReport updated = reportRepository.save(report);
        return ReportResponse.fromEntity(updated);
    }

    @Override
    public ReportResponse updateInternalStatus(String id, UpdateReportStatusRequest request) {
        log.info("Synchronizing internal status for report ID: {} to {}", id, request.getStatus());
        ComplaintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint report not found with ID: " + id));

        ReportStatus targetStatus = request.getStatus();
        ReportStatus currentStatus = report.getStatus();

        // Valid internal transitions triggered by maintenance assignments
        boolean isValidTransition = false;
        if (targetStatus == ReportStatus.ASSIGNED && (currentStatus == ReportStatus.VERIFIED || currentStatus == ReportStatus.SUBMITTED)) {
            isValidTransition = true;
        } else if (targetStatus == ReportStatus.IN_PROGRESS && (currentStatus == ReportStatus.ASSIGNED || currentStatus == ReportStatus.VERIFIED)) {
            isValidTransition = true;
        } else if (targetStatus == ReportStatus.RESOLVED && (currentStatus == ReportStatus.IN_PROGRESS || currentStatus == ReportStatus.ASSIGNED)) {
            isValidTransition = true;
        }

        if (!isValidTransition) {
            throw new UnprocessableEntityException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }

        report.setStatus(targetStatus);
        report.setUpdatedAt(Instant.now());

        ComplaintReport updated = reportRepository.save(report);
        return ReportResponse.fromEntity(updated);
    }
}
