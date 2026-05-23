package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.InventoryDTO;
import com.bloodconnect.backend.dto.InventoryHistoryDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin
public class InventoryController {

    private final BloodInventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final UserRepository userRepository;

    public InventoryController(BloodInventoryRepository inventoryRepository,
                               InventoryHistoryRepository historyRepository,
                               HospitalRepository hospitalRepository,
                               BloodBankRepository bloodBankRepository,
                               UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.userRepository = userRepository;
    }

    // ✅ GET USER (STRICT)
    private User getUser(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        String username = auth.getName();

        return userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ ADD / UPDATE STOCK + HISTORY (SECURE)
    @PostMapping("/add")
    public String addOrUpdateStock(@RequestParam String bloodGroup,
                                   @RequestParam Integer units,
                                   Authentication auth) {

        User user = getUser(auth);

        // ✅ VALIDATION
        if (bloodGroup == null || bloodGroup.isBlank()) {
            return "Invalid blood group";
        }

        if (units == null || units < 0) {
            return "Invalid units";
        }

        // 🔹 Hospital
        Hospital hospital = hospitalRepository.findByUser(user);

        if (hospital != null) {

            // 🔒 STORAGE CHECK (MAIN FIX)
            if (!Boolean.TRUE.equals(hospital.getHasStorageFacility())) {
                return "This hospital does not have blood storage facility";
            }

            BloodInventory inventory =
                    inventoryRepository.findByHospitalIdAndBloodGroup(
                            hospital.getId(), bloodGroup
                    );

            if (inventory == null) {
                inventory = new BloodInventory();
                inventory.setHospital(hospital);
                inventory.setBloodGroup(bloodGroup);
            }

            inventory.setUnitsAvailable(units);
            inventoryRepository.save(inventory);

            // ✅ HISTORY ENTRY
            InventoryHistory history = new InventoryHistory();
            history.setBloodGroup(bloodGroup);
            history.setUnits(units);
            history.setActionType("ADDED");
            history.setTime(LocalDateTime.now());
            history.setReference("Manual update");
            history.setHospital(hospital);

            historyRepository.save(history);

            return "Hospital stock updated";
        }

        // 🔹 Blood Bank
        BloodBank bank = bloodBankRepository.findByUser(user);

        if (bank != null) {

            BloodInventory inventory =
                    inventoryRepository.findByBloodBankIdAndBloodGroup(
                            bank.getId(), bloodGroup
                    );

            if (inventory == null) {
                inventory = new BloodInventory();
                inventory.setBloodBank(bank);
                inventory.setBloodGroup(bloodGroup);
            }

            inventory.setUnitsAvailable(units);
            inventoryRepository.save(inventory);

            // ✅ HISTORY ENTRY
            InventoryHistory history = new InventoryHistory();
            history.setBloodGroup(bloodGroup);
            history.setUnits(units);
            history.setActionType("ADDED");
            history.setTime(LocalDateTime.now());
            history.setReference("Manual update");
            history.setBloodBank(bank);

            historyRepository.save(history);

            return "Blood bank stock updated";
        }

        return "User not authorized";
    }

    // ✅ VIEW MY INVENTORY (STRICT USER DATA ONLY)
    @GetMapping("/my")
    public List<InventoryDTO> getMyInventory(Authentication auth) {

        User user = getUser(auth);

        // 🔹 Hospital
        Hospital hospital = hospitalRepository.findByUser(user);
        if (hospital != null) {

            // 🔒 ONLY IF STORAGE ENABLED
            if (!Boolean.TRUE.equals(hospital.getHasStorageFacility())) {
                return List.of();
            }

            return inventoryRepository.findByHospitalId(hospital.getId())
                    .stream()
                    .map(inv -> new InventoryDTO(
                            inv.getId(),
                            inv.getBloodGroup(),
                            inv.getUnitsAvailable(),
                            hospital.getHospitalName(),
                            "HOSPITAL"
                    ))
                    .toList();
        }

        // 🔹 Blood Bank
        BloodBank bank = bloodBankRepository.findByUser(user);
        if (bank != null) {
            return inventoryRepository.findByBloodBankId(bank.getId())
                    .stream()
                    .map(inv -> new InventoryDTO(
                            inv.getId(),
                            inv.getBloodGroup(),
                            inv.getUnitsAvailable(),
                            bank.getBankName(), // ✅ FIXED
                            "BLOOD_BANK"
                    ))
                    .toList();
        }

        return List.of();
    }

    // ✅ INVENTORY HISTORY (STRICT USER ONLY)
    @GetMapping("/history")
    public List<InventoryHistoryDTO> getHistory(Authentication auth) {

        User user = getUser(auth);

        // 🔹 Hospital
        Hospital hospital = hospitalRepository.findByUser(user);
        if (hospital != null) {

            if (!Boolean.TRUE.equals(hospital.getHasStorageFacility())) {
                return List.of();
            }

            return historyRepository.findByHospitalId(hospital.getId())
                    .stream()
                    .map(h -> new InventoryHistoryDTO(
                            h.getId(),
                            h.getBloodGroup(),
                            h.getUnits(),
                            h.getActionType(),
                            h.getTime(),
                            h.getReference()
                    ))
                    .toList();
        }

        // 🔹 Blood Bank
        BloodBank bank = bloodBankRepository.findByUser(user);
        if (bank != null) {
            return historyRepository.findByBloodBankId(bank.getId())
                    .stream()
                    .map(h -> new InventoryHistoryDTO(
                            h.getId(),
                            h.getBloodGroup(),
                            h.getUnits(),
                            h.getActionType(),
                            h.getTime(),
                            h.getReference()
                    ))
                    .toList();
        }

        return List.of();
    }
}