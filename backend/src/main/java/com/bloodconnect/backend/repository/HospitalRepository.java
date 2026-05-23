package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.Hospital;
import com.bloodconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Hospital findByUserId(Long userId);

    Hospital findByUser(User user);

}