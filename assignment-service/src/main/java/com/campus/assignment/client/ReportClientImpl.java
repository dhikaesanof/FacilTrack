package com.campus.assignment.client;

import com.campus.assignment.exception.ResourceNotFoundException;
import com.campus.assignment.exception.ServiceUnavailableException;
import com.campus.assignment.exception.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ReportClientImpl implements ReportClient {

    private static final Logger log = LoggerFactory.getLogger(ReportClientImpl.class);

    private final RestClient reportRestClient;

    public ReportClientImpl(RestClient reportRestClient) {
        this.reportRestClient = reportRestClient;
    }

    @Override
    public void syncReportStatus(String reportId, String status) {
        log.info("Synchronizing report status: reportId={}, targetStatus={}", reportId, status);
        try {
            reportRestClient.patch()
                    .uri("/api/v1/internal/reports/{id}/status", reportId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateReportStatusRequest(status))
                    .retrieve()
                    .onStatus(s -> s.value() == 404, (req, resp) -> {
                        throw new ResourceNotFoundException("Target complaint report not found with ID: " + reportId);
                    })
                    .onStatus(s -> s.value() == 422, (req, resp) -> {
                        throw new UnprocessableEntityException("Report service rejected status transition to " + status + " for report " + reportId);
                    })
                    .toBodilessEntity();
            log.info("Successfully synchronized report ID: {} to status: {}", reportId, status);
        } catch (ResourceNotFoundException | UnprocessableEntityException e) {
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to report-service: {}", e.getMessage());
            throw new ServiceUnavailableException("Report service is currently unreachable or timed out", e);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is5xxServerError()) {
                log.error("Report service returned 5xx error: {}", e.getStatusCode());
                throw new ServiceUnavailableException("Report service encountered an error: " + e.getStatusCode(), e);
            }
            if (e.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Target complaint report not found with ID: " + reportId);
            }
            if (e.getStatusCode().value() == 422) {
                throw new UnprocessableEntityException("Report service rejected status transition to " + status);
            }
            throw new ServiceUnavailableException("Unexpected response from report service: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error during report status sync", e);
            throw new ServiceUnavailableException("Report service is currently unavailable", e);
        }
    }
}
