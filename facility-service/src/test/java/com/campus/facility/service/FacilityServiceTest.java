package com.campus.facility.service;

import com.campus.facility.dto.FacilityPatchRequest;
import com.campus.facility.dto.FacilityRequest;
import com.campus.facility.dto.FacilityResponse;
import com.campus.facility.entity.Facility;
import com.campus.facility.entity.OperationalStatus;
import com.campus.facility.exception.ResourceNotFoundException;
import com.campus.facility.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityServiceImpl facilityService;

    private Facility testFacility;

    @BeforeEach
    void setUp() {
        testFacility = new Facility("Science Lab A", "LABORATORY", "Building 3", "Room 301", OperationalStatus.ACTIVE);
        testFacility.setId("fac-123");
        testFacility.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should retrieve all facilities without status filter")
    void shouldRetrieveAllFacilitiesWithoutFilter() {
        when(facilityRepository.findAll()).thenReturn(List.of(testFacility));

        List<FacilityResponse> result = facilityService.getAllFacilities(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Science Lab A");
        verify(facilityRepository).findAll();
        verify(facilityRepository, never()).findByOperationalStatus(any());
    }

    @Test
    @DisplayName("Should retrieve filtered facilities by operational status")
    void shouldRetrieveFilteredFacilitiesByStatus() {
        when(facilityRepository.findByOperationalStatus(OperationalStatus.ACTIVE)).thenReturn(List.of(testFacility));

        List<FacilityResponse> result = facilityService.getAllFacilities(OperationalStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOperationalStatus()).isEqualTo(OperationalStatus.ACTIVE);
        verify(facilityRepository).findByOperationalStatus(OperationalStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should retrieve facility by existing ID")
    void shouldRetrieveFacilityByExistingId() {
        when(facilityRepository.findById("fac-123")).thenReturn(Optional.of(testFacility));

        FacilityResponse response = facilityService.getFacilityById("fac-123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("fac-123");
        assertThat(response.getName()).isEqualTo("Science Lab A");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when facility ID not found")
    void shouldThrowExceptionWhenFacilityNotFound() {
        when(facilityRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getFacilityById("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facility not found with ID: non-existent");
    }

    @Test
    @DisplayName("Should create new facility with default status ACTIVE")
    void shouldCreateFacilitySuccessfully() {
        FacilityRequest request = new FacilityRequest("Library West", "STUDY_AREA", "Library Wing", "Room 101", null);

        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> {
            Facility saved = invocation.getArgument(0);
            saved.setId("fac-new-1");
            return saved;
        });

        FacilityResponse response = facilityService.createFacility(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("fac-new-1");
        assertThat(response.getName()).isEqualTo("Library West");
        assertThat(response.getOperationalStatus()).isEqualTo(OperationalStatus.ACTIVE);
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    @DisplayName("Should update existing facility")
    void shouldUpdateFacilitySuccessfully() {
        FacilityRequest updateRequest = new FacilityRequest("Updated Science Lab", "RESEARCH_LAB", "Building 3B", "Room 302", OperationalStatus.ACTIVE);
        when(facilityRepository.findById("fac-123")).thenReturn(Optional.of(testFacility));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacilityResponse response = facilityService.updateFacility("fac-123", updateRequest);

        assertThat(response.getName()).isEqualTo("Updated Science Lab");
        assertThat(response.getFacilityType()).isEqualTo("RESEARCH_LAB");
        assertThat(response.getBuilding()).isEqualTo("Building 3B");
    }

    @Test
    @DisplayName("Should partially patch facility details")
    void shouldPatchFacilitySuccessfully() {
        FacilityPatchRequest patchRequest = new FacilityPatchRequest(null, null, null, null, OperationalStatus.INACTIVE);
        when(facilityRepository.findById("fac-123")).thenReturn(Optional.of(testFacility));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacilityResponse response = facilityService.patchFacility("fac-123", patchRequest);

        assertThat(response.getOperationalStatus()).isEqualTo(OperationalStatus.INACTIVE);
        assertThat(response.getName()).isEqualTo("Science Lab A");
    }

    @Test
    @DisplayName("Should soft-delete facility by setting operational status to INACTIVE")
    void shouldDeactivateFacilityOnDelete() {
        when(facilityRepository.findById("fac-123")).thenReturn(Optional.of(testFacility));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facilityService.deleteFacility("fac-123");

        assertThat(testFacility.getOperationalStatus()).isEqualTo(OperationalStatus.INACTIVE);
        verify(facilityRepository).save(testFacility);
    }
}
