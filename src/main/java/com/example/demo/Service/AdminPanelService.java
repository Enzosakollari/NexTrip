package com.example.demo.Service;

import com.example.demo.Booking.Booking;
import com.example.demo.Booking.BookingRepository;
import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPanelService {

    private final AppUserRepository appUserRepository;
    private final BusinessUserRepository businessUserRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final BookingRepository bookingRepository;

    public long usersCount() { return appUserRepository.count(); }
    public long businessesCount() { return businessUserRepository.count(); }
    public long packsCount() { return travelPackageRepository.count(); }
    public long bookingsCount() { return bookingRepository.count(); }

    public List<AppUser> allUsers() {
        return appUserRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<BusinessUser> allBusinesses() {
        return businessUserRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<TravelPackage> allPacks() {
        return travelPackageRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<Booking> allBookings() {
        // if you have createdAt in Booking, use it:
        try {
            return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } catch (Exception ignored) {
            return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }

    @Transactional
    public void deleteBusiness(Long id) {
        businessUserRepository.deleteById(id);
    }

    @Transactional
    public void deletePack(Long id) {
        travelPackageRepository.deleteById(id);
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
}
