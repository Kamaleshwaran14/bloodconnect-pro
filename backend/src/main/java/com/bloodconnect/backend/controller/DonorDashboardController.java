package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.*;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/donor-dashboard")
@CrossOrigin
public class DonorDashboardController {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository requestRepository;
    private final DonorResponseRepository responseRepository;
    private final DonationScheduleRepository scheduleRepository;

    public DonorDashboardController(DonorRepository donorRepository,
                                    UserRepository userRepository,
                                    BloodRequestRepository requestRepository,
                                    DonorResponseRepository responseRepository,
                                    DonationScheduleRepository scheduleRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // ✅ GET DONOR
    private Donor getDonor(Authentication auth) {

        String username;

        if (auth.getPrincipal() instanceof User u) {
            username = (u.getEmail() != null) ? u.getEmail() : u.getPhone();
        } else {
            username = auth.getName();
        }

        User user = userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findByUser(user);

        if (donor == null) {
            throw new RuntimeException("Donor not found");
        }

        return donor;
    }

    // ✅ DASHBOARD OVERVIEW (FINAL CLEAN VERSION)
    @GetMapping("/overview")
    public DonorDashboardDTO getDashboard(Authentication auth) {

        Donor donor = getDonor(auth);

        // ✅ PROFILE
        DonorProfileDTO profile = new DonorProfileDTO(
                donor.getFullName(),
                donor.getBloodGroup(),
                donor.getLocation(),
                donor.getPhone(),
                donor.getAge(),
                donor.getPincode(),
                donor.isAvailable(),
                donor.getUser().getEmail()
        );

        // ✅ NOTIFICATIONS
        List<NotificationDTO> notifications = getNotificationsInternal(donor);

        // ✅ STEP 1: DIRECT SCHEDULES
        List<DonationSchedule> schedules =
                scheduleRepository.findByDonorId(donor.getId());

        // ✅ STEP 2: GET RESPONSES
        List<DonorResponse> responses =
                responseRepository.findByDonorId(donor.getId());

        // ✅ STEP 3: GET SCHEDULES FROM REQUEST (IMPORTANT FIX)
        List<DonationSchedule> fallbackSchedules =
                responses.stream()
                        .flatMap(res ->
                                scheduleRepository
                                        .findByBloodRequestId(res.getBloodRequest().getId())
                                        .stream()
                        )
                        .toList();

        // ✅ STEP 4: MERGE + REMOVE DUPLICATES
        schedules = java.util.stream.Stream
                .concat(schedules.stream(), fallbackSchedules.stream())
                .distinct()
                .toList();

        // ✅ DEBUG (KEEP THIS TEMP)
        System.out.println("TOTAL SCHEDULES FOUND = " + schedules.size());
        schedules.forEach(s ->
                System.out.println("Schedule ID: " + s.getId()
                        + " | Status: " + s.getStatus()
                        + " | DonorId: " + (s.getDonor() != null ? s.getDonor().getId() : "NULL")
                        + " | RequestId: " + s.getBloodRequest().getId())
        );

        // ✅ STEP 5: FILTER COMPLETED (🔥 FIX APPLIED HERE)
        List<DonationSchedule> completed = schedules.stream()
                .filter(s ->
                        "COMPLETED".equalsIgnoreCase(s.getStatus()) &&
                                s.getDonor() != null &&
                                s.getDonor().getId().equals(donor.getId())
                )
                .toList();

        int totalDonations = completed.size();

        String lastDonation = "-";
        String nextEligibleDate = "-";

        if (!completed.isEmpty()) {

            DonationSchedule last = completed.stream()
                    .max(Comparator.comparing(DonationSchedule::getScheduledDate))
                    .orElse(null);

            if (last != null && last.getScheduledDate() != null) {

                java.time.LocalDate lastDate =
                        last.getScheduledDate().toLocalDate();

                lastDonation = lastDate.toString();

                // ✅ AUTO 2 MONTHS RULE
                java.time.LocalDate nextDate =
                        lastDate.plusMonths(2);

                nextEligibleDate = nextDate.toString();
            }
        }

        return new DonorDashboardDTO(
                profile,
                donor.isAvailable(),
                notifications,
                totalDonations,
                lastDonation,
                nextEligibleDate
        );
    }

    // ✅ NOTIFICATIONS (UPDATED CLEAN VERSION)
    private List<NotificationDTO> getNotificationsInternal(Donor donor) {

        List<NotificationDTO> notifications = new java.util.ArrayList<>();

        // ✅ 1. NEW REQUESTS (ONLY IF NOT RESPONDED)
        List<BloodRequest> requests =
                requestRepository.findByBloodGroupAndLocationAndStatus(
                        donor.getBloodGroup(),
                        donor.getLocation(),
                        "OPEN"
                );

        for (BloodRequest r : requests) {

            boolean alreadyResponded = responseRepository
                    .findByBloodRequestId(r.getId())
                    .stream()
                    .anyMatch(res ->
                            res.getDonor() != null &&
                                    res.getDonor().getId().equals(donor.getId())
                    );

            if (!alreadyResponded) {
                notifications.add(new NotificationDTO(
                        "REQUEST",
                        "New request for " + r.getBloodGroup() +
                                " at " + r.getLocation(),
                        r.getCreatedAt().toString()
                ));
            }
        }

        // ✅ 2. ACCEPTED (HOSPITAL SELECTED YOU)
        List<DonorResponse> responses =
                responseRepository.findByDonorId(donor.getId());

        for (DonorResponse res : responses) {

            if ("SELECTED".equalsIgnoreCase(res.getStatus())) {
                notifications.add(new NotificationDTO(
                        "ACCEPTED",
                        "Hospital accepted your response for "
                                + res.getBloodRequest().getPatientName(),
                        res.getRespondedAt().toString()
                ));
            }
        }

        // ✅ 3. SCHEDULED (FROM SCHEDULE TABLE)
        List<DonationSchedule> schedules =
                scheduleRepository.findByDonorId(donor.getId());

        for (DonationSchedule s : schedules) {

            if ("SCHEDULED".equalsIgnoreCase(s.getStatus())) {
                notifications.add(new NotificationDTO(
                        "SCHEDULED",
                        "Donation scheduled at "
                                + s.getHospital().getHospitalName(),
                        s.getScheduledDate().toString()
                ));
            }
        }

        return notifications;
    }

    // ✅ UPDATE PROFILE
    @PutMapping("/update")
    public String updateDonor(@RequestBody DonorUpdateDTO dto, Authentication auth) {

        Donor donor = getDonor(auth);

        donor.setFullName(dto.getFullName());
        donor.setLocation(dto.getLocation());
        donor.setPhone(dto.getPhone());
        donor.setAge(dto.getAge());
        donor.setPincode(dto.getPincode());

        String currentEmail = donor.getUser().getEmail();
        String newEmail = dto.getUser().getEmail();

        if (!currentEmail.equals(newEmail)) {

            if (!dto.isEmailVerified()) {
                throw new RuntimeException("Email not verified");
            }

            donor.getUser().setEmail(newEmail);
        }

        donorRepository.save(donor);

        return "Profile updated successfully";
    }
}