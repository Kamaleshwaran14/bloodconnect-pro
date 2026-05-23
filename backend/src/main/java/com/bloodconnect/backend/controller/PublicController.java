package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.PublicStatsDTO;
import com.bloodconnect.backend.repository.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin
public class PublicController {

    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DonorRepository donorRepository;
    private final DonorResponseRepository responseRepository;

    public PublicController(
            HospitalRepository hospitalRepository,
            BloodBankRepository bloodBankRepository,
            DonorRepository donorRepository,
            DonorResponseRepository responseRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.donorRepository = donorRepository;
        this.responseRepository = responseRepository;
    }

    @GetMapping("/stats")
    public PublicStatsDTO getStats() {

        long hospitals = hospitalRepository.count();
        long bloodBanks = bloodBankRepository.count();
        long donors = donorRepository.count();
        long responses = responseRepository.count();

        return new PublicStatsDTO(
                hospitals,
                bloodBanks,
                donors,
                responses
        );
    }
}