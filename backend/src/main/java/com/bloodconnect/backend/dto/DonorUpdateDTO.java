package com.bloodconnect.backend.dto;

import lombok.Data;

@Data
public class DonorUpdateDTO {

    private String fullName;
    private String location;
    private String phone;
    private String age;
    private String pincode;

    private boolean emailVerified; // 🔥 VERY IMPORTANT

    private UserDTO user;
}