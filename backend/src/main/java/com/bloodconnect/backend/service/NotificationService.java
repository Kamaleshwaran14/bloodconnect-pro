package com.bloodconnect.backend.service;

import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DonorRepository donorRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository,
                               DonorRepository donorRepository,
                               BloodInventoryRepository inventoryRepository,
                               JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.donorRepository = donorRepository;
        this.inventoryRepository = inventoryRepository;
        this.mailSender = mailSender;
    }

    // ✅ EXISTING (UNCHANGED LOGIC)
    public void notifyAllUsers(BloodRequest request) {

        String message = buildEmailContent(request);

        // 🔴 DONORS
        List<Donor> donors =
                donorRepository.findByBloodGroupAndLocationContainingIgnoreCaseAndAvailableTrue(
                        request.getBloodGroup(),
                        request.getLocation()
                );

        for (Donor donor : donors) {
            if (donor.getUser() != null) {
                saveNotification(donor.getUser(), "REQUEST", message);
                sendEmail(donor.getUser().getEmail(), message);
            }
        }

        // 🔴 HOSPITAL + BLOOD BANK (WITH LOCATION FILTER)
        List<BloodInventory> inventories =
                inventoryRepository.findByBloodGroupAndUnitsAvailableGreaterThan(
                        request.getBloodGroup(), 0
                );

        for (BloodInventory inv : inventories) {

            // ✅ HOSPITAL FILTER (WITH LOCATION)
            if (inv.getHospital() != null &&
                    Boolean.TRUE.equals(inv.getHospital().getHasStorageFacility()) &&
                    inv.getHospital().getAddress() != null &&
                    inv.getHospital().getAddress().toLowerCase()
                            .contains(request.getLocation().toLowerCase())) {

                Hospital h = inv.getHospital();
                if (h.getUser() != null) {
                    saveNotification(h.getUser(), "REQUEST", message);
                    sendEmail(h.getUser().getEmail(), message);
                }
            }

            // ✅ BLOOD BANK FILTER (WITH LOCATION)
            if (inv.getBloodBank() != null &&
                    inv.getBloodBank().getAddress() != null &&
                    inv.getBloodBank().getAddress().toLowerCase()
                            .contains(request.getLocation().toLowerCase())) {

                BloodBank b = inv.getBloodBank();
                if (b.getUser() != null) {
                    saveNotification(b.getUser(), "REQUEST", message);
                    sendEmail(b.getUser().getEmail(), message);
                }
            }
        }
    }

    private String buildEmailContent(BloodRequest request) {

        return "🚨 BLOOD EMERGENCY ALERT 🚨\n\n" +
                "Patient Name: " + request.getPatientName() + "\n" +
                "Blood Group: " + request.getBloodGroup() + "\n" +
                "Units Required: " + request.getUnitsRequired() + "\n" +
                "Location: " + request.getLocation() + "\n" +
                "Hospital: " + request.getHospital().getHospitalName() + "\n" +
                "Contact: " + request.getContactNumber() + "\n" +
                "Urgency: " + request.getUrgencyLevel() + "\n\n" +
                "👉 Please login and respond immediately.\n";
    }

    // ✅ EXISTING (SMALL SAFETY ADDED)
    private void saveNotification(User user, String type, String message) {

        if (user == null) return;

        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(n);
    }

    // ✅ EXISTING METHOD (UNCHANGED)
    private void sendEmail(String toEmail, String messageText) {

        try {
            if (toEmail == null || toEmail.isEmpty()) return;

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(toEmail);
            mail.setSubject("🚨 Blood Request Emergency");
            mail.setText(messageText);

            mailSender.send(mail);

        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    // ✅ EXISTING METHOD (UNCHANGED)
    public void sendEmail(String toEmail, String subject, String messageText) {

        try {
            if (toEmail == null || toEmail.isEmpty()) return;

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(messageText);

            mailSender.send(mail);

        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }


    // 🔥 NEW (REQUIRED — ACCEPTED)
    public void notifyAccepted(User user, String message) {
        saveNotification(user, "ACCEPTED", message);
    }


}