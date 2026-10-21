package com.campus.report.client;

import com.campus.report.exception.ServiceUnavailableException;
import com.campus.report.exception.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class FacilityClientImpl implements FacilityClient {

    private static final Logger log = LoggerFactory.getLogger(FacilityClientImpl.class);

    private final RestClient facilityRestClient;

    public FacilityClientImpl(RestClient facilityRestClient) {
        this.facilityRestClient = facilityRestClient;
    }

    @Override
    public FacilityDto getFacilityById(String facilityId) {
        log.info("Calling facility-service to validate facility ID: {}", facilityId);
        try {
            FacilityDto facility = facilityRestClient.get()
                    .uri("/api/v1/facilities/{id}", facilityId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        throw new UnprocessableEntityException("Facility with ID '" + facilityId + "' does not exist");
                    })
                    .body(FacilityDto.class);

            if (facility == null) {
                throw new UnprocessableEntityException("Facility with ID '" + facilityId + "' does not exist");
            }

            if (!facility.isActive()) {
                throw new UnprocessableEntityException("Facility with ID '" + facilityId + "' is currently INACTIVE and cannot accept complaint reports");
            }

            return facility;
        } catch (UnprocessableEntityException e) {
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to facility-service: {}", e.getMessage());
            throw new ServiceUnavailableException("Facility service is currently unreachable or timed out", e);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is5xxServerError()) {
                log.error("Facility service returned 5xx error: {}", e.getStatusCode());
                throw new ServiceUnavailableException("Facility service encountered an error: " + e.getStatusCode(), e);
            }
            if (e.getStatusCode().value() == 404) {
                throw new UnprocessableEntityException("Facility with ID '" + facilityId + "' does not exist");
            }
            throw new ServiceUnavailableException("Unexpected response from facility service: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error while calling facility-service", e);
            throw new ServiceUnavailableException("Facility service is currently unavailable", e);
        }
    }
}
