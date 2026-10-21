package com.campus.assignment.service;

import com.campus.assignment.dto.AssignmentResponse;
import com.campus.assignment.dto.CreateAssignmentRequest;
import com.campus.assignment.dto.UpdateAssignmentStatusRequest;

import java.util.List;

public interface AssignmentService {

    AssignmentResponse createAssignment(CreateAssignmentRequest request);

    AssignmentResponse getAssignmentById(String id);

    AssignmentResponse updateAssignmentStatus(String id, UpdateAssignmentStatusRequest request);

    List<AssignmentResponse> getMaintenanceHistory(String reportId);
}
