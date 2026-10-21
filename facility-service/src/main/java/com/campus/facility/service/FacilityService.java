package com.campus.facility.service;

import com.campus.facility.dto.FacilityPatchRequest;
import com.campus.facility.dto.FacilityRequest;
import com.campus.facility.dto.FacilityResponse;
import com.campus.facility.entity.OperationalStatus;

import java.util.List;

public interface FacilityService {

    List<FacilityResponse> getAllFacilities(OperationalStatus status);

    FacilityResponse getFacilityById(String id);

    FacilityResponse createFacility(FacilityRequest request);

    FacilityResponse updateFacility(String id, FacilityRequest request);

    FacilityResponse patchFacility(String id, FacilityPatchRequest request);

    void deleteFacility(String id);
}
