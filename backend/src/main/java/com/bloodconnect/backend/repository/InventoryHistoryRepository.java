package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    List<InventoryHistory> findByHospitalId(Long hospitalId);

    List<InventoryHistory> findByBloodBankId(Long bloodBankId);
}