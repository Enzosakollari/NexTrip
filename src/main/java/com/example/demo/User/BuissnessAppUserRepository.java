package com.example.demo.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuissnessAppUserRepository extends JpaRepository<BuissnessAppUser, Long> {
    Optional<BuissnessAppUser> findByAppUserId(Long appUserId);
}
