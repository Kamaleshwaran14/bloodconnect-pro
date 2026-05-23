package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="blood_banks")
public class BloodBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;

    private String address;

    private String licenseNumber;

    private String gstNumber;

    private String authorizedPersonIdNumber;

    private String contactNumber;

    // document file paths
    private String registrationCertificatePath;

    private String authorizedPersonIdProofPath;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}