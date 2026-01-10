package com.example.demo.Booking;

import com.example.demo.Business.TravelPackage;
import com.example.demo.User.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private TravelPackage travelPackage;

    @ManyToOne
    private AppUser appUser;

    private String fullName;
    private String email;
    private String phone;

    private int travelersCount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        status = BookingStatus.PENDING;
    }
}
