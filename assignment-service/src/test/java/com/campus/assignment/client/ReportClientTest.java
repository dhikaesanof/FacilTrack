package com.campus.assignment.client;

import com.campus.assignment.exception.ResourceNotFoundException;
import com.campus.assignment.exception.ServiceUnavailableException;
import com.campus.assignment.exception.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ReportClientTest {

    private MockRestServiceServer mockServer;
    private ReportClient reportClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        reportClient = new ReportClientImpl(restClient);
    }

    @Test
    @DisplayName("Should successfully synchronize report status to ASSIGNED")
    void shouldSyncStatusSuccessfully() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/internal/reports/rep-1/status"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatCode(() -> reportClient.syncReportStatus("rep-1", "ASSIGNED"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when report ID not found (404)")
    void shouldThrowWhenReportNotFound() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/internal/reports/unknown/status"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> reportClient.syncReportStatus("unknown", "ASSIGNED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Target complaint report not found");

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityException when report-service rejects transition (422)")
    void shouldThrowWhenReportRejectsTransition() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/internal/reports/rep-1/status"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> reportClient.syncReportStatus("rep-1", "RESOLVED"))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("Report service rejected status transition");

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when report-service returns 500 or is down")
    void shouldThrowWhenReportServiceFails() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/internal/reports/rep-1/status"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withServerError());

        assertThatThrownBy(() -> reportClient.syncReportStatus("rep-1", "IN_PROGRESS"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Report service encountered an error");

        mockServer.verify();
    }
}
