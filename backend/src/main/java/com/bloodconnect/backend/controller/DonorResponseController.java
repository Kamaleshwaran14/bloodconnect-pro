package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.DonorResponseDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;
import com.bloodconnect.backend.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/donor-response")
@CrossOrigin
public class DonorResponseController {

    private final DonorResponseRepository responseRepository;
    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final BloodRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DonorResponseController(DonorResponseRepository responseRepository,
                                   DonorRepository donorRepository,
                                   HospitalRepository hospitalRepository,
                                   BloodBankRepository bloodBankRepository,
                                   BloodRequestRepository requestRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService) {
        this.responseRepository = responseRepository;
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getUser(Authentication auth) {
        String username = auth.getName();
        return userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/respond")
    public String respondToRequest(@RequestParam Long requestId,
                                   @RequestParam String action,
                                   Authentication auth) {

        User user = getUser(auth);
        BloodRequest request = requestRepository.findById(requestId).orElse(null);

        if (request == null) return "Invalid request";

        if (!"OPEN".equalsIgnoreCase(request.getStatus())) {
            return "Request already closed";
        }

        if (!action.equalsIgnoreCase("ACCEPTED") &&
                !action.equalsIgnoreCase("DECLINED")) {
            return "Invalid action";
        }

        Donor donor = donorRepository.findByUser(user);
        Hospital hospital = hospitalRepository.findByUser(user);
        BloodBank bank = bloodBankRepository.findByUser(user);

        boolean alreadyResponded = responseRepository
                .findByBloodRequestId(requestId)
                .stream()
                .anyMatch(r ->
                        (donor != null && r.getDonor() != null &&
                                r.getDonor().getId().equals(donor.getId())) ||
                                (hospital != null && "HOSPITAL".equals(r.getSourceType())
                                        && r.getSourceId().equals(hospital.getId())) ||
                                (bank != null && "BLOOD_BANK".equals(r.getSourceType())
                                        && r.getSourceId().equals(bank.getId()))
                );

        if (alreadyResponded) {
            return "You already responded";
        }

        DonorResponse response = new DonorResponse();
        response.setBloodRequest(request);
        response.setResponse(action.toUpperCase());
        response.setStatus("PENDING");
        response.setRespondedAt(LocalDateTime.now());

        String responderName = "";
        String responderContact = "";

        // ✅ FIXED (NO PROXY)
        if (donor != null) {
            response.setDonor(donor);
            response.setSourceType("DONOR");
            response.setSourceId(donor.getId());

            responderName = donor.getFullName();
            responderContact = donor.getPhone();
        } else if (hospital != null) {
            response.setDonor(null);
            response.setSourceType("HOSPITAL");
            response.setSourceId(hospital.getId());

            responderName = hospital.getHospitalName();
            responderContact = hospital.getContactNumber();
        } else if (bank != null) {
            response.setDonor(null);
            response.setSourceType("BLOOD_BANK");
            response.setSourceId(bank.getId());

            responderName = bank.getBankName();
            responderContact = bank.getUser().getPhone();
        }

        responseRepository.save(response);

        if (action.equalsIgnoreCase("ACCEPTED")) {

            String hospitalEmail = request.getHospital().getUser().getEmail();

            String subject = "New Response for Blood Request";

            String message = "A new response has been received.\n\n"
                    + "Responder: " + responderName + "\n"
                    + "Contact: " + responderContact + "\n"
                    + "Blood Group: " + request.getBloodGroup() + "\n"
                    + "Patient: " + request.getPatientName() + "\n"
                    + "Location: " + request.getLocation() + "\n"
                    + "Units Required: " + request.getUnitsRequired() + "\n"
                    + "Action: ACCEPTED\n";

            notificationService.sendEmail(hospitalEmail, subject, message);
        }

        return "Response submitted";
    }

    @PutMapping("/confirm")
    public String confirm(@RequestParam Long responseId, Authentication auth) {

        User user = getUser(auth);

        DonorResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) return "Not found";

        BloodRequest request = response.getBloodRequest();

        if (!request.getHospital().getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        response.setStatus("SELECTED");
        responseRepository.save(response);

        // ✅ FIXED
        if ("DONOR".equalsIgnoreCase(response.getSourceType())) {
            notificationService.notifyAccepted(
                    response.getDonor().getUser(),
                    "Hospital accepted your response"
            );
        }

        return "Selected. Please schedule donation.";
    }

    @PutMapping("/reject")
    public String reject(@RequestParam Long responseId, Authentication auth) {

        User user = getUser(auth);

        DonorResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) return "Not found";

        BloodRequest request = response.getBloodRequest();

        if (!request.getHospital().getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        response.setStatus("REJECTED");
        responseRepository.save(response);

        return "Rejected";
    }


}