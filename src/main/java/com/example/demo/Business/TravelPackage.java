package com.example.demo.Business;

import com.example.demo.User.BuissnessAppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String destination;
    private int durationDays;

    @Column(length = 2000)
    private String description;

    private BigDecimal price;
    private String currency;

    @ManyToOne
    @JoinColumn(name = "business_user_id")
    private BuissnessAppUser businessUser;

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BuissnessAppUser getBusinessUser() {
        return businessUser;
    }

    public void setBusinessUser(BuissnessAppUser businessUser) {
        this.businessUser = businessUser;
    }
}
