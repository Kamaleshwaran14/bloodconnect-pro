package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 PATIENT DETAILS (NEW)
    private String patientName;
    private Integer patientAge;
    private String contactNumber;

    // 🔹 BLOOD DETAILS
    private String bloodGroup;
    private Integer unitsRequired;
    private String location;
    private String urgencyLevel; // NORMAL / EMERGENCY

    private String status = "OPEN";

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
}