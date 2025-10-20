package com.example.demo.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    private boolean isVerified;
    private String verificationToken;


    @Column(name="reset_token")
    private String resetToken;

    // Add constructors, setters if using Lombok @Data instead of @Getter
    public AppUser() {}

    public AppUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
