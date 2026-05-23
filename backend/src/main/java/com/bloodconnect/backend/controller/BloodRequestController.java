package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.BloodRequestResponseDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;
import com.bloodconnect.backend.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blood-request")
@CrossOrigin
public class BloodRequestController {

    private final BloodRequestRepository bloodRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    // 🔥 KEEP (no removal)
    private final DonorRepository donorRepository;
    private final NotificationRepository notificationRepository;

    // 🔥 NEW SERVICE
    private final NotificationService notificationService;

    public BloodRequestController(BloodRequestRepository bloodRequestRepository,
                                  HospitalRepository hospitalRepository,
                                  UserRepository userRepository,
                                  DonorRepository donorRepository,
                                  NotificationRepository notificationRepository,
                                  NotificationService notificationService) {

        this.bloodRequestRepository = bloodRequestRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.donorRepository = donorRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    // ✅ GET LOGGED USER
    private User getUser(Authentication auth) {

        String username = auth.getName();

        return userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ CREATE BLOOD REQUEST (UPDATED 🔥)
    @PostMapping("/create")
    public String createRequest(@RequestBody BloodRequest request,
                                Authentication auth) {

        User user = getUser(auth);
        Hospital hospital = hospitalRepository.findByUser(user);

        request.setHospital(hospital);
        request.setStatus("OPEN");

        bloodRequestRepository.save(request);

        // 🔥 NEW CENTRALIZED NOTIFICATION
        notificationService.notifyAllUsers(request);

        return "Blood request created & all users notified";
    }


}