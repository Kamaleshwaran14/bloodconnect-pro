package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.BloodBank;
import com.bloodconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {

    BloodBank findByUserId(Long userId);


    BloodBank findByUser(User user);

}