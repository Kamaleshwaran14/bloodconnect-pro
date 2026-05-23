package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.*;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bloodbank-dashboard")
@CrossOrigin
public class BloodBankDashboardController {

    private final BloodInventoryRepository inventoryRepository;
    private final BloodBankRepository bloodBankRepository;
    private final UserRepository userRepository;
    private final DonationScheduleRepository scheduleRepository;
    private final HospitalRepository hospitalRepository;

    // ✅ ADDED (REQUIRED FOR YOUR LOGIC)
    private final DonorRepository donorRepository;
    private final DonorResponseRepository responseRepository;

    public BloodBankDashboardController(
            BloodInventoryRepository inventoryRepository,
            BloodBankRepository bloodBankRepository,
            UserRepository userRepository,
            DonationScheduleRepository scheduleRepository,
            HospitalRepository hospitalRepository,
            DonorRepository donorRepository,                  // ✅ ADDED
            DonorResponseRepository responseRepository        // ✅ ADDED
    ) {
        this.inventoryRepository = inventoryRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.hospitalRepository = hospitalRepository;
        this.donorRepository = donorRepository;              // ✅ ADDED
        this.responseRepository = responseRepository;        // ✅ ADDED
    }

    // ✅ AUTH HANDLING (UNCHANGED)
    private BloodBank getBloodBank(Authentication auth) {

        String username;

        if (auth.getPrincipal() instanceof User) {
            User u = (User) auth.getPrincipal();
            username = (u.getEmail() != null) ? u.getEmail() : u.getPhone();
        } else {
            username = auth.getName();
        }

        User user = userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BloodBank bank = bloodBankRepository.findByUser(user);

        if (bank == null) {
            throw new RuntimeException("Blood Bank not found");
        }

        return bank;
    }

    // 🏥 PROFILE (UNCHANGED)
    @GetMapping("/profile")
    public BloodBankProfileDTO getProfile(Authentication auth) {

        BloodBank bank = getBloodBank(auth);

        return new BloodBankProfileDTO(
                bank.getBankName(),
                bank.getLicenseNumber(),
                bank.getAddress(),
                bank.getContactNumber(),
                true
        );
    }

    // 📊 STATS (FIXED - WORKING)
    @GetMapping("/stats")
    public BloodBankStatsDTO getStats(Authentication auth) {

        BloodBank bank = getBloodBank(auth);

        List<BloodInventory> inventory =
                inventoryRepository.findByBloodBankId(bank.getId());

        int totalUnits = inventory.stream()
                .mapToInt(i -> i.getUnitsAvailable())
                .sum();

        long successfulDonations = 0;
        String lastDonationDate = "N/A";

        try {

            // 🔥 STEP 1: GET BLOODBANK RESPONSES
            List<DonorResponse> responses = responseRepository.findAll()
                    .stream()
                    .filter(r ->
                            "BLOOD_BANK".equalsIgnoreCase(r.getSourceType()) &&
                                    r.getSourceId() != null &&
                                    r.getSourceId().equals(bank.getId()) &&
                                    "CONFIRMED".equalsIgnoreCase(r.getStatus())
                    )
                    .toList();

            // 🔥 STEP 2: GET RELATED SCHEDULES
            List<DonationSchedule> schedules = responses.stream()
                    .flatMap(r ->
                            scheduleRepository
                                    .findByBloodRequestId(r.getBloodRequest().getId())
                                    .stream()
                    )
                    .toList();

            // 🔥 STEP 3: FILTER COMPLETED
            List<DonationSchedule> completed = schedules.stream()
                    .filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()))
                    .toList();

            successfulDonations = completed.size();

            // 🔥 STEP 4: LAST DATE
            if (!completed.isEmpty()) {

                DonationSchedule last = completed.stream()
                        .max(java.util.Comparator.comparing(DonationSchedule::getScheduledDate))
                        .orElse(null);

                if (last != null && last.getScheduledDate() != null) {
                    lastDonationDate = last.getScheduledDate().toLocalDate().toString();
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return new BloodBankStatsDTO(
                totalUnits,
                successfulDonations,
                lastDonationDate
        );
    }

    // 🩸 INVENTORY (UNCHANGED)
    @GetMapping("/inventory")
    public List<BloodInventoryDTO> getInventory(Authentication auth) {

        BloodBank bank = getBloodBank(auth);

        return inventoryRepository
                .findByBloodBankId(bank.getId())
                .stream()
                .map(i -> new BloodInventoryDTO(
                        i.getId(),
                        i.getBloodGroup(),
                        i.getUnitsAvailable()
                ))
                .collect(Collectors.toList());
    }
}