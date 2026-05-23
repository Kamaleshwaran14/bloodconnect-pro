package com.bloodconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDTO {

    private String type; // DONOR / HOSPITAL / BLOOD_BANK
    private Long id;
    private String name;
    private String bloodGroup;
    private String location;
    private String contact;
    private boolean available;
}