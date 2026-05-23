package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 RELATIONS
    @ManyToOne
    @JoinColumn(name = "donor_id") // ✅ ADD THIS
    private Donor donor;

    @ManyToOne
    @JoinColumn(name = "hospital_id") // ✅ ADD THIS
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "blood_request_id") // ✅ VERY IMPORTANT FIX
    private BloodRequest bloodRequest;

    // 🔹 SCHEDULE DETAILS
    private LocalDateTime scheduledDate;

    private String status; // SCHEDULED / COMPLETED
}