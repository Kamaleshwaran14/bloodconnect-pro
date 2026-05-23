package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonorResponseDTO {

    private Long responseId;

    private String donorName;
    private String bloodGroup;
    private String location;
    private String phone;

    private Long requestId;
    private String patientName;
    private String hospitalName;

    private String response;
    private String status;

    // 🔥 NEW
    private String sourceType;
}