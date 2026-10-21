package com.campus.assignment.repository;

import com.campus.assignment.entity.MaintenanceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<MaintenanceAssignment, String> {

    List<MaintenanceAssignment> findByReportIdOrderByAssignedAtDesc(String reportId);

    List<MaintenanceAssignment> findByReportId(String reportId);
}
