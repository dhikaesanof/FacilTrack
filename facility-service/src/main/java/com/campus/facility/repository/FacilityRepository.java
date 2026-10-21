package com.campus.facility.repository;

import com.campus.facility.entity.Facility;
import com.campus.facility.entity.OperationalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, String> {

    List<Facility> findByOperationalStatus(OperationalStatus operationalStatus);
}
