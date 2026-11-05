package com.campus.report;

import com.campus.report.client.FacilityClient;
import com.campus.report.client.FacilityDto;
import com.campus.report.dto.v2.ReportCategory;
import com.campus.report.entity.ComplaintReport;
import com.campus.report.entity.Priority;
import com.campus.report.entity.ReportStatus;
import com.campus.report.exception.ServiceUnavailableException;
import com.campus.report.exception.UnprocessableEntityException;
import com.campus.report.repository.ReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.time.Instant;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportV2IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ReportRepository repository;
    @MockBean FacilityClient facility;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        when(facility.getFacilityById("fac-1")).thenReturn(new FacilityDto(
                "fac-1", "Room", "CLASSROOM", "A", "1", "ACTIVE", Instant.now()));
    }

    private ResultActions body(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body)));
    }

    private Map<String, Object> createBody(String category) {
        return Map.of("reporterId", "student", "facilityId", "fac-1", "category", category,
                "description", "Broken", "priority", "HIGH");
    }

    private ComplaintReport seed(String id, String category, ReportStatus status, Instant time) {
        var report = new ComplaintReport("student", "fac-1", category, "Broken", Priority.HIGH);
        report.setId(id);
        report.setStatus(status);
        report.setCreatedAt(time);
        return repository.saveAndFlush(report);
    }

    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void defaultLastOutOfRangeAndEmptyPages() throws Exception {
        mvc.perform(get("/api/v2/reports")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10)).andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
        for (int i = 0; i < 25; i++) seed("r-" + i, "NETWORK", ReportStatus.SUBMITTED, Instant.now());
        mvc.perform(get("/api/v2/reports")).andExpect(jsonPath("$.items.length()").value(10))
                .andExpect(jsonPath("$.totalItems").value(25)).andExpect(jsonPath("$.totalPages").value(3));
        mvc.perform(get("/api/v2/reports?page=2&size=10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(5)).andExpect(jsonPath("$.size").value(10));
        mvc.perform(get("/api/v2/reports?page=999&size=10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.totalItems").value(25));
        mvc.perform(get("/api/v2/reports?page=2147483647&size=100")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/api/v2/reports?size=1")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=-1", "size=101", "page=abc", "size=1.5",
            "page=2147483648", "size=99999999999999", "page=", "size="})
    void invalidPagination(String query) throws Exception {
        mvc.perform(get("/api/v2/reports?" + query)).andExpect(status().isBadRequest());
    }

    @Test
    void filtersTotalsAndStableSortUseMappedCategories() throws Exception {
        Instant time = Instant.parse("2026-01-01T00:00:00Z");
        seed("b", " wifi ", ReportStatus.SUBMITTED, time);
        seed("a", "Wi-Fi", ReportStatus.SUBMITTED, time);
        seed("c", " INTERNET ", ReportStatus.SUBMITTED, time.minusSeconds(1));
        seed("d", "network", ReportStatus.VERIFIED, time.plusSeconds(1));
        seed("e", "unknown", ReportStatus.SUBMITTED, time);
        seed("f", "OTHER", ReportStatus.SUBMITTED, time);
        mvc.perform(get("/api/v2/reports?category=NETWORK&status=SUBMITTED&size=2"))
                .andExpect(jsonPath("$.items[0].id").value("a"))
                .andExpect(jsonPath("$.items[1].id").value("b"))
                .andExpect(jsonPath("$.items[0].category").value("NETWORK"))
                .andExpect(jsonPath("$.totalItems").value(3)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/v2/reports?category=NETWORK&status=SUBMITTED&size=2&page=1"))
                .andExpect(jsonPath("$.items[0].id").value("c")).andExpect(jsonPath("$.size").value(2));
        mvc.perform(get("/api/v2/reports?category=NETWORK"))
                .andExpect(jsonPath("$.items[0].id").value("d")).andExpect(jsonPath("$.totalItems").value(4));
        mvc.perform(get("/api/v2/reports?category=OTHER&size=1"))
                .andExpect(jsonPath("$.totalItems").value(2)).andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].category").value("OTHER"));
        mvc.perform(get("/api/v2/reports?status=VERIFIED"))
                .andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/v2/reports?category=HVAC"))
                .andExpect(jsonPath("$.totalItems").value(0)).andExpect(jsonPath("$.totalPages").value(0));
    }

    @ParameterizedTest
    @CsvSource({"WIFI,NETWORK", "WI-FI,NETWORK", "INTERNET,NETWORK", "LISTRIK,ELECTRICAL",
            "KELISTRIKAN,ELECTRICAL", "AC,HVAC", "AIR CONDITIONING,HVAC", "MEJA,FURNITURE",
            "KURSI,FURNITURE", "PROYEKTOR,EQUIPMENT", "PROJECTOR,EQUIPMENT", "unknown,OTHER",
            "WIFI broken,OTHER", "ELECTRICAL,ELECTRICAL", "PLUMBING,PLUMBING", "NETWORK,NETWORK",
            "HVAC,HVAC", "FURNITURE,FURNITURE", "EQUIPMENT,EQUIPMENT", "OTHER,OTHER"})
    void legacyMappingIsReadOnlyAndConsistentWithFilter(String raw, String expected) throws Exception {
        String stored = "  " + raw.toLowerCase(java.util.Locale.ROOT) + "  ";
        var before = seed("legacy", stored, ReportStatus.SUBMITTED, Instant.now());
        mvc.perform(get("/api/v2/reports/legacy")).andExpect(jsonPath("$.category").value(expected));
        mvc.perform(get("/api/v2/reports").param("category", expected))
                .andExpect(jsonPath("$.items[0].category").value(expected))
                .andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get("/api/v1/reports/legacy")).andExpect(jsonPath("$.category").value(stored));
        var after = repository.findById("legacy").orElseThrow();
        assertThat(after.getCategory()).isEqualTo(stored);
        assertThat(after.getUpdatedAt()).isEqualTo(before.getUpdatedAt());
    }

    @ParameterizedTest
    @EnumSource(ReportCategory.class)
    void allOfficialCategoriesAcceptedForCreateUpdateAndFilter(ReportCategory category) throws Exception {
        var created = read(body(post("/api/v2/reports"), createBody(category.name()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.category").value(category.name())));
        String id = created.get("id").asText();
        body(put("/api/v2/reports/{id}", id), Map.of("category", category.name(), "description", "Updated", "priority", "LOW"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value(category.name()));
        mvc.perform(get("/api/v2/reports").param("category", category.name()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(1));
        verify(facility).getFacilityById("fac-1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"WIFI", "electrical", " NETWORK", "NETWORK ", "", "unknown", "0"})
    void nonExactCategoryRejectedEverywhere(String category) throws Exception {
        body(post("/api/v2/reports"), createBody(category)).andExpect(status().isBadRequest());
        body(put("/api/v2/reports/missing"), Map.of("category", category, "description", "Updated", "priority", "LOW"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/reports").param("category", category)).andExpect(status().isBadRequest());
        verifyNoInteractions(facility);
    }

    @Test
    void v1FreeTextArrayAndV2MutationsPreserveBusinessRulesAndInternalSync() throws Exception {
        var created = read(body(post("/api/v1/reports"), createBody(" WIFI ")).andExpect(status().isCreated()));
        String id = created.get("id").asText();
        mvc.perform(get("/api/v1/reports")).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value(" WIFI "));
        body(patch("/api/v2/reports/{id}", id), Map.of("description", "Changed", "priority", "LOW", "category", "HVAC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("NETWORK"))
                .andExpect(jsonPath("$.description").value("Changed")).andExpect(jsonPath("$.priority").value("LOW"));
        assertThat(repository.findById(id).orElseThrow().getCategory()).isEqualTo(" WIFI ");
        body(patch("/api/v2/reports/{id}/verify", id), Map.of("status", "RESOLVED"))
                .andExpect(status().isBadRequest());
        body(patch("/api/v2/reports/{id}/verify", id), Map.of("status", "VERIFIED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("NETWORK"));
        body(put("/api/v2/reports/{id}", id), Map.of("category", "HVAC", "description", "No", "priority", "LOW"))
                .andExpect(status().isUnprocessableEntity());
        body(patch("/api/v2/reports/{id}", id), Map.of("description", "No"))
                .andExpect(status().isUnprocessableEntity());
        body(patch("/api/v2/reports/{id}/verify", id), Map.of("status", "REJECTED"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(delete("/api/v2/reports/{id}", id)).andExpect(status().isUnprocessableEntity());
        for (String state : new String[]{"ASSIGNED", "IN_PROGRESS", "RESOLVED"}) {
            body(patch("/api/v1/internal/reports/{id}/status", id), Map.of("status", state)).andExpect(status().isOk());
            mvc.perform(get("/api/v2/reports/{id}", id)).andExpect(jsonPath("$.status").value(state))
                    .andExpect(jsonPath("$.category").value("NETWORK"));
        }
        seed("cancel", "AC", ReportStatus.SUBMITTED, Instant.now());
        mvc.perform(delete("/api/v2/reports/cancel")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v2/reports/cancel")).andExpect(status().isNotFound());
        seed("reject", "AC", ReportStatus.SUBMITTED, Instant.now());
        body(patch("/api/v2/reports/reject/verify"), Map.of("status", "REJECTED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("HVAC"));
    }

    @Test
    void facilityErrorsAndRequiredFieldsRetainExistingContract() throws Exception {
        when(facility.getFacilityById("fac-1")).thenThrow(new UnprocessableEntityException("Inactive"));
        body(post("/api/v2/reports"), createBody("NETWORK")).andExpect(status().isUnprocessableEntity());
        doThrow(new ServiceUnavailableException("Unavailable")).when(facility).getFacilityById("fac-1");
        body(post("/api/v2/reports"), createBody("NETWORK")).andExpect(status().isServiceUnavailable());
        body(post("/api/v2/reports"), Map.of("category", "NETWORK")).andExpect(status().isBadRequest());
        body(put("/api/v2/reports/missing"), Map.of("description", "X", "priority", "LOW"))
                .andExpect(status().isBadRequest());
        body(put("/api/v2/reports/missing"), Map.of("category", "NETWORK", "description", "X", "priority", "LOW"))
                .andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void openApiIncludesBothVersionsAndExplicitPageSchema() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/reports']").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports']").exists())
                .andExpect(jsonPath("$.components.schemas.ReportPageResponse.properties.items").exists())
                .andExpect(jsonPath("$.components.schemas.ReportV2Response.properties.category").exists());
    }
}
