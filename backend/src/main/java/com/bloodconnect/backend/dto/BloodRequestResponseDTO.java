package com.bloodconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloodRequestResponseDTO {

    private Long id;

    // PATIENT DETAILS
    private String patientName;
    private Integer patientAge;
    private String contactNumber;

    // REQUEST DETAILS
    private String bloodGroup;
    private Integer unitsRequired;
    private String location;
    private String urgencyLevel;
    private String status;

    // HOSPITAL
    private String hospitalName;
}