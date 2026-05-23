package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.model.Donor;
import com.bloodconnect.backend.model.User;
import com.bloodconnect.backend.repository.DonorRepository;
import com.bloodconnect.backend.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donor")
@CrossOrigin
public class DonorController {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;

    public DonorController(DonorRepository donorRepository,
                           UserRepository userRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
    }

    // ✅ COMMON METHOD (FIXED)
    private User getLoggedInUser(Authentication authentication) {

        String username;

        if (authentication.getPrincipal() instanceof User) {
            User u = (User) authentication.getPrincipal();
            username = (u.getEmail() != null) ? u.getEmail() : u.getPhone();
        } else {
            username = authentication.getName();
        }

        return userRepository
                .findByEmailOrPhone(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ UPDATE AVAILABILITY (USED IN FRONTEND)
    @PutMapping("/availability")
    public String updateAvailability(@RequestParam boolean status,
                                     Authentication authentication) {

        User user = getLoggedInUser(authentication);

        Donor donor = donorRepository.findByUser(user);

        if (donor == null) {
            throw new RuntimeException("Donor not found");
        }

        donor.setAvailable(status);

        donorRepository.save(donor);

        return "Availability updated successfully";
    }
}