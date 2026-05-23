package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.BloodRequestResponseDTO;
import com.bloodconnect.backend.dto.DonationScheduleDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;
import com.bloodconnect.backend.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin
public class DonationScheduleController {

    private final DonationScheduleRepository scheduleRepository;
    private final DonorResponseRepository responseRepository;
    private final HospitalRepository hospitalRepository;
    private final DonorRepository donorRepository;
    private final BloodBankRepository bloodBankRepository;
    private final BloodRequestRepository requestRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DonationScheduleController(DonationScheduleRepository scheduleRepository,
                                      DonorResponseRepository responseRepository,
                                      HospitalRepository hospitalRepository,
                                      DonorRepository donorRepository,
                                      BloodBankRepository bloodBankRepository,
                                      BloodRequestRepository requestRepository,
                                      BloodInventoryRepository inventoryRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService) {
        this.scheduleRepository = scheduleRepository;
        this.responseRepository = responseRepository;
        this.hospitalRepository = hospitalRepository;
        this.donorRepository = donorRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.requestRepository = requestRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ✅ COMMON USER
    private User getUser(Authentication auth) {
        String username = auth.getName();
        return userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ CREATE SCHEDULE (ONLY HOSPITAL)
    // ONLY showing changed part inside createSchedule()

    @PostMapping("/create")
    public String createSchedule(@RequestParam Long responseId,
                                 @RequestParam String dateTime,
                                 Authentication auth) {

        User user = getUser(auth);
        Hospital hospital = hospitalRepository.findByUser(user);

        if (hospital == null) return "Only hospital can schedule";

        DonorResponse response =
                responseRepository.findById(responseId).orElse(null);

        if (response == null || !"SELECTED".equalsIgnoreCase(response.getStatus())) {
            return "Response not selected";
        }

        DonationSchedule schedule = new DonationSchedule();

        // ✅ FIXED CORE LOGIC
        if ("DONOR".equalsIgnoreCase(response.getSourceType())) {
            schedule.setDonor(response.getDonor());
        } else {
            schedule.setDonor(null);
        }

        schedule.setHospital(hospital);
        schedule.setBloodRequest(response.getBloodRequest());
        schedule.setScheduledDate(LocalDateTime.parse(dateTime));
        schedule.setStatus("SCHEDULED");

        scheduleRepository.save(schedule);

        response.setStatus("CONFIRMED");
        responseRepository.save(response);

        // ✅ EMAIL FIX (WORKS FOR ALL)
        String email = null;

        if ("DONOR".equalsIgnoreCase(response.getSourceType())) {
            email = response.getDonor().getUser().getEmail();
        } else if ("HOSPITAL".equalsIgnoreCase(response.getSourceType())) {
            Hospital h = hospitalRepository.findById(response.getSourceId()).orElse(null);
            if (h != null) email = h.getUser().getEmail();
        } else if ("BLOOD_BANK".equalsIgnoreCase(response.getSourceType())) {
            BloodBank b = bloodBankRepository.findById(response.getSourceId()).orElse(null);
            if (b != null) email = b.getUser().getEmail();
        }

        if (email != null) {

            String message = "Your donation has been scheduled.\n\n"
                    + "Hospital: " + hospital.getHospitalName() + "\n"
                    + "Location: " + hospital.getAddress() + "\n"
                    + "Contact: " + hospital.getUser().getPhone() + "\n"
                    + "Date & Time: " + dateTime;

            notificationService.sendEmail(email, "Donation Scheduled", message);
        }

        return "Scheduled successfully";
    }

    // 🔥 INCOMING (RESTORED - DON’T CHANGE LOGIC)
    @GetMapping("/incoming")
    public List<BloodRequestResponseDTO> getIncoming(Authentication auth) {

        User user = getUser(auth);

        Donor donor = donorRepository.findByUser(user);
        Hospital hospital = hospitalRepository.findByUser(user);
        BloodBank bank = bloodBankRepository.findByUser(user);

        List<BloodRequest> requests;

        // 👤 DONOR
        if (donor != null) {
            requests = requestRepository
                    .findByBloodGroupAndLocationAndStatus(
                            donor.getBloodGroup(),
                            donor.getLocation(),
                            "OPEN"
                    );
        }

        // 🏥 HOSPITAL (WITH INVENTORY)
        else if (hospital != null && Boolean.TRUE.equals(hospital.getHasStorageFacility())) {

            requests = requestRepository.findAll()
                    .stream()
                    .filter(r -> "OPEN".equals(r.getStatus()))
                    .filter(r -> r.getLocation().equalsIgnoreCase(hospital.getAddress()))
                    .filter(r -> inventoryRepository
                            .findByHospitalId(hospital.getId())
                            .stream()
                            .anyMatch(i ->
                                    i.getBloodGroup().equals(r.getBloodGroup()) &&
                                            i.getUnitsAvailable() >= r.getUnitsRequired()
                            ))
                    .collect(Collectors.toList());
        }

        // 🏦 BLOOD BANK
        else if (bank != null) {

            requests = requestRepository.findAll()
                    .stream()
                    .filter(r -> "OPEN".equals(r.getStatus()))
                    .filter(r -> r.getLocation().equalsIgnoreCase(bank.getAddress()))
                    .filter(r -> inventoryRepository
                            .findByBloodBankId(bank.getId())
                            .stream()
                            .anyMatch(i ->
                                    i.getBloodGroup().equals(r.getBloodGroup()) &&
                                            i.getUnitsAvailable() >= r.getUnitsRequired()
                            ))
                    .collect(Collectors.toList());
        }

        else {
            requests = List.of();
        }

        // ✅ ONLY THIS USER RESPONSE STATUS
        return requests.stream().map(r -> {

            DonorResponse response = responseRepository
                    .findByBloodRequestId(r.getId())
                    .stream()
                    .filter(res -> {

                        if (donor != null && res.getDonor() != null) {
                            return res.getDonor().getId().equals(donor.getId());
                        }

                        if (hospital != null &&
                                "HOSPITAL".equalsIgnoreCase(res.getSourceType()) &&
                                res.getSourceId().equals(hospital.getId())) {
                            return true;
                        }

                        if (bank != null &&
                                "BLOOD_BANK".equalsIgnoreCase(res.getSourceType()) &&
                                res.getSourceId().equals(bank.getId())) {
                            return true;
                        }

                        return false;
                    })
                    .findFirst()
                    .orElse(null);

            String status = (response != null) ? response.getStatus() : r.getStatus();

            return new BloodRequestResponseDTO(
                    r.getId(),
                    r.getPatientName(),
                    r.getPatientAge(),
                    r.getContactNumber(),
                    r.getBloodGroup(),
                    r.getUnitsRequired(),
                    r.getLocation(),
                    r.getUrgencyLevel(),
                    status,
                    r.getHospital().getHospitalName()
            );

        }).collect(Collectors.toList());
    }

    // 🔥 UPDATED UPCOMING
    @GetMapping("/upcoming")
    public List<DonationScheduleDTO> getUpcoming(Authentication auth) {

        User user = getUser(auth);

        Donor donor = donorRepository.findByUser(user);
        Hospital hospital = hospitalRepository.findByUser(user);
        BloodBank bank = bloodBankRepository.findByUser(user);

        // 👤 DONOR ✅ FIXED
        if (donor != null) {
            return scheduleRepository.findAll()
                    .stream()
                    .filter(s -> s.getDonor() != null)
                    .filter(s -> s.getDonor().getId().equals(donor.getId()))
                    .filter(this::handleCompletion)
                    .map(this::convert)
                    .collect(Collectors.toList());
        }

        // 🏦 BLOOD BANK (UNCHANGED)
        if (bank != null) {
            return responseRepository.findAll()
                    .stream()
                    .filter(r -> "BLOOD_BANK".equalsIgnoreCase(r.getSourceType()))
                    .filter(r -> r.getSourceId().equals(bank.getId()))
                    .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                    .map(r -> scheduleRepository
                            .findByBloodRequestId(r.getBloodRequest().getId())
                            .stream()
                            .findFirst()
                            .orElse(null))
                    .filter(this::handleCompletion)
                    .map(this::convertWithResponder)
                    .collect(Collectors.toList());
        }

        // 🏥 HOSPITAL (UNCHANGED)
        if (hospital != null) {
            return responseRepository.findAll()
                    .stream()
                    .filter(r -> "HOSPITAL".equalsIgnoreCase(r.getSourceType()))
                    .filter(r -> r.getSourceId().equals(hospital.getId()))
                    .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                    .map(r -> scheduleRepository
                            .findByBloodRequestId(r.getBloodRequest().getId())
                            .stream()
                            .findFirst()
                            .orElse(null))
                    .filter(this::handleCompletion)
                    .map(this::convertWithResponder)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // 🔥 REQUESTER UPCOMING
    @GetMapping("/hospital/requester-upcoming")
    public List<DonationScheduleDTO> getRequesterUpcoming(Authentication auth) {

        User user = getUser(auth);
        Hospital hospital = hospitalRepository.findByUser(user);

        if (hospital == null) return List.of();

        return scheduleRepository.findAll()
                .stream()
                .filter(s ->
                        s.getHospital() != null &&
                                s.getHospital().getId().equals(hospital.getId()))
                .filter(this::handleCompletion)
                .map(this::convertWithResponder)
                .collect(Collectors.toList());
    }

    // 🔥 HANDLE AUTO COMPLETE USING EXISTING API
    private boolean handleCompletion(DonationSchedule s) {

        if (s == null) return false;

        if (s.getScheduledDate().isBefore(LocalDateTime.now())
                && "SCHEDULED".equalsIgnoreCase(s.getStatus())) {

            completeDonation(s.getId());
            return false;
        }

        return "SCHEDULED".equalsIgnoreCase(s.getStatus());
    }

    // ✅ HISTORY
    // ✅ HISTORY (FIXED - ONLY RESPONDER HISTORY)
    @GetMapping("/history")
    public List<DonationScheduleDTO> getHistory(Authentication auth) {

        User user = getUser(auth);

        Donor donor = donorRepository.findByUser(user);
        Hospital hospital = hospitalRepository.findByUser(user);
        BloodBank bank = bloodBankRepository.findByUser(user);

        // 👤 DONOR ✅ FIXED
        if (donor != null) {
            return scheduleRepository.findAll()
                    .stream()
                    .filter(s -> s.getDonor() != null)
                    .filter(s -> s.getDonor().getId().equals(donor.getId()))
                    .filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()))
                    .map(this::convertHistory)
                    .collect(Collectors.toList());
        }

        // 🏥 HOSPITAL (UNCHANGED)
        if (hospital != null) {
            return responseRepository.findAll()
                    .stream()
                    .filter(r -> "HOSPITAL".equalsIgnoreCase(r.getSourceType()))
                    .filter(r -> r.getSourceId().equals(hospital.getId()))
                    .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                    .map(r -> scheduleRepository
                            .findByBloodRequestId(r.getBloodRequest().getId())
                            .stream()
                            .findFirst()
                            .orElse(null))
                    .filter(s -> s != null && "COMPLETED".equalsIgnoreCase(s.getStatus()))
                    .map(this::convertHistory)
                    .collect(Collectors.toList());
        }

        // 🏦 BLOOD BANK (UNCHANGED)
        if (bank != null) {
            return responseRepository.findAll()
                    .stream()
                    .filter(r -> "BLOOD_BANK".equalsIgnoreCase(r.getSourceType()))
                    .filter(r -> r.getSourceId().equals(bank.getId()))
                    .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                    .map(r -> scheduleRepository
                            .findByBloodRequestId(r.getBloodRequest().getId())
                            .stream()
                            .findFirst()
                            .orElse(null))
                    .filter(s -> s != null && "COMPLETED".equalsIgnoreCase(s.getStatus()))
                    .map(this::convertHistory)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // 🔥 CONVERT WITH RESPONDER NAME FIX
    private DonationScheduleDTO convertWithResponder(DonationSchedule s) {

        String name = "N/A";

        if (s.getDonor() != null) {
            name = s.getDonor().getFullName();
        }

        DonorResponse response = responseRepository
                .findByBloodRequestId(s.getBloodRequest().getId())
                .stream()
                .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (response != null) {

            if ("HOSPITAL".equalsIgnoreCase(response.getSourceType())) {
                Hospital h = hospitalRepository
                        .findById(response.getSourceId())
                        .orElse(null);
                if (h != null) name = h.getHospitalName();
            }

            else if ("BLOOD_BANK".equalsIgnoreCase(response.getSourceType())) {
                BloodBank b = bloodBankRepository
                        .findById(response.getSourceId())
                        .orElse(null);
                if (b != null) name = b.getBankName();
            }
        }

        return new DonationScheduleDTO(
                s.getId(),
                name,
                s.getHospital().getHospitalName(),
                s.getBloodRequest().getPatientName(),
                s.getBloodRequest().getBloodGroup(),
                s.getScheduledDate().toString(),
                s.getStatus()
        );
    }

    // ✅ COMPLETE
    @PutMapping("/complete")
    public String completeDonation(@RequestParam Long scheduleId) {

        DonationSchedule schedule =
                scheduleRepository.findById(scheduleId).orElse(null);

        if (schedule == null) return "Not found";

        schedule.setStatus("COMPLETED");

        BloodRequest request = schedule.getBloodRequest();
        request.setStatus("CLOSED");

        scheduleRepository.save(schedule);
        requestRepository.save(request);

        return "Completed";
    }

    // 🔥 ORIGINAL CONVERTERS (UNCHANGED)
    private DonationScheduleDTO convert(DonationSchedule s) {
        return new DonationScheduleDTO(
                s.getId(),
                s.getDonor() != null ? s.getDonor().getFullName() : "N/A",
                s.getHospital().getHospitalName(),
                s.getBloodRequest().getPatientName(),
                s.getBloodRequest().getBloodGroup(),
                s.getScheduledDate().toString(),
                s.getStatus()
        );
    }

    private DonationScheduleDTO convertHistory(DonationSchedule s) {
        return new DonationScheduleDTO(
                s.getId(),
                s.getDonor() != null ? s.getDonor().getFullName() : "N/A",
                s.getHospital().getHospitalName(),
                s.getBloodRequest().getPatientName(),
                s.getBloodRequest().getBloodGroup(),
                s.getScheduledDate().toString(),
                "COMPLETED"
        );
    }
}