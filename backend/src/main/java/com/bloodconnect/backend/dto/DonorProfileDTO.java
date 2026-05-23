package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonorProfileDTO {

    private String fullName;
    private String bloodGroup;
    private String location;
    private String phone;
    private String age;
    private String pincode;

    private boolean available;

    // 🔐 user safe data only
    private String email;
}