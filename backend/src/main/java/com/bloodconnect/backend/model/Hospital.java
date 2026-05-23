package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hospitalName;

    private String address;

    private String licenseNumber;

    private String gstNumber;

    private String authorizedPersonIdNumber;

    private String contactNumber;

    // whether hospital has blood storage facility
    private Boolean hasStorageFacility;

    // document file paths
    private String registrationCertificatePath;

    private String authorizedPersonIdProofPath;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

}