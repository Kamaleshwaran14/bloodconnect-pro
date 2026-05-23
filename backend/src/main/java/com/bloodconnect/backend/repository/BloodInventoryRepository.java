package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    List<BloodInventory> findByHospitalId(Long hospitalId);

    List<BloodInventory> findByBloodBankId(Long bloodBankId);

    BloodInventory findByHospitalIdAndBloodGroup(Long hospitalId, String bloodGroup);

    BloodInventory findByBloodBankIdAndBloodGroup(Long bloodBankId, String bloodGroup);

    List<BloodInventory> findByBloodGroupAndUnitsAvailableGreaterThan(String bloodGroup, int i);
}