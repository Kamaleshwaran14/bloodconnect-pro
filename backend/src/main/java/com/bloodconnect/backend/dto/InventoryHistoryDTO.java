package com.bloodconnect.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryHistoryDTO {

    private Long id;
    private String bloodGroup;
    private Integer units;
    private String actionType;
    private LocalDateTime time;
    private String reference;
}