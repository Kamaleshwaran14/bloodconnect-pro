package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {

    private Long id;
    private String bloodGroup;
    private Integer unitsAvailable;

    private String ownerName; // Hospital or BloodBank name
    private String type;      // HOSPITAL / BLOOD_BANK
}