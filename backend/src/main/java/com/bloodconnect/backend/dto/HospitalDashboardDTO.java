package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HospitalDashboardDTO {

    private long activeRequests;
    private long totalResponses;
    private long successfulDonations;
}