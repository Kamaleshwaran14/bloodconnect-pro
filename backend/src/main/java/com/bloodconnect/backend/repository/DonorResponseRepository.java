package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.DonorResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {

    List<DonorResponse> findByBloodRequestId(Long requestId);

    List<DonorResponse> findByDonorId(Long donorId);

    // total responses
    long countByBloodRequestHospitalId(Long hospitalId);

    // get all responses for hospital
    List<DonorResponse> findByBloodRequestHospitalId(Long hospitalId);



}