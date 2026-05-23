package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.DonationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationScheduleRepository extends JpaRepository<DonationSchedule, Long> {

    List<DonationSchedule> findByDonorId(Long donorId);

    List<DonationSchedule> findByHospitalIdAndStatus(Long hospitalId, String status);

    List<DonationSchedule> findByBloodRequestId(Long bloodRequestId);

    // ✅ HOSPITAL (KEEP THIS ONLY ONCE)
    @Query("""
        SELECT COUNT(ds)
        FROM DonationSchedule ds
        JOIN ds.bloodRequest br
        WHERE br.hospital.id = :hospitalId
        AND LOWER(ds.status) = 'completed'
    """)
    long countCompletedDonations(@Param("hospitalId") Long hospitalId);


}