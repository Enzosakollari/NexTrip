package com.example.demo.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "business_users")
public class BusinessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // hashed like AppUser.password

    @Column(unique = true, nullable = false)
    private String email;

    private boolean isVerified;
    private String verificationToken;

    @Column(name = "reset_token")
    private String resetToken;

    // business-specific fields
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String contactEmail;

    private boolean approved = false;

    public BusinessUser() {}

    public BusinessUser(String username, String password, String email,
                        String companyName, String contactEmail) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.companyName = companyName;
        this.contactEmail = contactEmail;
    }
}
