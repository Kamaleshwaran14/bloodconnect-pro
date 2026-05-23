package com.bloodconnect.backend.dto;

public class PublicStatsDTO {

    private long hospitals;
    private long bloodBanks;
    private long donors;
    private long responses;

    public PublicStatsDTO(long hospitals, long bloodBanks, long donors, long responses) {
        this.hospitals = hospitals;
        this.bloodBanks = bloodBanks;
        this.donors = donors;
        this.responses = responses;
    }

    public long getHospitals() { return hospitals; }
    public long getBloodBanks() { return bloodBanks; }
    public long getDonors() { return donors; }
    public long getResponses() { return responses; }
}