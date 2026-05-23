package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloodBankProfileDTO {

    private String name;
    private String license;
    private String location;
    private String contact;
    private boolean verified;
}