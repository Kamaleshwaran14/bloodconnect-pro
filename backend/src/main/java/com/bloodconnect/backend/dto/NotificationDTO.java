package com.bloodconnect.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private String type; // REQUEST / CONFIRMATION
    private String message;
    private String time;
}