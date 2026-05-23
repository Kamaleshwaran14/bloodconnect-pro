package com.bloodconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HospitalProfileDTO {

    private String hospitalName;
    private String licenseNumber;
    private String gstNumber;
    private String address;
}