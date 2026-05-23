package com.bloodconnect.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DonorDashboardDTO {

    private DonorProfileDTO profile;
    private boolean available;
    private List<NotificationDTO> notifications;

    private int totalDonations;
    private String lastDonation;
    private String nextEligibleDate;

    public DonorDashboardDTO(DonorProfileDTO profile,
                             boolean available,
                             List<NotificationDTO> notifications,
                             int totalDonations,
                             String lastDonation,
                             String nextEligibleDate) {
        this.profile = profile;
        this.available = available;
        this.notifications = notifications;
        this.totalDonations = totalDonations;
        this.lastDonation = lastDonation;
        this.nextEligibleDate = nextEligibleDate;
    }
}