package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "donor_responses")
public class DonorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 RELATIONS
    @ManyToOne
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private BloodRequest bloodRequest;

    // 🔥 NEW (WHO IS PROVIDING)
    private String sourceType; // DONOR / HOSPITAL / BLOOD_BANK

    private Long sourceId; // donorId / hospitalId / bankId

    // 🔹 RESPONSE
    private String response;

    private String status = "PENDING";

    private LocalDateTime respondedAt = LocalDateTime.now();
}