package com.bloodconnect.backend.dto;

import java.util.Map;

public class AdminUserViewDTO {

    public Long id;
    public String role;
    public String status;

    public String email;
    public String phone;

    public Map<String, Object> details;   // ✅ CLEAN KEY-VALUE DETAILS
    public Map<String, String> documents; // ✅ LABEL -> FILE PATH

    public AdminUserViewDTO(Long id, String role, String status,
                            String email, String phone,
                            Map<String, Object> details,
                            Map<String, String> documents) {

        this.id = id;
        this.role = role;
        this.status = status;
        this.email = email;
        this.phone = phone;
        this.details = details;
        this.documents = documents;
    }
}