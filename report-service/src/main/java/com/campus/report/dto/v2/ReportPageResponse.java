package com.campus.report.dto.v2;

import java.util.List;

public record ReportPageResponse(List<ReportV2Response> items, int page, int size,
                                 long totalItems, int totalPages) {
}
