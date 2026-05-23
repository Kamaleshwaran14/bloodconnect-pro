package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {


    List<BloodRequest> findByHospitalId(Long hospitalId);


    long countByHospitalIdAndStatus(Long hospitalId, String status);


    List<BloodRequest> findByBloodGroupAndLocationAndStatus(
            String bloodGroup,
            String location,
            String status
    );
}
