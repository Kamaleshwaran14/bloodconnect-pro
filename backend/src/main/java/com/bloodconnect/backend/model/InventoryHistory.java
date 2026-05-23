package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bloodGroup;
    private Integer units;

    // 🔹 ACTION TYPE
    // ADDED / USED / RECEIVED
    private String actionType;

    private LocalDateTime time;

    // 🔹 Reference (optional)
    private String reference; // e.g. "Request #1", "Donor John"

    // 🔹 Owner
    @ManyToOne
    private Hospital hospital;

    @ManyToOne
    private BloodBank bloodBank;
}