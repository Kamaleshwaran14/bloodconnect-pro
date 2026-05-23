package com.bloodconnect.backend.repository;

import com.bloodconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailOrPhone(String email, String phone);

    List<User> findByStatus(String status);

    List<User> findByRole(String role);
    long countByRole(String role);
    long countByStatus(String status);

}