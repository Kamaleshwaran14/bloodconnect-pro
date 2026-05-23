package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // REQUEST / CONFIRMATION

    private String message;

    private LocalDateTime createdAt;

    // 🔹 WHO RECEIVES
    @ManyToOne
    private User user;
}