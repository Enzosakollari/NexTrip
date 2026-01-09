package com.example.demo.Service;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessUserService implements UserDetailsService {

    private final BusinessUserRepository businessRepo;
    private final TravelPackageRepository packRepo;

    // AUTH
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BusinessUser u = businessRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Business not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                List.of()
        );
    }

    // HELPERS
    public BusinessUser currentBusiness(Principal principal) {
        if (principal == null) return null;
        return businessRepo.findByUsername(principal.getName()).orElse(null);
    }

    // PACKS
    public List<TravelPackage> getMyPackages(Long businessId) {
        // ideally repository method: findByBusinessUserId(businessId)
        return packRepo.findByBusinessUserId(businessId);
    }

    public TravelPackage getPackageForBusiness(Long businessId, Long packId) {
        TravelPackage p = packRepo.findById(packId)
                .orElseThrow(() -> new RuntimeException("Pack not found"));

        if (p.getBusinessUser() == null || !p.getBusinessUser().getId().equals(businessId)) {
            throw new RuntimeException("Not allowed");
        }

        return p;
    }

    public void createPackage(Long businessId, TravelPackage pack) {
        BusinessUser business = businessRepo.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // optional: block until approved
        // if (!business.isApproved()) throw new RuntimeException("Not approved");

        pack.setId(null);
        pack.setBusinessUser(business);
        packRepo.save(pack);
    }

    public void updatePackage(Long businessId, Long packId, TravelPackage incoming) {
        TravelPackage existing = getPackageForBusiness(businessId, packId);

        existing.setTitle(incoming.getTitle());
        existing.setDestination(incoming.getDestination());
        existing.setDurationDays(incoming.getDurationDays());
        existing.setDescription(incoming.getDescription());
        existing.setPrice(incoming.getPrice());
        existing.setCurrency(incoming.getCurrency());

        packRepo.save(existing);
    }

    public void deletePackage(Long businessId, Long packId) {
        TravelPackage existing = getPackageForBusiness(businessId, packId);
        packRepo.delete(existing);
    }
}
