package com.example.demo.Booking;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final AppUserRepository appUserRepository;

    @Transactional
    public void createBooking(Long packId, String fullName, String email, String phone, int travelersCount, Principal principal) {

        var pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));

        var user = appUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = new Booking();
        booking.setTravelPackage(pack);
        booking.setAppUser(user);
        booking.setFullName(fullName);
        booking.setEmail(email);
        booking.setPhone(phone);
        booking.setTravelersCount(travelersCount);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    } @Transactional
    public List<Booking> businessBookings(Long businessId) {
        return bookingRepository.findAllByBusinessId(businessId);
    }

    @Transactional
    public void updateStatus(Long bookingId, Long businessId, BookingStatus status) {
        Booking booking = bookingRepository.findByIdAndBusinessId(bookingId, businessId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found or not yours"));

        booking.setStatus(status);
        bookingRepository.save(booking);
    }
}
