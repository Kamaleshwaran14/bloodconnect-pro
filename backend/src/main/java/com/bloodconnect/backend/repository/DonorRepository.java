package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.Donor;
import com.bloodconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    Donor findByUserId(Long userId);

    Donor findByUser(User user);

    List<Donor> findByBloodGroupAndLocationContainingIgnoreCaseAndAvailableTrue(
            String bloodGroup,
            String location
    );

}