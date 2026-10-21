package com.campus.report.repository;

import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ComplaintReport, String> {

    List<ComplaintReport> findByStatus(ReportStatus status);

    List<ComplaintReport> findByCategory(String category);

    List<ComplaintReport> findByStatusAndCategory(ReportStatus status, String category);
}
