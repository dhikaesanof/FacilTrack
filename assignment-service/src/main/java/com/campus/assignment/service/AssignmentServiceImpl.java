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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    private final AssignmentRepository assignmentRepository;
    private final ReportClient reportClient;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, ReportClient reportClient) {
        this.assignmentRepository = assignmentRepository;
        this.reportClient = reportClient;
    }

    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        log.info("Assigning technician {} to report {}", request.getTechnicianId(), request.getReportId());

        MaintenanceAssignment assignment = new MaintenanceAssignment(
                request.getReportId(),
                request.getTechnicianId(),
                request.getNotes()
        );
        assignment.setWorkStatus(WorkStatus.ASSIGNED);
        assignment.setAssignedAt(Instant.now());

        MaintenanceAssignment saved = assignmentRepository.save(assignment);

        // Inter-service synchronization: notify report-service
        reportClient.syncReportStatus(request.getReportId(), WorkStatus.ASSIGNED.name());

        log.info("Created assignment ID: {}", saved.getId());
        return AssignmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(String id) {
        log.info("Fetching assignment ID: {}", id);
        MaintenanceAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance assignment not found with ID: " + id));
        return AssignmentResponse.fromEntity(assignment);
    }

    @Override
    public AssignmentResponse updateAssignmentStatus(String id, UpdateAssignmentStatusRequest request) {
        log.info("Updating assignment status: id={}, targetStatus={}", id, request.getWorkStatus());
        MaintenanceAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance assignment not found with ID: " + id));

        WorkStatus targetStatus = request.getWorkStatus();

        if (targetStatus == WorkStatus.RESOLVED) {
            if (request.getResolutionNote() == null || request.getResolutionNote().isBlank()) {
                throw new UnprocessableEntityException("Resolution note is strictly required when completing an assignment (status RESOLVED)");
            }
            assignment.setResolutionNote(request.getResolutionNote());
            assignment.setResolvedAt(Instant.now());
        }

        if (request.getNotes() != null) {
            assignment.setNotes(request.getNotes());
        }

        assignment.setWorkStatus(targetStatus);
        MaintenanceUpdated(assignment);

        MaintenanceAssignment updated = assignmentRepository.save(assignment);

        // Inter-service synchronization: notify report-service
        reportClient.syncReportStatus(assignment.getReportId(), targetStatus.name());

        return AssignmentResponse.fromEntity(updated);
    }

    private void MaintenanceUpdated(MaintenanceAssignment assignment) {
        // hook for internal audit or metrics if needed
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMaintenanceHistory(String reportId) {
        log.info("Fetching maintenance history for report ID: {}", reportId);
        List<MaintenanceAssignment> history = assignmentRepository.findByReportIdOrderByAssignedAtDesc(reportId);
        return history.stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
