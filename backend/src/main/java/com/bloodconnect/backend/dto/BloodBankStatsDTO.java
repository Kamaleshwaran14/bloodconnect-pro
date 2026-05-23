package com.bloodconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BloodBankStatsDTO {

    private int totalUnits;
    private long successfulDonations;
    private String lastDonationDate;
}