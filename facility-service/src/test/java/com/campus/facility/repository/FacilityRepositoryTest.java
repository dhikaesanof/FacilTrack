package com.campus.facility.repository;

import com.campus.facility.entity.Facility;
import com.campus.facility.entity.OperationalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FacilityRepositoryTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    @DisplayName("Should save and query facilities by operational status")
    void shouldFindFacilitiesByStatus() {
        Facility activeFac = new Facility("Lab A", "LAB", "Building 1", "101", OperationalStatus.ACTIVE);
        Facility inactiveFac = new Facility("Lab B", "LAB", "Building 2", "102", OperationalStatus.INACTIVE);

        facilityRepository.save(activeFac);
        facilityRepository.save(inactiveFac);

        List<Facility> activeList = facilityRepository.findByOperationalStatus(OperationalStatus.ACTIVE);
        List<Facility> inactiveList = facilityRepository.findByOperationalStatus(OperationalStatus.INACTIVE);

        assertThat(activeList).hasSize(1);
        assertThat(activeList.get(0).getName()).isEqualTo("Lab A");

        assertThat(inactiveList).hasSize(1);
        assertThat(inactiveList.get(0).getName()).isEqualTo("Lab B");
    }
}
