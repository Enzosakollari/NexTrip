package com.example.demo.Service;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.User.BuissnessAppUser;
import com.example.demo.User.BuissnessAppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuissnessAppUserService {

    private final BuissnessAppUserRepository buissnessRepo;
    private final AppUserRepository appUserRepo;
    private final TravelPackageRepository packageRepo;

    public BuissnessAppUserService(
            BuissnessAppUserRepository buissnessRepo,
            AppUserRepository appUserRepo,
            TravelPackageRepository packageRepo
    ) {
        this.buissnessRepo = buissnessRepo;
        this.appUserRepo = appUserRepo;
        this.packageRepo = packageRepo;
    }

    public BuissnessAppUser registerBuissnessAppUser(Long appUserId, String companyName, String contactEmail) {
        AppUser user = appUserRepo.findById(appUserId)
                .orElseThrow(() -> new RuntimeException("AppUser not found: " + appUserId));

        buissnessRepo.findByAppUserId(appUserId).ifPresent(existing -> {
            throw new RuntimeException("Business profile already exists for this user.");
        });

        BuissnessAppUser bu = new BuissnessAppUser();
        bu.setAppUser(user);
        bu.setCompanyName(companyName);
        bu.setContactEmail(contactEmail);
        bu.setApproved(false);

        return buissnessRepo.save(bu);
    }


    public TravelPackage createPackage(Long buissnessAppUserId, TravelPackage travelPackage) {
        BuissnessAppUser bu = buissnessRepo.findById(buissnessAppUserId)
                .orElseThrow(() -> new RuntimeException("BuissnessAppUser not found: " + buissnessAppUserId));

        if (!bu.isApproved()) {
            throw new RuntimeException("Business user is not approved yet.");
        }

        travelPackage.setBusinessUser(bu);
        return packageRepo.save(travelPackage);
    }

    public List<TravelPackage> getMyPackages(Long buissnessAppUserId) {
        return packageRepo.findByBusinessUserId(buissnessAppUserId);
    }


}
