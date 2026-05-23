package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;
import com.bloodconnect.backend.service.FileStorageService;
import com.bloodconnect.backend.dto.AdminUserViewDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;

import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // ✅ FIX CORS
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private BloodBankRepository bloodBankRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JavaMailSender mailSender;


    // ================= ✅ CLEAN DTO RESPONSE =================
    @GetMapping("/pending-users")
    public List<AdminUserViewDTO> getPendingUsers() {

        List<User> users = userRepository.findByStatus("PENDING");
        List<AdminUserViewDTO> response = new ArrayList<>();

        for (User user : users) {

            Map<String, Object> details = new LinkedHashMap<>();
            Map<String, String> documents = new LinkedHashMap<>();


            // ================= DONOR =================
            if ("DONOR".equals(user.getRole())) {

                Donor d = donorRepository.findByUserId(user.getId());

                if (d != null) {
                    details.put("Full Name", d.getFullName());
                    details.put("Blood Group", d.getBloodGroup());
                    details.put("Location", d.getLocation());
                    details.put("Age", d.getAge());
                    details.put("Pincode", d.getPincode());

                    documents.put("Profile Photo", d.getProfilePhoto());
                    documents.put("Government ID", d.getGovernmentIdProof());
                    documents.put("Blood Certificate", d.getBloodTestCertificate());
                }
            }

            // ================= HOSPITAL =================
            else if ("HOSPITAL".equals(user.getRole())) {

                Hospital h = hospitalRepository.findByUserId(user.getId());

                if (h != null) {
                    details.put("Hospital Name", h.getHospitalName());
                    details.put("Address", h.getAddress());
                    details.put("License Number", h.getLicenseNumber());
                    details.put("GST Number", h.getGstNumber()); // ✅ FIXED
                    details.put("Contact Person ID", h.getAuthorizedPersonIdNumber());
                    details.put("Storage Facility",
                            h.getHasStorageFacility() != null
                                    ? (h.getHasStorageFacility() ? "Yes" : "No")
                                    : "Not Provided"
                    );

                    documents.put("Registration Certificate", h.getRegistrationCertificatePath());
                    documents.put("Authorized Person ID", h.getAuthorizedPersonIdProofPath());
                }
            }

            // ================= BLOOD BANK =================
            else if ("BLOOD_BANK".equals(user.getRole())) {

                BloodBank b = bloodBankRepository.findByUserId(user.getId());

                if (b != null) {
                    details.put("Bank Name", b.getBankName());
                    details.put("Address", b.getAddress());
                    details.put("License Number", b.getLicenseNumber());
                    details.put("GST Number", b.getGstNumber());
                    details.put("Contact Person ID", b.getAuthorizedPersonIdNumber());

                    documents.put("Registration Certificate", b.getRegistrationCertificatePath());
                    documents.put("Authorized Person ID", b.getAuthorizedPersonIdProofPath());
                }
            }

            // ✅ DTO RESPONSE (NO OBJECT OBJECT ISSUE)
            response.add(
                    new AdminUserViewDTO(
                            user.getId(),
                            user.getRole(),
                            user.getStatus(),
                            user.getEmail(),
                            user.getPhone(),
                            details,
                            documents
                    )
            );
        }

        return response;
    }


    // ================= APPROVE =================
    @PutMapping("/approve/{id}")
    public String approveUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "User not found";

        user.setStatus("APPROVED");
        userRepository.save(user);

        sendApprovalEmail(user);

        return "User approved successfully";
    }


    // ================= REJECT =================
    @PutMapping("/reject/{id}")
    public String rejectUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "User not found";

        String role = user.getRole();

        if ("DONOR".equals(role)) {

            Donor donor = donorRepository.findByUserId(id);

            if (donor != null) {
                fileStorageService.deleteUserFolder("donors", id, donor.getFullName());
                donorRepository.delete(donor);
            }

        } else if ("HOSPITAL".equals(role)) {

            Hospital hospital = hospitalRepository.findByUserId(id);

            if (hospital != null) {
                fileStorageService.deleteUserFolder("hospitals", id, hospital.getHospitalName());
                hospitalRepository.delete(hospital);
            }

        } else if ("BLOOD_BANK".equals(role)) {

            BloodBank bank = bloodBankRepository.findByUserId(id);

            if (bank != null) {
                fileStorageService.deleteUserFolder("bloodbanks", id, bank.getBankName());
                bloodBankRepository.delete(bank);
            }
        }

        sendRejectionEmail(user);
        userRepository.delete(user);

        return "User rejected and removed completely";
    }


    // ================= FILE VIEW =================
    @GetMapping("/file")
    public ResponseEntity<Resource> getFile(@RequestParam String path) throws Exception {

        Resource file = fileStorageService.getFile(path);

        String contentType = Files.probeContentType(file.getFile().toPath());

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(contentType))
                .body(file);
    }

    // ================= DASHBOARD STATS =================
    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        long donors = userRepository.countByRole("DONOR");
        long hospitals = userRepository.countByRole("HOSPITAL");
        long bloodBanks = userRepository.countByRole("BLOOD_BANK");
        long pending = userRepository.countByStatus("PENDING");

        return Map.of(
                "donors", donors,
                "hospitals", hospitals,
                "bloodBanks", bloodBanks,
                "pendingApprovals", pending
        );
    }

    // ================= GET USERS BY ROLE (FULL DATA) =================
    @GetMapping("/users/{role}")
    public List<AdminUserViewDTO> getUsersByRole(@PathVariable String role) {

        List<User> users = userRepository.findByRole(role);
        List<AdminUserViewDTO> response = new ArrayList<>();

        for (User user : users) {

            Map<String, Object> details = new LinkedHashMap<>();
            Map<String, String> documents = new LinkedHashMap<>();

            // 🔥 DONOR
            if ("DONOR".equals(role)) {

                Donor d = donorRepository.findByUserId(user.getId());

                if (d != null) {
                    details.put("Full Name", d.getFullName());
                    details.put("Blood Group", d.getBloodGroup());
                    details.put("Location", d.getLocation());
                    details.put("Age", d.getAge());
                    details.put("Pincode", d.getPincode());

                    documents.put("Profile Photo", d.getProfilePhoto());
                    documents.put("Government ID", d.getGovernmentIdProof());
                    documents.put("Blood Certificate", d.getBloodTestCertificate());
                }
            }

            // 🔥 HOSPITAL
            else if ("HOSPITAL".equals(role)) {

                Hospital h = hospitalRepository.findByUserId(user.getId());

                if (h != null) {
                    details.put("Hospital Name", h.getHospitalName());
                    details.put("Address", h.getAddress());
                    details.put("License Number", h.getLicenseNumber());
                    details.put("GST Number", h.getGstNumber());
                    details.put("Contact Person ID", h.getAuthorizedPersonIdNumber());

                    documents.put("Registration Certificate", h.getRegistrationCertificatePath());
                    documents.put("Authorized Person ID", h.getAuthorizedPersonIdProofPath());
                }
            }

            // 🔥 BLOOD BANK
            else if ("BLOOD_BANK".equals(role)) {

                BloodBank b = bloodBankRepository.findByUserId(user.getId());

                if (b != null) {
                    details.put("Bank Name", b.getBankName());
                    details.put("Address", b.getAddress());
                    details.put("License Number", b.getLicenseNumber());
                    details.put("GST Number", b.getGstNumber());
                    details.put("Contact Person ID", b.getAuthorizedPersonIdNumber());

                    documents.put("Registration Certificate", b.getRegistrationCertificatePath());
                    documents.put("Authorized Person ID", b.getAuthorizedPersonIdProofPath());
                }
            }

            response.add(
                    new AdminUserViewDTO(
                            user.getId(),
                            user.getRole(),
                            user.getStatus(),
                            user.getEmail(),
                            user.getPhone(),
                            details,
                            documents
                    )
            );
        }

        return response;
    }

    // ================= BLOCK USER (SOFT DISABLE LOGIN) =================
    @PutMapping("/block/{id}")
    public String blockUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "User not found";

        user.setStatus("PENDING"); // 🔥 key logic
        userRepository.save(user);

        // ✅ SEND BLOCK EMAIL (NEW)
        sendBlockEmail(user);

        return "User blocked successfully";
    }


    // ================= EMAIL: APPROVAL =================
    @Async
    protected void sendApprovalEmail(User user) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject("BloodConnect Pro – Account Approved");

            String roleMessage = "";

            if ("DONOR".equals(user.getRole())) {
                roleMessage = "As a donor, your willingness to help can make a life-saving difference to patients in need.";
            } else if ("HOSPITAL".equals(user.getRole())) {
                roleMessage = "You can now raise blood requests and manage emergency requirements efficiently through our platform.";
            } else if ("BLOOD_BANK".equals(user.getRole())) {
                roleMessage = "You can now manage blood inventory and respond to urgent blood requests effectively.";
            }

            message.setText(
                    "Dear User,\n\n" +

                            "Greetings from BloodConnect Pro.\n\n" +

                            "We are pleased to inform you that your registration has been successfully reviewed and approved by our administration team.\n\n" +

                            "🔐 Account Details:\n" +
                            "Email: " + user.getEmail() + "\n" +
                            "Status: Active\n\n" +

                            roleMessage + "\n\n" +

                            "You can now log in and start using the platform.\n\n" +

                            "If you require any assistance, please feel free to contact our support team.\n\n" +

                            "Warm regards,\n" +
                            "BloodConnect Pro Team\n" +
                            "\"Connecting Lives, Saving Lives\""
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Approval email failed: " + e.getMessage());
        }
    }


    // ================= EMAIL: REJECTION =================
    @Async
    protected void sendRejectionEmail(User user) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject("BloodConnect Pro – Registration Update");

            message.setText(
                    "Dear User,\n\n" +

                            "Greetings from BloodConnect Pro.\n\n" +

                            "Thank you for your interest in joining our platform.\n\n" +

                            "After careful review, we regret to inform you that your registration request could not be approved at this time.\n\n" +

                            "Possible reasons may include:\n" +
                            "- Incomplete information\n" +
                            "- Invalid or unclear documents\n" +
                            "- Verification issues\n\n" +

                            "We encourage you to review your details and register again with accurate and complete information.\n\n" +

                            "If you believe this decision was made in error or need clarification, please contact our support team.\n\n" +

                            "Thank you for your understanding.\n\n" +

                            "Warm regards,\n" +
                            "BloodConnect Pro Team\n" +
                            "\"Connecting Lives, Saving Lives\""
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Rejection email failed: " + e.getMessage());
        }
    }

    @Async
    protected void sendBlockEmail(User user) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject("BloodConnect Pro – Account Temporarily Blocked");

            message.setText(
                    "Dear User,\n\n" +

                            "Greetings from BloodConnect Pro.\n\n" +

                            "We would like to inform you that your account has been temporarily restricted by the administration team.\n\n" +

                            "🔐 Account Status: Temporarily Blocked\n\n" +

                            "This action may have been taken due to verification concerns, policy compliance checks, or administrative review requirements.\n\n" +

                            "👉 What you can do next:\n" +
                            "- Please contact our support team for clarification\n" +
                            "- Provide any required information or documents if requested\n\n" +

                            "📧 Support Contact: bloodconnectpro@gmail.com\n\n" +

                            "We are committed to maintaining a secure and trustworthy platform for all users.\n\n" +

                            "Thank you for your understanding and cooperation.\n\n" +

                            "Warm regards,\n" +
                            "BloodConnect Pro Team\n" +
                            "\"Connecting Lives, Saving Lives\""
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Block email failed: " + e.getMessage());
        }
    }
}