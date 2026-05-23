package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloodInventoryDTO {

    private Long id;
    private String group;
    private int units;
}