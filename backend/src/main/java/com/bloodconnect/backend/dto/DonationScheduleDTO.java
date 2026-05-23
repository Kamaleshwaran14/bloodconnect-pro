package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationScheduleDTO {

    private Long id;

    private String donorName;
    private String hospitalName;

    private String patientName;
    private String bloodGroup;

    private String scheduledDate;
    private String status;
}