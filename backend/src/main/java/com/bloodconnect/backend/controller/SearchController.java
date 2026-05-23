package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.SearchResultDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {

    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final BloodInventoryRepository inventoryRepository;

    public SearchController(DonorRepository donorRepository,
                            HospitalRepository hospitalRepository,
                            BloodBankRepository bloodBankRepository,
                            BloodInventoryRepository inventoryRepository) {
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/global")
    public List<SearchResultDTO> searchAll(
            @RequestParam String bloodGroup,
            @RequestParam String location
    ) {

        String blood = normalize(bloodGroup);
        String loc = normalize(location);

        List<SearchResultDTO> results = new ArrayList<>();

        // ================= DONORS =================
        for (Donor d : donorRepository.findAll()) {

            if (!d.isAvailable()) continue;

            if (normalize(d.getBloodGroup()).equals(blood)
                    && normalize(d.getLocation()).contains(loc)) {

                results.add(new SearchResultDTO(
                        "DONOR",
                        d.getId(),
                        d.getFullName(),
                        d.getBloodGroup(),
                        d.getLocation(),
                        d.getPhone(),
                        true
                ));
            }
        }

        // ================= HOSPITALS =================
        for (Hospital h : hospitalRepository.findAll()) {

            // ✅ ONLY STORAGE HOSPITAL
            if (!Boolean.TRUE.equals(h.getHasStorageFacility())) continue;

            if (!normalize(h.getAddress()).contains(loc)) continue;

            List<BloodInventory> invList =
                    inventoryRepository.findByHospitalId(h.getId());

            boolean available = invList.stream()
                    .anyMatch(i ->
                            normalize(i.getBloodGroup()).equals(blood)
                                    && i.getUnitsAvailable() > 0
                    );

            if (available) {
                results.add(new SearchResultDTO(
                        "HOSPITAL",
                        h.getId(),
                        h.getHospitalName(),
                        blood,
                        h.getAddress(),
                        // ✅ FIXED CONTACT (FROM USER)
                        h.getUser() != null ? h.getUser().getPhone() : "N/A",
                        true
                ));
            }
        }

        // ================= BLOOD BANK =================
        for (BloodBank b : bloodBankRepository.findAll()) {

            if (!normalize(b.getAddress()).contains(loc)) continue;

            List<BloodInventory> invList =
                    inventoryRepository.findByBloodBankId(b.getId());

            boolean available = invList.stream()
                    .anyMatch(i ->
                            normalize(i.getBloodGroup()).equals(blood)
                                    && i.getUnitsAvailable() > 0
                    );

            if (available) {
                results.add(new SearchResultDTO(
                        "BLOOD_BANK",
                        b.getId(),
                        b.getBankName(),
                        blood,
                        b.getAddress(),
                        // ✅ FIXED CONTACT (FROM USER)
                        b.getUser() != null ? b.getUser().getPhone() : "N/A",
                        true
                ));
            }
        }

        return results;
    }

    // ✅ STRONG NORMALIZATION (UNCHANGED)
    private String normalize(String val) {
        if (val == null) return "";

        return val
                .replace(" ", "+")     // fix if + became space
                .replaceAll("\\s+", "")
                .toUpperCase()
                .trim();
    }
}