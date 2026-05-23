package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.*;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hospital-dashboard")
@CrossOrigin
public class HospitalDashboardController {

    private final BloodRequestRepository requestRepository;
    private final DonorResponseRepository responseRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final DonationScheduleRepository scheduleRepository; // ✅ ADD THIS

    public HospitalDashboardController(BloodRequestRepository requestRepository,
                                       DonorResponseRepository responseRepository,
                                       HospitalRepository hospitalRepository,
                                       UserRepository userRepository,
                                       DonationScheduleRepository scheduleRepository) { // ✅ ADD
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository; // ✅ ADD
    }

    // ✅ AUTH HANDLING
    private Hospital getHospital(Authentication auth) {

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

        Hospital hospital = hospitalRepository.findByUser(user);

        if (hospital == null) {
            throw new RuntimeException("Hospital not found");
        }

        return hospital;
    }

    // 🏥 PROFILE
    @GetMapping("/profile")
    public HospitalProfileDTO getProfile(Authentication auth) {

        Hospital hospital = getHospital(auth);

        return new HospitalProfileDTO(
                hospital.getHospitalName(),
                hospital.getLicenseNumber(),
                hospital.getGstNumber(),
                hospital.getAddress()
        );
    }

    // 📊 STATS
    @GetMapping("/stats")
    public HospitalDashboardDTO getStats(Authentication auth) {

        Hospital hospital = getHospital(auth);

        long activeRequests =
                requestRepository.countByHospitalIdAndStatus(hospital.getId(), "OPEN");

        long totalResponses =
                responseRepository.countByBloodRequestHospitalId(hospital.getId());

        // ✅ FINAL FIX (JPQL JOIN)
        long successful =
                scheduleRepository.countCompletedDonations(hospital.getId());

        // 🔥 DEBUG
        System.out.println("SUCCESSFUL DONATIONS = " + successful);

        return new HospitalDashboardDTO(activeRequests, totalResponses, successful);
    }


}