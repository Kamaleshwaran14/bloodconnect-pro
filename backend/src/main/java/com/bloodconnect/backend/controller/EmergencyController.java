package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.*;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin
public class EmergencyController {

    private final BloodRequestRepository requestRepository;
    private final DonorResponseRepository responseRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final UserRepository userRepository;
    private final DonationScheduleRepository scheduleRepository;

    public EmergencyController(BloodRequestRepository requestRepository,
                               DonorResponseRepository responseRepository,
                               HospitalRepository hospitalRepository,
                               BloodBankRepository bloodBankRepository,
                               UserRepository userRepository,
                               DonationScheduleRepository scheduleRepository) {
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    private Hospital getHospital(Authentication auth) {
        String username = auth.getName();

        User user = userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Hospital hospital = hospitalRepository.findByUser(user);

        if (hospital == null) throw new RuntimeException("Only hospital allowed");

        return hospital;
    }

    // ✅ ACTIVE REQUESTS
    @GetMapping("/requests")
    public List<BloodRequestResponseDTO> getActiveRequests(Authentication auth) {

        Hospital hospital = getHospital(auth);

        return requestRepository
                .findByHospitalId(hospital.getId())
                .stream()
                .filter(r -> scheduleRepository.findByBloodRequestId(r.getId()).isEmpty())
                .map(r -> new BloodRequestResponseDTO(
                        r.getId(),
                        r.getPatientName(),
                        r.getPatientAge(),
                        r.getContactNumber(),
                        r.getBloodGroup(),
                        r.getUnitsRequired(),
                        r.getLocation(),
                        r.getUrgencyLevel(),
                        r.getStatus(),
                        hospital.getHospitalName()
                ))
                .collect(Collectors.toList());
    }

    // 🔥 FIXED RESPONSES
    // 📥 2. INCOMING RESPONSES (FINAL FIXED)
    @GetMapping("/responses")
    public List<DonorResponseDTO> getIncomingResponses(Authentication auth) {

        Hospital hospital = getHospital(auth);

        return responseRepository
                .findByBloodRequestHospitalId(hospital.getId())
                .stream()

                // ✅ SHOW ONLY ACCEPTED RESPONSES FROM USERS
                .filter(r -> "ACCEPTED".equalsIgnoreCase(r.getResponse()))

                // ❌ REMOVE REJECTED BY HOSPITAL
                .filter(r -> !"REJECTED".equalsIgnoreCase(r.getStatus()))

                // ❌ REMOVE ALREADY SCHEDULED
                .filter(r -> scheduleRepository
                        .findByBloodRequestId(r.getBloodRequest().getId())
                        .stream()
                        .noneMatch(s -> {

                            if (r.getDonor() != null && s.getDonor() != null) {
                                return s.getDonor().getId().equals(r.getDonor().getId());
                            }

                            return true;
                        })
                )

                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // 📜 3. HISTORY (FULL FIXED WITH ALL NAMES)
    @GetMapping("/history")
    public List<DonationScheduleDTO> getHistory(Authentication auth) {

        Hospital hospital = getHospital(auth);

        return scheduleRepository
                .findByHospitalIdAndStatus(hospital.getId(), "COMPLETED")
                .stream()
                .map(s -> {

                    String responderName = "N/A";

                    if (s.getDonor() != null) {
                        responderName = s.getDonor().getFullName();
                    } else {

                        List<DonorResponse> responses =
                                responseRepository.findByBloodRequestId(
                                        s.getBloodRequest().getId()
                                );

                        for (DonorResponse r : responses) {

                            if ("HOSPITAL".equalsIgnoreCase(r.getSourceType())) {
                                Hospital h = hospitalRepository
                                        .findById(r.getSourceId()).orElse(null);

                                if (h != null) {
                                    responderName = h.getHospitalName();
                                    break;
                                }
                            }

                            if ("BLOOD_BANK".equalsIgnoreCase(r.getSourceType())) {
                                BloodBank b = bloodBankRepository
                                        .findById(r.getSourceId()).orElse(null);

                                if (b != null) {
                                    responderName = b.getBankName();
                                    break;
                                }
                            }
                        }
                    }

                    return new DonationScheduleDTO(
                            s.getId(),
                            responderName,
                            hospital.getHospitalName(),
                            s.getBloodRequest().getPatientName(),
                            s.getBloodRequest().getBloodGroup(),
                            s.getScheduledDate().toString(),
                            "COMPLETED"
                    );
                })
                .collect(Collectors.toList());
    }

    // 🔁 MAPPER
    private DonorResponseDTO mapToDTO(DonorResponse r) {

        String name = "-";
        String blood = "-";
        String location = "-";
        String phone = "-";

        if ("DONOR".equalsIgnoreCase(r.getSourceType()) && r.getDonor() != null) {
            name = r.getDonor().getFullName();
            blood = r.getDonor().getBloodGroup();
            location = r.getDonor().getLocation();
            phone = r.getDonor().getPhone();
        }

        else if ("HOSPITAL".equalsIgnoreCase(r.getSourceType())) {
            Hospital h = hospitalRepository.findById(r.getSourceId()).orElse(null);
            if (h != null) {
                name = h.getHospitalName();
                location = h.getAddress();
                phone = h.getContactNumber();
            }
        }

        else if ("BLOOD_BANK".equalsIgnoreCase(r.getSourceType())) {
            BloodBank b = bloodBankRepository.findById(r.getSourceId()).orElse(null);
            if (b != null) {
                name = b.getBankName();
                location = b.getAddress();
                phone = b.getContactNumber();
            }
        }

        return new DonorResponseDTO(
                r.getId(),
                name,
                blood,
                location,
                phone,
                r.getBloodRequest().getId(),
                r.getBloodRequest().getPatientName(),
                r.getBloodRequest().getHospital().getHospitalName(),
                r.getResponse(),
                r.getStatus(),
                r.getSourceType()
        );
    }
}