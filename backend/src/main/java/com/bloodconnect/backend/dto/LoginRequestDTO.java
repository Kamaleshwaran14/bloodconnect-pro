package com.bloodconnect.backend.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {

    private String emailOrPhone;
    private String password;

}