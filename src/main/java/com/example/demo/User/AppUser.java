package com.example.demo.User;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users") // Explicit table name to avoid reserved keyword issues
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Changed for MySQL auto-increment
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    // Add constructors, setters if using Lombok @Data instead of @Getter
    public AppUser() {}

    public AppUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}