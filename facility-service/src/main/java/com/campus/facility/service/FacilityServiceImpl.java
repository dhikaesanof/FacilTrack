package com.campus.facility.service;

import com.campus.facility.dto.FacilityPatchRequest;
import com.campus.facility.dto.FacilityRequest;
import com.campus.facility.dto.FacilityResponse;
import com.campus.facility.entity.Facility;
import com.campus.facility.entity.OperationalStatus;
import com.campus.facility.exception.ResourceNotFoundException;
import com.campus.facility.repository.FacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacilityServiceImpl implements FacilityService {

    private static final Logger log = LoggerFactory.getLogger(FacilityServiceImpl.class);

    private final FacilityRepository facilityRepository;

    public FacilityServiceImpl(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities(OperationalStatus status) {
        log.info("Fetching all facilities with filter status: {}", status);
        List<Facility> facilities = (status != null)
                ? facilityRepository.findByOperationalStatus(status)
                : facilityRepository.findAll();

        return facilities.stream()
                .map(FacilityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacilityResponse getFacilityById(String id) {
        log.info("Fetching facility with ID: {}", id);
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with ID: " + id));
        return FacilityResponse.fromEntity(facility);
    }

    @Override
    public FacilityResponse createFacility(FacilityRequest request) {
        log.info("Creating new facility: {}", request.getName());
        Facility facility = new Facility();
        facility.setName(request.getName());
        facility.setFacilityType(request.getFacilityType());
        facility.setBuilding(request.getBuilding());
        facility.setRoom(request.getRoom());
        facility.setOperationalStatus(request.getOperationalStatus() != null ? request.getOperationalStatus() : OperationalStatus.ACTIVE);
        facility.setCreatedAt(Instant.now());

        Facility saved = facilityRepository.save(facility);
        log.info("Created facility successfully with ID: {}", saved.getId());
        return FacilityResponse.fromEntity(saved);
    }

    @Override
    public FacilityResponse updateFacility(String id, FacilityRequest request) {
        log.info("Updating facility ID: {}", id);
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with ID: " + id));

        facility.setName(request.getName());
        facility.setFacilityType(request.getFacilityType());
        facility.setBuilding(request.getBuilding());
        facility.setRoom(request.getRoom());
        if (request.getOperationalStatus() != null) {
            facility.setOperationalStatus(request.getOperationalStatus());
        }

        Facility updated = facilityRepository.save(facility);
        return FacilityResponse.fromEntity(updated);
    }

    @Override
    public FacilityResponse patchFacility(String id, FacilityPatchRequest request) {
        log.info("Patching facility ID: {}", id);
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            facility.setName(request.getName());
        }
        if (request.getFacilityType() != null && !request.getFacilityType().isBlank()) {
            facility.setFacilityType(request.getFacilityType());
        }
        if (request.getBuilding() != null && !request.getBuilding().isBlank()) {
            facility.setBuilding(request.getBuilding());
        }
        if (request.getRoom() != null && !request.getRoom().isBlank()) {
            facility.setRoom(request.getRoom());
        }
        if (request.getOperationalStatus() != null) {
            facility.setOperationalStatus(request.getOperationalStatus());
        }

        Facility updated = facilityRepository.save(facility);
        return FacilityResponse.fromEntity(updated);
    }

    @Override
    public void deleteFacility(String id) {
        log.info("Deactivating (soft-deleting) facility ID: {}", id);
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with ID: " + id));

        facility.setOperationalStatus(OperationalStatus.INACTIVE);
        facilityRepository.save(facility);
    }
}
