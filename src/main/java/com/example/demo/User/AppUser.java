package com.example.demo.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public AppUser() {}

    public AppUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
