package com.campus.report.client;

import com.campus.report.exception.ServiceUnavailableException;
import com.campus.report.exception.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class FacilityClientTest {

    private MockRestServiceServer mockServer;
    private FacilityClient facilityClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8082");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        facilityClient = new FacilityClientImpl(restClient);
    }

    @Test
    @DisplayName("Should return facility when facility is found and ACTIVE")
    void shouldReturnActiveFacility() {
        String json = """
                {
                    "id": "fac-1",
                    "name": "Chemistry Lab",
                    "facilityType": "LAB",
                    "building": "Science Wing",
                    "room": "Room 101",
                    "operationalStatus": "ACTIVE"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8082/api/v1/facilities/fac-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        FacilityDto dto = facilityClient.getFacilityById("fac-1");

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("fac-1");
        assertThat(dto.isActive()).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when facility is INACTIVE")
    void shouldThrowWhenFacilityIsInactive() {
        String json = """
                {
                    "id": "fac-inactive",
                    "name": "Old Chemistry Lab",
                    "facilityType": "LAB",
                    "building": "Science Wing",
                    "room": "Room 101",
                    "operationalStatus": "INACTIVE"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8082/api/v1/facilities/fac-inactive"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> facilityClient.getFacilityById("fac-inactive"))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("is currently INACTIVE");

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when facility is not found (404)")
    void shouldThrowWhenFacilityNotFound() {
        mockServer.expect(requestTo("http://localhost:8082/api/v1/facilities/unknown-fac"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> facilityClient.getFacilityById("unknown-fac"))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("does not exist");

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when facility-service returns 500 error")
    void shouldThrowWhenFacilityServiceServerError() {
        mockServer.expect(requestTo("http://localhost:8082/api/v1/facilities/fac-err"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> facilityClient.getFacilityById("fac-err"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Facility service encountered an error");

        mockServer.verify();
    }
}
