package com.bloodconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String bloodGroup;

    private String location;

    private String phone;

    private String age;

    private String pincode;

    private String profilePhoto;

    private String bloodTestCertificate;

    private String governmentIdProof;

    private boolean available = true;

    private Integer totalDonations = 0;

    private String lastDonationDate;

    private String nextEligibleDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}