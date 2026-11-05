package com.campus.report.service;

import com.campus.report.dto.ReportResponse;
import com.campus.report.dto.v2.*;
import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.BadRequestException;
import com.campus.report.repository.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Service
public class ReportV2QueryService {
    private final ReportRepository repository;

    public ReportV2QueryService(ReportRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ReportPageResponse list(ReportStatus status, ReportCategory category, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("page must be >= 0 and size must be between 1 and 100");
        }
        Specification<ComplaintReport> filter = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (category != null) {
                var normalized = cb.upper(cb.trim(root.get("category")));
                // OTHER is the complement of every recognized non-OTHER value.
                var categoryPredicate = category == ReportCategory.OTHER
                        ? cb.not(normalized.in(Arrays.stream(ReportCategory.values())
                            .filter(value -> value != ReportCategory.OTHER)
                            .flatMap(value -> value.readValues().stream()).toList()))
                        : normalized.in(category.readValues());
                predicate = cb.and(predicate, categoryPredicate);
            }
            return predicate;
        };
        // JPA offsets are integers; larger offsets cannot address a row through this API.
        if ((long) page * size > Integer.MAX_VALUE) {
            long total = repository.count(filter);
            return new ReportPageResponse(java.util.List.of(), page, size, total,
                    (int) Math.ceil((double) total / size));
        }
        var result = repository.findAll(filter, PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))));
        return new ReportPageResponse(result.getContent().stream()
                .map(ReportResponse::fromEntity).map(ReportV2Response::from).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }
}
